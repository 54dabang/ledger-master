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
import com.ledger.business.domain.CtgLedgerProjectUser;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.common.core.page.TableDataInfo;

/**
 * 项目用户Controller
 * 
 * @author ledger
 * @date 2025-08-20
 */
@RestController
@RequestMapping("/api/projectUser")
public class CtgLedgerProjectUserController extends BaseController
{
    @Autowired
    private ICtgLedgerProjectUserService ctgLedgerProjectUserService;

    /**
     * 查询项目用户列表
     */
    @PreAuthorize("@ss.hasPermi('business:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(CtgLedgerProjectUser ctgLedgerProjectUser)
    {
        startPage();
        List<CtgLedgerProjectUser> list = ctgLedgerProjectUserService.selectCtgLedgerProjectUserList(ctgLedgerProjectUser);
        return getDataTable(list);
    }

    /**
     * 导出项目用户列表
     */
    @PreAuthorize("@ss.hasPermi('business:user:export')")
    @Log(title = "项目用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CtgLedgerProjectUser ctgLedgerProjectUser)
    {
        List<CtgLedgerProjectUser> list = ctgLedgerProjectUserService.selectCtgLedgerProjectUserList(ctgLedgerProjectUser);
        ExcelUtil<CtgLedgerProjectUser> util = new ExcelUtil<CtgLedgerProjectUser>(CtgLedgerProjectUser.class);
        util.exportExcel(response, list, "项目用户数据");
    }

    /**
     * 获取项目用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:user:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ctgLedgerProjectUserService.selectCtgLedgerProjectUserById(id));
    }

    /**
     * 新增项目用户
     */
    @PreAuthorize("@ss.hasPermi('business:user:add')")
    @Log(title = "项目用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CtgLedgerProjectUser ctgLedgerProjectUser)
    {
        return toAjax(ctgLedgerProjectUserService.insertCtgLedgerProjectUser(ctgLedgerProjectUser));
    }

    /**
     * 修改项目用户
     */
    @PreAuthorize("@ss.hasPermi('business:user:edit')")
    @Log(title = "项目用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CtgLedgerProjectUser ctgLedgerProjectUser)
    {
        return toAjax(ctgLedgerProjectUserService.updateCtgLedgerProjectUser(ctgLedgerProjectUser));
    }

    /**
     * 删除项目用户
     */
    @PreAuthorize("@ss.hasPermi('business:user:remove')")
    @Log(title = "项目用户", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ctgLedgerProjectUserService.deleteCtgLedgerProjectUserByIds(ids));
    }
}
