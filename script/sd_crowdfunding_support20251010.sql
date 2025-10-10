-- 众筹支持记录表（简化版）
CREATE TABLE `sd_crowdfunding_support` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支持记录ID',
  `support_no` varchar(32) NOT NULL COMMENT '支持订单号',
  `project_id` bigint(20) NOT NULL COMMENT '众筹项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '支持用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '支持用户姓名',
  `user_avatar` varchar(500) DEFAULT NULL COMMENT '支持用户头像',
  
  -- 支持信息
  `support_amount` decimal(15,2) NOT NULL COMMENT '支持金额',
  
  -- 支付信息
  `payment_method` varchar(50) DEFAULT NULL COMMENT '支付方式',
  `payment_status` tinyint(4) DEFAULT '0' COMMENT '支付状态：0=待支付，1=已支付，2=支付失败，3=已退款',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `payment_no` varchar(64) DEFAULT NULL COMMENT '支付流水号',
  `refund_amount` decimal(15,2) DEFAULT '0.00' COMMENT '退款金额',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  
  -- 留言信息
  `message` text COMMENT '支持留言',
  `is_anonymous` tinyint(1) DEFAULT '0' COMMENT '是否匿名支持：0=否，1=是',
  
  -- 状态信息
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '支持状态：0=正常，1=已取消，2=已退款',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  
  -- 系统字段
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志：0=未删除，1=已删除',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_support_no` (`support_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='众筹支持记录表';
