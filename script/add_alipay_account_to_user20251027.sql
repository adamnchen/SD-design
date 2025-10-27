-- ==========================================
-- 为 sys_user 表添加支付宝账号字段
-- 用途：记录用户的支付宝账号，用于资金转账
-- 创建时间：2025-10-27
-- ==========================================

SET NAMES utf8mb4;

-- ----------------------------
-- 添加支付宝账号字段
-- ----------------------------
ALTER TABLE `sys_user` 
ADD COLUMN `alipay_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝账号（手机号或邮箱）' AFTER `phonenumber`,
ADD COLUMN `alipay_real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝实名姓名' AFTER `alipay_account`,
ADD COLUMN `alipay_bind_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '支付宝绑定状态（0-未绑定，1-已绑定）' AFTER `alipay_real_name`;



