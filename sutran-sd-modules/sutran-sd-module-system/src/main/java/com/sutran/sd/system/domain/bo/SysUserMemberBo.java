package com.sutran.sd.system.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 授权用户会员实体类
 * @author zj
 * @date 2025年08月26日 10:25
 */
@Data
@Accessors(chain = true)
public class SysUserMemberBo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 会员ID
     */
    private Long memberId;
    /**
     * 用户ID集合
     */
    private List<Long> userIds;
    /**
     * 会员时长(天)[不填则已会员设置的时长为准]
     */
    private Integer duration;
    /**
     * 会员训练次数[不填则已会员设置的训练次数为准]
     */
    private Integer limitTrainTimes;
    /**
     * 会员绘图次数[不填则已会员设置的绘图次数为准]
     */
    private Integer limitDrawNum;
}
