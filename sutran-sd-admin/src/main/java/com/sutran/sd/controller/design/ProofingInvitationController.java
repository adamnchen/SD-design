package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationChooseDto;
import com.sutran.sd.common.core.domain.vo.ManufacturerSearchResultVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.design.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.List;

/**
 * 设计接口
 */
@RestController
@RequestMapping("/design/idea-center/proofing/invitations")
@RequiredArgsConstructor
public class ProofingInvitationController {

    private final ISdProofingInvitationService invitationService;
    private final IIndexingService indexingService;
    private final IFuzzySearchService fuzzySearchService;

    /**
     * 发起一个新的合作邀约
     */
    @PostMapping
    public R<String> createInvitation(@Validated @RequestBody ProofingInvitationRequestDTO createDTO) {
        invitationService.createInvitation(createDTO);
        return R.ok("邀约发送成功");
    }

    /**
     * 获取我收到的邀约列表
     */
    @GetMapping("/received")
    public R<List<ProofingInvitationDetailVO>> getMyReceivedInvitations() {
        List<ProofingInvitationDetailVO> list = invitationService.getReceivedInvitations();

        if (list == null || list.isEmpty()) {
            return R.ok("暂无收到的邀约", list);
        }

        return R.ok("成功获取 " + list.size() + " 个邀约", list);
    }

    /**
     * 获取我发出的邀约列表
     */
    @GetMapping("/sent")
    public R<List<ProofingInvitationDetailVO>> getMySentInvitations() {
        List<ProofingInvitationDetailVO> list = invitationService.getSentInvitations();

        if (list == null || list.isEmpty()) {
            return R.ok("暂无发出的邀约", list);
        }

        return R.ok("成功获取 " + list.size() + " 个邀约", list);
    }

    /**
     * 分页查询我收到的邀约列表
     */
    @GetMapping("/received/page")
    public TableDataInfo<ProofingInvitationDetailVO> getMyReceivedInvitationsPage(PageQuery pageQuery) {
        return invitationService.getReceivedInvitationsPage(pageQuery);
    }

    /**
     * 分页查询我发出的邀约列表
     */
    @GetMapping("/sent/page")
    public TableDataInfo<ProofingInvitationDetailVO> getMySentInvitationsPage(PageQuery pageQuery) {
        return invitationService.getSentInvitationsPage(pageQuery);
    }

    /**
     * 接受合作邀约
     */
    @PutMapping("/{id}/accept")
    public R<Void> acceptInvitation(@PathVariable("id") Long id, @Validated @RequestBody com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto dto) {
        dto.setInvitationId(id);
        invitationService.acceptInvitation(dto);
        return R.ok("已接受合作邀约");
    }

    /**
     * 拒绝合作邀约
     */
    @PutMapping("/{id}/reject")
    public R<Void> rejectInvitation(@PathVariable("id") Long id) {
        invitationService.rejectInvitation(id);
        return R.ok( "已拒绝合作邀约");
    }

    /**
     * 查看邀约详情
     */
    @GetMapping("/{id}")
    public R<ProofingInvitationDetailVO> getInvitationDetail(@PathVariable("id") Long id) {
        ProofingInvitationDetailVO detail = invitationService.getInvitationDetail(id);
        return R.ok(detail);
    }

    /**
     * 取消已发出的邀约
     */
    @DeleteMapping("/{id}")
    public R<Void> cancelInvitation(@PathVariable("id") Long id) {
        invitationService.cancelInvitation(id);
        return R.ok("邀约已成功取消");
    }

    /**
     * meilisearch索引测试
     * @return
     */
    @GetMapping("/reindex-test")
    public ResponseEntity<String> reindex() {

        try {
            indexingService.indexSampleData();
            indexingService.indexDataFromDatabase();
            return ResponseEntity.ok("sql数据已提交");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("索引失败: " + e.getMessage());
        }
    }


    /**
     * 根据标签搜索厂商（使用模糊匹配）
     * @param tags 从 URL 查询参数中获取的标签，例如: /search?tags=首饰
     * @return 搜索结果
     */
    @GetMapping("/search")
    public R<List<ManufacturerSearchResultVO>> search(@RequestParam String tags) {
        try {
            List<ManufacturerSearchResultVO> results = fuzzySearchService.searchManufacturers(tags);
            return R.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("搜索时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据多个标签搜索厂商
     * @param tags 标签列表，用逗号分隔，例如: /search/tags?tags=首饰,珠宝,金饰
     * @return 搜索结果
     */
    @GetMapping("/search/tags")
    public R<List<ManufacturerSearchResultVO>> searchByTags(@RequestParam String tags) {
        try {
            List<String> tagList = Arrays.asList(tags.split(","));
            List<ManufacturerSearchResultVO> results = fuzzySearchService.searchManufacturersByTags(tagList);
            return R.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("搜索时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据厂家名字模糊搜索厂商
     * @param name 厂家名字关键词，例如: /search/name?name=金饰厂
     * @return 搜索结果
     */
    @GetMapping("/search/name")
    public R<List<ManufacturerSearchResultVO>> searchByName(@RequestParam String name) {
        try {
            List<ManufacturerSearchResultVO> results = fuzzySearchService.searchManufacturersByName(name);
            return R.ok("找到 " + results.size() + " 个匹配的厂家", results);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("搜索时发生错误: " + e.getMessage());
        }
    }

    /**
     * 测试模糊匹配功能
     * @param keyword 测试关键词
     * @return 搜索结果
     */
    @GetMapping("/test-search")
    public R<List<ManufacturerSearchResultVO>> testSearch(@RequestParam(defaultValue = "首饰") String keyword) {
        try {
            List<ManufacturerSearchResultVO> results = fuzzySearchService.searchManufacturers(keyword);
            return R.ok("模糊匹配测试完成，找到 " + results.size() + " 个匹配结果", results);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("测试搜索时发生错误: " + e.getMessage());
        }
    }

    /**
     * 手动触发自动取消超时邀约
     * @return 操作结果
     */
    @PostMapping("/auto-cancel-expired")
    public R<String> autoCancelExpiredInvitations() {
        try {
            invitationService.autoCancelExpiredInvitations();
            return R.ok("自动取消超时邀约完成");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("自动取消超时邀约失败: " + e.getMessage());
        }
    }

    /**
     * 发起人最终选择一家厂家
     */
    @PostMapping("/{id}/choose")
    public R<Void> chooseCandidate(@PathVariable("id") Long id, @Validated @RequestBody ProofingInvitationChooseDto dto) {

        dto.setInvitationId(id);
        invitationService.chooseCandidate(dto);
        return R.ok("已选择最终合作厂家");
    }

    /**
     * 查看某邀约的候选厂家列表
     */
    @GetMapping("/{id}/candidates")
    public R<List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO>> getCandidates(@PathVariable("id") Long id) {
        List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> list = invitationService.getInvitationCandidates(id);
        return R.ok(list);
    }

    // === 商家查看已处理邀约相关接口 ===

    /**
     * 分页查询商家已处理的邀约列表
     * 商家只能查看自己作为被邀约人的邀约
     *
     * @param pageQuery 分页参数
     * @param status 邀约状态（可选）：0-待处理, 1-已接受, 2-已拒绝, 3-已取消
     * @return 分页结果
     */
    @GetMapping("/merchant/processed/page")
    public R<TableDataInfo<ProofingInvitationDetailVO>> getMerchantProcessedInvitationsPage(PageQuery pageQuery,
                                                                                           @RequestParam(required = false) Integer status) {
        try {
            TableDataInfo<ProofingInvitationDetailVO> result = invitationService.getMerchantProcessedInvitationsPage(pageQuery, status);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询商家已处理的邀约列表（不分页）
     *
     * @param status 邀约状态（可选）
     * @return 邀约列表
     */
    @GetMapping("/merchant/processed/list")
    public R<List<ProofingInvitationDetailVO>> getMerchantProcessedInvitationsList(@RequestParam(required = false) Integer status) {
        try {
            List<ProofingInvitationDetailVO> result = invitationService.getMerchantProcessedInvitationsList(status);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询商家已处理的邀约详情
     *
     * @param id 邀约ID
     * @return 邀约详情
     */
    @GetMapping("/merchant/processed/{id}")
    public R<ProofingInvitationDetailVO> getMerchantProcessedInvitationById(@PathVariable Long id) {
        try {
            ProofingInvitationDetailVO result = invitationService.getMerchantProcessedInvitationById(id);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 统计商家已处理的邀约总数
     *
     * @param status 邀约状态（可选）
     * @return 邀约总数
     */
    @GetMapping("/merchant/processed/count")
    public R<Long> countMerchantProcessedInvitations(@RequestParam(required = false) Integer status) {
        try {
            Long count = invitationService.countMerchantProcessedInvitations(status);
            return R.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("统计失败：" + e.getMessage());
        }
    }
}
