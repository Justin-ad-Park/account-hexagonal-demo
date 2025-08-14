// src/main/java/com/example/account/adapter/out/h2/H2AdapterConfig.java
package com.example.account.adapter.out.h2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * persistence.type=h2 일 때만 활성화
 * (yaml에서 전환)
 */
@Configuration
@ConditionalOnProperty(name = "persistence.type", havingValue = "h2")
public class H2AdapterConfig {
    // 별도 Bean 필요 없음 (MyBatis Starter가 SqlSessionFactory/DataSource 연동)
}
