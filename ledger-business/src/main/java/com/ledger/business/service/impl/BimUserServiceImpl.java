package com.ledger.business.service.impl;

import com.ledger.business.domain.BimUser;
import com.ledger.business.mapper.BimUserMapper;
import com.ledger.business.service.IBimUserService;
import com.ledger.common.annotation.DataSource;
import com.ledger.common.enums.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BIM用户Service业务层处理
 */
@Service
@DataSource(value = DataSourceType.SLAVE) // 方法级别的数据源注解
public class BimUserServiceImpl implements IBimUserService {

    @Autowired
    private BimUserMapper bimUserMapper;

    /**
     * 查询BIM用户列表
     *
     * @param bimUser BIM用户对象
     * @return BIM用户集合
     */

    @Override
    public List<BimUser> selectBimUserList(BimUser bimUser) {
        return bimUserMapper.selectBimUserList(bimUser);
    }

    /**
     * 根据ID查询BIM用户
     *
     * @param id BIM用户ID
     * @return BIM用户对象
     */
    @Override

    public BimUser selectBimUserById(String id) {
        return bimUserMapper.selectBimUserById(id);
    }

    /**
     * 新增BIM用户
     *
     * @param bimUser BIM用户对象
     * @return 结果
     */
    @Override
    @DataSource(value = DataSourceType.SLAVE) // 方法级别的数据源注解
    public int insertBimUser(BimUser bimUser) {
        return bimUserMapper.insertBimUser(bimUser);
    }

    /**
     * 修改BIM用户
     *
     * @param bimUser BIM用户对象
     * @return 结果
     */
    @Override
    @DataSource(value = DataSourceType.SLAVE) // 方法级别的数据源注解
    public int updateBimUser(BimUser bimUser) {
        return bimUserMapper.updateBimUser(bimUser);
    }

    /**
     * 删除BIM用户
     *
     * @param id BIM用户ID
     * @return 结果
     */
    @Override
    public int deleteBimUserById(String id) {
        return bimUserMapper.deleteBimUserById(id);
    }

    /**
     * 批量删除BIM用户
     *
     * @param ids BIM用户ID数组
     * @return 结果
     */
    @Override
    public int deleteBimUserByIds(String[] ids) {
        return bimUserMapper.deleteBimUserByIds(ids);
    }
}
