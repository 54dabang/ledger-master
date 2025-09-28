package com.ledger.business.controller;

import com.ledger.business.config.ChatbotConfig;
import com.ledger.business.config.LegerConfig;
import com.ledger.business.config.po.ChatbotProperty;
import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.dto.ReimbursementDTO;
import com.ledger.business.service.*;
import com.ledger.business.util.InitConstant;
import com.ledger.business.util.Result;
import com.ledger.business.util.StringUtil;
import com.ledger.business.vo.ProjectExpenditureLedgerVo;
import com.ledger.business.vo.SyncbackVo;
import com.ledger.business.vo.SysUserVo;
import com.ledger.common.annotation.Log;
import com.ledger.common.constant.HttpStatus;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.enums.OperatorType;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.framework.tools.RedisLock;
import com.ledger.framework.web.service.SysLoginService;
import com.ledger.system.service.ISysUserService;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysLoginService sysLoginService;

    @Autowired
    private ChatbotConfig chatbotConfig;

    @Autowired
    private LegerConfig legerConfig;

    @ApiOperation("同步台账基本数据信息")
    @RequestMapping(value = "/white/syncReimbursementData", method = RequestMethod.POST)
    @Log(
            title = "同步数据",          // 模块名称
            businessType = BusinessType.INSERT, // 业务类型（枚举）
            operatorType = OperatorType.MANAGE, // 操作类别
            isSaveRequestData = true,           // 保存请求参数
            isSaveResponseData = false          // 保存返回结果
    )
    @ApiResponses({
            @ApiResponse(code = 701, message = "同步数据已经存在", response = AjaxResult.class),
            @ApiResponse(code = 702, message = "同步项目缺失", response = AjaxResult.class)
    })
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
        boolean isMember = reimbursementService.isHandlerProjectMember(reimbursementDTO, ctgLedgerProject);
        if (!isMember) {
            return AjaxResult.error(HttpStatus.DATA_DUPLICATE, String.format("用户:%s，不是项目：《%s》 成员，请联系项目管理员添加！", reimbursementDTO.getHandler().getLoginName(), ctgLedgerProject.getProjectName()));
        }

        //资源加锁，防止并发冲突
        boolean locked = false;
        final String lockKey = InitConstant.LOCK_KEY_PROJECT_PREFIX + ctgLedgerProject.getId();
        try {
            locked = redisLock.tryLock(lockKey, 2, TimeUnit.MINUTES);
            if (!locked) {
                return AjaxResult.error(HttpStatus.CONFLICT, String.format("项目名称:《%s》,正在使用，请稍后重试！", reimbursementProjectName));
            }
            Long currentSequenceNo = reimbursementService.syncReimbursementData(reimbursementDTO, ctgLedgerProject);
            String loginName = reimbursementDTO.getHandler().getLoginName();
            String token = sysLoginService.getTokenByLoginName(loginName);
            SyncbackVo syncbackVo = SyncbackVo.builder()
                    .token(token)
                    .currentSequenceNo(currentSequenceNo)
                    .projectId(ctgLedgerProject.getId())
                    .build();
            log.info("reimbursementDTO:{} sync success! syncbackVo：{}", reimbursementDTO, syncbackVo);
            return AjaxResult.success(syncbackVo);
        } catch (Exception e) {
            log.error("reimbursementDTO:{} sync failed!", reimbursementDTO, e);
            return AjaxResult.error(e.getMessage());
        } finally {
            if (locked) {
                redisLock.releaseLock(lockKey);
            }
        }

    }

    @ApiOperation("导出台账")
    @RequestMapping(value = "/getProjectExpenditureLedger", method = RequestMethod.GET)
    @PreAuthorize("@ss.hasPermi('business:expenditure:exportledger')")
    @Log(title = "导出台账", businessType = BusinessType.EXPORT)
    public AjaxResult getProjectExpenditureLedger(@RequestParam("projectId") Long projectId, @RequestParam("year") Integer year, @RequestParam("maxReimbursementSequenceNo") Long maxReimbursementSequenceNo) {
        reimbursementService.checkPermisson(projectId, SecurityUtils.getUserId());
        // 使用Calendar获取实际年份
        Calendar calendar = Calendar.getInstance();
        year = Optional.ofNullable(year).orElse(calendar.get(Calendar.YEAR));
        if (Objects.isNull(maxReimbursementSequenceNo)) {
            maxReimbursementSequenceNo = projectExpenditureLedgerService.selectMaxReimbursementSequenceNo(projectId, year);
        }
        maxReimbursementSequenceNo = Optional.ofNullable(maxReimbursementSequenceNo).orElse(0L);
        ProjectExpenditureLedgerVo projectExpenditureLedgerVo = projectExpenditureLedgerService.getProjectExpenditureLedgerVo(projectId, year, maxReimbursementSequenceNo);
        return AjaxResult.success(projectExpenditureLedgerVo);
    }

    @ApiOperation("获取所有有效用户")
    @RequestMapping(value = "/loadValidUsers", method = RequestMethod.GET)
    @PreAuthorize("@ss.hasPermi('business:expenditure:userlist')")
    public AjaxResult loadValidUsers(@RequestParam(name = "name",required = false) String name) {
        startPage();
        SysUser param = new SysUser();
        param.setDelFlag(InitConstant.USER_EXIST_FLAG);
        Optional.ofNullable(name)
                .filter(n -> !n.isEmpty())
                .ifPresent(n -> {
                    boolean startEnglish = StringUtil.startWithEnglish(n);
                    if (startEnglish) {
                        param.setUserName(n);
                    } else {
                        param.setNickName(n);
                    }
                });

        List<SysUser> userList = userService.selectUserList(param);
        List<SysUserVo> sysUserVoList = userList.stream().map(u -> SysUserVo.toSysUserVo(u)).collect(Collectors.toList());
        return AjaxResult.success(sysUserVoList);
    }

    @ApiOperation("获取聊天机器人")
    @RequestMapping(value = "/getChatbotUrl", method = RequestMethod.GET)
    @Log(title = "聊天机器人", businessType = BusinessType.OTHER)
    public AjaxResult getChatbotUrl(@RequestParam("useCase") String useCase) {
        String url = chatbotConfig.getConfig().stream().filter(c -> c.getEnv().equals(legerConfig.getEnv())).map(c -> c.getUrl()).findFirst()
                .orElse(null);
        return AjaxResult.success(url);
    }

}
