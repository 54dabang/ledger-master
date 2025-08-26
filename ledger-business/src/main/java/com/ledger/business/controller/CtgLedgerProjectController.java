package com.ledger.business.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ledger.business.domain.CtgLedgerProjectUser;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import com.ledger.business.util.InitConstant;
import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
public class CtgLedgerProjectController extends BaseController {
    @Autowired
    private ICtgLedgerProjectService ctgLedgerProjectService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ICtgLedgerProjectUserService projectUserService;

    /**
     * 查询项目管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:project:list')")
    @GetMapping("/list")
    @ApiOperation(value = "项目基本信息列表", notes = "根据条件查询项目基本信息列表，支持分页和条件查询")
    public TableDataInfo list(CtgLedgerProject ctgLedgerProject, @ApiParam(value = "查询类型: manage-我管理的项目, participate-我参与的项目") @RequestParam(value = "type", required = false) String type) {
        if (InitConstant.PROJECT_QUERY_TYPE_MANAGE.equals(type)) {
            ctgLedgerProject.setProjectManagerLoginName(SecurityUtils.getUsername());
            startPage();
            List<CtgLedgerProject> list = ctgLedgerProjectService.selectCtgLedgerProjectList(ctgLedgerProject);
            List<CtgLedgerProjectVo> projectVoList = list.stream().map(p->projectUserService.toCtgLedgerProjectVo(p)).collect(Collectors.toList());
            return getDataTable(projectVoList);
        } else  {
            Long userId = userService.selectUserByUserName(SecurityUtils.getUsername()).getUserId();
            CtgLedgerProjectUser ctgLedgerProjectUser = new CtgLedgerProjectUser();
            ctgLedgerProjectUser.setSysUserId(userId);
            List<Long> projectIdList = projectUserService.selectCtgLedgerProjectUserList(ctgLedgerProjectUser).stream().map(c -> c.getCtgLedgerProjectId()).collect(Collectors.toList());
            //携带条件查询在projectIdList中，且符合查询条件ctgLedgerProject的项目
            if (projectIdList.isEmpty()) {
                // 如果用户没有参与任何项目，返回空列表
                return getDataTable(new ArrayList<>());
            }
            // 使用params参数传递ID列表
            ctgLedgerProject.getParams().put("ids", projectIdList);
            startPage();
            List<CtgLedgerProject> list = ctgLedgerProjectService.selectCtgLedgerProjectList(ctgLedgerProject);
            List<CtgLedgerProjectVo> projectVoList = list.stream().map(p->projectUserService.toCtgLedgerProjectVo(p)).collect(Collectors.toList());
            return getDataTable(projectVoList);

        }

    }
    @GetMapping("/listAll")
    @ApiOperation(value = "项目的所有列表信息", notes = "根据条件查询项目基本信息列表，支持分页和条件查询（只有管理员有权限）")
    @PreAuthorize("@ss.hasRole('admin')")
    public TableDataInfo listAll(CtgLedgerProject ctgLedgerProject){
        startPage();
        List<CtgLedgerProject> list = ctgLedgerProjectService.selectCtgLedgerProjectList(ctgLedgerProject);
        return getDataTable(list);
    }



    /**
     * 导出项目管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:project:export')")
    @Log(title = "项目管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出项目excel")
    public void export(HttpServletResponse response, CtgLedgerProject ctgLedgerProject) {
        List<CtgLedgerProject> list = ctgLedgerProjectService.selectCtgLedgerProjectList(ctgLedgerProject);
        ExcelUtil<CtgLedgerProject> util = new ExcelUtil<CtgLedgerProject>(CtgLedgerProject.class);
        util.exportExcel(response, list, "项目管理数据");
    }

    /**
     * 获取项目管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:project:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取项目基本信息")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(ctgLedgerProjectService.selectCtgLedgerProjectById(id));
    }

    /**
     * 新增项目管理
     */
    @PreAuthorize("@ss.hasPermi('business:project:add')")
    @Log(title = "项目管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增项目")
    public AjaxResult add(@RequestBody CtgLedgerProject ctgLedgerProject) {
        return success(ctgLedgerProjectService.insertCtgLedgerProject(ctgLedgerProject));
    }

    /**
     * 修改项目管理
     */
    @PreAuthorize("@ss.hasPermi('business:project:edit')")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("编辑项目")
    public AjaxResult edit(@RequestBody CtgLedgerProject ctgLedgerProject) {
        return toAjax(ctgLedgerProjectService.updateCtgLedgerProject(ctgLedgerProject));
    }

    /**
     * 删除项目管理
     */
    @PreAuthorize("@ss.hasPermi('business:project:remove')")
    @Log(title = "项目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ApiOperation("删除项目")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ctgLedgerProjectService.deleteCtgLedgerProjectByIds(ids));
    }
}
