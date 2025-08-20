package com.ledger.business.service.impl;

import java.util.List;
import com.ledger.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerProjectUserMapper;
import com.ledger.business.domain.CtgLedgerProjectUser;
import com.ledger.business.service.ICtgLedgerProjectUserService;

/**
 * 项目用户Service业务层处理
 * 
 * @author ledger
 * @date 2025-08-20
 */
@Service
public class CtgLedgerProjectUserServiceImpl implements ICtgLedgerProjectUserService 
{
    @Autowired
    private CtgLedgerProjectUserMapper ctgLedgerProjectUserMapper;

    /**
     * 查询项目用户
     * 
     * @param id 项目用户主键
     * @return 项目用户
     */
    @Override
    public CtgLedgerProjectUser selectCtgLedgerProjectUserById(Long id)
    {
        return ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserById(id);
    }

    /**
     * 查询项目用户列表
     * 
     * @param ctgLedgerProjectUser 项目用户
     * @return 项目用户
     */
    @Override
    public List<CtgLedgerProjectUser> selectCtgLedgerProjectUserList(CtgLedgerProjectUser ctgLedgerProjectUser)
    {
        return ctgLedgerProjectUserMapper.selectCtgLedgerProjectUserList(ctgLedgerProjectUser);
    }

    /**
     * 新增项目用户
     * 
     * @param ctgLedgerProjectUser 项目用户
     * @return 结果
     */
    @Override
    public int insertCtgLedgerProjectUser(CtgLedgerProjectUser ctgLedgerProjectUser)
    {
        ctgLedgerProjectUser.setCreateTime(DateUtils.getNowDate());
        return ctgLedgerProjectUserMapper.insertCtgLedgerProjectUser(ctgLedgerProjectUser);
    }

    /**
     * 修改项目用户
     * 
     * @param ctgLedgerProjectUser 项目用户
     * @return 结果
     */
    @Override
    public int updateCtgLedgerProjectUser(CtgLedgerProjectUser ctgLedgerProjectUser)
    {
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
    public int deleteCtgLedgerProjectUserByIds(Long[] ids)
    {
        return ctgLedgerProjectUserMapper.deleteCtgLedgerProjectUserByIds(ids);
    }

    /**
     * 删除项目用户信息
     * 
     * @param id 项目用户主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerProjectUserById(Long id)
    {
        return ctgLedgerProjectUserMapper.deleteCtgLedgerProjectUserById(id);
    }
}
