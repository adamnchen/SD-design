package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.design.domain.SdPresaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 预售订单Mapper接口
 *
 * @author sutran
 * @date 2025-10-19
 */
@Mapper
public interface SdPresaleOrderMapper extends BaseMapper<SdPresaleOrder> {

    /**
     * 根据订单号查询预售订单
     *
     * @param orderNo 订单号
     * @return 预售订单
     */
    SdPresaleOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据项目ID查询订单列表
     *
     * @param projectId 项目ID
     * @return 订单列表
     */
    java.util.List<SdPresaleOrder> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    java.util.List<SdPresaleOrder> selectByUserId(@Param("userId") Long userId);
}
