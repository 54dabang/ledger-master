package com.ledger.business.service;

import com.ledger.business.dto.ReimbursementDTO;
import org.springframework.web.bind.annotation.RequestBody;

public interface IReimbursementService {
    void syncReimbursementData(ReimbursementDTO reimbursementDTO);
}
