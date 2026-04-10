package com.example.signature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "签名响应", description = "签名接口响应结果")
public class SignatureResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "签名ID")
    private String signatureId;

    @ApiModelProperty(value = "签名结果")
    private String signature;

    @ApiModelProperty(value = "算法")
    private String algorithm;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "过期时间")
    private String expireTime;

    @ApiModelProperty(value = "是否成功")
    private Boolean success;

    @ApiModelProperty(value = "消息")
    private String message;
}
