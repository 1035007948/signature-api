package com.example.signature.controller;

import com.example.signature.dto.SignatureRequest;
import com.example.signature.dto.SignatureResponse;
import com.example.signature.dto.VerificationRequest;
import com.example.signature.dto.VerificationResponse;
import com.example.signature.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/signature")
@Api(tags = "签名验签接口")
public class SignatureController {

    @Autowired
    private SignatureService signatureService;

    @PostMapping("/sign")
    @ApiOperation(value = "创建签名", notes = "对数据进行签名，返回签名结果")
    public ResponseEntity<SignatureResponse> createSignature(
            @ApiParam(value = "签名请求", required = true) @RequestBody SignatureRequest request) {
        log.info("Received signature request for data: {}", request.getData());
        SignatureResponse response = signatureService.createSignature(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @ApiOperation(value = "验证签名", notes = "验证签名的有效性")
    public ResponseEntity<VerificationResponse> verifySignature(
            @ApiParam(value = "验签请求", required = true) @RequestBody VerificationRequest request) {
        log.info("Received verification request for signatureId: {}", request.getSignatureId());
        VerificationResponse response = signatureService.verifySignature(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @ApiOperation(value = "健康检查", notes = "检查服务是否正常运行")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }
}
