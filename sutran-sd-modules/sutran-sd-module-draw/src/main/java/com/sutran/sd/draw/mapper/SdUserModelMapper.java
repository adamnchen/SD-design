package com.sutran.sd.draw.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.dto.model.SdUserModelDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelShareDto;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.vo.SdUserModelVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserModelMapper extends BaseMapperPlus<SdUserModelMapper, SdUserModel, SdUserModel> {
    Page<SdUserModelVo> selectAllList(@Param("dto") SdUserModelPageDto dto, @Param("userId") Long userId, @Param("page") Page<SysUser> page);

    Page<SdUserModelVo> selectAllListOfAdmin(@Param("page") Page<SysUser> page, @Param("dto") SdUserModelDto dto);

    SdUserModelVo getModelInfo(@Param("id") String id, @Param("userId") Long userId);

    List<SdUserModelVo> getLatestModelInfo(@Param("userId") Long userId, @Param("limit") int limit);

    void insertData(SdUserModel model);

    void publishModel(@Param("id") String id, @Param("publishStatus") Integer publishStatus, @Param("isUserDel") Integer isUserDel, @Param("modelStrength") String modelStrength);

    void modifyModelStrength(@Param("id") String id, @Param("modelStrength") String modelStrength);

    List<JSONObject> selectInfosByIds(@Param("ids") Set<String> ids);

    List<String> selectHashList();

    JSONObject selectUserOpenIdAndPhoneById(@Param("id") String id);

    JSONObject selectLoraModelNameByTaskId(@Param("taskId") String taskId);

    @Delete("DELETE FROM sd_user_model_share WHERE model_id=#{modelId} AND user_id=#{userId}")
    void removeShareModelById(@Param("modelId") String modelId, @Param("userId") Long userId);

    void shareModel(@Param("dto") SdUserModelShareDto dto, @Param("userId") Long userId, @Param("crtTime") Date crtTime);
}
