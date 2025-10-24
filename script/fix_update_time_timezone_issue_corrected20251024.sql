-- 修复数据库时区问题：移除所有表的 update_time 字段的 ON UPDATE CURRENT_TIMESTAMP
-- 执行时间: 2025-10-24
-- 说明: 移除自动更新时间戳，改为代码控制时间更新，避免时区问题

-- ===========================================
-- 修复实际存在的表（基于实际表结构文件）
-- ===========================================

-- 1. 修复 pay_order 表（支付订单表）
-- 原字段: `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `pay_order` 
    MODIFY COLUMN `update_time` DATETIME NULL DEFAULT NULL COMMENT '更新时间';

-- 2. 修复 sd_crowdfunding_support 表（众筹支持记录表）
-- 原字段: `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `sd_crowdfunding_support` 
    MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间';

-- 3. 修复 sd_presale_order 表（预售订单表）
-- 原字段: `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `sd_presale_order` 
    MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间';

-- 4. 修复 sd_presale_project 表（预售项目表）
-- 原字段: `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `sd_presale_project` 
    MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间';

-- 5. 修复 sd_crowdfunding_project 表（众筹项目表）
-- 原字段: `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `sd_crowdfunding_project` 
    MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间';

-- 6. 修复 sd_proofing_invitations 表（打样邀约表）
-- 原字段: `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间'
ALTER TABLE `sd_proofing_invitations` 
    MODIFY COLUMN `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录更新时间';

-- 7. 修复 sd_presale_delivery 表（预售发货表）
-- 原字段: `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `sd_presale_delivery` 
    MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间';

-- 8. 修复 sd_crowdfunding_sample_delivery 表（众筹样品发货表）
-- 原字段: `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
ALTER TABLE `sd_crowdfunding_sample_delivery` 
    MODIFY COLUMN `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间';

-- 9. 修复 sys_user_tag 表（用户标签表）
-- 原字段: `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
ALTER TABLE `sys_user_tag` 
    MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间';

-- ===========================================
-- 验证修复结果
-- ===========================================

-- 检查是否还有使用 ON UPDATE CURRENT_TIMESTAMP 的表
SELECT 
    TABLE_NAME as '表名',
    COLUMN_NAME as '字段名',
    COLUMN_DEFAULT as '默认值',
    EXTRA as '额外属性'
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND COLUMN_NAME IN ('update_time', 'updated_at')
  AND EXTRA LIKE '%on update%'
ORDER BY TABLE_NAME;

-- 如果上面的查询返回空结果，说明修复成功
-- 如果还有结果，说明还有表需要修复

-- ===========================================
-- 说明
-- ===========================================

/*
修复说明：
1. 移除了所有 update_time/updated_at 字段的 ON UPDATE CURRENT_TIMESTAMP 属性
2. 保留了 DEFAULT CURRENT_TIMESTAMP 作为默认值（用于新记录）
3. 时间更新现在由代码控制：
   - BaseEntity 使用 @TableField(fill = FieldFill.INSERT_UPDATE)
   - CreateAndUpdateMetaObjectHandler 自动填充当前时间
   - 避免了数据库时区与系统时区不匹配的问题

注意事项：
1. 执行此脚本后，所有 update_time 字段的更新将由代码控制
2. 确保 MyBatis-Plus 的自动填充机制正常工作
3. 建议在测试环境先验证修复效果
4. 如果数据库时区设置正确，也可以保留 ON UPDATE CURRENT_TIMESTAMP

修复的表列表：
- pay_order（支付订单表）
- sd_crowdfunding_support（众筹支持记录表）
- sd_presale_order（预售订单表）
- sd_presale_project（预售项目表）
- sd_crowdfunding_project（众筹项目表）
- sd_proofing_invitations（打样邀约表）
- sd_presale_delivery（预售发货表）
- sd_crowdfunding_sample_delivery（众筹样品发货表）
- sys_user_tag（用户标签表）
*/
