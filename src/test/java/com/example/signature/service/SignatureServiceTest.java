package com.example.signature.service;

import com.example.signature.dto.SignRequest;
import com.example.signature.dto.SignResponse;
import com.example.signature.dto.VerifyRequest;
import com.example.signature.dto.VerifyResponse;
import com.example.signature.entity.SignatureRecord;
import com.example.signature.repository.SignatureRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureServiceTest {

    @Mock
    private SignatureRecordRepository signatureRecordRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SignatureService signatureService;

    @Test
    void testSign() {
        SignRequest request = SignRequest.builder()
                .appId("APP001")
                .timestamp("1704067200000")
                .nonce("abc123")
                .data("{\"orderNo\":\"202401010001\"}")
                .build();

        SignatureRecord savedRecord = SignatureRecord.builder()
                .id(1L)
                .appId("APP001")
                .timestamp("1704067200000")
                .nonce("abc123")
                .build();

        when(signatureRecordRepository.save(any(SignatureRecord.class))).thenReturn(savedRecord);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        SignResponse response = signatureService.sign(request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("签名成功", response.getMessage());
        assertNotNull(response.getSignature());
        assertEquals("SHA256", response.getSignType());

        verify(signatureRecordRepository, times(1)).save(any(SignatureRecord.class));
        verify(valueOperations, times(1)).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testVerifyFromCache() {
        VerifyRequest request = VerifyRequest.builder()
                .appId("APP001")
                .signature("test-signature")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("test-signature");

        VerifyResponse response = signatureService.verify(request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertTrue(response.getValid());
        assertEquals("验签成功（来自缓存）", response.getMessage());
    }

    @Test
    void testVerifyFromDatabase() {
        VerifyRequest request = VerifyRequest.builder()
                .appId("APP001")
                .signature("test-signature")
                .build();

        SignatureRecord record = SignatureRecord.builder()
                .id(1L)
                .appId("APP001")
                .timestamp("1704067200000")
                .nonce("abc123")
                .dataContent("{\"orderNo\":\"202401010001\"}")
                .signature("test-signature")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(signatureRecordRepository.findBySignature(anyString())).thenReturn(Optional.of(record));

        VerifyResponse response = signatureService.verify(request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertNotNull(response.getValid());
    }

    @Test
    void testVerifyNotFound() {
        VerifyRequest request = VerifyRequest.builder()
                .appId("APP001")
                .signature("non-existent-signature")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(signatureRecordRepository.findBySignature(anyString())).thenReturn(Optional.empty());

        VerifyResponse response = signatureService.verify(request);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertFalse(response.getValid());
        assertEquals("验签失败，未找到签名记录", response.getMessage());
    }

}
