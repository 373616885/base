### 基于tx标签的声明式事物

- bean

```java
public interface AccountService {
    void save();
}
```

```java
package com.example.jdbc.service;

import org.springframework.jdbc.core.JdbcTemplate;

public class AccountServiceImpl implements  AccountService {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String insert_sql = "INSERT INTO t_player (uid, name) VALUES ('10', 'a')";

    @Override
    public void save() {
        System.out.println("==开始执行sql");

        jdbcTemplate.update(insert_sql);

        System.out.println("==结束执行sql");

        System.out.println("==准备抛出异常");

        throw new RuntimeException("==手动抛出一个异常");
    }
}
```

- 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd">


    <!--事物管理器-->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!--数据源-->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://47.100.185.77:3306/qin?useSSL=false"/>
        <property name="username" value="root"/>
        <property name="password" value="373616885"/>
    </bean>

    <!--jdbcTemplate-->
    <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!--业务bean-->
    <bean id="accountService" class="com.example.jdbc.service.AccountServiceImpl">
        <property name="jdbcTemplate" ref="jdbcTemplate"/>
    </bean>

    <!--tx标签配置-->
    <tx:advice id="txAdvice" transaction-manager="transactionManager">
        <tx:attributes>
            <tx:method name="save*" propagation="REQUIRED"/>
            <tx:method name="delete*" propagation="REQUIRED"/>
            <tx:method name="update*" propagation="REQUIRED" rollback-for="Exception"/>
            <tx:method name="query*" propagation="NEVER"/>
            <tx:method name="*" read-only="true"/>
        </tx:attributes>
    </tx:advice>

    <!--aop配置-->
    <aop:config>
        <aop:pointcut id="txPointcut" expression="execution(* com.example.jdbc.service.*.*(..))"/>
        <aop:advisor advice-ref="txAdvice" pointcut-ref="txPointcut"/>
    </aop:config>
	<!-- 
	warning no match for this type name: com.example.jdbc.service [Xlint:invalidAbsoluteTypeName]
 	这个错误解决：
	里面的 expression="execution(* com.zrm.service.*(..))
	应该为 expression="execution(* com.zrm.service.*.*(..)) ,这样，切点才定位到方法上了。
	-->

</beans>
```

```java
@Test
public void xml() {
    // 基于tx标签的声明式事物
    ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-jdbc.xml");
    AccountService service = ctx.getBean("accountService", AccountService.class);
    service.save();
}
```





### 基于@Transactional注解的声明式事物

- bean

```java
package com.example.jdbc.service;

public interface AnnotationService {
    
    @Transactional
    void save();
}
```

```java
package com.example.jdbc.service;

import org.springframework.jdbc.core.JdbcTemplate;

public class AnnotationServiceImpl implements AnnotationService {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String insert_sql = "INSERT INTO t_player (uid, name) VALUES ('10', 'a')";

    @Override
    public void save() {
        System.out.println("==开始执行sql");

        jdbcTemplate.update(insert_sql);

        System.out.println("==结束执行sql");

        System.out.println("==准备抛出异常");

        throw new RuntimeException("==手动抛出一个异常");
    }
}
```

- 配置文件

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:tx="http://www.springframework.org/schema/tx"
         xsi:schemaLocation="http://www.springframework.org/schema/beans
         http://www.springframework.org/schema/beans/spring-beans.xsd
         http://www.springframework.org/schema/tx
         http://www.springframework.org/schema/tx/spring-tx.xsd">
  
      <!--开启tx注解-->
      <tx:annotation-driven transaction-manager="transactionManager"/>
  
      <!--事物管理器-->
      <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
          <property name="dataSource" ref="dataSource"/>
      </bean>
  
      <!--数据源-->
      <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
          <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
          <property name="jdbcUrl" value="jdbc:mysql://47.100.185.77:3306/qin?useSSL=false"/>
          <property name="username" value="root"/>
          <property name="password" value="373616885"/>
      </bean>
  
      <!--jdbcTemplate-->
      <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
          <property name="dataSource" ref="dataSource"/>
      </bean>
  
      <!--业务bean-->
      <bean id="annotationService" class="com.example.jdbc.service.AnnotationServiceImpl">
          <property name="jdbcTemplate" ref="jdbcTemplate"/>
      </bean>
  
  </beans>
  ```

测试

```java
@Test
public void annotation() {
    // 基于@Transactional注解的声明式事物
    ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-annotation.xml");
    AnnotationService service = ctx.getBean("annotationService", AnnotationService.class);
    service.save();
}
```

