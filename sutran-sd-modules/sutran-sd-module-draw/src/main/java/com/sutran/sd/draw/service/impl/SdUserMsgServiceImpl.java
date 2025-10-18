package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbitmq.client.Channel;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.SdUserMsg;
import com.sutran.sd.draw.domain.vo.MsgVo;
import com.sutran.sd.draw.mapper.SdUserModelMapper;
import com.sutran.sd.draw.mapper.SdUserMsgMapper;
import com.sutran.sd.framework.mq.MqConstant;
import com.sutran.sd.draw.service.SdUserMsgService;
import com.sutran.sd.system.mapper.SysUserMapper;
import com.sutran.sd.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author zj
 * @date 2024-04-13
 */
@SuppressWarnings({"LoggingSimilarMessage", "AlibabaUndefineMagicConstant"})
@Slf4j
@Service
@RequiredArgsConstructor
public class SdUserMsgServiceImpl implements SdUserMsgService {

    private final SdUserMsgMapper baseMapper;
    private final WxMpService wxMpService;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SdUserModelMapper sdUserModelMapper;

    @Value("${wx.mp.template-id:}")
    private String templateId;
    @Value("${wx.mp.approve-template-id:}")
    private String approveTemplateId;
    @Value("${wx.mp.model-test-template-id:}")
    private String modelTestTemplateId;
    @Value("${wx.mp.publish-template-id:}")
    private String publishTemplateId;
    @Value("${wx.mp.gpu-alert-template-id:}")
    private String gpuAlertTemplateId;

    @Resource(name = "threadPoolTaskExecutor")
    private Executor executor;

    /**
     * messageId的 SseEmitter对象映射集
     */
    private static final Map<Long, SseEmitter> SSE_EMITTER_MAP = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(Long userId) {
        if (userId==null) {
            throw new ServiceException("当前用户未登录或登录已失效!");
        }
        SseEmitter sseEmitter = new SseEmitter(0L);
        // 连接成功需要返回数据，否则会出现待处理状态
        try {
            MsgVo vo = new MsgVo();
            // 获取当前用户未读公众号条数
            int total = baseMapper.userMsgTotal(userId,0);
            // 获取是否存在未关注微信公众号的消息
            SdUserMsg msg = baseMapper.userNoFollowWxMpOfMsg(userId);
            if (msg!=null) {
                vo.setId(msg.getId()).setType(0).setTitle(msg.getTitle()).setMsgContent(msg.getMsgContent()).setCrtTime(DateUtil.format(msg.getCrtTime(),"yyyy-MM-dd HH:mm"));
                total = total-1;
            }
            vo.setTotal(total);

            // 推送消息
            sseEmitter.send(vo, MediaType.APPLICATION_JSON);

            if (msg!=null) {
                // 修改为已读
                baseMapper.readUserMsg(msg.getId(),new Date());
            }
        }
        catch (IOException e) {
            log.error("[SSE连接异常]>>>>>>>>>原因：",e);
        }
        // 连接断开
        sseEmitter.onCompletion(() -> SSE_EMITTER_MAP.remove(userId));
        // 连接超时
        sseEmitter.onTimeout(() -> {
            SSE_EMITTER_MAP.remove(userId);
            sseEmitter.complete();
        });
        // 连接报错
        sseEmitter.onError((throwable) -> SSE_EMITTER_MAP.remove(userId));
        SSE_EMITTER_MAP.put(userId, sseEmitter);
        return sseEmitter;
    }

    @Override
    public void asyncSendMessage(MsgVo vo, Long userId) {
        CompletableFuture.runAsync(()->{
            SseEmitter sseEmitter = SSE_EMITTER_MAP.get(userId);
            if (sseEmitter==null) {
                log.info("[SSE消息推送]>>>>>>>>>推送失败,当前用户没有连接SSE!");
                return;
            }
            int total = baseMapper.userMsgTotal(userId,0);
            vo.setTotal(total);
            try {
                sseEmitter.send(vo, MediaType.APPLICATION_JSON);
            }
            catch (IOException e) {
                log.error("[SSE消息推送]>>>>>>>>>推送异常：",e);
            }
        },executor);
    }

    @Override
    public List<SdUserMsg> userMsgLatest(Long userId) {
        return baseMapper.userMsgLatest(userId);
    }

    @Override
    public TableDataInfo<SdUserMsg> userMsgList(Long userId, Integer isRead, PageQuery pageQuery) {
        if (pageQuery==null) {
            List<SdUserMsg> list = baseMapper.userMsgList(userId, isRead);
            return TableDataInfo.build(list);
        }
        Page<SdUserMsg> page = baseMapper.userMsgList(userId, isRead, pageQuery.build());
        return TableDataInfo.build(page);
    }

    @Override
    public void readUserMsg(String id) {
        baseMapper.readUserMsg(id,new Date());
    }

    @Override
    public void insertUserMsg(SdUserMsg userMsg) {
        CompletableFuture.runAsync(()->{
            userMsg.setId(IdUtil.getSnowflakeNextIdStr()).setCrtTime(new Date()).setIsRead(0);
            try{
                baseMapper.insert(userMsg);
            }
            catch (Exception e1) {
                log.error("存储消息异常：{}",e1.getMessage());
            }
        },executor);
    }

    @RabbitListener(queues = MqConstant.WX_MSG_QUEUE)
    public void sendWxMsg(Channel channel, Message message) throws IOException {
        byte[] body = message.getBody();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        JSONObject msg = JSON.parseObject(body, JSONObject.class);
        String type = msg.getString("type");

        // 模型测试完成消息通知
        try{
            WxMpTemplateMessage wxMsg = WxMpTemplateMessage.builder().url(null).build();
            switch (type){
                case "MODEL_TEST":
                    sendModelTestMsg(msg,wxMsg);
                    break;
                case "GPU_RESTART":
                    sendGpuServerRestartMsg(msg,wxMsg);
                    break;
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

        wxMsg.setTemplateId(templateId);
        wxMsg.addData(new WxMpTemplateData("thing7", "模型训练完成,等待管理员审核!"));
        modelName = StrUtil.isNotEmpty(modelName) ? modelName : oldModelName;
        String modelName1 = modelName.length() > 11 ? modelName.substring(0, 7) + "..." : modelName;
        wxMsg.addData(new WxMpTemplateData("thing3", modelName1));
        wxMsg.addData(new WxMpTemplateData("time21", startTime));
        wxMsg.addData(new WxMpTemplateData("time11", endTime));
        wxMsg.setToUser(openId);
        try{
            if (StrUtil.isNotEmpty(openId)){
                wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg);
            }
        }
        catch (WxErrorException e) {
            log.error("微信消息提醒发送失败>>>>>>>>>{}",e.getMessage());
        }
        SdUserMsg userMsg = new SdUserMsg().setId(IdUtil.getSnowflakeNextIdStr()).setCrtTime(new Date()).setIsRead(0)
            .setUserId(Long.valueOf(belongUserId)).setWxOpenId(openId).setTemplateId(publishTemplateId)
            .setTitle("模型训练完成").setMsgBody(JSONObject.toJSONString(wxMsg)).setPreTaskId(preTaskId)
            .setMsgContent("您训练的模型 ["+modelName+"] 已完成训练,等待管理员审核!");
        try{
            baseMapper.insert(userMsg);
        }
        catch (Exception e1) {
            log.error("存储消息异常：{}",e1.getMessage());
        }

        // 推送消息
        MsgVo msgVo = new MsgVo();

        msgVo.setId(userMsg.getId()).setType(1).setTitle(userMsg.getTitle()).setMsgContent(userMsg.getMsgContent()).setCrtTime(DateUtil.format(userMsg.getCrtTime(),"yyyy-MM-dd HH:mm"));
        asyncSendMessage(msgVo,Long.valueOf(belongUserId));

        // 发送给管理员
        List<Map<String,String>> openIds = sysUserRoleMapper.selectAdminUserOpenId();
        if (CollectionUtil.isNotEmpty(openIds)) {
            WxMpTemplateMessage wxMsg1 = WxMpTemplateMessage.builder().url(null).build();
            wxMsg1.setTemplateId(approveTemplateId);
            wxMsg1.addData(new WxMpTemplateData("thing2", "模型待审核"));
            // modelName的长度大于20，则取前17个字符+...
            String modelName2 = modelName.length() > 20? modelName.substring(0, 17) + "..." : modelName;
            wxMsg1.addData(new WxMpTemplateData("thing22", modelName2));
            wxMsg1.addData(new WxMpTemplateData("thing16", "有新的模型需要您审核!"));
            wxMsg1.addData(new WxMpTemplateData("thing19", StrUtil.isEmptyIfStr(belongUserName)? belongUserId :belongUserName));
            wxMsg1.addData(new WxMpTemplateData("time4", endTime));

            for (Map<String,String> e : openIds) {
                wxMsg1.setToUser(e.get("wxOpenId"));
                try{
                    if (StrUtil.isNotEmpty(e.get("wxOpenId"))) {
                        wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg1);
                    }
                }
                catch (WxErrorException ex) {
                    log.error("微信消息提醒发送失败>>>>>>>>>{}",ex.getMessage());
                }
                SdUserMsg userMsg1 = new SdUserMsg().setId(IdUtil.getSnowflakeNextIdStr()).setCrtTime(new Date()).setIsRead(0)
                    .setUserId(Long.valueOf(e.get("userId"))).setWxOpenId(openId).setTemplateId(publishTemplateId)
                    .setTitle("模型待审核").setMsgBody(JSONObject.toJSONString(wxMsg))
                    .setMsgContent("有新的模型 ["+modelName+"] 需要您审核!");
                try{
                    baseMapper.insert(userMsg1);
                }
                catch (Exception e1) {
                    log.error("存储消息异常：{}",e1.getMessage());
                }

                // 推送消息
                msgVo.setId(userMsg.getId()).setType(1).setTitle(userMsg.getTitle()).setMsgContent(userMsg.getMsgContent()).setCrtTime(DateUtil.format(userMsg.getCrtTime(),"yyyy-MM-dd HH:mm"));
                asyncSendMessage(msgVo,Long.valueOf(e.get("userId")));
            }
        }
    }

    /** 发布模型提醒 **/
    private void sendPublishModelMsg(JSONObject msg, WxMpTemplateMessage wxMsg) {
        String modelId = msg.getString("modelId");
        long userId = msg.getLongValue("userId");
        String wxOpenId = msg.getString("openId");
        SdUserModel model = sdUserModelMapper.selectById(modelId);
        if (model!=null) {
            wxMsg.setTemplateId(publishTemplateId);
            wxMsg.addData(new WxMpTemplateData("thing7", "模型发布成功"));
            String modelName = model.getModelNameZh().substring(0, model.getModelNameZh().lastIndexOf("-"));
            modelName = modelName.length()>9?modelName.substring(0,6)+"...":modelName;
            wxMsg.addData(new WxMpTemplateData("thing12", "您训练的模型"+modelName+"已审核发布"));
            wxMsg.setToUser(wxOpenId);
            try{
                if (StrUtil.isNotEmpty(wxOpenId)) {
                    wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg);
                }
            }
            catch (WxErrorException e) {
                log.error("微信消息提醒发送失败>>>>>>>>>{}",e.getMessage());
            }
            SdUserMsg userMsg = new SdUserMsg().setId(IdUtil.getSnowflakeNextIdStr()).setCrtTime(new Date()).setIsRead(0)
                .setUserId(userId).setWxOpenId(wxOpenId).setTemplateId(publishTemplateId)
                .setTitle("模型发布成功").setMsgBody(JSONObject.toJSONString(wxMsg))
                .setMsgContent("您训练的模型"+modelName+"已完成审核并发布!");
            try{
                baseMapper.insert(userMsg);
            }
            catch (Exception e1) {
                log.error("存储消息异常：{}",e1.getMessage());
            }

            // 推送消息
            MsgVo msgVo = new MsgVo().setId(userMsg.getId()).setType(1).setTitle(userMsg.getTitle()).setMsgContent(userMsg.getMsgContent()).setCrtTime(DateUtil.format(userMsg.getCrtTime(),"yyyy-MM-dd HH:mm"));
            asyncSendMessage(msgVo,userId);
        }
    }

    /** GPU重启提醒 **/
    private void sendGpuServerRestartMsg(JSONObject msg, WxMpTemplateMessage wxMsg) {
        int deviceId = msg.getIntValue("deviceId");
        wxMsg.setTemplateId(gpuAlertTemplateId);
        wxMsg.addData(new WxMpTemplateData("thing4", "GPU内存溢出"));
        wxMsg.addData(new WxMpTemplateData("thing9", "GPU_"+deviceId+"内存溢出,急需重启"));
        List<Map<String,String>> openIds = sysUserRoleMapper.selectAdminUserOpenId();
        if (CollectionUtil.isNotEmpty(openIds)) {

            MsgVo msgVo = new MsgVo();
            for (Map<String,String> e : openIds) {
                wxMsg.setToUser(e.get("wxOpenId"));
                try{
                    wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg);
                }
                catch (WxErrorException ex) {
                    log.error("微信消息提醒发送失败>>>>>>>>>{}",ex.getMessage());
                }
                SdUserMsg userMsg = new SdUserMsg().setId(IdUtil.getSnowflakeNextIdStr()).setCrtTime(new Date()).setIsRead(0)
                    .setUserId(Long.valueOf(e.get("userId"))).setWxOpenId(e.get("wxOpenId")).setTemplateId(gpuAlertTemplateId)
                    .setTitle("GPU内存溢出").setMsgBody(JSONObject.toJSONString(wxMsg))
                    .setMsgContent("GPU_"+deviceId+"内存溢出,急需重启!");
                try{
                    baseMapper.insert(userMsg);
                }
                catch (Exception e1) {
                    log.error("存储消息异常：{}",e1.getMessage());
                }

                // 推送消息
                msgVo.setId(userMsg.getId()).setType(1).setTitle(userMsg.getTitle()).setMsgContent(userMsg.getMsgContent()).setCrtTime(DateUtil.format(userMsg.getCrtTime(),"yyyy-MM-dd HH:mm"));
                asyncSendMessage(msgVo,Long.valueOf(e.get("userId")));
            }
        }
    }

    /** 模型测试结束提醒 **/
    private void sendModelTestMsg(JSONObject msg, WxMpTemplateMessage wxMsg) {
        String taskId = msg.getString("taskId");
        // 获取测试的模型
        JSONObject info = sdUserModelMapper.selectLoraModelNameByTaskId(taskId);
        if (info==null) {
            return;
        }
        // 获取管理员openId
        List<Map<String,String>> openIds = sysUserRoleMapper.selectAdminUserOpenId();
        if (CollectionUtil.isNotEmpty(openIds)) {
            boolean isComplete = msg.getBooleanValue("isComplete");
            wxMsg.setTemplateId(modelTestTemplateId);
            wxMsg.addData(new WxMpTemplateData("thing7", isComplete?"模型测试完成":"模型测试失败"));
            String modelName1 = info.getString("modelNameZh").substring(0, info.getString("modelNameZh").lastIndexOf("-"));
            String modelName = modelName1.length()>11?modelName1.substring(0,7)+"...":modelName1;
            wxMsg.addData(new WxMpTemplateData("thing12", modelName));
            wxMsg.addData(new WxMpTemplateData("time21", info.getString("startTime")));
            wxMsg.addData(new WxMpTemplateData("time11", info.getString("endTime")));

            MsgVo msgVo = new MsgVo();
            for (Map<String,String> e : openIds) {
                wxMsg.setToUser(e.get("wxOpenId"));
                try{
                    if (StrUtil.isNotEmpty(e.get("wxOpenId"))) {
                        wxMpService.getTemplateMsgService().sendTemplateMsg(wxMsg);
                    }
                }
                catch (WxErrorException ex) {
                    log.error("微信消息提醒发送失败>>>>>>>>>{}",ex.getMessage());
                }
                SdUserMsg userMsg = new SdUserMsg().setId(IdUtil.getSnowflakeNextIdStr()).setCrtTime(new Date()).setIsRead(0)
                    .setUserId(Long.valueOf(e.get("userId"))).setWxOpenId(e.get("wxOpenId")).setTemplateId(modelTestTemplateId)
                    .setTitle(isComplete?"模型测试完成":"模型测试失败").setMsgBody(JSONObject.toJSONString(wxMsg))
                    .setMsgContent("模型 "+modelName1+(isComplete?" 已完成测试!":" 测试失败!"));
                try{
                    baseMapper.insert(userMsg);
                }
                catch (Exception e1) {
                    log.error("存储消息异常：{}",e1.getMessage());
                }

                // 推送消息
                msgVo.setId(userMsg.getId()).setType(1).setTitle(userMsg.getTitle()).setMsgContent(userMsg.getMsgContent()).setCrtTime(DateUtil.format(userMsg.getCrtTime(),"yyyy-MM-dd HH:mm"));
                asyncSendMessage(msgVo,Long.valueOf(e.get("userId")));
            }
        }
    }

    /** 获取用户的openId **/
    private String getOpenId(Long belongUserId) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().select(SysUser::getWxOpenId).eq(SysUser::getUserId, belongUserId));
        return ObjectUtil.isNull(sysUser) ? null : sysUser.getWxOpenId();
    }

}
