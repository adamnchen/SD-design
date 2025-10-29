package com.sutran.sd.common.annotation;

import java.lang.annotation.*;

/**
 * 会员权限校验注解
 * 用于标记需要会员权限的方法
 * 
 * @author 陈善
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireMember {
    
    /**
     * 业务类型描述（用于日志和异常提示）
     */
    String value() default "该功能";
    
    /**
     * 是否允许试用用户访问（默认不允许）
     */
    boolean allowTrial() default false;
    
    /**
     * 新用户权益类型
     * DRAW: 新注册用户3天内免费无限生图
     * DESIGN: 新注册用户1个月内打样邀约和预售不受限制
     */
    NewUserBenefitType[] newUserBenefit() default {};
    
    /**
     * 自定义错误提示信息
     */
    String message() default "";
    
    /**
     * 新用户权益类型枚举
     */
    enum NewUserBenefitType {
        /**
         * 生图功能：新注册用户3天内免费无限生图
         */
        DRAW,
        
        /**
         * 设计功能：新注册用户1个月内打样邀约和预售不受限制
         */
        DESIGN
    }
}
