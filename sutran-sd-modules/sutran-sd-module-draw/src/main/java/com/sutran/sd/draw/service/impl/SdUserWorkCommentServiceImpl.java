package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.draw.domain.SdUserWorkComment;
import com.sutran.sd.draw.domain.vo.SdUserWorkCommentVo;
import com.sutran.sd.draw.mapper.SdUserWorkCommentMapper;
import com.sutran.sd.draw.service.SdUserWorkCommentService;
import com.sutran.sd.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SD绘图 || 用户生图作品 || 评论 服务实现类
 * @author zj
 * @date 2024-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdUserWorkCommentServiceImpl implements SdUserWorkCommentService {

    private final SdUserWorkCommentMapper sdUserWorkCommentMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public void addComment(SdUserWorkComment sdUserWorkComment) {

    }

    @Override
    public void delComment(String commentId) {

    }

    /**
     * 按照作品批量查询对应的有效评论数量
     * @param workIds  作品ID
     * @return 作品和对应评论数量映射
     */
    @Override
    public Map<Long, Long> countCommentMapByWorkIds(List<Long> workIds) {
        if (CollectionUtil.isEmpty(workIds)) {
            return Collections.emptyMap();
        }
        List<SdUserWorkComment> list = sdUserWorkCommentMapper.selectList(new LambdaQueryWrapper<SdUserWorkComment>().in(SdUserWorkComment::getWorkId, workIds).eq(SdUserWorkComment::getStatus, 0));
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(SdUserWorkComment::getUserId, Collectors.counting()));
    }

    /**
     * 获取作品的全部评论列表
     * @param workId    作品ID
     * @return  评论列表
     */
    @Override
    public List<SdUserWorkCommentVo> getPublicWorksOfComments(String workId) {
        List<SdUserWorkCommentVo> list = sdUserWorkCommentMapper.selectListById(workId,0);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        //TODO 获取全部主评论
        List<SdUserWorkCommentVo> mainCommentList = list.stream().filter(e -> e.getParentId() == null || e.getParentId() == 0).collect(Collectors.toList());
        List<SdUserWorkCommentVo> childCommentList = list.stream().filter(e -> e.getParentId() != null && e.getParentId() > 0).collect(Collectors.toList());

        return Collections.emptyList();
    }
}
