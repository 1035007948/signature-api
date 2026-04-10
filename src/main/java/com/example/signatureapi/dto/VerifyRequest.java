package com.example.signatureapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
@ApiModel(description = "验签请求参数")
public class VerifyRequest {

    @ApiModelProperty(value = "签名值", required = true, example = "a1b2c3d4...")
    @NotBlank(message = "签名值不能为空")
    private String signature;

    @ApiModelProperty(value = "原始内容", example = "orderId=123456&amount=100.00")
    private String content;

    @ApiModelProperty(value = "签名算法", example = "HMAC-SHA256")
    private String algorithm = "HMAC-SHA256";

    @ApiModelProperty(value = "密钥", example = "your-secret-key")
    private String secretKey;

    @ApiModelProperty(value = "原始参数Map")
    private Map<String, String> params;
}
