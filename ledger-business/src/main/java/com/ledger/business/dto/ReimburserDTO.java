package com.ledger.business.dto;

import com.ledger.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReimburserDTO {

    @ApiModelProperty(name = "报销人姓名")
    private String reimburserName;

    @ApiModelProperty(name = "报销人登录用户名")
    private String reimburserLoginName;
}
