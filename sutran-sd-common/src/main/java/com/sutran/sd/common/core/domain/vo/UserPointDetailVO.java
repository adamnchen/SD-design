package com.sutran.sd.common.core.domain.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户积分详情VO
 * 
 * @author sutran
 * @date 2025-10-14
 */
@Data
public class UserPointDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 打样积分
     */
    private Integer proofingPoint;

    /**
     * 打样荣誉等级
     */
    private Integer priifingLevel;

    /**
     * 打样荣誉等级名称
     */
    private String priifingLevelName;

    /**
     * 销售积分
     */
    private String salePoint;

    /**
     * 销售荣誉等级
     */
    private String saleLevel;

    /**
     * 销售荣誉等级名称
     */
    private String saleLevelName;

    /**
     * 总积分（打样积分 + 销售积分）
     */
    private Integer totalPoint;

    /**
     * 综合荣誉等级
     */
    private String comprehensiveLevel;

    /**
     * 创建时间
     */
    private java.util.Date createTime;

    /**
     * 更新时间
     */
    private java.util.Date updateTime;
}
