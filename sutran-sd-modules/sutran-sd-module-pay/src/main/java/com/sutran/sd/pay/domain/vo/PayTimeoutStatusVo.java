package com.sutran.sd.pay.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 支付超时状态实体类
 * @author zj
 * @date 2025年10月18日 13:48
 */
@Data
@Accessors(chain=true)
public class PayTimeoutStatusVo {
    /**
     * 关联业务ID
     */
    private Long businessId;
    /**
     * 交易状态
     */
    private String tradeStatus;
    /**
     * 订单编号
     */
    private String outTradeNo;
    /**
     * 当前订单用户ID
     */
    private Long userId;
    /**
     * 项目编号
     */
    private Long projectid;
    /**
     * 打样众筹捐助金额
     */
    private BigDecimal amount;

}
