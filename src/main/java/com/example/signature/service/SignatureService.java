package com.example.signature.service;

import com.example.signature.dto.SignatureRequest;
import com.example.signature.dto.SignatureResponse;
import com.example.signature.dto.VerificationRequest;
import com.example.signature.dto.VerificationResponse;
import com.example.signature.entity.SignatureRecord;
import com.example.signature.repository.SignatureRecordRepository;
import com.example.signature.util.SignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
public class SignatureService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_ALGORITHM = "SHA256";

    @Value("${signature.secret-key:default-secret-key-12345}")
    private String secretKey;

    @Autowired
    private SignatureRecordRepository signatureRecordRepository;

    @Autowired
    private CacheService cacheService;

    @Transactional
    public SignatureResponse createSignature(SignatureRequest request) {
        try {
            String signatureId = SignatureUtil.generateSignatureId();
            String algorithm = request.getAlgorithm() != null ? request.getAlgorithm() : DEFAULT_ALGORITHM;
            String signature = SignatureUtil.sign(request.getData(), algorithm, secretKey);

            SignatureRecord record = new SignatureRecord();
            record.setSignatureId(signatureId);
            record.setOriginalData(request.getData());
            record.setSignatureResult(signature);
            record.setAlgorithm(algorithm);

            if (request.getExpireSeconds() != null && request.getExpireSeconds() > 0) {
                record.setExpireTime(LocalDateTime.now().plusSeconds(request.getExpireSeconds()));
            }

            signatureRecordRepository.save(record);
            cacheService.cacheSignature(signatureId, record);

            log.info("Signature created successfully, signatureId: {}", signatureId);

            SignatureResponse response = new SignatureResponse();
            response.setSignatureId(signatureId);
            response.setSignature(signature);
            response.setAlgorithm(algorithm);
            response.setCreateTime(record.getCreateTime().format(FORMATTER));
            if (record.getExpireTime() != null) {
                response.setExpireTime(record.getExpireTime().format(FORMATTER));
            }
            response.setSuccess(true);
            response.setMessage("签名创建成功");

            return response;
        } catch (Exception e) {
            log.error("Failed to create signature", e);
            SignatureResponse response = new SignatureResponse();
            response.setSuccess(false);
            response.setMessage("签名创建失败: " + e.getMessage());
            return response;
        }
    }

    @Transactional
    public VerificationResponse verifySignature(VerificationRequest request) {
        try {
            SignatureRecord record = getSignatureRecord(request.getSignatureId());
            if (record == null) {
                VerificationResponse response = new VerificationResponse();
                response.setSignatureId(request.getSignatureId());
                response.setVerified(false);
                response.setSuccess(false);
                response.setMessage("签名记录不存在");
                return response;
            }

            if (record.getExpireTime() != null && LocalDateTime.now().isAfter(record.getExpireTime())) {
                VerificationResponse response = new VerificationResponse();
                response.setSignatureId(request.getSignatureId());
                response.setVerified(false);
                response.setSuccess(false);
                response.setMessage("签名已过期");
                return response;
            }

            boolean verified = SignatureUtil.verify(
                request.getData(),
                request.getSignature(),
                record.getAlgorithm(),
                secretKey
            );

            record.setVerificationCount(record.getVerificationCount() + 1);
            record.setLastVerificationTime(LocalDateTime.now());
            signatureRecordRepository.save(record);
            cacheService.updateSignatureInCache(request.getSignatureId(), record);

            log.info("Signature verification completed, signatureId: {}, verified: {}", 
                request.getSignatureId(), verified);

            VerificationResponse response = new VerificationResponse();
            response.setSignatureId(request.getSignatureId());
            response.setVerified(verified);
            response.setSuccess(true);
            response.setMessage(verified ? "验签成功" : "验签失败");
            response.setVerificationCount(record.getVerificationCount());

            return response;
        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            VerificationResponse response = new VerificationResponse();
            response.setSignatureId(request.getSignatureId());
            response.setVerified(false);
            response.setSuccess(false);
            response.setMessage("验签失败: " + e.getMessage());
            return response;
        }
    }

    public SignatureRecord getSignatureRecord(String signatureId) {
        SignatureRecord cachedRecord = cacheService.getSignatureFromCache(signatureId);
        if (cachedRecord != null) {
            return cachedRecord;
        }

        Optional<SignatureRecord> optionalRecord = signatureRecordRepository.findBySignatureId(signatureId);
        if (optionalRecord.isPresent()) {
            SignatureRecord record = optionalRecord.get();
            cacheService.cacheSignature(signatureId, record);
            return record;
        }

        return null;
    }
}
