package com.example.signature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "验签响应", description = "验签接口响应结果")
public class VerificationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "签名ID")
    private String signatureId;

    @ApiModelProperty(value = "验证结果")
    private Boolean verified;

    @ApiModelProperty(value = "是否成功")
    private Boolean success;

    @ApiModelProperty(value = "消息")
    private String message;

    @ApiModelProperty(value = "验证次数")
    private Integer verificationCount;
}
