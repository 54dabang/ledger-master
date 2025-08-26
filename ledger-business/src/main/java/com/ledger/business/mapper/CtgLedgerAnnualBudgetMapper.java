package com.ledger.business.mapper;

import java.util.List;
import com.ledger.business.domain.CtgLedgerAnnualBudget;
import org.apache.ibatis.annotations.Param;

/**
 * 项目总预算台账Mapper接口
 * 
 * @author ledger
 * @date 2025-08-20
 */
public interface CtgLedgerAnnualBudgetMapper 
{
    /**
     * 查询项目总预算台账
     * 
     * @param id 项目总预算台账主键
     * @return 项目总预算台账
     */
    public CtgLedgerAnnualBudget selectCtgLedgerAnnualBudgetById(Long id);

    /**
     * 查询项目总预算台账列表
     * 
     * @param ctgLedgerAnnualBudget 项目总预算台账
     * @return 项目总预算台账集合
     */
    public List<CtgLedgerAnnualBudget> selectCtgLedgerAnnualBudgetList(CtgLedgerAnnualBudget ctgLedgerAnnualBudget);

    /**
     * 新增项目总预算台账
     * 
     * @param ctgLedgerAnnualBudget 项目总预算台账
     * @return 结果
     */
    public int insertCtgLedgerAnnualBudget(CtgLedgerAnnualBudget ctgLedgerAnnualBudget);

    /**
     * 修改项目总预算台账
     * 
     * @param ctgLedgerAnnualBudget 项目总预算台账
     * @return 结果
     */
    public int updateCtgLedgerAnnualBudget(CtgLedgerAnnualBudget ctgLedgerAnnualBudget);

    /**
     * 删除项目总预算台账
     * 
     * @param id 项目总预算台账主键
     * @return 结果
     */
    public int deleteCtgLedgerAnnualBudgetById(Long id);

    /**
     * 批量删除项目总预算台账
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCtgLedgerAnnualBudgetByIds(Long[] ids);

    CtgLedgerAnnualBudget selectByProjectIdAndYear(@Param("projectId") Long projectId,@Param("year")  Integer year);
}
