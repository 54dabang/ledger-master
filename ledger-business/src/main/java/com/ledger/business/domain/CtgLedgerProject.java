package com.ledger.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ledger.common.annotation.Excel;
import com.ledger.common.core.domain.BaseEntity;

/**
 * 项目管理对象 ctg_ledger_project
 * 
 * @author ledger
 * @date 2025-08-21
 */
public class CtgLedgerProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目id,自增字段 */
    private Long id;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 项目编号 */
    @Excel(name = "项目编号")
    private String projectCode;

    /** 报销系统中的项目id */
    @Excel(name = "报销系统中的项目id")
    private Long extendProjectId;

    /** 购置设备费 */
    @Excel(name = "购置设备费")
    private BigDecimal equipPurchaseFee;

    /** 试制设备费 */
    @Excel(name = "试制设备费")
    private BigDecimal protoEquipFee;

    /** 设备改造费 */
    @Excel(name = "设备改造费")
    private BigDecimal equipRenovFee;

    /** 设备租赁费 */
    @Excel(name = "设备租赁费")
    private BigDecimal equipRentFee;

    /** 材料费 */
    @Excel(name = "材料费")
    private BigDecimal materialCost;

    /** 测试化验加工费 */
    @Excel(name = "测试化验加工费")
    private BigDecimal testProcFee;

    /** 燃料动力费 */
    @Excel(name = "燃料动力费")
    private BigDecimal fuelPowerCost;

    /** 出版/文献/信息传播/知识产权事务费 */
    @Excel(name = "出版/文献/信息传播/知识产权事务费")
    private BigDecimal pubDocIpFee;

    /** 差旅/会议/国际合作交流费 */
    @Excel(name = "差旅/会议/国际合作交流费")
    private BigDecimal travelConfCoopFee;

    /** 人工费 */
    @Excel(name = "人工费")
    private BigDecimal laborCost;

    /** 劳务费 */
    @Excel(name = "劳务费")
    private BigDecimal serviceCost;

    /** 专家咨询费 */
    @Excel(name = "专家咨询费")
    private BigDecimal expertConsultFee;

    /** 管理费 */
    @Excel(name = "管理费")
    private BigDecimal mgmtFee;

    /** 税金 */
    @Excel(name = "税金")
    private BigDecimal taxFee;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setProjectName(String projectName) 
    {
        this.projectName = projectName;
    }

    public String getProjectName() 
    {
        return projectName;
    }

    public void setProjectCode(String projectCode) 
    {
        this.projectCode = projectCode;
    }

    public String getProjectCode() 
    {
        return projectCode;
    }

    public void setExtendProjectId(Long extendProjectId) 
    {
        this.extendProjectId = extendProjectId;
    }

    public Long getExtendProjectId() 
    {
        return extendProjectId;
    }

    public void setEquipPurchaseFee(BigDecimal equipPurchaseFee) 
    {
        this.equipPurchaseFee = equipPurchaseFee;
    }

    public BigDecimal getEquipPurchaseFee() 
    {
        return equipPurchaseFee;
    }

    public void setProtoEquipFee(BigDecimal protoEquipFee) 
    {
        this.protoEquipFee = protoEquipFee;
    }

    public BigDecimal getProtoEquipFee() 
    {
        return protoEquipFee;
    }

    public void setEquipRenovFee(BigDecimal equipRenovFee) 
    {
        this.equipRenovFee = equipRenovFee;
    }

    public BigDecimal getEquipRenovFee() 
    {
        return equipRenovFee;
    }

    public void setEquipRentFee(BigDecimal equipRentFee) 
    {
        this.equipRentFee = equipRentFee;
    }

    public BigDecimal getEquipRentFee() 
    {
        return equipRentFee;
    }

    public void setMaterialCost(BigDecimal materialCost) 
    {
        this.materialCost = materialCost;
    }

    public BigDecimal getMaterialCost() 
    {
        return materialCost;
    }

    public void setTestProcFee(BigDecimal testProcFee) 
    {
        this.testProcFee = testProcFee;
    }

    public BigDecimal getTestProcFee() 
    {
        return testProcFee;
    }

    public void setFuelPowerCost(BigDecimal fuelPowerCost) 
    {
        this.fuelPowerCost = fuelPowerCost;
    }

    public BigDecimal getFuelPowerCost() 
    {
        return fuelPowerCost;
    }

    public void setPubDocIpFee(BigDecimal pubDocIpFee) 
    {
        this.pubDocIpFee = pubDocIpFee;
    }

    public BigDecimal getPubDocIpFee() 
    {
        return pubDocIpFee;
    }

    public void setTravelConfCoopFee(BigDecimal travelConfCoopFee) 
    {
        this.travelConfCoopFee = travelConfCoopFee;
    }

    public BigDecimal getTravelConfCoopFee() 
    {
        return travelConfCoopFee;
    }

    public void setLaborCost(BigDecimal laborCost) 
    {
        this.laborCost = laborCost;
    }

    public BigDecimal getLaborCost() 
    {
        return laborCost;
    }

    public void setServiceCost(BigDecimal serviceCost) 
    {
        this.serviceCost = serviceCost;
    }

    public BigDecimal getServiceCost() 
    {
        return serviceCost;
    }

    public void setExpertConsultFee(BigDecimal expertConsultFee) 
    {
        this.expertConsultFee = expertConsultFee;
    }

    public BigDecimal getExpertConsultFee() 
    {
        return expertConsultFee;
    }

    public void setMgmtFee(BigDecimal mgmtFee) 
    {
        this.mgmtFee = mgmtFee;
    }

    public BigDecimal getMgmtFee() 
    {
        return mgmtFee;
    }

    public void setTaxFee(BigDecimal taxFee) 
    {
        this.taxFee = taxFee;
    }

    public BigDecimal getTaxFee() 
    {
        return taxFee;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("projectName", getProjectName())
            .append("projectCode", getProjectCode())
            .append("extendProjectId", getExtendProjectId())
            .append("equipPurchaseFee", getEquipPurchaseFee())
            .append("protoEquipFee", getProtoEquipFee())
            .append("equipRenovFee", getEquipRenovFee())
            .append("equipRentFee", getEquipRentFee())
            .append("materialCost", getMaterialCost())
            .append("testProcFee", getTestProcFee())
            .append("fuelPowerCost", getFuelPowerCost())
            .append("pubDocIpFee", getPubDocIpFee())
            .append("travelConfCoopFee", getTravelConfCoopFee())
            .append("laborCost", getLaborCost())
            .append("serviceCost", getServiceCost())
            .append("expertConsultFee", getExpertConsultFee())
            .append("mgmtFee", getMgmtFee())
            .append("taxFee", getTaxFee())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
