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

 Date: 08/10/2025 09:14:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user_tag
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_tag`;
CREATE TABLE `sys_user_tag`  (
  `tag_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID，主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID，关联用户',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称，如：厂家、饰品专长',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签说明/描述',
  `biz_type` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '身份标签为0厂商和设计师，1设计师，2普通用户和业务标签3，身份标签由系统分配',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值，用于前端展示顺序',
  `tag_level` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '标签等级/重要性：1=普通，2=重要，3=核心',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0=未删除，1=已删除',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_id` ASC, `tag_name` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1972559470144266243 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户自定义标签表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
