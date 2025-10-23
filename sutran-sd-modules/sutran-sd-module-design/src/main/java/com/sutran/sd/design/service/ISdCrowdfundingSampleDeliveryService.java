package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdCrowdfundingSampleDelivery;
import com.sutran.sd.design.vo.SampleDeliveryListVO;
import java.util.List;

/**
 * 众筹样品发货记录Service接口
 *
 * @author sutran
 * @date 2025-01-12
 */
public interface ISdCrowdfundingSampleDeliveryService {

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
     * @param sdCrowdfundingSampleDelivery 样品发货记录
     * @param pageQuery 分页查询
     * @return 样品发货记录分页数据
     */
    TableDataInfo<SdCrowdfundingSampleDelivery> selectPageSampleDeliveryList(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery, PageQuery pageQuery);

    /**
     * 新增样品发货记录
     *
     * @param sdCrowdfundingSampleDelivery 样品发货记录
     * @return 结果
     */
    int insertSdCrowdfundingSampleDelivery(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery);

    /**
     * 修改样品发货记录
     *
     * @param sdCrowdfundingSampleDelivery 样品发货记录
     * @return 结果
     */
    int updateSdCrowdfundingSampleDelivery(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery);

    /**
     * 批量删除样品发货记录
     *
     * @param ids 需要删除的样品发货记录主键集合
     * @return 结果
     */
    int deleteSdCrowdfundingSampleDeliveryByIds(Long[] ids);

    /**
     * 删除样品发货记录信息
     *
     * @param id 样品发货记录主键
     * @return 结果
     */
    int deleteSdCrowdfundingSampleDeliveryById(Long id);

    /**
     * 获取我承接的已完成众筹项目及发货信息（分页）
     *
     * @param pageQuery 分页查询参数
     * @return 发货项目分页数据
     */
    TableDataInfo<SampleDeliveryListVO> getMyDeliveryProjects(PageQuery pageQuery);

    /**
     * 更新快递单号
     *
     * @param id 发货记录ID
     * @param trackingNumber 快递单号
     * @return 操作结果
     */
    R<String> updateTrackingNumber(Long id, String trackingNumber);

    /**
     * 上传样品图片
     *
     * @param id 发货记录ID
     * @param file 图片文件
     * @return 操作结果
     */
    R<String> uploadSampleImage(Long id, org.springframework.web.multipart.MultipartFile file);

    /**
     * 根据众筹项目ID查询所有发货信息
     *
     * @param projectId 众筹项目ID
     * @param pageQuery 分页查询参数
     * @return 发货信息分页数据
     */
    TableDataInfo<SampleDeliveryListVO> getProjectDeliveryInfo(Long projectId, PageQuery pageQuery);

}
