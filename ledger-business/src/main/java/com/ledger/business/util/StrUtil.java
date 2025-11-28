package com.ledger.business.util;

import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;

import java.util.regex.*;

public class StrUtil {

    /** 唯一/主键冲突提取 */
    private static final Pattern UK_PATTERN = Pattern.compile(
            "Duplicate entry\\s+'.+?'\\s+for key\\s+'[^']+'", Pattern.CASE_INSENSITIVE);

    public static String extractCoreMessage(String raw) {
        if (raw == null) return "";
        Matcher m = UK_PATTERN.matcher(raw);
        return m.find() ? m.group() : "";
    }

    public static String buildRemark(CtgLedgerProjectExpenseDetail expenseDetail) {
        if (expenseDetail == null) {
            return "";
        }

        Long expenseReportNumber = expenseDetail.getExpenseReportNumber();
        String feeType = expenseDetail.getFeeType();
        java.math.BigDecimal amount = expenseDetail.getAmount();

        String amountStr = "";
        if (amount != null) {
            amountStr = amount.stripTrailingZeros().toPlainString();
        }

        return String.format("单号%s，发生%s金额%s万元",
                expenseReportNumber != null ? expenseReportNumber : "",
                feeType != null ? feeType : "",
                amountStr);
    }
}