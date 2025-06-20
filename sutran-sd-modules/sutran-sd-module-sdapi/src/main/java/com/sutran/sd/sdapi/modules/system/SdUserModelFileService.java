package com.sutran.sd.sdapi.modules.system;

import com.alibaba.fastjson2.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.sdapi.modules.system.vo.SdApiResult;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelFileVo;

import java.util.List;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserModelFileService {

    void asyncBatchInsert(SdApiResult rs, Long userId, String userName, List<JSONObject> loraInfos, String modelName, String taskId, int category, String prompt, String initImg, String promptDesc, String promptZh, String summonWord, String negativePrompt, String negativePromptZh, Integer isRedraw);

    TableDataInfo<SdUserModelFileVo> listUserModelFile(PageQuery pageQuery, Long userId, SdUserModelFilePageDto dto);

    List<SdUserModelFileVo> listUserModelFile(String taskId, Long userId);

    void removeUserModelFile(List<String> ids, Long userId);

    void removeUserModelFileByTaskId(String taskId);

    List<String> listImgUrlByTaskId(String taskId, Long userId);

    List<String> listImgUrlByIds(List<String> ids);

    List<JSONObject> selectModelTestDataAndTaskInfo(String loraModelId);
}
