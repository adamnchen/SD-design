package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.SdUserFavorite;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.DesignSdUserModelFile;
import com.sutran.sd.design.mapper.DesignSdUserModelFileMapper;
import com.sutran.sd.design.service.ISdUserModelFileService;
import com.sutran.sd.design.vo.UserModelFileVO;
import com.sutran.sd.user.service.IUserFavoriteService;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户生图文件数据记录Service业务层处理
 *
 * @author sutran
 * @date 2025-10-11
 */
@Slf4j
@Service("designSdUserModelFileServiceImpl")
@RequiredArgsConstructor
public class SdUserModelFileServiceImpl extends ServiceImpl<DesignSdUserModelFileMapper, DesignSdUserModelFile> implements ISdUserModelFileService {

    private final DesignSdUserModelFileMapper userModelFileMapper;
    private final IUserFavoriteService favoriteService;
    private final SysUserMapper sysUserMapper;

    @Override
    public DesignSdUserModelFile selectSdUserModelFileById(Long id) {
        return userModelFileMapper.selectSdUserModelFileById(id);
    }

    @Override
    public List<DesignSdUserModelFile> selectSdUserModelFileList(DesignSdUserModelFile sdUserModelFile) {
        return userModelFileMapper.selectSdUserModelFileList(sdUserModelFile);
    }

    @Override
    public List<DesignSdUserModelFile> selectSdUserModelFileListByUserId(Long userId) {
        return userModelFileMapper.selectSdUserModelFileListByUserId(userId);
    }

    @Override
    public List<DesignSdUserModelFile> selectSdUserModelFileListByUserIdAndCategory(Long userId, Integer category) {
        return userModelFileMapper.selectSdUserModelFileListByUserIdAndCategory(userId, category);
    }

    @Override
    public int insertSdUserModelFile(DesignSdUserModelFile sdUserModelFile) {
        return userModelFileMapper.insertSdUserModelFile(sdUserModelFile);
    }


    @Override
    public int deleteSdUserModelFileByIds(Long[] ids) {
        return userModelFileMapper.deleteSdUserModelFileByIds(ids);
    }

    @Override
    public int deleteSdUserModelFileById(Long id) {
        return userModelFileMapper.deleteSdUserModelFileById(id);
    }

    @Override
    public List<UserModelFileVO> getMyWorks(Long userId) {
        log.info("获取我的作品列表: 用户ID={}", userId);

        List<DesignSdUserModelFile> files = selectSdUserModelFileListByUserId(userId);

        return files.stream().map(file -> convertToVO(file, userId)).collect(Collectors.toList());
    }

    @Override
    public List<UserModelFileVO> getMyWorksByCategory(Long userId, Integer category) {
        log.info("根据分类获取我的作品列表: 用户ID={}, 分类={}", userId, category);

        List<DesignSdUserModelFile> files = selectSdUserModelFileListByUserIdAndCategory(userId, category);

        return files.stream().map(file -> convertToVO(file, userId)).collect(Collectors.toList());
    }

    @Override
    public UserModelFileVO getMyWorkDetail(Long id, Long userId) {
        log.info("获取我的作品详情: 作品ID={}, 用户ID={}", id, userId);

        // 先查询作品是否存在且属于当前用户
        LambdaQueryWrapper<DesignSdUserModelFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DesignSdUserModelFile::getId, id)
                   .eq(DesignSdUserModelFile::getBelongUserId, userId);

        DesignSdUserModelFile file = userModelFileMapper.selectOne(queryWrapper);
        if (file == null) {
            log.warn("作品不存在或不属于当前用户: 作品ID={}, 用户ID={}", id, userId);
            return null;
        }

        return convertToVO(file, userId);
    }

    /**
     * 转换为VO对象
     */
    private UserModelFileVO convertToVO(DesignSdUserModelFile file) {
        return convertToVO(file, null);
    }

    /**
     * 转换为VO对象（带用户ID，用于填充收藏状态）
     */
    private UserModelFileVO convertToVO(DesignSdUserModelFile file, Long userId) {
        UserModelFileVO vo = new UserModelFileVO();
        BeanUtils.copyProperties(file, vo);
        // 填充收藏状态
        if (userId != null && file.getId() != null) {
            vo.setIsFavorite(favoriteService.isFavorite(userId, SdUserFavorite.FavoriteType.WORK, file.getId()));
        } else {
            vo.setIsFavorite(false);
        }
        return vo;
    }

    @Override
    public TableDataInfo<UserModelFileVO> getMyWorksPage(Long userId, PageQuery pageQuery) {
        try {
            log.info("分页查询我的作品: 用户ID={}, 页码={}, 页大小={}", userId, pageQuery.getPageNum(), pageQuery.getPageSize());

            // 构建分页对象
            Page<DesignSdUserModelFile> page = pageQuery.build();

            // 构建查询条件
            LambdaQueryWrapper<DesignSdUserModelFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DesignSdUserModelFile::getBelongUserId, userId)
                       .orderByDesc(DesignSdUserModelFile::getCrtTime);

            // 执行分页查询
            Page<DesignSdUserModelFile> result = userModelFileMapper.selectPage(page, queryWrapper);

            // 转换为VO
            List<UserModelFileVO> voList = result.getRecords().stream()
                    .map(file -> convertToVO(file, userId))
                    .collect(Collectors.toList());

            // 构建分页结果
            TableDataInfo<UserModelFileVO> tableDataInfo = new TableDataInfo<>();
            tableDataInfo.setCode(200);
            tableDataInfo.setMsg("查询成功");
            tableDataInfo.setRows(voList);
            tableDataInfo.setTotal(result.getTotal());

            log.info("分页查询我的作品完成: 总数={}, 当前页数据量={}", result.getTotal(), voList.size());
            return tableDataInfo;

        } catch (Exception e) {
            log.error("分页查询我的作品失败: 用户ID={}", userId, e);
            return TableDataInfo.build();
        }
    }

    @Override
    public TableDataInfo<UserModelFileVO> getMyWorksByCategoryPage(Long userId, Integer category, PageQuery pageQuery) {
        try {
            log.info("分页查询我的作品(按分类): 用户ID={}, 分类={}, 页码={}, 页大小={}", userId, category, pageQuery.getPageNum(), pageQuery.getPageSize());

            // 构建分页对象
            Page<DesignSdUserModelFile> page = pageQuery.build();

            // 构建查询条件
            LambdaQueryWrapper<DesignSdUserModelFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DesignSdUserModelFile::getBelongUserId, userId)
                       .eq(DesignSdUserModelFile::getCategory, category)
                       .orderByDesc(DesignSdUserModelFile::getCrtTime);

            // 执行分页查询
            Page<DesignSdUserModelFile> result = userModelFileMapper.selectPage(page, queryWrapper);

            // 转换为VO
            List<UserModelFileVO> voList = result.getRecords().stream()
                    .map(file -> convertToVO(file, userId))
                    .collect(Collectors.toList());

            // 构建分页结果
            TableDataInfo<UserModelFileVO> tableDataInfo = new TableDataInfo<>();
            tableDataInfo.setCode(200);
            tableDataInfo.setMsg("查询成功");
            tableDataInfo.setRows(voList);
            tableDataInfo.setTotal(result.getTotal());

            log.info("分页查询我的作品(按分类)完成: 分类={}, 总数={}, 当前页数据量={}", category, result.getTotal(), voList.size());
            return tableDataInfo;

        } catch (Exception e) {
            log.error("分页查询我的作品(按分类)失败: 用户ID={}, 分类={}", userId, category, e);
            return TableDataInfo.build();
        }
    }

    @Override
    public boolean setPublic(Long id, Long userId, boolean isPublic) {
        // 只允许更新属于当前用户的作品
        // 如果设置为公开，记录公开时间；如果取消公开，清空公开时间
        Date publicTime = isPublic ? new Date() : null;
        int affected = userModelFileMapper.updatePublicByIdAndUser(id, userId, isPublic ? 1 : 0, publicTime);
        if (affected > 0) {
            log.info("设置作品公开状态: 作品ID={}, 用户ID={}, 是否公开={}, 公开时间={}",
                id, userId, isPublic, publicTime);
        }
        return affected > 0;
    }

    @Override
    public TableDataInfo<UserModelFileVO> getPublicWorksPage(PageQuery pageQuery) {
        try {
            log.info("分页查询公开作品");

            Page<DesignSdUserModelFile> page = pageQuery.build();
            LambdaQueryWrapper<DesignSdUserModelFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(DesignSdUserModelFile::getCrtTime)
                        .eq(DesignSdUserModelFile::getIsPublic, 1);


            Page<DesignSdUserModelFile> result = userModelFileMapper.selectPage(page, queryWrapper);

            // 转换为VO
            List<UserModelFileVO> voList = result.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 批量查询用户信息（头像和昵称）
            if (!voList.isEmpty()) {
                Set<Long> userIds = voList.stream()
                        .map(UserModelFileVO::getBelongUserId)
                        .filter(userId -> userId != null)
                        .collect(Collectors.toSet());

                if (!userIds.isEmpty()) {
                    // 批量查询用户信息（使用 LambdaQueryWrapper 查询指定用户ID列表）
                    LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
                    userQueryWrapper.in(SysUser::getUserId, userIds)
                                   .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName, SysUser::getAvatar);
                    List<SysUser> users = sysUserMapper.selectList(userQueryWrapper);
                    Map<Long, SysUser> userMap = users.stream()
                            .collect(Collectors.toMap(SysUser::getUserId, user -> user));

                    // 填充用户头像和昵称
                    voList.forEach(vo -> {
                        if (vo.getBelongUserId() != null) {
                            SysUser user = userMap.get(vo.getBelongUserId());
                            if (user != null) {
                                vo.setBelongUserAvatar(user.getAvatar());
                                vo.setBelongUserName(user.getUserName());
                                vo.setBelongUserNickName(user.getNickName());
                            }
                        }
                    });
                }
            }

            TableDataInfo<UserModelFileVO> tableDataInfo = new TableDataInfo<>();
            tableDataInfo.setCode(200);
            tableDataInfo.setMsg("查询成功");
            tableDataInfo.setRows(voList);
            tableDataInfo.setTotal(result.getTotal());
            return tableDataInfo;
        } catch (Exception e) {
            log.error("分页查询公开作品失败", e);
            return TableDataInfo.build();
        }
    }
}
