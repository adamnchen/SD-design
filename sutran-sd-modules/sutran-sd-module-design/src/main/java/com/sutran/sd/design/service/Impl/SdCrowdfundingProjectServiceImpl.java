package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingProjectSimpleCreateDTO;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.vo.CrowdfundingProjectDetailVO;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.CrowdfundingSupportVO;
import com.sutran.sd.design.vo.CrowdfundingDrawVO;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import com.sutran.sd.design.service.CrowdfundingMqService;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.config.CrowdfundingConfig;
import com.sutran.sd.common.utils.OrderNumUtils;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private final CrowdfundingRedisService crowdfundingRedisService;
    private final CrowdfundingMqService crowdfundingMqService;
    private final CrowdfundingConfig crowdfundingConfig;
    private final SdProofingInvitationMapper invitationMapper;
    private final ISysUserService userService;

    @Override
    public SdCrowdfundingProject selectSdCrowdfundingProjectById(Long id) {
        return crowdfundingProjectMapper.selectById(id);
    }

    @Override
    public SdCrowdfundingProject selectByProofingInvitationId(Long proofingInvitationId) {
        LambdaQueryWrapper<SdCrowdfundingProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SdCrowdfundingProject::getProofingInvitationId, proofingInvitationId);
        return crowdfundingProjectMapper.selectOne(lqw);
    }

    @Override
    public List<SdCrowdfundingProject> selectSdCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject) {
        LambdaQueryWrapper<SdCrowdfundingProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(sdCrowdfundingProject.getId() != null, SdCrowdfundingProject::getId, sdCrowdfundingProject.getId())
           .eq(sdCrowdfundingProject.getProjectNo() != null, SdCrowdfundingProject::getProjectNo, sdCrowdfundingProject.getProjectNo())
           .like(sdCrowdfundingProject.getTitle() != null, SdCrowdfundingProject::getTitle, sdCrowdfundingProject.getTitle())
           .eq(sdCrowdfundingProject.getCreatorUserId() != null, SdCrowdfundingProject::getCreatorUserId, sdCrowdfundingProject.getCreatorUserId())
           .eq(sdCrowdfundingProject.getManufacturerUserId() != null, SdCrowdfundingProject::getManufacturerUserId, sdCrowdfundingProject.getManufacturerUserId())
           .eq(sdCrowdfundingProject.getStatus() != null, SdCrowdfundingProject::getStatus, sdCrowdfundingProject.getStatus())
           .ge(sdCrowdfundingProject.getStartTime() != null, SdCrowdfundingProject::getStartTime, sdCrowdfundingProject.getStartTime())
           .le(sdCrowdfundingProject.getEndTime() != null, SdCrowdfundingProject::getEndTime, sdCrowdfundingProject.getEndTime())
           .orderByDesc(SdCrowdfundingProject::getCreateTime);
        return crowdfundingProjectMapper.selectList(lqw);
    }

    @Override
    public TableDataInfo<SdCrowdfundingProject> selectPageCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject, PageQuery pageQuery) {
        Page<SdCrowdfundingProject> page = pageQuery.build();
        LambdaQueryWrapper<SdCrowdfundingProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(sdCrowdfundingProject.getId() != null, SdCrowdfundingProject::getId, sdCrowdfundingProject.getId());
        lqw.like(sdCrowdfundingProject.getTitle() != null, SdCrowdfundingProject::getTitle, sdCrowdfundingProject.getTitle());
        lqw.eq(sdCrowdfundingProject.getStatus() != null, SdCrowdfundingProject::getStatus, sdCrowdfundingProject.getStatus());
        lqw.orderByDesc(SdCrowdfundingProject::getCreateTime);

        Page<SdCrowdfundingProject> result = crowdfundingProjectMapper.selectPage(page, lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<SdCrowdfundingProject> selectPageCrowdfundingProjectListByType(String type, PageQuery pageQuery) {
        Page<SdCrowdfundingProject> page = pageQuery.build();
        Long currentUserId = LoginHelper.getUserId();

        // 验证类型参数
        if (!"published".equals(type) && !"supported".equals(type) && !"manufactured".equals(type)) {
            throw new ServiceException("不支持的类型: " + type);
        }

        // 使用XML中的查询方法
        Page<SdCrowdfundingProject> result = crowdfundingProjectMapper.selectPageCrowdfundingProjectListByType(page, type, currentUserId);
        return TableDataInfo.build(result);
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void insertSdCrowdfundingProjectSimple(CrowdfundingProjectSimpleCreateDTO createDTO) {
        // 1. 通过多表联查获取完整的邀约信息（包括发起人和厂家信息）
        com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO invitationDetail =
            invitationMapper.selectInvitationDetailById(createDTO.getProofingInvitationId());

        if (invitationDetail == null) {
            throw new ServiceException("打样邀约不存在");
        }

        // 2. 验证厂家ID是否匹配
        if (!invitationDetail.getInviteeUserId().equals(createDTO.getManufacturerUserId())) {
            throw new ServiceException("厂家ID与打样邀约中选中的厂家不匹配");
        }

        // 3. 生成项目编号
        String projectNo = "CF" + System.currentTimeMillis();

        // 4. 构建众筹项目对象 - 从多表联查结果中获取所有信息
        SdCrowdfundingProject project = new SdCrowdfundingProject();
        project.setProjectNo(projectNo);

        // 从邀约详情获取项目信息
        project.setTitle(invitationDetail.getProductTitle());
        project.setDescription(invitationDetail.getProductDescription());
        project.setCoverImage(invitationDetail.getImageUrl()); // 从联查结果获取图片

        // 发起人信息（从联查结果获取）
        project.setCreatorUserId(invitationDetail.getInviterUserId());
        project.setCreatorName(invitationDetail.getInviterNickName());
        project.setCreatorAvatar(invitationDetail.getInviterAvatar());
        project.setProofingInvitationId(createDTO.getProofingInvitationId());

        // 厂家信息（从联查结果获取）
        project.setManufacturerUserId(createDTO.getManufacturerUserId());
        project.setManufacturerName(invitationDetail.getInviteeNickName());
        project.setManufacturerAvatar(invitationDetail.getInviteeAvatar());

        // 众筹信息 - 从邀约中获取实际数据
        project.setTargetAmount(invitationDetail.getQuotedPrice()); // 使用报价作为目标金额
        project.setCurrentAmount(BigDecimal.ZERO);
        project.setSupportCount(0);
        project.setViewCount(0);

        // 设置众筹时间 - 基于报价周期计算
        java.util.Date now = new java.util.Date();
        project.setStartTime(now);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(now);

        int crowdfundingDays = 60;
        calendar.add(java.util.Calendar.DAY_OF_MONTH, crowdfundingDays);
        project.setEndTime(calendar.getTime());
        invitationDetail.setStatus(7);
        project.setStatus(1); // 众筹中
        project.setDrawNumber(invitationDetail.getDrawNumber()); // 使用邀约中的抽奖数量
        project.setTotalSamples(invitationDetail.getProofingQuantity());
        project.setDrawStatus(0); // 未开始
        project.setEscrowStatus(0); // 托管中

        // 5. 插入众筹项目
        int result = crowdfundingProjectMapper.insert(project);


        // 6. 初始化Redis金额缓存
        if (result > 0 && project.getId() != null) {
            boolean initSuccess = crowdfundingRedisService.initProjectAmount(
                project.getId(),
                project.getTargetAmount()
            );
            if (!initSuccess) {
                log.error("初始化众筹项目Redis金额缓存失败: 项目ID={}", project.getId());
            }
        }
        return null;
    }

    @Override
    public SdCrowdfundingSupport getSupportByOrderNo(String orderNo) {
        return supportMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<CrowdfundingProjectListVO> getCrowdfundingProjectList() {
        // 查询所有众筹项目
        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectSdCrowdfundingProjectList(new SdCrowdfundingProject());

        // 转换为VO
        return projects.stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            // 使用BeanUtils进行属性拷贝
            org.springframework.beans.BeanUtils.copyProperties(project, vo);

            // 计算进度百分比
            if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = project.getCurrentAmount()
                    .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                vo.setProgressPercentage(progress);
            } else {
                vo.setProgressPercentage(BigDecimal.ZERO);
            }

            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<CrowdfundingProjectListVO> getActiveCrowdfundingProjects() {
        // 查询进行中的众筹项目（状态为1-进行中，且未结束）
        LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingProject::getStatus, 1) // 进行中
                   .gt(SdCrowdfundingProject::getEndTime, new Date()) // 未结束
                   .orderByDesc(SdCrowdfundingProject::getCreateTime);

        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectList(queryWrapper);

        // 转换为VO
        return projects.stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            // 使用BeanUtils进行属性拷贝
            org.springframework.beans.BeanUtils.copyProperties(project, vo);

            // 计算进度百分比
            if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = project.getCurrentAmount()
                    .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                vo.setProgressPercentage(progress);
            } else {
                vo.setProgressPercentage(BigDecimal.ZERO);
            }

            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public CrowdfundingProjectDetailVO getCrowdfundingProjectDetail(Long id) {
        // 查询项目详情
        SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(id);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }

        // 转换为VO
        CrowdfundingProjectDetailVO vo = new CrowdfundingProjectDetailVO();
        vo.setId(project.getId());
        vo.setProjectNo(project.getProjectNo());
        vo.setTitle(project.getTitle());
        vo.setDescription(project.getDescription());
        vo.setCoverImage(project.getCoverImage());
        vo.setCreatorUserId(project.getCreatorUserId());
        vo.setCreatorName(project.getCreatorName());
        vo.setTargetAmount(project.getTargetAmount());
        vo.setCurrentAmount(project.getCurrentAmount());
        vo.setSupportCount(project.getSupportCount());
        vo.setStartTime(project.getStartTime());
        vo.setEndTime(project.getEndTime());
        vo.setStatus(project.getStatus());
        vo.setDrawNumber(project.getDrawNumber());
        vo.setDrawStatus(project.getDrawStatus());

        // 计算进度百分比
        if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = project.getCurrentAmount()
                .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
            vo.setProgressPercentage(progress);
        } else {
            vo.setProgressPercentage(BigDecimal.ZERO);
        }

        // 计算剩余天数
        if (project.getEndTime() != null) {
            long daysLeft = (project.getEndTime().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
            vo.setRemainingDays(Math.max(0L, daysLeft));
        } else {
            vo.setRemainingDays(0L);
        }

        return vo;
    }


    @Override
    public List<CrowdfundingSupportVO> getMySupports() {
        Long currentUserId = LoginHelper.getUserId();

        // 查询当前用户的支持记录
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingSupport::getUserId, currentUserId)
                   .orderByDesc(SdCrowdfundingSupport::getCreateTime);

        List<SdCrowdfundingSupport> supports = supportMapper.selectList(queryWrapper);

        // 转换为VO
        return supports.stream().map(support -> {
            CrowdfundingSupportVO vo = new CrowdfundingSupportVO();
            vo.setId(support.getId());
            vo.setProjectId(support.getProjectId());
            vo.setUserId(support.getUserId());
            vo.setUserName(support.getUserName());
            vo.setOrderNo(support.getOrderNo());
            vo.setSupportAmount(support.getSupportAmount());
            vo.setDrawStatus(support.getDrawStatus());
            vo.setIsWinner(support.getIsWinner() == 1);
            vo.setPrizeInfo(support.getPrizeInfo());
            vo.setCreateTime(support.getCreateTime());
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<CrowdfundingDrawVO> getMyDraws() {
        Long currentUserId = LoginHelper.getUserId();

        // 查询当前用户的抽奖记录（已参与抽奖的记录）
        LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingSupport::getUserId, currentUserId)
                   .gt(SdCrowdfundingSupport::getDrawStatus, 0) // 已参与抽奖
                   .orderByDesc(SdCrowdfundingSupport::getCreateTime);

        List<SdCrowdfundingSupport> supports = supportMapper.selectList(queryWrapper);

        // 转换为VO
        return supports.stream().map(support -> {
            CrowdfundingDrawVO vo = new CrowdfundingDrawVO();
            vo.setId(support.getId());
            vo.setProjectId(support.getProjectId());
            vo.setUserId(support.getUserId());
            vo.setUserName(support.getUserName());
            vo.setDrawStatus(support.getDrawStatus());
            vo.setIsWinner(support.getIsWinner() == 1);
            vo.setPrizeInfo(support.getPrizeInfo());
            vo.setCreateTime(support.getCreateTime());
        return vo;
        }).collect(java.util.stream.Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createSupportOrder(CrowdfundingSupportDTO supportDTO) {
        String orderNo = null;
        try {
            // 1. 创建订单号
            orderNo = OrderNumUtils.getOrderNum(new Date());
            log.info("创建众筹支持订单: 订单号={}, 项目ID={}, 金额={}", orderNo, supportDTO.getProjectId(), supportDTO.getSupportAmount());

            // 2. 落库参与者数据（设置订单号）
            SdCrowdfundingSupport support = new SdCrowdfundingSupport();
            support.setProjectId(supportDTO.getProjectId());
            support.setUserId(supportDTO.getUserId());
            support.setUserName(supportDTO.getUserName());
            support.setOrderNo(orderNo); // 设置订单号
            support.setSupportAmount(supportDTO.getSupportAmount());
            support.setDrawStatus(0); // 未参与抽奖
            support.setIsWinner(0); // 未中奖
            // createBy, createTime, updateBy, updateTime 字段由 BaseEntity 自动填充

            supportMapper.insert(support);
            log.info("参与者数据落库成功: 订单号={}", orderNo);

            // 3. 扣除订单金额（Redis）
            boolean deducted = crowdfundingRedisService.tryDeductAmount(supportDTO.getProjectId(), supportDTO.getSupportAmount());
            if (!deducted) {
                throw new RuntimeException("众筹金额不足，无法创建订单");
            }
            log.info("Redis金额扣除成功: 订单号={}, 金额={}", orderNo, supportDTO.getSupportAmount());

            // 4. 投递到MQ
            crowdfundingMqService.sendPaymentOrderMessage(orderNo, support);
            log.info("MQ消息投递成功: 订单号={}", orderNo);

            // 5. 返回订单号
            return orderNo;

        } catch (Exception e) {
            log.error("创建众筹支持订单失败: 订单号={}, 项目ID={}, 金额={}", orderNo, supportDTO.getProjectId(), supportDTO.getSupportAmount(), e);

            // 异常处理：回滚数据库、回退金额
            if (orderNo != null) {
                handleRollback(orderNo, supportDTO.getProjectId(), supportDTO.getSupportAmount());
            }

            // 根据异常类型决定是否抛出运行时异常
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
                } else {
                throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(String orderNo) {
        try {
            log.info("处理支付成功回调: 订单号={}", orderNo);

            // 1. 查询支持记录
            SdCrowdfundingSupport support = supportMapper.selectByOrderNo(orderNo);
            if (support == null) {
                log.warn("未找到支持记录: 订单号={}", orderNo);
                return false;
            }

            // 2. 更新支持记录状态（设置订单号，标记为已支付）
            support.setOrderNo(orderNo);
            supportMapper.updateById(support);

            // 3. 更新众筹项目金额
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(support.getProjectId());
        if (project == null) {
                log.error("未找到众筹项目: 项目ID={}", support.getProjectId());
            return false;
            }

            // 4. 增加当前金额和支持人数
            project.setCurrentAmount(project.getCurrentAmount().add(support.getSupportAmount()));
            project.setSupportCount(project.getSupportCount() + 1);

            // 5. 检查是否达到目标金额
            if (project.getCurrentAmount().compareTo(project.getTargetAmount()) >= 0) {
                // 众筹成功，自动开始抽奖
                project.setStatus(2); // 众筹成功
                project.setDrawStatus(1); // 开始抽奖
                log.info("众筹成功，自动开始抽奖: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());

                // 更新项目状态
                crowdfundingProjectMapper.updateSdCrowdfundingProject(project);

                // 延迟执行抽奖
                scheduleDrawExecution(project);
                } else {
                // 更新项目金额
                crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
            }

            log.info("支付成功回调处理完成: 订单号={}, 项目ID={}", orderNo, support.getProjectId());
            return true;

        } catch (Exception e) {
            log.error("处理支付成功回调异常: 订单号={}", orderNo, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoExecuteDraw(SdCrowdfundingProject project) {
        try {
            log.info("开始自动执行抽奖: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());

            // 查询所有支持记录（已支付且未参与抽奖的）
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSupport::getProjectId, project.getId())
                       .eq(SdCrowdfundingSupport::getDrawStatus, 0) // 未参与抽奖
                       .isNotNull(SdCrowdfundingSupport::getOrderNo); // 已支付

            List<SdCrowdfundingSupport> supports = supportMapper.selectList(queryWrapper);
            if (supports.isEmpty()) {
                log.warn("没有可参与抽奖的支持记录: 项目ID={}", project.getId());
                // 更新项目抽奖状态为已结束
                project.setDrawStatus(2);
                crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
                return;
            }

            // 随机选择中奖者
            int drawNumber = project.getDrawNumber() != null ? project.getDrawNumber() : 1;
            int winnerCount = Math.min(drawNumber, supports.size());

            // 打乱顺序并选择前N个作为中奖者
            Collections.shuffle(supports);
            List<SdCrowdfundingSupport> winners = supports.subList(0, winnerCount);

            // 更新中奖者状态
            for (SdCrowdfundingSupport winner : winners) {
                winner.setDrawStatus(2); // 中奖
                winner.setIsWinner(1); // 是中奖者
                winner.setPrizeInfo("恭喜中奖！奖品信息待定");
                supportMapper.updateById(winner);
                log.info("中奖者: 用户ID={}, 用户名={}", winner.getUserId(), winner.getUserName());
            }

            // 更新未中奖者状态
            for (SdCrowdfundingSupport loser : supports.subList(winnerCount, supports.size())) {
                loser.setDrawStatus(2); // 已参与抽奖
                loser.setIsWinner(0); // 不是中奖者
                supportMapper.updateById(loser);
            }

            // 处理未参加抽奖的样品分配 - 发起人必中奖
            int totalSamples = project.getTotalSamples() != null ? project.getTotalSamples() : 0;
            int unallocatedSamples = totalSamples - drawNumber;

            if (unallocatedSamples > 0) {
                log.info("未参加抽奖的样品数量: {}, 发起人必中奖: 用户ID={}",
                        unallocatedSamples, project.getCreatorUserId());

                // 为发起人创建必中奖记录
                createInitiatorWinnerRecord(project, unallocatedSamples);
            }

            // 更新项目抽奖状态为已结束
            project.setDrawStatus(2);
            crowdfundingProjectMapper.updateSdCrowdfundingProject(project);

            log.info("自动执行抽奖成功: 项目ID={}, 中奖人数={}, 总参与人数={}, 未参加抽奖样品数={}",
                    project.getId(), winnerCount, supports.size(), unallocatedSamples);

        } catch (Exception e) {
            log.error("自动执行抽奖异常: 项目ID={}", project.getId(), e);
            throw e;
        }
    }

    /**
     * 为发起人创建必中奖记录
     */
    private void createInitiatorWinnerRecord(SdCrowdfundingProject project, int sampleCount) {
        try {
            // 检查发起人是否已经参与了抽奖（通过支付支持）
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSupport::getProjectId, project.getId())
                       .eq(SdCrowdfundingSupport::getUserId, project.getCreatorUserId())
                       .isNotNull(SdCrowdfundingSupport::getOrderNo); // 已支付的支持记录

            List<SdCrowdfundingSupport> existingSupports = supportMapper.selectList(queryWrapper);

            if (existingSupports.isEmpty()) {
                // 发起人没有参与抽奖，创建必中奖记录
                SdCrowdfundingSupport initiatorSupport = new SdCrowdfundingSupport();
                initiatorSupport.setProjectId(project.getId());
                initiatorSupport.setUserId(project.getCreatorUserId());
                initiatorSupport.setUserName(project.getCreatorName());
                initiatorSupport.setOrderNo("INITIATOR_WINNER_" + project.getId()); // 特殊标识
                initiatorSupport.setSupportAmount(BigDecimal.ZERO); // 发起人必得样品，不需要额外支付
                initiatorSupport.setDrawStatus(2); // 已参与抽奖
                initiatorSupport.setIsWinner(1); // 必中奖
                initiatorSupport.setPrizeInfo("发起人必得样品，获得" + sampleCount + "个样品");

                supportMapper.insert(initiatorSupport);

                log.info("发起人必得样品记录创建成功: 项目ID={}, 发起人ID={}, 样品数量={}",
                        project.getId(), project.getCreatorUserId(), sampleCount);
            } else {
                // 发起人已经参与了抽奖，更新其奖品信息，增加必得样品
                SdCrowdfundingSupport existingSupport = existingSupports.get(0);
                String originalPrizeInfo = existingSupport.getPrizeInfo() != null ? existingSupport.getPrizeInfo() : "";
                existingSupport.setPrizeInfo(originalPrizeInfo + " + 发起人必得样品" + sampleCount + "个");
                supportMapper.updateById(existingSupport);

                log.info("发起人已参与抽奖，增加必得样品: 项目ID={}, 发起人ID={}, 样品数量={}",
                        project.getId(), project.getCreatorUserId(), sampleCount);
            }

        } catch (Exception e) {
            log.error("创建发起人必得样品记录失败: 项目ID={}, 发起人ID={}, 样品数量={}",
                    project.getId(), project.getCreatorUserId(), sampleCount, e);
        }
    }

    @Override
    public List<CrowdfundingProjectListVO> getManufacturerProjects() {
        Long currentUserId = LoginHelper.getUserId();
        log.info("查询厂家参与的众筹项目: 厂家ID={}", currentUserId);

        // 查询厂家参与的所有众筹项目
        LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingProject::getManufacturerUserId, currentUserId)
                   .orderByDesc(SdCrowdfundingProject::getCreateTime);

        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectList(queryWrapper);

        // 转换为VO
        return projects.stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            vo.setId(project.getId());
            vo.setProjectNo(project.getProjectNo());
            vo.setTitle(project.getTitle());
            vo.setCoverImage(project.getCoverImage());
            vo.setCreatorName(project.getCreatorName());
            vo.setTargetAmount(project.getTargetAmount());
            vo.setCurrentAmount(project.getCurrentAmount());
            vo.setSupportCount(project.getSupportCount());
            vo.setStartTime(project.getStartTime());
            vo.setEndTime(project.getEndTime());
            vo.setStatus(project.getStatus());
            vo.setDrawNumber(project.getDrawNumber());
            vo.setDrawStatus(project.getDrawStatus());

            // 计算进度百分比
            if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = project.getCurrentAmount()
                    .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                vo.setProgressPercentage(progress);
            } else {
                vo.setProgressPercentage(BigDecimal.ZERO);
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CrowdfundingProjectListVO> getManufacturerSuccessfulProjects() {
        Long currentUserId = LoginHelper.getUserId();
        log.info("查询厂家参与的众筹成功项目: 厂家ID={}", currentUserId);

        // 查询厂家参与的众筹成功项目（状态=2）
        LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingProject::getManufacturerUserId, currentUserId)
                   .eq(SdCrowdfundingProject::getStatus, 2) // 众筹成功
                   .orderByDesc(SdCrowdfundingProject::getCreateTime);

        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectList(queryWrapper);

        // 转换为VO
        return projects.stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            vo.setId(project.getId());
            vo.setProjectNo(project.getProjectNo());
            vo.setTitle(project.getTitle());
            vo.setCoverImage(project.getCoverImage());
            vo.setCreatorName(project.getCreatorName());
            vo.setTargetAmount(project.getTargetAmount());
            vo.setCurrentAmount(project.getCurrentAmount());
            vo.setSupportCount(project.getSupportCount());
            vo.setStartTime(project.getStartTime());
            vo.setEndTime(project.getEndTime());
            vo.setStatus(project.getStatus());
            vo.setDrawNumber(project.getDrawNumber());
            vo.setDrawStatus(project.getDrawStatus());

            // 计算进度百分比
            if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = project.getCurrentAmount()
                    .divide(project.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                vo.setProgressPercentage(progress);
            } else {
                vo.setProgressPercentage(BigDecimal.ZERO);
            }

        return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 异常回滚处理
     */
    private void handleRollback(String orderNo, Long projectId, BigDecimal amount) {
        try {
            // 1. 删除参与者记录（根据订单号查询）
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSupport::getOrderNo, orderNo);

            SdCrowdfundingSupport support = supportMapper.selectOne(queryWrapper);
            if (support != null) {
                supportMapper.deleteById(support.getId());
                log.info("回滚参与者记录: 订单号={}, 支持记录ID={}", orderNo, support.getId());
            } else {
                log.warn("未找到需要回滚的支持记录: 订单号={}", orderNo);
            }

            // 2. 回退Redis金额
            boolean refunded = crowdfundingRedisService.refundAmount(projectId, amount);
            if (refunded) {
                log.info("回退Redis金额: 订单号={}, 金额={}", orderNo, amount);
            } else {
                log.error("回退Redis金额失败: 订单号={}, 金额={}", orderNo, amount);
            }

        } catch (Exception e) {
            log.error("回滚处理失败: 订单号={}, 项目ID={}, 金额={}", orderNo, projectId, amount, e);
        }
    }

    /**
     * 清理异常的支持记录（手动调用）
     * 用于清理之前因为MQ问题导致的异常记录
     */
    public void cleanupAbnormalSupportRecords() {
        try {
            // 查询所有没有对应支付订单的支持记录
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNotNull(SdCrowdfundingSupport::getOrderNo)
                       .orderByDesc(SdCrowdfundingSupport::getCreateTime);

            List<SdCrowdfundingSupport> supports = supportMapper.selectList(queryWrapper);
            log.info("查询到{}条支持记录，开始检查异常记录", supports.size());

            for (SdCrowdfundingSupport support : supports) {
                try {
                    // 检查是否存在对应的支付订单
                    // 这里可以调用支付服务检查订单是否存在
                    // 如果不存在，则删除这条记录并回退Redis金额

                    // 暂时先记录日志，后续可以根据实际需求处理
                    log.info("检查支持记录: 订单号={}, 项目ID={}, 金额={}, 创建时间={}",
                            support.getOrderNo(), support.getProjectId(),
                            support.getSupportAmount(), support.getCreateTime());

                } catch (Exception e) {
                    log.error("检查支持记录失败: 订单号={}", support.getOrderNo(), e);
                }
            }
        } catch (Exception e) {
            log.error("清理异常支持记录失败", e);
        }
    }

    /**
     * 延迟执行抽奖（
     */
    private void scheduleDrawExecution(SdCrowdfundingProject project) {
        new Thread(() -> {
            try {
                Thread.sleep(crowdfundingConfig.getDrawDelaySeconds() * 1000L); // 使用配置的延迟秒数

                // 重新查询项目信息，确保状态正确
                SdCrowdfundingProject currentProject = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(project.getId());
                if (currentProject != null && currentProject.getDrawStatus() == 1) {
                    autoExecuteDraw(currentProject);
            }
        } catch (Exception e) {
                log.error("延迟执行抽奖异常: 项目ID={}", project.getId(), e);
            }
        }).start();
    }
}
