/**
 * 签名和验签控制器层
 *
 * 提供 RESTful API 接口：
 * 1. POST /api/v1/signature/sign - 生成数字签名
 * 2. POST /api/v1/signature/verify - 验证签名有效性
 * 3. GET /api/v1/signature/health - 服务健康检查
 */
package com.example.signature.controller;

import com.example.signature.dto.SignRequest;
import com.example.signature.dto.SignResponse;
import com.example.signature.dto.VerifyRequest;
import com.example.signature.dto.VerifyResponse;
import com.example.signature.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 签名控制器类
 * 处理所有与签名相关的 HTTP 请求
 */
@Slf4j  // Lombok 日志注解
@RestController  // Spring REST 控制器注解
@RequestMapping("/api/v1/signature")  // 基础请求路径
@RequiredArgsConstructor  // Lombok 生成构造方法
@Api(tags = "签名和验签接口")  // Swagger 接口分组标签
public class SignatureController {

    /**
     * 签名服务层
     * 通过构造方法注入（由 @RequiredArgsConstructor 生成）
     */
    private final SignatureService signatureService;

    /**
     * 签名接口
     *
     * 接收签名请求，调用服务层生成数字签名
     *
     * @param request 签名请求对象，包含 appId、timestamp、nonce、data
     *                使用 @Valid 进行参数校验
     *                使用 @RequestBody 接收 JSON 格式的请求体
     * @return ResponseEntity<SignResponse> 包含签名结果的 HTTP 响应
     *
     * 请求示例：
     * POST /api/v1/signature/sign
     * {
     *   "appId": "APP001",
     *   "timestamp": "1704067200000",
     *   "nonce": "abc123",
     *   "data": "{\"orderNo\":\"202401010001\"}"
     * }
     */
    @PostMapping("/sign")
    @ApiOperation(value = "签名接口", notes = "对请求数据进行签名，返回签名结果")
    public ResponseEntity<SignResponse> sign(@Valid @RequestBody SignRequest request) {
        log.info("收到签名请求，appId: {}", request.getAppId());
        SignResponse response = signatureService.sign(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 验签接口
     *
     * 接收验签请求，验证签名的有效性
     *
     * @param request 验签请求对象，包含 appId 和 signature
     *                使用 @Valid 进行参数校验
     * @return ResponseEntity<VerifyResponse> 包含验签结果的 HTTP 响应
     *
     * 请求示例：
     * POST /api/v1/signature/verify
     * {
     *   "appId": "APP001",
     *   "signature": "A1B2C3D4E5F6...",
     *   "data": "{\"orderNo\":\"202401010001\"}"
     * }
     */
    @PostMapping("/verify")
    @ApiOperation(value = "验签接口", notes = "验证签名是否有效")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyRequest request) {
        log.info("收到验签请求，appId: {}", request.getAppId());
        VerifyResponse response = signatureService.verify(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     *
     * 用于监控系统检查服务是否正常运行
     *
     * @return ResponseEntity<String> 返回 "OK" 表示服务正常
     *
     * 请求示例：
     * GET /api/v1/signature/health
     */
    @GetMapping("/health")
    @ApiOperation(value = "健康检查", notes = "检查服务是否正常运行")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

}
