package com.gen.ai.rag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vector-db")
@Getter
@Setter
public class DbConfig {
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
}
