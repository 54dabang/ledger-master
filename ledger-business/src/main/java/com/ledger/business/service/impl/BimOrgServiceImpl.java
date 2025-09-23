package com.ledger.business.service.impl;

import com.ledger.business.domain.BimOrg;
import com.ledger.business.mapper.BimOrgMapper;
import com.ledger.business.service.IBimOrgService;
import com.ledger.common.annotation.DataSource;
import com.ledger.common.enums.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * BIM组织机构Service业务层处理
 */
@Service
@DataSource(value = DataSourceType.SLAVE)
public class BimOrgServiceImpl implements IBimOrgService {

    @Autowired
    private BimOrgMapper bimOrgMapper;

    /**
     * 查询BIM组织机构列表
     *
     * @param bimOrg BIM组织机构对象
     * @return BIM组织机构集合
     */
    @Override
    public List<BimOrg> selectBimOrgList(BimOrg bimOrg) {
        return bimOrgMapper.selectBimOrgList(bimOrg);
    }

    /**
     * 根据ID查询BIM组织机构
     *
     * @param id BIM组织机构ID
     * @return BIM组织机构对象
     */
    @Override
    public BimOrg selectBimOrgById(String id) {
        return bimOrgMapper.selectBimOrgById(id);
    }

    /**
     * 新增BIM组织机构
     *
     * @param bimOrg BIM组织机构对象
     * @return 结果
     */
    @Override
    public int insertBimOrg(BimOrg bimOrg) {
        return bimOrgMapper.insertBimOrg(bimOrg);
    }

    /**
     * 修改BIM组织机构
     *
     * @param bimOrg BIM组织机构对象
     * @return 结果
     */
    @Override
    public int updateBimOrg(BimOrg bimOrg) {
        return bimOrgMapper.updateBimOrg(bimOrg);
    }

    /**
     * 删除BIM组织机构
     *
     * @param id BIM组织机构ID
     * @return 结果
     */
    @Override
    public int deleteBimOrgById(String id) {
        return bimOrgMapper.deleteBimOrgById(id);
    }

    /**
     * 批量删除BIM组织机构
     *
     * @param ids BIM组织机构ID数组
     * @return 结果
     */
    @Override
    public int deleteBimOrgByIds(String[] ids) {
        return bimOrgMapper.deleteBimOrgByIds(ids);
    }
}
