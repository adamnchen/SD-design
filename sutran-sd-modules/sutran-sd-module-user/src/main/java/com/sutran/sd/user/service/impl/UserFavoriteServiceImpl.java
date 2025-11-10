package com.sutran.sd.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.dto.UserFavoriteDTO;
import com.sutran.sd.common.core.domain.entity.SdUserFavorite;
import com.sutran.sd.common.core.domain.vo.UserFavoriteVO;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.user.mapper.SdUserFavoriteMapper;
import com.sutran.sd.user.service.IUserFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收藏服务实现类
 * 
 * @author sutran
 * @date 2025-11-07
 */
@Slf4j
@RequiredArgsConstructor
@Service("userFavoriteService")
public class UserFavoriteServiceImpl implements IUserFavoriteService {

    private final SdUserFavoriteMapper favoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFavorite(Long userId, UserFavoriteDTO favoriteDTO) {
        if (userId == null) {
            throw new ServiceException("用户未登录，操作失败");
        }

        // 验证收藏类型
        if (favoriteDTO.getFavoriteType() == null || 
            (favoriteDTO.getFavoriteType() != SdUserFavorite.FavoriteType.MODEL && 
             favoriteDTO.getFavoriteType() != SdUserFavorite.FavoriteType.WORK)) {
            throw new ServiceException("收藏类型无效，只能是1（模型）或2（作品）");
        }

        // 验证收藏对象ID
        if (favoriteDTO.getTargetId() == null) {
            throw new ServiceException("收藏对象ID不能为空");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<SdUserFavorite> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SdUserFavorite::getUserId, userId)
                    .eq(SdUserFavorite::getFavoriteType, favoriteDTO.getFavoriteType())
                    .eq(SdUserFavorite::getTargetId, favoriteDTO.getTargetId());
        long count = favoriteMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new ServiceException("您已经收藏过该对象");
        }

        // 创建收藏记录
        SdUserFavorite favorite = new SdUserFavorite();
        favorite.setUserId(userId);
        favorite.setFavoriteType(favoriteDTO.getFavoriteType());
        favorite.setTargetId(favoriteDTO.getTargetId());
        favorite.setCreateTime(new Date());
        favorite.setUpdateTime(new Date());

        return favoriteMapper.insert(favorite) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavorite(Long userId, Long favoriteId) {
        if (userId == null) {
            throw new ServiceException("用户未登录，操作失败");
        }

        if (favoriteId == null) {
            throw new ServiceException("收藏ID不能为空");
        }

        // 查询收藏记录，确保属于当前用户
        SdUserFavorite favorite = favoriteMapper.selectById(favoriteId);
        if (favorite == null) {
            throw new ServiceException("收藏记录不存在");
        }

        if (!favorite.getUserId().equals(userId)) {
            throw new ServiceException("无权删除该收藏记录");
        }

        return favoriteMapper.deleteById(favoriteId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavoriteByTarget(Long userId, Integer favoriteType, Long targetId) {
        if (userId == null) {
            throw new ServiceException("用户未登录，操作失败");
        }

        if (favoriteType == null || targetId == null) {
            throw new ServiceException("收藏类型和目标ID不能为空");
        }

        LambdaQueryWrapper<SdUserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SdUserFavorite::getUserId, userId)
               .eq(SdUserFavorite::getFavoriteType, favoriteType)
               .eq(SdUserFavorite::getTargetId, targetId);

        return favoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public Page<UserFavoriteVO> selectFavoritePage(Long userId, Integer favoriteType, PageQuery pageQuery) {
        if (userId == null) {
            throw new ServiceException("用户未登录，操作失败");
        }

        // 构建查询条件
        LambdaQueryWrapper<SdUserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SdUserFavorite::getUserId, userId);
        
        if (favoriteType != null) {
            wrapper.eq(SdUserFavorite::getFavoriteType, favoriteType);
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(SdUserFavorite::getCreateTime);

        // 构建分页对象
        Page<SdUserFavorite> page = pageQuery.build();
        
        // 执行分页查询
        Page<SdUserFavorite> favoritePage = favoriteMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<UserFavoriteVO> voPage = new Page<>(favoritePage.getCurrent(), favoritePage.getSize(), favoritePage.getTotal());
        List<UserFavoriteVO> voList = favoritePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public boolean isFavorite(Long userId, Integer favoriteType, Long targetId) {
        if (userId == null || favoriteType == null || targetId == null) {
            return false;
        }

        LambdaQueryWrapper<SdUserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SdUserFavorite::getUserId, userId)
               .eq(SdUserFavorite::getFavoriteType, favoriteType)
               .eq(SdUserFavorite::getTargetId, targetId);
        
        long count = favoriteMapper.selectCount(wrapper);
        return count > 0;
    }

    /**
     * 转换为VO对象
     */
    private UserFavoriteVO convertToVO(SdUserFavorite favorite) {
        UserFavoriteVO vo = new UserFavoriteVO();
        BeanUtils.copyProperties(favorite, vo);
        
        // 设置收藏类型名称
        if (favorite.getFavoriteType() != null) {
            if (favorite.getFavoriteType() == SdUserFavorite.FavoriteType.MODEL) {
                vo.setFavoriteTypeName("模型");
            } else if (favorite.getFavoriteType() == SdUserFavorite.FavoriteType.WORK) {
                vo.setFavoriteTypeName("作品");
            }
        }

        return vo;
    }
}

