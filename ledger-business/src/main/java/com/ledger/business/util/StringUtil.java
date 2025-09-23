package com.ledger.business.util;

public class StringUtil {

    //判断是否以英文字母开头
    public static boolean startWithEnglish(String str) {
        // 检查字符串是否为空或长度为0
        if (str == null || str.length() == 0) {
            return false;
        }

        // 获取第一个字符
        char firstChar = str.charAt(0);

        // 判断第一个字符是否为英文字母（大小写均可）
        return (firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z');
    }
}
