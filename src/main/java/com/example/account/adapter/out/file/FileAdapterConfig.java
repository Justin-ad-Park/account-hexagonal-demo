// src/main/java/com/example/account/adapter/out/file/FileAdapterConfig.java
package com.example.account.adapter.out.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 어댑터만 빈으로 노출합니다.
 * AccountService는 SaveAccountPort/LoadAccountPort 타입을 주입받지만,
 * 해당 타입을 구현한 어댑터(FileAccountPersistenceAdapter) 하나만 있으니
 * 충돌이 발생하지 않습니다.
 */


/**
 *  Bean 팩토리 설정
 *   - 스프링 컨테이너를 통해 빈을 가져옴
 *   - 싱글턴 보장(중복 생성 방지)
 */

/**
 * 기본값은 file. persistence.type=file 일 때만 활성화
 * matchIfMissing = true : 프로퍼티가 없을 경우에 이 Configuration을 사용하도록 설정
 */
@Configuration
@Profile("file")
//@ConditionalOnProperty(name = "persistence.type", havingValue = "file", matchIfMissing = true)
class FileAdapterConfig {

    @Bean
    @ConditionalOnMissingBean(Path.class)
    public Path accountsBasePath(
            @Value("${account.storage.path:data}") String storagePath
    ) {
        return Paths.get(storagePath);
    }

    @Bean
    @ConditionalOnMissingBean(FileAccountPersistenceAdapter.class)
    public FileAccountPersistenceAdapter fileAccountPersistenceAdapter(Path accountsBasePath) {
        return new FileAccountPersistenceAdapter(accountsBasePath);
    }
}
