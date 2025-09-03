package com.sutran.sd.system.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.constant.CacheConstants;
import com.sutran.sd.common.constant.Constants;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.event.LogininforEvent;
import com.sutran.sd.common.core.domain.model.RegisterBody;
import com.sutran.sd.common.enums.UserType;
import com.sutran.sd.common.exception.user.CaptchaException;
import com.sutran.sd.common.exception.user.CaptchaExpireException;
import com.sutran.sd.common.exception.user.UserException;
import com.sutran.sd.common.utils.MessageUtils;
import com.sutran.sd.common.utils.ServletUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.common.utils.spring.SpringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 注册校验方法
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;
    private final ISysConfigService configService;

    /**
     * 注册
     */
    public void register(RegisterBody registerBody) {
        String username = registerBody.getPhoneNumber();
        String nickName = StrUtil.isBlankIfStr(registerBody.getNickName())?registerBody.getNickName():"BS_USER_"+System.currentTimeMillis();
        String password = registerBody.getPassword();
        String phoneNumber = registerBody.getPhoneNumber();
        // 校验用户类型是否存在
        String userType = UserType.getUserType(registerBody.getUserType()).getUserType();
        // 校验验证码
//        if (!validateSmsCode(phoneNumber, registerBody.getSmsCode())) {
        if (!validateCaptcha(registerBody.getVerifyCode(),registerBody.getVerifyUuid())) {
            throw new CaptchaException();
        }

        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(nickName);
        sysUser.setPhonenumber(phoneNumber);
        sysUser.setEmail(registerBody.getEmail());
        sysUser.setPassword(BCrypt.hashpw(password));
        sysUser.setUserType(userType);
        sysUser.setChannelId("3");
        sysUser.setChannel("自行注册");
        String drawNum = configService.selectConfigByKey("registry.user.drawNum");
        drawNum = StringUtils.isEmpty(drawNum)?"10":drawNum;
        sysUser.setLimitDrawNum(Integer.parseInt(drawNum));
        sysUser.setLimitTrainTimes(0);

        if (!userService.checkUserNameUnique(sysUser)) {
            throw new UserException("user.register.save.error", username);
        }
        if (!userService.checkPhoneUnique(sysUser)) {
            throw new UserException("user.register.save.error", username);
        }
        boolean regFlag = userService.registerUser(sysUser);
        if (!regFlag) {
            throw new UserException("user.register.error");
        }
        recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success"));
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    /**
     * 校验验证码
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public boolean validateCaptcha(String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captchaCode = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (StringUtils.isBlank(code)) {
            throw new CaptchaExpireException();
        }
        return code.equalsIgnoreCase(captchaCode);
    }

    /**
     * 校验短信验证码
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     */
    private boolean validateSmsCode(String phoneNumber, String smsCode) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + phoneNumber;
        String code = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (StringUtils.isBlank(code)) {
            throw new CaptchaExpireException();
        }
        return code.equals(smsCode);
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    private void recordLogininfor(String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }

}
