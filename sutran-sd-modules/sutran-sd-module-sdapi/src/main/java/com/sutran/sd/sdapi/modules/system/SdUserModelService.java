package com.sutran.sd.sdapi.modules.system;

import com.alibaba.fastjson2.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelModifyDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.sdapi.domain.dto.model.SdUserModelShareDto;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModel;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelVo;

import java.util.List;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserModelService {

    void batchInsert(List<SdUserModel> models);

    TableDataInfo<SdUserModelVo> selectAllList(SdUserModelPageDto dto, Long userId, PageQuery pageQuery);

    TableDataInfo<SdUserModelVo> listLoraModelsInTaskAndTrainData(SdUserModelDto dto);

    void modifyModel(SdUserModelModifyDto dto, Long userId);

    SdUserModelVo getModelInfo(String modelId, Long userId);

    List<SdUserModelVo> getLatestModelInfo(Long userId, int limit);

    SdUserModel selectById(String modelId);

    void publishModel(String id, Integer publishStatus, Integer isUserDel, String modelStrength);

    void modifyModelStrength(String id, String modelStrength);

    boolean isExistByClassifyId(String id, Long userId);

    void removeModelById(String id, Long userId);

    void removeModelOfAdminById(long id);

    List<SdUserModel> selectByIds(List<String> ids);

    List<String> selectHashList();

    JSONObject selectUserOpenIdAndPhoneById(String id);

    /**
     * 删除分享给指定人的指定模型
     * @param modelId   模型ID
     * @param userId    用户ID
     */
    void removeShareModelById(String modelId, Long userId);

    /**
     * 分享指定模型
     * @param dto       分享参数实体
     * @param userId    模型拥有者userId
     */
    void shareModel(SdUserModelShareDto dto, Long userId);
}
