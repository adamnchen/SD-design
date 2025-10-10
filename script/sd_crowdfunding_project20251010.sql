-- 众筹项目主表（简化版，从打样邀约转换）
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
