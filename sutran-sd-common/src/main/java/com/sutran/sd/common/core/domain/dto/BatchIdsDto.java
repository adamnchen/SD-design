package com.sutran.sd.common.core.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zj
 * @date 2024-03-10
 */
@Data
public class BatchIdsDto implements Serializable {

    /**
     * 需要删除的id集合
     */
    private List<String> ids;

}
