package com.sutran.sd.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.common.core.domain.entity.SdUserNormalPoint;
import com.sutran.sd.common.core.domain.vo.UserPointDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户信誉积分Mapper接口
 * 
 * @author sutran
 * @date 2025-10-14
 */
@Mapper
public interface SdUserNormalPointMapper extends BaseMapper<SdUserNormalPoint> {

    /**
     * 查询用户积分详情
     * 
     * @param userId 用户ID
     * @return 积分详情
     */
    UserPointDetailVO selectUserPointDetail(@Param("userId") Long userId);


    /**
     * 更新用户打样积分
     * 
     * @param userId 用户ID
     * @param pointChange 积分变化量
     * @return 影响行数
     */
    int updateProofingPoint(@Param("userId") Long userId, @Param("pointChange") Integer pointChange);

    /**
     * 更新用户销售积分
     * 
     * @param userId 用户ID
     * @param pointChange 积分变化量
     * @return 影响行数
     */
    int updateSalePoint(@Param("userId") Long userId, @Param("pointChange") Integer pointChange);
}
