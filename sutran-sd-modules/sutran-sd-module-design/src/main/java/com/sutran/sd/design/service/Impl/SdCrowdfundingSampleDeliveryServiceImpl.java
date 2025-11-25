package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSampleDelivery;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSampleDeliveryMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.service.ISdCrowdfundingSampleDeliveryService;
import com.sutran.sd.design.vo.SampleDeliveryListVO;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 众筹样品发货记录Service业务层处理
 *
 * @author sutran
 * @date 2025-01-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdCrowdfundingSampleDeliveryServiceImpl implements ISdCrowdfundingSampleDeliveryService {

    private final SdCrowdfundingSampleDeliveryMapper sampleDeliveryMapper;
    private final SdCrowdfundingProjectMapper projectMapper;
    private final SdCrowdfundingSupportMapper supportMapper;
    private final ISysOssService ossService;
    private final SdCrowdfundingProjectMapper sdCrowdfundingProjectMapper;
    private final SdCrowdfundingSupportMapper crowdfundingSupportMapper;

    @Override
    public SdCrowdfundingSampleDelivery selectSdCrowdfundingSampleDeliveryById(Long id) {
        return sampleDeliveryMapper.selectSdCrowdfundingSampleDeliveryById(id);
    }

    @Override
    public List<SdCrowdfundingSampleDelivery> selectSdCrowdfundingSampleDeliveryList(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery) {
        return sampleDeliveryMapper.selectSdCrowdfundingSampleDeliveryList(sdCrowdfundingSampleDelivery);
    }

    @Override
    public TableDataInfo<SdCrowdfundingSampleDelivery> selectPageSampleDeliveryList(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery, PageQuery pageQuery) {
        Page<SdCrowdfundingSampleDelivery> page = pageQuery.build();
        IPage<SdCrowdfundingSampleDelivery> result = sampleDeliveryMapper.selectPageSampleDeliveryList(page, sdCrowdfundingSampleDelivery);
        return TableDataInfo.build(result);
    }

    @Override
    public int insertSdCrowdfundingSampleDelivery(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery) {
        return sampleDeliveryMapper.insert(sdCrowdfundingSampleDelivery);
    }

    @Override
    public int updateSdCrowdfundingSampleDelivery(SdCrowdfundingSampleDelivery sdCrowdfundingSampleDelivery) {
        return sampleDeliveryMapper.updateById(sdCrowdfundingSampleDelivery);
    }

    @Override
    public int deleteSdCrowdfundingSampleDeliveryByIds(Long[] ids) {
        return sampleDeliveryMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public int deleteSdCrowdfundingSampleDeliveryById(Long id) {
        return sampleDeliveryMapper.deleteById(id);
    }

    @Override
    public TableDataInfo<SampleDeliveryListVO> getMyDeliveryProjects(PageQuery pageQuery) {
        try {
            log.info("获取我承接的已完成众筹项目及发货信息（分页）");

            Long manufacturerUserId = LoginHelper.getUserId();

            // 1. 查询我承接的已完成众筹项目
            LambdaQueryWrapper<SdCrowdfundingProject> projectWrapper = new LambdaQueryWrapper<>();
            projectWrapper.eq(SdCrowdfundingProject::getManufacturerUserId, manufacturerUserId)
                         .eq(SdCrowdfundingProject::getStatus, 2) // 众筹成功
                         .eq(SdCrowdfundingProject::getDrawStatus, 2);

            List<SdCrowdfundingProject> projects = projectMapper.selectList(projectWrapper);

            if (projects.isEmpty()) {
                log.info("没有找到我承接的已完成众筹项目");
                return TableDataInfo.build(new ArrayList<>());
            }

            // 构建项目Map方便后续查询
            java.util.Map<Long, SdCrowdfundingProject> projectMap = projects.stream()
                    .collect(Collectors.toMap(SdCrowdfundingProject::getId, p -> p));
            List<Long> projectIds = new ArrayList<>(projectMap.keySet());

            // 2. 查询这些项目的所有发货记录 (分页)
            Page<SdCrowdfundingSampleDelivery> page = pageQuery.build();
            LambdaQueryWrapper<SdCrowdfundingSampleDelivery> deliveryWrapper = new LambdaQueryWrapper<>();
            deliveryWrapper.in(SdCrowdfundingSampleDelivery::getCrowdfundingProjectId, projectIds)
                           .orderByDesc(SdCrowdfundingSampleDelivery::getCreateTime);

            IPage<SdCrowdfundingSampleDelivery> result = sampleDeliveryMapper.selectPage(page, deliveryWrapper);

            // 3. 转换为发货信息VO
            List<SampleDeliveryListVO> resultList = result.getRecords().stream()
                    .map(delivery -> convertToSampleDeliveryListVO(delivery, projectMap.get(delivery.getCrowdfundingProjectId())))
                    .collect(Collectors.toList());

            log.info("获取我的发货项目成功: 项目数量={}, 发货记录总数={}, 当前页数量={}", projects.size(), result.getTotal(), resultList.size());

            TableDataInfo<SampleDeliveryListVO> dataInfo = new TableDataInfo<>();
            dataInfo.setRows(resultList);
            dataInfo.setTotal(result.getTotal());
            return dataInfo;

        } catch (Exception e) {
            log.error("获取我的发货项目失败", e);
            return TableDataInfo.build(new ArrayList<>());
        }
    }

    @Override
    public R<String> updateTrackingNumber(Long id, String trackingNumber) {
        try {
            log.info("更新快递单号: 记录ID={}, 快递单号={}", id, trackingNumber);

            // 1. 查询发货记录
            SdCrowdfundingSampleDelivery delivery = sampleDeliveryMapper.selectSdCrowdfundingSampleDeliveryById(id);
            if (delivery == null) {
                log.warn("发货记录不存在: ID={}", id);
                return R.fail("发货记录不存在");
            }

            // 2. 更新快递单号
            delivery.setTrackingNumber(trackingNumber);
            delivery.setStatus(2); // 已发货
            delivery.setDeliveryTime(new java.util.Date());

            int result = sampleDeliveryMapper.updateById(delivery);
            if (result > 0) {
                log.info("快递单号更新成功: 记录ID={}, 快递单号={}", id, trackingNumber);
                return R.ok("快递单号更新成功");
            } else {
                log.warn("快递单号更新失败: 记录ID={}", id);
                return R.fail("快递单号更新失败");
            }

        } catch (Exception e) {
            log.error("更新快递单号失败: 记录ID={}", id, e);
            return R.fail("更新快递单号失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> uploadSampleImage(Long id, MultipartFile file) {
        try {
            log.info("上传样品图片: 记录ID={}, 文件名={}", id, file.getOriginalFilename());

            // 1. 验证文件
            if (file.isEmpty()) {
                return R.fail("样品图片文件不能为空");
            }

            // 验证文件类型
            String extension = getFileExtension(file.getOriginalFilename());
            if (!isImageFile(extension)) {
                return R.fail("文件格式不正确，请上传JPG、JPEG、PNG格式的图片");
            }

            // 2. 查询发货记录
            SdCrowdfundingSampleDelivery delivery = sampleDeliveryMapper.selectSdCrowdfundingSampleDeliveryById(id);
            SdCrowdfundingProject project = sdCrowdfundingProjectMapper.selectSdCrowdfundingProjectById(id);
            if (delivery == null) {

                return R.fail("发货记录不存在");
            }

            //3.上传
            SysOssVo oss = ossService.upload(file);
            String uploadUrl = oss.getUrl();

            // 4. 更新发货记录的样品图片地址
            delivery.setSampleImageUrl(uploadUrl);
            project.setManufacturerPhotos(uploadUrl);

            int result = sampleDeliveryMapper.updateById(delivery);
            int result2 = sdCrowdfundingProjectMapper.updateById(project);

            if (result > 0&& result2 > 0) {
                log.info("样品图片上传成功: 记录ID={}, 图片URL={}", id, uploadUrl);
                return R.ok(uploadUrl);
            } else {
                log.warn("更新样品图片地址失败: 记录ID={}", id);
                return R.fail("更新样品图片地址失败");
            }

        } catch (Exception e) {
            log.error("上传样品图片失败: 记录ID={}", id, e);
            return R.fail("上传样品图片失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> deleteSampleImage(Long id) {
        try {
            log.info("删除样品图片: 邀约ID={}", id);

            // 1. 根据邀约ID查询发货记录
            LambdaQueryWrapper<SdCrowdfundingSampleDelivery> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSampleDelivery::getProofingInvitationId, id);
            List<SdCrowdfundingSampleDelivery> deliveryList = sampleDeliveryMapper.selectList(queryWrapper);

            if (deliveryList == null || deliveryList.isEmpty()) {
                log.warn("发货记录不存在: 邀约ID={}", id);
                return R.fail("发货记录不存在");
            }

            // 2. 查询项目信息（使用第一条发货记录中的项目ID）
            SdCrowdfundingSampleDelivery firstDelivery = deliveryList.get(0);
            SdCrowdfundingProject project = sdCrowdfundingProjectMapper.selectSdCrowdfundingProjectById(firstDelivery.getCrowdfundingProjectId());
            if (project == null) {
                log.warn("众筹项目不存在: 项目ID={}", firstDelivery.getCrowdfundingProjectId());
                return R.fail("众筹项目不存在");
            }

            // 3. 检查是否有样品图片 (检查项目图片或任意发货记录图片)
            boolean hasImage = StringUtils.isNotBlank(project.getManufacturerPhotos());
            if (!hasImage) {
                for (SdCrowdfundingSampleDelivery item : deliveryList) {
                    if (StringUtils.isNotBlank(item.getSampleImageUrl())) {
                        hasImage = true;
                        break;
                    }
                }
            }

            if (!hasImage) {
                log.info("该记录没有样品图片: 邀约ID={}", id);
                // return R.fail("该记录没有样品图片");
            }

            // 4. 清空数据库中的样品图片地址 (清空项目和所有发货记录的图片)
            if (StringUtils.isNotBlank(project.getManufacturerPhotos())) {

                sdCrowdfundingProjectMapper.clearManufacturerPhotos(id);
            }

            for (SdCrowdfundingSampleDelivery item : deliveryList) {
                if (StringUtils.isNotBlank(item.getSampleImageUrl())) {
                    // 使用专门的 XML 方法来清空图片字段
                    sampleDeliveryMapper.clearSampleImageUrl(item.getId());
                }
            }

            log.info("样品图片删除成功: 邀约ID={}", id);
            return R.ok("样品图片删除成功");

        } catch (Exception e) {
            log.error("删除样品图片失败: 邀约ID={}", id, e);
            return R.fail("删除样品图片失败: " + e.getMessage());
        }
    }


    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
       Long userId = LoginHelper.getUserId();
       return userId;
    }

    private String buildFullReceiverAddress(String area, String address) {
        if (StringUtils.isBlank(area)) {
            return address;
        }
        if (StringUtils.isBlank(address)) {
            return area;
        }
        return address + area ;
    }

    @Override
    public TableDataInfo<SampleDeliveryListVO> getProjectDeliveryInfo(Long projectId, PageQuery pageQuery) {
        try {
            log.info("[查询项目发货信息] 开始查询: 项目ID={}", projectId);

            // 1. 验证项目是否存在
            SdCrowdfundingProject project = projectMapper.selectSdCrowdfundingProjectById(projectId);
            if (project == null) {
                log.warn("[查询项目发货信息] 项目不存在: 项目ID={}", projectId);
                return TableDataInfo.build(new ArrayList<>());
            }

            // 2. 构建分页查询
            Page<SdCrowdfundingSampleDelivery> page = pageQuery.build();

            // 3. 查询该项目的所有发货记录
            LambdaQueryWrapper<SdCrowdfundingSampleDelivery> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingSampleDelivery::getCrowdfundingProjectId, projectId)
                       .orderByDesc(SdCrowdfundingSampleDelivery::getCreateTime);

            IPage<SdCrowdfundingSampleDelivery> result = sampleDeliveryMapper.selectPage(page, queryWrapper);

            // 4. 转换为VO
            List<SampleDeliveryListVO> voList = result.getRecords().stream()
                    .map(delivery -> convertToSampleDeliveryListVO(delivery, project))
                    .collect(Collectors.toList());

            log.info("[查询项目发货信息] 查询完成: 项目ID={}, 记录数={}", projectId, voList.size());

            // 构建返回结果
            TableDataInfo<SampleDeliveryListVO> tableDataInfo = new TableDataInfo<>();
            tableDataInfo.setRows(voList);
            tableDataInfo.setTotal(result.getTotal());
            return tableDataInfo;

        } catch (Exception e) {
            log.error("[查询项目发货信息] 查询异常: 项目ID={}", projectId, e);
            return TableDataInfo.build(new ArrayList<>());
        }
    }

    /**
     * 转换为发货列表VO
     */
    private SampleDeliveryListVO convertToSampleDeliveryListVO(SdCrowdfundingSampleDelivery delivery, SdCrowdfundingProject project) {
        SampleDeliveryListVO vo = new SampleDeliveryListVO();

        // 基本信息
        vo.setId(delivery.getId());
        vo.setCrowdfundingProjectId(delivery.getCrowdfundingProjectId());
        vo.setProjectTitle(project != null ? project.getTitle() : "");
        vo.setProofingInvitationId(delivery.getProofingInvitationId());
        vo.setSampleImageUrl(delivery.getSampleImageUrl());

        // 收货人信息
        vo.setRecipientUserId(delivery.getRecipientUserId());
        vo.setRecipientName(delivery.getRecipientName());
        vo.setRecipientPhone(delivery.getRecipientPhone());
        vo.setDeliveryAddress(delivery.getDeliveryAddress());

        // 发货信息
        vo.setTrackingNumber(delivery.getTrackingNumber());
        vo.setDeliveryCompany(delivery.getDeliveryCompany());
        vo.setStatus(delivery.getStatus());
        vo.setStatusText(delivery.getStatus() == 1 ? "待发货" : "已发货");
        vo.setRemark(delivery.getRemark());

        // 发货数量
        vo.setQuantity(delivery.getQuantity());

        // 发货人信息
        vo.setSenderUserId(delivery.getSenderUserId());
        vo.setSenderName(delivery.getSenderName());

        // 时间信息
        vo.setDeliveryTime(delivery.getDeliveryTime());
        vo.setConfirmTime(delivery.getConfirmTime());
        vo.setCreateTime(delivery.getCreateTime());
        vo.setOrderStatus(delivery.getOrderStatus());

        // 查询订单编号：通过收货人用户ID和项目ID查询支持记录
        try {
            LambdaQueryWrapper<SdCrowdfundingSupport> supportWrapper = new LambdaQueryWrapper<>();
            supportWrapper.eq(SdCrowdfundingSupport::getProjectId, delivery.getCrowdfundingProjectId())
                         .eq(SdCrowdfundingSupport::getUserId, delivery.getRecipientUserId())
                         .eq(SdCrowdfundingSupport::getIsWinner, 1) // 中奖者
                         .orderByDesc(SdCrowdfundingSupport::getCreateTime)
                         .last("LIMIT 1");

            SdCrowdfundingSupport support = supportMapper.selectOne(supportWrapper);
            if (support != null) {
                vo.setDeliveryAddress(buildFullReceiverAddress(support.getReceiverArea(), support.getReceiverAddress()));
                if (support.getOrderNo() != null) {
                    // 设置订单编号
                    vo.setOrderNo(support.getOrderNo());
                }
            } else if (project != null && delivery.getRecipientUserId().equals(project.getCreatorUserId())) {
                // 如果是发起人且没有中奖记录（通常就是发起人自留样品），生成一个特殊的订单号
                vo.setOrderNo("INITIATOR_WINNER_" + project.getId());
            }
        } catch (Exception e) {
            log.warn("查询订单编号失败: 项目ID={}, 收货人ID={}, 错误={}",
                    delivery.getCrowdfundingProjectId(), delivery.getRecipientUserId(), e.getMessage());
        }

        return vo;
    }

}
