package com.sutran.sd.design.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sutran.sd.design.domain.DesignSdUserModelFile;
import com.sutran.sd.design.vo.UserModelFileVO;

import java.util.List;

/**
 * 用户生图文件数据记录Service接口
 *
 * @author sutran
 * @date 2025-10-11
 */
public interface ISdUserModelFileService extends IService<DesignSdUserModelFile> {

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
    List<DesignSdUserModelFile> selectSdUserModelFileListByUserId(Long userId);

    /**
     * 根据用户ID和分类查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @return 用户生图文件数据记录集合
     */
    List<DesignSdUserModelFile> selectSdUserModelFileListByUserIdAndCategory(Long userId, Integer category);

    /**
     * 新增用户生图文件数据记录
     *
     * @param sdUserModelFile 用户生图文件数据记录
     * @return 结果
     */
    int insertSdUserModelFile(DesignSdUserModelFile sdUserModelFile);


    /**
     * 批量删除用户生图文件数据记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSdUserModelFileByIds(Long[] ids);

    /**
     * 删除用户生图文件数据记录信息
     *
     * @param id 用户生图文件数据记录主键
     * @return 结果
     */
    int deleteSdUserModelFileById(Long id);

    /**
     * 获取我的作品列表（VO格式）
     *
     * @param userId 用户ID
     * @return 我的作品列表
     */
    List<UserModelFileVO> getMyWorks(Long userId);

    /**
     * 根据分类获取我的作品列表（VO格式）
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @return 我的作品列表
     */
    List<UserModelFileVO> getMyWorksByCategory(Long userId, Integer category);

    /**
     * 获取我的作品详情（VO格式）
     *
     * @param id 作品ID
     * @param userId 用户ID
     * @return 作品详情
     */
    UserModelFileVO getMyWorkDetail(Long id, Long userId);
}
