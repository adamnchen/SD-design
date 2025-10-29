package com.sutran.sd.framework.utils;

import com.sutran.sd.common.core.domain.entity.SysUserMember;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.mapper.SysUserMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 会员权限工具类
 * 提供编程式的会员权限校验方法
 *
 * @author SutranSD
 */
@Component
@RequiredArgsConstructor
public class MemberPermissionUtils {

    private final SysUserMemberMapper userMemberMapper;

    /**
     * 检查当前用户是否有有效会员
     *
     * @return true-有有效会员，false-无有效会员
     */
    public boolean hasValidMember() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return false;
        }
        return getCurrentValidMember(userId) != null;
    }

    /**
     * 获取当前用户的有效会员信息
     *
     * @return 有效会员信息，如果无有效会员则返回null
     */
    public SysUserMember getCurrentValidMember() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return null;
        }
        return getCurrentValidMember(userId);
    }

    /**
     * 获取指定用户的有效会员信息
     *
     * @param userId 用户ID
     * @return 有效会员信息，如果无有效会员则返回null
     */
    public SysUserMember getCurrentValidMember(Long userId) {
        try {
            List<Long> userIds = Collections.singletonList(userId);
            List<SysUserMember> members = userMemberMapper.selectMemberInfoByUserIds(userIds, new Date());

            if (members != null && !members.isEmpty()) {
                SysUserMember member = members.get(0);
                // 检查会员是否过期
                if (member.getEndTime() != null && member.getEndTime().before(new Date())) {
                    return null;
                }
                // 检查会员状态
                if (member.getStatus() == null || member.getStatus() != 1) {
                    return null;
                }
                return member;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验当前用户会员权限，无权限时抛出异常
     *
     * @param businessType 业务类型描述
     * @throws ServiceException 无会员权限时抛出异常
     */
    public void checkMemberPermission(String businessType) {
        if (!hasValidMember()) {
            throw new ServiceException(businessType + "需要会员权限，请先购买会员");
        }
    }

    /**
     * 校验当前用户会员权限，无权限时抛出异常
     *
     * @param businessType 业务类型描述
     * @param customMessage 自定义错误信息
     * @throws ServiceException 无会员权限时抛出异常
     */
    public void checkMemberPermission(String businessType, String customMessage) {
        if (!hasValidMember()) {
            String errorMessage = StringUtils.isNotBlank(customMessage)
                ? customMessage
                : businessType + "需要会员权限，请先购买会员";
            throw new ServiceException(errorMessage);
        }
    }

    /**
     * 获取当前用户剩余的训练次数
     *
     * @return 剩余训练次数，无会员时返回0
     */
    public Integer getRemainingTrainTimes() {
        SysUserMember member = getCurrentValidMember();
        if (member == null) {
            return 0;
        }
        return member.getLimitTrainTimes() - member.getUseTrainTimes();
    }

    /**
     * 获取当前用户剩余的绘图次数
     *
     * @return 剩余绘图次数，无会员时返回0
     */
    public Integer getRemainingDrawNum() {
        SysUserMember member = getCurrentValidMember();
        if (member == null) {
            return 0;
        }
        return member.getLimitDrawNum() - member.getUseDrawNum();
    }

    /**
     * 检查当前用户是否有足够的训练次数
     *
     * @param requiredTimes 需要的训练次数
     * @return true-有足够次数，false-次数不足
     */
    public boolean hasEnoughTrainTimes(Integer requiredTimes) {
        if (requiredTimes == null || requiredTimes <= 0) {
            return true;
        }
        return getRemainingTrainTimes() >= requiredTimes;
    }

    /**
     * 检查当前用户是否有足够的绘图次数
     *
     * @param requiredNum 需要的绘图次数
     * @return true-有足够次数，false-次数不足
     */
    public boolean hasEnoughDrawNum(Integer requiredNum) {
        if (requiredNum == null || requiredNum <= 0) {
            return true;
        }
        return getRemainingDrawNum() >= requiredNum;
    }
}

