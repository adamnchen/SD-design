/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.3.7
 Source Server Type    : MySQL
 Source Server Version : 80023 (8.0.23)
 Source Host           : 192.168.3.7:3306
 Source Schema         : sutran-sd-v1

 Target Server Type    : MySQL
 Target Server Version : 80023 (8.0.23)
 File Encoding         : 65001

 Date: 14/10/2025 08:58:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sd_draw_node
-- ----------------------------
DROP TABLE IF EXISTS `sd_draw_node`;
CREATE TABLE `sd_draw_node`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点编码',
  `type` tinyint NULL DEFAULT 0 COMMENT '节点类型[0-绘图,1-训练]',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点基础URL',
  `is_active` tinyint NULL DEFAULT 0 COMMENT '是否激活[0-否,1-是]',
  `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点地区',
  `weight` bigint NULL DEFAULT 10 COMMENT '基础权重(0-100)',
  `max_concurrent_tasks` int NULL DEFAULT 20 COMMENT '最大任务并发数',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 节点配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_draw_node
-- ----------------------------
INSERT INTO `sd_draw_node` VALUES (1, '仙宫云-Comfyui', 'comfyui-1', 0, 'https://welwoaopymi4eiqp-8188.container.x-gpu.com', 1, '广州', 10, 10, NULL, '2025-07-06 10:18:11', NULL, NULL);
INSERT INTO `sd_draw_node` VALUES (2, '仙宫云-FluxGym', 'fluxgym-1', 1, 'https://8z0w9ml4swf3rh12-7860.container.x-gpu.com', 1, '广州', 10, 1, NULL, '2025-10-09 20:38:46', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
