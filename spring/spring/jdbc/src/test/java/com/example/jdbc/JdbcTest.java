package com.example.jdbc;

import com.example.jdbc.service.AccountService;
import com.example.jdbc.service.AnnotationService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JdbcTest {

    @Test
    public void xml() {
        // 基于tx标签的声明式事物
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-jdbc.xml");
        AccountService service = ctx.getBean("accountService", AccountService.class);
        service.save();

    }

    @Test
    public void annotation() {
        // 基于@Transactional注解的声明式事物
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-annotation.xml");
        AnnotationService service = ctx.getBean("annotationService", AnnotationService.class);
        service.save();

    }

}
