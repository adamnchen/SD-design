package com.sutran.sd.sdapi.modules.system;

import com.sutran.sd.sdapi.modules.system.entity.SdChannelData;

import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
public interface SdChannelDataService {

    /**
     * 异步保存Sd绘图图片消息推送记录
     *
     * @param picList           图片数组集合
     * @param channelUserId     渠道用户ID
     * @param userId            用户ID
     */
    void asyncInsert(String picList, String channelUserId, String userId);

    /**
     * 异步推送渠道消息
     * @param msg   消息体
     * @param isModify   是否执行修改
     */
    void asyncSend(SdChannelData msg,boolean isModify);

    /**
     * 异步修改Sd绘图图片消息推送记录
     * @param msg 实体
     */
    void asyncModify(SdChannelData msg);

    /**
     * 查询列表
     * @return 列表数据
     */
    List<SdChannelData> queryList(SdChannelData msg);
}

