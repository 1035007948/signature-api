/**
 * 签名服务层
 *
 * 提供签名生成和验签验证的业务逻辑
 * 数据存储：MySQL 数据库
 * 缓存机制：Redis（24小时过期）
 */
package com.example.signature.service;

import com.example.signature.dto.SignRequest;
import com.example.signature.dto.SignResponse;
import com.example.signature.dto.VerifyRequest;
import com.example.signature.dto.VerifyResponse;
import com.example.signature.entity.SignatureRecord;
import com.example.signature.repository.SignatureRecordRepository;
import com.example.signature.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 签名业务服务类
 * 处理签名生成和验签验证的核心业务逻辑
 */
@Slf4j  // Lombok 日志注解
@Service  // Spring 服务层注解
@RequiredArgsConstructor  // Lombok 生成包含 final 字段的构造方法
public class SignatureService {

    /**
     * 签名记录数据访问层
     * 用于操作 signature_record 表
     */
    private final SignatureRecordRepository signatureRecordRepository;

    /**
     * Redis 字符串操作模板
     * 用于签名结果的缓存存储
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * Redis 键前缀
     * 用于区分不同类型的缓存数据
     */
    private static final String REDIS_KEY_PREFIX = "signature:";

    /**
     * Redis 缓存过期时间（小时）
     * 签名结果缓存 24 小时
     */
    private static final long REDIS_EXPIRE_HOURS = 24;

    /**
     * 生成数字签名
     *
     * 业务流程：
     * 1. 构建签名参数
     * 2. 调用工具类生成签名
     * 3. 保存签名记录到 MySQL
     * 4. 缓存签名到 Redis
     * 5. 返回签名结果
     *
     * @param request 签名请求对象，包含 appId、timestamp、nonce、data
     * @return 签名响应对象，包含签名结果和相关信息
     */
    public SignResponse sign(SignRequest request) {
        log.info("开始签名，appId: {}", request.getAppId());

        // 构建签名参数集合
        Map<String, String> params = new HashMap<>();
        params.put("appId", request.getAppId());
        params.put("timestamp", request.getTimestamp());
        params.put("nonce", request.getNonce());
        if (request.getData() != null) {
            params.put("data", request.getData());
        }

        // 调用工具类生成 SHA256 签名
        String signature = SignatureUtil.generateSignature(params);

        // 构建签名记录实体对象
        SignatureRecord record = SignatureRecord.builder()
                .appId(request.getAppId())
                .timestamp(request.getTimestamp())
                .nonce(request.getNonce())
                .dataContent(request.getData())
                .signature(signature)
                .signType("SHA256")
                .build();

        // 保存到数据库
        SignatureRecord savedRecord = signatureRecordRepository.save(record);

        // 缓存到 Redis，设置 24 小时过期
        String redisKey = REDIS_KEY_PREFIX + signature;
        redisTemplate.opsForValue().set(redisKey, signature, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        log.info("签名完成，记录ID: {}", savedRecord.getId());

        // 构建并返回响应对象
        return SignResponse.builder()
                .success(true)
                .message("签名成功")
                .signature(signature)
                .signType("SHA256")
                .timestamp(request.getTimestamp())
                .build();
    }

    /**
     * 验证签名有效性
     *
     * 业务流程：
     * 1. 优先从 Redis 缓存查询
     * 2. 缓存未命中则从 MySQL 查询
     * 3. 验证签名是否匹配
     * 4. 验证成功则更新缓存
     * 5. 返回验签结果
     *
     * @param request 验签请求对象，包含 appId 和 signature
     * @return 验签响应对象，包含验证结果
     */
    public VerifyResponse verify(VerifyRequest request) {
        log.info("开始验签，appId: {}", request.getAppId());

        // 构建 Redis 缓存键
        String redisKey = REDIS_KEY_PREFIX + request.getSignature();
        // 尝试从 Redis 获取缓存
        String cachedSignature = redisTemplate.opsForValue().get(redisKey);

        // 缓存命中，直接返回成功
        if (cachedSignature != null) {
            log.info("从Redis缓存中找到签名记录");
            return VerifyResponse.builder()
                    .success(true)
                    .valid(true)
                    .message("验签成功（来自缓存）")
                    .build();
        }

        // 缓存未命中，查询数据库
        Optional<SignatureRecord> recordOpt = signatureRecordRepository.findBySignature(request.getSignature());

        // 数据库中存在记录
        if (recordOpt.isPresent()) {
            SignatureRecord record = recordOpt.get();

            // 重建签名参数
            Map<String, String> params = new HashMap<>();
            params.put("appId", record.getAppId());
            params.put("timestamp", record.getTimestamp());
            params.put("nonce", record.getNonce());
            if (record.getDataContent() != null) {
                params.put("data", record.getDataContent());
            }

            // 验证签名是否匹配
            boolean isValid = SignatureUtil.verifySignature(params, request.getSignature());

            // 验证成功，更新缓存
            if (isValid) {
                redisTemplate.opsForValue().set(redisKey, request.getSignature(), REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
            }

            log.info("验签完成，结果: {}", isValid);

            // 返回验签结果
            return VerifyResponse.builder()
                    .success(true)
                    .valid(isValid)
                    .message(isValid ? "验签成功" : "验签失败，签名不匹配")
                    .build();
        }

        // 未找到签名记录
        log.warn("未找到签名记录");
        return VerifyResponse.builder()
                .success(false)
                .valid(false)
                .message("验签失败，未找到签名记录")
                .build();
    }

}
