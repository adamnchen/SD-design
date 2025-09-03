package com.sutran.sd.pay.controller;

import com.ijpay.alipay.AliPayApiConfig;

/**
 * @author zj
 * @date 2025年08月19日 17:49
 */
public abstract class BaseAliPayApiController{
    public abstract AliPayApiConfig getApiConfig();
}
