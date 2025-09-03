package com.sutran.sd.pay.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ijpay.alipay.AliPayApi;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.enums.BusinessType;
import com.sutran.sd.pay.enums.ChannelType;
import com.sutran.sd.pay.mapper.PayOrderMapper;
import com.sutran.sd.pay.service.PayMemberService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zj
 * @date 2025年08月23日 23:00
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PayOrderServiceImpl implements PayOrderService {

    private final PayOrderMapper baseMapper;
    private final PayMemberService payMemberService;
    private final UserService userService;

    @Override
    public TableDataInfo<PayOrder> selectPageOrderList(PayOrder order, PageQuery pageQuery) {
        Map<String, Object> params = order.getParams();
        LambdaQueryWrapper<PayOrder> lqw = new LambdaQueryWrapper<PayOrder>()
            .eq(StringUtils.isNotBlank(order.getOutTradeNo()), PayOrder::getOutTradeNo, order.getOutTradeNo())
            .eq(StringUtils.isNotBlank(order.getTradeNo()), PayOrder::getTradeNo, order.getTradeNo())
            .like(StringUtils.isNotBlank(order.getSubject()), PayOrder::getSubject, order.getSubject())
            .eq(StringUtils.isNotBlank(order.getBusinessType()), PayOrder::getBusinessType, order.getBusinessType())
            .eq(StringUtils.isNotBlank(order.getChannelType()), PayOrder::getChannelType, order.getChannelType())
            .eq(ObjectUtil.isNotNull(order.getBusinessId()), PayOrder::getBusinessId, order.getBusinessId())
            .eq(ObjectUtil.isNotNull(order.getStatus()), PayOrder::getStatus, order.getStatus())
            .between(params.get("beginTime") != null && params.get("endTime") != null, PayOrder::getCreateTime, params.get("beginTime"), params.get("endTime"));
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            pageQuery.setOrderByColumn("id");
            pageQuery.setIsAsc("desc");
        }
        Page<PayOrder> page = baseMapper.selectPage(pageQuery.build(), lqw);
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            QrConfig  qrConfig = new QrConfig();
            qrConfig.setWidth(300);
            qrConfig.setHeight(300);
            for (PayOrder record : page.getRecords()) {
                if (record.getStatus()!=null && record.getStatus()==0) {
                    // 使用hutool生成二维码图片返回
                    String qrCode = QrCodeUtil.generateAsBase64(record.getQrCode(), qrConfig, "png");
                    record.setQrCode(qrCode);
                }
                else {
                    record.setQrCode(null);
                }
            }
        }
        return TableDataInfo.build(page);
    }

    @Override
    public PayOrder detailById(String id) {
        PayOrder record = baseMapper.selectById(id);
        if (record.getStatus()!=null && record.getStatus()==0) {
            QrConfig  qrConfig = new QrConfig();
            qrConfig.setWidth(300);
            qrConfig.setHeight(300);
            // 使用hutool生成二维码图片返回
            String qrCode = QrCodeUtil.generateAsBase64(record.getQrCode(), qrConfig, "png");
            record.setQrCode(qrCode);
        }
        return record;
    }

    @Override
    public PayOrder detailByIdAndUserId(String id, Long userId) {
        PayOrder record = baseMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getId, id).eq(PayOrder::getUserId, userId));
        if (record==null) {
            return null;
        }
        if (record.getStatus()!=null && record.getStatus()==0) {
            QrConfig qrConfig = new QrConfig();
            qrConfig.setWidth(300);
            qrConfig.setHeight(300);
            // 使用hutool生成二维码图片返回
            String qrCode = QrCodeUtil.generateAsBase64(record.getQrCode(), qrConfig, "png");
            record.setQrCode(qrCode);
        }
        return record;
    }

    @Override
    public PayOrder detailByOutTradeNo(String outTradeNo) {
        PayOrder record = baseMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOutTradeNo, outTradeNo));
        if (record.getStatus()!=null && record.getStatus()==0) {
            QrConfig  qrConfig = new QrConfig();
            qrConfig.setWidth(300);
            qrConfig.setHeight(300);
            // 使用hutool生成二维码图片返回
            String qrCode = QrCodeUtil.generateAsBase64(record.getQrCode(), qrConfig, "png");
            record.setQrCode(qrCode);
        }
        return record;
    }

    @Override
    public PayOrder isExistNoDealOrder(Long userId, String appId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
            .eq(PayOrder::getAppId, appId)
            .eq(PayOrder::getUserId, userId)
            .eq(PayOrder::getBusinessType, BusinessType.SD_MEMBER.name())
            .eq(PayOrder::getChannelType, ChannelType.ALI_PAY.name())
            .eq(PayOrder::getStatus, 0)
            .lt(PayOrder::getExpireTime, new Date())
            .orderByDesc(PayOrder::getId));
    }

    @Override
    public void insert(PayOrder order) {
        baseMapper.insert(order);
    }

    @Override
    public boolean successPay(String outTradeNo, String tradeNo, String totalAmount, String gmtPayment) {
        return baseMapper.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);
    }

    @Override
    public void failPay(String outTradeNo, String tradeNo, String totalAmount) {
        baseMapper.failPay(outTradeNo, tradeNo, totalAmount);
    }

    @Override
    public void saveQrCode(String outTradeNo, String qrCode) {
        baseMapper.saveQrCode(outTradeNo, qrCode);
    }

    @Override
    public void handlePayTimeoutOfData(Date now) {
        baseMapper.handlePayTimeoutOfData(now);
    }

    @Override
    public void handleNoPayOfData(Date now) {
        List<PayOrder> list = baseMapper.selectList(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getStatus, 0).gt(PayOrder::getExpireTime, now));
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        for (PayOrder record : list) {
            model.setOutTradeNo(record.getOutTradeNo());
            try {
                AlipayTradeQueryResponse response = AliPayApi.tradeQueryToResponse(model);
                if (response.isSuccess()) {
                    final String outTradeNo = response.getOutTradeNo();
                    final String tradeNo = response.getTradeNo();
                    final String tradeStatus = response.getTradeStatus();

                    // 业务逻辑：更新订单状态（需保证幂等性，避免重复处理）
                    if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                        // 修改订单状态
                        boolean updateSuccess = successPay(outTradeNo, tradeNo, response.getTotalAmount(), DateUtil.formatDateTime(response.getSendPayDate()));
                        if (updateSuccess) {
                            // 通知支付宝处理成功，不再重复通知，并处理用户会员逻辑
                            if (Objects.equals(record.getBusinessType(), BusinessType.SD_MEMBER.name())) {
                                PayMember payMember = payMemberService.detailById(record.getBusinessId().toString());
                                // 处理用户会员逻辑
                                userService.insertMember(record.getUserId(),record.getBusinessId(),new Date(),payMember, outTradeNo);
                            }
                        }
                    }
                    else {
                        failPay(outTradeNo, tradeNo, response.getTotalAmount());
                        log.error("[支付宝][定时处理未失效且未支付订单]>>>>>>>>>支付宝查询指定交易信息并修改订单数据失败,订单号：{},流水号：{},交易状态：{}",outTradeNo,tradeNo, tradeStatus);
                    }
                }
            }
            catch (AlipayApiException e) {
                log.error("[支付宝][定时处理未失效且未支付订单]>>>>>>>>>查询支付宝指定交易信息失败,订单号：{},异常：",record.getOutTradeNo(),e);
            }
        }
    }
}
