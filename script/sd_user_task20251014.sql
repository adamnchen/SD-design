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

 Date: 14/10/2025 08:58:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_user_task
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_task`;
CREATE TABLE `sd_user_task`  (
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'WEBUI' COMMENT '任务类型[WEBUI,COMFYUI]',
  `status` int NULL DEFAULT NULL COMMENT '执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]',
  `flow` json NULL COMMENT 'comfy工作流',
  `category` int NULL DEFAULT 0 COMMENT '分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图]',
  `prompt_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy内部任务ID',
  `prompt` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy提示词(英)',
  `prompt_zh` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy提示词(中)',
  `node_id` bigint NULL DEFAULT NULL COMMENT 'comfy任务执行的节点ID',
  `init_img_list` json NULL COMMENT '参考图片地址数组',
  `belong_user_id` bigint NULL DEFAULT NULL COMMENT '任务归属人ID',
  `belong_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务归属人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '任务创建时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '任务开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '任务结束时间',
  `reason` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `is_redraw` int NULL DEFAULT 0 COMMENT '是否局部重绘(仅在图生图启用:0-否,1-是)',
  `upd_time` datetime NULL DEFAULT NULL COMMENT '任务更新时间',
  PRIMARY KEY (`task_id`) USING BTREE,
  UNIQUE INDEX `task_id`(`task_id` ASC) USING BTREE,
  UNIQUE INDEX `prompt_id`(`prompt_id` ASC) USING BTREE,
  INDEX `status`(`status` ASC, `belong_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户任务执行记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
