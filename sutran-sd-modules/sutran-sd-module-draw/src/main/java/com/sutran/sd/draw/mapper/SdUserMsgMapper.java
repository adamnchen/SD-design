package com.sutran.sd.draw.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdUserMsg;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
@Mapper
public interface SdUserMsgMapper extends BaseMapperPlus<SdUserMsgMapper, SdUserMsg, SdUserMsg> {

    /**
     * 查询用户最新消息
     * @param userId    用户ID
     * @return          用户最新消息
     */
    @Select("SELECT id, title, msg_content AS msgContent, is_read AS isRead, read_time AS readTime,crt_time AS crtTime FROM sd_user_msg WHERE user_id=#{userId} AND is_read=0 ORDER BY crt_time DESC LIMIT 5")
    List<SdUserMsg> userMsgLatest(@Param("userId") Long userId);

    /**
     * 查询用户消息列表
     * @param userId    用户ID
     * @param isRead    是否已读
     * @return          用户消息列表
     */
    List<SdUserMsg> userMsgList(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    /**
     * 查询用户消息列表
     * @param userId    用户ID
     * @param isRead    是否已读
     * @param page      分页查询
     * @return          用户消息列表
     */
    Page<SdUserMsg> userMsgList(@Param("userId") Long userId, @Param("isRead") Integer isRead, @Param("page") Page<SdUserMsg> page);

    /**
     * 查询用户消息总数
     * @param userId    用户ID
     * @param isRead    是否已读
     * @return          用户消息总数
     */
    @Select("SELECT COUNT(id) FROM sd_user_msg WHERE user_id=#{userId} AND is_read=#{isRead}")
    int userMsgTotal(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    /**
     * 读取用户消息
     * @param id    消息ID
     */
    @Update("UPDATE sd_user_msg SET is_read=1,read_time=#{now} WHERE id=#{id}")
    void readUserMsg(@Param("id") String id, @Param("now") Date now);

    /**
     * 查询用户未关注微信公众号消息
     * @param userId    用户ID
     * @return          用户未关注微信公众号消息
     */
    @Select("SELECT id, title, msg_content AS msgContent, is_read AS isRead, read_time AS readTime,crt_time AS crtTime FROM sd_user_msg WHERE user_id=#{userId} AND is_read=0 AND title='关注微信公众号' LIMIT 1")
    SdUserMsg userNoFollowWxMpOfMsg(@Param("userId") Long userId);
}
