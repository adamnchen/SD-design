package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.entity.SysUserTag;
import com.sutran.sd.common.core.domain.vo.ManufacturerSearchResultVO;
import com.sutran.sd.common.constant.TagConstants;
import com.sutran.sd.design.service.IFuzzySearchService;
import com.sutran.sd.system.mapper.SysUserMapper;
import com.sutran.sd.system.mapper.SysUserTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.enums.WxMpApiUrl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模糊匹配搜索服务实现
 * 基于数据库进行模糊匹配，替代 MeiliSearch
 *
 * @author SutranSD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FuzzySearchServiceImpl implements IFuzzySearchService {

    private final SysUserMapper sysUserMapper;
    private final SysUserTagMapper sysUserTagMapper;

    @Override
    public List<ManufacturerSearchResultVO> searchManufacturers(String keywords) {
        if (!StringUtils.hasText(keywords)) {
            return Collections.emptyList();
        }

        log.info("开始模糊搜索厂商，关键词：{}", keywords);

        // 1. 获取所有厂商用户
        List<SysUser> manufacturers = getAllManufacturers();
        if (manufacturers.isEmpty()) {
            log.warn("数据库中没有找到厂商用户");
            return Collections.emptyList();
        }

        // 2. 获取所有厂商的标签
        Map<Long, List<SysUserTag>> userTagsMap = getUserTagsMap(manufacturers);

        // 3. 进行模糊匹配
        List<ManufacturerSearchResultVO> results = new ArrayList<>();
        for (SysUser manufacturer : manufacturers) {
            List<SysUserTag> userTags = userTagsMap.getOrDefault(manufacturer.getUserId(), Collections.emptyList());
            ManufacturerSearchResultVO result = calculateMatch(manufacturer, userTags, keywords);

            if (result.getMatchScore() > 0) {
                results.add(result);
            }
        }

        // 4. 按匹配度排序
        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        log.info("搜索完成，找到 {} 个匹配的厂商", results.size());
        return results;
    }

    @Override
    public List<ManufacturerSearchResultVO> searchManufacturersByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("开始根据标签搜索厂商，标签：{}", tags);
        log.info("搜索标签详情：{}", tags.stream().map(tag -> "'" + tag + "'").collect(Collectors.joining(", ")));

        // 1. 获取所有厂商用户
        List<SysUser> manufacturers = getAllManufacturers();
        if (manufacturers.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取所有厂商的标签
        Map<Long, List<SysUserTag>> userTagsMap = getUserTagsMap(manufacturers);
        log.info("找到 {} 个厂商，共 {} 个标签映射", manufacturers.size(), userTagsMap.size());

        // 3. 进行标签匹配
        List<ManufacturerSearchResultVO> results = new ArrayList<>();
        for (SysUser manufacturer : manufacturers) {
            List<SysUserTag> userTags = userTagsMap.getOrDefault(manufacturer.getUserId(), Collections.emptyList());
            log.debug("厂商 {} 的标签：{}", manufacturer.getNickName(), 
                userTags.stream().map(SysUserTag::getTagName).collect(Collectors.joining(", ")));
            
            ManufacturerSearchResultVO result = calculateTagMatch(manufacturer, userTags, tags);

            if (result.getMatchScore() > 0) {
                log.info("厂商 {} 匹配成功，得分：{}，匹配标签：{}", 
                    manufacturer.getNickName(), result.getMatchScore(), result.getMatchedTags());
                results.add(result);
            }
        }

        // 4. 按匹配度排序
        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        log.info("标签搜索完成，找到 {} 个匹配的厂商", results.size());
        return results;
    }

    /**
     * 获取所有厂商用户
     */
    private List<SysUser> getAllManufacturers() {
        // 1. 查询所有 bizType 为 "0" 的 SysUserTag 记录
        LambdaQueryWrapper<SysUserTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(SysUserTag::getBizType, "0");
        // 仅查询 userId 字段
        tagWrapper.select(SysUserTag::getUserId);

        List<SysUserTag> tagList = sysUserTagMapper.selectList(tagWrapper);

        if (tagList.isEmpty()) {
            return Collections.emptyList(); // 没有找到任何厂商用户标签，直接返回空列表
        }

        // 2. 提取所有关联的用户 ID
        Set<Long> userIds = tagList.stream()
            .map(SysUserTag::getUserId) // 假设 SysUserTag 有 getUserId() 方法
            .collect(Collectors.toSet());

        // 3. 根据用户 ID 集合批量查询 SysUser
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(SysUser::getUserId, userIds); // 假设 SysUser 的主键是 getId()

        return sysUserMapper.selectList(userWrapper); // 假设您有 SysUser 的 Mapper：sysUserMapper
    }

    /**
     * 获取用户标签映射
     */
    private Map<Long, List<SysUserTag>> getUserTagsMap(List<SysUser> manufacturers) {
        List<Long> userIds = manufacturers.stream()
                .map(SysUser::getUserId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<SysUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUserTag::getUserId, userIds)
               .eq(SysUserTag::getBizType, TagConstants.BUSINESS_TAG); // 只获取业务标签

        List<SysUserTag> allTags = sysUserTagMapper.selectList(wrapper);
        return allTags.stream()
                .collect(Collectors.groupingBy(SysUserTag::getUserId));
    }

    /**
     * 计算关键词匹配度
     */
    private ManufacturerSearchResultVO calculateMatch(SysUser manufacturer, List<SysUserTag> userTags, String keywords) {
        ManufacturerSearchResultVO result = new ManufacturerSearchResultVO();
        result.setUserId(manufacturer.getUserId());
        result.setNickName(manufacturer.getNickName());
        result.setAvatar(manufacturer.getAvatar());
        result.setDescription(manufacturer.getRemark());

        List<String> tagNames = userTags.stream()
                .map(SysUserTag::getTagName)
                .collect(Collectors.toList());
        result.setTags(tagNames);

        // 计算匹配度
        int matchScore = 0;
        List<String> matchedTags = new ArrayList<>();
        String lowerKeywords = keywords.toLowerCase();

        for (SysUserTag tag : userTags) {
            String tagName = tag.getTagName().toLowerCase();

            // 完全匹配
            if (tagName.equals(lowerKeywords)) {
                matchScore += 100;
                matchedTags.add(tag.getTagName());
            }
            // 包含匹配
            else if (tagName.contains(lowerKeywords) || lowerKeywords.contains(tagName)) {
                matchScore += 80;
                matchedTags.add(tag.getTagName());
            }
            // 模糊匹配（简单的字符相似度）
            else if (calculateSimilarity(tagName, lowerKeywords) > 0.6) {
                matchScore += 60;
                matchedTags.add(tag.getTagName());
            }
        }

        result.setMatchScore(Math.min(matchScore, 100));
        result.setMatchedTags(matchedTags);

        return result;
    }

    /**
     * 计算标签匹配度
     */
    private ManufacturerSearchResultVO calculateTagMatch(SysUser manufacturer, List<SysUserTag> userTags, List<String> searchTags) {
        ManufacturerSearchResultVO result = new ManufacturerSearchResultVO();
        result.setUserId(manufacturer.getUserId());
        result.setNickName(manufacturer.getNickName());
        result.setAvatar(manufacturer.getAvatar());
        result.setDescription(manufacturer.getRemark());

        List<String> tagNames = userTags.stream()
                .map(SysUserTag::getTagName)
                .collect(Collectors.toList());
        result.setTags(tagNames);

        // 计算匹配度
        int matchScore = 0;
        List<String> matchedTags = new ArrayList<>();

        log.debug("开始计算厂商 {} 的标签匹配度", manufacturer.getNickName());
        log.debug("用户标签：{}", userTags.stream().map(SysUserTag::getTagName).collect(Collectors.joining(", ")));
        log.debug("搜索标签：{}", searchTags);

        for (String searchTag : searchTags) {
            // 去除前后空格，避免空格导致的匹配问题
            String trimmedSearchTag = searchTag.trim();
            String lowerSearchTag = trimmedSearchTag.toLowerCase();
            log.debug("正在搜索标签：'{}' (小写：'{}')", searchTag, lowerSearchTag);

            for (SysUserTag userTag : userTags) {
                String tagName = userTag.getTagName().toLowerCase();
                log.debug("比较用户标签：'{}' (小写：'{}')", userTag.getTagName(), tagName);

                // 完全匹配
                if (tagName.equals(lowerSearchTag)) {
                    matchScore += 100;
                    log.debug("完全匹配！得分+100");
                    if (!matchedTags.contains(userTag.getTagName())) {
                        matchedTags.add(userTag.getTagName());
                    }
                }
                // 包含匹配
                else if (tagName.contains(lowerSearchTag) || lowerSearchTag.contains(tagName)) {
                    matchScore += 80;
                    log.debug("包含匹配！得分+80");
                    if (!matchedTags.contains(userTag.getTagName())) {
                        matchedTags.add(userTag.getTagName());
                    }
                }
                // 模糊匹配
                else if (calculateSimilarity(tagName, lowerSearchTag) > 0.6) {
                    matchScore += 60;
                    log.debug("模糊匹配！得分+60");
                    if (!matchedTags.contains(userTag.getTagName())) {
                        matchedTags.add(userTag.getTagName());
                    }
                } else {
                    log.debug("无匹配");
                }
            }
        }

        result.setMatchScore(Math.min(matchScore, 100));
        result.setMatchedTags(matchedTags);

        return result;
    }

    /**
     * 计算字符串相似度（简单的编辑距离算法）
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        if (s1.length() == 0 || s2.length() == 0) return 0.0;

        int maxLength = Math.max(s1.length(), s2.length());
        int editDistance = calculateEditDistance(s1, s2);

        return 1.0 - (double) editDistance / maxLength;
    }

    /**
     * 计算编辑距离
     */
    private int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    @Override
    public List<ManufacturerSearchResultVO> searchManufacturersByName(String name) {
        if (!StringUtils.hasText(name)) {
            return Collections.emptyList();
        }

        log.info("开始根据厂家名字模糊搜索，关键词：{}", name);

        // 1. 获取所有厂商用户
        List<SysUser> manufacturers = getAllManufacturers();
        if (manufacturers.isEmpty()) {
            log.warn("数据库中没有找到厂商用户");
            return Collections.emptyList();
        }

        // 2. 获取所有厂商的标签
        Map<Long, List<SysUserTag>> userTagsMap = getUserTagsMap(manufacturers);

        // 3. 进行名字模糊匹配
        List<ManufacturerSearchResultVO> results = new ArrayList<>();
        String lowerName = name.toLowerCase();

        for (SysUser manufacturer : manufacturers) {
            List<SysUserTag> userTags = userTagsMap.getOrDefault(manufacturer.getUserId(), Collections.emptyList());
            ManufacturerSearchResultVO result = calculateNameMatch(manufacturer, userTags, lowerName);

            if (result.getMatchScore() > 0) {
                results.add(result);
            }
        }

        // 4. 按匹配度排序
        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        log.info("根据名字搜索完成，找到 {} 个匹配的厂商", results.size());
        return results;
    }

    /**
     * 计算名字匹配度
     */
    private ManufacturerSearchResultVO calculateNameMatch(SysUser manufacturer, List<SysUserTag> userTags, String searchName) {
        ManufacturerSearchResultVO result = new ManufacturerSearchResultVO();
        result.setUserId(manufacturer.getUserId());
        result.setNickName(manufacturer.getNickName());
        result.setAvatar(manufacturer.getAvatar());
        result.setDescription(manufacturer.getRemark());

        List<String> tagNames = userTags.stream()
                .map(SysUserTag::getTagName)
                .collect(Collectors.toList());
        result.setTags(tagNames);

        // 计算名字匹配度
        int matchScore = 0;
        List<String> matchedTags = new ArrayList<>();

        // 检查用户昵称匹配
        if (StringUtils.hasText(manufacturer.getNickName())) {
            String nickName = manufacturer.getNickName().toLowerCase();

            // 完全匹配
            if (nickName.equals(searchName)) {
                matchScore += 100;
            }
            // 包含匹配
            else if (nickName.contains(searchName) || searchName.contains(nickName)) {
                matchScore += 80;
            }
            // 模糊匹配
            else if (calculateSimilarity(nickName, searchName) > 0.6) {
                matchScore += 60;
            }
        }

        // 检查用户名匹配
        if (StringUtils.hasText(manufacturer.getUserName())) {
            String userName = manufacturer.getUserName().toLowerCase();

            // 完全匹配
            if (userName.equals(searchName)) {
                matchScore += 90;
            }
            // 包含匹配
            else if (userName.contains(searchName) || searchName.contains(userName)) {
                matchScore += 70;
            }
            // 模糊匹配
            else if (calculateSimilarity(userName, searchName) > 0.6) {
                matchScore += 50;
            }
        }

        // 检查标签匹配（作为辅助匹配）
        for (SysUserTag tag : userTags) {
            String tagName = tag.getTagName().toLowerCase();

            if (tagName.contains(searchName) || searchName.contains(tagName)) {
                matchScore += 30;
                if (!matchedTags.contains(tag.getTagName())) {
                    matchedTags.add(tag.getTagName());
                }
            }
        }

        result.setMatchScore(Math.min(matchScore, 100));
        result.setMatchedTags(matchedTags);

        return result;
    }
}
