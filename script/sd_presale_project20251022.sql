/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : sutran-sd-v1

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 22/10/2025 17:50:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_presale_project
-- ----------------------------
DROP TABLE IF EXISTS `sd_presale_project`;
CREATE TABLE `sd_presale_project`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预售项目ID',
  `project_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目编号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目详细描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目图片列表(JSON格式)',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目视频URL',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目标签，逗号分隔',
  `creator_user_id` bigint NOT NULL COMMENT '发起人用户ID',
  `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起人姓名',
  `creator_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起人头像',
  `manufacturer_user_id` bigint NULL DEFAULT NULL COMMENT '厂家用户ID',
  `manufacturer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂家姓名',
  `manufacturer_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '厂家头像',
  `proofing_invitation_id` bigint NULL DEFAULT NULL COMMENT '关联的打样邀约ID',
  `base_price` decimal(15, 2) NOT NULL COMMENT '基础单价（最低阶梯价格）',
  `tiered_pricing` json NULL COMMENT '阶梯价格配置(JSON数组: [{\"unitPrice\":100,\"node\":20}])',
  `validity_days` int NOT NULL DEFAULT 30 COMMENT '有效期天数（从创建时间开始计算）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '项目状态：1=销售中，2=暂停销售，3=已下架，4=已取消',
  `production_status` tinyint NULL DEFAULT 0 COMMENT '生产状态：0=未开始，1=进行中，2=已完成',
  `delivery_status` tinyint NULL DEFAULT 0 COMMENT '发货状态：0=未开始，1=进行中，2=已完成',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` int NULL DEFAULT 0 COMMENT '收藏次数',
  `share_count` int NULL DEFAULT 0 COMMENT '分享次数',
  `manufacturer_photos` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '厂家上传的实物照片（JSON格式，多张图片）',
  `manufacturer_upload_time` datetime NULL DEFAULT NULL COMMENT '厂家上传照片时间',
  `total_quantities` int NULL DEFAULT NULL COMMENT '销售数量',
  `total_sales_amount` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '累计销售金额',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_no`(`project_no` ASC) USING BTREE,
  INDEX `idx_creator_user_id`(`creator_user_id` ASC) USING BTREE,
  INDEX `idx_manufacturer_user_id`(`manufacturer_user_id` ASC) USING BTREE,
  INDEX `idx_proofing_invitation_id`(`proofing_invitation_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_validity_days`(`validity_days` ASC) USING BTREE,
  INDEX `idx_production_status`(`production_status` ASC) USING BTREE,
  INDEX `idx_delivery_status`(`delivery_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1980931749156839426 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '批量订单项目主表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
