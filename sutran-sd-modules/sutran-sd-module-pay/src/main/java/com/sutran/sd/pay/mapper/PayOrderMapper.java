package com.sutran.sd.pay.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.pay.domain.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * @author zj
 * @date 2025年08月23日 23:00
 */
@Mapper
public interface PayOrderMapper extends BaseMapperPlus<PayOrderMapper, PayOrder, PayOrder> {

    /**
     * 更新订单状态
     * @param outTradeNo 订单号
     * @param tradeNo 支付订单号
     * @param totalAmount 总金额
     * @param gmtPayment 支付时间
     * @return 是否更新成功
     */
    @Update("update pay_order " +
        "set trade_no = #{tradeNo}, total_amount = #{totalAmount}, status = 1, gmt_payment = #{gmtPayment} " +
        "where out_trade_no = #{outTradeNo}")
    boolean successPay(@Param("outTradeNo") String outTradeNo,
                       @Param("tradeNo") String tradeNo,
                       @Param("totalAmount") String totalAmount,
                       @Param("gmtPayment") String gmtPayment);

    /**
     * 失败更新订单状态
     * @param outTradeNo 订单号
     * @param tradeNo 支付订单号
     * @param totalAmount 总金额
     * @return 是否更新成功
     */
    @Update("update pay_order " +
        "set trade_no = #{tradeNo}, total_amount = #{totalAmount}, status = 2 " +
        "where out_trade_no = #{outTradeNo}")
    boolean failPay(@Param("outTradeNo") String outTradeNo,
                    @Param("tradeNo") String tradeNo,
                    @Param("totalAmount") String totalAmount);

    /**
     * 保存二维码
     * @param outTradeNo 订单号
     * @param qrCode 二维码
     */
    @Update("update pay_order set qr_code = #{qrCode} where out_trade_no = #{outTradeNo}")
    void saveQrCode(@Param("outTradeNo") String outTradeNo, @Param("qrCode") String qrCode);

    /**
     * 处理支付超时数据
     * @param now 现在时间
     */
    @Update("update pay_order set status = 3 where expire_time < #{now} and status = 0")
    void handlePayTimeoutOfData(@Param("now") Date now);
}
