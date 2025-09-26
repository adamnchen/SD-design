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

 Date: 26/09/2025 12:29:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user_address_area
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_address_area`;
CREATE TABLE `sys_user_address_area`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '唯一主键ID',
  `area_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行政区划代码，如: 320102000',
  `area_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域名称，如: 玄武区',
  `parent_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '父级区域代码，用于级联查询',
  `area_level` tinyint UNSIGNED NOT NULL COMMENT '区域级别: 1=省, 2=市, 3=区/县, 4=街道/乡镇',
  `zip_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮政编码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_area_code`(`area_code` ASC) USING BTREE,
  INDEX `idx_parent_code`(`parent_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45381 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户地址行政区划表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
