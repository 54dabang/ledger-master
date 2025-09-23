package com.ledger.business.mapper;


import com.ledger.business.domain.BimUser;
import com.ledger.common.annotation.DataSource;
import com.ledger.common.enums.DataSourceType;

import java.util.List;

/**
 * BIM用户Mapper接口
 */
public interface BimUserMapper {

    /**
     * 查询BIM用户列表
     *
     * @param bimUser BIM用户对象
     * @return BIM用户集合
     */

    List<BimUser> selectBimUserList(BimUser bimUser);

    /**
     * 根据ID查询BIM用户
     *
     * @param id BIM用户ID
     * @return BIM用户对象
     */

    BimUser selectBimUserById(String id);

    /**
     * 新增BIM用户
     *
     * @param bimUser BIM用户对象
     * @return 结果
     */

    int insertBimUser(BimUser bimUser);

    /**
     * 修改BIM用户
     *
     * @param bimUser BIM用户对象
     * @return 结果
     */

    int updateBimUser(BimUser bimUser);

    /**
     * 删除BIM用户
     *
     * @param id BIM用户ID
     * @return 结果
     */

    int deleteBimUserById(String id);

    /**
     * 批量删除BIM用户
     *
     * @param ids BIM用户ID数组
     * @return 结果
     */

    int deleteBimUserByIds(String[] ids);
}

