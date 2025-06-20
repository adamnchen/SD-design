package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.Forest;
import com.rabbitmq.client.Channel;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.sdapi.domain.dto.train.*;
import com.sutran.sd.sdapi.domain.vo.TrainProcessDataVo;
import com.sutran.sd.sdapi.domain.vo.TrainProcessTaskVo;
import com.sutran.sd.sdapi.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.sdapi.domain.vo.TrainTaskVo;
import com.sutran.sd.sdapi.events.RefreshLoraEvent;
import com.sutran.sd.sdapi.modules.system.SdCommonConfigService;
import com.sutran.sd.sdapi.modules.system.SdGpuPoolService;
import com.sutran.sd.sdapi.modules.system.SdTrainService;
import com.sutran.sd.sdapi.modules.system.SdTrainPreTaskService;
import com.sutran.sd.sdapi.modules.system.entity.SdCommonConfig;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
import com.sutran.sd.sdapi.modules.system.entity.SdTrainTask;
import com.sutran.sd.system.service.ISysDictDataService;
import com.sutran.sd.system.service.SysTranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sutran.sd.common.constant.CacheConstants.*;
import static com.sutran.sd.sdapi.mq.MqConstant.*;

/**
 * @author zj
 * @date 2024-03-24
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "AlibabaLowerCamelCaseVariableNaming"})
@Slf4j
@Service("SdTrainApiService")
@RequiredArgsConstructor
public class SdTrainServiceImpl implements SdTrainService {

    private final SdTrainPreTaskService sdTrainPreTaskService;
    private final RabbitTemplate rabbitTemplate;
    private final SysTranslateService sysTranslateService;
    private final SdGpuPoolService sdGpuPoolService;
    private final static Lock TRAIN_LOCK = new ReentrantLock();
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ISysDictDataService sysDictDataService;
    private final SdCommonConfigService sdCommonConfigService;
    private final UserService userService;

    @Resource(name = "threadPoolTaskExecutor")
    private Executor executor;
    private final static Map<String,WatchMonitor> MONITOR_MAP = new ConcurrentHashMap<>();

    /**
     * 预处理图片任务状态
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @param userId 登录人id
     * @return 任务集合
     */
    @Override
    public List<TrainTaskVo> getTrainTasks(Integer newStatus, Long userId) {
        List<SdTrainTask> list = sdTrainPreTaskService.selectByUserId(userId,newStatus);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e->{
            TrainTaskVo vo = new TrainTaskVo();
            BeanUtils.copyProperties(e,vo);
            if (e.getPreParams()!=null) {
                JSONObject preParams = JSONObject.parseObject(String.valueOf(e.getPreParams()));
                vo.setPreTaskParams(preParams);
            }
            if (e.getTrainParams()!=null) {
                JSONObject trainParams = JSONObject.parseObject(String.valueOf(e.getTrainParams()));
                vo.setTrainTaskParams(trainParams);
            }
            if (e.getAdditionTag()!=null) {
                List<String> additionTag = JSON.parseArray(String.valueOf(e.getAdditionTag()), String.class);
                vo.setAdditionTag(additionTag);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * SD-获取当前任务状态
     * @param preTaskId 预处理任务id
     * @return 任务状态
     */
    @Override
    public TrainTaskStatusVo getSdTaskStatus(String preTaskId) {
        TrainTaskStatusVo data = sdTrainPreTaskService.selectTaskStatusByPreTaskId(preTaskId);
        if (data==null) {
            throw new ServiceException("训练任务不存在!");
        }
        return data;
    }

    /**
     * 定时任务-获取当前任务状态
     * @param preTaskId 预处理任务id
     * @return  任务状态
     */
    @Override
    public TrainTaskStatusVo getSdTaskStatusOfJob(String preTaskId) {
        return sdTrainPreTaskService.selectTaskStatusByPreTaskId(preTaskId);
    }


    /*
     * 目录挂载情况：系统容器(/train-data/*) -> 宿主机(/home/train/train-data/*) -> 训练器容器(/lora-scripts/train-data)
     * 目录挂载情况：系统容器(/output/*) -> 宿主机(/home/train/out/*) -> 训练器容器(/lora-scripts/output)
     * 目录挂载情况：系统容器(/models/*) -> 宿主机(/home/sd/data/models/Lora/*) -> SD容器(/stable-diffusion-webui/models/Lora/*)
     */

    /* 预处理任务 start */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitPreImg(MultipartFile[] images, Double threshold, String interrogatorModel, String batchOutputActionOnConflict) throws IOException {
        Long userId = LoginHelper.getUserId();
        // 获取当前用户的剩余训练次数
        Integer trainTimes = userService.selectTrainTimesById(userId);
        if (trainTimes!=null && trainTimes<=0) {
            throw new ServiceException("暂无可用训练次数,请联系管理员!");
        }
        SdCommonConfig config = sdCommonConfigService.selectOne();
        int canMoreSubmitPreImgNum = config==null||config.getPreImgMaxNum()==null||config.getPreImgMaxNum()<=0?50:config.getPreImgMaxNum();
        int canLeastSubmitPreImgNum = config==null||config.getPreImgMinNum()==null||config.getPreImgMinNum()<=0?7:config.getPreImgMinNum();
        if(images==null || images.length==0){
            throw new ServiceException("训练数据不能为空!");
        }
        else if (images.length<canLeastSubmitPreImgNum) {
            throw new ServiceException("预处理图最少要提交"+canLeastSubmitPreImgNum+"张!");
        }
        else if (images.length>canMoreSubmitPreImgNum) {
            throw new ServiceException("预处理图最多能提交"+canMoreSubmitPreImgNum+"张!");
        }
        // 先判断是否已存在模型训练任务
        SdTrainTask task = sdTrainPreTaskService.selectDetailByUserId(userId);
        if (task!=null && task.getAdditionTag()==null) {
            throw new ServiceException("请至少填入一个共性词[缺少共性词]!");
        }
        // 不存在训练任务 或者 已完成 或者 已失败
        else if (task==null || task.getNewStatus()==5 || task.getNewStatus()==6) {
            String username = LoginHelper.getUsername();
            String taskId = IdUtil.getSnowflakeNextIdStr();
            // 系统服务器的容器中创建上传的图片数据集，例如现在的系统容器会在根目录下创建 train-data目录
            // 目录挂载情况：系统容器(/train-data/*) -> 宿主机(/home/train/train-data/*) -> 训练器容器(/lora-scripts/train-data)
            // 将图片保存到指定的文件夹下；如果父文件夹不存在，就创建
            String parentFileUrl = "/train-data/"+DateUtil.formatDate(new Date())+"/"+userId+"/"+taskId;
            File parent = new File(parentFileUrl);
            if (!parent.exists()) {
                parent.mkdirs();
            }
            int index = 1;
            for (MultipartFile file : images) {
                String originalName = Objects.requireNonNull(file.getOriginalFilename());
                String fileName = originalName.substring(0, originalName.lastIndexOf("."));
                String fileType = originalName.substring(originalName.lastIndexOf(".") + 1);
                File file1 = new File(parent, String.format("%s_%s.%s", fileName, index, fileType.toLowerCase()));
                //将图片保存入服务器
                file.transferTo(file1);
                index++;
            }

            // 调用打标签接口，对图片进行标签处理
            Map<String,Object> params = new HashMap<>();
            params.put("batch_input_recursive",false);
            params.put("batch_output_action_on_conflict", StrUtil.isEmptyIfStr(batchOutputActionOnConflict)?"copy":batchOutputActionOnConflict);
            params.put("escape_tag",true);
            params.put("interrogator_model",StrUtil.isEmptyIfStr(interrogatorModel)?"wd14-convnextv2-v2":interrogatorModel);
            params.put("path","/lora-scripts"+parentFileUrl);
            params.put("replace_underscore",true);
            params.put("threshold",threshold!=null?threshold:0.5d);
            sdTrainPreTaskService.insert(userId, username, params, images.length, taskId);
            params.put("taskId",taskId);
            RedisUtils.setCacheObject(PRE_IMG_PROGRESS_TOTAL+taskId, images.length);
            RedisUtils.setCacheObject(PRE_IMG_PROGRESS_COMPLETE+taskId, 0);
            // 添加到预处理任务map中，便于定时处理预处理图片的任务进度
            RedisUtils.setCacheMapValue(PRE_IMG_PROGRESS_TASK_MAP,taskId,taskId);
            // 创建该数据集文件夹监听任务
            CompletableFuture.runAsync(()->createFileDirMonitor(parent,taskId),executor);
            try {
                rabbitTemplate.convertAndSend(SD_PRE_IMG_TASK_EXCHANGE,SD_PRE_IMG_TASK_ROUTING_KEY,params,new CorrelationData(taskId));
            }
            catch (Exception e) {
                sdTrainPreTaskService.deleteById(taskId);
                delPreTaskData(taskId);
            }
            return taskId;
        }
        else if (task.getNewStatus()==0) {
            throw new ServiceException("当前用户已存在[排队中]的[预处理]任务!");
        }
        else if (task.getNewStatus()==1) {
            throw new ServiceException("当前用户已存在[进行中]的[预处理]任务!");
        }
        else if (task.getNewStatus() == 2) {
            throw new ServiceException("当前用户已存在[未开始]的[训练]任务!");
        }
        else if (task.getNewStatus() == 3) {
            throw new ServiceException("当前用户已存在[队列中]的[训练]任务!");
        }
        else {
            throw new ServiceException("当前用户已存在[进行中]的[训练]任务!");
        }
    }
    @RabbitListener(queues = SD_PRE_IMG_TASK_QUEUE)
    public void preImgTask(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Map<String,Object> params = JSON.parseObject(body, Map.class);
        String preTaskId = String.valueOf(params.get("taskId"));

        // 随机获取一个训练GPU卡
        SdGpuPool sdGpuPool = sdGpuPoolService.selectRandomEnableOfOneGpu(1);
        if (sdGpuPool==null) {
            log.error("图片预处理>>>>>>>>>训练卡池中暂无可使用的GPU服务,请等待!");
            //TODO 发送消息提醒管理人员
            return;
        }
        // 数据集图片目录：path -> /lora-scripts/train-data/{userId}/{preTaskId}
        try{
            // 通过gpu卡池中的训练数据集目录
            log.warn("图片预处理>>>>>>>>>任务ID[{}],开始进行预处理图片", preTaskId);
            Forest.post("/api/interrogate").address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().addBody(params).execute();
            // 修改队列状态 和 gpu使用池
            sdTrainPreTaskService.modifyNewStatusById(String.valueOf(preTaskId),1, null, null);
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            log.error("图片预处理>>>>>>>>>任务ID[{}],预处理图片异常,重新进入队列：{}", preTaskId,e.getMessage());
            channel.basicNack(deliveryTag, false, true);
        }
    }
    @Override
    public boolean getPreImgProgress(String preTaskId) {
         JSONObject data = sdTrainPreTaskService.selectNewStatusAndGpuPoolById(preTaskId);
         if (data == null) {
             return true;
         }
        if (data.getIntValue("newStatus")>=2) {
            // 已完成预处理训练，移除redis中预处理任务数据
            delPreTaskData(preTaskId);
            return true;
        }
        Integer total = RedisUtils.getCacheObject(PRE_IMG_PROGRESS_TOTAL+preTaskId);
        Integer complete = RedisUtils.getCacheObject(PRE_IMG_PROGRESS_COMPLETE+preTaskId);
        if (total==null) {
            return true;
        }
        boolean flag = Objects.equals(complete, total);
        if (flag) {
            try{
                // 完成预处理任务
                sdTrainPreTaskService.completePreTask(preTaskId);
            }
            finally {
                delPreTaskData(preTaskId);
            }
        }
        return flag;
    }
    /* 预处理任务 end */


    /** 获取训练GPU卡**/
    private SdGpuPool getGpuFromTrainGpu(String preTaskId, SdGpuPool sdGpuPool, Boolean isStart) {
        // 每次只允许一个任务进行获取
        TRAIN_LOCK.lock();
        try{
            Set<SdGpuPool> pools = RedisUtils.getCacheSet(TRAIN_GPU_POOL);
            if (CollectionUtil.isEmpty(pools)) {
                return null;
            }

            // 下线GPU服务./ru
            if (isStart!=null && !isStart) {
                // 下线GPU绘图服务：表示停止gpu成功
                if (sdGpuPool != null && pools.contains(sdGpuPool)) {
                    RedisUtils.delCacheSet(TRAIN_GPU_POOL,Collections.singleton(sdGpuPool));
                    return null;
                }
                // 下线GPU绘图服务失败：表示停止gpu失败
                else if (sdGpuPool != null) {
                    return sdGpuPool;
                }
            }
            else if (isStart != null) {
                // 上线GPU绘图服务：表示上线gpu成功
                if (sdGpuPool != null) {
                    RedisUtils.setCacheSet(TRAIN_GPU_POOL,Collections.singleton(sdGpuPool));
                    return null;
                }
            }

            sdGpuPool = new ArrayList<>(pools).get(new Random().nextInt(pools.size()));
            RedisUtils.delCacheSet(TRAIN_GPU_POOL,Collections.singleton(sdGpuPool));
            RedisUtils.setCacheObject(TRAIN_GPU_TASK+preTaskId,sdGpuPool);
            return sdGpuPool;
        }
        finally {
            TRAIN_LOCK.unlock();
        }
    }

    /** 归还训练GPU到卡池 **/
    private void returnGpuFromTrainGpuPool(String preTaskId) {
        SdGpuPool sdGpuPool = RedisUtils.getCacheObject(TRAIN_GPU_TASK+preTaskId);
        if (sdGpuPool==null) {
            return;
        }
        // 每次只允许一个任务进行获取
        TRAIN_LOCK.lock();
        try{
            RedisUtils.setCacheSet(TRAIN_GPU_POOL,Collections.singleton(sdGpuPool));
            RedisUtils.deleteObject(TRAIN_GPU_TASK+preTaskId);
        }
        finally {
            TRAIN_LOCK.unlock();
        }
    }


    /** 删除预处理任务数据 **/
    private void delPreTaskData(String preTaskId) {
        WatchMonitor watchMonitor = MONITOR_MAP.get(preTaskId);
        if (watchMonitor!=null) {
            watchMonitor.interrupt();
            watchMonitor.close();
            MONITOR_MAP.remove(preTaskId);
        }
        RedisUtils.delCacheMapValue(PRE_IMG_PROGRESS_TASK_MAP,preTaskId);
        RedisUtils.deleteMultiObject(PRE_IMG_PROGRESS_TOTAL+preTaskId,PRE_IMG_PROGRESS_COMPLETE+preTaskId);
    }

    /** 查询预处理图片数据 **/
    @Override
    public JSONObject getPreImgList(String preTaskId) throws IOException {
        SdTrainTask task;
        if (StrUtil.isNotEmpty(preTaskId)) {
            task = sdTrainPreTaskService.selectDetailById(preTaskId);
        }
        else {
            task = sdTrainPreTaskService.selectDetailByUserId(LoginHelper.getUserId());
        }
        if (task==null) {
            JSONObject data = new JSONObject();
            data.put("imgList",Collections.emptyList());
            data.put("status",null);
            data.put("newStatus",null);
            data.put("preTaskId",null);
            data.put("taskId",null);
            data.put("additionTags", Collections.emptyList());
            return data;
        }

        JSONObject params = JSONObject.parseObject(String.valueOf(task.getPreParams()));
        File preImgDir = new File(params.getString("path").replace("/lora-scripts","")+suggestNumRepeat());
        if (!preImgDir.exists()) {
            preImgDir = new File(params.getString("path").replace("/lora-scripts",""));
        }
        // 判断文件夹是否存在
        if (!preImgDir.exists()) {
            throw new ServiceException("当前图片预处理图片已被删除!");
        }
        List<File> allFileList = getAllFile(preImgDir);
        // 将数据分组
        Map<String, List<File>> group = allFileList.stream().collect(Collectors.groupingBy(e -> e.getName()
            .replace(".jpg", "").replace(".JPG", "")
            .replace(".jpeg", "").replace(".JPEG", "")
            .replace(".png", "").replace(".PNG", "").replace(".txt", "")));
        List<JSONObject> list = new ArrayList<>();
        // 获取违禁词字典
        List<String> wjValueList = sysDictDataService.selectDictValueListByDictType("sys_weijin_code");
        for (String key : group.keySet()) {
            List<File> files = group.get(key);
            JSONObject data = new JSONObject();

            File jpgFile = files.get(0);
            if (jpgFile.isFile()) {
                data.put("img", jpgFile.getPath());
            }
            if (files.size() == 2) {
                File txtFile = files.get(1);
                // 读取txt文件中的标签
                Optional<String> first1;
                try (Stream<String> lines = Files.lines(txtFile.toPath())) {
                    first1 = lines.findFirst();
                }
                if (first1.isPresent()) {
                    List<String> tags = new ArrayList<>(Arrays.asList(StringEscapeUtils.unescapeJava(first1.get()).split(", ")));
                    final int size = tags.size();
                    // 标签去除违禁词
                    if (CollectionUtil.isNotEmpty(wjValueList)) {
                        tags.removeAll(wjValueList);
                        // 标签有变化才修改
                        if (tags.size()<size) {
                            String content = CollectionUtil.isEmpty(tags)?"":tags.stream().map(String::valueOf).collect(Collectors.joining(", "));
                            // 覆盖
                            FileUtil.writeString(content,txtFile,CharsetUtil.UTF_8);
                        }
                    }
                    data.put("tags",tags);
                    //TODO 如果标签是空，这将缺少标签的图片存入缓存，方便后续做提示

                }
                else {
                    data.put("tags", Collections.emptyList());
                    //TODO 如果标签是空，这将将缺少标签的图片存入缓存，方便后续做提示

                }
            }
            list.add(data);
        }
        JSONObject data = new JSONObject();
        data.put("imgList",list);
        data.put("status",task.getStatus());
        data.put("newStatus",task.getNewStatus());
        data.put("preTaskId",String.valueOf(task.getId()));
        data.put("taskId",task.getTaskId());
        data.put("additionTags", RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + task.getId()));
        data.put("translateTagMap", RedisUtils.getCacheMap(TRAIN_TAG_TRANSLATE_MAP + task.getId()));
        return data;
    }

    /** 删除预处理图片数据 **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delPreImg(String imgUrl, String preTaskId) {
        // 添加共性词
        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存在或未完成!");
        }
        else if (sdTrainTask.getNewStatus()<2 && StrUtil.isNotEmpty(sdTrainTask.getTaskId())) {
            throw new ServiceException("预处理未完成,不可删除图片!");
        }
        else if (sdTrainTask.getNewStatus()>2 && StrUtil.isNotEmpty(sdTrainTask.getTaskId())) {
            throw new ServiceException("正在进行训练任务,不可删除图片!");
        }
        // 图片数量减一
        sdTrainPreTaskService.reduceImgNum(preTaskId);
        // 删除图片数量小于1的任务
        boolean flag = sdTrainPreTaskService.deleteByIdAndPicNumIsZero(preTaskId);
        if (flag) {
            // 已完成预处理训练，移除redis中预处理任务数据
            delPreTaskData(preTaskId);
        }

        String txtUrl = imgUrl
            .replace(".jpg", ".txt").replace(".JPG", ".txt")
            .replace(".jpeg", ".txt").replace(".JPEG", ".txt")
            .replace(".png", ".txt").replace(".PNG", ".txt");
        String npzUrl = imgUrl
            .replace(".jpg", ".npz").replace(".JPG", ".npz")
            .replace(".jpeg", ".npz").replace(".JPEG", ".npz")
            .replace(".png", ".npz").replace(".PNG", ".npz");
        FileUtil.del(new File(imgUrl));
        FileUtil.del(new File(txtUrl));
        FileUtil.del(new File(npzUrl));
    }

    /** 修改预处理图片上的标签 **/
    @Override
    public void modifyPreImgTag(SdTrainPreImgDto dto) {
        if (CollectionUtil.isEmpty(dto.getTags())) {
            throw new ServiceException("训练数据集图片标签不能为空!");
        }
        // 新版本：对前端传入的中文进行翻译
        List<String> tags = new ArrayList<>();
        for (SdTrainTagDto tagDto : dto.getTags()) {
            if (StrUtil.isEmptyIfStr(tagDto.getTagZh()) && StrUtil.isEmptyIfStr(tagDto.getTag())) {
                continue;
            }
            // 有英文
            if (StrUtil.isNotEmpty(tagDto.getTag())) {
                tags.add(tagDto.getTag());
            }
            // 没有英文，使用中文并进行翻译
            else {
                String enTag = RedisUtils.getCacheMapValue(TRANSLATE_ZH_TO_EN_MAP, tagDto.getTagZh());
                if (StrUtil.isEmptyIfStr(enTag)) {
                    try {
                        enTag = sysTranslateService.zhToEn(tagDto.getTagZh(), TranslateType.BAIDU);
                        RedisUtils.setCacheMapValue(TRANSLATE_ZH_TO_EN_MAP, tagDto.getTagZh(), enTag);
                    }
                    catch (NoSuchAlgorithmException e) {
                        throw new ServiceException(String.format("翻译服务异常,添加标签失败!原因：%s",e.getMessage()));
                    }
                }
                tags.add(enTag);
            }
        }
        // 获取违禁词字典
        List<String> wjValueList = sysDictDataService.selectDictValueListByDictType("sys_weijin_code");
        tags.removeAll(wjValueList);
        if (CollectionUtil.isEmpty(tags)) {
            throw new ServiceException("训练数据集图片标签[移除违禁词]不能为空!");
        }

        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailById(dto.getPreTaskId());
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存在!");
        }
        else if (sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务已开始,不可进行添加标签操作!");
        }
        else if (sdTrainTask.getNewStatus()>2) {
            throw new ServiceException("模型训练已开始,不可进行添加标签操作!");
        }
        String txtUrl = dto.getImgUrl()
            .replace(".jpg", ".txt").replace(".JPG", ".txt")
            .replace(".jpeg", ".txt").replace(".JPEG", ".txt")
            .replace(".png", ".txt").replace(".PNG", ".txt");
        String content = tags.stream().map(String::valueOf).collect(Collectors.joining(", "));
        // 覆盖
        FileUtil.writeString(content,txtUrl, CharsetUtil.UTF_8);
        // 添加翻译
        Map<String, String> newTagMap = dto.getTags().stream().filter(e->StrUtil.isNotEmpty(e.getTagZh())).collect(Collectors.toMap(SdTrainTagDto::getTag, SdTrainTagDto::getTagZh, (v1, v2) -> v1));
        RedisUtils.setCacheMap(TRAIN_TAG_TRANSLATE_MAP + dto.getPreTaskId(),newTagMap);
    }

    /** 添加共性词标签 **/
    @Override
    public void insertAdditionTag(SdTrainAdditionTagDto dto) {
        if (StrUtil.isEmpty(dto.getAdditionTagZh()) && StrUtil.isEmpty(dto.getAdditionTag())) {
            throw new ServiceException("缺少共性词!");
        }
        String additionTagEn = dto.getAdditionTag();
        // 中文不为空，英文为空
        if (StrUtil.isEmptyIfStr(additionTagEn) && StrUtil.isNotEmpty(dto.getAdditionTagZh())) {
            additionTagEn = RedisUtils.getCacheMapValue(TRANSLATE_ZH_TO_EN_MAP, dto.getAdditionTagZh());
            if (StrUtil.isEmptyIfStr(additionTagEn)) {
                try {
                    additionTagEn = sysTranslateService.zhToEn(dto.getAdditionTagZh(),TranslateType.BAIDU);
                    RedisUtils.setCacheMapValue(TRANSLATE_ZH_TO_EN_MAP, dto.getAdditionTagZh(), additionTagEn);
                }
                catch (NoSuchAlgorithmException e) {
                    throw new ServiceException(String.format("翻译服务异常,添加标签失败!原因：%s",e.getMessage()));
                }
            }
        }

        // 获取违禁词字典
        List<String> wjValueList = sysDictDataService.selectDictValueListByDictType("sys_weijin_code");
        if (wjValueList.contains(additionTagEn)) {
            throw new ServiceException("当前共性词不可使用[违禁词]!");
        }

        Set<Object> tagSet = RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + dto.getPreTaskId());
        if (CollectionUtil.isNotEmpty(tagSet) && tagSet.size()>=3) {
            throw new ServiceException("当前训练任务的共性词已达上限,不可添加!");
        }
        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailById(dto.getPreTaskId());
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存!");
        }
        else if (sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务已开始,不可进行添加共性词操作!");
        }
        else if (sdTrainTask.getNewStatus()>2) {
            throw new ServiceException("模型训练已开始,不可进行添加共性词操作!");
        }
        // 添加共性词
        JSONObject paramsJson = JSONObject.parseObject(String.valueOf(sdTrainTask.getPreParams()));
        String path = paramsJson.getString("path").replace("/lora-scripts","");
        File preImgDir = new File(path);
        List<File> allFileList = getAllFile(preImgDir);
        if (CollectionUtil.isNotEmpty(allFileList)) {
            final String finalAdditionTagEn = additionTagEn;
            allFileList.stream().filter(e->e.getName().contains(".txt")).forEach(e->{
                Optional<String> first1 = Optional.empty();
                try (Stream<String> lines = Files.lines(e.toPath())) {
                    first1 = lines.findFirst();
                }
                catch (IOException ex) {
                    log.error("获取文件[{}]内容失败：{}",e.getPath(),ex.getMessage());
                }
                first1.ifPresent(s -> {
                    s = StringEscapeUtils.unescapeJava(s);
                    FileUtil.writeString(finalAdditionTagEn + ", " + s, e.getPath(), CharsetUtil.UTF_8);
                });
            });
        }
        // 添加翻译
        RedisUtils.setCacheMapValue(TRAIN_TAG_TRANSLATE_MAP + dto.getPreTaskId(),additionTagEn,StrUtil.isEmptyIfStr(dto.getAdditionTagZh())?additionTagEn:dto.getAdditionTagZh());
        // 添加共性词数组
        RedisUtils.setCacheSet(TRAIN_ADDITION_LIST+dto.getPreTaskId(),Collections.singleton(additionTagEn));
        // 将共性词更新到数据库
        sdTrainPreTaskService.updateAdditionTag(dto.getPreTaskId(),JSON.toJSONString(Collections.singleton(additionTagEn)));
    }

    /** 删除标签 **/
    @Override
    public void delPreImgTag(SdTrainTagDelDto dto) {
        if (StrUtil.isEmptyIfStr(dto.getTag())) {
            throw new ServiceException("需要删除的共性词或标签不能为空!");
        }
        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailById(dto.getPreTaskId());
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存在!");
        }
        else if (sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务已开始,不可进行删除标签操作!");
        }
        else if (sdTrainTask.getNewStatus()>2) {
            throw new ServiceException("模型训练任务已开始,不可进行删除标签操作!");
        }
        // 判断删除的标签是否是共性词
        Set<Object> tagSet = RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + dto.getPreTaskId());
        String removeTag = dto.getTag();
        // 删除共性词
        if (CollectionUtil.isNotEmpty(tagSet) && tagSet.contains(removeTag)) {
            String dir = dto.getImgUrl().substring(0, dto.getImgUrl().lastIndexOf("/"));
            File preImgDir = new File(dir);
            // 获取当前图片所属的文件夹下的全部文件
            List<File> allFileList = getAllFile(preImgDir);
            if (CollectionUtil.isNotEmpty(allFileList)) {
                // 过滤出txt文件且只对txt文件进行处理
                allFileList.stream().filter(e->e.getName().contains(".txt")).forEach(e->{
                    Optional<String> first1 = Optional.empty();
                    try (Stream<String> lines = Files.lines(e.toPath(), StandardCharsets.UTF_8)) {
                        first1 = lines.findFirst();
                    }
                    catch (IOException ex) {
                        log.error("获取文件[{}]内容失败：{}",e.getPath(),ex.getMessage());
                    }
                    // 将需要删除的标签替换成空
                    first1.ifPresent(s -> {
                        s = StringEscapeUtils.unescapeJava(s);
                        FileUtil.writeString(s.replaceFirst(removeTag + ", ", "").replaceFirst(removeTag, ""), e.getPath(), CharsetUtil.UTF_8);
                    });
                });
            }
            // 缓存中移除当前任务对应的删除的共性词
            RedisUtils.delCacheSet(TRAIN_ADDITION_LIST + dto.getPreTaskId(),Collections.singleton(dto.getTag()));
        }
        // 删除标签
        else {
            File imgUrl = new File(dto.getImgUrl()
                .replace(".jpg", ".txt").replace(".JPG", ".txt")
                .replace(".jpeg", ".txt").replace(".JPEG", ".txt")
                .replace(".png", ".txt").replace(".PNG", ".txt"));
            Optional<String> first1 = Optional.empty();
            try (Stream<String> lines = Files.lines(imgUrl.toPath(), StandardCharsets.UTF_8)) {
                first1 = lines.findFirst();
            }
            catch (IOException ex) {
                log.error("获取文件[{}]内容失败：{}",imgUrl.getPath(),ex.getMessage());
            }
            first1.ifPresent(s -> {
                s = StringEscapeUtils.unescapeJava(s);
                FileUtil.writeString(s.replace(removeTag + ", ", "").replace(removeTag,""), imgUrl.getPath(), CharsetUtil.UTF_8);
            });
        }
    }



    /* 模型训练任务 start */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void trainSdLora(SdTrainLoraDto dto) {
        String preTaskId = dto.getPreTaskId();
        if (preTaskId==null || StrUtil.isEmptyIfStr(preTaskId)) {
            throw new ServiceException("缺少图片预处理任务ID!");
        }

        // 获取当前用户的剩余训练次数
        Integer trainTimes = userService.selectTrainTimesById(LoginHelper.getUserId());
        if (trainTimes!=null && trainTimes<=0) {
            throw new ServiceException("余额不足,请联系管理员!");
        }

        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null || sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务不存在或未完成!");
        }
        else if (sdTrainTask.getNewStatus()>2 && sdTrainTask.getNewStatus()<5) {
            throw new ServiceException("存在未完成的训练任务!");
        }
        JSONObject paramsJson = JSONObject.parseObject(String.valueOf(sdTrainTask.getPreParams()));
        // path -> /lora-scripts/train-data/{userId}/{preTaskId}
        String path = paramsJson.getString("path");
        if (StringUtils.isBlank(path)) {
            throw new ServiceException("缺少图片预处理后的数据集路径!");
        }
        Map<String,Object> trainParams = createSdTrainParam(dto,path,preTaskId);

        JSONObject msg = new JSONObject();
        msg.put("trainParams",trainParams);
        msg.put("preTaskId",preTaskId);
        msg.put("modelName",dto.getModelName());

        // 更新为 队列中 状态
        sdTrainPreTaskService.modifyNewStatusById(preTaskId, 3, null, null);
        // 将任务添加到任务列表中
        RedisUtils.setCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(preTaskId));

        rabbitTemplate.convertAndSend(SD_TRAIN_TASK_EXCHANGE,SD_TRAIN_TASK_ROUTING_KEY,msg,new CorrelationData(preTaskId));
    }
    @RabbitListener(queues = SD_TRAIN_TASK_QUEUE)
    public void trainTask(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        JSONObject params = JSON.parseObject(body, JSONObject.class);
        String preTaskId = params.getString("preTaskId");

        // 判断任务是否在任务列表中
        List<String> list = RedisUtils.getCacheList(TRAIN_TASK_QUEUE_LIST);
        if (CollectionUtil.isEmpty(list) || !list.contains(preTaskId)) {
            sdTrainPreTaskService.modifyNewStatusById(params.getString("preTaskId"),2, "手动取消训练任务", null);
            channel.basicAck(deliveryTag, false);
            return;
        }

        // 判断是否还有训练服务可以使用
        SdGpuPool sdGpuPool = getGpuFromTrainGpu(preTaskId,null, null);
        if (sdGpuPool==null) {
            log.error("模型训练任务>>>>>>>>>训练卡池中暂无可使用的GPU服务,请等待!");
            return;
        }
        try{
            Map<String,Object> trainParams = JSONObject.parseObject(params.getString("trainParams"), Map.class);
//            trainParams.put("gpu_ids",Collections.singletonList(String.valueOf(sdGpuPool.getDeviceId())));
            log.warn("模型训练任务>>>>>>>>>任务ID[{}],开始进行模型训练任务,训练参数：{}",preTaskId,trainParams);

            JSONObject run = Forest.post("/api/run").address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().addBody(trainParams).execute(JSONObject.class);
            log.warn("模型训练任务>>>>>>>>>启动训练：{}",run);
            if ("error".equals(run.getString("status"))) {
                log.error("模型训练任务>>>>>>>>>训练失败：{}",run.getString("message"));
                sdTrainPreTaskService.modifyNewStatusById(params.getString("preTaskId"),2, run.getString("message"), sdGpuPool);
                returnGpuFromTrainGpuPool(preTaskId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            if ("fail".equals(run.getString("status"))) {
                sdTrainPreTaskService.modifyNewStatusById(params.getString("preTaskId"),6, run.getString("message"), sdGpuPool);
                log.error("模型训练任务>>>>>>>>>训练失败：{}",run.getString("message"));
                returnGpuFromTrainGpuPool(preTaskId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            String taskId = run.getJSONObject("data").getString("taskId");
            // 修改为进行中状态
            sdTrainPreTaskService.updateTaskIdAndNewStatus(params.getString("preTaskId"),taskId,trainParams,params.getString("modelName"),new Date(),4, sdGpuPool);
            RedisUtils.setCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,taskId,taskId);
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            returnGpuFromTrainGpuPool(preTaskId);
            log.error("模型训练任务>>>>>>>>>任务ID[{}],训练异常,重新进入队列：{}", preTaskId,e.getMessage());
            channel.basicNack(deliveryTag, false, true);
        }
    }
    @Override
    public TrainProcessDataVo trainProgress(String taskId) {
        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailByTaskId(taskId);
        if (sdTrainTask==null || sdTrainTask.getNewStatus()==2 || sdTrainTask.getNewStatus()>4) {
            if (sdTrainTask!=null) {
                // 将当前任务从任务列表中移除
                RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,sdTrainTask.getTaskId());
                RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));
                returnGpuFromTrainGpuPool(String.valueOf(sdTrainTask.getId()));
            }
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED");
        }
        SdGpuPool sdGpuPool = RedisUtils.getCacheObject(TRAIN_GPU_TASK+sdTrainTask.getId());
        if (sdGpuPool==null) {
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,sdTrainTask.getTaskId());
            RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED");
        }

        LinkedHashMap<String, Object> execute = Forest.get("/api/tasks").address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().execute(LinkedHashMap.class);
        LinkedHashMap<String, Object> data1 = (LinkedHashMap<String, Object>) execute.get("data");

        List<LinkedHashMap<String, String>> tasks = (List<LinkedHashMap<String, String>>) data1.get("tasks");
        if (CollectionUtil.isEmpty(tasks)) {
            return new TrainProcessDataVo().setId(taskId).setStatus(null);
        }
        Optional<LinkedHashMap<String, String>> task = tasks.stream().filter(e -> taskId.equals((e.get("id")))).findFirst();
        if (!task.isPresent()) {
            return new TrainProcessDataVo().setId(taskId).setStatus(null);
        }
        LinkedHashMap<String, String> map = task.get();
        if ("FINISHED".equals(map.get("status"))) {
            sdTrainPreTaskService.completeTrainTask(map.get("id"),new Date());
            sdTrainTask.setEndTime(new Date());
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,sdTrainTask.getTaskId());
            RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));
            returnGpuFromTrainGpuPool(String.valueOf(sdTrainTask.getId()));
            CompletableFuture.runAsync(()-> dealTrainModelFile(sdTrainTask),executor)
            .exceptionally(e -> {
                log.error("处理模型文件出现异常：{}",e.getMessage());
                return null;
            });

            // 扣除训练次数
            userService.deductedTrainTimes(sdTrainTask.getCrtUserId());
        }
        return new TrainProcessDataVo().setId(map.get("id")).setStatus(map.get("status"));
    }
    @Override
    public TrainProcessDataVo trainProgressV2(String taskId) {
        SdTrainTask sdTrainTask = sdTrainPreTaskService.selectDetailByTaskId(taskId);
        if (sdTrainTask==null || sdTrainTask.getNewStatus()==2 || sdTrainTask.getNewStatus()>4) {
            if (sdTrainTask!=null) {
                // 将当前任务从任务列表中移除
                RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,sdTrainTask.getTaskId());
                RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));
                returnGpuFromTrainGpuPool(String.valueOf(sdTrainTask.getId()));
            }
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED");
        }
        // 获取任务训练时使用的GPU
        SdGpuPool sdGpuPool = RedisUtils.getCacheObject(TRAIN_GPU_TASK+sdTrainTask.getId());
        if (sdGpuPool==null) {
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,sdTrainTask.getTaskId());
            RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED");
        }

        TrainProcessTaskVo execute = Forest.get("/api/tasks/"+taskId).address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().execute(TrainProcessTaskVo.class);
        TrainProcessDataVo taskData = execute.getData();
        if (taskData==null) {
            return new TrainProcessDataVo().setId(taskId).setStatus(null);
        }
        if ("FINISHED".equals(taskData.getStatus())) {
            sdTrainPreTaskService.completeTrainTask(taskData.getId(),new Date());
            sdTrainTask.setEndTime(new Date());
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP,sdTrainTask.getTaskId());
            RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));
            returnGpuFromTrainGpuPool(String.valueOf(sdTrainTask.getId()));
            CompletableFuture.runAsync(()-> dealTrainModelFile(sdTrainTask),executor)
            .exceptionally(e -> {
                log.error("处理模型文件出现异常：",e);
                return null;
            });

            // 扣除训练次数
            userService.deductedTrainTimes(sdTrainTask.getCrtUserId());
        }
        return taskData;
    }
    private void dealTrainModelFile(SdTrainTask sdTrainTask) {
        JSONObject trainParams = JSONObject.parseObject(String.valueOf(sdTrainTask.getTrainParams()));
        // 训练后的模型原始名称
        String oldModelName = trainParams.getString("output_name");
        JSONObject preParams = JSONObject.parseObject(String.valueOf(sdTrainTask.getPreParams()));
        // 获取数据集中的第一张图片作为训练模型的封面，移除训练器容器中的目录前缀
        // path -> /lora-scripts/train-data/{userId}/{preTaskId}/*
        File[] imgs = FileUtil.ls(preParams.getString("path").replace("/lora-scripts","")+suggestNumRepeat());
        if (imgs != null) {
            Optional<File> first = Arrays.stream(imgs).filter(e ->
                e.getName().contains(".png") || e.getName().contains(".PNG") ||
                    e.getName().contains(".jpg") || e.getName().contains(".JPG") ||
                    e.getName().contains(".jpeg") || e.getName().contains(".JPEG")
            ).findFirst();
            if (first.isPresent()) {
                File img = first.get();
                log.warn("训练完成>>>>>>>>>复制数据集中的第一张图片作为模型图片：{}",img.getPath());
                try {
                    FileInputStream inputStream = new FileInputStream(img);
                    FileOutputStream outputStream = new FileOutputStream("/models/"+oldModelName+img.getName().substring(img.getName().lastIndexOf(".")));
                    IoUtil.copy(inputStream,outputStream);
                } catch (FileNotFoundException e) {
                    log.error("训练完成>>>>>>>>>需要复制的模型图片不存在：{}",e.getMessage());
                }
            }
        }

        String oldModelDir = "/output/"+sdTrainTask.getId();
        String suffix = String.format("%06d",trainParams.getIntValue("max_train_epochs"));

        // 重命名最后一轮生成的模型名称
        String modelName = oldModelName+"."+ trainParams.getString("save_model_as");
        String newModelName = oldModelName+"-"+suffix+ "." + trainParams.getString("save_model_as");
        File startFile = new File(oldModelDir+"/"+modelName);
        File renameFile = new File(oldModelDir+"/"+newModelName);

        log.warn("训练完成>>>>>>>>>修改最后一轮训练的模型名称：{}->{}",startFile.getPath(),renameFile.getPath());
        startFile.renameTo(renameFile);

        log.warn("训练完成>>>>>>>>>拷贝训练模型到SdWebUI的lora目录下：{}->{}",oldModelDir,"/models");
        // 移动全部模型到sd的models/Lora目录下
        try{
            File[] files = FileUtil.ls(oldModelDir);
            if (files != null) {
                for (File file : files) {
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        FileOutputStream outputStream = new FileOutputStream("/models/"+file.getName());
                        IoUtil.copy(inputStream,outputStream);
                    } catch (FileNotFoundException e) {
                        log.error("训练完成>>>>>>>>>需要复制的模型文件不存在：{}",e.getMessage());
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("训练完成>>>>>>>>>拷贝训练模型到SdWebUI的lora目录下：{}->{}报错：{}",oldModelDir,"/models",e.getMessage());
        }

        // 删除模型目录
        File outputDir = new File("/output/"+sdTrainTask.getId());
        log.warn("训练完成>>>>>>>>>删除训练模型[{}]的所在原目录：{}",modelName,outputDir.getPath());
        FileUtil.del(outputDir);

        // 删除redis
        RedisUtils.deleteMultiObject(TRAIN_ADDITION_LIST+sdTrainTask.getId(),TRAIN_TAG_TRANSLATE_MAP+sdTrainTask.getId());
        RedisUtils.delCacheList(TRAIN_TASK_QUEUE_LIST,Collections.singletonList(sdTrainTask.getId()));

        // 通知刷新lora模型
        applicationEventPublisher.publishEvent(new RefreshLoraEvent(true));

        // 发送微信公众号消息
        JSONObject wxMsg = new JSONObject();
        wxMsg.put("preTaskId",sdTrainTask.getId());
        wxMsg.put("taskId",sdTrainTask.getTaskId());
        wxMsg.put("belongUserId",sdTrainTask.getCrtUserId());
        wxMsg.put("belongUserName",sdTrainTask.getCrtUserName());
        wxMsg.put("type","MODEL_TRAIN");
        wxMsg.put("modelName",sdTrainTask.getModelName());
        wxMsg.put("oldModelName",oldModelName);
        wxMsg.put("startTime",DateUtil.formatDateTime(sdTrainTask.getStartTime()));
        wxMsg.put("endTime",DateUtil.formatDateTime(sdTrainTask.getEndTime()));
        rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg);
    }
    /* 模型训练任务 end */



    @Override
    public void stopGpuPool(SdGpuPool sdGpuPool) {
        SdGpuPool gpuPool = getGpuFromTrainGpu(null, sdGpuPool, false);
        if (gpuPool!=null) {
            throw new ServiceException("当前GPU正在使用!");
        }
    }

    @Override
    public void startGpuPool(SdGpuPool sdGpuPool) {
        SdGpuPool gpuPool = getGpuFromTrainGpu(null, sdGpuPool, true);
        if (gpuPool!=null) {
            throw new ServiceException("当前GPU正在使用!");
        }
    }

    /** 获取模型训练的数据集 **/
    @Override
    public List<JSONObject> getModelTrainDateList(String preTaskId) throws IOException {
        SdTrainTask task = sdTrainPreTaskService.selectDetailById(preTaskId);
        if (task==null) {
            return Collections.emptyList();
        }
        JSONObject params = JSONObject.parseObject(String.valueOf(task.getPreParams()));
        File preImgDir = new File(params.getString("path").replace("/lora-scripts","")+suggestNumRepeat());
        if (!preImgDir.exists()) {
            preImgDir = new File(params.getString("path").replace("/lora-scripts",""));
        }
        // 判断文件夹是否存在
        if (!preImgDir.exists()) {
            throw new ServiceException("当前模型的预处理图片已被删除!");
        }
        List<File> allFileList = getAllFile(preImgDir);
        // 将数据分组
        Map<String, List<File>> group = allFileList.stream().collect(Collectors.groupingBy(e -> e.getName()
            .replace(".jpg", "").replace(".JPG", "")
            .replace(".jpeg", "").replace(".JPEG", "")
            .replace(".png", "").replace(".PNG", "").replace(".txt", "")));
        List<JSONObject> list = new ArrayList<>();
        for (String key : group.keySet()) {
            List<File> files = group.get(key);
            JSONObject data = new JSONObject();

            File jpgFile = files.get(0);
            if (jpgFile.isFile()) {
                data.put("dataImg", jpgFile.getPath());
            }
            if (files.size() == 2) {
                File txtFile = files.get(1);
                // 读取txt文件中的标签
                Optional<String> first1;
                try (Stream<String> lines = Files.lines(txtFile.toPath())) {
                    first1 = lines.findFirst();
                }
                data.put("tags", Collections.emptyList());
                data.put("tagZhs", Collections.emptyList());
                if (first1.isPresent()) {
                    String line = first1.get();
                    line = StringEscapeUtils.unescapeJava(line);
                    List<String> tags = Arrays.asList(line.split(", "));
                    if (CollectionUtil.isNotEmpty(tags)) {
                        data.put("tags",tags);
                        // 将tags使用redis中的缓存TRANSLATE_EN_TO_ZH_MAP转换为中文
                        List<String> tagZhs = tags.stream().map(e->{
                            String zhStr = RedisUtils.getCacheMapValue(TRANSLATE_EN_TO_ZH_MAP,e);
                            return StringUtils.isBlank(zhStr)?e:zhStr;
                        }).collect(Collectors.toList());
                        data.put("tagZhs",tagZhs);
                    }
                }
            }
            list.add(data);
        }
        return list;
    }

    /** 获取训练文件夹名称 **/
    public String suggestNumRepeat() {
        return "/20_zkz";
    }

    /** 获取指定目录下的全部文件 **/
    private List<File> getAllFile(File preImgDir) {
        // 获取文件列表
        File[] fileList = preImgDir.listFiles();
        // 只要png、jpg、jpeg、txt文件
        assert fileList != null;
        // 如果是文件则将其加入到文件数组中
        return Arrays.stream(fileList).filter(e ->
            e.getName().contains(".jpg") || e.getName().contains(".JPG") ||
            e.getName().contains(".png") || e.getName().contains(".PNG") ||
            e.getName().contains(".jpeg") || e.getName().contains(".JPEG") || e.getName().contains(".txt")
        ).collect(Collectors.toList());
    }

    /** 创建训练参数 **/
    private Map<String, Object> createSdTrainParam(SdTrainLoraDto dto, String path, String preTaskId) {
        Map<String,Object> map = new HashMap<>();
        map.put("model_train_type","sdxl-lora");
        map.put("pretrained_model_name_or_path","/lora-scripts/sd-models/sd_xl_base_1.0_0.9vae.safetensors");
        map.put("v2",false);
        map.put("train_data_dir",path);
        map.put("prior_loss_weight",1);
        map.put("resolution","512,512");
        map.put("enable_bucket",true);
        map.put("min_bucket_reso",256);
        map.put("max_bucket_reso",1024);
        map.put("bucket_reso_steps",64);
        map.put("output_name","user_"+preTaskId);
        map.put("output_dir","/lora-scripts/output/"+preTaskId);
        map.put("save_model_as","safetensors");
        map.put("save_precision","bf16");
        map.put("save_every_n_epochs",2);
        map.put("max_train_epochs",10);
        map.put("train_batch_size",1);
        map.put("gradient_checkpointing",false);
        map.put("network_train_unet_only",false);
        map.put("network_train_text_encoder_only",false);
        map.put("learning_rate",0.0001);
        map.put("unet_lr",0.0001);
        map.put("text_encoder_lr",0.00001);
        map.put("lr_scheduler","cosine_with_restarts");
        map.put("lr_warmup_steps",0);
        map.put("lr_scheduler_num_cycles",1);
        map.put("optimizer_type","AdamW8bit");
        map.put("network_module","networks.lora");
        map.put("network_dim",32);
        map.put("network_alpha",32);
        map.put("log_with","tensorboard");
        map.put("logging_dir","/lora-scripts/logs");
        map.put("caption_extension",".txt");
        map.put("shuffle_caption",true);
        map.put("keep_tokens",0);
        map.put("max_token_length",255);
        map.put("seed",1337);
        map.put("mixed_precision","bf16");
        map.put("full_bf16",true);
        map.put("no_half_vae",true);
        map.put("xformers",true);
        map.put("lowram",false);
        map.put("cache_latents",true);
        map.put("cache_latents_to_disk",true);
        map.put("persistent_data_loader_workers",true);
        if (CollectionUtil.isNotEmpty(dto.getExtParam())) {
            map.putAll(dto.getExtParam());
            if ("sd_lora".equals(map.get("model_train_type"))) {
                map.put("clip_skip",2);
            }
        }
        return map;
    }

    /** 创建文件夹监听器 **/
    private void createFileDirMonitor(File dir, String taskId) {
        // 只监听目录的创建事件
        WatchMonitor watchMonitor = WatchMonitor.create(dir, WatchMonitor.ENTRY_CREATE);
        watchMonitor.setWatcher(new SimpleWatcher() {
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                Object context = watchEvent.context();
                log.warn("创建：{}-->{}", path, context);
                RedisUtils.incrAtomicValue(PRE_IMG_PROGRESS_COMPLETE+taskId);
                File txtFile = new File(path.toFile().getPath()+"/"+context);
                try (Stream<String> lines = Files.lines(txtFile.toPath())) {
                    Optional<String> content = lines.findFirst();
                    if (content.isPresent()) {
                        String en = content.get();
                        //去转义
                        en = StringEscapeUtils.unescapeJava(en);
                        List<String> enList = Arrays.asList(en.split(", "));
                        if (CollectionUtil.isNotEmpty(enList)) {
                            for (String enStr : enList) {
                                String zhStr = RedisUtils.getCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, enStr);
                                if (StrUtil.isEmptyIfStr(zhStr)) {
                                    try{
                                        zhStr = sysTranslateService.enToZh(enStr,TranslateType.BAIDU);
                                    } catch (NoSuchAlgorithmException ex) {
                                        log.error("翻译失败：{}",ex.getMessage());
                                    }
                                }
                                if (StrUtil.isNotEmpty(zhStr) && !enStr.equals(zhStr)) {
                                    RedisUtils.setCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, enStr, zhStr);
                                    RedisUtils.setCacheMapValue(TRAIN_TAG_TRANSLATE_MAP + taskId,enStr,zhStr);
                                }
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    log.error("获取文件[{}]内容失败：{}",path,ex.getMessage());
                }
            }
        });
        // 启动监听
        watchMonitor.start();
        MONITOR_MAP.put(taskId,watchMonitor);
    }
}
