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

 Date: 13/10/2025 15:25:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_crowdfunding_sample_delivery
-- ----------------------------
DROP TABLE IF EXISTS `sd_crowdfunding_sample_delivery`;
CREATE TABLE `sd_crowdfunding_sample_delivery`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `crowdfunding_project_id` bigint NOT NULL COMMENT '众筹项目ID',
  `proofing_invitation_id` bigint NOT NULL COMMENT '关联邀约记录ID',
  `sample_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '样品图片地址',
  `recipient_user_id` bigint NOT NULL COMMENT '收货人用户ID',
  `recipient_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `recipient_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `delivery_address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '收货人地址',
  `tracking_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递单号',
  `delivery_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递公司',
  `delivery_status` tinyint NULL DEFAULT 1 COMMENT '状态：1=待发货，2=已发货',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sender_user_id` bigint NOT NULL COMMENT '发货人用户ID',
  `sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发货人姓名',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认收货时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `order_status` tinyint NULL DEFAULT NULL COMMENT '状态：1=待处理 2=已完成',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_crowdfunding_project_id`(`crowdfunding_project_id` ASC) USING BTREE,
  INDEX `idx_recipient_user_id`(`recipient_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`delivery_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '众筹样品发货记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
