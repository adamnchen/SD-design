package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.system.domain.SysNotice;

import java.util.List;

/**
 * 公告 服务层
 *
 * @author Lion Li
 */
public interface ISysNoticeService {

    /**
     * 查询公告分页列表
     *
     * @param notice 公告信息
     * @param pageQuery 分页查询对象
     * @return 公告分页列表
     */
    TableDataInfo<SysNotice> selectPageNoticeList(SysNotice notice, PageQuery pageQuery);

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    int insertNotice(SysNotice notice);

    /**
     * 修改公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    int updateNotice(SysNotice notice);

    /**
     * 删除公告信息
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    int deleteNoticeByIds(Long[] noticeIds);

    /**
     * 标记已读
     * @param id        消息ID
     * @param userId    用户ID
     */
    void markRead(String id, Long userId);

    /**
     * 批量标记已读
     * @param ids       消息ID列表
     * @param userId    用户ID
     */
    void markReadBatch(List<String> ids, Long userId);
}
