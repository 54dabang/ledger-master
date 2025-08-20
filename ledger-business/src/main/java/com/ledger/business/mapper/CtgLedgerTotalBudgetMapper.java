package com.ledger.business.mapper;

import java.util.List;
import com.ledger.business.domain.CtgLedgerTotalBudget;

/**
 * 项目总预算台账Mapper接口
 * 
 * @author ledger
 * @date 2025-08-20
 */
public interface CtgLedgerTotalBudgetMapper 
{
    /**
     * 查询项目总预算台账
     * 
     * @param id 项目总预算台账主键
     * @return 项目总预算台账
     */
    public CtgLedgerTotalBudget selectCtgLedgerTotalBudgetById(Long id);

    /**
     * 查询项目总预算台账列表
     * 
     * @param ctgLedgerTotalBudget 项目总预算台账
     * @return 项目总预算台账集合
     */
    public List<CtgLedgerTotalBudget> selectCtgLedgerTotalBudgetList(CtgLedgerTotalBudget ctgLedgerTotalBudget);

    /**
     * 新增项目总预算台账
     * 
     * @param ctgLedgerTotalBudget 项目总预算台账
     * @return 结果
     */
    public int insertCtgLedgerTotalBudget(CtgLedgerTotalBudget ctgLedgerTotalBudget);

    /**
     * 修改项目总预算台账
     * 
     * @param ctgLedgerTotalBudget 项目总预算台账
     * @return 结果
     */
    public int updateCtgLedgerTotalBudget(CtgLedgerTotalBudget ctgLedgerTotalBudget);

    /**
     * 删除项目总预算台账
     * 
     * @param id 项目总预算台账主键
     * @return 结果
     */
    public int deleteCtgLedgerTotalBudgetById(Long id);

    /**
     * 批量删除项目总预算台账
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCtgLedgerTotalBudgetByIds(Long[] ids);
}
