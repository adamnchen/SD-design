package com.sutran.sd.controller.sdapi;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelClassifyDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelModifyDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelShareDto;
import com.sutran.sd.sdapi.modules.webui.SdApiService;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelClassifyVo;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * SD-模型接口
 * @author zj
 * @date 2024-02-27
 */
@RestController
@RequestMapping("/sd/model")
@RequiredArgsConstructor
public class SdModelController {

    @Resource
    private SdApiService sdApiService;

    /**
     * [业务接口]SD Lora模型-获取lora模型分类列表
     */
    @GetMapping("/lora/classify/list")
    public R<List<SdUserModelClassifyVo>> listModelClassify() {
        return R.ok(sdApiService.listModelClassify());
    }

    /**
     * [业务接口]SD Lora模型-添加lora模型分类列表
     */
    @PostMapping("/lora/classify/add")
    public R<Void> addModelClassify(@RequestBody SdUserModelClassifyDto dto) {
        sdApiService.addModelClassify(dto);
        return R.ok();
    }

    /**
     * [业务接口]SD Lora模型-修改lora模型分类列表
     */
    @PutMapping("/lora/classify/modify")
    public R<Void> modifyModelClassify(@RequestBody SdUserModelClassifyDto dto) {
        if ("1".equals(dto.getId())) {
            throw new ServiceException("当前分类不可修改!");
        }
        sdApiService.modifyModelClassify(dto);
        return R.ok();
    }

    /**
     * [业务接口]SD Lora模型-删除lora模型分类列表
     */
    @DeleteMapping("/lora/classify/remove")
    public R<Void> removeModelClassify(@RequestParam String id) {
        if ("1".equals(id)) {
            throw new ServiceException("当前分类不可删除!");
        }
        sdApiService.removeModelClassify(id);
        return R.ok();
    }

    /**
     * [业务接口]SD Lora模型-获取lora模型列表
     */
    @GetMapping("/lora/list")
    public TableDataInfo<SdUserModelVo> listLoraModels(SdUserModelPageDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        return sdApiService.listLoraModels(dto,pageQuery);
    }

    /**
     * [业务接口]SD Lora模型-修改模型基础信息(仅能修改个人模型,系统模型不能修改)
     */
    @PutMapping("/lora")
    public R<Void> modifyModel(@RequestBody SdUserModelModifyDto dto) {
        sdApiService.modifyModel(dto);
        return R.ok();
    }

    /**
     * [业务接口]SD Lora模型-根据ID删除个人模型
     */
    @DeleteMapping("/lora/remove")
    public R<Void> removeModel(@RequestParam String id) {
        sdApiService.removeModel(id);
        return R.ok();
    }

    /**
     *  [业务接口]SD Lora模型-获取模型详情
     */
    @GetMapping("/lora/info")
    public R<SdUserModelVo> getModelInfo(@RequestParam String id) {
        return R.ok(sdApiService.getModelInfo(id));
    }

    /**
     * [业务接口]SD Lora模型-获取最近使用的模型(返回最近5个模型)
     */
    @GetMapping("/lora/latest")
    public R<List<SdUserModelVo>> getLatestModelInfo() {
        return R.ok(sdApiService.getLatestModelInfo(5));
    }

    /**
     * [业务接口]SD Lora模型-分享模型
     * @param dto 分享请求参数
     * @return 返回分享结果
     */
    @PostMapping("/lora/share")
    public R<Void> shareModel(@RequestBody SdUserModelShareDto dto) {
        if (CollectionUtil.isEmpty(dto.getModelIds())) {
            throw new ServiceException("请选择要分享的模型!");
        }
        if (StrUtil.isBlankIfStr(dto.getToShareUserId()) && StrUtil.isBlankIfStr(dto.getToSharePhone())){
            throw new ServiceException("请选择要分享的用户或者手机号!");
        }
        sdApiService.shareModel(dto);
        return R.ok();
    }

}
