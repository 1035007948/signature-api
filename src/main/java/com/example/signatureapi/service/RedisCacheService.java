package com.example.signatureapi.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private static final String SIGNATURE_PREFIX = "signature:";
    private static final long DEFAULT_EXPIRE_TIME = 24;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void saveSignature(String signatureValue, Object signatureData) {
        String key = SIGNATURE_PREFIX + signatureValue;
        redisTemplate.opsForValue().set(key, signatureData, DEFAULT_EXPIRE_TIME, TimeUnit.HOURS);
    }

    public void saveSignatureWithExpire(String signatureValue, Object signatureData, long timeout, TimeUnit unit) {
        String key = SIGNATURE_PREFIX + signatureValue;
        redisTemplate.opsForValue().set(key, signatureData, timeout, unit);
    }

    public Object getSignature(String signatureValue) {
        String key = SIGNATURE_PREFIX + signatureValue;
        return redisTemplate.opsForValue().get(key);
    }

    public boolean existsSignature(String signatureValue) {
        String key = SIGNATURE_PREFIX + signatureValue;
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    public void deleteSignature(String signatureValue) {
        String key = SIGNATURE_PREFIX + signatureValue;
        redisTemplate.delete(key);
    }

    public boolean expireSignature(String signatureValue, long timeout, TimeUnit unit) {
        String key = SIGNATURE_PREFIX + signatureValue;
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return result != null && result;
    }
}
