package com.sutran.sd.pay.service;

import com.alipay.api.internal.util.AlipaySignature;
import com.ijpay.alipay.AliPayApi;
import com.sutran.sd.common.utils.spring.SpringUtils;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付回调抽象类
 * @author zj
 * @date 2025年10月18日 11:26
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BasePayNotifyService {

    /**
     * 支付订单服务
     * 通过spring上下文获取
     */
    private static final PayOrderService ORDER_SERVICE = SpringUtils.getBean(PayOrderService.class);

    /**
     * 处理支付回调
     *
     * @param request 支付回调请求
     * @param aliPayCertPath 支付宝公钥证书路径
     * @return 处理结果
     */
    public String handleNotify(HttpServletRequest request,String aliPayCertPath) {
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);
            // 商户订单号
            String outTradeNo = params.get("out_trade_no");
            // 支付宝交易流水号
            String tradeNo = params.get("trade_no");
            boolean verifyResult = AlipaySignature.rsaCertCheckV1(params, aliPayCertPath, "UTF-8", "RSA2");
            if (verifyResult) {
                log.warn("[支付宝][支付回调结果]>>>>>>>>>回调数据：{}", params);
                // 交易状态
                String tradeStatus = params.get("trade_status");
                // 实际支付金额
                String totalAmount = params.get("total_amount");
                // 支付时间
                String gmtPayment = params.get("gmt_payment");
                // 业务逻辑：更新订单状态（需保证幂等性，避免重复处理）
                if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                    // 查询订单
                    PayOrder order = ORDER_SERVICE.detailByOutTradeNo(outTradeNo);
                    if (order == null) {
                        log.error("[支付宝][支付回调结果]>>>>>>>>>订单号：{}，未查询到订单记录",outTradeNo);
                        return "success";
                    }
                    // 检查订单状态,已处理过，直接返回成功
                    if (order.getStatus() != 0) {
                        return "success";
                    }
                    // 支付成功-修改订单状态
                    ORDER_SERVICE.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);
                    // 处理业务逻辑
                    try{
                        handleSuccessBusiness(tradeStatus, outTradeNo, tradeNo, totalAmount, gmtPayment, order.getBusinessId(), order.getUserId());
                    }
                    catch (Exception e){
                        // 业务处理异常，手动回滚数据
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return "failure";
                    }
                }
                else {
                    ORDER_SERVICE.failPay(outTradeNo, tradeNo, totalAmount);
                    log.error("[支付宝][支付回调结果]>>>>>>>>>订单号：{},流水号：{},交易状态：{}",outTradeNo,tradeNo,tradeStatus);
                    try{
                        handleFailedBusiness(tradeStatus, outTradeNo, tradeNo, totalAmount, gmtPayment);
                    }
                    catch (Exception e){
                        // 业务处理异常，手动回滚数据
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return "failure";
                    }
                }
                return "success";
            }
            else {
                log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{},流水号：{}",outTradeNo,tradeNo);
                return "failure";
            }
        }
        catch (Exception e) {
            log.error("[支付宝][支付回调验证]>>>>>>>>>支付结果回调异常:",e);
            return "failure";
        }
    }

    /**
     * 处理支付成功的业务逻辑
     *
     * @param tradeStatus 交易状态
     * @param outTradeNo  商户订单号
     * @param tradeNo     支付宝交易流水号
     * @param totalAmount 实际支付金额
     * @param gmtPayment  支付时间
     * @param businessId  业务id
     * @param userId      用户id
     */
    abstract public void handleSuccessBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Long businessId, Long userId);

    /**
     * 处理支付失败的业务逻辑
     *
     * @param tradeStatus 交易状态
     * @param outTradeNo  商户订单号
     * @param tradeNo     支付宝交易流水号
     * @param totalAmount 实际支付金额
     * @param gmtPayment  支付时间
     */
    abstract public void handleFailedBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment);

    /**
     * 消费支付模块超时回调数据
     * @param vo 支付订单超时参数
     */
    abstract public void dealPayTimeoutData(PayTimeoutStatusVo vo);
}
