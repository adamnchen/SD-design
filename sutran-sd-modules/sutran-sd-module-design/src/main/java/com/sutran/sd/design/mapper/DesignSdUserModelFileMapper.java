package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.design.domain.DesignSdUserModelFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设计模块用户生图文件数据记录Mapper接口
 *
 * @author sutran
 * @date 2025-10-11
 */
@Mapper
public interface DesignSdUserModelFileMapper extends BaseMapper<DesignSdUserModelFile> {

    /**
     * 查询用户生图文件数据记录
     *
     * @param id 用户生图文件数据记录主键
     * @return 用户生图文件数据记录
     */
    DesignSdUserModelFile selectSdUserModelFileById(Long id);

    /**
     * 查询用户生图文件数据记录列表
     *
     * @param sdUserModelFile 用户生图文件数据记录
     * @return 用户生图文件数据记录集合
     */
    List<DesignSdUserModelFile> selectSdUserModelFileList(DesignSdUserModelFile sdUserModelFile);

    /**
     * 根据用户ID查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @return 用户生图文件数据记录集合
     */
    List<DesignSdUserModelFile> selectSdUserModelFileListByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和分类查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @return 用户生图文件数据记录集合
     */
    List<DesignSdUserModelFile> selectSdUserModelFileListByUserIdAndCategory(@Param("userId") Long userId, @Param("category") Integer category);

    /**
     * 新增用户生图文件数据记录
     *
     * @param sdUserModelFile 用户生图文件数据记录
     * @return 结果
     */
    int insertSdUserModelFile(DesignSdUserModelFile sdUserModelFile);


    /**
     * 删除用户生图文件数据记录
     *
     * @param id 用户生图文件数据记录主键
     * @return 结果
     */
    int deleteSdUserModelFileById(Long id);

    /**
     * 批量删除用户生图文件数据记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSdUserModelFileByIds(Long[] ids);

    /**
     * 设置/取消公开
     * @param id 作品ID
     * @param userId 所属用户ID
     * @param isPublic 是否公开(0/1)
     * @return 受影响行数
     */
    int updatePublicByIdAndUser(@Param("id") Long id, @Param("userId") Long userId, @Param("isPublic") Integer isPublic);
}
