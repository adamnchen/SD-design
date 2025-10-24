-- 为众筹支持表添加退款相关字段
-- 执行时间: 2025-10-24
-- 说明: 添加退款状态、退款时间、退款原因字段

ALTER TABLE `sd_crowdfunding_support`
    ADD COLUMN `status` INT DEFAULT 0 COMMENT '支持状态：0=正常，1=已取消，2=已退款',
    ADD COLUMN `refund_time` DATETIME NULL COMMENT '退款时间',
    ADD COLUMN `refund_reason` VARCHAR(500) NULL COMMENT '退款原因';

-- 为新字段添加索引以提高查询性能
CREATE INDEX idx_sd_crowdfunding_support_status ON sd_crowdfunding_support (status);
CREATE INDEX idx_sd_crowdfunding_support_refund_time ON sd_crowdfunding_support (refund_time);

-- 初始化现有记录的状态为正常
UPDATE `sd_crowdfunding_support` SET `status` = 0 WHERE `status` IS NULL;

-- 验证字段添加结果
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'sd_crowdfunding_support' 
  AND COLUMN_NAME IN ('status', 'refund_time', 'refund_reason');