package com.ledger.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidDTO {
    private Boolean tokenValid;
}
