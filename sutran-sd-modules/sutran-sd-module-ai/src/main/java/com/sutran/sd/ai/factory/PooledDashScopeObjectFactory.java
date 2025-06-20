package com.sutran.sd.ai.factory;

/**
 * @author zj
 * @date 2024-12-20
 */

import com.alibaba.dashscope.aigc.generation.Generation;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class PooledDashScopeObjectFactory extends BasePooledObjectFactory<Generation> {

    @Override
    public Generation create() throws Exception {
        return new Generation();
    }

    @Override
    public PooledObject<Generation> wrap(Generation obj) {
        return new DefaultPooledObject<>(obj);
    }
}
