package com.example.signature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("验签请求")
public class VerifyRequest {

    @NotBlank(message = "appId不能为空")
    @ApiModelProperty(value = "应用ID", required = true, example = "APP001")
    private String appId;

    @NotBlank(message = "签名不能为空")
    @ApiModelProperty(value = "待验证的签名", required = true, example = "A1B2C3D4E5F6...")
    private String signature;

    @ApiModelProperty(value = "数据内容", example = "{\"orderNo\":\"202401010001\",\"amount\":100.00}")
    private String data;

}
