package com.example.signatureapi.service;

import com.example.signatureapi.dto.SignRequest;
import com.example.signatureapi.dto.VerifyRequest;
import com.example.signatureapi.entity.SignatureRecord;
import com.example.signatureapi.repository.SignatureRecordRepository;
import com.example.signatureapi.util.SignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名验签核心业务服务类
 * 实现签名生成、验签、记录查询等核心功能
 * 采用MySQL + Redis二级缓存架构保证高性能和数据一致性
 *
 * @author Signature API Team
 * @version 1.0.0
 * @since 2026-04-10
 */
@Service
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    /**
     * 数据库操作DAO
     */
    @Resource
    private SignatureRecordRepository signatureRecordRepository;

    /**
     * Redis缓存服务
     */
    @Resource
    private RedisCacheService redisCacheService;

    /**
     * 生成数字签名
     * 1. 生成唯一请求ID
     * 2. 根据内容/参数生成签名值
     * 3. 签名记录持久化到MySQL
     * 4. 签名信息写入Redis缓存
     *
     * @param request 签名请求参数，包含签名方、内容、算法等
     * @return 包含requestId、signature、algorithm的结果Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sign(SignRequest request) {
        logger.info("开始处理签名请求，signer: {}", request.getSigner());

        String requestId = SignatureUtil.generateRequestId();
        String content = request.getContent();
        Map<String, String> params = request.getParams();

        String signatureValue;
        if (params != null && !params.isEmpty()) {
            signatureValue = SignatureUtil.sign(params, request.getSecretKey(), request.getAlgorithm());
        } else {
            signatureValue = SignatureUtil.sign(content, request.getSecretKey(), request.getAlgorithm());
        }

        SignatureRecord record = new SignatureRecord();
        record.setRequestId(requestId);
        record.setSigner(request.getSigner());
        record.setContent(content != null ? content : params.toString());
        record.setSignatureValue(signatureValue);
        record.setAlgorithm(request.getAlgorithm());
        record.setExpireTime(LocalDateTime.now().plusHours(request.getExpireHours()));
        signatureRecordRepository.save(record);

        Map<String, Object> signatureData = new HashMap<>();
        signatureData.put("requestId", requestId);
        signatureData.put("signer", request.getSigner());
        signatureData.put("content", content);
        signatureData.put("algorithm", request.getAlgorithm());
        redisCacheService.saveSignature(signatureValue, signatureData);

        Map<String, Object> result = new HashMap<>();
        result.put("requestId", requestId);
        result.put("signature", signatureValue);
        result.put("algorithm", request.getAlgorithm());

        logger.info("签名处理完成，requestId: {}, signature: {}", requestId, signatureValue);
        return result;
    }

    /**
     * 验签处理
     * 高性能验签流程：先查Redis缓存，再查MySQL数据库
     * 1. 优先从Redis获取签名数据（微秒级响应）
     * 2. 数据库查询签名记录做最终比对
     * 3. 重新计算签名与传入值进行比对
     * 4. 更新验签次数统计
     *
     * @param request 验签请求参数，包含签名值、原始内容、算法等
     * @return 包含验签结果、签名信息的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> verify(VerifyRequest request) {
        logger.info("开始处理验签请求");

        Map<String, Object> result = new HashMap<>();
        boolean valid = false;
        String cachedSigner = null;

        Object cachedData = redisCacheService.getSignature(request.getSignature());
        if (cachedData != null) {
            logger.info("从Redis缓存中找到签名数据");
            Map<?, ?> cacheMap = (Map<?, ?>) cachedData;
            cachedSigner = (String) cacheMap.get("signer");
            result.put("fromCache", true);
        } else {
            logger.info("从数据库查询签名记录");
            result.put("fromCache", false);
        }

        SignatureRecord record = signatureRecordRepository.findBySignatureValue(request.getSignature()).orElse(null);

        if (record != null) {
            String content = request.getContent();
            Map<String, String> params = request.getParams();

            if (params != null && !params.isEmpty()) {
                valid = SignatureUtil.verify(params, request.getSignature(), request.getSecretKey(), request.getAlgorithm());
            } else {
                valid = SignatureUtil.verify(content, request.getSignature(), request.getSecretKey(), request.getAlgorithm());
            }

            record.setVerifiedCount(record.getVerifiedCount() + 1);
            signatureRecordRepository.save(record);

            result.put("requestId", record.getRequestId());
            result.put("signer", record.getSigner());
            result.put("timestamp", record.getTimestamp());
            result.put("verifiedCount", record.getVerifiedCount());
        } else {
            logger.warn("签名记录不存在: {}", request.getSignature());
        }

        if (cachedSigner != null) {
            result.put("signer", cachedSigner);
        }

        result.put("valid", valid);
        result.put("signature", request.getSignature());

        logger.info("验签处理完成，签名: {}, 结果: {}", request.getSignature(), valid);
        return result;
    }

    /**
     * 根据签名值查询签名记录
     *
     * @param signature 签名值
     * @return 签名记录实体，不存在返回null
     */
    public SignatureRecord getRecordBySignature(String signature) {
        return signatureRecordRepository.findBySignatureValue(signature).orElse(null);
    }

    /**
     * 根据请求ID查询签名记录
     *
     * @param requestId 32位唯一请求ID
     * @return 签名记录实体，不存在返回null
     */
    public SignatureRecord getRecordByRequestId(String requestId) {
        return signatureRecordRepository.findByRequestId(requestId).orElse(null);
    }
}
