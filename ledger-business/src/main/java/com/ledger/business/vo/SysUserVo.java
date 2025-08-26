package com.ledger.business.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysUserVo {
    private Long userId;

    private String userName;

    private String nickName;

}
