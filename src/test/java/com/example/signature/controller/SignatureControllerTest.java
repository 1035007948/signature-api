package com.example.signature.controller;

import com.example.signature.dto.SignRequest;
import com.example.signature.dto.SignResponse;
import com.example.signature.dto.VerifyRequest;
import com.example.signature.dto.VerifyResponse;
import com.example.signature.service.SignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignatureController.class)
class SignatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignatureService signatureService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSign() throws Exception {
        SignRequest request = SignRequest.builder()
                .appId("APP001")
                .timestamp("1704067200000")
                .nonce("abc123")
                .data("{\"orderNo\":\"202401010001\"}")
                .build();

        SignResponse response = SignResponse.builder()
                .success(true)
                .message("签名成功")
                .signature("A1B2C3D4E5F6")
                .signType("SHA256")
                .timestamp("1704067200000")
                .build();

        when(signatureService.sign(any(SignRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/signature/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("签名成功"))
                .andExpect(jsonPath("$.signature").value("A1B2C3D4E5F6"));
    }

    @Test
    void testVerify() throws Exception {
        VerifyRequest request = VerifyRequest.builder()
                .appId("APP001")
                .signature("A1B2C3D4E5F6")
                .build();

        VerifyResponse response = VerifyResponse.builder()
                .success(true)
                .valid(true)
                .message("验签成功")
                .build();

        when(signatureService.verify(any(VerifyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/signature/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("验签成功"));
    }

}
