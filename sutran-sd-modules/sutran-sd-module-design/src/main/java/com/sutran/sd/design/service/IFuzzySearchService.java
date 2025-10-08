package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.vo.ManufacturerSearchResultVO;
import java.util.List;

/**
 * 模糊匹配搜索服务接口
 * 替代 MeiliSearch 搜索引擎，基于数据库进行模糊匹配
 *
 * @author SutranSD
 */
public interface IFuzzySearchService {

    /**
     * 根据关键词模糊搜索厂商
     * @param keywords 搜索关键词，如："金手镯"
     * @return 匹配的厂商列表
     */
    List<ManufacturerSearchResultVO> searchManufacturers(String keywords);

    /**
     * 根据多个标签搜索厂商
     * @param tags 标签列表，如：["首饰", "珠宝", "金饰"]
     * @return 匹配的厂商列表
     */
    List<ManufacturerSearchResultVO> searchManufacturersByTags(List<String> tags);
}
