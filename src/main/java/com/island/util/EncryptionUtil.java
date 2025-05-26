package com.island.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {
    // 私有构造函数防止实例化
    private EncryptionUtil() {
        throw new AssertionError("工具类禁止实例化");
    }

    // AES 配置
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "ThisIsASecretKey"; // 实际项目中应从安全配置读取
    private static final int IV_LENGTH = 16; // AES 块大小

    //-------------------------
    // 加密方法
    //-------------------------
    public static String encrypt(String plainText) {
        try {
            // 1. 生成随机初始化向量 (IV)
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 2. 创建密钥和加密器
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 3. 执行加密并拼接 IV + 密文
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // 4. 返回 Base64 编码结果
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    //-------------------------
    // 解密方法
    //-------------------------
    public static String decrypt(String encryptedText) {
        try {
            // 1. 解码 Base64 并分离 IV 和密文
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            // 2. 创建密钥和解密器
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 3. 执行解密
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    //-------------------------
    // 使用示例
    //-------------------------
    public static void main(String[] args) {
        String original = "Hello, 这是一条敏感数据!";

        // 加密
        String encrypted = encrypt(original);
        System.out.println("加密结果: " + encrypted);

        // 解密
        String decrypted = decrypt(encrypted);
        System.out.println("解密结果: " + decrypted);

        System.out.println("验证结果: " + original.equals(decrypted)); // 应输出 true
    }
}