package com.ledger.business.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/**
 * 项目支出台账列的信息
 */
public class ProjectExpenditureLedgerColumnVo {


    @ApiModelProperty(name = "中文列名")
    private String columnCnName;

    @ApiModelProperty(name = "英文列名")
    private String columnEngName;

    @ApiModelProperty(name = "直接费用")
    private BigDecimal directCosts;

    @ApiModelProperty(name = "项目id")
    private Long projectId;

    @ApiModelProperty(name = "项目名称")
    private String projectName;


    /** 项目编号 */
    @ApiModelProperty(name = "项目编号")
    private String projectCode;

    /** 报销系统中的项目id */
    @ApiModelProperty(name = "报销系统中的项目id")
    private Long extendProjectId;

    @ApiModelProperty(name = "项目负责人登录用户名")
    private String projectManagerLoginName;

    /** 购置设备费 */
    @ApiModelProperty(name = "购置设备费")
    private BigDecimal equipPurchaseFee;

    /** 试制设备费 */
    @ApiModelProperty(name = "试制设备费")
    private BigDecimal protoEquipFee;

    /** 设备改造费 */
    @ApiModelProperty(name = "设备改造费")
    private BigDecimal equipRenovFee;

    /** 设备租赁费 */
    @ApiModelProperty(name = "设备租赁费")
    private BigDecimal equipRentFee;

    /** 材料费 */
    @ApiModelProperty(name = "材料费")
    private BigDecimal materialCost;

    /** 测试化验加工费 */
    @ApiModelProperty(name = "测试化验加工费")
    private BigDecimal testProcFee;

    /** 燃料动力费 */
    @ApiModelProperty(name = "燃料动力费")
    private BigDecimal fuelPowerCost;

    /** 出版/文献/信息传播/知识产权事务费 */
    @ApiModelProperty(name = "出版/文献/信息传播/知识产权事务费")
    private BigDecimal pubDocIpFee;

    /** 差旅/会议/国际合作交流费 */
    @ApiModelProperty(name = "差旅/会议/国际合作交流费")
    private BigDecimal travelConfCoopFee;

    /** 人工费 */
    @ApiModelProperty(name = "人工费")
    private BigDecimal laborCost;

    /** 劳务费 */
    @ApiModelProperty(name = "劳务费")
    private BigDecimal serviceCost;

    /** 专家咨询费 */
    @ApiModelProperty(name = "专家咨询费")
    private BigDecimal expertConsultFee;

    @ApiModelProperty(name = "间接费用")
    private BigDecimal indirectCosts;


    /** 管理费 */
    @ApiModelProperty(name = "管理费")
    private BigDecimal mgmtFee;

    /** 税金 */
    @ApiModelProperty(name = "税金")
    private BigDecimal taxFee;

    @ApiModelProperty(name = "合同金额")
    private BigDecimal contractAmount;





}
