package com.sutran.sd.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.constant.CacheNames;
import com.sutran.sd.common.constant.UserConstants;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.domain.entity.SysDept;
import com.sutran.sd.common.core.domain.entity.SysRole;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.exception.TaskErrorException;
import com.sutran.sd.common.helper.DataBaseHelper;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StreamUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.domain.SysPost;
import com.sutran.sd.common.core.domain.entity.SysUserMember;
import com.sutran.sd.system.domain.SysUserPost;
import com.sutran.sd.system.domain.SysUserRole;
import com.sutran.sd.system.domain.bo.SysUserMemberBo;
import com.sutran.sd.system.mapper.*;
import com.sutran.sd.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService, UserService {

    private final SysUserMapper baseMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysUserMemberMapper userMemberMapper;

    @Override
    public TableDataInfo<SysUser> selectPageUserList(SysUser user, PageQuery pageQuery) {
        Page<SysUser> page = baseMapper.selectPageUserList(pageQuery.build(), this.buildQueryWrapper(user));
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<Long> userIds = page.getRecords().stream().map(SysUser::getUserId).collect(Collectors.toList());
            Map<Long, SysUserMember> memberMap = userMemberMapper.selectMemberInfoByUserIds(userIds, new Date());
            page.getRecords().forEach(item -> item.setMember(memberMap.get(item.getUserId())));
        }
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectUserList(SysUser user) {
        return baseMapper.selectUserList(this.buildQueryWrapper(user));
    }

    private Wrapper<SysUser> buildQueryWrapper(SysUser user) {
        Map<String, Object> params = user.getParams();
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", user.getDelFlag()!=null&&UserConstants.USER_DELETE.equals(user.getDelFlag())?UserConstants.USER_DELETE:UserConstants.USER_NORMAL)
            .eq(ObjectUtil.isNotNull(user.getUserId()), "u.user_id", user.getUserId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .and(StringUtils.isNotBlank(user.getUserType()), w->w.eq("u.user_type", user.getUserType()))
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .eq(StringUtils.isNotBlank(user.getChannelId()), "u.channel_id", user.getChannelId())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "u.create_time", params.get("beginTime"), params.get("endTime"))
            .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                List<SysDept> deptList = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                    .select(SysDept::getDeptId)
                    .apply(DataBaseHelper.findInSet(user.getDeptId(), "ancestors")));
                List<Long> ids = StreamUtils.toList(deptList, SysDept::getDeptId);
                ids.add(user.getDeptId());
                w.in("u.dept_id", ids);
            });
        return wrapper;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public TableDataInfo<SysUser> selectAllocatedList(SysUser user, PageQuery pageQuery) {
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.USER_NORMAL)
            .eq(ObjectUtil.isNotNull(user.getRoleId()), "r.role_id", user.getRoleId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber());
        Page<SysUser> page = baseMapper.selectAllocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public TableDataInfo<SysUser> selectUnallocatedList(SysUser user, PageQuery pageQuery) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.USER_NORMAL)
            .and(w -> w.ne("r.role_id", user.getRoleId()).or().isNull("r.role_id"))
            .notIn(CollUtil.isNotEmpty(userIds), "u.user_id", userIds)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber());
        Page<SysUser> page = baseMapper.selectUnallocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
        return baseMapper.selectUserByUserName(userName);
    }

    /**
     * 通过手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByPhoneNumber(String phoneNumber) {
        return baseMapper.selectUserByPhonenumber(phoneNumber);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return baseMapper.selectUserById(userId);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRole::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName) {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysPost::getPostName);
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, user.getUserName())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     */
    @Override
    public boolean checkPhoneUnique(SysUser user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhonenumber, user.getPhonenumber())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     */
    @Override
    public boolean checkEmailUnique(SysUser user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, user.getEmail())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user) {
        if (ObjectUtil.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (!LoginHelper.isAdmin()) {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = this.selectUserList(user);
            if (CollUtil.isEmpty(users)) {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUser user) {
        // 新增用户信息
        if (StrUtil.isEmptyIfStr(user.getEmail())){
            user.setEmail(null);
        }
        if (StrUtil.isEmptyIfStr(user.getPhonenumber())){
            user.setPhonenumber(null);
        }
        if (StrUtil.isEmptyIfStr(user.getAvatar())){
            user.setAvatar(null);
        }
        int rows = baseMapper.insert(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user) {
        user.setCreateBy(user.getUserName());
        user.setUpdateBy(user.getUserName());
        return baseMapper.insert(user) > 0;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUser user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        // 新增用户与岗位管理
        insertUserPost(user);
        return baseMapper.updateById(user);
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, userId));
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user) {
        return baseMapper.updateById(user);
    }

    /**
     *
     * @param userId    用户ID
     * @param delFlag   删除标志（0代表存在 2代表删除）
     * @return 结果
     */
    @Override
    public int recoverDel(Long userId, String delFlag) {
        return baseMapper.recoverDel(userId,delFlag);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user) {
        return baseMapper.updateById(user);
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getAvatar, avatar)
                .eq(SysUser::getUserName, userName)) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user) {
        return baseMapper.updateById(user);
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, password)
                .eq(SysUser::getUserName, userName));
    }

    @Override
    public int forgetPwd(String phone, String password) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, password)
                .eq(SysUser::getPhonenumber, phone));
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserPost(SysUser user) {
        Long[] posts = user.getPostIds();
        if (ArrayUtil.isNotEmpty(posts)) {
            // 新增用户与岗位管理
            List<SysUserPost> list = StreamUtils.toList(Arrays.asList(posts), postId -> {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                return up;
            });
            userPostMapper.insertBatch(list);
        }
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds) {
        if (ArrayUtil.isNotEmpty(roleIds)) {
            // 新增用户与角色管理
            List<SysUserRole> list = StreamUtils.toList(Arrays.asList(roleIds), roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            });
            userRoleMapper.insertBatch(list);
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 删除用户与岗位表
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        return baseMapper.deleteById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        List<Long> ids = Arrays.asList(userIds);
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        // 删除用户与岗位表
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, ids));
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public boolean isFollowWxMp(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        return sysUser != null && StrUtil.isNotBlank(sysUser.getWxOpenId());
    }

    @Override
    public void closeGuide(Long userId, Integer isCloserGuide) {
        baseMapper.closeGuide(userId,isCloserGuide);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertAuthMember(SysUserMemberBo bo, PayMember payMember, Date now) {
        if (CollUtil.isEmpty(bo.getUserIds()) || ObjectUtil.isNull(bo.getMemberId())) {
            return;
        }
        // 会员时长(天)[不填则已会员设置的时长为准]
        Integer duration = bo.getDuration();
        if (ObjectUtil.isNull(duration)) {
            duration = payMember.getDuration();
        }
        // 会员训练次数[不填则已会员设置的训练次数为准]
        Integer limitTrainTimes = bo.getLimitTrainTimes();
        if (ObjectUtil.isNull(limitTrainTimes)) {
            limitTrainTimes = payMember.getLimitTrainTimes();
        }
        // 会员绘图次数[不填则已会员设置的绘图次数为准]
        Integer limitDrawNum = bo.getLimitDrawNum();
        if (ObjectUtil.isNull(limitDrawNum)) {
            limitDrawNum = payMember.getLimitDrawNum();
        }

        for (Long userId : bo.getUserIds()) {
            // 获取已有生效中的会员用户
            String memberId = userMemberMapper.selectMemberIdByUserId(userId, now);
            if (StrUtil.isNotBlank(memberId)) {
                log.error("[后台][授权会员]>>>>>>>>>用户[{}]已存在生效中的会员[{}]", userId,memberId);
                continue;
            }
            // 新增用户会员
            SysUserMember sysUserMember = new SysUserMember()
                .setUserId(userId)
                .setMemberId(bo.getMemberId())
                .setLevelName(payMember.getLevelName())
                .setStartTime(now)
                .setDuration(duration)
                .setEndTime(DateUtil.offsetDay(now, duration))
                .setStatus(1)
                .setLimitTrainTimes(limitTrainTimes)
                .setLimitDrawNum(limitDrawNum)
                .setRemark("后台授权会员")
                .setCreateTime(now);
            userMemberMapper.insert(sysUserMember);
        }
    }

    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    @Override
    public String selectUserNameById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserName).eq(SysUser::getUserId, userId));
        return ObjectUtil.isNull(sysUser) ? null : sysUser.getUserName();
    }

    @Override
    public String selectOpenIdById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getWxOpenId).eq(SysUser::getUserId, userId));
        return ObjectUtil.isNull(sysUser) ? null : sysUser.getWxOpenId();
    }

    @Override
    public Integer selectTrainTimesById(Long userId) {
//        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
//            .select(SysUser::getLimitTrainTimes).eq(SysUser::getUserId, userId));
//        return ObjectUtil.isNull(sysUser) ? null : sysUser.getLimitTrainTimes();
        return userMemberMapper.selectTrainTimesById(userId,new Date());
    }

    @Override
    public Integer selectDrawNumById(Long userId) {
//        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
//            .select(SysUser::getLimitDrawNum).eq(SysUser::getUserId, userId));
//        return ObjectUtil.isNull(sysUser) ? null : sysUser.getLimitDrawNum();
        return userMemberMapper.selectDrawNumById(userId,new Date());
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void deductedTrainTimes(Long userId) {
//        baseMapper.deductedTrainTimes(userId);
        userMemberMapper.deductedTrainTimes(userId,new Date());
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void returnedTrainTimes(Long userId) {
        if (userId == null) {
            return;
        }
//        baseMapper.returnedTrainTimes(userId);
        userMemberMapper.returnedTrainTimes(userId,new Date());
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void deductedDrawNum(Long userId, int num) {
//        baseMapper.deductedDrawNum(userId,num);
        userMemberMapper.deductedDrawNum(userId,num,new Date());
    }

    @Override
    public String selectChannelUserIdById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getChannelUserId).eq(SysUser::getUserId, userId));
        return ObjectUtil.isNull(sysUser) ? null : sysUser.getChannelUserId();
    }

    @Override
    public String selectUserIdByPhone(String phone) {
        return baseMapper.selectUserIdByPhone(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertMember(Long userId, Long businessId, Date startTime, PayMember payMember, String outTradeNo) {
        // 先查询用户是否已购买且未失效的会员
        SysUserMember sysUserMember = userMemberMapper.selectOne(new LambdaQueryWrapper<SysUserMember>().eq(SysUserMember::getUserId, userId).eq(SysUserMember::getStatus, 1).ge(SysUserMember::getEndTime,startTime).orderByDesc(SysUserMember::getId).last("limit 1"));
        // 存在该会员，设置失效，新增叠加
        if (sysUserMember!=null) {
            if (outTradeNo.equals(sysUserMember.getOutTradeNo())) {
                return;
            }
            final int limitTrainTimes = sysUserMember.getLimitTrainTimes();
            final int limitDrawNum = sysUserMember.getLimitDrawNum();

            // 新的会员和之前的会员相同，则备注为“购买相同会员叠加次数,当前会员失效”
            if (Objects.equals(payMember.getId(), sysUserMember.getMemberId())){
                sysUserMember.setRemark("购买相同会员叠加次数");
            }
            // 新的会员和之前的会员不同(因为设置问题，只能是升级会员)，则备注为“升级会员叠加次数,当前会员失效”
            else {
                sysUserMember.setRemark("升级会员叠加次数");
            }
            sysUserMember.setStatus(0);
            userMemberMapper.updateStatusById(sysUserMember);

            // 新增叠加新会员
            SysUserMember insert = new SysUserMember()
                .setUserId(userId).setMemberId(businessId).setLevelName(payMember.getLevelName())
                .setStartTime(startTime).setEndTime(DateUtil.offsetDay(startTime,payMember.getDuration()))
                .setDuration(payMember.getDuration()).setStatus(1).setOutTradeNo(outTradeNo)
                .setOldLimitTrainTimes(limitTrainTimes)
                .setOldLimitDrawNum(limitDrawNum)
                .setLimitTrainTimes(payMember.getLimitTrainTimes() + limitTrainTimes)
                .setLimitDrawNum(payMember.getLimitDrawNum()+ limitDrawNum)
                .setUseTrainTimes(sysUserMember.getUseTrainTimes())
                .setUseDrawNum(sysUserMember.getUseDrawNum())
                .setCreateTime(startTime);
            userMemberMapper.insert(insert);
        }
        // 不存在，新增
        else {
            sysUserMember = new SysUserMember()
                .setUserId(userId).setMemberId(businessId).setLevelName(payMember.getLevelName())
                .setStartTime(startTime).setEndTime(DateUtil.offsetDay(startTime,payMember.getDuration()))
                .setDuration(payMember.getDuration()).setStatus(1).setOutTradeNo(outTradeNo)
                .setLimitTrainTimes(payMember.getLimitTrainTimes())
                .setLimitDrawNum(payMember.getLimitDrawNum())
                .setCreateTime(startTime);
            userMemberMapper.insert(sysUserMember);
        }
    }

    @Override
    public String selectMemberIdByUserId(Long userId, Date now) {
        return userMemberMapper.selectMemberIdByUserId(userId,now);
    }

    @Override
    public void checkDrawNumOfMember(Long userId, Integer drawNum) {
        final Integer drawNumOfMember = userMemberMapper.selectDrawNumById(userId,new Date());
        if (drawNumOfMember==null || drawNumOfMember<drawNum) {
            throw new TaskErrorException("会员绘图数量不足");
        }
    }

}
