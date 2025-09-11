package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.BeanCopyUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdUserModelFile;
import com.sutran.sd.draw.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.draw.domain.vo.SdApiResult;
import com.sutran.sd.draw.domain.vo.SdUserModelFileVo;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.mapper.SdUserModelFileMapper;
import com.sutran.sd.draw.mapper.SdUserModelMapper;
import com.sutran.sd.draw.service.SdUserModelFileService;
import com.sutran.sd.system.service.SysTranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;
import static com.sutran.sd.common.constant.CacheConstants.TRANSLATE_EN_TO_ZH_MAP;

/**
 * @author zj
 * @date 2024-03-03
 */
@SuppressWarnings("unchecked")
@Slf4j
@Service
@RequiredArgsConstructor
public class SdUserModelFileServiceImpl implements SdUserModelFileService {

    private final SdUserModelFileMapper baseMapper;
    private final SdUserModelMapper sdUserModelMapper;
    private final SysTranslateService sysTranslateService;

    /**
     * 异步批量插入
     * @param rs     接口返回结果
     * @param userId        用户id
     * @param userName      用户名
     * @param loraInfos     lora信息
     * @param modelName     模型名称
     * @param taskId        任务id
     * @param category      类别
     * @param prompt        提示词
     * @param initImg       初始化图片
     * @param promptDesc    提示词描述
     * @param promptZh      提示词中文
     * @param summonWord    召唤词
     * @param negativePrompt    负面提示词
     * @param negativePromptZh  负面提示词中文
     * @param isRedraw      是否重绘
     */
    @Override
    public void asyncBatchInsert(SdApiResult rs, Long userId, String userName, List<JSONObject> loraInfos, String modelName, String taskId, int category, String prompt, String initImg, String promptDesc, String promptZh, String summonWord, String negativePrompt, String negativePromptZh, Integer isRedraw) {
        if (CollectionUtil.isEmpty(rs.getImages())) {
            return;
        }
        Integer isRedrawEntity = isRedraw==null?0:isRedraw;
        List<SdUserModelFile> list = rs.getImages().stream().map(url -> new SdUserModelFile().setIsRedraw(isRedrawEntity).setId(IdUtil.getSnowflakeNextId()).setTaskId(Long.parseLong(taskId))
            .setPrompt(prompt).setPromptZh(promptZh).setPromptDesc(promptDesc).setSummonWord(summonWord).setNegativePrompt(negativePrompt).setNegativePromptZh(negativePromptZh)
            .setFileInfo(JSONObject.toJSONString(rs.getInfo(), WriteMapNullValue)).setInitImg(initImg).setCategory(category).setFileUrl(url)
            .setFileParameters(JSONObject.toJSONString(rs.getParameters(), WriteMapNullValue)).setBelongUserId(userId).setBelongUserName(userName)
            .setLoraTitle(loraInfos.get(0).getString("loraTitle")).setLoraTitle(loraInfos.get(0).getString("loraTitleZh")).setLoraModelId(loraInfos.get(0).getLongValue("loraModelId")).setModelStrength(loraInfos.get(0).getString("modelStrength"))
            .setModelName(modelName).setLoraInfo(CollectionUtil.isNotEmpty(loraInfos)? JSONObject.toJSONString(loraInfos):null).setCrtTime(new Date())).collect(Collectors.toList());
        baseMapper.insertBatch(list);
    }

    @Override
    public void asyncBatchInsert(SdUserTaskVo sdUserTaskVo, List<String> urlList, String initImgUrl) {
        if (CollectionUtil.isEmpty(urlList)) {
            return;
        }
        List<SdUserModelFile> list = urlList.stream().map(url ->{
            SdUserModelFile entity = BeanCopyUtils.copy(sdUserTaskVo,SdUserModelFile.class);
            entity.setId(IdUtil.getSnowflakeNextId())
                .setTaskId(Long.parseLong(sdUserTaskVo.getTaskId()))
                .setInitImg(initImgUrl)
                .setFileUrl(url)
                .setCrtTime(new Date());
            return entity;
        }).collect(Collectors.toList());
        baseMapper.insertBatch(list);
    }

    /**
     * 分页查询用户模型文件列表
     * @param pageQuery 分页查询参数
     * @param userId    用户id
     * @param dto       查询参数
     * @return  用户模型文件列表
     */
    @Override
    public TableDataInfo<SdUserModelFileVo> listUserModelFile(PageQuery pageQuery, Long userId, SdUserModelFilePageDto dto) {
        if (StrUtil.isEmptyIfStr(dto.getEndTime())) {
            dto.setEndTime(dto.getStartTime());
        }
        Page<SdUserModelFileVo> page = baseMapper.selectAllList(pageQuery.build(),userId,dto.getTaskId(),dto.getCategory(),dto.getStartTime(),dto.getEndTime(),dto.getKeyword());
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            for (SdUserModelFileVo vo : page.getRecords()) {
                vo.setFileInfo(vo.getFileInfo()!=null? JSONObject.parseObject(String.valueOf(vo.getFileInfo())):null);
                vo.setFileParameters(vo.getFileParameters()!=null? JSONObject.parseObject(String.valueOf(vo.getFileParameters())):null);
                if (vo.getLoraInfo()!=null) {
                    vo.setLoraInfo(JSONArray.parse(String.valueOf(vo.getLoraInfo())));
                }
                else {
                    JSONObject loraInfo = new JSONObject();
                    loraInfo.put("loraTitle",vo.getLoraTitle());
                    loraInfo.put("loraModelId",vo.getLoraModelId());
                    loraInfo.put("loraModelUrl",vo.getLoraModelUrl());
                    loraInfo.put("modelStrength",vo.getLoraTitleZh());
                    vo.setLoraInfo(Collections.singletonList(loraInfo));
                }
            }
        }
        return TableDataInfo.build(page);
    }

    /**
     * 根据任务id查询用户模型文件列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return 用户模型文件列表
     */
    @Override
    public List<SdUserModelFileVo> listUserModelFile(String taskId, Long userId) {
        List<SdUserModelFileVo> list = baseMapper.selectAllListByTaskId(taskId,userId);
        if (CollectionUtil.isNotEmpty(list)) {
            List<SdUserModelFileVo> singleModels = list.stream().filter(e -> e.getLoraInfo()==null || CollectionUtil.isEmpty(JSON.parseArray(String.valueOf(e.getLoraInfo()), JSONObject.class))).collect(Collectors.toList());
            List<SdUserModelFileVo> multiModels = list.stream().filter(e -> e.getLoraInfo()!=null && CollectionUtil.isNotEmpty(JSON.parseArray(String.valueOf(e.getLoraInfo()), JSONObject.class))).collect(Collectors.toList());
            Set<String> allLoraModelIds = new HashSet<>();
            if (CollectionUtil.isNotEmpty(singleModels)) {
                Set<String> loraModelIds = singleModels.stream().map(SdUserModelFileVo::getLoraModelId).collect(Collectors.toSet());
                allLoraModelIds.addAll(loraModelIds);
            }
            if (CollectionUtil.isNotEmpty(multiModels)) {
                for (SdUserModelFileVo multiModel : multiModels) {
                    Set<String> loraModelIds = JSON.parseArray(String.valueOf(multiModel.getLoraInfo()), JSONObject.class).stream().map(e1 -> e1.getString("loraModelId")).collect(Collectors.toSet());
                    allLoraModelIds.addAll(loraModelIds);
                }
            }
            List<JSONObject> loraModelInfos = sdUserModelMapper.selectInfosByIds(allLoraModelIds);
            Map<String, JSONObject> map = loraModelInfos.stream().collect(Collectors.toMap(e -> e.getString("id"), e -> e));
            for (SdUserModelFileVo vo : list) {
                vo.setFileInfo(vo.getFileInfo()!=null? JSONObject.parseObject(String.valueOf(vo.getFileInfo())):null);
                vo.setFileParameters(vo.getFileParameters()!=null? JSONObject.parseObject(String.valueOf(vo.getFileParameters())):null);
                if (vo.getLoraInfo()!=null) {
                    List<JSONObject> arr = JSON.parseArray(String.valueOf(vo.getLoraInfo()),JSONObject.class);
                    for (JSONObject loraInfo : arr) {
                        JSONObject object = map.get(loraInfo.getString("loraModelId"));
                        loraInfo.put("ss_tag_frequency", Collections.emptyMap());
                        loraInfo.put("ss_tag_frequency_translate_map", Collections.emptyMap());
                        loraInfo.put("additionTag",Collections.emptyList());
                        if (CollectionUtil.isNotEmpty(object)) {
                            JSONObject tag = object.getJSONObject("config").getJSONObject("ss_tag_frequency");
                            JSONArray additionTag = object.getJSONArray("additionTag");
                            JSONObject tagTranslate = dealTagTranslate(tag);
                            loraInfo.put("ss_tag_frequency", tag);
                            loraInfo.put("ss_tag_frequency_translate_map", tagTranslate);
                            loraInfo.put("additionTag", CollectionUtil.isEmpty(additionTag)?Collections.emptyList():additionTag);
                        }
                        loraInfo.put("loraModelId", loraInfo.getString("loraModelId"));
                    }
                    vo.setLoraInfo(arr);
                }
                else {
                    JSONObject loraInfo = new JSONObject();
                    loraInfo.put("loraTitle",vo.getLoraTitle());
                    loraInfo.put("loraModelId",vo.getLoraModelId());
                    loraInfo.put("loraModelUrl",vo.getLoraModelUrl());
                    loraInfo.put("modelStrength",vo.getModelStrength());
                    JSONObject tag = map.get(loraInfo.getString("loraModelId")).getJSONObject("config").getJSONObject("ss_tag_frequency");
                    JSONArray additionTag = map.get(loraInfo.getString("loraModelId")).getJSONArray("additionTag");
                    JSONObject tagTranslate = dealTagTranslate(tag);
                    loraInfo.put("ss_tag_frequency", tag);
                    loraInfo.put("ss_tag_frequency_translate_map", tagTranslate);
                    loraInfo.put("additionTag", CollectionUtil.isEmpty(additionTag)?Collections.emptyList():additionTag);
                    vo.setLoraInfo(Collections.singletonList(loraInfo));
                }
            }
        }
        return list;
    }

    private JSONObject dealTagTranslate(JSONObject tag) {
        JSONObject translateMap = new JSONObject();
        for (String key : tag.keySet()) {
            Map<String, Integer> tagInfo = JSONObject.parseObject(tag.getString(key), Map.class);
            int lastIndex = Math.min(tagInfo.size(), 10);
            List<String> enKeys = tagInfo.entrySet().stream()
                .sorted((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> o2.getValue() - o1.getValue())
                .map(Map.Entry::getKey).collect(Collectors.toList()).subList(0, lastIndex);
            // 取交集
            enKeys.retainAll(tagInfo.keySet());
            for (String en : enKeys) {
                en = StringEscapeUtils.unescapeJava(en);
                String zh = RedisUtils.getCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, en);
                if (StrUtil.isEmptyIfStr(zh)) {
                    try {
                        zh = sysTranslateService.enToZh(en, TranslateType.BAIDU);
                    } catch (NoSuchAlgorithmException ex) {
                        log.error("翻译失败：{}", ex.getMessage());
                    }
                }
                if (StrUtil.isNotEmpty(zh) && !en.equals(zh)) {
                    RedisUtils.setCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, en, zh);
                    translateMap.put(en, zh);
                }
            }
        }
        return translateMap;
    }

    /**
     * 删除用户模型文件
     * @param ids    用户模型文件id列表
     * @param userId 用户id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserModelFile(List<String> ids, Long userId) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ServiceException("至少选择一个需要删除的图片!");
        }
        // 获取所选图片的全部任务ID集合
        List<String> taskIds = baseMapper.selectTaskIdsByIds(ids);
        baseMapper.batchDelByIds(ids,userId);
        if (CollectionUtil.isNotEmpty(taskIds)) {
            // 删除对应任务下的全部绘图图片数据
            for (String taskId : taskIds) {
                List<SdUserModelFileVo> list = baseMapper.selectAllListByTaskId(taskId, userId);
                if (CollectionUtil.isEmpty(list)) {
                    baseMapper.deleteTaskById(taskId,userId);
                }
            }
        }
    }

    /**
     * 根据任务id删除用户模型文件
     * @param taskId 任务id
     */
    @Override
    public void removeUserModelFileByTaskId(String taskId) {
        baseMapper.removeUserModelFileByTaskId(taskId);
    }

    /**
     * 根据任务id查询用户模型文件列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return 用户模型文件列表
     */
    @Override
    public List<String> listImgUrlByTaskId(String taskId, Long userId) {
        return baseMapper.listImgUrlByTaskId(taskId,userId);
    }

    /**
     * 根据用户模型文件id列表查询图片url列表
     * @param ids 用户模型文件id列表
     * @return 图片url列表
     */
    @Override
    public List<String> listImgUrlByIds(List<String> ids) {
        return baseMapper.listImgUrlByIds(ids);
    }

    /**
     * 根据模型id查询模型测试数据和任务信息
     * @param loraModelId 模型id
     * @return 模型测试数据和任务信息列表
     */
    @Override
    public List<JSONObject> selectModelTestDataAndTaskInfo(String loraModelId) {
        List<JSONObject> list = baseMapper.selectModelTestDataAndTaskInfo(loraModelId);
        return CollectionUtil.isEmpty(list)?Collections.emptyList():list;
    }
}
