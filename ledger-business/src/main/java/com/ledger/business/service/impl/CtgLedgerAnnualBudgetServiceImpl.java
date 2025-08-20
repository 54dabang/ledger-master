package com.ledger.business.service.impl;

import java.util.List;
import com.ledger.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerAnnualBudgetMapper;
import com.ledger.business.domain.CtgLedgerAnnualBudget;
import com.ledger.business.service.ICtgLedgerAnnualBudgetService;

/**
 * 项目总预算台账Service业务层处理
 * 
 * @author ledger
 * @date 2025-08-20
 */
@Service
public class CtgLedgerAnnualBudgetServiceImpl implements ICtgLedgerAnnualBudgetService 
{
    @Autowired
    private CtgLedgerAnnualBudgetMapper ctgLedgerAnnualBudgetMapper;

    /**
     * 查询项目总预算台账
     * 
     * @param id 项目总预算台账主键
     * @return 项目总预算台账
     */
    @Override
    public CtgLedgerAnnualBudget selectCtgLedgerAnnualBudgetById(Long id)
    {
        return ctgLedgerAnnualBudgetMapper.selectCtgLedgerAnnualBudgetById(id);
    }

    /**
     * 查询项目总预算台账列表
     * 
     * @param ctgLedgerAnnualBudget 项目总预算台账
     * @return 项目总预算台账
     */
    @Override
    public List<CtgLedgerAnnualBudget> selectCtgLedgerAnnualBudgetList(CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        return ctgLedgerAnnualBudgetMapper.selectCtgLedgerAnnualBudgetList(ctgLedgerAnnualBudget);
    }

    /**
     * 新增项目总预算台账
     * 
     * @param ctgLedgerAnnualBudget 项目总预算台账
     * @return 结果
     */
    @Override
    public int insertCtgLedgerAnnualBudget(CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        ctgLedgerAnnualBudget.setCreateTime(DateUtils.getNowDate());
        return ctgLedgerAnnualBudgetMapper.insertCtgLedgerAnnualBudget(ctgLedgerAnnualBudget);
    }

    /**
     * 修改项目总预算台账
     * 
     * @param ctgLedgerAnnualBudget 项目总预算台账
     * @return 结果
     */
    @Override
    public int updateCtgLedgerAnnualBudget(CtgLedgerAnnualBudget ctgLedgerAnnualBudget)
    {
        ctgLedgerAnnualBudget.setUpdateTime(DateUtils.getNowDate());
        return ctgLedgerAnnualBudgetMapper.updateCtgLedgerAnnualBudget(ctgLedgerAnnualBudget);
    }

    /**
     * 批量删除项目总预算台账
     * 
     * @param ids 需要删除的项目总预算台账主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerAnnualBudgetByIds(Long[] ids)
    {
        return ctgLedgerAnnualBudgetMapper.deleteCtgLedgerAnnualBudgetByIds(ids);
    }

    /**
     * 删除项目总预算台账信息
     * 
     * @param id 项目总预算台账主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerAnnualBudgetById(Long id)
    {
        return ctgLedgerAnnualBudgetMapper.deleteCtgLedgerAnnualBudgetById(id);
    }
}
