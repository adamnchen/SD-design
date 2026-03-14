package com.sutran.sd.draw.service;

import com.sutran.sd.draw.domain.SdUserWorkComment;
import com.sutran.sd.draw.domain.vo.SdUserWorkCommentVo;

import java.util.List;
import java.util.Map;

/**
 * SD绘图 || 用户生图作品 || 评论
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserWorkCommentService {

    /**
     * 新增评论
     * @param sdUserWorkComment 评论实体类
     */
    void addComment(SdUserWorkComment sdUserWorkComment);

    /**
     * 删除评论
     * @param commentId 评论ID
     */
    void delComment(String commentId);

    /**
     * 按照作品批量查询对应的有效评论数量
     * @param workIds  作品ID
     * @return 作品和对应评论数量映射
     */
    Map<Long, Long> countCommentMapByWorkIds(List<Long> workIds);

    /**
     * 获取作品的全部评论列表
     * @param workId    作品ID
     * @return  评论列表
     */
    List<SdUserWorkCommentVo> getPublicWorksOfComments(String workId);
}
