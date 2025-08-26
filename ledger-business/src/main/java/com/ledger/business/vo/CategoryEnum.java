package com.ledger.business.vo;

import java.util.*;

public enum CategoryEnum {

    EQUIP_PURCHASE_FEE("equipPurchaseFee", "购置设备费"),
    PROTO_EQUIP_FEE("protoEquipFee", "试制设备费"),
    EQUIP_RENOV_FEE("equipRenovFee", "设备改造费"),
    EQUIP_RENT_FEE("equipRentFee", "设备租赁费"),
    MATERIAL_COST("materialCost", "材料费"),
    TEST_PROC_FEE("testProcFee", "测试化验加工费"),
    FUEL_POWER_COST("fuelPowerCost", "燃料动力费"),
    PUB_DOC_IP_FEE("pubDocIpFee", "出版/文献/信息传播/知识产权事务费"),
    TRAVEL_CONF_COOP_FEE("travelConfCoopFee", "差旅/会议/国际合作交流费"),
    LABOR_COST("laborCost", "人工费"),
    SERVICE_COST("serviceCost", "劳务费"),
    EXPERT_CONSULT_FEE("expertConsultFee", "专家咨询费"),
    MGMT_FEE("mgmtFee", "管理费"),
    TAX_FEE("taxFee", "税金"),
    CONTRACT_AMOUNT("contractAmount", "合同金额");

    private String engName;
    private String cnName;

    CategoryEnum(String engName, String cnName) {
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
    public static final Map<String, CategoryEnum> ENG_ENUM_MAP = new HashMap<>();
    /**
     * cnName -> 枚举映射
     */
    public static final Map<String, CategoryEnum> CN_ENUM_MAP = new HashMap<>();
    /**
     * 仅中文描述集合，保持原始顺序
     */
    public static final List<String> CN_NAME_LIST = new ArrayList<>();

    static {
        for (CategoryEnum e : values()) {
            ENG_ENUM_MAP.put(e.engName, e);
            CN_ENUM_MAP.put(e.cnName, e);
            CN_NAME_LIST.add(e.cnName);
        }
    }

    /**
     * 根据英文标识获取枚举
     */
    public static CategoryEnum getByEngName(String engName) {
        return ENG_ENUM_MAP.get(engName);
    }

    /**
     * 根据中文描述获取枚举
     */
    public static CategoryEnum getByCnName(String cnName) {
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
        for (CategoryEnum e : values()) {
            list.add(e.engName());
        }
        return list;
    }
}
