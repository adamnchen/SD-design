package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.entity.Model;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型数据访问层
 * 
 * @author SutranSD
 */
@Mapper
public interface ModelMapper extends BaseMapper<Model> {
    
    /**
     * 分页查询用户的模型列表
     * 
     * @param page 分页参数
     * @param userId 用户ID
     * @param modelType 模型类型（可选）
     * @param isUserDel 是否删除（可选）
     * @return 模型列表
     */
    IPage<Model> selectUserModelPage(Page<Model> page, 
                                   @Param("userId") String userId,
                                   @Param("modelType") String modelType,
                                   @Param("isUserDel") Integer isUserDel);
    
    /**
     * 查询用户的模型列表（不分页）
     * 
     * @param userId 用户ID
     * @param modelType 模型类型（可选）
     * @param isUserDel 是否删除（可选）
     * @return 模型列表
     */
    List<Model> selectUserModelList(@Param("userId") String userId,
                                  @Param("modelType") String modelType,
                                  @Param("isUserDel") Integer isUserDel);
    
    /**
     * 根据ID查询用户的模型详情
     * 
     * @param id 模型ID
     * @param userId 用户ID
     * @return 模型详情
     */
    Model selectUserModelById(@Param("id") Long id, @Param("userId") String userId);
    
    /**
     * 统计用户的模型数量
     * 
     * @param userId 用户ID
     * @param modelType 模型类型（可选）
     * @param isUserDel 是否删除（可选）
     * @return 模型数量
     */
    Long countUserModels(@Param("userId") String userId,
                        @Param("modelType") String modelType,
                        @Param("isUserDel") Integer isUserDel);
    
    /**
     * 删除用户的模型
     * 
     * @param id 模型ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteUserModel(@Param("id") Long id, @Param("userId") String userId);
}
