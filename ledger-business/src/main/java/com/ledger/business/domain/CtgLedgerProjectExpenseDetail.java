package com.ledger.business.domain;

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
    private Long id;

    /**
     * 台账系统项目id
     */
    @Excel(name = "台账系统项目id")
    private Long ledgerProjectId;

    @Excel(name = "报销年度")
    private Integer year;

    /**
     * 报销单号，差旅系统支出明细编号，用于去重
     */
    @Excel(name = "报销单号，差旅系统支出明细编号，用于去重")
    private Long expenseReportNumber;

    /**
     * 科目名称
     */
    @Excel(name = "科目名称")
    private String subjectName;

    /**
     * 费用类型
     */
    @Excel(name = "费用类型")
    private String feeType;


    /**
     * 设备改造费
     */
    @Excel(name = "金额")
    private BigDecimal amount;


    @Excel(name = "报销人姓名")
    private String reimburserName;

    @Excel(name = "报销人登录用户名")
    private String reimburserLoginName;

    /**
     * 报销次序
     */
    @Excel(name = "报销次序")
    private Long reimbursementSequenceNo;

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
