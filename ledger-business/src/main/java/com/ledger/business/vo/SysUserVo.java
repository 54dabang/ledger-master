package com.ledger.business.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysUserVo {
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "登录用户名")
    private String userName;

    @ApiModelProperty(value = "用户真实姓名")
    private String nickName;

}
