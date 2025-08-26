package com.ledger.business.controller;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.service.ICtgLedgerProjectService;
import com.ledger.common.constant.Constants;
import com.ledger.common.constant.HttpStatus;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.framework.web.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
@Api(tags = "项目用户管理")
@RestController
@RequestMapping("/api/projectUser")
public class CtgLedgerProjectUserController extends BaseController {
    @Autowired
    private ICtgLedgerProjectUserService ctgLedgerProjectUserService;
    @Autowired
    private ICtgLedgerProjectService projectService;
    @Autowired
    private PermissionService permissionService;

    /**
     * 查询项目用户列表
     */
    @ApiOperation("查询项目用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ctgLedgerProjectId", value = "项目ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "ctgLedgerUserId", value = "用户ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "projectUserNickname", value = "项目用户昵称", dataType = "String", paramType = "query")
    })
    @PreAuthorize("@ss.hasPermi('business:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(CtgLedgerProjectUser ctgLedgerProjectUser) {
        startPage();
        List<CtgLedgerProjectUser> list = ctgLedgerProjectUserService.selectCtgLedgerProjectUserList(ctgLedgerProjectUser);
        return getDataTable(list);
    }

    /**
     * 导出项目用户列表
     */
    @ApiOperation("导出项目用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ctgLedgerProjectId", value = "项目ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "ctgLedgerUserId", value = "用户ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "projectUserNickname", value = "项目用户昵称", dataType = "String", paramType = "query")
    })
    @PreAuthorize("@ss.hasPermi('business:user:export')")
    @Log(title = "项目用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CtgLedgerProjectUser ctgLedgerProjectUser) {
        List<CtgLedgerProjectUser> list = ctgLedgerProjectUserService.selectCtgLedgerProjectUserList(ctgLedgerProjectUser);
        ExcelUtil<CtgLedgerProjectUser> util = new ExcelUtil<CtgLedgerProjectUser>(CtgLedgerProjectUser.class);
        util.exportExcel(response, list, "项目用户数据");
    }

    /**
     * 获取项目用户详细信息
     */
    @ApiOperation("获取项目用户详细信息")
    @ApiImplicitParam(name = "id", value = "项目用户主键ID", required = true, dataType = "Long", paramType = "path")
    @PreAuthorize("@ss.hasPermi('business:user:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(ctgLedgerProjectUserService.selectCtgLedgerProjectUserById(id));
    }

    /**
     * 新增项目用户
     */
    @ApiOperation("新增项目用户")
    @PreAuthorize("@ss.hasPermi('business:user:add')")
    @Log(title = "添加项目用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult batchAdd(@ApiParam("项目用户对象列表") @RequestBody List<CtgLedgerProjectUser> ctgLedgerProjectUsers) {
        if (CollectionUtils.isEmpty(ctgLedgerProjectUsers)) {
            AjaxResult.error(HttpStatus.BAD_REQUEST, String.format("数据不能为空！"));
        }
        CtgLedgerProjectUser ctgLedgerProjectUser = ctgLedgerProjectUsers.get(0);
        Long projectId = ctgLedgerProjectUser.getCtgLedgerProjectId();
        boolean isProjectManager = ctgLedgerProjectUserService.isProjectManager(projectId, SecurityUtils.getUsername());
        boolean isAdmin = permissionService.hasRole(Constants.SUPER_ADMIN);
        if (!isProjectManager && !isAdmin) {
            AjaxResult.error(HttpStatus.UNAUTHORIZED, String.format("只有项目管理员或系统管理员可添加用户！"));
        }
        List<CtgLedgerProjectUser> projectUsers = ctgLedgerProjectUserService.batchInsertCtgLedgerProjectUser(ctgLedgerProjectUsers);
        return success(projectUsers);
    }

    /**
     * 修改项目用户
     */
    @ApiOperation("修改项目用户")
    @PreAuthorize("@ss.hasPermi('business:user:edit')")
    @Log(title = "项目用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("项目用户对象") @RequestBody CtgLedgerProjectUser ctgLedgerProjectUser) {
        return toAjax(ctgLedgerProjectUserService.updateCtgLedgerProjectUser(ctgLedgerProjectUser));
    }

    /**
     * 删除项目用户
     */
    @ApiOperation("删除项目用户")
    @ApiImplicitParam(name = "ids", value = "项目用户主键ID数组", required = true, dataType = "Long", paramType = "path")
    @PreAuthorize("@ss.hasPermi('business:user:remove')")
    @Log(title = "项目用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ctgLedgerProjectUserService.deleteCtgLedgerProjectUserByIds(ids));
    }
}
