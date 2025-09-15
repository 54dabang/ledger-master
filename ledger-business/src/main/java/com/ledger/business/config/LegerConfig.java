package com.ledger.business.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class LegerConfig {
    @Value("${ledger.env}")
    private String env;
}
