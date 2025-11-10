package com.sutran.sd.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


/**
 * 通知公告目标表 sys_notice_targets
 *
 * @author zj
 */
@Data
@TableName("sys_notice_targets")
public class SysNoticeTargets {

    /**
     * 数据ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 公告ID
     */
    private Long noticeId;

     /**
      * 用户ID
      */
    private Long userId;

    /**
     * 发送状态[0-未推送,1-已推送]
     */
    private String sendStatus;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 查看状态[0-否,1-是]
     */
    private String readStatus;

    /**
     * 查看时间
     */
    private Date readTime;

}
