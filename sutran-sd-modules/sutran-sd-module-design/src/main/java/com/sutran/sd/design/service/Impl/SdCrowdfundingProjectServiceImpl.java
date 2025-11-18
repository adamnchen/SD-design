package com.sutran.sd.design.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.OrderNumUtils;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSampleDelivery;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingProjectSimpleCreateDTO;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.enums.CrowdfundingProjectStatus;
import com.sutran.sd.design.enums.CrowdfundingSupportStatus;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSampleDeliveryMapper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.design.service.CrowdfundingMqService;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.design.vo.*;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.system.service.IForbiddenWordService;
import com.sutran.sd.system.service.ISysUserService;
import com.sutran.sd.common.core.service.NoticeService;
import com.sutran.sd.common.core.domain.vo.NoticeCommonVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
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
    private final SdProofingInvitationMapper invitationMapper;
    private final ISysUserService userService;
    private final AliPayService aliPayService;
    private final IForbiddenWordService forbiddenWordService;
    private final SdCrowdfundingSampleDeliveryMapper deliveryMapper;
    private final NoticeService noticeService;

    @Override
    public SdCrowdfundingProject selectSdCrowdfundingProjectById(Long id) {
        return crowdfundingProjectMapper.selectSdCrowdfundingProjectByIdWithTieredPricing(id);
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

        // 如果是“我购买的”列表，为当前登录用户在每个项目下填充自己的快递单号
        if ("supported".equals(type)) {
            // 查询当前用户的众筹样品发货记录，并按项目ID归类
            List<SdCrowdfundingSampleDelivery> deliveries = deliveryMapper.selectByRecipientUserId(currentUserId);
            if (deliveries != null && !deliveries.isEmpty()) {
                java.util.Map<Long, java.util.List<SdCrowdfundingSampleDelivery>> projectDeliveryMap =
                    deliveries.stream()
                        .collect(Collectors.groupingBy(SdCrowdfundingSampleDelivery::getCrowdfundingProjectId));

                result.getRecords().forEach(project -> {
                    List<SdCrowdfundingSampleDelivery> projectDeliveries = projectDeliveryMap.get(project.getId());
                    if (projectDeliveries != null && !projectDeliveries.isEmpty()) {
                        projectDeliveries.stream()
                            .max(Comparator.comparing(SdCrowdfundingSampleDelivery::getCreateTime))
                            .ifPresent(d -> project.setTrackingNumber(d.getTrackingNumber()));
                    }
                });
            }
        }

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

        // 2. 检查邀约是否已有回应，防止重复发布众筹
        if (invitationDetail.getStatus() != null && invitationDetail.getStatus() != 0) {
            // 邀约有回应，检查是否已存在进行中的众筹项目
            SdCrowdfundingProject existingProject = crowdfundingProjectMapper.selectByProofingInvitationId(invitationDetail.getId());
            if (existingProject != null) {
                // 检查现有众筹项目状态：1=众筹中，4=已发布
                if (existingProject.getStatus() != null &&
                    (existingProject.getStatus().equals(CrowdfundingProjectStatus.FUNDING.getCode()) ||
                     existingProject.getStatus().equals(CrowdfundingProjectStatus.PUBLISHED.getCode()))) {
                    throw new ServiceException("该邀约已存在进行中的众筹项目，不能重复发布");
                }
            }
        }

        // 3. 厂家匹配：如果当前邀约的被邀约人不等于传入厂家，则在同一作品+发起人的邀约组中尝试定位该厂家
        if (!invitationDetail.getInviteeUserId().equals(createDTO.getManufacturerUserId())) {
            // 在同一 workId + inviterUserId 下查找包含该厂家的邀约记录
            java.util.List<com.sutran.sd.common.core.domain.entity.SdProofingInvitation> groupInvitations =
                invitationMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.sutran.sd.common.core.domain.entity.SdProofingInvitation>()
                    .eq(com.sutran.sd.common.core.domain.entity.SdProofingInvitation::getWorkId, invitationDetail.getWorkId())
                    .eq(com.sutran.sd.common.core.domain.entity.SdProofingInvitation::getInviterUserId, invitationDetail.getInviterUserId()));

            com.sutran.sd.common.core.domain.entity.SdProofingInvitation matched = null;
            for (com.sutran.sd.common.core.domain.entity.SdProofingInvitation inv : groupInvitations) {
                if (createDTO.getManufacturerUserId().equals(inv.getInviteeUserId())) {
                    matched = inv;
                    break;
                }
            }

            if (matched != null) {
                // 以匹配到的邀约详情作为后续的数据来源，确保厂家信息一致
                com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO matchedDetail =
                    invitationMapper.selectInvitationDetailById(matched.getId());
                if (matchedDetail != null) {
                    invitationDetail = matchedDetail;
                }
            } else {
                throw new ServiceException("厂家ID与该作品的邀约不匹配");
            }
        }

        // 4. 生成项目编号
        String projectNo = "CF" + System.currentTimeMillis();

        // 5. 构建众筹项目对象 - 从多表联查结果中获取所有信息
        SdCrowdfundingProject project = new SdCrowdfundingProject();
        project.setProjectNo(projectNo);

        // 从邀约详情获取项目信息
        String title = invitationDetail.getProductTitle();
        String description = invitationDetail.getProductDescription();

        // 违禁词校验
        if (StringUtils.isNotBlank(title)) {
            forbiddenWordService.validateForbiddenWord(title, "项目标题");
        }
        if (StringUtils.isNotBlank(description)) {
            forbiddenWordService.validateForbiddenWord(description, "项目描述");
        }

        project.setTitle(title);
        project.setDescription(description);
        project.setCoverImage(invitationDetail.getImageUrl()); // 从联查结果获取图片

        // 发起人信息（从联查结果获取）
        project.setCreatorUserId(invitationDetail.getInviterUserId());
        project.setCreatorName(invitationDetail.getInviterNickName());
        project.setCreatorAvatar(invitationDetail.getInviterAvatar());
        // 绑定实际使用的邀约ID（可能是组内被匹配到的那一条）
        project.setProofingInvitationId(invitationDetail.getId());

        // 厂家信息（从联查结果获取）
        project.setManufacturerUserId(invitationDetail.getInviteeUserId());
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

        project.setStatus(CrowdfundingProjectStatus.FUNDING.getCode()); // 众筹中
        project.setDrawNumber(invitationDetail.getDrawNumber()); // 使用邀约中的抽奖数量
        project.setTotalSamples(invitationDetail.getProofingQuantity());
        project.setDrawStatus(0); // 未开始
        project.setEscrowStatus(0); // 托管中

        // 6. 插入众筹项目
        int result = crowdfundingProjectMapper.insert(project);


        // 7. 初始化Redis金额缓存
        if (result > 0 && project.getId() != null) {
            boolean initSuccess = crowdfundingRedisService.initProjectAmount(
                project.getId(),
                project.getTargetAmount()

            );
            if (!initSuccess) {
                log.error("初始化众筹项目Redis金额缓存失败: 项目ID={}", project.getId());
            }
            invitationMapper.updateStatusById(invitationDetail.getId(), 7);
        }
        return null;
    }

    @Override
    public SdCrowdfundingSupport getSupportByOrderNo(String orderNo) {
        return supportMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<CrowdfundingProjectListVO> getCrowdfundingProjectList() {
        // 使用多表联查获取众筹项目列表（带正确图片）
        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectCrowdfundingProjectListWithImage();

        // 转换为VO
        return projects.stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            // 使用BeanUtils进行属性拷贝
            org.springframework.beans.BeanUtils.copyProperties(project, vo);
            // 确保发起人ID传递
            vo.setCreatorUserId(project.getCreatorUserId());

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

    /**
     * 执行众筹项目抽奖逻辑：
     * - 以 userId 为单位，一人一次机会
     * - 从参与用户中随机抽取 drawNumber 个中奖用户
     * - 中奖用户的所有支持记录标记为中奖，未中奖用户标记为未中奖
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runDrawForProject(Long projectId) {
        SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
        if (project == null) {
            log.warn("[众筹抽奖] 项目不存在, projectId={}", projectId);
            return;
        }

        if (!CrowdfundingProjectStatus.SUCCESS.getCode().equals(project.getStatus())) {
            log.warn("[众筹抽奖] 项目状态非成功, 跳过抽奖, projectId={}, status={}", projectId, project.getStatus());
            return;
        }

        // 如果已经抽奖结束则不再重复执行
        if (project.getDrawStatus() != null && project.getDrawStatus() == 2) {
            log.info("[众筹抽奖] 项目已完成抽奖, 跳过, projectId={}", projectId);
            return;
        }

        // 查询该项目下所有有效的支持记录（已支付、正常状态）
        LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdCrowdfundingSupport::getProjectId, projectId)
                   .eq(SdCrowdfundingSupport::getStatus, CrowdfundingSupportStatus.NORMAL.getCode())
                   .isNotNull(SdCrowdfundingSupport::getOrderNo);

        List<SdCrowdfundingSupport> supports = supportMapper.selectList(queryWrapper);
        if (supports == null || supports.isEmpty()) {
            log.warn("[众筹抽奖] 无有效支持记录, 直接标记抽奖结束, projectId={}", projectId);
            project.setDrawStatus(2);
            project.setDrawTime(new Date());
            crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
            return;
        }

        // 以 userId 维度去重，一人一次机会
        Map<Long, List<SdCrowdfundingSupport>> userSupportMap = supports.stream()
            .collect(Collectors.groupingBy(SdCrowdfundingSupport::getUserId));

        List<Long> userIds = new ArrayList<>(userSupportMap.keySet());
        if (userIds.isEmpty()) {
            log.warn("[众筹抽奖] 无参与用户, 直接标记抽奖结束, projectId={}", projectId);
            project.setDrawStatus(2);
            project.setDrawTime(new Date());
            crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
            return;
        }

        // 随机打乱用户列表
        Collections.shuffle(userIds);

        Integer drawNumber = project.getDrawNumber();
        int winnerCount = (drawNumber != null && drawNumber > 0)
            ? Math.min(drawNumber, userIds.size())
            : Math.min(1, userIds.size());

        Set<Long> winnerUserIds = new HashSet<>(userIds.subList(0, winnerCount));

        log.info("[众筹抽奖] 开始抽奖, projectId={}, 参与人数={}, 中奖名额={}, 实际中奖人数={}",
            projectId, userIds.size(), drawNumber, winnerUserIds.size());

        // 构建通知模板
        NoticeCommonVo noticeTemplate = new NoticeCommonVo()
            .setTitle("众筹抽奖结果通知")
            .setPublishTime(new Date());

        // 更新支持记录的抽奖状态
        for (Map.Entry<Long, List<SdCrowdfundingSupport>> entry : userSupportMap.entrySet()) {
            Long userId = entry.getKey();
            boolean isWinner = winnerUserIds.contains(userId);

            for (SdCrowdfundingSupport support : entry.getValue()) {
                support.setDrawStatus(isWinner ? 2 : 3); // 2=中奖,3=未中奖
                support.setIsWinner(isWinner ? 1 : 0);
                supportMapper.updateById(support);
            }

            // 给中奖用户发送通知
            if (isWinner) {
                String projectTitle = project.getTitle() != null ? project.getTitle() : "众筹项目";
                String content = "恭喜您参与的众筹项目【" + projectTitle + "】抽奖中奖，商家将尽快为您发放样品。";
                NoticeCommonVo notice = new NoticeCommonVo()
                    .setTitle(noticeTemplate.getTitle())
                    .setContent(content)
                    .setPublishTime(new Date());
                try {
                    noticeService.asyncSendCommonMsg(notice, userId);
                } catch (Exception e) {
                    log.error("[众筹抽奖] 发送中奖通知失败, projectId={}, userId={}", projectId, userId, e);
                }
            }
        }

        // 更新项目抽奖状态为已结束
        project.setDrawStatus(2);
        project.setDrawTime(new Date());
        crowdfundingProjectMapper.updateSdCrowdfundingProject(project);

        log.info("[众筹抽奖] 抽奖完成, projectId={}, 中奖用户ID={} ", projectId, winnerUserIds);

        // 插入众筹样品发货记录
        insertSampleDeliveryRecordsForWinners(projectId, winnerUserIds, userSupportMap);
    }

    /**
     * 为中奖者插入众筹样品发货记录
     */
    private void insertSampleDeliveryRecordsForWinners(Long projectId, Set<Long> winnerUserIds, Map<Long, List<SdCrowdfundingSupport>> userSupportMap) {
        try {
            log.info("[众筹发货] 开始为中奖者插入样品发货记录, projectId={}, 中奖人数={}", projectId, winnerUserIds.size());

            // 获取项目信息
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
            if (project == null) {
                log.error("[众筹发货] 项目不存在, projectId={}", projectId);
                return;
            }

            // 查询项目的邀约记录ID（发货记录需要）
            Long proofingInvitationId = project.getProofingInvitationId();
            if (proofingInvitationId == null) {
                log.error("[众筹发货] 项目邀约记录ID为空, projectId={}", projectId);
                return;
            }

            // 获取发起人信息作为发货人
            Long senderUserId = project.getCreatorUserId();
            String senderName = "项目发起人";
            if (senderUserId != null) {
                try {
                    SysUser senderUser = userService.selectUserById(senderUserId);
                    if (senderUser != null) {
                        senderName = senderUser.getUserName();
                    }
                } catch (Exception e) {
                    log.warn("[众筹发货] 获取发起人信息失败, senderUserId={}", senderUserId, e);
                }
            }

            int insertedCount = 0;
            // 为每个中奖用户创建发货记录
            for (Long winnerUserId : winnerUserIds) {
                List<SdCrowdfundingSupport> userSupports = userSupportMap.get(winnerUserId);
                if (userSupports == null || userSupports.isEmpty()) {
                    log.warn("[众筹发货] 中奖用户无支持记录, projectId={}, userId={}", projectId, winnerUserId);
                    continue;
                }

                // 使用该用户的第一个支持记录的收货信息
                SdCrowdfundingSupport support = userSupports.get(0);

                // 检查是否已有发货记录（避免重复插入）
                LambdaQueryWrapper<SdCrowdfundingSampleDelivery> existQuery = new LambdaQueryWrapper<>();
                existQuery.eq(SdCrowdfundingSampleDelivery::getCrowdfundingProjectId, projectId)
                         .eq(SdCrowdfundingSampleDelivery::getRecipientUserId, winnerUserId);
                SdCrowdfundingSampleDelivery existingDelivery = deliveryMapper.selectOne(existQuery);
                if (existingDelivery != null) {
                    log.info("[众筹发货] 中奖用户已有发货记录，跳过插入, projectId={}, userId={}", projectId, winnerUserId);
                    continue;
                }

                // 创建发货记录
                SdCrowdfundingSampleDelivery delivery = new SdCrowdfundingSampleDelivery();
                delivery.setId(IdUtil.getSnowflakeNextId());
                delivery.setCrowdfundingProjectId(projectId);
                delivery.setProofingInvitationId(proofingInvitationId);
                delivery.setSampleImageUrl(""); // 样品图片初始为空
                delivery.setRecipientUserId(winnerUserId);
                delivery.setRecipientName(support.getReceiverName());
                delivery.setRecipientPhone(support.getReceiverPhone());
                delivery.setDeliveryAddress(buildFullReceiverAddress(support.getReceiverArea(), support.getReceiverAddress()));
                delivery.setSenderUserId(senderUserId);
                delivery.setSenderName(senderName);
                delivery.setStatus(1); // 1=待发货
                delivery.setTrackingNumber(""); // 快递单号初始为空，由商家后续填写
                delivery.setDeliveryCompany(""); // 快递公司初始为空

                // 插入发货记录
                int result = deliveryMapper.insert(delivery);
                if (result > 0) {
                    insertedCount++;
                    log.info("[众筹发货] 插入发货记录成功, projectId={}, userId={}, deliveryId={}",
                        projectId, winnerUserId, delivery.getId());
                } else {
                    log.error("[众筹发货] 插入发货记录失败, projectId={}, userId={}", projectId, winnerUserId);
                }
            }

            log.info("[众筹发货] 完成为中奖者插入样品发货记录, projectId={}, 中奖人数={}, 成功插入{}条记录",
                projectId, winnerUserIds.size(), insertedCount);

        } catch (Exception e) {
            log.error("[众筹发货] 插入样品发货记录异常, projectId={}", projectId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    private String buildFullReceiverAddress(String area, String address) {
        if (StringUtils.isBlank(area)) {
            return address;
        }
        if (StringUtils.isBlank(address)) {
            return area;
        }
        return area + address;
    }

    @Override
    public List<CrowdfundingProjectListVO> getActiveCrowdfundingProjects() {
        // 使用多表联查获取进行中的众筹项目列表（带正确图片）
        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectActiveCrowdfundingProjectsWithImage();

        // 转换为VO
        return projects.stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            // 使用BeanUtils进行属性拷贝
            org.springframework.beans.BeanUtils.copyProperties(project, vo);
            // 确保发起人ID传递
            vo.setCreatorUserId(project.getCreatorUserId());

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
    public TableDataInfo<CrowdfundingProjectListVO> getActiveCrowdfundingProjectsPage(PageQuery pageQuery) {
        log.info("获取进行中的众筹项目列表（分页）");

        // 使用分页查询进行中的众筹项目
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SdCrowdfundingProject> page = pageQuery.build();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SdCrowdfundingProject::getCreateTime);

        com.baomidou.mybatisplus.core.metadata.IPage<SdCrowdfundingProject> result =
            crowdfundingProjectMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<CrowdfundingProjectListVO> voList = result.getRecords().stream().map(project -> {
            CrowdfundingProjectListVO vo = new CrowdfundingProjectListVO();
            org.springframework.beans.BeanUtils.copyProperties(project, vo);

            // 计算进度百分比
            if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = project.getCurrentAmount()
                    .divide(project.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                vo.setProgressPercentage(progress);
            } else {
                vo.setProgressPercentage(BigDecimal.ZERO);
            }

            return vo;
        }).collect(java.util.stream.Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }

    @Override
    public CrowdfundingProjectDetailVO getCrowdfundingProjectDetail(Long id) {
        // 查询项目详情（包含阶梯价格）
        SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectByIdWithTieredPricing(id);
        if (project == null) {
            throw new RuntimeException("众筹项目不存在");
        }

        // 转换为VO
        CrowdfundingProjectDetailVO vo = new CrowdfundingProjectDetailVO();
        vo.setId(project.getId());
        vo.setProjectNo(project.getProjectNo());
        vo.setProofingInvitationId(project.getProofingInvitationId());
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
        vo.setProfitShareRatio(project.getProfitShareRatio());

        // 计算进度百分比
        if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = project.getCurrentAmount()
                .divide(project.getTargetAmount(), 4, RoundingMode.HALF_UP)
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

        // 计算阶梯价格相关信息
        calculateCrowdfundingTieredPricingInfo(project, vo);

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

            // 设置抽奖状态描述
            switch (support.getDrawStatus()) {
                case 0:
                    vo.setDrawStatusDesc("未参与抽奖");
                    break;
                case 1:
                    vo.setDrawStatusDesc("已参与抽奖");
                    break;
                case 2:
                    vo.setDrawStatusDesc("恭喜中奖");
                    break;
                case 3:
                    vo.setDrawStatusDesc("未中奖");
                    break;
                default:
                    vo.setDrawStatusDesc("未知状态");
                    break;
            }

            vo.setCreateTime(support.getCreateTime());

            // 查询项目标题
            try {
                SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(support.getProjectId());
                if (project != null) {
                    vo.setProjectTitle(project.getTitle());
                }
            } catch (Exception e) {
                log.warn("查询项目信息失败: 项目ID={}, 错误={}", support.getProjectId(), e.getMessage());
            }

            // 查询关联的样品发货记录
            try {
                LambdaQueryWrapper<SdCrowdfundingSampleDelivery> deliveryQuery = new LambdaQueryWrapper<>();
                deliveryQuery.eq(SdCrowdfundingSampleDelivery::getCrowdfundingProjectId, support.getProjectId())
                           .eq(SdCrowdfundingSampleDelivery::getRecipientUserId, support.getUserId());

                List<SdCrowdfundingSampleDelivery> deliveries = deliveryMapper.selectList(deliveryQuery);
                if (deliveries != null && !deliveries.isEmpty()) {
                    // 如果有多个发货记录，取最新的一个
                    SdCrowdfundingSampleDelivery latestDelivery = deliveries.stream()
                        .max(java.util.Comparator.comparing(SdCrowdfundingSampleDelivery::getCreateTime))
                        .orElse(null);

                    if (latestDelivery != null) {
                        vo.setTrackingNumber(latestDelivery.getTrackingNumber());
                        vo.setDeliveryStatus(latestDelivery.getStatus());
                        vo.setDeliveryTime(latestDelivery.getDeliveryTime());
                    }
                }
            } catch (Exception e) {
                log.warn("查询发货记录失败: 支持记录ID={}, 错误={}", support.getId(), e.getMessage());
            }

            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SampleDeliveryListVO> getMySampleDeliveries() {
        Long currentUserId = LoginHelper.getUserId();

        // 查询当前用户的样品发货记录
        List<SdCrowdfundingSampleDelivery> deliveries = deliveryMapper.selectByRecipientUserId(currentUserId);

        // 转换为VO
        return deliveries.stream().map(delivery -> {
            SampleDeliveryListVO vo = new SampleDeliveryListVO();
            BeanUtils.copyProperties(delivery, vo);

            // 设置状态描述
            if (delivery.getStatus() != null) {
                switch (delivery.getStatus()) {
                    case 1:
                        vo.setStatusText("待发货");
                        break;
                    case 2:
                        vo.setStatusText("已发货");
                        break;
                    default:
                        vo.setStatusText("未知状态");
                        break;
                }
            }

            // 查询项目标题
            if (delivery.getCrowdfundingProjectId() != null) {
                SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(delivery.getCrowdfundingProjectId());
                if (project != null) {
                    vo.setProjectTitle(project.getTitle());
                }
            }

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
        String orderNo;
        try {
            // 1. 检查用户是否已经参与过该众筹项目
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSupport::getProjectId, supportDTO.getProjectId())
                       .eq(SdCrowdfundingSupport::getUserId, supportDTO.getUserId());

            SdCrowdfundingSupport existingSupport = supportMapper.selectOne(queryWrapper);
            if (existingSupport != null) {
                throw new RuntimeException("您已经参与过该众筹项目，每个用户只能参与一次打样众筹");
            }

            // 2. 创建订单号
            orderNo = OrderNumUtils.getOrderNum(new Date());
            log.info("创建众筹支持订单: 订单号={}, 项目ID={}, 金额={}", orderNo, supportDTO.getProjectId(), supportDTO.getSupportAmount());

            // 3. 落库参与者数据（设置订单号）
            SdCrowdfundingSupport support = new SdCrowdfundingSupport();
            support.setProjectId(supportDTO.getProjectId());
            support.setUserId(supportDTO.getUserId());
            support.setUserName(supportDTO.getUserName());
            support.setOrderNo(orderNo); // 设置订单号
            support.setSupportAmount(supportDTO.getSupportAmount());
            support.setDrawStatus(0); // 未参与抽奖
            support.setIsWinner(0); // 未中奖
            support.setStatus(CrowdfundingSupportStatus.CANCELLED.getCode()); // 初始状态为已取消，支付成功后会更新为正常
            // 设置收货信息
            support.setReceiverName(supportDTO.getReceiverName());
            support.setReceiverPhone(supportDTO.getReceiverPhone());
            support.setReceiverAddress(supportDTO.getReceiverAddress());
            support.setReceiverArea(supportDTO.getReceiverArea());
            // createBy, createTime, updateBy, updateTime 字段由 BaseEntity 自动填充

            supportMapper.insert(support);
            log.info("参与者数据落库成功: 订单号={}, 收货信息: {}，{}，{}", orderNo, 
                support.getReceiverName(), support.getReceiverPhone(), support.getReceiverAddress());

            // 4. 扣除订单金额（Redis）
            boolean deducted = crowdfundingRedisService.tryDeductAmount(supportDTO.getProjectId(), supportDTO.getSupportAmount());
            if (!deducted) {
                throw new RuntimeException("众筹金额不符，无法创建订单");
            }
            log.info("Redis金额扣除成功: 订单号={}, 金额={}", orderNo, supportDTO.getSupportAmount());

            // 5. 投递到MQ
            crowdfundingMqService.sendPaymentOrderMessage(orderNo, support);
            log.info("MQ消息投递成功: 订单号={}", orderNo);

            // 6. 返回订单号
            return orderNo;

        } catch (Exception e) {
            // 根据异常类型决定是否抛出运行时异常
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
                } else {
                throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
            }
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
                    .divide(project.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                vo.setProgressPercentage(progress);
            } else {
                vo.setProgressPercentage(BigDecimal.ZERO);
            }

        return vo;
        }).collect(Collectors.toList());
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
     * 计算众筹项目阶梯价格相关信息
     */
    private void calculateCrowdfundingTieredPricingInfo(SdCrowdfundingProject project, CrowdfundingProjectDetailVO vo) {
        try {
            // 设置阶梯价格配置
            vo.setTieredPricing(project.getTieredPricing());

            // 1. 解析阶梯价格配置
            if (StringUtils.isBlank(project.getTieredPricing())) {
                // 没有阶梯价格配置，使用目标金额作为基础价格
                vo.setCurrentPrice(project.getTargetAmount());
                vo.setTieredPricingList(new ArrayList<>());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            List<com.sutran.sd.design.vo.TieredPricingItem> tieredPricingList = mapper.readValue(project.getTieredPricing(),
                mapper.getTypeFactory().constructCollectionType(List.class, com.sutran.sd.design.vo.TieredPricingItem.class));

            vo.setTieredPricingList(tieredPricingList);

            // 2. 查询当前支持数量（已支付的支持记录）
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSupport::getProjectId, project.getId());
            // 注意：众筹支持记录没有status字段，我们假设所有记录都是已支付的

            List<SdCrowdfundingSupport> paidSupports = supportMapper.selectList(queryWrapper);
            // 众筹支持记录没有quantity字段，我们使用支持金额来计算
            int totalQuantity = paidSupports.size(); // 使用支持记录数量作为数量

            // 3. 计算当前价格
            BigDecimal currentPrice = calculateCrowdfundingCurrentPrice(totalQuantity, tieredPricingList);
            vo.setCurrentPrice(currentPrice);

            // 4. 计算下一个价格阈值和价格
            calculateCrowdfundingNextPriceInfo(totalQuantity, tieredPricingList, vo);

        } catch (Exception e) {
            log.error("[众筹项目] 计算阶梯价格信息失败: 项目ID={}", project.getId(), e);
            // 出错时使用目标金额作为基础价格
            vo.setCurrentPrice(project.getTargetAmount());
            vo.setTieredPricingList(new ArrayList<>());
        }
    }

    /**
     * 计算众筹项目当前价格
     */
    private BigDecimal calculateCrowdfundingCurrentPrice(int totalQuantity, List<com.sutran.sd.design.vo.TieredPricingItem> tieredPricingList) {
        if (tieredPricingList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 按数量节点排序
        tieredPricingList.sort(Comparator.comparingInt(TieredPricingItem::getNode));

        // 找到对应的价格区间
        for (int i = tieredPricingList.size() - 1; i >= 0; i--) {
            com.sutran.sd.design.vo.TieredPricingItem tier = tieredPricingList.get(i);
            if (totalQuantity >= tier.getNode()) {
                return tier.getUnitPrice();
            }
        }

        // 如果数量小于第一个节点，返回第一个价格
        return tieredPricingList.get(0).getUnitPrice();
    }

    /**
     * 计算众筹项目下一个价格信息
     */
    private void calculateCrowdfundingNextPriceInfo(int totalQuantity, List<com.sutran.sd.design.vo.TieredPricingItem> tieredPricingList, CrowdfundingProjectDetailVO vo) {
        if (tieredPricingList.isEmpty()) {
            return;
        }

        // 按数量节点排序
        tieredPricingList.sort(Comparator.comparingInt(TieredPricingItem::getNode));

        // 找到下一个价格阈值
        for (com.sutran.sd.design.vo.TieredPricingItem tier : tieredPricingList) {
            if (totalQuantity < tier.getNode()) {
                vo.setNextThreshold(tier.getNode());
                vo.setNextPrice(tier.getUnitPrice());
                return;
            }
        }

        // 如果已经达到最高阶梯，没有下一个价格
        vo.setNextThreshold(null);
        vo.setNextPrice(null);
    }

    /**
     * 释放众筹资金给商家（商家上传图片后调用）
     *
     * @param projectId 众筹项目ID
     * @return 转账订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String releaseCrowdfundingFunds(Long projectId) {
        log.info("[众筹资金释放] 开始处理项目ID: {}", projectId);

        try {
            // 1. 查询众筹项目信息
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(projectId);
            if (project == null) {
                log.error("[众筹资金释放] 项目不存在: 项目ID={}", projectId);
                throw new ServiceException("众筹项目不存在");
            }

            // 2. 验证项目状态（只允许成功或已发布的项目释放资金）
            if (project.getStatus() == null ||
                (!project.getStatus().equals(CrowdfundingProjectStatus.SUCCESS.getCode())
                 && !project.getStatus().equals(CrowdfundingProjectStatus.PUBLISHED.getCode()))) {
                log.error("[众筹资金释放] 项目状态不允许释放资金: 项目ID={}, 状态={}", projectId, project.getStatus());
                throw new ServiceException("只有众筹成功或已发布的项目才能释放资金");
            }

            // 3. 验证托管状态（确保资金还未释放）
            if (project.getEscrowStatus() == null || project.getEscrowStatus() != 0) {
                log.error("[众筹资金释放] 资金状态不允许释放: 项目ID={}, 托管状态={}", projectId, project.getEscrowStatus());
                throw new ServiceException("资金已释放或状态异常");
            }

            // 3.1 验证审核状态（必须审核通过才能释放资金）
            if (project.getFundReleaseAuditStatus() == null || project.getFundReleaseAuditStatus() != 2) {
                log.error("[众筹资金释放] 审核状态不允许释放: 项目ID={}, 审核状态={}", projectId, project.getFundReleaseAuditStatus());
                throw new ServiceException("资金释放申请尚未审核通过，无法释放资金");
            }

            // 4. 验证是否上传了实物照片
            if (StringUtils.isBlank(project.getManufacturerPhotos())) {
                log.error("[众筹资金释放] 商家尚未上传实物照片: 项目ID={}", projectId);
                throw new ServiceException("商家尚未上传实物照片，无法释放资金");
            }

            // 5. 获取商家用户信息（需要支付宝账号）
            Long manufacturerUserId = project.getManufacturerUserId();

            // 查询商家用户信息
            SysUser manufacturer = userService.selectUserById(manufacturerUserId);
            if (manufacturer == null) {
                log.error("[众筹资金释放] 商家用户不存在: 商家用户ID={}", manufacturerUserId);
                throw new ServiceException("商家用户不存在");
            }

            // 验证商家是否绑定支付宝账号
            if (!"1".equals(manufacturer.getAlipayBindStatus())) {
                log.error("[众筹资金释放] 商家尚未绑定支付宝账号: 商家用户ID={}", manufacturerUserId);
                throw new ServiceException("商家尚未绑定支付宝账号，无法释放资金");
            }

            // 验证支付宝账号是否为空
            if (StringUtils.isBlank(manufacturer.getAlipayAccount())) {
                log.error("[众筹资金释放] 商家支付宝账号为空: 商家用户ID={}", manufacturerUserId);
                throw new ServiceException("商家支付宝账号为空，无法释放资金");
            }

            // 6. 调用支付服务进行资金释放
            String projectNo = project.getProjectNo();
            String payeeName = project.getManufacturerName();
            BigDecimal releaseAmount = project.getCurrentAmount(); // 释放已筹集的全部金额

            log.info("[众筹资金释放] 准备转账: 项目ID={}, 收款方={}, 金额={}",
                projectId, payeeName, releaseAmount);

            // 使用商家真实的支付宝账号
            String payeeAccount = manufacturer.getAlipayAccount();
            String payeeRealName = manufacturer.getAlipayRealName();

            String transferOrderNo = aliPayService.releaseCrowdfundingFunds(
                projectNo,           // 业务订单号（使用项目编号）
                payeeAccount,        // 商家支付宝账号（真实账号）
                payeeRealName,       // 商家实名姓名（真实姓名）
                releaseAmount,       // 释放金额
                project.getTitle()   // 项目标题
            );

            // 7. 更新项目托管状态为已释放
            project.setEscrowStatus(1); // 1=已释放给厂家
            project.setFundReleaseTime(new Date());
            int updateResult = crowdfundingProjectMapper.updateById(project);

            if (updateResult > 0) {
                log.info("[众筹资金释放] 成功: 项目ID={}, 转账订单号={}", projectId, transferOrderNo);
                return transferOrderNo;
            } else {
                log.error("[众筹资金释放] 更新项目状态失败: 项目ID={}", projectId);
                throw new ServiceException("更新项目状态失败");
            }

        } catch (ServiceException e) {
            log.error("[众筹资金释放] 业务异常: 项目ID={}, 错误={}", projectId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[众筹资金释放] 系统异常: 项目ID={}, 异常=", projectId, e);
            throw new ServiceException("释放资金失败: " + e.getMessage());
        }
    }

    /**
     * 审核资金释放申请
     *
     * @param projectId 众筹项目ID
     * @param auditStatus 审核状态：2=审核通过，3=审核拒绝
     * @param auditRemark 审核备注
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditFundRelease(Long projectId, Integer auditStatus, String auditRemark) {
        log.info("[资金释放审核] 开始审核: 项目ID={}, 审核状态={}, 审核备注={}", projectId, auditStatus, auditRemark);

        try {
            // 1. 查询众筹项目信息
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(projectId);
            if (project == null) {
                log.error("[资金释放审核] 项目不存在: 项目ID={}", projectId);
                throw new ServiceException("众筹项目不存在");
            }

            // 2. 验证项目状态（只允许成功或已发布的项目进行审核）
            if (project.getStatus() == null ||
                (!project.getStatus().equals(CrowdfundingProjectStatus.SUCCESS.getCode())
                 && !project.getStatus().equals(CrowdfundingProjectStatus.PUBLISHED.getCode()))) {
                log.error("[资金释放审核] 项目状态不允许审核: 项目ID={}, 状态={}", projectId, project.getStatus());
                throw new ServiceException("只有众筹成功或已发布的项目才能审核资金释放申请");
            }

            // 3. 验证审核状态（必须是待审核状态）
            if (project.getFundReleaseAuditStatus() == null || project.getFundReleaseAuditStatus() != 1) {
                log.error("[资金释放审核] 审核状态不允许审核: 项目ID={}, 审核状态={}", projectId, project.getFundReleaseAuditStatus());
                throw new ServiceException("当前状态不允许审核，只能审核待审核状态的申请");
            }

            // 4. 验证审核状态值
            if (auditStatus == null || (auditStatus != 2 && auditStatus != 3)) {
                log.error("[资金释放审核] 审核状态值不正确: 项目ID={}, 审核状态={}", projectId, auditStatus);
                throw new ServiceException("审核状态值不正确，必须是2（审核通过）或3（审核拒绝）");
            }

            // 5. 获取当前登录用户（审核人）
            Long auditUserId = LoginHelper.getUserId();
            String auditUserName = LoginHelper.getUsername();

            // 6. 更新审核状态
            project.setFundReleaseAuditStatus(auditStatus);
            project.setAuditRemark(auditRemark);
            project.setAuditUserId(auditUserId);
            project.setAuditTime(new Date());

            int updateResult = crowdfundingProjectMapper.updateById(project);

            if (updateResult > 0) {
                if (auditStatus == 2) {
                    log.info("[资金释放审核] 审核通过: 项目ID={}, 审核人={}, 审核备注={}", projectId, auditUserName, auditRemark);

                    // 审核通过后，自动执行资金释放
                    try {
                        String transferOrderNo = releaseCrowdfundingFunds(projectId);
                        log.info("[资金释放审核] 审核通过后自动释放资金成功: 项目ID={}, 转账订单号={}", projectId, transferOrderNo);
                    } catch (Exception e) {
                        log.error("[资金释放审核] 审核通过后自动释放资金失败: 项目ID={}", projectId, e);
                        // 审核状态已经更新为通过，但资金释放失败，这里记录错误但不影响审核结果
                        // 可以后续通过其他方式重新触发资金释放
                        throw new ServiceException("审核通过，但资金释放失败: " + e.getMessage());
                    }
                } else {
                    log.info("[资金释放审核] 审核拒绝: 项目ID={}, 审核人={}, 审核备注={}", projectId, auditUserName, auditRemark);
                }
                return true;
            } else {
                log.error("[资金释放审核] 更新项目状态失败: 项目ID={}", projectId);
                throw new ServiceException("更新项目状态失败");
            }

        } catch (ServiceException e) {
            log.error("[资金释放审核] 业务异常: 项目ID={}, 错误={}", projectId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[资金释放审核] 系统异常: 项目ID={}, 异常=", projectId, e);
            throw new ServiceException("审核失败: " + e.getMessage());
        }

    }

    @Override
    public TableDataInfo<SdCrowdfundingProject> getPendingFundReleasePage(PageQuery pageQuery) {
        Page<SdCrowdfundingProject> page = pageQuery.build();
        LambdaQueryWrapper<SdCrowdfundingProject> lqw = new LambdaQueryWrapper<>();
        lqw.and(wrapper -> wrapper
                .eq(SdCrowdfundingProject::getStatus, CrowdfundingProjectStatus.SUCCESS.getCode()) // 众筹成功
                .or()
                .eq(SdCrowdfundingProject::getStatus, CrowdfundingProjectStatus.PUBLISHED.getCode())) // 已发布
           .eq(SdCrowdfundingProject::getEscrowStatus, 0) // 托管中
           .eq(SdCrowdfundingProject::getFundReleaseAuditStatus, 1) // 待审核
           .orderByDesc(SdCrowdfundingProject::getCreateTime);

        Page<SdCrowdfundingProject> result = crowdfundingProjectMapper.selectPage(page, lqw);
        return TableDataInfo.build(result);
    }

}
