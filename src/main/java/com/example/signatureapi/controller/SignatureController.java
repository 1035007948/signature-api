package com.example.signatureapi.controller;

import com.example.signatureapi.dto.ApiResponse;
import com.example.signatureapi.dto.SignRequest;
import com.example.signatureapi.dto.VerifyRequest;
import com.example.signatureapi.entity.SignatureRecord;
import com.example.signatureapi.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "签名验签接口")
@RestController
@RequestMapping("/api/signature")
public class SignatureController {

    private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

    @Resource
    private SignatureService signatureService;

    @ApiOperation("生成签名接口")
    @PostMapping("/sign")
    public ApiResponse<Map<String, Object>> sign(@Valid @RequestBody SignRequest request) {
        logger.info("收到签名请求，signer: {}", request.getSigner());
        Map<String, Object> result = signatureService.sign(request);
        return ApiResponse.success(result);
    }

    @ApiOperation("验签接口")
    @PostMapping("/verify")
    public ApiResponse<Map<String, Object>> verify(@Valid @RequestBody VerifyRequest request) {
        logger.info("收到验签请求");
        Map<String, Object> result = signatureService.verify(request);
        return ApiResponse.success(result);
    }

    @ApiOperation("根据签名值查询签名记录")
    @GetMapping("/record/{signature}")
    public ApiResponse<SignatureRecord> getRecordBySignature(
            @ApiParam(value = "签名值", required = true) @PathVariable String signature) {
        SignatureRecord record = signatureService.getRecordBySignature(signature);
        if (record == null) {
            return ApiResponse.error("签名记录不存在");
        }
        return ApiResponse.success(record);
    }

    @ApiOperation("根据请求ID查询签名记录")
    @GetMapping("/record/request/{requestId}")
    public ApiResponse<SignatureRecord> getRecordByRequestId(
            @ApiParam(value = "请求ID", required = true) @PathVariable String requestId) {
        SignatureRecord record = signatureService.getRecordByRequestId(requestId);
        if (record == null) {
            return ApiResponse.error("签名记录不存在");
        }
        return ApiResponse.success(record);
    }
}
