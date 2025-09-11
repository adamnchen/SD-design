package com.sutran.sd.draw.domain.dto.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表实体类
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
@Data
@Accessors(chain = true)
public class SdUserModelClassifyDto implements Serializable {
    /**
     * 分类ID（修改时传递）
     */
    private String id;
    /**
     * 分类名称
     */
    private String name;
}

