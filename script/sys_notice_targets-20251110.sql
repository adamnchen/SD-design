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

 Date: 10/11/2025 17:46:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_notice_targets
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_targets`;
CREATE TABLE `sys_notice_targets`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `notice_id` bigint NULL DEFAULT NULL COMMENT '公告ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `send_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '发送状态[0-未推送,1-已推送]',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '查看状态[0-否,1-是]',
  `read_time` datetime NULL DEFAULT NULL COMMENT '查看时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `notice_id`(`notice_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC, `read_status` ASC) USING BTREE,
  INDEX `notice_id_2`(`notice_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告目标' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_targets
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
