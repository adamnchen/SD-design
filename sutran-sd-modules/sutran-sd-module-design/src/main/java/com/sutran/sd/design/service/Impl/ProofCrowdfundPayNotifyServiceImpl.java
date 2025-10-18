package com.sutran.sd.design.service.impl;

import com.ijpay.alipay.AliPayApiConfig;
import com.rabbitmq.client.Channel;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 众筹订单支付回调
 * @author zj
 * @date 2025年10月18日 11:30
 */
@Slf4j
@Service(PayNotifyServer.PROOF_CROWDFUND_NOTIFY)
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class ProofCrowdfundPayNotifyServiceImpl extends BasePayNotifyService {

    private final PayOrderService payOrderService;

    @Override
    public String handleBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, AliPayApiConfig aliPayConfig) {
        try {
            // 业务逻辑：更新订单状态（需保证幂等性，避免重复处理）
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                // 查询订单
                PayOrder order = payOrderService.detailByOutTradeNo(outTradeNo);
                if (order == null) {
                    log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{}，未查询到订单记录", outTradeNo);
                    return "failure";
                }
                // 检查订单状态,已处理过，直接返回成功
                if (order.getStatus() != 0) {
                    return "success";
                }
                // 修改订单状态
                payOrderService.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);
            }
            // 支付失败
            else {
                payOrderService.failPay(outTradeNo, tradeNo, totalAmount);
                //TODO 回滚redis总金额、按照订单删除对应的参与者
                log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{},流水号：{},交易状态：{}",outTradeNo,tradeNo,tradeStatus);
            }
            return "success";
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
                return;
            }
            //TODO 回滚redis总金额、按照订单删除对应的参与者

        }
        catch (Exception e) {
            log.error("[众筹打样支付超时]>>>>>>>>>超时业务逻辑处理异常,异常信息: ", e);
        }
    }

}
