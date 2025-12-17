package com.ledger.business.service;

import java.util.List;

import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectUser;
import com.ledger.business.dto.ProjectUsersDTO;
import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.business.vo.SysUserVo;

/**
 * 项目用户Service接口
 * 
 * @author ledger
 * @date 2025-08-20
 */
public interface ICtgLedgerProjectUserService 
{
    /**
     * 查询项目用户
     * 
     * @param id 项目用户主键
     * @return 项目用户
     */
    public CtgLedgerProjectUser selectCtgLedgerProjectUserById(Long id);

    /**
     * 根据ctgLedgerProjectId和sysUserId查询项目用户
     * 
     * @param ctgLedgerProjectId 项目ID
     * @param sysUserId 用户ID
     * @return 项目用户
     */
    public CtgLedgerProjectUser selectCtgLedgerProjectUserByProjectIdAndUserId(Long ctgLedgerProjectId, Long sysUserId);

    /**
     * 查询项目用户列表
     * 
     * @param ctgLedgerProjectUser 项目用户
     * @return 项目用户集合
     */
    public List<CtgLedgerProjectUser> selectCtgLedgerProjectUserList(CtgLedgerProjectUser ctgLedgerProjectUser);

    /**
     * 新增项目用户
     * 
     * @param ctgLedgerProjectUser 项目用户
     * @return 结果
     */
    public CtgLedgerProjectUser insertCtgLedgerProjectUser(CtgLedgerProjectUser ctgLedgerProjectUser);

    /**
     * 修改项目用户
     * 
     * @param ctgLedgerProjectUser 项目用户
     * @return 结果
     */
    public int updateCtgLedgerProjectUser(CtgLedgerProjectUser ctgLedgerProjectUser);

    /**
     * 批量删除项目用户
     * 
     * @param ids 需要删除的项目用户主键集合
     * @return 结果
     */
    public int deleteCtgLedgerProjectUserByIds(Long[] ids);

    /**
     * 删除项目用户信息
     * 
     * @param id 项目用户主键
     * @return 结果
     */
    public int deleteCtgLedgerProjectUserById(Long id);


    public boolean isProjectUser(Long projectId,Long userId);

    boolean isProjectManager(Long projectId,String loginName);

    boolean isProjectMember(Long projectId,String loginName);

    public List<CtgLedgerProjectUser> batchInsertCtgLedgerProjectUser(ProjectUsersDTO projectUsersDTO);

    CtgLedgerProjectVo toCtgLedgerProjectVo(CtgLedgerProject project);

    List<SysUserVo> getAllMembers(CtgLedgerProject project);
}
