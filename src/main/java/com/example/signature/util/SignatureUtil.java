/**
 * 签名工具类
 *
 * 提供签名生成和验证的静态方法
 * 使用 SHA256 算法进行签名计算
 *
 * 签名规则：
 * 1. 将所有参数按参数名 ASCII 码从小到大排序
 * 2. 拼接成 key=value&key2=value2 格式
 * 3. 末尾追加 &key=密钥
 * 4. 进行 SHA256 哈希运算
 * 5. 结果转换为大写十六进制字符串
 */
package com.example.signature.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数字签名工具类
 * 提供签名生成和验证功能
 */
public class SignatureUtil {

    /**
     * 默认密钥
     * 生产环境应从配置文件或环境变量读取
     */
    private static final String DEFAULT_SECRET_KEY = "your-secret-key";

    /**
     * 生成数字签名
     *
     * @param params   待签名的参数集合（key-value 形式）
     * @param secretKey 密钥
     * @return 大写的 SHA256 签名字符串
     *
     * 签名步骤：
     * 1. 过滤空值参数
     * 2. 按参数名排序
     * 3. 拼接成字符串
     * 4. 追加密钥
     * 5. SHA256 哈希
     */
    public static String generateSignature(Map<String, String> params, String secretKey) {
        // 过滤空值参数，按参数名排序，拼接成 key=value 格式
        String sortedParams = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // 在末尾追加密钥
        String stringToSign = sortedParams + "&key=" + secretKey;

        // 使用 SHA256 算法生成签名并转为大写
        return DigestUtils.sha256Hex(stringToSign).toUpperCase();
    }

    /**
     * 使用默认密钥生成签名
     *
     * @param params 待签名的参数集合
     * @return 大写的 SHA256 签名字符串
     */
    public static String generateSignature(Map<String, String> params) {
        return generateSignature(params, DEFAULT_SECRET_KEY);
    }

    /**
     * 验证签名是否有效
     *
     * @param params    参数集合
     * @param signature 待验证的签名
     * @param secretKey 密钥
     * @return true - 签名有效，false - 签名无效
     *
     * 验证逻辑：
     * 1. 使用相同参数和密钥重新生成签名
     * 2. 比较生成的签名与传入的签名（忽略大小写）
     */
    public static boolean verifySignature(Map<String, String> params, String signature, String secretKey) {
        String generatedSignature = generateSignature(params, secretKey);
        return generatedSignature.equalsIgnoreCase(signature);
    }

    /**
     * 使用默认密钥验证签名
     *
     * @param params    参数集合
     * @param signature 待验证的签名
     * @return true - 签名有效，false - 签名无效
     */
    public static boolean verifySignature(Map<String, String> params, String signature) {
        return verifySignature(params, signature, DEFAULT_SECRET_KEY);
    }

}
