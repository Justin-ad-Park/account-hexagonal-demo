// src/main/java/com/example/account/adapter/out/file/FileAdapterConfig.java
package com.example.account.adapter.out.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 어댑터만 빈으로 노출합니다.
 * AccountService는 SaveAccountPort/LoadAccountPort 타입을 주입받지만,
 * 해당 타입을 구현한 어댑터(FileAccountPersistenceAdapter) 하나만 있으니
 * 충돌이 발생하지 않습니다.
 */
@Configuration
public class FileAdapterConfig {

    @Bean
    @ConditionalOnMissingBean(Path.class)
    public Path accountsBasePath() {
        return Paths.get("data");
    }

    @Bean
    @ConditionalOnMissingBean(FileAccountPersistenceAdapter.class)
    public FileAccountPersistenceAdapter fileAccountPersistenceAdapter(Path accountsBasePath) {
        return new FileAccountPersistenceAdapter(accountsBasePath);
    }

    // ✅ 아래 두 메서드는 제거하세요 (중복 후보 원인)
    // @Bean public LoadAccountPort loadAccountPort(FileAccountPersistenceAdapter adapter) { return adapter; }
    // @Bean public SaveAccountPort saveAccountPort(FileAccountPersistenceAdapter adapter) { return adapter; }
}
