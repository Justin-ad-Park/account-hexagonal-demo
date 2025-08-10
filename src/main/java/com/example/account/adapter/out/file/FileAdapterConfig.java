package com.example.account.adapter.out.file;

import com.example.account.application.port.out.LoadAccountPort;
import com.example.account.application.port.out.SaveAccountPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 어댑터를 Out Port에 연결하는 프로덕션 구성.
 * 테스트에서 동일 타입 빈이 제공되면(예: FileAccountPersistenceAdapter),
 * @ConditionalOnMissingBean 덕분에 프로덕션 빈 생성이 건너뛰어집니다.
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

    @Bean
    public LoadAccountPort loadAccountPort(FileAccountPersistenceAdapter adapter) { return adapter; }

    @Bean
    public SaveAccountPort saveAccountPort(FileAccountPersistenceAdapter adapter) { return adapter; }
}
