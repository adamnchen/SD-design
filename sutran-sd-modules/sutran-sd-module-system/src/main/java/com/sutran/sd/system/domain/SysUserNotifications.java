package com.sutran.sd.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


/**
 * 用户通知关联表 sys_user_notifications
 *
 * @author zj
 */
@Data
@TableName("sys_user_notifications")
@Accessors(chain = true)
public class SysUserNotifications {

    /**
     * 数据ID
     */
    @TableId(value = "id")
    private Long id;

     /**
      * 用户ID
      */
    private Long userId;
    /**
     * 通知模板ID
     */
    private Long templateId;
     /**
      * 微信OpenID
      */
    private String wxOpenId;
    /**
     * 通知标题
     */
    private String title;
    /**
     * 消息内容
     */
    private String msgContent;
    /**
     * 公众号消息内容
     */
    private String mpMsgContent;
    /**
     * 跳转链接地址
     */
    private String linkUrl;
    /**
     * 通知类型[1-系统通知,2-自定义通知]
     */
    private String notificationType;
    /**
     * 发送状态[0-未推送,1-已推送]
     */
    private Integer sendStatus;
    /**
     * 发送时间
     */
    private Date sendTime;
    /**
     * 查看状态[0-否,1-是]
     */
    private Integer readStatus;
    /**
     * 查看时间
     */
    private Date readTime;
    /**
     * 其他参数(json字符串格式)
     */
    private String otherParams;

}
