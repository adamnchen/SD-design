package com.sutran.sd.sdapi.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
@Mapper
public interface SdGpuPoolMapper extends BaseMapperPlus<SdGpuPoolMapper, SdGpuPool, SdGpuPool> {

    @Select("SELECT id, host, port, device_id AS deviceId, type, img_grid_dir AS imgGridDir, txt_grid_dir AS txtGridDir, is_enable AS isEnable FROM sd_gpu_pool WHERE type=#{type} AND is_enable=1")
    List<SdGpuPool> selectEnableGpuListByType(@Param("type") int type);
}
