package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sutran.sd.design.domain.SdCrowdfundingSampleDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 众筹样品发货记录Mapper接口
 *
 * @author sutran
 * @date 2025-01-12
 */
@Mapper
public interface SdCrowdfundingSampleDeliveryMapper extends BaseMapper<SdCrowdfundingSampleDelivery> {

    /**
     * 查询样品发货记录
     *
     * @param id 样品发货记录主键
     * @return 样品发货记录
     */
    SdCrowdfundingSampleDelivery selectSdCrowdfundingSampleDeliveryById(Long id);

    /**
     * 查询样品发货记录列表
     *
     * @param sdCrowdfundingSampleDelivery 样品发货记录
     * @return 样品发货记录集合
     */
    List<SdCrowdfundingSampleDelivery> selectSdCrowdfundingSampleDeliveryList(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery);

    /**
     * 分页查询样品发货记录列表
     *
     * @param page 分页对象
     * @param sdCrowdfundingSampleDelivery 样品发货记录
     * @return 样品发货记录分页数据
     */
    IPage<SdCrowdfundingSampleDelivery> selectPageSampleDeliveryList(IPage<SdCrowdfundingSampleDelivery> page, @Param("sdCrowdfundingSampleDelivery") SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery);

    /**
     * 根据众筹项目ID查询发货记录
     *
     * @param projectId 众筹项目ID
     * @return 发货记录列表
     */
    List<SdCrowdfundingSampleDelivery> selectByProjectId(Long projectId);

    /**
     * 根据收货人用户ID查询收货记录
     *
     * @param recipientUserId 收货人用户ID
     * @return 收货记录列表
     */
    List<SdCrowdfundingSampleDelivery> selectByRecipientUserId(Long recipientUserId);

    /**
     * 根据发货人用户ID查询发货记录
     *
     * @param senderUserId 发货人用户ID
     * @return 发货记录列表
     */
    List<SdCrowdfundingSampleDelivery> selectBySenderUserId(Long senderUserId);
}
