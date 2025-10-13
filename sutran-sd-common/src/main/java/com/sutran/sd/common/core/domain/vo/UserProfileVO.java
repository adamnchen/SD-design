package com.sutran.sd.common.core.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 客户端用户个人信息VO
 * 只包含客户端用户可以查看的字段
 *
 * @author SutranSD
 */
@Data
public class UserProfileVO implements Serializable {

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
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 个人简介
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 限制有效期
     */
    private Date limitValidDate;

    /**
     * 限制模型训练次数
     */
    private Integer limitTrainTimes;

    /**
     * 限制绘图张数
     */
    private Integer limitDrawNum;

    /**
     * 是否关闭引导[0-否,1-是]
     */
    private Integer isCloseGuide;
}
