package com.ledger.business.domain;

import com.ledger.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;

public class CtgLedgerConfig extends BaseEntity {

    @ApiModelProperty(name = "业务系统生成的id")
    private String id;


    @ApiModelProperty(value = "系统配置名称")
    private String name;

    @ApiModelProperty(value = "具体配置信息")
    private String configObjStr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigObjStr() {
        return configObjStr;
    }

    public void setConfigObjStr(String configObjStr) {
        this.configObjStr = configObjStr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
