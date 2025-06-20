package com.sutran.sd.sdapi.controller;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.sdapi.modules.system.entity.SdUserMsg;
import com.sutran.sd.sdapi.modules.system.SdUserMsgService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * SD-业务端消息
 * @author zj
 * @date 2024-05-28
 */
@RestController
@RequestMapping("/sd/msg")
@RequiredArgsConstructor
public class SdMsgController {

    private final SdUserMsgService sdUserMsgService;

    /**
     * 用户消息-sse消息推送连接
     */
    @GetMapping(value = "/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        return sdUserMsgService.connect(LoginHelper.getUserId());
    }

    /**
     * 用户消息-获取用户最近未读的5条消息(isRead[0-未读,1-已读])
     */
    @GetMapping("/user-latest")
    public R<List<SdUserMsg>> userMsgLatest() {
        return R.ok(sdUserMsgService.userMsgLatest(LoginHelper.getUserId()));
    }

    /**
     * 用户消息-获取用户消息列表(isRead[0-未读,1-已读])
     */
    @GetMapping("/user-list")
    public TableDataInfo<SdUserMsg> userMsgList(@RequestParam(required = false) Integer isRead,
                                                @RequestParam(required = false) Integer pageNum,
                                                @RequestParam(required = false) Integer pageSize) {
        PageQuery pageQuery = null;
        if (pageNum != null && pageSize != null) {
            pageQuery = new PageQuery();
            pageQuery.setPageNum(pageNum);
            pageQuery.setPageSize(pageSize);
        }
        return sdUserMsgService.userMsgList(LoginHelper.getUserId(),isRead,pageQuery);
    }

    /**
     * 用户消息-修改消息为已读
     */
    @GetMapping("/user-read")
    public R<Boolean> readUserMsg(@RequestParam String id) {
        sdUserMsgService.readUserMsg(id);
        return R.ok();
    }

}
