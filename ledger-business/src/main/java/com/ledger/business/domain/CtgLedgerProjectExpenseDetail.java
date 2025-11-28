package com.ledger.business.domain;

import com.ledger.business.util.StrUtil;
import com.ledger.common.utils.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ledger.common.annotation.Excel;
import com.ledger.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 项目支出明细对象 ctg_ledger_project_expense_detail
 *
 * @author ledger
 * @date 2025-08-21
 */
public class CtgLedgerProjectExpenseDetail extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 支出明细id,自增
     */
    @Excel(name = "id")
    private Long id;

    /**
     * 台账系统项目id
     */
    @ApiModelProperty(name = "台账系统项目id")
    @Excel(name = "台账系统项目id")
    private Long ledgerProjectId;

    @ApiModelProperty(name = "报销年度")
    @Excel(name = "报销年度")
    private Integer year;

    /**
     * 报销单号，差旅系统支出明细编号，用于去重
     */
    @ApiModelProperty(name = "报销单号，差旅系统支出明细编号，用于去重")
    @Excel(name = "报销单号")
    private Long expenseReportNumber;

    /**
     * 科目名称
     */
    @ApiModelProperty(name = "科目名称")
    @Excel(name = "科目名称")
    private String subjectName;

    /**
     * 费用类型
     */
    @ApiModelProperty(name = "费用类型")
    @Excel(name = "费用类型")
    private String feeType;


    /**
     * 设备改造费
     */
    @ApiModelProperty(name = "金额")
    @Excel(name = "金额（万元）")
    private BigDecimal amount;


    @ApiModelProperty(name = "报销人姓名")
    @Excel(name = "报销人")
    private String reimburserName;

    @ApiModelProperty(name = "报销人登录用户名")
    private String reimburserLoginName;

    /**
     * 报销次序
     */
    @ApiModelProperty(name = "报销次序")
    @Excel(name = "第几次报销（输入数字）")
    private Long reimbursementSequenceNo;
    @Excel(name = "备注（无需填写，自动生成，请将内容复制到支出台账备注中）")
    private String remarkTemp;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setLedgerProjectId(Long ledgerProjectId) {
        this.ledgerProjectId = ledgerProjectId;
    }

    public Long getLedgerProjectId() {
        return ledgerProjectId;
    }

    public void setExpenseReportNumber(Long expenseReportNumber) {
        this.expenseReportNumber = expenseReportNumber;
    }

    public Long getExpenseReportNumber() {
        return expenseReportNumber;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setReimbursementSequenceNo(Long reimbursementSequenceNo) {
        this.reimbursementSequenceNo = reimbursementSequenceNo;
    }

    public Long getReimbursementSequenceNo() {
        return reimbursementSequenceNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReimburserName() {
        return reimburserName;
    }

    public void setReimburserName(String reimburserName) {
        this.reimburserName = reimburserName;
    }

    public String getReimburserLoginName() {
        return reimburserLoginName;
    }

    public void setReimburserLoginName(String reimburserLoginName) {
        this.reimburserLoginName = reimburserLoginName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getRemarkTemp() {
        return remarkTemp;
    }

    public void setRemarkTemp(String remarkTemp) {
        this.remarkTemp = remarkTemp;
    }

    @Override
    public String getRemark() {
        return StrUtil.buildRemark(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("ledgerProjectId", getLedgerProjectId())
                .append("year",getYear())
                .append("expenseReportNumber", getExpenseReportNumber())
                .append("subjectName", getSubjectName())
                .append("feeType", getFeeType())
                .append("amount", getAmount())
                .append("reimburserName", getReimburserName())
                .append("reimburserLoginName", getReimburserLoginName())
                .append("reimbursementSequenceNo", getReimbursementSequenceNo())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
