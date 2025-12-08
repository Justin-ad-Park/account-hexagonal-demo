package com.example.account.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
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
    private static final String APP_PORT_ALL = "..application..port..";
    private static final String APP_PORT_IN_COMMAND = "..application..port..in..command..";
    private static final String APP_PORT_OUT = "..application..port..out..";
    private static final String APP_SERVICE = "..application..service..";
    private static final String ADAPTER = "..adapter..";
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
     * (유스케이스는 포트 인터페이스를 통해서만 바깥세상을 본다)
     */
    @ArchTest
    static final ArchRule application_should_depend_only_on_domain_or_itself =
            classes().that().resideInAPackage(APPLICATION)
                    //.and().areNotAnnotatedWith(Configuration.class)   //빈 주입을 위한 @Configuration 어노테이션이 붙은 경우는 제외할 때
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            APPLICATION, DOMAIN, "java..", "jakarta..", "org.."
                    );
//
    /**
     * 3) 인바운드 어댑터는 Application의 인바운드 포트에 의존할 수 있고,
     * 아웃바운드 어댑터는 Application의 아웃바운드 포트에 의존할 수 있다.
     * (반대로 Application이 adapter.* 를 바라보면 안 됨)
     */
    @ArchTest
    static final ArchRule adapters_must_not_be_depended_on =
            noClasses().that().resideInAnyPackage(APPLICATION, DOMAIN, CONFIG)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER);


    private static final String ADAPTER_IN = "..adapter..in..";
    private static final String APP_PORT_IN = "..application..port..in..";
    @ArchTest
    static final ArchRule inbound_adapters_should_depend_on_app_in_ports_or_service =
            classes().that().resideInAPackage(ADAPTER_IN)
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            ADAPTER_IN, APPLICATION, APP_PORT_IN, APP_PORT_IN_COMMAND, APP_SERVICE, DOMAIN,
                            "java..", "javax..", "jakarta..", "org..", "com..", "io.."
                    );


    private static final String ADAPTER_OUT = "..adapter..out..";
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
     * 5) 순환 의존 금지 (패키지 슬라이스 간)
     */
    @ArchTest
    static final ArchRule no_cycles_in_base_package =
            slices().matching(BASE_PACKAGE + ".(*)..")
                    .should().beFreeOfCycles();

    /**
     * 6) 포트유스케이스 규칙
     * --application port의 클래스는 (COMMAND 하위를 제외하고) 모두 인터페이스여야 함
     */
    @ArchTest
    static final ArchRule ports_should_be_interfaces =
            classes().that().resideInAPackage(APP_PORT_ALL)
                    .and(not(resideInAnyPackage(APP_PORT_IN_COMMAND))) // 🔹 command 패키지는 제외
                    .should().beInterfaces();

    /**
     * 7) 포트유스케이스 규칙을 다르게 표현한 방식
     */
//    @ArchTest
//    static final ArchRule ports_should_be_interfaces =
//            classes().that().resideInAPackage(APP_PORT_IN)
//                    .or().resideInAPackage(APP_PORT_OUT)
//                    .and(not(resideInAnyPackage(APP_PORT_IN_COMMAND))) // 🔹 command 패키지는 제외
//                    .should().beInterfaces();

    /**
     * 8) Service 접미사는 Service 패키지에만 허용
     */
    @ArchTest
    static final ArchRule usecase_implementations_should_reside_in_application_service =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage(APP_SERVICE);


    /**
     * 9) 프로덕션 클래스만 로드(테스트/외부 라이브러리 제외)
     */
    private JavaClasses loadProductionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    /**
     * 어댑터(in)의 하위 도메인은 도메인 모델에 직접 의존하지 않고,
     * 도메인 모델이 필요하면 dto 계층을 거쳐서 사용하도록 강제
     */
    private static final String DOMAIN_MODEL = "..domain..model..";
    private static final String ADAPTER_IN_REQUEST = "..adapter.in.web.dto.request..";
    private static final String ADAPTER_IN_RESPONSE = "..adapter.in.web.dto.response..";


    /**
     * 10) Adapter in의 Request DTO: domain 의존 금지
     */
    @ArchTest
    static final ArchRule request_dto_should_not_depend_on_domain =
            noClasses()
                    .that().resideInAPackage(ADAPTER_IN_REQUEST)
                    .should().dependOnClassesThat().resideInAPackage(DOMAIN_MODEL);

    /**
     * 11) Adapter in: 도메인 모델 직접 참조 금지
     *     단, web response DTO(adapter.in.web.dto.response)는 domain model 의존 허용
     */
    @ArchTest
    static final ArchRule inbound_adapters_should_not_depend_on_domain_model =
            noClasses()
                    .that().resideInAPackage(ADAPTER_IN)
                    .and(not(resideInAnyPackage(ADAPTER_IN_RESPONSE))) // response만 예외
                    .should().dependOnClassesThat().resideInAPackage(DOMAIN_MODEL);
}

