package com.ledger.business.config;
import com.ledger.business.config.po.ChatbotProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "chatbot")
@Data
@Slf4j
public class ChatbotConfig {
    //需要与application.yml实际名称一致，否则无法注入
    private List<ChatbotProperty> config;
    @PostConstruct
    public void init() {
        log.info("ChatbotConfig loaded{}" , config);
    }
}
