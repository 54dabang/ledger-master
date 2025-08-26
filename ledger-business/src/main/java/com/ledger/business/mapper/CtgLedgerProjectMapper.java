package com.ledger.business.mapper;

import java.util.List;
import com.ledger.business.domain.CtgLedgerProject;
import org.apache.ibatis.annotations.Param;
/**
 * 项目管理Mapper接口
 * 
 * @author ledger
 * @date 2025-08-21
 */
public interface CtgLedgerProjectMapper 
{
    /**
     * 查询项目管理
     * 
     * @param id 项目管理主键
     * @return 项目管理
     */
    public CtgLedgerProject selectCtgLedgerProjectById(Long id);

    /**
     * 查询项目管理列表
     * 
     * @param ctgLedgerProject 项目管理
     * @return 项目管理集合
     */
    public List<CtgLedgerProject> selectCtgLedgerProjectList(CtgLedgerProject ctgLedgerProject);

    /**
     * 新增项目管理
     * 
     * @param ctgLedgerProject 项目管理
     * @return 结果
     */
    public int insertCtgLedgerProject(CtgLedgerProject ctgLedgerProject);

    /**
     * 修改项目管理
     * 
     * @param ctgLedgerProject 项目管理
     * @return 结果
     */
    public int updateCtgLedgerProject(CtgLedgerProject ctgLedgerProject);

    /**
     * 删除项目管理
     * 
     * @param id 项目管理主键
     * @return 结果
     */
    public int deleteCtgLedgerProjectById(Long id);

    /**
     * 批量删除项目管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCtgLedgerProjectByIds(Long[] ids);

    /**
     * 根据项目名称查询项目管理信息
     *
     * @param projectName 项目名称
     * @return 项目管理信息
     */
    public CtgLedgerProject selectCtgLedgerProjectByProjectName(String projectName);
}
