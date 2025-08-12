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

    @Bean
    public H2AccountPersistenceAdapter h2AccountPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        return new H2AccountPersistenceAdapter(jdbcTemplate);
    }

    // 참고: DataSource, JdbcTemplate는 spring-boot-starter-jdbc가 자동 구성해 줍니다.
    // (application.yml의 spring.datasource.* 값을 사용)
}
