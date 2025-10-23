package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.dto.model.SdUserModelDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelModifyDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelShareDto;
import com.sutran.sd.draw.domain.vo.ComfyUserModelVo;
import com.sutran.sd.draw.domain.vo.SdLoraModelVo;
import com.sutran.sd.draw.domain.vo.SdUserModelVo;
import com.sutran.sd.draw.mapper.SdUserModelClassifyMapper;
import com.sutran.sd.draw.mapper.SdUserModelMapper;
import com.sutran.sd.draw.service.SdUserModelService;
import com.sutran.sd.draw.utils.CommonUtil;
import com.sutran.sd.system.service.SysTranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
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
@SuppressWarnings("AlibabaUndefineMagicConstant")
@Slf4j
@Service
@RequiredArgsConstructor
public class SdUserModelServiceImpl implements SdUserModelService {

    private final SdUserModelMapper baseMapper;
    private final SdUserModelClassifyMapper classifyMapper;
    private final SysTranslateService sysTranslateService;

    @Resource(name = "threadPoolTaskExecutor")
    private Executor executor;

    /**
     * 批量插入模型
     * @param models    模型列表
     */
    @Override
    public void batchInsert(List<SdUserModel> models) {
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        for (SdUserModel model : models) {
            baseMapper.insertData(model);
        }
    }

    /**
     * 批量添加模型
     * @param models    模型列表
     */
    @Override
    public void batchAdd(List<SdUserModel> models) {
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        baseMapper.insertBatch(models);
    }

    /**
     * 查询所有模型列表
     * @param dto       查询参数实体
     * @param userId    用户ID
     * @param pageQuery 分页参数实体
     * @return  模型列表
     */
    @Override
    public TableDataInfo<SdUserModelVo> selectAllList(SdUserModelPageDto dto, Long userId, PageQuery pageQuery) {
        Page<SdUserModelVo> page = baseMapper.selectAllList(dto, userId, pageQuery.build());
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return TableDataInfo.build(page);
        }
        // 获取当前人的全部分类
        List<JSONObject> list = classifyMapper.selectModelClassifyListByUserId(userId);
        Map<String, JSONObject> modelClassifyMap = new HashMap<>(list.size());
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

    /**
     * 查询所有模型列表(admin)
     * @param dto       查询参数实体
     * @return  模型列表
     */
    @Override
    public TableDataInfo<SdUserModelVo> listLoraModelsInTaskAndTrainData(SdUserModelDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        Page<SdUserModelVo> page = baseMapper.selectAllListOfAdmin(pageQuery.build(),dto);
        return TableDataInfo.build(page);
    }

    /**
     * 修改模型
     * @param dto       修改参数实体
     * @param userId    用户ID
     */
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

    /**
     * 查询模型详情
     * @param modelId   模型ID
     * @param userId    用户ID
     * @return  模型详情
     */
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

    /**
     * 获取最新模型列表
     * @param userId    用户ID
     * @param limit     数量
     * @return          模型列表
     */
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

    /**
     * 查询模型详情(admin)
     * @param modelId   模型ID
     * @return  模型详情
     */
    @Override
    public SdUserModel selectById(String modelId) {
        return baseMapper.selectById(modelId);
    }

    /**
     * 发布模型
     * @param id            模型ID
     * @param publishStatus 发布状态
     * @param isUserDel     是否用户删除
     * @param modelStrength 模型强度
     */
    @Override
    public void publishModel(String id, Integer publishStatus, Integer isUserDel, String modelStrength) {
        baseMapper.publishModel(id,publishStatus,isUserDel,modelStrength);
    }

    /**
     * 修改模型强度
     * @param id            模型ID
     * @param modelStrength 模型强度
     */
    @Override
    public void modifyModelStrength(String id, String modelStrength) {
        baseMapper.modifyModelStrength(id,modelStrength);
    }

    /**
     * 判断模型是否存在
     * @param classifyId       分类ID
     * @param userId    用户ID
     * @return          是否存在
     */
    @Override
    public boolean isExistByClassifyId(String classifyId, Long userId) {
        return classifyMapper.selectCountByClassifyIdAndUserId(classifyId,userId)>0;
    }

    /**
     * 删除模型
     * @param id        模型ID
     * @param userId    用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeModelById(String id, Long userId) {
        baseMapper.deleteById(id);
        classifyMapper.deleteByModelId(id, userId);
    }

    /**
     * 删除模型(admin)
     * @param modelId   模型ID
     */
    @Override
    public void removeModelOfAdminById(long modelId) {
        baseMapper.deleteById(modelId);
        classifyMapper.deleteOfAdminByModelId(modelId);
    }

    /**
     * 根据模型ID查询模型
     * @param ids   模型ID列表
     * @return      模型实体列表
     */
    @Override
    public List<SdUserModel> selectByIds(List<String> ids) {
        return baseMapper.selectList(new LambdaQueryWrapper<SdUserModel>().in(SdUserModel::getId, ids));
    }

    /**
     * 查询模型哈希列表
     * @return  模型哈希列表
     */
    @Override
    public List<String> selectHashList() {
        List<String> list = baseMapper.selectHashList();
        return CollectionUtil.isEmpty(list)?Collections.emptyList():list;
    }

    /**
     * 查询用户openId和手机号
     * @param id    模型ID
     * @return      用户openId和手机号
     */
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

     /**
     * 获取ComfyUI最近使用的n个模型列表
     * @param userId    用户ID
     * @param num       数量
     * @return          模型列表
     */
    @Override
    public List<ComfyUserModelVo> getLatestModelInfoOfComfyui(Long userId, int num) {
        List<ComfyUserModelVo> list = baseMapper.getLatestModelInfoOfComfyui(userId, num);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> taskIds = list.stream().map(ComfyUserModelVo::getTaskId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(taskIds)) {
            return list;
        }
        // 获取每个训练任务的预参数
        List<JSONObject> params = baseMapper.selectPreParamByTaskIds(taskIds);
        // 构建任务ID到预参数的映射
        Map<String, String> paramMap = CollectionUtil.isEmpty(params)?Collections.emptyMap():params.stream().collect(Collectors.toMap(e->e.getString("taskId"), e -> e.getString("preParam")));
        // 获取每个模型对应的训练数据中的提示词文件
        for (ComfyUserModelVo vo : list) {
            // 处理ComfyUI数据中的提示词
            dealComfyUiDataPrompt(vo,paramMap);
        }
        return list;
    }

    /**
     * 获取comfyui lora模型列表
     * @param dto       SdUserModelPageDto
     * @param pageQuery 分页查询参数
     * @return  TableDataInfo<ComfyUserModelVo>
     */
    @Override
    public TableDataInfo<ComfyUserModelVo> listLoraModelsOfComfyui(SdUserModelPageDto dto, PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        Page<ComfyUserModelVo> page = baseMapper.selectAllListOfComfyui(dto, userId, pageQuery.build());
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return TableDataInfo.build(page);
        }
        // 获取当前人的全部分类
        List<JSONObject> list = classifyMapper.selectModelClassifyListByUserId(userId);
        Map<String, JSONObject> modelClassifyMap = new HashMap<>(list.size());
        if (CollectionUtil.isNotEmpty(list)) {
            modelClassifyMap = list.stream().collect(Collectors.toMap(e -> e.getString("modelId"), e -> e, (v1, v2) -> v1));
        }
        // 获取任务ID集合
        List<String> taskIds = page.getRecords().stream().map(ComfyUserModelVo::getTaskId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(taskIds)) {
            return TableDataInfo.build(page);
        }
        // 获取每个训练任务的预参数
        List<JSONObject> params = baseMapper.selectPreParamByTaskIds(taskIds);
        // 构建任务ID到预参数的映射
        Map<String, String> paramMap = CollectionUtil.isEmpty(params)?Collections.emptyMap():params.stream().collect(Collectors.toMap(e->e.getString("taskId"), e -> e.getString("preParam")));
        for (ComfyUserModelVo e : page.getRecords()) {
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
            // 处理ComfyUI数据中的提示词
            dealComfyUiDataPrompt(e,paramMap);
        }
        return TableDataInfo.build(page);
    }

    /**
     * 处理ComfyUI数据中的提示词
     * @param vo        模型实体
     * @param paramMap  预参数映射
     */
    private void dealComfyUiDataPrompt(ComfyUserModelVo vo, Map<String, String> paramMap) {
        // 获取关联训练任务ID
        String preParam = paramMap.get(vo.getTaskId());
        if (StringUtils.isBlank(preParam)) {
            return;
        }
        // 解析预参数中的数据集路径
        JSONObject paramJson = JSONObject.parseObject(preParam);
        String captionStr = paramJson.getString("captions");
        String loraName = paramJson.getString("loraName");
        List<String> captions;
        if (StringUtils.isBlank(captionStr)) {
            String dataPath = paramJson.getString("path");
            if (StringUtils.isBlank(dataPath)) {
                return;
            }
            dataPath += CommonUtil.suggestNumRepeat();
            // 获取dataPath目录下的全部txt文件
            List<File> txtFiles = CommonUtil.getAllFileOfTxt(new File(dataPath));
            if (CollectionUtil.isEmpty(txtFiles)) {
                return;
            }
            // 解析每个txt文件中的提示词
            captions = txtFiles.stream().map(FileUtil::readUtf8Lines).flatMap(List::stream).collect(Collectors.toList());
        }
        else {
            // 获取训练使用的提示词列表
            captions = JSONArray.parseArray(captionStr, String.class);
        }
        // 翻译提示词
        List<ComfyUserModelVo.PromptVo> captionList = captions.stream().map(caption->{
            String promptZh = RedisUtils.getCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, caption);
            // caption移除第一个逗号前的数据包括第一个逗号
            String prompt = caption.substring(caption.indexOf(",")+1);
            // 如果缓存中没有数据，则调用翻译接口
            if (StringUtils.isBlank(promptZh)) {
                try{
                    promptZh = sysTranslateService.enToZh(prompt, TranslateType.BAIDU);
                    if (StringUtils.isBlank(promptZh)) {
                        promptZh = prompt;
                    }
                    else {
                        // 翻译成功将翻译结果缓存起来
                        RedisUtils.setCacheMapValue(TRANSLATE_EN_TO_ZH_MAP, caption, loraName+"，"+promptZh);
                    }
                }
                catch (Exception e){
                    promptZh = prompt;
                }
            }
            else {
                // value移除第一个逗号前的数据包括第一个逗号，示例：测试FLUX模型，五瓶Loveb布丁，粉红色表面。瓶子有不同的颜色，上面写着文字。在图像的底部，有额外的文本。
                promptZh = promptZh.substring(promptZh.indexOf("，")+1);
            }
            return new ComfyUserModelVo.PromptVo().setPrompt(prompt).setPromptZh(promptZh);
        }).collect(Collectors.toList());
        vo.setPromptList(captionList);
    }
}
