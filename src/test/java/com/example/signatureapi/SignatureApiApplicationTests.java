package com.example.signatureapi;

import com.example.signatureapi.dto.SignRequest;
import com.example.signatureapi.dto.VerifyRequest;
import com.example.signatureapi.service.SignatureService;
import com.example.signatureapi.util.SignatureUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SignatureApiApplicationTests {

    @Resource
    private SignatureService signatureService;

    @Test
    void contextLoads() {
    }

    @Test
    void testSignatureUtil() {
        String content = "test content";
        String signature = SignatureUtil.sign(content, "HMAC-SHA256");
        assertNotNull(signature);
        System.out.println("签名结果: " + signature);

        boolean valid = SignatureUtil.verify(content, signature, "HMAC-SHA256");
        assertTrue(valid);

        boolean invalid = SignatureUtil.verify(content + "tampered", signature, "HMAC-SHA256");
        assertFalse(invalid);
    }

    @Test
    void testSignatureWithParams() {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", "123456");
        params.put("amount", "100.00");
        params.put("userId", "user001");

        String signature = SignatureUtil.sign(params, "HMAC-SHA256");
        assertNotNull(signature);
        System.out.println("参数签名结果: " + signature);

        boolean valid = SignatureUtil.verify(params, signature, "HMAC-SHA256");
        assertTrue(valid);
    }

    @Test
    void testSignatureService() {
        SignRequest request = new SignRequest();
        request.setSigner("test-client");
        request.setContent("orderId=123456&amount=100.00");
        request.setAlgorithm("HMAC-SHA256");

        Map<String, Object> signResult = signatureService.sign(request);
        assertNotNull(signResult);
        String signature = (String) signResult.get("signature");
        String requestId = (String) signResult.get("requestId");
        assertNotNull(signature);
        assertNotNull(requestId);
        System.out.println("服务签名结果: " + signature);
        System.out.println("请求ID: " + requestId);

        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setSignature(signature);
        verifyRequest.setContent("orderId=123456&amount=100.00");
        verifyRequest.setAlgorithm("HMAC-SHA256");

        Map<String, Object> verifyResult = signatureService.verify(verifyRequest);
        assertNotNull(verifyResult);
        Boolean valid = (Boolean) verifyResult.get("valid");
        assertTrue(valid);
        System.out.println("验签结果: " + valid);
    }

    @Test
    void testAlgorithmCompatibility() {
        String content = "test algorithm compatibility";

        String sha256Signature = SignatureUtil.sign(content, "SHA-256");
        assertNotNull(sha256Signature);
        boolean sha256Valid = SignatureUtil.verify(content, sha256Signature, "SHA-256");
        assertTrue(sha256Valid);

        String hmacSignature = SignatureUtil.sign(content, "HMAC-SHA256");
        assertNotNull(hmacSignature);
        boolean hmacValid = SignatureUtil.verify(content, hmacSignature, "HMAC-SHA256");
        assertTrue(hmacValid);

        assertNotEquals(sha256Signature, hmacSignature);
    }

    @Test
    void testGenerateRequestId() {
        String requestId1 = SignatureUtil.generateRequestId();
        String requestId2 = SignatureUtil.generateRequestId();
        assertNotNull(requestId1);
        assertNotNull(requestId2);
        assertNotEquals(requestId1, requestId2);
        assertEquals(32, requestId1.length());
    }
}
