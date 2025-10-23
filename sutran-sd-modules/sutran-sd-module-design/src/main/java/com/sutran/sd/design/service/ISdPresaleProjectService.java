package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.dto.PresaleOrderCreateDTO;
import com.sutran.sd.design.dto.PresaleProjectPublishDTO;
import com.sutran.sd.design.vo.PresaleOrderDetailVO;
import com.sutran.sd.design.vo.PresaleOrderListVO;
import com.sutran.sd.design.vo.PresaleProjectDetailVO;
import com.sutran.sd.design.vo.PresaleProjectListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 预售项目Service接口
 *
 * @author sutran
 * @date 2025-01-12
 */
public interface ISdPresaleProjectService {

    /**
     * 查询预售项目
     *
     * @param id 预售项目主键
     * @return 预售项目
     */
    SdPresaleProject selectSdPresaleProjectById(Long id);

    /**
     * 查询预售项目列表
     *
     * @param sdPresaleProject 预售项目
     * @return 预售项目集合
     */
    List<SdPresaleProject> selectSdPresaleProjectList(SdPresaleProject sdPresaleProject);

    /**
     * 分页查询预售项目列表
     *
     * @param sdPresaleProject 预售项目
     * @param pageQuery 分页查询
     * @return 预售项目分页数据
     */
    TableDataInfo<SdPresaleProject> selectPagePresaleProjectList(SdPresaleProject sdPresaleProject, PageQuery pageQuery);

    /**
     * 获取预售项目列表（前端展示用）
     *
     * @return 预售项目列表
     */
    R<List<PresaleProjectListVO>> getPresaleProjectList();

    /**
     * 获取预售项目列表（分页）
     *
     * @param pageQuery 分页查询
     * @return 预售项目分页数据
     */
    TableDataInfo<PresaleProjectListVO> getPresaleProjectListPage(PageQuery pageQuery);

    /**
     * 获取预售项目详情
     *
     * @param id 项目ID
     * @return 预售项目详情
     */
    R<PresaleProjectDetailVO> getPresaleProjectDetail(Long id);

    /**
     * 获取厂家参与的预售项目列表
     *
     * @return 厂家参与的预售项目列表
     */
    R<List<PresaleProjectListVO>> getManufacturerPresaleProjects();

    /**
     * 获取厂家参与的预售项目列表（分页）
     *
     * @param pageQuery 分页查询
     * @return 厂家参与的预售项目分页数据
     */
    TableDataInfo<PresaleProjectListVO> getManufacturerPresaleProjectsPage(PageQuery pageQuery);

    /**
     * 获取发起人的预售项目列表
     *
     * @return 发起人的预售项目列表
     */
    R<List<PresaleProjectListVO>> getCreatorPresaleProjects();

    /**
     * 获取发起人的预售项目列表（分页）
     *
     * @param pageQuery 分页查询
     * @return 发起人的预售项目分页数据
     */
    TableDataInfo<PresaleProjectListVO> getCreatorPresaleProjectsPage(PageQuery pageQuery);



    /**
     * 获取买家购买的预售项目列表
     *
     * @return 买家购买的预售项目列表
     */
    R<List<PresaleProjectListVO>> getBuyerPresaleProjects();

    /**
     * 获取买家购买的预售项目列表（分页）
     *
     * @param pageQuery 分页查询
     * @return 买家购买的预售项目分页数据
     */
    TableDataInfo<PresaleProjectListVO> getBuyerPresaleProjectsPage(PageQuery pageQuery);

    /**
     * 创建预售订单
     *
     * @param createDTO 创建订单DTO
     * @return 订单号
     */
    R<String> createPresaleOrder(PresaleOrderCreateDTO createDTO);

    /**
     * 获取我的预售订单列表
     *
     * @return 订单列表
     */
    R<List<PresaleOrderListVO>> getMyPresaleOrders();

    /**
     * 获取我的预售订单列表（分页）
     *
     * @param pageQuery 分页查询
     * @return 订单分页数据
     */
    TableDataInfo<PresaleOrderListVO> getMyPresaleOrdersPage(PageQuery pageQuery);

    /**
     * 获取预售项目的订单列表（分页）- 发货用
     *
     * @param projectId 项目ID
     * @param pageQuery 分页查询
     * @return 订单分页数据
     */
    TableDataInfo<PresaleOrderListVO> getProjectPresaleOrdersPage(Long projectId, PageQuery pageQuery);

    /**
     * 获取预售订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    R<PresaleOrderDetailVO> getPresaleOrderDetail(String orderNo);

    /**
     * 获取支付二维码
     *
     * @param orderNo 订单号
     * @return 支付二维码
     */
    R<String> getPaymentQr(String orderNo);

    /**
     * 发布预售项目
     *
     * @param publishDTO 发布项目DTO
     * @return 项目ID
     */
    R<String> publishPresaleProject(PresaleProjectPublishDTO publishDTO);

    /**
     * 上传实物照片
     *
     * @param file 照片文件
     * @return 照片URL
     */
    R<String> uploadManufacturerPhotos(MultipartFile file);
}
