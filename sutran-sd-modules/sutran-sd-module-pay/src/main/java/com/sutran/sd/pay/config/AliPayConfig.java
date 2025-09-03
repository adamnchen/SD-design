package com.sutran.sd.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025年08月18日 21:46
 */
@Data
@Component
@ConfigurationProperties(prefix = "ali.pay")
public class AliPayConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用编号
     */
    private String appId;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥，
     * 通过应用公钥上传到支付宝开放平台换取支付宝公钥
     * (如果是证书模式，公钥与私钥在CSR目录)。
     */
    private String publicKey;

    /**
     * 应用公钥证书 (证书模式必须)
     */
    private String appCertPath;

    /**
     * 支付宝公钥证书 (证书模式必须)
     */
    private String aliPayCertPath;

    /**
     * 支付宝根证书 (证书模式必须)
     */
    private String aliPayRootCertPath;

    /**
     * 支付宝支付网关
     */
    private String serverUrl;

    /**
     * 外网访问项目的域名，支付通知中会使用
     */
    private String domain;

    /**
     * 支付描述信息
     */
    private String body;
}
