package com.sutran.sd.sdapi.mapper;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModelClassify;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelClassifyVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-10 20:54:48
 */
public interface SdUserModelClassifyMapper extends BaseMapper<SdUserModelClassify> {

    @Select("SELECT id, name, crt_user_id AS crtUserId, crt_time AS crtTime FROM sd_user_model_classify WHERE crt_user_id=#{userId}")
    List<SdUserModelClassifyVo> selectListByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT A.model_id AS modelId,A.classify_id AS classifyId,B.name AS classifyName FROM sd_user_model_classify_tmp AS A LEFT JOIN sd_user_model_classify AS B ON A.classify_id=B.id WHERE B.crt_user_id=#{userId}")
    List<JSONObject> selectModelClassifyListByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT A.classify_id AS classifyId,B.name AS classifyName FROM sd_user_model_classify_tmp AS A LEFT JOIN sd_user_model_classify AS B ON A.classify_id=B.id WHERE A.model_id=#{modelId} AND A.crt_user_id=#{userId}")
    JSONObject selectClassifyInfoByModelId(@Param("modelId") String modelId, @Param("userId") Long userId);

    @Insert("INSERT INTO sd_user_model_classify_tmp (model_id, classify_id, crt_user_id) VALUE (#{modelId},#{classifyId},#{userId})")
    void insertClassifyId(@Param("modelId") String modelId, @Param("classifyId") String classifyId, @Param("userId") Long userId);

    @Delete("DELETE FROM sd_user_model_classify_tmp WHERE model_id=#{modelId} AND crt_user_id=#{userId}")
    void deleteByModelId(@Param("modelId") String modelId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sd_user_model_classify_tmp WHERE classify_id=#{classifyId} AND crt_user_id=#{userId}")
    int selectCountByClassifyIdAndUserId(@Param("classifyId") String classifyId, @Param("userId") Long userId);


    @Delete("DELETE FROM sd_user_model_classify_tmp WHERE model_id=#{modelId}")
    void deleteOfAdminByModelId(@Param("modelId") long modelId);
}

