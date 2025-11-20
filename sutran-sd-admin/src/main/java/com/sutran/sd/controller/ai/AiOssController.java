package com.sutran.sd.controller.ai;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.BatchIdsDto;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.validate.QueryGroup;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.system.domain.bo.SysOssBo;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI-OSS对象存储
 * @author zj
 * @date 2024-05-28
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiOssController extends BaseController {
    private final ISysOssService iSysOssService;

    /**
     * [OSS] 查询存储列表
     */
    @GetMapping("/oss/list")
    public TableDataInfo<SysOssVo> list(@Validated(QueryGroup.class) SysOssBo bo, PageQuery pageQuery) {
        return iSysOssService.queryPageList(bo, pageQuery);
    }

    /**
     * [OSS] 查询对象基于id串
     *
     * @param dto OSS对象ID串
     */
    @PostMapping("/oss/listByIds")
    public R<List<SysOssVo>> listByIds(@RequestBody BatchIdsDto dto) {
        if (CollectionUtil.isEmpty(dto.getIds())) {
            return R.fail("主键不能为空");
        }
        List<Long> ossIds = dto.getIds().stream().map(Long::parseLong).collect(Collectors.toList());
        List<SysOssVo> list = iSysOssService.listByIds(ossIds);
        return R.ok(list);
    }

    /**
     * [OSS] 上传OSS对象存储
     *
     * @param file 文件
     */
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/oss/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, String>> upload(@RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        SysOssVo oss = iSysOssService.upload(file);
        Map<String, String> map = new HashMap<>(2);
        map.put("url", oss.getUrl());
        map.put("fileName", oss.getOriginalName());
        map.put("ossId", oss.getOssId().toString());
        return R.ok(map);
    }

    /**
     * [OSS] 下载OSS对象
     *
     * @param ossId OSS对象ID
     */
    @GetMapping("/oss/download")
    public void download(@RequestParam String ossId, HttpServletResponse response) throws IOException {
        iSysOssService.download(Long.parseLong(ossId),response);
    }

    /**
     * [OSS] 删除OSS对象存储
     *
     * @param dto OSS对象ID串
     */
    @Log(title = "OSS对象存储", businessType = BusinessType.DELETE)
    @DeleteMapping("/oss")
    public R<Void> remove(@RequestBody BatchIdsDto dto) {
        if (CollectionUtil.isEmpty(dto.getIds())) {
            return R.fail("主键不能为空");
        }
        List<Long> ossIds = dto.getIds().stream().map(Long::parseLong).collect(Collectors.toList());
        return toAjax(iSysOssService.deleteWithValidByIds(ossIds, true));
    }
}
