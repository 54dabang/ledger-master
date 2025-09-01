package com.ledger.business.service.impl;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledger.business.domain.CtgLedgerAnnualBudget;
import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.service.ICtgLedgerAnnualBudgetService;
import com.ledger.business.service.ICtgLedgerProjectService;
import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.business.vo.SysUserVo;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.spring.SpringUtils;
import com.ledger.system.service.ISysUserService;
import com.sun.jna.platform.win32.COM.IStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerProjectUserMapper;
import com.ledger.business.domain.CtgLedgerProjectUser;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 项目用户Service业务层处理
 *
 * @author ledger
 * @date 2025-08-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CtgLedgerProjectUserServiceImpl implements ICtgLedgerProjectUserService {
    @Autowired
    private CtgLedgerProjectUserMapper ctgLedgerProjectUserMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ICtgLedgerProjectService projectService;
    @Autowired
    private ICtgLedgerAnnualBudgetService annualBudgetService;

    /**
     * 查询项目用户
     *
     * @param id 项目用户主键
     * @return 项目用户
     */
    @Override
    public CtgLedgerProjectUser selectCtgLedgerProjectUserById(Long id) {
        return ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserById(id);
    }

    /**
     * 根据ctgLedgerProjectId和sysUserId查询项目用户
     *
     * @param ctgLedgerProjectId 项目ID
     * @param sysUserId          用户ID
     * @return 项目用户
     */
    @Override
    public CtgLedgerProjectUser selectCtgLedgerProjectUserByProjectIdAndUserId(Long ctgLedgerProjectId, Long sysUserId) {
        return ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserByProjectIdAndUserId(ctgLedgerProjectId, sysUserId);
    }

    /**
     * 查询项目用户列表
     *
     * @param ctgLedgerProjectUser 项目用户
     * @return 项目用户
     */
    @Override
    public List<CtgLedgerProjectUser> selectCtgLedgerProjectUserList(CtgLedgerProjectUser ctgLedgerProjectUser) {
        return ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserList(ctgLedgerProjectUser);
    }

    /**
     * 新增项目用户
     *
     * @param ctgLedgerProjectUser 项目用户
     * @return 结果
     */
    @Override
    public CtgLedgerProjectUser insertCtgLedgerProjectUser(CtgLedgerProjectUser ctgLedgerProjectUser) {
        ctgLedgerProjectUser.setCreateTime(DateUtils.getNowDate());
        ctgLedgerProjectUser.setCreateBy(SecurityUtils.getUsername());
        ctgLedgerProjectUserMapper.insertCtgLedgerProjectUser(ctgLedgerProjectUser);
        return ctgLedgerProjectUser;
    }

    /**
     * 修改项目用户
     *
     * @param ctgLedgerProjectUser 项目用户
     * @return 结果
     */
    @Override
    public int updateCtgLedgerProjectUser(CtgLedgerProjectUser ctgLedgerProjectUser) {
        ctgLedgerProjectUser.setUpdateTime(DateUtils.getNowDate());
        return ctgLedgerProjectUserMapper.updateCtgLedgerProjectUser(ctgLedgerProjectUser);
    }

    /**
     * 批量删除项目用户
     *
     * @param ids 需要删除的项目用户主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectUserByIds(Long[] ids) {
        return ctgLedgerProjectUserMapper.deleteCtgLedgerProjectUserByIds(ids);
    }

    /**
     * 删除项目用户信息
     *
     * @param id 项目用户主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectUserById(Long id) {
        return ctgLedgerProjectUserMapper.deleteCtgLedgerProjectUserById(id);
    }

    @Override
    public boolean isProjectManager(Long projectId, String loginName) {
        CtgLedgerProject project = projectService.selectCtgLedgerProjectById(projectId);

        return project.getProjectManagerLoginName().equals(loginName);
    }

    @Override
    public boolean isProjectMember(Long projectId, String loginName) {
        SysUser sysUser = userService.selectUserByUserName(loginName);
        return isProjectUser(projectId, sysUser.getUserId());
    }

    @Override
    public boolean isProjectUser(Long projectId, Long userId) {
        return ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserByProjectIdAndUserId(projectId, userId) != null;
    }

    @Override
    public List<CtgLedgerProjectUser> batchInsertCtgLedgerProjectUser(List<CtgLedgerProjectUser> ctgLedgerProjectUsers) {
        Long projectId = Optional.ofNullable(ctgLedgerProjectUsers).map(pus -> pus.get(0)).map(p -> p.getCtgLedgerProjectId()).orElse(null);
        ctgLedgerProjectUserMapper.deleteByCtgLedgerProjectIdInt(projectId);
        List<CtgLedgerProjectUser> ctgLedgerProjectUsersIndb = new ArrayList<>(ctgLedgerProjectUsers.size());
        for (CtgLedgerProjectUser u : ctgLedgerProjectUsers) {
            insertCtgLedgerProjectUser(u);
            ctgLedgerProjectUsersIndb.add(u);
        }
        return ctgLedgerProjectUsersIndb;
    }

    @Override
    public CtgLedgerProjectVo toCtgLedgerProjectVo(CtgLedgerProject project) {
        CtgLedgerProjectVo projectVo = new CtgLedgerProjectVo();
        // 复制所有属性
        org.springframework.beans.BeanUtils.copyProperties(project, projectVo);
        CtgLedgerProjectUser param = new CtgLedgerProjectUser();
        param.setCtgLedgerProjectId(project.getId());
        List<CtgLedgerProjectUser> projectUserList = ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserList(param);
        List<SysUser> members = projectUserList.stream().map(p -> p.getSysUserId()).map(uid -> userService.selectUserById(uid)).collect(Collectors.toList());
        List<SysUserVo> sysUserVoList = members.stream().map(m -> SysUserVo.builder().userId(m.getUserId())
                .userName(m.getUserName())
                .nickName(m.getNickName()).build()).collect(Collectors.toList());
        SysUser user = userService.selectUserByUserName(project.getProjectManagerLoginName());
        SysUserVo manager = SysUserVo.builder().userId(user.getUserId())
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .build();
        int currentYear = Year.now().getValue();
        CtgLedgerAnnualBudget ctgLedgerAnnualBudget = annualBudgetService.selectByProjectIdAndYear(project.getId(), currentYear);
        BigDecimal annualBudgetFee = Optional.ofNullable(ctgLedgerAnnualBudget)
                .map(this::toAnnualBudgetFee)
                .orElse(null);

        //设置其他关联信息
        projectVo.setMembers(sysUserVoList);
        projectVo.setManager(manager);
        projectVo.setYear(currentYear);
        projectVo.setAnnualBudgetFee(annualBudgetFee);
        Long  budgetFeeId = Optional.ofNullable(ctgLedgerAnnualBudget).map(CtgLedgerAnnualBudget::getId).orElse(null);
        projectVo.setAnnualBudgetFeeId(budgetFeeId);
        return projectVo;
    }

    private BigDecimal toAnnualBudgetFee(CtgLedgerAnnualBudget ctgLedgerAnnualBudget) {
        if (Objects.isNull(ctgLedgerAnnualBudget)) {
            return null;
        }
        //累加除contractAmount以外的所有费用
        return Stream.of(
                        ctgLedgerAnnualBudget.getEquipPurchaseFee(),
                        ctgLedgerAnnualBudget.getProtoEquipFee(),
                        ctgLedgerAnnualBudget.getEquipRenovFee(),
                        ctgLedgerAnnualBudget.getEquipRentFee(),
                        ctgLedgerAnnualBudget.getMaterialCost(),
                        ctgLedgerAnnualBudget.getTestProcFee(),
                        ctgLedgerAnnualBudget.getFuelPowerCost(),
                        ctgLedgerAnnualBudget.getPubDocIpFee(),
                        ctgLedgerAnnualBudget.getTravelConfCoopFee(),
                        ctgLedgerAnnualBudget.getLaborCost(),
                        ctgLedgerAnnualBudget.getServiceCost(),
                        ctgLedgerAnnualBudget.getExpertConsultFee(),
                        ctgLedgerAnnualBudget.getMgmtFee(),
                        ctgLedgerAnnualBudget.getTaxFee()
                )
                .filter(Objects::nonNull) // 过滤掉null值
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
