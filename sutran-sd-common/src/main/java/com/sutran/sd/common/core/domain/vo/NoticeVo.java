package com.sutran.sd.common.core.domain.vo;

import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 消息通知公共VO
 * @author zj
 * @date 2025年11月10日 20:40
 */
@Data
@Accessors(chain=true)
public class NoticeVo {
    /**
     * 通知类型[TOTAL-数量，DETAIL-详情]
     */
    private String noticeType;
    /**
     * 消息ID
     */
    private String id;
    /**
     * 消息类型[sys-系统通知\公告，infos-用户消息]
     */
    private String type;
    /**
      * 标题
      */
    private String title;
    /**
      * 内容
      */
    private String content;
    /**
      * 发布时间
      */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

}
