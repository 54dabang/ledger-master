package com.ledger.business.service.impl;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.dto.ClaimantDTO;
import com.ledger.business.dto.ReimbursementDTO;
import com.ledger.business.mapper.CtgLedgerProjectExpenseDetailMapper;
import com.ledger.business.mapper.CtgLedgerProjectUserMapper;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import com.ledger.business.service.IReimbursementService;
import com.ledger.business.util.InitConstant;
import com.ledger.common.core.domain.entity.SysDept;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.system.mapper.SysDeptMapper;
import com.ledger.system.mapper.SysUserMapper;
import com.ledger.system.service.ISysDeptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void syncReimbursementData(ReimbursementDTO reimbursementDTO, CtgLedgerProject ctgLedgerProject) {

        CtgLedgerProjectExpenseDetail maxSequenceNoReimbursement = projectExpenseDetailMapper
                .selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(ctgLedgerProject.getId());
        Long currentSequenceNo = Optional.ofNullable(maxSequenceNoReimbursement).map(d -> d.getReimbursementSequenceNo()).map(seq -> seq + 1).orElse(1L);

        for (ClaimantDTO claimantDTO : reimbursementDTO.getClaimantList()) {
            //填充信息
            CtgLedgerProjectExpenseDetail expenseDetail = new CtgLedgerProjectExpenseDetail();

            expenseDetail.setExpenseReportNumber(reimbursementDTO.getId());
            expenseDetail.setFeeType(reimbursementDTO.getFeeType());
            expenseDetail.setSubjectName(reimbursementDTO.getSubjectName());
            expenseDetail.setRemark(reimbursementDTO.getTitle() + InitConstant.DATA_RESOURCE);
            expenseDetail.setAmount(reimbursementDTO.getTotalAmount());
            expenseDetail.setLedgerProjectId(ctgLedgerProject.getId());
            expenseDetail.setExpenseReportNumber(reimbursementDTO.getId());
            expenseDetail.setReimbursementSequenceNo(currentSequenceNo);
            expenseDetail.setReimburserName(claimantDTO.getUser().getName());
            expenseDetail.setReimburserLoginName(claimantDTO.getUser().getLoginName());
            expenseDetail.setCreateBy(reimbursementDTO.getHandler().getLoginName());
            expenseDetail.setCreateTime(DateUtils.getNowDate());

            expenseDetailMapper.insertCtgLedgerProjectExpenseDetail(expenseDetail);
        }

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
                return  Pair.of(false, claimantDTO.getUser().getLoginName());
            }
        }
        return Pair.of(true, "");
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

    private void createClaimantUsersIfAbsent(List<ClaimantDTO> claimantList) {
        for (ClaimantDTO claimantDTO : claimantList) {
            createSysUserIfAbsent(claimantDTO.getUser());
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
        sysUser.setPassword(InitConstant.USER_PASSWORD);
        sysUser.setSex(handler.getSex());
        sysUser.setCreateBy(SecurityUtils.getUsername());
        sysUser.setUpdateBy(SecurityUtils.getUsername());
        sysUser.setUpdateTime(new Date());
        sysUser.setRemark(InitConstant.USER_REMARK);


        return sysUser;
    }


}
