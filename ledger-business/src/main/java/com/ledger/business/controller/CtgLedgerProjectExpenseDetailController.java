package com.ledger.business.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.service.ICtgLedgerProjectService;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import com.ledger.business.util.StrUtil;
import com.ledger.business.vo.CtgLedgerProjectExpenseDetailVo;
import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.business.vo.SysUserVo;
import com.ledger.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.enums.BusinessType;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.service.ICtgLedgerProjectExpenseDetailService;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.common.core.page.TableDataInfo;

/**
 * 项目支出明细Controller
 *
 * @author ledger
 * @date 2025-08-21
 */
@RestController
@RequestMapping("/api/expenseDetail")
@Api(tags = "台账明细管理")
public class CtgLedgerProjectExpenseDetailController extends BaseController {
    @Autowired
    private ICtgLedgerProjectExpenseDetailService ctgLedgerProjectExpenseDetailService;
    @Autowired
    private ICtgLedgerProjectUserService projectUserService;
    @Autowired
    private ICtgLedgerProjectService projectService;

    /**
     * 查询项目支出明细列表
     */
    @PreAuthorize("@ss.hasPermi('business:detail:list')")
    @GetMapping("/list")
    @ApiOperation("项目支出明细列表")
    public TableDataInfo list(@ApiParam("项目ID") @RequestParam(required = false) Long projectId,
                              @ApiParam("年份") @RequestParam(required = false) Integer year) {
        startPage();
        CtgLedgerProjectExpenseDetail detailParam = new CtgLedgerProjectExpenseDetail();
        detailParam.setLedgerProjectId(projectId);
        detailParam.setYear(year);
        CtgLedgerProject project = projectService.selectCtgLedgerProjectById(projectId);
        CtgLedgerProjectVo ctgLedgerProjectVo =  projectUserService.toCtgLedgerProjectVo(project);
        List<CtgLedgerProjectExpenseDetail> list = ctgLedgerProjectExpenseDetailService.selectCtgLedgerProjectExpenseDetailList(detailParam);
        List<CtgLedgerProjectExpenseDetailVo> lst = list.stream().map(e->toExpenseDetailVo(e,ctgLedgerProjectVo)).collect(Collectors.toList());
        List<SysUserVo> members = new ArrayList<>();
        members.addAll(ctgLedgerProjectVo.getMembers());
        members.add(ctgLedgerProjectVo.getManager());
        members.add(ctgLedgerProjectVo.getContact());
        List<SysUserVo> distinctMembers = members.stream()
                .filter(Objects::nonNull)          // 如果集合里可能有 null，先过滤
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                SysUserVo::getUserId,   // key
                                v -> v,                 // value
                                (v1, v2) -> v1),        // 重复 key 时保留第一个
                        map -> new ArrayList<>(map.values())));

        return getDataTable(lst,"members",distinctMembers);
    }

    private CtgLedgerProjectExpenseDetailVo toExpenseDetailVo(CtgLedgerProjectExpenseDetail expenseDetail, CtgLedgerProjectVo ctgLedgerProjectVo) {
        CtgLedgerProjectExpenseDetailVo detailVo = new CtgLedgerProjectExpenseDetailVo();
        BeanUtils.copyProperties(expenseDetail, detailVo);
        detailVo.setContact(ctgLedgerProjectVo.getContact());
        detailVo.setManager(ctgLedgerProjectVo.getManager());
        return detailVo;

    }



    @PreAuthorize("@ss.hasPermi('business:detail:list')")
    @GetMapping("/getAllDbYears")
    @ApiOperation("获取数据库中已经导入数据的年份")
    public AjaxResult getAllDbYears(@ApiParam("项目ID") @RequestParam(required = false) Long projectId){
        CtgLedgerProjectExpenseDetail detailParam = new CtgLedgerProjectExpenseDetail();
        detailParam.setLedgerProjectId(projectId);
        List<CtgLedgerProjectExpenseDetail> list = ctgLedgerProjectExpenseDetailService.selectCtgLedgerProjectExpenseDetailList(detailParam);
        List<Integer> years = list.stream().map(e->e.getYear()).distinct().collect(Collectors.toList());
        return success(years);
    }


    /**
     * 导出项目支出明细列表
     */
    @PreAuthorize("@ss.hasPermi('business:detail:export')")
    @Log(title = "导出项目支出明细excel", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail) {
        List<CtgLedgerProjectExpenseDetail> list = ctgLedgerProjectExpenseDetailService.selectCtgLedgerProjectExpenseDetailList(ctgLedgerProjectExpenseDetail);
        ExcelUtil<CtgLedgerProjectExpenseDetail> util = new ExcelUtil<CtgLedgerProjectExpenseDetail>(CtgLedgerProjectExpenseDetail.class);
        util.exportExcel(response, list, "项目支出明细数据");
    }

    /**
     * 获取项目支出明细详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:detail:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取项目支持明细详情")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(ctgLedgerProjectExpenseDetailService.selectCtgLedgerProjectExpenseDetailById(id));
    }

    /**
     * 新增项目支出明细
     */
    @PreAuthorize("@ss.hasPermi('business:detail:add')")
    @ApiOperation("新增项目支持明细详情")
    @Log(title = "新增项目支持明细详情", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail) {
        int count = 0;
        try {
            //ctgLedgerProjectExpenseDetail.setRemark(StringUtils.isEmpty(ctgLedgerProjectExpenseDetail.getRemarkTemp()) ? StrUtil.buildRemark(ctgLedgerProjectExpenseDetail) : ctgLedgerProjectExpenseDetail.getRemarkTemp());
            count = ctgLedgerProjectExpenseDetailService.insertCtgLedgerProjectExpenseDetail(ctgLedgerProjectExpenseDetail);
        } catch (Exception ex) {
            for (Throwable cur = ex; cur != null; cur = cur.getCause()) {
                if (cur instanceof SQLIntegrityConstraintViolationException) {
                    return error("报销单号重复！NO:" + ctgLedgerProjectExpenseDetail.getExpenseReportNumber());
                }
            }
            throw ex;
        }
        return toAjax(count);
    }

    /**
     * 修改项目支出明细
     */
    @PreAuthorize("@ss.hasPermi('business:detail:edit')")
    @ApiOperation("修改项目支出明细")
    @Log(title = "修改项目支出明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail) {
        return toAjax(ctgLedgerProjectExpenseDetailService.updateCtgLedgerProjectExpenseDetail(ctgLedgerProjectExpenseDetail));
    }

    /**
     * 删除项目支出明细
     */
    @PreAuthorize("@ss.hasPermi('business:detail:remove')")
    @ApiOperation("删除项目支出明细")
    @Log(title = "删除项目支出明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ctgLedgerProjectExpenseDetailService.deleteCtgLedgerProjectExpenseDetailByIds(ids));
    }
}
