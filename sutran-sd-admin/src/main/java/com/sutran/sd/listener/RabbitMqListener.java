package com.sutran.sd.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.sutran.sd.common.core.domain.vo.NoticeMpVo;
import com.sutran.sd.common.core.service.NoticeService;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.service.SdUserModelService;
import com.sutran.sd.framework.mq.MqConstant;
import com.sutran.sd.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2025年11月10日 22:17
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqListener {

    @Value("${wx.mp.template-id:}")
    private String templateId;
    @Value("${wx.mp.approve-template-id:}")
    private String approveTemplateId;
    @Value("${wx.mp.publish-template-id:}")
    private String publishTemplateId;

    private final WxMpService wxMpService;
    private final ISysUserService sysUserService;
    private final NoticeService noticeService;
    private final SdUserModelService sdUserModelService;

    @RabbitListener(queues = MqConstant.NEW_WX_MSG_QUEUE)
    public void sendWxMsg(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        JSONObject msg = JSON.parseObject(body, JSONObject.class);
        String type = msg.getString("type");

        // 模型测试完成消息通知
        try{
            WxMpTemplateMessage wxMsg = WxMpTemplateMessage.builder().url(null).build();
            switch (type){
                case "PUBLISH_MODEL":
                    sendPublishModelMsg(msg,wxMsg);
                    break;
                case "MODEL_TRAIN":
                    sendTrainEndMsg(msg,wxMsg);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            log.error("发送消息异常：",e);
        }
        finally {
            channel.basicAck(deliveryTag, false);
        }
    }

    /** 模型训练完成 **/
    private void sendTrainEndMsg(JSONObject msg, WxMpTemplateMessage wxMsg) {
        // 发送给模型拥有者
        final String belongUserId = msg.getString("belongUserId");
        final String preTaskId = msg.getString("preTaskId");
        final String belongUserName = msg.getString("belongUserName");
        final String startTime = msg.getString("startTime");
        final String endTime = msg.getString("endTime");
        final String oldModelName = msg.getString("oldModelName");
        final String openId = getOpenId(Long.valueOf(belongUserId));
        String modelName = msg.getString("modelName");

        // 发送给模型拥有者的消息通知
        wxMsg.setTemplateId(templateId);
        wxMsg.addData(new WxMpTemplateData("thing7", "模型训练完成,等待管理员审核!"));
        modelName = StrUtil.isNotEmpty(modelName) ? modelName : oldModelName;
        String modelName1 = modelName.length() > 11 ? modelName.substring(0, 7) + "..." : modelName;
        wxMsg.addData(new WxMpTemplateData("thing3", modelName1));
        wxMsg.addData(new WxMpTemplateData("time21", startTime));
        wxMsg.addData(new WxMpTemplateData("time11", StrUtil.isNotEmpty(endTime) ? endTime : DateUtil.now()));
        wxMsg.setToUser(openId);
        try{
            if (StrUtil.isNotEmpty(openId)){
                wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg);
            }
        }
        catch (WxErrorException e) {
            log.error("[模型训练完成]>>>>>>>>>微信消息提醒推送给模型拥有者失败：{}",e.getMessage());
        }

        JSONObject otherParams = new JSONObject();
        otherParams.put("trainTaskId",preTaskId);
        NoticeMpVo userMsg = new NoticeMpVo().setId(IdUtil.getSnowflakeNextIdStr()).setPublishTime(new Date()).setTitle("模型训练完成").setMpContent(JSONObject.toJSONString(wxMsg)).setOtherParams(otherParams.toJSONString()).setContent("您训练的模型 ["+modelName+"] 已完成训练,等待管理员审核!");
        noticeService.asyncSendMpMsg(userMsg, Long.valueOf(belongUserId));


        // 发送给管理员
        List<Map<String,String>> openIds = sysUserService.selectAdminUserOpenId();
        if (CollectionUtil.isNotEmpty(openIds)) {
            WxMpTemplateMessage wxMsg1 = WxMpTemplateMessage.builder().url(null).build();
            wxMsg1.setTemplateId(approveTemplateId);
            wxMsg1.addData(new WxMpTemplateData("thing2", "模型待审核"));
            // modelName的长度大于20，则取前17个字符+...
            String modelName2 = modelName.length() > 20? modelName.substring(0, 17) + "..." : modelName;
            wxMsg1.addData(new WxMpTemplateData("thing22", modelName2));
            wxMsg1.addData(new WxMpTemplateData("thing16", "有新的模型需要您审核!"));
            wxMsg1.addData(new WxMpTemplateData("thing19", StrUtil.isEmptyIfStr(belongUserName)? belongUserId :belongUserName));
            wxMsg1.addData(new WxMpTemplateData("time4", StringUtils.isNotBlank(endTime)?endTime:DateUtil.now()));

            for (Map<String,String> e : openIds) {
                wxMsg1.setToUser(e.get("wxOpenId"));
                try{
                    if (StrUtil.isNotEmpty(e.get("wxOpenId"))) {
                        wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg1);
                    }
                }
                catch (WxErrorException ex) {
                    log.error("[模型训练完成]>>>>>>>>>微信消息提醒推送给管理员失败：{}",ex.getMessage());
                }
                NoticeMpVo userMsg1 = new NoticeMpVo().setId(IdUtil.getSnowflakeNextIdStr()).setPublishTime(new Date()).setTitle("模型待审核").setMpContent(JSONObject.toJSONString(wxMsg)).setContent("有新的模型 ["+modelName+"] 需要您审核!");
                noticeService.asyncSendMpMsg(userMsg1, Long.valueOf(e.get("userId")));
            }
        }
    }

    /** 发布模型提醒 **/
    private void sendPublishModelMsg(JSONObject msg, WxMpTemplateMessage wxMsg) {
        String modelId = msg.getString("modelId");
        long userId = msg.getLongValue("userId");
        String wxOpenId = msg.getString("openId");
        SdUserModel model = sdUserModelService.selectById(modelId);
        if (model!=null) {
            wxMsg.setTemplateId(publishTemplateId);
            wxMsg.addData(new WxMpTemplateData("thing7", "模型发布成功"));
            String modelName = model.getModelNameZh().contains("-")?model.getModelNameZh().substring(0, model.getModelNameZh().lastIndexOf("-")):model.getModelNameZh();
            modelName = modelName.length()>11?modelName.substring(0,11)+"...":modelName;
            wxMsg.addData(new WxMpTemplateData("thing12", "模型["+modelName+"]已审核发布"));
            wxMsg.setToUser(wxOpenId);
            try{
                if (StrUtil.isNotEmpty(wxOpenId)) {
                    wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg);
                }
            }
            catch (WxErrorException e) {
                log.error("[模型发布]>>>>>>>>>微信消息提醒推送给模型拥有者失败：{}",e.getMessage());
            }
            NoticeMpVo userMsg = new NoticeMpVo().setId(IdUtil.getSnowflakeNextIdStr()).setPublishTime(new Date()).setTitle("模型发布成功").setMpContent(JSONObject.toJSONString(wxMsg)).setContent("您训练的模型["+modelName+"]已完成审核并发布!");
            noticeService.asyncSendMpMsg(userMsg, userId);
        }
    }

    /** 获取用户的openId **/
    private String getOpenId(Long belongUserId) {
        return sysUserService.selectOpenIdByUserId(belongUserId);
    }

}
