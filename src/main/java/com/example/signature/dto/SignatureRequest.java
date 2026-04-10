package com.example.signature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "签名请求", description = "签名接口请求参数")
public class SignatureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原始数据", required = true, example = "Hello World")
    private String data;

    @ApiModelProperty(value = "签名算法", example = "SHA256withRSA", allowableValues = "SHA256withRSA,SHA1withRSA,MD5withRSA")
    private String algorithm;

    @ApiModelProperty(value = "过期时间（秒）", example = "3600")
    private Long expireSeconds;
}
