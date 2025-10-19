package com.sutran.sd.pay.constants;

/**
 * @author zj
 * @date 2025年08月23日 23:06
 */
public interface PayNotifyServer {
    /** 会员订单支付回调 **/
    String SD_MEMBER_NOTIFY = "sdMemberNotify";
    /** 众筹订单支付回调 **/
    String PROOF_CROWDFUND_NOTIFY = "proofCrowdfundNotify";
    /** 众筹预售支付回调 **/
    String PROOF_PRESEAL_NOTIFY = "proofPresealNotify";
    /** 预售订单支付回调 **/
    String PRESALE_ORDER_NOTIFY = "presaleOrderNotify";
    // 后续有其他回调业务逻辑，再继续加

}
