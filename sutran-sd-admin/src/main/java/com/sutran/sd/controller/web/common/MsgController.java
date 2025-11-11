package com.sutran.sd.controller.web.common;

import cn.hutool.core.collection.CollectionUtil;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.vo.NoticeVo;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.NoticeService;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.domain.SysNotice;
import com.sutran.sd.system.domain.SysUserNotifications;
import com.sutran.sd.system.service.ISysNoticeService;
import com.sutran.sd.system.service.ISysUserNotificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 公告通知消息推送
 * @author zj
 * @date 2024-05-28
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class MsgController {

    private final NoticeService noticeService;
    private final ISysUserNotificationsService sysUserNotificationsService;
    private final ISysNoticeService sysNoticeService;

    /**
     * 用户消息-sse消息推送连接
     */
    @GetMapping(value = "/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        return noticeService.connect(LoginHelper.getUserId());
    }

    /**
     * 查询最近5条通知消息列表
     * @param scope     查询范围[sys-系统通知\公告，infos-用户消息]
     */
    @GetMapping("/latest-info")
    public R<List<NoticeVo>> infos(@RequestParam(required = false) String scope) {
        if (StringUtils.isBlank(scope)) {
            List<NoticeVo> noticeVos = noticeService.selectNoticeList(1);
            List<NoticeVo> msgVos;
            if (CollectionUtil.isEmpty(noticeVos)) {
                msgVos = noticeService.selectMsgList(LoginHelper.getUserId(), 5);
            }
            else {
                msgVos = noticeService.selectMsgList(LoginHelper.getUserId(), 4);
            }
            msgVos.addAll(noticeVos);
            return R.ok(msgVos);
        }
        if ("sys".equals(scope)) {
            return R.ok(noticeService.selectNoticeList(5));
        }
        return R.ok(noticeService.selectMsgList(LoginHelper.getUserId(),5));
    }

    /**
     * 分页查询通知消息列表
     * @param pageSize  每页数量
     * @param pageNum   当前页码
     */
    @GetMapping("/infos")
    public TableDataInfo<SysUserNotifications> pageInfos(@RequestParam int pageSize, @RequestParam int pageNum) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);
        pageQuery.setOrderByColumn("send_time");
        pageQuery.setIsAsc("desc");
        SysUserNotifications notice = new SysUserNotifications().setUserId(LoginHelper.getUserId());
        return sysUserNotificationsService.selectPageNoticeList(notice,pageQuery);
    }

    /**
     * 分页查询通知消息列表
     * @param pageSize  每页数量
     * @param pageNum   当前页码
     */
    @GetMapping("/sys")
    public TableDataInfo<SysNotice> pageSys(@RequestParam int pageSize, @RequestParam int pageNum) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);
        pageQuery.setOrderByColumn("create_time");
        pageQuery.setIsAsc("desc");
        SysNotice notice = new SysNotice().setStatus("1").setExpireTime(new Date());
        return sysNoticeService.selectPageNoticeList(notice,pageQuery);
    }

    /**
     * 标记已读
     * @param id        消息ID
     */
    @GetMapping("/read")
    public R<Void> read(@RequestParam String id) {
        sysNoticeService.markRead(id,LoginHelper.getUserId());
        return R.ok();
    }
}
