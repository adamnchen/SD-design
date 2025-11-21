package com.sutran.sd.design.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "更新快递单号请求体")
public class UpdateTrackingNumberDTO {

    @NotBlank(message = "快递单号不能为空")
    @Schema(description = "快递单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "预售发货记录ID")
    private Long deliveryId;
}


