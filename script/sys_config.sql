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

 Date: 16/07/2025 22:48:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` bigint(20) NOT NULL COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'false', 'N', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-14 21:27:03', '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '第三方用户-账号初始密码', 'third.user.initPassword', '123456', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (7, '第三方用户-账号有效天数', 'third.user.validDay', '3', 'Y', 'admin', '2024-03-03 00:45:30', 'sutran', '2024-10-24 00:11:40', '账号有效期 3天');
INSERT INTO `sys_config` VALUES (8, '第三方用户-模型训练次数', 'third.user.trainTimes', '3', 'Y', 'admin', '2024-03-03 00:45:30', 'sutran', '2024-11-07 14:42:06', '账号模型默认可训练次数 3次');
INSERT INTO `sys_config` VALUES (9, '第三方用户-绘图图片数量', 'third.user.drawNum', '300', 'Y', 'admin', '2024-03-03 00:45:30', 'sutran', '2024-11-07 14:42:17', '账号默认可绘图图片数量 300张');
INSERT INTO `sys_config` VALUES (11, 'OSS预览列表资源开关', 'sys.oss.previewListResource', 'true', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, 'true:开启, false:关闭');
INSERT INTO `sys_config` VALUES (12, '新用户判定时间规则', 'sys.user.isNewUser', '30', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '时间规则(单位天)');
INSERT INTO `sys_config` VALUES (13, '自行注册用户-绘图图片数量', 'registry.user.drawNum', '10', 'Y', 'admin', '2024-03-03 00:45:30', '', NULL, '自行注册默认可绘图图片数量 10张');

SET FOREIGN_KEY_CHECKS = 1;
