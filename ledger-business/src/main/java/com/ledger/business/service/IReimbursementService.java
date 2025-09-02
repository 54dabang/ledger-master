package com.ledger.business.service;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.dto.ReimbursementDTO;

import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.RequestBody;

public interface IReimbursementService {
    void syncReimbursementData(ReimbursementDTO reimbursementDTO,CtgLedgerProject ctgLedgerProject);

    void syncUsersReimbursementData(ReimbursementDTO reimbursementDTO);

    Pair<Boolean,String> isClaimantsProjectMember(ReimbursementDTO reimbursementDTO, CtgLedgerProject ctgLedgerProject);

    boolean hasPermission(Long projectId, Long userId);

    void checkPermisson(Long projectId, Long userId);
}
