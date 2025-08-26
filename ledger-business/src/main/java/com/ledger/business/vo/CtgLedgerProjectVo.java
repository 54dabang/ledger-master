package com.ledger.business.vo;

import com.ledger.business.domain.CtgLedgerProject;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
