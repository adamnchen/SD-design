/*
 Navicat Premium Dump SQL

 Create: sd_proofing_invitation_candidates (no foreign keys)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_proofing_invitation_candidates
-- ----------------------------
DROP TABLE IF EXISTS `sd_proofing_invitation_candidates`;
CREATE TABLE `sd_proofing_invitation_candidates` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '候选记录ID',
  `invitation_id` bigint UNSIGNED NOT NULL COMMENT '主邀约ID（逻辑关联，无外键）',
  `invitee_user_id` bigint UNSIGNED NOT NULL COMMENT '被邀约厂家用户ID',
  `quoted_price` decimal(10,2) NULL DEFAULT NULL COMMENT '厂家报价(元)',
  `quoted_period_days` int UNSIGNED NULL DEFAULT NULL COMMENT '预计打样周期(天)',
  `is_quote_batch_plan` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否提供批量方案(0/1)',
  `tiered_pricing` json NULL COMMENT '阶梯价格(JSON数组)',
  `profit_share_ratio` decimal(5,2) NULL DEFAULT NULL COMMENT '利润分成比例(%)',
  `quote_submit_at` timestamp NULL DEFAULT NULL COMMENT '报价提交时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '候选状态(0:待处理,1:已接受,2:已拒绝,4:已关闭)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_invitation_invitee` (`invitation_id`,`invitee_user_id`) USING BTREE,
  KEY `idx_invitation_id_status` (`invitation_id`,`status`) USING BTREE,
  KEY `idx_invitee_user_id` (`invitee_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邀约候选厂家报价表';

SET FOREIGN_KEY_CHECKS = 1;


