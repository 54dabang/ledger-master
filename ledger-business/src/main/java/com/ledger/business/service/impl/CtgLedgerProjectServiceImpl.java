package com.ledger.business.service.impl;

import java.util.List;

import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerProjectMapper;
import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.service.ICtgLedgerProjectService;

/**
 * 项目管理Service业务层处理
 *
 * @author ledger
 * @date 2025-08-21
 */
@Service
public class CtgLedgerProjectServiceImpl implements ICtgLedgerProjectService {
    @Autowired
    private CtgLedgerProjectMapper ctgLedgerProjectMapper;

    /**
     * 查询项目管理
     *
     * @param id 项目管理主键
     * @return 项目管理
     */
    @Override
    public CtgLedgerProject selectCtgLedgerProjectById(Long id) {
        return ctgLedgerProjectMapper.selectCtgLedgerProjectById(id);
    }

    /**
     * 查询项目管理列表
     *
     * @param ctgLedgerProject 项目管理
     * @return 项目管理
     */
    @Override
    public List<CtgLedgerProject> selectCtgLedgerProjectList(CtgLedgerProject ctgLedgerProject) {
        return ctgLedgerProjectMapper.selectCtgLedgerProjectList(ctgLedgerProject);
    }

    /**
     * 新增项目管理
     *
     * @param ctgLedgerProject 项目管理
     * @return 结果
     */
    @Override
    public CtgLedgerProject insertCtgLedgerProject(CtgLedgerProject ctgLedgerProject) {
        ctgLedgerProject.setCreateTime(DateUtils.getNowDate());
        ctgLedgerProject.setCreateBy(SecurityUtils.getUsername());
        ctgLedgerProject.setUpdateTime(DateUtils.getNowDate());
        ctgLedgerProject.setUpdateBy(SecurityUtils.getUsername());
        ctgLedgerProjectMapper.insertCtgLedgerProject(ctgLedgerProject);

        return ctgLedgerProject;
    }

    /**
     * 修改项目管理
     *
     * @param ctgLedgerProject 项目管理
     * @return 结果
     */
    @Override
    public int updateCtgLedgerProject(CtgLedgerProject ctgLedgerProject) {
        ctgLedgerProject.setUpdateTime(DateUtils.getNowDate());
        ctgLedgerProject.setUpdateBy(SecurityUtils.getUsername());
        return ctgLedgerProjectMapper.updateCtgLedgerProject(ctgLedgerProject);
    }

    /**
     * 批量删除项目管理
     *
     * @param ids 需要删除的项目管理主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectByIds(Long[] ids) {
        return ctgLedgerProjectMapper.deleteCtgLedgerProjectByIds(ids);
    }

    /**
     * 删除项目管理信息
     *
     * @param id 项目管理主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectById(Long id) {
        return ctgLedgerProjectMapper.deleteCtgLedgerProjectById(id);
    }

    /**
     * 根据项目名称查询项目管理信息
     *
     * @param projectName 项目名称
     * @return 项目管理信息
     */
    @Override
    public CtgLedgerProject selectCtgLedgerProjectByProjectName(String projectName) {
        return ctgLedgerProjectMapper.selectCtgLedgerProjectByProjectName(projectName);
    }


}
