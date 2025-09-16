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

 Date: 16/09/2025 14:48:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_msg_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_msg_history`;
CREATE TABLE `ai_msg_history`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `session_id` bigint NULL DEFAULT NULL COMMENT '会话ID',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色[system,user,assistant]',
  `type` int NULL DEFAULT 0 COMMENT '类型[0-问题,1-答案]',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `file_urls` json NULL COMMENT '文件地址集合',
  `crt_user_id` bigint NULL DEFAULT NULL COMMENT '归属人ID',
  `crt_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `session_id`(`session_id` ASC, `role` ASC, `crt_user_id` ASC, `crt_time` ASC) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id` ASC) USING BTREE,
  CONSTRAINT `ai_msg_history_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `ai_msg_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI对话 || 历史消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_msg_history
-- ----------------------------
INSERT INTO `ai_msg_history` VALUES (1871486387082838018, 1, 'system', 0, '你是一个AI助手', NULL, 1838096394063040512, '18852862861', '2024-12-24 17:21:20');
INSERT INTO `ai_msg_history` VALUES (1871486387082838019, 1, 'user', 1, '这个文件内容是什么?', '[{\"fileUrl\": \"http://106.15.90.128:9502/img/sutran/2024/12/24/dc7f02b31fe44a99b5595683ad81dfde.pdf\", \"fileType\": \"text\", \"modelFileId\": \"file-fe-LsOd1vNDnyhEzeDs1iZbhz4y\"}]', 1838096394063040512, '18852862861', '2024-12-24 17:21:20');
INSERT INTO `ai_msg_history` VALUES (1871486438530170881, 1, 'assistant', 2, '这个文件的内容是关于Unity3d可视化编程的基础教程,特别是针对2D游戏模块的开发。它详细介绍了2D精灵动画控制的各种方面,包括:\n\n1. 序列图谱的处理方法\n2. 动画的制作流程 (如在Animator中制作、调整动画节奏等)\n3. 角色动画状态的管理\n4. 添加物理特性 (如重力、碰撞器等)\n5. 使用Playmaker状态机制控制动画和角色行为\n6. 实现角色的移动、跳跃、攻击等动作\n7. 创建和控制不同类型的NPC (如老鹰、青蛙等)\n8. 制作游戏道具 (如钻石) 和其动画\n9. 开发玩家的生命值系统和UI显示\n10. 增加游戏音效\n11. 制作启动界面\n12. 游戏的最终完善和测试\n\n文件还提到了Bolt插件的安装与使用,以及游戏开发过程中的一些通用技巧和注意事项。总的来说,这是一份非常全面的Unity3d 2D游戏开发指南。', NULL, 1838096394063040512, '18852862861', '2024-12-24 17:21:52');
INSERT INTO `ai_msg_history` VALUES (1871487561034973185, 1, 'user', 1, '这个文件内容中哪些是我们能快速掌握的？', NULL, 1838096394063040512, '18852862861', '2024-12-24 17:26:20');
INSERT INTO `ai_msg_history` VALUES (1871487670917349377, 1, 'assistant', 2, '在这个文件内容中，有许多知识点是相对容易上手且能够快速掌握的。以下是一些较为基础且容易学习的部分：\n\n### 1. **基本的2D精灵动画控制**\n   - **序列图谱的处理**：\n     - 模式改为多重\n     - 最大尺寸要高于图片\n     - 使用精灵编辑器\n     - 过滤类型为“点类型”\n   - **动画制作**：\n     - 在Animator中制作\n     - 导入序列图\n     - 改名\n     - 调整动画节奏（Sample采样用30）\n     - 颜色不好可以改format成truecolor\n\n### 2. **角色的基本物理特性**\n   - **添加地面**：\n     - 添加Edge Collider\n     - 修改边界线\n   - **添加角色重力和碰撞器**：\n     - 添加2D刚体\n     - 添加碰撞器（可以组合，以匹配形状）\n     - 圆形碰撞器注意在刚体属性中固定旋转\n\n### 3. **基本的动画状态管理**\n   - **Animator中调用动画**：\n     - 添加Parameters参数\n     - 在每个动画状态上添加参数\n     - 指定条件（大于或小于）\n     - 关闭动画过渡，减少滑动效果\n\n### 4. **使用Playmaker控制角色行为**\n   - **移动状态机**：\n     - 使用左右箭头映射得到水平方向Horizontal的浮点数值\n     - 浮点数值来控制X轴的移动速度\n     - 左右状态里面加Scale，反转方向\n   - **跳跃**：\n     - Get Key Down\n     - Add Force 2D\n     - 摄像机作为子物体跟随\n     - 父子物体注意数据归零\n\n### 5. **简单的游戏道具和敌人**\n   - **创建浮动道具-钻石**：\n     - 建立钻石精灵\n     - 在Animation中制作钻石动画\n     - 添加碰撞器\n   - **创建空中角色-老鹰**：\n     - 建立老鹰精灵\n     - 在Animation中制作老鹰动画\n     - 添加碰撞器\n\n### 6. **基本的游戏音效**\n   - **角色行走声音**：\n     - 水平有速度就播放声音\n     - 开始就不能播放声音\n   - **角色跳起声音**：\n     - 检测键盘\n   - **角色落地声音**：\n     - 检测碰撞\n\n### 7. **简单的UI元素**\n   - **制作血条**：\n     - 在PS中制作白色底色和血条外框\n     - 把血条的外框放入场景\n     - 给血条添加FSM\n     - 定义角色生命值全局变量player health\n\n### 8. **基本的关卡跳转**\n   - **关卡的跳转**：\n     - 场景中新建一个Sprite赋予贴图或者动画\n     - 赋予碰撞器\n     - PM通过检测碰撞来控制跳转\n     - 需要碰撞标签都是player\n\n这些内容涵盖了2D游戏开发的基础，通过逐步实践和练习，你可以快速掌握这些技能，并在此基础上进一步深入学习更复杂的概念和技术。', NULL, 1838096394063040512, '18852862861', '2024-12-24 17:26:46');
INSERT INTO `ai_msg_history` VALUES (1871500043761827841, 1, 'user', 1, '上传的这个文件能在服务器上存储多久？能存储多少个文件？', NULL, 1838096394063040512, '18852862861', '2024-12-24 18:15:56');
INSERT INTO `ai_msg_history` VALUES (1871500103308361730, 1, 'assistant', 2, '关于上传文件在服务器上的存储时间和数量限制，这通常取决于具体的平台或服务提供商。以下是一些常见的限制和说明：\n\n### 存储时间\n1. **临时存储**：许多平台提供临时存储服务，文件可能会在几天到几周内自动删除。例如，某些在线协作工具或临时文件存储服务可能会在7天到30天后删除文件。\n2. **长期存储**：如果你使用的是云存储服务（如AWS S3、Google Cloud Storage、Azure Blob Storage），文件可以长期存储，直到你手动删除它们。\n\n### 存储数量\n1. **有限制的存储**：一些平台可能对每个用户或每个项目有文件数量的限制。例如，某些在线协作工具可能会限制每个项目只能上传100个文件。\n2. **无限制的存储**：如果你使用的是专业的云存储服务，通常没有明确的文件数量限制，但可能会有存储容量的限制。例如，你可能可以存储数百万个文件，但总存储容量不能超过你购买的存储空间。\n\n### 具体平台的限制\n如果你使用的是某个特定的平台或服务，建议查看该平台的官方文档或帮助中心，了解具体的存储时间和数量限制。例如：\n\n- **GitHub**：每个仓库的文件数量和大小都有一定的限制。\n- **Google Drive**：个人账户有存储容量限制，但没有明确的文件数量限制。\n- **Dropbox**：同样有存储容量限制，但没有明确的文件数量限制。\n\n### 如何查询具体限制\n1. **查看官方文档**：大多数平台都会在其官方文档中详细列出存储限制。\n2. **联系客服**：如果你无法找到相关信息，可以联系平台的客服或技术支持团队询问。\n3. **管理面板**：有些平台在管理面板中会显示你的存储使用情况和限制。\n\n如果你能告诉我具体使用的是哪个平台或服务，我可以提供更详细的指导。', NULL, 1838096394063040512, '18852862861', '2024-12-24 18:16:10');
INSERT INTO `ai_msg_history` VALUES (1871500335333064705, 1, 'user', 1, '阿里云百炼', NULL, 1838096394063040512, '18852862861', '2024-12-24 18:17:05');
INSERT INTO `ai_msg_history` VALUES (1871500450651258882, 1, 'assistant', 2, '阿里云百炼（也称为阿里云Link）是一个物联网平台，主要用于设备连接、数据处理和应用开发。不过，你提到的文件存储问题似乎与一般的文件存储服务相关，而不是专门的物联网平台功能。假设你指的是阿里云的文件存储服务，以下是一些关于阿里云文件存储的常见限制和说明：\n\n### 阿里云文件存储服务\n\n#### 1. **对象存储OSS（Object Storage Service）**\n阿里云的对象存储OSS是一种海量、安全、低成本、高可靠的云存储服务。以下是OSS的一些关键特点和限制：\n\n- **存储时间**：\n  - **永久存储**：OSS中的文件可以永久存储，直到你手动删除它们。\n  - **生命周期管理**：你可以设置生命周期规则，自动删除或归档旧文件。\n\n- **存储数量**：\n  - **无文件数量限制**：OSS没有明确的文件数量限制，可以存储数百万甚至更多的文件。\n  - **单个Bucket的文件数量**：单个Bucket可以存储的文件数量也没有明确限制。\n\n- **存储容量**：\n  - **无容量限制**：OSS的存储容量是按需扩展的，没有固定的上限。你只需要支付实际使用的存储量。\n\n- **访问频率**：\n  - **标准存储**：适用于频繁访问的数据。\n  - **低频访问存储**：适用于不经常访问的数据，成本较低。\n  - **归档存储**：适用于很少访问的数据，成本最低，但访问时间较长。\n\n#### 2. **文件存储NAS（Network Attached Storage）**\n阿里云的文件存储NAS是一种支持POSIX协议的共享文件存储服务，适用于需要共享文件存储的应用场景。\n\n- **存储时间**：\n  - **永久存储**：NAS中的文件可以永久存储，直到你手动删除它们。\n\n- **存储数量**：\n  - **无文件数量限制**：NAS没有明确的文件数量限制，可以存储大量的文件。\n\n- **存储容量**：\n  - **可扩展存储**：NAS的存储容量是按需扩展的，可以根据需要增加存储空间。\n\n#### 3. **块存储EBS（Elastic Block Storage）**\n阿里云的块存储EBS是一种提供高性能、低延迟的块级存储服务，适用于需要高性能存储的应用场景。\n\n- **存储时间**：\n  - **与ECS实例绑定**：EBS卷通常与ECS实例绑定，实例删除时可以选择保留或删除EBS卷。\n\n- **存储数量**：\n  - **每个ECS实例的EBS卷数量限制**：每个ECS实例可以挂载多个EBS卷，具体数量取决于实例类型。\n\n- **存储容量**：\n  - **可扩展存储**：EBS卷的存储容量是可扩展的，可以根据需要增加存储空间。\n\n### 查询具体限制\n如果你需要更详细的信息，建议参考阿里云的官方文档或联系阿里云的技术支持：\n\n1. **官方文档**：\n   - [阿里云OSS文档](https://help.aliyun.com/product/31819.html)\n   - [阿里云NAS文档](https://help.aliyun.com/product/57503.html)\n   - [阿里云EBS文档](https://help.aliyun.com/document_detail/25383.html)\n\n2. **技术支持**：\n   - 你可以通过阿里云的在线客服或技术支持渠道获取更详细的信息。\n\n希望这些信息对你有所帮助！如果你有更具体的需求或问题，请随时告诉我。', NULL, 1838096394063040512, '18852862861', '2024-12-24 18:17:33');
INSERT INTO `ai_msg_history` VALUES (1880582004534960129, 1870464542103572482, 'system', 0, '你是图片分析工具人', NULL, 1, 'admin', '2025-01-18 19:43:44');
INSERT INTO `ai_msg_history` VALUES (1880582004564320258, 1870464542103572482, 'user', 1, '这个图片中描述的是什么?', '[{\"fileUrl\": \"http://127.0.0.1:9000/ruitong/2024/11/29/1.png\", \"fileName\": \"tp.png\", \"fileType\": \"image\", \"modelFileId\": \"file-fe-VeO1shDT4z4DZP92NBNtauNa\"}]', 1, 'admin', '2025-01-18 19:43:44');
INSERT INTO `ai_msg_history` VALUES (1880582007466778625, 1870464542103572482, 'assistant', 2, '当前问题暂时无法回答您!', NULL, 1, 'admin', '2025-01-18 19:44:25');

-- ----------------------------
-- Table structure for ai_msg_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_msg_session`;
CREATE TABLE `ai_msg_session`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话名称',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型[text-文本,image-图片]',
  `crt_user_id` bigint NULL DEFAULT NULL COMMENT '归属人',
  `crt_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '归属人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '添加时间',
  `order_num` int NULL DEFAULT NULL COMMENT '排序值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI对话 || 会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_msg_session
-- ----------------------------
INSERT INTO `ai_msg_session` VALUES (1, '工具人', '你是一个AI助手', 'text', 1838096394063040512, '18852862861', '2024-12-21 10:50:52', 1);
INSERT INTO `ai_msg_session` VALUES (1870464542103572482, '测试', '你是图片分析工具人', 'image', 1838096394063040512, '18852862861', '2024-12-21 21:41:13', 1);

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for pay_member
-- ----------------------------
DROP TABLE IF EXISTS `pay_member`;
CREATE TABLE `pay_member`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `level` int NULL DEFAULT 1 COMMENT '会员等级',
  `level_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员权益描述',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `duration` int NULL DEFAULT NULL COMMENT '会员有效期(天)',
  `limit_train_times` int NULL DEFAULT 0 COMMENT '限制训练次数',
  `limit_draw_num` int NULL DEFAULT 0 COMMENT '限制绘图数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付会员配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_member
-- ----------------------------
INSERT INTO `pay_member` VALUES (1, 1, '9.9元/月初级会员', '1、可使用10个训练好的产品模型;2、可生成100张图片;3、可云端存储', 0.01, 30, 10, 100, 1, NULL, NULL, NULL, '2025-08-25 15:36:33');
INSERT INTO `pay_member` VALUES (2, 2, '99元/月黄金会员', '1、可使用10个训练好的产品模型;2、可生成300张图片;3、可云端存储', 0.01, 30, 30, 300, 1, NULL, NULL, NULL, '2025-08-25 15:51:33');

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
  `status` tinyint NULL DEFAULT 0 COMMENT '支付状态[0-待支付,1-已支付,2-支付失败,3-已关闭]',
  `channel_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付渠道[WX_PAY-微信支付,ALI_PAY-支付宝支付]',
  `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型[SD_MEMBER-SD会员]',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付订单记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_order
-- ----------------------------
INSERT INTO `pay_order` VALUES (1959880953714749441, '202508251530051959880953609891840', 1, '测试用户', '2021005185628219', '2025082522001452451445536159', '2025-08-25 15:31:57', '9.9元/月初级会员', '1、可使用10个训练好的产品模型;2、可生成100张图片;3、可云端存储', 0.01, 1, 'ALI_PAY', 'SD_MEMBER', 1, '2025-08-25 15:30:06', '2025-08-25 15:34:37', '2025-08-25 16:00:06', NULL, NULL, 'https://qr.alipay.com/bax06401zl3gs8jwvswp5530');
INSERT INTO `pay_order` VALUES (1959882814182162433, '202508251537291959882814161190912', 1, '测试用户', '2021005185628219', '2025082522001452451444996148', '2025-08-25 15:38:07', '9.9元/月初级会员', '1、可使用10个训练好的产品模型;2、可生成100张图片;3、可云端存储', 0.01, 1, 'ALI_PAY', 'SD_MEMBER', 1, '2025-08-25 15:37:30', '2025-08-25 15:38:09', '2025-08-25 16:07:30', NULL, NULL, 'https://qr.alipay.com/bax04160nzop5mzshymk3054');
INSERT INTO `pay_order` VALUES (1959883061201502210, '202508251538291959883061235056640', 1, '测试用户', '2021005185628219', '2025082522001452451448336590', '2025-08-25 15:38:45', '99元/月黄金会员', '1、可使用10个训练好的产品模型;2、可生成300张图片;3、可云端存储', 0.01, 1, 'ALI_PAY', 'SD_MEMBER', 2, '2025-08-25 15:38:29', '2025-08-25 15:38:46', '2025-08-25 16:08:29', NULL, NULL, 'https://qr.alipay.com/bax01914yojzdwxdv0fb00e2');
INSERT INTO `pay_order` VALUES (1959900039366602754, '202508251645561959900039261745152', 1, '测试用户', '2021005185628219', '2025082522001452451448492402', '2025-08-25 16:51:20', '99元/月黄金会员', '1、可使用10个训练好的产品模型;2、可生成300张图片;3、可云端存储', 0.01, 1, 'ALI_PAY', 'SD_MEMBER', 2, '2025-08-25 16:45:56', '2025-08-25 16:51:22', '2025-08-25 17:15:56', NULL, NULL, 'https://qr.alipay.com/bax057779px7i4uwb3zw554b');
INSERT INTO `pay_order` VALUES (1959922114269589506, '202508251813391959922114223452160', 1, '测试用户', '2021005185628219', '2025082522001452451449484610', '2025-08-25 18:13:58', '99元/月黄金会员', '1、可使用10个训练好的产品模型;2、可生成300张图片;3、可云端存储', 0.01, 1, 'ALI_PAY', 'SD_MEMBER', 2, '2025-08-25 18:13:40', '2025-08-25 18:14:00', '2025-08-25 18:43:40', NULL, NULL, 'https://qr.alipay.com/bax04490p9lrfiufu7wa00dc');
INSERT INTO `pay_order` VALUES (1959923659946070017, '202508251819481959923659895738368', 1, '测试用户', '2021005185628219', '2025082522001452451447135489', '2025-08-25 18:20:02', '99元/月黄金会员', '1、可使用10个训练好的产品模型;2、可生成300张图片;3、可云端存储', 0.01, 1, 'ALI_PAY', 'SD_MEMBER', 2, '2025-08-25 18:19:48', '2025-08-25 18:20:03', '2025-08-25 18:49:48', NULL, NULL, 'https://qr.alipay.com/bax05793euwcw9kxeqmd25a6');

-- ----------------------------
-- Table structure for sd_channel_data
-- ----------------------------
DROP TABLE IF EXISTS `sd_channel_data`;
CREATE TABLE `sd_channel_data`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `is_send` tinyint NULL DEFAULT 0 COMMENT '是否发送成功[0-否,1-是]',
  `channel_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '第三方渠道用户ID',
  `api_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求API地址',
  `api_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求API签名',
  `pic_url_list` json NULL COMMENT '发送图片数组',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `retry_times` int NULL DEFAULT NULL COMMENT '重试次数',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求错误原因',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `channel_user_id`(`channel_user_id` ASC) USING BTREE,
  INDEX `is_send`(`is_send` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 渠道消息推送记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_channel_data
-- ----------------------------

-- ----------------------------
-- Table structure for sd_common_config
-- ----------------------------
DROP TABLE IF EXISTS `sd_common_config`;
CREATE TABLE `sd_common_config`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `pre_img_min_num` int NULL DEFAULT NULL COMMENT '预处理提交图片最小数量',
  `pre_img_max_num` int NULL DEFAULT NULL COMMENT '预处理提交图片最大数量',
  `channel_send_api_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道推送API地址',
  `channel_send_api_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道推送API签名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 通用设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_common_config
-- ----------------------------
INSERT INTO `sd_common_config` VALUES (1, 7, 15, NULL, NULL);

-- ----------------------------
-- Table structure for sd_draw_node
-- ----------------------------
DROP TABLE IF EXISTS `sd_draw_node`;
CREATE TABLE `sd_draw_node`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点编码',
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
INSERT INTO `sd_draw_node` VALUES (1, '本地SD-WEBUI', 'local', 'http://127.0.0.1:8188', 1, '广州', 10, 10, NULL, '2025-07-06 10:18:11', NULL, NULL);

-- ----------------------------
-- Table structure for sd_flow
-- ----------------------------
DROP TABLE IF EXISTS `sd_flow`;
CREATE TABLE `sd_flow`  (
  `id` bigint NOT NULL COMMENT '工作流ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工作流名称',
  `draw_num` int NULL DEFAULT 3 COMMENT '工作流生图数量',
  `flow` json NULL COMMENT '工作流',
  `is_fixed` tinyint NULL DEFAULT 0 COMMENT '是否固定[0-否,1-是]',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `belong_user_id` bigint NULL DEFAULT NULL COMMENT '归属人ID',
  `belong_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '归属人名称',
  `is_open` tinyint NULL DEFAULT 0 COMMENT '是否开放[0-否,1-是]',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SDXL' COMMENT '模型类型[SDXL,FLUX]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `model_type`(`model_type` ASC) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 工作流' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_flow
-- ----------------------------
INSERT INTO `sd_flow` VALUES (1, 'SDXL', 1, '{\"2\": {\"_meta\": {\"title\": \"K采样器\"}, \"inputs\": {\"cfg\": 8, \"seed\": 714135418367822, \"model\": [\"12\", 0], \"steps\": 30, \"denoise\": 1, \"negative\": [\"5\", 0], \"positive\": [\"4\", 0], \"scheduler\": \"karras\", \"latent_image\": [\"6\", 0], \"sampler_name\": \"dpmpp_3m_sde\"}, \"class_type\": \"KSampler\"}, \"3\": {\"_meta\": {\"title\": \"Checkpoint加载器(简易)\"}, \"inputs\": {\"+\": null, \"ckpt_name\": \"sd_xl_base_1.0_0.9vae.safetensors\"}, \"class_type\": \"CheckpointLoaderSimple\"}, \"4\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"12\", 1], \"text\": \"{{prompt}}\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"5\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"12\", 1], \"text\": \"\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"6\": {\"_meta\": {\"title\": \"空Latent\"}, \"inputs\": {\"width\": 1024, \"height\": 1024, \"batch_size\": \"{{batch_size}}\"}, \"class_type\": \"EmptyLatentImage\"}, \"7\": {\"_meta\": {\"title\": \"VAE解码\"}, \"inputs\": {\"vae\": [\"3\", 2], \"samples\": [\"2\", 0]}, \"class_type\": \"VAEDecode\"}, \"8\": {\"_meta\": {\"title\": \"保存图像\"}, \"inputs\": {\"images\": [\"7\", 0], \"filename_prefix\": \"ComfyUI\"}, \"class_type\": \"SaveImage\"}, \"9\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"7\", 0]}, \"class_type\": \"PreviewImage\"}, \"12\": {\"_meta\": {\"title\": \"LoRA加载器\"}, \"inputs\": {\"+\": null, \"clip\": [\"3\", 1], \"model\": [\"3\", 0], \"lora_name\": \"{{lora_model}}.safetensors\", \"strength_clip\": 1, \"strength_model\": \"{{lora_model_strength}}\"}, \"class_type\": \"LoraLoader\"}}', 0, '2025-09-10 22:56:39', 1, 'admin', 1, 'SDXL');
INSERT INTO `sd_flow` VALUES (2, 'FLUX', 1, '{\"37\": {\"_meta\": {\"title\": \"UNet加载器\"}, \"inputs\": {\"unet_name\": \"flux1-dev-fp8.safetensors\", \"weight_dtype\": \"default\"}, \"class_type\": \"UNETLoader\"}, \"39\": {\"_meta\": {\"title\": \"CLIP文本编码Flux\"}, \"inputs\": {\"clip\": [\"50\", 1], \"t5xxl\": \"{{prompt}}\", \"clip_l\": \"\", \"guidance\": 3.5}, \"class_type\": \"CLIPTextEncodeFlux\"}, \"40\": {\"_meta\": {\"title\": \"空Latent图像\"}, \"inputs\": {\"width\": 1024, \"height\": 1024, \"batch_size\": \"{{batch_size}}\"}, \"class_type\": \"EmptyLatentImage\"}, \"42\": {\"_meta\": {\"title\": \"K采样器\"}, \"inputs\": {\"cfg\": 1, \"seed\": 736838894453703, \"model\": [\"50\", 0], \"steps\": 20, \"denoise\": 1, \"negative\": [\"49\", 0], \"positive\": [\"39\", 0], \"scheduler\": \"simple\", \"latent_image\": [\"40\", 0], \"sampler_name\": \"euler\"}, \"class_type\": \"KSampler\"}, \"43\": {\"_meta\": {\"title\": \"双CLIP加载器\"}, \"inputs\": {\"type\": \"flux\", \"device\": \"default\", \"clip_name1\": \"clip_l.safetensors\", \"clip_name2\": \"t5xxl_fp16.safetensors\"}, \"class_type\": \"DualCLIPLoader\"}, \"45\": {\"_meta\": {\"title\": \"加载VAE\"}, \"inputs\": {\"vae_name\": \"ae.safetensors\"}, \"class_type\": \"VAELoader\"}, \"46\": {\"_meta\": {\"title\": \"VAE解码\"}, \"inputs\": {\"vae\": [\"45\", 0], \"samples\": [\"42\", 0]}, \"class_type\": \"VAEDecode\"}, \"49\": {\"_meta\": {\"title\": \"CLIP文本编码\"}, \"inputs\": {\"clip\": [\"50\", 1], \"text\": \"\"}, \"class_type\": \"CLIPTextEncode\"}, \"50\": {\"_meta\": {\"title\": \"加载LoRA\"}, \"inputs\": {\"clip\": [\"43\", 0], \"model\": [\"37\", 0], \"lora_name\": \"{{lora_model}}.safetensors\", \"strength_clip\": 1, \"strength_model\": \"{{lora_model_strength}}\"}, \"class_type\": \"LoraLoader\"}, \"51\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"46\", 0]}, \"class_type\": \"PreviewImage\"}}', 0, '2025-09-10 22:57:22', 1, 'admin', 1, 'FLUX');
INSERT INTO `sd_flow` VALUES (3, '背景去除', 1, '{\"210\": {\"_meta\": {\"title\": \"加载图像\"}, \"inputs\": {\"image\": \"ComfyUI_00028_.png\"}, \"class_type\": \"LoadImage\"}, \"229\": {\"_meta\": {\"title\": \"G-DinoSAM语义分割\"}, \"inputs\": {\"image\": [\"256\", 0], \"prompt\": [\"250\", 0], \"sam_model\": [\"232\", 0], \"threshold\": 0.3, \"grounding_dino_model\": [\"233\", 0]}, \"class_type\": \"GroundingDinoSAMSegment (segment anything)\"}, \"230\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"256\", 0]}, \"class_type\": \"easy imageSize\"}, \"231\": {\"_meta\": {\"title\": \"空图像\"}, \"inputs\": {\"color\": 16777215, \"width\": [\"230\", 0], \"height\": [\"230\", 1], \"batch_size\": 1}, \"class_type\": \"EmptyImage\"}, \"232\": {\"_meta\": {\"title\": \"SAM加载器\"}, \"inputs\": {\"model_name\": \"sam_vit_h_4b8939.pth\", \"device_mode\": \"Prefer GPU\"}, \"class_type\": \"SAMLoader\"}, \"233\": {\"_meta\": {\"title\": \"G-Dino模型加载器\"}, \"inputs\": {\"model_name\": \"GroundingDINO_SwinB (938MB)\"}, \"class_type\": \"GroundingDinoModelLoader (segment anything)\"}, \"247\": {\"_meta\": {\"title\": \"图像遮罩复合\"}, \"inputs\": {\"x\": 0, \"y\": 0, \"mask\": [\"229\", 1], \"source\": [\"229\", 0], \"destination\": [\"231\", 0], \"resize_source\": false}, \"class_type\": \"ImageCompositeMasked\"}, \"248\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"229\", 0]}, \"class_type\": \"PreviewImage\"}, \"250\": {\"_meta\": {\"title\": \"文本\"}, \"inputs\": {\"text\": \"women\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"TextInput_\"}, \"256\": {\"_meta\": {\"title\": \"图像缩放（KJ）\"}, \"inputs\": {\"crop\": \"disabled\", \"image\": [\"210\", 0], \"width\": [\"263\", 0], \"height\": [\"263\", 1], \"divisible_by\": 2, \"upscale_method\": \"lanczos\", \"keep_proportion\": false}, \"class_type\": \"ImageResizeKJ\"}, \"261\": {\"_meta\": {\"title\": \"保存图像\"}, \"inputs\": {\"images\": [\"247\", 0], \"filename_prefix\": \"ComfyUI\"}, \"class_type\": \"SaveImage\"}, \"263\": {\"_meta\": {\"title\": \"获取图像尺寸\"}, \"inputs\": {\"image\": [\"210\", 0]}, \"class_type\": \"GetImageSize+\"}}', 1, '2025-09-14 23:49:44', 1, 'admin', 1, 'FLUX');
INSERT INTO `sd_flow` VALUES (4, '高清放大', 1, '{\"31\": {\"_meta\": {\"title\": \"VAE加载器\"}, \"inputs\": {\"+\": null, \"vae_name\": \"FLUX.1-ae.safetensors\"}, \"class_type\": \"VAELoader\"}, \"34\": {\"_meta\": {\"title\": \"图像按像素缩放\"}, \"inputs\": {\"image\": [\"134\", 0], \"megapixels\": 8, \"upscale_method\": \"lanczos\"}, \"class_type\": \"ImageScaleToTotalPixels\"}, \"36\": {\"_meta\": {\"title\": \"TTP_Image_Tile_Batch\"}, \"inputs\": {\"image\": [\"34\", 0], \"tile_width\": [\"111\", 0], \"tile_height\": [\"111\", 1]}, \"class_type\": \"TTP_Image_Tile_Batch\"}, \"42\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"162\", 1], \"text\": [\"253\", 2], \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"67\": {\"_meta\": {\"title\": \"自定义采样器(高级)\"}, \"inputs\": {\"noise\": [\"70\", 0], \"guider\": [\"72\", 0], \"sigmas\": [\"69\", 0], \"sampler\": [\"68\", 0], \"latent_image\": [\"129\", 0]}, \"class_type\": \"SamplerCustomAdvanced\"}, \"68\": {\"_meta\": {\"title\": \"K采样器选择\"}, \"inputs\": {\"sampler_name\": \"euler\"}, \"class_type\": \"KSamplerSelect\"}, \"69\": {\"_meta\": {\"title\": \"基础调度器\"}, \"inputs\": {\"model\": [\"162\", 0], \"steps\": 4, \"denoise\": 0.1, \"scheduler\": \"simple\"}, \"class_type\": \"BasicScheduler\"}, \"70\": {\"_meta\": {\"title\": \"随机噪波\"}, \"inputs\": {\"noise_seed\": 316430325547060}, \"class_type\": \"RandomNoise\"}, \"72\": {\"_meta\": {\"title\": \"基础引导\"}, \"inputs\": {\"model\": [\"162\", 0], \"conditioning\": [\"259\", 0]}, \"class_type\": \"BasicGuider\"}, \"73\": {\"_meta\": {\"title\": \"Flux引导\"}, \"inputs\": {\"guidance\": 2.5, \"conditioning\": [\"42\", 0]}, \"class_type\": \"FluxGuidance\"}, \"77\": {\"_meta\": {\"title\": \"UNET加载器\"}, \"inputs\": {\"+\": null, \"unet_name\": \"flux1-FP8-dev.safetensors\", \"weight_dtype\": \"fp8_e4m3fn\"}, \"class_type\": \"UNETLoader\"}, \"96\": {\"_meta\": {\"title\": \"VAE分块解码\"}, \"inputs\": {\"vae\": [\"31\", 0], \"overlap\": 64, \"samples\": [\"67\", 0], \"tile_size\": 1536, \"temporal_size\": 64, \"temporal_overlap\": 8}, \"class_type\": \"VAEDecodeTiled\"}, \"111\": {\"_meta\": {\"title\": \"TTP_Tile_image_size\"}, \"inputs\": {\"image\": [\"34\", 0], \"overlap_rate\": 0.05, \"width_factor\": 2, \"height_factor\": 3}, \"class_type\": \"TTP_Tile_image_size\"}, \"127\": {\"_meta\": {\"title\": \"TTP_Image_Assy\"}, \"inputs\": {\"tiles\": [\"237\", 0], \"padding\": 128, \"grid_size\": [\"36\", 3], \"positions\": [\"36\", 1], \"original_size\": [\"36\", 2]}, \"class_type\": \"TTP_Image_Assy\"}, \"128\": {\"_meta\": {\"title\": \"保存图像\"}, \"inputs\": {\"images\": [\"127\", 0], \"filename_prefix\": \"ComfyUI\"}, \"class_type\": \"SaveImage\"}, \"129\": {\"_meta\": {\"title\": \"VAE编码\"}, \"inputs\": {\"vae\": [\"31\", 0], \"pixels\": [\"163\", 0]}, \"class_type\": \"VAEEncode\"}, \"130\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"163\", 0]}, \"class_type\": \"PreviewImage\"}, \"133\": {\"_meta\": {\"title\": \"放大模型加载器\"}, \"inputs\": {\"+\": null, \"model_name\": \"4xNomos8kSCHAT-L.pth\"}, \"class_type\": \"UpscaleModelLoader\"}, \"134\": {\"_meta\": {\"title\": \"图像通过模型放大\"}, \"inputs\": {\"image\": [\"244\", 0], \"upscale_model\": [\"133\", 0]}, \"class_type\": \"ImageUpscaleWithModel\"}, \"162\": {\"_meta\": {\"title\": \"LoRA加载器\"}, \"inputs\": {\"+\": null, \"clip\": [\"260\", 0], \"model\": [\"77\", 0], \"lora_name\": \"Flux Dev模型4步出图lora_Flux Dev模型4步出图lora_Flux Dev 4-step.safetensors\", \"strength_clip\": 1, \"strength_model\": 1}, \"class_type\": \"LoraLoader\"}, \"163\": {\"_meta\": {\"title\": \"图像批次到图像列表\"}, \"inputs\": {\"image\": [\"36\", 0]}, \"class_type\": \"easy imageBatchToImageList\"}, \"233\": {\"_meta\": {\"title\": \"加载图像\"}, \"inputs\": {\"image\": \"ComfyUI_00200_.png\"}, \"class_type\": \"LoadImage\"}, \"237\": {\"_meta\": {\"title\": \"图像列表到图像批次\"}, \"inputs\": {\"images\": [\"96\", 0]}, \"class_type\": \"ImageListToImageBatch\"}, \"244\": {\"_meta\": {\"title\": \"图像缩放（KJ）\"}, \"inputs\": {\"crop\": \"disabled\", \"image\": [\"233\", 0], \"width\": 1024, \"height\": 1024, \"divisible_by\": 2, \"upscale_method\": \"nearest-exact\", \"keep_proportion\": true}, \"class_type\": \"ImageResizeKJ\"}, \"245\": {\"_meta\": {\"title\": \"图像对比\"}, \"inputs\": {\"image_a\": [\"233\", 0], \"image_b\": [\"127\", 0], \"rgthree_comparer\": {\"images\": [{\"url\": \"/api/view?filename=rgthree.compare._temp_azjdv_00003_.png&type=temp&subfolder=&rand=0.6867502709839512\", \"name\": \"A\", \"selected\": true}, {\"url\": \"/api/view?filename=rgthree.compare._temp_azjdv_00004_.png&type=temp&subfolder=&rand=0.10461341791451617\", \"name\": \"B\", \"selected\": true}]}}, \"class_type\": \"Image Comparer (rgthree)\"}, \"253\": {\"_meta\": {\"title\": \"Florence2Run\"}, \"inputs\": {\"seed\": 726687677925004, \"task\": \"more_detailed_caption\", \"image\": [\"233\", 0], \"do_sample\": true, \"fill_mask\": true, \"num_beams\": 3, \"text_input\": \"\", \"max_new_tokens\": 1024, \"florence2_model\": [\"254\", 0], \"keep_model_loaded\": false, \"output_mask_select\": \"\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"Florence2Run\"}, \"254\": {\"_meta\": {\"title\": \"Florence2ModelLoader\"}, \"inputs\": {\"model\": \"Florence-2-base\", \"attention\": \"sdpa\", \"precision\": \"fp16\"}, \"class_type\": \"Florence2ModelLoader\"}, \"256\": {\"_meta\": {\"title\": \"风格模型加载器\"}, \"inputs\": {\"+\": null, \"style_model_name\": \"flux1-redux-dev.safetensors\"}, \"class_type\": \"StyleModelLoader\"}, \"257\": {\"_meta\": {\"title\": \"CLIP视觉加载器\"}, \"inputs\": {\"+\": null, \"clip_name\": \"sigclip_vision_patch14_384.safetensors\"}, \"class_type\": \"CLIPVisionLoader\"}, \"258\": {\"_meta\": {\"title\": \"CLIP视觉编码\"}, \"inputs\": {\"crop\": \"center\", \"image\": [\"233\", 0], \"clip_vision\": [\"257\", 0]}, \"class_type\": \"CLIPVisionEncode\"}, \"259\": {\"_meta\": {\"title\": \"风格模型应用\"}, \"inputs\": {\"strength\": 0.6, \"style_model\": [\"256\", 0], \"conditioning\": [\"73\", 0], \"strength_type\": \"multiply\", \"clip_vision_output\": [\"258\", 0]}, \"class_type\": \"StyleModelApply\"}, \"260\": {\"_meta\": {\"title\": \"双CLIP加载器\"}, \"inputs\": {\"+\": null, \"type\": \"flux\", \"device\": \"default\", \"clip_name1\": \"FLUX.1-t5xxl_fp8_e4m3fn.safetensors\", \"clip_name2\": \"FLUX.1-clip_l.safetensors\"}, \"class_type\": \"DualCLIPLoader\"}}', 1, '2025-09-14 23:49:44', 1, 'admin', 1, 'FLUX');
INSERT INTO `sd_flow` VALUES (5, '局部抹除', 1, '{\"6\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"38\", 0], \"text\": \"Remove women from the image,\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"8\": {\"_meta\": {\"title\": \"VAE解码\"}, \"inputs\": {\"vae\": [\"39\", 0], \"samples\": [\"31\", 0]}, \"class_type\": \"VAEDecode\"}, \"31\": {\"_meta\": {\"title\": \"K采样器\"}, \"inputs\": {\"cfg\": 1, \"seed\": 113696772698209, \"model\": [\"211\", 0], \"steps\": 20, \"denoise\": 1, \"negative\": [\"135\", 0], \"positive\": [\"35\", 0], \"scheduler\": \"simple\", \"latent_image\": [\"124\", 0], \"sampler_name\": \"euler\"}, \"class_type\": \"KSampler\"}, \"35\": {\"_meta\": {\"title\": \"Flux引导\"}, \"inputs\": {\"guidance\": 2.5, \"conditioning\": [\"177\", 0]}, \"class_type\": \"FluxGuidance\"}, \"37\": {\"_meta\": {\"title\": \"UNET加载器\"}, \"inputs\": {\"+\": null, \"unet_name\": \"flux1-dev-kontext_fp8_scaled.safetensors\", \"weight_dtype\": \"fp8_e4m3fn\"}, \"class_type\": \"UNETLoader\"}, \"38\": {\"_meta\": {\"title\": \"双CLIP加载器\"}, \"inputs\": {\"+\": null, \"type\": \"flux\", \"device\": \"default\", \"clip_name1\": \"FLUX.1-clip_l.safetensors\", \"clip_name2\": \"t5xxl_fp8_e4m3fn_scaled.safetensors\"}, \"class_type\": \"DualCLIPLoader\"}, \"39\": {\"_meta\": {\"title\": \"VAE加载器\"}, \"inputs\": {\"+\": null, \"vae_name\": \"ae.safetensors\"}, \"class_type\": \"VAELoader\"}, \"124\": {\"_meta\": {\"title\": \"VAE编码\"}, \"inputs\": {\"vae\": [\"39\", 0], \"pixels\": [\"256\", 0]}, \"class_type\": \"VAEEncode\"}, \"135\": {\"_meta\": {\"title\": \"条件零化\"}, \"inputs\": {\"conditioning\": [\"6\", 0]}, \"class_type\": \"ConditioningZeroOut\"}, \"177\": {\"_meta\": {\"title\": \"ReferenceLatent\"}, \"inputs\": {\"latent\": [\"124\", 0], \"conditioning\": [\"6\", 0]}, \"class_type\": \"ReferenceLatent\"}, \"189\": {\"_meta\": {\"title\": \"双CLIP加载器\"}, \"inputs\": {\"+\": null, \"type\": \"flux\", \"device\": \"default\", \"clip_name1\": \"FLUX.1-clip_l.safetensors\", \"clip_name2\": \"FLUX.1-t5xxl_fp8_e4m3fn.safetensors\"}, \"class_type\": \"DualCLIPLoader\"}, \"190\": {\"_meta\": {\"title\": \"UNET加载器\"}, \"inputs\": {\"+\": null, \"unet_name\": \"flux\\\\flux1-FP8-dev.safetensors\", \"weight_dtype\": \"fp8_e4m3fn\"}, \"class_type\": \"UNETLoader\"}, \"191\": {\"_meta\": {\"title\": \"LoRA加载器\"}, \"inputs\": {\"+\": null, \"clip\": [\"189\", 0], \"model\": [\"190\", 0], \"lora_name\": \"flux\\\\AWPortraitFL LoRA_1.0.safetensors\", \"strength_clip\": 1.0000000000000002, \"strength_model\": 1.0000000000000002}, \"class_type\": \"LoraLoader\"}, \"194\": {\"_meta\": {\"title\": \"LoRA加载器\"}, \"inputs\": {\"+\": null, \"clip\": [\"191\", 1], \"model\": [\"191\", 0], \"lora_name\": \"F.1光影斑驳_光影质感增强V1_F.1光影斑驳_光影增强V1.safetensors\", \"strength_clip\": 0.8, \"strength_model\": 0.65}, \"class_type\": \"LoraLoader\"}, \"195\": {\"_meta\": {\"title\": \"基础引导\"}, \"inputs\": {\"model\": [\"190\", 0], \"conditioning\": [\"196\", 0]}, \"class_type\": \"BasicGuider\"}, \"196\": {\"_meta\": {\"title\": \"Flux引导\"}, \"inputs\": {\"guidance\": 10, \"conditioning\": [\"206\", 0]}, \"class_type\": \"FluxGuidance\"}, \"197\": {\"_meta\": {\"title\": \"VAE加载器\"}, \"inputs\": {\"+\": null, \"vae_name\": \"FLUX.1-ae.safetensors\"}, \"class_type\": \"VAELoader\"}, \"198\": {\"_meta\": {\"title\": \"VAE编码\"}, \"inputs\": {\"vae\": [\"197\", 0], \"pixels\": [\"8\", 0]}, \"class_type\": \"VAEEncode\"}, \"199\": {\"_meta\": {\"title\": \"随机噪波\"}, \"inputs\": {\"noise_seed\": 427877902275004}, \"class_type\": \"RandomNoise\"}, \"200\": {\"_meta\": {\"title\": \"基础调度器\"}, \"inputs\": {\"model\": [\"194\", 0], \"steps\": 20, \"denoise\": 0.1, \"scheduler\": \"simple\"}, \"class_type\": \"BasicScheduler\"}, \"201\": {\"_meta\": {\"title\": \"自定义采样器(高级)\"}, \"inputs\": {\"noise\": [\"199\", 0], \"guider\": [\"195\", 0], \"sigmas\": [\"200\", 0], \"sampler\": [\"203\", 0], \"latent_image\": [\"198\", 0]}, \"class_type\": \"SamplerCustomAdvanced\"}, \"202\": {\"_meta\": {\"title\": \"VAE解码\"}, \"inputs\": {\"vae\": [\"197\", 0], \"samples\": [\"201\", 0]}, \"class_type\": \"VAEDecode\"}, \"203\": {\"_meta\": {\"title\": \"K采样器选择\"}, \"inputs\": {\"sampler_name\": \"euler\"}, \"class_type\": \"KSamplerSelect\"}, \"205\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"8\", 0]}, \"class_type\": \"PreviewImage\"}, \"206\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"194\", 1], \"text\": [\"258\", 2], \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"210\": {\"_meta\": {\"title\": \"加载图像\"}, \"inputs\": {\"image\": \"微信图片_20250826112657.jpg\"}, \"class_type\": \"LoadImage\"}, \"211\": {\"_meta\": {\"title\": \"LoRA加载器(仅模型)\"}, \"inputs\": {\"+\": null, \"model\": [\"37\", 0], \"lora_name\": \"kontext-lora  万物消除_场景物品移除_kontext万物消除V1.1.safetensors\", \"strength_model\": 1}, \"class_type\": \"LoraLoaderModelOnly\"}, \"212\": {\"_meta\": {\"title\": \"FluxKontextImageScale\"}, \"inputs\": {\"image\": [\"210\", 0]}, \"class_type\": \"FluxKontextImageScale\"}, \"227\": {\"_meta\": {\"title\": \"亮度/对比度\"}, \"inputs\": {\"image\": [\"202\", 0], \"contrast\": 1.05, \"brightness\": 1, \"saturation\": 1.05}, \"class_type\": \"LayerColor: Brightness & Contrast\"}, \"236\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"227\", 0]}, \"class_type\": \"PreviewImage\"}, \"237\": {\"_meta\": {\"title\": \"保存图像\"}, \"inputs\": {\"images\": [\"227\", 0], \"filename_prefix\": \"ComfyUI\"}, \"class_type\": \"SaveImage\"}, \"241\": {\"_meta\": {\"title\": \"展示文本\"}, \"inputs\": {\"text\": [\"258\", 2], \"text_0\": \"The image shows a person standing in front of a window with their arms crossed. The person is wearing a white sweater with blue trim and a denim skirt. They are holding a small white teddy bear in their arms. The background is blurred, but it appears to be a room with white walls and a wooden door. The overall mood of the image is peaceful and serene.\"}, \"class_type\": \"ShowText|pysssss\"}, \"255\": {\"_meta\": {\"title\": \"图像对比\"}, \"inputs\": {\"image_a\": [\"210\", 0], \"image_b\": [\"227\", 0], \"rgthree_comparer\": {\"images\": [{\"url\": \"/api/view?filename=rgthree.compare._temp_urine_00015_.png&type=temp&subfolder=&rand=0.09483254448005463\", \"name\": \"A\", \"selected\": true}, {\"url\": \"/api/view?filename=rgthree.compare._temp_urine_00016_.png&type=temp&subfolder=&rand=0.8452258113272707\", \"name\": \"B\", \"selected\": true}]}}, \"class_type\": \"Image Comparer (rgthree)\"}, \"256\": {\"_meta\": {\"title\": \"图像缩放（KJ）\"}, \"inputs\": {\"crop\": \"disabled\", \"image\": [\"212\", 0], \"width\": [\"261\", 0], \"height\": [\"261\", 1], \"divisible_by\": 2, \"upscale_method\": \"lanczos\", \"keep_proportion\": false}, \"class_type\": \"ImageResizeKJ\"}, \"258\": {\"_meta\": {\"title\": \"Florence2Run\"}, \"inputs\": {\"seed\": 630668400156122, \"task\": \"more_detailed_caption\", \"image\": [\"8\", 0], \"do_sample\": true, \"fill_mask\": true, \"num_beams\": 3, \"text_input\": \"\", \"max_new_tokens\": 1024, \"florence2_model\": [\"259\", 0], \"keep_model_loaded\": false, \"output_mask_select\": \"\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"Florence2Run\"}, \"259\": {\"_meta\": {\"title\": \"Florence2ModelLoader\"}, \"inputs\": {\"model\": \"Florence-2-base\", \"attention\": \"sdpa\", \"precision\": \"fp16\"}, \"class_type\": \"Florence2ModelLoader\"}, \"261\": {\"_meta\": {\"title\": \"获取图像尺寸\"}, \"inputs\": {\"image\": [\"210\", 0]}, \"class_type\": \"GetImageSize\"}}', 1, '2025-09-14 23:49:44', 1, 'admin', 1, 'FLUX');
INSERT INTO `sd_flow` VALUES (6, '物体迁移', 1, '{\"3\": {\"_meta\": {\"title\": \"K采样器\"}, \"inputs\": {\"cfg\": 1, \"seed\": 72421124590570, \"model\": [\"13\", 0], \"steps\": 25, \"denoise\": 1, \"negative\": [\"211\", 0], \"positive\": [\"219\", 0], \"scheduler\": \"beta\", \"latent_image\": [\"17\", 2], \"sampler_name\": \"euler\"}, \"class_type\": \"KSampler\"}, \"6\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"154\", 1], \"text\": \"\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"7\": {\"_meta\": {\"title\": \"CLIP文本编码器\"}, \"inputs\": {\"clip\": [\"154\", 1], \"text\": \"\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"CLIPTextEncode\"}, \"8\": {\"_meta\": {\"title\": \"VAE解码\"}, \"inputs\": {\"vae\": [\"16\", 0], \"samples\": [\"3\", 0]}, \"class_type\": \"VAEDecode\"}, \"11\": {\"_meta\": {\"title\": \"Flux引导\"}, \"inputs\": {\"guidance\": 50, \"conditioning\": [\"17\", 0]}, \"class_type\": \"FluxGuidance\"}, \"13\": {\"_meta\": {\"title\": \"差异扩散\"}, \"inputs\": {\"model\": [\"154\", 0]}, \"class_type\": \"DifferentialDiffusion\"}, \"15\": {\"_meta\": {\"title\": \"UNET加载器\"}, \"inputs\": {\"+\": null, \"unet_name\": \"flux1-fill-dev-fp16.safetensors\", \"weight_dtype\": \"fp8_e4m3fn\"}, \"class_type\": \"UNETLoader\"}, \"16\": {\"_meta\": {\"title\": \"VAE加载器\"}, \"inputs\": {\"+\": null, \"vae_name\": \"FLUX.1-ae.safetensors\"}, \"class_type\": \"VAELoader\"}, \"17\": {\"_meta\": {\"title\": \"内补模型条件\"}, \"inputs\": {\"vae\": [\"16\", 0], \"mask\": [\"27\", 2], \"pixels\": [\"27\", 1], \"negative\": [\"7\", 0], \"positive\": [\"6\", 0], \"noise_mask\": false}, \"class_type\": \"InpaintModelConditioning\"}, \"27\": {\"_meta\": {\"title\": \"✂️ Inpaint Crop\"}, \"inputs\": {\"mask\": [\"30\", 0], \"mode\": \"ranged size\", \"image\": [\"42\", 0], \"padding\": 32, \"max_width\": [\"235\", 0], \"min_width\": 512, \"max_height\": [\"231\", 0], \"min_height\": 512, \"force_width\": 1024, \"invert_mask\": false, \"blend_pixels\": 16, \"force_height\": 1024, \"rescale_factor\": 1, \"fill_mask_holes\": true, \"blur_mask_pixels\": 16, \"rescale_algorithm\": \"bicubic\", \"context_expand_factor\": 1, \"context_expand_pixels\": 20, \"optional_context_mask\": [\"40\", 0]}, \"class_type\": \"InpaintCrop\"}, \"28\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"161\", 0]}, \"class_type\": \"MaskToImage\"}, \"30\": {\"_meta\": {\"title\": \"图像到遮罩\"}, \"inputs\": {\"image\": [\"43\", 0], \"channel\": \"red\"}, \"class_type\": \"ImageToMask\"}, \"34\": {\"_meta\": {\"title\": \"✂️ Inpaint Stitch\"}, \"inputs\": {\"stitch\": [\"27\", 0], \"inpainted_image\": [\"8\", 0], \"rescale_algorithm\": \"bislerp\"}, \"class_type\": \"InpaintStitch\"}, \"37\": {\"_meta\": {\"title\": \"图像联结\"}, \"inputs\": {\"image1\": [\"38\", 0], \"image2\": [\"57\", 0], \"direction\": \"right\", \"match_image_size\": true}, \"class_type\": \"ImageConcanate\"}, \"38\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"159\", 0]}, \"class_type\": \"MaskToImage\"}, \"40\": {\"_meta\": {\"title\": \"图像到遮罩\"}, \"inputs\": {\"image\": [\"37\", 0], \"channel\": \"red\"}, \"class_type\": \"ImageToMask\"}, \"42\": {\"_meta\": {\"title\": \"图像联结\"}, \"inputs\": {\"image1\": [\"173\", 0], \"image2\": [\"277\", 0], \"direction\": \"right\", \"match_image_size\": true}, \"class_type\": \"ImageConcanate\"}, \"43\": {\"_meta\": {\"title\": \"图像联结\"}, \"inputs\": {\"image1\": [\"59\", 0], \"image2\": [\"278\", 0], \"direction\": \"right\", \"match_image_size\": true}, \"class_type\": \"ImageConcanate\"}, \"57\": {\"_meta\": {\"title\": \"颜色调整\"}, \"inputs\": {\"blur\": 0, \"image\": [\"277\", 0], \"contrast\": 0, \"sharpness\": 0, \"brightness\": -1, \"saturation\": 0, \"edge_enhance\": 0, \"gaussian_blur\": 0, \"detail_enhance\": \"false\"}, \"class_type\": \"Image Filter Adjustments\"}, \"59\": {\"_meta\": {\"title\": \"颜色调整\"}, \"inputs\": {\"blur\": 0, \"image\": [\"276\", 0], \"contrast\": 1, \"sharpness\": 1, \"brightness\": -1, \"saturation\": 1, \"edge_enhance\": 0, \"gaussian_blur\": 0, \"detail_enhance\": \"false\"}, \"class_type\": \"Image Filter Adjustments\"}, \"68\": {\"_meta\": {\"title\": \"加载图像\"}, \"inputs\": {\"image\": \"ComfyUI_01242_.png\"}, \"class_type\": \"LoadImage\"}, \"69\": {\"_meta\": {\"title\": \"加载图像\"}, \"inputs\": {\"image\": \"DSC06511.jpg\"}, \"class_type\": \"LoadImage\"}, \"71\": {\"_meta\": {\"title\": \"保存图像\"}, \"inputs\": {\"images\": [\"88\", 0], \"filename_prefix\": \"ComfyUI\"}, \"class_type\": \"SaveImage\"}, \"88\": {\"_meta\": {\"title\": \"图像裁剪\"}, \"inputs\": {\"image\": [\"94\", 0], \"width\": [\"199\", 0], \"height\": [\"199\", 1], \"position\": \"right-center\", \"x_offset\": 0, \"y_offset\": 0}, \"class_type\": \"ImageCrop+\"}, \"94\": {\"_meta\": {\"title\": \"按宽高比缩放_V2\"}, \"inputs\": {\"fit\": \"letterbox\", \"mask\": [\"30\", 0], \"image\": [\"34\", 0], \"method\": \"lanczos\", \"aspect_ratio\": \"original\", \"scale_to_side\": \"height\", \"scale_to_length\": [\"230\", 0], \"background_color\": \"#000000\", \"round_to_multiple\": \"8\", \"proportional_width\": 1, \"proportional_height\": 1}, \"class_type\": \"LayerUtility: ImageScaleByAspectRatio V2\"}, \"107\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"69\", 1]}, \"class_type\": \"MaskToImage\"}, \"108\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"107\", 0]}, \"class_type\": \"PreviewImage\"}, \"114\": {\"_meta\": {\"title\": \"保存图像\"}, \"inputs\": {\"images\": [\"8\", 0], \"filename_prefix\": \"ComfyUI\"}, \"class_type\": \"SaveImage\"}, \"115\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"37\", 0]}, \"class_type\": \"PreviewImage\"}, \"142\": {\"_meta\": {\"title\": \"图像对比\"}, \"inputs\": {\"image_a\": [\"277\", 0], \"image_b\": [\"88\", 0], \"rgthree_comparer\": {\"images\": [{\"url\": \"/api/view?filename=rgthree.compare._temp_thspo_00001_.png&type=temp&subfolder=&rand=0.9234653641046767\", \"name\": \"A\", \"selected\": true}, {\"url\": \"/api/view?filename=rgthree.compare._temp_thspo_00002_.png&type=temp&subfolder=&rand=0.3454178996219316\", \"name\": \"B\", \"selected\": true}]}}, \"class_type\": \"Image Comparer (rgthree)\"}, \"151\": {\"_meta\": {\"title\": \"双CLIP加载器\"}, \"inputs\": {\"+\": null, \"type\": \"flux\", \"device\": \"default\", \"clip_name1\": \"ViT-L-14-TEXT-detail-improved-hiT-GmP-TE-only-HF.safetensors\", \"clip_name2\": \"FLUX.1-t5xxl_fp16.safetensors\"}, \"class_type\": \"DualCLIPLoader\"}, \"154\": {\"_meta\": {\"title\": \"LoRA加载器\"}, \"inputs\": {\"+\": null, \"clip\": [\"151\", 0], \"model\": [\"15\", 0], \"lora_name\": \"flux\\\\comfyui_subject_lora16.safetensors\", \"strength_clip\": 1, \"strength_model\": 1}, \"class_type\": \"LoraLoader\"}, \"156\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"276\", 0]}, \"class_type\": \"easy imageSize\"}, \"158\": {\"_meta\": {\"title\": \"遮罩模糊生长\"}, \"inputs\": {\"mask\": [\"191\", 1], \"expand\": 10, \"fill_holes\": false, \"flip_input\": false, \"lerp_alpha\": 1, \"blur_radius\": 2, \"decay_factor\": 1, \"tapered_corners\": true, \"incremental_expandrate\": 0}, \"class_type\": \"GrowMaskWithBlur\"}, \"159\": {\"_meta\": {\"title\": \"羽化遮罩\"}, \"inputs\": {\"mask\": [\"158\", 0], \"start_offset\": 15, \"feathering_weight\": 0.30000000000000004}, \"class_type\": \"FeatheredMask\"}, \"160\": {\"_meta\": {\"title\": \"遮罩模糊生长\"}, \"inputs\": {\"mask\": [\"257\", 0], \"expand\": 10, \"fill_holes\": false, \"flip_input\": false, \"lerp_alpha\": 1, \"blur_radius\": 4, \"decay_factor\": 1, \"tapered_corners\": true, \"incremental_expandrate\": 0}, \"class_type\": \"GrowMaskWithBlur\"}, \"161\": {\"_meta\": {\"title\": \"羽化遮罩\"}, \"inputs\": {\"mask\": [\"160\", 0], \"start_offset\": 15, \"feathering_weight\": 0.30000000000000004}, \"class_type\": \"FeatheredMask\"}, \"163\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"27\", 1]}, \"class_type\": \"PreviewImage\"}, \"172\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"276\", 0]}, \"class_type\": \"PreviewImage\"}, \"173\": {\"_meta\": {\"title\": \"图像遮罩复合\"}, \"inputs\": {\"x\": 0, \"y\": 0, \"mask\": [\"159\", 0], \"source\": [\"276\", 0], \"destination\": [\"267\", 0], \"resize_source\": false}, \"class_type\": \"ImageCompositeMasked\"}, \"175\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"173\", 0]}, \"class_type\": \"PreviewImage\"}, \"176\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"42\", 0]}, \"class_type\": \"PreviewImage\"}, \"191\": {\"_meta\": {\"title\": \"Inspyrenet Rembg Advanced\"}, \"inputs\": {\"image\": [\"276\", 0], \"threshold\": 0.6, \"torchscript_jit\": \"on\"}, \"class_type\": \"InspyrenetRembgAdvanced\"}, \"192\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"193\", 0]}, \"class_type\": \"easy imageSize\"}, \"193\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"40\", 0]}, \"class_type\": \"MaskToImage\"}, \"194\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"195\", 0]}, \"class_type\": \"easy imageSize\"}, \"195\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"30\", 0]}, \"class_type\": \"MaskToImage\"}, \"197\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"42\", 0]}, \"class_type\": \"easy imageSize\"}, \"198\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"27\", 1]}, \"class_type\": \"easy imageSize\"}, \"199\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"277\", 0]}, \"class_type\": \"easy imageSize\"}, \"211\": {\"_meta\": {\"title\": \"条件零化\"}, \"inputs\": {\"conditioning\": [\"17\", 1]}, \"class_type\": \"ConditioningZeroOut\"}, \"212\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"30\", 0]}, \"class_type\": \"MaskToImage\"}, \"213\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"212\", 0]}, \"class_type\": \"PreviewImage\"}, \"214\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"40\", 0]}, \"class_type\": \"MaskToImage\"}, \"215\": {\"_meta\": {\"title\": \"预览图像\"}, \"inputs\": {\"images\": [\"214\", 0]}, \"class_type\": \"PreviewImage\"}, \"217\": {\"_meta\": {\"title\": \"清理GPU占用\"}, \"inputs\": {\"anything\": [\"3\", 0]}, \"class_type\": \"easy cleanGpuUsed\"}, \"219\": {\"_meta\": {\"title\": \"风格模型应用\"}, \"inputs\": {\"strength\": 1, \"style_model\": [\"220\", 0], \"conditioning\": [\"11\", 0], \"strength_type\": \"multiply\", \"clip_vision_output\": [\"221\", 0]}, \"class_type\": \"StyleModelApply\"}, \"220\": {\"_meta\": {\"title\": \"风格模型加载器\"}, \"inputs\": {\"+\": null, \"style_model_name\": \"flux1-redux-dev.safetensors\"}, \"class_type\": \"StyleModelLoader\"}, \"221\": {\"_meta\": {\"title\": \"CLIP视觉编码\"}, \"inputs\": {\"crop\": \"center\", \"image\": [\"173\", 0], \"clip_vision\": [\"222\", 0]}, \"class_type\": \"CLIPVisionEncode\"}, \"222\": {\"_meta\": {\"title\": \"CLIP视觉加载器\"}, \"inputs\": {\"+\": null, \"clip_name\": \"sigclip_vision_patch14_384.safetensors\"}, \"class_type\": \"CLIPVisionLoader\"}, \"230\": {\"_meta\": {\"title\": \"整数\"}, \"inputs\": {\"Number\": \"1536\"}, \"class_type\": \"Int\"}, \"231\": {\"_meta\": {\"title\": \"整数\"}, \"inputs\": {\"Number\": \"1536\"}, \"class_type\": \"Int\"}, \"235\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"276\", 1], \"b\": [\"277\", 1], \"expression\": \"a+b\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"237\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"68\", 0]}, \"class_type\": \"easy imageSize\"}, \"238\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"237\", 1], \"expression\": \"a//8*8\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"239\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"237\", 0], \"expression\": \"a//8*8\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"240\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"250\", 1], \"expression\": \"a//8*8\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"241\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"250\", 0], \"expression\": \"a//8*8\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"243\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"277\", 0]}, \"class_type\": \"easy imageSize\"}, \"244\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"276\", 0]}, \"class_type\": \"easy imageSize\"}, \"248\": {\"_meta\": {\"title\": \"图像裁剪\"}, \"inputs\": {\"x\": 0, \"y\": 0, \"image\": [\"68\", 0], \"width\": [\"239\", 0], \"height\": [\"238\", 0]}, \"class_type\": \"ImageCrop\"}, \"249\": {\"_meta\": {\"title\": \"图像裁剪\"}, \"inputs\": {\"x\": 0, \"y\": 0, \"image\": [\"69\", 0], \"width\": [\"241\", 0], \"height\": [\"240\", 0]}, \"class_type\": \"ImageCrop\"}, \"250\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"69\", 0]}, \"class_type\": \"easy imageSize\"}, \"252\": {\"_meta\": {\"title\": \"遮罩到图像\"}, \"inputs\": {\"mask\": [\"69\", 1]}, \"class_type\": \"MaskToImage\"}, \"253\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"252\", 0]}, \"class_type\": \"easy imageSize\"}, \"254\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"253\", 0], \"expression\": \"a//8*8\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"255\": {\"_meta\": {\"title\": \"数学表达式\"}, \"inputs\": {\"a\": [\"253\", 1], \"expression\": \"a//8*8\", \"speak_and_recognation\": {\"__value__\": [false, true]}}, \"class_type\": \"MathExpression|pysssss\"}, \"256\": {\"_meta\": {\"title\": \"图像裁剪\"}, \"inputs\": {\"x\": 0, \"y\": 0, \"image\": [\"252\", 0], \"width\": [\"254\", 0], \"height\": [\"255\", 0]}, \"class_type\": \"ImageCrop\"}, \"257\": {\"_meta\": {\"title\": \"图像到遮罩\"}, \"inputs\": {\"image\": [\"256\", 0], \"channel\": \"red\"}, \"class_type\": \"ImageToMask\"}, \"263\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"277\", 0]}, \"class_type\": \"easy imageSize\"}, \"264\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"173\", 0]}, \"class_type\": \"easy imageSize\"}, \"267\": {\"_meta\": {\"title\": \"空图像\"}, \"inputs\": {\"color\": 16777215, \"width\": [\"156\", 0], \"height\": [\"156\", 1], \"batch_size\": 1}, \"class_type\": \"EmptyImage\"}, \"268\": {\"_meta\": {\"title\": \"图像尺寸\"}, \"inputs\": {\"image\": [\"267\", 0]}, \"class_type\": \"easy imageSize\"}, \"276\": {\"_meta\": {\"title\": \"图像缩放（KJ）\"}, \"inputs\": {\"crop\": \"disabled\", \"image\": [\"248\", 0], \"width\": [\"230\", 0], \"height\": [\"231\", 0], \"divisible_by\": 2, \"upscale_method\": \"lanczos\", \"keep_proportion\": true}, \"class_type\": \"ImageResizeKJ\"}, \"277\": {\"_meta\": {\"title\": \"图像缩放（KJ）\"}, \"inputs\": {\"crop\": \"disabled\", \"image\": [\"249\", 0], \"width\": [\"230\", 0], \"height\": [\"231\", 0], \"divisible_by\": 2, \"upscale_method\": \"lanczos\", \"keep_proportion\": true}, \"class_type\": \"ImageResizeKJ\"}, \"278\": {\"_meta\": {\"title\": \"图像缩放（KJ）\"}, \"inputs\": {\"crop\": \"disabled\", \"image\": [\"28\", 0], \"width\": [\"230\", 0], \"height\": [\"231\", 0], \"divisible_by\": 2, \"upscale_method\": \"lanczos\", \"keep_proportion\": true}, \"class_type\": \"ImageResizeKJ\"}}', 1, '2025-09-14 23:49:44', 1, 'admin', 1, 'FLUX');

-- ----------------------------
-- Table structure for sd_gpu_pool
-- ----------------------------
DROP TABLE IF EXISTS `sd_gpu_pool`;
CREATE TABLE `sd_gpu_pool`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `host` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'SD服务IP地址',
  `port` int NULL DEFAULT NULL COMMENT 'SD服务端口',
  `device_id` int NULL DEFAULT NULL COMMENT 'GPU序号',
  `type` int NULL DEFAULT NULL COMMENT '类型[0-sd,1-训练]',
  `img_grid_dir` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型测试图生图grid目录',
  `txt_grid_dir` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型测试文生图grid目录',
  `is_enable` int NULL DEFAULT 0 COMMENT '是否启用[0-否,1-是]',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `host`(`host` ASC, `port` ASC, `device_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || GPU卡池' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_gpu_pool
-- ----------------------------
INSERT INTO `sd_gpu_pool` VALUES (1, '192.168.3.48', 7860, 0, 0, '/img2img-grids', '/txt2img-grids', 1);
INSERT INTO `sd_gpu_pool` VALUES (2, '192.168.3.48', 28000, 0, 1, NULL, NULL, 1);

-- ----------------------------
-- Table structure for sd_train_task
-- ----------------------------
DROP TABLE IF EXISTS `sd_train_task`;
CREATE TABLE `sd_train_task`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `pre_params` json NULL COMMENT '预处理参数',
  `img_num` int NULL DEFAULT NULL COMMENT '预处理图片数量',
  `pre_submit_time` datetime NULL DEFAULT NULL COMMENT '预处理提交时间',
  `pre_start_time` datetime NULL DEFAULT NULL COMMENT '预处理开始时间',
  `pre_end_time` datetime NULL DEFAULT NULL COMMENT '预处理结束时间',
  `pre_reason` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预处理失败原因',
  `status` int NULL DEFAULT 0 COMMENT '任务状态(废弃)[0-未训练,1-已预处理,2-已训练,3-训练失败]',
  `new_status` int NULL DEFAULT 0 COMMENT '任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]',
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练任务ID',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练任务模型名称',
  `addition_tag` json NULL COMMENT '训练任务共性词数组',
  `train_params` json NULL COMMENT '训练任务参数',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '训练任务提交时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '训练任务开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '训练任务完成时间',
  `reason` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练失败原因',
  `gpu_pool` json NULL COMMENT '训练使用的GPU',
  `crt_user_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `crt_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `task_id`(`task_id` ASC) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id` ASC, `new_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 图片预处理任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_train_task
-- ----------------------------

-- ----------------------------
-- Table structure for sd_translation
-- ----------------------------
DROP TABLE IF EXISTS `sd_translation`;
CREATE TABLE `sd_translation`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `zh` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '中文',
  `en` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '英文',
  `type` tinyint NULL DEFAULT NULL COMMENT '类型[0-提示词英译中,1-tag标签中译英,2-共性词,3-标签词英译中]',
  `train_task_id` bigint NULL DEFAULT NULL COMMENT '训练任务ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `type`(`type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 翻译' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_translation
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_model
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model`;
CREATE TABLE `sd_user_model`  (
  `id` bigint NOT NULL COMMENT '模型ID',
  `classify_id` bigint NULL DEFAULT 1 COMMENT '模型分类ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型标题',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型名称',
  `model_name_zh` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型别名',
  `model_strength` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0.5' COMMENT '模型强度',
  `addition_tag` json NULL COMMENT '模型共性词',
  `hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型hash值',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型存储位置',
  `config` json NULL COMMENT '模型配置',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型封面地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型描述',
  `type` int NULL DEFAULT NULL COMMENT '模型归属类型[0-系统,1-个人]',
  `is_open` int NULL DEFAULT NULL COMMENT '模型是否公开[0-否,1-是]',
  `belong_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型归属人ID',
  `publish_status` int NULL DEFAULT 0 COMMENT '发布状态[0-否,1-是]',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '模型创建时间',
  `is_user_del` int NULL DEFAULT 0 COMMENT '用户是否已删除该模型[0-否,1-是]',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SDXL' COMMENT '模型类型[SDXL,FLUX]',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `hash`(`hash` ASC) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id` ASC) USING BTREE,
  INDEX `classify_id`(`classify_id` ASC) USING BTREE,
  INDEX `type`(`type` ASC) USING BTREE,
  INDEX `publish_status`(`publish_status` ASC) USING BTREE,
  INDEX `idx_type_open`(`type` ASC, `is_open` ASC, `belong_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_model
-- ----------------------------
INSERT INTO `sd_user_model` VALUES (1, 1, 'realism_lora', 'realism_lora', 'realism_lora', '0.8', NULL, NULL, 'D:\\project\\ai_project\\models\\Lora\\comfy\\realism_lora_comfy_converted.safetensors', '{\"ss_v2\": null, \"ss_seed\": null, \"ss_epoch\": null, \"ss_lowram\": null, \"ss_unet_lr\": null, \"ss_flip_aug\": null, \"ss_clip_skip\": null, \"ss_color_aug\": null, \"ss_full_fp16\": null, \"ss_optimizer\": null, \"ss_num_epochs\": null, \"ss_resolution\": null, \"ss_session_id\": null, \"ss_bucket_info\": null, \"ss_keep_tokens\": null, \"ss_network_dim\": null, \"ss_output_name\": null, \"ss_random_crop\": null, \"ss_dataset_dirs\": null, \"ss_lr_scheduler\": null, \"ss_noise_offset\": null, \"sshs_model_hash\": null, \"ss_cache_latents\": null, \"ss_enable_bucket\": null, \"ss_learning_rate\": null, \"ss_max_grad_norm\": null, \"ss_min_snr_gamma\": null, \"ss_network_alpha\": null, \"ss_sd_model_hash\": null, \"ss_sd_model_name\": null, \"ss_tag_frequency\": null, \"sshs_legacy_hash\": null, \"ss_network_module\": null, \"ss_num_reg_images\": null, \"ssTagFrequencyList\": [], \"ss_lr_warmup_steps\": null, \"ss_max_bucket_reso\": null, \"ss_max_train_steps\": null, \"ss_min_bucket_reso\": null, \"ss_mixed_precision\": null, \"ss_shuffle_caption\": null, \"ss_text_encoder_lr\": null, \"ss_max_token_length\": null, \"ss_num_train_images\": null, \"ss_reg_dataset_dirs\": null, \"ss_total_batch_size\": null, \"ss_training_comment\": null, \"ss_bucket_no_upscale\": null, \"ss_new_sd_model_hash\": null, \"ss_prior_loss_weight\": null, \"ss_face_crop_aug_range\": null, \"ss_training_started_at\": null, \"ss_caption_dropout_rate\": null, \"ss_training_finished_at\": null, \"ss_batch_size_per_device\": null, \"ss_num_batches_per_epoch\": null, \"ss_gradient_checkpointing\": null, \"ss_sd_scripts_commit_hash\": null, \"ss_caption_tag_dropout_rate\": null, \"ss_gradient_accumulation_steps\": null, \"ss_tag_frequency_translate_map\": {}, \"ss_caption_dropout_every_n_epochs\": null}', NULL, NULL, 0, 1, NULL, 1, '2025-05-26 22:20:01', 0, 'SDXL');
INSERT INTO `sd_user_model` VALUES (1927006813242077184, 1, 'realism_lora_comfy_converted', 'realism_lora_comfy_converted', 'realism_lora_comfy_converted', '0.8', NULL, NULL, 'D:\\project\\ai_project\\models\\Lora\\comfy\\realism_lora_comfy_converted.safetensors', '{\"ss_v2\": null, \"ss_seed\": null, \"ss_epoch\": null, \"ss_lowram\": null, \"ss_unet_lr\": null, \"ss_flip_aug\": null, \"ss_clip_skip\": null, \"ss_color_aug\": null, \"ss_full_fp16\": null, \"ss_optimizer\": null, \"ss_num_epochs\": null, \"ss_resolution\": null, \"ss_session_id\": null, \"ss_bucket_info\": null, \"ss_keep_tokens\": null, \"ss_network_dim\": null, \"ss_output_name\": null, \"ss_random_crop\": null, \"ss_dataset_dirs\": null, \"ss_lr_scheduler\": null, \"ss_noise_offset\": null, \"sshs_model_hash\": null, \"ss_cache_latents\": null, \"ss_enable_bucket\": null, \"ss_learning_rate\": null, \"ss_max_grad_norm\": null, \"ss_min_snr_gamma\": null, \"ss_network_alpha\": null, \"ss_sd_model_hash\": null, \"ss_sd_model_name\": null, \"ss_tag_frequency\": null, \"sshs_legacy_hash\": null, \"ss_network_module\": null, \"ss_num_reg_images\": null, \"ssTagFrequencyList\": [], \"ss_lr_warmup_steps\": null, \"ss_max_bucket_reso\": null, \"ss_max_train_steps\": null, \"ss_min_bucket_reso\": null, \"ss_mixed_precision\": null, \"ss_shuffle_caption\": null, \"ss_text_encoder_lr\": null, \"ss_max_token_length\": null, \"ss_num_train_images\": null, \"ss_reg_dataset_dirs\": null, \"ss_total_batch_size\": null, \"ss_training_comment\": null, \"ss_bucket_no_upscale\": null, \"ss_new_sd_model_hash\": null, \"ss_prior_loss_weight\": null, \"ss_face_crop_aug_range\": null, \"ss_training_started_at\": null, \"ss_caption_dropout_rate\": null, \"ss_training_finished_at\": null, \"ss_batch_size_per_device\": null, \"ss_num_batches_per_epoch\": null, \"ss_gradient_checkpointing\": null, \"ss_sd_scripts_commit_hash\": null, \"ss_caption_tag_dropout_rate\": null, \"ss_gradient_accumulation_steps\": null, \"ss_tag_frequency_translate_map\": {}, \"ss_caption_dropout_every_n_epochs\": null}', NULL, NULL, 0, 1, NULL, 1, '2025-05-26 22:20:01', 0, 'FLUX');

-- ----------------------------
-- Table structure for sd_user_model_classify
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_classify`;
CREATE TABLE `sd_user_model_classify`  (
  `id` bigint NOT NULL COMMENT '分类ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类名称',
  `crt_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `crt_user_id`(`crt_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sd_user_model_classify
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_model_classify_tmp
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_classify_tmp`;
CREATE TABLE `sd_user_model_classify_tmp`  (
  `model_id` bigint NOT NULL,
  `classify_id` bigint NOT NULL,
  `crt_user_id` bigint NOT NULL,
  PRIMARY KEY (`model_id`, `classify_id`, `crt_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型分类(自定义分类)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_model_classify_tmp
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_model_file
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_file`;
CREATE TABLE `sd_user_model_file`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `category` int NULL DEFAULT 0 COMMENT '分类[0-文生图，1-图生图]',
  `is_redraw` int NULL DEFAULT 0 COMMENT '是否局部重绘[0-否,1-是]',
  `summon_word` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '召唤词',
  `prompt` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述词(译文)',
  `prompt_zh` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述词(原文)',
  `prompt_desc` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '弃用词',
  `negative_prompt` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '反向提示词(译文)',
  `negative_prompt_zh` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '反向提示词(原文)',
  `model_strength` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型强度',
  `init_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参考图片',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件地址',
  `file_info` json NULL COMMENT '文件信息',
  `file_parameters` json NULL COMMENT '文件参数信息',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础大模型名称',
  `lora_model_id` bigint NULL DEFAULT NULL COMMENT 'lora模型ID',
  `lora_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型名称',
  `lora_title_zh` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型名称(中文)',
  `lora_info` json NULL COMMENT 'lora模型信息数组',
  `belong_user_id` bigint NULL DEFAULT NULL COMMENT '文件归属人ID',
  `belong_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件归属人名称',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `task_id`(`task_id` ASC) USING BTREE,
  INDEX `belong_user_id`(`belong_user_id` ASC) USING BTREE,
  INDEX `crt_time`(`crt_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户生图文件数据记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_model_file
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_model_log
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_log`;
CREATE TABLE `sd_user_model_log`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础大模型名称',
  `lora_model_id` bigint NULL DEFAULT NULL COMMENT 'lora模型ID',
  `lora_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型标题',
  `model_strength` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'lora模型强度',
  `use_times` int NULL DEFAULT 1 COMMENT '使用次数',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `lora_model_id` ASC) USING BTREE,
  INDEX `idx_user_crt_use`(`user_id` ASC, `crt_time` DESC, `use_times` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型使用记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_model_log
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_model_share
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_model_share`;
CREATE TABLE `sd_user_model_share`  (
  `model_id` bigint NOT NULL COMMENT '模型ID',
  `user_id` bigint NOT NULL COMMENT '被分享人ID',
  `crt_user_id` bigint NULL DEFAULT NULL COMMENT '分享人ID',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '分享时间',
  PRIMARY KEY (`model_id`, `user_id`) USING BTREE,
  INDEX `idx_model_user`(`model_id` ASC, `user_id` ASC) USING BTREE,
  CONSTRAINT `sd_user_model_share_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `sd_user_model` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户模型分享' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_model_share
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_msg
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_msg`;
CREATE TABLE `sd_user_msg`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `wx_open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信公众号openId',
  `template_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信公众号消息模板ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息标题',
  `msg_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `msg_body` json NULL COMMENT '消息体',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `user_id` bigint NULL DEFAULT NULL COMMENT '被通知人ID',
  `is_read` int NULL DEFAULT NULL COMMENT '是否已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '读取时间',
  `pre_task_id` bigint NULL DEFAULT NULL COMMENT '训练任务预处理ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 消息提醒' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_msg
-- ----------------------------

-- ----------------------------
-- Table structure for sd_user_task
-- ----------------------------
DROP TABLE IF EXISTS `sd_user_task`;
CREATE TABLE `sd_user_task`  (
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'WEBUI' COMMENT '任务类型[WEBUI,COMFYUI]',
  `status` int NULL DEFAULT NULL COMMENT '执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]',
  `flow` json NULL COMMENT 'comfy工作流',
  `category` int NULL DEFAULT 0 COMMENT '分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图]',
  `prompt_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy内部任务ID',
  `prompt` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy提示词(英)',
  `prompt_zh` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'comfy提示词(中)',
  `node_id` bigint NULL DEFAULT NULL COMMENT 'comfy任务执行的节点ID',
  `belong_user_id` bigint NULL DEFAULT NULL COMMENT '任务归属人ID',
  `belong_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务归属人',
  `crt_time` datetime NULL DEFAULT NULL COMMENT '任务创建时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '任务开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '任务结束时间',
  `reason` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `is_redraw` int NULL DEFAULT 0 COMMENT '是否局部重绘(仅在图生图启用:0-否,1-是)',
  `upd_time` datetime NULL DEFAULT NULL COMMENT '任务更新时间',
  PRIMARY KEY (`task_id`) USING BTREE,
  UNIQUE INDEX `task_id`(`task_id` ASC) USING BTREE,
  UNIQUE INDEX `prompt_id`(`prompt_id` ASC) USING BTREE,
  INDEX `status`(`status` ASC, `belong_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'SD绘图 || 用户任务执行记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sd_user_task
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` bigint NOT NULL COMMENT '参数主键',
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

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-03-03 00:45:29', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
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
  INDEX `dict_type`(`dict_type` ASC) USING BTREE
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

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (11, '用户渠道来源', 'sys_user_channel', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '用户渠道来源');
INSERT INTO `sys_dict_type` VALUES (1769555471065214978, '违禁词', 'sys_weijin_code', '0', 'admin', '2024-03-18 10:44:36', 'admin', '2024-05-12 19:38:11', 'sex toy,anal sphincter,pubic hair,anus,glans penis,sexual products,phallus,vaginal region,penis,clitoris,vulva');
INSERT INTO `sys_dict_type` VALUES (1776174345753825281, '模型生图状态', 'model_t2i', '0', 'admin', '2024-04-05 17:05:38', 'admin', '2024-04-05 17:05:38', NULL);
INSERT INTO `sys_dict_type` VALUES (1779890419347816450, '微信openId', 'sys_wechat_openid', '0', 'admin', '2024-04-15 23:11:59', 'admin', '2024-04-15 23:11:59', NULL);

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL COMMENT '访问ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `user_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户类型',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (1764143467657515009, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-03 12:19:14');
INSERT INTO `sys_logininfor` VALUES (1764578898593271809, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-04 17:09:28');
INSERT INTO `sys_logininfor` VALUES (1765394902021832706, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-06 23:11:59');
INSERT INTO `sys_logininfor` VALUES (1765762498135162881, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-07 23:32:40');
INSERT INTO `sys_logininfor` VALUES (1766101475123896322, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-08 21:59:39');
INSERT INTO `sys_logininfor` VALUES (1766114270175395841, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-08 22:50:29');
INSERT INTO `sys_logininfor` VALUES (1766484446448316417, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-09 23:21:26');
INSERT INTO `sys_logininfor` VALUES (1769010137739948033, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-03-16 22:37:38');
INSERT INTO `sys_logininfor` VALUES (1838096395082256386, 1838096394063040512, 'bs_user', '18852862861', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-09-23 14:01:45');
INSERT INTO `sys_logininfor` VALUES (1838097015587590145, 1838096394063040512, 'bs_user', '18852862861', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-09-23 14:04:13');
INSERT INTO `sys_logininfor` VALUES (1838100119477399553, 1838096394063040512, 'bs_user', '18852862861', '192.168.3.7', '内网IP', 'Safari', 'iPhone', '0', '登录成功', '2024-09-23 14:16:33');
INSERT INTO `sys_logininfor` VALUES (1838111336749182978, 1838096394063040512, 'bs_user', '18852862861', '192.168.3.7', '内网IP', 'Safari', 'iPhone', '0', '登录成功', '2024-09-23 15:01:08');
INSERT INTO `sys_logininfor` VALUES (1838111755193921538, 1838096394063040512, 'bs_user', '18852862861', '192.168.3.7', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-09-23 15:02:48');
INSERT INTO `sys_logininfor` VALUES (1838111939223203841, 1838096394063040512, 'bs_user', '18852862861', '192.168.3.7', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-09-23 15:03:31');
INSERT INTO `sys_logininfor` VALUES (1838114653684477954, 1838096394063040512, 'bs_user', '18852862861', '192.168.3.7', '内网IP', 'MicroMessenger', 'OSX', '0', '登录成功', '2024-09-23 15:14:19');
INSERT INTO `sys_logininfor` VALUES (1838136225862365185, 1838096394063040512, 'bs_user', '18852862861', '192.168.3.7', '内网IP', 'MicroMessenger', 'OSX', '0', '登录成功', '2024-09-23 16:40:02');
INSERT INTO `sys_logininfor` VALUES (1838177534090260481, NULL, NULL, 'test', '192.168.3.7', '内网IP', 'Chrome', 'Windows 10 or Windows Server 2016', '1', '验证码已失效', '2024-09-23 19:24:10');
INSERT INTO `sys_logininfor` VALUES (1838177545737842689, NULL, NULL, 'test', '192.168.3.7', '内网IP', 'Chrome', 'Windows 10 or Windows Server 2016', '1', '验证码已失效', '2024-09-23 19:24:13');
INSERT INTO `sys_logininfor` VALUES (1870140397524467713, NULL, NULL, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '1', '验证码已失效', '2024-12-21 00:13:11');
INSERT INTO `sys_logininfor` VALUES (1870140666953973762, NULL, NULL, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '1', '验证码已失效', '2024-12-21 00:14:15');
INSERT INTO `sys_logininfor` VALUES (1870141114490404865, 1838096394063040512, 'bs_user', '18852862861', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '1', '密码输入错误1次', '2024-12-21 00:16:02');
INSERT INTO `sys_logininfor` VALUES (1870141144555175938, 1838096394063040512, 'bs_user', '18852862861', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-12-21 00:16:09');
INSERT INTO `sys_logininfor` VALUES (1870321274648866818, 1838096394063040512, 'bs_user', '18852862861', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-12-21 12:11:55');
INSERT INTO `sys_logininfor` VALUES (1871473998694297601, 1838096394063040512, 'bs_user', '18852862861', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2024-12-24 16:32:26');
INSERT INTO `sys_logininfor` VALUES (1880155490219823105, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-01-17 15:29:35');
INSERT INTO `sys_logininfor` VALUES (1880556081429458945, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-01-18 18:01:23');
INSERT INTO `sys_logininfor` VALUES (1926479546434793474, 1, 'sys_user', 'admin', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-05-25 11:24:51');
INSERT INTO `sys_logininfor` VALUES (1926482296363102209, 1838096394063040512, 'bs_user', '18852862861', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-05-25 11:35:46');
INSERT INTO `sys_logininfor` VALUES (1927003762267598850, 1838096394063040512, 'bs_user', '18852862861', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-05-26 22:07:54');
INSERT INTO `sys_logininfor` VALUES (1927003762267598851, 1838096394063040512, 'bs_user', '18852862861', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-05-26 22:07:54');
INSERT INTO `sys_logininfor` VALUES (1927003915942703106, 1, 'sys_user', 'admin', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-05-26 22:08:30');
INSERT INTO `sys_logininfor` VALUES (1927007946438197249, 1, 'sys_user', 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-05-26 22:24:31');
INSERT INTO `sys_logininfor` VALUES (1935319697667768322, 1838096394063040512, 'bs_user', '18852862861', '127.0.0.1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-06-18 20:52:27');
INSERT INTO `sys_logininfor` VALUES (1945865721715658753, 1838096394063040512, 'bs_user', '18852862861', '0:0:0:0:0:0:0:1', '内网IP', 'MSEdge', 'Windows 10 or Windows Server 2016', '0', '登录成功', '2025-07-17 23:18:35');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2024-03-03 00:45:30', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2024-03-03 00:45:30', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool', NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2024-03-03 00:45:30', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2024-03-03 00:45:30', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2024-03-03 00:45:30', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2024-03-03 00:45:30', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2024-03-03 00:45:30', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2024-03-03 00:45:30', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2024-03-03 00:45:30', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2024-03-03 00:45:30', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2024-03-03 00:45:30', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2024-03-03 00:45:30', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2024-03-03 00:45:30', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (112, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2024-03-03 00:45:30', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2024-03-03 00:45:30', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114, '表单构建', 3, 1, 'build', 'tool/build/index', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2024-03-03 00:45:30', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (115, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2024-03-03 00:45:30', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (117, 'Admin监控', 2, 5, 'Admin', 'monitor/admin/index', '', 1, 0, 'C', '1', '1', 'monitor:admin:list', 'dashboard', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-03 01:41:51', 'Admin监控菜单');
INSERT INTO `sys_menu` VALUES (118, '文件管理', 1, 10, 'oss', 'system/oss/index', '', 1, 0, 'C', '0', '0', 'system:oss:list', 'upload', 'admin', '2024-03-03 00:45:30', '', NULL, '文件管理菜单');
INSERT INTO `sys_menu` VALUES (120, '任务调度中心', 2, 5, 'XxlJob', 'monitor/xxljob/index', '', 1, 0, 'C', '1', '1', 'monitor:xxljob:list', 'job', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-03 01:41:41', 'Xxl-Job控制台菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2024-03-03 00:45:30', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2024-03-03 00:45:30', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1001, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单查询', 102, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单新增', 102, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单修改', 102, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '菜单删除', 102, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典查询', 105, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典新增', 105, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典修改', 105, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典删除', 105, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '字典导出', 105, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数查询', 106, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数新增', 106, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数修改', 106, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数删除', 106, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '参数导出', 106, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告查询', 107, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告新增', 107, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告修改', 107, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '公告删除', 107, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作查询', 500, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '操作删除', 500, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '日志导出', 500, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录查询', 501, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '登录删除', 501, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '日志导出', 501, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '账户解锁', 501, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 115, 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 115, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 115, 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 115, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 115, 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 115, 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1600, '文件查询', 118, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:query', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1601, '文件上传', 118, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:upload', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1602, '文件下载', 118, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:download', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1603, '文件删除', 118, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:remove', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1604, '配置添加', 118, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:add', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1605, '配置编辑', 118, 6, '#', '', '', 1, 0, 'F', '0', '0', 'system:oss:edit', '#', 'admin', '2024-03-03 00:45:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1768642467876368386, '模型管理', 0, 1, 'sdModel', NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'druid', 'admin', '2024-03-15 22:16:39', 'admin', '2024-03-15 22:16:39', '');
INSERT INTO `sys_menu` VALUES (1768661287768748033, '模型训练器', 0, 1, 'sdTrainer', 'sdModel/modelTrain/index', NULL, 1, 0, 'C', '1', '1', NULL, 'server', 'admin', '2024-03-15 23:31:26', 'admin', '2024-04-05 10:11:31', '');
INSERT INTO `sys_menu` VALUES (1770129685866409986, 'sd大模型管理', 1768642467876368386, 1, 'sdxlModel', 'sdModel/sdxlModel/index', NULL, 1, 0, 'C', '0', '0', NULL, 'bug', 'admin', '2024-03-20 00:46:19', 'admin', '2024-03-20 00:46:19', '');
INSERT INTO `sys_menu` VALUES (1770129928745971713, 'lora模型测试列表', 1768642467876368386, 1, 'loraModel', 'sdModel/loraModel/index', NULL, 1, 0, 'C', '0', '0', NULL, 'dashboard', 'admin', '2024-03-20 00:47:17', 'admin', '2024-03-31 11:37:19', '');
INSERT INTO `sys_menu` VALUES (1770130089832411138, 'lora模型管理', 1768642467876368386, 1, 'modelTest', 'sdModel/modelTest/index', NULL, 1, 0, 'C', '0', '0', NULL, 'druid', 'admin', '2024-03-20 00:47:55', 'admin', '2024-03-30 22:42:18', '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2024-03-03 00:45:30', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2024-03-03 00:45:30', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1763982897708806146, '菜单管理', 3, 'com.sutran.sd.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/4', '0:0:0:0:0:0:0:1', '内网IP', '{}', '{\"code\":601,\"msg\":\"菜单已分配,不允许删除\",\"data\":null}', 0, '', '2024-03-03 01:41:11');
INSERT INTO `sys_oper_log` VALUES (1763982953245585410, '角色管理', 2, 'com.sutran.sd.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2024-03-03 00:45:30\",\"updateBy\":\"admin\",\"updateTime\":\"2024-03-03 01:41:23\",\"roleId\":2,\"roleName\":\"普通角色\",\"roleKey\":\"common\",\"roleSort\":2,\"dataScope\":\"2\",\"menuCheckStrictly\":true,\"deptCheckStrictly\":true,\"status\":\"0\",\"delFlag\":\"0\",\"remark\":\"普通角色\",\"flag\":false,\"menuIds\":[1,2,100,1001,1002,1003,1004,1005,1006,1007,101,1008,1009,1010,1011,1012,102,1013,1014,1015,1016,103,1017,1018,1019,1020,104,1021,1022,1023,1024,1025,105,1026,1027,1028,1029,1030,106,1031,1032,1033,1034,1035,107,1036,1037,1038,1039,108,500,1040,1041,1042,501,1043,1044,1045,1050,109,1046,1047,1048,113,112,3,114,115,1055,1056,1058,1057,1059,1060],\"deptIds\":null,\"admin\":false}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-03 01:41:24');
INSERT INTO `sys_oper_log` VALUES (1763982982249197569, '菜单管理', 3, 'com.sutran.sd.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/4', '0:0:0:0:0:0:0:1', '内网IP', '{}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-03 01:41:31');
INSERT INTO `sys_oper_log` VALUES (1763983023504371714, '菜单管理', 2, 'com.sutran.sd.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2024-03-03 00:45:30\",\"updateBy\":\"admin\",\"updateTime\":\"2024-03-03 01:41:40\",\"parentName\":null,\"parentId\":2,\"children\":[],\"menuId\":120,\"menuName\":\"任务调度中心\",\"orderNum\":5,\"path\":\"XxlJob\",\"component\":\"monitor/xxljob/index\",\"queryParam\":\"\",\"isFrame\":\"1\",\"isCache\":\"0\",\"menuType\":\"C\",\"visible\":\"1\",\"status\":\"1\",\"perms\":\"monitor:xxljob:list\",\"icon\":\"job\",\"remark\":\"Xxl-Job控制台菜单\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-03 01:41:41');
INSERT INTO `sys_oper_log` VALUES (1763983068312121346, '菜单管理', 2, 'com.sutran.sd.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2024-03-03 00:45:30\",\"updateBy\":\"admin\",\"updateTime\":\"2024-03-03 01:41:51\",\"parentName\":null,\"parentId\":2,\"children\":[],\"menuId\":117,\"menuName\":\"Admin监控\",\"orderNum\":5,\"path\":\"Admin\",\"component\":\"monitor/admin/index\",\"queryParam\":\"\",\"isFrame\":\"1\",\"isCache\":\"0\",\"menuType\":\"C\",\"visible\":\"1\",\"status\":\"1\",\"perms\":\"monitor:admin:list\",\"icon\":\"dashboard\",\"remark\":\"Admin监控菜单\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-03 01:41:51');
INSERT INTO `sys_oper_log` VALUES (1766285534131212290, '对象存储配置', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":\"admin\",\"secretKey\":\"sd2024@.\",\"bucketName\":\"sd\",\"prefix\":\"\",\"endpoint\":\"106.75.214.61:8081\",\"domain\":\"\",\"isHttps\":\"N\",\"status\":\"0\",\"region\":\"\",\"ext1\":\"\",\"remark\":null,\"accessPolicy\":\"1\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 10:11:02');
INSERT INTO `sys_oper_log` VALUES (1766285555333419010, '对象存储配置', 3, 'com.sutran.sd.web.controller.system.SysOssConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/oss/config/5', '0:0:0:0:0:0:0:1', '内网IP', '{}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 10:11:07');
INSERT INTO `sys_oper_log` VALUES (1766285569715687426, '对象存储配置', 3, 'com.sutran.sd.web.controller.system.SysOssConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/oss/config/4', '0:0:0:0:0:0:0:1', '内网IP', '{}', '', 1, '系统内置, 不可删除!', '2024-03-09 10:11:10');
INSERT INTO `sys_oper_log` VALUES (1766467572528439297, '对象存储配置', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":\"admin\",\"secretKey\":\"sd2024@.\",\"bucketName\":\"sutran\",\"prefix\":\"\",\"endpoint\":\"106.75.214.61:8081\",\"domain\":\"\",\"isHttps\":\"N\",\"status\":\"0\",\"region\":\"\",\"ext1\":\"\",\"remark\":\"\",\"accessPolicy\":\"1\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 22:14:23');
INSERT INTO `sys_oper_log` VALUES (1766467582909341698, '对象存储状态修改', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.changeStatus()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config/changeStatus', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":null,\"secretKey\":null,\"bucketName\":null,\"prefix\":null,\"endpoint\":null,\"domain\":null,\"isHttps\":null,\"status\":\"1\",\"region\":null,\"ext1\":null,\"remark\":null,\"accessPolicy\":null}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 22:14:26');
INSERT INTO `sys_oper_log` VALUES (1766467591566385153, '对象存储状态修改', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.changeStatus()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config/changeStatus', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":null,\"secretKey\":null,\"bucketName\":null,\"prefix\":null,\"endpoint\":null,\"domain\":null,\"isHttps\":null,\"status\":\"0\",\"region\":null,\"ext1\":null,\"remark\":null,\"accessPolicy\":null}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 22:14:28');
INSERT INTO `sys_oper_log` VALUES (1766468881570615298, '对象存储配置', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":\"admin\",\"secretKey\":\"sd2024@.\",\"bucketName\":\"sutran\",\"prefix\":\"\",\"endpoint\":\"106.75.214.61:8081\",\"domain\":\"106.75.214.61:8081\",\"isHttps\":\"N\",\"status\":\"0\",\"region\":\"\",\"ext1\":\"\",\"remark\":\"\",\"accessPolicy\":\"1\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 22:19:35');
INSERT INTO `sys_oper_log` VALUES (1766468968145244162, 'OSS对象存储', 1, 'com.sutran.sd.web.controller.system.SysOssController.upload()', 'POST', 1, 'admin', '研发部门', '/system/oss/upload', '0:0:0:0:0:0:0:1', '内网IP', '', '', 1, '创建Bucket失败, 请核对配置信息:[The request signature we calculated does not match the signature you provided. Check your key and signing method. (Service: Amazon S3; Status Code: 403; Error Code: SignatureDoesNotMatch; Request ID: 17BB1E7D9CF5AB31; S3 Extended Request ID: dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8; Proxy: null)]', '2024-03-09 22:19:56');
INSERT INTO `sys_oper_log` VALUES (1766469544547471362, 'OSS对象存储', 1, 'com.sutran.sd.web.controller.system.SysOssController.upload()', 'POST', 1, 'admin', '研发部门', '/system/oss/upload', '0:0:0:0:0:0:0:1', '内网IP', '', '', 1, '创建Bucket失败, 请核对配置信息:[The request signature we calculated does not match the signature you provided. Check your key and signing method. (Service: Amazon S3; Status Code: 403; Error Code: SignatureDoesNotMatch; Request ID: 17BB1E9BE21AAAF7; S3 Extended Request ID: dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8; Proxy: null)]', '2024-03-09 22:22:13');
INSERT INTO `sys_oper_log` VALUES (1766470020261236737, 'OSS对象存储', 1, 'com.sutran.sd.web.controller.system.SysOssController.upload()', 'POST', 1, 'admin', '研发部门', '/system/oss/upload', '0:0:0:0:0:0:0:1', '内网IP', '', '', 1, '创建Bucket失败, 请核对配置信息:[The request signature we calculated does not match the signature you provided. Check your key and signing method. (Service: Amazon S3; Status Code: 403; Error Code: SignatureDoesNotMatch; Request ID: 17BB1EB803E4174F; S3 Extended Request ID: dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8; Proxy: null)]', '2024-03-09 22:24:07');
INSERT INTO `sys_oper_log` VALUES (1766471433842348034, 'OSS对象存储', 1, 'com.sutran.sd.web.controller.system.SysOssController.upload()', 'POST', 1, 'admin', '研发部门', '/system/oss/upload', '0:0:0:0:0:0:0:1', '内网IP', '', '', 1, '创建Bucket失败, 请核对配置信息:[The request signature we calculated does not match the signature you provided. Check your key and signing method. (Service: Amazon S3; Status Code: 403; Error Code: SignatureDoesNotMatch; Request ID: 17BB1F067AD10947; S3 Extended Request ID: dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8; Proxy: null)]', '2024-03-09 22:29:44');
INSERT INTO `sys_oper_log` VALUES (1766472388851814402, 'OSS对象存储', 1, 'com.sutran.sd.web.controller.system.SysOssController.upload()', 'POST', 1, 'admin', '研发部门', '/system/oss/upload', '0:0:0:0:0:0:0:1', '内网IP', '', '{\"code\":200,\"msg\":\"操作成功\",\"data\":{\"ossId\":\"1766472388788899842\",\"url\":\"http://106.75.214.61:8081/sutran/2024/03/09/0c436d695c514a77bb99b80f0a78b9da.jpg\",\"fileName\":\"狼途.jpg\"}}', 0, '', '2024-03-09 22:33:32');
INSERT INTO `sys_oper_log` VALUES (1766472443327434754, 'OSS对象存储', 3, 'com.sutran.sd.web.controller.system.SysOssController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/oss/1766472388788899842', '0:0:0:0:0:0:0:1', '内网IP', '{}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-09 22:33:45');
INSERT INTO `sys_oper_log` VALUES (1769011506211319810, '对象存储配置', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config', '0:0:0:0:0:0:0:1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":\"admin\",\"secretKey\":\"sd2024@.\",\"bucketName\":\"sutran\",\"prefix\":\"\",\"endpoint\":\"106.75.214.61:8081\",\"domain\":\"106.75.214.61:8081\",\"isHttps\":\"N\",\"status\":\"0\",\"region\":\"\",\"ext1\":\"\",\"remark\":\"\",\"accessPolicy\":\"1\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2024-03-16 22:43:04');
INSERT INTO `sys_oper_log` VALUES (1926481404029120514, '对象存储配置', 2, 'com.sutran.sd.web.controller.system.SysOssConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/oss/config', '127.0.0.1', '内网IP', '{\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"ossConfigId\":1,\"configKey\":\"minio\",\"accessKey\":\"admin\",\"secretKey\":\"12345678\",\"bucketName\":\"sutran\",\"prefix\":\"\",\"endpoint\":\"127.0.0.1:9000\",\"domain\":\"127.0.0.1:9000\",\"isHttps\":\"N\",\"status\":\"0\",\"region\":\"\",\"ext1\":\"\",\"remark\":\"\",\"accessPolicy\":\"1\"}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2025-05-25 11:32:14');

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `oss_id` bigint NOT NULL COMMENT '对象存储主键',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '原名',
  `file_suffix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件后缀名',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'URL地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '上传人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新人',
  `service` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'minio' COMMENT '服务商',
  PRIMARY KEY (`oss_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OSS对象存储表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oss
-- ----------------------------
INSERT INTO `sys_oss` VALUES (1966429950629662721, '2025/09/12/8f2289c301334031831a01c1396d0903.jpg', 'SD_20250912_1966429950583525376.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/8f2289c301334031831a01c1396d0903.jpg', '2025-09-12 17:13:29', '18852862861', '2025-09-12 17:13:29', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966430549991538690, '2025/09/12/8e89f7f84cec47ccbacfc5f449ce5c8a.jpg', 'SD_20250912_1966430549895069696.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/8e89f7f84cec47ccbacfc5f449ce5c8a.jpg', '2025-09-12 17:15:52', '18852862861', '2025-09-12 17:15:52', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966431074011103234, '2025/09/12/a07a008e4da547cf80b43303ee673e2c.jpg', 'SD_20250912_1966431074069823488.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/a07a008e4da547cf80b43303ee673e2c.jpg', '2025-09-12 17:17:57', '18852862861', '2025-09-12 17:17:57', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966496867692351490, '2025/09/12/d92f279d9b6c417287f34788b0f9d8a7.jpg', 'SD_20250912_1966496867662991360.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/d92f279d9b6c417287f34788b0f9d8a7.jpg', '2025-09-12 21:39:24', '18852862861', '2025-09-12 21:39:24', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966496868002729985, '2025/09/12/e81a66e45c5b4d87916a2e77c8870cfb.jpg', 'SD_20250912_1966496868006924288.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/e81a66e45c5b4d87916a2e77c8870cfb.jpg', '2025-09-12 21:39:24', '18852862861', '2025-09-12 21:39:24', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966497453074575362, '2025/09/12/56770d750a1e4c008b96290fc03e0c41.jpg', 'SD_20250912_1966497453024243712.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/56770d750a1e4c008b96290fc03e0c41.jpg', '2025-09-12 21:41:43', '18852862861', '2025-09-12 21:41:43', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966497453322039297, '2025/09/12/df3695fd0bb24a8ab7cf23a3ebaa6cd3.jpg', 'SD_20250912_1966497453322039296.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/df3695fd0bb24a8ab7cf23a3ebaa6cd3.jpg', '2025-09-12 21:41:43', '18852862861', '2025-09-12 21:41:43', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966497941631307778, '2025/09/12/4062781346c74f99917b2939beefe435.jpg', 'SD_20250912_1966497941585170432.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/4062781346c74f99917b2939beefe435.jpg', '2025-09-12 21:43:40', '18852862861', '2025-09-12 21:43:40', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966497941857800193, '2025/09/12/e6cf53a325c541bf8c791a8feb87a4d5.jpg', 'SD_20250912_1966497941853605888.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/e6cf53a325c541bf8c791a8feb87a4d5.jpg', '2025-09-12 21:43:40', '18852862861', '2025-09-12 21:43:40', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966498062817370113, '2025/09/12/2a3bd1b52d734415b1ba8c7e04a0e243.jpg', 'SD_20250912_1966498062783815680.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/2a3bd1b52d734415b1ba8c7e04a0e243.jpg', '2025-09-12 21:44:09', '18852862861', '2025-09-12 21:44:09', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966498063014502401, '2025/09/12/531e216eccf349c5b1a827e5b7963389.jpg', 'SD_20250912_1966498063014502400.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/531e216eccf349c5b1a827e5b7963389.jpg', '2025-09-12 21:44:09', '18852862861', '2025-09-12 21:44:09', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966500239342379009, '2025/09/12/ba796f0f65084ef5bf40d65ef4c968e2.jpg', 'SD_20250912_1966500239338184704.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/ba796f0f65084ef5bf40d65ef4c968e2.jpg', '2025-09-12 21:52:48', '18852862861', '2025-09-12 21:52:48', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966500239640174593, '2025/09/12/68bfaa29ab1c4617936ce4204acda59c.jpg', 'SD_20250912_1966500239640174592.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/68bfaa29ab1c4617936ce4204acda59c.jpg', '2025-09-12 21:52:48', '18852862861', '2025-09-12 21:52:48', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966505604532477954, '2025/09/12/20732772b22e49eea6a750adac39ea06.jpg', 'SD_20250912_1966505604587003904.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/20732772b22e49eea6a750adac39ea06.jpg', '2025-09-12 22:14:07', '18852862861', '2025-09-12 22:14:07', '18852862861', 'minio');
INSERT INTO `sys_oss` VALUES (1966505604855439361, '2025/09/12/b1e62ed2a232482c83b0bd741557bcaa.jpg', 'SD_20250912_1966505604863827968.jpg', '.jpg', 'http://192.168.3.7:9000/sutran/2025/09/12/b1e62ed2a232482c83b0bd741557bcaa.jpg', '2025-09-12 22:14:07', '18852862861', '2025-09-12 22:14:07', '18852862861', 'minio');

-- ----------------------------
-- Table structure for sys_oss_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss_config`;
CREATE TABLE `sys_oss_config`  (
  `oss_config_id` bigint NOT NULL COMMENT '主建',
  `config_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置key',
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'accessKey',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '秘钥',
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '桶名称',
  `prefix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '前缀',
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '访问站点',
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '自定义域名',
  `is_https` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否https（Y=是,N=否）',
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '域',
  `access_policy` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '桶权限类型(0=private 1=public 2=custom)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否默认（0=是,1=否）',
  `ext1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '扩展字段',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`oss_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对象存储配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oss_config
-- ----------------------------
INSERT INTO `sys_oss_config` VALUES (1, 'minio', 'admin', '12345678', 'sutran', '', '192.168.3.7:9000', '192.168.3.7:9000', 'N', '', '1', '0', '', 'admin', '2024-03-03 00:45:30', 'admin', '2025-05-25 11:32:14', '');
INSERT INTO `sys_oss_config` VALUES (2, 'qiniu', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'ruoyi', '', 's3-cn-north-1.qiniucs.com', '', 'N', '', '1', '1', '', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-03 00:45:30', NULL);
INSERT INTO `sys_oss_config` VALUES (3, 'aliyun', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'ruoyi', '', 'oss-cn-beijing.aliyuncs.com', '', 'N', '', '1', '1', '', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-03 00:45:30', NULL);
INSERT INTO `sys_oss_config` VALUES (4, 'qcloud', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'ruoyi-1250000000', '', 'cos.ap-beijing.myqcloud.com', '', 'N', 'ap-beijing', '1', '1', '', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-03 00:45:30', NULL);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2024-03-03 00:45:29', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2024-03-03 00:45:29', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2024-03-03 00:45:29', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2024-03-03 00:45:29', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2024-03-03 00:45:30', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2024-03-03 00:45:30', 'admin', '2024-03-03 01:41:24', '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
INSERT INTO `sys_role_menu` VALUES (2, 114);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
INSERT INTO `sys_role_menu` VALUES (2, 1050);
INSERT INTO `sys_role_menu` VALUES (2, 1055);
INSERT INTO `sys_role_menu` VALUES (2, 1056);
INSERT INTO `sys_role_menu` VALUES (2, 1057);
INSERT INTO `sys_role_menu` VALUES (2, 1058);
INSERT INTO `sys_role_menu` VALUES (2, 1059);
INSERT INTO `sys_role_menu` VALUES (2, 1060);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `wx_open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `user_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'sys_user' COMMENT '用户类型（sys_user系统用户,bs_user-业务用户）',
  `channel_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '用户渠道来源ID',
  `channel` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '系统注册' COMMENT '用户渠道来源',
  `channel_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道用户ID',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `limit_valid_date` datetime NULL DEFAULT NULL COMMENT '限制有效期',
  `limit_train_times` int NULL DEFAULT NULL COMMENT '限制模型训练次数',
  `limit_draw_num` int NULL DEFAULT NULL COMMENT '限制绘图张数',
  `is_close_guide` int NULL DEFAULT 0 COMMENT '是否关闭引导[0-否,1-是]',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `wx_open_id`(`wx_open_id` ASC) USING BTREE,
  UNIQUE INDEX `phonenumber`(`phonenumber` ASC) USING BTREE,
  INDEX `user_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('1', 103, NULL, 'admin', '系统管理员', 'sys_user', '1', '系统注册', NULL, 'zhujun19950922@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '0:0:0:0:0:0:0:1', '2025-05-26 22:24:31', 'admin', '2024-03-03 00:45:29', 'admin', '2025-05-26 22:24:31', '管理员', NULL, NULL, NULL, 0);
INSERT INTO `sys_user` VALUES ('1838096394063040512', 103, NULL, '18852862861', '朱军', 'bs_user', '1', '系统注册', NULL, 'zhujun19950922@163.com', '18852862861', '0', NULL, '$2a$10$7YIRQRx5WYM4taFN3cVJleyW8Xn9FO2.cjPwm0bpeIZXshHr9syiK', '0', '0', '0:0:0:0:0:0:0:1', '2025-07-17 23:18:35', NULL, '2024-09-23 14:01:45', '18852862861', '2025-07-17 23:18:35', '管理员', NULL, NULL, NULL, 1);

-- ----------------------------
-- Table structure for sys_user_address
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_address`;
CREATE TABLE `sys_user_address`  (
  `id` bigint NOT NULL COMMENT '标签ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签名称',
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省(直辖市)',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '市(直辖市区)',
  `county` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区(直辖市县)',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  `is_default` tinyint NULL DEFAULT NULL COMMENT '是否默认地址[0-否,1-是]',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户物流地址关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_address
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_member
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_member`;
CREATE TABLE `sys_user_member`  (
  `id` bigint NOT NULL COMMENT '数据ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `level_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员名称',
  `start_time` datetime NULL DEFAULT NULL COMMENT '会员开始时间',
  `duration` int NULL DEFAULT NULL COMMENT '会员有效期(天)',
  `end_time` datetime NULL DEFAULT NULL COMMENT '会员结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0:已过期,1:有效)',
  `out_trade_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统订单号',
  `old_limit_train_times` int NULL DEFAULT 0 COMMENT '原始限制训练次数',
  `old_limit_draw_num` int NULL DEFAULT 0 COMMENT '原始限制绘图数量',
  `limit_train_times` int NULL DEFAULT NULL COMMENT '限制训练次数',
  `limit_draw_num` int NULL DEFAULT NULL COMMENT '限制绘图数量',
  `use_train_times` int NULL DEFAULT 0 COMMENT '已使用训练次数',
  `use_draw_num` int NULL DEFAULT 0 COMMENT '已使用绘图数量',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `end_time`(`end_time` ASC) USING BTREE,
  INDEX `status`(`status` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC, `status` ASC, `member_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户会员关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_member
-- ----------------------------
INSERT INTO `sys_user_member` VALUES (1959901384379236353, 1838096394063040512, 2, '99元/月黄金会员', '2025-08-25 16:51:18', 30, '2025-09-24 16:51:18', 0, '202508251645561959900039261745152', 0, 0, 80, 800, 0, 0, '购买相同会员叠加次数,当前会员失效', '2025-08-25 16:51:18', '2025-09-11 11:50:40');
INSERT INTO `sys_user_member` VALUES (1959922195173519361, 1838096394063040512, 2, '99元/月黄金会员', '2025-08-25 18:14:00', 30, '2025-09-24 18:14:00', 0, '202508251813391959922114223452160', 80, 800, 110, 1100, 0, 0, '购买相同会员叠加次数', '2025-08-25 18:14:00', '2025-09-11 11:50:39');
INSERT INTO `sys_user_member` VALUES (1959923718485970946, 1838096394063040512, 2, '99元/月黄金会员', '2025-08-25 18:20:03', 30, '2025-09-24 18:20:03', 1, '202508251819481959923659895738368', 110, 1100, 140, 1400, 0, 0, NULL, '2025-08-25 18:20:03', '2025-09-11 11:50:37');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_tag
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_tag`;
CREATE TABLE `sys_user_tag`  (
  `id` bigint NOT NULL COMMENT '标签ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签名称',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签来源[DEFINE-自定义,SYSTEM-系统]',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_tag
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
