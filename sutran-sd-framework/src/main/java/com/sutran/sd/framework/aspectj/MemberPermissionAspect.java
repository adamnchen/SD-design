package com.sutran.sd.framework.aspectj;

import cn.hutool.core.date.DateUtil;
import com.sutran.sd.common.annotation.RequireMember;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.entity.SysUserMember;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.mapper.SysUserMemberMapper;
import com.sutran.sd.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 会员权限校验切面
 *
 * @author SutranSD
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberPermissionAspect {

    private final SysUserMemberMapper userMemberMapper;
    private final SysUserMapper userMapper;
    
    /**
     * 新用户生图权益天数（3天）
     */
    private static final int NEW_USER_DRAW_BENEFIT_DAYS = 3;
    
    /**
     * 新用户设计权益天数（30天）
     */
    private static final int NEW_USER_DESIGN_BENEFIT_DAYS = 30;

    /**
     * 环绕通知：校验会员权限
     */
    @Around("@annotation(requireMember)")
    public Object checkMemberPermission(ProceedingJoinPoint joinPoint, RequireMember requireMember) throws Throwable {
        // 1. 获取当前用户ID
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录或Token无效");
        }

        // 2. 检查新用户权益（优先检查）
        if (hasNewUserBenefit(userId, requireMember)) {
            log.info("[会员权限校验] 用户ID: {}, 功能: {}, 享受新用户权益", userId, requireMember.value());
            return joinPoint.proceed();
        }

        // 3. 获取用户当前有效的会员信息
        SysUserMember currentMember = getCurrentValidMember(userId);

        // 4. 校验会员权限
        validateMemberPermission(currentMember, requireMember, userId);

        // 5. 记录访问日志
        log.info("[会员权限校验] 用户ID: {}, 功能: {}, 会员状态: {}",
                userId, requireMember.value(),
                currentMember != null ? "有效" : "无效");

        // 6. 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 获取用户当前有效的会员信息
     */
    private SysUserMember getCurrentValidMember(Long userId) {
        try {
            List<Long> userIds = Collections.singletonList(userId);
            List<SysUserMember> members = userMemberMapper.selectMemberInfoByUserIds(userIds, new Date());

            if (members != null && !members.isEmpty()) {
                return members.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("[会员权限校验] 查询用户会员信息失败: 用户ID={}", userId, e);
            return null;
        }
    }

    /**
     * 检查是否享受新用户权益
     */
    private boolean hasNewUserBenefit(Long userId, RequireMember requireMember) {
        // 如果没有配置新用户权益，直接返回false
        if (requireMember.newUserBenefit() == null || requireMember.newUserBenefit().length == 0) {
            return false;
        }
        
        try {
            // 获取用户信息
            SysUser user = userMapper.selectUserById(userId);
            if (user == null || user.getCreateTime() == null) {
                return false;
            }
            
            Date now = new Date();
            Date createTime = user.getCreateTime();
            // 计算注册天数：createTime到now的天数差
            long daysSinceRegister = DateUtil.betweenDay(createTime, now, true);
            
            // 检查各种新用户权益类型
            for (RequireMember.NewUserBenefitType benefitType : requireMember.newUserBenefit()) {
                switch (benefitType) {
                    case DRAW:
                        // 生图功能：新注册用户3天内免费无限生图
                        if (daysSinceRegister < NEW_USER_DRAW_BENEFIT_DAYS) {
                            log.info("[会员权限校验] 用户ID: {}, 享受新用户生图权益，注册天数: {}", userId, daysSinceRegister);
                            return true;
                        }
                        break;
                    case DESIGN:
                        // 设计功能：新注册用户1个月内打样邀约和预售不受限制
                        if (daysSinceRegister < NEW_USER_DESIGN_BENEFIT_DAYS) {
                            log.info("[会员权限校验] 用户ID: {}, 享受新用户设计权益，注册天数: {}", userId, daysSinceRegister);
                            return true;
                        }
                        break;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("[会员权限校验] 检查新用户权益失败: 用户ID={}", userId, e);
            return false;
        }
    }

    /**
     * 校验会员权限
     */
    private void validateMemberPermission(SysUserMember member, RequireMember requireMember, Long userId) {
        // 如果没有有效会员
        if (member == null) {
            String errorMessage = StringUtils.isNotBlank(requireMember.message())
                ? requireMember.message()
                : requireMember.value() + "需要会员权限，请先购买会员";
            throw new ServiceException(errorMessage);
        }

        // 检查会员是否过期
        if (member.getEndTime() != null && member.getEndTime().before(new Date())) {
            String errorMessage = StringUtils.isNotBlank(requireMember.message())
                ? requireMember.message()
                : "您的会员已过期，请续费后使用" + requireMember.value();
            throw new ServiceException(errorMessage);
        }

        // 检查会员状态
        if (member.getStatus() == null || member.getStatus() != 1) {
            String errorMessage = StringUtils.isNotBlank(requireMember.message())
                ? requireMember.message()
                : "您的会员状态异常，无法使用" + requireMember.value();
            throw new ServiceException(errorMessage);
        }

        // 如果允许试用用户访问，可以在这里添加额外的逻辑
        if (requireMember.allowTrial()) {
            // 可以添加试用用户的特殊处理逻辑
            log.debug("[会员权限校验] 允许试用用户访问: 用户ID={}", userId);
        }
    }
}
