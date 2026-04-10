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
@ApiModel("验签响应")
public class VerifyResponse {

    @ApiModelProperty(value = "是否成功", example = "true")
    private Boolean success;

    @ApiModelProperty(value = "签名是否有效", example = "true")
    private Boolean valid;

    @ApiModelProperty(value = "响应消息", example = "验签成功")
    private String message;

}
