实体类A中有password和confirmPassword两个属性

如何优雅得校验这两个属性是否相等

```java
package com.qin.demo.controller;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

@Data
@ScriptAssert(lang = "javascript", script ="_this.password.equals(_this.confirmPassword)"  ,message = "password and confirmPassword diff")
public class User {

    @NotBlank(message = "password is null")
    private String password;

    @NotBlank(message = "confirmPassword is null")
    private String confirmPassword;

    @AssertTrue(message = "password and confirmPassword diff")
    private boolean isPasswordMatch() {
        boolean equals = StringUtils.equals(this.password, this.confirmPassword);
        return equals;
    }

}

```



方法一

需要注意：返回值只能是 boolean   ，Boolean 都不行  

```java
@AssertTrue(message = "password and confirmPassword diff")
private boolean isPasswordMatch() {
    boolean equals = StringUtils.equals(this.password, this.confirmPassword);
    System.out.println(equals);
    return equals;
}
```



方法二

```java
@ScriptAssert(lang = "javascript", script ="_this.password.equals(_this.confirmPassword)"  ,message = "password and confirmPassword diff")
```

需要引入：js 引擎 nashorn  

```xml
 <!-- https://mvnrepository.com/artifact/org.openjdk.nashorn/nashorn-core -->
<dependency>
    <groupId>org.openjdk.nashorn</groupId>
    <artifactId>nashorn-core</artifactId>
    <version>15.4</version>
</dependency>
```



方法三

自定义一个检验器

1. 自定义注解

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface PasswordEqual {
    String message() default "密码和确认密码不一致";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```



2. 自定义校验器

```java
public class PasswordValidator implements ConstraintValidator<PasswordEqual, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(value);
        String password = (String) beanWrapper.getPropertyValue("password");
        String checkPassword = (String) beanWrapper.getPropertyValue("checkPassword");
        return password != null && password.equals(checkPassword);
    }
}
```



3. 在实体类中使用自定义注解

```java
@PasswordEqual
public class User {
    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;

    // getter and setter
}
```

这样，在校验实体类时，就会自动校验password和checkPassword两个属性是否相等。如果不相等，则会抛出校验异常，提示“密码和确认密码不一致”。