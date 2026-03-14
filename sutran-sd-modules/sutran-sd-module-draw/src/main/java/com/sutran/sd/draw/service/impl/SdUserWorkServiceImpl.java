package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.SdUserFavorite;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdUserWork;
import com.sutran.sd.draw.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.draw.domain.vo.*;
import com.sutran.sd.draw.mapper.SdUserWorkMapper;
import com.sutran.sd.draw.mapper.SdUserModelMapper;
import com.sutran.sd.draw.service.SdUserWorkCommentService;
import com.sutran.sd.draw.service.SdUserWorkService;
import com.sutran.sd.system.service.SysTranslateService;
import com.sutran.sd.user.service.SdUserFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.BeanUtils;
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
public class SdUserWorkServiceImpl implements SdUserWorkService {

    private final SdUserWorkMapper sdUserWorkMapper;
    private final SdUserModelMapper sdUserModelMapper;
    private final SysTranslateService sysTranslateService;
    private final SdUserFavoriteService favoriteService;
    private final SdUserWorkCommentService sdUserWorkCommentService;
    private final UserService userService;

    /**
     * 异步批量插入用户作品
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
     * @param negativePrompt    负面提示词
     * @param negativePromptZh  负面提示词中文
     * @param isRedraw      是否重绘
     */
    @Override
    public void asyncBatchInsert(SdApiResult rs, Long userId, String userName, List<JSONObject> loraInfos, String modelName, String taskId, int category, String prompt, String initImg, String promptDesc, String promptZh, String negativePrompt, String negativePromptZh, Integer isRedraw) {
        if (CollectionUtil.isEmpty(rs.getImages())) {
            return;
        }
        Integer isRedrawEntity = isRedraw==null?0:isRedraw;
        List<SdUserWork> list = rs.getImages().stream().map(url -> new SdUserWork().setIsRedraw(isRedrawEntity).setId(IdUtil.getSnowflakeNextId()).setTaskId(Long.parseLong(taskId))
            .setPrompt(prompt).setPromptZh(promptZh).setPromptDesc(promptDesc).setNegativePrompt(negativePrompt).setNegativePromptZh(negativePromptZh)
            .setFileInfo(JSONObject.toJSONString(rs.getInfo(), WriteMapNullValue)).setInitImg(initImg).setCategory(category).setFileUrl(url)
            .setFileParameters(JSONObject.toJSONString(rs.getParameters(), WriteMapNullValue)).setBelongUserId(userId).setBelongUserName(userName)
            .setLoraTitle(loraInfos.get(0).getString("loraTitle")).setLoraTitle(loraInfos.get(0).getString("loraTitleZh")).setLoraModelId(loraInfos.get(0).getLongValue("loraModelId")).setModelStrength(loraInfos.get(0).getString("modelStrength"))
            .setModelName(modelName).setLoraInfo(CollectionUtil.isNotEmpty(loraInfos)? JSONObject.toJSONString(loraInfos):null).setCrtTime(new Date())).collect(Collectors.toList());
        sdUserWorkMapper.insertBatch(list);
    }

    /**
     * 异步批量插入
     *
     * @param taskVo        任务信息
     * @param urlList       图片url列表
     */
    @Override
    public void asyncBatchInsert(SdUserTaskVo taskVo, List<String> urlList) {
        if (CollectionUtil.isEmpty(urlList)) {
            return;
        }
        List<SdUserWork> list = urlList.stream().map(url ->{
            SdUserWork entity = new SdUserWork();
            BeanUtils.copyProperties(taskVo,entity);
            entity.setId(IdUtil.getSnowflakeNextId())
                .setTaskId(Long.parseLong(taskVo.getTaskId()))
                .setBelongUserId(Long.parseLong(taskVo.getBelongUserId()))
                .setFileUrl(url)
                .setCrtTime(new Date());
            return entity;
        }).collect(Collectors.toList());
        sdUserWorkMapper.insertBatch(list);
    }

    /**
     * 分页查询用户作品列表
     * @param pageQuery 分页查询参数
     * @param userId    用户id
     * @param dto       查询参数
     * @return  用户作品列表
     */
    @Override
    public TableDataInfo<SdUserWorkVo> listUserWork(PageQuery pageQuery, Long userId, SdUserModelFilePageDto dto) {
        if (StrUtil.isEmptyIfStr(dto.getEndTime())) {
            dto.setEndTime(dto.getStartTime());
        }
        Page<SdUserWorkVo> page = sdUserWorkMapper.selectAllList(pageQuery.build(),userId,dto.getTaskId(),dto.getCategory(),dto.getStartTime(),dto.getEndTime(),dto.getKeyword());
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            for (SdUserWorkVo vo : page.getRecords()) {
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
     * 根据任务id查询用户作品列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return 用户作品列表
     */
    @Override
    public List<SdUserWorkVo> listUserWork(String taskId, Long userId) {
        List<SdUserWorkVo> list = sdUserWorkMapper.selectAllListByTaskId(taskId,userId);
        if (CollectionUtil.isNotEmpty(list)) {
            List<SdUserWorkVo> singleModels = list.stream().filter(e -> e.getLoraInfo()==null || CollectionUtil.isEmpty(JSON.parseArray(String.valueOf(e.getLoraInfo()), JSONObject.class))).collect(Collectors.toList());
            List<SdUserWorkVo> multiModels = list.stream().filter(e -> e.getLoraInfo()!=null && CollectionUtil.isNotEmpty(JSON.parseArray(String.valueOf(e.getLoraInfo()), JSONObject.class))).collect(Collectors.toList());
            Set<String> allLoraModelIds = new HashSet<>();
            if (CollectionUtil.isNotEmpty(singleModels)) {
                Set<String> loraModelIds = singleModels.stream().map(SdUserWorkVo::getLoraModelId).collect(Collectors.toSet());
                allLoraModelIds.addAll(loraModelIds);
            }
            if (CollectionUtil.isNotEmpty(multiModels)) {
                for (SdUserWorkVo multiModel : multiModels) {
                    Set<String> loraModelIds = JSON.parseArray(String.valueOf(multiModel.getLoraInfo()), JSONObject.class).stream().map(e1 -> e1.getString("loraModelId")).collect(Collectors.toSet());
                    allLoraModelIds.addAll(loraModelIds);
                }
            }
            List<JSONObject> loraModelInfos = sdUserModelMapper.selectInfosByIds(allLoraModelIds);
            Map<String, JSONObject> map = loraModelInfos.stream().collect(Collectors.toMap(e -> e.getString("id"), e -> e));
            for (SdUserWorkVo vo : list) {
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
                            JSONObject config = object.getJSONObject("config");
                            JSONObject tag = config.containsKey("ss_tag_frequency")?config.getJSONObject("ss_tag_frequency"):config.getJSONObject("ssTagFrequency");
                            JSONObject tagTranslate = dealTagTranslate(tag);

                            JSONArray additionTag = object.getJSONArray("additionTag");
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
                    loraInfo.put("ss_tag_frequency", Collections.emptyMap());
                    loraInfo.put("ss_tag_frequency_translate_map", Collections.emptyMap());
                    loraInfo.put("additionTag",Collections.emptyList());
                    JSONObject object = map.get(loraInfo.getString("loraModelId"));
                    if (CollectionUtil.isNotEmpty(object)) {
                        JSONObject config = object.getJSONObject("config");
                        JSONObject tag = config.containsKey("ss_tag_frequency")?config.getJSONObject("ss_tag_frequency"):config.getJSONObject("ssTagFrequency");
                        JSONObject tagTranslate = dealTagTranslate(tag);

                        JSONArray additionTag = object.getJSONArray("additionTag");
                        loraInfo.put("ss_tag_frequency", tag);
                        loraInfo.put("ss_tag_frequency_translate_map", tagTranslate);
                        loraInfo.put("additionTag", CollectionUtil.isEmpty(additionTag)?Collections.emptyList():additionTag);
                    }
                    vo.setLoraInfo(Collections.singletonList(loraInfo));
                }
            }
        }
        return list;
    }

    /**
     * webui-处理标签翻译
     * @param tag webui上的标签
     * @return 翻译后的标签值
     */
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
     * 删除用户作品
     * @param ids    用户作品id列表
     * @param userId 用户id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserWork(List<String> ids, Long userId) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ServiceException("至少选择一个需要删除的图片!");
        }
        // 获取所选图片的全部任务ID集合
        List<String> taskIds = sdUserWorkMapper.selectTaskIdsByIds(ids);
        sdUserWorkMapper.batchDelByIds(ids,userId);
        if (CollectionUtil.isNotEmpty(taskIds)) {
            // 删除对应任务下的全部绘图图片数据
            for (String taskId : taskIds) {
                List<SdUserWorkVo> list = sdUserWorkMapper.selectAllListByTaskId(taskId, userId);
                if (CollectionUtil.isEmpty(list)) {
                    sdUserWorkMapper.deleteTaskById(taskId,userId);
                }
            }
        }
    }

    /**
     * 根据任务id删除用户作品
     * @param taskId 任务id
     */
    @Override
    public void removeUserWorkByTaskId(String taskId) {
        sdUserWorkMapper.removeUserWorkByTaskId(taskId);
    }

    /**
     * 根据任务id查询用户作品列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return 用户作品列表
     */
    @Override
    public List<String> listImgUrlByTaskId(String taskId, Long userId) {
        return sdUserWorkMapper.listImgUrlByTaskId(taskId,userId);
    }

    /**
     * 根据用户作品id列表查询图片url列表
     * @param ids 用户作品id列表
     * @return 图片url列表
     */
    @Override
    public List<String> listImgUrlByIds(List<String> ids) {
        return sdUserWorkMapper.listImgUrlByIds(ids);
    }

    /**
     * 根据模型id查询模型测试数据和任务信息
     * @param loraModelId 模型id
     * @return 模型测试数据和任务信息列表
     */
    @Override
    public List<JSONObject> selectModelTestDataAndTaskInfo(String loraModelId) {
        List<JSONObject> list = sdUserWorkMapper.selectModelTestDataAndTaskInfo(loraModelId);
        return CollectionUtil.isEmpty(list)?Collections.emptyList():list;
    }

    /**
     * 查询指定任务的生图列表
     * @param taskId 任务id
     * @return 任务详情
     */
    @Override
    public List<ComfyUserWorkVo> getComfyImageOutputByTaskId(String taskId) {
        List<ComfyUserWorkVo> list = sdUserWorkMapper.getComfyImageOutputByTaskId(taskId);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        for (ComfyUserWorkVo item : list) {
            if (item.getInitImgList()!=null) {
                item.setInitImgList(JSONArray.parseArray(String.valueOf(item.getInitImgList()), String.class));
            }
        }
        return list;
    }

    /**
     * 检查指定任务是否已生成图片
     * @param taskId 任务id
     * @return 是否已生成图片
     */
    @Override
    public boolean checkHasImgByTaskId(String taskId) {
        return sdUserWorkMapper.checkHasImgByTaskId(taskId);
    }

    /**
     * 根据用户ID查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @return 用户生图文件数据记录集合
     */
    @Override
    public List<SdUserWork> selectSdUserWorkListByUserId(Long userId) {
        return sdUserWorkMapper.selectSdUserWorkListByUserId(userId);
    }

    /**
     * 根据用户ID和分类查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @return 用户生图文件数据记录集合
     */
    @Override
    public List<SdUserWork> selectSdUserWorkListByUserIdAndCategory(Long userId, Integer category) {
        return sdUserWorkMapper.selectSdUserWorkListByUserIdAndCategory(userId, category);
    }

    /**
     * 新增用户生图文件数据记录
     * @param sdUserModelFile 用户生图文件数据记录
     * @return 是否新增成功
     */
    @Override
    public int insertSdUserWork(SdUserWork sdUserModelFile) {
        return sdUserWorkMapper.insertSdUserWork(sdUserModelFile);
    }



    /**
     * 批量删除用户生图文件数据记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteSdUserWorkByIds(Long[] ids) {
        return sdUserWorkMapper.deleteSdUserWorkByIds(ids);
    }

    /**
     * 删除用户生图文件数据记录信息
     *
     * @param id 用户生图文件数据记录主键
     * @return 结果
     */
    @Override
    public int deleteSdUserWorkById(Long id) {
        return sdUserWorkMapper.deleteSdUserWorkById(id);
    }

    /**
     * 获取我的作品详情（VO格式）
     *
     * @param id 作品ID
     * @param userId 用户ID
     * @return 作品详情
     */
    @Override
    public UserWorkVo getMyWorkDetail(Long id, Long userId) {
        // 先查询作品是否存在且属于当前用户
        LambdaQueryWrapper<SdUserWork> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SdUserWork::getId, id).eq(SdUserWork::getBelongUserId, userId);
        SdUserWork file = sdUserWorkMapper.selectOne(queryWrapper);
        if (file == null) {
            log.warn("作品不存在或不属于当前用户: 作品ID={}, 用户ID={}", id, userId);
            return null;
        }
        UserWorkVo vo = convertToVo(file, userId);
        // 查询作品收藏数量
        Map<Long,Long> favoriteCountMap = favoriteService.countFavoritesByTypeAndIds(SdUserFavorite.FavoriteType.WORK, Collections.singletonList(vo.getId()));
        // 查询作品评论数量
        Map<Long,Long> commentCountMap = sdUserWorkCommentService.countCommentMapByWorkIds(Collections.singletonList(vo.getId()));
        // 获取评论数量和收藏数量
        vo.setFavoriteCount(favoriteCountMap.getOrDefault(file.getId(), 0L));
        vo.setCommentCount(commentCountMap.getOrDefault(file.getId(), 0L));
        return vo;
    }

    /**
     * 转换为VO对象
     */
    private UserWorkVo convertToVo(SdUserWork file) {
        return convertToVo(file, null);
    }

    /**
     * 转换为VO对象（带用户ID，用于填充收藏状态）
     */
    private UserWorkVo convertToVo(SdUserWork file, Long userId) {
        UserWorkVo vo = new UserWorkVo();
        BeanUtils.copyProperties(file, vo);
        // 填充收藏状态
        if (userId != null && file.getId() != null) {
            vo.setIsFavorite(favoriteService.isFavorite(userId, SdUserFavorite.FavoriteType.WORK, file.getId()));
        }
        else {
            vo.setIsFavorite(false);
        }
        return vo;
    }

    /**
     * 获取我的作品列表（分页查询）
     *
     * @param userId 用户ID
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    @Override
    public TableDataInfo<UserWorkVo> getMyWorksPage(Long userId, PageQuery pageQuery) {
        try {
            // 构建分页对象
            Page<SdUserWork> page = pageQuery.build();
            // 构建查询条件
            LambdaQueryWrapper<SdUserWork> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdUserWork::getBelongUserId, userId).orderByDesc(SdUserWork::getCrtTime);
            // 执行分页查询
            Page<SdUserWork> result = sdUserWorkMapper.selectPage(page, queryWrapper);
            if (CollectionUtil.isEmpty(result.getRecords())) {
                return TableDataInfo.build();
            }
            List<Long> workIds = result.getRecords().stream().map(SdUserWork::getId).collect(Collectors.toList());
            // 查询作品收藏数量
            Map<Long,Long> favoriteCountMap = favoriteService.countFavoritesByTypeAndIds(SdUserFavorite.FavoriteType.WORK, workIds);
            // 查询作品评论数量
            Map<Long,Long> commentCountMap = sdUserWorkCommentService.countCommentMapByWorkIds(workIds);
            // 查询用户信息映射
            Set<Long> userIds = result.getRecords().stream().map(SdUserWork::getBelongUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long,SysUser> userMap = userService.selectUserMap(userIds);
            // 转换为VO
            List<UserWorkVo> voList = result.getRecords().stream().map(file -> {
                UserWorkVo vo = convertToVo(file,userId);
                // 获取评论数量和收藏数量
                vo.setFavoriteCount(favoriteCountMap.getOrDefault(file.getId(), 0L));
                vo.setCommentCount(commentCountMap.getOrDefault(file.getId(), 0L));
                SysUser user = userMap.get(vo.getBelongUserId());
                if (user != null) {
                    vo.setBelongUserAvatar(user.getAvatar());
                    vo.setBelongUserName(user.getUserName());
                    vo.setBelongUserNickName(user.getNickName());
                }
                return vo;
            }).collect(Collectors.toList());
            return new TableDataInfo<>(voList,result.getTotal());
        } catch (Exception e) {
            log.error("分页查询我的作品失败: 用户ID={}", userId, e);
            return TableDataInfo.build();
        }
    }

    @Override
    public TableDataInfo<UserWorkVo> getMyWorksByCategoryPage(Long userId, Integer category, PageQuery pageQuery) {
        try {
            // 构建分页对象
            Page<SdUserWork> page = pageQuery.build();
            // 构建查询条件
            LambdaQueryWrapper<SdUserWork> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdUserWork::getBelongUserId, userId).eq(SdUserWork::getCategory, category).orderByDesc(SdUserWork::getCrtTime);
            // 执行分页查询
            Page<SdUserWork> result = sdUserWorkMapper.selectPage(page, queryWrapper);
            if (CollectionUtil.isEmpty(result.getRecords())) {
                return TableDataInfo.build();
            }
            List<Long> workIds = result.getRecords().stream().map(SdUserWork::getId).collect(Collectors.toList());
            // 查询作品收藏数量
            Map<Long,Long> favoriteCountMap = favoriteService.countFavoritesByTypeAndIds(SdUserFavorite.FavoriteType.WORK, workIds);
            // 查询作品评论数量
            Map<Long,Long> commentCountMap = sdUserWorkCommentService.countCommentMapByWorkIds(workIds);
            // 查询用户信息映射
            Set<Long> userIds = result.getRecords().stream().map(SdUserWork::getBelongUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long,SysUser> userMap = userService.selectUserMap(userIds);
            // 转换为VO
            List<UserWorkVo> voList = result.getRecords().stream().map(file -> {
                UserWorkVo vo = convertToVo(file,userId);
                // 获取评论数量和收藏数量
                vo.setFavoriteCount(favoriteCountMap.getOrDefault(file.getId(), 0L));
                vo.setCommentCount(commentCountMap.getOrDefault(file.getId(), 0L));
                SysUser user = userMap.get(vo.getBelongUserId());
                if (user != null) {
                    vo.setBelongUserAvatar(user.getAvatar());
                    vo.setBelongUserName(user.getUserName());
                    vo.setBelongUserNickName(user.getNickName());
                }
                return vo;
            }).collect(Collectors.toList());
            return new TableDataInfo<>(voList,result.getTotal());

        } catch (Exception e) {
            log.error("分页查询我的作品(按分类)失败: 用户ID={}, 分类={}", userId, category, e);
            return TableDataInfo.build();
        }
    }

    /**
     * 设置或取消作品公开
     *
     * @param id 作品ID
     * @param userId 当前用户ID
     * @param isPublic 是否公开 true/false
     * @return 操作是否成功
     */
    @Override
    public boolean setPublic(Long id, Long userId, boolean isPublic) {
        // 只允许更新属于当前用户的作品
        // 如果设置为公开，记录公开时间；如果取消公开，清空公开时间
        Date publicTime = isPublic ? new Date() : null;
        int affected = sdUserWorkMapper.updatePublicByIdAndUser(id, userId, isPublic ? 1 : 0, publicTime);
        return affected > 0;
    }

    /**
     * 查询公开作品列表（分页）
     *
     * @param pageQuery 分页查询参数
     * @return 分页结果（仅公开作品）
     */
    @Override
    public TableDataInfo<UserWorkVo> getPublicWorksPage(PageQuery pageQuery) {
        try {
            Page<SdUserWork> page = pageQuery.build();
            LambdaQueryWrapper<SdUserWork> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(SdUserWork::getCrtTime).eq(SdUserWork::getIsPublic, 1);
            Page<SdUserWork> result = sdUserWorkMapper.selectPage(page, queryWrapper);
            if (CollectionUtil.isEmpty(result.getRecords())) {
                return TableDataInfo.build();
            }
            List<Long> workIds = result.getRecords().stream().map(SdUserWork::getId).collect(Collectors.toList());
            // 查询作品收藏数量
            Map<Long,Long> favoriteCountMap = favoriteService.countFavoritesByTypeAndIds(SdUserFavorite.FavoriteType.WORK, workIds);
            // 查询作品评论数量
            Map<Long,Long> commentCountMap = sdUserWorkCommentService.countCommentMapByWorkIds(workIds);
            // 查询用户信息映射
            Set<Long> userIds = result.getRecords().stream().map(SdUserWork::getBelongUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long,SysUser> userMap = userService.selectUserMap(userIds);
            // 转换为VO
            List<UserWorkVo> voList = result.getRecords().stream().map(file -> {
                UserWorkVo vo = convertToVo(file);
                // 获取评论数量和收藏数量
                vo.setFavoriteCount(favoriteCountMap.getOrDefault(file.getId(), 0L));
                vo.setCommentCount(commentCountMap.getOrDefault(file.getId(), 0L));
                SysUser user = userMap.get(vo.getBelongUserId());
                if (user != null) {
                    vo.setBelongUserAvatar(user.getAvatar());
                    vo.setBelongUserName(user.getUserName());
                    vo.setBelongUserNickName(user.getNickName());
                }
                return vo;
            }).collect(Collectors.toList());
            return new TableDataInfo<>(voList,result.getTotal());
        }
        catch (Exception e) {
            log.error("分页查询公开作品失败", e);
            return TableDataInfo.build();
        }
    }
}
