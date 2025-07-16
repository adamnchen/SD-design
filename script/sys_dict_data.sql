/*
 Navicat Premium Dump SQL

 Source Server         : SD-4090
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : 106.15.90.128:9101
 Source Schema         : sutran-sd-v1

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 16/07/2025 22:48:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(20) NOT NULL COMMENT '字典编码',
  `dict_sort` int(11) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE,
  INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (19, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (20, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (21, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (22, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (23, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (24, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (25, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (26, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (27, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (28, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (29, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (30, 1, '系统注册', '1', 'sys_user_channel', NULL, 'default', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (31, 2, 'ChinaGoods用户', 'f6a10077849f442797e1a0353df55578', 'sys_user_channel', NULL, 'default', 'N', '0', 'admin', '2024-03-03 00:45:30', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (32, 3, '自行注册', '3', 'sys_user_channel', NULL, 'default', 'N', '0', 'admin', NULL, '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1769556035576590337, 0, '麻黄素', '麻黄素', 'sys_weijin_code', NULL, 'default', 'N', '0', 'admin', '2024-03-18 10:46:50', 'admin', '2024-03-18 10:46:50', NULL);
INSERT INTO `sys_dict_data` VALUES (1769556077347663874, 0, '反人类', '反人类', 'sys_weijin_code', NULL, 'default', 'N', '0', 'admin', '2024-03-18 10:47:00', 'admin', '2024-03-18 10:47:00', NULL);
INSERT INTO `sys_dict_data` VALUES (1769556114307870722, 0, '脑子不好', '脑子不好', 'sys_weijin_code', NULL, 'default', 'N', '0', 'admin', '2024-03-18 10:47:09', 'admin', '2024-03-18 10:47:09', NULL);
INSERT INTO `sys_dict_data` VALUES (1769556176974966786, 0, '赌博', '赌博', 'sys_weijin_code', NULL, 'default', 'N', '0', 'admin', '2024-03-18 10:47:24', 'admin', '2024-03-18 10:47:24', NULL);
INSERT INTO `sys_dict_data` VALUES (1769556401395396610, 0, 'SB', 'SB', 'sys_weijin_code', NULL, 'default', 'N', '0', 'admin', '2024-03-18 10:48:18', 'admin', '2024-03-18 10:48:18', NULL);
INSERT INTO `sys_dict_data` VALUES (1776174621919383554, 0, '排队等待中', '0', 'model_t2i', NULL, 'warning', 'N', '0', 'admin', '2024-04-05 17:06:44', 'admin', '2024-04-07 20:04:38', NULL);
INSERT INTO `sys_dict_data` VALUES (1776174702718455809, 0, '进行中', '1', 'model_t2i', NULL, 'primary', 'N', '0', 'admin', '2024-04-05 17:07:04', 'admin', '2024-04-05 17:09:27', NULL);
INSERT INTO `sys_dict_data` VALUES (1776174773862240257, 0, '成功', '2', 'model_t2i', NULL, 'success', 'N', '0', 'admin', '2024-04-05 17:07:20', 'admin', '2024-04-07 20:04:05', NULL);
INSERT INTO `sys_dict_data` VALUES (1776174830774751234, 0, '失败', '3', 'model_t2i', NULL, 'danger', 'N', '0', 'admin', '2024-04-05 17:07:34', 'admin', '2024-04-07 20:04:11', NULL);
INSERT INTO `sys_dict_data` VALUES (1779891538513952770, 0, '1', 'o04IBALpXq0gvM1xwXBxTZBVgIoY&tbkt=C1', 'sys_wechat_openid', NULL, 'default', 'N', '0', 'admin', '2024-04-15 23:16:26', 'admin', '2024-04-15 23:16:26', NULL);

SET FOREIGN_KEY_CHECKS = 1;
