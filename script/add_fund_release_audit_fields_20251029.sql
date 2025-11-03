-- =====================================================
-- 添加众筹项目资金释放审核相关字段
-- 执行时间：2025-10-29
-- 说明：为sd_crowdfunding_project表添加资金释放审核功能所需的字段
-- =====================================================

-- 添加资金释放审核状态字段
ALTER TABLE `sd_crowdfunding_project` 
ADD COLUMN `fund_release_audit_status` tinyint NULL DEFAULT NULL COMMENT '资金释放审核状态：1=待审核，2=审核通过，3=审核拒绝' AFTER `escrow_status`;

-- 添加审核备注字段
ALTER TABLE `sd_crowdfunding_project` 
ADD COLUMN `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注' AFTER `fund_release_audit_status`;

-- 添加审核人ID字段
ALTER TABLE `sd_crowdfunding_project` 
ADD COLUMN `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID' AFTER `audit_remark`;

-- 添加审核时间字段
ALTER TABLE `sd_crowdfunding_project` 
ADD COLUMN `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间' AFTER `audit_user_id`;

-- 添加审核状态索引（用于查询待审核的项目）
CREATE INDEX `idx_fund_release_audit_status` ON `sd_crowdfunding_project` (`fund_release_audit_status`);

