package com.sutran.sd.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 打样邀约相关常量
 *
 * @author SutranSD
 */
public class ProofingInvitationConstants {

    /**
     * 邀约状态常量
     */
    public static final Integer STATUS_PENDING = 0;      // 待处理
    public static final Integer STATUS_ACCEPTED = 1;    // 已接受
    public static final Integer STATUS_REJECTED = 2;    // 已拒绝
    public static final Integer STATUS_CANCELLED = 3;    // 已取消
    public static final Integer STATUS_REPLYING  = 4;    //待回应

    /**
     * 自动取消时限常量
     */
    public static final Integer CANCEL_TIME_ONE_DAY = 1;    // 一天
    public static final Integer CANCEL_TIME_TWO_DAYS = 2;    // 两天
    public static final Integer CANCEL_TIME_THREE_DAYS = 3;  // 三天

    /**
     * 取消时限描述映射
     */
    public static final Map<Integer, String> CANCEL_TIME_DESCRIPTIONS = new HashMap<Integer, String>() {{
        put(CANCEL_TIME_ONE_DAY, "1天内无人应答自动取消");
        put(CANCEL_TIME_TWO_DAYS, "2天内无人应答自动取消");
        put(CANCEL_TIME_THREE_DAYS, "3天内无人应答自动取消");
    }};

    /**
     * 取消时限天数映射
     */
    public static final Map<Integer, Integer> CANCEL_TIME_DAYS = new HashMap<Integer, Integer>() {{
        put(CANCEL_TIME_ONE_DAY, 1);
        put(CANCEL_TIME_TWO_DAYS, 2);
        put(CANCEL_TIME_THREE_DAYS, 3);
    }};

    /**
     * 获取取消时限描述
     * @param cancelTimeLimit 取消时限值 (1,2,3)
     * @return 描述文本
     */
    public static String getCancelTimeDescription(Integer cancelTimeLimit) {
        return CANCEL_TIME_DESCRIPTIONS.getOrDefault(cancelTimeLimit, "未知时限");
    }

    /**
     * 获取取消时限天数
     * @param cancelTimeLimit 取消时限值 (1,2,3)
     * @return 天数
     */
    public static Integer getCancelTimeDays(Integer cancelTimeLimit) {
        return CANCEL_TIME_DAYS.getOrDefault(cancelTimeLimit, 1);
    }

    /**
     * 验证取消时限值是否有效
     * @param cancelTimeLimit 取消时限值
     * @return 是否有效
     */
    public static boolean isValidCancelTimeLimit(Integer cancelTimeLimit) {
        return cancelTimeLimit != null &&
               (cancelTimeLimit == CANCEL_TIME_ONE_DAY ||
                cancelTimeLimit == CANCEL_TIME_TWO_DAYS ||
                cancelTimeLimit == CANCEL_TIME_THREE_DAYS);
    }

    /**
     * 获取状态描述
     * @param status 状态值
     * @return 状态描述
     */
    public static String getStatusDescription(Integer status) {
        switch (status) {
            case 0: return "待处理";
            case 1: return "已接受";
            case 2: return "已拒绝";
            case 3: return "已取消";
            default: return "未知状态";
        }
    }
}
