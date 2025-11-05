package com.ledger.business.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginDTO {
    @ApiModelProperty(value = "插件版本号")
    private String pluginVersion;

    @ApiModelProperty(value = "插件版上传后的完整路径")
    private String path;
}
