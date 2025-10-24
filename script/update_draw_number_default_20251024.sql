-- 更新抽奖数量默认值为2个
-- 执行时间: 2025-10-24
-- 说明: 将抽奖数量的最低要求从1个改为2个，并更新现有数据

-- 1. 更新打样邀约表中的抽奖数量
-- 将抽奖数量为1的记录更新为2
UPDATE `sd_proofing_invitations` 
SET `draw_number` = 2 
WHERE `draw_number` = 1;

-- 2. 更新众筹项目表中的抽奖数量
-- 将抽奖数量为1的记录更新为2
UPDATE `sd_crowdfunding_project` 
SET `draw_number` = 2 
WHERE `draw_number` = 1;

-- 3. 验证更新结果
SELECT 
    'sd_proofing_invitations' as '表名',
    COUNT(*) as '总记录数',
    COUNT(CASE WHEN draw_number = 2 THEN 1 END) as '抽奖数量为2的记录数',
    COUNT(CASE WHEN draw_number < 2 THEN 1 END) as '抽奖数量小于2的记录数'
FROM `sd_proofing_invitations`
WHERE `draw_number` IS NOT NULL

UNION ALL

SELECT 
    'sd_crowdfunding_project' as '表名',
    COUNT(*) as '总记录数',
    COUNT(CASE WHEN draw_number = 2 THEN 1 END) as '抽奖数量为2的记录数',
    COUNT(CASE WHEN draw_number < 2 THEN 1 END) as '抽奖数量小于2的记录数'
FROM `sd_crowdfunding_project`
WHERE `draw_number` IS NOT NULL;

-- 4. 显示更新后的抽奖数量分布
SELECT 
    'sd_proofing_invitations' as '表名',
    draw_number as '抽奖数量',
    COUNT(*) as '记录数'
FROM `sd_proofing_invitations`
WHERE `draw_number` IS NOT NULL
GROUP BY draw_number

UNION ALL

SELECT 
    'sd_crowdfunding_project' as '表名',
    draw_number as '抽奖数量',
    COUNT(*) as '记录数'
FROM `sd_crowdfunding_project`
WHERE `draw_number` IS NOT NULL
GROUP BY draw_number

ORDER BY '表名', '抽奖数量';
