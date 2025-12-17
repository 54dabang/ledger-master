package com.ledger.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledger.business.domain.CtgLedgerAnnualBudget;
import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.service.ICtgLedgerProjectService;
import com.ledger.business.service.ICtgLedgerProjectUserService;
import com.ledger.business.service.IReimbursementService;
import com.ledger.business.util.StrUtil;
import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.business.vo.SysUserVo;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerProjectExpenseDetailMapper;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.service.ICtgLedgerProjectExpenseDetailService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 项目支出明细Service业务层处理
 *
 * @author ledger
 * @date 2025-08-21
 */
@Service
@Transactional
public class CtgLedgerProjectExpenseDetailServiceImpl implements ICtgLedgerProjectExpenseDetailService {
    @Autowired
    private CtgLedgerProjectExpenseDetailMapper ctgLedgerProjectExpenseDetailMapper;
    @Autowired
    private ICtgLedgerProjectUserService projectUserService;
    @Autowired
    private ICtgLedgerProjectService projectService;
    @Autowired
    private IReimbursementService reimbursementService;

    /**
     * 查询项目支出明细
     *
     * @param id 项目支出明细主键
     * @return 项目支出明细
     */
    @Override
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailById(Long id) {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailById(id);
    }

    /**
     * 根据报销单号查询项目支出明细
     *
     * @param expenseReportNumber 报销单号
     * @return 项目支出明细
     */
    @Override
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(Long expenseReportNumber) {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(expenseReportNumber);
    }

    /**
     * 根据项目ID查询报销次序最大的项目支出明细
     *
     * @param ledgerProjectId 项目ID
     * @return 项目支出明细
     */
    @Override
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(Long ledgerProjectId) {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(ledgerProjectId);
    }

    /**
     * 查询项目支出明细列表
     *
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 项目支出明细
     */
    @Override
    public List<CtgLedgerProjectExpenseDetail> selectCtgLedgerProjectExpenseDetailList(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail) {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailList(ctgLedgerProjectExpenseDetail);
    }

    /**
     * 新增项目支出明细
     *
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 结果
     */
    @Override
    public int insertCtgLedgerProjectExpenseDetail(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail) {
        ctgLedgerProjectExpenseDetail.setCreateTime(DateUtils.getNowDate());
        CtgLedgerProject ctgLedgerProject = projectService.selectCtgLedgerProjectById(ctgLedgerProjectExpenseDetail.getLedgerProjectId());
        CtgLedgerProjectVo ctgLedgerProjectVo = this.projectUserService.toCtgLedgerProjectVo(ctgLedgerProject);
        List<SysUserVo> allMembers = Stream.concat(
                ctgLedgerProjectVo.getMembers().stream(),
                Stream.of(ctgLedgerProjectVo.getManager())
        ).collect(Collectors.toList());
        allMembers.stream()
                .filter(m -> m.getUserName().equals(ctgLedgerProjectExpenseDetail.getReimburserLoginName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("用户：%s 不是项目成员，请联系项目负责人（联系人）添加", ctgLedgerProjectExpenseDetail.getReimburserName())
                ));

        return ctgLedgerProjectExpenseDetailMapper.insertCtgLedgerProjectExpenseDetail(ctgLedgerProjectExpenseDetail);
    }


    /**
     * 修改项目支出明细
     *
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 结果
     */
    @Override
    public int updateCtgLedgerProjectExpenseDetail(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail) {
        checkPermission(ctgLedgerProjectExpenseDetail.getId());
        ctgLedgerProjectExpenseDetail.setUpdateTime(DateUtils.getNowDate());
        return ctgLedgerProjectExpenseDetailMapper.updateCtgLedgerProjectExpenseDetail(ctgLedgerProjectExpenseDetail);
    }

    public void checkPermission(Long expenseDetailId) {
        if (Objects.isNull(expenseDetailId)) {
            return;
        }
        CtgLedgerProjectExpenseDetail expenseDetail = this.ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailById(expenseDetailId);
        CtgLedgerProject project = projectService.selectCtgLedgerProjectById(expenseDetail.getLedgerProjectId());
        String loginUser = SecurityUtils.getUsername();
        if (!loginUser.equals(expenseDetail.getReimburserLoginName()) && !reimbursementService.enableManageProject(loginUser, project)) {
            throw new IllegalStateException(String.format("%s 没有该报销记录 %s 的操作权限！", SecurityUtils.getLoginUser().getUser().getNickName(), expenseDetail.getExpenseReportNumber()));
        }
    }

    /**
     * 批量删除项目支出明细
     *
     * @param ids 需要删除的项目支出明细主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectExpenseDetailByIds(Long[] ids) {
        for (Long id : ids) {
            checkPermission(id);
        }
        return ctgLedgerProjectExpenseDetailMapper.deleteCtgLedgerProjectExpenseDetailByIds(ids);
    }

    /**
     * 删除项目支出明细信息
     *
     * @param id 项目支出明细主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectExpenseDetailById(Long id) throws IllegalAccessException {
        checkPermission(id);
        return ctgLedgerProjectExpenseDetailMapper.deleteCtgLedgerProjectExpenseDetailById(id);
    }

    @Override
    public List<CtgLedgerProjectExpenseDetail> batchSave(List<CtgLedgerProjectExpenseDetail> projectExpenseDetails, Long projectId, Long year) {
        if (CollectionUtils.isEmpty(projectExpenseDetails)) {
            throw new RuntimeException("导入数据为空");
        }
        CtgLedgerProject ctgLedgerProject = projectService.selectCtgLedgerProjectById(projectId);
        List<SysUserVo> allMembers = projectUserService.getAllMembers(ctgLedgerProject);

        List<CtgLedgerProjectExpenseDetail> dbDetailList = ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(projectId, year.intValue());
        List<CtgLedgerProjectExpenseDetail> detailList = Lists.newArrayList();
        checkProjectExpenseDetailsReimburserNameInMebmbers(projectExpenseDetails,allMembers);

        for (CtgLedgerProjectExpenseDetail detail : projectExpenseDetails) {
            if (StringUtils.isEmpty(detail.getSubjectName())) {
                break;
            }
            //通过用户的名字（中文）找到对应的用户
            SysUserVo user = findByReimburserName(allMembers, detail.getReimburserName());
            detail.setRemark(StringUtils.isEmpty(detail.getRemarkTemp()) ? StrUtil.buildRemark(detail) : detail.getRemarkTemp());
            detail.setLedgerProjectId(projectId);
            detail.setYear(year.intValue());
            detail.setReimburserLoginName(user.getUserName());
            if (isExistInDb(detail, dbDetailList)) {
                detail.setUpdateBy(SecurityUtils.getUsername());
                detail.setUpdateTime(DateUtils.getNowDate());
                updateCtgLedgerProjectExpenseDetail(detail);
            } else {
                detail.setId(null);
                detail.setCreateBy(SecurityUtils.getUsername());
                detail.setCreateTime(DateUtils.getNowDate());
                ctgLedgerProjectExpenseDetailMapper.insertCtgLedgerProjectExpenseDetail(detail);
            }
            detailList.add(detail);
        }

        return detailList;
    }

    public void checkProjectExpenseDetailsReimburserNameInMebmbers(
            List<CtgLedgerProjectExpenseDetail> detailList,
            List<SysUserVo> allMembers) {

        // 1. 收集所有明细里的报销人姓名
        Set<String> reimburserNames = detailList.stream()
                .map(CtgLedgerProjectExpenseDetail::getReimburserName)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());

        // 2. 收集项目成员的所有昵称
        Set<String> memberNickNames = allMembers.stream()
                .map(SysUserVo::getNickName)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());

        // 3. 找出“不在项目成员中”的报销人
        List<String> notInMembers = reimburserNames.stream()
                .filter(name -> !memberNickNames.contains(name))
                .collect(Collectors.toList());

        // 4. 一次性抛出异常
        if (!notInMembers.isEmpty()) {
            throw new IllegalStateException(
                    "以下报销人不是项目成员，请联系项目负责人添加："
                            + String.join("、", notInMembers));
        }
    }

    private SysUserVo findByReimburserName(List<SysUserVo> allMembers, String name) {
        SysUserVo sysUserVo = allMembers.stream().filter(m -> m.getNickName().equals(name.trim())).findFirst().orElse(null);
        if (Objects.isNull(sysUserVo)) {
            throw new IllegalStateException(String.format("用户：%s 不是项目成员，请联系项目负责人（联系人）添加", name));
        }
        return sysUserVo;
    }


    private boolean isExistInDb(CtgLedgerProjectExpenseDetail detail, List<CtgLedgerProjectExpenseDetail> dbDetailList) {
        if (Objects.isNull(detail)) {
            return false;
        }
        if (Objects.isNull(detail.getId())) {
            return false;
        }
        for (CtgLedgerProjectExpenseDetail dbDetail : dbDetailList) {
            if (detail.getId().equals(dbDetail.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<CtgLedgerProjectExpenseDetail> selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(Long projectId, Integer year) {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(projectId, year);
    }
}
