package com.sutran.sd.sdapi.modules.system;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.modules.system.entity.SdUserMsg;
import com.sutran.sd.sdapi.modules.system.vo.MsgVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
public interface SdUserMsgService {

    SseEmitter connect(Long userId);

    void asyncSendMessage(MsgVo vo,Long userId);

    List<SdUserMsg> userMsgLatest(Long userId);

    TableDataInfo<SdUserMsg> userMsgList(Long userId, Integer isRead, PageQuery pageQuery);

    void readUserMsg(String id);

    void insertUserMsg(SdUserMsg sdUserMsg);
}
