package com.ledger.business.domain;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

public class BimUser implements Serializable{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "业务系统生成的id")
    private String id;

    @ApiModelProperty(name = "用户ID")
    private String userId;

    @ApiModelProperty(name = "部门ID")
    private String deptId;

    @ApiModelProperty(name = "账号")
    private String username;

    @ApiModelProperty(name = "姓名")
    private String name;

    @ApiModelProperty(name = "是否启用")
    private String enable;

    @ApiModelProperty(name = "账号类型")
    private String type;

    @ApiModelProperty(name = "身份证号")
    private String idcard;

    @ApiModelProperty(name = "EHR人员编码")
    private String ehrUserCode;

    @ApiModelProperty(name = "创建时间")
    private Date createTime;

    @ApiModelProperty(name = "更新时间")
    private Date updateTime;

    @ApiModelProperty(name = "删除标志（0代表存在 2代表删除）")
    private String delFlag;

    @ApiModelProperty(name = "手机号")
    private String phonenumber;

    @ApiModelProperty(name = "邮箱")
    private String email;

    @ApiModelProperty(name = "座机")
    private String telphone;

    @ApiModelProperty(name = "性别")
    private String sex;

    @ApiModelProperty(name = "职务信息")
    private String posts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getEhrUserCode() {
        return ehrUserCode;
    }

    public void setEhrUserCode(String ehrUserCode) {
        this.ehrUserCode = ehrUserCode;
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPosts() {
        return posts;
    }

    public void setPosts(String posts) {
        this.posts = posts;
    }
}

