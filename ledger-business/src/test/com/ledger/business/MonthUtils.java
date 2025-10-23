package com.ledger.business;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class MonthUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 按月份累加
     * @param ymStr  原始年月，格式：yyyy-MM
     * @param months 要累加的月数（可为负）
     * @return       累加后的年月字符串，格式：yyyy-MM
     */
    public static String addMonths(String ymStr, int months) {
        YearMonth ym = YearMonth.parse(ymStr, FORMATTER);
        YearMonth result = ym.plusMonths(months);
        return result.format(FORMATTER);
    }

    // 简单测试
    public static void main(String[] args) {
        System.out.println(addMonths("2027-12", 6)); // 输出：2026-06
    }
}