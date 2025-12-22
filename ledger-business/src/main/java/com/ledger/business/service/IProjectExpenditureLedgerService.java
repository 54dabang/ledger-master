package com.ledger.business.service;

import com.ledger.business.vo.ProjectExpenditureLedgerVo;
import com.ledger.common.core.domain.AjaxResult;

public interface IProjectExpenditureLedgerService {
    ProjectExpenditureLedgerVo getProjectExpenditureLedgerVo(Long projectId,Integer year,Long reimbursementSequenceNo);

    Long selectMaxReimbursementSequenceNo(Long projectId,Integer year);

    AjaxResult projectExpenditureLedgerValid(Long projectId, Integer year, Long reimbursementSequenceNo);
}
