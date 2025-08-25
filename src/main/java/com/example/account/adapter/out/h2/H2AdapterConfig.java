// src/main/java/com/example/account/adapter/out/h2/H2AdapterConfig.java
package com.example.account.adapter.out.h2;

import com.example.account.adapter.out.h2.mapper.AccountMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * persistence.type=h2 일 때만 활성화
 * (yaml에서 전환)
 */
@Configuration
@Profile("h2")
class H2AdapterConfig {

    @Bean
    @ConditionalOnMissingBean(H2AccountPersistenceAdapter.class)
    public H2AccountPersistenceAdapter h2AccountPersistenceAdapter(AccountMapper mapper) {
        return new H2AccountPersistenceAdapter(mapper);
    }
}
