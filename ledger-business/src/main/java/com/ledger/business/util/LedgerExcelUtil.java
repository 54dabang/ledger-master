package com.ledger.business.util;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class LedgerExcelUtil {
    public static Sheet pickSheet(Workbook workbook) {
        // 1. 先精确查找包含“预算”的 sheet
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet != null && sheet.getSheetName().contains(InitConstant.SHEET_NAME_ANNUAL_DETAIL_FLAG)) {
                return sheet;
            }
        }
        // 2. 找不到含“预算”的，返回第 1 个
        return workbook.getSheetAt(0);
    }
}
