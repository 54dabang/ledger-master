package com.ledger.business.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidDTO {
    @ApiModelProperty(value = "token是否有效")
    private Boolean tokenValid;
}
