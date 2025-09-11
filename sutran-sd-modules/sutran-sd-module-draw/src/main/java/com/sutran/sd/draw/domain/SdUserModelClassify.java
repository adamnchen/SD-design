package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表实体类
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SdUserModelClassify extends Model<SdUserModelClassify> implements Serializable {
    /**
     * 分类ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 创建人
     */
    private Long crtUserId;
    /**
     * 创建时间
     */
    private Date crtTime;
}

