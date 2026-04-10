package com.example.signatureapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
@ApiModel(description = "签名请求参数")
public class SignRequest {

    @ApiModelProperty(value = "签名方标识", required = true, example = "client001")
    @NotBlank(message = "签名方标识不能为空")
    private String signer;

    @ApiModelProperty(value = "待签名内容", required = true, example = "orderId=123456&amount=100.00")
    @NotBlank(message = "待签名内容不能为空")
    private String content;

    @ApiModelProperty(value = "签名算法", example = "HMAC-SHA256")
    private String algorithm = "HMAC-SHA256";

    @ApiModelProperty(value = "密钥", example = "your-secret-key")
    private String secretKey;

    @ApiModelProperty(value = "待签名参数Map")
    private Map<String, String> params;

    @ApiModelProperty(value = "过期时间（小时）", example = "24")
    private Integer expireHours = 24;
}
