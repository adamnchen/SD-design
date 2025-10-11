package com.sutran.sd.controller.pay;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单记录
 * @author zj
 * @date 2025年08月21日 10:59
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/pay/order")
public class PayOrderController extends BaseController {

    private final PayOrderService payOrderService;

    /**
     * [用户]分页获取当前登录人的订单记录
     */
    @GetMapping("/page")
    public TableDataInfo<PayOrder> page(PayOrder order, PageQuery pageQuery) {
        order.setUserId(LoginHelper.getUserId());
        return payOrderService.selectPageOrderList(order, pageQuery);
    }

    /**
     * [用户]查询当前用户的指定订单详情
     *
     * @param id 订单ID
     */
    @GetMapping(value = "/detail")
    public R<PayOrder> getInfo(@RequestParam String id) {
        return R.ok(payOrderService.detailByIdAndUserId(id, LoginHelper.getUserId()));
    }

    /**
     * [用户]根据订单号查询支付码
     *
     * @param outTradeNo 订单号
     */
    @GetMapping(value = "/qr")
    public R<String> getQr(@RequestParam String outTradeNo) {
        return R.ok(payOrderService.getPayQr(outTradeNo,LoginHelper.getUserId()));
    }
}
