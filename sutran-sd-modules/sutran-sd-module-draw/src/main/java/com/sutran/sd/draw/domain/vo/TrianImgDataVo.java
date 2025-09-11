package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 训练图片数据
 * @author zj
 * @date 2025年06月22日 10:14
 */
@Data
@Accessors(chain=true)
public class TrianImgDataVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 图片路径
     */
    private String img;
    /**
     * 图片标签集合
     */
    private List<String> tags = new ArrayList<>();
}
