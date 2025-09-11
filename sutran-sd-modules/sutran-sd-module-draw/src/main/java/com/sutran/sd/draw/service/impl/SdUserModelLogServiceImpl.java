package com.sutran.sd.draw.service.impl;

import cn.hutool.core.util.IdUtil;
import com.sutran.sd.draw.domain.SdUserModelLog;
import com.sutran.sd.draw.mapper.SdUserModelLogMapper;
import com.sutran.sd.draw.service.SdUserModelLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author zj
 * @date 2024-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdUserModelLogServiceImpl implements SdUserModelLogService {

    private final SdUserModelLogMapper baseMapper;

    @Async("threadPoolTaskExecutor")
    @Override
    public void asyncInsertData(Long userId, String userName, Long modelId, String loraTitle, String modelName, String modelStrength) {
        SdUserModelLog modelLog = new SdUserModelLog().setId(IdUtil.getSnowflakeNextId()).setUserId(userId).setUserName(userName).setModelName(modelName).setLoraModelId(modelId).setLoraTitle(loraTitle).setModelStrength(modelStrength).setCrtTime(new Date());
        baseMapper.insertEntity(modelLog);
    }
}
