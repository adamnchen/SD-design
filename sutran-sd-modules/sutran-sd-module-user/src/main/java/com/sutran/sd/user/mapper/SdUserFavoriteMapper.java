package com.sutran.sd.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.sutran.sd.common.core.domain.entity.SdUserFavorite;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户收藏Mapper接口
 * 
 * @author sutran
 * @date 2025-11-07
 */
@Mapper
public interface SdUserFavoriteMapper extends BaseMapper<SdUserFavorite> {

    class IdUrl {
        public Long id;
        public String url;
    }

    @Select({
        "<script>",
        "SELECT id, url AS url",
        "FROM sd_user_model",
        "WHERE id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<IdUrl> selectModelUrlsByIds(@Param("ids") List<Long> ids);

    @Select({
        "<script>",
        "SELECT id, file_url AS url",
        "FROM sd_user_model_file",
        "WHERE id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<IdUrl> selectWorkUrlsByIds(@Param("ids") List<Long> ids);
}

