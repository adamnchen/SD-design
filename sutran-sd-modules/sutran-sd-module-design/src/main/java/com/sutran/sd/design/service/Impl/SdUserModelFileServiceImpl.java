package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.design.domain.DesignSdUserModelFile;
import com.sutran.sd.design.mapper.DesignSdUserModelFileMapper;
import com.sutran.sd.design.service.ISdUserModelFileService;
import com.sutran.sd.design.vo.UserModelFileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
        
        return files.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<UserModelFileVO> getMyWorksByCategory(Long userId, Integer category) {
        log.info("根据分类获取我的作品列表: 用户ID={}, 分类={}", userId, category);
        
        List<DesignSdUserModelFile> files = selectSdUserModelFileListByUserIdAndCategory(userId, category);
        
        return files.stream().map(this::convertToVO).collect(Collectors.toList());
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
        
        return convertToVO(file);
    }

    /**
     * 转换为VO对象
     */
    private UserModelFileVO convertToVO(DesignSdUserModelFile file) {
        UserModelFileVO vo = new UserModelFileVO();
        BeanUtils.copyProperties(file, vo);
        return vo;
    }
}
