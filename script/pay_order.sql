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

 Date: 12/10/2025 22:04:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order`  (
  `id` bigint NOT NULL COMMENT '支付记录ID',
  `out_trade_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统订单号',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝应用编号',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝交易流水号',
  `gmt_payment` datetime NULL DEFAULT NULL COMMENT '支付宝支付时间',
  `subject` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务订单标题名称',
  `body` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '务订单详情内容',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `refund_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '退款金额',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款单号',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '支付状态[0-待支付,1-已支付,2-支付失败,3-已关闭,4-已退款]',
  `channel_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付渠道[WX_PAY-微信支付,ALI_PAY-支付宝支付]',
  `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型[SD_MEMBER-SD会员,CROWDFUNDING_SUPPORT-众筹支持]',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '订单过期时间',
  `notify_time` datetime NULL DEFAULT NULL COMMENT '支付宝异步通知时间',
  `notify_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '支付宝异步通知内容',
  `qr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付二维码地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `out_trade_no`(`out_trade_no` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付订单记录' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
