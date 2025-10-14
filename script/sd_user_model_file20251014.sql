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

 Date: 14/10/2025 08:59:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_user_model_file
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_file`;
CREATE TABLE `sd_user_model_file`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `category` int NULL DEFAULT 0 COMMENT '分类[0-文生图，1-图生图]',
  `is_redraw` int NULL DEFAULT 0 COMMENT '是否局部重绘[0-否,1-是]',
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
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础大模型名称',
  `lora_model_id` bigint NULL DEFAULT NULL COMMENT 'lora模型ID',
  `lora_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型名称',
  `lora_title_zh` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型名称(中文)',
  `lora_info` json NULL COMMENT 'lora模型信息数组',
  `belong_user_id` bigint NULL DEFAULT NULL COMMENT '文件归属人ID',
  `belong_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件归属人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `task_id`(`task_id` ASC) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id` ASC) USING BTREE,
  INDEX `crt_time`(`crt_time` ASC) USING BTREE,
  INDEX `lora_model_id`(`lora_model_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户生图文件数据记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
