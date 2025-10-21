package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.OssService;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.domain.SdCrowdfundingSampleDelivery;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.mapper.SdCrowdfundingSampleDeliveryMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
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
                         .eq(SdCrowdfundingProject::getDrawStatus, 2)
                         .orderByDesc(SdCrowdfundingProject::getUpdateTime);

            List<SdCrowdfundingProject> projects = projectMapper.selectList(projectWrapper);

            if (projects.isEmpty()) {
                log.info("没有找到我承接的已完成众筹项目");
                return TableDataInfo.build(new ArrayList<>());
            }

            List<SampleDeliveryListVO> resultList = new ArrayList<>();

            // 2. 为每个项目查询中奖者信息
            for (SdCrowdfundingProject project : projects) {
                // 查询该项目的中奖者
                LambdaQueryWrapper<SdCrowdfundingSupport> supportWrapper = new LambdaQueryWrapper<>();
                supportWrapper.eq(SdCrowdfundingSupport::getProjectId, project.getId())
                             .eq(SdCrowdfundingSupport::getIsWinner, 1) // 中奖者
                             .orderByDesc(SdCrowdfundingSupport::getCreateTime);

                List<SdCrowdfundingSupport> winners = supportMapper.selectList(supportWrapper);

                if (winners.isEmpty()) {
                    log.info("项目ID={}没有中奖者信息", project.getId());
                    continue;
                }

                // 3. 转换为发货信息VO
                for (SdCrowdfundingSupport support : winners) {
                    SampleDeliveryListVO vo = new SampleDeliveryListVO();
                    vo.setId(support.getId());
                    vo.setCrowdfundingProjectId(project.getId());
                    vo.setProjectTitle(project.getTitle());
                    vo.setRecipientUserId(support.getUserId());
                    vo.setRecipientName(support.getUserName());
                    vo.setStatus(1); // 待发货
                    vo.setStatusText("待发货");
                    vo.setRemark(support.getPrizeInfo());
                    vo.setCreateTime(support.getCreateTime());

                    // 判断是否为发起人自留样品
                    if (support.getOrderNo() != null && support.getOrderNo().startsWith("INITIATOR_WINNER_")) {
                        vo.setRemark("发起人自留样品 - " + support.getPrizeInfo());
                    }

                    resultList.add(vo);
                }
            }

            // 4. 手动分页处理
            int total = resultList.size();
            int pageNum = pageQuery.getPageNum();
            int pageSize = pageQuery.getPageSize();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            List<SampleDeliveryListVO> pageData = new ArrayList<>();
            if (start < total) {
                pageData = resultList.subList(start, end);
            }

            log.info("获取我的发货项目成功: 项目数量={}, 发货记录数量={}, 分页数据={}", projects.size(), resultList.size(), pageData.size());
            return new TableDataInfo<>(pageData, (long) total);

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
            if (delivery == null) {

                return R.fail("发货记录不存在");
            }

            //3.上传
            SysOssVo oss = ossService.upload(file);
            String uploadUrl = oss.getUrl();

            // 4. 更新发货记录的样品图片地址
            delivery.setSampleImageUrl(uploadUrl);
            int result = sampleDeliveryMapper.updateById(delivery);

            if (result > 0) {
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

}
