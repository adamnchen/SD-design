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

 Date: 13/11/2025 20:36:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1988948055431495683 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '众筹项目主表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
