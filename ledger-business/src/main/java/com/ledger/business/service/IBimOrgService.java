package com.ledger.business.service;

import com.ledger.business.domain.BimOrg;
import java.util.List;

/**
 * BIM组织机构Service接口
 */
public interface IBimOrgService {

    /**
     * 查询BIM组织机构列表
     *
     * @param bimOrg BIM组织机构对象
     * @return BIM组织机构集合
     */
    List<BimOrg> selectBimOrgList(BimOrg bimOrg);

    /**
     * 根据ID查询BIM组织机构
     *
     * @param id BIM组织机构ID
     * @return BIM组织机构对象
     */
    BimOrg selectBimOrgById(String id);

    /**
     * 新增BIM组织机构
     *
     * @param bimOrg BIM组织机构对象
     * @return 结果
     */
    int insertBimOrg(BimOrg bimOrg);

    /**
     * 修改BIM组织机构
     *
     * @param bimOrg BIM组织机构对象
     * @return 结果
     */
    int updateBimOrg(BimOrg bimOrg);

    /**
     * 删除BIM组织机构
     *
     * @param id BIM组织机构ID
     * @return 结果
     */
    int deleteBimOrgById(String id);

    /**
     * 批量删除BIM组织机构
     *
     * @param ids BIM组织机构ID数组
     * @return 结果
     */
    int deleteBimOrgByIds(String[] ids);
}
