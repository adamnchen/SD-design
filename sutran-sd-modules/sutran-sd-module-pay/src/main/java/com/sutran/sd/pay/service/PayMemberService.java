package com.sutran.sd.pay.service;

import com.sutran.sd.common.core.domain.entity.PayMember;

import java.util.List;

/**
 * @author zj
 * @date 2025年08月21日 10:59
 */
public interface PayMemberService {
    /**
     * 获取会员配置列表
     * @param config    会员配置
     * @return          会员配置列表
     */
    List<PayMember> selectMemberList(PayMember config);

    /**
     * 获取会员配置详情
     * @param id 会员配置ID
     * @return 会员配置详情
     */
    PayMember detailById(String id);

    /**
     * 新增会员配置
     * @param config 会员配置
     * @return 新增结果
     */
    boolean insert(PayMember config);

    /**
     * 更新会员配置
     * @param member 会员配置
     * @return 更新结果
     */
    boolean updateById(PayMember member);

    /**
     * 删除会员配置
     * @param id 会员配置ID
     * @return 删除结果
     */
    boolean removeById(String id);

    /**
     * 获取会员配置时长
     * @param id 会员配置ID
     * @return 会员配置时长
     */
    int selectDurationById(Long id);

    /**
     * 判断会员配置下是否有用户
     * @param id 会员配置ID
     * @return 是否有用户
     */
    boolean hasUser(String id);
}
