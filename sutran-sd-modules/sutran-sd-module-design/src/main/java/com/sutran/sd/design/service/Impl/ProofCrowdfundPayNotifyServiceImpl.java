package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.design.config.CrowdfundingConfig;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.enums.CrowdfundingProjectStatus;
import com.sutran.sd.design.enums.CrowdfundingSupportStatus;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayOrderService;
import com.sutran.sd.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 众筹订单支付回调
 * @author zj
 * @date 2025年10月18日 11:30
 */
@Slf4j
@Service(PayNotifyServer.PROOF_CROWDFUND_NOTIFY)
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class ProofCrowdfundPayNotifyServiceImpl extends BasePayNotifyService {

    private final SdCrowdfundingSupportMapper supportMapper;
    private final CrowdfundingRedisService crowdfundingRedisService;
    private final SdCrowdfundingProjectMapper crowdfundingProjectMapper;
    private final CrowdfundingConfig crowdfundingConfig;
    private final SdProofingInvitationMapper proofingInvitationMapper;
    private final AliPayService aliPayService;
    private final PayOrderService payOrderService;

    /**
     * 处理支付成功业务
     * @param tradeStatus 交易状态
     * @param outTradeNo  商户订单号
     * @param tradeNo     支付宝交易流水号
     * @param totalAmount 实际支付金额
     * @param gmtPayment  支付时间
     * @param businessId  业务id
     * @param userId      用户id
     */
    @Override
    public void handleSuccessBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Long businessId, Long userId) {
        SdCrowdfundingSupport support= supportMapper.selectByOrderNo(outTradeNo);
        Long projectId = support.getProjectId();
        BigDecimal amount = new BigDecimal(totalAmount);

        // 查询项目信息
//        SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(projectId);
        SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
        if (project != null) {
            // 更新项目金额和支持人数
            project.setCurrentAmount(project.getCurrentAmount().add(amount));
            project.setSupportCount(project.getSupportCount() + 1);

            // 检查是否达到目标金额
            if (project.getCurrentAmount().compareTo(project.getTargetAmount()) >= 0) {
                // 众筹成功，自动开始抽奖
                project.setStatus(CrowdfundingProjectStatus.SUCCESS.getCode());
                project.setDrawStatus(1);
                project.setDrawTime(new Date()); // 记录抽奖开始时间
                log.info("众筹成功，自动开始抽奖: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());

                // 更新项目状态
                crowdfundingProjectMapper.updateSdCrowdfundingProject(project);

                // 延迟执行抽奖
                scheduleDrawExecution(project);
            }
            else {
                // 更新项目金额
                crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
            }
        }
    }

    /**
     * 处理支付失败业务
     * @param tradeStatus 交易状态
     * @param outTradeNo  商户订单号
     * @param tradeNo     支付宝交易流水号
     * @param totalAmount 实际支付金额
     * @param gmtPayment  支付时间
     */
    @Override
    public void handleFailedBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment) {
        SdCrowdfundingSupport support= supportMapper.selectByOrderNo(outTradeNo);
        Long projectId = support.getProjectId();
        BigDecimal amount = new BigDecimal(totalAmount);
        // 支付失败回滚金额
        handleRollback(outTradeNo,projectId,amount);
    }

    @Override
    public void dealPayTimeoutData(PayTimeoutStatusVo vo) {
        try {


            handleRollback(vo.getOutTradeNo(),vo.getProjectid(),vo.getAmount());
            // 支付成功 或 完成
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(vo.getTradeStatus()) || AliPayTradeStatus.TRADE_FINISHED.name().equals(vo.getTradeStatus())) {
                return;

            }

        }
        catch (Exception e) {
            log.error("[众筹打样支付超时]>>>>>>>>>超时业务逻辑处理异常,异常信息: ", e);

        }
    }


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
                loser.setDrawStatus(3); // 未中奖
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
                // 从打样邀约中获取发起人的收货信息
                if (project.getProofingInvitationId() != null) {
                    try {
                        SdProofingInvitation invitation = proofingInvitationMapper.selectById(project.getProofingInvitationId());
                        if (invitation != null) {
                            initiatorSupport.setReceiverName(invitation.getReceiverName());
                            initiatorSupport.setReceiverPhone(invitation.getReceiverPhone());
                            initiatorSupport.setReceiverAddress(invitation.getReceiverAddress());
                            initiatorSupport.setReceiverArea(invitation.getReceiverArea());
                            log.info("从打样邀约获取发起人收货信息: 邀约ID={}, 收货人={}, 电话={}",
                                project.getProofingInvitationId(), invitation.getReceiverName(), invitation.getReceiverPhone());
                        } else {
                            log.warn("未找到对应的打样邀约: 邀约ID={}", project.getProofingInvitationId());
                            // 使用发起人姓名作为备选
                            initiatorSupport.setReceiverName(project.getCreatorName());
                            initiatorSupport.setReceiverPhone("");
                            initiatorSupport.setReceiverAddress("");
                            initiatorSupport.setReceiverArea("");
                        }
                    } catch (Exception e) {
                        log.error("查询打样邀约失败: 邀约ID={}", project.getProofingInvitationId(), e);
                        // 使用发起人姓名作为备选
                        initiatorSupport.setReceiverName(project.getCreatorName());
                        initiatorSupport.setReceiverPhone("");
                        initiatorSupport.setReceiverAddress("");
                        initiatorSupport.setReceiverArea("");
                    }
                } else {
                    log.warn("众筹项目未关联打样邀约: 项目ID={}", project.getId());
                    // 使用发起人姓名作为备选
                    initiatorSupport.setReceiverName(project.getCreatorName());
                    initiatorSupport.setReceiverPhone("");
                    initiatorSupport.setReceiverAddress("");
                    initiatorSupport.setReceiverArea("");
                }

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

    /**
     * 处理众筹退款（内部方法）
     * @param orderNo 订单号
     * @param refundReason 退款原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void processCrowdfundingRefund(String orderNo, String refundReason) {
        try {
            log.info("[众筹订单] 开始处理退款: 订单号={}, 退款原因={}", orderNo, refundReason);

            // 1. 查询众筹支持记录
            SdCrowdfundingSupport support = supportMapper.selectByOrderNo(orderNo);
            if (support == null) {
                log.error("[众筹订单] 退款失败: 未找到支持记录, 订单号={}", orderNo);
                return;
            }

            // 2. 检查支持状态
            if (Objects.equals(support.getStatus(), CrowdfundingSupportStatus.REFUNDED.getCode())) {
                log.warn("[众筹订单] 退款失败: 订单已经退款, 订单号={}", orderNo);
                return;
            }

            // 3. 查询支付订单获取支付宝交易号
            PayOrder payOrder = payOrderService.detailByOutTradeNo(orderNo);
            if (payOrder == null) {
                log.error("[众筹订单] 退款失败: 未找到支付订单, 订单号={}", orderNo);
                return;
            }

            if (StringUtils.isBlank(payOrder.getTradeNo())) {
                log.error("[众筹订单] 退款失败: 支付订单缺少支付宝交易号, 订单号={}", orderNo);
                return;
            }

            // 4. 调用支付宝退款API
            String reason = StringUtils.isNotBlank(refundReason) ? refundReason : "众筹项目退款";
            aliPayService.tradeRefund(orderNo, payOrder.getTradeNo(), support.getSupportAmount().setScale(2, RoundingMode.HALF_UP).toString(), reason);

            // 5. 更新众筹支持记录状态为已退款
            support.setStatus(CrowdfundingSupportStatus.REFUNDED.getCode());
            support.setRefundTime(new Date());
            support.setRefundReason(reason);
            supportMapper.updateById(support);

            // 6. 更新支付订单状态 调用支付宝退款的方法中就更新了，这里就不需要再更新了
            //payOrderService.updateRefundStatus(orderNo, support.getSupportAmount(), refundReason);

            // 7. 回滚项目金额和支持人数
            rollbackProjectAmount(support.getProjectId(), support.getSupportAmount());

            log.info("[众筹订单] 退款成功: 订单号={}, 退款金额={}, 支付宝交易号={}",
                orderNo, support.getSupportAmount(), payOrder.getTradeNo());

        } catch (Exception e) {
            log.error("[众筹订单] 退款失败: 订单号={}, 异常：", orderNo, e);
            throw new ServiceException("众筹订单退款失败: " + e.getMessage());
        }
    }

    /**
     * 回滚项目金额和支持人数
     * @param projectId 项目ID
     * @param amount 退款金额
     */
    private void rollbackProjectAmount(Long projectId, BigDecimal amount) {
        try {
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
            if (project != null) {
                // 回滚当前金额
                BigDecimal newCurrentAmount = project.getCurrentAmount().subtract(amount);
                project.setCurrentAmount(newCurrentAmount);

                // 回滚支持人数
                Integer newSupportCount = project.getSupportCount() - 1;
                project.setSupportCount(newSupportCount);

                // 如果回滚后金额低于目标金额，需要重新调整项目状态
                if (newCurrentAmount.compareTo(project.getTargetAmount()) < 0) {
                    project.setStatus(CrowdfundingProjectStatus.FUNDING.getCode()); // 重新设为进行中
                    project.setDrawStatus(0); // 重置抽奖状态
                    project.setDrawTime(null); // 清空抽奖时间
                    log.info("[众筹订单] 项目状态回滚: 项目ID={}, 当前金额={}, 目标金额={}",
                        projectId, newCurrentAmount, project.getTargetAmount());
                }

                crowdfundingProjectMapper.updateSdCrowdfundingProject(project);

                // 同时回滚Redis中的金额
                crowdfundingRedisService.refundAmount(projectId, amount);

                log.info("[众筹订单] 项目金额回滚成功: 项目ID={}, 回滚金额={}, 新当前金额={}, 新支持人数={}",
                    projectId, amount, newCurrentAmount, newSupportCount);
            }
        } catch (Exception e) {
            log.error("[众筹订单] 项目金额回滚失败: 项目ID={}, 回滚金额={}, 异常：", projectId, amount, e);
        }
    }

}
