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

 Date: 15/10/2025 17:38:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_proofing_invitations
-- ----------------------------
DROP TABLE IF EXISTS `sd_proofing_invitations`;
CREATE TABLE `sd_proofing_invitations`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '邀请单的唯一ID，主键',
  `work_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发起邀约的作品ID，关联作品表',
  `product_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '打样产品的标题',
  `product_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '邀约作品的详细描述（如材质、尺寸、风格等）',
  `model_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型来源或版本号（例如：v2.3）',
  `inviter_user_id` bigint UNSIGNED NOT NULL COMMENT '邀约人用户ID (创建人)',
  `invitee_user_id` bigint UNSIGNED NOT NULL COMMENT '被邀约用户ID',
  `cooperation_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '其他合作内容详情',
  `keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发起邀约时填写的关键词/标签',
  `is_batch_production` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要批量生产（0:否, 1:是）',
  `proofing_quantity` int UNSIGNED NOT NULL COMMENT '期望的打样数量 (件)',
  `delivery_limit_hours` int UNSIGNED NOT NULL COMMENT '发起方要求的预计交付时限 (小时)',
  `cancel_time_limit` int NULL DEFAULT NULL COMMENT '无人应答自动取消时限(1,2,3,一天两天与三天)',
  `quoted_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '被邀约方提交的报价金额 (元)',
  `quoted_period_days` int UNSIGNED NULL DEFAULT NULL COMMENT '被邀约方提交的预计打样周期 (天)',
  `is_quote_batch_plan` tinyint(1) NOT NULL DEFAULT 0 COMMENT '报价时是否提供了批量生产方案 (0:否, 1:是)',
  `quote_submit_at` timestamp NULL DEFAULT NULL COMMENT '报价提交时间',
  `tiered_pricing` json NULL COMMENT '阶梯价格配置(JSON数组: [20, 30, 40] 对应 0-20, 20-30, 30-40 区间)',
  `profit_share_ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT '利润分成比例(%)，例如 15.50 表示 15.5%',
  `selected_invitee_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '最终选中的厂家用户ID',
  `selected_at` timestamp NULL DEFAULT NULL COMMENT '最终选择时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '邀约状态 (0: 待处理, 1: 已接受, 2: 已拒绝, 3: 已取消，4.待回应)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 (邀约时间)',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `draw_number` int NOT NULL COMMENT '打样样品参与抽奖的数量分配给众筹用户,最少一个',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_id_status`(`work_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_inviter_user_id`(`inviter_user_id` ASC) USING BTREE,
  INDEX `idx_invitee_user_id`(`invitee_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1978393994602344451 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作品打样合作邀请记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
