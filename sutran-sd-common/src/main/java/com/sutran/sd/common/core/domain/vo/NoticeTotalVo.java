package com.sutran.sd.common.core.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025年11月10日 21:10
 */
@Data
@Accessors(chain=true)
public class NoticeTotalVo implements Serializable {
    /**
     * 通知类型[TOTAL-数量，DETAIL-详情]
     */
    private String noticeType;
    /**
     * 系统通知\公告总数
     */
    private long sysTotal;
    /**
     * 用户通知总数
     */
    private long infosTotal;
     /**
     * 总数
     */
    private long total;
}
