package com.ledger.business.mapper;

import java.util.List;
import com.ledger.business.domain.CtgLedgerConfig;

/**
 * 台账配置Mapper接口
 * 
 * @author ledger
 * @date 2025-11-05
 */
public interface CtgLedgerConfigMapper 
{
    /**
     * 查询台账配置
     * 
     * @param id 台账配置主键
     * @return 台账配置
     */
    public CtgLedgerConfig selectCtgLedgerConfigById(Long id);

    /**
     * 查询台账配置列表
     * 
     * @param ctgLedgerConfig 台账配置
     * @return 台账配置集合
     */
    public List<CtgLedgerConfig> selectCtgLedgerConfigList(CtgLedgerConfig ctgLedgerConfig);

    /**
     * 新增台账配置
     * 
     * @param ctgLedgerConfig 台账配置
     * @return 结果
     */
    public int insertCtgLedgerConfig(CtgLedgerConfig ctgLedgerConfig);

    /**
     * 修改台账配置
     * 
     * @param ctgLedgerConfig 台账配置
     * @return 结果
     */
    public int updateCtgLedgerConfig(CtgLedgerConfig ctgLedgerConfig);

    /**
     * 删除台账配置
     * 
     * @param id 台账配置主键
     * @return 结果
     */
    public int deleteCtgLedgerConfigById(Long id);

    /**
     * 批量删除台账配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCtgLedgerConfigByIds(Long[] ids);
}