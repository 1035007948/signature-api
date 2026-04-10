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
@ApiModel("签名请求")
public class SignRequest {

    @NotBlank(message = "appId不能为空")
    @ApiModelProperty(value = "应用ID", required = true, example = "APP001")
    private String appId;

    @NotBlank(message = "时间戳不能为空")
    @ApiModelProperty(value = "时间戳", required = true, example = "1704067200000")
    private String timestamp;

    @NotBlank(message = "随机数不能为空")
    @ApiModelProperty(value = "随机数", required = true, example = "abc123xyz")
    private String nonce;

    @ApiModelProperty(value = "数据内容", example = "{\"orderNo\":\"202401010001\",\"amount\":100.00}")
    private String data;

}
