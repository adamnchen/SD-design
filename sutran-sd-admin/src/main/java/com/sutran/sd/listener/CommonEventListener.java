package com.sutran.sd.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.draw.domain.dto.ImgSendThirdDto;
import com.sutran.sd.draw.events.MsgSendThirdEvent;
import com.sutran.sd.draw.events.RefreshLoraEvent;
import com.sutran.sd.draw.service.SdChannelDataService;
import com.sutran.sd.draw.service.SdWebuiApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * spring事件监听
 * @author zj
 * @date 2024-09-24
 */
@Component
@RequiredArgsConstructor
public class CommonEventListener {

    private final SdWebuiApiService sdWebuiApiService;
    private final UserService userService;
    private final SdChannelDataService sdChannelDataService;

    /**
     * 消息推送到第三方
     */
    @EventListener(classes = {MsgSendThirdEvent.class})
    @Async("threadPoolTaskExecutor")
    public void msgSendThird(MsgSendThirdEvent event) {
        ImgSendThirdDto dto = (ImgSendThirdDto) event.getSource();
        if (StrUtil.isBlankIfStr(dto.getUserId()) || CollectionUtil.isEmpty(dto.getImgUrlList())) {
            return;
        }
        // 获取当前用户的第三方userId
        String channelUserId = userService.selectChannelUserIdById(dto.getUserId());
        if (StrUtil.isBlankIfStr(channelUserId)) {
            return;
        }
        String picList = JSONObject.toJSONString(dto.getImgUrlList());
        sdChannelDataService.asyncInsert(picList,channelUserId,String.valueOf(dto.getUserId()));
    }

    /**
     * 刷新Lora模型
     */
    @EventListener(classes = {RefreshLoraEvent.class})
    @Async("threadPoolTaskExecutor")
    public void refreshLoraModels() {
        sdWebuiApiService.refreshLoraModels();
    }

}
