package com.ledger.web.controller.system;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ledger.common.core.domain.TreeSelect;
import com.ledger.common.core.redis.RedisCache;
import com.ledger.common.utils.StringUtil;
import com.ledger.system.AdminService;
import com.ledger.system.domain.SysPost;
import com.ledger.system.mapper.SysRoleMapper;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.domain.entity.SysDept;
import com.ledger.common.core.domain.entity.SysRole;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.core.page.TableDataInfo;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.system.service.ISysDeptService;
import com.ledger.system.service.ISysPostService;
import com.ledger.system.service.ISysRoleService;
import com.ledger.system.service.ISysUserService;

/**
 * 用户信息
 *
 * @author ledger
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private SysRoleMapper roleMapper;




    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        if(StringUtils.isNotNull(user.getUserName())){
            if(!StringUtil.startWithEnglish(user.getUserName())){
                user.setNickName(user.getUserName());
                user.setUserName(null);

            }
        }
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    @ApiOperation("获取用户信息")
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(userId)) {
           //userService.checkUserDataScope(userId);
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
            if (sysUser.getDeptId() != null) {
                SysDept dept = deptService.selectDeptById(sysUser.getDeptId());
                ajax.put("dept", dept);
            }
        }
        List<SysRole> roles = roleService.selectAllRoles();
        ajax.put("roles", roles);
        List<SysPost> posts = redisCache.getCacheObject(CACHE_KEY_ALL_POSTS);
        if(CollectionUtils.isEmpty(posts)){
            posts = postService.selectPostAll();
            redisCache.setCacheObject(CACHE_KEY_ALL_POSTS,posts,24,TimeUnit.HOURS);
        }
        ajax.put("posts", posts);

        return ajax;
    }

    /**
     * 获取所有角色列表
     */

    @GetMapping("/allRoles")
    @ApiOperation("获取所有角色列表")
    public AjaxResult getAllRoles() {
        List<SysRole> roles = roleService.selectRoleAll();
        return success(roles);
    }

    /**
     * 获取所有岗位列表
     */

    @GetMapping("/allPosts")
    @ApiOperation("获取所有岗位列表")
    public AjaxResult getAllPosts() {
        List<SysPost> posts = postService.selectPostAll();
        return success(posts);
    }



    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user)) {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        //userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        if (ArrayUtils.contains(userIds, getUserId())) {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", AdminService.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }


    /**
     * 获取部门树列表（优化缓存Key设计）
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept) {
       /* // 生成唯一缓存key - 基于所有关键查询属性构建
        String keyContent = buildDeptTreeCacheKey(dept);
        String cacheKey = CACHE_KEY_DEPT_PREFIX + keyContent;

        // 优先从缓存获取
        List<TreeSelect> treeList = redisCache.getCacheObject(cacheKey);
        if (treeList == null) {
            // 缓存未命中时查询数据库

            // 设置缓存（24小时过期）
            redisCache.setCacheObject(cacheKey, treeList, 24, TimeUnit.HOURS);
        }*/
        List<TreeSelect> treeList = deptService.selectDeptTreeList(dept);
        return success(treeList);
    }

    /**
     * 构建部门树缓存Key - 包含所有关键查询属性
     */
    private String buildDeptTreeCacheKey(SysDept dept) {
        if (dept == null) {
            return "default";
        }

        StringBuilder keyBuilder = new StringBuilder();

        // 按固定顺序添加关键属性（确保相同条件生成相同Key）
        appendKeyPart(keyBuilder, "deptId", dept.getDeptId());
        appendKeyPart(keyBuilder, "parentId", dept.getParentId());
        appendKeyPart(keyBuilder, "deptName", dept.getDeptName());
        appendKeyPart(keyBuilder, "depFullName", dept.getDepFullName());
        appendKeyPart(keyBuilder, "status", dept.getStatus());
        appendKeyPart(keyBuilder, "delFlag", dept.getDelFlag());

        return keyBuilder.toString();
    }

    /**
     * 安全添加Key片段 - 处理null值并转义特殊字符
     */
    private void appendKeyPart(StringBuilder keyBuilder, String name, Object value) {
        if (value != null) {
            // 转义特殊字符（避免Redis Key冲突）
            String safeValue = value.toString().replaceAll("[|:]", "_");
            keyBuilder.append(name).append(":").append(safeValue).append("|");
        }
    }


}
