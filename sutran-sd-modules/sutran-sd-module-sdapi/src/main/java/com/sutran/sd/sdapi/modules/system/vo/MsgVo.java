package com.sutran.sd.sdapi.modules.system.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-09-19
 */
@Data
@Accessors(chain = true)
public class MsgVo implements Serializable {
    private Integer total;

    /** 消息类型(0-关注微信公众号,1-SD相关消息) **/
    private Integer type;
    private String id;
    private String title;
    private String msgContent;
    private String crtTime;
}
