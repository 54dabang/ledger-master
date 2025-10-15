package com.ledger.business.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.service.*;
import com.ledger.business.util.LedgerExcelUtil;
import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.common.constant.Constants;
import com.ledger.common.constant.HttpStatus;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.framework.web.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.enums.BusinessType;
import com.ledger.business.domain.CtgLedgerAnnualBudget;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 项目总预算台账Controller
 *
 * @author ledger
 * @date 2025-08-20
 */
@Api(tags = "项目年度预算接口（含年度预算明细）")
@RestController
@RequestMapping("/api/annualBudget")
@Slf4j
public class CtgLedgerAnnualBudgetController extends BaseController {
    @Autowired
    private ICtgLedgerAnnualBudgetService ctgLedgerAnnualBudgetService;
    @Autowired
    private ICtgLedgerProjectExpenseDetailService projectExpenseDetailService;
    @Autowired
    private IReimbursementService reimbursementService;
    @Autowired
    private ICtgLedgerProjectService ctgLedgerProjectService;
    @Autowired
    private ICtgLedgerProjectUserService projectUserService;
    @Autowired
    private PermissionService permissionService;


    /**
     * 查询项目总预算台账列表
     */
    @ApiOperation("查询年度预算列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "year", value = "年份", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "equipPurchaseFee", value = "设备购置费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "protoEquipFee", value = "试制设备费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "equipRenovFee", value = "设备改造与租赁费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "materialCost", value = "材料费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "testProcFee", value = "测试化验加工费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "fuelPowerCost", value = "燃料动力费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "pubDocIpFee", value = "出版/文献/信息传播/知识产权事务费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "travelConfCoopFee", value = "差旅/会议/国际合作与交流费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "laborCost", value = "劳务费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "serviceCost", value = "专家咨询费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "expertConsultFee", value = "管理费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "mgmtFee", value = "税费", dataType = "java.math.BigDecimal", paramType = "query")
    })
    @PreAuthorize("@ss.hasPermi('business:budget:list')")
    @GetMapping("/list")
    public TableDataInfo list(CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {

        startPage();
        List<CtgLedgerAnnualBudget> list = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetList(ctgLedgerAnnualBudget);
        return getDataTable(list);
    }

    /**
     * 导出项目总预算台账列表
     */
    @ApiOperation("导出年度预算列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "year", value = "年份", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "equipPurchaseFee", value = "设备购置费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "protoEquipFee", value = "试制设备费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "equipRenovFee", value = "设备改造与租赁费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "materialCost", value = "材料费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "testProcFee", value = "测试化验加工费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "fuelPowerCost", value = "燃料动力费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "pubDocIpFee", value = "出版/文献/信息传播/知识产权事务费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "travelConfCoopFee", value = "差旅/会议/国际合作与交流费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "laborCost", value = "劳务费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "serviceCost", value = "专家咨询费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "expertConsultFee", value = "管理费", dataType = "java.math.BigDecimal", paramType = "query"),
            @ApiImplicitParam(name = "mgmtFee", value = "税费", dataType = "java.math.BigDecimal", paramType = "query")
    })
    @PreAuthorize("@ss.hasPermi('business:budget:export')")
    @Log(title = "导出年度预算excel", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        List<CtgLedgerAnnualBudget> list = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetList(ctgLedgerAnnualBudget);
        ExcelUtil<CtgLedgerAnnualBudget> util = new ExcelUtil<CtgLedgerAnnualBudget>(CtgLedgerAnnualBudget.class);
        util.exportExcel(response, list, "项目总预算台账数据");
    }

    /**
     * 获取项目总预算台账详细信息
     */
    @ApiOperation("获取年度预算台账详细信息")
    @ApiImplicitParam(name = "id", value = "项目总预算台账主键ID", required = true, dataType = "Long", paramType = "path")
    @PreAuthorize("@ss.hasPermi('business:budget:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        CtgLedgerAnnualBudget budget = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetById(id);
        reimbursementService.checkPermisson(budget.getProjectId(), SecurityUtils.getUserId());
        return success(ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetById(id));
    }

    /**
     * 新增项目总预算台账
     */
    @ApiOperation("新增年度预算算台账")
    @PreAuthorize("@ss.hasPermi('business:budget:add')")
    @Log(title = "项目总预算台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("项目总预算台账对象") @RequestBody CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        ctgLedgerAnnualBudgetService.insertCtgLedgerAnnualBudget(ctgLedgerAnnualBudget);
        return success(ctgLedgerAnnualBudget);
    }

    /**
     * 修改项目总预算台账
     */
    @ApiOperation("修改年度预算算台账")
    @PreAuthorize("@ss.hasPermi('business:budget:edit')")
    @Log(title = "项目总预算台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("项目总预算台账对象") @RequestBody CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        CtgLedgerAnnualBudget budget = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetById(ctgLedgerAnnualBudget.getId());
        reimbursementService.checkPermisson(budget.getProjectId(), SecurityUtils.getUserId());
        ctgLedgerAnnualBudgetService.updateCtgLedgerAnnualBudget(ctgLedgerAnnualBudget);
        return success(ctgLedgerAnnualBudget);
    }

    /**
     * 删除项目总预算台账
     */
    @ApiOperation("删除年度预算算台账")
    @ApiImplicitParam(name = "ids", value = "项目总预算台账主键ID数组", required = true, dataType = "Long", paramType = "path")
    @PreAuthorize("@ss.hasPermi('business:budget:remove')")
    @Log(title = "项目总预算台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for(Long id : ids){
            CtgLedgerAnnualBudget ctgLedgerAnnualBudget = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetById(id);
            reimbursementService.checkPermisson(ctgLedgerAnnualBudget.getProjectId(), SecurityUtils.getUserId());
        }


        return toAjax(ctgLedgerAnnualBudgetService.deleteCtgLedgerAnnualBudgetByIds(ids));
    }

    @ApiOperation("导入excel项目台账支出明细Excel")
    @PreAuthorize("@ss.hasPermi('business:budget:import')")
    @Log(title = "导入年度预算台账excel", businessType = BusinessType.DELETE)
    @PostMapping("/importExcelData")
    public AjaxResult importExcelData(MultipartFile file, Long projectId,Long year) throws Exception {
        reimbursementService.checkPermisson(projectId, SecurityUtils.getUserId());
        ExcelUtil<CtgLedgerProjectExpenseDetail> util = new ExcelUtil<>(CtgLedgerProjectExpenseDetail.class);
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = LedgerExcelUtil.pickSheet(workbook);
        if (sheet == null) {
            return AjaxResult.error("没有找到sheet");
        }

        List<CtgLedgerProjectExpenseDetail> list = util.importExcel(sheet.getSheetName(),file.getInputStream(),0);
        projectExpenseDetailService.batchSave(list,projectId,year);
        log.info("成功导入projectId:{},年度：{},{}条数据",projectId,year,list.size());
        return success(list);
    }
    /**
     * 导出项目支出明细台账列表
     */
    @ApiOperation("导出项目支出明细台账列表excel")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "year", value = "年份", dataType = "Integer", paramType = "query")
    })
    @PreAuthorize("@ss.hasPermi('business:expense:export')")
    @Log(title = "导出项目年度支出明细台账excel", businessType = BusinessType.EXPORT)
    @GetMapping("/exportProjectExpenseDetails")
    public void exportProjectExpenseDetails(HttpServletResponse response,
                                            @ApiParam("项目ID") @RequestParam(required = false) Long projectId,
                                            @ApiParam("年份") @RequestParam(required = false) Integer year) {
        try {
            reimbursementService.checkPermisson(projectId,SecurityUtils.getUserId());
            // 查询符合条件的项目支出明细数据
            List<CtgLedgerProjectExpenseDetail> list = projectExpenseDetailService.selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(projectId, year);
            list.stream().forEach(e->e.setRemarkTemp(e.getRemark()));
            // 使用 ExcelUtil 导出数据
            ExcelUtil<CtgLedgerProjectExpenseDetail> util = new ExcelUtil<>(CtgLedgerProjectExpenseDetail.class);
            util.exportExcel(response, list, "项目支出明细台账数据");
        } catch (Exception e) {
            log.error("导出项目支出明细台账失败：{}", e.getMessage(), e);
            throw new RuntimeException("导出项目支出明细台账失败，请稍后重试！");
        }
    }
    /**
     * 根据项目ID查询项目支出明细列表
     */
    @ApiOperation("在智能对话中获取项目信息以及用户基本信息")
    @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, dataType = "Long", paramType = "query")
    @PreAuthorize("@ss.hasPermi('business:expense:list')")
    @GetMapping("/selectProjectExpenseDetail")
    public AjaxResult selectCtgLedgerProjectExpenseDetail(@RequestParam(required = true) Long projectId){
        // 获取当前年份
        Integer year = java.time.LocalDate.now().getYear();
        CtgLedgerProject ctgLedgerProject = ctgLedgerProjectService.selectCtgLedgerProjectById(projectId);
        //检测用户是否是项目成员
        boolean isMember = reimbursementService.isProjectMember(SecurityUtils.getUsername(), ctgLedgerProject);
        boolean isAdmin = permissionService.hasRole(Constants.SUPER_ADMIN);
        if (!isMember && !isAdmin) {
            return AjaxResult.error(HttpStatus.DATA_DUPLICATE, String.format("用户:%s，不是项目：《%s》 成员或者管理员角色，请联系项目管理员添加！", SecurityUtils.getUsername(), ctgLedgerProject.getProjectName()));
        }
        CtgLedgerProjectVo ctgLedgerProjectVo = projectUserService.toCtgLedgerProjectVo(ctgLedgerProject);
        // 根据项目ID和年份查询支出明细列表
        List<CtgLedgerProjectExpenseDetail> list = projectExpenseDetailService.selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(projectId, year);
        Map<String,Object> dataDetail = new HashMap<String,Object>();
        LoginUser user =  SecurityUtils.getLoginUserWithoutEpx();
        dataDetail.put("当前登录用户的基本信息",user);
        dataDetail.put("当前项目的报销数据",list);
        dataDetail.put("当前项目信息",ctgLedgerProjectVo);

        return success(dataDetail);
    }


}
