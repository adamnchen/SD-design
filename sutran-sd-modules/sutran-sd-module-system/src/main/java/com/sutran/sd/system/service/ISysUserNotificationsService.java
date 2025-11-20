package com.sutran.sd.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.system.domain.SysUserNotifications;

import java.util.List;

/**
 * 用户通知 服务层
 *
 * @author zj
 */
public interface ISysUserNotificationsService {

    /**
     * 查询用户通知分页列表
     *
     * @param notice 公告信息
     * @param pageQuery 分页查询对象
     * @return 用户通知分页列表
     */
    TableDataInfo<SysUserNotifications> selectPageNoticeList(SysUserNotifications notice, PageQuery pageQuery);

    /**
     * 查询用户通知信息
     *
     * @param noticeId 用户通知ID
     * @return 用户通知信息
     */
    SysUserNotifications selectNoticeById(Long noticeId);

    /**
     * 查询用户通知列表
     *
     * @param notice 用户通知信息
     * @return 用户通知集合
     */
    List<SysUserNotifications> selectNoticeList(SysUserNotifications notice);

    /**
     * 查询用户通知列表
     *
     * @param lqw Lambda查询包装器
     * @return 用户通知集合
     */
    List<SysUserNotifications> selectNoticeList(LambdaQueryWrapper<SysUserNotifications> lqw);

    /**
     * 新增用户通知
     *
     * @param notice 用户通知信息
     * @return 结果
     */
    int insertNotice(SysUserNotifications notice);

    /**
     * 修改用户通知
     *
     * @param notice 公告信息
     * @return 结果
     */
    int updateNotice(SysUserNotifications notice);

    /**
     * 删除用户通知信息
     *
     * @param noticeId 用户通知ID
     * @return 结果
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批量删除用户通知信息
     *
     * @param noticeIds 需要删除的用户通知ID
     * @return 结果
     */
    int deleteNoticeByIds(Long[] noticeIds);

    /**
     * 获取用户未读通知\公告总数
     * @param userId    用户ID
     * @return          用户未读通知\公告总数
     */
    long userMsgTotalOfNotRead(Long userId);

    /**
     * 标记用户通知\公告为已读
     * @param id        用户通知\公告ID
     * @param userId    用户ID
     */
    void markRead(String id, Long userId);

    /**
     * 查询用户通知\公告是否已存在
     * @param queryWrapper 查询包装器
     * @return             用户通知\公告是否已存在
     */
    long selectCount(LambdaQueryWrapper<SysUserNotifications> queryWrapper);

    /**
     * 批量标记用户通知\公告为已读
     * @param ids       用户通知\公告ID列表
     * @param userId    用户ID
     */
    void markReadBatch(List<String> ids, Long userId);
}
