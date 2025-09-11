package com.sutran.sd.draw.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdChannelData;
import com.sutran.sd.draw.domain.SdUserModelClassify;
import com.sutran.sd.draw.domain.vo.SdUserModelClassifyVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-10 20:54:48
 */
@Mapper
public interface SdUserModelClassifyMapper extends BaseMapperPlus<SdUserModelClassifyMapper, SdUserModelClassify, SdUserModelClassifyVo> {

    /**
     * 获取用户模型分类列表
     * @param userId 用户ID
     * @return 用户模型分类列表
     */
    @Select("SELECT id, name, crt_user_id AS crtUserId, crt_time AS crtTime FROM sd_user_model_classify WHERE crt_user_id=#{userId}")
    List<SdUserModelClassifyVo> selectListByUserId(@Param("userId") Long userId);

    /**
     * 获取用户模型分类列表
     * @param userId 用户ID
     * @return 用户模型分类列表
     */
    @Select("SELECT DISTINCT A.model_id AS modelId,A.classify_id AS classifyId,B.name AS classifyName FROM sd_user_model_classify_tmp AS A LEFT JOIN sd_user_model_classify AS B ON A.classify_id=B.id WHERE B.crt_user_id=#{userId}")
    List<JSONObject> selectModelClassifyListByUserId(@Param("userId") Long userId);

    /**
     * 获取用户模型分类详情
     * @param modelId 模型ID
     * @param userId 用户ID
     * @return 用户模型分类详情
     */
    @Select("SELECT DISTINCT A.classify_id AS classifyId,B.name AS classifyName FROM sd_user_model_classify_tmp AS A LEFT JOIN sd_user_model_classify AS B ON A.classify_id=B.id WHERE A.model_id=#{modelId} AND A.crt_user_id=#{userId}")
    JSONObject selectClassifyInfoByModelId(@Param("modelId") String modelId, @Param("userId") Long userId);

    /**
     * 新增用户模型分类
     * @param modelId 模型ID
     * @param classifyId 分类ID
     * @param userId 用户ID
     */
    @Insert("INSERT INTO sd_user_model_classify_tmp (model_id, classify_id, crt_user_id) VALUE (#{modelId},#{classifyId},#{userId})")
    void insertClassifyId(@Param("modelId") String modelId, @Param("classifyId") String classifyId, @Param("userId") Long userId);

    /**
     * 删除用户模型分类
     * @param modelId 模型ID
     * @param userId 用户ID
     */
    @Delete("DELETE FROM sd_user_model_classify_tmp WHERE model_id=#{modelId} AND crt_user_id=#{userId}")
    void deleteByModelId(@Param("modelId") String modelId, @Param("userId") Long userId);

    /**
     * 删除用户模型分类
     * @param classifyId 分类ID
     * @param userId 用户ID
     * @return 分类下模型数量
     */
    @Select("SELECT COUNT(*) FROM sd_user_model_classify_tmp WHERE classify_id=#{classifyId} AND crt_user_id=#{userId}")
    int selectCountByClassifyIdAndUserId(@Param("classifyId") String classifyId, @Param("userId") Long userId);

    /**
     * 删除用户模型分类
     * @param modelId 模型ID
     */
    @Delete("DELETE FROM sd_user_model_classify_tmp WHERE model_id=#{modelId}")
    void deleteOfAdminByModelId(@Param("modelId") long modelId);
}

