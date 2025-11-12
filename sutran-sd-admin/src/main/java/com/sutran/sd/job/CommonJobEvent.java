package com.sutran.sd.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.sutran.sd.common.core.service.NoticeService;
import com.sutran.sd.draw.service.SdComfyuiApiService;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.service.SdDrawNodeService;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayOrderService;
import com.sutran.sd.draw.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.draw.service.SdChannelDataService;
import com.sutran.sd.draw.domain.SdChannelData;
import com.sutran.sd.draw.service.SdTrainService;
import com.sutran.sd.draw.service.SdWebuiApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.sutran.sd.common.constant.CacheConstants.*;

/**
 * @author zj
 * @date 2024-03-31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CommonJobEvent {

    private final SdTrainService sdTrainService;
    private final SdWebuiApiService sdWebuiApiService;
    private final SdChannelDataService sdChannelDataService;
    private final SdDrawNodeService sdDrawNodeService;
    private final PayOrderService payOrderService;
    private final AliPayService aliPayService;
    private final SdComfyuiApiService sdComfyuiApiService;
    private final NoticeService noticeService;

    /**
     * 定时处理训练任务V1
     * 每10秒执行一次
     */
    @Scheduled(cron="0/10 * * * * ?")
    public void executeTrainProgressV1(){
        Map<String, String> cacheMap = RedisUtils.getCacheMap(TRAIN_MODEL_PROGRESS_TASK_MAP_V1);
        if (CollectionUtil.isEmpty(cacheMap)) {
            return;
        }
        cacheMap.forEach((taskId, preTaskId) -> sdTrainService.trainProgress(taskId));
    }

    /**
     * 定时处理训练任务V2
     * 每10秒执行一次
     */
    @Scheduled(cron="0/10 * * * * ?")
    public void executeTrainProgressV2(){
        Map<String, String> cacheMap = RedisUtils.getCacheMap(TRAIN_MODEL_PROGRESS_TASK_MAP_V2);
        if (CollectionUtil.isEmpty(cacheMap)) {
            return;
        }
        cacheMap.forEach((taskId, preTaskId) -> sdTrainService.trainProgressV2(taskId));
    }

    /**
     * 定时处理预处理任务V1
     * 每10秒执行一次
     */
    @Scheduled(cron="0/10 * * * * ?")
    public void executePreImgProgressV1(){
        List<String> cacheList = RedisUtils.getCacheList(PRE_IMG_TASK_QUEUE_LIST_V1);
        if (CollectionUtil.isEmpty(cacheList)) {
            return;
        }
        for (String preTaskId : cacheList) {
            sdTrainService.getPreImgProgress(preTaskId);
        }
    }

    /**
     * 定时处理预处理任务V2
     * 每10秒执行一次
     */
    @Scheduled(cron="0/10 * * * * ?")
    public void executePreImgProgressV2(){
        List<String> cacheList = RedisUtils.getCacheList(PRE_IMG_TASK_QUEUE_LIST_V2);
        if (CollectionUtil.isEmpty(cacheList)) {
            return;
        }
        for (String preTaskId : cacheList) {
            sdTrainService.getPreImgProgressV2(preTaskId);
        }
    }

    /**
     * 定时拉取lora模型
     * 每10分钟执行一次
     */
//    @Scheduled(cron="0 0/10 * * * ?")
    public void executeRefreshLora(){
        sdWebuiApiService.refreshLoraModels();
    }

    /**
     * 定时清理标签翻译缓存
     * 每5分钟执行一次
     */
    @Scheduled(cron="0 0/5 * * * ?")
    public void executeClearTranslateMap(){
        Collection<String> keys = RedisUtils.keys(TRAIN_TAG_TRANSLATE_MAP+"*");
        if (CollectionUtil.isEmpty(keys)) {
            return;
        }
        for (String key : keys) {
            String preTaskId = key.replace(TRAIN_TAG_TRANSLATE_MAP, "");
            TrainTaskStatusVo taskStatus = sdTrainService.getSdTaskStatusOfJob(preTaskId);
            if (taskStatus==null || taskStatus.getNewStatus()>4) {
                RedisUtils.deleteMultiObject(TRAIN_TAG_TRANSLATE_MAP+preTaskId,TRAIN_ADDITION_LIST+preTaskId);
            }
        }
    }

    /**
     * 定时推送消息
     * 每5分钟执行一次
     */
    @Scheduled(cron="0 0/5 * * * ?")
    public void executeSendChannelMsg(){
        try{
            // 10分钟前的数据
            DateTime dateTime = DateUtil.offsetMinute(new Date(), -10);
            List<SdChannelData> list = sdChannelDataService.queryList(new SdChannelData().setIsSend(0).setSendTime(dateTime));
            if (CollectionUtil.isEmpty(list)) {
                return;
            }
            for (SdChannelData msg : list) {
                sdChannelDataService.asyncSend(msg,true);
            }
        }
        catch (Exception e) {
            log.error("[定时任务]>>>>>>>>>定时推送渠道数据异常：",e);
        }
    }

    /**
     * 定时处理支付订单超时的数据
     * 每2分钟执行一次
     */
    @Scheduled(cron="0 0/2 * * * ?")
    public void executeHandleMemberPayTimeout(){
        try{
            Collection<String> outTradeNos = RedisUtils.getLeCacheZSet(PAY_ORDER_TASK, System.currentTimeMillis());
            if (CollectionUtil.isEmpty(outTradeNos)) {
                return;
            }
            aliPayService.getConfig();
            for (String outTradeNo : outTradeNos) {
                // 处理支付未超时且未支付的数据
                payOrderService.handleNoPayOfDataByOutTradeNo(outTradeNo);
            }
        }
        catch (Exception e) {
            log.error("[定时任务]>>>>>>>>>定时处理支付超时数据异常：",e);
        }
    }

    /**
     * 定时处理绘图节点健康检查
     * 每10秒执行一次
     */
    @Scheduled(cron="0/20 * * * * ?")
    public void executeDrawNodeHealthCheck(){
        sdDrawNodeService.drawNodeHealthCheck();
    }

    /**
     * 定时处理训练节点健康检查
     * 每10秒执行一次
     */
    @Scheduled(cron="0/20 * * * * ?")
    public void executeTrainNodeHealthCheck(){
        sdDrawNodeService.trainNodeHealthCheck();
    }

    /**
     * 定时处理节点任务
     * 每10秒执行一次
     */
    @Scheduled(cron="0/20 * * * * ?")
    public void executeComfyDrawTask(){
        Map<String, String> cacheMap = RedisUtils.getCacheMap(DRAW_NODE_TASK_MAP);
        if (CollectionUtil.isEmpty(cacheMap)) {
            return;
        }
        cacheMap.forEach((nodeId, taskId) -> {
            try{
                sdComfyuiApiService.autoDealComfyTask(nodeId, taskId);
            }
            catch (Exception e){
                log.error("[定时任务]>>>>>>>>>定时处理绘图节点任务异常：",e);
            }
        });
    }

    /**
     * 定时处理节点任务
     * 每10秒执行一次
     */
    @Scheduled(cron="0/20 * * * * ?")
    public void executeFluxgymTrainTask(){
        Map<String, String> cacheMap = RedisUtils.getCacheMap(TRAIN_NODE_TASK_MAP);
        if (CollectionUtil.isEmpty(cacheMap)) {
            return;
        }
        cacheMap.forEach((nodeId,taskId) -> {
            try{
                sdTrainService.getFluxgymProgress(taskId,nodeId,true);
            }
            catch (Exception e){
                log.error("[定时任务]>>>>>>>>>定时处理训练节点任务异常：",e);
            }
        });
    }

    /**
     * 定时处理节点任务
     * 每10秒执行一次
     */
    @Scheduled(cron="0/10 * * * * ?")
    public void executeNotice(){
        try{
            noticeService.dealExpireData(new Date());
        }
        catch (Exception e){
            log.error("[定时任务]>>>>>>>>>定时处理通知异常：",e);
        }
    }

}
