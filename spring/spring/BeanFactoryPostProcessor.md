### BeanFactoryPostProcessor 接口

**在bean  为实例化之前 给 bean工厂内所有的beandefinition 数据 一次修改属性的 机会**

**通过实现Ordered 接口来控制执行顺序**

接口定义：

```java
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
```

demo 1: 

```java
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /**
     * 主要是用来自定义修改持有的bean
     * ConfigurableListableBeanFactory 其实就是DefaultListableBeanDefinition对象
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("调用了自定义的BeanFactoryPostProcessor " + beanFactory);
        Iterator it = beanFactory.getBeanNamesIterator();

        String[] names = beanFactory.getBeanDefinitionNames();
        // 获取了所有的bean名称列表
        for(int i=0; i<names.length; i++){
            String name = names[i];

            BeanDefinition bd = beanFactory.getBeanDefinition(name);
            System.out.println(name + " bean properties: " + bd.getPropertyValues().toString());
            // 本内容只是个demo，打印持有的bean的属性情况
        }
    }
}

xml:
<bean id="customBeanFactoryPostProcessor" class="com.qin.demo.listener.CustomBeanFactoryPostProcessor" />
```

demo 2 :

```java

public class UpdateBeanFactoryPostProcessor  implements BeanFactoryPostProcessor, Ordered {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition bd = beanFactory.getBeanDefinition("myBean");
        MutablePropertyValues pv =  bd.getPropertyValues();
        if(pv.contains("testStr")){
            pv.addPropertyValue("testStr", "373616885");
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

xml:
<bean id="updateBeanFactoryPostProcessor" class="com.qin.demo.listener.UpdateBeanFactoryPostProcessor"/>
```

demo 3:

```java

public class ObscenityRemovingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private Set<String> obscentities = new HashSet<>();

    public void setObscentities(Set<String> obscentities) {
        this.obscentities = obscentities;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] names = beanFactory.getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition definition = beanFactory.getBeanDefinition(name);
            StringValueResolver resolver = strVal -> {
                System.out.println("***** " + strVal + " *****");
                if (this.obscentities.contains(strVal)) {
                    return "***** " + strVal + " *****";
                }
                return strVal;
            };
            BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(resolver);
            visitor.visitBeanDefinition(definition);
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}

xml:
<bean id="obscenityRemovingBeanFactoryPostProcessor" class="com.qin.demo.listener.ObscenityRemovingBeanFactoryPostProcessor">
        <property name="obscentities">
            <set>
                <value>qin</value>
                <value>jie</value>
                <value>peng</value>
                <value>373616885</value>
            </set>
        </property>
    </bean>
```

BeanFactoryPostProcessor 的典型应用： PropertyPlaceholderConfigurer

```xml
<bean id ="message" class="dist.config.HelloMessages">
    <property name="msg">
        <value>${bean.message}</value>
    </property>
</bean>

<bean id="mesHandler" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="locations">
        <value>classpath:bean.properties</value>
    </property>
</bean>


bean.message ="hi，can you find me?";
```

其中变量 ${bean.message}  和spring 是 分散配置的，那么 message 实例 如何找到 msg 属性的呢？

PropertyPlaceholderConfigurer 这个类 ，间接继承了 BeanFactoryPostProcessor 接口

在方法中先后调用了  mergeProperties();   convertProperties(); processProperties( ); 

三个方法就是改变



  

















