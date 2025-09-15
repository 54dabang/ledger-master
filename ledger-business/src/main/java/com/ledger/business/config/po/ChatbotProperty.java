package com.ledger.business.config.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotProperty {
    @ApiModelProperty(value = "环境")
    private String env;

    @ApiModelProperty(value = "应用场景[唯一标识]")
    private String useCase;

    @ApiModelProperty(value = "chatbot对应的url")
    private String url;
}
