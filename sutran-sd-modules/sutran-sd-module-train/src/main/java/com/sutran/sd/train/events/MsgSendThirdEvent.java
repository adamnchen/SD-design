package com.sutran.sd.train.events;

import com.sutran.sd.draw.domain.dto.ImgSendThirdDto;
import org.springframework.context.ApplicationEvent;

/**
 * @author zj
 * @date 2024-04-15
 */
public class MsgSendThirdEvent extends ApplicationEvent {
    public MsgSendThirdEvent(ImgSendThirdDto source) {
        super(source);
    }
}
