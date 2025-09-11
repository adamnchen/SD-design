package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表实体类
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
@Data
@Accessors(chain = true)
public class SdUserModelClassifyVo implements Serializable {
    /**
     * 分类ID
     */
    private String id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 创建人
     */
    private String crtUserId;
    /**
     * 创建时间
     */
    private Date crtTime;
}

