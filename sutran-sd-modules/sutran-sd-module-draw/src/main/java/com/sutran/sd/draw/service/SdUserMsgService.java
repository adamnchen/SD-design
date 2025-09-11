package com.sutran.sd.draw.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.SdUserMsg;
import com.sutran.sd.draw.domain.vo.MsgVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
public interface SdUserMsgService {
    /**
     * sse连接
     * @param userId    用户ID
     * @return          SseEmitter
     */
    SseEmitter connect(Long userId);

    /**
     * 异步发送消息
     * @param vo        消息VO
     * @param userId    用户ID
     */
    void asyncSendMessage(MsgVo vo,Long userId);

    /**
     * 查询用户最新消息
     * @param userId    用户ID
     * @return          用户最新消息
     */
    List<SdUserMsg> userMsgLatest(Long userId);

    /**
     * 查询用户消息列表
     * @param userId    用户ID
     * @param isRead    是否已读
     * @param pageQuery 分页查询
     * @return          用户消息列表
     */
    TableDataInfo<SdUserMsg> userMsgList(Long userId, Integer isRead, PageQuery pageQuery);

    /**
     * 读取用户消息
     * @param id    消息ID
     */
    void readUserMsg(String id);

    /**
     * 插入用户消息
     * @param sdUserMsg 用户消息
     */
    void insertUserMsg(SdUserMsg sdUserMsg);
}
