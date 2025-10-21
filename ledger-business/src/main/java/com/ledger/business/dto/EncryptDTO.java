package com.ledger.business.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptDTO {
    @ApiModelProperty(value = "加密后的数据")
    private String data;
}
