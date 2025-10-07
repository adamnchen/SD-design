package com.sutran.sd.design.service;

import com.meilisearch.sdk.model.SearchResult;

public interface IManufacturerSearchService {

    /**
     * 根据查询字符串进行搜索
     * @param query 查询的关键字或标签
     * @return MeiliSearch 返回的原始搜索结果
     * @throws Exception 抛出执行中的异常
     */
    SearchResult search(String query) throws Exception;
}
