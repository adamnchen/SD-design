/*
 为 sd_user_model_file 表添加公开时间字段

 Date: 2025-11-13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 添加公开时间字段
-- ----------------------------
ALTER TABLE `sd_user_model_file` 
ADD COLUMN `public_time` datetime NULL DEFAULT NULL COMMENT '公开时间' AFTER `is_public`;

SET FOREIGN_KEY_CHECKS = 1;

