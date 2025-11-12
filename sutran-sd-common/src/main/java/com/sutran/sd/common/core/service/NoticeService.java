package com.sutran.sd.common.core.service;

import com.sutran.sd.common.core.domain.vo.NoticeCommonVo;
import com.sutran.sd.common.core.domain.vo.NoticeMpVo;
import com.sutran.sd.common.core.domain.vo.NoticeVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 消息通知公共服务
 * @author zj
 * @date 2025年11月10日 20:39
 */
public interface NoticeService {

    /**
     * sse连接
     * @param userId    用户ID
     * @return          SseEmitter
     */
    SseEmitter connect(Long userId);

    /**
     * 发送公共消息
     * @param vo        消息VO
     * @param userId    用户ID
     */
    void asyncSendCommonMsg(NoticeCommonVo vo, Long userId);

    /**
     * 发送微信公众号消息消息
     * @param vo        消息VO
     * @param userId    用户ID
     */
    void asyncSendMpMsg(NoticeMpVo vo, Long userId);

    /**
     * 查询用户通知列表
     * @param userId    用户ID
     * @param num       查询数量
     * @return          用户最新消息
     */
    List<NoticeVo> selectLatestMsgList(Long userId, int num);

     /**
      * 查询系统已发布且有效的通知列表
      * @param num       查询数量
      * @return          系统通知列表
      */
    List<NoticeVo> selectNoticeList(int num);
}
