package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.system.domain.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 通知公告表 数据层
 *
 * @author Lion Li
 */
@Mapper
public interface SysNoticeMapper extends BaseMapperPlus<SysNoticeMapper, SysNotice, SysNotice> {

    /**
     * 获取系统通知\公告总数（未过期）
     * @return 系统通知\公告总数（未过期）
     */
    @Select("select count(1) from sys_notice where status = '1' and expire_time > now()")
    long sysMsgTotalOfNotExpire();

}
