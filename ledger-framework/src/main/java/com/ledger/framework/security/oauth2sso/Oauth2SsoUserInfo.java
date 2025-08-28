package com.ledger.framework.security.oauth2sso;


/**
 * {
  "country": null,
  "sorgId": null,
  "updateDate": "2023-04-16 05:04:04",
  "mail": null,
  "gender": 1.0,
  "loginType": 1.0,
  "nation": null,
  "displayName": "王乐陶",
  "positionNumber": null,
  "title": null,
  "loginInterceptFlagTwo": 0.0,
  "loginInterceptFlagFour": 0.0,
  "employeeNumber": null,
  "changePwdAt": "2021-08-26 00:00:00",
  "spRoleList": [
    
  ],
  "pwdPolicy": "30",
  "loginInterceptFlagFive": 0.0,
  "loginInterceptFlagThree": 0.0,
  "identityNumber": null,
  "identityType": null,
  "loginName": "wang_letao",
  "secAccValid": 1.0,
  "pinyinShortName": null,
  "orgNumber": null,
  "wechatNo": "wang_letao",
  "orgNamePath": null,
  "passwordModifyRequired": 0.0,
  "birthDay": "2023-01-05 03:42:00",
  "givenName": "wang_letao",
  "mobile": "15923075093",
  "loginInterceptFlagOne": 0.0,
  "certSn": null,
  "20200805100359104-B0DC-4508D4E5F": "20200805100359104-B0DC-4508D4E5F",
  "employeeType": "0",
  "orgCodePath": null,
  "otpKey": null,
  "positionStatus": null,
  "departmentNumber": null,
  "certDn": null,
  "spNameList": [
    
  ],
  "isPassRemind": 0.0
}
 * @author wujiatong
 *
 */

public class Oauth2SsoUserInfo {
    /**
     * 获取accessToken时同时可以获取到uid
     */
    private String uid; 
    
    private String country;
    private String sorgId;
    private String updateDate;
    private String mail;
    private int gender;
    private int loginType;
    private String nation;
    private String displayName;
    private String positionNumber;
    private String title;
    private int loginInterceptFlagTwo;
    private int loginInterceptFlagFour;
    private String employeeNumber;
    private String changePwdAt;
    // private List<String> spRoleList;
    private String pwdPolicy;
    private int loginInterceptFlagFive;
    private int loginInterceptFlagThree;
    private String identityNumber;
    private String identityType;
    private String loginName;
    private int secAccValid;
    private String pinyinShortName;
    private String orgNumber;
    private String wechatNo;
    private String orgNamePath;
    private int passwordModifyRequired;
    private String birthDay;
    private String givenName;
    private String mobile;
    private int loginInterceptFlagOne;
    private String certSn;
    // private String 20200805100359104-B0DC-4508D4E5F;
    private String employeeType;
    private String orgCodePath;
    private String otpKey;
    private String positionStatus;
    private String departmentNumber;
    private String certDn;
    // private List<String> spNameList;
    private int isPassRemind;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSorgId() {
        return sorgId;
    }

    public void setSorgId(String sorgId) {
        this.sorgId = sorgId;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLoginInterceptFlagTwo() {
        return loginInterceptFlagTwo;
    }

    public void setLoginInterceptFlagTwo(int loginInterceptFlagTwo) {
        this.loginInterceptFlagTwo = loginInterceptFlagTwo;
    }

    public int getLoginInterceptFlagFour() {
        return loginInterceptFlagFour;
    }

    public void setLoginInterceptFlagFour(int loginInterceptFlagFour) {
        this.loginInterceptFlagFour = loginInterceptFlagFour;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getChangePwdAt() {
        return changePwdAt;
    }

    public void setChangePwdAt(String changePwdAt) {
        this.changePwdAt = changePwdAt;
    }

    public String getPwdPolicy() {
        return pwdPolicy;
    }

    public void setPwdPolicy(String pwdPolicy) {
        this.pwdPolicy = pwdPolicy;
    }

    public int getLoginInterceptFlagFive() {
        return loginInterceptFlagFive;
    }

    public void setLoginInterceptFlagFive(int loginInterceptFlagFive) {
        this.loginInterceptFlagFive = loginInterceptFlagFive;
    }

    public int getLoginInterceptFlagThree() {
        return loginInterceptFlagThree;
    }

    public void setLoginInterceptFlagThree(int loginInterceptFlagThree) {
        this.loginInterceptFlagThree = loginInterceptFlagThree;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public int getSecAccValid() {
        return secAccValid;
    }

    public void setSecAccValid(int secAccValid) {
        this.secAccValid = secAccValid;
    }

    public String getPinyinShortName() {
        return pinyinShortName;
    }

    public void setPinyinShortName(String pinyinShortName) {
        this.pinyinShortName = pinyinShortName;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public String getWechatNo() {
        return wechatNo;
    }

    public void setWechatNo(String wechatNo) {
        this.wechatNo = wechatNo;
    }

    public String getOrgNamePath() {
        return orgNamePath;
    }

    public void setOrgNamePath(String orgNamePath) {
        this.orgNamePath = orgNamePath;
    }

    public int getPasswordModifyRequired() {
        return passwordModifyRequired;
    }

    public void setPasswordModifyRequired(int passwordModifyRequired) {
        this.passwordModifyRequired = passwordModifyRequired;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getLoginInterceptFlagOne() {
        return loginInterceptFlagOne;
    }

    public void setLoginInterceptFlagOne(int loginInterceptFlagOne) {
        this.loginInterceptFlagOne = loginInterceptFlagOne;
    }

    public String getCertSn() {
        return certSn;
    }

    public void setCertSn(String certSn) {
        this.certSn = certSn;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getOrgCodePath() {
        return orgCodePath;
    }

    public void setOrgCodePath(String orgCodePath) {
        this.orgCodePath = orgCodePath;
    }

    public String getOtpKey() {
        return otpKey;
    }

    public void setOtpKey(String otpKey) {
        this.otpKey = otpKey;
    }

    public String getPositionStatus() {
        return positionStatus;
    }

    public void setPositionStatus(String positionStatus) {
        this.positionStatus = positionStatus;
    }

    public String getDepartmentNumber() {
        return departmentNumber;
    }

    public void setDepartmentNumber(String departmentNumber) {
        this.departmentNumber = departmentNumber;
    }

    public String getCertDn() {
        return certDn;
    }

    public void setCertDn(String certDn) {
        this.certDn = certDn;
    }

    public int getIsPassRemind() {
        return isPassRemind;
    }

    public void setIsPassRemind(int isPassRemind) {
        this.isPassRemind = isPassRemind;
    }
}
