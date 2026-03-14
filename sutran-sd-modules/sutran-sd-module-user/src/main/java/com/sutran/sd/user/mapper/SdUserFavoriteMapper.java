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

    class ModelDetail {
        public Long id;
        public String url;
        public String modelTag;
        public String belongUserName;
    }

    class WorkDetail {
        public Long id;
        public String url;
        public String belongUserName;
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
        "FROM sd_user_work",
        "WHERE id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<IdUrl> selectWorkUrlsByIds(@Param("ids") List<Long> ids);

    @Select({
        "<script>",
        "SELECT A.id, A.url AS url, A.model_tag AS modelTag, ",
        "COALESCE(B.nick_name, B.user_name) AS belongUserName",
        "FROM sd_user_model AS A",
        "LEFT JOIN sys_user AS B ON CAST(A.belong_user_id AS UNSIGNED) = B.user_id",
        "WHERE A.id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<ModelDetail> selectModelDetailsByIds(@Param("ids") List<Long> ids);

    @Select({
        "<script>",
        "SELECT A.id, A.file_url AS url, ",
        "COALESCE(B.nick_name, B.user_name) AS belongUserName",
        "FROM sd_user_work AS A",
        "LEFT JOIN sys_user AS B ON A.belong_user_id = B.user_id",
        "WHERE A.id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<WorkDetail> selectWorkDetailsByIds(@Param("ids") List<Long> ids);
}

