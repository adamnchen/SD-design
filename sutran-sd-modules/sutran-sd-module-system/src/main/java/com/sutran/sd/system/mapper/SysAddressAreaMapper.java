package com.sutran.sd.system.mapper;
import com.sutran.sd.common.core.domain.entity.SysAddressArea; // 导入正确的实体类
import com.sutran.sd.common.core.mapper.BaseMapperPlus; // 假设这是你的基础 Mapper
import org.apache.ibatis.annotations.Mapper;
/**
 * 【SysAddressAreaMapper】
 * 行政区划（省市区县）数据层
 *
 * @author chen shan
 */
@Mapper
public interface SysAddressAreaMapper extends BaseMapperPlus<SysAddressAreaMapper, SysAddressArea, SysAddressArea> {
}
