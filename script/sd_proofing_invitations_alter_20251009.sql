SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `sd_proofing_invitations`
  ADD COLUMN `selected_invitee_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '最终选中的厂家用户ID' AFTER `profit_share_ratio`,
  ADD COLUMN `selected_at` timestamp NULL DEFAULT NULL COMMENT '最终选择时间' AFTER `selected_invitee_user_id`;

SET FOREIGN_KEY_CHECKS = 1;


