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
public class NoticeMpVo {
    /**
     * 消息ID
     */
    private String id;
    /**
     * 微信公众号消息内容(json字符串)
     */
    private String mpContent;
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
    /**
     * 其他参数(json字符串)
     */
    private String otherParams;

}
