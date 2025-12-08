package com.ledger.business.util;

import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;

import java.math.BigDecimal;
import java.util.regex.*;

public class StrUtil {

    /**
     * 唯一/主键冲突提取
     */
    private static final Pattern UK_PATTERN = Pattern.compile(
            "Duplicate entry\\s+'.+?'\\s+for key\\s+'[^']+'", Pattern.CASE_INSENSITIVE);

    public static String extractCoreMessage(String raw) {
        if (raw == null) return "";
        Matcher m = UK_PATTERN.matcher(raw);
        return m.find() ? m.group() : "";
    }

    public static String buildRemark(CtgLedgerProjectExpenseDetail expenseDetail) {
        return buildRemarkInYuan(expenseDetail, false);
    }

    public static String buildRemarkInYuan(CtgLedgerProjectExpenseDetail expenseDetail, boolean inYuan) {
        if (expenseDetail == null) {
            return "";
        }

        Long expenseReportNumber = expenseDetail.getExpenseReportNumber();
        String feeType = expenseDetail.getFeeType();
        java.math.BigDecimal amount = expenseDetail.getAmount();
        return buildRemark(expenseReportNumber, feeType, amount, inYuan);
    }

    public static String buildRemark(Long expenseReportNumber, String feeType, BigDecimal amount, boolean inYuan) {
        String amountStr = "";
        if (amount != null) {
            // 根据 inYuan 决定单位转换
            if (inYuan) {
                amount = amount.multiply(BigDecimal.valueOf(10000));
            }
            amountStr = amount.stripTrailingZeros().toPlainString();
        }

        // 根据 inYuan 决定单位文字
        String unit = inYuan ? "元" : "万元";

        return String.format("单号%s，发生%s金额%s%s",
                expenseReportNumber != null ? expenseReportNumber : "",
                feeType != null ? feeType : "",
                amountStr,
                unit);
    }


}