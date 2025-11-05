package com.ledger.business.config.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginProperty {
    @ApiModelProperty(value = "环境")
    private String env;

    @ApiModelProperty(value = "上传路径")
    private String path;
}
