package com.ledger.business.service;

import com.ledger.business.domain.BimUser;
import com.ledger.business.domain.BimOrg;
import com.ledger.business.util.InitConstant;
import com.ledger.common.core.domain.entity.SysRole;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.core.domain.entity.SysDept;
import com.ledger.common.core.text.Convert;
import com.ledger.common.enums.BusinessStatus;
import com.ledger.common.enums.OperatorType;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.ExceptionUtil;
import com.ledger.common.utils.StringUtils;
import com.ledger.framework.manager.AsyncManager;
import com.ledger.framework.manager.factory.AsyncFactory;
import com.ledger.system.domain.SysOperLog;
import com.ledger.system.domain.SysPost;
import com.ledger.system.mapper.SysPostMapper;
import com.ledger.system.mapper.SysDeptMapper;
import com.ledger.system.mapper.SysRoleMapper;
import com.ledger.system.service.ISysRoleService;
import com.ledger.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service("syncBimUserService")
@Slf4j
public class SyncBimUserService {
    @Autowired
    private IBimUserService iBimUserService;
    @Autowired
    private IBimOrgService bimOrgService;
    @Autowired
    private ISysUserService sysUserservice;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysPostMapper sysPostMapper;
    @Autowired
    private SysRoleMapper iSysRoleService;

    public synchronized void syncUsersAndDepts() {
        log.info("开始同步bim用户和组织机构数据");
        try {
            // 查询所有BimOrg数据
           /* List<BimOrg> bimOrgList = bimOrgService.selectBimOrgList(new BimOrg());
            List<SysDept> existingDeptList = sysDeptMapper.selectDeptList(new SysDept());
            syncDept(bimOrgList, existingDeptList);*/

            List<BimUser> bimUserList = iBimUserService.selectBimUserList(new BimUser());
            syncPosts(bimUserList);
            syncUsers(bimUserList);

            log.info("bim用户和组织机构数据同步完成");
        } catch (Exception e) {
            log.error("bim用户和组织机构数据同步失败", e);
            throw new RuntimeException("bim用户和组织机构数据同步失败", e);
        }
    }
    /**
     * 同步BIM系统中的岗位信息到系统岗位表
     *
     * @param bimUserList BIM用户列表
     */
    /**
     * 同步BIM系统中的岗位信息到系统岗位表
     *
     * @param bimUserList BIM用户列表
     */
    public void syncPosts(List<BimUser> bimUserList) {
        log.info("开始同步bim用户岗位数据");
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperatorType(OperatorType.OTHER.ordinal());
        operLog.setTitle("同步bim用户岗位数据");
        try {
            if (CollectionUtils.isEmpty(bimUserList)) {
                log.info("没有需要同步的bim用户岗位数据");
                operLog.setErrorMsg("没有需要同步的bim用户岗位数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }

            // 提取所有唯一的职位名称
            Set<String> postNames = bimUserList.stream()
                    .map(BimUser::getPosts)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (postNames.isEmpty()) {
                log.info("没有需要同步的bim用户岗位数据");
                operLog.setErrorMsg("没有需要同步的bim用户岗位数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }

            int insertCount = 0;
            int skipCount = 0;

            for (String postName : postNames) {
                try {
                    // 1. 实时检查数据库是否存在（关键修复点）
                    // 即使在单线程环境下，也必须检查数据库当前状态
                    if (sysPostMapper.checkPostNameUnique(postName) != null) {
                        log.debug("跳过已存在的岗位: {}", postName);
                        skipCount++;
                        continue;
                    }

                    // 2. 创建新岗位
                    SysPost newPost = new SysPost();
                    newPost.setPostName(postName);
                    newPost.setPostCode(postName);
                    newPost.setPostSort(100);
                    newPost.setStatus(InitConstant.VALID_FLAG);
                    newPost.setCreateBy("系统自动同步");
                    newPost.setCreateTime(DateUtils.getNowDate());

                    // 3. 执行插入
                    sysPostMapper.insertPost(newPost);
                    insertCount++;
                    log.debug("新增岗位: {}", postName);
                } catch (DuplicateKeyException e) {
                    // 4. 处理并发插入冲突（双重保险）
                    // 即使是单线程执行，也可能存在外部操作导致的冲突
                    log.warn("岗位名称已存在（可能由外部操作创建），跳过: {}", postName);
                    skipCount++;
                }
            }

            log.info("bim用户岗位数据同步成功,新增{}个岗位,跳过{}个已存在岗位,总计{}个唯一岗位",
                    insertCount, skipCount, postNames.size());
            operLog.setErrorMsg("bim用户岗位数据同步成功,新增" + insertCount + "个岗位,跳过" + skipCount + "个已存在岗位,总计" + postNames.size() + "个唯一岗位");
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception e) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            log.error("bim用户岗位数据同步失败", e);
            throw new RuntimeException("bim用户岗位数据同步失败", e);
        }
    }


    public void syncUsers(List<BimUser> bimUserList) {
        log.info("开始同步bim用户数据");
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperatorType(OperatorType.OTHER.ordinal());
        operLog.setTitle("同步bim用户数据");
        try {
            if (CollectionUtils.isEmpty(bimUserList)) {
                log.info("没有需要同步的bim用户数据");
                operLog.setErrorMsg("没有需要同步的bim用户数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }
            List<SysRole> roleList = iSysRoleService.selectRoleAll();
            SysRole commonRole = roleList.stream().filter(r -> r.getRoleName().equals(InitConstant.ROLE_COMMON)).findFirst().get();
            for (BimUser bimUser : bimUserList) {
                // 检查用户名是否为空
                if (bimUser.getUsername() == null || bimUser.getUsername().isEmpty()) {
                    log.warn("跳过用户名为空的用户记录: {}", bimUser.getName());
                    continue;
                }

                SysUser sysUser = sysUserservice.selectUserByUserName(bimUser.getUsername());
                if (Objects.isNull(sysUser)) {
                    sysUser = new SysUser();
                    sysUser.setRoleIds(new Long[]{commonRole.getRoleId()});
                    sysUser.setCreateBy("系统自动同步");
                    sysUser.setCreateTime(DateUtils.getNowDate());
                    sysUser.setUpdateTime(DateUtils.getNowDate());
                    copyBimUserToSysUser(bimUser, sysUser);

                    sysUserservice.insertUser(sysUser);
                    log.info("新增用户: {}", bimUser.getUsername());
                } else {
                    copyBimUserToSysUser(bimUser, sysUser);
                    sysUser.setUpdateTime(DateUtils.getNowDate());
                    sysUserservice.updateUser(sysUser);
                    log.info("更新用户: {},post:{}", bimUser.getUsername(),sysUser.getPostIds());
                }
            }
            log.info("bim用户数据同步成功,累计同步{}个用户", bimUserList.size());
            operLog.setErrorMsg("bim用户数据同步成功,累计同步" + bimUserList.size() + "个用户");
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception e) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            log.error("bim用户数据同步失败", e);
            // 不应该吞掉异常，应该重新抛出
            throw new RuntimeException("bim用户数据同步失败", e);
        }
    }


    public void syncDept(List<BimOrg> bimOrgList, List<SysDept> existingDeptList) {
        log.info("开始同步bim组织机构数据");
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperatorType(OperatorType.OTHER.ordinal());
        operLog.setTitle("同步bim组织机构数据");
        try {
            if (CollectionUtils.isEmpty(bimOrgList)) {
                log.info("没有需要同步的bim组织机构数据");
                operLog.setErrorMsg("没有需要同步的bim组织机构数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }

            // 创建映射关系：BimOrg.id -> SysDept.deptId
            Map<String, Long> bimOrgIdToSysDeptIdMap = new HashMap<>();
            Map<Long, SysDept> sysDeptIdToSysDeptMap = existingDeptList.stream()
                    .collect(Collectors.toMap(SysDept::getDeptId, dept -> dept, (existing, replacement) -> existing));

            // 创建BimOrg映射：BimOrg.id -> BimOrg
            Map<String, BimOrg> bimOrgIdMap = bimOrgList.stream()
                    .filter(org -> org.getId() != null && !org.getId().isEmpty())
                    .collect(Collectors.toMap(BimOrg::getId, bimOrg -> bimOrg, (existing, replacement) -> existing));

            // 创建SysDept映射：SysDept.depFullName -> SysDept (用于根据depFullName查找已存在的部门)
            Map<String, SysDept> sysDeptDepFullNameMap = existingDeptList.stream()
                    .filter(dept -> dept.getDepFullName() != null && !dept.getDepFullName().isEmpty())
                    .collect(Collectors.toMap(SysDept::getDepFullName, dept -> dept, (existing, replacement) -> existing));

            int insertCount = 0;
            int updateCount = 0;

            // 第一步：处理根节点（没有父节点或父节点为null的节点）
            for (BimOrg bimOrg : bimOrgList) {
                // 检查必要字段
                if (bimOrg.getId() == null || bimOrg.getId().isEmpty()) {
                    log.warn("跳过ID为空的组织机构记录");
                    continue;
                }

                if (bimOrg.getDepPid() == null || bimOrg.getDepPid().isEmpty() ||
                        "0".equals(bimOrg.getDepPid()) || !bimOrgIdMap.containsKey(bimOrg.getDepPid())) {

                    // 根据depFullName查找已存在的SysDept
                    SysDept sysDept = null;
                    if (bimOrg.getDepFullName() != null && !bimOrg.getDepFullName().isEmpty()) {
                        sysDept = sysDeptDepFullNameMap.get(bimOrg.getDepFullName());
                    }

                    if (sysDept == null) {
                        // 新增部门
                        sysDept = new SysDept();
                        sysDept.setCreateBy("系统自动同步");
                        sysDept.setCreateTime(DateUtils.getNowDate());
                        copyBimOrgToSysDept(bimOrg, sysDept, null);
                        sysDeptMapper.insertDept(sysDept);
                        insertCount++;
                        log.debug("新增根部门: {}", sysDept.getDeptName());
                    } else {
                        // 更新部门
                        copyBimOrgToSysDept(bimOrg, sysDept, sysDept.getParentId());
                        sysDept.setUpdateTime(DateUtils.getNowDate());
                        sysDeptMapper.updateDept(sysDept);
                        updateCount++;
                        log.debug("更新根部门: {}", sysDept.getDeptName());
                    }
                    bimOrgIdToSysDeptIdMap.put(bimOrg.getId(), sysDept.getDeptId());
                    sysDeptIdToSysDeptMap.put(sysDept.getDeptId(), sysDept);
                }
            }

            // 第二步：处理子节点（使用迭代方式处理多层嵌套）
            int unprocessedCount;
            int iteration = 0;
            do {
                iteration++;
                unprocessedCount = 0;
                int currentInsertCount = 0;
                int currentUpdateCount = 0;

                for (BimOrg bimOrg : bimOrgList) {
                    // 检查必要字段
                    if (bimOrg.getId() == null || bimOrg.getId().isEmpty()) {
                        continue;
                    }

                    // 如果该节点尚未处理且其父节点已处理
                    if (!bimOrgIdToSysDeptIdMap.containsKey(bimOrg.getId()) &&
                            bimOrg.getDepPid() != null &&
                            bimOrgIdToSysDeptIdMap.containsKey(bimOrg.getDepPid())) {

                        // 根据depFullName查找已存在的SysDept
                        SysDept sysDept = null;
                        if (bimOrg.getDepFullName() != null && !bimOrg.getDepFullName().isEmpty()) {
                            sysDept = sysDeptDepFullNameMap.get(bimOrg.getDepFullName());
                        }

                        Long parentId = bimOrgIdToSysDeptIdMap.get(bimOrg.getDepPid());

                        if (sysDept == null) {
                            // 新增部门
                            sysDept = new SysDept();
                            sysDept.setCreateBy("系统自动同步");
                            sysDept.setCreateTime(DateUtils.getNowDate());
                            copyBimOrgToSysDept(bimOrg, sysDept, parentId);
                            sysDeptMapper.insertDept(sysDept);
                            currentInsertCount++;
                            log.debug("新增子部门: {}", sysDept.getDeptName());
                        } else {
                            // 更新部门
                            copyBimOrgToSysDept(bimOrg, sysDept, parentId);
                            sysDept.setUpdateTime(DateUtils.getNowDate());
                            sysDeptMapper.updateDept(sysDept);
                            currentUpdateCount++;
                            log.debug("更新子部门: {}", sysDept.getDeptName());
                        }
                        bimOrgIdToSysDeptIdMap.put(bimOrg.getId(), sysDept.getDeptId());
                        sysDeptIdToSysDeptMap.put(sysDept.getDeptId(), sysDept);
                    } else if (!bimOrgIdToSysDeptIdMap.containsKey(bimOrg.getId())) {
                        unprocessedCount++;
                    }
                }

                insertCount += currentInsertCount;
                updateCount += currentUpdateCount;

                // 防止无限循环
                if (iteration > bimOrgList.size()) {
                    log.warn("处理轮次超过组织机构总数，可能存在数据异常");
                    break;
                }
            } while (unprocessedCount > 0 && unprocessedCount < bimOrgList.size());

            // 处理剩余未处理的节点（可能是循环引用或数据异常）
            if (unprocessedCount > 0) {
                log.warn("存在 {} 个未处理的组织机构节点，可能存在循环引用或数据异常", unprocessedCount);
                for (BimOrg bimOrg : bimOrgList) {
                    if (bimOrg.getId() != null && !bimOrgIdToSysDeptIdMap.containsKey(bimOrg.getId())) {
                        log.warn("未处理的组织机构节点: id={}, depPid={}", bimOrg.getId(), bimOrg.getDepPid());
                    }
                }
            }

            // 更新祖先列表
            updateAncestors(bimOrgIdToSysDeptIdMap, sysDeptIdToSysDeptMap, bimOrgIdMap);

            log.info("bim组织机构数据同步成功,新增{}个组织机构,更新{}个组织机构,总计{}个组织机构",
                    insertCount, updateCount, bimOrgList.size());
            operLog.setErrorMsg("bim组织机构数据同步成功,新增" + insertCount + "个组织机构,更新" + updateCount + "个组织机构,总计" + bimOrgList.size() + "个组织机构");
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception e) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            log.error("bim组织机构数据同步失败", e);
            // 不应该吞掉异常，应该重新抛出
            throw new RuntimeException("bim组织机构数据同步失败", e);
        }
    }

    /**
     * 将BimOrg对象的属性复制到SysDept对象
     */
    private void copyBimOrgToSysDept(BimOrg bimOrg, SysDept sysDept, Long parentId) {
        if (bimOrg == null || sysDept == null) {
            return;
        }

        sysDept.setParentId(parentId != null ? parentId : 0L);
        sysDept.setDeptName(StringUtils.isNotEmpty(bimOrg.getDepShortName()) ? bimOrg.getDepShortName() : bimOrg.getDepFullName());
        if (InitConstant.CHINA_THREE_GORGES_CORPORATION_NAME.equals(sysDept.getDeptName()) || InitConstant.SCIENCE_AND_TECHNOLOGY_RESEARCH_INSTITUTE_DEPARTMENT_NAME.equals(sysDept.getDeptName())) {
            sysDept.setOrderNum(0);
        } else {
            sysDept.setOrderNum(100);
        }

        sysDept.setLeader("");
        sysDept.setPhone("");
        sysDept.setEmail("");
        sysDept.setDepFullName(bimOrg.getDepFullName());
        sysDept.setDepFullPath(bimOrg.getDepFullPath());
        sysDept.setBimDeptId(bimOrg.getId());

        // 根据BimOrg的enable字段设置SysDept状态
        if (bimOrg.getEnable() != null) {
            if ("true".equals(bimOrg.getEnable())) {
                sysDept.setStatus("1"); // 启用
            } else {
                sysDept.setStatus("0"); // 停用
            }
        } else {
            sysDept.setStatus("1"); // 默认启用
        }

        // 删除标志复制
        sysDept.setDelFlag(bimOrg.getDelFlag() != null ? bimOrg.getDelFlag() : "0");

        // 将BimOrg的hrDeptPk保存到remark字段中，用于后续查找
        sysDept.setRemark(bimOrg.getHrDeptPk());
    }

    /**
     * 更新部门的祖先列表
     */
    private void updateAncestors(Map<String, Long> bimOrgIdToSysDeptIdMap,
                                 Map<Long, SysDept> sysDeptIdToSysDeptMap,
                                 Map<String, BimOrg> bimOrgIdMap) {
        int updateCount = 0;
        for (Map.Entry<String, Long> entry : bimOrgIdToSysDeptIdMap.entrySet()) {
            String bimOrgId = entry.getKey();
            Long sysDeptId = entry.getValue();

            SysDept sysDept = sysDeptIdToSysDeptMap.get(sysDeptId);
            if (sysDept == null) continue;

            BimOrg bimOrg = bimOrgIdMap.get(bimOrgId);
            if (bimOrg == null) continue;

            // 构建祖先列表
            StringBuilder ancestors = new StringBuilder("0");
            String currentDepPid = bimOrg.getDepPid();

            while (currentDepPid != null && !currentDepPid.isEmpty() && !currentDepPid.equals("0")) {
                Long parentId = bimOrgIdToSysDeptIdMap.get(currentDepPid);
                if (parentId == null) {
                    break;
                }
                ancestors.insert(0, parentId + ",");
                BimOrg parentOrg = bimOrgIdMap.get(currentDepPid);
                if (parentOrg == null) {
                    break;
                }
                currentDepPid = parentOrg.getDepPid();
            }

            sysDept.setAncestors(ancestors.toString());
            sysDeptMapper.updateDept(sysDept);
            updateCount++;
        }
        log.debug("更新了{}个部门的祖先列表", updateCount);
    }

    /**
     * 将BimUser对象的属性复制到SysUser对象
     */
    public void copyBimUserToSysUser(BimUser bimUser, SysUser sysUser) {
        if (bimUser == null || sysUser == null) {
            return;
        }

        sysUser.setUserName(bimUser.getUsername());
        sysUser.setNickName(bimUser.getName());
        sysUser.setEmail(bimUser.getEmail());
        sysUser.setPhonenumber(bimUser.getPhonenumber());
        sysUser.setSex(bimUser.getSex());

        //获取部门id信息
        SysDept param = new SysDept();
        param.setBimDeptId(bimUser.getDeptId());
        List<SysDept> deptList = sysDeptMapper.selectDeptList(param);
        if (!CollectionUtils.isEmpty(deptList)) {
            SysDept sysDept = deptList.get(0);
            sysUser.setDeptId(sysDept.getDeptId());
        }

        if (StringUtils.isNotEmpty(bimUser.getPosts())) {
            SysPost post = sysPostMapper.checkPostNameUnique(bimUser.getPosts());
            Long postId = (post != null) ? post.getPostId() : null;
            if (postId != null) {
                sysUser.setPostIds(new Long[]{postId});
            } else {
                sysUser.setPostIds(new Long[]{});
            }
        } else {
            sysUser.setPostIds(new Long[]{});
        }


        // 设置默认密码（如果需要）
        if (sysUser.getPassword() == null || sysUser.getPassword().isEmpty()) {
            sysUser.setPassword("123456");
        }

        // 根据BimUser的enable字段设置SysUser状态
        if (bimUser.getEnable() != null) {
            if ("true".equals(bimUser.getEnable())) {
                sysUser.setStatus("1"); // 启用
            } else {
                sysUser.setStatus("0"); // 禁用
            }
        } else {
            sysUser.setStatus("1"); // 默认启用
        }

        // 删除标志复制
        sysUser.setDelFlag(bimUser.getDelFlag() != null ? bimUser.getDelFlag() : "0");
    }
}
