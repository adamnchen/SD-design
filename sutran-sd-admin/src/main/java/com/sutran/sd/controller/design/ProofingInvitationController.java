package com.sutran.sd.controller.design;

import com.meilisearch.sdk.model.SearchResult;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.design.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
    private final IManufacturerSearchService searchService;

    /**
     * 发起一个新的合作邀约
     */
    @PostMapping
    public R<SdProofingInvitation> createInvitation(@Validated @RequestBody ProofingInvitationRequestDTO createDTO) {
        SdProofingInvitation invitation = invitationService.createInvitation(createDTO);
        return R.ok("邀约发送成功");
    }

    /**
     * 获取我收到的邀约列表
     */
    @GetMapping("/received")
    public R<List<ProofingInvitationDetailVO>> getMyReceivedInvitations() {
        List<ProofingInvitationDetailVO> list = invitationService.getReceivedInvitations();
        return R.ok(list);
    }

    /**
     * 获取我发出的邀约列表
     */
    @GetMapping("/sent")
    public R<List<ProofingInvitationDetailVO>> getMySentInvitations() {
        List<ProofingInvitationDetailVO> list = invitationService.getSentInvitations();
        return R.ok(list);
    }

    /**
     * 接受合作邀约
     */
    @PutMapping("/{id}/accept")
    public R<Void> acceptInvitation(@PathVariable("id") Long id) {
        invitationService.acceptInvitation(id);
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
     * 根据标签搜索厂商
     * @param tags 从 URL 查询参数中获取的标签，例如: /search?tags=首饰
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String tags) {
        try {
            SearchResult results = searchService.search(tags);
            // 将 MeiliSearch 的原始结果直接返回给前端
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("搜索时发生错误: " + e.getMessage());
        }
    }
}
