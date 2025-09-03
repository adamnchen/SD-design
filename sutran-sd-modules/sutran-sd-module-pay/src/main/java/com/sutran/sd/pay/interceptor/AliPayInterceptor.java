package com.sutran.sd.pay.interceptor;

import com.ijpay.alipay.AliPayApiConfigKit;
import com.sutran.sd.pay.controller.BaseAliPayApiController;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zj
 * @date 2025年08月19日 17:47
 */
public class AliPayInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (HandlerMethod.class.equals(handler.getClass())) {
            HandlerMethod method = (HandlerMethod) handler;
            Object controller = method.getBean();
            if (!(controller instanceof BaseAliPayApiController)) {
                throw new RuntimeException("控制器需要继承 BaseAliPayApiController");
            }

            try {
                AliPayApiConfigKit.setThreadLocalAliPayApiConfig(((BaseAliPayApiController)controller).getApiConfig());
                return true;
            }
            finally {
            }
        }
        return false;
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o, Exception e) throws Exception {
    }
}
