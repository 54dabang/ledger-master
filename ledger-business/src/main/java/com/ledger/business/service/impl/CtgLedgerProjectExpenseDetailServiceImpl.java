package com.ledger.business.service.impl;

import java.util.List;
import java.util.Objects;

import com.ledger.business.domain.CtgLedgerAnnualBudget;
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
public class CtgLedgerProjectExpenseDetailServiceImpl implements ICtgLedgerProjectExpenseDetailService 
{
    @Autowired
    private CtgLedgerProjectExpenseDetailMapper ctgLedgerProjectExpenseDetailMapper;

    /**
     * 查询项目支出明细
     * 
     * @param id 项目支出明细主键
     * @return 项目支出明细
     */
    @Override
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailById(Long id)
    {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailById(id);
    }

    /**
     * 根据报销单号查询项目支出明细
     *
     * @param expenseReportNumber 报销单号
     * @return 项目支出明细
     */
    @Override
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(Long expenseReportNumber)
    {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(expenseReportNumber);
    }

    /**
     * 根据项目ID查询报销次序最大的项目支出明细
     *
     * @param ledgerProjectId 项目ID
     * @return 项目支出明细
     */
    @Override
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(Long ledgerProjectId)
    {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(ledgerProjectId);
    }

    /**
     * 查询项目支出明细列表
     * 
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 项目支出明细
     */
    @Override
    public List<CtgLedgerProjectExpenseDetail> selectCtgLedgerProjectExpenseDetailList(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail)
    {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailList(ctgLedgerProjectExpenseDetail);
    }

    /**
     * 新增项目支出明细
     * 
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 结果
     */
    @Override
    public int insertCtgLedgerProjectExpenseDetail(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail)
    {
        ctgLedgerProjectExpenseDetail.setCreateTime(DateUtils.getNowDate());
        return ctgLedgerProjectExpenseDetailMapper.insertCtgLedgerProjectExpenseDetail(ctgLedgerProjectExpenseDetail);
    }

    /**
     * 修改项目支出明细
     * 
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 结果
     */
    @Override
    public int updateCtgLedgerProjectExpenseDetail(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail)
    {
        ctgLedgerProjectExpenseDetail.setUpdateTime(DateUtils.getNowDate());
        return ctgLedgerProjectExpenseDetailMapper.updateCtgLedgerProjectExpenseDetail(ctgLedgerProjectExpenseDetail);
    }

    /**
     * 批量删除项目支出明细
     * 
     * @param ids 需要删除的项目支出明细主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectExpenseDetailByIds(Long[] ids)
    {
        return ctgLedgerProjectExpenseDetailMapper.deleteCtgLedgerProjectExpenseDetailByIds(ids);
    }

    /**
     * 删除项目支出明细信息
     * 
     * @param id 项目支出明细主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectExpenseDetailById(Long id)
    {
        return ctgLedgerProjectExpenseDetailMapper.deleteCtgLedgerProjectExpenseDetailById(id);
    }

    @Override
    public List<CtgLedgerProjectExpenseDetail> batchSave(List<CtgLedgerProjectExpenseDetail> projectExpenseDetails, Long projectId, Long year) {
        if (CollectionUtils.isEmpty(projectExpenseDetails)) {
            throw new RuntimeException("导入数据为空");
        }
        List<CtgLedgerProjectExpenseDetail> detailList = Lists.newArrayList();
        for (CtgLedgerProjectExpenseDetail detail : projectExpenseDetails) {
            if (StringUtils.isEmpty(detail.getSubjectName())) {
                break;
            }
            detail.setRemark(detail.getRemarkTemp());
            detail.setLedgerProjectId(projectId);
            detail.setYear(year.intValue());
            if (Objects.nonNull(detail.getId())) {
                detail.setUpdateBy(SecurityUtils.getUsername());
                detail.setUpdateTime(DateUtils.getNowDate());
                updateCtgLedgerProjectExpenseDetail(detail);
            } else {
                detail.setCreateBy(SecurityUtils.getUsername());
                detail.setCreateTime(DateUtils.getNowDate());
                detail.setReimburserLoginName(SecurityUtils.getUsername());
                ctgLedgerProjectExpenseDetailMapper.insertCtgLedgerProjectExpenseDetail(detail);
            }
            detailList.add(detail);
        }

        return detailList;
    }

    @Override
    public List<CtgLedgerProjectExpenseDetail> selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(Long projectId, Integer year) {
        return ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(projectId,year);
    }
}
