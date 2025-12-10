package com.ledger.business.vo;

import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CtgLedgerProjectExpenseDetailVo extends CtgLedgerProjectExpenseDetail {
    @ApiModelProperty(value = "项目管理员")
    private SysUserVo manager;

    @ApiModelProperty(value = "项目联系人")
    private SysUserVo contact;


}
