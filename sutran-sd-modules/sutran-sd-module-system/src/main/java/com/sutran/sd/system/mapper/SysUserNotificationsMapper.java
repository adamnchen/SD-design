package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.system.domain.SysUserNotifications;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知 数据层
 *
 * @author zj
 */
@Mapper
public interface SysUserNotificationsMapper extends BaseMapperPlus<SysUserNotificationsMapper, SysUserNotifications, SysUserNotifications> {

}
