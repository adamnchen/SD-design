-- 众筹支持记录表（简化版）
CREATE TABLE `sd_crowdfunding_support` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支持记录ID',
  `project_id` bigint(20) NOT NULL COMMENT '众筹项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '参与者用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '参与者姓名',
  `order_no` varchar(64) NOT NULL COMMENT '支付订单号（唯一）',
  `support_amount` decimal(15,2) NOT NULL COMMENT '支持金额',
  `draw_status` tinyint(4) DEFAULT '0' COMMENT '抽奖状态：0=未参与，1=已参与，2=中奖，3=未中奖',
  `is_winner` tinyint(1) DEFAULT '0' COMMENT '是否中奖：0=否，1=是',
  `prize_info` text COMMENT '奖品信息（JSON格式）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='众筹支持记录表';
