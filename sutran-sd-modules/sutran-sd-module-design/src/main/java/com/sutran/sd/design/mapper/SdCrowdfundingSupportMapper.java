package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 众筹支持记录Mapper接口
 *
 * @author sutran
 * @date 2025-10-10
 */
@Mapper
public interface SdCrowdfundingSupportMapper extends BaseMapper<SdCrowdfundingSupport> {

    /**
     * 根据项目ID查询支持记录
     *
     * @param projectId 项目ID
     * @return 支持记录列表
     */
    List<SdCrowdfundingSupport> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID查询已支付的支持记录
     *
     * @param projectId 项目ID
     * @return 已支付的支持记录列表
     */
    List<SdCrowdfundingSupport> selectPaidByProjectId(@Param("projectId") Long projectId);

    /**
     * 批量更新支持记录状态
     *
     * @param projectId 项目ID
     * @param status 新状态
     * @param refundReason 退款原因
     * @return 更新记录数
     */
    int updateStatusByProjectId(@Param("projectId") Long projectId, 
                               @Param("status") Integer status, 
                               @Param("refundReason") String refundReason);

    /**
     * 根据支持订单号查询支持记录
     *
     * @param supportNo 支持订单号
     * @return 支持记录
     */
    @Select("SELECT * FROM sd_crowdfunding_support WHERE support_no = #{supportNo}")
    SdCrowdfundingSupport selectBySupportNo(@Param("supportNo") String supportNo);
}
