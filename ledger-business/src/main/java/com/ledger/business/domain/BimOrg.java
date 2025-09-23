package com.ledger.business.domain;


import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

public class BimOrg implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "业务系统生成的id")
    private String id;

    @ApiModelProperty(name = "机构id")
    private String depId;

    @ApiModelProperty(name = "父机构id")
    private String depPid;

    @ApiModelProperty(name = "HR组织机构主键PK")
    private String hrDeptPk;

    @ApiModelProperty(name = "机构简称")
    private String depShortName;

    @ApiModelProperty(name = "机构全称")
    private String depFullName;

    @ApiModelProperty(name = "机构完整路径")
    private String depFullPath;

    @ApiModelProperty(name = "是否启用")
    private String enable;

    @ApiModelProperty(name = "机构类型")
    private String depType;

    @ApiModelProperty(name = "创建时间")
    private Date createTime;

    @ApiModelProperty(name = "更新时间")
    private Date updateTime;

    @ApiModelProperty(name = "删除标志（0代表存在 2代表删除）")
    private String delFlag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    public String getDepPid() {
        return depPid;
    }

    public void setDepPid(String depPid) {
        this.depPid = depPid;
    }

    public String getHrDeptPk() {
        return hrDeptPk;
    }

    public void setHrDeptPk(String hrDeptPk) {
        this.hrDeptPk = hrDeptPk;
    }

    public String getDepShortName() {
        return depShortName;
    }

    public void setDepShortName(String depShortName) {
        this.depShortName = depShortName;
    }

    public String getDepFullName() {
        return depFullName;
    }

    public void setDepFullName(String depFullName) {
        this.depFullName = depFullName;
    }

    public String getDepFullPath() {
        return depFullPath;
    }

    public void setDepFullPath(String depFullPath) {
        this.depFullPath = depFullPath;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getDepType() {
        return depType;
    }

    public void setDepType(String depType) {
        this.depType = depType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
}
