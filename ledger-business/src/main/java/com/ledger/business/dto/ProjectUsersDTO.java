package com.ledger.business.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUsersDTO {
    @ApiModelProperty(value = "项目Id")
    private Long projectId;
    @ApiModelProperty(value = "系统用户id列表")
    private List<Long> projectUserIdList;
}
