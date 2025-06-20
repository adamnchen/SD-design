package com.sutran.sd.web.controller.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.sutran.sd.common.constant.CacheConstants;
import com.sutran.sd.common.constant.Constants;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.enums.CaptchaType;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.email.MailUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.common.utils.reflect.ReflectUtils;
import com.sutran.sd.common.utils.spring.SpringUtils;
import com.sutran.sd.framework.config.properties.CaptchaProperties;
import com.sutran.sd.framework.config.properties.MailProperties;
import com.sutran.sd.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.enumerate.SupplierType;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证码操作处理
 *
 * @author Lion Li
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class CaptchaController {

    private final CaptchaProperties captchaProperties;
    private final ISysConfigService configService;
    private final MailProperties mailProperties;

    /**
     * 短信验证码
     *
     * @param phonenumber 用户手机号
     */
    @GetMapping("/captchaSms")
    public R<Void> smsCaptcha(@NotBlank(message = "{user.phonenumber.not.blank}") String phonenumber) {
        // 先判断
        String templateId = "SMS_465409540";
        String key = CacheConstants.CAPTCHA_CODE_KEY + phonenumber;
        String code = RandomUtil.randomNumbers(4);
        // 验证码模板id 自行处理 (查数据库或写死均可)
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("code", code);
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        log.warn("短信验证码>>>>>>>>>手机号[{}], 在[{}]时发送了验证码[{}]",phonenumber, DateUtil.now(),code);
        SmsResponse smsResponse = SmsFactory.createSmsBlend(SupplierType.ALIBABA).sendMessage(phonenumber, templateId, map);
        if (!"OK".equals(smsResponse.getCode())) {
            log.error("短信验证码>>>>>>>>>手机号[{}], 验证码短信发送异常 => {}", phonenumber, smsResponse);
            return R.fail(smsResponse.getMessage());
        }
        return R.ok();
    }

    /**
     * 邮箱验证码
     *
     * @param email 邮箱
     */
    @GetMapping("/captchaEmail")
    public R<Void> emailCode(@NotBlank(message = "{user.email.not.blank}") String email) {
        if (!mailProperties.getEnabled()) {
            return R.fail("当前系统没有开启邮箱功能！");
        }
        String key = CacheConstants.CAPTCHA_CODE_KEY + email;
        String code = RandomUtil.randomNumbers(4);
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        try {
            MailUtils.sendText(email, "登录验证码", "您本次验证码为：" + code + "，有效性为" + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。");
        } catch (Exception e) {
            log.error("验证码短信发送异常 => {}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public R<Map<String, Object>> getCode() {
        Map<String, Object> ajax = new HashMap<>();
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled) {
            return R.ok(ajax);
        }
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        // 生成验证码
        CaptchaType captchaType = captchaProperties.getType();
        boolean isMath = CaptchaType.MATH == captchaType;
        Integer length = isMath ? captchaProperties.getNumberLength() : captchaProperties.getCharLength();
        CodeGenerator codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), length);
        AbstractCaptcha captcha = SpringUtils.getBean(captchaProperties.getCategory().getClazz());
        captcha.setGenerator(codeGenerator);
        captcha.createCode();
        String code = captcha.getCode();
        if (isMath) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        ajax.put("uuid", uuid);
        ajax.put("img", captcha.getImageBase64());
        return R.ok(ajax);
    }

    /**
     * 获取当前日志输出级别
     * @return 结果
     */
    @GetMapping("/getLoggerLevel")
    public R<String> getLocalLoggerLevel() {
        // 获取 LoggerContext
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // 获取 Logger 实例，这里假设我们要获取 root logger 的级别
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        // 获取当前日志级别
        Level currentLevel = rootLogger.getLevel();
        // 输出当前日志级别
        return R.ok("请求成功","当前日志输出级别为：" + currentLevel.levelStr);
    }

    /**
     * 更改日志输出级别
     * @param level 日志级别[debug,info,warn,error]
     * @return 结果
     */
    @PostMapping("/changeLoggerLevel")
    public R<String> loggerLevelChange(@RequestParam("level") String level){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        switch (level.toLowerCase()) {
            case "debug":
                logger.setLevel(Level.DEBUG);
                break;
            case "info":
                logger.setLevel(Level.INFO);
                break;
            case "warn":
                logger.setLevel(Level.WARN);
                break;
            case "error":
                logger.setLevel(Level.ERROR);
                break;
            default:
                return R.fail("请输入正确的日志级别");
        }
        return R.ok("请求成功","日志输出级别已切换为："+level);
    }

}
