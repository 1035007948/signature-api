package com.example.signature.service;

import com.example.signature.entity.SignatureRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final String SIGNATURE_CACHE_PREFIX = "signature:";
    private static final long DEFAULT_EXPIRE_SECONDS = 3600;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void cacheSignature(String signatureId, SignatureRecord record) {
        String key = SIGNATURE_CACHE_PREFIX + signatureId;
        long expireSeconds = DEFAULT_EXPIRE_SECONDS;
        if (record.getExpireTime() != null) {
            long seconds = java.time.Duration.between(
                java.time.LocalDateTime.now(), 
                record.getExpireTime()
            ).getSeconds();
            if (seconds > 0) {
                expireSeconds = seconds;
            }
        }
        redisTemplate.opsForValue().set(key, record, expireSeconds, TimeUnit.SECONDS);
    }

    public SignatureRecord getSignatureFromCache(String signatureId) {
        String key = SIGNATURE_CACHE_PREFIX + signatureId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof SignatureRecord) {
            return (SignatureRecord) value;
        }
        return null;
    }

    public void updateSignatureInCache(String signatureId, SignatureRecord record) {
        String key = SIGNATURE_CACHE_PREFIX + signatureId;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            redisTemplate.opsForValue().set(key, record, ttl, TimeUnit.SECONDS);
        } else {
            cacheSignature(signatureId, record);
        }
    }

    public void deleteSignatureFromCache(String signatureId) {
        String key = SIGNATURE_CACHE_PREFIX + signatureId;
        redisTemplate.delete(key);
    }

    public boolean existsInCache(String signatureId) {
        String key = SIGNATURE_CACHE_PREFIX + signatureId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
