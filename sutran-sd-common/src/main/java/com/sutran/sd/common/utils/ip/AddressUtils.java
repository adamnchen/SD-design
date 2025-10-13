package com.sutran.sd.common.utils.ip;

import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HtmlUtil;
import com.sutran.sd.common.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取地址类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        if (StringUtils.isBlank(ip)) {
            return UNKNOWN;
        }
        // 内网不查询
        ip = StringUtils.contains(ip, "0:0:0:0:0:0:0:1") ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
        
        // 处理IPv6格式的IPv4地址 (如 ::ffff:192.168.31.172)
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7); // 移除 ::ffff: 前缀
        }
        
        if (NetUtil.isInnerIP(ip)) {
            return "内网IP";
        }
        return RegionUtils.getCityInfo(ip);
    }
}
