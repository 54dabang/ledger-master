package com.ledger.business.util;

import com.ledger.business.vo.CtgLedgerProjectVo;
import com.ledger.business.vo.SysUserVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 项目成员工具类
 */
public class ProjectMemberUtil {

    /**
     * 获取项目所有成员（包括普通成员和项目经理）
     * 传统方式实现
     *
     * @param projectVo 项目VO
     * @return 所有成员列表
     */
    public static List<SysUserVo> getAllMembersTraditional(CtgLedgerProjectVo projectVo) {
        List<SysUserVo> allMembers = new ArrayList<>();
        if (projectVo.getMembers() != null) {
            allMembers.addAll(projectVo.getMembers());
        }
        if (projectVo.getManager() != null) {
            allMembers.add(projectVo.getManager());
        }
        return allMembers;
    }


}