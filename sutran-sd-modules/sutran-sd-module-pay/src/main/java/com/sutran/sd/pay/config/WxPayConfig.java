package com.sutran.sd.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zj
 * @date 2025年07月28日 10:44
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.pay.v3")
public class WxPayConfig {
    private String appId;
    private String keyPath;
    private String publicKeyPath;
    private String certPath;
    private String certP12Path;
    private String platformCertPath;
    private String mchId;
    private String apiKey;
    private String apiKey3;
    private String domain;
    private String publicKeyId;
}

