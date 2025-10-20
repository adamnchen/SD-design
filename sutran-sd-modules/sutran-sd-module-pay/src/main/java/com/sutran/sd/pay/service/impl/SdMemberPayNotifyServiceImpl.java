package com.sutran.sd.pay.service.impl;

import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 *会员订单支付回调
 * @author zj
 * @date 2025年10月18日 11:30
 */
@SuppressWarnings("AlibabaAvoidComplexCondition")
@Slf4j
@Service(PayNotifyServer.SD_MEMBER_NOTIFY)
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class SdMemberPayNotifyServiceImpl extends BasePayNotifyService {

    private final UserService userService;
    private final PayMemberService payMemberService;

    @Override
    public void handleSuccessBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Long businessId, Long userId) {
        // 处理用户会员逻辑
        PayMember payMember = payMemberService.detailById(businessId.toString());
        userService.insertMember(userId,businessId,new Date(),payMember,outTradeNo);
    }

    @Override
    public void handleFailedBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment) {
    }

    @Override
    public void dealPayTimeoutData(PayTimeoutStatusVo vo) {
        try {
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(vo.getTradeStatus()) || AliPayTradeStatus.TRADE_FINISHED.name().equals(vo.getTradeStatus())) {
                PayMember payMember = payMemberService.detailById(vo.getBusinessId().toString());
                // 处理用户会员逻辑
                userService.insertMember(vo.getUserId(),vo.getBusinessId(),new Date(),payMember,vo.getOutTradeNo());
            }
        }
        catch (Exception e) {
            log.error("[会员支付超时]>>>>>>>>>支付超时,异常信息: ", e);
        }
    }
}
