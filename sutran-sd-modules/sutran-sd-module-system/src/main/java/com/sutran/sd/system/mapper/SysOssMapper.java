package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.system.domain.SysOss;
import com.sutran.sd.system.domain.vo.SysOssVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传 数据层
 *
 * @author Lion Li
 */
@Mapper
public interface SysOssMapper extends BaseMapperPlus<SysOssMapper, SysOss, SysOssVo> {
}
