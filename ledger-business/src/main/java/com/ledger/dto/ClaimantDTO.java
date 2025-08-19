package com.ledger.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantDTO {
    @ApiModelProperty(value = "用户真实姓名（汉字）")
    private String userName;

    @ApiModelProperty(value = "性别")
    private String sex;

    private UserDetail user;




    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetail{
        private Long id;

        @ApiModelProperty(value = "身份证号")
        private String identityNum;

        @ApiModelProperty(value = "登录名称 eg:lei_xingbang")
        private String loginName;

        @ApiModelProperty(value = "职位名称 eg:专业师")
        private String job;

        @ApiModelProperty(value = "职级 eg:F")
        private String postLevel;

        @ApiModelProperty(value = "公司信息")
        private Company company;

        @ApiModelProperty(value = "部门信息")
        private Department department;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Company{
        @ApiModelProperty(value = "公司名称")
        private String name;

        @ApiModelProperty(value = "公司id")
        private Long id;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Department{
        @ApiModelProperty(value = "部门id")
        private Long id;

        @ApiModelProperty(value = "部门名称")
        private String name;

        @ApiModelProperty(value = "部门级别")
        private String departmentType;
    }


}
