### Spring源码学习 —— ProxyConfig属性详解

```java
public class ProxyConfig implements Serializable {

    /** use serialVersionUID from Spring 1.2 for interoperability */
    private static final long serialVersionUID = -8409359707199703185L;

    // 标记是否直接对目标类进行代理，而不是通过接口产生代理
    // true : 使用 cglib 代理 false ：使用 jdk 代理
    private boolean proxyTargetClass = false;

    // 标记是否对代理进行优化。启动优化通常意味着在代理对象被创建后，增强的修改将不会生效，因此默认值为false。
    // 如果exposeProxy设置为true，即使optimize为true也会被忽略。
    // optimize属性与exposeProxy属性是不能兼容的
    // 只对CGLIB代理有效，对JDK动态代理无效（默认）
    private boolean optimize = false;
    
    // 标记是否需要阻止通过该配置创建的代理对象转换为Advised类型，默认值为false，表示代理对象可以被转换为Advised类型
    boolean opaque = false;

    // 标记代理对象是否应该被aop框架通过AopContext以ThreadLocal的形式暴露出去。
    // 当一个代理对象需要调用它自己的另外一个代理方法时，这个属性将非常有用。
    // 默认是是false，以避免不必要的拦截。
    boolean exposeProxy = false;

    // 标记该配置是否需要被冻结，如果被冻结，将不可以修改增强的配置。
    // 当我们不希望调用方修改转换成Advised对象之后的代理对象时，这个配置将非常有用。
    private boolean frozen = false;
}
```

