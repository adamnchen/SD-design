package com.sutran.sd.draw.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 文生图参数请求实体
 * @author zj
 * @date 2024-02-27
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class AlwaysonScripts {
    private ControlNet ControlNet;
}
