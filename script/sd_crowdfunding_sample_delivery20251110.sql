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

 Date: 10/11/2025 17:43:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_crowdfunding_sample_delivery
-- ----------------------------
DROP TABLE IF EXISTS `sd_crowdfunding_sample_delivery`;
CREATE TABLE `sd_crowdfunding_sample_delivery`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '众筹样品发货记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
