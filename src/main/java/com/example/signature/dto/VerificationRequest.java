package com.example.signature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "验签请求", description = "验签接口请求参数")
public class VerificationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "签名ID", required = true)
    private String signatureId;

    @ApiModelProperty(value = "原始数据", required = true)
    private String data;

    @ApiModelProperty(value = "签名结果", required = true)
    private String signature;
}
