package com.sutran.sd.controller.sdapi;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.domain.dto.BatchRemoveDto;
import com.sutran.sd.draw.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.draw.service.SdWebuiApiService;
import com.sutran.sd.draw.domain.vo.SdUserModelFileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SD-设计库接口
 * @author zj
 * @date 2024-03-10
 */
@RestController
@RequestMapping("/sd/design")
@RequiredArgsConstructor
public class SdDesignController {

    @Resource
    private SdWebuiApiService sdWebuiApiService;

    /**
     * [业务接口]SD设计库-获取当前用户绘图数据列表
     */
    @GetMapping("/model-file/list")
    public TableDataInfo<SdUserModelFileVo> userModelFileList(SdUserModelFilePageDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        return sdWebuiApiService.listUserModelFile(pageQuery,dto);
    }

    /**
     * [业务接口]SD设计库-批量删除当前用户绘图数据列表
     */
    @DeleteMapping("/model-file/remove")
    public R<Void> removeUserModelFile(@RequestBody BatchRemoveDto dto) {
        sdWebuiApiService.removeUserModelFile(dto.getIds());
        return R.ok();
    }

    /**
     * [业务接口]SD设计库-批量删除当前用户绘图数据列表
     */
    @PostMapping("/model-file/batch/download")
    public void batchDownloadUserModelFile(@RequestBody BatchRemoveDto dto, HttpServletResponse response) throws IOException {
        if (CollectionUtil.isEmpty(dto.getIds())) {
            return;
        }
        sdWebuiApiService.batchDownloadUserModelFile(dto.getIds(),response);
    }

    /**
     * [业务接口]SD设计库-批量删除当前用户绘图数据列表
     */
    @GetMapping("/model-file/download")
    public void downloadUserModelFile(@RequestParam String imgUrl, HttpServletResponse response) throws IOException {
        if (StrUtil.isEmpty(imgUrl)) {
            return;
        }
        sdWebuiApiService.downloadUserModelFile(imgUrl,response);
    }

}
