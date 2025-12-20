package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingProjectSimpleCreateDTO;
import com.sutran.sd.design.dto.CrowdfundingSupportDto;
import com.sutran.sd.design.vo.CrowdfundingProjectDetailVO;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.CrowdfundingSupportVO;
import com.sutran.sd.design.vo.CrowdfundingDrawVO;
import com.sutran.sd.design.vo.SampleDeliveryListVO;

import java.util.List;

/**
 * 众筹项目Service接口
 *
 * @author sutran
 * @date 2025-10-10
 */
public interface ISdCrowdfundingProjectService {

    /**
     * 查询众筹项目
     *
     * @param id 众筹项目主键
     * @return 众筹项目
     */
    SdCrowdfundingProject selectSdCrowdfundingProjectById(Long id);

    /**
     * 根据打样邀约ID查询众筹项目
     *
     * @param proofingInvitationId 打样邀约ID
     * @return 众筹项目
     */
    SdCrowdfundingProject selectByProofingInvitationId(Long proofingInvitationId);

    /**
     * 查询众筹项目列表
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 众筹项目集合
     */
    List<SdCrowdfundingProject> selectSdCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 分页查询众筹项目列表
     *
     * @param sdCrowdfundingProject 众筹项目
     * @param pageQuery 分页查询
     * @return 众筹项目分页数据
     */
    TableDataInfo<SdCrowdfundingProject> selectPageCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject, PageQuery pageQuery);

    /**
     * 根据类型分页查询众筹项目列表
     *
     * @param type 类型：published(我发布的)、supported(我购买的)、manufactured(我承接的)
     * @param pageQuery 分页查询
     * @return 众筹项目分页数据
     */
    TableDataInfo<SdCrowdfundingProject> selectPageCrowdfundingProjectListByType(String type, PageQuery pageQuery);



    /**
     * 简化新增众筹项目（前端只需要传厂家ID，其他信息从打样邀约中获取）
     *
     * @param createDTO 简化创建DTO
     */
    void insertSdCrowdfundingProjectSimple(CrowdfundingProjectSimpleCreateDTO createDTO);



    /**
     * 获取众筹项目列表
     *
     * @return 众筹项目列表
     */
    List<CrowdfundingProjectListVO> getCrowdfundingProjectList();

    /**
     * 获取进行中的众筹项目列表（前端展示用）
     *
     * @return 进行中的众筹项目列表
     */
    List<CrowdfundingProjectListVO> getActiveCrowdfundingProjects();

    /**
     * 获取进行中的众筹项目列表（分页）
     *
     * @param pageQuery 分页查询
     * @return 进行中的众筹项目分页数据
     */
    TableDataInfo<CrowdfundingProjectListVO> getActiveCrowdfundingProjectsPage(PageQuery pageQuery);

    /**
     * 获取众筹项目详情
     *
     * @param id 项目ID
     * @return 众筹项目详情
     */
    CrowdfundingProjectDetailVO getCrowdfundingProjectDetail(Long id);


    /**
     * 获取我的众筹支持记录
     *
     * @return 支持记录列表
     */
    List<CrowdfundingSupportVO> getMySupports();

    /**
     * 获取我的样品发货记录（包含快递单号）
     *
     * @return 样品发货记录列表
     */
    List<SampleDeliveryListVO> getMySampleDeliveries();

    /**
     * 获取我的抽奖记录
     *
     * @return 抽奖记录列表
     */
    List<CrowdfundingDrawVO> getMyDraws();

    /**
     * 执行指定众筹项目的抽奖逻辑
     *
     * @param projectId 众筹项目ID
     */
    void runDrawForProject(Long projectId);


    /**
     * 根据订单号查询支持记录
     *
     * @param orderNo 订单号
     * @return 支持记录
     */
    SdCrowdfundingSupport getSupportByOrderNo(String orderNo);

    /**
     * 创建众筹支持订单
     *
     * @param supportDto 支持DTO
     * @return 订单号
     */
    String createSupportOrder(CrowdfundingSupportDto supportDto);




    /**
     * 获取厂家参与的众筹项目列表
     *
     * @return 厂家参与的众筹项目列表
     */
    List<CrowdfundingProjectListVO> getManufacturerProjects();

    /**
     * 获取厂家参与的众筹成功项目列表
     *
     * @return 厂家参与的众筹成功项目列表
     */
    List<CrowdfundingProjectListVO> getManufacturerSuccessfulProjects();

    /**
     * 释放众筹资金给商家（审核通过后调用）
     *
     * @param projectId 众筹项目ID
     * @return 转账订单号
     */
    String releaseCrowdfundingFunds(Long projectId);

    /**
     * 审核资金释放申请
     *
     * @param projectId 众筹项目ID
     * @param auditStatus 审核状态：2=审核通过，3=审核拒绝
     * @param auditRemark 审核备注
     * @return 是否成功
     */
    boolean auditFundRelease(Long projectId, Integer auditStatus, String auditRemark);

    /**
     * 分页查询待审核资金释放申请列表（状态=成功，托管中，审核待审）
     * @param pageQuery 分页查询
     * @return 分页数据
     */
    TableDataInfo<SdCrowdfundingProject> getPendingFundReleasePage(PageQuery pageQuery);
}
