package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.Forest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Joiner;
import com.rabbitmq.client.Channel;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.OssService;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.BeanCopyUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.FileUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.vo.*;
import com.sutran.sd.draw.service.*;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.entity.UploadResult;
import com.sutran.sd.oss.factory.OssFactory;
import com.sutran.sd.draw.domain.dto.ImgSendThirdDto;
import com.sutran.sd.draw.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.draw.domain.dto.img2img.SdImg2ImgDto;
import com.sutran.sd.draw.domain.dto.model.*;
import com.sutran.sd.draw.domain.dto.task.SdInternalProgressDto;
import com.sutran.sd.draw.domain.dto.txt2img.SdApiModelParamDto;
import com.sutran.sd.draw.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.draw.service.SdTrainTaskService;
import com.sutran.sd.draw.events.MsgSendThirdEvent;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.SdTrainTask;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.SdUserModelClassify;
import com.sutran.sd.draw.service.SdWebuiApiService;
import com.sutran.sd.draw.utils.ResultUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;
import static com.sutran.sd.common.constant.CacheConstants.*;
import static com.sutran.sd.draw.constants.CommonKey.CHECK_POINT;
import static com.sutran.sd.draw.constants.CommonKey.PNG;
import static com.sutran.sd.draw.constants.DrawApi.*;
import static com.sutran.sd.draw.mq.MqConstant.*;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "unchecked", "LoggingSimilarMessage", "AlibabaUndefineMagicConstant", "AlibabaMethodTooLong"})
@Slf4j
@Service
@RequiredArgsConstructor
public class SdWebuiApiServiceImpl implements SdWebuiApiService {

    private final SdUserModelService sdUserModelService;
    private final SdUserModelLogService sdUserModelLogService;
    private final SdUserModelFileService sdUserModelFileService;
    private final SdUserTaskService sdUserTaskService;
    private final SdUserModelClassifyService sdUserModelClassifyService;
    private final OssService ossService;
    private final SdTrainTaskService sdTrainTaskService;
    private final RabbitTemplate rabbitTemplate;
    private final SdGpuPoolService sdGpuPoolService;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Resource(name = "threadPoolTaskExecutor")
    private Executor executor;
    @Value("${sd.sampler:DPM++ 2M Karras}")
    private String sdSampler;
    /** 绘图任务锁 **/
    private final static Lock DRAW_LOCK = new ReentrantLock();

    /**
     * 获取基础大模型列表
     * @return  List<CheckPointVo>
     */
    @Override
    public List<CheckPointVo> listCheckpointModels() {
        List<SdGpuPool> list = sdGpuPoolService.getList(0);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取当前已有的gpu卡池
        List<SdGpuPool> sdGpuPools = list.stream().filter(e -> e.getType() == 0 && e.getIsEnable()==1).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(sdGpuPools)) {
            return Collections.emptyList();
        }
        SdGpuPool sdGpuPool = sdGpuPools.get(new Random().nextInt(sdGpuPools.size()));
        List<CheckPointVo> vos = new ArrayList<>();
        try{
            // 获取基础大模型列表
            String response = Forest.get(SD_MODELS_API).address(sdGpuPool.getHost(),sdGpuPool.getPort()).contentTypeJson().executeAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            vos = objectMapper.readValue(response, TypeFactory.defaultInstance().constructCollectionType(List.class, CheckPointVo.class));
        }
        catch (Exception e) {
            log.error("获取大模型异常：",e);
        }
        if (CollectionUtil.isEmpty(vos)) {
            return Collections.emptyList();
        }
        String checkpoint = RedisUtils.getCacheObject(CHECK_POINT);
        vos.forEach(e-> e.setUseStatus(e.getTitle().equals(checkpoint)?1:0));
        return vos;
    }


    /**
     * 切换基础大模型
     * @param title  模型名称
     */
    @Override
    public void checkpointOptions(String title) {
        Map<String,String> param = new HashMap<>(2);
        param.put("sd_model_checkpoint",title);
        List<SdGpuPool> list = sdGpuPoolService.getList(0);
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        // 获取当前已有的gpu卡池
        List<SdGpuPool> sdGpuPools = list.stream().filter(e -> e.getType() == 0 && e.getIsEnable()==1).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(sdGpuPools)) {
            return;
        }
        SdGpuPool sdGpuPool = sdGpuPools.get(new Random().nextInt(sdGpuPools.size()));
        try{
            // 刷新模型
            Forest.post(OPTIONS_API).address(sdGpuPool.getHost(),sdGpuPool.getPort()).addBody(param).contentTypeJson().execute();
            log.warn("[SD]>>>>>>>>>切换基础模型：{}",title);
            // 存储当前使用模型
            RedisUtils.setCacheObject(CHECK_POINT,title);
        }
        catch (Exception e) {
            log.error("[SD]>>>>>>>>>切换基础模型异常：{}",e.getMessage());
        }
    }


    /**
     * 刷新并获取lora模型
     */
    @Override
    public void refreshLoraModels() {
        List<SdGpuPool> list = sdGpuPoolService.getList(0);
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        // 获取当前已有的gpu卡池
        SdGpuPool sdGpuPool = sdGpuPoolService.selectRandomEnableOfOneGpu(0);
        if (sdGpuPool==null) {
            log.error("系统暂无可用绘图卡池!");
            return;
        }
        List<JSONObject> loraModelVos = new ArrayList<>();
        try{
            // 刷新模型
            Forest.post(REFRESH_LORAS_API).address(sdGpuPool.getHost(),sdGpuPool.getPort()).contentTypeJson().execute();
            // 获取lora模型列表
            loraModelVos = Forest.get(LORAS_API).address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().executeAsList();
        }
        catch (Exception e) {
            log.error("刷新并获取模型异常：{}",e.getMessage());
        }
        if (CollectionUtil.isEmpty(loraModelVos)) {
            log.error("系统暂无可用Lora模型!");
            return;
        }
        // 获取系统现有的模型hash值
        List<String> hashList = sdUserModelService.selectHashList();
        // 过滤掉已存在的
        loraModelVos = loraModelVos.stream().filter(e -> {
            JSONObject metadata = e.getJSONObject("metadata");
            String hash = metadata.getString("sshs_model_hash");
            return !hashList.contains(hash);
        }).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(loraModelVos)) {
            return;
        }
        // 处理数据库不存在的模型数据并落库
        List<SdUserModel> models = loraModelVos.stream().map(e -> {
            SdLoraModelVo lora = JSONObject.parseObject(e.toJSONString(), SdLoraModelVo.class);
            SdUserModel model = new SdUserModel().setId(IdUtil.getSnowflakeNextId()).setTitle(lora.getAlias()).setModelName(lora.getName()).setModelNameZh(lora.getName())
                .setHash(lora.getMetadata().getSshsModelHash()).setFileName(lora.getPath()).setCrtTime(new Date()).setIsOpen(1).setType(0).setPublishStatus(1)
                .setConfig(JSONObject.toJSONString(lora.getMetadata(), WriteMapNullValue));
            // 获取的模型，判断时用户训练模型还是自带模型，user_preTaskId
            String imgUrl = "/lora-img/";
            String oldImgUrl = "/home/stable-diffusion-webui/models/Lora/";

            // 用户训练的模型(携带user_)
            if (lora.getAlias().contains("user_")) {
                imgUrl += lora.getAlias();
                oldImgUrl += lora.getAlias();
                final String preTaskId = lora.getAlias().replace("user_", "");
                SdTrainTask task = sdTrainTaskService.selectDetailById(preTaskId);
                if (task!=null && StrUtil.isNotEmpty(task.getModelName())) {
                    model.setModelNameZh(task.getModelName()+(lora.getName().contains("-")?"-"+lora.getName().split("-")[1]:""));
                }
                if (task!=null) {
                    model.setAdditionTag(task.getAdditionTag()).setBelongUserId(task.getCrtUserId()).setIsOpen(0).setType(1).setPublishStatus(0);
                }
            }
            else {
                imgUrl += lora.getName();
                oldImgUrl += lora.getName();
            }
            if (judgeImgIsExist(oldImgUrl + ".jpg")) {
                model.setUrl(imgUrl + ".jpg");
            }
            else if (judgeImgIsExist(oldImgUrl + ".JPG")) {
                model.setUrl(imgUrl + ".JPG");
            }
            else if (judgeImgIsExist(oldImgUrl + ".png")) {
                model.setUrl(imgUrl + ".png");
            }
            else if (judgeImgIsExist(oldImgUrl + ".PNG")) {
                model.setUrl(imgUrl + ".PNG");
            }
            else if (judgeImgIsExist(oldImgUrl + ".jpeg")) {
                model.setUrl(imgUrl + ".jpeg");
            }
            else if (judgeImgIsExist(oldImgUrl + ".JPEG")) {
                model.setUrl(imgUrl + ".JPEG");
            }
            return model;
        }).collect(Collectors.toList());
        sdUserModelService.batchInsert(models);
    }
    /** 判断模型图片是否存在 **/
    private boolean judgeImgIsExist(String imgUrl) {
        return new File(imgUrl).exists();
    }


    /** 模型分类 **/
    @Override
    public List<SdUserModelClassifyVo> listModelClassify() {
        return sdUserModelClassifyService.selectList(LoginHelper.getUserId());
    }
    @Override
    public void addModelClassify(SdUserModelClassifyDto dto) {
        sdUserModelClassifyService.insert(new SdUserModelClassify().setName(dto.getName()).setCrtUserId(LoginHelper.getUserId()).setCrtTime(new Date()));
    }
    @Override
    public void modifyModelClassify(SdUserModelClassifyDto dto) {
        sdUserModelClassifyService.updateById(new SdUserModelClassify().setName(dto.getName()).setId(Long.parseLong(dto.getId())));
    }
    @Override
    public void removeModelClassify(String id) {
        SdUserModelClassify entity = sdUserModelClassifyService.selectById(id);
        if (entity==null) {
            return;
        }
        Long userId = LoginHelper.getUserId();
        if (!entity.getCrtUserId().equals(userId)){
            throw new ServiceException("不是当前分类归属人,不可删除!");
        }
        // 先判断当前分类下是否存在lora模型
        boolean flag = sdUserModelService.isExistByClassifyId(id, userId);
        if (flag) {
            throw new ServiceException("当前分类下存在模型!");
        }
        sdUserModelClassifyService.removeById(id);
    }


    /** 模型分享 **/
    @Override
    public void shareModel(SdUserModelShareDto dto) {
        if (StringUtils.isBlank(dto.getToShareUserId())) {
            String userId = userService.selectUserIdByPhone(dto.getToSharePhone());
            if (StringUtils.isBlank(userId)) {
                throw new ServiceException("手机号["+dto.getToSharePhone()+"]不存在!");
            }
            dto.setToShareUserId(userId);
        }
        sdUserModelService.shareModel(dto,LoginHelper.getUserId());
    }


    /** Lora模型分页查询 **/
    @Override
    public TableDataInfo<SdUserModelVo> listLoraModels(SdUserModelPageDto dto, PageQuery pageQuery) {
        TableDataInfo<SdUserModelVo> page = sdUserModelService.selectAllList(dto, LoginHelper.getUserId(), pageQuery);
        if (CollectionUtil.isNotEmpty(page.getRows())) {
            for (SdUserModelVo vo : page.getRows()) {
                if (vo.getTitle().startsWith("user_")) {
                    vo.setPreTaskId(vo.getTitle().replace("user_",""));
                }
            }
        }
        return page;
    }
    /** lora模型分页查询(包含关联的模型测试任务和训练数据) **/
    @Override
    public TableDataInfo<SdUserModelVo> listLoraModelsOfTestTaskAndTrainData(SdUserModelDto dto) {
        TableDataInfo<SdUserModelVo> info = sdUserModelService.listLoraModelsInTaskAndTrainData(dto);
        if (CollectionUtil.isNotEmpty(info.getRows())) {
            for (SdUserModelVo vo : info.getRows()) {
                vo.setConfig(JSONObject.parseObject(String.valueOf(vo.getConfig()), SdLoraModelVo.MetadataVo.class));
                // 获取xyz测试数据集
                List<JSONObject> taskList = sdUserModelFileService.selectModelTestDataAndTaskInfo(vo.getId());
                vo.setTaskList(taskList);
                if (vo.getTitle().startsWith("user_")) {
                    vo.setPreTaskId(vo.getTitle().replace("user_",""));
                }
            }
        }
        return info;
    }
    /** 删除lora模型 **/
    @Override
    public void removeModel(String id) {
        Long userId = LoginHelper.getUserId();
        SdUserModel model = sdUserModelService.selectById(id);
        if (model==null) {
            throw new ServiceException("模型不存在!");
        }
        else if (model.getType()==0) {
            throw new ServiceException("系统模型,不可删除!");
        }
        // 个人模型，但是 模型归属人不是当前人(分享模型)，则只能删除分享数据
        else if (model.getType()==1 && !Objects.requireNonNull(userId).equals(model.getBelongUserId())) {
            // 删除分享给我的模型，只删除分享关联数据
            sdUserModelService.removeShareModelById(id,userId);
            return;
        }
        // 拼接模型路径(lora模型所属目录下：model.getFileName() = /home/stable-diffusion-webui/models/Lora/user_xxxxxx.safetensors)
        File delFile = new File(model.getFileName());
        log.warn("[模型删除]>>>>>>>>>[{}]删除了lora模型路径：{}",userId,delFile.getPath());
        if (FileUtil.del(delFile)) {
            // 删除成功后移除模型
            sdUserModelService.removeModelById(id,userId);
            RedisUtils.deleteObject("UserModel:" + model.getId());
        }
    }
    /** 管理员删除lora模型 **/
    @Override
    public void removeModelOfAdmin(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ServiceException("没有可删除模型!");
        }
        List<String> errorMsgList = new ArrayList<>();
        List<SdUserModel> modelList = sdUserModelService.selectByIds(ids);
        if (CollectionUtil.isEmpty(modelList)) {
            throw new ServiceException("当前所选模型不存在或已被删除!");
        }
        for (SdUserModel model : modelList) {
            if (model.getType()==0) {
                errorMsgList.add("["+model.getModelNameZh()+"]是系统模型,不可删除!");
                continue;
            }
            else if (model.getType()==1 && model.getPublishStatus()==1) {
                errorMsgList.add("["+model.getModelNameZh()+"]是已发布的个人模型,不可删除!");
                continue;
            }
            // 拼接模型路径(lora模型所属目录下：model.getFileName() = /home/stable-diffusion-webui/models/Lora/user_xxxxxx.safetensors)
            FileUtil.del(new File(model.getFileName()));
            sdUserModelService.removeModelOfAdminById(model.getId());
            RedisUtils.deleteObject("UserModel:" + model.getId());
        }
        if (CollectionUtil.isNotEmpty(errorMsgList)) {
            throw new ServiceException(Joiner.on("\n").join(errorMsgList));
        }
    }
    /** 修改模型 **/
    @Override
    public void modifyModel(SdUserModelModifyDto dto) {
        sdUserModelService.modifyModel(dto,LoginHelper.getUserId());
        RedisUtils.deleteObject("UserModel:" + dto.getId());
    }
    /** 获取模型详情 **/
    @Override
    public SdUserModelVo getModelInfo(String id) {
        Long userId = LoginHelper.getUserId();
        String key = "UserModel:" + id;
        SdUserModelVo modelInfo = RedisUtils.getCacheObject(key);
        if (modelInfo==null) {
            modelInfo = sdUserModelService.getModelInfo(id, userId);
            if (modelInfo!=null) {
                // 缓存30分钟
                RedisUtils.setCacheObject(key, modelInfo, Duration.ZERO.minusMinutes(30));
            }
        }
        return modelInfo;
    }
    /** 获取最近使用的模型(limit个模型) **/
    @Override
    public List<SdUserModelVo> getLatestModelInfo(int limit) {
        return sdUserModelService.getLatestModelInfo(LoginHelper.getUserId(),limit);
    }


    /** 分页获取当前用户绘图任务列表 **/
    @Override
    public TableDataInfo<SdUserTaskVo> userDrawTaskList(PageQuery pageQuery, Integer category, Integer status) {
        return sdUserTaskService.userDrawTaskList(pageQuery,LoginHelper.getUserId(),category,status);
    }
    /** 分页获取所有用户绘图任务列表 **/
    @Override
    public TableDataInfo<SdUserTaskVo> allUserTaskList(PageQuery pageQuery, Integer category, Integer status) {
        Long userId = LoginHelper.getUserId();
        if (LoginHelper.isAdmin(userId)) {
            userId = null;
        }
        return sdUserTaskService.userDrawTaskList(pageQuery,userId,category,status);
    }

    /** 分页获取用户绘图图片数据 **/
    @Override
    public TableDataInfo<SdUserModelFileVo> listUserModelFile(PageQuery pageQuery, SdUserModelFilePageDto dto) {
        return sdUserModelFileService.listUserModelFile(pageQuery,LoginHelper.getUserId(),dto);
    }
    /** 根据任务ID获取用户绘图图片数据列表 **/
    @Override
    public List<SdUserModelFileVo> listUserModelFile(String taskId) {
        return sdUserModelFileService.listUserModelFile(taskId,LoginHelper.getUserId());
    }
    /** 批量删除用户绘图图片数据 **/
    @Override
    public void removeUserModelFile(List<String> ids) {
        sdUserModelFileService.removeUserModelFile(ids,LoginHelper.getUserId());
    }


    /** 发布/下架模型 **/
    @Override
    public void publishModel(String id, Integer publishStatus, String modelStrength) {
        // 获取模型对应的用户的openId和手机号
        JSONObject info = sdUserModelService.selectUserOpenIdAndPhoneById(id);
        if (info == null) {
            return;
        }
        // isUserDel目前其实并没有使用到
        sdUserModelService.publishModel(id,publishStatus,Objects.equals(LoginHelper.getUserId(), info.getLong("userId"))?1:0,modelStrength);
        if (publishStatus==1 && CollectionUtil.isNotEmpty(info)) {
            // 发送完成消息
            JSONObject wxMsg = new JSONObject();
            wxMsg.put("modelId",id);
            wxMsg.put("type","PUBLISH_MODEL");
            wxMsg.put("openId",info.getString("wxOpenId"));
            wxMsg.put("userId",info.getString("userId"));
            rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg);
        }
    }
    /** 修改模型强度 **/
    @Override
    public void modifyModelStrength(String id, String modelStrength) {
        sdUserModelService.modifyModelStrength(id,modelStrength);
    }


    /** 获取当前用正在进行的指定分类的绘图任务ID **/
    @Override
    public String getDoingTaskId(Integer category) {
        // 绘图任务，每个人仅能存在一个正在进行的绘图任务，上一个任务没有完成时，是不能进行下一个绘图任务的
        return sdUserTaskService.getDoingTask(LoginHelper.getUserId(),category);
    }
    /** 删除指定绘图任务 **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskById(String taskId) {
        sdUserTaskService.deleteTaskByTaskId(taskId);
        sdUserModelFileService.removeUserModelFileByTaskId(taskId);
    }


    /** 批量下载指定绘图任务的绘图图片数据 **/
    @Override
    public void batchDownloadModelFile(String taskId, HttpServletResponse response) throws IOException {
        List<String> imgUrls = sdUserModelFileService.listImgUrlByTaskId(taskId,LoginHelper.getUserId());
        if (CollectionUtil.isEmpty(imgUrls)) {
            return;
        }
        downloadZip(response,imgUrls);
    }
    /** 批量下载指定绘图图片数据 **/
    @Override
    public void batchDownloadUserModelFile(List<String> ids, HttpServletResponse response) throws IOException {
        List<String> imgUrls = sdUserModelFileService.listImgUrlByIds(ids);
        if (CollectionUtil.isEmpty(imgUrls)) {
            return;
        }
        downloadZip(response,imgUrls);
    }
    @Override
    public void downloadUserModelFile(String imgUrl, HttpServletResponse response) throws IOException {
        String imgName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
        //打包文件里的每个文件的名字
        FileUtils.setAttachmentResponseHeader(response, imgName);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OssClient storage = OssFactory.instance();
        try(InputStream inputStream = storage.getObjectContent(imgUrl)) {
            int available = inputStream.available();
            IoUtil.copy(inputStream, response.getOutputStream(), available);
            response.setContentLength(available);
        } catch (Exception e) {
            log.error("获取图片失败:{}", e.getMessage());
        }
    }
    /** 打包下载 **/
    private void downloadZip(HttpServletResponse response, List<String> imgUrls) throws IOException {
        String downloadName = "data_"+DateUtil.format(new Date(),"yyyyMMddHHmmss")+".zip";
        //打包文件里的每个文件的名字
        FileUtils.setAttachmentResponseHeader(response, downloadName);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();
        ZipArchiveOutputStream zus = new ZipArchiveOutputStream(outputStream);
        zus.setUseZip64(Zip64Mode.AsNeeded);
        zus.setEncoding("utf-8");
        OssClient storage = OssFactory.instance();
        try{
            //具体你想生成什么类型的多个文件打包，只需要循环创建ArchiveEntry 然后zus.putArchiveEntry(entry)就可以了
            for (String url : imgUrls) {
                //打包文件里的每个文件的名字
                String imgName = url.substring(url.lastIndexOf("/")+1);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try(InputStream inputStream = storage.getObjectContent(url)) {
                    int available = inputStream.available();
                    IoUtil.copy(inputStream, out, available);
                } catch (Exception e) {
                    log.error("获取图片失败:{}",e.getMessage());
                    continue;
                }
                byte[] bytes = out.toByteArray();
                ArchiveEntry entry = new ZipArchiveEntry(imgName);
                zus.putArchiveEntry(entry);
                zus.write(bytes);
                zus.closeArchiveEntry();
                out.close();
            }
        } catch (Exception e){
            log.error("图片打包下载异常:{}",e.getMessage());
        } finally{
            if(outputStream != null){
                outputStream.close();
            }
        }
    }


    /** 同步指定类型GPU卡池到缓存中 **/
    @Override
    public JSONObject syncGpuPool(Integer type) {
        List<SdGpuPool> list = sdGpuPoolService.getList(type);
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException("未配置GPU池!");
        }
        Set<SdGpuPool> trainGpuPools = list.stream().filter(e -> e.getType() == 1 && e.getIsEnable()==1).collect(Collectors.toSet());
        Set<SdGpuPool> drawGpuPools = list.stream().filter(e -> e.getType() == 0 && e.getIsEnable()==1).collect(Collectors.toSet());

        if (CollectionUtil.isNotEmpty(trainGpuPools)) {
            RedisUtils.setCacheSet(TRAIN_GPU_POOL,trainGpuPools);
        }
        if (CollectionUtil.isNotEmpty(drawGpuPools)) {
            RedisUtils.setCacheSet(DRAW_GPU_POOL,drawGpuPools);
        }
        JSONObject data = new JSONObject();
        data.put("trainGpuPools",trainGpuPools);
        data.put("drawGpuPools",drawGpuPools);
        return data;
    }
    /** 下架指定GPU卡到缓存池中 **/
    @Override
    public void stopGpuPool(SdGpuPool sdGpuPool) {
        SdGpuPool gpuPool = getGpuFromDrawGpuPool(null, sdGpuPool, false);
        if (gpuPool!=null) {
            throw new ServiceException("当前GPU正在使用!");
        }
    }
    /** 上架指定GPU卡到缓存池中 **/
    @Override
    public void startGpuPool(SdGpuPool sdGpuPool) {
        SdGpuPool gpuPool = getGpuFromDrawGpuPool(null, sdGpuPool, true);
        if (gpuPool!=null) {
            throw new ServiceException("当前GPU正在使用!");
        }
    }


    /** 删除XYZ测试数据 **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delXyzData(String taskId) {
        String gridUrl = sdUserTaskService.selectGridUrlByTaskId(taskId);
        sdUserTaskService.deleteTaskByTaskId(taskId);
        sdUserModelFileService.removeUserModelFileByTaskId(taskId);
        if (StrUtil.isNotEmpty(gridUrl)) {
            FileUtil.del(new File(gridUrl));
        }
    }

    /** 获取任务进度 **/
    @Override
    public JSONObject getProcess(String taskId) {
        // 先判断当前任务是否已失败
        Integer status = sdUserTaskService.selectStatusByTaskIdAndUserId(taskId,LoginHelper.getUserId());
        if (status==null || status==3) {
            throw new ServiceException("当前绘图任务不存在或已失败!");
        }
        SdGpuPool sdGpuPool = RedisUtils.getCacheObject(DRAW_GPU_TASK + taskId);
        if (sdGpuPool==null) {
            JSONObject data = new JSONObject();
            data.put("active",false);
            data.put("completed",true);
            data.put("eta",null);
            data.put("id_live_preview",-1);
            data.put("live_preview",null);
            data.put("progress",null);
            data.put("queued",false);
            return data;
        }
        SdInternalProgressDto dto = new SdInternalProgressDto().setIdTask(taskId).setIdLivePreview(2).setLivePreview(false);
        Map<String,Object> map = JSONObject.parseObject(JSONObject.toJSONString(dto));
        map.put("taskId",taskId);
        return Forest.post("/internal/progress").address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().addBody(map).execute(JSONObject.class);
    }


    /** 文生图 **/
    @Override
    public String txt2img(SdText2ImgDto dto) {
        Long userId = LoginHelper.getUserId();
        String userName = LoginHelper.getUsername();
        // 从缓存中获取基础大模型
        String checkpoint = RedisUtils.getCacheObject(CHECK_POINT);
        if (StringUtils.isBlank(checkpoint)) {
            throw new ServiceException("缺少基础大模型!");
        }
        // 获取当前用户是否存在正在进行的任务
        String taskId = getDoingTaskId(1);
        if (StrUtil.isNotEmpty(taskId)) {
            throw new ServiceException("当前用户存在未完成的任务!",501,taskId);
        }
        // 获取绘图剩余次数
        Integer drawNum = userService.selectDrawNumById(LoginHelper.getUserId());
        if (drawNum!=null && drawNum<dto.getBatch_size()) {
            throw new ServiceException("余额不足,请联系管理员!");
        }

        // 获取多模型数据和多模型强度
        List<SdUserModel> models = new ArrayList<>();
        Map<String,String> modelStrengthMap = new HashMap<>(8);
        dealMultiModelAndModelStrength(dto, models,modelStrengthMap);

        // 生成绘图任务ID 和 绘图参数
        taskId = IdUtil.getSnowflakeNextIdStr();
        JSONObject msg = dealTxt2ImgParam(dto,taskId,models,modelStrengthMap,checkpoint,userId,userName,false);
        // 新增任务
        sdUserTaskService.addWebuiTask(taskId,userId,userName,0,0);
        // 请求放入消息队列中
        rabbitTemplate.convertAndSend(SD_TXT_TO_IMG_DRAW_EXCHANGE,SD_TXT_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
        return taskId;
    }
    /** 文生图-模型测试 **/
    @Override
    public String testTxt2ImgOfLoraModel(SdText2ImgDto dto) {
        if (StrUtil.isNotEmpty(dto.getModelId()) && CollectionUtil.isEmpty(dto.getModelInfos())) {
            SdApiModelParamDto dto1 = new SdApiModelParamDto().setModelId(dto.getModelId()).setModelStrength(StrUtil.isEmptyIfStr(dto.getModelStrength())?"0.5":dto.getModelStrength());
            dto.setModelInfos(new ArrayList<>(Collections.singletonList(dto1)));
        }
        Long userId = LoginHelper.getUserId();
        String userName = LoginHelper.getUsername();
        String taskId = IdUtil.getSnowflakeNextIdStr();
        String checkpoint = RedisUtils.getCacheObject(CHECK_POINT);

        // 获取多模型数据和多模型强度
        List<SdUserModel> models = new ArrayList<>();
        Map<String,String> modelStrengthMap = new HashMap<>(8);
        dealMultiModelAndModelStrength(dto, models,modelStrengthMap);

        JSONObject msg = dealTxt2ImgParam(dto,taskId,models, modelStrengthMap, checkpoint,userId,userName,true);
        // 新增任务
        sdUserTaskService.addWebuiTask(taskId,userId,userName,2,0);
        // 请求放入消息队列中
        rabbitTemplate.convertAndSend(SD_TXT_TO_IMG_DRAW_EXCHANGE,SD_TXT_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
        return null;
    }
    /** 文生图>>>>>>>>>处理参数 **/
    private JSONObject dealTxt2ImgParam(SdText2ImgDto dto, String taskId, List<SdUserModel> models, Map<String, String> modelStrengthMap, String checkpoint, Long userId, String userName, boolean isTest) {
        // 提示词原文
        String prompt = dto.getPrompt();
        // 提示词译文
        String promptZh = dto.getPromptZh();
        // 召唤词
        String summonWord = dto.getSummonWord();
        // 反向提示词原文
        String negativePrompt = dto.getNegative_prompt();
        // 反向提示词译文
        String negativePromptZh = dto.getNegativePromptZh();

        // 提交给SD的提示词 summonWord+", "+prompt
        StringBuilder newPrompt = new StringBuilder(prompt);

        // 多模型集合
        List<JSONObject> loraInfo = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(models)) {
            // 提示词中添加lora模型
            for (SdUserModel model : models) {
                String modelStrength = modelStrengthMap.get(String.valueOf(model.getId()));
                JSONObject data = new JSONObject();
                data.put("loraModelId",model.getId());
                data.put("loraTitle",model.getModelName());
                data.put("loraTitleZh",model.getModelNameZh());
                data.put("modelStrength", modelStrength);
                data.put("loraModelUrl",model.getUrl());
                loraInfo.add(data);
                if (isTest) {
                    newPrompt.append(" <lora:").append(model.getModelName().contains("-")?model.getModelName().split("-")[0]:model.getModelName()).append("-NUM:STRENGTH>");
                }
                else {
                    newPrompt.append(" <lora:").append(model.getModelName()).append(":").append(modelStrength).append(">");
                }
            }
        }

        dto.setSteps(dto.getSteps()==null || dto.getSteps()<=0?20:dto.getSteps());
        dto.setWidth(dto.getWidth()==null || dto.getWidth()<=0?512:dto.getWidth());
        dto.setHeight(dto.getHeight()==null || dto.getHeight()<=0?512:dto.getHeight());
        dto.setBatch_size(dto.getBatch_size()==null || dto.getBatch_size()<=0?1:dto.getBatch_size());
        dto.setN_iter(dto.getN_iter()==null || dto.getN_iter()<=0?1:dto.getN_iter());
        dto.setSeed(dto.getSeed()==null || dto.getSeed()<=-1?-1L:dto.getSeed());
        dto.setRestore_faces(dto.getRestore_faces() == null || dto.getRestore_faces());
        dto.setCLIP_stop_at_last_layers(dto.getCLIP_stop_at_last_layers()==null || dto.getCLIP_stop_at_last_layers()<=0? 1:dto.getCLIP_stop_at_last_layers());
        dto.setSampler_name(StrUtil.isEmptyIfStr(dto.getSampler_name())?this.sdSampler:dto.getSampler_name());
        dto.setCfg_scale(dto.getCfg_scale()==null?7:dto.getCfg_scale());

        Map<String, Object> map = new HashMap<>(Objects.requireNonNull(BeanCopyUtils.copyToMap(dto)));
        Map<String,Object> overrideSettings = new HashMap<>(4);
        overrideSettings.put("sd_model_checkpoint",checkpoint);
        overrideSettings.put("CLIP_stop_at_last_layers",dto.getCLIP_stop_at_last_layers());
        overrideSettings.put("sd_vae",StrUtil.isEmptyIfStr(dto.getSd_vae())?"Automatic":dto.getSd_vae());
        map.put("override_settings",overrideSettings);

        Map<String,Object> alwayson_scripts = new HashMap<>(2);
        if (CollectionUtil.isNotEmpty(dto.getControlNetArgs())) {
            Map<String,Object> ControlNet = new HashMap<>(2);
            ControlNet.put("args", dto.getControlNetArgs());
            alwayson_scripts.put("ControlNet",ControlNet);
        }
        if (CollectionUtil.isNotEmpty(dto.getRefinerArgs())) {
            Map<String,Object> Refiner = new HashMap<>(2);
            Refiner.put("args", dto.getControlNetArgs());
            alwayson_scripts.put("Refiner",Refiner);
        }
        if (CollectionUtil.isNotEmpty(alwayson_scripts)) {
            map.put("alwayson_scripts",alwayson_scripts);
        }

        map.put("task_id",taskId);
        map.put("id_task",taskId);
        map.put("force_task_id",taskId);
        // 新的提示词
        map.put("prompt",newPrompt.toString());
        // 是否保存生成的图像 一般api设置成False
        map.put("save_images", false);
        // 是否保存samples 一般api设置成False
        map.put("do_not_save_samples", false);
        // 是否保存网格的图像 一般api设置成False
        map.put("do_not_save_grid", false);
        // 是否在响应中返回生成的图像
        map.put("send_images", !isTest);
        // override_settings 是否在之后恢复覆盖的设置
        map.put("override_settings_restore_afterwards", true);
        // 去噪强度 0-1之前
        map.put("denoising_strength", 0.75);

        map.remove("modelId");
        map.remove("modelStrength");
        map.remove("modelInfos");
        map.remove("summonWord");
        map.remove("promptZh");
        map.remove("negativePromptZh");

        map.remove("CLIP_stop_at_last_layers");
        map.remove("sd_vae");
        map.remove("controlNetArgs");
        map.remove("refinerArgs");

        JSONObject msg = new JSONObject();
        msg.put("data",map);
        msg.put("taskId",taskId);
        msg.put("userId",userId);
        msg.put("userName",userName);
        msg.put("modelName",checkpoint);
        msg.put("loraInfo",loraInfo);
        msg.put("now", DateUtil.now());
        msg.put("isTest", isTest);
        msg.put("prompt", prompt);
        msg.put("promptZh", promptZh);
        msg.put("promptDesc", prompt);
        msg.put("summonWord", summonWord);
        msg.put("negativePrompt", negativePrompt);
        msg.put("negativePromptZh", negativePromptZh);
        return msg;
    }
    /** 文生图>>>>>>>>>队列处理数据 **/
    @RabbitListener(queues = SD_TXT_TO_IMG_DRAW_QUEUE)
    public void txt2ImgConsume(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        final JSONObject msg = JSON.parseObject(body, JSONObject.class);
        String taskId = msg.getString("taskId");
        String userId = msg.getString("userId");
        Date now = DateUtil.parseDateTime(msg.getString("now"));
        Date time = new Date();
        long queueTime = DateUtil.betweenMs(now, time);
        Integer status = sdUserTaskService.selectStatusByTaskIdAndUserId(taskId, Long.parseLong(userId));
        // 判断队列任务是否已被删除结束掉
        if (status==null) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        // 获取绘图GPU服务
        final SdGpuPool sdGpuPool = getGpuFromDrawGpuPool(taskId, null, null);
        if (sdGpuPool == null) {
            log.error("[绘图任务][任务ID:{}]>>>>>>>>>绘图卡池中暂无可使用的GPU服务,请等待!",taskId);
            Long startTime = RedisUtils.getCacheMapValue(DRAW_TASK_TIME_IN_QUEUE_MAP, taskId);
            if (startTime == null) {
                // 记录在队列中的时间
                RedisUtils.setCacheMapValue(DRAW_TASK_TIME_IN_QUEUE_MAP, taskId, System.currentTimeMillis()/1000);
                return;
            }
            // 在队列中的时间小于25分钟
            else if (System.currentTimeMillis()/1000 - startTime < 1500) {
                return;
            }
            else {
                // 重新进入队列，防止30分钟超时
                rabbitTemplate.convertAndSend(SD_TXT_TO_IMG_DRAW_EXCHANGE,SD_TXT_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
                channel.basicAck(deliveryTag, false);
                return;
            }
        }
        // 确认消费
        // 异步处理绘图请求
        CompletableFuture.runAsync(()-> executeTxtToImgDrawTask(sdGpuPool,taskId,msg,queueTime),executor);
        channel.basicAck(deliveryTag, false);
    }
    /** 文生图>>>>>>>>>执行绘图任务 **/
    private void executeTxtToImgDrawTask(final SdGpuPool sdGpuPool, final String taskId, final JSONObject msg, long queueTime) {
        String userName = msg.getString("userName");
        String prompt = msg.getString("prompt");
        String promptDesc = msg.getString("promptDesc");
        String promptZh = msg.getString("promptZh");
        String summonWord = msg.getString("summonWord");
        String negativePrompt = msg.getString("negativePrompt");
        String negativePromptZh = msg.getString("negativePromptZh");
        // 获取模型数据信息集合
        List<JSONObject> loraInfo = JSON.parseArray(msg.getString("loraInfo"),JSONObject.class);

        JSONObject data = msg.getJSONObject("data");
        boolean isTest = msg.getBooleanValue("isTest");
        final int deviceId = sdGpuPool.getDeviceId();
        try {
            AtomicBoolean gpuIsOverflow = new AtomicBoolean(false);
            // 修改执行中状态
            sdUserTaskService.startWebuiTask(taskId,queueTime);
            Forest.post(TXT_TO_IMG_API).address(sdGpuPool.getHost(), sdGpuPool.getPort()).contentTypeJson().addBody(data).connectTimeout(30, TimeUnit.MINUTES)
                .onSuccess((result, req, res) -> {
                    log.warn("[绘图任务][任务ID:{}]>>>>>>>>>文生图任务完成，耗时：{} ms",taskId, res.getTimeAsMillisecond());
                    Map<String,Object> result1 = null;
                    if (!isTest) {
                        result1 = JSONObject.parseObject(JSONObject.toJSONString(result),Map.class);
                    }
                    // 请求成功，处理响应结果
                    SdApiResult rs = ResultUtil.apiToResult(result1,ossService,userName,isTest, sdGpuPool.getTxtGridDir(),"/grids/");
                    if (CollectionUtil.isNotEmpty(loraInfo) && !isTest) {
                        for (JSONObject e : loraInfo) {
                            // 新增模型使用日志
                            sdUserModelLogService.asyncInsertData(msg.getLong("userId"),msg.getString("userName"),e.getLong("loraModelId"),e.getString("loraTitle"),msg.getString("modelName"),e.getString("modelStrength"));
                        }
                    }
                    if (CollectionUtil.isNotEmpty(rs.getImages())) {
                        sdUserModelFileService.asyncBatchInsert(rs,msg.getLong("userId"),msg.getString("userName"),loraInfo,msg.getString("modelName"), taskId, isTest?2:0, prompt, null, promptDesc, promptZh, summonWord, negativePrompt, negativePromptZh, 0);
                    }
                    // 更新进度状态
                    sdUserTaskService.completeWebuiTask(taskId,res.getTimeAsMillisecond());
                    if (isTest) {
                        // 发送完成消息
                        JSONObject wxMsg = new JSONObject();
                        wxMsg.put("taskId",taskId);
                        wxMsg.put("type","MODEL_TEST");
                        wxMsg.put("isComplete",true);
                        rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg,new CorrelationData(taskId));
                    }
                    // 扣除绘图次数
                    else if (!rs.getImages().isEmpty()) {
                        userService.deductedDrawNum(msg.getLong("userId"),rs.getImages().size());
                        // 将图片发送给第三方
                        ImgSendThirdDto dto = new ImgSendThirdDto().setUserId(msg.getLong("userId")).setImgUrlList(rs.getImages());
                        applicationEventPublisher.publishEvent(new MsgSendThirdEvent(dto));
                    }
                })
                .onError((ex,req,res)->{
                    log.error("[绘图任务][任务ID:{}]>>>>>>>>>文生图任务失败，请求参数：{}，耗时：{} ms，失败原因{}", taskId, data, res.getTimeAsMillisecond(), ex.getMessage());
                    // 更新进度状态
                    if (ex.getMessage().contains("OutOfMemoryError") || ex.getMessage().contains("cpu and cuda:0") || ex.getMessage().contains("CUDA out of memory")) {
                        // 当前GPU显存溢出，需要重启
                        // 自动下架该GPU服务
                        log.error("[GPU服务自动下线]>>>>>>>>>deviceId[{}]===>{}",sdGpuPool.getDeviceId(),sdGpuPool);
                        getGpuFromDrawGpuPool(null, sdGpuPool, false);
                        gpuIsOverflow.set(true);
                        // 发送消息GPU服务需要重启
                        JSONObject wxMsg = new JSONObject();
                        wxMsg.put("deviceId",deviceId);
                        wxMsg.put("type","GPU_RESTART");
                        rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg,new CorrelationData(taskId));
                    }
                    else if (isTest){
                        sdUserTaskService.failWebuiTask(taskId,ex.getMessage(),queueTime);
                        JSONObject wxMsg = new JSONObject();
                        wxMsg.put("taskId",taskId);
                        wxMsg.put("type","MODEL_TEST");
                        wxMsg.put("isComplete",false);
                        rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg,new CorrelationData(taskId));
                    }
                    else {
                        // 更新进度状态
                        sdUserTaskService.failWebuiTask(taskId,ex.getMessage(), queueTime);
                    }
                })
                .execute();
            if (gpuIsOverflow.get()) {
                // 重新进入队列
                log.error("GPU[{}]服务显存溢出，重新进入队列!",deviceId);
                rabbitTemplate.convertAndSend(SD_TXT_TO_IMG_DRAW_EXCHANGE,SD_TXT_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
            }
        }
        catch (Exception e) {
            // 更新进度状态
            sdUserTaskService.failWebuiTask(taskId,e.getMessage(), queueTime);
        }
        finally {
            RedisUtils.deleteObject(DRAW_GPU_TASK+taskId);
            returnGpuFromTrainGpuPool(sdGpuPool);
        }
    }



    /** 1、图生图 **/
    @Override
    public String img2img(SdImg2ImgDto dto) {
        final Long userId = LoginHelper.getUserId();
        final String userName = LoginHelper.getUsername();
        // 从缓存中获取基础大模型
        String checkpoint = RedisUtils.getCacheObject(CHECK_POINT);
        if (StringUtils.isBlank(checkpoint)) {
            throw new ServiceException("缺少基础大模型!");
        }
        // 获取当前用户是否存在正在进行的任务
        String taskId = getDoingTaskId(1);
        if (StrUtil.isNotEmpty(taskId)) {
            throw new ServiceException("当前用户存在未完成的任务!",501,taskId);
        }
        // 获取绘图剩余次数
        Integer drawNum = userService.selectDrawNumById(LoginHelper.getUserId());
        if (drawNum!=null && drawNum<dto.getBatch_size()) {
            throw new ServiceException("余额不足,请联系管理员!");
        }

        // 获取多模型数据和多模型强度
        List<SdUserModel> models = new ArrayList<>();
        Map<String,String> modelStrengthMap = new HashMap<>(32);
        dealMultiModelAndModelStrength(dto, models,modelStrengthMap);

        // 生成绘图任务ID 和 绘图参数
        taskId = IdUtil.getSnowflakeNextIdStr();
        final int isRedraw = StringUtils.isNotBlank(dto.getMask())?1:0;
        JSONObject msg = dealImg2ImgParam(dto,taskId,models,modelStrengthMap,checkpoint,userId,userName);
        // 新增任务
        sdUserTaskService.addWebuiTask(taskId,userId,userName,1,isRedraw);
        msg.put("isRedraw",isRedraw);
        // 请求放入消息队列中
        rabbitTemplate.convertAndSend(SD_IMG_TO_IMG_DRAW_EXCHANGE,SD_IMG_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
        return taskId;
    }
    /** 2、图生图>>>>>>>>>处理参数 **/
    private JSONObject dealImg2ImgParam(SdImg2ImgDto dto, String taskId, List<SdUserModel> models, Map<String, String> modelStrengthMap, String checkpoint, Long userId, String userName) {
        if (StrUtil.isEmptyIfStr(dto.getInitImage())) {
            throw new ServiceException("缺少参考图片!");
        }
        // 提示词
        String promptZh = dto.getPromptZh();
        String prompt = dto.getPrompt();
        // 召唤词
        String summonWord = dto.getSummonWord();
        String negativePrompt = dto.getNegative_prompt();
        String negativePromptZh = dto.getNegativePromptZh();
        //TODO 提交给SD的提示词 summonWord+", "+prompt
        StringBuilder newPrompt = new StringBuilder(prompt);
        // 多模型集合
        List<JSONObject> loraInfo = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(models)) {
            // 提示词中添加lora模型
            for (SdUserModel model : models) {
                String modelStrength = modelStrengthMap.get(String.valueOf(model.getId()));
                JSONObject data = new JSONObject();
                data.put("loraModelId",model.getId());
                data.put("loraTitle",model.getModelName());
                data.put("loraTitleZh",model.getModelNameZh());
                data.put("modelStrength", modelStrength);
                data.put("loraModelUrl",model.getUrl());
                loraInfo.add(data);
                newPrompt.append(" <lora:").append(model.getModelName()).append(":").append(modelStrength).append(">");
            }
        }
        dto.setSteps(dto.getSteps()==null || dto.getSteps()<=0?20:dto.getSteps());
        dto.setWidth(dto.getWidth()==null || dto.getWidth()<=0?512:dto.getWidth());
        dto.setHeight(dto.getHeight()==null || dto.getHeight()<=0?512:dto.getHeight());
        dto.setBatch_size(dto.getBatch_size()==null || dto.getBatch_size()<=0?1:dto.getBatch_size());
        dto.setN_iter(dto.getN_iter()==null || dto.getN_iter()<=0?1:dto.getN_iter());
        dto.setSeed(dto.getSeed()==null || dto.getSeed()<=-1?-1L:dto.getSeed());
        dto.setRestore_faces(dto.getRestore_faces() == null || dto.getRestore_faces());
        dto.setCLIP_stop_at_last_layers(dto.getCLIP_stop_at_last_layers()==null || dto.getCLIP_stop_at_last_layers()<=0? 1:dto.getCLIP_stop_at_last_layers());
        dto.setSampler_name(StrUtil.isEmptyIfStr(dto.getSampler_name())?this.sdSampler:dto.getSampler_name());
        dto.setCfg_scale(dto.getCfg_scale()==null?7:dto.getCfg_scale());

        Map<String, Object> map = new HashMap<>(Objects.requireNonNull(BeanCopyUtils.copyToMap(dto)));
        Map<String,Object> overrideSettings = new HashMap<>(4);
        overrideSettings.put("sd_model_checkpoint",checkpoint);
        overrideSettings.put("CLIP_stop_at_last_layers",dto.getCLIP_stop_at_last_layers());
        overrideSettings.put("sd_vae",StrUtil.isEmptyIfStr(dto.getSd_vae())?"Automatic":dto.getSd_vae());
        map.put("override_settings",overrideSettings);

        Map<String,Object> alwayson_scripts = new HashMap<>(2);
        if (CollectionUtil.isNotEmpty(dto.getControlNetArgs())) {
            Map<String,Object> ControlNet = new HashMap<>(2);
            ControlNet.put("args", dto.getControlNetArgs());
            alwayson_scripts.put("ControlNet",ControlNet);
        }
        if (CollectionUtil.isNotEmpty(dto.getRefinerArgs())) {
            Map<String,Object> Refiner = new HashMap<>(2);
            Refiner.put("args", dto.getControlNetArgs());
            alwayson_scripts.put("Refiner",Refiner);
        }
        if (CollectionUtil.isNotEmpty(alwayson_scripts)) {
            map.put("alwayson_scripts",alwayson_scripts);
        }

        map.put("task_id",taskId);
        map.put("id_task",taskId);
        map.put("force_task_id",taskId);
        // 新的提示词
        map.put("prompt",newPrompt.toString());
        if (StrUtil.isNotEmpty(dto.getInitImage()) && dto.getInitImage().startsWith("http")) {
            OssClient storage = OssFactory.instance();
            String base64 = FileUtils.inputStreamToBase64(storage.getObjectContent(dto.getInitImage()));
            map.put("init_images",Collections.singletonList(base64));
        }
        else {
            map.put("init_images",Collections.singletonList(dto.getInitImage()));
        }
        // 是否局部绘图
        if (StringUtils.isNotBlank(dto.getMask())) {
            map.put("mask",dto.getMask());
        }
        // 去噪强度
        if (dto.getDenoising_strength() != null) {
            map.put("denoising_strength",dto.getDenoising_strength());
        }
        // 是否保存生成的图像 一般api设置成False
        map.put("save_images", false);
        // 是否保存samples 一般api设置成False
        map.put("do_not_save_samples", false);
        // 是否保存网格的图像 一般api设置成False
        map.put("do_not_save_grid", false);
        // 是否在响应中返回生成的图像
        map.put("send_images", true);
        // override_settings 是否在之后恢复覆盖的设置
        map.put("override_settings_restore_afterwards", true);
        // 去噪强度 0-1之前
        map.put("denoising_strength", 0.75);

        map.remove("modelId");
        map.remove("modelStrength");
        map.remove("modelInfos");
        map.remove("summonWord");
        map.remove("promptZh");
        map.remove("negativePromptZh");

        map.remove("CLIP_stop_at_last_layers");
        map.remove("sd_vae");
        map.remove("controlNetArgs");
        map.remove("refinerArgs");

        JSONObject msg = new JSONObject();
        msg.put("data",map);
        msg.put("taskId",taskId);
        msg.put("userId",userId);
        msg.put("userName",userName);
        msg.put("loraInfo",loraInfo);
        msg.put("modelName",checkpoint);
        msg.put("now", DateUtil.now());
        msg.put("isTest", false);
        msg.put("prompt", prompt);
        msg.put("promptDesc", prompt);
        msg.put("summonWord", summonWord);
        msg.put("promptZh", promptZh);
        msg.put("negativePrompt", negativePrompt);
        msg.put("negativePromptZh", negativePromptZh);
        msg.put("initImg", dto.getInitImage());
        return msg;
    }
    /** 3、图生图>>>>>>>>>队列处理数据 **/
    @RabbitListener(queues = SD_IMG_TO_IMG_DRAW_QUEUE)
    public void img2ImgConsume(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        JSONObject msg = JSON.parseObject(body, JSONObject.class);
        String taskId = msg.getString("taskId");
        String userId = msg.getString("userId");
        Integer isRedraw = msg.getInteger("isRedraw");
        Date now = DateUtil.parseDateTime(msg.getString("now"));
        Date time = new Date();
        long queueTime = DateUtil.betweenMs(now, time);
        if (sdUserTaskService.selectStatusByTaskIdAndUserId(taskId, Long.parseLong(userId))==null) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        final SdGpuPool sdGpuPool = getGpuFromDrawGpuPool(taskId, null, null);
        if (sdGpuPool == null) {
            log.error("[绘图任务][任务ID:{}]>>>>>>>>>绘图卡池中暂无可使用的GPU服务,请等待!",taskId);
            Long startTime = RedisUtils.getCacheMapValue(DRAW_TASK_TIME_IN_QUEUE_MAP, taskId);
            if (startTime == null) {
                // 记录在队列中的时间
                RedisUtils.setCacheMapValue(DRAW_TASK_TIME_IN_QUEUE_MAP, taskId, System.currentTimeMillis()/1000);
                return;
            }
            // 在队列中的时间小于25分钟
            else if (System.currentTimeMillis()/1000 - startTime < 1500) {
                return;
            }
            else {
                // 重新进入队列，防止30分钟超时
                rabbitTemplate.convertAndSend(SD_IMG_TO_IMG_DRAW_EXCHANGE,SD_IMG_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
                channel.basicAck(deliveryTag, false);
                return;
            }
        }
        // 确认消费
        // 异步处理绘图请求
        CompletableFuture.runAsync(()-> executeImgToImgDrawTask(sdGpuPool,taskId,msg,queueTime,isRedraw),executor);
        channel.basicAck(deliveryTag, false);
    }
    /** 4、图生图>>>>>>>>>执行图生图任务 **/
    private void executeImgToImgDrawTask(final SdGpuPool sdGpuPool, final String taskId, final JSONObject msg, long queueTime, Integer isRedraw) {
        final int deviceId = sdGpuPool.getDeviceId();
        String userName = msg.getString("userName");
        String prompt = msg.getString("prompt");
        String promptDesc = msg.getString("promptDesc");
        String promptZh = msg.getString("promptZh");
        String summonWord = msg.getString("summonWord");
        String negativePrompt = msg.getString("negativePrompt");
        String negativePromptZh = msg.getString("negativePromptZh");
        String initImg = msg.getString("initImg");
        // 获取模型数据信息集合
        List<JSONObject> loraInfo = JSON.parseArray(msg.getString("loraInfo"),JSONObject.class);
        try {
            JSONObject data = msg.getJSONObject("data");
            boolean isTest = msg.getBooleanValue("isTest");
            OssClient storage = OssFactory.instance();
            if (StrUtil.isNotEmpty(initImg) && !initImg.startsWith("http")) {
                UploadResult uploadResult = storage.uploadSuffix(FileUtils.base64ToInputStream(initImg),PNG,"image/png");
                initImg = uploadResult.getUrl();
            }
            String newInitImg = initImg;
            // 修改执行中状态
            sdUserTaskService.startWebuiTask(taskId,queueTime);
            AtomicBoolean gpuIsOverflow = new AtomicBoolean(false);
            Forest.post(IMG_TO_IMG_API)
                .address(sdGpuPool.getHost(), sdGpuPool.getPort())
                .contentTypeJson()
                .addBody(data)
                .connectTimeout(30, TimeUnit.MINUTES)
                .onSuccess((result, req, res) -> {
                    log.warn("[绘图任务][任务ID:{}]>>>>>>>>>图生图任务完成，耗时：{} ms", taskId, res.getTimeAsMillisecond());
                    Map<String,Object> result1 = null;
                    if (!isTest) {
                        result1 = JSONObject.parseObject(JSONObject.toJSONString(result),Map.class);
                    }
                    // 请求成功，处理响应结果
                    SdApiResult rs = ResultUtil.apiToResult(result1,ossService,userName, isTest,sdGpuPool.getImgGridDir(),"/grids/");
                    if (CollectionUtil.isNotEmpty(loraInfo) && !isTest) {
                        for (JSONObject e : loraInfo) {
                            // 新增模型使用日志
                            sdUserModelLogService.asyncInsertData(msg.getLong("userId"),msg.getString("userName"),e.getLong("loraModelId"),e.getString("loraTitle"),msg.getString("modelName"), e.getString("modelStrength"));
                        }
                    }
                    if (CollectionUtil.isNotEmpty(rs.getImages())) {
                        sdUserModelFileService.asyncBatchInsert(rs,msg.getLong("userId"),msg.getString("userName"),loraInfo,msg.getString("modelName"), taskId, isTest?2:1, prompt, newInitImg, promptDesc, promptZh, summonWord, negativePrompt, negativePromptZh, isRedraw);
                    }
                    // 更新进度状态
                    sdUserTaskService.completeWebuiTask(taskId,res.getTimeAsMillisecond());
                    // 扣除绘图次数
                    if (!rs.getImages().isEmpty()) {
                        userService.deductedDrawNum(msg.getLong("userId"),rs.getImages().size());
                        // 将图片发送给第三方
                        ImgSendThirdDto dto = new ImgSendThirdDto().setUserId(msg.getLong("userId")).setImgUrlList(rs.getImages());
                        applicationEventPublisher.publishEvent(new MsgSendThirdEvent(dto));
                    }
                })
                .onError((ex,req,res)->{
                    log.warn("[绘图任务][任务ID:{}]>>>>>>>>>图生图任务失败，请求参数：{}，耗时：{} ms，失败原因{}", taskId, data, res.getTimeAsMillisecond(), ex.getMessage());
                    // 更新进度状态
                    sdUserTaskService.failWebuiTask(taskId,ex.getMessage(),queueTime);
                    if (ex.getMessage().contains("OutOfMemoryError") || ex.getMessage().contains("cpu and cuda:0")) {
                        // 当前GPU显存溢出，需要重启
                        // 自动下架该GPU服务
                        log.error("[GPU服务自动下线]============>deviceId[{}]===>{}",sdGpuPool.getDeviceId(),sdGpuPool);
                        getGpuFromDrawGpuPool(null, sdGpuPool, false);
                        gpuIsOverflow.set(true);
                        // 发送消息GPU服务需要重启
                        JSONObject wxMsg = new JSONObject();
                        wxMsg.put("deviceId",deviceId);
                        wxMsg.put("type","GPU_RESTART");
                        rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg,new CorrelationData(taskId));
                    }
                    else if (isTest){
                        sdUserTaskService.failWebuiTask(taskId,ex.getMessage(),queueTime);
                        JSONObject wxMsg = new JSONObject();
                        wxMsg.put("taskId",taskId);
                        wxMsg.put("type","MODEL_TEST");
                        wxMsg.put("isComplete",false);
                        rabbitTemplate.convertAndSend(WX_MSG_EXCHANGE,WX_MSG_ROUTING_KEY,wxMsg,new CorrelationData(taskId));
                    }
                    else {
                        // 更新进度状态
                        sdUserTaskService.failWebuiTask(taskId,ex.getMessage(), queueTime);
                    }
                })
                .execute();
            if (gpuIsOverflow.get()) {
                // 重新进入队列
                log.error("GPU[{}]服务显存溢出，重新进入队列!",deviceId);
                rabbitTemplate.convertAndSend(SD_IMG_TO_IMG_DRAW_EXCHANGE,SD_IMG_TO_IMG_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
            }
        }
        catch (Exception e) {
            // 更新进度状态
            sdUserTaskService.failWebuiTask(taskId,e.getMessage(), queueTime);
        }
        finally {
            RedisUtils.deleteObject(DRAW_GPU_TASK+taskId);
            returnGpuFromTrainGpuPool(sdGpuPool);
        }

    }



    /** 处理多模型和强度 **/
    private void dealMultiModelAndModelStrength(SdText2ImgDto dto, List<SdUserModel> models, Map<String, String> modelStrengthMap) {
        // 判断是单模型，还是多模型
        if (StrUtil.isNotEmpty(dto.getModelId()) && CollectionUtil.isEmpty(dto.getModelInfos())) {
            SdApiModelParamDto dto1 = new SdApiModelParamDto().setModelId(dto.getModelId()).setModelStrength(StrUtil.isEmptyIfStr(dto.getModelStrength())?"1":dto.getModelStrength());
            dto.setModelInfos(new ArrayList<>(Collections.singletonList(dto1)));
        }
        if (CollectionUtil.isEmpty(dto.getModelInfos())) {
            return;
        }
        List<String> modelIds = dto.getModelInfos().stream().map(SdApiModelParamDto::getModelId).distinct().collect(Collectors.toList());
        List<SdUserModel> modelList = sdUserModelService.selectByIds(modelIds);
        if (CollectionUtil.isEmpty(modelList)) {
            throw new ServiceException("所选模型不存在或已被删除!");
        }
        else if (modelList.size()!=modelIds.size()) {
            throw new ServiceException("所选"+modelIds.size()+"个模型中有部分模型不存在或已被删除!");
        }
        models.addAll(modelList);
        Map<String,String> modelStrengthMap1 = dto.getModelInfos().stream().collect(Collectors.toMap(SdApiModelParamDto::getModelId, SdApiModelParamDto::getModelStrength,(v1, v2)->v1));
        modelStrengthMap.putAll(modelStrengthMap1);
    }


    /** 获取训练卡池 **/
    private SdGpuPool getGpuFromDrawGpuPool(String taskId, SdGpuPool sdGpuPool, Boolean isUpGpu) {
        DRAW_LOCK.lock();
        // 每次只允许一个任务进行获取
        try{
            Set<SdGpuPool> pools = RedisUtils.getCacheSet(DRAW_GPU_POOL);
            if (CollectionUtil.isEmpty(pools)) {
                return null;
            }

            // 下线GPU服务
            if (isUpGpu!=null && !isUpGpu) {
                // 下线GPU绘图服务：表示停止gpu成功
                if (sdGpuPool != null && pools.contains(sdGpuPool)) {
                    RedisUtils.delCacheSet(DRAW_GPU_POOL,Collections.singleton(sdGpuPool));
                    return null;
                }
                // 下线GPU绘图服务失败：表示停止gpu失败
                else if (sdGpuPool != null) {
                    return sdGpuPool;
                }
            }
            // 上线GPU
            else if (isUpGpu != null) {
                // 上线GPU绘图服务：表示上线gpu成功
                if (sdGpuPool != null) {
                    RedisUtils.setCacheSet(DRAW_GPU_POOL,Collections.singleton(sdGpuPool));
                    return null;
                }
            }

            Map<SdGpuPool, Long> usageFrequencyMap = new HashMap<>(pools.size());
            for (SdGpuPool pool : pools) {
                Long frequency = RedisUtils.getCacheMapValue(DRAW_GPU_USAGE_FREQUENCY, String.valueOf(pool.getDeviceId()));
                usageFrequencyMap.put(pool, frequency == null ? 0L : frequency);
            }

            // 按使用频率排序，使用频率低的优先
            List<Map.Entry<SdGpuPool, Long>> sortedEntries = new ArrayList<>(usageFrequencyMap.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue());

            // 选择使用频率最低的 GPU
            SdGpuPool selectedGpu = sortedEntries.get(0).getKey();
            RedisUtils.delCacheSet(DRAW_GPU_POOL, Collections.singleton(selectedGpu));
            RedisUtils.setCacheObject(DRAW_GPU_TASK + taskId, selectedGpu);

            // 更新选中 GPU 的使用频率
            long newFrequency = usageFrequencyMap.get(selectedGpu) + 1;
            RedisUtils.setCacheMapValue(DRAW_GPU_USAGE_FREQUENCY, String.valueOf(selectedGpu.getDeviceId()), newFrequency);
            return selectedGpu;
        }
        finally {
            DRAW_LOCK.unlock();
        }
    }
    /** 归还训练卡池 **/
    private void returnGpuFromTrainGpuPool(SdGpuPool sdGpuPool) {
        if (sdGpuPool==null) {
            return;
        }
        // 每次只允许一个任务进行获取
        DRAW_LOCK.lock();
        try{
            RedisUtils.setCacheSet(DRAW_GPU_POOL,Collections.singleton(sdGpuPool));
        }
        finally {
            DRAW_LOCK.unlock();
        }
    }

}
