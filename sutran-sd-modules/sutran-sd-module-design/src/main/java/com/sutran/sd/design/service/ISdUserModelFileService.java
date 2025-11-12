package com.sutran.sd.design.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
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

    /**
     * 获取我的作品列表（分页查询）
     *
     * @param userId 用户ID
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    TableDataInfo<UserModelFileVO> getMyWorksPage(Long userId, PageQuery pageQuery);

    /**
     * 根据分类获取我的作品列表（分页查询）
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    TableDataInfo<UserModelFileVO> getMyWorksByCategoryPage(Long userId, Integer category, PageQuery pageQuery);

    /**
     * 设置或取消作品公开
     *
     * @param id 作品ID
     * @param userId 当前用户ID
     * @param isPublic 是否公开 true/false
     * @return 操作是否成功
     */
    boolean setPublic(Long id, Long userId, boolean isPublic);

    /**
     * 查询公开作品列表（分页）
     *
     * @param pageQuery 分页查询参数
     * @return 分页结果（仅公开作品）
     */
    TableDataInfo<UserModelFileVO> getPublicWorksPage(PageQuery pageQuery);
}
