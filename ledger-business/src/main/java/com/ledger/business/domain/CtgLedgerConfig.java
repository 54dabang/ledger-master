package com.ledger.business.domain;

import com.ledger.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;

public class CtgLedgerConfig extends BaseEntity {

    /** 项目id,自增字段 */
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
