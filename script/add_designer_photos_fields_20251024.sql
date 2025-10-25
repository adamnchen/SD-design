-- 添加设计师照片字段到众筹项目表
-- 执行时间: 2025-10-24
-- 说明: 为众筹项目表添加设计师上传的实物照片字段

-- 1. 添加设计师照片字段
ALTER TABLE `sd_crowdfunding_project` 
ADD COLUMN `designer_photos` TEXT NULL COMMENT '设计师上传的实物照片（JSON格式，多张图片）',
ADD COLUMN `designer_upload_time` DATETIME NULL COMMENT '设计师上传照片时间';

-- 2. 添加索引
CREATE INDEX idx_sd_crowdfunding_project_designer_upload_time ON sd_crowdfunding_project (designer_upload_time);

-- 3. 验证字段添加成功
SELECT 
    COLUMN_NAME as '字段名',
    DATA_TYPE as '数据类型',
    IS_NULLABLE as '是否可空',
    COLUMN_COMMENT as '字段注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sd_crowdfunding_project'
  AND COLUMN_NAME IN ('designer_photos', 'designer_upload_time')
ORDER BY COLUMN_NAME;
