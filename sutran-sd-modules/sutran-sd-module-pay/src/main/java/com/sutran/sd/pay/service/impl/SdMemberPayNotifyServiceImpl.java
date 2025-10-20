package com.sutran.sd.pay.service.impl;

import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayMemberService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

    private final PayOrderService payOrderService;
    private final UserService userService;
    private final PayMemberService payMemberService;

    @Override
    public String handleBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment) {
        try {
            // 业务逻辑：更新订单状态（需保证幂等性，避免重复处理）
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                // 查询订单
                PayOrder order = payOrderService.detailByOutTradeNo(outTradeNo);
                if (order == null) {
                    log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{}，未查询到订单记录",outTradeNo);
                    return "failure";
                }
                // 检查订单状态,已处理过，直接返回成功
                if (order.getStatus() != 0) {
                    return "success";
                }
                // 修改订单状态
                boolean updateSuccess = payOrderService.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);
                if (updateSuccess) {
                    // 通知支付宝处理成功，不再重复通知，并处理业务逻辑
                    PayMember payMember = payMemberService.detailById(order.getBusinessId().toString());
                    // 处理用户会员逻辑
                    userService.insertMember(order.getUserId(),order.getBusinessId(),new Date(),payMember,outTradeNo);
                    return "success";
                }
                else {
                    // 业务处理失败，支付宝会重试（最多8次）
                    return "failure";
                }
            }
            else {
                payOrderService.failPay(outTradeNo, tradeNo, totalAmount);
                log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{},流水号：{},交易状态：{}",outTradeNo,tradeNo,tradeStatus);
                return "failure";
            }
        }
        catch (Exception e) {
            log.error("[支付宝][支付回调验证]>>>>>>>>>支付结果回调异常:",e);
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "failure";
        }
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
