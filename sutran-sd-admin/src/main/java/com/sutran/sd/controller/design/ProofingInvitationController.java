package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationChooseDto;
import com.sutran.sd.common.core.domain.vo.ManufacturerSearchResultVO;
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
     * 接受合作邀约
     */
    @PutMapping("/{id}/accept")
    public R<Void> acceptInvitation(@PathVariable("id") Long id, @Validated @RequestBody com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto dto) {
        if (dto.getInvitationId() == null) {
            dto.setInvitationId(id);
        } else if (!id.equals(dto.getInvitationId())) {
            return R.fail("路径ID与请求体的邀约ID不一致");
        }
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
        if (dto.getInvitationId() == null) {
            dto.setInvitationId(id);
        } else if (!id.equals(dto.getInvitationId())) {
            return R.fail("路径ID与请求体的邀约ID不一致");
        }
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
}
