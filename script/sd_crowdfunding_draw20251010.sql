-- 众筹抽奖记录表
CREATE TABLE `sd_crowdfunding_draw` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '抽奖记录ID',
  `project_id` bigint(20) NOT NULL COMMENT '众筹项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '参与抽奖用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '参与抽奖用户姓名',
  `user_avatar` varchar(500) DEFAULT NULL COMMENT '参与抽奖用户头像',
  
  -- 抽奖信息
  `draw_no` varchar(32) NOT NULL COMMENT '抽奖编号',
  `is_winner` tinyint(1) DEFAULT '0' COMMENT '是否中奖：0=否，1=是',
  `prize_name` varchar(200) DEFAULT NULL COMMENT '奖品名称',
  `prize_description` text COMMENT '奖品描述',
  `prize_image` varchar(500) DEFAULT NULL COMMENT '奖品图片',
  
  -- 中奖信息
  `win_time` datetime DEFAULT NULL COMMENT '中奖时间',
  `win_order` int(11) DEFAULT NULL COMMENT '中奖顺序（第几个中奖）',
  `is_claimed` tinyint(1) DEFAULT '0' COMMENT '是否已领取：0=否，1=是',
  `claim_time` datetime DEFAULT NULL COMMENT '领取时间',
  `claim_address` varchar(500) DEFAULT NULL COMMENT '收货地址',
  `claim_phone` varchar(20) DEFAULT NULL COMMENT '收货电话',
  `claim_name` varchar(100) DEFAULT NULL COMMENT '收货人姓名',
  
  -- 物流信息
  `shipping_status` tinyint(4) DEFAULT '0' COMMENT '发货状态：0=未发货，1=已发货，2=已收货',
  `shipping_time` datetime DEFAULT NULL COMMENT '发货时间',
  `shipping_company` varchar(100) DEFAULT NULL COMMENT '物流公司',
  `shipping_no` varchar(100) DEFAULT NULL COMMENT '物流单号',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  
  -- 系统字段
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志：0=未删除，1=已删除',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_draw_no` (`draw_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_winner` (`is_winner`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='众筹抽奖记录表';
