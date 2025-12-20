package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.entity.SysMenu;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.model.*;
import com.sutran.sd.common.core.domain.vo.NoticeCommonVo;
import com.sutran.sd.common.core.service.DictService;
import com.sutran.sd.common.core.service.NoticeService;
import com.sutran.sd.common.encrypt.EncryptContext;
import com.sutran.sd.common.enums.AlgorithmType;
import com.sutran.sd.common.enums.DeviceType;
import com.sutran.sd.common.enums.EncodeType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.framework.manager.EncryptorManager;
import com.sutran.sd.system.domain.vo.RouterVo;
import com.sutran.sd.system.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录验证
 * @author Lion Li
 */
@SuppressWarnings({"LoggingSimilarMessage", "AlibabaUndefineMagicConstant"})
@Validated
@RequiredArgsConstructor
@RestController
@Slf4j
public class SysLoginController {

    private final SysLoginService loginService;
    private final ISysMenuService menuService;
    private final ISysUserService userService;
    private final NoticeService noticeService;
    private final EncryptorManager encryptorManager;
    private final DictService dictService;
    private final SysRegisterService registerService;
    private final ISysConfigService configService;

    @Value("${third.login.publicKey}")
    private String thirdLoginPublicKey;
    @Value("${third.login.privateKey}")
    private String thirdLoginPrivateKey;

    /**
     * 后台-用户名密码登录
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Validated @RequestBody LoginBody loginBody) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(), loginBody.getUuid(), DeviceType.PC, ajax);
        return R.ok(ajax);
    }

    /**
     * 业务-用户名密码登录
     *
     * @param loginBody 业务-SD绘图 登录信息
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/bs-login")
    public R<Map<String, Object>> bsLogin(@Validated @RequestBody LoginBody loginBody) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(), loginBody.getUuid(), DeviceType.BS_PC, ajax);
        // 查询当前用户是否关注微信公众号
        Long userId = LoginHelper.getUserId();
        boolean flag = userService.isFollowWxMp(userId);
        if (!flag) {
            String name = loginService.selectConfigByKey("sys.wxPublic.name");
            name = StrUtil.isBlankIfStr(name)?"Zein AI":name;
            // 异步推送未关注微信公众号的消息
            NoticeCommonVo vo = new NoticeCommonVo()
                .setTitle("关注并绑定微信公众号").setPublishTime(new Date())
                .setContent(String.format("系统检测到您当前还未关注并绑定 %s 微信公众号,如需接收微信公众号消息请关注并绑定 %s 微信公众号!",name,name));
            noticeService.asyncSendCommonMsg(vo,userId);
        }
        return R.ok(ajax);
    }

    /**
     * 业务-第三方预登录
     *
     * @param data 加密数据
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/pre-login")
    public R<Map<String, Object>> preLogin(@RequestBody ThirdLogin data) {
        EncryptContext encryptContext = new EncryptContext();
        encryptContext.setAlgorithm(AlgorithmType.RSA);
        encryptContext.setEncode(EncodeType.BASE64);
        encryptContext.setPublicKey(thirdLoginPublicKey);
        encryptContext.setPrivateKey(thirdLoginPrivateKey);
        // 手机号、第三方ID、是否会员、渠道来源字典ID
        String phone,thirdUserId,isMember,sysUserChannelId;
        String result = null;
        try{
            result = this.encryptorManager.decrypt(data.getData(), encryptContext);
            String[] split = result.split("&");
            phone = split[0];
            thirdUserId = split[1];
            isMember = split[2];
            sysUserChannelId = split[3];
            if (split.length!=4 || StrUtil.isBlankIfStr(phone) || StrUtil.isBlankIfStr(thirdUserId) || StrUtil.isBlankIfStr(isMember) || StrUtil.isBlankIfStr(sysUserChannelId)) {
                log.error("[第三方登录校验]>>>>>>>>>解密数据:[{}]",result);
                throw new ServiceException("数据解析异常,登录失败!");
            }
            if (!PhoneUtil.isMobile(phone)) {
                log.error("[第三方登录校验]>>>>>>>>>解密数据:[{}]",result);
                throw new ServiceException("非法手机号,登录失败!");
            }
        }
        catch (Exception e) {
            log.error("[第三方登录校验]>>>>>>>>>解密数据:[{}]",result);
            throw new ServiceException("数据解析异常,登录失败!");
        }

        if (!"1".equals(isMember)) {
            throw new ServiceException("非会员用户,登录失败!");
        }

        String userChannel = dictService.getDictLabel("sys_user_channel",sysUserChannelId);
        if (StrUtil.isBlankIfStr(userChannel)) {
            throw new ServiceException("当前渠道用户不支持,登录失败!");
        }

        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.registerOrLogin(phone, thirdUserId, userChannel, sysUserChannelId, DeviceType.BS_PC, ajax);

        // 查询当前用户是否关注微信公众号
        Long userId = LoginHelper.getUserId();
        boolean flag = userService.isFollowWxMp(userId);
        if (!flag) {
            String name = loginService.selectConfigByKey("sys.wxPublic.name");
            name = StrUtil.isBlankIfStr(name)?"Zein AI":name;
            // 异步推送未关注微信公众号的消息
            NoticeCommonVo vo = new NoticeCommonVo()
                .setTitle("关注并绑定微信公众号").setPublishTime(new Date())
                .setContent(String.format("系统检测到您当前还未关注并绑定 %s 微信公众号,如需接收微信公众号消息请关注并绑定 %s 微信公众号!",name,name));
            noticeService.asyncSendCommonMsg(vo,userId);
        }
        return R.ok(ajax);
    }

    /**
     * 后台-短信登录
     *
     * @param smsLoginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/sms-login")
    public R<Map<String, Object>> smsLogin(@Validated @RequestBody SmsLoginBody smsLoginBody) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.smsLogin(smsLoginBody.getPhonenumber(), smsLoginBody.getSmsCode(), DeviceType.PC, ajax);
        return R.ok(ajax);
    }

    /**
     * 业务-短信登录
     *
     * @param smsLoginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/bs-sms-login")
    public R<Map<String, Object>> bsSmsLogin(@Validated @RequestBody SmsLoginBody smsLoginBody) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.smsLogin(smsLoginBody.getPhonenumber(), smsLoginBody.getSmsCode(), DeviceType.BS_PC, ajax);
        return R.ok(ajax);
    }

    /**
     * 后台-邮件登录
     *
     * @param body 登录信息
     * @return 结果
     */
    @PostMapping("/email-login")
    public R<Map<String, Object>> emailLogin(@Validated @RequestBody EmailLoginBody body) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.emailLogin(body.getEmail(), body.getEmailCode(), DeviceType.PC, ajax);
        return R.ok(ajax);
    }

    /**
     * 业务-邮件登录
     *
     * @param body 登录信息
     * @return 结果
     */
    @PostMapping("/bs-email-login")
    public R<Map<String, Object>> bsEmailLogin(@Validated @RequestBody EmailLoginBody body) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.emailLogin(body.getEmail(), body.getEmailCode(), DeviceType.BS_PC, ajax);
        return R.ok(ajax);
    }

    /**
     * 后台-小程序登录(示例)
     *
     * @param xcxCode 小程序code
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/xcx-login")
    public R<Map<String, Object>> xcxLogin(@NotBlank(message = "{xcx.code.not.blank}") String xcxCode) {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.xcxLogin(xcxCode,ajax);
        return R.ok(ajax);
    }

    /**
     * 业务-微信公众号登录
     *
     * @param code 微信公众号code
     * @return 结果
     */
    @SaIgnore
    @PostMapping("/wx-mp-login")
    public R<Map<String, Object>> bsWxMpLogin(@NotBlank(message = "{xcx.code.not.blank}") @RequestParam String code) throws WxErrorException {
        Map<String, Object> ajax = new HashMap<>(4);
        // 生成令牌
        loginService.wxMpLogin(code, DeviceType.BS_PC,ajax);
        return R.ok(ajax);
    }

    /**
     * 业务-获取微信公众号的jsapi_ticket
     * @param url 当前页面url
     * @return ticket
     */
    @SaIgnore
    @GetMapping("/mp/js-ticket")
    public R<WxJsapiSignature> getSignature(@NotBlank(message = "{xcx.code.not.blank}") @RequestParam String url) throws WxErrorException {
        WxJsapiSignature signature = loginService.getSignature(url);
        return R.ok("获取成功",signature);
    }

    /**
     * 业务-绑定微信公众号
     *
     * @param code 微信公众号code
     * @return 结果
     */
    @PostMapping("/wx-mp-bind")
    public R<Boolean> wxMpBind(@NotBlank(message = "{xcx.code.not.blank}") @RequestParam String code) throws WxErrorException {
        loginService.wxMpBind(code);
        return R.ok(true);
    }

    /**
     * 业务-绑定微信公众号
     *
     * @return 结果
     */
    @PostMapping("/wx-mp-unbind")
    public R<Boolean> wxMpUnBind() {
        loginService.wxMpUnBind();
        return R.ok(true);
    }

    /**
     * 退出登录
     */
    @SaIgnore
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        assert loginUser != null;
        SysUser user = userService.selectUserById(loginUser.getUserId());
        Map<String, Object> ajax = new HashMap<>(4);
        ajax.put("user", user);
        ajax.put("roles", loginUser.getRolePermission());
        ajax.put("permissions", loginUser.getMenuPermission());
        return R.ok(ajax);
    }

    /**
     * 关闭引导(1-关闭 0-开启)
     */
    @GetMapping("close-guide")
    public R<Void> closeGuide(@RequestParam(required = false) Integer isCloserGuide) {
        isCloserGuide = isCloserGuide ==null?1: isCloserGuide;
        userService.closeGuide(LoginHelper.getUserId(), isCloserGuide);
        return R.ok();
    }

    /**
     * 获取路由信息
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public R<List<RouterVo>> getRouters() {
        Long userId = LoginHelper.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return R.ok(menuService.buildMenus(menus));
    }

    /**
     * 用户注册
     */
    @SaIgnore
    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return R.fail("当前系统没有开启注册功能！");
        }
        registerService.register(user);
        return R.ok();
    }
}
