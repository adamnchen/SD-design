-- 为收藏表新增图片URL字段，供前端直接展示封面/缩略图
ALTER TABLE `sd_user_favorite`
  ADD COLUMN `image_url` varchar(512) NULL COMMENT '目标对象封面/缩略图URL' AFTER `target_id`;


