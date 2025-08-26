package com.example.account.archinit;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * BASE_PACKAGE만 여러분 프로젝트의 루트 패키지로 바꿔서 사용하세요.
 * 예: "com.pulmuone.shop" 또는 기존 "Java 접근 제어 비교" 프로젝트의 루트 패키지
 */
@AnalyzeClasses(
        packages = ArchitectureTest.BASE_PACKAGE,
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class
        }
)
public class ArchitectureTest {

    // ✅ 여러분 프로젝트의 루트 패키지로 바꾸세요
    public static final String BASE_PACKAGE = "com.example.account";

    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String APP_PORT_IN = "..application..port..in..";
    private static final String APP_PORT_OUT = "..application..port..out..";
    private static final String APP_SERVICE = "..application..service..";
    private static final String ADAPTER = "..adapter..";
    private static final String ADAPTER_IN = "..adapter..in..";
    private static final String ADAPTER_OUT = "..adapter..out..";
    private static final String CONFIG = "..config..";

    private static final String SPRING = "..org.springframework..";
    private static final String JPA = "..jakarta.persistence..";

    /**
     * 1) 도메인은 어떤 레이어에도 의존하지 않는다 (순수성 보장)
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_others =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            APPLICATION, ADAPTER, CONFIG
                    );

    /**
     * 2) Application은 도메인에는 의존 가능하지만, 어댑터/설정에는 의존하지 않는다
     *    (유스케이스는 포트 인터페이스를 통해서만 바깥세상을 본다)
     */
    @ArchTest
    static final ArchRule application_should_depend_only_on_domain_or_itself =
            classes().that().resideInAPackage(APPLICATION)
                    .and().areNotAnnotatedWith(Configuration.class)
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            APPLICATION, DOMAIN, "java..", "jakarta..", "org.slf4j..", "org.springframework.."
                    );
//
    /**
     * 3) 인바운드 어댑터는 Application의 인바운드 포트에 의존할 수 있고,
     *    아웃바운드 어댑터는 Application의 아웃바운드 포트에 의존할 수 있다.
     *    (반대로 Application이 adapter.* 를 바라보면 안 됨)
     */
    @ArchTest
    static final ArchRule adapters_must_not_be_depended_on =
            noClasses().that().resideInAnyPackage(APPLICATION, DOMAIN, CONFIG)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER);
//
    @ArchTest
    static final ArchRule inbound_adapters_should_depend_on_app_in_ports_or_service =
            classes().that().resideInAPackage(ADAPTER_IN)
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            ADAPTER_IN, APPLICATION, APP_PORT_IN, APP_SERVICE, DOMAIN,
                            "java..", "javax..", "jakarta..", "org.slf4j..", "org.springframework.."
                    );

    @ArchTest
    static final ArchRule outbound_adapters_should_depend_on_app_out_ports =
            classes().that().resideInAPackage(ADAPTER_OUT)
                    .and().areNotAnnotatedWith(Configuration.class)
                    .and().areNotAnnotatedWith(Mapper.class)
                    .should().dependOnClassesThat().resideInAnyPackage(APP_PORT_OUT, DOMAIN);

    /**
     * 4) 도메인은 Spring/JPA 프레임워크에 의존하지 않는다 (순수 자바)
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring_or_jpa =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(SPRING, JPA);

    /**
     * 5) Application은 Adapter에 의존하지 않는다 (포트만 본다)
     */
    @ArchTest
    static final ArchRule application_should_not_depend_on_adapters =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER);

    /**
     * 6) 순환 의존 금지 (패키지 슬라이스 간)
     */
    @ArchTest
    static final ArchRule no_cycles_in_base_package =
            slices().matching(BASE_PACKAGE + ".(*)..")
                    .should().beFreeOfCycles();

    /**
     * 7) 포트/유스케이스 네이밍 규칙 (선택) — 팀 컨벤션에 맞게 조정
     */
    @ArchTest
    static final ArchRule ports_should_be_interfaces =
            classes().that().resideInAPackage(APP_PORT_IN)
                    .or().resideInAPackage(APP_PORT_OUT)
                    .should().beInterfaces();

    /**
     * 8) Service 접미사는 Service 패키지에만 허용
     */
    @ArchTest
    static final ArchRule usecase_implementations_should_reside_in_application_service =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage(APP_SERVICE);



//    /**
//     * 9) 레이어드 아키텍처 형태로도 한번 더 보수적으로 검증 (선택)
//     *    - domain <- application <- adapter 방향만 허용
//     */
//    @ArchTest
//    static final ArchRule layered_dependencies_are_respected =
//            layeredArchitecture()
//                    .consideringAllDependencies()
//                    .ignoreDependency(
//                            annotatedWith(Configuration.class)
//                                    .or(annotatedWith(SpringBootConfiguration.class))
//                                    .or(annotatedWith(EnableAutoConfiguration.class))
//                                    .or(annotatedWith(ConfigurationProperties.class))
//                                    .or(annotatedWith(TestConfiguration.class)),
//                            DescribedPredicate.alwaysTrue()
//                    )
//                    .layer("Domain").definedBy(DOMAIN)
//                    .layer("Application").definedBy(APPLICATION)
//                    .layer("Adapter").definedBy(ADAPTER)
//                    .layer("Config").definedBy(CONFIG)
//
//                    // 허용 규칙
//                    .whereLayer("Application").mayOnlyAccessLayers("Domain", "Application")
//                    .whereLayer("Adapter").mayOnlyAccessLayers("Application", "Domain", "Adapter", "Config")
//                    .whereLayer("Config").mayOnlyAccessLayers("Application", "Domain", "Adapter", "Config")
//
//                    // 금지 규칙
//                    .whereLayer("Domain").mayNotBeAccessedByAnyLayer()
//                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "Config", "Application");
//                    ;
//
//    public static DescribedPredicate<JavaClass> annotatedWith(final Class<? extends java.lang.annotation.Annotation> annotation) {
//        return new DescribedPredicate<JavaClass>("annotated with " + annotation.getSimpleName()) {
//            @Override
//            public boolean test(JavaClass input) {
//                return input.isAnnotatedWith(annotation);
//            }
//        };
//    }

}
