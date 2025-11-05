package com.ledger.business.service.impl;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.dto.ClaimantDTO;
import com.ledger.business.dto.ReimbursementDTO;
import com.ledger.business.mapper.CtgLedgerProjectExpenseDetailMapper;
import com.ledger.business.mapper.CtgLedgerProjectMapper;
import com.ledger.business.mapper.CtgLedgerProjectUserMapper;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import com.ledger.business.service.IReimbursementService;
import com.ledger.business.util.InitConstant;
import com.ledger.business.util.StrUtil;
import com.ledger.common.core.domain.entity.SysDept;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.framework.web.service.PermissionService;
import com.ledger.system.mapper.SysDeptMapper;
import com.ledger.system.mapper.SysUserMapper;
import com.ledger.system.service.ISysDeptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ReimbursementServiceImpl implements IReimbursementService {


    @Autowired
    private ICtgLedgerProjectUserService projectUserService;

    @Autowired
    private CtgLedgerProjectExpenseDetailMapper expenseDetailMapper;
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private CtgLedgerProjectExpenseDetailMapper projectExpenseDetailMapper;
    @Autowired
    private CtgLedgerProjectMapper projectMapper;
    @Autowired
    private PermissionService permissionService;


    @Override
    public Long syncReimbursementData(ReimbursementDTO reimbursementDTO, CtgLedgerProject ctgLedgerProject) {

        CtgLedgerProjectExpenseDetail maxSequenceNoReimbursement = projectExpenseDetailMapper
                .selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(ctgLedgerProject.getId());

        CtgLedgerProjectExpenseDetail expenseDetail = projectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(reimbursementDTO.getBillCode());
        Long currentSequenceNo = null;
        boolean isNewData = false;
        if (Objects.isNull(expenseDetail)) {
            expenseDetail = new CtgLedgerProjectExpenseDetail();
            currentSequenceNo = Optional.ofNullable(maxSequenceNoReimbursement).map(d -> d.getReimbursementSequenceNo()).map(seq -> seq + 1).orElse(1L);
            expenseDetail.setReimbursementSequenceNo(currentSequenceNo);
            isNewData = true;
        } else {
            currentSequenceNo = expenseDetail.getReimbursementSequenceNo();
        }

        //设置同步属性值
        expenseDetail.setYear(reimbursementDTO.getCreateTime().getYear() + 1900);
        expenseDetail.setExpenseReportNumber(reimbursementDTO.getBillCode());
        expenseDetail.setFeeType(reimbursementDTO.getFeeType());
        expenseDetail.setSubjectName(reimbursementDTO.getSubjectName());
        expenseDetail.setRemark(StrUtil.buildRemark(expenseDetail));
        expenseDetail.setAmount(reimbursementDTO.getTotalAmount());
        expenseDetail.setLedgerProjectId(ctgLedgerProject.getId());
        expenseDetail.setReimburserName(reimbursementDTO.getHandler().getName());
        expenseDetail.setReimburserLoginName(reimbursementDTO.getHandler().getLoginName());
        expenseDetail.setCreateBy(reimbursementDTO.getHandler().getLoginName());
        expenseDetail.setCreateTime(DateUtils.getNowDate());

        //新数据插入，否则执行更新
        if (isNewData) {
            expenseDetailMapper.insertCtgLedgerProjectExpenseDetail(expenseDetail);
        } else {
            expenseDetailMapper.updateCtgLedgerProjectExpenseDetail(expenseDetail);
        }

        return currentSequenceNo;
    }

    @Override
    public void syncUsersReimbursementData(ReimbursementDTO reimbursementDTO) {
        //创建操作人
        createSysUserIfAbsent(reimbursementDTO.getHandler());
        //创建报销人
        createClaimantUsersIfAbsent(reimbursementDTO.getClaimantList());
    }

    @Override
    public Pair<Boolean, String> isClaimantsProjectMember(ReimbursementDTO reimbursementDTO, CtgLedgerProject ctgLedgerProject) {
        for (ClaimantDTO claimantDTO : reimbursementDTO.getClaimantList()) {
            SysUser user = userMapper.selectUserByUserName(claimantDTO.getUser().getLoginName());
            boolean isMember = projectUserService.isProjectUser(ctgLedgerProject.getId(), user.getUserId());
            boolean isProjectManager = claimantDTO.getUser().getLoginName().equals(ctgLedgerProject.getProjectManagerLoginName());
            if (!isMember && !isProjectManager) {
                return Pair.of(false, claimantDTO.getUser().getLoginName());
            }
        }
        return Pair.of(true, "");
    }

    @Override
    public boolean isHandlerProjectMember(ReimbursementDTO reimbursementDTO, CtgLedgerProject ctgLedgerProject) {

        return isProjectMember(reimbursementDTO.getHandler().getLoginName(), ctgLedgerProject);
    }

    @Override
    public boolean isProjectMember(String loginName, CtgLedgerProject ctgLedgerProject) {
        // 复用已有逻辑：检查用户是否为项目成员或项目经理
        SysUser user = userMapper.selectUserByUserName(loginName);
        if (user == null) {
            return false;
        }
        boolean isMember = projectUserService.isProjectUser(ctgLedgerProject.getId(), user.getUserId());
        boolean isProjectManager = loginName.trim().equals(ctgLedgerProject.getProjectManagerLoginName().trim());
        return isMember || isProjectManager;
    }


    @Override
    public boolean hasPermission(Long projectId, Long userId) {
        SysUser user = userMapper.selectUserById(userId);
        CtgLedgerProject project = projectMapper.selectCtgLedgerProjectById(projectId);
        boolean isMember = projectUserService.isProjectUser(projectId, userId);
        boolean isProjectManager = user.getUserName().equals(project.getProjectManagerLoginName());
        if (!isMember && !isProjectManager) {
            return false;
        }
        return true;
    }

    @Override
    public void checkPermisson(Long projectId, Long userId) {
        if (permissionService.hasRole("admin")) {
            return;
        }
        if (!hasPermission(projectId, userId)) {
            throw new PermissionDeniedDataAccessException(String.format("您没有项目:%s对应的权限", projectId), null);
        }
    }

    private SysUser createSysUserIfAbsent(ClaimantDTO.UserDetail handler) {
        if (Objects.isNull(handler)) {
            return null;
        }
        SysDept currentUserDept = createSysDeptIfAbsent(handler.getDepartment(), handler.getLoginName());
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

    private void createClaimantUsersIfAbsent(List<ClaimantDTO> claimantList) {
        if (CollectionUtils.isEmpty(claimantList)) {
            return;
        }
        for (ClaimantDTO claimantDTO : claimantList) {
            createSysUserIfAbsent(claimantDTO.getUser());
        }
    }

    private SysDept createSysDeptIfAbsent(ClaimantDTO.Department department, String userName) {
        SysDept sysDept = buildByDepartment(department, userName);
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

    private SysDept buildByDepartment(ClaimantDTO.Department department, String userName) {
        SysDept sysDept = new SysDept();
        sysDept.setDeptName(department.getName());
        sysDept.setCreateBy(userName);
        sysDept.setUpdateBy(userName);
        sysDept.setUpdateTime(new Date());
        sysDept.setParentId(InitConstant.SCIENCE_AND_TECHNOLOGY_RESEARCH_INSTITUTE_DEPARTMENT_ID);
        return sysDept;
    }

    private SysUser buildByUserDetail(ClaimantDTO.UserDetail handler) {
        SysUser sysUser = new SysUser();
        sysUser.setNickName(handler.getName());
        sysUser.setUserName(handler.getLoginName());
        //临时密码
        sysUser.setPassword(InitConstant.USER_PASSWORD);
        sysUser.setSex(handler.getSex());
        sysUser.setCreateBy(handler.getLoginName());
        sysUser.setUpdateBy(handler.getLoginName());
        sysUser.setUpdateTime(new Date());
        sysUser.setRemark(InitConstant.USER_REMARK);


        return sysUser;
    }


}
