package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.Forest;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.sdapi.modules.system.entity.SdChannelData;
import com.sutran.sd.sdapi.modules.system.entity.SdCommonConfig;
import com.sutran.sd.sdapi.mapper.SdChannelDataMapper;
import com.sutran.sd.sdapi.modules.system.SdChannelDataService;
import com.sutran.sd.sdapi.modules.system.SdCommonConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
@Slf4j
@Service
public class SdChannelDataServiceImpl implements SdChannelDataService {

    @Resource
    private SdChannelDataMapper baseMapper;
    @Resource
    private SdCommonConfigService sdCommonConfigService;
    @Value("${third.api.privateKey}")
    private String privateKey;

    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncInsert(String picList,String channelUserId,String userId) {
        SdChannelData msg = new SdChannelData().setId(IdUtil.getSnowflakeNextId()).setPicUrlList(picList)
            .setChannelUserId(channelUserId).setRetryTimes(null).setUserId(userId)
            .setApiUrl("https://apiserver.chinagoods.com/cgchat/v1/open/ai/pic/save");
        SdCommonConfig config = sdCommonConfigService.selectOne();
        if (config!=null && StringUtils.isNotBlank(config.getChannelSendApiUrl())) {
            msg.setApiUrl(config.getChannelSendApiUrl());
        }
        asyncSend(msg,false);
        baseMapper.insert(msg);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncSend(SdChannelData msg,boolean isModify) {
        final Integer retryTimes = msg.getRetryTimes();
        msg.setSendTime(new Date());
        SdCommonConfig config = sdCommonConfigService.selectOne();
        if (config!=null && StringUtils.isNotBlank(config.getChannelSendApiUrl())) {
            msg.setApiUrl(config.getChannelSendApiUrl());
        }

        long time = DateUtil.offsetMinute(new Date(), 10).getTime();
        Sign sign = new Sign(SignAlgorithm.SHA1withRSA,privateKey,null);
        String data = "expireTime="+time+"&picList="+msg.getPicUrlList()+"&userId="+msg.getChannelUserId();
        byte[] signedData = sign.sign(data.getBytes(CharsetUtil.CHARSET_UTF_8));
        String signature = StrUtil.str(Base64.encodeBase64(signedData), CharsetUtil.CHARSET_UTF_8);

        // 调用接口发送数据{"expireTime": 11111,"picList": "", "sign": "", "userId": ""}
        Forest.post(msg.getApiUrl()).contentTypeJson()
            .addBody("expireTime", time)
            .addBody("sign", signature)
            .addBody("userId", msg.getChannelUserId())
            .addBody("picList", msg.getPicUrlList())
            .onSuccess((result, req, res)->{
                // 推送图片数据成功，保存记录
                msg.setIsSend(1);
            })
            .onError((ex,req,res)->{
                // 推送图片数据失败，保存记录
                msg.setIsSend(0).setErrorMsg(ex.getMessage());
            })
            .execute();
        msg.setRetryTimes(retryTimes==null?0:retryTimes+1);

        if (isModify) {
            baseMapper.update(msg, new LambdaQueryWrapper<SdChannelData>().eq(SdChannelData::getId,msg.getId()));
        }
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncModify(SdChannelData msg) {
        baseMapper.update(msg, new LambdaQueryWrapper<SdChannelData>().eq(SdChannelData::getId,msg.getId()));
    }

    @Override
    public List<SdChannelData> queryList(SdChannelData msg) {
        return baseMapper.selectVoList(this.buildQueryWrapper(msg));
    }

    private Wrapper<SdChannelData> buildQueryWrapper(SdChannelData msg) {
        QueryWrapper<SdChannelData> wrapper = Wrappers.query();
        wrapper.eq(StringUtils.isNotBlank(msg.getUserId()), "user_id", msg.getUserId())
            .eq(StringUtils.isNotBlank(msg.getChannelUserId()), "channel_user_id", msg.getChannelUserId())
            .eq(msg.getIsSend()!=null, "is_send", msg.getIsSend())
            .le(msg.getSendTime()!=null, "send_time", msg.getSendTime());
        return wrapper;
    }
}
