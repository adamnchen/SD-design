package com.sutran.sd.controller.web.system;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * [订单]后台API
 * @author zj
 * @date 2025年08月21日 10:59
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/order")
public class SysOrderController extends BaseController {

    private final PayOrderService payOrderService;
    private final AliPayService aliPayService;

    /**
     * 分页获取指定人的订单记录
     */
    @GetMapping("/page")
    public TableDataInfo<PayOrder> list(PayOrder order, PageQuery pageQuery) {
        return payOrderService.selectPageOrderList(order, pageQuery);
    }

    /**
     * 查询指定订单详情
     *
     * @param id 订单ID
     */
    @GetMapping(value = "/detail")
    public R<PayOrder> getInfo(@RequestParam String id) {
        return R.ok(payOrderService.detailById(id));
    }

    /**
     * 手动同步支付宝指定订单支付状态
     * @param outTradeNo 订单号（和tradeNo二选一）
     * @param tradeNo 交易流水号（和outTradeNo二选一）
     * @return 同步完成
     */
    @GetMapping(value = "/sync-order-status")
    public R<Void> syncOrderStatus(@RequestParam(required = false, name = "outTradeNo") String outTradeNo, @RequestParam(required = false, name = "tradeNo") String tradeNo) {
        aliPayService.syncOrderStatus(outTradeNo, tradeNo);
        return R.ok();
    }

}
