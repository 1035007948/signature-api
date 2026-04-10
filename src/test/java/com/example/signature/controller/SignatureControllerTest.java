package com.example.signature.controller;

import com.example.signature.dto.SignatureRequest;
import com.example.signature.dto.SignatureResponse;
import com.example.signature.dto.VerificationRequest;
import com.example.signature.dto.VerificationResponse;
import com.example.signature.service.SignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class SignatureControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private SignatureController signatureController;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(signatureController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateSignature() throws Exception {
        SignatureRequest request = new SignatureRequest();
        request.setData("test data");
        request.setAlgorithm("SHA256");

        SignatureResponse response = new SignatureResponse();
        response.setSignatureId("test-id");
        response.setSignature("test-signature");
        response.setSuccess(true);

        when(signatureService.createSignature(any(SignatureRequest.class))).thenReturn(response);

        mockMvc.perform(post("/signature/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signatureId").value("test-id"))
                .andExpect(jsonPath("$.signature").value("test-signature"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testVerifySignature() throws Exception {
        VerificationRequest request = new VerificationRequest();
        request.setSignatureId("test-id");
        request.setData("test data");
        request.setSignature("test-signature");

        VerificationResponse response = new VerificationResponse();
        response.setSignatureId("test-id");
        response.setVerified(true);
        response.setSuccess(true);

        when(signatureService.verifySignature(any(VerificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/signature/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signatureId").value("test-id"))
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testHealth() throws Exception {
        mockMvc.perform(get("/signature/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is running"));
    }
}
