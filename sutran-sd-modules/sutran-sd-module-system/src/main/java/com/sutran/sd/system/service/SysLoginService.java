package com.sutran.sd.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.constant.CacheConstants;
import com.sutran.sd.common.constant.Constants;
import com.sutran.sd.common.core.domain.dto.RoleDTO;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.event.LogininforEvent;
import com.sutran.sd.common.core.domain.model.LoginUser;
import com.sutran.sd.common.core.domain.model.XcxLoginUser;
import com.sutran.sd.common.enums.DeviceType;
import com.sutran.sd.common.enums.LoginType;
import com.sutran.sd.common.enums.UserStatus;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.exception.user.CaptchaException;
import com.sutran.sd.common.exception.user.CaptchaExpireException;
import com.sutran.sd.common.exception.user.UserException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.DateUtils;
import com.sutran.sd.common.utils.MessageUtils;
import com.sutran.sd.common.utils.ServletUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.common.utils.spring.SpringUtils;
import com.sutran.sd.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.sutran.sd.common.enums.UserType.BS_USER;

/**
 * 登录校验方法
 * @author Lion Li
 */
@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginService {

    private final SysUserMapper userMapper;
    private final ISysConfigService configService;
    private final SysPermissionService permissionService;

    @Resource
    private WxMpService wxMpService;
    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;
    @Value("${user.password.lockTime}")
    private Integer lockTime;

    /**
     * 用户名密码登录
     *
     * @param username      用户名
     * @param password      密码
     * @param code          验证码
     * @param uuid          唯一标识
     * @param deviceType    设备类型
     * @param ajax          ajax返回数据
     */
    public void login(String username, String password, String code, String uuid, DeviceType deviceType, Map<String, Object> ajax) {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(username, code, uuid);
        }
        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUser user = loadUserByUsername(username);
        checkLogin(LoginType.PASSWORD, username, user.getUserId(), user.getUserType(), () -> !BCrypt.checkpw(password, user.getPassword()));
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, deviceType);

        recordLogininfor(user.getUserId(),user.getUserType(),username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginIp(user.getUserId(), username);

        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        ajax.put("isCloseGuide", user.getIsCloseGuide());
        ajax.put("isNewUser", judgeIsNewUser(user.getCreateTime()));
    }

    private Integer judgeIsNewUser(Date createTime) {
        String isNewUserDay = configService.selectConfigByKey("sys.user.isNewUser");
        int isNewUser = 0;
        if (StringUtils.isNotBlank(isNewUserDay)) {
            Date beforeDate = DateUtils.addDays(DateUtils.getNowDate(), -(Integer.parseInt(isNewUserDay)));
            if (beforeDate.compareTo(createTime) < 0) {
                isNewUser = 1;
            }
        }
        return isNewUser;
    }

    /**
     * 第三方注册或登录
     *
     * @param phone       手机号
     * @param thirdUserId 第三方系统ID
     * @param channel     渠道来源
     * @param channelId   渠道来源ID
     * @param deviceType  设备类型
     * @param ajax        ajax返回数据
     */
    public void registerOrLogin(String phone, String thirdUserId, String channel, String channelId, DeviceType deviceType, Map<String, Object> ajax) {
        /// 通过渠道ID+手机号查找用户
        SysUser user = loadThirdUserByPhonenumber(phone,channelId,channel,thirdUserId);
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, deviceType);

        recordLogininfor(user.getUserId(),user.getUserType(),phone,Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginIp(user.getUserId(), phone);

        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        ajax.put("isCloseGuide", user.getIsCloseGuide());
        ajax.put("isNewUser", judgeIsNewUser(user.getCreateTime()));
    }

    /** 短信登录 **/
    public void smsLogin(String phonenumber, String smsCode, DeviceType deviceType, Map<String, Object> ajax) {
        // 通过手机号查找用户
        SysUser user = loadSysUserByPhonenumber(phonenumber);

        checkLogin(LoginType.SMS, user.getUserName(), user.getUserId(), user.getUserType(), () -> !validateSmsCode(phonenumber, smsCode));
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, deviceType);

        recordLogininfor(user.getUserId(), user.getUserType(), user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginIp(user.getUserId(), user.getUserName());

        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        ajax.put("isCloseGuide", user.getIsCloseGuide());
        ajax.put("isNewUser", judgeIsNewUser(user.getCreateTime()));
    }

    /**
     * 邮箱登录
     **/
    public void emailLogin(String email, String emailCode, DeviceType deviceType, Map<String, Object> ajax) {
        // 通过手邮箱查找用户
        SysUser user = loadSysUserByEmail(email);

        checkLogin(LoginType.EMAIL, user.getUserName(), user.getUserId(), user.getUserType(), () -> !validateEmailCode(email, emailCode));
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, deviceType);

        recordLogininfor(user.getUserId(), user.getUserType(), user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginIp(user.getUserId(), user.getUserName());

        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        ajax.put("isCloseGuide", user.getIsCloseGuide());
        ajax.put("isNewUser", judgeIsNewUser(user.getCreateTime()));
    }

    /**
     * 小程序授权登录
     **/
    public void xcxLogin(String code, Map<String, Object> ajax) {
        // xcxCode 为 小程序调用 wx.login 授权后获取
        // todo 以下自行实现
        // 校验 appid + appsrcret + xcxCode 调用登录凭证校验接口 获取 session_key 与 openid
        String openid = "";

        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUser user = loadSysUserByOpenid(openid);

        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        XcxLoginUser loginUser = new XcxLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
        loginUser.setOpenid(openid);
        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.BS_XCX);

        recordLogininfor(user.getUserId(), user.getUserType(), user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginIp(user.getUserId(), user.getUserName());

        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        ajax.put("isCloseGuide", user.getIsCloseGuide());
        ajax.put("isNewUser", judgeIsNewUser(user.getCreateTime()));
    }

    /**
     * 微信公众号授权登录
     **/
    public void wxMpLogin(String code, DeviceType deviceType, Map<String, Object> ajax) throws WxErrorException {
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUser user = loadSysUserByOpenid(accessToken.getOpenId());

        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        XcxLoginUser loginUser = new XcxLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
        loginUser.setOpenid(accessToken.getOpenId());
        // 生成token
        LoginHelper.loginByDevice(loginUser, deviceType);

        recordLogininfor(user.getUserId(), user.getUserType(), user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginIp(user.getUserId(), user.getUserName());

        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        ajax.put("isCloseGuide", user.getIsCloseGuide());
        ajax.put("isNewUser", judgeIsNewUser(user.getCreateTime()));
    }

    /** 微信公众号授权登录 **/
    public void wxMpBind(String code) throws WxErrorException {
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUser sysUser = new SysUser();
        sysUser.setUserId(LoginHelper.getUserId());
        sysUser.setWxOpenId(accessToken.getOpenId());
        try{
            userMapper.updateById(sysUser);
        }
        catch (DuplicateKeyException e) {
            throw new ServiceException("当前微信已经绑定了其他账号了,绑定失败!");
        }
    }

    /** 微信公众号授权解绑 **/
    public void wxMpUnBind() {
        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUser sysUser = new SysUser();
        sysUser.setUserId(LoginHelper.getUserId());
        sysUser.setWxOpenId("");
        userMapper.updateById(sysUser);
    }

    /**
     * 退出登录
     */
    public void logout() {
        try {
            LoginUser loginUser = LoginHelper.getLoginUser();
            assert loginUser != null;
            recordLogininfor(loginUser.getUserId(), loginUser.getUserType(), loginUser.getUsername(), Constants.LOGOUT, MessageUtils.message("user.logout.success"));
        } catch (NotLoginException ignored) {
        } finally {
            try {
                StpUtil.logout();
            } catch (NotLoginException ignored) {
            }
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     */
    private void recordLogininfor(Long userId, String userType, String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUserId(userId);
        logininforEvent.setUserType(userType);
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }

    /**
     * 校验短信验证码
     */
    private boolean validateSmsCode(String phonenumber, String smsCode) {
        String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + phonenumber);
        if (StringUtils.isBlank(code)) {
            recordLogininfor(null, null, phonenumber, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        return code.equals(smsCode);
    }

    /**
     * 校验邮箱验证码
     */
    private boolean validateEmailCode(String email, String emailCode) {
        String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + email);
        if (StringUtils.isBlank(code)) {
            recordLogininfor(null, null, email, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        return code.equals(emailCode);
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
            recordLogininfor(null, null, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            recordLogininfor(null, null, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    /** 手机号登录系统后台 **/
    private SysUser loadThirdUserByPhonenumber(String phonenumber,String channelId,String channel,String channelUserId) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getPhonenumber, SysUser::getStatus, SysUser::getLimitValidDate, SysUser::getChannelId, SysUser::getChannelUserId)
            .eq(SysUser::getPhonenumber, phonenumber));
        if (ObjectUtil.isNull(user)) {
            String initPwd = configService.selectConfigByKey("third.user.initPassword");
            initPwd = StringUtils.isEmpty(initPwd)?"123456":initPwd;
            String validDay = configService.selectConfigByKey("third.user.validDay");
            validDay = StringUtils.isEmpty(validDay)?"3":validDay;
            String trainTimes = configService.selectConfigByKey("third.user.trainTimes");
            trainTimes = StringUtils.isEmpty(trainTimes)?"2":trainTimes;
            String drawNum = configService.selectConfigByKey("third.user.drawNum");
            drawNum = StringUtils.isEmpty(drawNum)?"200":drawNum;
            // 添加用户
            user = new SysUser();
            user.setUserId(IdUtil.getSnowflakeNextId());
            user.setUserName(phonenumber);
            user.setPhonenumber(phonenumber);
            user.setNickName(phonenumber);
            user.setPassword(BCrypt.hashpw(initPwd));
            user.setUserType(BS_USER.getUserType());
            user.setChannel(channel);
            user.setChannelId(channelId);
            user.setChannelUserId(channelUserId);
            user.setLimitValidDate(DateUtil.offsetDay(new Date(), Integer.parseInt(validDay)));
            user.setLimitTrainTimes(Integer.parseInt(trainTimes));
            user.setLimitDrawNum(Integer.parseInt(drawNum));
            user.setRemark("第三方登录自动注册");
            try {
                int insert = userMapper.insert(user);
                if (insert<=0) {
                    throw new UserException("user.register.error");
                }
                
                // 为第三方登录用户自动创建默认身份标签
                createDefaultIdentityTagForThirdParty(user.getUserId());
            }
            catch (DuplicateKeyException e) {
                log.warn("[用户注册]>>>>>>>>>手机号[{}],渠道[{}]已存在用户",phonenumber,channel);
                throw new UserException("user.register.save.error", phonenumber);
            }
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.warn("登录用户：{} 已被停用.", phonenumber);
            throw new UserException("user.blocked", phonenumber);
        }
        // 非系统用户(渠道用户) 且 已过期
        else if (!"1".equals(user.getChannelId()) && new Date().after(user.getLimitValidDate())) {
            log.warn("登录用户：{} 已失效.", phonenumber);
            throw new UserException("user.expired", phonenumber);
        }
        else if (!"1".equals(user.getChannelId()) && !channelUserId.equals(user.getChannelUserId())) {
            log.warn("[用户注册]>>>>>>>>>手机号[{}],渠道[{}]已存在用户",phonenumber,channel);
            throw new UserException("user.register.save.error", phonenumber);
        }
        return userMapper.selectUserByPhonenumber(phonenumber);
    }

    /** 用户名登录系统后台 **/
    private SysUser loadUserByUsername(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserName, SysUser::getStatus)
            .eq(SysUser::getUserName, username));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return userMapper.selectUserByUserName(username);
    }

    /** 手机号登录系统后台 **/
    private SysUser loadSysUserByPhonenumber(String phonenumber) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getPhonenumber, SysUser::getStatus)
            .eq(SysUser::getPhonenumber, phonenumber));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", phonenumber);
            throw new UserException("user.not.exists", phonenumber);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", phonenumber);
            throw new UserException("user.blocked", phonenumber);
        }
        return userMapper.selectUserByPhonenumber(phonenumber);
    }

    /** 邮箱登录系统后台 **/
    private SysUser loadSysUserByEmail(String email) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getPhonenumber, SysUser::getStatus)
            .eq(SysUser::getEmail, email));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", email);
            throw new UserException("user.not.exists", email);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", email);
            throw new UserException("user.blocked", email);
        }
        return userMapper.selectUserByEmail(email);
    }

    /** 微信授权登录系统后台 **/
    private SysUser loadSysUserByOpenid(String openid) {
        SysUser user = userMapper.selectUserByWxOpenId(openid);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", openid);
            throw new ServiceException("用户不存在", 40501);
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", openid);
            throw new ServiceException("用户已被停用", 500);
        }
        return user;
    }

    /**
     * 构建登录用户
     */
    private LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
        loginUser.setMenuPermission(permissionService.getMenuPermission(user));
        loginUser.setRolePermission(permissionService.getRolePermission(user));
        loginUser.setDeptName(ObjectUtil.isNull(user.getDept()) ? "" : user.getDept().getDeptName());
        List<RoleDTO> roles = BeanUtil.copyToList(user.getRoles(), RoleDTO.class);
        loginUser.setRoles(roles);
        return loginUser;
    }

    /**
     * 记录登录IP信息
     *
     * @param userId 用户ID
     */
    public void recordLoginIp(Long userId, String username) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ServletUtils.getClientIP());
        sysUser.setLoginDate(DateUtils.getNowDate());
        sysUser.setUpdateBy(username);
        userMapper.updateById(sysUser);
    }

    /**
     * 登录校验
     */
    private void checkLogin(LoginType loginType, String username, Long userId, String userType, Supplier<Boolean> supplier) {
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + username;
        String loginFail = Constants.LOGIN_FAIL;

        // 获取用户登录错误次数，默认为0 (可自定义限制策略 例如: key + username + ip)
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);
        // 锁定时间内登录 则踢出
        if (errorNumber >= maxRetryCount) {
            recordLogininfor(userId, userType, username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
            throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
        }

        if (supplier.get()) {
            // 错误次数递增
            errorNumber++;
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
            // 达到规定错误次数 则锁定登录
            if (errorNumber >= maxRetryCount) {
                recordLogininfor(userId, userType, username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
                throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
            } else {
                // 未达到规定错误次数
                recordLogininfor(userId, userType, username, loginFail, MessageUtils.message(loginType.getRetryLimitCount(), errorNumber));
                throw new UserException(loginType.getRetryLimitCount(), errorNumber);
            }
        }

        // 登录成功 清空错误次数
        RedisUtils.deleteObject(errorKey);
    }

    public String selectConfigByKey(String key) {
        return configService.selectConfigByKey(key);
    }
    
    /**
     * 为第三方登录用户创建默认身份标签
     * 
     * @param userId 用户ID
     */
    private void createDefaultIdentityTagForThirdParty(Long userId) {
        try {
            // 这里可以调用系统用户标签服务来创建默认标签
            // 由于SysLoginService没有直接依赖ISysUserTagService，我们使用简单的日志记录
            System.out.println("第三方登录用户 " + userId + " 需要手动创建默认身份标签");
        } catch (Exception e) {
            System.err.println("为第三方登录用户 " + userId + " 创建默认身份标签失败: " + e.getMessage());
        }
    }
}
