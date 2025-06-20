package com.sutran.sd.sdapi.extensions;

import com.sutran.sd.sdapi.domain.dto.ControlNet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zj
 * @date 2024-02-27
 */
public class ControlNetUnit implements  Plugin {

    private List<ControlNet> controlNets=new ArrayList<>();
    public ControlNetUnit(List<ControlNet> net){
        controlNets.addAll(net);
    }
    public ControlNetUnit(ControlNet net){
        controlNets.add(net);
    }

    @Override
    public Object toDict() {
        return controlNets;
    }

    @Override
    public String getName() {
        return "ControlNet";
    }

}
