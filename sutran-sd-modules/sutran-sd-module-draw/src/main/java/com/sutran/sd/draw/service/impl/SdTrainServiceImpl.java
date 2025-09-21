package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dtflys.forest.Forest;
import com.rabbitmq.client.Channel;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.vo.*;
import com.sutran.sd.draw.domain.dto.train.*;
import com.sutran.sd.draw.mq.MqConstant;
import com.sutran.sd.draw.events.RefreshLoraEvent;
import com.sutran.sd.draw.service.SdCommonConfigService;
import com.sutran.sd.draw.service.SdGpuPoolService;
import com.sutran.sd.draw.service.SdTrainService;
import com.sutran.sd.draw.service.SdTrainTaskService;
import com.sutran.sd.draw.domain.SdCommonConfig;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.SdTrainTask;
import com.sutran.sd.draw.utils.CommonUtil;
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

/**
 * @author zj
 * @date 2024-03-24
 */
@SuppressWarnings("ALL")
@Slf4j
@Service
@RequiredArgsConstructor
public class SdTrainServiceImpl implements SdTrainService {

    private final SdTrainTaskService sdTrainTaskService;
    private final RabbitTemplate rabbitTemplate;
    private final SysTranslateService sysTranslateService;
    private final SdGpuPoolService sdGpuPoolService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ISysDictDataService sysDictDataService;
    private final SdCommonConfigService sdCommonConfigService;
    private final UserService userService;

    @Resource(name = "threadPoolTaskExecutor")
    private Executor executor;
    private final static Map<String,WatchMonitor> MONITOR_MAP = new ConcurrentHashMap<>();
    private final static Lock TRAIN_LOCK = new ReentrantLock();

    /**
     * 预处理图片任务状态列表
     *
     * @param pageQuery 分页参数
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @param userId    登录人id
     * @return 任务集合
     */
    @Override
    public TableDataInfo<TrainTaskVo> getTrainTasksV2(PageQuery pageQuery, Integer newStatus, Long userId) {
        // 根据当前用户和任务状态获取任务列表
        Page<SdTrainTask> page = sdTrainTaskService.selectListByUserIdAndNewStatus(userId,newStatus,pageQuery);
        List<SdTrainTask> list = page.getRecords();
        if (CollectionUtil.isEmpty(list)) {
            return TableDataInfo.build(Collections.emptyList());
        }
        List<TrainTaskVo> records = list.stream().map(e -> {
            TrainTaskVo vo = new TrainTaskVo();
            BeanUtils.copyProperties(e, vo);
            vo.setPreTaskId(String.valueOf(e.getId()));
            if (StringUtils.isNotBlank(e.getPreParams())) {
                JSONObject preParams = JSONObject.parseObject(e.getPreParams());
                vo.setPreTaskParams(preParams);
            }
            if (StringUtils.isNotBlank(e.getTrainParams())) {
                JSONObject trainParams = JSONObject.parseObject(e.getTrainParams());
                vo.setTrainTaskParams(trainParams);
            }
            if (StringUtils.isNotBlank(e.getAdditionTag())) {
                List<String> additionTag = JSON.parseArray(e.getAdditionTag(), String.class);
                vo.setAdditionTag(additionTag);
            }
            return vo;
        }).collect(Collectors.toList());
        return new TableDataInfo<>(records,page.getTotal());
    }

    /**
     * 获取当前任务状态
     * @param preTaskId 预处理任务id
     * @return 任务状态
     */
    @Override
    public TrainTaskStatusVo getSdTaskStatus(String preTaskId) {
        TrainTaskStatusVo data = sdTrainTaskService.selectTaskStatusByPreTaskId(preTaskId);
        if (data==null) {
            throw new ServiceException("训练任务不存在或已被删除!");
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
        return sdTrainTaskService.selectTaskStatusByPreTaskId(preTaskId);
    }

    /**
     * 查询预处理图片数据
     * @param preTaskId 预处理任务id
     * @return  预处理图片任务
     */
    @Override
    public TrainPreImgTaskVo getPreImgList(String preTaskId) {
        SdTrainTask task;
        // 没有预处理任务ID，则按照当前登录用查询
        if (StrUtil.isNotEmpty(preTaskId) || "null".equals(preTaskId)) {
            task = sdTrainTaskService.selectDetailById(preTaskId);
        }
        else {
            task = sdTrainTaskService.selectDetailByUserId(LoginHelper.getUserId());
        }
        // 没有任务，则返回空
        if (task==null) {
            return new TrainPreImgTaskVo();
        }

        // 出来预处理任务参数
        JSONObject params = JSONObject.parseObject(task.getPreParams());
        String path = dealTrainDataSetPath(params.getString("path"));
        File preImgDir = new File(path+CommonUtil.suggestNumRepeat());
        if (!preImgDir.exists()) {
            preImgDir = new File(path);
        }

        // 判断文件夹是否存在
        if (!preImgDir.exists()) {
            throw new ServiceException("当前图片预处理图片已被删除!");
        }

        // 获取预处理任务下的所有图片文件 并 按照文件名(xxx.jpg、xxx.txt)分组
        Map<String, List<File>> group = CommonUtil.getAllFileAndGroup(preImgDir);

        // 获取违禁词字典
        List<String> wjValueList = sysDictDataService.selectDictValueListByDictType("sys_weijin_code");

        // 读取图片标签和图片地址
        List<TrianImgDataVo> results = CommonUtil.readTagFromTxtAndImgUrl(group, wjValueList);

        // 获取预处理任务下的所有共性词
        Set<String> additionTags = RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + task.getId());
        // 获取预处理任务下的所有标签翻译
        Map<String, String> cacheMap = RedisUtils.getCacheMap(TRAIN_TAG_TRANSLATE_MAP + task.getId());
        return new TrainPreImgTaskVo()
            .setImgList(results)
            .setPreTaskId(String.valueOf(task.getId()))
            .setNewStatus(task.getNewStatus())
            .setStatus(task.getStatus())
            .setTaskId(task.getTaskId())
            .setAdditionTags(additionTags)
            .setTranslateTagMap(cacheMap);
    }

    /**
     * [V2]查询预处理图片数据
     * @param preTaskId 预处理任务id
     * @return  预处理图片任务
     */
    @Override
    public TrainPreImgTaskVo getPreImgListV2(String preTaskId) {
        SdTrainTask task = sdTrainTaskService.selectDetailById(preTaskId);;
        // 没有任务，则返回空
        if (task==null) {
            return new TrainPreImgTaskVo();
        }

        // 出来预处理任务参数
        JSONObject params = JSONObject.parseObject(task.getPreParams());
        String path = dealTrainDataSetPath(params.getString("path"));
        File preImgDir = new File(path+CommonUtil.suggestNumRepeat());
        if (!preImgDir.exists()) {
            preImgDir = new File(path);
        }

        // 判断文件夹是否存在
        if (!preImgDir.exists()) {
            throw new ServiceException("当前图片预处理图片已被删除!");
        }

        // 获取预处理任务下的所有图片文件 并 按照文件名(xxx.jpg、xxx.txt)分组
        Map<String, List<File>> group = CommonUtil.getAllFileAndGroup(preImgDir);

        // 获取违禁词字典
        List<String> wjValueList = sysDictDataService.selectDictValueListByDictType("sys_weijin_code");

        // 读取图片标签和图片地址
        List<TrianImgDataVo> results = CommonUtil.readTagFromTxtAndImgUrl(group, wjValueList);

        // 获取预处理任务下的所有共性词
        Set<String> additionTags = RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + preTaskId);
        // 获取预处理任务下的所有标签翻译
        Map<String, String> cacheMap = RedisUtils.getCacheMap(TRAIN_TAG_TRANSLATE_MAP + preTaskId);
        return new TrainPreImgTaskVo()
            .setImgList(results)
            .setPreTaskId(String.valueOf(preTaskId))
            .setNewStatus(task.getNewStatus())
            .setTaskId(task.getTaskId())
            .setAdditionTags(additionTags)
            .setTranslateTagMap(cacheMap);
    }

    /*
     * 训练数据目录挂载情况：/home/lora-scripts/train-data/*
     * 训练后的模型输出目录挂载情况：/home/lora-scripts/output/*
     * SD模型存储目录挂载情况：/home/stable-diffusion-webui/models/Lora/*
     */

    /**
     * 提交预处理任务
     * @param images                        图片集合
     * @param threshold                     阈值
     * @param interrogatorModel             识别模型名称
     * @param batchOutputActionOnConflict   冲突时的处理方式
     * @return  预处理任务id
     * @throws IOException  IO异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitPreImg(MultipartFile[] images, Double threshold, String interrogatorModel, String batchOutputActionOnConflict) throws IOException {
        if(images==null || images.length==0){
            throw new ServiceException("训练数据不能为空!");
        }
        // 校验用户训练次数
        Long userId = LoginHelper.getUserId();
        userService.checkTrainTimesOfMember(userId);

        // 获取通用配置
        SdCommonConfig config = sdCommonConfigService.selectOne();
        int canMoreSubmitPreImgNum = config==null||config.getPreImgMaxNum()==null||config.getPreImgMaxNum()<=0?50:config.getPreImgMaxNum();
        int canLeastSubmitPreImgNum = config==null||config.getPreImgMinNum()==null||config.getPreImgMinNum()<=0?7:config.getPreImgMinNum();
        if (images.length<canLeastSubmitPreImgNum) {
            throw new ServiceException("预处理图最少要提交"+canLeastSubmitPreImgNum+"张!");
        }
        else if (images.length>canMoreSubmitPreImgNum) {
            throw new ServiceException("预处理图最多能提交"+canMoreSubmitPreImgNum+"张!");
        }
        // 先判断是否已存在模型训练任务
        SdTrainTask task = sdTrainTaskService.selectDetailByUserId(userId);
        if (task!=null && StringUtils.isBlank(task.getAdditionTag())) {
            throw new ServiceException("请至少填入一个共性词[缺少共性词]!");
        }
        // 不存在训练任务 或者 已提交训练（训练队列中、训练中、已完成、已失败）
        if (task==null || task.getNewStatus()==3 || task.getNewStatus()==4 || task.getNewStatus()==5 || task.getNewStatus()==6) {
            String username = LoginHelper.getUsername();
            String preTaskId = IdUtil.getSnowflakeNextIdStr();
            // 目录挂载情况：/home/lora-scripts/train-data
            String trainDataDir = "/home/lora-scripts/train-data/";
            String parentFileUrl = trainDataDir+DateUtil.formatDate(new Date())+"/"+userId+"/"+preTaskId;
            File parent = new File(parentFileUrl);
            // 将图片保存到指定的文件夹下；如果父文件夹不存在，就创建
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
            params.put("path",parentFileUrl);
            params.put("replace_underscore",true);
            params.put("threshold",threshold!=null?threshold:0.5d);
            // 提交预处理任务
            sdTrainTaskService.insert(userId, username, params, images.length, preTaskId, new Date());
            params.put("taskId",preTaskId);
            RedisUtils.setCacheObject(PRE_IMG_PROGRESS_TOTAL+preTaskId, images.length);
            RedisUtils.setCacheObject(PRE_IMG_PROGRESS_COMPLETE+preTaskId, 0);
            // 添加到预处理任务队列中
            RedisUtils.setCacheListValue(PRE_IMG_TASK_QUEUE_LIST_V1,preTaskId);
            // 创建该数据集文件夹监听任务
            CompletableFuture.runAsync(()->createFileDirMonitor(parent,preTaskId),executor);
            try {
                rabbitTemplate.convertAndSend(MqConstant.SD_PRE_IMG_TASK_EXCHANGE, MqConstant.SD_PRE_IMG_TASK_ROUTING_KEY,params,new CorrelationData(preTaskId));
            }
            catch (Exception e) {
                sdTrainTaskService.deleteById(preTaskId);
                delPreTaskData(preTaskId);
            }
            return preTaskId;
        }
        else if (task.getNewStatus()==0) {
            throw new ServiceException("当前用户已存在[排队中]的[预处理]任务!");
        }
        else if (task.getNewStatus()==1) {
            throw new ServiceException("当前用户已存在[进行中]的[预处理]任务!");
        }
        else {
            throw new ServiceException("当前用户已存在[未开始]的[训练]任务,请先提交预处理完成的训练任务!");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitPreImgV2(MultipartFile[] images, Double threshold, String interrogatorModel, String batchOutputActionOnConflict) throws IOException {
        final String username = LoginHelper.getUsername();
        if(images==null || images.length==0){
            throw new ServiceException("训练数据不能为空!");
        }
        // 校验用户训练次数
        Long userId = LoginHelper.getUserId();
        userService.checkTrainTimesOfMember(userId);

        // 获取通用配置
        SdCommonConfig config = sdCommonConfigService.selectOne();
        int canMoreSubmitPreImgNum = config==null||config.getPreImgMaxNum()==null||config.getPreImgMaxNum()<=0?50:config.getPreImgMaxNum();
        int canLeastSubmitPreImgNum = config==null||config.getPreImgMinNum()==null||config.getPreImgMinNum()<=0?7:config.getPreImgMinNum();
        if (images.length<canLeastSubmitPreImgNum) {
            throw new ServiceException("预处理图最少要提交"+canLeastSubmitPreImgNum+"张!");
        }
        else if (images.length>canMoreSubmitPreImgNum) {
            throw new ServiceException("预处理图最多能提交"+canMoreSubmitPreImgNum+"张!");
        }
        SdTrainTask task = sdTrainTaskService.selectDetailByUserId(userId);
        if (task!=null && task.getNewStatus()==0) {
            throw new ServiceException("当前用户已存在[排队中]的[预处理]任务!");
        }
        else if (task!=null && task.getNewStatus()==1) {
            throw new ServiceException("当前用户已存在[进行中]的[预处理]任务!");
        }
        else if (task!=null && task.getNewStatus()==2){
            throw new ServiceException("当前用户已存在已完成的预处理任务,请先提交训练!");
        }

        // 预处理任务ID
        String preTaskId = IdUtil.getSnowflakeNextIdStr();
        // 目录挂载情况：/home/lora-scripts/train-data
        String trainDataDir = "/home/lora-scripts/train-data/";
//        String trainDataDir = "D:\\project\\ai_project\\train-data\\";
        String parentFileUrl = trainDataDir+DateUtil.formatDate(new Date())+"/"+userId+"/"+preTaskId;
        File parent = new File(parentFileUrl);
        // 将图片保存到指定的文件夹下；如果父文件夹不存在，就创建
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
        params.put("interrogator_model",StrUtil.isEmptyIfStr(interrogatorModel)?"wd-convnext-v3":interrogatorModel);
        params.put("path",parentFileUrl);
        params.put("replace_underscore",true);
        params.put("threshold",threshold!=null?threshold:0.5d);
        // 提交预处理任务
        sdTrainTaskService.insert(userId, username, params, images.length, preTaskId, new Date());
        params.put("taskId",preTaskId);
        params.put("task_id",preTaskId);
        // 添加到预处理任务队列中
        RedisUtils.setCacheListValue(PRE_IMG_TASK_QUEUE_LIST_V2,preTaskId);
        // 创建该数据集文件夹监听任务(主要是处理标签文件翻译)
        CompletableFuture.runAsync(()->createFileDirMonitorV2(parent,preTaskId),executor);
        try {
            rabbitTemplate.convertAndSend(MqConstant.SD_PRE_IMG_TASK_EXCHANGE, MqConstant.SD_PRE_IMG_TASK_ROUTING_KEY,params,new CorrelationData(preTaskId));
        }
        catch (Exception e) {
            sdTrainTaskService.deleteById(preTaskId);
            delPreTaskData(preTaskId);
        }
        return preTaskId;
    }
    @RabbitListener(queues = MqConstant.SD_PRE_IMG_TASK_QUEUE)
    public void preImgTask(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Map<String,Object> params = JSON.parseObject(body, Map.class);
        String preTaskId = String.valueOf(params.get("taskId"));

        // 随机获取一个可用的训练GPU卡
        SdGpuPool sdGpuPool = sdGpuPoolService.selectRandomEnableOfOneGpu(1);
        if (sdGpuPool==null) {
            log.warn("图片预处理>>>>>>>>>训练卡池中暂无可使用的GPU服务,CPU休眠100ms,请等待!");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("图片预处理>>>>>>>>>训练卡池中暂无可使用的GPU服务,CPU休眠异常：{}",e.getMessage());
            }
            return;
        }
        // 数据集图片目录：path -> /home/lora-scripts/train-data/{userId}/{preTaskId}
        try{
            // 通过gpu卡池中的训练数据集目录
            log.warn("图片预处理>>>>>>>>>任务ID[{}],开始进行预处理图片", preTaskId);
            Forest.post("/api/interrogate").address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().addBody(params).execute();
            // 开始执行预处理任务
            sdTrainTaskService.startPreTask(String.valueOf(preTaskId),new Date());
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            log.error("图片预处理>>>>>>>>>任务ID[{}],预处理图片异常,重新进入队列首位：{}", preTaskId,e.getMessage());
            channel.basicNack(deliveryTag, false, true);
        }
    }
    @Override
    public boolean getPreImgProgress(String preTaskId) {
        Integer newStatus = sdTrainTaskService.selectNewStatusById(preTaskId);
         if (newStatus == null) {
             return true;
         }
        if (newStatus>=2) {
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
                sdTrainTaskService.completePreTask(preTaskId,null,new Date());
            }
            finally {
                delPreTaskData(preTaskId);
            }
        }
        return flag;
    }
    @Override
    public int getPreImgProgressV2(String preTaskId) {
        Integer newStatus = sdTrainTaskService.selectNewStatusById(preTaskId);
        if (newStatus == null) {
            return 100;
        }
        if (newStatus>=2) {
            // 已完成预处理训练，移除redis中预处理任务数据
            delPreTaskData(preTaskId);
            return 100;
        }
        Integer progress = RedisUtils.getCacheMapValue(PRE_IMG_PROCESS+preTaskId,"progress");
        String status = RedisUtils.getCacheMapValue(PRE_IMG_PROCESS+preTaskId,"status");
        String reason = RedisUtils.getCacheMapValue(PRE_IMG_PROCESS+preTaskId,"error");
        // 没有进度，但是有预处理任务，则返回进度=0
        if (progress==null && newStatus<=1) {
            return 0;
        }
        else if (progress==null) {
            return progress;
        }

        // 完成预处理任务
        if (progress>=100 || "FAILED".equals(status)) {
            try{
                // 休眠1秒，确保任务状态更新到数据库
                Thread.sleep(1000);
                sdTrainTaskService.completePreTask(preTaskId, reason, new Date());
            }
            catch (InterruptedException e) {
                log.error("图片预处理>>>>>>>>>任务ID[{}],休眠0.5s：{}", preTaskId,e.getMessage());
            }
            finally {
                delPreTaskData(preTaskId);
            }
        }
        return progress;
    }
    /** 预处理任务 end **/

    /** 删除预处理任务数据 **/
    private void delPreTaskData(String preTaskId) {
        WatchMonitor watchMonitor = MONITOR_MAP.get(preTaskId);
        if (watchMonitor!=null) {
            watchMonitor.interrupt();
            watchMonitor.close();
            MONITOR_MAP.remove(preTaskId);
        }
        RedisUtils.delCacheListValue(PRE_IMG_TASK_QUEUE_LIST_V1,preTaskId);
        RedisUtils.delCacheListValue(PRE_IMG_TASK_QUEUE_LIST_V2,preTaskId);
        RedisUtils.deleteKey(PRE_IMG_PROCESS+preTaskId);
        RedisUtils.deleteMultiObject(PRE_IMG_PROGRESS_TOTAL+preTaskId,PRE_IMG_PROGRESS_COMPLETE+preTaskId);
    }

    /** 删除预处理图片数据 **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delPreImg(String imgUrl, String preTaskId) {
        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailById(preTaskId);
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
        sdTrainTaskService.reduceImgNum(preTaskId);
        // 删除图片数量小于1的任务
        boolean flag = sdTrainTaskService.deleteByIdAndPicNumIsZero(preTaskId);
        if (flag) {
            // 已完成预处理训练，移除redis中预处理任务数据
            delPreTaskData(preTaskId);
        }

        // 处理图片地址
        String imgUrlString = dealTrainDataSetPath(imgUrl);

        String txtUrl = imgUrlString
            .replace(".jpg", ".txt").replace(".JPG", ".txt")
            .replace(".jpeg", ".txt").replace(".JPEG", ".txt")
            .replace(".png", ".txt").replace(".PNG", ".txt");
        String npzUrl = imgUrlString
            .replace(".jpg", ".npz").replace(".JPG", ".npz")
            .replace(".jpeg", ".npz").replace(".JPEG", ".npz")
            .replace(".png", ".npz").replace(".PNG", ".npz");
        FileUtil.del(new File(imgUrlString));
        FileUtil.del(new File(txtUrl));
        FileUtil.del(new File(npzUrl));
    }

    /** 修改预处理图片上的标签 **/
    @Override
    public void modifyPreImgTag(SdTrainPreImgDto dto) {
        String preTaskId = dto.getPreTaskId();
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

        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存在!");
        }
        else if (sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理进行中,不可进行添加标签操作!");
        }
        else if (sdTrainTask.getNewStatus()>2) {
            throw new ServiceException("图片预处理已完成,不可进行添加标签操作!");
        }
        // 处理图片地址
        String imgUrlString = dealTrainDataSetPath(dto.getImgUrl());

        String txtUrl = imgUrlString
            .replace(".jpg", ".txt").replace(".JPG", ".txt")
            .replace(".jpeg", ".txt").replace(".JPEG", ".txt")
            .replace(".png", ".txt").replace(".PNG", ".txt");
        String content = tags.stream().map(String::valueOf).collect(Collectors.joining(", "));
        // 覆盖
        FileUtil.writeString(content,txtUrl, CharsetUtil.UTF_8);
        // 添加翻译
        Map<String, String> newTagMap = dto.getTags().stream().filter(e->StrUtil.isNotEmpty(e.getTagZh())).collect(Collectors.toMap(SdTrainTagDto::getTag, SdTrainTagDto::getTagZh, (v1, v2) -> v1));
        RedisUtils.setCacheMap(TRAIN_TAG_TRANSLATE_MAP + preTaskId,newTagMap);
    }

    /** 添加共性词标签 **/
    @Override
    public void insertAdditionTag(SdTrainAdditionTagDto dto) {
        final String preTaskId = dto.getPreTaskId();
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

        Set<Object> tagSet = RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + preTaskId);
        if (CollectionUtil.isNotEmpty(tagSet) && tagSet.size()>=3) {
            throw new ServiceException("当前训练任务的共性词已达上限,不可添加!");
        }
        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存在!");
        }
        else if (sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务已开始,不可进行添加共性词操作!");
        }
        else if (sdTrainTask.getNewStatus()>2) {
            throw new ServiceException("模型训练已开始,不可进行添加共性词操作!");
        }
        // 添加共性词
        JSONObject paramsJson = JSONObject.parseObject(sdTrainTask.getPreParams());
        String path = dealTrainDataSetPath(paramsJson.getString("path"));
        File preImgDir = new File(path);
        List<File> allFileList = CommonUtil.getAllFile(preImgDir);
        if (CollectionUtil.isNotEmpty(allFileList)) {
            final String finalAdditionTagEn = additionTagEn;
            allFileList.stream().filter(e->e.getName().contains(".txt")).forEach(e->{
                Optional<String> first1 = Optional.empty();
                try (Stream<String> lines = Files.lines(e.toPath(), StandardCharsets.UTF_8)) {
                    first1 = lines.findFirst();
                }
                catch (IOException ex) {
                    log.error("获取文件[{}]内容失败：",e.getPath(),ex);
                }
                first1.ifPresent(s -> {
                    s = StringEscapeUtils.unescapeJava(s);
                    FileUtil.writeString(finalAdditionTagEn + ", " + s, e.getPath(), CharsetUtil.UTF_8);
                });
            });
        }
        // 添加翻译
        RedisUtils.setCacheMapValue(TRAIN_TAG_TRANSLATE_MAP + preTaskId,additionTagEn,StrUtil.isEmptyIfStr(dto.getAdditionTagZh())?additionTagEn:dto.getAdditionTagZh());
        // 添加共性词数组
        RedisUtils.setCacheSet(TRAIN_ADDITION_LIST+ preTaskId,Collections.singleton(additionTagEn));
        // 将共性词更新到数据库
        tagSet.add(additionTagEn);
        sdTrainTaskService.updateAdditionTag(preTaskId,JSONObject.toJSONString(tagSet));
    }

    /** 删除标签 **/
    @Override
    public void delPreImgTag(SdTrainTagDelDto dto) {
        String preTaskId = dto.getPreTaskId();
        if (StrUtil.isEmptyIfStr(dto.getTag())) {
            throw new ServiceException("需要删除的共性词或标签不能为空!");
        }
        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null) {
            throw new ServiceException("图片预处理任务不存在!");
        }
        else if (sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务已开始,不可进行删除标签操作!");
        }
        else if (sdTrainTask.getNewStatus()>2) {
            throw new ServiceException("模型训练任务已开始,不可进行删除标签操作!");
        }

        // 处理图片地址
        String imgUrlString = dealTrainDataSetPath(dto.getImgUrl());

        // 判断删除的标签是否是共性词
        Set<Object> tagSet = RedisUtils.getCacheSet(TRAIN_ADDITION_LIST + preTaskId);
        String removeTag = dto.getTag();
        // 删除共性词
        if (CollectionUtil.isNotEmpty(tagSet) && tagSet.contains(removeTag)) {
            String dir = imgUrlString.substring(0, imgUrlString.lastIndexOf("/"));
            File preImgDir = new File(dir);
            // 获取当前图片所属的文件夹下的全部文件
            List<File> allFileList = CommonUtil.getAllFile(preImgDir);
            if (CollectionUtil.isNotEmpty(allFileList)) {
                // 过滤出txt文件且只对txt文件进行处理
                allFileList.stream().filter(e->e.getName().contains(".txt")).forEach(e->{
                    Optional<String> first1 = Optional.empty();
                    try (Stream<String> lines = Files.lines(e.toPath(), StandardCharsets.UTF_8)) {
                        first1 = lines.findFirst();
                    }
                    catch (IOException ex) {
                        log.error("获取文件[{}]内容失败：",e.getPath(),ex);
                    }
                    // 将需要删除的标签替换成空
                    first1.ifPresent(s -> {
                        s = StringEscapeUtils.unescapeJava(s);
                        FileUtil.writeString(s.replaceFirst(removeTag + ", ", "").replaceFirst(removeTag, ""), e.getPath(), CharsetUtil.UTF_8);
                    });
                });
            }
            // 缓存中移除当前任务对应的删除的共性词
            RedisUtils.delCacheSet(TRAIN_ADDITION_LIST + preTaskId,Collections.singleton(dto.getTag()));
        }
        // 删除标签
        else {
            File imgUrl = new File(imgUrlString
                .replace(".jpg", ".txt").replace(".JPG", ".txt")
                .replace(".jpeg", ".txt").replace(".JPEG", ".txt")
                .replace(".png", ".txt").replace(".PNG", ".txt"));
            Optional<String> first1 = Optional.empty();
            try (Stream<String> lines = Files.lines(imgUrl.toPath(), StandardCharsets.UTF_8)) {
                first1 = lines.findFirst();
            }
            catch (IOException ex) {
                log.error("获取文件[{}]内容失败：",imgUrl.getPath(),ex);
            }
            first1.ifPresent(s -> {
                s = StringEscapeUtils.unescapeJava(s);
                FileUtil.writeString(s.replace(removeTag + ", ", "").replace(removeTag,""), imgUrl.getPath(), CharsetUtil.UTF_8);
            });
        }
    }



    /** 模型训练任务 start **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void trainSdLora(SdTrainLoraDto dto) {
        String preTaskId = dto.getPreTaskId();
        if (preTaskId==null || StrUtil.isEmptyIfStr(preTaskId)) {
            throw new ServiceException("缺少图片预处理任务ID!");
        }
        // 校验用户训练次数
        Long userId = LoginHelper.getUserId();
        userService.checkTrainTimesOfMember(userId);

        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null || sdTrainTask.getNewStatus()<2) {
            throw new ServiceException("图片预处理任务不存在或未完成!");
        }
        else if (sdTrainTask.getNewStatus()>2 && sdTrainTask.getNewStatus()<5) {
            throw new ServiceException("存在未完成的训练任务!");
        }
        JSONObject paramsJson = JSONObject.parseObject(sdTrainTask.getPreParams());
        // path -> /home/lora-scripts/train-data/{userId}/{preTaskId}
        String path = dealTrainDataSetPath(paramsJson.getString("path"));
        if (StringUtils.isBlank(path)) {
            throw new ServiceException("缺少图片预处理后的数据集路径!");
        }
        Map<String,Object> trainParams = CommonUtil.createSdTrainParam(dto,path,preTaskId);

        JSONObject msg = new JSONObject();
        msg.put("trainParams",trainParams);
        msg.put("preTaskId",preTaskId);
        msg.put("trainVersion","V1");
        msg.put("modelName",dto.getModelName());

        // 提交训练任务
        sdTrainTaskService.submitTrainTask(preTaskId,dto.getModelName(),JSONObject.toJSONString(trainParams),new Date());
        // 将任务添加到任务列表中
        RedisUtils.setCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);

        rabbitTemplate.convertAndSend(MqConstant.SD_TRAIN_TASK_EXCHANGE, MqConstant.SD_TRAIN_TASK_ROUTING_KEY,msg,new CorrelationData(preTaskId));
        // 扣除训练次数
        userService.deductedTrainTimes(sdTrainTask.getCrtUserId());
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void trainSdLoraV2(SdTrainLoraDto dto) {
        final String preTaskId = dto.getPreTaskId();
        if (preTaskId==null || StrUtil.isEmptyIfStr(preTaskId)) {
            throw new ServiceException("缺少图片预处理任务ID!");
        }
        // 校验用户训练次数
        Long userId = LoginHelper.getUserId();
        userService.checkTrainTimesOfMember(userId);

        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailById(preTaskId);
        if (sdTrainTask==null) {
            throw new ServiceException("前置任务不存在,不可进行训练!");
        }
        // 查询是否存在队列或进行中的三个以上的任务
        Integer runningTaskCount = sdTrainTaskService.countRunningTaskByUserId(userId);
        if (runningTaskCount!=null && runningTaskCount>=3) {
            throw new ServiceException("当前可提交的训练任务数量已达上限,请先等待训练完成!");
        }

        JSONObject paramsJson = JSONObject.parseObject(sdTrainTask.getPreParams());
        // path -> /home/lora-scripts/train-data/{userId}/{preTaskId}
        String path = dealTrainDataSetPath(paramsJson.getString("path"));
        if (StringUtils.isBlank(path)) {
            throw new ServiceException("缺少前置任务的数据集路径!");
        }
        Map<String,Object> trainParams = CommonUtil.createSdTrainParam(dto,path,preTaskId);

        JSONObject msg = new JSONObject();
        msg.put("trainParams",trainParams);
        msg.put("preTaskId",preTaskId);
        msg.put("trainVersion","V2");
        msg.put("modelName",dto.getModelName());

        // 提交训练任务
        sdTrainTaskService.submitTrainTask(preTaskId, dto.getModelName(), JSONObject.toJSONString(trainParams), new Date());
        // 将任务添加到任务列表中
        RedisUtils.setCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);

        rabbitTemplate.convertAndSend(MqConstant.SD_TRAIN_TASK_EXCHANGE, MqConstant.SD_TRAIN_TASK_ROUTING_KEY,msg,new CorrelationData(preTaskId));
        // 扣除训练次数
        userService.deductedTrainTimes(sdTrainTask.getCrtUserId());
    }
    @RabbitListener(queues = MqConstant.SD_TRAIN_TASK_QUEUE)
    public void trainTask(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        JSONObject params = JSON.parseObject(body, JSONObject.class);
        final String preTaskId = params.getString("preTaskId");
        final String trainVersion = params.getString("trainVersion");
        Integer newStatus = sdTrainTaskService.selectNewStatusById(preTaskId);
        if (newStatus == null || newStatus<=2 ||  newStatus>5) {
            log.error("模型训练任务>>>>>>>>>前置任务[{}]不存在或未开始或已结束,不可进行训练!",preTaskId);
            if ("V1".equals(trainVersion) || StringUtils.isBlank(trainVersion)) {
                RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
            }
            else {
                RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
            }
            RedisUtils.deleteKey(TRAIN_PROCESS+preTaskId);
            returnGpuFromTrainGpuPool(preTaskId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        // 判断是否还有训练服务可以使用
        SdGpuPool sdGpuPool = getGpuFromTrainGpu(preTaskId,null, null);
        if (sdGpuPool==null) {
            log.warn("模型训练任务>>>>>>>>>训练卡池中暂无可使用的GPU服务,CPU休眠200ms,请等待!");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("模型训练任务>>>>>>>>>训练卡池中暂无可使用的GPU服务,CPU休眠异常：{}",e.getMessage());
            }
            channel.basicNack(deliveryTag, false, true);
            return;
        }
        String taskId = null;
        try{
            Map<String,Object> trainParams = JSONObject.parseObject(params.getString("trainParams"), Map.class);
            trainParams.put("gpu_ids", Collections.singletonList(sdGpuPool.getDeviceId().toString()));
            log.info("模型训练任务>>>>>>>>>任务ID[{}],开始进行模型训练任务,训练参数：{}",preTaskId,trainParams);

            JSONObject run = Forest.post("/api/run").address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().addBody(trainParams).execute(JSONObject.class);
            log.warn("模型训练任务>>>>>>>>>启动训练：{}",run);
            String status = run.getString("status");
            JSONObject data = run.getJSONObject("data");
            taskId = CollectionUtil.isEmpty(data)? null: StringUtils.isBlank(data.getString("taskId"))?data.getString("task_id"):data.getString("taskId");
            if ("error".equals(status) || "fail".equals(status)) {
                Long crtUserId = sdTrainTaskService.selectCrtUserIdById(preTaskId);
                log.error("模型训练任务>>>>>>>>>训练失败：{}",run.getString("message"));
                sdTrainTaskService.failTrainTask(preTaskId,run.getString("message"),sdGpuPool,new Date());
                if ("V1".equals(trainVersion) || StringUtils.isBlank(trainVersion)) {
                    RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
                }
                else {
                    RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
                }
                returnGpuFromTrainGpuPool(preTaskId);
                userService.returnedTrainTimes(crtUserId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 开始执行训练任务
            sdTrainTaskService.startTrainTask(preTaskId,taskId,new Date(),sdGpuPool);
            if ("V1".equals(trainVersion) || StringUtils.isBlank(trainVersion)) {
                // 添加训练任务到训练任务列表
                RedisUtils.setCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V1,taskId,preTaskId);
                // 从训练任务队列中移除
                RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
            }
            else {
                // 添加训练任务到训练任务列表
                RedisUtils.setCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V2,taskId,preTaskId);
                RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
            }
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
            RedisUtils.deleteKey(TRAIN_PROCESS+taskId);
            returnGpuFromTrainGpuPool(preTaskId);
            log.error("模型训练任务>>>>>>>>>任务ID[{}],训练异常,重新进入队列：", preTaskId, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
    @Override
    public TrainProcessDataVo trainProgress(String taskId) {
        SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailByTaskId(taskId);
        if (sdTrainTask==null || sdTrainTask.getNewStatus()==2 || sdTrainTask.getNewStatus()>4) {
            if (sdTrainTask!=null) {
                long preTaskId = sdTrainTask.getId();
                // 将当前任务从任务列表中移除
                RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V1, taskId);
                RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
                returnGpuFromTrainGpuPool(String.valueOf(preTaskId));
            }
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED");
        }
        long preTaskId = sdTrainTask.getId();
        SdGpuPool sdGpuPool = RedisUtils.getCacheObject(TRAIN_GPU_TASK+ preTaskId);
        if (sdGpuPool==null) {
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V1, taskId);
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
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
            Date endTime = new Date();
            sdTrainTaskService.completeTrainTask(map.get("id"), endTime);
            sdTrainTask.setEndTime(endTime);
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V1, taskId);
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V1,preTaskId);
            returnGpuFromTrainGpuPool(String.valueOf(preTaskId));
            CompletableFuture.runAsync(()-> dealTrainModelFile(sdTrainTask),executor)
            .exceptionally(e -> {
                log.error("处理模型文件出现异常：{}",e.getMessage());
                return null;
            });
        }
        return new TrainProcessDataVo().setId(map.get("id")).setStatus(map.get("status"));
    }
    @Override
    public TrainProcessDataVo trainProgressV2(String taskId) {
        // preTaskId、newStatus
        JSONObject taskInfo = sdTrainTaskService.selectNewStatusByTaskId(taskId);
        if (taskInfo==null) {
            // 将当前任务从任务列表中移除
            RedisUtils.deleteKey(TRAIN_PROCESS+taskId);
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V2,taskId);
            throw new ServiceException("任务不存在!");
        }
        final String preTaskId = taskInfo.getString("preTaskId");
        final int newStatus = taskInfo.getIntValue("newStatus");
        if (StringUtils.isBlank(preTaskId) || newStatus<=2 || newStatus>4) {
            // 将当前任务从任务列表中移除
            RedisUtils.deleteKey(TRAIN_PROCESS+taskId);
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V2,taskId);
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
            returnGpuFromTrainGpuPool(preTaskId);
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED").setProcess(100);
        }
        if (newStatus>=5) {
            // 将当前任务从任务列表中移除
            RedisUtils.deleteKey(TRAIN_PROCESS+taskId);
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V2,taskId);
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
            returnGpuFromTrainGpuPool(preTaskId);
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED").setProcess(100);
        }
        // 从redis中获取训练任务进度
        String status = RedisUtils.getCacheMapValue(TRAIN_PROCESS+taskId,"status");
        Integer progress = RedisUtils.getCacheMapValue(TRAIN_PROCESS+taskId,"progress");
        String reason = RedisUtils.getCacheMapValue(TRAIN_PROCESS+taskId,"reason");
        Integer remainingTime = RedisUtils.getCacheMapValue(TRAIN_PROCESS+taskId,"remainingTime");
        if (StrUtil.isEmptyIfStr(status) || "CREATED".equals(status)) {
            return new TrainProcessDataVo().setId(taskId).setStatus("CREATED").setProcess(progress);
        }
        else if ("FINISHED".equals(status) || "TERMINATED".equals(status) || "FAILED".equals(status)) {
            sdTrainTaskService.completeTrainTask(taskId,new Date());
            RedisUtils.deleteKey(TRAIN_PROCESS+taskId);
            RedisUtils.delCacheMapValue(TRAIN_MODEL_PROGRESS_TASK_MAP_V2,taskId);
            RedisUtils.delCacheListValue(TRAIN_TASK_QUEUE_LIST_V2,preTaskId);
            returnGpuFromTrainGpuPool(preTaskId);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("训练任务>>>>>>>>>CPU休眠200ms失败：{}",e.getMessage());
            }
            SdTrainTask sdTrainTask = sdTrainTaskService.selectDetailByTaskId(taskId);
            CompletableFuture.runAsync(()-> dealTrainModelFile(sdTrainTask),executor)
            .exceptionally(e -> {
                log.error("处理模型文件出现异常：",e);
                return null;
            });
            return new TrainProcessDataVo().setId(taskId).setStatus("FINISHED").setProcess(100);
        }
        return new TrainProcessDataVo().setId(taskId).setStatus(status).setProcess(progress).setReason(reason).setRemainingTime(remainingTime);
    }
    private void dealTrainModelFile(SdTrainTask sdTrainTask) {
        // 预处理任务ID
        final long preTaskId = sdTrainTask.getId();
        // 训练参数
        JSONObject trainParams = JSONObject.parseObject(sdTrainTask.getTrainParams());
        // 训练后的模型原始名称
        String oldModelName = trainParams.getString("output_name");
        // 预处理参数
        JSONObject preParams = JSONObject.parseObject(sdTrainTask.getPreParams());
        // 处理模型封面图片
        String path = dealTrainDataSetPath(preParams.getString("path"));
        dealModelImg(path,preTaskId,oldModelName);

        // 训练后的模型所在目录
        //TODO windows
        String oldModelDir = "/home/lora-scripts/output/"+ preTaskId;
//        String oldModelDir = "D:\\project\\ai_project\\models\\Lora\\train\\"+ preTaskId;
        // 原始模型名称
        String modelName = oldModelName+"."+trainParams.getString("save_model_as");
        // 最后一轮训练的模型名称后缀
        String suffix = String.format("%06d",trainParams.getIntValue("max_train_epochs"));
        // 重命名最后一轮生成的模型名称
        String newModelName = oldModelName+"-"+suffix+ "." + trainParams.getString("save_model_as");
        //TODO windows
        File startFile = new File(oldModelDir+"/"+modelName);
//        File startFile = new File(oldModelDir+"\\"+modelName);
        //TODO windows
        File renameFile = new File(oldModelDir+"/"+newModelName);
//        File renameFile = new File(oldModelDir+"\\"+newModelName);
        log.warn("[训练完成][任务ID：{}]>>>>>>>>>修改最后一轮训练的模型名称：{}->{}",preTaskId,startFile.getPath(),renameFile.getPath());
        startFile.renameTo(renameFile);

        // 全部模型移动到sd的models/Lora目录下
        //TODO windows
        String modelDir = "/home/stable-diffusion-webui/models/Lora/";
//        String modelDir = "D:\\project\\ai_project\\models\\Lora\\sd\\";
        log.warn("[训练完成][任务ID：{}]>>>>>>>>>移动模型文件到sd的models/Lora目录下：{}->{}",preTaskId,oldModelDir,modelDir);
        try{
            File[] files = FileUtil.ls(oldModelDir);
            if (files != null) {
                File destDir = new File(modelDir);
                for (File srcFil : files) {
                    FileUtil.move(srcFil,destDir,true);
                }
            }
        }
        catch (Exception e) {
            log.error("[训练完成][任务ID：{}]>>>>>>>>>移动模型文件到sd的models/Lora目录下：{}->{}报错：{}",preTaskId,oldModelDir,modelDir,e.getMessage());
        }

        // 删除原始lora模型目录
        File outputDir = new File(oldModelDir);
        log.warn("[训练完成][任务ID：{}]>>>>>>>>>删除训练模型[{}]的所在原目录：{}",preTaskId,modelName,outputDir.getPath());
        FileUtil.del(outputDir);
        // 删除redis
        RedisUtils.deleteMultiObject(TRAIN_ADDITION_LIST+ preTaskId,TRAIN_TAG_TRANSLATE_MAP+ preTaskId);
        // 通知刷新lora模型
        applicationEventPublisher.publishEvent(new RefreshLoraEvent(true));

        // 发送微信公众号消息
        JSONObject wxMsg = new JSONObject();
        wxMsg.put("preTaskId", preTaskId);
        wxMsg.put("taskId",sdTrainTask.getTaskId());
        wxMsg.put("belongUserId",sdTrainTask.getCrtUserId());
        wxMsg.put("belongUserName",sdTrainTask.getCrtUserName());
        wxMsg.put("type","MODEL_TRAIN");
        wxMsg.put("modelName",sdTrainTask.getModelName());
        wxMsg.put("oldModelName",oldModelName);
        wxMsg.put("startTime",DateUtil.formatDateTime(sdTrainTask.getStartTime()));
        wxMsg.put("endTime",DateUtil.formatDateTime(sdTrainTask.getEndTime()));
        //TODO windows
        rabbitTemplate.convertAndSend(MqConstant.WX_MSG_EXCHANGE, MqConstant.WX_MSG_ROUTING_KEY,wxMsg);
    }
    /**
     * 处理数据集的第一张图片作为模型图片
     *
     * @param path         训练数据集目录
     * @param preTaskId    系统内训练任务ID
     * @param oldModelName 训练的模型原始名称
     */
    private void dealModelImg(String path, long preTaskId, String oldModelName) {
        // 获取数据集中的第一张图片作为训练模型的封面
        // path -> /home/lora-scripts/train-data/{userId}/{preTaskId}/*
        File[] imgs = FileUtil.ls(path+CommonUtil.suggestNumRepeat());
        String modelImgDir = "/home/stable-diffusion-webui/models/Lora/";
//        String modelImgDir = "D:\\project\\ai_project\\models\\Lora\\train\\"+preTaskId+"\\";
        if (imgs != null) {
            Optional<File> first = Arrays.stream(imgs).filter(e ->
                e.getName().contains(".png") || e.getName().contains(".PNG") ||
                    e.getName().contains(".jpg") || e.getName().contains(".JPG") ||
                    e.getName().contains(".jpeg") || e.getName().contains(".JPEG")
            ).findFirst();
            if (first.isPresent()) {
                File img = first.get();
                String newModelImgPath = modelImgDir+oldModelName+img.getName().substring(img.getName().lastIndexOf("."));
                log.warn("[训练完成][任务ID：{}]>>>>>>>>>复制数据集中的第一张图片作为模型图片：{}->{}",preTaskId,img.getPath(),newModelImgPath);
                try {
                    FileInputStream inputStream = new FileInputStream(img);
                    FileOutputStream outputStream = new FileOutputStream(newModelImgPath);
                    IoUtil.copy(inputStream,outputStream);
                } catch (FileNotFoundException e) {
                    log.error("[训练完成][任务ID：{}]>>>>>>>>>需要复制的模型图片不存在：{}",preTaskId,e.getMessage());
                }
            }
        }
    }
    /** 模型训练任务 end **/


    /** 获取模型训练的数据集 **/
    @Override
    public List<JSONObject> getModelTrainDateList(String preTaskId) throws IOException {
        SdTrainTask task = sdTrainTaskService.selectDetailById(preTaskId);
        if (task==null) {
            return Collections.emptyList();
        }
        JSONObject params = JSONObject.parseObject(task.getPreParams());
        // 兼容容器路径
        String path = dealTrainDataSetPath(params.getString("path"));

        File preImgDir = new File(path +CommonUtil.suggestNumRepeat());
        if (!preImgDir.exists()) {
            preImgDir = new File(path);
        }
        // 判断文件夹是否存在
        if (!preImgDir.exists()) {
            throw new ServiceException("当前模型的预处理图片已被删除!");
        }
        List<File> allFileList = CommonUtil.getAllFile(preImgDir);
        // 将数据分组
        Map<String, List<File>> group = allFileList.stream().collect(Collectors.groupingBy(
            e -> {
                String fileName = e.getName();
                int dotIndex = fileName.lastIndexOf('.');
                return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
            },
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    list.sort(Comparator.comparing(File::getName));
                    return list;
                }
            )
        ));
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
                try (Stream<String> lines = Files.lines(txtFile.toPath(), StandardCharsets.UTF_8)) {
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

    /** 处理训练数据集路径 **/
    private String dealTrainDataSetPath(String path) {
        if (path.startsWith("/train-data")) {
            path = "/home/lora-scripts"+path;
        }
        else if (path.startsWith("/lora-scripts")) {
            path = "/home"+path;
        }
        return path;
    }

    /** 创建文件夹监听器 **/
    private void createFileDirMonitor(File dir, String preTaskId) {
        // 只监听目录的创建事件
        WatchMonitor watchMonitor = WatchMonitor.create(dir, WatchMonitor.ENTRY_CREATE);
        watchMonitor.setWatcher(new SimpleWatcher() {
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                Object context = watchEvent.context();
                log.warn("创建：{}-->{}", path, context);
                RedisUtils.incrAtomicValue(PRE_IMG_PROGRESS_COMPLETE+preTaskId);
                File txtFile = new File(path.toFile().getPath()+"/"+context);
                try (Stream<String> lines = Files.lines(txtFile.toPath(), StandardCharsets.UTF_8)) {
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
                                    RedisUtils.setCacheMapValue(TRAIN_TAG_TRANSLATE_MAP + preTaskId,enStr,zhStr);
                                }
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    log.error("获取文件[{}]内容失败：",path,ex);
                }
            }
        });
        // 启动监听
        watchMonitor.start();
        MONITOR_MAP.put(preTaskId,watchMonitor);
    }

    /** 创建文件夹监听器 **/
    private void createFileDirMonitorV2(File dir, String preTaskId) {
        // 只监听目录的创建事件
        WatchMonitor watchMonitor = WatchMonitor.create(dir, WatchMonitor.ENTRY_CREATE);
        watchMonitor.setWatcher(new SimpleWatcher() {
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                Object context = watchEvent.context();
                log.warn("创建：{}-->{}", path, context);
                File txtFile = new File(path.toFile().getPath()+"/"+context);
                try (Stream<String> lines = Files.lines(txtFile.toPath(), StandardCharsets.UTF_8)) {
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
                                        log.error("标签文件翻译失败：",ex);
                                    }
                                }
                                if (StrUtil.isNotEmpty(zhStr) && !enStr.equals(zhStr)) {
                                    RedisUtils.setCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, enStr, zhStr);
                                    RedisUtils.setCacheMapValue(TRAIN_TAG_TRANSLATE_MAP + preTaskId,enStr,zhStr);
                                }
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    log.error("获取文件[{}]内容失败：",path,ex);
                }
            }
        });
        // 启动监听
        watchMonitor.start();
        MONITOR_MAP.put(preTaskId,watchMonitor);
    }

    /** 下线指定GPU **/
    @Override
    public void stopGpuPool(SdGpuPool sdGpuPool) {
        SdGpuPool gpuPool = getGpuFromTrainGpu(null, sdGpuPool, false);
        if (gpuPool!=null) {
            throw new ServiceException("当前GPU正在使用!");
        }
    }
    /** 上线指定GPU **/
    @Override
    public void startGpuPool(SdGpuPool sdGpuPool) {
        SdGpuPool gpuPool = getGpuFromTrainGpu(null, sdGpuPool, true);
        if (gpuPool!=null) {
            throw new ServiceException("当前GPU正在使用!");
        }
    }


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
}
