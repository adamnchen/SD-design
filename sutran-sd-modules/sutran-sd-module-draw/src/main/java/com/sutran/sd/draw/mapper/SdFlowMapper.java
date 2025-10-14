package com.sutran.sd.draw.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdFlow;
import com.sutran.sd.draw.domain.vo.ComfyuiImageToolVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zj
 * @date 2025年09月07日 23:19
 */
@Mapper
public interface SdFlowMapper extends BaseMapperPlus<SdFlowMapper, SdFlow, SdFlow> {

    /**
     * 查询固定工作流(修复工具)
     * @return 数据集合
     */
    @Select("SELECT id AS flowId,name AS toolName,draw_num AS drawNum,model_type AS modelType,init_prompt AS initPrompt,init_prompt_zh AS initPromptZh FROM sd_flow WHERE is_fixed=1")
    List<ComfyuiImageToolVo> queryFixedFlowList();
}
