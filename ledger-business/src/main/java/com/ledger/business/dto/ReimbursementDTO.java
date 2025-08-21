package com.ledger.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDTO {
    @ApiModelProperty(value = "唯一编号，用于后续的去重")
    private Long id;

    @ApiModelProperty(value = "报销标题")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "公司信息")
    private ClaimantDTO.Company company;

    @ApiModelProperty(value = "部门信息")
    private ClaimantDTO.Department department;


    @ApiModelProperty(value = "报销人信息")
    private List<ClaimantDTO> claimantList;

    @ApiModelProperty(value = "挂靠项目相关信息")
    private RsiContractData rsiContractData;

    @ApiModelProperty(value = "报销金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "科目名称")
    private String subjectName;

    @ApiModelProperty(value = "费用类型")
    private String feeType;

    @ApiModelProperty(value = "提交人相关信息")
    private ClaimantDTO.UserDetail handler;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RsiContractData{

        @ApiModelProperty(value = "项目名称")
        private String projectName;

        @ApiModelProperty(value = "项目编号")
        private String  projectCode;


    }


}
