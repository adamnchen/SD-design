package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.Model;
import com.sutran.sd.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 模型服务接口
 * 
 * @author SutranSD
 */
public interface IModelService {
    
    /**
     * 分页查询当前用户的模型列表
     * 
     * @param pageQuery 分页参数
     * @param modelType 模型类型（可选）
     * @param isUserDel 是否删除（可选）
     * @return 分页结果
     */
    TableDataInfo<Model> getUserModelPage(PageQuery pageQuery, String modelType, Integer isUserDel);
    
    /**
     * 查询当前用户的模型列表（不分页）
     * 
     * @param modelType 模型类型（可选）
     * @param isUserDel 是否删除（可选）
     * @return 模型列表
     */
    List<Model> getUserModelList(String modelType, Integer isUserDel);
    
    /**
     * 根据ID查询当前用户的模型详情
     * 
     * @param id 模型ID
     * @return 模型详情
     */
    Model getUserModelById(Long id);
    
    /**
     * 统计当前用户的模型数量
     * 
     * @param modelType 模型类型（可选）
     * @param isUserDel 是否删除（可选）
     * @return 模型数量
     */
    Long countUserModels(String modelType, Integer isUserDel);
    
    /**
     * 删除当前用户的模型
     * 
     * @param id 模型ID
     * @return 是否成功
     */
    boolean deleteUserModel(Long id);
}
