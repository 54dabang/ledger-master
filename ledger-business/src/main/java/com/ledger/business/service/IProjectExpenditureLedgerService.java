package com.ledger.business.service;

import com.ledger.business.vo.ProjectExpenditureLedgerVo;

public interface IProjectExpenditureLedgerService {
    ProjectExpenditureLedgerVo getProjectExpenditureLedgerVo(Long projectId,Integer year,Long reimbursementSequenceNo);

    Long selectMaxReimbursementSequenceNo(Long projectId,Integer year);
}
