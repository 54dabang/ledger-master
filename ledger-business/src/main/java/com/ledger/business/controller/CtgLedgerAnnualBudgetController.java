package com.ledger.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
@RestController
@RequestMapping("/api/annualBudget")
public class CtgLedgerAnnualBudgetController extends BaseController
{
    @Autowired
    private ICtgLedgerAnnualBudgetService ctgLedgerAnnualBudgetService;

    /**
     * 查询项目总预算台账列表
     */
    @PreAuthorize("@ss.hasPermi('business:budget:list')")
    @GetMapping("/list")
    public TableDataInfo list(CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        startPage();
        List<CtgLedgerAnnualBudget> list = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetList(ctgLedgerAnnualBudget);
        return getDataTable(list);
    }

    /**
     * 导出项目总预算台账列表
     */
    @PreAuthorize("@ss.hasPermi('business:budget:export')")
    @Log(title = "项目总预算台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        List<CtgLedgerAnnualBudget> list = ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetList(ctgLedgerAnnualBudget);
        ExcelUtil<CtgLedgerAnnualBudget> util = new ExcelUtil<CtgLedgerAnnualBudget>(CtgLedgerAnnualBudget.class);
        util.exportExcel(response, list, "项目总预算台账数据");
    }

    /**
     * 获取项目总预算台账详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:budget:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ctgLedgerAnnualBudgetService.selectCtgLedgerAnnualBudgetById(id));
    }

    /**
     * 新增项目总预算台账
     */
    @PreAuthorize("@ss.hasPermi('business:budget:add')")
    @Log(title = "项目总预算台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        return toAjax(ctgLedgerAnnualBudgetService.insertCtgLedgerAnnualBudget(ctgLedgerAnnualBudget));
    }

    /**
     * 修改项目总预算台账
     */
    @PreAuthorize("@ss.hasPermi('business:budget:edit')")
    @Log(title = "项目总预算台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        return toAjax(ctgLedgerAnnualBudgetService.updateCtgLedgerAnnualBudget(ctgLedgerAnnualBudget));
    }

    /**
     * 删除项目总预算台账
     */
    @PreAuthorize("@ss.hasPermi('business:budget:remove')")
    @Log(title = "项目总预算台账", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ctgLedgerAnnualBudgetService.deleteCtgLedgerAnnualBudgetByIds(ids));
    }
}
