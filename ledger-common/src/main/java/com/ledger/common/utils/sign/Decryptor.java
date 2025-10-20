package com.ledger.common.utils.sign;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
public class Decryptor {
    public static String decrypt(String encryptedText, String passphrase) throws Exception {
// 1. Base64解码并提取盐值（8字节）
        byte[] cipherBytes = Base64.getDecoder().decode(encryptedText);
        byte[] salt = new byte[8];
        System.arraycopy(cipherBytes, 8, salt, 0, 8); // 跳过"Salted__"头
// 2. 生成密钥和IV（EVP_BytesToKey逻辑）
        byte[][] keyIv = generateKeyAndIV(passphrase, salt, 32, 16);
        byte[] keyBytes = keyIv[0];
        byte[] ivBytes = keyIv[1];
// 3. 解密（AES/CBC/PKCS5Padding）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(keyBytes, "AES"),
                new IvParameterSpec(ivBytes));
// 4. 处理密文（跳过前16字节：8字节头+8字节盐）
        byte[] decrypted = cipher.doFinal(cipherBytes, 16, cipherBytes.length - 16);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    private static byte[][] generateKeyAndIV(String passphrase, byte[] salt, int keySize, int ivSize) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] key = new byte[keySize + ivSize];
        byte[] hash = new byte[0];
// 多次MD5哈希直到生成足够长度的密钥材料
        for (int i = 0, offset = 0; offset < key.length; i++) {
            md5.update(hash);
            md5.update(passphrase.getBytes(StandardCharsets.UTF_8));
            md5.update(salt);
            hash = md5.digest();
            md5.reset();
            int copyLen = Math.min(hash.length, key.length - offset);
            System.arraycopy(hash, 0, key, offset, copyLen);
            offset += copyLen;
        }
        return new byte[][]{
                Arrays.copyOfRange(key, 0, keySize), // 密钥
                Arrays.copyOfRange(key, keySize, keySize + ivSize) // IV
        };
    }

    /**
     * 与 crypto-js 完全对齐的 AES 加密
     * @param plainText  明文（任意字符串，可以是 JSON）
     * @param passphrase 前端传过来的口令
     * @return Base64 串，与 crypto-js.AES.encrypt(data, passphrase).toString() 完全一致
     */
    public static String encrypt(String plainText, String passphrase) throws Exception {
        // 1. 生成 8 字节随机盐
        byte[] salt = new byte[8];
        new SecureRandom().nextBytes(salt);

        // 2. EVP_BytesToKey 派生 32 字节密钥 + 16 字节 IV
        byte[][] keyIv = generateKeyAndIV(passphrase, salt, 32, 16);
        SecretKeySpec key = new SecretKeySpec(keyIv[0], "AES");
        IvParameterSpec iv  = new IvParameterSpec(keyIv[1]);

        // 3. AES/CBC/PKCS5Padding 加密
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 4. 组装： "Salted__" + salt + ciphertext
        byte[] prefix = "Salted__".getBytes(StandardCharsets.US_ASCII);
        byte[] cipherBytes = new byte[prefix.length + salt.length + encrypted.length];
        System.arraycopy(prefix, 0, cipherBytes, 0, prefix.length);
        System.arraycopy(salt,  0, cipherBytes, prefix.length, salt.length);
        System.arraycopy(encrypted, 0, cipherBytes, prefix.length + salt.length, encrypted.length);

        // 5. Base64 输出
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    /* 与 Decryptor 中完全一致的 EVP_BytesToKey 实现 */
    /* ------ 简单自测 ------ */
    public static void main(String[] args) throws Exception {
        String json = "liu_jinling";
        String pwd = "Ledger^Y&U*I2026";

        String ct = encrypt(json, pwd);
        System.out.println("Java 加密结果（直接复制给前端可解密）:");
        System.out.println(ct);

        // 用已有的 Decryptor 解密验证
        String pt = Decryptor.decrypt(ct, pwd);
        System.out.println("Java 解密结果:");
        System.out.println(pt);
    }
}
