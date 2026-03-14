package com.sutran.sd.draw.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdUserWorkComment;
import com.sutran.sd.draw.domain.vo.SdUserWorkCommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SD绘图 || 用户生图作品 || 评论 映射器
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserWorkCommentMapper extends BaseMapperPlus<SdUserWorkCommentMapper, SdUserWorkComment, SdUserWorkComment> {
    /**
     * 获取作品的全部评论列表
     * @param workId    作品ID
     * @param status    状态[0-正常,1-隐藏,2-删除]
     * @return 评论列表
     */
    List<SdUserWorkCommentVo> selectListById(@Param("workId") String workId, @Param("status") int status);
}
