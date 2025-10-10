package com.ledger.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ledger.common.core.redis.RedisCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.page.TableDataInfo;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.utils.poi.ExcelUtil;
import com.ledger.system.domain.SysPost;
import com.ledger.system.service.ISysPostService;

/**
 * 岗位信息操作处理
 *
 * @author ledger
 */
@RestController
@RequestMapping("/system/post")
@Api(tags = "岗位管理接口")
public class SysPostController extends BaseController
{
    @Autowired
    private ISysPostService postService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 获取岗位列表
     */
    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @GetMapping("/list")
    @ApiOperation("获取岗位列表")
    @ApiImplicitParam(name = "post", value = "岗位信息", dataType = "SysPost", paramType = "query")
    public TableDataInfo list(SysPost post)
    {
        startPage();
        List<SysPost> list = postService.selectPostList(post);
        return getDataTable(list);
    }

    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:post:export')")
    @PostMapping("/export")
    @ApiOperation("导出岗位列表")
    @ApiImplicitParam(name = "post", value = "岗位信息", dataType = "SysPost", paramType = "query")
    public void export(HttpServletResponse response, SysPost post)
    {
        List<SysPost> list = postService.selectPostList(post);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        util.exportExcel(response, list, "岗位数据");
    }

    /**
     * 根据岗位编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:post:query')")
    @GetMapping(value = "/{postId}")
    @ApiOperation("根据岗位编号获取详细信息")
    @ApiImplicitParam(name = "postId", value = "岗位ID", required = true, dataType = "Long", paramType = "path")
    public AjaxResult getInfo(@PathVariable Long postId)
    {
        return success(postService.selectPostById(postId));
    }

    /**
     * 新增岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增岗位")
    @ApiParam(name = "post", value = "岗位信息", required = true)
    public AjaxResult add(@Validated @RequestBody SysPost post)
    {
        if (!postService.checkPostNameUnique(post))
        {
            return error("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        }
        else if (!postService.checkPostCodeUnique(post))
        {
            return error("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        post.setCreateBy(getUsername());
        redisCache.deleteObject(CACHE_KEY_ALL_POSTS);
        return toAjax(postService.insertPost(post));
    }

    /**
     * 修改岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("修改岗位")
    @ApiParam(name = "post", value = "岗位信息", required = true)
    public AjaxResult edit(@Validated @RequestBody SysPost post)
    {
        if (!postService.checkPostNameUnique(post))
        {
            return error("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        }
        else if (!postService.checkPostCodeUnique(post))
        {
            return error("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        post.setUpdateBy(getUsername());
        redisCache.deleteObject(CACHE_KEY_ALL_POSTS);
        return toAjax(postService.updatePost(post));
    }

    /**
     * 删除岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    @ApiOperation("删除岗位")
    @ApiImplicitParam(name = "postIds", value = "岗位ID数组", required = true, dataType = "Long", paramType = "path")
    public AjaxResult remove(@PathVariable Long[] postIds)
    {
        int count = postService.deletePostByIds(postIds);
        redisCache.deleteObject(CACHE_KEY_ALL_POSTS);
        return toAjax(count);
    }

    /**
     * 获取岗位选择框列表
     */
    @GetMapping("/optionselect")
    @ApiOperation("获取岗位选择框列表")
    public AjaxResult optionselect()
    {
        List<SysPost> posts = postService.selectPostAll();
        return success(posts);
    }
}
