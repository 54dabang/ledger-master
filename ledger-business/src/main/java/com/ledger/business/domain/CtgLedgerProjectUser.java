package com.ledger.business.domain;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ledger.common.annotation.Excel;
import com.ledger.common.core.domain.BaseEntity;

/**
 * 项目用户对象 ctg_ledger_project_user
 * 
 * @author ledger
 * @date 2025-08-20
 */
public class CtgLedgerProjectUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目id,自增字段 */
    private Long id;

    /** ctg_ledger_project表项目id */
    @ApiModelProperty(name = "ctg_ledger_project表项目id")
    private Long ctgLedgerProjectId;

    /** sys_user表用户id */
    @ApiModelProperty(name = "sys_user表用户id")
    private Long sysUserId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCtgLedgerProjectId(Long ctgLedgerProjectId) 
    {
        this.ctgLedgerProjectId = ctgLedgerProjectId;
    }

    public Long getCtgLedgerProjectId() 
    {
        return ctgLedgerProjectId;
    }

    public void setSysUserId(Long sysUserId) 
    {
        this.sysUserId = sysUserId;
    }

    public Long getSysUserId() 
    {
        return sysUserId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("ctgLedgerProjectId", getCtgLedgerProjectId())
            .append("sysUserId", getSysUserId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
