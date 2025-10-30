package com.sutran.sd.controller.web.system;

import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.pay.service.PayMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [会员]后台API
 * @author zj
 * @date 2025年08月21日 10:59
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/member")
public class SysMemberController extends BaseController {

    private final PayMemberService payMemberService;

    /**
     * [前端]获取会员配置列表
     * @param config 会员配置
     * @return 会员配置列表
     */
    @GetMapping("/list")
    public R<List<PayMember>> list(PayMember config) {
        List<PayMember> list = payMemberService.selectMemberList(config);
        return R.ok(list);
    }

    /**
     * [后台]获取会员配置分页列表
     * @param config    会员配置
     * @param pageQuery 分页查询参数
     * @return          会员配置分页列表
     */
    @GetMapping("/page")
    public TableDataInfo<PayMember> page(PayMember config, PageQuery pageQuery) {
        return payMemberService.selectMemberPage(config,pageQuery);
    }

    /**
     * [后台]查询会员配置详情
     *
     * @param id 会员配置ID
     * @return 会员配置详情
     */
    @GetMapping(value = "/detail")
    public R<PayMember> getInfo(@RequestParam String id) {
        return R.ok(payMemberService.detailById(id));
    }

    /**
     * [通用]新增会员配置
     */
    @Log(title = "会员配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> insert(@Validated @RequestBody PayMember config) {
        return toAjax(payMemberService.insert(config));
    }

    /**
     * [后台]修改会员配置
     * @param member 会员配置
     * @return 修改结果
     */
    @Log(title = "会员配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> modify(@Validated @RequestBody PayMember member) {
        return toAjax(payMemberService.updateById(member));
    }

    /**
     * [后台]删除会员配置
     *
     * @param id 会员配置ID
     * @return 删除结果
     */
    @Log(title = "会员配置", businessType = BusinessType.DELETE)
    @DeleteMapping
    public R<Void> remove(@RequestParam String id) {
        // 判断是否有用户正在使用该会员
        boolean hasUser = payMemberService.hasUser(id);
        if (hasUser) {
            throw new ServiceException("该会员下有用户正在使用，不能删除!");
        }
        boolean result = payMemberService.removeById(id);
        return toAjax(result);
    }
}
