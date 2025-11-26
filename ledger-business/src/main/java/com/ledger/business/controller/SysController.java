package com.ledger.business.controller;

import com.ledger.business.config.LegerConfig;
import com.ledger.business.vo.UploadFileVO;
import com.ledger.common.annotation.Log;
import com.ledger.common.constant.Constants;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.domain.entity.SysMenu;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.utils.SecurityUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.common.utils.file.FileUploadUtils;
import com.ledger.framework.web.service.PermissionService;
import com.ledger.system.service.ISysMenuService;
import com.ledger.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ledger.business.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@Api(value = "用户电子签", tags = {"用户电子签"})
@Slf4j
public class SysController extends BaseController {
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private LegerConfig legerConfig;
    @Autowired
    private ISysUserService userService;

    // 定义允许的文件类型，仅允许图片文件
    private static final String[] ALLOWED_EXTENSION = {"png", "jpg", "jpeg"};
    
    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = permissionService.hasRole(Constants.SUPER_ADMIN);
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId,isAdmin);
        return AjaxResult.success(menuService.buildMenus(menus));
    }



    @ApiOperation("上传个人电子签")
    @PreAuthorize("@ss.hasPermi('business:user:upload:signature')")
    @Log(title = "上传个人电子签", businessType = BusinessType.INSERT)
    @PostMapping("/api/user/uploadSignaturePic")
    public AjaxResult uploadSignaturePic(@RequestParam("file") MultipartFile file) {
        String uploadPath = legerConfig.getUploadPicPath()+"/pic";
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return error("上传文件不能为空！");
            }
            if(StringUtils.isEmpty(uploadPath)){
                return error("默认上传路径配置错误，请联系管理员！");
            }
            SysUser loginUser = userService.selectUserByUserName(SecurityUtils.getUsername());
            if(StringUtils.isNotEmpty(loginUser.getSignaturePic())){
                FileUploadUtils.deleteFile(loginUser.getSignaturePic());
            }

            // 使用UUID重命名文件
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String newFileName = UUID.randomUUID() + suffix;

            // 创建包装后的MultipartFile，使用UUID作为文件名
            MultipartFile uuidNamedFile = FileUtils.wrap(newFileName,file);

            String path = FileUploadUtils.uploadDirect(uploadPath, uuidNamedFile, ALLOWED_EXTENSION);

            loginUser.setSignaturePic(path);
            userService.updateUser(loginUser);
            return success(UploadFileVO.builder().path(path).build());
        } catch (Exception e) {
            logger.error("上传文件报错", e);
            return error("文件格式不正确，请上传png/jpg/jpeg格式的文件");
        }
    }
}