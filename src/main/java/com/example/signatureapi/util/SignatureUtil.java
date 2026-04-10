package com.example.signatureapi.util;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 数字签名工具类
 * 提供标准的签名/验签算法实现
 * 支持HMAC-SHA256和SHA-256两种主流算法
 *
 * 签名算法流程：
 * 1. 参数按字典序升序排列
 * 2. 按key=value&格式拼接
 * 3. 末尾追加密钥
 * 4. 使用指定算法生成摘要
 * 5. 转为十六进制字符串输出
 *
 * @author Signature API Team
 * @version 1.0.0
 * @since 2026-04-10
 */
public class SignatureUtil {

    private static final Logger logger = LoggerFactory.getLogger(SignatureUtil.class);

    /**
     * HMAC-SHA256算法标识
     */
    private static final String HMAC_SHA256 = "HmacSHA256";
    /**
     * SHA-256算法标识
     */
    private static final String SHA256 = "SHA-256";
    /**
     * 默认签名密钥（生产环境建议配置化）
     */
    private static final String DEFAULT_SECRET_KEY = "signature-api-secret-key-2024";

    /**
     * 私有构造方法，禁止实例化
     */
    private SignatureUtil() {
    }

    /**
     * 生成32位唯一请求ID
     * 使用UUID去除横杠，保证全局唯一性
     *
     * @return 32位十六进制字符串
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Map参数签名（使用默认密钥）
     *
     * @param params 待签名参数Map
     * @param algorithm 签名算法：HMAC-SHA256 / SHA-256
     * @return 64位十六进制签名值
     */
    public static String sign(Map<String, String> params, String algorithm) {
        return sign(params, DEFAULT_SECRET_KEY, algorithm);
    }

    /**
     * Map参数签名（指定密钥）
     * 1. 按键名升序排序参数
     * 2. 按key=value&格式拼接
     * 3. 末尾追加密钥后生成签名
     *
     * @param params 待签名参数Map
     * @param secretKey 签名密钥
     * @param algorithm 签名算法
     * @return 64位十六进制签名值
     */
    public static String sign(Map<String, String> params, String secretKey, String algorithm) {
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(secretKey);

        String content = sb.toString();

        if ("HMAC-SHA256".equalsIgnoreCase(algorithm) || HMAC_SHA256.equalsIgnoreCase(algorithm)) {
            return hmacSha256(content, secretKey);
        } else {
            return sha256(content);
        }
    }

    /**
     * 字符串内容签名（使用默认密钥）
     *
     * @param content 待签名字符串内容
     * @param algorithm 签名算法
     * @return 64位十六进制签名值
     */
    public static String sign(String content, String algorithm) {
        return sign(content, DEFAULT_SECRET_KEY, algorithm);
    }

    /**
     * 字符串内容签名（指定密钥）
     *
     * @param content 待签名字符串内容
     * @param secretKey 签名密钥
     * @param algorithm 签名算法
     * @return 64位十六进制签名值
     */
    public static String sign(String content, String secretKey, String algorithm) {
        String signContent = content + secretKey;
        if ("HMAC-SHA256".equalsIgnoreCase(algorithm) || HMAC_SHA256.equalsIgnoreCase(algorithm)) {
            return hmacSha256(content, secretKey);
        } else {
            return sha256(signContent);
        }
    }

    /**
     * 字符串内容验签（使用默认密钥）
     *
     * @param content 原始字符串内容
     * @param signature 待验证的签名值
     * @param algorithm 签名算法
     * @return true=签名有效，false=签名无效
     */
    public static boolean verify(String content, String signature, String algorithm) {
        return verify(content, signature, DEFAULT_SECRET_KEY, algorithm);
    }

    /**
     * 字符串内容验签（指定密钥）
     * 使用相同算法重新签名后，忽略大小写进行比对
     *
     * @param content 原始字符串内容
     * @param signature 待验证的签名值
     * @param secretKey 签名密钥
     * @param algorithm 签名算法
     * @return true=签名有效，false=签名无效
     */
    public static boolean verify(String content, String signature, String secretKey, String algorithm) {
        String calculatedSignature = sign(content, secretKey, algorithm);
        return calculatedSignature.equalsIgnoreCase(signature);
    }

    /**
     * Map参数验签（使用默认密钥）
     *
     * @param params 原始参数Map
     * @param signature 待验证的签名值
     * @param algorithm 签名算法
     * @return true=签名有效，false=签名无效
     */
    public static boolean verify(Map<String, String> params, String signature, String algorithm) {
        return verify(params, signature, DEFAULT_SECRET_KEY, algorithm);
    }

    /**
     * Map参数验签（指定密钥）
     *
     * @param params 原始参数Map
     * @param signature 待验证的签名值
     * @param secretKey 签名密钥
     * @param algorithm 签名算法
     * @return true=签名有效，false=签名无效
     */
    public static boolean verify(Map<String, String> params, String signature, String secretKey, String algorithm) {
        String calculatedSignature = sign(params, secretKey, algorithm);
        return calculatedSignature.equalsIgnoreCase(signature);
    }

    /**
     * HMAC-SHA256算法实现
     * 基于密钥的消息认证码算法，防篡改能力强
     *
     * @param content 待签名内容
     * @param secretKey 密钥
     * @return 64位十六进制字符串
     */
    private static String hmacSha256(String content, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (Exception e) {
            logger.error("HMAC-SHA256签名失败", e);
            throw new RuntimeException("签名失败", e);
        }
    }

    /**
     * SHA-256算法实现
     * 标准安全哈希算法，生成256位(32字节)消息摘要
     *
     * @param content 待签名内容
     * @return 64位十六进制字符串
     */
    private static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (Exception e) {
            logger.error("SHA-256签名失败", e);
            throw new RuntimeException("签名失败", e);
        }
    }

    /**
     * Base64编码
     *
     * @param content 原始字符串
     * @return Base64编码字符串
     */
    public static String base64Encode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     *
     * @param encoded Base64编码字符串
     * @return 原始字符串
     */
    public static String base64Decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
