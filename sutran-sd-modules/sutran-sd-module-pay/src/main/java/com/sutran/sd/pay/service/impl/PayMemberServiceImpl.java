package com.sutran.sd.pay.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.page.TableDataInfo;
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

    /**
     * [前端]购买会员
     * @param memberId 会员ID
     * @return 支付二维码
     */
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

    /**
     * [前端]获取会员配置列表
     * @param member 会员配置
     * @return 会员配置列表
     */
    @Override
    public List<PayMember> selectMemberList(PayMember member) {
        LambdaQueryWrapper<PayMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PayMember::getStatus,1).eq(PayMember::getIsHide,0)
            .eq(ObjectUtil.isNotNull(member.getId()), PayMember::getId, member.getId())
            .eq(ObjectUtil.isNotNull(member.getLevelName()), PayMember::getLevelName, member.getLevelName())
            .like(StringUtils.isNotBlank(member.getDescription()), PayMember::getDescription, member.getDescription())
            .eq(ObjectUtil.isNotNull(member.getLevel()), PayMember::getLevel, member.getLevel())
            .orderByAsc(PayMember::getId);
        return baseMapper.selectList(lqw);
    }

    /**
     * [后台]获取会员配置分页列表
     * @param member    会员配置
     * @param pageQuery 分页查询参数
     * @return          会员配置分页列表
     */
    @Override
    public TableDataInfo<PayMember> selectMemberPage(PayMember member, PageQuery pageQuery) {
        LambdaQueryWrapper<PayMember> lqw = new LambdaQueryWrapper<PayMember>()
            .eq(PayMember::getStatus,member.getStatus()).eq(PayMember::getIsHide,member.getIsHide())
            .eq(ObjectUtil.isNotNull(member.getId()), PayMember::getId, member.getId())
            .eq(ObjectUtil.isNotNull(member.getLevelName()), PayMember::getLevelName, member.getLevelName())
            .like(StringUtils.isNotBlank(member.getDescription()), PayMember::getDescription, member.getDescription())
            .eq(ObjectUtil.isNotNull(member.getLevel()), PayMember::getLevel, member.getLevel())
            .orderByAsc(PayMember::getId);
        Page<PayMember> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * [后台]查询会员配置详情
     * @param id 会员配置ID
     * @return 会员配置详情
     */
    @Override
    public PayMember detailById(String id) {
        return baseMapper.selectById(id);
    }

    /**
     * [后台]新增会员配置
     * @param config 会员配置
     * @return 新增结果
     */
    @Override
    public boolean insert(PayMember config) {
        return baseMapper.insertOrUpdate(config);
    }

    /**
     * [后台]更新会员配置
     * @param member 会员配置
     * @return 更新结果
     */
    @Override
    public boolean updateById(PayMember member) {
        return baseMapper.updateById(member)>0;
    }

    /**
     * [后台]删除会员配置
     * @param id 会员配置ID
     * @return 删除结果
     */
    @Override
    public boolean removeById(String id) {
        return baseMapper.deleteById(id)>0;
    }

    /**
     * [后台]获取会员配置时长
     * @param id 会员配置ID
     * @return 会员配置时长
     */
    @Override
    public int selectDurationById(Long id) {
        Integer duration = baseMapper.selectDurationById(id);
        return duration==null?30:duration;
    }

     /**
     * [后台]判断会员配置下是否有用户
     * @param id 会员配置ID
     * @return 是否有用户
     */
    @Override
    public boolean hasUser(String id) {
        return baseMapper.hasUser(id);
    }
}
