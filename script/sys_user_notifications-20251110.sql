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

 Date: 10/11/2025 22:40:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user_notifications
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_notifications`;
CREATE TABLE `sys_user_notifications`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `template_id` bigint NULL DEFAULT NULL COMMENT '通知模板ID',
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
  INDEX `user_id`(`user_id` ASC, `read_status` ASC, `send_time` ASC) USING BTREE,
  INDEX `wx_open_id`(`wx_open_id` ASC, `send_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户通知关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
