package com.sutran.sd.draw.service;

import com.sutran.sd.draw.domain.SdUserModelClassify;
import com.sutran.sd.draw.domain.vo.SdUserModelClassifyVo;

import java.util.List;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表服务接口
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
public interface SdUserModelClassifyService {

    /**
     * 获取用户模型分类列表
     * @param userId 用户ID
     * @return 用户模型分类列表
     */
    List<SdUserModelClassifyVo> selectList(Long userId);

    /**
     * 获取用户模型分类详情
     * @param id 分类ID
     * @return 用户模型分类详情
     */
    SdUserModelClassify selectById(String id);

    /**
     * 新增用户模型分类
     * @param entity 用户模型分类
     */
    void insert(SdUserModelClassify entity);

    /**
     * 更新用户模型分类
     * @param entity 用户模型分类
     */
    void updateById(SdUserModelClassify entity);

    /**
     * 删除用户模型分类
     * @param id 分类ID
     */
    void removeById(String id);
}

