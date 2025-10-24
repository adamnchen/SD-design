package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.OrderNumUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.design.domain.SdPresaleOrder;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.enums.PresaleOrderStatus;
import com.sutran.sd.design.enums.PresaleProjectStatus;
import com.sutran.sd.design.dto.PresaleOrderCreateDTO;
import com.sutran.sd.design.dto.PresaleProjectPublishDTO;
import com.sutran.sd.design.mapper.SdPresaleOrderMapper;
import com.sutran.sd.design.mapper.SdPresaleProjectMapper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import com.sutran.sd.design.vo.PresaleOrderDetailVO;
import com.sutran.sd.design.vo.PresaleOrderListVO;
import com.sutran.sd.design.vo.PresaleProjectDetailVO;
import com.sutran.sd.design.vo.PresaleProjectListVO;
import com.sutran.sd.pay.service.impl.PayOrderServiceImpl;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.system.service.ISysOssService;
import com.sutran.sd.system.domain.vo.SysOssVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 预售项目Service业务层处理
 *
 * @author sutran
 * @date 2025-01-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdPresaleProjectServiceImpl implements ISdPresaleProjectService {

    private final SdPresaleProjectMapper presaleProjectMapper;
    private final SdPresaleOrderMapper presaleOrderMapper;
    private final PayOrderServiceImpl payOrderService;
    private final AliPayService aliPayService;
    private final SdProofingInvitationMapper proofingInvitationMapper;
    private final ISysOssService ossService;

    @Override
    public SdPresaleProject selectSdPresaleProjectById(Long id) {
        return presaleProjectMapper.selectSdPresaleProjectById(id);
    }

    @Override
    public List<SdPresaleProject> selectSdPresaleProjectList(SdPresaleProject sdPresaleProject) {
        return presaleProjectMapper.selectSdPresaleProjectList(sdPresaleProject);
    }

    @Override
    public TableDataInfo<SdPresaleProject> selectPagePresaleProjectList(SdPresaleProject sdPresaleProject, PageQuery pageQuery) {
        Page<SdPresaleProject> page = pageQuery.build();
        IPage<SdPresaleProject> result = presaleProjectMapper.selectPagePresaleProjectList(page, sdPresaleProject);
        return TableDataInfo.build(result);
    }

    @Override
    public R<List<PresaleProjectListVO>> getPresaleProjectList() {
        log.info("获取预售项目列表");

        // 使用多表联查获取预售项目列表
        Page<SdPresaleProject> page = new Page<>(1, 100); // 默认查询前100条
        IPage<SdPresaleProject> result = presaleProjectMapper.selectPresaleProjectListWithUserInfo(page, 1); // 销售中状态

        // 转换为VO
        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return R.ok(voList);
    }

    @Override
    public TableDataInfo<PresaleProjectListVO> getPresaleProjectListPage(PageQuery pageQuery) {
        log.info("获取预售项目列表（分页）");

        // 使用多表联查获取预售项目列表
        Page<SdPresaleProject> page = pageQuery.build();
        IPage<SdPresaleProject> result = presaleProjectMapper.selectPresaleProjectListWithUserInfo(page, 1); // 销售中状态

        // 转换为VO
        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }

    @Override
    public R<PresaleProjectDetailVO> getPresaleProjectDetail(Long id) {
        log.info("获取预售项目详情: {}", id);

        SdPresaleProject project = presaleProjectMapper.selectSdPresaleProjectById(id);
        if (project == null) {
            return R.fail("预售项目不存在");
        }

        // 增加浏览次数
        project.setViewCount(project.getViewCount() != null ? project.getViewCount() + 1 : 1);
        presaleProjectMapper.updateById(project);

        PresaleProjectDetailVO vo = convertToProjectDetailVO(project);
        return R.ok(vo);
    }


    @Override
    public R<List<PresaleProjectListVO>> getManufacturerPresaleProjects() {
        log.info("获取厂家参与的预售项目列表");

        Long currentUserId = LoginHelper.getUserId();
        Page<SdPresaleProject> page = new Page<>(1, 100);
        IPage<SdPresaleProject> result = presaleProjectMapper.selectUserPresaleProjects(page, currentUserId, "manufacturer");

        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return R.ok(voList);
    }

    @Override
    public TableDataInfo<PresaleProjectListVO> getManufacturerPresaleProjectsPage(PageQuery pageQuery) {
        log.info("获取厂家参与的预售项目列表（分页）");

        Long currentUserId = LoginHelper.getUserId();
        Page<SdPresaleProject> page = pageQuery.build();
        IPage<SdPresaleProject> result = presaleProjectMapper.selectUserPresaleProjects(page, currentUserId, "manufacturer");

        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }

    @Override
    public R<List<PresaleProjectListVO>> getCreatorPresaleProjects() {
        log.info("获取发起人的预售项目列表");

        Long currentUserId = LoginHelper.getUserId();
        Page<SdPresaleProject> page = new Page<>(1, 100);
        IPage<SdPresaleProject> result = presaleProjectMapper.selectUserPresaleProjects(page, currentUserId, "creator");

        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return R.ok(voList);
    }

    @Override
    public TableDataInfo<PresaleProjectListVO> getCreatorPresaleProjectsPage(PageQuery pageQuery) {
        log.info("获取发起人的预售项目列表（分页）");

        Long currentUserId = LoginHelper.getUserId();
        Page<SdPresaleProject> page = pageQuery.build();
        IPage<SdPresaleProject> result = presaleProjectMapper.selectUserPresaleProjects(page, currentUserId, "creator");

        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }
    @Override
    public R<List<PresaleProjectListVO>> getBuyerPresaleProjects() {
        log.info("获取买家购买的预售项目列表");

        Long currentUserId = LoginHelper.getUserId();
        Page<SdPresaleProject> page = new Page<>(1, 100);
        IPage<SdPresaleProject> result = presaleProjectMapper.selectUserPresaleProjects(page, currentUserId, "buyer");

        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return R.ok(voList);
    }

    @Override
    public TableDataInfo<PresaleProjectListVO> getBuyerPresaleProjectsPage(PageQuery pageQuery) {
        log.info("获取买家购买的预售项目列表（分页）");

        Long currentUserId = LoginHelper.getUserId();
        log.info("当前用户ID: {}", currentUserId);
        
        // 先查询用户是否有预售订单
        LambdaQueryWrapper<SdPresaleOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(SdPresaleOrder::getUserId, currentUserId);
        List<SdPresaleOrder> userOrders = presaleOrderMapper.selectList(orderQuery);
        log.info("用户预售订单数量: {}, 订单详情: {}", userOrders.size(), 
            userOrders.stream().map(order -> String.format("订单ID=%d, 项目ID=%d, 状态=%d", 
                order.getId(), order.getProjectId(), order.getOrderStatus())).collect(Collectors.toList()));

        // 查询符合条件的订单（已支付及以上状态）
        LambdaQueryWrapper<SdPresaleOrder> paidOrderQuery = new LambdaQueryWrapper<>();
        paidOrderQuery.eq(SdPresaleOrder::getUserId, currentUserId)
                     .in(SdPresaleOrder::getOrderStatus, 2, 3, 4, 5);
        List<SdPresaleOrder> paidOrders = presaleOrderMapper.selectList(paidOrderQuery);
        log.info("用户已支付订单数量: {}, 订单详情: {}", paidOrders.size(),
            paidOrders.stream().map(order -> String.format("订单ID=%d, 项目ID=%d, 状态=%d", 
                order.getId(), order.getProjectId(), order.getOrderStatus())).collect(Collectors.toList()));

        Page<SdPresaleProject> page = pageQuery.build();
        IPage<SdPresaleProject> result = presaleProjectMapper.selectUserPresaleProjects(page, currentUserId, "buyer");
        log.info("查询到的预售项目数量: {}", result.getRecords().size());
        
        // 如果查询结果为空，尝试直接查询项目
        if (result.getRecords().isEmpty() && !paidOrders.isEmpty()) {
            log.info("尝试直接查询项目...");
            List<Long> projectIds = paidOrders.stream()
                    .map(SdPresaleOrder::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());
            log.info("项目ID列表: {}", projectIds);
            
            if (!projectIds.isEmpty()) {
                LambdaQueryWrapper<SdPresaleProject> projectQuery = new LambdaQueryWrapper<>();
                projectQuery.in(SdPresaleProject::getId, projectIds);
                List<SdPresaleProject> projects = presaleProjectMapper.selectList(projectQuery);
                log.info("直接查询到的项目数量: {}", projects.size());
            }
        }

        List<PresaleProjectListVO> voList = result.getRecords().stream()
                .map(this::convertToProjectListVO)
                .collect(Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }

    @Override
    public R<String> createPresaleOrder(PresaleOrderCreateDTO createDTO) {
        log.info("创建预售订单: {}", createDTO);

        try {
            // 1. 查询项目信息
            SdPresaleProject project = presaleProjectMapper.selectSdPresaleProjectById(createDTO.getProjectId());
            if (project == null) {
                return R.fail("预售项目不存在");
            }

            if (project.getStatus() != 1) {
                return R.fail("项目不在销售中状态");
            }

            // 2. 生成订单号
            String  orderNo = OrderNumUtils.getOrderNum(new Date());

            // 3. 计算价格（根据当前销售数量计算阶梯价格）
            BigDecimal unitPrice = calculateCurrentUnitPrice(project);
            BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(createDTO.getQuantity()));

            // 4. 创建订单
            SdPresaleOrder order = new SdPresaleOrder();
            order.setOrderNo(orderNo);
            order.setProjectId(createDTO.getProjectId());
            order.setUserId(LoginHelper.getUserId());
            order.setUserName(LoginHelper.getUsername());
            order.setProductTitle(project.getTitle());
            order.setProductImage(project.getCoverImage());
            order.setQuantity(createDTO.getQuantity());
            order.setOriginalUnitPrice(unitPrice);
            order.setFinalUnitPrice(unitPrice);
            order.setOriginalTotalAmount(totalAmount);
            order.setFinalTotalAmount(totalAmount);
            order.setReceiverName(createDTO.getReceiverName());
            order.setReceiverPhone(createDTO.getReceiverPhone());
            order.setReceiverAddress(createDTO.getReceiverAddress());
            order.setReceiverArea(createDTO.getReceiverArea());
            order.setOrderStatus(PresaleOrderStatus.PENDING_PAYMENT.getCode()); // 待支付

            presaleOrderMapper.insert(order);

            // 5. 创建支付订单
            try {
                String subject = "预售商品：" + project.getTitle();
                String body = "购买数量：" + createDTO.getQuantity() + "件";
                String notifyUrl = "/design/presale/payment/alipay/notify";

                // 调用支付宝服务创建支付订单
                aliPayService.createPayOrder(
                    LoginHelper.getUserId(),
                    LoginHelper.getUsername(),
                    orderNo,
                    subject,
                    body,
                    totalAmount,
                    notifyUrl
                );

                log.info("支付订单创建成功: 订单号={}", orderNo);
            } catch (Exception e) {
                log.error("创建支付订单失败: 订单号={}", orderNo, e);
                // 如果支付订单创建失败，删除预售订单
                presaleOrderMapper.deleteById(order.getId());
                return R.fail("创建支付订单失败: " + e.getMessage());
            }

            return R.ok("订单创建成功", orderNo);
        } catch (Exception e) {
            log.error("创建预售订单失败: {}", e.getMessage(), e);
            return R.fail("创建订单失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<PresaleOrderListVO>> getMyPresaleOrders() {
        log.info("获取我的预售订单列表");

        try {
            Long currentUserId = LoginHelper.getUserId();
            LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdPresaleOrder::getUserId, currentUserId)
                       .orderByDesc(SdPresaleOrder::getCreateTime);

            List<SdPresaleOrder> orders = presaleOrderMapper.selectList(queryWrapper);

            List<PresaleOrderListVO> voList = orders.stream()
                    .map(this::convertToOrderListVO)
                    .collect(Collectors.toList());

            return R.ok(voList);
        } catch (Exception e) {
            log.error("获取我的预售订单列表失败: {}", e.getMessage(), e);
            return R.fail("获取订单列表失败: " + e.getMessage());
        }
    }

    @Override
    public TableDataInfo<PresaleOrderListVO> getMyPresaleOrdersPage(PageQuery pageQuery) {
        log.info("获取我的预售订单列表（分页）");

        Long currentUserId = LoginHelper.getUserId();
        Page<SdPresaleOrder> page = pageQuery.build();

        LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdPresaleOrder::getUserId, currentUserId)
                   .orderByDesc(SdPresaleOrder::getCreateTime);

        IPage<SdPresaleOrder> result = presaleOrderMapper.selectPage(page, queryWrapper);

        List<PresaleOrderListVO> voList = result.getRecords().stream()
                .map(this::convertToOrderListVO)
                .collect(Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }

    @Override
    public TableDataInfo<PresaleOrderListVO> getProjectPresaleOrdersPage(Long projectId, PageQuery pageQuery) {
        log.info("获取预售项目订单列表（分页）: 项目ID={}", projectId);

        // 验证项目是否存在
        SdPresaleProject project = presaleProjectMapper.selectSdPresaleProjectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 验证权限：只有项目创建者或厂家可以查看订单
        Long currentUserId = LoginHelper.getUserId();
        if (!project.getCreatorUserId().equals(currentUserId) && !project.getManufacturerUserId().equals(currentUserId)) {
            throw new RuntimeException("无权限查看此项目的订单");
        }

        Page<SdPresaleOrder> page = pageQuery.build();

        LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdPresaleOrder::getProjectId, projectId)
                   .orderByDesc(SdPresaleOrder::getCreateTime);

        IPage<SdPresaleOrder> result = presaleOrderMapper.selectPage(page, queryWrapper);

        List<PresaleOrderListVO> voList = result.getRecords().stream()
                .map(this::convertToOrderListVO)
                .collect(Collectors.toList());

        return new TableDataInfo<>(voList, result.getTotal());
    }

    @Override
    public R<PresaleOrderDetailVO> getPresaleOrderDetail(String orderNo) {
        log.info("获取预售订单详情: {}", orderNo);

        try {
            SdPresaleOrder order = presaleOrderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return R.fail("订单不存在");
            }

            // 验证订单归属
            Long currentUserId = LoginHelper.getUserId();
            if (!order.getUserId().equals(currentUserId)) {
                return R.fail("无权限查看此订单");
            }

            PresaleOrderDetailVO vo = convertToOrderDetailVO(order);
            return R.ok(vo);
        } catch (Exception e) {
            log.error("获取预售订单详情失败: {}", e.getMessage(), e);
            return R.fail("获取订单详情失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> getPaymentQr(String orderNo) {
        log.info("获取支付二维码: {}", orderNo);

        try {
            SdPresaleOrder order = presaleOrderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return R.fail("订单不存在");
            }

            if (order.getOrderStatus() != 1) {
                return R.fail("订单状态不是待支付");
            }

            // 调用支付服务生成二维码
            String qrCode = payOrderService.getPayQr(orderNo, order.getUserId());
            return R.ok("获取支付二维码成功", qrCode);
        } catch (Exception e) {
            log.error("获取支付二维码失败: 订单号={}", orderNo, e);
            return R.fail("获取支付二维码失败: " + e.getMessage());
        }
    }

    /**
     * 转换为项目列表VO
     */
    private PresaleProjectListVO convertToProjectListVO(SdPresaleProject project) {
        PresaleProjectListVO vo = new PresaleProjectListVO();
        BeanUtils.copyProperties(project, vo);

        // 计算剩余天数（基于有效期天数）
        if (project.getValidityDays() != null && project.getCreateTime() != null) {
            long expireTime = project.getCreateTime().getTime() + (project.getValidityDays() * 24L * 60L * 60L * 1000L);
            long diffInMillies = expireTime - System.currentTimeMillis();
            vo.setRemainingDays(diffInMillies > 0 ? diffInMillies / (1000 * 60 * 60 * 24) : 0);
        }

        // 计算阶梯价格相关信息（包括当前价格、已售件数等）
        calculateTieredPricingInfoForList(project, vo);

        return vo;
    }

    /**
     * 转换为项目详情VO
     */
    private PresaleProjectDetailVO convertToProjectDetailVO(SdPresaleProject project) {
        PresaleProjectDetailVO vo = new PresaleProjectDetailVO();
        BeanUtils.copyProperties(project, vo);

        // 计算剩余天数（基于有效期天数）
        if (project.getValidityDays() != null && project.getCreateTime() != null) {
            long expireTime = project.getCreateTime().getTime() + (project.getValidityDays() * 24L * 60L * 60L * 1000L);
            long diffInMillies = expireTime - System.currentTimeMillis();
            vo.setRemainingDays(diffInMillies > 0 ? diffInMillies / (1000 * 60 * 60 * 24) : 0);
        }

        // 查询已支付订单（状态 >= 2 表示已支付）
        LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdPresaleOrder::getProjectId, project.getId())
                   .ge(SdPresaleOrder::getOrderStatus, 2); // 已支付及以上状态
        
        List<SdPresaleOrder> paidOrders = presaleOrderMapper.selectList(queryWrapper);
        
        // 计算购买人数（去重用户ID）
        long buyerCount = paidOrders.stream()
                .map(SdPresaleOrder::getUserId)
                .distinct()
                .count();
        vo.setBuyerCount((int) buyerCount);
        
        // 计算购买件数（所有订单的数量总和）
        int totalQuantity = paidOrders.stream()
                .mapToInt(SdPresaleOrder::getQuantity)
                .sum();
        vo.setTotalQuantity(totalQuantity);

        // 计算阶梯价格相关信息
        calculateTieredPricingInfo(project, vo);

        return vo;
    }

    /**
     * 转换为订单列表VO
     */
    private PresaleOrderListVO convertToOrderListVO(SdPresaleOrder order) {
        PresaleOrderListVO vo = new PresaleOrderListVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    /**
     * 转换为订单详情VO
     */
    private PresaleOrderDetailVO convertToOrderDetailVO(SdPresaleOrder order) {
        PresaleOrderDetailVO vo = new PresaleOrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public R<String> publishPresaleProject(PresaleProjectPublishDTO publishDTO) {
        try {
            log.info("[发布预售项目] 开始发布: 标题={}, 打样邀约ID={}", publishDTO.getTitle(), publishDTO.getProofingInvitationId());

            // 1. 获取当前用户ID
            Long currentUserId = LoginHelper.getUserId();
            if (currentUserId == null) {
                return R.fail("用户未登录");
            }

            // 2. 查询打样邀约信息，获取AI设计图
             ProofingInvitationDetailVO invitationDetail = proofingInvitationMapper.selectInvitationDetailById(publishDTO.getProofingInvitationId());
            if (invitationDetail == null) {
                return R.fail("打样邀约不存在");
            }

            // 3. 验证用户权限（只有被邀约人才能发布预售）
            if (!invitationDetail.getInviteeUserId().equals(currentUserId)) {
                return R.fail("无权限发布此项目的预售");
            }

            // 4. 创建预售项目，从打样邀约继承默认数据
            SdPresaleProject project = new SdPresaleProject();

            // 生成项目编号
            String projectNo = "PP" + System.currentTimeMillis();
            project.setProjectNo(projectNo);

            // 标题：优先使用用户填写的，否则继承打样邀约的产品标题
            project.setTitle(StringUtils.isNotBlank(publishDTO.getTitle()) ?
                publishDTO.getTitle() : invitationDetail.getProductTitle());

            // 描述：优先使用用户填写的，否则继承打样邀约的产品描述
            project.setDescription(StringUtils.isNotBlank(publishDTO.getDescription()) ?
                publishDTO.getDescription() : invitationDetail.getProductDescription());

            project.setCoverImage(invitationDetail.getImageUrl()); // 使用AI设计图作为封面

            // 设置发起人信息（当前用户）
            project.setCreatorUserId(currentUserId);
            project.setCreatorName(LoginHelper.getUsername());


            // 设置厂家信息（打样邀约的发起人）
            project.setManufacturerUserId(invitationDetail.getInviterUserId());
            project.setManufacturerName(invitationDetail.getInviterNickName());
            project.setManufacturerAvatar(invitationDetail.getInviterAvatar());

            project.setProofingInvitationId(publishDTO.getProofingInvitationId());

            // 基础价格：优先使用用户填写的，否则继承打样邀约的报价
            project.setBasePrice(publishDTO.getBasePrice() != null ?
                publishDTO.getBasePrice() : invitationDetail.getQuotedPrice());

            // 有效期天数：使用用户填写的有效期天数
            project.setValidityDays(publishDTO.getValidityDays());
            project.setManufacturerPhotos(publishDTO.getManufacturerPhotos());
            project.setStatus(PresaleProjectStatus.ON_SALE.getCode()); // 销售中
            project.setViewCount(0);
            project.setFavoriteCount(0);
            project.setShareCount(0);
            project.setTotalSalesAmount(BigDecimal.ZERO);

            // 5. 处理阶梯价格配置：优先使用用户填写的，否则继承打样邀约的阶梯价格
            String tieredPricingJson = null;
            if (publishDTO.getTieredPricing() != null && !publishDTO.getTieredPricing().isEmpty()) {
                // 用户填写了阶梯价格
                ObjectMapper mapper = new ObjectMapper();
                tieredPricingJson = mapper.writeValueAsString(publishDTO.getTieredPricing());
            } else if (StringUtils.isNotBlank(invitationDetail.getTieredPricing())) {
                // 继承打样邀约的阶梯价格
                tieredPricingJson = invitationDetail.getTieredPricing();
            }
            project.setTieredPricing(tieredPricingJson);

            // 6. 保存项目
            int result = presaleProjectMapper.insert(project);
            if (result > 0) {
                log.info("[发布预售项目] 发布成功: 项目ID={}, 标题={}", project.getId(), project.getTitle());
                return R.ok("发布成功", project.getId().toString());
            } else {
                return R.fail("发布失败");
            }

        } catch (Exception e) {
            log.error("[发布预售项目] 发布异常: {}", e.getMessage(), e);
            return R.fail("发布失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> uploadManufacturerPhotos(MultipartFile file) {
        try {
            log.info("[上传实物照片] 开始上传: 文件名={}", file.getOriginalFilename());

            // 1. 验证文件
            if (file.isEmpty()) {
                return R.fail("文件不能为空");
            }

            // 2. 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isImageFile(originalFilename)) {
                return R.fail("文件格式不正确，请上传JPG、JPEG、PNG格式的图片");
            }

            // 3. 上传文件
            SysOssVo oss = ossService.upload(file);
            String photoUrl = oss.getUrl();

            log.info("[上传实物照片] 上传成功: URL={}", photoUrl);
            return R.ok("上传成功", photoUrl);

        } catch (Exception e) {
            log.error("[上传实物照片] 上传异常: {}", e.getMessage(), e);
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(String filename) {
        if (filename == null) {
            return false;
        }
        String extension = filename.toLowerCase();
        return extension.endsWith(".jpg") || extension.endsWith(".jpeg") ||
               extension.endsWith(".png") || extension.endsWith(".gif") ||
               extension.endsWith(".bmp") || extension.endsWith(".webp");
    }

    /**
     * 计算阶梯价格相关信息（列表VO专用）
     */
    private void calculateTieredPricingInfoForList(SdPresaleProject project, PresaleProjectListVO vo) {
        try {
            // 1. 查询当前销售数量（已支付订单）
            LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdPresaleOrder::getProjectId, project.getId())
                       .eq(SdPresaleOrder::getOrderStatus, 2); // 已支付状态

            List<SdPresaleOrder> paidOrders = presaleOrderMapper.selectList(queryWrapper);
            int totalQuantity = paidOrders.stream()
                    .mapToInt(SdPresaleOrder::getQuantity)
                    .sum();

            // 设置已售件数
            vo.setSoldQuantity(totalQuantity);

            // 2. 解析阶梯价格配置
            if (StringUtils.isBlank(project.getTieredPricing())) {
                // 没有阶梯价格配置，使用基础价格
                vo.setCurrentPrice(project.getBasePrice());
                vo.setTieredPricingList(new ArrayList<>());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            List<TieredPricingItem> tieredPricingList = mapper.readValue(project.getTieredPricing(),
                mapper.getTypeFactory().constructCollectionType(List.class, TieredPricingItem.class));

            vo.setTieredPricingList(tieredPricingList);

            // 3. 计算当前价格
            BigDecimal currentPrice = calculateCurrentPrice(totalQuantity, tieredPricingList);
            vo.setCurrentPrice(currentPrice);

            // 4. 计算下一个价格阈值和价格
            calculateNextPriceInfoForList(totalQuantity, tieredPricingList, vo);

        } catch (Exception e) {
            log.error("[预售项目] 计算阶梯价格信息失败: 项目ID={}", project.getId(), e);
            // 出错时使用基础价格
            vo.setCurrentPrice(project.getBasePrice());
            vo.setTieredPricingList(new ArrayList<>());
        }
    }

    /**
     * 计算阶梯价格相关信息
     */
    private void calculateTieredPricingInfo(SdPresaleProject project, PresaleProjectDetailVO vo) {
        try {
            // 1. 解析阶梯价格配置
            if (StringUtils.isBlank(project.getTieredPricing())) {
                // 没有阶梯价格配置，使用基础价格
                vo.setCurrentPrice(project.getBasePrice());
                vo.setTieredPricingList(new ArrayList<>());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            List<TieredPricingItem> tieredPricingList = mapper.readValue(project.getTieredPricing(),
                mapper.getTypeFactory().constructCollectionType(List.class, TieredPricingItem.class));

            vo.setTieredPricingList(tieredPricingList);

            // 2. 查询当前销售数量（已支付订单）
            LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdPresaleOrder::getProjectId, project.getId())
                       .eq(SdPresaleOrder::getOrderStatus, 2); // 已支付状态

            List<SdPresaleOrder> paidOrders = presaleOrderMapper.selectList(queryWrapper);
            int totalQuantity = paidOrders.stream()
                    .mapToInt(SdPresaleOrder::getQuantity)
                    .sum();

            // 3. 计算当前价格
            BigDecimal currentPrice = calculateCurrentPrice(totalQuantity, tieredPricingList);
            vo.setCurrentPrice(currentPrice);

            // 4. 计算下一个价格阈值和价格
            calculateNextPriceInfo(totalQuantity, tieredPricingList, vo);

        } catch (Exception e) {
            log.error("[预售项目] 计算阶梯价格信息失败: 项目ID={}", project.getId(), e);
            // 出错时使用基础价格
            vo.setCurrentPrice(project.getBasePrice());
            vo.setTieredPricingList(new ArrayList<>());
        }
    }

    /**
     * 计算当前价格
     */
    private BigDecimal calculateCurrentPrice(int totalQuantity, List<TieredPricingItem> tieredPricingList) {
        if (tieredPricingList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 按数量节点排序
        tieredPricingList.sort((a, b) -> Integer.compare(a.getNode(), b.getNode()));

        // 找到对应的价格区间
        for (int i = tieredPricingList.size() - 1; i >= 0; i--) {
            TieredPricingItem tier = tieredPricingList.get(i);
            if (totalQuantity >= tier.getNode()) {
                return tier.getUnitPrice();
            }
        }

        // 如果数量小于第一个节点，返回第一个价格
        return tieredPricingList.get(0).getUnitPrice();
    }

    /**
     * 计算下一个价格信息
     */
    private void calculateNextPriceInfo(int totalQuantity, List<TieredPricingItem> tieredPricingList, PresaleProjectDetailVO vo) {
        if (tieredPricingList.isEmpty()) {
            return;
        }

        // 按数量节点排序
        tieredPricingList.sort((a, b) -> Integer.compare(a.getNode(), b.getNode()));

        // 找到下一个价格阈值
        for (TieredPricingItem tier : tieredPricingList) {
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
     * 阶梯价格配置项
     */
    public static class TieredPricingItem {
        private BigDecimal unitPrice;
        private Integer node;

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Integer getNode() {
            return node;
        }

        public void setNode(Integer node) {
            this.node = node;
        }
    }

    /**
     * 计算当前应该的单价（根据阶梯价格）
     */
    private BigDecimal calculateCurrentUnitPrice(SdPresaleProject project) {
        try {
            // 1. 检查是否有阶梯价格配置
            if (StringUtils.isBlank(project.getTieredPricing())) {
                // 没有阶梯价格配置，使用基础价格
                return project.getBasePrice();
            }

            // 2. 解析阶梯价格配置
            ObjectMapper mapper = new ObjectMapper();
            List<TieredPricingItem> tieredPricingList = mapper.readValue(project.getTieredPricing(),
                mapper.getTypeFactory().constructCollectionType(List.class, TieredPricingItem.class));

            if (tieredPricingList.isEmpty()) {
                return project.getBasePrice();
            }

            // 3. 查询当前销售数量（已支付订单）
            LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdPresaleOrder::getProjectId, project.getId())
                       .eq(SdPresaleOrder::getOrderStatus, 2); // 已支付状态

            List<SdPresaleOrder> paidOrders = presaleOrderMapper.selectList(queryWrapper);
            int totalQuantity = paidOrders.stream()
                    .mapToInt(SdPresaleOrder::getQuantity)
                    .sum();

            log.info("[预售项目] 计算阶梯价格: 项目ID={}, 当前销售数量={}", project.getId(), totalQuantity);

            // 4. 计算当前价格
            return calculateCurrentPrice(totalQuantity, tieredPricingList);

        } catch (Exception e) {
            log.error("[预售项目] 计算阶梯价格失败: 项目ID={}", project.getId(), e);
            // 出错时使用基础价格
            return project.getBasePrice();
        }
    }

    /**
     * 检查项目是否过期
     */
    private boolean isProjectExpired(SdPresaleProject project) {
        if (project == null || project.getValidityDays() == null || project.getCreateTime() == null) {
            return false;
        }

        // 计算过期时间：创建时间 + 有效期天数
        long expireTime = project.getCreateTime().getTime() + (project.getValidityDays() * 24L * 60L * 60L * 1000L);
        return System.currentTimeMillis() > expireTime;
    }

    /**
     * 计算下一个价格阈值和价格（列表VO专用）
     */
    private void calculateNextPriceInfoForList(int totalQuantity, List<TieredPricingItem> tieredPricingList, PresaleProjectListVO vo) {
        if (tieredPricingList.isEmpty()) {
            vo.setNextThreshold(null);
            vo.setNextPrice(null);
            return;
        }

        // 按节点数量排序
        tieredPricingList.sort((a, b) -> Integer.compare(a.getNode(), b.getNode()));

        // 找到下一个价格阈值
        for (TieredPricingItem item : tieredPricingList) {
            if (totalQuantity < item.getNode()) {
                vo.setNextThreshold(item.getNode());
                vo.setNextPrice(item.getUnitPrice());
                return;
            }
        }

        // 如果已经达到最高阶梯，没有下一个价格
        vo.setNextThreshold(null);
        vo.setNextPrice(null);
    }
}
