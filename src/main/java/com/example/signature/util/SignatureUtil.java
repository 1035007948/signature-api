package com.example.signature.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class SignatureUtil {

    public static String generateSignatureId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String signWithSHA256(String data, String secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combinedData = data + secretKey;
            byte[] hash = digest.digest(combinedData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String signWithMD5(String data, String secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            String combinedData = data + secretKey;
            byte[] hash = digest.digest(combinedData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static String signWithSHA1(String data, String secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            String combinedData = data + secretKey;
            byte[] hash = digest.digest(combinedData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }

    public static String sign(String data, String algorithm, String secretKey) {
        switch (algorithm.toUpperCase()) {
            case "SHA256":
            case "SHA-256":
                return signWithSHA256(data, secretKey);
            case "MD5":
                return signWithMD5(data, secretKey);
            case "SHA1":
            case "SHA-1":
                return signWithSHA1(data, secretKey);
            default:
                return signWithSHA256(data, secretKey);
        }
    }

    public static boolean verify(String data, String signature, String algorithm, String secretKey) {
        String computedSignature = sign(data, algorithm, secretKey);
        return computedSignature.equals(signature);
    }
}
