package com.sutran.sd.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;

import java.util.Date;

import static cn.hutool.core.date.DatePattern.PURE_DATETIME_PATTERN;

/**
 * @author zj
 * @date 2025年10月10日 22:55
 */
public class OrderNumUtils {

    /**
     * 生成全局唯一订单号(纯数值：日期+雪花算法ID)
     * @param now 日期
     * @return 14+19 的33位纯数字的订单号
     */
    public static String getOrderNum(Date now){
        return DateUtil.format(now,PURE_DATETIME_PATTERN) + IdUtil.getSnowflakeNextIdStr();
    }

}
