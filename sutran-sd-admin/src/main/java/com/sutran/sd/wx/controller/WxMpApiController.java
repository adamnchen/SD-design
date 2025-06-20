package com.sutran.sd.wx.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.sutran.sd.common.core.domain.dto.WxMsgDto;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 消息API
 * @author zj
 * @date 2024-02-27
 */
@RestController
@RequestMapping("/wx")
@RequiredArgsConstructor
public class WxMpApiController {
    @Resource
    private WxMpService wxMpService;

    /**
     * 微信公众号-微信公众号验证接口
     */
    @GetMapping("/verify")
    @SaIgnore
    public String verify(String signature, String timestamp, String nonce, String echostr) {
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            // 消息签名不正确，说明不是公众平台发过来的消息
            return null;
        }
        // 消息合法
        return echostr;
    }

    /**
     * 微信公众号-测试消息推送
     */
    @GetMapping("/test-send")
    @SaIgnore
    public void testMsg(@RequestBody WxMsgDto data) throws WxErrorException {
        WxMpTemplateMessage message = WxMpTemplateMessage.builder().toUser(data.getOpenId()).templateId(data.getTemplateId()).url(data.getUrl()).build();
        for (String key : data.getParam().keySet()) {
            message.addData(new WxMpTemplateData(key, data.getParam().getStr(key)));
        }
        wxMpService.getTemplateMsgService().sendTemplateMsg(message);
    }

}
