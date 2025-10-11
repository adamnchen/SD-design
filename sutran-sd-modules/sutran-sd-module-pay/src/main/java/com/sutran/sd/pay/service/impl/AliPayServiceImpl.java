package com.sutran.sd.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.OrderNumUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.pay.config.AliPayConfig;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.enums.BusinessType;
import com.sutran.sd.pay.enums.ChannelType;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayMemberService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.sutran.sd.common.constant.CacheConstants.PAY_ORDER_QR;
import static com.sutran.sd.common.constant.CacheConstants.PAY_ORDER_TASK;

/**
 * @author zj
 * @date 2025年08月18日 22:01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliPayServiceImpl implements AliPayService {

    private final AliPayConfig aliPayConfig;
    private final PayOrderService payOrderService;
    private final UserService  userService;
    private final PayMemberService payMemberService;

    @Override
    public AliPayApiConfig getConfig() {
        AliPayApiConfig aliPayApiConfig;
        try {
            aliPayApiConfig = AliPayApiConfigKit.getApiConfig(aliPayConfig.getAppId());
        }
        catch (Exception e) {
            try {
                aliPayApiConfig = AliPayApiConfig.builder()
                    .setAppId(aliPayConfig.getAppId())
                    .setAliPayPublicKey(aliPayConfig.getPublicKey())
                    .setPrivateKey(aliPayConfig.getPrivateKey())
                    .setAppCertPath(aliPayConfig.getAppCertPath())
                    .setAliPayCertPath(aliPayConfig.getAliPayCertPath())
                    .setAliPayRootCertPath(aliPayConfig.getAliPayRootCertPath())
                    .setCharset("UTF-8")
                    .setServiceUrl(aliPayConfig.getServerUrl())
                    .setSignType("RSA2")
                    // 证书模式
                    .buildByCert();
            } catch (AlipayApiException ex) {
                throw new RuntimeException(ex);
            }
            AliPayApiConfigKit.setThreadLocalAliPayApiConfig(aliPayApiConfig);
        }
        return aliPayApiConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String preCreateMemberOrder(String  memberId) {
        PayMember payMember = payMemberService.detailById(memberId);
        if (payMember == null) {
            throw new ServiceException("会员不存在或已被刪除!");
        }
        final String subject = payMember.getLevelName();
        final BigDecimal totalAmount = payMember.getPrice();
        final String body = payMember.getDescription();
        final Date now = new Date();

//        final Long userId = 1L;
        final Long userId = LoginHelper.getUserId();
//        final String username = "测试用户";
        final String username = LoginHelper.getUsername();
        final String appId = aliPayConfig.getAppId();

        // 获取当前用户在当前支付应用下是否存在未超时且未完成的支付
        PayOrder payOrder = payOrderService.isExistNoDealOrder(userId,appId);
        if (payOrder != null) {
            throw new ServiceException("当前用户在当前支付应用下存在未完成的订单",500,payOrder.getId().toString());
        }

        // 获取当前用户已购买且处于生效中的会员ID
        String currentMemberId = userService.selectMemberIdByUserId(userId,now);
        if (StringUtils.isNotBlank(currentMemberId)) {
            PayMember currentPayMember = payMemberService.detailById(currentMemberId);
            if (currentPayMember != null && currentPayMember.getLevel() > payMember.getLevel()) {
                throw new ServiceException(String.format("会员[%s]未到期，不可降级购买会员!",currentPayMember.getLevelName()));
            }
        }

        // 订单过期时间，默认35分钟后过期(稍微大于支付宝默认超时时间30分钟)
        Date expireTime = DateUtil.offsetMinute(now, 35);
        String outTradeNo = OrderNumUtils.getOrderNum(now);;

        // 存入redis,扫描redis进行过期订单处理
        RedisUtils.setCacheZSet(PAY_ORDER_TASK,expireTime.getTime(),outTradeNo);

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setSubject(subject);
        model.setBody(body);
        // 订单总金额，单位为元，精确到小数点后两位
        model.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP).toString());
        // 商户网站 唯一 订单号0
        model.setOutTradeNo(outTradeNo);
        // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：5m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m
        model.setTimeoutExpress("30m");
        try {
            // 新增订单记录
            PayOrder order = new PayOrder()
                .setOutTradeNo(outTradeNo)
                .setUserId(userId)
                .setUserName(username)
                .setAppId(appId)
                .setSubject(subject)
                .setBody(body)
                .setTotalAmount(totalAmount)
                .setStatus(0)
                .setChannelType(ChannelType.ALI_PAY.name())
                .setBusinessType(BusinessType.SD_MEMBER.name())
                .setBusinessId(payMember.getId())
                .setCreateTime(now)
                .setExpireTime(expireTime);
            payOrderService.insert(order);

            // 支付宝调用回调接口(系统上的接口，用于接收支付宝支付结果通知，要设置不需要登录认证)
            String notifyUrl = aliPayConfig.getDomain() + "/pay/ali/notify_url";
            AlipayTradePrecreateResponse response = AliPayApi.tradePrecreatePayToResponse(model, notifyUrl);
            if (!response.isSuccess()) {
                throw new ServiceException(response.getSubMsg());
            }
            String qrCode = response.getQrCode();
            // 保存二维码到订单记录
            payOrderService.saveQrCode(outTradeNo,qrCode);
            return qrCode;
        }
        catch (Exception e) {
            log.error("[支付宝][扫码支付]>>>>>>>>>创建订单失败,订单号:{}，异常：", outTradeNo, e);
            throw new ServiceException("创建订单失败："+e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String notifyUrl(HttpServletRequest request) {
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);
            // 商户订单号
            String outTradeNo = params.get("out_trade_no");
            // 支付宝交易流水号
            String tradeNo = params.get("trade_no");
            boolean verifyResult = AlipaySignature.rsaCertCheckV1(params, aliPayConfig.getAliPayCertPath(), "UTF-8", "RSA2");
            if (verifyResult) {
                // 交易状态
                String tradeStatus = params.get("trade_status");
                // 实际支付金额
                String totalAmount = params.get("total_amount");
                // 支付时间
                String gmtPayment = params.get("gmt_payment");

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
                        // 通知支付宝处理成功，不再重复通知，并处理用户会员逻辑
                        if (Objects.equals(order.getBusinessType(), BusinessType.SD_MEMBER.name())) {
                            PayMember payMember = payMemberService.detailById(order.getBusinessId().toString());
                            // 处理用户会员逻辑
                            userService.insertMember(order.getUserId(),order.getBusinessId(),new Date(),payMember,outTradeNo);
                        }
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
            else {
                log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{},流水号：{}",outTradeNo,tradeNo);
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
    @Transactional(rollbackFor = Exception.class)
    public void syncStatus(String outTradeNo, String tradeNo) {
        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            if (StringUtils.isNotBlank(outTradeNo)) {
                model.setOutTradeNo(outTradeNo);
            }
            if (StringUtils.isNotBlank(tradeNo)) {
                model.setTradeNo(tradeNo);
            }
            AlipayTradeQueryResponse response = AliPayApi.tradeQueryToResponse(model);
            if (response.isSuccess()) {
                // 根据outTradeNo查询订单
                PayOrder order = payOrderService.detailByOutTradeNo(outTradeNo);
                if (order == null) {
                    return;
                }
                // 检查订单状态,已处理过，直接返回成功
                if (order.getStatus() != 0) {
                    return;
                }
                // 业务逻辑：更新订单状态（需保证幂等性，避免重复处理）
                if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(response.getTradeStatus()) || AliPayTradeStatus.TRADE_FINISHED.name().equals(response.getTradeStatus())) {
                    // 修改订单状态
                    boolean updateSuccess = payOrderService.successPay(outTradeNo, tradeNo, response.getTotalAmount(), DateUtil.formatDateTime(response.getSendPayDate()));
                    if (updateSuccess) {
                        // 通知支付宝处理成功，不再重复通知，并处理用户会员逻辑
                        if (Objects.equals(order.getBusinessType(), BusinessType.SD_MEMBER.name())) {
                            PayMember payMember = payMemberService.detailById(order.getBusinessId().toString());
                            // 处理用户会员逻辑
                            userService.insertMember(order.getUserId(),order.getBusinessId(),new Date(),payMember,outTradeNo);
                        }
                    }
                }
                else {
                    payOrderService.failPay(outTradeNo, tradeNo, response.getTotalAmount());
                    log.error("[支付宝][查询指定交易信息]>>>>>>>>>支付宝查询指定交易信息并修改订单数据失败,订单号：{},流水号：{},交易状态：{}",outTradeNo,tradeNo,response.getTradeStatus());
                }
            }
        }
        catch (AlipayApiException e) {
            log.error("[支付宝][查询指定交易信息]>>>>>>>>>查询支付宝指定交易信息失败,订单号：{},流水号：{},异常：",outTradeNo,tradeNo,e);
            throw new ServiceException("查询支付宝指定交易信息失败:"+e.getMessage());
        }
    }

    /**
     * 创建订单记录->调用支付宝创建订单->保存二维码到数据库和redis中
     * @param userId 下单人用户ID
     * @param userName 下单人姓名
     * @param outTradeNo 订单号
     * @param subject 商品名称
     * @param body 商品参数或者描述信息(可以用json字符串表示)
     * @param totalAmount 订单总金额
     * @param notifyUrl 支付结果回调接口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPayOrder(Long userId, String userName, String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl) {
        // 当前上下文添加支付宝参数
        getConfig();
        // 支付宝应用ID
        final String appId = aliPayConfig.getAppId();

        // 订单过期时间，10分钟后过期(稍微大于支付认超时时间)
        Date now = new Date();
        Date expireTime = DateUtil.offsetMinute(now, 6);

        // 存入redis,扫描redis进行过期订单处理
        RedisUtils.setCacheZSet(PAY_ORDER_TASK,expireTime.getTime(),outTradeNo);

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setSubject(subject);
        model.setBody(body);
        // 订单总金额，单位为元，精确到小数点后两位
        model.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP).toString());
        // 商户网站 唯一 订单号0
        model.setOutTradeNo(outTradeNo);
        // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：5m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m
        model.setTimeoutExpress("5m");
        // 新增订单记录
        PayOrder order = new PayOrder()
            .setOutTradeNo(outTradeNo)
            .setUserId(userId).setUserName(userName).setAppId(appId)
            .setSubject(subject).setBody(body).setTotalAmount(totalAmount)
            .setChannelType(ChannelType.ALI_PAY.name()).setBusinessType(BusinessType.PROOF_CROWDFUND.name())
            .setStatus(0).setCreateTime(now).setExpireTime(expireTime);
        payOrderService.insert(order);
        try {
            // 支付宝调用回调接口(系统上的接口，用于接收支付宝支付结果通知，要设置不需要登录认证)
            AlipayTradePrecreateResponse response = AliPayApi.tradePrecreatePayToResponse(model, aliPayConfig.getDomain()+notifyUrl);
            if (!response.isSuccess()) {
                throw new ServiceException(response.getSubMsg());
            }
            String qrCode = response.getQrCode();
            // 保存二维码到订单记录
            payOrderService.saveQrCode(outTradeNo,qrCode);
            // redis中存储二维码地址，并设置有效时间6分钟
            RedisUtils.setCacheObject(PAY_ORDER_QR+outTradeNo,qrCode, Duration.ofMinutes(6));
        }
        catch (Exception e) {
            log.error("[支付宝][扫码支付]>>>>>>>>>创建订单失败,订单号:{}，异常：", outTradeNo, e);
            payOrderService.failPay(outTradeNo,null, model.getTotalAmount());
        }
    }


    /**
     * 更新订单状态
     * @param outTradeNo 订单号
     * @param tradeNo 支付宝交易流水号
     * @param totalAmount 总金额
     * @param gmtPayment 支付时间
     * @param payStatus 支付状态[0-待支付,1-支付成功,2-支付失败]
     * @return 是否修改成功
     */
    @Override
    public boolean updateOrderStatus(String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Integer payStatus) {
        if (payStatus == null) {
            return false;
        }
        else if (payStatus == 1) {
            return payOrderService.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);
        }
        else {
            return payOrderService.failPay(outTradeNo,tradeNo,totalAmount);
        }
    }

}
