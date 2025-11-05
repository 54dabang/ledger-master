package com.ledger.business.config;
import com.ledger.business.config.po.ChatbotProperty;
import com.ledger.business.config.po.PluginProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "plugin")
@Data
@Slf4j
public class PluginConfig {
    private List<PluginProperty> config;
    @PostConstruct
    public void init() {
        log.info("plugin config loaded{}" , config);
    }
}
