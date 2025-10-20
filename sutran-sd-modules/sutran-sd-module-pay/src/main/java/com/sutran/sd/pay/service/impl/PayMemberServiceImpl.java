package com.sutran.sd.pay.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.OrderNumUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.pay.mapper.PayMemberMapper;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author zj
 * @date 2025年08月21日 11:00
 */
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
@Service
public class PayMemberServiceImpl implements PayMemberService {

    private final PayMemberMapper baseMapper;
    private final UserService userService;
    private final AliPayService aliPayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String purchaseMember(String  memberId) {
        PayMember payMember = baseMapper.selectById(memberId);
        if (payMember == null) {
            throw new ServiceException("会员不存在或已被刪除!");
        }
        final String subject = payMember.getLevelName();
        final BigDecimal totalAmount = payMember.getPrice();
        final String body = payMember.getDescription();
        final Date now = new Date();

        final Long userId = LoginHelper.getUserId();
        final String username = LoginHelper.getUsername();

        // 获取当前用户已购买且处于生效中的会员ID
        String currentMemberId = userService.selectMemberIdByUserId(userId,now);
        if (StringUtils.isNotBlank(currentMemberId)) {
            PayMember currentPayMember = baseMapper.selectById(currentMemberId);
            if (currentPayMember != null && currentPayMember.getLevel() > payMember.getLevel()) {
                throw new ServiceException(String.format("会员[%s]未到期，不可降级购买会员!",currentPayMember.getLevelName()));
            }
        }
        String outTradeNo = OrderNumUtils.getOrderNum(now);
        return aliPayService.createMemberPayOrder(userId, username, outTradeNo, subject, body, totalAmount, "/pay/ali/notify_url", payMember.getId());
    }

    @Override
    public List<PayMember> selectMemberList(PayMember member) {
        LambdaQueryWrapper<PayMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ObjectUtil.isNotNull(member.getStatus()), PayMember::getStatus,member.getStatus())
            .eq(ObjectUtil.isNotNull(member.getId()), PayMember::getId, member.getId())
            .eq(ObjectUtil.isNotNull(member.getLevelName()), PayMember::getLevelName, member.getLevelName())
            .like(StringUtils.isNotBlank(member.getDescription()), PayMember::getDescription, member.getDescription())
            .eq(ObjectUtil.isNotNull(member.getLevel()), PayMember::getLevel, member.getLevel())
            .orderByAsc(PayMember::getId);
        return baseMapper.selectList(lqw);
    }

    @Override
    public PayMember detailById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean insert(PayMember config) {
        return baseMapper.insertOrUpdate(config);
    }

    @Override
    public boolean updateById(PayMember member) {
        return baseMapper.updateById(member)>0;
    }

    @Override
    public boolean removeById(String id) {
        return baseMapper.deleteById(id)>0;
    }

    @Override
    public int selectDurationById(Long id) {
        Integer duration = baseMapper.selectDurationById(id);
        return duration==null?30:duration;
    }

    @Override
    public boolean hasUser(String id) {
        return baseMapper.hasUser(id);
    }
}
