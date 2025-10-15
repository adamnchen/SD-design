/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.3.7
 Source Server Type    : MySQL
 Source Server Version : 80023 (8.0.23)
 Source Host           : 192.168.3.7:3306
 Source Schema         : sutran-sd-v1

 Target Server Type    : MySQL
 Target Server Version : 80023 (8.0.23)
 File Encoding         : 65001

 Date: 15/10/2025 11:06:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_user_model
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model`;
CREATE TABLE `sd_user_model`  (
  `id` bigint NOT NULL COMMENT '模型ID',
  `classify_id` bigint NULL DEFAULT 1 COMMENT '模型分类ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '关联训练任务ID',
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
  `type` int NULL DEFAULT NULL COMMENT '模型归属类型[0-系统,1-个人]',
  `is_open` int NULL DEFAULT NULL COMMENT '模型是否公开[0-否,1-是]',
  `belong_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型归属人ID',
  `publish_status` int NULL DEFAULT 0 COMMENT '发布状态[0-否,1-是]',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '模型创建时间',
  `is_user_del` int NULL DEFAULT 0 COMMENT '用户是否已删除该模型[0-否,1-是]',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SDXL' COMMENT '模型类型[SDXL,FLUX]',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `hash`(`hash` ASC) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id` ASC) USING BTREE,
  INDEX `classify_id`(`classify_id` ASC) USING BTREE,
  INDEX `publish_status`(`publish_status` ASC) USING BTREE,
  INDEX `idx_type_open`(`type` ASC, `is_open` ASC, `belong_user_id` ASC) USING BTREE,
  INDEX `task_id`(`task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
