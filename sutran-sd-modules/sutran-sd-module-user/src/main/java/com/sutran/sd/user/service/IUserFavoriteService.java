package com.sutran.sd.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.dto.UserFavoriteDTO;
import com.sutran.sd.common.core.domain.vo.UserFavoriteVO;

/**
 * 用户收藏服务接口
 * 
 * @author sutran
 * @date 2025-11-07
 */
public interface IUserFavoriteService {

    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param favoriteDTO 收藏信息
     * @return 是否成功
     */
    boolean addFavorite(Long userId, UserFavoriteDTO favoriteDTO);

    /**
     * 删除收藏
     * 
     * @param userId 用户ID
     * @param favoriteId 收藏ID
     * @return 是否成功
     */
    boolean deleteFavorite(Long userId, Long favoriteId);

    /**
     * 根据收藏对象删除收藏
     * 
     * @param userId 用户ID
     * @param favoriteType 收藏类型
     * @param targetId 收藏对象ID
     * @return 是否成功
     */
    boolean deleteFavoriteByTarget(Long userId, Integer favoriteType, Long targetId);

    /**
     * 分页查询用户收藏列表
     * 
     * @param userId 用户ID
     * @param favoriteType 收藏类型（可选）
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    Page<UserFavoriteVO> selectFavoritePage(Long userId, Integer favoriteType, PageQuery pageQuery);

    /**
     * 检查用户是否已收藏指定对象
     * 
     * @param userId 用户ID
     * @param favoriteType 收藏类型
     * @param targetId 收藏对象ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Integer favoriteType, Long targetId);
}

