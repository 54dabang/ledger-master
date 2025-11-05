package com.ledger.business.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.enums.BusinessType;
import com.ledger.business.domain.CtgLedgerConfig;
import com.ledger.business.service.ICtgLedgerConfigService;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.common.core.page.TableDataInfo;

/**
 * 台账配置Controller
 *
 * @author ledger
 * @date 2025-11-05
 */
@RestController
@RequestMapping("/api/config")
@Api(value = "台账配置接口", tags = {"台账配置管理"})
public class CtgLedgerConfigController extends BaseController
{
    @Autowired
    private ICtgLedgerConfigService ctgLedgerConfigService;

    /**
     * 查询台账配置列表
     */
    @PreAuthorize("@ss.hasPermi('business:config:list')")
    @GetMapping("/list")
    @ApiOperation("查询台账配置列表")
    public TableDataInfo list(CtgLedgerConfig ctgLedgerConfig)
    {
        startPage();
        List<CtgLedgerConfig> list = ctgLedgerConfigService.selectCtgLedgerConfigList(ctgLedgerConfig);
        return getDataTable(list);
    }

    /**
     * 导出台账配置列表
     */
    @PreAuthorize("@ss.hasPermi('business:config:export')")
    @Log(title = "台账配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出台账配置")
    public void export(@RequestBody CtgLedgerConfig ctgLedgerConfig, HttpServletResponse response)
    {
        List<CtgLedgerConfig> list = ctgLedgerConfigService.selectCtgLedgerConfigList(ctgLedgerConfig);
        ExcelUtil<CtgLedgerConfig> util = new ExcelUtil<CtgLedgerConfig>(CtgLedgerConfig.class);
        util.exportExcel(response, list, "台账配置数据");
    }

    /**
     * 获取台账配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:config:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取台账配置详情")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ctgLedgerConfigService.selectCtgLedgerConfigById(id));
    }

    /**
     * 新增台账配置
     */
    @PreAuthorize("@ss.hasPermi('business:config:add')")
    @Log(title = "台账配置", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增台账配置")
    public AjaxResult add(@RequestBody CtgLedgerConfig ctgLedgerConfig)
    {
        return toAjax(ctgLedgerConfigService.insertCtgLedgerConfig(ctgLedgerConfig));
    }

    /**
     * 修改台账配置
     */
    @PreAuthorize("@ss.hasPermi('business:config:edit')")
    @Log(title = "台账配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("修改台账配置")
    public AjaxResult edit(@RequestBody CtgLedgerConfig ctgLedgerConfig)
    {
        return toAjax(ctgLedgerConfigService.updateCtgLedgerConfig(ctgLedgerConfig));
    }

    /**
     * 删除台账配置
     */
    @PreAuthorize("@ss.hasPermi('business:config:remove')")
    @Log(title = "台账配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ApiOperation("删除台账配置")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ctgLedgerConfigService.deleteCtgLedgerConfigByIds(ids));
    }
}