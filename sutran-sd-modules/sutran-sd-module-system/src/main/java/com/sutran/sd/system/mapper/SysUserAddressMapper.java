package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.common.core.domain.entity.SysAddress;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import javax.mail.MailSessionDefinition;
import java.util.List;

/**
 * 用户地址 数据层
 *
 * @author chen shan
 */
@Mapper
public interface SysUserAddressMapper extends BaseMapperPlus<SysUserAddressMapper, SysAddress, SysAddress> {
    List<SysAddress> selectAddressList(Long userId);
}
