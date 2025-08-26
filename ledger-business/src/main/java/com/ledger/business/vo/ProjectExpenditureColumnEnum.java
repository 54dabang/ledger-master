package com.ledger.business.vo;

import java.util.*;

public enum ProjectExpenditureColumnEnum {

    TOTAL_BUDGET("totalBudget", "项目总经费"),
    EXECUTED_AMOUNT("executedAmount", "已执行金额"),
    ANNUAL_BUDGET("annualBudget", "年度预算经费"),
    LAST_REMAINING_FUNDS("lastRemainingFunds", "上次剩余经费"),
    CURRENT_FUNDS("currentFunds", "本次支出经费"),
    CUMULATIVE_FUNDS("cumulativeFunds", "累计支出经费"),
    CURRENT_REMAINING_FUNDS("currentRemainingFunds", "本次剩余经费");

    private String engName;
    private String cnName;

    ProjectExpenditureColumnEnum(String engName, String cnName) {
        this.engName = engName;
        this.cnName = cnName;
    }

    public String engName() {
        return engName;
    }

    public String cnName() {
        return cnName;
    }

    /* ---------- 静态辅助 ---------- */

    /**
     * engName -> 枚举映射
     */
    public static final Map<String, ProjectExpenditureColumnEnum> ENG_ENUM_MAP = new HashMap<>();
    /**
     * cnName -> 枚举映射
     */
    public static final Map<String, ProjectExpenditureColumnEnum> CN_ENUM_MAP = new HashMap<>();
    /**
     * 仅中文描述集合，保持原始顺序
     */
    public static final List<String> CN_NAME_LIST = new ArrayList<>();

    static {
        for (ProjectExpenditureColumnEnum e : values()) {
            ENG_ENUM_MAP.put(e.engName, e);
            CN_ENUM_MAP.put(e.cnName, e);
            CN_NAME_LIST.add(e.cnName);
        }
    }

    /**
     * 根据英文标识获取枚举
     */
    public static ProjectExpenditureColumnEnum getByEngName(String engName) {
        return ENG_ENUM_MAP.get(engName);
    }

    /**
     * 根据中文描述获取枚举
     */
    public static ProjectExpenditureColumnEnum getByCnName(String cnName) {
        return CN_ENUM_MAP.get(cnName);
    }

    /**
     * 获取所有中文描述列表
     */
    public static List<String> getCnNameList() {
        return CN_NAME_LIST;
    }

    /**
     * 获取所有英文标识列表
     */
    public static List<String> getEngNameList() {
        List<String> list = new ArrayList<>();
        for (ProjectExpenditureColumnEnum e : values()) {
            list.add(e.engName());
        }
        return list;
    }
}
