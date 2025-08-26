package com.ledger.business.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProjectExpenditureLedgerVo {

    @ApiModelProperty(name = "项目总经费")
    private ProjectExpenditureLedgerColumnVo totalBudget;

    @ApiModelProperty(name = "已执行金额")
    private ProjectExpenditureLedgerColumnVo executedAmount;

    @ApiModelProperty(name = "年度预算经费")
    private ProjectExpenditureLedgerColumnVo annualBudget;

    @ApiModelProperty(name = "上次剩余经费")
    private ProjectExpenditureLedgerColumnVo lastRemainingFunds;

    @ApiModelProperty(name = "本次支出经费")
    private ProjectExpenditureLedgerColumnVo currentFunds;

    @ApiModelProperty(name = "累计支出经费")
    private ProjectExpenditureLedgerColumnVo cumulativeFunds;

    @ApiModelProperty(name = "本次剩余经费")
    private ProjectExpenditureLedgerColumnVo currentRemainingFunds;

    @ApiModelProperty(name = "剩余总经费")
    private BigDecimal remainingTotalFunds;

}
