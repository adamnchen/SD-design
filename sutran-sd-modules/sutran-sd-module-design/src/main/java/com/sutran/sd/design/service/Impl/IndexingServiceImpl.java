package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.entity.SysUserTag;
import com.sutran.sd.design.doc.ManufacturerDocument;
import com.sutran.sd.design.service.IIndexingService;
import com.sutran.sd.system.mapper.SysUserMapper;
import com.sutran.sd.system.mapper.SysUserTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IIndexingService {

    private final Client meiliSearchClient;
    private final SysUserMapper sysUserMapper;
    private final SysUserTagMapper sysUserTagMapper;
    private final Gson gson = new Gson();

    @Override
    public void indexSampleData() throws Exception {
        log.info("开始索引示例数据到 MeiliSearch...");
        Index index = meiliSearchClient.index("manufacturers");

        // 准备示例数据
        List<ManufacturerDocument> documents = new ArrayList<>();
        ManufacturerDocument m1 = new ManufacturerDocument();
        m1.setId(Long.valueOf("101"));
        m1.setName("老凤祥珠宝");
        m1.setTags(Arrays.asList("首饰", "黄金", "贵金属", "传统工艺"));
        documents.add(m1);


        // 将数据转换为 JSON
        String documentsJson = gson.toJson(documents);


        index.addDocuments(documentsJson, "id");

        log.info("示例数据索引任务已提交到 MeiliSearch！");
    }

    /**
     * 从数据库读取真实数据，并将其索引到 MeiliSearch
     */
    @Override
    public void indexDataFromDatabase() throws Exception {
        log.info("开始从数据库读取真实厂商数据并进行索引...");

        // 1. 筛选出作为“厂商”的用户 (此部分逻辑不变)
        QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user_type", "bs_user");
        List<SysUser> manufacturers = sysUserMapper.selectList(userWrapper);

        if (CollectionUtils.isEmpty(manufacturers)) {
            log.warn("数据库中没有找到符合条件的厂商用户，无需索引。");
            return;
        }


        List<Long> manufacturerIds = manufacturers.stream()
            .map(SysUser::getUserId)
            .collect(Collectors.toList());


        QueryWrapper<SysUserTag> tagWrapper = new QueryWrapper<>();
        tagWrapper.in("user_id", manufacturerIds);
        List<SysUserTag> allTags = sysUserTagMapper.selectList(tagWrapper);


        Map<Long, List<String>> tagsMap = allTags.stream()
            .collect(Collectors.groupingBy(
                SysUserTag::getUserId,
                Collectors.mapping(SysUserTag::getTagName, Collectors.toList())
            ));


        List<ManufacturerDocument> documents = new ArrayList<>();
        for (SysUser manufacturer : manufacturers) {
            ManufacturerDocument doc = new ManufacturerDocument();


            doc.setId(Long.valueOf(String.valueOf(manufacturer.getUserId())));
            doc.setName(manufacturer.getNickName());


            List<String> tags = tagsMap.getOrDefault(manufacturer.getUserId(), Collections.emptyList());
            doc.setTags(tags);

            documents.add(doc);
        }


        Index index = meiliSearchClient.index("manufacturers");
        String documentsJson = gson.toJson(documents);
        index.addDocuments(documentsJson, "id");

        log.info("成功将 {} 笔真实厂商数据索引任务提交到 MeiliSearch！", documents.size());
    }
}
