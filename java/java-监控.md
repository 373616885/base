### SpringBoot项目配置JVM采集

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

```

```
spring.application.name=springboot_jvm
server.port=6001
management.endpoints.web.exposure.include=*
management.metrics.tags.application=${spring.application.name}  

```

```
@Bean
MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
    return registry -> registry.config().commonTags("application", applicationName);
}

```







### Prometheus 配置的Node Exporter服务

 

### 配置grafana







