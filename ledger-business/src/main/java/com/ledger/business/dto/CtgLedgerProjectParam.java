package com.ledger.business.dto;

import com.ledger.business.domain.CtgLedgerProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CtgLedgerProjectParam extends CtgLedgerProject {
    private Integer participationType;
}
