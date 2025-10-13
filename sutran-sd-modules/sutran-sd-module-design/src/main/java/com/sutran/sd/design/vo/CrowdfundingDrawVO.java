package com.sutran.sd.design.vo;

import lombok.Data;

import java.util.Date;

/**
 * 众筹抽奖VO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingDrawVO {

    /**
     * 支持记录ID
     */
    private Long id;

    /**
     * 众筹项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 参与者用户ID
     */
    private Long userId;

    /**
     * 参与者姓名
     */
    private String userName;

    /**
     * 抽奖状态：0=未参与，1=已参与，2=中奖，3=未中奖
     */
    private Integer drawStatus;

    /**
     * 抽奖状态描述
     */
    private String drawStatusDesc;

    /**
     * 是否中奖：0=否，1=是
     */
    private Boolean isWinner;

    /**
     * 奖品信息（JSON格式）
     */
    private String prizeInfo;

    /**
     * 创建时间
     */
    private Date createTime;
}
