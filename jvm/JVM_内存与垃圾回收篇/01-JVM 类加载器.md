### 类加载器的类型

Bootstrap class loader：虚拟机的内置类加载器，底层是用C++实现的，没有父加载器

Platform class loader：平台类加载器，负责加载JDK中一些特殊的模块

System class loader：系统类加载器，负责加载用户类路径上所指定的类库



```java
public static void main(String[] args) {
    System.out.println("*********启动类加载器************");
    
    ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
    System.out.println("应用类加载器：" + appClassLoader);
    
    ClassLoader platformClassLoader = ClassLoader.getPlatformClassLoader();
    System.out.println("平台类加载器：" + platformClassLoader);
    
    ClassLoader bootClassLoader = platformClassLoader.getParent();
    System.out.println("启动类加载器：" + bootClassLoader);
}


/**
 * app 类加载器
 */
public static void appClassLoader() {
    String property = System.getProperty("java.class.path");
    List<String> list = Arrays.asList(property.split(";"));
    list.forEach((t) -> {
        System.out.println("应用类加载器" + t);
    });
}
```



### 类加载器的加载的模块

java9模块化之后，对应的classloader加载各自对应的模块。

文档 ： https://openjdk.org/jeps/261

 platform class loader are:

```java
java.activation*            jdk.accessibility
java.compiler*              jdk.charsets
java.corba*                 jdk.crypto.cryptoki
java.scripting              jdk.crypto.ec
java.se                     jdk.dynalink
java.se.ee                  jdk.incubator.httpclient
java.security.jgss          jdk.internal.vm.compiler*
java.smartcardio            jdk.jsobject
java.sql                    jdk.localedata
java.sql.rowset             jdk.naming.dns
java.transaction*           jdk.scripting.nashorn
java.xml.bind*              jdk.security.auth
java.xml.crypto             jdk.security.jgss
java.xml.ws*                jdk.xml.dom
java.xml.ws.annotation*     jdk.zipfs
```

application class loader:

```java
jdk.aot                     jdk.jdeps
jdk.attach                  jdk.jdi
jdk.compiler                jdk.jdwp.agent
jdk.editpad                 jdk.jlink
jdk.hotspot.agent           jdk.jshell
jdk.internal.ed             jdk.jstatd
jdk.internal.jvmstat        jdk.pack
jdk.internal.le             jdk.policytool
jdk.internal.opt            jdk.rmic
jdk.jartool                 jdk.scripting.nashorn.shell
jdk.javadoc                 jdk.xml.bind*
jdk.jcmd                    jdk.xml.ws*
jdk.jconsole
```

bootstrap class loader:

```java
java.base                   java.security.sasl
java.datatransfer           java.xml
java.desktop                jdk.httpserver
java.instrument             jdk.internal.vm.ci
java.logging                jdk.management
java.management             jdk.management.agent
java.management.rmi         jdk.naming.rmi
java.naming                 jdk.net
java.prefs                  jdk.sctp
java.rmi                    jdk.unsupported
```



### 查看class加载的参数

-Xlog:class+load=info 

