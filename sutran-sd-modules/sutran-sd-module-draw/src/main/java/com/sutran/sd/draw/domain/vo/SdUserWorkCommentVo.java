package com.sutran.sd.draw.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * SD绘图 || 用户生图作品 || 评论
 * @author zj
 * @date 2025年12月31日 10:53
 */
@Data
@Accessors(chain = true)
public class SdUserWorkCommentVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @Schema(description = "评论ID")
    private Long id;
    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private Long parentId;
    /**
     * 作品ID
     */
    @Schema(description = "作品ID")
    private Long workId;
    /**
     * 评论用户ID
     */
    @Schema(description = "评论用户ID")
    private Long userId;
    /**
     * 评论用户昵称
     */
    @Schema(description = "评论用户昵称")
    private String nickName;
    /**
     * 评论用户头像
     */
    @Schema(description = "评论用户头像")
    private String avatar;

    /**
     * 被回复评论ID
     */
    @Schema(description = "被回复评论ID")
    private Long toId;
    /**
     * 被回复用户ID
     */
    @Schema(description = "被回复用户ID")
    private Long toUserId;
    /**
     * 被回复用户昵称
     */
    @Schema(description = "被回复用户昵称")
    private String toNickName;
    /**
     * 被回复用户头像
     */
    @Schema(description = "被回复用户头像")
    private String toAvatar;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 状态[0-正常,1-隐藏,2-删除]
     */
    @Schema(description = "状态[0-正常,1-隐藏,2-删除]")
    private Integer status;
    /**
     * 子评论列表
     */
    @Schema(description = "子评论列表")
    private List<SdUserWorkCommentVo> childList;
}
