package com.ledger.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.service.ICtgLedgerProjectService;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.common.core.page.TableDataInfo;

/**
 * 项目管理Controller
 * 
 * @author ledger
 * @date 2025-08-21
 */
@RestController
@RequestMapping("/api/project")
@Api(value = "台账接口", tags = {"项目信息维护"})
public class CtgLedgerProjectController extends BaseController
{
    @Autowired
    private ICtgLedgerProjectService ctgLedgerProjectService;

    /**
     * 查询项目管理列表
     */
   // @PreAuthorize("@ss.hasPermi('business:project:list')")
    @GetMapping("/list")
    @ApiOperation("项目基本信息列表")
    public TableDataInfo list(CtgLedgerProject ctgLedgerProject)
    {
        startPage();
        List<CtgLedgerProject> list = ctgLedgerProjectService.selectCtgLedgerProjectList(ctgLedgerProject);
        return getDataTable(list);
    }

    /**
     * 导出项目管理列表
     */
  //  @PreAuthorize("@ss.hasPermi('business:project:export')")
    @Log(title = "项目管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出项目excel")
    public void export(HttpServletResponse response, CtgLedgerProject ctgLedgerProject)
    {
        List<CtgLedgerProject> list = ctgLedgerProjectService.selectCtgLedgerProjectList(ctgLedgerProject);
        ExcelUtil<CtgLedgerProject> util = new ExcelUtil<CtgLedgerProject>(CtgLedgerProject.class);
        util.exportExcel(response, list, "项目管理数据");
    }

    /**
     * 获取项目管理详细信息
     */
 //   @PreAuthorize("@ss.hasPermi('business:project:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取项目基本信息")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ctgLedgerProjectService.selectCtgLedgerProjectById(id));
    }

    /**
     * 新增项目管理
     */
   // @PreAuthorize("@ss.hasPermi('business:project:add')")
    @Log(title = "项目管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增项目")
    public AjaxResult add(@RequestBody CtgLedgerProject ctgLedgerProject)
    {
        return toAjax(ctgLedgerProjectService.insertCtgLedgerProject(ctgLedgerProject));
    }

    /**
     * 修改项目管理
     */
  //  @PreAuthorize("@ss.hasPermi('business:project:edit')")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("编辑项目")
    public AjaxResult edit(@RequestBody CtgLedgerProject ctgLedgerProject)
    {
        return toAjax(ctgLedgerProjectService.updateCtgLedgerProject(ctgLedgerProject));
    }

    /**
     * 删除项目管理
     */
 //   @PreAuthorize("@ss.hasPermi('business:project:remove')")
    @Log(title = "项目管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    @ApiOperation("删除项目")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ctgLedgerProjectService.deleteCtgLedgerProjectByIds(ids));
    }
}
