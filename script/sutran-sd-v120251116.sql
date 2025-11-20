/*
 Navicat Premium Dump SQL

 Source Server         : sd生产数据库（重要！）
 Source Server Type    : MySQL
 Source Server Version : 50729 (5.7.29)
 Source Host           : 106.15.90.128:9113
 Source Schema         : sutran-sd-v1

 Target Server Type    : MySQL
 Target Server Version : 50729 (5.7.29)
 File Encoding         : 65001

 Date: 16/11/2025 14:11:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_msg_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_msg_history`;
CREATE TABLE `ai_msg_history`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `session_id` bigint(20) NULL DEFAULT NULL COMMENT '会话ID',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色[system,user,assistant]',
  `type` int(11) NULL DEFAULT 0 COMMENT '类型[0-问题,1-答案]',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `file_urls` json NULL COMMENT '文件地址集合',
  `crt_user_id` bigint(20) NULL DEFAULT NULL COMMENT '归属人ID',
  `crt_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `session_id`(`session_id`, `role`, `crt_user_id`, `crt_time`) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id`) USING BTREE,
  CONSTRAINT `ai_msg_history_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `ai_msg_session` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI对话 || 历史消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_msg_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_msg_session`;
CREATE TABLE `ai_msg_session`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话名称',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型[text-文本,image-图片]',
  `crt_user_id` bigint(20) NULL DEFAULT NULL COMMENT '归属人',
  `crt_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '归属人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '添加时间',
  `order_num` int(11) NULL DEFAULT NULL COMMENT '排序值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI对话 || 会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint(20) NOT NULL COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint(20) NOT NULL COMMENT '编号',
  `table_id` bigint(20) NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_member
-- ----------------------------
DROP TABLE IF EXISTS `pay_member`;
CREATE TABLE `pay_member`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `level` int(11) NULL DEFAULT 1 COMMENT '会员等级',
  `level_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员权益描述',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `duration` int(11) NULL DEFAULT NULL COMMENT '会员有效期(天)',
  `limit_train_times` int(11) NULL DEFAULT 0 COMMENT '限制训练次数',
  `limit_draw_num` int(11) NULL DEFAULT 0 COMMENT '限制绘图数量',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `is_hide` tinyint(4) NULL DEFAULT 0 COMMENT '是否隐藏[0-否,1-是]',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付会员配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order`  (
  `id` bigint(20) NOT NULL COMMENT '支付记录ID',
  `out_trade_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统订单号',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝应用编号',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝交易流水号',
  `gmt_payment` datetime NULL DEFAULT NULL COMMENT '支付宝支付时间',
  `subject` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务订单标题名称',
  `body` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '务订单详情内容',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '支付状态[0-待支付,1-已支付,2-支付失败,3-已退款]',
  `channel_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付渠道[WX_PAY-微信支付,ALI_PAY-支付宝支付]',
  `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型[SD_MEMBER-SD会员,PROOF_CROWDFUND-打样众筹]',
  `business_id` bigint(20) NULL DEFAULT NULL COMMENT '业务ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '订单过期时间',
  `notify_time` datetime NULL DEFAULT NULL COMMENT '支付宝异步通知时间',
  `notify_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '支付宝异步通知内容',
  `qr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付二维码地址',
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款原因',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `out_trade_no`(`out_trade_no`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付订单记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sd_channel_data
-- ----------------------------
DROP TABLE IF EXISTS `sd_channel_data`;
CREATE TABLE `sd_channel_data`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `is_send` tinyint(4) NULL DEFAULT 0 COMMENT '是否发送成功[0-否,1-是]',
  `channel_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '第三方渠道用户ID',
  `api_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求API地址',
  `api_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求API签名',
  `pic_url_list` json NULL COMMENT '发送图片数组',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `retry_times` int(11) NULL DEFAULT NULL COMMENT '重试次数',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求错误原因',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `channel_user_id`(`channel_user_id`) USING BTREE,
  INDEX `is_send`(`is_send`, `send_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 渠道消息推送记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_common_config
-- ----------------------------
DROP TABLE IF EXISTS `sd_common_config`;
CREATE TABLE `sd_common_config`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `pre_img_min_num` int(11) NULL DEFAULT NULL COMMENT '预处理提交图片最小数量',
  `pre_img_max_num` int(11) NULL DEFAULT NULL COMMENT '预处理提交图片最大数量',
  `channel_send_api_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道推送API地址',
  `channel_send_api_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道推送API签名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 通用设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_crowdfunding_project
-- ----------------------------
DROP TABLE IF EXISTS `sd_crowdfunding_project`;
CREATE TABLE `sd_crowdfunding_project`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '众筹项目ID',
  `project_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目编号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目详细描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目图片列表(JSON格式)',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目视频URL',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目标签，逗号分隔',
  `creator_user_id` bigint(20) NOT NULL COMMENT '发起人用户ID',
  `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起人姓名',
  `creator_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起人头像',
  `manufacturer_user_id` bigint(20) NULL DEFAULT NULL COMMENT '厂家用户ID',
  `manufacturer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂家姓名',
  `manufacturer_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂家头像',
  `proofing_invitation_id` bigint(20) NULL DEFAULT NULL COMMENT '关联的打样邀约ID',
  `target_amount` decimal(15, 2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '当前已筹金额',
  `support_count` int(11) NULL DEFAULT 0 COMMENT '支持人数',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `start_time` datetime NOT NULL COMMENT '众筹开始时间',
  `end_time` datetime NOT NULL COMMENT '众筹结束时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '预计发货时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '项目状态：1=众筹中，2=众筹成功，3=众筹失败',
  `is_featured` tinyint(1) NULL DEFAULT 0 COMMENT '是否精选：0=否，1=是',
  `is_hot` tinyint(1) NULL DEFAULT 0 COMMENT '是否热门：0=否，1=是',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序权重',
  `risk_tips` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '风险提示',
  `draw_number` int(11) NULL DEFAULT 1 COMMENT '抽奖名额数量',
  `total_samples` int(11) NULL DEFAULT NULL COMMENT '样品总数量',
  `draw_status` tinyint(4) NULL DEFAULT 0 COMMENT '抽奖状态：0=未开始，1=进行中，2=已结束',
  `draw_time` datetime NULL DEFAULT NULL COMMENT '抽奖时间',
  `manufacturer_photos` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '厂家上传的实物照片（JSON格式，多张图片）',
  `manufacturer_upload_time` datetime NULL DEFAULT NULL COMMENT '厂家上传照片时间',
  `escrow_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '资金托管状态：0=托管中，1=已释放给厂家，2=已退款',
  `fund_release_audit_status` tinyint(4) NULL DEFAULT NULL COMMENT '资金释放审核状态：1=待审核，2=审核通过，3=审核拒绝',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `audit_user_id` bigint(20) NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `fund_release_time` datetime NULL DEFAULT NULL COMMENT '资金释放时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `designer_photos` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设计师上传的实物照片（JSON格式，多张图片）',
  `designer_upload_time` datetime NULL DEFAULT NULL COMMENT '设计师上传照片时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_no`(`project_no`) USING BTREE,
  INDEX `idx_creator_user_id`(`creator_user_id`) USING BTREE,
  INDEX `idx_proofing_invitation_id`(`proofing_invitation_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_start_time`(`start_time`) USING BTREE,
  INDEX `idx_end_time`(`end_time`) USING BTREE,
  INDEX `idx_draw_status`(`draw_status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_sd_crowdfunding_project_designer_upload_time`(`designer_upload_time`) USING BTREE,
  INDEX `idx_fund_release_audit_status`(`fund_release_audit_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1989609854120210434 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '众筹项目主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_crowdfunding_sample_delivery
-- ----------------------------
DROP TABLE IF EXISTS `sd_crowdfunding_sample_delivery`;
CREATE TABLE `sd_crowdfunding_sample_delivery`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `crowdfunding_project_id` bigint(20) NOT NULL COMMENT '众筹项目ID',
  `proofing_invitation_id` bigint(20) NOT NULL COMMENT '关联邀约记录ID',
  `sample_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样品图片地址',
  `recipient_user_id` bigint(20) NOT NULL COMMENT '收货人用户ID',
  `recipient_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `recipient_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `delivery_address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '收货人地址',
  `tracking_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '快递单号',
  `delivery_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '快递公司',
  `delivery_status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1=待发货，2=已发货',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sender_user_id` bigint(20) NOT NULL COMMENT '发货人用户ID',
  `sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发货人姓名',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认收货时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL,
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `order_status` tinyint(4) NULL DEFAULT NULL COMMENT '状态：1=待处理 2=已完成',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_crowdfunding_project_id`(`crowdfunding_project_id`) USING BTREE,
  INDEX `idx_recipient_user_id`(`recipient_user_id`) USING BTREE,
  INDEX `idx_status`(`delivery_status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '众筹样品发货记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_crowdfunding_support
-- ----------------------------
DROP TABLE IF EXISTS `sd_crowdfunding_support`;
CREATE TABLE `sd_crowdfunding_support`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支持记录ID',
  `project_id` bigint(20) NOT NULL COMMENT '众筹项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '参与者用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参与者姓名',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人手机号',
  `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地址',
  `receiver_area` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地区',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付订单号（唯一）',
  `support_amount` decimal(15, 2) NOT NULL COMMENT '支持金额',
  `draw_status` tinyint(4) NULL DEFAULT 0 COMMENT '抽奖状态：0=未参与，1=已参与，2=中奖，3=未中奖',
  `is_winner` tinyint(1) NULL DEFAULT 0 COMMENT '是否中奖：0=否，1=是',
  `prize_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '奖品信息（JSON格式）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` int(11) NULL DEFAULT 0 COMMENT '支持状态：0=正常，1=已取消，2=已退款',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款原因',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_sd_crowdfunding_support_status`(`status`) USING BTREE,
  INDEX `idx_sd_crowdfunding_support_refund_time`(`refund_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '众筹支持记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_draw_node
-- ----------------------------
DROP TABLE IF EXISTS `sd_draw_node`;
CREATE TABLE `sd_draw_node`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点编码',
  `type` tinyint(4) NULL DEFAULT 0 COMMENT '节点类型[0-绘图,1-训练]',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点基础URL',
  `is_active` tinyint(4) NULL DEFAULT 0 COMMENT '是否激活[0-否,1-是]',
  `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点地区',
  `weight` bigint(20) NULL DEFAULT 10 COMMENT '基础权重(0-100)',
  `max_concurrent_tasks` int(11) NULL DEFAULT 20 COMMENT '最大任务并发数',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 节点配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_flow
-- ----------------------------
DROP TABLE IF EXISTS `sd_flow`;
CREATE TABLE `sd_flow`  (
  `id` bigint(20) NOT NULL COMMENT '工作流ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工作流名称',
  `flow` json NULL COMMENT '工作流',
  `draw_num` int(11) NULL DEFAULT 1 COMMENT '初始生图数量',
  `init_prompt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '初始提示词(英文)',
  `init_prompt_zh` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '初始提示词(中文)',
  `is_fixed` tinyint(4) NULL DEFAULT 0 COMMENT '是否固定[0-否,1-是]',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `belong_user_id` bigint(20) NULL DEFAULT NULL COMMENT '归属人ID',
  `belong_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '归属人名称',
  `is_open` tinyint(4) NULL DEFAULT 0 COMMENT '是否开放[0-否,1-是]',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SDXL' COMMENT '模型类型[SDXL,FLUX]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `model_type`(`model_type`) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id`) USING BTREE,
  INDEX `is_fixed`(`is_fixed`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 工作流' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_gpu_pool
-- ----------------------------
DROP TABLE IF EXISTS `sd_gpu_pool`;
CREATE TABLE `sd_gpu_pool`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `host` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'SD服务IP地址',
  `port` int(11) NULL DEFAULT NULL COMMENT 'SD服务端口',
  `device_id` int(11) NULL DEFAULT NULL COMMENT 'GPU序号',
  `type` int(11) NULL DEFAULT NULL COMMENT '类型[0-sd,1-训练]',
  `img_grid_dir` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型测试图生图grid目录',
  `txt_grid_dir` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型测试文生图grid目录',
  `is_enable` int(11) NULL DEFAULT 0 COMMENT '是否启用[0-否,1-是]',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `host`(`host`, `port`, `device_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || GPU卡池' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_presale_delivery
-- ----------------------------
DROP TABLE IF EXISTS `sd_presale_delivery`;
CREATE TABLE `sd_presale_delivery`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '发货记录ID',
  `delivery_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发货单号（唯一）',
  `project_id` bigint(20) NOT NULL COMMENT '预售项目ID',
  `order_id` bigint(20) NOT NULL COMMENT '预售订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '收货用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货用户姓名',
  `product_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品标题',
  `quantity` int(11) NOT NULL COMMENT '发货数量',
  `total_amount` decimal(15, 2) NOT NULL COMMENT '总金额',
  `recipient_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人姓名',
  `recipient_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人手机号',
  `recipient_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货地址',
  `tracking_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '快递单号',
  `delivery_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '发货状态：1=待发货，2=已发货',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `sender_user_id` bigint(20) NULL DEFAULT NULL COMMENT '发货人用户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_delivery_no`(`delivery_no`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_delivery_status`(`delivery_status`) USING BTREE,
  INDEX `idx_tracking_number`(`tracking_number`) USING BTREE,
  INDEX `idx_delivery_time`(`delivery_time`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '预售发货表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sd_presale_order
-- ----------------------------
DROP TABLE IF EXISTS `sd_presale_order`;
CREATE TABLE `sd_presale_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预售订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号（唯一）',
  `project_id` bigint(20) NOT NULL COMMENT '预售项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '购买用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '购买用户姓名',
  `product_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品标题',
  `product_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品图片',
  `quantity` int(11) NOT NULL COMMENT '购买数量',
  `original_unit_price` decimal(15, 2) NOT NULL COMMENT '下单时的单价（基于当时销售数量计算的阶梯价格）',
  `final_unit_price` decimal(15, 2) NULL DEFAULT NULL COMMENT '最终确认单价（支付成功后，根据最新销售数量重新计算的阶梯价格）',
  `original_total_amount` decimal(15, 2) NOT NULL COMMENT '下单时的总金额',
  `final_total_amount` decimal(15, 2) NULL DEFAULT NULL COMMENT '最终确认总金额',
  `refund_amount` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '退款金额（阶梯价格调整后的退款）',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人手机号',
  `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地址',
  `receiver_area` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地区',
  `pay_order_id` bigint(20) NULL DEFAULT NULL COMMENT '支付订单ID（关联pay_order表）',
  `order_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '订单状态：1=待支付，2=已支付，3=生产中，4=已发货，5=已完成，6=已取消，7=已退款',
  `delivery_status` tinyint(4) NULL DEFAULT 0 COMMENT '发货状态：0=未开始，1=已发货，2=已签收',
  `express_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '快递单号',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '签收时间',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款原因',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_pay_order_id`(`pay_order_id`) USING BTREE,
  INDEX `idx_order_status`(`order_status`) USING BTREE,
  INDEX `idx_delivery_status`(`delivery_status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1989282015751630850 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '批量订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_presale_project
-- ----------------------------
DROP TABLE IF EXISTS `sd_presale_project`;
CREATE TABLE `sd_presale_project`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预售项目ID',
  `project_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目编号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目详细描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目图片列表(JSON格式)',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目视频URL',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目标签，逗号分隔',
  `creator_user_id` bigint(20) NOT NULL COMMENT '发起人用户ID',
  `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起人姓名',
  `creator_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起人头像',
  `manufacturer_user_id` bigint(20) NULL DEFAULT NULL COMMENT '厂家用户ID',
  `manufacturer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂家姓名',
  `manufacturer_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂家头像',
  `proofing_invitation_id` bigint(20) NULL DEFAULT NULL COMMENT '关联的打样邀约ID',
  `base_price` decimal(15, 2) NOT NULL COMMENT '基础单价（最低阶梯价格）',
  `tiered_pricing` json NULL COMMENT '阶梯价格配置(JSON数组: [{\"unitPrice\":100,\"node\":20}])',
  `validity_days` int(11) NOT NULL DEFAULT 30 COMMENT '有效期天数（从创建时间开始计算）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '项目状态：1=销售中，2=暂停销售，3=已下架，4=已取消',
  `production_status` tinyint(4) NULL DEFAULT 0 COMMENT '生产状态：0=未开始，1=进行中，2=已完成',
  `delivery_status` tinyint(4) NULL DEFAULT 0 COMMENT '发货状态：0=未开始，1=进行中，2=已完成',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` int(11) NULL DEFAULT 0 COMMENT '收藏次数',
  `share_count` int(11) NULL DEFAULT 0 COMMENT '分享次数',
  `manufacturer_photos` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '厂家上传的实物照片（JSON格式，多张图片）',
  `manufacturer_upload_time` datetime NULL DEFAULT NULL COMMENT '厂家上传照片时间',
  `total_quantities` int(11) NULL DEFAULT NULL COMMENT '销售数量',
  `total_sales_amount` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '累计销售金额',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_no`(`project_no`) USING BTREE,
  INDEX `idx_creator_user_id`(`creator_user_id`) USING BTREE,
  INDEX `idx_manufacturer_user_id`(`manufacturer_user_id`) USING BTREE,
  INDEX `idx_proofing_invitation_id`(`proofing_invitation_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_validity_days`(`validity_days`) USING BTREE,
  INDEX `idx_production_status`(`production_status`) USING BTREE,
  INDEX `idx_delivery_status`(`delivery_status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1988410914909601802 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '批量订单项目主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_proofing_invitation_candidates
-- ----------------------------
DROP TABLE IF EXISTS `sd_proofing_invitation_candidates`;
CREATE TABLE `sd_proofing_invitation_candidates`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '候选记录ID',
  `invitation_id` bigint(20) UNSIGNED NOT NULL COMMENT '主邀约ID（逻辑关联，无外键）',
  `invitee_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '被邀约厂家用户ID',
  `quoted_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '厂家报价(元)',
  `quoted_period_days` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '预计打样周期(天)',
  `is_quote_batch_plan` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否提供批量方案(0/1)',
  `tiered_pricing` json NULL COMMENT '阶梯价格(JSON数组)',
  `profit_share_ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT '利润分成比例(%)',
  `quote_submit_at` timestamp NULL DEFAULT NULL COMMENT '报价提交时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '候选状态(0:待处理,1:已接受,2:已拒绝,4:已关闭)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invitation_invitee`(`invitation_id`, `invitee_user_id`) USING BTREE,
  INDEX `idx_invitation_id_status`(`invitation_id`, `status`) USING BTREE,
  INDEX `idx_invitee_user_id`(`invitee_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1989609796591136771 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邀约候选厂家报价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_proofing_invitations
-- ----------------------------
DROP TABLE IF EXISTS `sd_proofing_invitations`;
CREATE TABLE `sd_proofing_invitations`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '邀请单的唯一ID，主键',
  `work_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起邀约的作品ID，关联作品表',
  `product_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '打样产品的标题',
  `product_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '邀约作品的详细描述（如材质、尺寸、风格等）',
  `model_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型来源或版本号（例如：v2.3）',
  `inviter_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '邀约人用户ID (创建人)',
  `invitee_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '被邀约用户ID',
  `cooperation_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '其他合作内容详情',
  `keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起邀约时填写的关键词/标签',
  `is_batch_production` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要批量生产（0:否, 1:是）',
  `proofing_quantity` int(10) UNSIGNED NOT NULL COMMENT '期望的打样数量 (件)',
  `delivery_limit_hours` int(10) UNSIGNED NOT NULL COMMENT '发起方要求的预计交付时限 (小时)',
  `cancel_time_limit` int(11) NULL DEFAULT NULL COMMENT '无人应答自动取消时限(1,2,3,一天两天与三天)',
  `quoted_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '被邀约方提交的报价金额 (元)',
  `quoted_period_days` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '被邀约方提交的预计打样周期 (天)',
  `is_quote_batch_plan` tinyint(1) NOT NULL DEFAULT 0 COMMENT '报价时是否提供了批量生产方案 (0:否, 1:是)',
  `quote_submit_at` timestamp NULL DEFAULT NULL COMMENT '报价提交时间',
  `tiered_pricing` json NULL COMMENT '阶梯价格配置(JSON数组: [20, 30, 40] 对应 0-20, 20-30, 30-40 区间)',
  `profit_share_ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT '利润分成比例(%)，例如 15.50 表示 15.5%',
  `selected_invitee_user_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '最终选中的厂家用户ID',
  `selected_at` timestamp NULL DEFAULT NULL COMMENT '最终选择时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '邀约状态 (0: 待处理, 1: 已接受, 2: 已拒绝, 3: 已取消，4.待回应，5已处理，6待确认，7已发布)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 (邀约时间)',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `draw_number` int(11) NOT NULL COMMENT '打样样品参与抽奖的数量分配给众筹用户,最少一个',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人手机号',
  `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地址',
  `receiver_area` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地区',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_id_status`(`work_id`, `status`) USING BTREE,
  INDEX `idx_inviter_user_id`(`inviter_user_id`) USING BTREE,
  INDEX `idx_invitee_user_id`(`invitee_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1989609626163982339 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作品打样合作邀请记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_train_task
-- ----------------------------
DROP TABLE IF EXISTS `sd_train_task`;
CREATE TABLE `sd_train_task`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `pre_params` json NULL COMMENT '预处理参数',
  `img_num` int(11) NULL DEFAULT NULL COMMENT '预处理图片数量',
  `pre_submit_time` datetime NULL DEFAULT NULL COMMENT '预处理提交时间',
  `pre_start_time` datetime NULL DEFAULT NULL COMMENT '预处理开始时间',
  `pre_end_time` datetime NULL DEFAULT NULL COMMENT '预处理结束时间',
  `pre_reason` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预处理失败原因',
  `status` int(11) NULL DEFAULT 0 COMMENT '任务状态(废弃)[0-未训练,1-已预处理,2-已训练,3-训练失败]',
  `new_status` int(11) NULL DEFAULT 0 COMMENT '任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]',
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练任务ID',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练任务模型名称',
  `addition_tag` json NULL COMMENT '训练任务共性词数组',
  `train_params` json NULL COMMENT '训练任务参数',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '训练任务提交时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '训练任务开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '训练任务完成时间',
  `reason` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练失败原因',
  `gpu_pool` json NULL COMMENT '训练使用的GPU',
  `node_id` bigint(20) NULL DEFAULT NULL COMMENT '训练任务节点ID',
  `crt_user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `crt_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `task_id`(`task_id`) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id`, `new_status`) USING BTREE,
  INDEX `node_id`(`node_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 图片预处理任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_translation
-- ----------------------------
DROP TABLE IF EXISTS `sd_translation`;
CREATE TABLE `sd_translation`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `zh` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '中文',
  `en` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '英文',
  `type` tinyint(4) NULL DEFAULT NULL COMMENT '类型[0-提示词英译中,1-tag标签中译英,2-共性词,3-标签词英译中]',
  `train_task_id` bigint(20) NULL DEFAULT NULL COMMENT '训练任务ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `type`(`type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 翻译' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_favorite`;
CREATE TABLE `sd_user_favorite`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（收藏者）',
  `favorite_type` tinyint(4) NOT NULL COMMENT '收藏类型：1=模型，2=作品',
  `target_id` bigint(20) NOT NULL COMMENT '收藏对象ID（模型ID或作品ID）',
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标对象封面/缩略图URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_type_target`(`user_id`, `favorite_type`, `target_id`) USING BTREE COMMENT '唯一索引：防止同一用户重复收藏同一对象',
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '用户ID索引：查询用户的所有收藏',
  INDEX `idx_target_id`(`target_id`, `favorite_type`) USING BTREE COMMENT '对象ID索引：查询某对象的收藏数',
  INDEX `idx_favorite_type`(`favorite_type`) USING BTREE COMMENT '收藏类型索引：按类型查询',
  INDEX `idx_create_time`(`create_time`) USING BTREE COMMENT '创建时间索引：按时间排序'
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收藏表（支持收藏模型和作品）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sd_user_model
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model`;
CREATE TABLE `sd_user_model`  (
  `id` bigint(20) NOT NULL COMMENT '模型ID',
  `classify_id` bigint(20) NULL DEFAULT 1 COMMENT '模型分类ID',
  `task_id` bigint(20) NULL DEFAULT NULL COMMENT '关联训练任务ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型标题',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型名称',
  `model_name_zh` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型别名',
  `model_strength` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0.5' COMMENT '模型强度',
  `addition_tag` json NULL COMMENT '模型共性词',
  `hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型hash值',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型存储位置',
  `config` json NULL COMMENT '模型配置',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型封面地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型描述',
  `model_tag` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型标签(逗号隔开的字符串)',
  `type` int(11) NULL DEFAULT NULL COMMENT '模型归属类型[0-系统,1-个人]',
  `is_open` int(11) NULL DEFAULT NULL COMMENT '模型是否公开[0-否,1-是]',
  `belong_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型归属人ID',
  `publish_status` int(11) NULL DEFAULT 0 COMMENT '发布状态[0-否,1-是]',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '模型创建时间',
  `is_user_del` int(11) NULL DEFAULT 0 COMMENT '用户是否已删除该模型[0-否,1-是]',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SDXL' COMMENT '模型类型[SDXL,FLUX]',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `hash`(`hash`) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id`) USING BTREE,
  INDEX `classify_id`(`classify_id`) USING BTREE,
  INDEX `publish_status`(`publish_status`) USING BTREE,
  INDEX `idx_type_open`(`type`, `is_open`, `belong_user_id`) USING BTREE,
  INDEX `task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_model_classify
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_classify`;
CREATE TABLE `sd_user_model_classify`  (
  `id` bigint(20) NOT NULL COMMENT '分类ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类名称',
  `crt_user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_model_classify_tmp
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_classify_tmp`;
CREATE TABLE `sd_user_model_classify_tmp`  (
  `model_id` bigint(20) NOT NULL,
  `classify_id` bigint(20) NOT NULL,
  `crt_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`model_id`, `classify_id`, `crt_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型分类(自定义分类)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_model_file
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_file`;
CREATE TABLE `sd_user_model_file`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `task_id` bigint(20) NULL DEFAULT NULL COMMENT '任务ID',
  `category` int(11) NULL DEFAULT 0 COMMENT '分类[0-文生图，1-图生图]',
  `is_redraw` int(11) NULL DEFAULT 0 COMMENT '是否局部重绘[0-否,1-是]',
  `prompt` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述词(译文)',
  `prompt_zh` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述词(原文)',
  `prompt_desc` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '弃用词',
  `negative_prompt` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '反向提示词(译文)',
  `negative_prompt_zh` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '反向提示词(原文)',
  `model_strength` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型强度',
  `init_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参考图片',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件地址',
  `file_info` json NULL COMMENT '文件信息',
  `file_parameters` json NULL COMMENT '文件参数信息',
  `is_public` int(11) NULL DEFAULT 0 COMMENT '是否公开[0-否,1-是]',
  `public_time` datetime NULL DEFAULT NULL COMMENT '公开时间',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础大模型名称',
  `lora_model_id` bigint(20) NULL DEFAULT NULL COMMENT 'lora模型ID',
  `lora_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型名称',
  `lora_title_zh` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型名称(中文)',
  `lora_info` json NULL COMMENT 'lora模型信息数组',
  `belong_user_id` bigint(20) NULL DEFAULT NULL COMMENT '文件归属人ID',
  `belong_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件归属人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `task_id`(`task_id`) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id`) USING BTREE,
  INDEX `crt_time`(`crt_time`) USING BTREE,
  INDEX `lora_model_id`(`lora_model_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户生图文件数据记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_model_log
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_log`;
CREATE TABLE `sd_user_model_log`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础大模型名称',
  `lora_model_id` bigint(20) NULL DEFAULT NULL COMMENT 'lora模型ID',
  `lora_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型标题',
  `model_strength` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型强度',
  `use_times` int(11) NULL DEFAULT 1 COMMENT '使用次数',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`, `lora_model_id`) USING BTREE,
  INDEX `idx_user_crt_use`(`user_id`, `crt_time`, `use_times`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型使用记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_model_share
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_share`;
CREATE TABLE `sd_user_model_share`  (
  `model_id` bigint(20) NOT NULL COMMENT '模型ID',
  `user_id` bigint(20) NOT NULL COMMENT '被分享人ID',
  `crt_user_id` bigint(20) NULL DEFAULT NULL COMMENT '分享人ID',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '分享时间',
  PRIMARY KEY (`model_id`, `user_id`) USING BTREE,
  INDEX `idx_model_user`(`model_id`, `user_id`) USING BTREE,
  CONSTRAINT `sd_user_model_share_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `sd_user_model` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型分享' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_msg
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_msg`;
CREATE TABLE `sd_user_msg`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `wx_open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信公众号openId',
  `template_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信公众号消息模板ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息标题',
  `msg_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `msg_body` json NULL COMMENT '消息体',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '被通知人ID',
  `is_read` int(11) NULL DEFAULT NULL COMMENT '是否已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '读取时间',
  `pre_task_id` bigint(20) NULL DEFAULT NULL COMMENT '训练任务预处理ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 消息提醒' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sd_user_normal_point
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_normal_point`;
CREATE TABLE `sd_user_normal_point`  (
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `proofing_point` int(11) NULL DEFAULT NULL COMMENT '打样积分',
  `priifing_level` int(11) NULL DEFAULT NULL COMMENT '打样荣誉等级',
  `sale_point` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '销售积分',
  `sale_level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '销售荣誉等级',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sd_user_task
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_task`;
CREATE TABLE `sd_user_task`  (
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'WEBUI' COMMENT '任务类型[WEBUI,COMFYUI]',
  `status` int(11) NULL DEFAULT NULL COMMENT '执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]',
  `flow` json NULL COMMENT 'comfy工作流',
  `category` int(11) NULL DEFAULT 0 COMMENT '分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图,4-工具修复]',
  `prompt_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy内部任务ID',
  `prompt` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy提示词(英)',
  `prompt_zh` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy提示词(中)',
  `node_id` bigint(20) NULL DEFAULT NULL COMMENT 'comfy任务执行的节点ID',
  `init_img_list` json NULL COMMENT '参考图片地址数组',
  `belong_user_id` bigint(20) NULL DEFAULT NULL COMMENT '任务归属人ID',
  `belong_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务归属人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '任务创建时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '任务开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '任务结束时间',
  `reason` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `is_redraw` int(11) NULL DEFAULT 0 COMMENT '是否局部重绘(仅在图生图启用:0-否,1-是)',
  `upd_time` datetime NULL DEFAULT NULL COMMENT '任务更新时间',
  PRIMARY KEY (`task_id`) USING BTREE,
  UNIQUE INDEX `task_id`(`task_id`) USING BTREE,
  UNIQUE INDEX `prompt_id`(`prompt_id`) USING BTREE,
  INDEX `status`(`status`, `belong_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户任务执行记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_address
-- ----------------------------
DROP TABLE IF EXISTS `sys_address`;
CREATE TABLE `sys_address`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '唯一主键ID',
  `area_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '行政区划代码，如: 320102000',
  `area_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '区域名称，如: 玄武区',
  `parent_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '父级区域代码，用于级联查询',
  `area_level` tinyint(3) UNSIGNED NOT NULL COMMENT '区域级别: 1=省, 2=市, 3=区/县, 4=街道/乡镇',
  `zip_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮政编码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_area_code`(`area_code`) USING BTREE,
  INDEX `idx_parent_code`(`parent_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45381 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户地址行政区划表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` bigint(20) NOT NULL COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(20) NOT NULL COMMENT '部门id',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(20) NOT NULL COMMENT '字典编码',
  `dict_sort` int(11) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE,
  INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(20) NOT NULL COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint(20) NOT NULL COMMENT '访问ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `user_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户类型',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status`) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `is_frame` int(11) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int(11) NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint(20) NOT NULL COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型[0-普通,1-紧急,2-活动]',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态[0-未发布,1-已发布]',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice_targets
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_targets`;
CREATE TABLE `sys_notice_targets`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `notice_id` bigint(20) NULL DEFAULT NULL COMMENT '公告ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `send_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '发送状态[0-未推送,1-已推送]',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '查看状态[0-否,1-是]',
  `read_time` datetime NULL DEFAULT NULL COMMENT '查看时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `notice_id`(`notice_id`, `user_id`) USING BTREE,
  INDEX `user_id`(`user_id`, `read_status`) USING BTREE,
  INDEX `notice_id_2`(`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告目标' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint(20) NOT NULL COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int(11) NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int(11) NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int(11) NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type`) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status`) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `oss_id` bigint(20) NOT NULL COMMENT '对象存储主键',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '原名',
  `file_suffix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件后缀名',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'URL地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '上传人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新人',
  `service` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'minio' COMMENT '服务商',
  PRIMARY KEY (`oss_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OSS对象存储表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_oss_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss_config`;
CREATE TABLE `sys_oss_config`  (
  `oss_config_id` bigint(20) NOT NULL COMMENT '主建',
  `config_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置key',
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'accessKey',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '秘钥',
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '桶名称',
  `prefix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '前缀',
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '访问站点',
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '自定义域名',
  `is_https` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否https（Y=是,N=否）',
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '域',
  `access_policy` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '桶权限类型(0=private 1=public 2=custom)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否默认（0=是,1=否）',
  `ext1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '扩展字段',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`oss_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对象存储配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int(11) NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(11) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `wx_open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `user_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'sys_user' COMMENT '用户类型（sys_user系统用户,bs_user-业务用户）',
  `biz_type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '用户身份类型：0=厂商和设计师，1=设计师，2=普通用户',
  `channel_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '用户渠道来源ID',
  `channel` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '系统注册' COMMENT '用户渠道来源',
  `channel_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道用户ID',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `alipay_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝账号（手机号或邮箱）',
  `alipay_real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝实名姓名',
  `alipay_bind_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '支付宝绑定状态（0-未绑定，1-已绑定）',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `limit_valid_date` datetime NULL DEFAULT NULL COMMENT '限制有效期',
  `limit_train_times` int(11) NULL DEFAULT NULL COMMENT '限制模型训练次数',
  `limit_draw_num` int(11) NULL DEFAULT NULL COMMENT '限制绘图张数',
  `is_close_guide` int(11) NULL DEFAULT 0 COMMENT '是否关闭引导[0-否,1-是]',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户个人简介',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `wx_open_id`(`wx_open_id`) USING BTREE,
  UNIQUE INDEX `phonenumber`(`phonenumber`) USING BTREE,
  INDEX `user_type`(`user_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_address
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_address`;
CREATE TABLE `sys_user_address`  (
  `id` bigint(20) NOT NULL COMMENT '标签ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签名称',
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省(直辖市)',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '市(直辖市区)',
  `county` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区(直辖市县)',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '街道',
  `home` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号',
  `is_default` tinyint(4) NULL DEFAULT NULL COMMENT '是否默认地址[0-否,1-是]',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户物流地址关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_member
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_member`;
CREATE TABLE `sys_user_member`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `level_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员名称',
  `start_time` datetime NULL DEFAULT NULL COMMENT '会员开始时间',
  `duration` int(11) NULL DEFAULT NULL COMMENT '会员有效期(天)',
  `end_time` datetime NULL DEFAULT NULL COMMENT '会员结束时间',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态(0:已过期,1:有效)',
  `out_trade_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统订单号',
  `old_limit_train_times` int(11) NULL DEFAULT 0 COMMENT '原始限制训练次数',
  `old_limit_draw_num` int(11) NULL DEFAULT 0 COMMENT '原始限制绘图数量',
  `limit_train_times` int(11) NULL DEFAULT NULL COMMENT '限制训练次数',
  `limit_draw_num` int(11) NULL DEFAULT NULL COMMENT '限制绘图数量',
  `use_train_times` int(11) NULL DEFAULT 0 COMMENT '已使用训练次数',
  `use_draw_num` int(11) NULL DEFAULT 0 COMMENT '已使用绘图数量',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `end_time`(`end_time`) USING BTREE,
  INDEX `status`(`status`) USING BTREE,
  INDEX `user_id`(`user_id`, `status`, `member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户会员关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_notifications
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_notifications`;
CREATE TABLE `sys_user_notifications`  (
  `id` bigint(20) NOT NULL COMMENT '数据ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `template_id` bigint(20) NULL DEFAULT NULL COMMENT '通知模板ID',
  `wx_open_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联微信openId',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渲染后的通知标题',
  `msg_content` longblob NULL COMMENT '渲染后的通知内容',
  `mp_msg_content` json NULL COMMENT '渲染后的公众号通知内容',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `notification_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知类型',
  `send_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '发送状态[0-否,1-是]',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '查看状态[0-否,1-是]',
  `read_time` datetime NULL DEFAULT NULL COMMENT '查看时间',
  `other_params` json NULL COMMENT '关联其他参数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`, `read_status`, `send_time`) USING BTREE,
  INDEX `wx_open_id`(`wx_open_id`, `send_status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户通知关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_tag
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_tag`;
CREATE TABLE `sys_user_tag`  (
  `tag_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID，主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID，关联用户',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称，如：厂家、饰品专长',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签说明/描述',
  `biz_type` tinyint(3) UNSIGNED NOT NULL DEFAULT 3 COMMENT '身份标签为0厂商和设计师，1设计师，2普通用户和业务标签3，身份标签由系统分配',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序值，用于前端展示顺序',
  `tag_level` tinyint(3) UNSIGNED NOT NULL DEFAULT 1 COMMENT '标签等级/重要性：1=普通，2=重要，3=核心',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0=未删除，1=已删除',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_id`, `tag_name`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1989588426012471299 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户自定义标签表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
