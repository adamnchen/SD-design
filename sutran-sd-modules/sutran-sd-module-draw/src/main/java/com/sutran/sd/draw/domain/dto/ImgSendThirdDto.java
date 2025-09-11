package com.sutran.sd.draw.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 发送生成图片到第三方
 * @author zj
 * @date 2024-09-24
 */
@Data
@Accessors(chain = true)
public class ImgSendThirdDto implements Serializable {
    private Long userId;
    private List<String> imgUrlList;
}
