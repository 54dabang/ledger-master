package com.ledger.business.vo;

import com.ledger.business.domain.CtgLedgerProject;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CtgLedgerProjectVo extends CtgLedgerProject {
    @ApiModelProperty(value = "项目成员")
    private List<SysUserVo> members;

    @ApiModelProperty(value = "项目管理员")
    private SysUserVo manager;

    @ApiModelProperty(value = "年度预算")
    private BigDecimal annualBudgetFee;

    @ApiModelProperty(value = "预算所属年度")
    private Integer year;

    @ApiModelProperty(value = "年度预算id")
    private Long annualBudgetFeeId;


}
