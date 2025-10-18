package com.sutran.sd.draw.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025年10月11日 13:45
 */
@Data
@Accessors(chain=true)
public class ImageInfoBo implements Serializable {
    private String imageName;
    private String contentType;
    private byte[] fileData;
}
