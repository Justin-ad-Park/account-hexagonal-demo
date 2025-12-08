package com.example.account.archunit;

//public class ArchunitTest_Backup {
//    /**
//     * 9) @DomainCommand이 붙은 클래스는 서비스 패키지에서만 의존해야 한다.
//     * (즉, AccountCommands 등은 application.service 안에서만 사용할 수 있다)
//     *
//     * 테스트 방법 : AbuseControllerForArchunit의 주석을 풀어 AccountCommands를 직접 사용하도록 만든 후 테스트
//     */
//    @Test
//    void forbid_using_DomainCommand_outside_service_package() {
//        JavaClasses classes = loadProductionClasses();
//
//        ArchRule rule = noClasses()
//                .that().resideOutsideOfPackage(APP_SERVICE)
//                .should().dependOnClassesThat().areAnnotatedWith(DomainCommand.class);
//
//        rule.check(classes);
//    }

//    /**
//     * 10)@DomainCommand 어노테이션 사용 위반 검출
//     * @DomainCommand는 오직 application.service 패키지만 사용해야 한다.
//     *
//     *      * 테스트 방법
//     *          1) AbuseControllerForArchunit의 주석을 풀어 AccountCommands를 직접 사용하도록 만든 후 테스트
//     *          2) 다른 패키지에 어노테이션을 붙여 테스트
//     */
//    @Test
//    void only_services_may_depend_on_DomainCommand_types() {
//        JavaClasses classes = loadProductionClasses();
//
//        ArchRule rule = classes()
//                .that().areAnnotatedWith(DomainCommand.class)
//                .should().onlyHaveDependentClassesThat().resideInAnyPackage(APP_SERVICE);
//
//        rule.check(classes);
//    }
//}
