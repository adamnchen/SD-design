package com.sutran.sd.design.service.impl;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.SearchResult;
import com.sutran.sd.design.service.IManufacturerSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManufacturerSearchServiceImpl implements IManufacturerSearchService {

    private final Client meiliSearchClient;

    @Override
    public SearchResult search(String query) throws Exception {
        // 1. 获取指向 "manufacturers" 索引的实例
        Index index = meiliSearchClient.index("manufacturers");

        // 2. 执行搜索！
        // index.search() 方法会将查询字符串传送给 MeiliSearch
        SearchResult results = index.search(query);

        // 3. 返回从 MeiliSearch 获取到的原始结果
        return results;
    }
}
