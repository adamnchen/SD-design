package com.sutran.sd.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.domain.SysUserNotifications;
import com.sutran.sd.system.mapper.SysUserNotificationsMapper;
import com.sutran.sd.system.service.ISysUserNotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 用户通知 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysUserNotificationsServiceImpl implements ISysUserNotificationsService {

    private final SysUserNotificationsMapper baseMapper;

    /**
     * 查询用户通知分页列表
     *
     * @param notice 用户通知信息
     * @param pageQuery 分页查询参数
     * @return 用户通知分页列表
     */
    @Override
    public TableDataInfo<SysUserNotifications> selectPageNoticeList(SysUserNotifications notice, PageQuery pageQuery) {
        LambdaQueryWrapper<SysUserNotifications> lqw = new LambdaQueryWrapper<SysUserNotifications>()
            .eq(StringUtils.isNotBlank(notice.getNotificationType()), SysUserNotifications::getNotificationType, notice.getNotificationType())
            .like(StringUtils.isNotBlank(notice.getTitle()), SysUserNotifications::getTitle, notice.getTitle())
            .eq(notice.getUserId()!=null, SysUserNotifications::getUserId, notice.getUserId())
            .eq(notice.getReadStatus()!=null, SysUserNotifications::getReadStatus, notice.getReadStatus());
        Page<SysUserNotifications> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 查询用户通知信息
     *
     * @param noticeId 用户通知ID
     * @return 用户通知信息
     */
    @Override
    public SysUserNotifications selectNoticeById(Long noticeId) {
        return baseMapper.selectById(noticeId);
    }

    /**
     * 查询用户通知列表
     *
     * @param notice 用户通知信息
     * @return 用户通知集合
     */
    @Override
    public List<SysUserNotifications> selectNoticeList(SysUserNotifications notice) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysUserNotifications>()
            .like(StringUtils.isNotBlank(notice.getTitle()), SysUserNotifications::getTitle, notice.getTitle())
            .eq(notice.getUserId()!=null, SysUserNotifications::getUserId, notice.getUserId())
            .eq(notice.getReadStatus()!=null, SysUserNotifications::getReadStatus, notice.getReadStatus()));
    }

    /**
     * 查询用户通知列表
     *
     * @param lqw Lambda查询包装器
     * @return 用户通知集合
     */
    @Override
    public List<SysUserNotifications> selectNoticeList(LambdaQueryWrapper<SysUserNotifications> lqw) {
        return baseMapper.selectList(lqw);
    }

    /**
     * 新增用户通知
     *
     * @param notice 用户通知信息
     * @return 结果
     */
    @Override
    public int insertNotice(SysUserNotifications notice) {
        return baseMapper.insert(notice);
    }

    /**
     * 修改用户通知
     *
     * @param notice 用户通知信息
     * @return 结果
     */
    @Override
    public int updateNotice(SysUserNotifications notice) {
        return baseMapper.updateById(notice);
    }

    /**
     * 删除用户通知对象
     *
     * @param noticeId 用户通知ID
     * @return 结果
     */
    @Override
    public int deleteNoticeById(Long noticeId) {
        return baseMapper.deleteById(noticeId);
    }

    /**
     * 批量删除用户通知信息
     *
     * @param noticeIds 需要删除的用户通知ID
     * @return 结果
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return baseMapper.deleteBatchIds(Arrays.asList(noticeIds));
    }

    /**
     * 获取用户未读通知总数
     * @param userId    用户ID
     * @return          用户未读通知总数
     */
    @Override
    public long userMsgTotalOfNotRead(Long userId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysUserNotifications>()
            .eq(SysUserNotifications::getUserId, userId)
            .eq(SysUserNotifications::getReadStatus, 0));
    }

    /**
     * 标记用户通知\公告为已读
     * @param id        用户通知\公告ID
     * @param userId    用户ID
     */
    @Override
    public void markRead(String id, Long userId) {
        SysUserNotifications notice = baseMapper.selectById(id);
        if (notice==null) {
            throw new ServiceException("用户通知不存在");
        }
        if (!userId.equals(notice.getUserId())) {
            throw new ServiceException("用户通知不属于该用户");
        }
        notice.setReadStatus(1).setReadTime(new Date());
        baseMapper.updateById(notice);
    }

    @Override
    public long selectCount(LambdaQueryWrapper<SysUserNotifications> queryWrapper) {
        Long count = baseMapper.selectCount(queryWrapper);
        return count==null?0:count;
    }

    @Override
    public void markReadBatch(List<String> ids, Long userId) {
        List<SysUserNotifications> noticeList = baseMapper.selectList(new LambdaQueryWrapper<SysUserNotifications>()
            .in(SysUserNotifications::getId, ids).eq(SysUserNotifications::getUserId, userId));
        if (CollectionUtil.isEmpty(noticeList)) {
            return;
        }
        noticeList.forEach(notice -> notice.setReadStatus(1).setReadTime(new Date()));
        baseMapper.updateBatchById(noticeList);
    }
}
