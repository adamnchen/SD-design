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

 Date: 01/11/2025
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_favorite`;
CREATE TABLE `sd_user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（收藏者）',
  `favorite_type` tinyint NOT NULL COMMENT '收藏类型：1=模型，2=作品',
  `target_id` bigint NOT NULL COMMENT '收藏对象ID（模型ID或作品ID）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_type_target`(`user_id`, `favorite_type`, `target_id`) USING BTREE COMMENT '唯一索引：防止同一用户重复收藏同一对象',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引：查询用户的所有收藏',
  INDEX `idx_target_id`(`target_id` ASC, `favorite_type` ASC) USING BTREE COMMENT '对象ID索引：查询某对象的收藏数',
  INDEX `idx_favorite_type`(`favorite_type` ASC) USING BTREE COMMENT '收藏类型索引：按类型查询',
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE COMMENT '创建时间索引：按时间排序'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收藏表（支持收藏模型和作品）' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

