-- 更新 sutran-sd-v1.sql 数据库结构
-- 添加众筹相关表和字段

-- 1. 添加众筹项目主表
CREATE TABLE `sd_crowdfunding_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '众筹项目ID',
  `project_no` varchar(32) NOT NULL COMMENT '项目编号',
  `proofing_invitation_id` bigint(20) NOT NULL COMMENT '关联的打样邀约ID',
  
  -- 项目基本信息（从打样邀约继承）
  `title` varchar(200) NOT NULL COMMENT '项目标题',
  `description` text COMMENT '项目详细描述',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片URL',
  `images` text COMMENT '项目图片列表(JSON格式)',
  `video_url` varchar(500) DEFAULT NULL COMMENT '项目视频URL',
  `tags` varchar(500) DEFAULT NULL COMMENT '项目标签，逗号分隔',
  
  -- 发起人信息（从打样邀约继承）
  `creator_user_id` bigint(20) NOT NULL COMMENT '发起人用户ID（邀约人）',
  `creator_name` varchar(100) NOT NULL COMMENT '发起人姓名',
  `creator_avatar` varchar(500) DEFAULT NULL COMMENT '发起人头像',
  
  -- 厂家信息（从打样邀约继承）
  `manufacturer_user_id` bigint(20) NOT NULL COMMENT '厂家用户ID（被邀约人）',
  `manufacturer_name` varchar(100) NOT NULL COMMENT '厂家姓名',
  `manufacturer_avatar` varchar(500) DEFAULT NULL COMMENT '厂家头像',
  
  -- 众筹目标
  `target_amount` decimal(15,2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(15,2) DEFAULT '0.00' COMMENT '当前已筹金额',
  `support_count` int(11) DEFAULT '0' COMMENT '支持人数',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览次数',
  
  -- 时间设置
  `start_time` datetime NOT NULL COMMENT '众筹开始时间',
  `end_time` datetime NOT NULL COMMENT '众筹结束时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '预计发货时间',
  
  -- 项目状态（简化状态）
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '项目状态：1=众筹中，2=众筹成功，3=众筹失败',
  
  -- 项目设置
  `is_featured` tinyint(1) DEFAULT '0' COMMENT '是否精选：0=否，1=是',
  `is_hot` tinyint(1) DEFAULT '0' COMMENT '是否热门：0=否，1=是',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序权重',
  
  -- 风险提示
  `risk_tips` text COMMENT '风险提示',
  
  -- 抽奖相关
  `draw_number` int(11) DEFAULT '1' COMMENT '抽奖名额数量（从打样邀约继承）',
  `draw_status` tinyint(4) DEFAULT '0' COMMENT '抽奖状态：0=未开始，1=进行中，2=已结束',
  `draw_time` datetime DEFAULT NULL COMMENT '抽奖时间',
  `manufacturer_photos` text COMMENT '厂家上传的实物照片（JSON格式，多张图片）',
  `manufacturer_upload_time` datetime DEFAULT NULL COMMENT '厂家上传照片时间',
  `escrow_status` tinyint NOT NULL DEFAULT '0' COMMENT '资金托管状态：0=托管中，1=已释放给厂家，2=已退款',
  `fund_release_time` datetime DEFAULT NULL COMMENT '资金释放时间',
  
  -- 系统字段
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志：0=未删除，1=已删除',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_no` (`project_no`),
  UNIQUE KEY `uk_proofing_invitation_id` (`proofing_invitation_id`),
  KEY `idx_creator_user_id` (`creator_user_id`),
  KEY `idx_manufacturer_user_id` (`manufacturer_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_escrow_status` (`escrow_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='众筹项目主表';

-- 2. 添加众筹支持记录表
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

-- 3. 添加众筹抽奖记录表
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

-- 4. 更新 pay_order 表，添加退款相关字段
ALTER TABLE `pay_order` 
ADD COLUMN `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '退款金额' AFTER `total_amount`,
ADD COLUMN `refund_time` datetime DEFAULT NULL COMMENT '退款时间' AFTER `refund_amount`,
ADD COLUMN `refund_no` varchar(64) DEFAULT NULL COMMENT '退款单号' AFTER `refund_time`,
ADD COLUMN `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因' AFTER `refund_no`;

-- 5. 更新 pay_order 表的 status 字段注释，添加退款状态
ALTER TABLE `pay_order` 
MODIFY COLUMN `status` tinyint DEFAULT 0 COMMENT '支付状态[0-待支付,1-已支付,2-支付失败,3-已关闭,4-已退款]';

-- 6. 更新 business_type 字段注释，添加众筹业务类型
ALTER TABLE `pay_order` 
MODIFY COLUMN `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型[SD_MEMBER-SD会员,CROWDFUNDING_SUPPORT-众筹支持]';
