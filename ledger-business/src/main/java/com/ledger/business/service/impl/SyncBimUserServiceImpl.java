package com.ledger.business.service.impl;

import com.ledger.business.domain.BimUser;
import com.ledger.business.domain.BimOrg;
import com.ledger.business.service.IBimOrgService;
import com.ledger.business.service.IBimUserService;
import com.ledger.business.service.SyncBimUserService;
import com.ledger.business.util.InitConstant;
import com.ledger.common.core.domain.entity.SysRole;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.core.domain.entity.SysDept;
import com.ledger.common.core.text.Convert;
import com.ledger.common.enums.BusinessStatus;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.enums.OperatorType;
import com.ledger.common.exception.ServiceException;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.ExceptionUtil;
import com.ledger.common.utils.StringUtils;
import com.ledger.framework.manager.AsyncManager;
import com.ledger.framework.manager.factory.AsyncFactory;
import com.ledger.system.domain.SysOperLog;
import com.ledger.system.domain.SysPost;
import com.ledger.system.mapper.SysDeptMapper;
import com.ledger.system.mapper.SysPostMapper;
import com.ledger.system.mapper.SysRoleMapper;
import com.ledger.system.service.ISysPostService;
import com.ledger.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service("syncBimUserService")
@Slf4j
public class SyncBimUserServiceImpl implements SyncBimUserService {
    @Autowired
    private IBimUserService iBimUserService;
    @Autowired
    private IBimOrgService bimOrgService;
    @Autowired
    private ISysUserService sysUserservice;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private ISysPostService iSysPostService;
    @Autowired
    private SysPostMapper sysPostMapper;

    @Autowired
    private SysRoleMapper sysRoleService;

    public synchronized void syncUsersAndDepts() {
        log.info("开始同步bim用户和组织机构数据");
        long startTime = System.currentTimeMillis();
        try {
            // 查询所有BimOrg数据
            long stepStartTime = System.currentTimeMillis();
            List<BimOrg> bimOrgList = bimOrgService.selectBimOrgList(new BimOrg());
            List<SysDept> existingDeptList = sysDeptMapper.selectDeptList(new SysDept());
            long stepEndTime = System.currentTimeMillis();
            log.info("查询BIM组织机构数据耗时: {}毫秒", stepEndTime - stepStartTime);

            stepStartTime = System.currentTimeMillis();
            syncDept(bimOrgList, existingDeptList);
            stepEndTime = System.currentTimeMillis();
            log.info("同步组织机构数据耗时: {}毫秒", stepEndTime - stepStartTime);

            stepStartTime = System.currentTimeMillis();
            List<BimUser> bimUserList = iBimUserService.selectBimUserList(new BimUser());
            stepEndTime = System.currentTimeMillis();
            log.info("查询BIM用户数据耗时: {}毫秒", stepEndTime - stepStartTime);

            stepStartTime = System.currentTimeMillis();
            syncPosts(bimUserList);
            stepEndTime = System.currentTimeMillis();
            log.info("同步岗位数据耗时: {}毫秒", stepEndTime - stepStartTime);

            stepStartTime = System.currentTimeMillis();
            syncUsers(bimUserList);
            stepEndTime = System.currentTimeMillis();
            log.info("同步用户数据耗时: {}毫秒", stepEndTime - stepStartTime);

            long endTime = System.currentTimeMillis();
            log.info("bim用户和组织机构数据同步完成，总耗时: {}毫秒", endTime - startTime);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("bim用户和组织机构数据同步失败，总耗时: {}毫秒", endTime - startTime, e);
            throw new RuntimeException("bim用户和组织机构数据同步失败", e);
        }
    }




    public void syncUsers(List<BimUser> bimUserList) {
        log.info("开始同步bim用户数据");
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperatorType(OperatorType.OTHER.ordinal());
        operLog.setTitle("同步bim用户数据");
        operLog.setBusinessType(BusinessType.OTHER.ordinal());
        operLog.setOperName("系统定时任务");
        Long start = System.currentTimeMillis();

        try {
            if (CollectionUtils.isEmpty(bimUserList)) {
                log.info("没有需要同步的bim用户数据");
                operLog.setErrorMsg("没有需要同步的bim用户数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }

            // 1. 预加载所有部门信息
            Map<String, SysDept> deptMap = loadDeptMap();

            // 2. 预加载所有岗位信息
            Map<String, SysPost> postMap = loadPostMap();

            // 3. 预加载所有角色信息
            List<SysRole> roleList = sysRoleService.selectRoleAll();
            SysRole commonRole = roleList.stream()
                    .filter(r -> InitConstant.ROLE_COMMON.equals(r.getRoleName()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("未找到普通用户角色"));

            // 4. 预加载所有现有用户
            Map<String, SysUser> existingUserMap = loadExistingUserMap();




            AtomicInteger insertCount = new AtomicInteger(0);
            AtomicInteger updateCount = new AtomicInteger(0);

           // 5. 使用Java8并行流并发处理用户
            bimUserList.parallelStream().forEach(bimUser -> {
                // 检查用户名是否为空
                if (StringUtils.isEmpty(bimUser.getUsername())) {
                    log.warn("跳过用户名为空的用户记录: {}", bimUser.getName());
                    return;
                }

                // 查找现有用户
                SysUser sysUser = existingUserMap.get(bimUser.getUsername());

                boolean isNewUser = sysUser == null;
                if (isNewUser) {
                    // 新建用户
                    sysUser = new SysUser();
                    sysUser.setUserName(bimUser.getUsername());
                    sysUser.setRoleIds(new Long[]{commonRole.getRoleId()});
                    sysUser.setCreateBy("系统自动同步");
                    sysUser.setCreateTime(DateUtils.getNowDate());
                    sysUser.setUpdateTime(DateUtils.getNowDate());
                }

                // 复制数据并检查是否变化
                boolean changed = copyBimUserToSysUser(isNewUser, bimUser, sysUser, deptMap, postMap);

                if (isNewUser || changed) {
                    if (isNewUser) {
                        sysUserservice.insertUser(sysUser);
                        insertCount.incrementAndGet();
                        log.debug("新增用户: {}", bimUser.getUsername());
                    } else {
                        sysUser.setUpdateTime(DateUtils.getNowDate());
                        sysUserservice.updateUser(sysUser);
                        updateCount.incrementAndGet();
                        log.info("更新用户: {}", bimUser.getUsername());
                    }
                    // 更新现有用户映射
                    existingUserMap.put(bimUser.getUsername(), sysUser);
                } else {
                    log.debug("用户数据未变化，跳过更新: {}", bimUser.getUsername());
                }
            });



            log.info("bim用户数据同步成功,新增{}个用户,更新{}个用户,总计{}个用户",
                    insertCount, updateCount, bimUserList.size());
            operLog.setCostTime(System.currentTimeMillis()-start);
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception e) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(),
                    ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            log.error("bim用户数据同步失败", e);
            throw new RuntimeException("bim用户数据同步失败", e);
        }
    }

    /**
     * 加载部门映射：BIM部门ID -> SysDept
     */
    private Map<String, SysDept> loadDeptMap() {
        List<SysDept> deptList = sysDeptMapper.selectDeptList(new SysDept());
        return deptList.stream()
                .filter(dept -> dept.getBimDeptId() != null)
                .collect(Collectors.toMap(SysDept::getBimDeptId, dept -> dept, (existing, replacement) -> existing));
    }

    /**
     * 加载岗位映射：岗位名称 -> SysPost
     */
    private Map<String, SysPost> loadPostMap() {
        List<SysPost> postList = iSysPostService.selectPostList(new SysPost());
        return postList.stream()
                .collect(Collectors.toMap(SysPost::getPostName, post -> post, (existing, replacement) -> existing));
    }

    /**
     * 加载现有用户映射：用户名 -> SysUser
     */
    private Map<String, SysUser> loadExistingUserMap() {
        List<SysUser> userList = sysUserservice.selectUserList(new SysUser());

        // 为每个用户加载岗位信息和部门信息
        for (SysUser user : userList) {
            // 查询用户的岗位ID列表
            List<Long> postIdsList = iSysPostService.selectPostListByUserId(user.getUserId());
            // 转换为Long数组，处理空值情况
            Long[] postIds = (postIdsList != null && !postIdsList.isEmpty())
                    ? postIdsList.toArray(new Long[0])
                    : new Long[0];
            user.setPostIds(postIds);

            // 确保部门ID信息已加载（selectUserList已包含deptId，但确保不为空）
            /*if (user.getDeptId() == null) {
                user.setDeptId(0L);
            }*/
        }

        return userList.stream()
                .collect(Collectors.toMap(SysUser::getUserName, user -> user, (existing, replacement) -> existing));
    }

    // 统一日志模板
    private void logIfChanged(boolean isNew,String field, Object oldVal, Object newVal) {
        if(!isNew){
            log.info("{} changed, old:{}, new:{}", field, oldVal, newVal);
        }

    }

    /**
     * 将BimUser对象的属性复制到SysUser对象，并返回是否发生了变化
     */
    private boolean copyBimUserToSysUser(boolean isNew,BimUser bimUser, SysUser sysUser,
                                         Map<String, SysDept> deptMap, Map<String, SysPost> postMap) {
        if (bimUser == null || sysUser == null) {
            return false;
        }

        boolean changed = false;




// 1. nickName
        String oldNick = sysUser.getNickName();
        changed |= updateField(() -> oldNick, sysUser::setNickName, bimUser.getName());
        if (oldNick != sysUser.getNickName()) {          // 真正变了
            logIfChanged(isNew,"nickName", oldNick, bimUser.getName());
        }

// 2. email
        String oldEmail = sysUser.getEmail();
        changed |= updateField(() -> oldEmail, sysUser::setEmail, bimUser.getEmail());
        if (oldEmail != sysUser.getEmail()) {
            logIfChanged(isNew,"email", oldEmail, bimUser.getEmail());
        }

// 3. phonenumber
        String oldPhone = sysUser.getPhonenumber();
        changed |= updateField(() -> oldPhone, sysUser::setPhonenumber, bimUser.getPhonenumber());
        if (oldPhone != sysUser.getPhonenumber()) {
            logIfChanged(isNew,"phonenumber", oldPhone, bimUser.getPhonenumber());
        }

// 4. sex
        String oldSex = sysUser.getSex();
        changed |= updateField(() -> oldSex, sysUser::setSex, bimUser.getSex());
        if (oldSex != sysUser.getSex()) {
            logIfChanged(isNew,"sex", oldSex, bimUser.getSex());
        }

        // 2. 处理部门信息
        SysDept sysDept = deptMap.get(bimUser.getDeptId());
        Long newDeptId = sysDept != null ? sysDept.getDeptId() : null;
        if (!Objects.equals(sysUser.getDeptId(), newDeptId)) {
            if(!isNew) {
                log.info("deptId,sysUser:{},newDeptId:{},bimUser:{}", sysUser.getDeptId(), newDeptId, bimUser.getDeptId());
            }
            sysUser.setDeptId(newDeptId);
            changed = true;
        }

        // 3. 处理岗位信息
        SysPost sysPost = postMap.get(bimUser.getPosts());
        Long newPostId = sysPost != null ? sysPost.getPostId() : null;
        Long[] newPostIds = newPostId != null ? new Long[]{newPostId} : new Long[0];

        if (!Arrays.equals(sysUser.getPostIds(), newPostIds)) {
            sysUser.setPostIds(newPostIds);
            changed = true;
            if(!isNew){
                log.info("postIds,sysUser:{},bimUser:{}",sysUser.getPostIds(),bimUser.getPosts());
            }

        }

        // 4. 处理状态信息
        String newStatus = "true".equals(bimUser.getEnable()) ? "1" : "0";
        if (!Objects.equals(sysUser.getStatus(), newStatus)) {
            sysUser.setStatus(newStatus);
            changed = true;
            if(!isNew) {
                log.info("status,sysUser:{},bimUser:{}", sysUser.getStatus(), bimUser.getEnable());
            }
        }

        // 5. 处理删除标志
        String newDelFlag = StringUtils.defaultIfEmpty(bimUser.getDelFlag(), "0");
        if (!Objects.equals(sysUser.getDelFlag(), newDelFlag)) {
            sysUser.setDelFlag(newDelFlag);
            changed = true;
            if(!isNew) {
                log.info("delFlag,sysUser:{},bimUser:{}", sysUser.getDelFlag(), bimUser.getDelFlag());
            }
        }

        // 6. 处理密码（仅当是新用户时设置）
        if (sysUser.getUserId() == null && (sysUser.getPassword() == null || sysUser.getPassword().isEmpty())) {
            sysUser.setPassword("123456");
            changed = true;
            if(!isNew) {
                log.info("password,sysUser:{}", sysUser.getPassword());
            }
        }

        return changed;
    }






    /**
     * 同步BIM系统中的岗位信息到系统岗位表
     *
     * @param bimUserList BIM用户列表
     */
    public void syncPosts(List<BimUser> bimUserList) {
        log.info("开始同步bim岗位数据");
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperatorType(OperatorType.OTHER.ordinal());
        operLog.setTitle("同步bim岗位数据");
        operLog.setOperName("系统定时任务");
        long start = System.currentTimeMillis();

        try {
            if (CollectionUtils.isEmpty(bimUserList)) {
                log.info("没有需要同步的bim岗位数据");
                operLog.setErrorMsg("没有需要同步的bim岗位数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }

            // 1. 提取所有唯一的职位名称
            Set<String> allPostNames = extractUniquePostNames(bimUserList);

            if (allPostNames.isEmpty()) {
                log.info("没有需要同步的bim岗位数据");
                operLog.setErrorMsg("没有需要同步的bim岗位数据");
                AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
                return;
            }

            // 2. 获取数据库中已存在的岗位名称
            Set<String> existingPostNames = getExistingPostNames();

            // 3. 计算需要新增的岗位
            Set<String> newPostNames = calculateNewPostNames(allPostNames, existingPostNames);

            // 4. 处理新岗位
            int insertCount = 0;
            int skipCount = allPostNames.size() - newPostNames.size();

            for (String postName : newPostNames) {
                try {
                    // 二次检查确保唯一性（处理并发场景）
                    if (!isPostNameUnique(postName)) {
                        log.debug("跳过已存在的岗位(并发插入): {}", postName);
                        skipCount++;
                        continue;
                    }

                    // 创建并插入新岗位
                    SysPost newPost = createNewPost(postName);
                    iSysPostService.insertPost(newPost);
                    insertCount++;
                    log.debug("新增岗位: {}", postName);

                    // 更新已存在岗位集合（用于后续日志统计）
                    existingPostNames.add(postName);
                } catch (DuplicateKeyException e) {
                    log.warn("岗位名称已存在（可能由外部操作创建），跳过: {}", postName);
                    skipCount++;
                } catch (Exception e) {
                    log.error("插入岗位 {} 时发生错误", postName, e);
                    skipCount++;
                }
            }

            log.info("bim岗位数据同步成功,新增{}个岗位,跳过{}个已存在岗位,总计{}个唯一岗位",
                    insertCount, skipCount, allPostNames.size());
            operLog.setJsonResult("bim岗位数据同步成功,新增" + insertCount + "个岗位,跳过" + skipCount +
                    "个已存在岗位,总计" + allPostNames.size() + "个唯一岗位");
            operLog.setCostTime(System.currentTimeMillis()-start);
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception e) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setJsonResult(StringUtils.substring(Convert.toStr(e.getMessage(),
                    ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            log.error("bim岗位数据同步失败", e);
            throw new RuntimeException("bim岗位数据同步失败", e);
        }
    }

    /**
     * 提取所有唯一的职位名称
     */
    private Set<String> extractUniquePostNames(List<BimUser> bimUserList) {
        return bimUserList.stream()
                .map(BimUser::getPosts)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 获取数据库中已存在的岗位名称
     */
    private Set<String> getExistingPostNames() {
        return iSysPostService.selectPostList(new SysPost()).stream()
                .map(SysPost::getPostName)
                .collect(Collectors.toSet());
    }

    /**
     * 计算需要新增的岗位
     */
    private Set<String> calculateNewPostNames(Set<String> allPostNames, Set<String> existingPostNames) {
        return allPostNames.stream()
                .filter(postName -> !existingPostNames.contains(postName))
                .collect(Collectors.toSet());
    }

    /**
     * 检查岗位名称是否唯一
     */
    private boolean isPostNameUnique(String postName) {
        return sysPostMapper.checkPostNameUnique(postName) == null;
    }

    /**
     * 创建新的岗位对象
     */
    private SysPost createNewPost(String postName) {
        SysPost newPost = new SysPost();
        newPost.setPostName(postName);
        newPost.setPostCode(postName);
        newPost.setPostSort(100);
        newPost.setStatus(InitConstant.VALID_FLAG);
        newPost.setCreateBy("系统自动同步");
        newPost.setCreateTime(DateUtils.getNowDate());
        return newPost;
    }






    public void syncDept(List<BimOrg> bimOrgList, List<SysDept> existingDeptList) {
        log.info("开始同步bim组织机构数据");
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperatorType(OperatorType.OTHER.ordinal());
        operLog.setTitle("同步bim组织机构数据");
        operLog.setOperName("系统定时任务");
        long start = System.currentTimeMillis();

        try {
            if (CollectionUtils.isEmpty(bimOrgList)) {
                log.info("没有需要同步的bim组织机构数据");
                operLog.setJsonResult("没有需要同步的bim组织机构数据");
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

            // 创建SysDept映射：SysDept.bimDeptId -> SysDept (用于根据bimOrg.id查找已存在的部门)
            Map<String, SysDept> bimDeptIdToSysDeptMap = existingDeptList.stream()
                    .filter(dept -> dept.getBimDeptId() != null && !dept.getBimDeptId().isEmpty())
                    .collect(Collectors.toMap(SysDept::getBimDeptId, dept -> dept, (existing, replacement) -> existing));

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

                    // 根据bimOrg.id查找已存在的SysDept
                    SysDept sysDept = bimDeptIdToSysDeptMap.get(bimOrg.getId());

                    if (sysDept == null) {
                        // 新增部门
                        sysDept = new SysDept();
                        sysDept.setCreateBy("系统自动同步");
                        sysDept.setCreateTime(DateUtils.getNowDate());
                        copyBimOrgToSysDept(true,bimOrg, sysDept, null);
                        sysDeptMapper.insertDept(sysDept);
                        insertCount++;
                        log.debug("新增根部门: {}", sysDept.getDeptName());
                    } else {
                        // 只有在数据发生变化时才更新部门
                        if (copyBimOrgToSysDept(false,bimOrg, sysDept, sysDept.getParentId())) {
                            sysDept.setUpdateTime(DateUtils.getNowDate());
                            sysDeptMapper.updateDept(sysDept);
                            updateCount++;
                            log.debug("更新部门: {}", sysDept);
                        } else {
                            log.debug("根部门数据未变化，跳过更新: {}", sysDept.getDeptName());
                        }
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

                        // 根据bimOrg.id查找已存在的SysDept
                        SysDept sysDept = bimDeptIdToSysDeptMap.get(bimOrg.getId());

                        Long parentId = bimOrgIdToSysDeptIdMap.get(bimOrg.getDepPid());

                        if (sysDept == null) {
                            // 新增部门
                            sysDept = new SysDept();
                            sysDept.setCreateBy("系统自动同步");
                            sysDept.setCreateTime(DateUtils.getNowDate());
                            copyBimOrgToSysDept(true,bimOrg, sysDept, parentId);
                            sysDeptMapper.insertDept(sysDept);
                            currentInsertCount++;
                            log.debug("新增子部门: {}", sysDept.getDeptName());
                        } else {
                            // 只有在数据发生变化时才更新部门
                            if (copyBimOrgToSysDept(false,bimOrg, sysDept, parentId)) {
                                sysDept.setUpdateTime(DateUtils.getNowDate());
                                sysDeptMapper.updateDept(sysDept);
                                currentUpdateCount++;
                                log.debug("更新子部门: {}", sysDept.getDeptName());
                            } else {
                                log.debug("子部门数据未变化，跳过更新: {}", sysDept.getDeptName());
                            }
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
            operLog.setCostTime(System.currentTimeMillis()-start);
            operLog.setJsonResult("bim组织机构数据同步成功,新增" + insertCount + "个组织机构,更新" + updateCount + "个组织机构,总计" + bimOrgList.size() + "个组织机构");
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception e) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setJsonResult(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
            log.error("bim组织机构数据同步失败", e);
            throw new RuntimeException("bim组织机构数据同步失败", e);
        }
    }






    /**
     * 将BimOrg对象的属性复制到SysDept对象，并返回是否发生了变化
     */
    private boolean copyBimOrgToSysDept(boolean isNew,BimOrg bimOrg, SysDept sysDept, Long parentId) {
        if (bimOrg == null || sysDept == null) {
            return false;
        }

        boolean changed = false;

        // 记录原始值用于日志输出
        Long oldParentId = sysDept.getParentId();
        String oldDeptName = sysDept.getDeptName();
        Integer oldOrderNum = sysDept.getOrderNum();
        String oldLeader = sysDept.getLeader();
        String oldPhone = sysDept.getPhone();
        String oldEmail = sysDept.getEmail();
        String oldDepFullName = sysDept.getDepFullName();
        String oldDepFullPath = sysDept.getDepFullPath();
        String oldBimDeptId = sysDept.getBimDeptId();
        String oldRemark = sysDept.getRemark();
        String oldStatus = sysDept.getStatus();
        String oldDelFlag = sysDept.getDelFlag();

        // 1. 处理parentId
        Long newParentId = parentId != null ? parentId : 0L;
        if (!Objects.equals(sysDept.getParentId(), newParentId)) {
            sysDept.setParentId(newParentId);
            changed = true;
        }

        // 2. 处理部门名称 - 先处理部门名称，因为后续排序依赖于部门名称
        String newDeptName = StringUtils.isNotEmpty(bimOrg.getDepShortName()) ?
                bimOrg.getDepShortName() : bimOrg.getDepFullName();
        if (!Objects.equals(sysDept.getDeptName(), newDeptName)) {
            sysDept.setDeptName(newDeptName);
            changed = true;
        }

        // 3. 处理排序 - 现在可以正确使用新的部门名称
        int newOrderNum = computeOrderNum(sysDept.getDeptName());
        if (!Objects.equals(sysDept.getOrderNum(), newOrderNum)) {
            sysDept.setOrderNum(newOrderNum);
            changed = true;
        }

        // 4. 处理固定清空字段（业务特殊要求）
        changed |= updateField(sysDept::getLeader, sysDept::setLeader, "");
        changed |= updateField(sysDept::getPhone, sysDept::setPhone, "");
        changed |= updateField(sysDept::getEmail, sysDept::setEmail, "");

        // 5. 处理部门路径信息
        changed |= updateField(sysDept::getDepFullName, sysDept::setDepFullName, bimOrg.getDepFullName());
        changed |= updateField(sysDept::getDepFullPath, sysDept::setDepFullPath, bimOrg.getDepFullPath());
        changed |= updateField(sysDept::getBimDeptId, sysDept::setBimDeptId, bimOrg.getId());


        // 7. 处理状态信息
        String newStatus = convertEnableToStatus(bimOrg.getEnable());
        changed |= updateField(sysDept::getStatus, sysDept::setStatus, newStatus);

        String newDelFlag = StringUtils.defaultIfEmpty(bimOrg.getDelFlag(), "0");
        changed |= updateField(sysDept::getDelFlag, sysDept::setDelFlag, newDelFlag);

        // 如果有变化，记录变更日志
        if (changed && !isNew) {
            log.info("部门信息发生变更 - 部门ID: {}, 部门名称: {}", sysDept.getDeptId(), sysDept.getDeptName());
            if (!Objects.equals(oldParentId, newParentId)) {
                log.info("  parentId: {} -> {}", oldParentId, newParentId);
            }
            if (!Objects.equals(oldDeptName, newDeptName)) {
                log.info("  deptName: {} -> {}", oldDeptName, newDeptName);
            }
            if (!Objects.equals(oldOrderNum, newOrderNum)) {
                log.info("  orderNum: {} -> {}", oldOrderNum, newOrderNum);
            }
            if (!Objects.equals(oldLeader, "")) {
                log.info("  leader: {} -> {}", oldLeader, "");
            }
            if (!Objects.equals(oldPhone, "")) {
                log.info("  phone: {} -> {}", oldPhone, "");
            }
            if (!Objects.equals(oldEmail, "")) {
                log.info("  email: {} -> {}", oldEmail, "");
            }
            if (!Objects.equals(oldDepFullName, bimOrg.getDepFullName())) {
                log.info("  depFullName: {} -> {}", oldDepFullName, bimOrg.getDepFullName());
            }
            if (!Objects.equals(oldDepFullPath, bimOrg.getDepFullPath())) {
                log.info("  depFullPath: {} -> {}", oldDepFullPath, bimOrg.getDepFullPath());
            }
            if (!Objects.equals(oldBimDeptId, bimOrg.getId())) {
                log.info("  bimDeptId: {} -> {}", oldBimDeptId, bimOrg.getId());
            }
            if (!Objects.equals(oldRemark, bimOrg.getHrDeptPk())) {
                log.info("  remark: {} -> {}", oldRemark, bimOrg.getHrDeptPk());
            }
            if (!Objects.equals(oldStatus, newStatus)) {
                log.info("  status: {} -> {}", oldStatus, newStatus);
            }

        }

        return changed;
    }



    /**
     * 辅助方法：更新字段，如果值发生变化
     */
    /**
     * 辅助方法：更新字段，如果值发生变化。
     * 如果 getter 或 setter 为 null，则直接返回 false。
     * 如果旧值与新值不同，则更新并返回 true；否则返回 false。
     */
    private <T> boolean updateField(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (getter == null || setter == null) {
            return false; // 健壮性：防止 NPE
        }

        T oldValue;
        try {
            oldValue = getter.get();
        } catch (Exception e) {
            // 可选：记录日志或抛出自定义异常
            return false;
        }
        if(Objects.deepEquals(oldValue,"")&& Objects.isNull(newValue)){
            return false;
        }

        if (!Objects.equals(oldValue, newValue)) {
            setter.accept(newValue);
            return true;
        }

        return false;
    }
    /**
     * 计算部门排序值
     */
    private int computeOrderNum(String deptName) {
        if (InitConstant.CHINA_THREE_GORGES_CORPORATION_NAME.equals(deptName) ||
                InitConstant.SCIENCE_AND_TECHNOLOGY_RESEARCH_INSTITUTE_DEPARTMENT_NAME.equals(deptName)) {
            return 0;
        }
        return 100;
    }

    /**
     * 将enable字段转换为状态值
     */
    private String convertEnableToStatus(String enable) {
        if ("true".equals(enable)) {
            return "1"; // 启用
        } else if ("false".equals(enable)) {
            return "0"; // 停用
        }
        return "1"; // 默认启用
    }





    /**
     * 更新部门的祖先列表
     */
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

            // 只有在祖先列表发生变化时才更新
            if (!Objects.equals(sysDept.getAncestors(), ancestors.toString())) {
                sysDept.setAncestors(ancestors.toString());
                sysDeptMapper.updateDept(sysDept);
                updateCount++;
            }
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
