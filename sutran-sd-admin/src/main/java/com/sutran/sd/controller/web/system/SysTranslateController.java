package com.sutran.sd.controller.web.system;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.system.service.SysTranslateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;

/**
 * 翻译API
 * @author zj
 * @date 2024-04-07
 */
@Slf4j
@RestController
@RequestMapping("/system/translate")
public class SysTranslateController {

    @Resource
    private SysTranslateService sysTranslateService;

    /**
     * 中转英
     */
    @GetMapping("/zh-to-en")
    public R<String> zhToEn(@RequestParam("content")String content) throws NoSuchAlgorithmException {
        return R.ok("请求成功", sysTranslateService.zhToEn(content, TranslateType.BAIDU));
    }

}
