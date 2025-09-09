package com.ledger.system;


import com.ledger.common.core.domain.entity.SysRole;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.utils.bean.ApplicationContextUtil;
import com.ledger.system.service.ISysRoleService;

import java.util.List;

public class AdminService {
    public static boolean isAdmin(Long userId) {
        ISysRoleService sysRoleService = ApplicationContextUtil.getBean(ISysRoleService.class);
        List<SysRole> roleList = sysRoleService.selectRolesByUserIdWithoutProxy(userId);
        boolean isAdminRole = roleList.stream().map(r -> r.getRoleKey()).anyMatch(key -> key.equals("admin"));
        return SysUser.isAdmin(userId) || isAdminRole;
    }
}
