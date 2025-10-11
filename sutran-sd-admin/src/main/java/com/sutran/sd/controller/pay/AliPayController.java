package com.sutran.sd.controller.pay;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.ijpay.alipay.AliPayApiConfig;
import com.sutran.sd.common.annotation.RepeatSubmit;
import com.sutran.sd.pay.controller.BaseAliPayApiController;
import com.sutran.sd.pay.service.AliPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 支付宝支付
 * @author zj
 * @date 2025年08月19日 18:08
 */
@RestController
@RequestMapping("/pay/ali")
@RequiredArgsConstructor
public class AliPayController extends BaseAliPayApiController {

    private final AliPayService aliPayService;

    @Override
    public AliPayApiConfig getApiConfig() {
        return aliPayService.getConfig();
    }

    /**
     * [用户]创建预付款订单二维码
     * @param memberId 会员id
     */
    @GetMapping(value ="/preCreateOrder")
    @RepeatSubmit()
    @SaIgnore
    public void preCreateOrder(@RequestParam String memberId, HttpServletResponse response) throws IOException {
        String qrCode = aliPayService.preCreateMemberOrder(memberId);
        // 使用hutool生成二维码图片返回
        QrCodeUtil.generate(qrCode, 300, 300, "png", response.getOutputStream());
    }

    /**
     * [回调]支付宝支付回调
     */
    @PostMapping(value = "/notify_url")
    @SaIgnore
    public String notifyUrl(HttpServletRequest request) {
        return aliPayService.notifyUrl(request);
    }

}
