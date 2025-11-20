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
public class NoticeCommonVo {
    /**
     * 消息ID
     */
    private String id;
    /**
      * 标题
      */
    private String title;
    /**
      * 内容
      */
    private String content;
    /**
     * 通知类型 link NotificationType
     */
    private String notificationType;
    /**
      * 发布时间
      */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

}
