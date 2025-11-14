package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.design.domain.SdPresaleDelivery;
import com.sutran.sd.design.vo.PresaleDeliveryListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预售发货记录Mapper接口
 *
 * @author sutran
 * @date 2025-10-22
 */
@Mapper
public interface SdPresaleDeliveryMapper {

    /**
     * 查询预售发货记录
     *
     * @param id 预售发货记录主键
     * @return 预售发货记录
     */
    SdPresaleDelivery selectSdPresaleDeliveryById(Long id);

    /**
     * 查询预售发货记录列表
     *
     * @param sdPresaleDelivery 预售发货记录
     * @return 预售发货记录集合
     */
    List<SdPresaleDelivery> selectSdPresaleDeliveryList(SdPresaleDelivery sdPresaleDelivery);

    /**
     * 分页查询预售发货记录列表
     *
     * @param page 分页参数
     * @param sdPresaleDelivery 预售发货记录
     * @return 预售发货记录分页数据
     */
    IPage<SdPresaleDelivery> selectPageSdPresaleDeliveryList(Page<SdPresaleDelivery> page, SdPresaleDelivery sdPresaleDelivery);

    /**
     * 新增预售发货记录
     *
     * @param sdPresaleDelivery 预售发货记录
     * @return 结果
     */
    int insertSdPresaleDelivery(SdPresaleDelivery sdPresaleDelivery);

    /**
     * 修改预售发货记录
     *
     * @param sdPresaleDelivery 预售发货记录
     * @return 结果
     */
    int updateSdPresaleDelivery(SdPresaleDelivery sdPresaleDelivery);

    /**
     * 删除预售发货记录
     *
     * @param id 预售发货记录主键
     * @return 结果
     */
    int deleteSdPresaleDeliveryById(Long id);

    /**
     * 批量删除预售发货记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSdPresaleDeliveryByIds(Long[] ids);

    /**
     * 根据发货单号查询发货记录
     *
     * @param deliveryNo 发货单号
     * @return 发货记录
     */
    SdPresaleDelivery selectByDeliveryNo(String deliveryNo);

    /**
     * 根据订单号查询发货记录
     *
     * @param orderNo 订单号
     * @return 发货记录列表
     */
    List<SdPresaleDelivery> selectByOrderNo(String orderNo);

    /**
     * 根据项目ID查询发货记录列表（分页）
     *
     * @param page 分页参数
     * @param projectId 项目ID
     * @return 发货记录分页数据
     */
    IPage<PresaleDeliveryListVO> selectProjectDeliveryList(Page<PresaleDeliveryListVO> page, @Param("projectId") Long projectId);

    /**
     * 根据用户ID查询发货记录列表（分页）
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @return 发货记录分页数据
     */
    IPage<PresaleDeliveryListVO> selectUserDeliveryList(Page<PresaleDeliveryListVO> page, @Param("userId") Long userId);

    /**
     * 根据发货人ID查询发货记录列表（分页）
     *
     * @param page 分页参数
     * @param senderUserId 发货人ID
     * @return 发货记录分页数据
     */
    IPage<PresaleDeliveryListVO> selectSenderDeliveryList(Page<PresaleDeliveryListVO> page, @Param("senderUserId") Long senderUserId);

    /**
     * 统计项目发货数量
     *
     * @param projectId 项目ID
     * @return 发货数量
     */
    int countProjectDelivery(@Param("projectId") Long projectId);

    /**
     * 统计用户发货数量
     *
     * @param userId 用户ID
     * @return 发货数量
     */
    int countUserDelivery(@Param("userId") Long userId);
}
