package com.sutran.sd.train.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author zj
 * @date 2024-04-15
 */
public class RefreshLoraEvent extends ApplicationEvent {
    public RefreshLoraEvent(Boolean source) {
        super(source);
    }
}
