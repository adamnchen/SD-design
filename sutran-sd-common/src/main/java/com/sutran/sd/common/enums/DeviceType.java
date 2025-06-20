package com.sutran.sd.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型
 * 针对一套 用户体系
 *
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum DeviceType {

    /**
     * pc端
     */
    PC("pc"),

    /**
     * 业务端-SD绘图
     */
    BS_PC("bs_pc"),

    /**
     * app端
     */
    BS_APP("bs_app"),

    /**
     * 小程序端
     */
    BS_XCX("bs_xcx"),

    /**
     * 小程序端
     */
    WX_MP("wx_mp");

    private final String device;
}
