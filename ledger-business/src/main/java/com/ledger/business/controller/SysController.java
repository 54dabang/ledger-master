package com.ledger.business.controller;

import com.ledger.common.constant.Constants;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.domain.entity.SysMenu;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.framework.web.service.PermissionService;
import com.ledger.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SysController {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = permissionService.hasRole(Constants.SUPER_ADMIN);
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId,isAdmin);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
