package com.ledger.business.vo;

import com.ledger.common.core.domain.entity.SysUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class SysUserVo {
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "登录用户名")
    private String userName;

    @ApiModelProperty(value = "用户真实姓名")
    private String nickName;

    @ApiModelProperty(value = "性别")
    private String sex;
    @ApiModelProperty(value = "部门名称")
    private String deptName;
    @ApiModelProperty(value = "部门id")
    private Long deptId;

    public static SysUserVo toSysUserVo(SysUser sysUser){
        return SysUserVo.builder().userId(sysUser.getUserId())
                .userName(sysUser.getUserName())
                .nickName(sysUser.getNickName())
                .sex("0".equals(sysUser.getSex())?"男":"女")
                .deptName(Optional.ofNullable(sysUser.getDept()).map(d->d.getDeptName()).orElse(""))
                .deptId(Optional.ofNullable(sysUser.getDept()).map(d->d.getDeptId()).orElse(null))
                .build();
    }

}
