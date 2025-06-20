package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.system.domain.SysLogininfor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问日志情况信息 数据层
 *
 * @author Lion Li
 */
@Mapper
public interface SysLogininforMapper extends BaseMapperPlus<SysLogininforMapper, SysLogininfor, SysLogininfor> {

}
