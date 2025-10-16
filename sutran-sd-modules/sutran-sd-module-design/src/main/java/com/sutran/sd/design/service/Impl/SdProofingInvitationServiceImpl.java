package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationChooseDto;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.InvitationCandidateVO;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.constant.ProofingInvitationConstants;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.design.mapper.SdProofingInvitationCandidateMapper;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitationCandidate;
import com.sutran.sd.design.service.ISdProofingInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 打样邀约服务实现类
 *
 * @author YourName
 */
@Slf4j
@Service
@RequiredArgsConstructor // Lombok注解，用于自动生成包含 final 字段的构造函数，实现依赖注入
public class SdProofingInvitationServiceImpl implements ISdProofingInvitationService {

    /**
     * 注入数据访问层依赖 (Mapper or Repository)
     */
    private final SdProofingInvitationMapper invitationMapper;
    private final SdProofingInvitationCandidateMapper candidateMapper;


    // 使用常量类管理状态，不再定义重复常量



    /**
     * 创建并发送一个新的合作邀约
     */
    @Override
    @Transactional
    public SdProofingInvitation createInvitation(ProofingInvitationRequestDTO createDTO) {

        // 检查当前作品是否已有待回应的邀约
        String workId = createDTO.getWorkId();
        if (workId != null) {
            List<SdProofingInvitation> existingInvitations = invitationMapper.selectList(
                new LambdaQueryWrapper<SdProofingInvitation>()
                    .eq(SdProofingInvitation::getWorkId, workId)
                    .eq(SdProofingInvitation::getStatus, ProofingInvitationConstants.STATUS_REPLYING)
            );

            if (!existingInvitations.isEmpty()) {
                throw new ServiceException("当前作品已有待回应的邀约，无法重复发起");
            }
        }


        // 1. 获取当前登录用户ID
        Long senderId = LoginHelper.getUserId();

        // 2. 获取并校验接收者ID（优先使用数组，兼容单个，最多3个）
        Set<Long> inviteeIds = new HashSet<>();



        // 优先处理 inviteeUserIds 数组
        if (createDTO.getInviteeUserIds() != null && !createDTO.getInviteeUserIds().isEmpty()) {
            // 过滤掉无效的ID（null、0、负数）
            List<Long> validIds = createDTO.getInviteeUserIds().stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
            inviteeIds.addAll(validIds);
            log.info("从 inviteeUserIds 数组中获取到 {} 个有效ID: {}", validIds.size(), validIds);
        }


        log.info("最终确定的邀约接收者ID列表: {}", inviteeIds);
        if (inviteeIds.isEmpty()) {
            throw new ServiceException("邀约的接收用户不能为空");
        }
        if (inviteeIds.size() > 3) {
            throw new ServiceException("最多可选择3个被邀约厂家");
        }

        // 3. 验证取消时限值
        if (createDTO.getCancelTimeLimit() != null &&
            !ProofingInvitationConstants.isValidCancelTimeLimit(createDTO.getCancelTimeLimit())) {
            throw new ServiceException("自动取消时限值无效，必须是1、2、3中的一个");
        }

        // 4. 验证抽奖数量
        if (createDTO.getDrawNumber() == null || createDTO.getDrawNumber() < 1 ||createDTO.getDrawNumber() > createDTO.getProofingQuantity()) {
            throw new ServiceException("抽奖数量必须大于等于1且不得超过打样数量");
        }

        // 5. 为每个被邀约人创建独立的邀约记录
        SdProofingInvitation createdInvitation = null;
        for (Long inviteeId : inviteeIds) {
            SdProofingInvitation invitation = new SdProofingInvitation();
            BeanUtils.copyProperties(createDTO, invitation);
            invitation.setInviterUserId(senderId);
            invitation.setInviteeUserId(inviteeId); // 设置被邀约人ID
            invitation.setStatus(ProofingInvitationConstants.STATUS_REPLYING); // 发起人创建邀约时状态为待回应

            // 保存到数据库
            invitationMapper.insert(invitation);
            createdInvitation = invitation; // 记录最后一个创建的邀约，作为返回结果
        }

        return createdInvitation;
    }

    /**
     * 获取当前用户收到的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getReceivedInvitations() {
        Long currentUserId = LoginHelper.getUserId();
        log.info("开始查询用户 {} 收到的邀约", currentUserId);

        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectReceivedInvitationList(currentUserId);
        log.info("数据库查询返回 {} 条记录", invitationList != null ? invitationList.size() : "null");

        if (invitationList == null || invitationList.isEmpty()) {
            log.info("用户 {} 没有收到任何邀约", currentUserId);
            return new ArrayList<>(); // 返回空列表而不是null
        }

        // 状态转换：候选人看到的邀约状态转换
        // 如果状态是4（待回应），需要根据是否已提交报价来判断显示状态
        for (ProofingInvitationDetailVO invitation : invitationList) {
            if (ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
                // 如果已经提交了报价（quoteSubmitAt不为空），显示为5（已处理）
                if (invitation.getQuoteSubmitAt() != null) {
                    invitation.setStatus(ProofingInvitationConstants.STATUS_PROCESSED); // 候选人看到的是已处理状态
                } else {
                    // 如果还没有提交报价，显示为0（待处理）
                    invitation.setStatus(ProofingInvitationConstants.STATUS_PENDING); // 候选人看到的是待处理状态
                }
            }
        }

        log.info("用户 {} 收到 {} 个邀约", currentUserId, invitationList.size());
        return invitationList;
    }

    /**
     * 获取当前用户发出的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getSentInvitations() {
        Long currentUserId = LoginHelper.getUserId();
        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectSentInvitationList(currentUserId);

        if (invitationList == null || invitationList.isEmpty()) {
            log.info("用户 {} 没有发出任何邀约", currentUserId);
            return new ArrayList<>();
        }

        // 按work_id和inviter_user_id分组去重，避免同一邀约显示多条记录
        Map<String, ProofingInvitationDetailVO> uniqueInvitations = new LinkedHashMap<>();
        for (ProofingInvitationDetailVO invitation : invitationList) {
            String key = invitation.getWorkId() + "_" + invitation.getInviterUserId();
            if (!uniqueInvitations.containsKey(key)) {
                // 状态转换：发起人查看时，如果状态是4（待回应），显示为6（待确认）
                if (ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
                    invitation.setStatus(ProofingInvitationConstants.STATUS_PENDING_CONFIRMATION);
                }
                uniqueInvitations.put(key, invitation);
            }
        }

        List<ProofingInvitationDetailVO> result = new ArrayList<>(uniqueInvitations.values());
        log.info("用户 {} 发出 {} 个邀约（去重后）", currentUserId, result.size());
        return result;
    }

    /**
     * 分页查询当前用户收到的邀约列表
     */
    @Override
    public TableDataInfo<ProofingInvitationDetailVO> getReceivedInvitationsPage(PageQuery pageQuery) {
        Long currentUserId = LoginHelper.getUserId();
        log.info("开始分页查询用户 {} 收到的邀约，分页参数：pageNum={}, pageSize={}",
                currentUserId, pageQuery.getPageNum(), pageQuery.getPageSize());

        IPage<ProofingInvitationDetailVO> page = invitationMapper.selectReceivedInvitationPage(pageQuery.build(), currentUserId);
        log.info("分页查询返回 {} 条记录", page.getRecords() != null ? page.getRecords().size() : "null");

        // 状态转换：候选人看到的邀约状态转换
        // 如果状态是4（待回应），需要根据是否已提交报价来判断显示状态
        if (page.getRecords() != null) {
            for (ProofingInvitationDetailVO invitation : page.getRecords()) {
                if (ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
                    // 如果已经提交了报价（quoteSubmitAt不为空），显示为5（已处理）
                    if (invitation.getQuoteSubmitAt() != null) {
                        invitation.setStatus(ProofingInvitationConstants.STATUS_PROCESSED); // 候选人看到的是已处理状态
                    } else {
                        // 如果还没有提交报价，显示为0（待处理）
                        invitation.setStatus(ProofingInvitationConstants.STATUS_PENDING); // 候选人看到的是待处理状态
                    }
                }
            }
        }

        // 手动设置total，因为自定义SQL可能无法被分页插件正确计算
        Long total = invitationMapper.countReceivedInvitations(currentUserId);
        log.info("统计查询返回总数：{}", total);
        page.setTotal(total);

        return TableDataInfo.build(page);
    }

    /**
     * 分页查询当前用户发出的邀约列表
     */
    @Override
    public TableDataInfo<ProofingInvitationDetailVO> getSentInvitationsPage(PageQuery pageQuery) {
        Long currentUserId = LoginHelper.getUserId();
        IPage<ProofingInvitationDetailVO> page = invitationMapper.selectSentInvitationPage(pageQuery.build(), currentUserId);

        // 对结果进行去重处理
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            Map<String, ProofingInvitationDetailVO> uniqueInvitations = new LinkedHashMap<>();
            for (ProofingInvitationDetailVO invitation : page.getRecords()) {
                String key = invitation.getWorkId() + "_" + invitation.getInviterUserId();
                if (!uniqueInvitations.containsKey(key)) {
                    // 状态转换：发起人查看时，如果状态是4（待回应），显示为6（待确认）
                    if (ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
                        invitation.setStatus(ProofingInvitationConstants.STATUS_PENDING_CONFIRMATION);
                    }
                    uniqueInvitations.put(key, invitation);
                }
            }
            page.setRecords(new ArrayList<>(uniqueInvitations.values()));
        }

        // 计算去重后的总数
        List<ProofingInvitationDetailVO> allInvitations = invitationMapper.selectSentInvitationList(currentUserId);
        Map<String, ProofingInvitationDetailVO> uniqueAllInvitations = new LinkedHashMap<>();
        if (allInvitations != null && !allInvitations.isEmpty()) {
            for (ProofingInvitationDetailVO invitation : allInvitations) {
                String key = invitation.getWorkId() + "_" + invitation.getInviterUserId();
                if (!uniqueAllInvitations.containsKey(key)) {
                    uniqueAllInvitations.put(key, invitation);
                }
            }
        }
        page.setTotal((long) uniqueAllInvitations.size());

        return TableDataInfo.build(page);
    }

    /**
     * 获取某邀约下的候选厂家列表（含报价与用户信息）
     */
    public java.util.List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> getInvitationCandidates(Long invitationId) {
        Long currentUserId = LoginHelper.getUserId();
        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("邀约不存在");
        }
        if (!invitation.getInviterUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，只有发起人可查看候选列表");
        }
        return candidateMapper.selectCandidateVOs(invitationId);
    }

    /**
     * 接受合作邀约
     */
    @Override
    @Transactional
    public void acceptInvitation(ProofingInvitationAcceptDto  acceptDTO) {


        Long currentUserId = LoginHelper.getUserId();


        SdProofingInvitation invitation = invitationMapper.selectById(acceptDTO.getInvitationId());
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }


        // 检查当前用户是否为被邀约人
        if (!currentUserId.equals(invitation.getInviteeUserId())) {
            throw new ServiceException("权限不足，您不是该邀约的被邀约人");
        }

        if (!ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理或已取消，无法接受");
        }

        // 参数基础校验
        if (acceptDTO.getQuotedPrice() == null || acceptDTO.getQuotedPrice().signum() < 0) {
            throw new ServiceException("报价金额无效");
        }
        if (acceptDTO.getQuotedPeriodDays() == null || acceptDTO.getQuotedPeriodDays() <= 0) {
            throw new ServiceException("预计打样周期必须大于0");
        }
        if (acceptDTO.getProfitShareRatio() != null) {
            if (acceptDTO.getProfitShareRatio().compareTo(new java.math.BigDecimal("0")) < 0
                || acceptDTO.getProfitShareRatio().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new ServiceException("利润分成比例区间为0-100");
            }
        }

        // 条件验证：如果提供批量生产方案，阶梯价格必填
        if (Boolean.TRUE.equals(acceptDTO.getIsQuoteBatchPlan())) {
            if (acceptDTO.getTieredQuantities() == null || acceptDTO.getTieredQuantities().isEmpty() ||
                acceptDTO.getTieredPrices() == null || acceptDTO.getTieredPrices().isEmpty()) {
                throw new ServiceException("提供批量生产方案时，阶梯价格不能为空");
            }

            List<Integer> quantities = acceptDTO.getTieredQuantities();
            List<java.math.BigDecimal> prices = acceptDTO.getTieredPrices();

            // 验证数组长度：数量点数组长度应该等于价格数组长度
            // 每个阶梯都有对应的数量点和价格
            if (quantities.size() != prices.size()) {
                throw new ServiceException("阶梯价格设置错误：数量点数量必须等于价格数量");
            }

            // 验证数量点不能为null、0或负数
            if (quantities.stream().anyMatch(qty -> qty == null || qty <= 0)) {
                throw new ServiceException("阶梯数量点必须大于0");
            }

            // 验证价格点不能为null、0或负数
            if (prices.stream().anyMatch(price -> price == null || price.signum() <= 0)) {
                throw new ServiceException("阶梯价格必须大于0");
            }

            // 验证数量点必须递增（阶梯起始点递增）
            // 注意：tieredQuantities实际上是价格数组，tieredPrices实际上是数量点数组
            for (int i = 1; i < prices.size(); i++) {
                if (prices.get(i).compareTo(prices.get(i - 1)) <= 0) {
                    throw new ServiceException("阶梯数量点必须从小到大递增");
                }
            }

            // 验证价格必须递减（数量越多价格越低）
            // 注意：tieredQuantities实际上是价格数组，tieredPrices实际上是数量点数组
            for (int i = 1; i < quantities.size(); i++) {
                if (quantities.get(i).compareTo(quantities.get(i - 1)) >= 0) {
                    throw new ServiceException("阶梯价格必须从高到低递减（数量越多价格越低）");
                }
            }
        }

        // 处理阶梯价格为JSON - 正确的阶梯价格逻辑
        String tieredPricingJson = null;
        if (acceptDTO.getTieredQuantities() != null && !acceptDTO.getTieredQuantities().isEmpty() &&
            acceptDTO.getTieredPrices() != null && !acceptDTO.getTieredPrices().isEmpty()) {
            try {
                // 正确的阶梯价格格式：注意tieredQuantities实际上是价格数组，tieredPrices实际上是数量点数组
                List<java.util.Map<String, Object>> tieredPricingList = new ArrayList<>();
                List<Integer> quantities = acceptDTO.getTieredQuantities(); // 实际上是价格数组
                List<java.math.BigDecimal> prices = acceptDTO.getTieredPrices(); // 实际上是数量点数组

                // 第一个阶梯：从0开始到第一个数量点-1
                java.util.Map<String, Object> firstTier = new java.util.HashMap<>();
                //firstTier.put("minQty", 0);
                //firstTier.put("maxQty", prices.get(0).intValue() - 1);
                firstTier.put("unitPrice", quantities.get(0));
                firstTier.put("node", prices.get(0).intValue());
                tieredPricingList.add(firstTier);

                // 中间阶梯：从数量点i开始到数量点i+1-1
                for (int i = 0; i < prices.size() - 2; i++) {
                    java.util.Map<String, Object> tier = new java.util.HashMap<>();
                    //tier.put("minQty", prices.get(i).intValue());
                    //tier.put("maxQty", prices.get(i + 1).intValue() - 1);
                    tier.put("unitPrice", quantities.get(i + 1));
                    tier.put("node", prices.get(i+1).intValue());
                    tieredPricingList.add(tier);
                }

                // 最后一个阶梯：从最后一个数量点开始，无上限
                java.util.Map<String, Object> lastTier = new java.util.HashMap<>();
                //lastTier.put("minQty", prices.get(prices.size() - 1).intValue());
                //lastTier.put("maxQty", null);
                lastTier.put("unitPrice", quantities.get(quantities.size() - 1));
                lastTier.put("node", prices.get(prices.size() - 1).intValue());
                tieredPricingList.add(lastTier);

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                tieredPricingJson = mapper.writeValueAsString(tieredPricingList);

                log.info("阶梯价格JSON生成成功：{}", tieredPricingJson);
            } catch (Exception e) {
                log.error("阶梯价格序列化失败", e);
                throw new ServiceException("阶梯价格序列化失败");
            }
        }

        // 更新邀约状态和报价信息
        invitation.setQuotedPrice(acceptDTO.getQuotedPrice());
        invitation.setQuotedPeriodDays(acceptDTO.getQuotedPeriodDays());
        invitation.setIsQuoteBatchPlan(Boolean.TRUE.equals(acceptDTO.getIsQuoteBatchPlan()));
        invitation.setQuoteSubmitAt(new java.util.Date());
        invitation.setTieredPricing(tieredPricingJson);
        invitation.setProfitShareRatio(acceptDTO.getProfitShareRatio());
        invitation.setStatus(ProofingInvitationConstants.STATUS_REPLYING);

        int rows = invitationMapper.updateById(invitation);

        if (rows == 0) {
            throw new ServiceException("操作失败，请重试");
        }

        // 在候选人表中插入记录
        SdProofingInvitationCandidate candidate = new SdProofingInvitationCandidate();
        candidate.setInvitationId(acceptDTO.getInvitationId());
        candidate.setInviteeUserId(currentUserId);
        candidate.setQuotedPrice(acceptDTO.getQuotedPrice());
        candidate.setQuotedPeriodDays(acceptDTO.getQuotedPeriodDays());
        candidate.setIsQuoteBatchPlan(Boolean.TRUE.equals(acceptDTO.getIsQuoteBatchPlan()));
        candidate.setTieredPricing(tieredPricingJson);
        candidate.setProfitShareRatio(acceptDTO.getProfitShareRatio());
        candidate.setQuoteSubmitAt(new java.util.Date());


        int candidateInsertResult = candidateMapper.insert(candidate);
        log.info("候选人记录插入结果：{}，候选人ID：{}", candidateInsertResult, candidate.getId());

        log.info("用户 {} 接受邀约 {} 成功，候选人记录已创建", currentUserId, acceptDTO.getInvitationId());
    }

    /**
     * 拒绝合作邀约
     */
    @Override
    @Transactional
    public void rejectInvitation(Long invitationId) {
        // 准备工作：获取当前操作的用户ID
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }

        // 检查当前用户是否为被邀约人
        if (!currentUserId.equals(invitation.getInviteeUserId())) {
            throw new ServiceException("权限不足，您不是该邀约的被邀约人");
        }

        if (!ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理或已取消，无法拒绝");
        }

        // 更新当前邀约状态为已拒绝
        invitation.setStatus(ProofingInvitationConstants.STATUS_REJECTED);

        int rows = invitationMapper.updateById(invitation);
        if (rows == 0) {
            throw new ServiceException("操作失败，请重试");
        }

        // 检查是否所有候选人都拒绝了
        // 查询同一个work_id下的所有邀约
        List<SdProofingInvitation> allInvitations = invitationMapper.selectList(
            new LambdaQueryWrapper<SdProofingInvitation>()
                .eq(SdProofingInvitation::getWorkId, invitation.getWorkId())
                .eq(SdProofingInvitation::getInviterUserId, invitation.getInviterUserId())
        );

        // 检查是否所有邀约都被拒绝或取消
        boolean allRejectedOrCancelled = true;
        for (SdProofingInvitation inv : allInvitations) {
            if (!ProofingInvitationConstants.STATUS_REJECTED.equals(inv.getStatus()) &&
                !ProofingInvitationConstants.STATUS_CANCELLED.equals(inv.getStatus())) {
                allRejectedOrCancelled = false;
                break;
            }
        }

        // 如果所有候选人都拒绝了，更新主邀约状态
        if (allRejectedOrCancelled) {
            // 找到主邀约（通常是第一个创建的）
            SdProofingInvitation mainInvitation = allInvitations.stream()
                .min((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                .orElse(invitation);

            if (!mainInvitation.getId().equals(invitationId)) {
                mainInvitation.setStatus(ProofingInvitationConstants.STATUS_REJECTED);
                invitationMapper.updateById(mainInvitation);
                log.info("所有候选人都拒绝了邀约，主邀约状态已更新为已拒绝");
            }
        }

        log.info("用户 {} 拒绝邀约 {} 成功", currentUserId, invitationId);
    }

    /**
     * (发送方)取消已发出的邀约
     */
    @Override
    @Transactional
    public void cancelInvitation(Long invitationId) {
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }

        if (!invitation.getInviterUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的发起人");
        }

        if (!ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理，无法取消");
        }

        invitation.setStatus(ProofingInvitationConstants.STATUS_CANCELLED);

        int rows = invitationMapper.updateById(invitation);
        if (rows == 0) {
            throw new ServiceException("操作失败，请重试");
        }

        log.info("用户 {} 取消邀约 {} 成功", currentUserId, invitationId);
    }

    /**
     * 自动取消超时的邀约
     */
    @Override
    @Transactional
    public void autoCancelExpiredInvitations() {
        // 查询所有待回应状态的邀约（发起人创建邀约后状态为4）
        List<SdProofingInvitation> replyingInvitations = invitationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SdProofingInvitation>()
                .eq(SdProofingInvitation::getStatus, ProofingInvitationConstants.STATUS_REPLYING)
        );

        int cancelledCount = 0;
        for (SdProofingInvitation invitation : replyingInvitations) {
            if (isInvitationExpired(invitation)) {
                invitation.setStatus(ProofingInvitationConstants.STATUS_CANCELLED);
                invitationMapper.updateById(invitation);
                cancelledCount++;
            }
        }

        log.info("自动取消超时邀约完成，共取消 {} 个邀约", cancelledCount);
    }

    /**
     * 发起人从候选厂家中最终选择一家
     */
    @Override
    @Transactional
    public void chooseCandidate(ProofingInvitationChooseDto chooseDto) {
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(chooseDto.getInvitationId());
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }

        if (!invitation.getInviterUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的发起人");
        }

        if (!ProofingInvitationConstants.STATUS_REPLYING.equals(invitation.getStatus())) {
            throw new ServiceException("该邀约当前状态不可进行最终选择");
        }

        SdProofingInvitationCandidate selected = candidateMapper.selectOneByInvitationAndInvitee(chooseDto.getInvitationId(), chooseDto.getInviteeUserId());
        if (selected == null) {
            throw new ServiceException("所选厂家不存在或未提交报价");
        }
        
        // 检查候选人是否已提交报价
        if (selected.getQuoteSubmitAt() == null) {
            throw new ServiceException("所选厂家尚未提交报价");
        }

        // 更新主表最终选择信息
        invitation.setSelectedInviteeUserId(chooseDto.getInviteeUserId());
        invitation.setSelectedAt(new java.util.Date());
        invitation.setQuotedPrice(selected.getQuotedPrice());
        invitation.setQuotedPeriodDays(selected.getQuotedPeriodDays());
        invitation.setIsQuoteBatchPlan(selected.getIsQuoteBatchPlan());
        invitation.setQuoteSubmitAt(selected.getQuoteSubmitAt());
        invitation.setTieredPricing(selected.getTieredPricing());
        invitation.setProfitShareRatio(selected.getProfitShareRatio());
        invitation.setStatus(ProofingInvitationConstants.STATUS_ACCEPTED);
        invitationMapper.updateById(invitation);

        // 更新同一个work_id下的所有邀约记录状态
        List<SdProofingInvitation> allInvitations = invitationMapper.selectList(
            new LambdaQueryWrapper<SdProofingInvitation>()
                .eq(SdProofingInvitation::getWorkId, invitation.getWorkId())
                .eq(SdProofingInvitation::getInviterUserId, invitation.getInviterUserId())
        );

        for (SdProofingInvitation inv : allInvitations) {
            if (inv.getInviteeUserId().equals(chooseDto.getInviteeUserId())) {
                // 被选中的候选人：状态设为已接受
                inv.setStatus(ProofingInvitationConstants.STATUS_ACCEPTED);
                inv.setSelectedInviteeUserId(chooseDto.getInviteeUserId());
                inv.setSelectedAt(new java.util.Date());
                inv.setQuotedPrice(selected.getQuotedPrice());
                inv.setQuotedPeriodDays(selected.getQuotedPeriodDays());
                inv.setIsQuoteBatchPlan(selected.getIsQuoteBatchPlan());
                inv.setQuoteSubmitAt(selected.getQuoteSubmitAt());
                inv.setTieredPricing(selected.getTieredPricing());
                inv.setProfitShareRatio(selected.getProfitShareRatio());
            } else {
                // 其他候选人：状态设为已拒绝
                inv.setStatus(ProofingInvitationConstants.STATUS_REJECTED);
            }
            invitationMapper.updateById(inv);
        }

        // 更新所有候选人的状态
        List<SdProofingInvitationCandidate> candidates = candidateMapper.selectByInvitationId(chooseDto.getInvitationId());
        for (SdProofingInvitationCandidate c : candidates) {
            if (c.getInviteeUserId().equals(chooseDto.getInviteeUserId())) {
                // 被选中的候选人：状态设为已接受
                c.setStatus(ProofingInvitationConstants.STATUS_ACCEPTED);
            } else {
                // 其他候选人：状态设为已拒绝
                c.setStatus(ProofingInvitationConstants.STATUS_REJECTED);
            }
            candidateMapper.updateById(c);
        }
    }

    /**
     * 判断邀约是否已超时
     */
    private boolean isInvitationExpired(SdProofingInvitation invitation) {
        if (invitation.getCancelTimeLimit() == null) {
            return false; // 没有设置取消时限，不自动取消
        }

        // 计算超时时间（天数）
        int expireDays = ProofingInvitationConstants.getCancelTimeDays(invitation.getCancelTimeLimit());

        // 计算创建时间 + 超时天数
        long expireTime = invitation.getCreateTime().getTime() + (expireDays * 24 * 60 * 60 * 1000L);

        // 当前时间是否超过超时时间
        return System.currentTimeMillis() > expireTime;
    }

    /**
     * 查看邀约详情
     */
    @Override
    public ProofingInvitationDetailVO getInvitationDetail(Long invitationId) {
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("邀约不存在或已被删除");
        }

        // 权限检查：只有发起人或被邀约人可以查看详情
        if (!currentUserId.equals(invitation.getInviterUserId()) &&
            !currentUserId.equals(invitation.getInviteeUserId())) {
            throw new ServiceException("权限不足，您无权查看该邀约详情");
        }

        // 直接根据邀约ID查询详情，不依赖去重逻辑
        ProofingInvitationDetailVO detail = null;

        // 先尝试从收到的邀约中查找
        List<ProofingInvitationDetailVO> receivedDetails = invitationMapper.selectReceivedInvitationList(currentUserId);
        for (ProofingInvitationDetailVO receivedDetail : receivedDetails) {
            if (receivedDetail.getId().equals(invitationId)) {
                detail = receivedDetail;
                break;
            }
        }

        // 如果没找到，再从发出的邀约中查找
        if (detail == null) {
            List<ProofingInvitationDetailVO> sentDetails = invitationMapper.selectSentInvitationList(currentUserId);
            for (ProofingInvitationDetailVO sentDetail : sentDetails) {
                if (sentDetail.getId().equals(invitationId)) {
                    detail = sentDetail;
                    break;
                }
            }
        }

        if (detail == null) {
            throw new ServiceException("邀约详情获取失败");
        }

        // 附加候选人列表 - 查询同一个商品当前进行中邀约的候选人
        java.util.List<InvitationCandidateVO> candidates = candidateMapper.selectCandidateVOsByWorkId(detail.getWorkId());
        detail.setCandidates(candidates);

        log.info("邀约 {} 详情查询成功，找到 {} 个候选人", invitationId, candidates != null ? candidates.size() : "null");
        return detail;
    }



    @Override
    public TableDataInfo<ProofingInvitationDetailVO> getMerchantProcessedInvitationsPage(PageQuery pageQuery, Integer status) {
        Long currentUserId = LoginHelper.getUserId();
        log.info("分页查询商家 {} 已处理的邀约，状态：{}", currentUserId, status);

        // 使用现有的分页查询方法，但只查询商家作为被邀约人的邀约
        IPage<ProofingInvitationDetailVO> page = invitationMapper.selectReceivedInvitationPage(
            pageQuery.build(), currentUserId);

        // 如果指定了状态，过滤结果
        if (status != null) {
            page.getRecords().removeIf(record -> !status.equals(record.getStatus()));
        }

        // 手动设置总数
        Long total = invitationMapper.countReceivedInvitations(currentUserId);
        if (status != null) {
            // 如果指定了状态，需要重新计算总数
            total = (long) page.getRecords().size();
        }
        page.setTotal(total);

        log.info("查询完成，共找到 {} 个已处理的邀约", total);
        return TableDataInfo.build(page);
    }

    @Override
    public List<ProofingInvitationDetailVO> getMerchantProcessedInvitationsList(Integer status) {
        Long currentUserId = LoginHelper.getUserId();
        log.info("查询商家 {} 已处理的邀约列表，状态：{}", currentUserId, status);

        List<ProofingInvitationDetailVO> invitations = invitationMapper.selectReceivedInvitationList(currentUserId);

        // 如果指定了状态，过滤结果
        if (status != null) {
            invitations.removeIf(invitation -> !status.equals(invitation.getStatus()));
        }

        log.info("查询完成，共找到 {} 个已处理的邀约", invitations.size());
        return invitations;
    }

    @Override
    public ProofingInvitationDetailVO getMerchantProcessedInvitationById(Long invitationId) {
        if (invitationId == null) {
            throw new ServiceException("邀约ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        log.info("查询商家 {} 已处理的邀约详情，邀约ID：{}", currentUserId, invitationId);

        // 使用现有的详情查询方法
        ProofingInvitationDetailVO invitation = getInvitationDetail(invitationId);

        // 验证是否为当前商家的邀约
        if (!currentUserId.equals(invitation.getInviteeUserId())) {
            throw new ServiceException("邀约不存在或您无权查看该邀约");
        }

        log.info("查询完成，邀约标题：{}", invitation.getProductTitle());
        return invitation;
    }

    @Override
    public Long countMerchantProcessedInvitations(Integer status) {
        Long currentUserId = LoginHelper.getUserId();
        log.info("统计商家 {} 已处理的邀约数量，状态：{}", currentUserId, status);

        Long count = invitationMapper.countReceivedInvitations(currentUserId);

        // 如果指定了状态，需要重新计算
        if (status != null) {
            List<ProofingInvitationDetailVO> invitations = invitationMapper.selectReceivedInvitationList(currentUserId);
            count = invitations.stream()
                .mapToLong(invitation -> status.equals(invitation.getStatus()) ? 1 : 0)
                .sum();
        }

        log.info("统计完成，共 {} 个已处理的邀约", count);
        return count;
    }

}

