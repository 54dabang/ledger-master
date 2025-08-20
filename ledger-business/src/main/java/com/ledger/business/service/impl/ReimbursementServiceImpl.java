package com.ledger.business.service.impl;

import com.ledger.business.dto.ClaimantDTO;
import com.ledger.business.dto.ReimbursementDTO;
import com.ledger.business.mapper.CtgLedgerProjectMapper;
import com.ledger.business.mapper.CtgLedgerProjectUserMapper;
import com.ledger.business.mapper.CtgLedgerTotalBudgetMapper;
import com.ledger.business.service.ICtgLedgerTotalBudgetService;
import com.ledger.business.service.IReimbursementService;
import com.ledger.business.util.InitConstant;
import com.ledger.common.core.domain.entity.SysDept;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.system.mapper.SysDeptMapper;
import com.ledger.system.mapper.SysUserMapper;
import com.ledger.system.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ReimbursementServiceImpl implements IReimbursementService {
    @Autowired
    private CtgLedgerTotalBudgetMapper ctgLedgerTotalBudgetMapper;
    @Autowired
    private CtgLedgerProjectUserMapper projectUserMapper;
    @Autowired
    private CtgLedgerProjectMapper ctgLedgerProjectMapper;

    @Autowired
    private ISysDeptService sysDeptService;


    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private SysUserMapper userMapper;

    @Override
    public void syncReimbursementData(ReimbursementDTO reimbursementDTO) {

        createSysUserIfAbsent(reimbursementDTO.getHandler());


    }


    private SysUser createSysUserIfAbsent(ClaimantDTO.UserDetail handler) {
        SysDept currentUserDept = createSysDeptIfAbsent(handler.getDepartment());
        SysUser user = buildByUserDetail(handler);
        SysUser dbUser = userMapper.checkUserNameUnique(user.getUserName());
        if (Objects.isNull(dbUser)) {
            user.setDeptId(currentUserDept.getDeptId());
            userMapper.insertUser(user);
            return user;
        } else {
            return dbUser;
        }

    }

    private SysDept createSysDeptIfAbsent(ClaimantDTO.Department department) {
        SysDept sysDept = buildByDepartment(department);
        if (sysDeptService.checkDeptNameUnique(sysDept)) {
            SysDept parentDept = sysDeptService.selectDeptById(sysDept.getParentId());
            //将祖先信息关联
            String ancestors = StringUtils.join(parentDept.getAncestors(), sysDept.getParentId(), ",");
            sysDept.setAncestors(ancestors);
            sysDeptService.insertDept(sysDept);
            return sysDept;

        } else {
            return deptMapper.checkDeptNameUnique(sysDept.getDeptName(), sysDept.getParentId());
        }
    }

    private SysDept buildByDepartment(ClaimantDTO.Department department) {
        SysDept sysDept = new SysDept();
        sysDept.setDeptName(department.getName());
        sysDept.setCreateBy(SecurityUtils.getUsername());
        sysDept.setUpdateBy(SecurityUtils.getUsername());
        sysDept.setUpdateTime(new Date());
        sysDept.setParentId(InitConstant.SCIENCE_AND_TECHNOLOGY_RESEARCH_INSTITUTE_DEPARTMENT_ID);
        return sysDept;
    }

    private SysUser buildByUserDetail(ClaimantDTO.UserDetail handler) {
        SysUser sysUser = new SysUser();
        sysUser.setNickName(handler.getName());
        sysUser.setUserName(handler.getLoginName());
        //临时密码
        sysUser.setPassword("123");
        sysUser.setSex(handler.getSex());
        sysUser.setCreateBy(SecurityUtils.getUsername());
        sysUser.setUpdateBy(SecurityUtils.getUsername());
        sysUser.setUpdateTime(new Date());
        sysUser.setRemark(InitConstant.USER_REMARK);


        return sysUser;
    }


}
