package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 众筹支持记录Mapper接口
 *
 * @author sutran
 * @date 2025-10-10
 */
@Mapper
public interface SdCrowdfundingSupportMapper extends BaseMapper<SdCrowdfundingSupport> {

    /**
     * 根据支付订单号查询支持记录
     *
     * @param orderNo 支付订单号
     * @return 支持记录
     */
    @Select("SELECT * FROM sd_crowdfunding_support WHERE order_no = #{orderNo}")
    SdCrowdfundingSupport selectByOrderNo(@Param("orderNo") String orderNo);
}
