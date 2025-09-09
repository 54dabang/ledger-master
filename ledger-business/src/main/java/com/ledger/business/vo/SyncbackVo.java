package com.ledger.business.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncbackVo {

    private String token;

    private Long currentSequenceNo;

    private Long projectId;

}
