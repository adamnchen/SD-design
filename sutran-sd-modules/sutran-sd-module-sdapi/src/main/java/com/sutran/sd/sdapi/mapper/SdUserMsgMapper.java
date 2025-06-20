package com.sutran.sd.sdapi.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.sdapi.modules.system.entity.SdUserMsg;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
@Mapper
public interface SdUserMsgMapper extends BaseMapperPlus<SdUserMsgMapper, SdUserMsg, SdUserMsg> {
    @Select("SELECT id, title, msg_content AS msgContent, is_read AS isRead, read_time AS readTime,crt_time AS crtTime FROM sd_user_msg WHERE user_id=#{userId} AND is_read=0 ORDER BY crt_time DESC LIMIT 5")
    List<SdUserMsg> userMsgLatest(@Param("userId") Long userId);

    List<SdUserMsg> userMsgList(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    Page<SdUserMsg> userMsgList(@Param("userId") Long userId, @Param("isRead") Integer isRead, @Param("page") Page<SdUserMsg> page);

    @Select("SELECT COUNT(id) FROM sd_user_msg WHERE user_id=#{userId} AND is_read=#{isRead}")
    int userMsgTotal(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    @Update("UPDATE sd_user_msg SET is_read=1,read_time=#{now} WHERE id=#{id}")
    void readUserMsg(@Param("id") String id, @Param("now") Date now);

    @Select("SELECT id, title, msg_content AS msgContent, is_read AS isRead, read_time AS readTime,crt_time AS crtTime FROM sd_user_msg WHERE user_id=#{userId} AND is_read=0 AND title='关注微信公众号' LIMIT 1")
    SdUserMsg userNoFollowWxMpOfMsg(@Param("userId") Long userId);
}
