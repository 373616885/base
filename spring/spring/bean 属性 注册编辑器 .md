### 1 . 使用自定义属性编辑器

1. 使用自定义属性编辑器，通过继承 PropertyEditorSupport，重写 setAsText 方法

```java

public class DatePropertyEditor  extends java.beans.PropertyEditorSupport {

    private String format = "yyyy-MM-dd";

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        System.out.println("原始值： " + text);
        SimpleDateFormat sdf =new SimpleDateFormat(format) ;
        try {
            Date date = sdf.parse(text);
            this.setValue(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
```

​	2. 将自定义属性编辑器注册到 Spring 中 

```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
        <property name="customEditors">
            <map>
                <entry key="java.util.Date" 		value="com.qin.demo.config.DatePropertyEditor">
                </entry>
            </map>
        </property>
    </bean>
```

​		在配置文件中引人类型为 org.Springframework.beans.factory.config.CustomEditorConfigurer 的 bean，并在属性 customEditors 中加入自定义的属性编辑器，其中 key 为属性编辑器所对应 的类型。 

​		通过这样的配置，当 Spring 在注入 bean 的属性时一旦遇到了 java. util.Date 类型的属 性会自动调用自定义的 DatePropertyEditor 解析器进行解析，并用解析结果代替配置属性进行 注人

​		

###  2.注册 Spring 自带的属性编辑器 CustomDateEditor

1.  定义属性编辑器 -- 将Date.class 和 Spring 自带的属性编辑器 CustomDateEditor 注册到 

   org.springframework.beans.factory.config.CustomEditorConfigurer 的 propertyEditorRegistrars

   属性中

   ```java
   public class DatePropertyEditorRegistrar implements PropertyEditorRegistrar {
   
       private String format = "yyyy-MM-dd";
   
       @Override
       public void registerCustomEditors(PropertyEditorRegistry registry) {
           registry.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(format), true));
       }
   }
   ```

2. 注册到spring中

   ```xml
   <bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
       <property name="propertyEditorRegistrars">
           <list>
               <bean class="com.qin.demo.config.DatePropertyEditorRegistrar"/>
           </list>
       </property>
   </bean>
   ```

   

   ​		通过在配置文件中将自定义的 DatePropertyEditorRegistrar 注册进入 org.springframework.beans.factory.config.CustomEditorConfigurer 的 propertyEditorRegistrars 属性中 

   也可以实现 Date 类型转换



### 深入分析Spring属性编辑器



####  PropertyEditor 接口

PropertyEditor是属性编辑器的接口，将外部设置值转换为内部JavaBean属性值的转换接口方法。

PropertyEditor主要的接口方法说明如下： 

- Object getValue();   返回属性的当前值。基本类型被封装成对应的封装类实例
- void setValue(Object value);  设置属性的值，基本类型以封装类传入；
- String getAsText()：将属性对象用一个字符串表示，以便外部的属性编辑器能以可视化的方式显示。缺省返回null，表示该属性不能以字符串表示
- void setAsText(String text)：用一个字符串去更新属性的内部值，这个字符串一般从外部属性编辑器传入；
- String[] getTags()：返回表示有效属性值的字符串数组（如boolean属性对应的有效Tag为true和false），以便属性编辑器能以下拉框的方式显示出来。缺省返回null，表示属性没有匹配的字符值有限集合；
-  String getJavaInitializationString()：为属性提供一个表示初始值的字符串，属性编辑器以此值作为属性的默

#### PropertyEditorSupport

Java为PropertyEditor提供了一个方便类：PropertyEditorSupport，该类实现了PropertyEditor接口并提供默认实现，一般情况下，**用户可以通过扩展这个方便类设计自己的属性编辑器**。



#### Spring 属性编辑器

Spring框架内置了一些PropertyEditor ，并且bean填充属性之前都会把初始化把 这些PropertyEditor添加到

AbstractBeanFactory的 propertyEditorRegistrars 和 customEditors 中

1. 实例化bean并使用 BeanWrapper包装Bean实例后，调用initBeanWrapper() 

2. 接着调用 initBeanWrapper(bw);

3. 里面 registerCustomEditors(bw);

4. !this.propertyEditorRegistrars.isEmpty() 将 this.propertyEditorRegistrars执行registerCustomEditors

5. !this.customEditors.isEmpty() 将 registry.registerCustomEditor

   ```java
   //实例化bean并使用 BeanWrapper包装Bean实例后 无参构造器 instantiateBean(beanName, mbd)
   instantiateBean(final String beanName, final RootBeanDefinition mbd) {
       BeanWrapper bw = new BeanWrapperImpl(beanInstance);
   	initBeanWrapper(bw);
   }
   protected void initBeanWrapper(BeanWrapper bw) {
       bw.setConversionService(getConversionService());
       registerCustomEditors(bw);
   }
   
   protected void registerCustomEditors(PropertyEditorRegistry registry) {
       
   		PropertyEditorRegistrySupport registrySupport =
   				(registry instanceof PropertyEditorRegistrySupport ? 
                   (PropertyEditorRegistrySupport) registry : null);
       
   		if (registrySupport != null) {
   			registrySupport.useConfigValueEditors();
   		}
       
   		if (!this.propertyEditorRegistrars.isEmpty()) {
   			for (PropertyEditorRegistrar registrar : this.propertyEditorRegistrars) {
   				// 注册 AbstractBeanFactory.propertyEditorRegistrars 属性的 
                   // 用户自定义的PropertyEditorRegistrar 都放到了这里
                   // 实现了PropertyEditorRegistrar接口的bean
                   registrar.registerCustomEditors(registry);
   				
   			}
   		}
       	
   		if (!this.customEditors.isEmpty()) {
               // 注册 AbstractBeanFactory.customEditors 属性的 
               // 一般都是继承PropertyEditorSupport 的Bean
   			this.customEditors.forEach((requiredType, editorClass) ->
   					registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass)));
   		}
   	}
   
   
   ```

   

此外还内置了一些Resource相关的PropertyEditor，代码在ResourceEditorRegistrar类的registerCustomEditors方法中：

AbstractApplicationContext.refresh()-->prepareBeanFactory()中注册了ResourceEditorRegistrar

1. prepareBeanFactory(beanFactory); 填充BeanFactory功能 
2. beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));
3. ResourceEditorRegistrar.registerCustomEditors() 里面有一些

```java
@Override
public void registerCustomEditors(PropertyEditorRegistry registry) {
    ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
    doRegisterEditor(registry, Resource.class, baseEditor);
    doRegisterEditor(registry, ContextResource.class, baseEditor);
    doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
    doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
    doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
    doRegisterEditor(registry, Path.class, new PathEditor(baseEditor));
    doRegisterEditor(registry, Reader.class, new ReaderEditor(baseEditor));
    doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));

    ClassLoader classLoader = this.resourceLoader.getClassLoader();
    doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
    doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
    doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));

    if (this.resourceLoader instanceof ResourcePatternResolver) {
        doRegisterEditor(registry, Resource[].class,
                         new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, this.propertyResolver));
    }
}

/**
	 * Override default editor, if possible (since that's what we really mean to do here);
	 * otherwise register as a custom editor.
	 */
private void doRegisterEditor(PropertyEditorRegistry registry, Class<?> requiredType, PropertyEditor editor) {
    if (registry instanceof PropertyEditorRegistrySupport) {
        ((PropertyEditorRegistrySupport) registry).overrideDefaultEditor(requiredType, editor);
    }
    else {
        // 放到  AbstractBeanFactory.customEditors 里面
        registry.registerCustomEditor(requiredType, editor);
    }
}
```



Spring 获取 默认的Bean属性编辑器的时候 

```java
@Nullable
public PropertyEditor getDefaultEditor(Class<?> requiredType) {
    if (!this.defaultEditorsActive) {
        return null;
    }
    if (this.overriddenDefaultEditors != null) {
        PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
        if (editor != null) {
            return editor;
        }
    }
    if (this.defaultEditors == null) {
        createDefaultEditors();
    }
    return this.defaultEditors.get(requiredType);
}


/**
 * Actually register the default editors for this registry instance.
 */
private void createDefaultEditors() {
    this.defaultEditors = new HashMap<>(64);

    // Simple editors, without parameterization capabilities.
    // The JDK does not contain a default editor for any of these target types.
    this.defaultEditors.put(Charset.class, new CharsetEditor());
    this.defaultEditors.put(Class.class, new ClassEditor());
    this.defaultEditors.put(Class[].class, new ClassArrayEditor());
    this.defaultEditors.put(Currency.class, new CurrencyEditor());
    this.defaultEditors.put(File.class, new FileEditor());
    this.defaultEditors.put(InputStream.class, new InputStreamEditor());
    this.defaultEditors.put(InputSource.class, new InputSourceEditor());
    this.defaultEditors.put(Locale.class, new LocaleEditor());
    this.defaultEditors.put(Path.class, new PathEditor());
    this.defaultEditors.put(Pattern.class, new PatternEditor());
    this.defaultEditors.put(Properties.class, new PropertiesEditor());
    this.defaultEditors.put(Reader.class, new ReaderEditor());
    this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
    this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
    this.defaultEditors.put(URI.class, new URIEditor());
    this.defaultEditors.put(URL.class, new URLEditor());
    this.defaultEditors.put(UUID.class, new UUIDEditor());
    this.defaultEditors.put(ZoneId.class, new ZoneIdEditor());

    // Default instances of collection editors.
    // Can be overridden by registering custom instances of those as custom editors.
    this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
    this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
    this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
    this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
    this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

    // Default editors for primitive arrays.
    this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
    this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

    // The JDK does not contain a default editor for char!
    this.defaultEditors.put(char.class, new CharacterEditor(false));
    this.defaultEditors.put(Character.class, new CharacterEditor(true));

    // Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
    this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
    this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

    // The JDK does not contain default editors for number wrapper types!
    // Override JDK primitive number editors with our own CustomNumberEditor.
    this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
    this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
    this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
    this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
    this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
    this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
    this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
    this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
    this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
    this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
    this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
    this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
    this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
    this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

    // Only register config value editors if explicitly requested.
    if (this.configValueEditorsActive) {
        StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
        this.defaultEditors.put(String[].class, sae);
        this.defaultEditors.put(short[].class, sae);
        this.defaultEditors.put(int[].class, sae);
        this.defaultEditors.put(long[].class, sae);
    }
}
```



**AbstractBeanFactory Bean工厂中提供了customEditors自定义编辑器注册清单存储**

**在激活各种 BeanFactory 处理器 调用  invokeBeanFactoryPostProcessors(beanFactory); **

**调用 beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);**

**获取配置文件的 BeanFactoryPostProcessor 类型的beanName**

**接着实例化 ：orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));**

**实例化bean并使用 BeanWrapper包装Bean实例后，调用initBeanWrapper() 里面 执行** **registerCustomEditors(bw) 将 默认的ResourceEditorRegistrar 放到 CustomEditorConfigurer 的this.propertyEditorRegistrars** 

**接着调用 invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);**

**调用了CustomEditorConfigurer（BeanFactoryPostProcessor  类型的）中的postProcessBeanFactory()方法**

**将配置的属性编辑器注册至 AbstractBeanFactory中的customEditors 和 propertyEditorRegistrars 中**



```java

@Override
public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    if (this.propertyEditorRegistrars != null) {
        for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
            beanFactory.addPropertyEditorRegistrar(propertyEditorRegistrar);
        }
    }
    if (this.customEditors != null) {
        this.customEditors.forEach(beanFactory::registerCustomEditor);
    }
}
```

**编辑器注册后，在使用时属性填充的时候通过findCustomEditor()指定属性类型，查找默认和自定义属性编辑器将字面量的值转换为属性定义的类型对象**

```java
populateBean(beanName, mbd, instanceWrapper);

applyPropertyValues(beanName, mbd, bw, pvs);

Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
Object convertedValue = resolvedValue;
boolean convertible = bw.isWritableProperty(propertyName) &&
    !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
// 是否需要转换
if (convertible) {
    // 类型转换
    convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
}

// 获取指定的属性编辑器
PropertyEditor editor = 
    this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);
// 转换
editor.setAsText(newTextValue);

 
```





## 总结

```java
1. 在填充 BeanFactory 里面 prepareBeanFactory(beanFactory);
2. 添加默认的 ResourceEditorRegistrar 属性编辑器
3. beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment())); 
4，将ResourceEditorRegistrar放到 AbstractBeanFactory.propertyEditorRegistrars 属性
4. 在激活BeanFactoryPostProcessor 处理器 invokeBeanFactoryPostProcessors(beanFactory);
5. 先在配置信息找打到自定义的 BeanPostProcessor 的beanName
6. 在实例化自定义的Bean中
7. 调用initBeanWrapper() 
8. 接着调用 registerCustomEditors(bw); 
9. 将 ResourceEditorRegistrar 的属性编辑器放到 
10. 放到每个BeanWrapper里面的 PropertyEditorRegistrySupport的overriddenDefaultEditors 里面
11. 在激活里 invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory); 
12. 调用 CustomEditorConfigurer.postProcessBeanFactory()
13. 将自定义的 datePropertyEditorRegistrar 
14. 放到AbstractBeanFactory.propertyEditorRegistrars 属性中
15. 下次 实例化的时候调用 initBeanWrapper() 的时候
16. 将ResourceEditorRegistrar放到PropertyEditorRegistrySupport的overriddenDefaultEditors 
17. 将datePropertyEditorRegistrar放到PropertyEditorRegistrySupport.customEditors
18. 使用就是在属性注入的时候使用 findCustomEditor() 找到对应得属性编辑器
19. 转换 editor.setAsText(newTextValue);


```

























