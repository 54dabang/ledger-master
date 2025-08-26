package com.ledger.business.controller;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.dto.ReimbursementDTO;
import com.ledger.business.service.*;
import com.ledger.business.util.InitConstant;
import com.ledger.business.util.Result;
import com.ledger.business.vo.ProjectExpenditureLedgerVo;
import com.ledger.common.annotation.Log;
import com.ledger.common.constant.HttpStatus;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.enums.OperatorType;
import com.ledger.common.utils.DateUtils;
import com.ledger.framework.tools.RedisLock;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 报销申请控制器
 */
@RestController
@Api(value = "台账接口", tags = {"台账接口"})
@Slf4j
@RequestMapping("/api")
public class ReimbursementController extends BaseController {

    @Autowired
    private IReimbursementService reimbursementService;
    @Autowired
    private ICtgLedgerProjectService projectService;
    @Autowired
    private ICtgLedgerProjectExpenseDetailService expenseDetailService;
    @Autowired
    private ICtgLedgerProjectUserService projectUserService;
    @Autowired
    private RedisLock redisLock;
    @Autowired
    private IProjectExpenditureLedgerService projectExpenditureLedgerService;


    @ApiOperation("同步台账基本数据信息")
    @RequestMapping(value = "/white/syncReimbursementData", method = RequestMethod.POST)
    @Log(
            title = "business",          // 模块名称
            businessType = BusinessType.INSERT, // 业务类型（枚举）
            operatorType = OperatorType.MANAGE, // 操作类别
            isSaveRequestData = true,           // 保存请求参数
            isSaveResponseData = false          // 保存返回结果
    )
    public AjaxResult syncReimbursementData(@RequestBody ReimbursementDTO reimbursementDTO) {
        log.info("reimbursementDTO:{}", reimbursementDTO);
        String reimbursementProjectName = reimbursementDTO.getRsiContractData().getProjectName();
        CtgLedgerProject ctgLedgerProject = projectService.selectCtgLedgerProjectByProjectName(reimbursementProjectName);
        //数据检查
        if (Objects.isNull(ctgLedgerProject)) {
            return AjaxResult.error(HttpStatus.DATA_PROJECT_MISSING, String.format("同步的项目信息不存在，请联系管理员添加项目:%s", reimbursementProjectName));
        }
        CtgLedgerProjectExpenseDetail expenseDetail = expenseDetailService.selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(reimbursementDTO.getId());
        if (Objects.nonNull(expenseDetail)) {
            return AjaxResult.error(HttpStatus.DATA_DUPLICATE, String.format("该笔费用明细已经同步过！费用编号:%s", reimbursementDTO.getId()));
        }

        //如果用户不存在，则在数据库中创建、新增用户以及对应的部门，避免脏数据出现
        reimbursementService.syncUsersReimbursementData(reimbursementDTO);

        //检测用户是否是项目成员
        Pair<Boolean, String> resPair = reimbursementService.isClaimantsProjectMember(reimbursementDTO, ctgLedgerProject);
        if (!resPair.getFirst()) {
            return AjaxResult.error(HttpStatus.DATA_DUPLICATE, String.format("用户:%s，不是项目：%s 成员，请联系管理员添加！", resPair.getSecond(), ctgLedgerProject.getProjectName()));
        }

        //资源加锁，防止并发冲突
        boolean locked = false;
        final String lockKey = InitConstant.LOCK_KEY_PROJECT_PREFIX + ctgLedgerProject.getId();
        try {
            locked = redisLock.tryLock(lockKey, 2, TimeUnit.MINUTES);
            if (!locked) {
                return AjaxResult.error(HttpStatus.CONFLICT, String.format("项目名称:%s,正在使用，请稍后重试！", reimbursementProjectName));
            }
            reimbursementService.syncReimbursementData(reimbursementDTO, ctgLedgerProject);
        } finally {
            if (locked) {
                redisLock.releaseLock(lockKey);
            }
        }

        return AjaxResult.success("同步台账数据成功");
    }

    @RequestMapping(value = "/getProjectExpenditureLedger", method = RequestMethod.GET)
    public AjaxResult getProjectExpenditureLedger(@RequestParam("projectId") Long projectId) {
        Integer year = DateUtils.getNowDate().getYear() + 1900;
        Long  maxReimbursementSequenceNo = projectExpenditureLedgerService.selectMaxReimbursementSequenceNo(projectId, year);
        maxReimbursementSequenceNo = Optional.ofNullable(maxReimbursementSequenceNo).orElse(0l);
        ProjectExpenditureLedgerVo projectExpenditureLedgerVo = projectExpenditureLedgerService.getProjectExpenditureLedgerVo(projectId, year, maxReimbursementSequenceNo);
        return AjaxResult.success(projectExpenditureLedgerVo);
    }

}
