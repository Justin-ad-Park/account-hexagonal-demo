
##  @SpringBootTest
- SpringBootTestContextBootstrapper → SpringBootContextLoader
- 전체 애플리케이션 컨텍스트(내장 서버 여부, webEnvironment)까지 고려
-SpringApplication 경로로 빈 등록/오토컨피그 전부 활성화

## WebMvcTest
- WebMvcTestContextBootstrapper
- MVC 관련 빈만 로딩하도록 슬라이스 필터링
- MockMvc 자동 구성, 컨트롤러/@ControllerAdvice / MappingJackson2HttpMessageConverter 등만 빈 로딩
- 서비스/리포지토리 등은 로딩되지 않음
- 필요 시 @MockBean으로 주입

