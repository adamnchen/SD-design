package com.sutran.sd.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.system.domain.SdTranslation;
import com.sutran.sd.system.mapper.SdTranslationMapper;
import com.sutran.sd.system.service.SdTranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sutran.sd.common.constant.CacheConstants.*;

/**
 * @author zj
 * @date 2025年09月13日 23:19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdTranslationServiceImpl implements SdTranslationService {
    private final SdTranslationMapper sdTranslationMapper;

    /**
     * 从redis同步翻译字典到数据库
     * @param type 类型[0-提示词英译中,1-tag标签中译英,2-共性词翻译,3-标签词翻译]
     */
    @Override
    @Async("threadPoolTaskExecutor")
    public void syncTranslationFromRedisToDb(Integer type) {
        // 提示词英译中
        if (type==null || type==0) {
            // 清除数据库中旧数据
            sdTranslationMapper.delete(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType,0));
            Map<String,String> map = RedisUtils.getCacheMap(TRANSLATE_EN_TO_ZH_MAP);
            map.forEach((k,v)->{
                SdTranslation sdTranslation = new SdTranslation();
                sdTranslation.setEn(k);
                sdTranslation.setZh(v);
                sdTranslation.setType(0);
                sdTranslationMapper.insert(sdTranslation);
            });
        }
        // tag标签中译英
        else if (type==1) {
            // 清除数据库中旧数据
            sdTranslationMapper.delete(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType,1));
            Map<String, String> cacheMap = RedisUtils.getCacheMap(TRANSLATE_ZH_TO_EN_MAP);
            cacheMap.forEach((k,v)->{
                SdTranslation sdTranslation = new SdTranslation();
                sdTranslation.setZh(k);
                sdTranslation.setEn(v);
                sdTranslation.setType(1);
                sdTranslationMapper.insert(sdTranslation);
            });
        }
        // 共性词
        else if (type==2) {
            // 清除数据库中旧数据
            sdTranslationMapper.delete(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType,2));
            Collection<String> keys = RedisUtils.keys(TRAIN_ADDITION_LIST+"*");
            if (CollectionUtil.isEmpty(keys)) {
                return;
            }
            keys.forEach(trainTaskId->{
                Set<String> list = RedisUtils.getCacheSet(trainTaskId);
                if (CollectionUtil.isNotEmpty(list)) {
                    list.forEach(item->{
                        SdTranslation sdTranslation = new SdTranslation();
                        sdTranslation.setEn(item);
                        sdTranslation.setType(2);
                        sdTranslation.setTrainTaskId(Long.valueOf(trainTaskId));
                        sdTranslationMapper.insert(sdTranslation);
                    });
                }
            });
        }
        // 标签词英译中
        else if (type==3) {
            // 清除数据库中旧数据
            sdTranslationMapper.delete(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType,3));
            Collection<String> keys = RedisUtils.keys(TRAIN_TAG_TRANSLATE_MAP+"*");
            if (CollectionUtil.isEmpty(keys)) {
                return;
            }
            keys.forEach(trainTaskId->{
                Map<String, String> cacheMap = RedisUtils.getCacheMap(trainTaskId);
                cacheMap.forEach((k, v)->{
                    SdTranslation sdTranslation = new SdTranslation();
                    sdTranslation.setZh(k);
                    sdTranslation.setEn(v);
                    sdTranslation.setType(3);
                    sdTranslation.setTrainTaskId(Long.valueOf(trainTaskId));
                    sdTranslationMapper.insert(sdTranslation);
                });
            });
        }
    }

    /**
     * 从数据库同步翻译字典到redis
     * @param type 类型[0-提示词英译中,1-tag标签中译英,2-共性词翻译,3-标签词翻译]
     */
    @Override
    @Async("threadPoolTaskExecutor")
    public void syncTranslationFromDbToRedis(Integer type) {
        // 提示词英译中
        if (type==null || type==0) {
            // 清除redis中旧数据
            RedisUtils.deleteKey(TRANSLATE_EN_TO_ZH_MAP);
            List<SdTranslation> list = sdTranslationMapper.selectList(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType,0));
            if (CollectionUtil.isNotEmpty(list)){
                Map<String,String> map = list.stream().collect(Collectors.toMap(SdTranslation::getEn,SdTranslation::getZh));
                RedisUtils.setCacheMap(TRANSLATE_EN_TO_ZH_MAP,map);
            }
        }
        // tag标签中译英
        else if (type==1) {
            // 清除redis中旧数据
            RedisUtils.deleteKey(TRANSLATE_ZH_TO_EN_MAP);
            List<SdTranslation> list = sdTranslationMapper.selectList(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType, 1));
            if (CollectionUtil.isNotEmpty(list)) {
                Map<String, String> map = list.stream().collect(Collectors.toMap(SdTranslation::getZh, SdTranslation::getEn));
                RedisUtils.setCacheMap(TRANSLATE_ZH_TO_EN_MAP, map);
            }
        }
        // 共性词
        else if (type==2) {
            List<SdTranslation> list = sdTranslationMapper.selectList(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType, 2));
            if (CollectionUtil.isNotEmpty(list)) {
                list.stream().collect(Collectors.groupingBy(SdTranslation::getTrainTaskId)).forEach((trainTaskId,v)->{
                    Set<String> collect = v.stream().map(SdTranslation::getEn).collect(Collectors.toSet());
                    RedisUtils.setCacheSet(TRAIN_ADDITION_LIST+trainTaskId,collect);
                });
            }
        }
        // 标签词英译中
        else if (type==3) {
            List<SdTranslation> list = sdTranslationMapper.selectList(new LambdaQueryWrapper<SdTranslation>().eq(SdTranslation::getType, 3));
            if (CollectionUtil.isNotEmpty(list)) {
                list.stream().collect(Collectors.groupingBy(SdTranslation::getTrainTaskId)).forEach((trainTaskId,v)->{
                    Map<String, String> map = v.stream().collect(Collectors.toMap(SdTranslation::getEn, SdTranslation::getZh));
                    RedisUtils.setCacheMap(TRAIN_TAG_TRANSLATE_MAP+trainTaskId,map);
                });
            }
        }
    }
}
