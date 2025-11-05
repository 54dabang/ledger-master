package com.ledger.business.service.impl;

import java.util.List;

import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ledger.business.mapper.CtgLedgerConfigMapper;
import com.ledger.business.domain.CtgLedgerConfig;
import com.ledger.business.service.ICtgLedgerConfigService;

/**
 * 台账配置Service业务层处理
 * 
 * @author ledger
 * @date 2025-11-05
 */
@Service
public class CtgLedgerConfigServiceImpl implements ICtgLedgerConfigService
{
    @Autowired
    private CtgLedgerConfigMapper ctgLedgerConfigMapper;

    /**
     * 查询台账配置
     * 
     * @param id 台账配置主键
     * @return 台账配置
     */
    @Override
    public CtgLedgerConfig selectCtgLedgerConfigById(Long id)
    {
        return ctgLedgerConfigMapper.selectCtgLedgerConfigById(id);
    }

    /**
     * 查询台账配置列表
     * 
     * @param ctgLedgerConfig 台账配置
     * @return 台账配置
     */
    @Override
    public List<CtgLedgerConfig> selectCtgLedgerConfigList(CtgLedgerConfig ctgLedgerConfig)
    {
        return ctgLedgerConfigMapper.selectCtgLedgerConfigList(ctgLedgerConfig);
    }

    /**
     * 新增台账配置
     * 
     * @param ctgLedgerConfig 台账配置
     * @return 结果
     */
    @Override
    public int insertCtgLedgerConfig(CtgLedgerConfig ctgLedgerConfig)
    {
        ctgLedgerConfig.setCreateTime(DateUtils.getNowDate());
        ctgLedgerConfig.setCreateBy(SecurityUtils.getUsername());
        ctgLedgerConfig.setUpdateTime(DateUtils.getNowDate());
        ctgLedgerConfig.setUpdateBy(SecurityUtils.getUsername());
        return ctgLedgerConfigMapper.insertCtgLedgerConfig(ctgLedgerConfig);
    }

    /**
     * 修改台账配置
     * 
     * @param ctgLedgerConfig 台账配置
     * @return 结果
     */
    @Override
    public int updateCtgLedgerConfig(CtgLedgerConfig ctgLedgerConfig)
    {
        ctgLedgerConfig.setUpdateTime(DateUtils.getNowDate());
        ctgLedgerConfig.setUpdateBy(SecurityUtils.getUsername());
        return ctgLedgerConfigMapper.updateCtgLedgerConfig(ctgLedgerConfig);
    }

    /**
     * 批量删除台账配置
     * 
     * @param ids 需要删除的台账配置主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerConfigByIds(Long[] ids)
    {
        return ctgLedgerConfigMapper.deleteCtgLedgerConfigByIds(ids);
    }

    /**
     * 删除台账配置信息
     * 
     * @param id 台账配置主键
     * @return 结果
     */
    @Override
    public int deleteCtgLedgerConfigById(Long id)
    {
        return ctgLedgerConfigMapper.deleteCtgLedgerConfigById(id);
    }
}