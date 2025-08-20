package com.ledger.business.service.impl;

import java.util.List;
import com.ledger.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerTotalBudgetMapper;
import com.ledger.business.domain.CtgLedgerTotalBudget;
import com.ledger.business.service.ICtgLedgerTotalBudgetService;

/**
 * 项目总预算台账Service业务层处理
 * 
 * @author ledger
 * @date 2025-08-20
 */
@Service
public class CtgLedgerTotalBudgetServiceImpl implements ICtgLedgerTotalBudgetService 
{
    @Autowired
    private CtgLedgerTotalBudgetMapper ctgLedgerTotalBudgetMapper;

    /**
     * 查询项目总预算台账
     * 
     * @param id 项目总预算台账主键
     * @return 项目总预算台账
     */
    @Override
    public CtgLedgerTotalBudget selectCtgLedgerTotalBudgetById(Long id)
    {
        return ctgLedgerTotalBudgetMapper.selectCtgLedgerTotalBudgetById(id);
    }

    /**
     * 查询项目总预算台账列表
     * 
     * @param ctgLedgerTotalBudget 项目总预算台账
     * @return 项目总预算台账
     */
    @Override
    public List<CtgLedgerTotalBudget> selectCtgLedgerTotalBudgetList(CtgLedgerTotalBudget ctgLedgerTotalBudget)
    {
        return ctgLedgerTotalBudgetMapper.selectCtgLedgerTotalBudgetList(ctgLedgerTotalBudget);
    }

    /**
     * 新增项目总预算台账
     * 
     * @param ctgLedgerTotalBudget 项目总预算台账
     * @return 结果
     */
    @Override
    public int insertCtgLedgerTotalBudget(CtgLedgerTotalBudget ctgLedgerTotalBudget)
    {
        ctgLedgerTotalBudget.setCreateTime(DateUtils.getNowDate());
        return ctgLedgerTotalBudgetMapper.insertCtgLedgerTotalBudget(ctgLedgerTotalBudget);
    }

    /**
     * 修改项目总预算台账
     * 
     * @param ctgLedgerTotalBudget 项目总预算台账
     * @return 结果
     */
    @Override
    public int updateCtgLedgerTotalBudget(CtgLedgerTotalBudget ctgLedgerTotalBudget)
    {
        ctgLedgerTotalBudget.setUpdateTime(DateUtils.getNowDate());
        return ctgLedgerTotalBudgetMapper.updateCtgLedgerTotalBudget(ctgLedgerTotalBudget);
    }

    /**
     * 批量删除项目总预算台账
     * 
     * @param ids 需要删除的项目总预算台账主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerTotalBudgetByIds(Long[] ids)
    {
        return ctgLedgerTotalBudgetMapper.deleteCtgLedgerTotalBudgetByIds(ids);
    }

    /**
     * 删除项目总预算台账信息
     * 
     * @param id 项目总预算台账主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerTotalBudgetById(Long id)
    {
        return ctgLedgerTotalBudgetMapper.deleteCtgLedgerTotalBudgetById(id);
    }
}
