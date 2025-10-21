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
import com.sutran.sd.pay.domain.PayOrder;
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

        // 2. 厂家匹配：如果当前邀约的被邀约人不等于传入厂家，则在同一作品+发起人的邀约组中尝试定位该厂家
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
            int updateCount = invitationMapper.updateStatusById(invitationDetail.getId(), 7);
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
        // 使用多表联查获取进行中的众筹项目列表（带正确图片）
        List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectActiveCrowdfundingProjectsWithImage();

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
            // createBy, createTime, updateBy, updateTime 字段由 BaseEntity 自动填充

            supportMapper.insert(support);
            log.info("参与者数据落库成功: 订单号={}", orderNo);

            // 4. 扣除订单金额（Redis）
            boolean deducted = crowdfundingRedisService.tryDeductAmount(supportDTO.getProjectId(), supportDTO.getSupportAmount());
            if (!deducted) {
                throw new RuntimeException("众筹金额不足，无法创建订单");
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


}
