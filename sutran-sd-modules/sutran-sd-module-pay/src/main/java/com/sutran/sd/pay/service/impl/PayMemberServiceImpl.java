package com.sutran.sd.pay.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.pay.mapper.PayMemberMapper;
import com.sutran.sd.pay.service.PayMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zj
 * @date 2025年08月21日 11:00
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PayMemberServiceImpl implements PayMemberService {

    private final PayMemberMapper baseMapper;

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
