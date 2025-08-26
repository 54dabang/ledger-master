package com.ledger.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.enums.BusinessType;
import com.ledger.business.domain.CtgLedgerAnnualBudget;
import com.ledger.business.service.ICtgLedgerAnnualBudgetService;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.common.core.page.TableDataInfo;

/**
 * 项目总预算台账Controller
 *
 * @author ledger
 * @date 2025-08-20
 */
@Api(tags = "项目总预算台账管理")
@RestController
@RequestMapping("/api/annualBudget")
public class CtgLedgerAnnualBudgetController extends BaseController {
    @Autowired
    private ICtgLedgerAnnualBudgetService ctgLedgerAnnualBudgetService;

    /**
     * 查询项目总预算台账列表
     */
    @ApiOperation("查询项目总预算台账列表")
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
    @ApiOperation("导出项目总预算台账列表")
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
    @Log(title = "项目总预算台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        List<CtgLedgerAnnualBudget> list = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetList(ctgLedgerAnnualBudget);
        ExcelUtil<CtgLedgerAnnualBudget> util = new ExcelUtil<CtgLedgerAnnualBudget>(CtgLedgerAnnualBudget.class);
        util.exportExcel(response, list, "项目总预算台账数据");
    }

    /**
     * 获取项目总预算台账详细信息
     */
    @ApiOperation("获取项目总预算台账详细信息")
    @ApiImplicitParam(name = "id", value = "项目总预算台账主键ID", required = true, dataType = "Long", paramType = "path")
    @PreAuthorize("@ss.hasPermi('business:budget:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetById(id));
    }

    /**
     * 新增项目总预算台账
     */
    @ApiOperation("新增项目总预算台账")
    @PreAuthorize("@ss.hasPermi('business:budget:add')")
    @Log(title = "项目总预算台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("项目总预算台账对象") @RequestBody CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        return toAjax(ctgLedgerAnnualBudgetService.insertCtgLedgerAnnualBudget(ctgLedgerAnnualBudget));
    }

    /**
     * 修改项目总预算台账
     */
    @ApiOperation("修改项目总预算台账")
    @PreAuthorize("@ss.hasPermi('business:budget:edit')")
    @Log(title = "项目总预算台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("项目总预算台账对象") @RequestBody CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        return toAjax(ctgLedgerAnnualBudgetService.updateCtgLedgerAnnualBudget(ctgLedgerAnnualBudget));
    }

    /**
     * 删除项目总预算台账
     */
    @ApiOperation("删除项目总预算台账")
    @ApiImplicitParam(name = "ids", value = "项目总预算台账主键ID数组", required = true, dataType = "Long", paramType = "path")
    @PreAuthorize("@ss.hasPermi('business:budget:remove')")
    @Log(title = "项目总预算台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ctgLedgerAnnualBudgetService.deleteCtgLedgerAnnualBudgetByIds(ids));
    }
}
