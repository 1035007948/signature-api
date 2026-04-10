package com.example.signature.service;

import com.example.signature.dto.SignatureRequest;
import com.example.signature.dto.SignatureResponse;
import com.example.signature.dto.VerificationRequest;
import com.example.signature.dto.VerificationResponse;
import com.example.signature.entity.SignatureRecord;
import com.example.signature.repository.SignatureRecordRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SignatureServiceTest {

    @Mock
    private SignatureRecordRepository signatureRecordRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private SignatureService signatureService;

    private SignatureRequest signatureRequest;
    private SignatureRecord signatureRecord;

    @Before
    public void setUp() {
        signatureRequest = new SignatureRequest();
        signatureRequest.setData("test data");
        signatureRequest.setAlgorithm("SHA256");
        signatureRequest.setExpireSeconds(3600L);

        signatureRecord = new SignatureRecord();
        signatureRecord.setSignatureId("test-signature-id");
        signatureRecord.setOriginalData("test data");
        signatureRecord.setSignatureResult("test-signature-result");
        signatureRecord.setAlgorithm("SHA256");
    }

    @Test
    public void testCreateSignature() {
        when(signatureRecordRepository.save(any(SignatureRecord.class))).thenReturn(signatureRecord);

        SignatureResponse response = signatureService.createSignature(signatureRequest);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertNotNull(response.getSignatureId());
        assertNotNull(response.getSignature());
        assertEquals("SHA256", response.getAlgorithm());

        verify(signatureRecordRepository, times(1)).save(any(SignatureRecord.class));
        verify(cacheService, times(1)).cacheSignature(anyString(), any(SignatureRecord.class));
    }

    @Test
    public void testVerifySignature_Success() {
        when(cacheService.getSignatureFromCache(anyString())).thenReturn(signatureRecord);

        VerificationRequest request = new VerificationRequest();
        request.setSignatureId("test-signature-id");
        request.setData("test data");
        request.setSignature("test-signature");

        VerificationResponse response = signatureService.verifySignature(request);

        assertNotNull(response);
        assertEquals("test-signature-id", response.getSignatureId());
        assertTrue(response.getSuccess());

        verify(signatureRecordRepository, times(1)).save(any(SignatureRecord.class));
        verify(cacheService, times(1)).updateSignatureInCache(anyString(), any(SignatureRecord.class));
    }

    @Test
    public void testVerifySignature_NotFound() {
        when(cacheService.getSignatureFromCache(anyString())).thenReturn(null);
        when(signatureRecordRepository.findBySignatureId(anyString())).thenReturn(Optional.empty());

        VerificationRequest request = new VerificationRequest();
        request.setSignatureId("non-existent-id");
        request.setData("test data");
        request.setSignature("test-signature");

        VerificationResponse response = signatureService.verifySignature(request);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("签名记录不存在", response.getMessage());
    }

    @Test
    public void testGetSignatureRecord_FromCache() {
        when(cacheService.getSignatureFromCache(anyString())).thenReturn(signatureRecord);

        SignatureRecord result = signatureService.getSignatureRecord("test-signature-id");

        assertNotNull(result);
        assertEquals("test-signature-id", result.getSignatureId());
        verify(cacheService, times(1)).getSignatureFromCache(anyString());
        verify(signatureRecordRepository, times(0)).findBySignatureId(anyString());
    }

    @Test
    public void testGetSignatureRecord_FromDatabase() {
        when(cacheService.getSignatureFromCache(anyString())).thenReturn(null);
        when(signatureRecordRepository.findBySignatureId(anyString())).thenReturn(Optional.of(signatureRecord));

        SignatureRecord result = signatureService.getSignatureRecord("test-signature-id");

        assertNotNull(result);
        assertEquals("test-signature-id", result.getSignatureId());
        verify(cacheService, times(1)).getSignatureFromCache(anyString());
        verify(signatureRecordRepository, times(1)).findBySignatureId(anyString());
        verify(cacheService, times(1)).cacheSignature(anyString(), any(SignatureRecord.class));
    }
}
