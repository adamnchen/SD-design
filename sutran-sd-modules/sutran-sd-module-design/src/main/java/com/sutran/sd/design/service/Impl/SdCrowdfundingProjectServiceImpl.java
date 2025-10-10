package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.dto.CrowdfundingDrawClaimDTO;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import com.sutran.sd.design.service.OrderReservationService;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.system.service.ISysUserService;
import com.sutran.sd.common.core.domain.entity.SysUser;
import org.springframework.data.redis.core.RedisTemplate;
import com.sutran.sd.design.vo.CrowdfundingProjectDetailVO;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.CrowdfundingSupportVO;
import com.sutran.sd.design.vo.CrowdfundingDrawVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 众筹项目Service业务层处理
 *
 * @author sutran
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdCrowdfundingProjectServiceImpl extends ServiceImpl<SdCrowdfundingProjectMapper, SdCrowdfundingProject> implements ISdCrowdfundingProjectService {

    private final SdCrowdfundingProjectMapper crowdfundingProjectMapper;
    private final SdCrowdfundingSupportMapper supportMapper;
    private final SdProofingInvitationMapper proofingInvitationMapper;
    private final ISysUserService sysUserService;
    private final OrderReservationService orderReservationService;
    private final CrowdfundingRedisService crowdfundingRedisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AliPayService aliPayService;

    @Override
    public SdCrowdfundingProject selectSdCrowdfundingProjectById(Long id) {
        return crowdfundingProjectMapper.selectSdCrowdfundingProjectById(id);
    }

    @Override
    public List<SdCrowdfundingProject> selectSdCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject) {
        return crowdfundingProjectMapper.selectSdCrowdfundingProjectList(sdCrowdfundingProject);
    }

    @Override
    public int insertSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject) {
        return crowdfundingProjectMapper.insertSdCrowdfundingProject(sdCrowdfundingProject);
    }

    @Override
    public int updateSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject) {
        return crowdfundingProjectMapper.updateSdCrowdfundingProject(sdCrowdfundingProject);
    }

    @Override
    public int deleteSdCrowdfundingProjectByIds(Long[] ids) {
        return crowdfundingProjectMapper.deleteSdCrowdfundingProjectByIds(ids);
    }

    @Override
    public int deleteSdCrowdfundingProjectById(Long id) {
        return crowdfundingProjectMapper.deleteSdCrowdfundingProjectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrowdfundingProjectDetailVO createFromProofingInvitation(Long proofingInvitationId) {
        // 1. 查询打样邀约信息
        SdProofingInvitation invitation = proofingInvitationMapper.selectById(proofingInvitationId);
        if (invitation == null) {
            throw new RuntimeException("打样邀约不存在");
        }
        if (!invitation.getStatus().equals(1)) {
            throw new RuntimeException("只有已接受的打样邀约才能创建众筹项目");
        }
        if (invitation.getQuotedPrice() == null || invitation.getQuotedPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("打样邀约报价无效，无法创建众筹项目");
        }

        // 2. 检查是否已经创建过众筹项目
        LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingProject::getProofingInvitationId, proofingInvitationId);
        SdCrowdfundingProject existingProject = this.getOne(queryWrapper);
        if (existingProject != null) {
            throw new RuntimeException("该打样邀约已经创建过众筹项目");
        }

        // 3. 创建众筹项目
        SdCrowdfundingProject project = new SdCrowdfundingProject();
        project.setProjectNo("CF" + System.currentTimeMillis());
        project.setProofingInvitationId(proofingInvitationId);
        project.setTitle(invitation.getProductTitle());
        project.setDescription(invitation.getProductDescription());
        project.setCreatorUserId(invitation.getInviterUserId());
        project.setCreatorName(getUserNameById(invitation.getInviterUserId()));
        project.setManufacturerUserId(invitation.getSelectedInviteeUserId());
        project.setManufacturerName(getUserNameById(invitation.getSelectedInviteeUserId()));
        project.setTargetAmount(invitation.getQuotedPrice()); // 使用厂家报价作为目标金额
        project.setCurrentAmount(BigDecimal.ZERO);
        project.setSupportCount(0);
        project.setViewCount(0);
        project.setStartTime(new Date());
        project.setEndTime(new Date(System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000)); // 60天倒计时
        project.setStatus(1); // 众筹中
        project.setDrawNumber(invitation.getDrawNumber()); // 从邀约继承抽奖数量
        project.setDrawStatus(0); // 未开始
        project.setEscrowStatus(0); // 资金托管中

        this.save(project);
        log.info("从打样邀约创建众筹项目成功: 项目编号={}, 目标金额={}", project.getProjectNo(), project.getTargetAmount());
        return convertToDetailVO(project);
    }

    @Override
    public List<CrowdfundingProjectListVO> getCrowdfundingProjectList() {
        List<SdCrowdfundingProject> projects = this.list();
        return projects.stream().map(this::convertToListVO).collect(Collectors.toList());
    }

    @Override
    public CrowdfundingProjectDetailVO getCrowdfundingProjectDetail(Long id) {
        SdCrowdfundingProject project = this.getById(id);
        if (project == null) {
            return null;
        }
        return convertToDetailVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrowdfundingSupportVO supportProject(CrowdfundingSupportDTO supportDTO) {
        // 验证项目状态
        SdCrowdfundingProject project = this.getById(supportDTO.getProjectId());
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }
        if (!project.getStatus().equals(1)) {
            throw new RuntimeException("项目不在众筹中，无法参与");
        }
        if (project.getEndTime().before(new Date())) {
            throw new RuntimeException("众筹已结束，无法参与");
        }

        // 检查捐助金额不能超过剩余目标金额
        BigDecimal remainingAmount = project.getTargetAmount().subtract(project.getCurrentAmount());
        if (supportDTO.getSupportAmount().compareTo(remainingAmount) > 0) {
            throw new RuntimeException("捐助金额不能超过剩余目标金额，剩余金额: " + remainingAmount + " 元");
        }

        // 创建支持记录
        SdCrowdfundingSupport support = new SdCrowdfundingSupport();
        support.setSupportNo("SP" + System.currentTimeMillis());
        support.setProjectId(supportDTO.getProjectId());
        support.setUserId(1L); // TODO: 从当前登录用户获取
        support.setUserName(getUserNameById(1L));
        support.setSupportAmount(supportDTO.getSupportAmount());
        support.setMessage(supportDTO.getMessage());
        support.setIsAnonymous(supportDTO.getIsAnonymous() ? 1 : 0);
        support.setStatus(0); // 正常
        support.setPaymentStatus(1); // 已支付（简化处理）
        support.setPaymentTime(new Date());
        support.setCreateTime(new Date());
        support.setUpdateTime(new Date());

        // 保存支持记录
        supportMapper.insert(support);

        // 更新项目统计
        project.setCurrentAmount(project.getCurrentAmount().add(supportDTO.getSupportAmount()));
        project.setSupportCount(project.getSupportCount() + 1);
        this.updateById(project);

        log.info("用户参与众筹成功: 项目={}, 金额={}", supportDTO.getProjectId(), supportDTO.getSupportAmount());

        // 返回支持记录
        CrowdfundingSupportVO supportVO = new CrowdfundingSupportVO();
        BeanUtils.copyProperties(support, supportVO);
        supportVO.setProjectTitle(project.getTitle());
        supportVO.setPaymentStatusDesc(getPaymentStatusDesc(support.getPaymentStatus()));
        supportVO.setStatusDesc(getSupportStatusDesc(support.getStatus()));

        return supportVO;
    }

    @Override
    public List<CrowdfundingSupportVO> getMySupports() {
        // TODO: 从当前登录用户获取用户ID
        Long currentUserId = 1L;

        // 查询当前用户的支持记录
        LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingSupport::getUserId, currentUserId)
                   .orderByDesc(SdCrowdfundingSupport::getCreateTime);
        List<SdCrowdfundingSupport> supports = supportMapper.selectList(queryWrapper);

        return supports.stream().map(this::convertToSupportVO).collect(Collectors.toList());
    }

    @Override
    public List<CrowdfundingDrawVO> getMyDraws() {
        // TODO: 从当前登录用户获取用户ID，实现抽奖记录查询
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimPrize(CrowdfundingDrawClaimDTO claimDTO) {
        // 简化处理，直接返回成功
        log.info("用户领取奖品: {}", claimDTO);
        return true;
    }

    /**
     * 转换为列表VO
     */
    private CrowdfundingProjectListVO convertToListVO(SdCrowdfundingProject project) {
        CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
        BeanUtils.copyProperties(project, vo);

        // 计算进度百分比
        if (project.getTargetAmount() != null && project.getCurrentAmount() != null) {
            BigDecimal progress = project.getCurrentAmount()
                .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
            vo.setProgressPercentage(progress);
        }

        // 计算剩余天数
        if (project.getEndTime() != null) {
            long remainingDays = (project.getEndTime().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
            vo.setRemainingDays(Math.max(0, remainingDays));
        }

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(project.getStatus()));
        vo.setDrawStatusDesc(getDrawStatusDesc(project.getDrawStatus()));

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private CrowdfundingProjectDetailVO convertToDetailVO(SdCrowdfundingProject project) {
        CrowdfundingProjectDetailVO vo = new CrowdfundingProjectDetailVO();
        BeanUtils.copyProperties(project, vo);

        // 计算进度百分比
        if (project.getTargetAmount() != null && project.getCurrentAmount() != null) {
            BigDecimal progress = project.getCurrentAmount()
                .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
            vo.setProgressPercentage(progress);
        }

        // 计算剩余天数
        if (project.getEndTime() != null) {
            long remainingDays = (project.getEndTime().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
            vo.setRemainingDays(Math.max(0, remainingDays));
        }

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(project.getStatus()));
        vo.setDrawStatusDesc(getDrawStatusDesc(project.getDrawStatus()));

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "众筹中";
            case 2: return "众筹成功";
            case 3: return "众筹失败";
            default: return "未知";
        }
    }

    /**
     * 获取抽奖状态描述
     */
    private String getDrawStatusDesc(Integer drawStatus) {
        if (drawStatus == null) return "未知";
        switch (drawStatus) {
            case 0: return "未开始";
            case 1: return "进行中";
            case 2: return "已结束";
            default: return "未知";
        }
    }

    /**
     * 获取支付状态描述
     */
    private String getPaymentStatusDesc(Integer paymentStatus) {
        if (paymentStatus == null) return "未知";
        switch (paymentStatus) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "支付失败";
            case 3: return "已退款";
            default: return "未知";
        }
    }

    /**
     * 获取支持状态描述
     */
    private String getSupportStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "正常";
            case 1: return "已取消";
            case 2: return "已退款";
            default: return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateProjectStatus() {
        log.info("开始自动检查众筹项目状态");
        Date now = new Date();

        // 1. 检查未过期但已达到目标金额的项目，立即标记为成功
        LambdaQueryWrapper<SdCrowdfundingProject> successQuery = new LambdaQueryWrapper<>();
        successQuery.eq(SdCrowdfundingProject::getStatus, 1) // 众筹中
                    .gt(SdCrowdfundingProject::getEndTime, now); // 未过期
        List<SdCrowdfundingProject> activeProjects = this.list(successQuery);
        if (!activeProjects.isEmpty()) {
            for (SdCrowdfundingProject project : activeProjects) {
                // 检查是否达到目标金额
                if (project.getCurrentAmount().compareTo(project.getTargetAmount()) >= 0) {
                    project.setStatus(2); // 众筹成功
                    this.updateById(project);
                    log.info("众筹项目 {} 达到目标金额自动标记为成功，当前金额: {}, 目标金额: {}",
                        project.getProjectNo(), project.getCurrentAmount(), project.getTargetAmount());
                }
            }
        }

        // 2. 检查已到期的众筹项目，根据是否达到目标金额判断成功或失败
        LambdaQueryWrapper<SdCrowdfundingProject> expiredQuery = new LambdaQueryWrapper<>();
        expiredQuery.eq(SdCrowdfundingProject::getStatus, 1) // 众筹中
                   .le(SdCrowdfundingProject::getEndTime, now); // 截止时间已到
        List<SdCrowdfundingProject> expiredProjects = this.list(expiredQuery);

        if (!expiredProjects.isEmpty()) {
            for (SdCrowdfundingProject project : expiredProjects) {
                // 检查是否达到目标金额
                if (project.getCurrentAmount().compareTo(project.getTargetAmount()) < 0) {
                    // 未达到目标金额，标记为失败并自动退款
                    project.setStatus(3); // 众筹失败
                    this.updateById(project);

                    // 自动退款给所有支持用户
                    try {
                        refundAllSupports(project.getId());
                        log.info("众筹项目 {} 因60天到期未达到目标金额自动标记为失败，并完成自动退款", project.getProjectNo());
                    } catch (Exception e) {
                        log.error("众筹项目 {} 自动退款失败: {}", project.getProjectNo(), e.getMessage());
                    }
                } else {
                    // 达到目标金额，标记为成功
                    project.setStatus(2); // 众筹成功
                    this.updateById(project);
                    log.info("众筹项目 {} 在60天到期时达到目标金额自动标记为成功，当前金额: {}, 目标金额: {}",
                        project.getProjectNo(), project.getCurrentAmount(), project.getTargetAmount());
                }
            }
        }

        log.info("众筹项目状态自动检查完成，活跃项目: {}, 到期项目: {}", activeProjects.size(), expiredProjects.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startDraw(Long projectId) {
        SdCrowdfundingProject project = this.getById(projectId);
        if (project == null) {
            log.error("众筹项目不存在: {}", projectId);
            return false;
        }

        // 只有众筹成功的项目才能开始抽奖
        if (!project.getStatus().equals(2)) {
            log.error("只有众筹成功的项目才能开始抽奖: {}", projectId);
            return false;
        }

        // 更新抽奖状态
        project.setDrawStatus(1); // 进行中
        project.setDrawTime(new Date());
        this.updateById(project);

        log.info("众筹项目 {} 开始抽奖", project.getProjectNo());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeDraw(Long projectId) {
        // 验证项目状态
        SdCrowdfundingProject project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }
        if (!project.getStatus().equals(2)) {
            throw new RuntimeException("只有众筹成功的项目才能执行抽奖");
        }
        if (!project.getDrawStatus().equals(1)) {
            throw new RuntimeException("抽奖未开始，无法执行");
        }

        // 更新项目抽奖状态为已结束
        project.setDrawStatus(2); // 已结束
        project.setDrawTime(new Date());
        this.updateById(project);

        log.info("众筹项目 {} 抽奖执行成功", projectId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uploadManufacturerPhotos(Long projectId, String photos) {
        SdCrowdfundingProject project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }
        if (!project.getStatus().equals(2)) {
            throw new RuntimeException("只有众筹成功的项目才能上传实物照片");
        }
        if (project.getEscrowStatus() != 0) {
            throw new RuntimeException("资金已处理，无法上传照片");
        }

        // 更新厂家照片信息
        project.setManufacturerPhotos(photos);
        project.setManufacturerUploadTime(new Date());
        this.updateById(project);

        log.info("厂家上传实物照片成功: 项目={}, 照片数量={}", projectId, photos);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseFundsToManufacturer(Long projectId) {
        SdCrowdfundingProject project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }
        if (!project.getStatus().equals(2)) {
            throw new RuntimeException("只有众筹成功的项目才能释放资金");
        }
        if (project.getEscrowStatus() != 0) {
            throw new RuntimeException("资金已处理，无法重复释放");
        }

        // 更新资金托管状态
        project.setEscrowStatus(1); // 已释放给厂家
        project.setFundReleaseTime(new Date());
        this.updateById(project);

        log.info("资金释放给厂家成功: 项目={}, 金额={}", projectId, project.getCurrentAmount());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundAllSupports(Long projectId) {
        SdCrowdfundingProject project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }
        if (!project.getStatus().equals(3)) {
            throw new RuntimeException("只有众筹失败的项目才能退款");
        }

        // 查询所有已支付的支持记录
        List<SdCrowdfundingSupport> supports = supportMapper.selectPaidByProjectId(projectId);
        if (supports.isEmpty()) {
            log.info("项目 {} 没有需要退款的支持记录", projectId);
            return true;
        }

        Date now = new Date();
        String refundReason = "众筹失败，自动退款";
        int refundCount = 0;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        for (SdCrowdfundingSupport support : supports) {
            // 更新支持记录状态为已退款
            support.setStatus(2); // 已退款
            support.setPaymentStatus(3); // 已退款
            support.setRefundAmount(support.getSupportAmount()); // 全额退款
            support.setRefundTime(now);
            support.setRefundReason(refundReason);
            support.setUpdateTime(now);

            supportMapper.updateById(support);
            refundCount++;
            totalRefundAmount = totalRefundAmount.add(support.getSupportAmount());

            log.info("用户 {} 的 {} 元支持已退款", support.getUserId(), support.getSupportAmount());
        }

        // 更新项目资金托管状态为已退款
        project.setEscrowStatus(2); // 已退款
        project.setUpdateTime(now);
        this.updateById(project);

        log.info("众筹项目 {} 退款完成，共退款 {} 笔，总金额: {}",
            project.getProjectNo(), refundCount, totalRefundAmount);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualRefund(Long projectId) {
        SdCrowdfundingProject project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }
        if (!project.getStatus().equals(3)) {
            throw new RuntimeException("只有众筹失败的项目才能手动退款");
        }
        if (project.getEscrowStatus() == 2) {
            throw new RuntimeException("该项目已经完成退款");
        }

        return refundAllSupports(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSupportFromPayment(Object payOrder) {
        try {
            // 使用反射获取支付订单属性，避免直接依赖pay模块
            Long projectId = getFieldValue(payOrder, "businessId", Long.class);
            Long userId = getFieldValue(payOrder, "userId", Long.class);
            String userName = getFieldValue(payOrder, "userName", String.class);
            BigDecimal supportAmount = getFieldValue(payOrder, "totalAmount", BigDecimal.class);
            String outTradeNo = getFieldValue(payOrder, "outTradeNo", String.class);
            
            if (projectId == null || userId == null || supportAmount == null) {
                log.error("支付订单信息不完整，无法创建众筹支持记录");
                return false;
            }
            
            // 1. 更新支持记录状态
            SdCrowdfundingSupport support = supportMapper.selectBySupportNo(outTradeNo);
            if (support == null) {
                log.error("支持记录不存在: {}", outTradeNo);
                return false;
            }
            
            support.setPaymentStatus(1); // 已支付
            support.setPaymentTime(new Date());
            support.setPaymentNo(outTradeNo);
            support.setUpdateTime(new Date());
            supportMapper.updateById(support);
            
            // 2. 清理Redis中的订单缓存
            String orderKey = "crowdfunding:order:" + outTradeNo;
            String timeoutKey = "crowdfunding:timeout:" + outTradeNo;
            redisTemplate.delete(orderKey);
            redisTemplate.delete(timeoutKey);
            
            // 3. 检查项目是否真正完成（基于实际支付金额）
            checkAndUpdateProjectStatus(projectId);
            
            log.info("从支付订单创建众筹支持记录成功: 项目={}, 用户={}, 金额={}, 剩余金额={}", 
                projectId, userId, supportAmount, crowdfundingRedisService.getRemainingAmount(projectId));
            return true;
            
        } catch (Exception e) {
            log.error("从支付订单创建众筹支持记录失败", e);
            return false;
        }
    }

    @Override
    public String createSupportPaymentOrder(Long projectId, BigDecimal supportAmount, String message, Boolean isAnonymous) {
        try {
            // 1. 验证项目状态
            SdCrowdfundingProject project = this.getById(projectId);
            if (project == null) {
                throw new RuntimeException("众筹项目不存在");
            }
            if (!project.getStatus().equals(1)) {
                throw new RuntimeException("项目不在众筹中，无法参与");
            }
            if (project.getEndTime().before(new Date())) {
                throw new RuntimeException("众筹已结束，无法参与");
            }
            
            // 2. 检查Redis中是否还有剩余金额
            if (!crowdfundingRedisService.tryDeductAmount(projectId, supportAmount)) {
                // 检查是否有未支付订单可能释放金额
                if (hasPendingOrders(projectId)) {
                    throw new RuntimeException("众筹金额暂时不足，请稍后再试（可能有订单即将释放金额）");
                } else {
                    throw new RuntimeException("众筹金额不足，无法创建订单");
                }
            }
            
            // 3. 生成支付订单号
            String outTradeNo = "CF" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            
            // 4. 创建支持记录（待支付状态）
            SdCrowdfundingSupport support = new SdCrowdfundingSupport();
            support.setSupportNo(outTradeNo);
            support.setProjectId(projectId);
            support.setUserId(1L); // TODO: 从当前登录用户获取
            support.setUserName(getUserNameById(1L)); // TODO: 从当前登录用户获取
            support.setSupportAmount(supportAmount);
            support.setMessage(message);
            support.setIsAnonymous(isAnonymous ? 1 : 0);
            support.setStatus(0); // 正常
            support.setPaymentStatus(0); // 待支付
            support.setCreateTime(new Date());
            support.setUpdateTime(new Date());
            
            // 保存支持记录
            supportMapper.insert(support);
            
            // 5. 将订单放入Redis，设置超时时间（20分钟）
            String orderKey = "crowdfunding:order:" + outTradeNo;
            String timeoutKey = "crowdfunding:timeout:" + outTradeNo;
            redisTemplate.opsForValue().set(orderKey, support, 20, java.util.concurrent.TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(timeoutKey, "1", 20, java.util.concurrent.TimeUnit.MINUTES);
            
            log.info("创建众筹支持支付订单成功: 项目={}, 金额={}, 订单号={}, 剩余金额={}", 
                projectId, supportAmount, outTradeNo, crowdfundingRedisService.getRemainingAmount(projectId));
            return outTradeNo;
            
        } catch (Exception e) {
            log.error("创建众筹支持支付订单失败", e);
            throw new RuntimeException("创建支付订单失败: " + e.getMessage());
        }
    }

    @Override
    public boolean initProjectRedisCache(Long projectId, BigDecimal targetAmount) {
        try {
            return crowdfundingRedisService.initProjectAmount(projectId, targetAmount);
        } catch (Exception e) {
            log.error("初始化众筹项目Redis缓存失败: 项目={}, 金额={}", projectId, targetAmount, e);
            return false;
        }
    }

    /**
     * 检查并更新项目状态（基于实际支付金额）
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateProjectStatus(Long projectId) {
        try {
            // 1. 查询项目信息
            SdCrowdfundingProject project = this.getById(projectId);
            if (project == null) {
                log.error("项目不存在: {}", projectId);
                return;
            }
            
            // 只有众筹中的项目才需要检查
            if (!project.getStatus().equals(1)) {
                return;
            }
            
            // 2. 计算实际已支付金额
            BigDecimal actualPaidAmount = calculateActualPaidAmount(projectId);
            BigDecimal targetAmount = project.getTargetAmount();
            
            log.info("检查众筹状态: 项目={}, 实际支付={}, 目标金额={}", projectId, actualPaidAmount, targetAmount);
            
            // 3. 检查是否达到目标金额
            if (actualPaidAmount.compareTo(targetAmount) >= 0) {
                // 使用乐观锁更新状态，防止重复处理
                int updateResult = crowdfundingProjectMapper.updateStatusIfCrowdfunding(projectId, 2); // 众筹成功
                if (updateResult > 0) {
                    // 更新项目实际金额
                    project.setCurrentAmount(actualPaidAmount);
                    project.setSupportCount(getPaidSupportCount(projectId));
                    this.updateById(project);
                    
                    // 释放资金给厂家
                    releaseFundsToManufacturer(projectId);
                    
                    // 清理Redis缓存
                    crowdfundingRedisService.clearProjectCache(projectId);
                    
                    log.info("众筹项目成功: 项目={}, 实际支付={}, 目标金额={}", 
                        projectId, actualPaidAmount, targetAmount);
                } else {
                    log.info("项目状态已被其他线程更新: {}", projectId);
                }
            }
        } catch (Exception e) {
            log.error("检查并更新项目状态失败: 项目={}", projectId, e);
        }
    }

    /**
     * 计算实际已支付金额
     */
    private BigDecimal calculateActualPaidAmount(Long projectId) {
        try {
            List<SdCrowdfundingSupport> paidSupports = supportMapper.selectPaidByProjectId(projectId);
            if (paidSupports == null || paidSupports.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            return paidSupports.stream()
                .map(SdCrowdfundingSupport::getSupportAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("计算实际已支付金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取已支付支持数量
     */
    private Integer getPaidSupportCount(Long projectId) {
        try {
            List<SdCrowdfundingSupport> paidSupports = supportMapper.selectPaidByProjectId(projectId);
            return paidSupports != null ? paidSupports.size() : 0;
        } catch (Exception e) {
            log.error("获取已支付支持数量失败: 项目={}", projectId, e);
            return 0;
        }
    }

    /**
     * 检查是否有未支付的订单
     */
    private boolean hasPendingOrders(Long projectId) {
        try {
            // 查询待支付状态的支持记录
            List<SdCrowdfundingSupport> pendingSupports = supportMapper.selectList(
                new LambdaQueryWrapper<SdCrowdfundingSupport>()
                    .eq(SdCrowdfundingSupport::getProjectId, projectId)
                    .eq(SdCrowdfundingSupport::getPaymentStatus, 0) // 待支付
                    .eq(SdCrowdfundingSupport::getStatus, 0) // 正常状态
            );
            
            return pendingSupports != null && !pendingSupports.isEmpty();
        } catch (Exception e) {
            log.error("检查未支付订单失败: 项目={}", projectId, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getProjectParticipationStatus(Long projectId) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 1. 查询项目信息
            SdCrowdfundingProject project = this.getById(projectId);
            if (project == null) {
                status.put("canParticipate", false);
                status.put("message", "项目不存在");
                return status;
            }
            
            // 2. 检查项目状态
            if (!project.getStatus().equals(1)) {
                status.put("canParticipate", false);
                status.put("message", "项目不在众筹中");
                return status;
            }
            
            // 3. 检查是否过期
            if (project.getEndTime().before(new Date())) {
                status.put("canParticipate", false);
                status.put("message", "众筹已结束");
                return status;
            }
            
            // 4. 检查Redis剩余金额
            BigDecimal remainingAmount = crowdfundingRedisService.getRemainingAmount(projectId);
            boolean hasRemainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0;
            
            // 5. 检查是否有未支付订单
            boolean hasPendingOrders = hasPendingOrders(projectId);
            
            // 6. 计算实际已支付金额
            BigDecimal actualPaidAmount = calculateActualPaidAmount(projectId);
            boolean isCompleted = actualPaidAmount.compareTo(project.getTargetAmount()) >= 0;
            
            // 7. 判断参与状态
            if (isCompleted) {
                status.put("canParticipate", false);
                status.put("message", "众筹已完成");
                status.put("status", "completed");
            } else if (hasRemainingAmount) {
                status.put("canParticipate", true);
                status.put("message", "可以参与众筹");
                status.put("status", "available");
                status.put("remainingAmount", remainingAmount);
            } else if (hasPendingOrders) {
                status.put("canParticipate", false);
                status.put("message", "众筹金额暂时不足，请稍后再试（可能有订单即将释放金额）");
                status.put("status", "pending");
                status.put("remainingAmount", BigDecimal.ZERO);
            } else {
                status.put("canParticipate", false);
                status.put("message", "众筹金额不足，无法参与");
                status.put("status", "unavailable");
                status.put("remainingAmount", BigDecimal.ZERO);
            }
            
            // 8. 添加其他信息
            status.put("targetAmount", project.getTargetAmount());
            status.put("actualPaidAmount", actualPaidAmount);
            status.put("progress", actualPaidAmount.divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
            status.put("hasPendingOrders", hasPendingOrders);
            
        } catch (Exception e) {
            log.error("获取项目参与状态失败: 项目={}", projectId, e);
            status.put("canParticipate", false);
            status.put("message", "获取状态失败");
            status.put("status", "error");
        }
        
        return status;
    }

    @Override
    public SdCrowdfundingSupport getSupportBySupportNo(String supportNo) {
        try {
            return supportMapper.selectBySupportNo(supportNo);
        } catch (Exception e) {
            log.error("根据支持订单号查询支持记录失败: 订单号={}", supportNo, e);
            return null;
        }
    }

    /**
     * 使用反射获取对象属性值
     */
    private <T> T getFieldValue(Object obj, String fieldName, Class<T> fieldType) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return fieldType.cast(value);
        } catch (Exception e) {
            log.warn("获取字段值失败: {}.{}", obj.getClass().getSimpleName(), fieldName);
            return null;
        }
    }

    /**
     * 根据用户ID获取用户姓名
     *
     * @param userId 用户ID
     * @return 用户姓名
     */
    private String getUserNameById(Long userId) {
        if (userId == null) {
            return "未知用户";
        }
        try {
            SysUser user = sysUserService.selectUserById(userId);
            if (user != null && user.getNickName() != null && !user.getNickName().trim().isEmpty()) {
                return user.getNickName();
            } else if (user != null && user.getUserName() != null && !user.getUserName().trim().isEmpty()) {
                return user.getUserName();
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败，用户ID: {}, 错误: {}", userId, e.getMessage());
        }
        return "用户" + userId;
    }

    /**
     * 转换支持记录为VO
     *
     * @param support 支持记录
     * @return 支持记录VO
     */
    private CrowdfundingSupportVO convertToSupportVO(SdCrowdfundingSupport support) {
        CrowdfundingSupportVO vo = new CrowdfundingSupportVO();
        BeanUtils.copyProperties(support, vo);

        // 获取项目标题
        try {
            SdCrowdfundingProject project = this.getById(support.getProjectId());
            if (project != null) {
                vo.setProjectTitle(project.getTitle());
            }
        } catch (Exception e) {
            log.warn("获取项目信息失败，项目ID: {}", support.getProjectId());
            vo.setProjectTitle("未知项目");
        }

        // 设置状态描述
        vo.setPaymentStatusDesc(getPaymentStatusDesc(support.getPaymentStatus()));
        vo.setStatusDesc(getSupportStatusDesc(support.getStatus()));

        return vo;
    }

    /**
     * 检查并更新项目状态，处理超募退款（原子操作）
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateProjectStatusWithRefund(Long projectId) {
        try {
            // 使用数据库行锁确保原子性
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
            if (project == null) {
                log.error("项目不存在: {}", projectId);
                return;
            }
            
            // 只有众筹中的项目才需要检查
            if (!project.getStatus().equals(1)) {
                return;
            }
            
            // 1. 重新计算所有已支付订单的总金额
            BigDecimal totalPaidAmount = calculateTotalPaidAmount(projectId);
            BigDecimal targetAmount = project.getTargetAmount();
            
            log.info("检查众筹状态: 项目={}, 已支付总额={}, 目标金额={}", projectId, totalPaidAmount, targetAmount);
            
            // 2. 检查是否达到目标金额
            if (totalPaidAmount.compareTo(targetAmount) >= 0) {
                // 使用乐观锁更新状态，防止重复处理
                int updateResult = crowdfundingProjectMapper.updateStatusIfCrowdfunding(projectId, 2); // 众筹成功
                if (updateResult > 0) {
                    // 处理超募退款
                    processOverfundingRefund(projectId, project, totalPaidAmount);
                    
                    // 释放资金给厂家
                    releaseFundsToManufacturer(projectId);
                    log.info("众筹项目达到目标金额，立即成功: 项目={}, 已支付总额={}, 目标金额={}", 
                        projectId, totalPaidAmount, targetAmount);
                } else {
                    log.info("项目状态已被其他线程更新: {}", projectId);
                }
            }
        } catch (Exception e) {
            log.error("检查并更新项目状态失败: 项目={}", projectId, e);
        }
    }

    /**
     * 计算所有已支付订单的总金额
     */
    private BigDecimal calculateTotalPaidAmount(Long projectId) {
        try {
            List<SdCrowdfundingSupport> paidSupports = supportMapper.selectPaidByProjectId(projectId);
            if (paidSupports == null || paidSupports.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            return paidSupports.stream()
                .map(SdCrowdfundingSupport::getSupportAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("计算已支付总金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 处理超募退款
     */
    @Transactional(rollbackFor = Exception.class)
    private void processOverfundingRefund(Long projectId, SdCrowdfundingProject project, BigDecimal totalPaidAmount) {
        try {
            // 1. 获取所有已支付的支持记录，按创建时间升序排序
            List<SdCrowdfundingSupport> paidSupports = supportMapper.selectPaidByProjectId(projectId);
            if (paidSupports == null || paidSupports.isEmpty()) {
                return;
            }
            
            // 按创建时间升序排序（最早的支持记录在前）
            paidSupports.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
            
            // 2. 从前往后累加，直到大于等于众筹金额
            BigDecimal targetAmount = project.getTargetAmount();
            BigDecimal accumulatedAmount = BigDecimal.ZERO;
            List<SdCrowdfundingSupport> validSupports = new ArrayList<>();
            SdCrowdfundingSupport lastSupport = null;
            
            for (SdCrowdfundingSupport support : paidSupports) {
                if (accumulatedAmount.compareTo(targetAmount) < 0) {
                    validSupports.add(support);
                    accumulatedAmount = accumulatedAmount.add(support.getSupportAmount());
                    lastSupport = support;
                } else {
                    break;
                }
            }
            
            // 3. 计算最后一个人的部分退款金额
            BigDecimal refundAmount = BigDecimal.ZERO;
            if (lastSupport != null && accumulatedAmount.compareTo(targetAmount) > 0) {
                // 最后一个人的总金额 - 众筹金额 = 需要退回的金额
                refundAmount = accumulatedAmount.subtract(targetAmount);
                
                // 更新最后一个人的支持金额（保留部分）
                BigDecimal keepAmount = lastSupport.getSupportAmount().subtract(refundAmount);
                lastSupport.setSupportAmount(keepAmount);
                lastSupport.setRefundAmount(refundAmount);
                lastSupport.setRefundReason("超募部分退款");
                lastSupport.setRefundTime(new Date());
                lastSupport.setStatus(2); // 已退款
                supportMapper.updateById(lastSupport);
                
                log.info("最后一个人部分退款: 用户={}, 原金额={}, 保留={}, 退款={}", 
                    lastSupport.getUserId(), lastSupport.getSupportAmount().add(refundAmount), keepAmount, refundAmount);
            }
            
            // 4. 处理需要全额退款的记录（排在最后一个人之后的所有记录）
            List<SdCrowdfundingSupport> refundSupports = new ArrayList<>();
            for (SdCrowdfundingSupport support : paidSupports) {
                if (!validSupports.contains(support)) {
                    refundSupports.add(support);
                }
            }
            
            // 5. 执行全额退款
            for (SdCrowdfundingSupport support : refundSupports) {
                try {
                    // 调用退款接口
                    boolean refundSuccess = processRefund(support);
                    if (refundSuccess) {
                        support.setStatus(2); // 已退款
                        support.setRefundAmount(support.getSupportAmount());
                        support.setRefundReason("超募全额退款");
                        support.setRefundTime(new Date());
                        supportMapper.updateById(support);
                        
                        log.info("全额退款成功: 用户={}, 金额={}", support.getUserId(), support.getSupportAmount());
                    }
                } catch (Exception e) {
                    log.error("退款失败: 用户={}, 金额={}", support.getUserId(), support.getSupportAmount(), e);
                }
            }
            
            // 6. 更新项目实际金额（只计算有效支持的总和）
            BigDecimal actualAmount = validSupports.stream()
                .map(SdCrowdfundingSupport::getSupportAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            project.setCurrentAmount(actualAmount);
            project.setSupportCount(validSupports.size());
            crowdfundingProjectMapper.updateById(project);
            
            log.info("超募退款处理完成: 项目={}, 总支付={}, 目标金额={}, 有效支持={}, 退款数量={}", 
                projectId, totalPaidAmount, targetAmount, validSupports.size(), refundSupports.size());
                
        } catch (Exception e) {
            log.error("处理超募退款失败: 项目={}", projectId, e);
        }
    }

    /**
     * 处理退款（调用支付宝退款接口）
     */
    private boolean processRefund(SdCrowdfundingSupport support) {
        try {
            // 调用支付宝退款接口
            boolean refundSuccess = aliPayService.refund(support.getSupportNo(), support.getSupportAmount(), "众筹超募退款");
            
            if (refundSuccess) {
                log.info("支付宝退款成功: 订单号={}, 金额={}", support.getSupportNo(), support.getSupportAmount());
                return true;
            } else {
                log.error("支付宝退款失败: 订单号={}, 金额={}", support.getSupportNo(), support.getSupportAmount());
                return false;
            }
        } catch (Exception e) {
            log.error("退款处理失败: 订单号={}, 金额={}", support.getSupportNo(), support.getSupportAmount(), e);
            return false;
        }
    }

    /**
     * 清理过期预占订单（每1分钟执行一次，确保及时释放）
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void cleanExpiredReservations() {
        try {
            orderReservationService.cleanExpiredReservations();
        } catch (Exception e) {
            log.error("清理过期预占订单失败", e);
        }
    }


}
