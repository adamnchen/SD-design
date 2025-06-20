package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.util.IdUtil;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModelLog;
import com.sutran.sd.sdapi.mapper.SdUserModelLogMapper;
import com.sutran.sd.sdapi.modules.system.SdUserModelLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author zj
 * @date 2024-03-03
 */
@Slf4j
@Service("SdUserModelLogService")
public class SdUserModelLogServiceImpl implements SdUserModelLogService {

    @Resource
    private SdUserModelLogMapper baseMapper;

    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncInsertData(Long userId, String userName, Long modelId, String loraTitle, String modelName, String modelStrength) {
        SdUserModelLog modelLog = new SdUserModelLog().setId(IdUtil.getSnowflakeNextId()).setUserId(userId).setUserName(userName).setModelName(modelName).setLoraModelId(modelId).setLoraTitle(loraTitle).setModelStrength(modelStrength).setCrtTime(new Date());
        baseMapper.insertEntity(modelLog);
    }
}
