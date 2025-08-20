package com.ledger.business.controller;

import com.ledger.business.dto.ReimbursementDTO;
import com.ledger.business.service.IReimbursementService;
import com.ledger.business.util.Result;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报销申请控制器
 */
@RestController
@Api(value = "台账接口", tags = {"台账接口"})
@Slf4j
@RequestMapping("/api")
public class ReimbursementController extends BaseController{

   @Autowired
  private IReimbursementService reimbursementService;
    @ApiOperation("同步台账基本数据信息")
    @RequestMapping(value = "/white/syncReimbursementData", method = RequestMethod.POST)
    public AjaxResult syncReimbursementData(@RequestBody ReimbursementDTO reimbursementDTO) {
        log.info("reimbursementDTO:{}",reimbursementDTO);
        reimbursementService.syncReimbursementData(reimbursementDTO);
        return AjaxResult.success("同步台账数据成功");
    }
}
