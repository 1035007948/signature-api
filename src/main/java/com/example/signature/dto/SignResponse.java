package com.example.signature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("签名响应")
public class SignResponse {

    @ApiModelProperty(value = "是否成功", example = "true")
    private Boolean success;

    @ApiModelProperty(value = "响应消息", example = "签名成功")
    private String message;

    @ApiModelProperty(value = "签名结果", example = "A1B2C3D4E5F6...")
    private String signature;

    @ApiModelProperty(value = "签名类型", example = "SHA256")
    private String signType;

    @ApiModelProperty(value = "时间戳", example = "1704067200000")
    private String timestamp;

}
