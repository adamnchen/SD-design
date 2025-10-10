package com.sutran.sd.design.service;

import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.dto.CrowdfundingDrawClaimDTO;
import com.sutran.sd.design.vo.CrowdfundingProjectDetailVO;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.CrowdfundingSupportVO;
import com.sutran.sd.design.vo.CrowdfundingDrawVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
     * 查询众筹项目列表
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 众筹项目集合
     */
    List<SdCrowdfundingProject> selectSdCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 新增众筹项目
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 结果
     */
    int insertSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 修改众筹项目
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 结果
     */
    int updateSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 批量删除众筹项目
     *
     * @param ids 需要删除的众筹项目主键集合
     * @return 结果
     */
    int deleteSdCrowdfundingProjectByIds(Long[] ids);

    /**
     * 删除众筹项目信息
     *
     * @param id 众筹项目主键
     * @return 结果
     */
    int deleteSdCrowdfundingProjectById(Long id);

    /**
     * 从打样邀约创建众筹项目
     *
     * @param proofingInvitationId 打样邀约ID
     * @return 结果
     */
    CrowdfundingProjectDetailVO createFromProofingInvitation(Long proofingInvitationId);

    /**
     * 获取众筹项目列表
     *
     * @return 众筹项目列表
     */
    List<CrowdfundingProjectListVO> getCrowdfundingProjectList();

    /**
     * 获取众筹项目详情
     *
     * @param id 项目ID
     * @return 众筹项目详情
     */
    CrowdfundingProjectDetailVO getCrowdfundingProjectDetail(Long id);

    /**
     * 参与众筹
     *
     * @param supportDTO 支持DTO
     * @return 支持记录
     */
    CrowdfundingSupportVO supportProject(CrowdfundingSupportDTO supportDTO);

    /**
     * 获取我的众筹支持记录
     *
     * @return 支持记录列表
     */
    List<CrowdfundingSupportVO> getMySupports();

    /**
     * 获取我的抽奖记录
     *
     * @return 抽奖记录列表
     */
    List<CrowdfundingDrawVO> getMyDraws();

    /**
     * 领取奖品
     *
     * @param claimDTO 领取DTO
     * @return 结果
     */
    boolean claimPrize(CrowdfundingDrawClaimDTO claimDTO);

    /**
     * 自动检查并更新众筹项目状态
     * 检查截止时间，自动将未达到目标金额的项目标记为失败
     */
    void autoUpdateProjectStatus();

    /**
     * 开始抽奖
     *
     * @param projectId 众筹项目ID
     * @return 结果
     */
    boolean startDraw(Long projectId);

    /**
     * 执行抽奖
     *
     * @param projectId 众筹项目ID
     * @return 结果
     */
    boolean executeDraw(Long projectId);

    /**
     * 厂家上传实物照片
     *
     * @param projectId 项目ID
     * @param photos 照片URL列表（JSON格式）
     * @return 结果
     */
    boolean uploadManufacturerPhotos(Long projectId, String photos);

    /**
     * 释放资金给厂家
     *
     * @param projectId 项目ID
     * @return 结果
     */
    boolean releaseFundsToManufacturer(Long projectId);

    /**
     * 众筹失败时自动退款
     *
     * @param projectId 项目ID
     * @return 结果
     */
    boolean refundAllSupports(Long projectId);

    /**
     * 手动触发退款
     *
     * @param projectId 项目ID
     * @return 结果
     */
    boolean manualRefund(Long projectId);

    /**
     * 从支付订单创建众筹支持记录
     *
     * @param payOrder 支付订单
     * @return 结果
     */
    boolean createSupportFromPayment(Object payOrder);

    /**
     * 创建众筹支持支付订单
     *
     * @param projectId 项目ID
     * @param supportAmount 支持金额
     * @param message 支持留言
     * @param isAnonymous 是否匿名
     * @return 支付订单号
     */
    String createSupportPaymentOrder(Long projectId, BigDecimal supportAmount, String message, Boolean isAnonymous);

    /**
     * 初始化众筹项目Redis缓存
     *
     * @param projectId 项目ID
     * @param targetAmount 目标金额
     * @return 是否成功
     */
    boolean initProjectRedisCache(Long projectId, BigDecimal targetAmount);

    /**
     * 获取项目参与状态信息
     *
     * @param projectId 项目ID
     * @return 参与状态信息
     */
    Map<String, Object> getProjectParticipationStatus(Long projectId);

    /**
     * 根据支持订单号查询支持记录
     *
     * @param supportNo 支持订单号
     * @return 支持记录
     */
    SdCrowdfundingSupport getSupportBySupportNo(String supportNo);
}
