package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelModifyDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelShareDto;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModel;
import com.sutran.sd.sdapi.mapper.SdUserModelClassifyMapper;
import com.sutran.sd.sdapi.mapper.SdUserModelMapper;
import com.sutran.sd.sdapi.modules.system.SdUserModelService;
import com.sutran.sd.system.service.SysTranslateService;
import com.sutran.sd.sdapi.modules.system.vo.SdLoraModelVo;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.sutran.sd.common.constant.CacheConstants.TRANSLATE_EN_TO_ZH_MAP;

/**
 * @author zj
 * @date 2024-03-03
 */
@Slf4j
@Service("SdUserModelService")
public class SdUserModelServiceImpl implements SdUserModelService {

    @Resource
    private SdUserModelMapper baseMapper;
    @Resource
    private SdUserModelClassifyMapper classifyMapper;
    @Resource
    private SysTranslateService sysTranslateService;
    @Resource(name = "threadPoolTaskExecutor")
    private Executor executor;

    @Override
    public void batchInsert(List<SdUserModel> models) {
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        for (SdUserModel model : models) {
            baseMapper.insertData(model);
        }
    }

    @Override
    public TableDataInfo<SdUserModelVo> selectAllList(SdUserModelPageDto dto, Long userId, PageQuery pageQuery) {
        Page<SdUserModelVo> page = baseMapper.selectAllList(dto, userId, pageQuery.build());
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return TableDataInfo.build(page);
        }
        // 获取当前人的全部分类
        List<JSONObject> list = classifyMapper.selectModelClassifyListByUserId(userId);
        Map<String, JSONObject> modelClassifyMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(list)) {
            modelClassifyMap = list.stream().collect(Collectors.toMap(e -> e.getString("modelId"), e -> e, (v1, v2) -> v1));
        }
        for (SdUserModelVo e : page.getRecords()) {
            e.setClassifyName("1".equals(e.getClassifyId())?"全部模型":e.getClassifyName());
            if (CollectionUtil.isNotEmpty(modelClassifyMap)) {
                JSONObject object = modelClassifyMap.get(e.getId());
                if (CollectionUtil.isNotEmpty(object)) {
                    e.setClassifyName(object.getString("classifyName"));
                    e.setClassifyId(object.getString("classifyId"));
                }
            }
            // 如果不是模型归属人，则是被分享的模型
            e.setShareModel(!Objects.equals(String.valueOf(userId), e.getBelongUserId()) && e.getIsOpen()==0 && e.getType()==1);
        }
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<SdUserModelVo> listLoraModelsInTaskAndTrainData(SdUserModelDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        Page<SdUserModelVo> page = baseMapper.selectAllListOfAdmin(pageQuery.build(),dto);
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return TableDataInfo.build(page);
        }
        page.getRecords().forEach(e-> e.setConfig(JSONObject.parseObject(String.valueOf(e.getConfig()), SdLoraModelVo.MetadataVo.class)));
        return TableDataInfo.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyModel(SdUserModelModifyDto dto, Long userId) {
        SdUserModel model = baseMapper.selectById(dto.getId());
        if (model==null) {
            throw new ServiceException("模型不存在!");
        }
        if (model.getType()==0 && StrUtil.isNotEmpty(dto.getClassifyId())) {
            classifyMapper.deleteByModelId(dto.getId(),userId);
            // 修改模型分类
            if (!"1".equals(dto.getClassifyId())) {
                classifyMapper.insertClassifyId(dto.getId(),dto.getClassifyId(),userId);
            }
            return;
        }
        else if (model.getType()==0) {
            throw new ServiceException("系统模型不可修改!");
        }
        if (!Objects.equals(userId, model.getBelongUserId())) {
            throw new ServiceException("无法修改他人模型!");
        }
        model.setCrtTime(new Date()).setIsOpen(dto.getIsOpen()).setRemark(dto.getRemark()).setUrl(dto.getUrl());
        if (StrUtil.isNotEmpty(dto.getClassifyId())) {
            classifyMapper.deleteByModelId(dto.getId(),userId);
            // 修改模型分类
            if (!"1".equals(dto.getClassifyId())) {
                classifyMapper.insertClassifyId(dto.getId(),dto.getClassifyId(),userId);
            }
        }
        baseMapper.updateById(model);
    }

    @Override
    public SdUserModelVo getModelInfo(String modelId, Long userId) {
        SdUserModelVo modelInfo = baseMapper.getModelInfo(modelId, LoginHelper.getUserId());
        if (modelInfo!=null) {
            dealConfig(modelInfo);
            modelInfo.setClassifyName("1".equals(modelInfo.getClassifyId())?"全部模型":modelInfo.getClassifyName());
            JSONObject classifyIndo = classifyMapper.selectClassifyInfoByModelId(modelId,userId);
            if (CollectionUtil.isNotEmpty(classifyIndo)) {
                modelInfo.setClassifyName(classifyIndo.getString("classifyName"));
                modelInfo.setClassifyId(classifyIndo.getString("classifyId"));
            }
            // 如果不是模型归属人，则是被分享的模型
            modelInfo.setShareModel(!Objects.equals(String.valueOf(userId), modelInfo.getBelongUserId()) && modelInfo.getIsOpen()==0 && modelInfo.getType()==1);
        }
        return modelInfo;
    }

    @Override
    public List<SdUserModelVo> getLatestModelInfo(Long userId, int limit) {
        List<SdUserModelVo> modelVoList = baseMapper.getLatestModelInfo(LoginHelper.getUserId(),limit);
        if (CollectionUtil.isEmpty(modelVoList)) {
            return Collections.emptyList();
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (SdUserModelVo modelVo : modelVoList) {
            // 如果不是模型归属人，则是被分享的模型
            modelVo.setShareModel(!Objects.equals(String.valueOf(userId), modelVo.getBelongUserId()) && modelVo.getIsOpen()==0 && modelVo.getType()==1);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dealConfig(modelVo), executor);
            futures.add(future);
        }
        // 等待futures中的异步任务全部完成，在返回数据
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return modelVoList;
    }

    private void dealConfig(SdUserModelVo e) {
        if (e.getConfig()!=null) {
            SdLoraModelVo.MetadataVo metadataVo = JSONObject.parseObject(String.valueOf(e.getConfig()), SdLoraModelVo.MetadataVo.class);
            if (metadataVo.getSsTagFrequency()!=null) {
                for (String key : metadataVo.getSsTagFrequency().keySet()) {
                    Map<String, Integer> map = metadataVo.getSsTagFrequency().get(key);
                    if (CollectionUtil.isEmpty(map)) {
                        continue;
                    }
                    List<String> enKeys = map.entrySet().stream()
                        .sorted((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> o2.getValue() - o1.getValue())
                        .map(Map.Entry::getKey).collect(Collectors.toList());
                    metadataVo.setSsTagFrequencyList(enKeys);
                    for (String en : enKeys) {
                        en = StringEscapeUtils.unescapeJava(en);
                        String zh = RedisUtils.getCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, en);
                        if (StrUtil.isEmptyIfStr(zh)) {
                            try {
                                zh = sysTranslateService.enToZh(en, TranslateType.BAIDU);
                            } catch (NoSuchAlgorithmException ex) {
                                log.error("翻译失败：{}",ex.getMessage());
                            }
                        }
                        if (StrUtil.isNotEmpty(zh) && !en.equals(zh)) {
                            RedisUtils.setCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, en, zh);
                            metadataVo.getSsTagFrequencyTranslateMap().put(en,zh);
                        }
                    }
                }
            }
            e.setConfig(metadataVo);
        }
        if (e.getAdditionTag()!=null) {
            e.setAdditionTag(JSONArray.parseArray(String.valueOf(e.getAdditionTag())));
        }
        else {
            e.setAdditionTag(Collections.emptyList());
        }
    }

    @Override
    public SdUserModel selectById(String modelId) {
        return baseMapper.selectById(modelId);
    }

    @Override
    public void publishModel(String id, Integer publishStatus, Integer isUserDel, String modelStrength) {
        baseMapper.publishModel(id,publishStatus,isUserDel,modelStrength);
    }

    @Override
    public void modifyModelStrength(String id, String modelStrength) {
        baseMapper.modifyModelStrength(id,modelStrength);
    }

    @Override
    public boolean isExistByClassifyId(String classifyId, Long userId) {
        return classifyMapper.selectCountByClassifyIdAndUserId(classifyId,userId)>0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeModelById(String id, Long userId) {
        baseMapper.deleteById(id);
        classifyMapper.deleteByModelId(id, userId);
    }

    @Override
    public void removeModelOfAdminById(long modelId) {
        baseMapper.deleteById(modelId);
        classifyMapper.deleteOfAdminByModelId(modelId);
    }

    @Override
    public List<SdUserModel> selectByIds(List<String> ids) {
        return baseMapper.selectList(new LambdaQueryWrapper<SdUserModel>().in(SdUserModel::getId, ids));
    }

    @Override
    public List<String> selectHashList() {
        List<String> list = baseMapper.selectHashList();
        return CollectionUtil.isEmpty(list)?Collections.emptyList():list;
    }

    @Override
    public JSONObject selectUserOpenIdAndPhoneById(String id) {
        return baseMapper.selectUserOpenIdAndPhoneById(id);
    }

    /**
     * 删除分享给指定人的指定模型
     * @param modelId   模型ID
     * @param userId    用户ID
     */
    @Override
    public void removeShareModelById(String modelId, Long userId) {
        baseMapper.removeShareModelById(modelId,userId);
    }

    /**
     * 分享指定模型
     * @param dto       分享参数实体
     * @param userId    模型拥有者userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shareModel(SdUserModelShareDto dto, Long userId) {
        List<SdUserModel> list = baseMapper.selectList(new LambdaQueryWrapper<SdUserModel>().in(SdUserModel::getId, dto.getModelIds()));
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException("分享模型不存在或已被删除!");
        }
        List<String> errMsg = new ArrayList<>();
        for (SdUserModel model : list) {
            // 判断模型是否系统模型
            if (model.getType()==0) {
                errMsg.add("["+model.getModelNameZh()+"]为系统模型，不用分享!");
                continue;
            }
            // 判断模型是否公开
            if (model.getIsOpen()==1) {
                errMsg.add("["+model.getModelNameZh()+"]模型已公开，不用分享!");
                continue;
            }
            // 判断模型归属人是否是当前用户
            if (!Objects.equals(userId, model.getBelongUserId())) {
                errMsg.add("["+model.getModelNameZh()+"]非模型拥有者，无法分享!");
                continue;
            }
            baseMapper.shareModel(dto,userId, new Date());
        }
        if (CollectionUtil.isNotEmpty(errMsg)) {
            throw new ServiceException(String.join(",",errMsg));
        }
    }
}
