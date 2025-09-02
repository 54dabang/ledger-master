package com.ledger.business.mapper;

import java.util.List;

import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import org.apache.ibatis.annotations.Param;

/**
 * 项目支出明细Mapper接口
 *
 * @author ledger
 * @date 2025-08-21
 */
public interface CtgLedgerProjectExpenseDetailMapper {
    /**
     * 查询项目支出明细
     *
     * @param id 项目支出明细主键
     * @return 项目支出明细
     */
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailById(Long id);

    /**
     * 根据报销单号查询项目支出明细
     *
     * @param expenseReportNumber 报销单号
     * @return 项目支出明细
     */
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailByExpenseReportNumber(Long expenseReportNumber);

    /**
     * 根据项目ID查询报销次序最大的项目支出明细
     *
     * @param ledgerProjectId 项目ID
     * @return 项目支出明细
     */
    public CtgLedgerProjectExpenseDetail selectCtgLedgerProjectExpenseDetailWithMaxReimbursementSequenceNoByProjectId(Long ledgerProjectId);

    /**
     * 查询项目支出明细列表
     *
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 项目支出明细集合
     */
    public List<CtgLedgerProjectExpenseDetail> selectCtgLedgerProjectExpenseDetailList(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail);

    /**
     * 新增项目支出明细
     *
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 结果
     */
    public int insertCtgLedgerProjectExpenseDetail(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail);

    /**
     * 修改项目支出明细
     *
     * @param ctgLedgerProjectExpenseDetail 项目支出明细
     * @return 结果
     */
    public int updateCtgLedgerProjectExpenseDetail(CtgLedgerProjectExpenseDetail ctgLedgerProjectExpenseDetail);

    /**
     * 删除项目支出明细
     *
     * @param id 项目支出明细主键
     * @return 结果
     */
    public int deleteCtgLedgerProjectExpenseDetailById(Long id);

    /**
     * 批量删除项目支出明细
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCtgLedgerProjectExpenseDetailByIds(Long[] ids);

    List<CtgLedgerProjectExpenseDetail> selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(@Param("projectId") Long projectId,
                                                                                                  @Param("year") Integer year);


    Long selectMaxReimbursementSequenceNoByProjectIdAndYear(@Param("projectId") Long projectId,
                                         @Param("year") Integer year);
}
