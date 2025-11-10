package com.sutran.sd.system.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.vo.NoticeCommonVo;
import com.sutran.sd.common.core.domain.vo.NoticeMpVo;
import com.sutran.sd.common.core.domain.vo.NoticeTotalVo;
import com.sutran.sd.common.core.domain.vo.NoticeVo;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.NoticeService;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.domain.SysNotice;
import com.sutran.sd.system.domain.SysUserNotifications;
import com.sutran.sd.system.mapper.SysNoticeMapper;
import com.sutran.sd.system.service.ISysNoticeService;
import com.sutran.sd.system.service.ISysUserNotificationsService;
import com.sutran.sd.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 公告 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysNoticeServiceImpl implements ISysNoticeService, NoticeService {

    private final SysNoticeMapper baseMapper;
    private final ISysUserNotificationsService sysUserNotificationsService;
    private final ISysUserService sysUserService;
    /** messageId的 SseEmitter对象映射集 **/
    private static final Map<Long, SseEmitter> SSE_EMITTER_MAP = new ConcurrentHashMap<>();

    /**
     * sse连接
     * @param userId    用户ID
     * @return          SseEmitter
     */
    @Override
    public SseEmitter connect(Long userId) {
        if (userId==null) {
            throw new ServiceException("当前用户未登录或登录已失效!");
        }
        SseEmitter sseEmitter = new SseEmitter(0L);
        // 连接成功需要返回数据，否则会出现待处理状态
        try {
            // 获取当前用户未读系统通知\公告条数
            NoticeTotalVo vo = getTotalVo(userId);
            // 推送消息
            sseEmitter.send(vo, MediaType.APPLICATION_JSON);
        }
        catch (IOException e) {
            log.error("[SSE连接异常]>>>>>>>>>原因：{}",e.getMessage());
        }
        // 连接断开
        sseEmitter.onCompletion(() -> SSE_EMITTER_MAP.remove(userId));
        // 连接超时
        sseEmitter.onTimeout(() -> {
            SSE_EMITTER_MAP.remove(userId);
            sseEmitter.complete();
        });
        // 连接报错
        sseEmitter.onError((throwable) -> SSE_EMITTER_MAP.remove(userId));
        SSE_EMITTER_MAP.put(userId, sseEmitter);
        return sseEmitter;
    }

    /**
     * 异步发送公共消息
     * @param commonVo        消息VO
     * @param userId    用户ID
     */
    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncSendCommonMsg(NoticeCommonVo commonVo, Long userId) {
        if (commonVo.getPublishTime()==null) {
            commonVo.setPublishTime(new Date());
        }
        SysUserNotifications notice = new SysUserNotifications();
        if (StringUtils.isBlank(commonVo.getId())) {
            notice.setId(IdUtil.getSnowflakeNextId());
        }
        else {
            notice.setId(Long.parseLong(commonVo.getId()));
        }
        // 存储数据
        notice.setUserId(userId).setMsgContent(commonVo.getContent()).setTitle(commonVo.getTitle()).setSendTime(commonVo.getPublishTime()).setSendStatus(1);
        commonVo.setId(notice.getId().toString());
        sysUserNotificationsService.insertNotice(notice);
        SseEmitter sseEmitter = SSE_EMITTER_MAP.get(userId);
        if (sseEmitter!=null) {
            try {
                NoticeVo vo = new NoticeVo().setId(commonVo.getId()).setNoticeType("DETAIL").setType("infos").setContent(commonVo.getContent()).setTitle(commonVo.getTitle()).setPublishTime(commonVo.getPublishTime());
                sseEmitter.send(vo, MediaType.APPLICATION_JSON);

                // 获取当前用户未读系统通知\公告条数
                NoticeTotalVo totalVo = getTotalVo(userId);
                // 推送消息
                sseEmitter.send(totalVo, MediaType.APPLICATION_JSON);
            }
            catch (IOException e) {
                log.error("[SSE发送消息异常]>>>>>>>>>原因：{}",e.getMessage());
            }
        }
    }

    /**
     * 异步发送微信公众号消息
     * @param mpVo        消息VO
     * @param userId    用户ID
     */
    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncSendMpMsg(NoticeMpVo mpVo, Long userId) {
        if (mpVo.getPublishTime()==null) {
            mpVo.setPublishTime(new Date());
        }
        SysUserNotifications notice = new SysUserNotifications();
        if (StringUtils.isBlank(mpVo.getId())) {
            notice.setId(IdUtil.getSnowflakeNextId());
        }
        else {
            notice.setId(Long.parseLong(mpVo.getId()));
        }
        // 获取openId
        String openId = sysUserService.selectOpenIdByUserId(userId);
        // 存储数据
        notice.setUserId(userId).setWxOpenId(openId).setMsgContent(mpVo.getContent()).setTitle(mpVo.getTitle()).setMpMsgContent(mpVo.getMpContent()).setSendTime(mpVo.getPublishTime()).setSendStatus(1);
        sysUserNotificationsService.insertNotice(notice);

        mpVo.setId(notice.getId().toString());
        SseEmitter sseEmitter = SSE_EMITTER_MAP.get(userId);
        if (sseEmitter!=null) {
            try {
                NoticeVo vo = new NoticeVo().setId(mpVo.getId()).setNoticeType("DETAIL").setType("infos").setContent(mpVo.getContent()).setTitle(mpVo.getTitle()).setPublishTime(mpVo.getPublishTime());
                sseEmitter.send(vo, MediaType.APPLICATION_JSON);

                // 获取当前用户未读系统通知\公告条数
                NoticeTotalVo totalVo = getTotalVo(userId);
                // 推送消息
                sseEmitter.send(totalVo, MediaType.APPLICATION_JSON);
            }
            catch (IOException e) {
                log.error("[SSE发送消息异常]>>>>>>>>>原因：{}",e.getMessage());
            }
        }
    }

    private NoticeTotalVo getTotalVo(Long userId) {
        NoticeTotalVo vo = new NoticeTotalVo().setNoticeType("TOTAL");
        // 获取当前用户未读系统通知\公告条数
        vo.setSysTotal(baseMapper.sysMsgTotalOfNotExpire());
        // 获取当前用户未读用户消息条数
        vo.setInfosTotal(sysUserNotificationsService.userMsgTotalOfNotRead(userId));
        // 计算总数
        vo.setTotal(vo.getSysTotal()+vo.getInfosTotal());
        return vo;
    }

    /**
     * 查询用户通知列表
     * @param userId    用户ID
     * @param num       查询数量
     * @return          用户最新消息
     */
    @Override
    public List<NoticeVo> selectMsgList(Long userId,int num) {
        if (userId==null) {
            throw new ServiceException("当前用户未登录或登录已失效!");
        }
        // 查询用户通知列表
        List<SysUserNotifications> list = sysUserNotificationsService.selectNoticeList(new LambdaQueryWrapper<SysUserNotifications>()
            .eq(SysUserNotifications::getUserId, userId)
            .orderByDesc(SysUserNotifications::getSendTime)
            .last("limit "+num));
        return list.stream().map(e-> new NoticeVo().setId(e.getId().toString()).setType("infos").setContent(e.getMsgContent()).setTitle(e.getTitle()).setPublishTime(e.getSendTime())).collect(Collectors.toList());
    }

    /**
     * 查询系统已发布且有效的通知列表
     * @param num       查询数量
     * @return          系统通知列表
     */
    @Override
    public List<NoticeVo> selectNoticeList(int num) {
        // 查询系统通知列表
        List<SysNotice> list = baseMapper.selectList(new LambdaQueryWrapper<SysNotice>()
            .orderByDesc(SysNotice::getPublishTime)
            .last("limit "+num));
        return list.stream().map(e-> new NoticeVo().setId(e.getNoticeId().toString()).setType("sys").setContent(e.getNoticeContent()).setTitle(e.getNoticeTitle()).setPublishTime(e.getPublishTime())).collect(Collectors.toList());
    }


    @Override
    public TableDataInfo<SysNotice> selectPageNoticeList(SysNotice notice, PageQuery pageQuery) {
        LambdaQueryWrapper<SysNotice> lqw = new LambdaQueryWrapper<SysNotice>()
            .like(StringUtils.isNotBlank(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
            .eq(StringUtils.isNotBlank(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
            .eq(StringUtils.isNotBlank(notice.getStatus()), SysNotice::getStatus, notice.getStatus())
            .gt(notice.getExpireTime()!=null, SysNotice::getExpireTime, notice.getExpireTime())
            .like(StringUtils.isNotBlank(notice.getCreateBy()), SysNotice::getCreateBy, notice.getCreateBy());
        Page<SysNotice> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNotice selectNoticeById(Long noticeId) {
        return baseMapper.selectById(noticeId);
    }

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysNotice>()
            .like(StringUtils.isNotBlank(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
            .eq(StringUtils.isNotBlank(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
            .eq(StringUtils.isNotBlank(notice.getStatus()), SysNotice::getStatus, notice.getStatus())
            .like(StringUtils.isNotBlank(notice.getCreateBy()), SysNotice::getCreateBy, notice.getCreateBy()));
    }

    /**
     * 新增公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    @Override
    public int insertNotice(SysNotice notice) {
        int insert = baseMapper.insert(notice);
        if (insert >= 1 && "1".equals(notice.getStatus())) {
            SSE_EMITTER_MAP.forEach((userId,sseEmitter)->{
                try {
                    NoticeVo vo = new NoticeVo().setId(notice.getNoticeId().toString()).setNoticeType("DETAIL").setType("sys").setContent(notice.getNoticeContent()).setTitle(notice.getNoticeTitle()).setPublishTime(notice.getPublishTime());
                    sseEmitter.send(vo, MediaType.APPLICATION_JSON);

                    // 获取当前用户未读系统通知\公告条数
                    NoticeTotalVo totalVo = getTotalVo(userId);
                    // 推送消息
                    sseEmitter.send(totalVo, MediaType.APPLICATION_JSON);
                }
                catch (IOException e) {
                    log.error("[SSE发送消息异常]>>>>>>>>>原因：{}",e.getMessage());
                }
            });
        }
        return insert;
    }

    /**
     * 修改公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    @Override
    public int updateNotice(SysNotice notice) {
        // 查询
        SysNotice oldNotice = baseMapper.selectById(notice.getNoticeId());
        if (oldNotice==null) {
            throw new ServiceException("公告不存在");
        }
        int update = baseMapper.updateById(notice);
        if (update >= 1 && "1".equals(notice.getStatus()) && !"1".equals(oldNotice.getStatus())) {
            SSE_EMITTER_MAP.forEach((userId,sseEmitter)->{
                try {
                    NoticeVo vo = new NoticeVo().setId(notice.getNoticeId().toString()).setNoticeType("DETAIL").setType("sys").setContent(notice.getNoticeContent()).setTitle(notice.getNoticeTitle()).setPublishTime(notice.getPublishTime());
                    sseEmitter.send(vo, MediaType.APPLICATION_JSON);

                    // 获取当前用户未读系统通知\公告条数
                    NoticeTotalVo totalVo = getTotalVo(userId);
                    // 推送消息
                    sseEmitter.send(totalVo, MediaType.APPLICATION_JSON);
                }
                catch (IOException e) {
                    log.error("[SSE发送消息异常]>>>>>>>>>原因：{}",e.getMessage());
                }
            });
        }
        return update;
    }

    /**
     * 删除公告对象
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeById(Long noticeId) {
        return baseMapper.deleteById(noticeId);
    }

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return baseMapper.deleteBatchIds(Arrays.asList(noticeIds));
    }

    /**
     * 标记已读
     * @param id        消息ID
     * @param userId    用户ID
     */
    @Override
    public void markRead(String id, Long userId) {
        sysUserNotificationsService.markRead(id,userId);
        SseEmitter sseEmitter = SSE_EMITTER_MAP.get(userId);
        if (sseEmitter!=null) {
            try {
                // 获取当前用户未读系统通知\公告条数
                NoticeTotalVo totalVo = getTotalVo(userId);
                // 推送消息
                sseEmitter.send(totalVo, MediaType.APPLICATION_JSON);
            }
            catch (IOException e) {
                log.error("[SSE发送消息异常]>>>>>>>>>原因：",e);
            }
        }
    }
}
