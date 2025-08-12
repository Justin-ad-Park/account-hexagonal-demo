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


/**
 *  Bean 팩토리 설정
 *   - 스프링 컨테이너를 통해 빈을 가져옴
 *   - 싱글턴 보장(중복 생성 방지)
 */
@Configuration
public class FileAdapterConfig {

    /**
     * 해당 메서드의 리턴값을 스프링의 빈으로 등록
     * - 메서드 이름이 빈 이름이 됨
     * - 이 경우 빈 이름은 "accountsBeanPath", 타입 Path
     */
    @Bean
    @ConditionalOnMissingBean(Path.class)
    // 해당 타입의 빈이 아직 컨테이너에 없다면 이 빈을 등록해라.
    // Path 타입 빈이 없을 때만 "accountsBeanPath" 빈을 등록
    // 다른 설정/프로필/테스트에서 Path 빈을 제공하면 이 기본값은 비활성화
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
