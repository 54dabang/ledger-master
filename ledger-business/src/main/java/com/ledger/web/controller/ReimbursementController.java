package com.ledger.web.controller;

import com.ledger.dto.ReimbursementDTO;
import com.ledger.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

import lombok.extern.slf4j.Slf4j;
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
public class ReimbursementController {


    @ApiModelProperty(value = "同步台账基本数据信息")
    @RequestMapping(value = "/white/syncReimbursementData", method = RequestMethod.POST)
    public Result<String> syncReimbursementData(@RequestBody ReimbursementDTO reimbursementDTO) {
        log.info("reimbursementDTO:{}",reimbursementDTO);
        return Result.success(null);
    }
}
