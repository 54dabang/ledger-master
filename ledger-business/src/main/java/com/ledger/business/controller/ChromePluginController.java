package com.ledger.business.controller;

import com.alibaba.fastjson2.JSON;
import com.ledger.business.config.LegerConfig;
import com.ledger.business.config.PluginConfig;
import com.ledger.business.domain.CtgLedgerConfig;
import com.ledger.business.dto.PluginDTO;
import com.ledger.business.service.ICtgLedgerConfigService;
import com.ledger.business.util.InitConstant;
import com.ledger.business.vo.UploadFileVO;
import com.ledger.common.annotation.Log;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.utils.file.FileUploadUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * 浏览器插件上传Controller
 *
 * @author ledger
 */
@Api(tags = "浏览器插件上传接口")
@RestController
@RequestMapping("/api/plugin")
public class ChromePluginController extends BaseController {


    // 定义允许的文件类型，仅允许zip文件
    private static final String[] PLUGIN_ALLOWED_EXTENSION = {"zip"};

    @Autowired
    private PluginConfig pluginConfig;

    @Autowired
    private LegerConfig legerConfig;

    @Autowired
    private ICtgLedgerConfigService ctgLedgerConfigService;

    /**
     * 上传浏览器插件
     */
    @ApiOperation("上传浏览器插件")
    @PreAuthorize("@ss.hasPermi('business:plugin:upload')")
    @PostMapping("/upload")
    public AjaxResult uploadPlugin(@RequestParam("file") MultipartFile file) {
        String uploadPath = pluginConfig.getConfig().stream().filter(c -> c.getEnv().equals(legerConfig.getEnv())).map(c -> c.getPath()).findFirst()
                .orElse(null);
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return error("上传文件不能为空");
            }

            // 上传文件，只允许zip格式
            String path = FileUploadUtils.uploadDirect(uploadPath, file, PLUGIN_ALLOWED_EXTENSION);
            return success(UploadFileVO.builder().path(path).build());
        } catch (Exception e) {
            logger.error("上传文件报错", e);
            return error("文件格式不正确，请上传zip格式的文件");
        }
    }

    @PreAuthorize("@ss.hasPermi('business:plugin:edit')")
    @Log(title = "更改插件配置", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("更改插件配置")
    public AjaxResult edit(@RequestBody PluginDTO pluginDTO) {
        CtgLedgerConfig queryParam = new CtgLedgerConfig();
        queryParam.setName(InitConstant.SYS_CONFIG_CHROME_PLUGIN);
        List<CtgLedgerConfig> lst = this.ctgLedgerConfigService.selectCtgLedgerConfigList(queryParam);
        CtgLedgerConfig ctgLedgerConfig = lst.stream().findFirst().orElse(null);
        boolean isNew = false;
        if (Objects.isNull(ctgLedgerConfig)) {
            ctgLedgerConfig = new CtgLedgerConfig();
            isNew = true;

        }
        ctgLedgerConfig.setName(InitConstant.SYS_CONFIG_CHROME_PLUGIN);
        ctgLedgerConfig.setConfigObjStr(JSON.toJSONString(pluginDTO));
        if (isNew) {
            ctgLedgerConfigService.insertCtgLedgerConfig(ctgLedgerConfig);
        } else {
            ctgLedgerConfigService.updateCtgLedgerConfig(ctgLedgerConfig);
        }
        pluginDTO = JSON.parseObject(ctgLedgerConfig.getConfigObjStr(), PluginDTO.class);


        return success(pluginDTO);
    }

    @PreAuthorize("@ss.hasPermi('business:plugin:query')")
    @Log(title = "查看插件配置", businessType = BusinessType.EXPORT)
    @GetMapping
    @ApiOperation("查看插件配置")
    public AjaxResult query() {
        CtgLedgerConfig queryParam = new CtgLedgerConfig();
        queryParam.setName(InitConstant.SYS_CONFIG_CHROME_PLUGIN);
        List<CtgLedgerConfig> lst = this.ctgLedgerConfigService.selectCtgLedgerConfigList(queryParam);
        CtgLedgerConfig ctgLedgerConfig = lst.stream().findFirst().orElse(null);
        PluginDTO pluginDTO = null;
        if (Objects.nonNull(ctgLedgerConfig)) {
            pluginDTO = JSON.parseObject(ctgLedgerConfig.getConfigObjStr(), PluginDTO.class);

        }


        return success(pluginDTO);
    }


}