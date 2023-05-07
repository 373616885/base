### 沙箱安全机制

沙箱安全机制：将java代码限定在jvm特定的运行范围中，并严格限制代码对本地系统资源访问，这样来保证代码的有效隔离，防止对本地系统造成破坏



例如：不同的类加载器加载不同模块的class

String，Object 这只能由启动类加载器加载



当前最新的安全机制实现，则引入了域 (Domain) 的概念。

虚拟机会把所有代码加载到不同的系统域和应用域，系统域部分专门负责与关键资源进行交互，而各个应用域部分则通过系统域的部分代理来对各种需要的资源进行访问。

虚拟机中不同的受保护域 (Protected Domain)，对应不一样的权限 (Permission)。存在于不同域中的类文件就具有了当前域的全部权限，如下图所示

![](images\image-20200705105151259.png)



最常用到的 API 就是 doPrivileged。doPrivileged 方法能够使一段受信任代码获得更大的权限



### java沙箱组成

基本组件：字节码检验器、类装载器、存取控制器、安全管理器、安全软件包。

1. 字节码校验器 bytecode verifier
   确保java类文件遵循java语言规范，帮助程序实现内存保护。并不是所有类都经过字节码校验器，如核心类。

2. 类加载器 class loader
   双亲委派机制、安全校验等，防止恶意代码干涉。守护类库边界。

3. 存取控制器 access controller
   它可以控制核心API对操作系统的存取权限，控制策略可以有由用户指定。

4. 安全管理器 security manager
   它是核心API和系统间的主要接口，实现权限控制，比存取控制器优先级高。

5. 安全软件包 secruity package 

   java.secruity下的类和扩展包下的类，允许用户为自己的应用增加新的安全特性。包括：安全提供者、消息摘要、数字签名、加密、鉴别等。



### 策略文件

全局默认的：$JREHOME/lib/security/default.policy，作用于 JVM 的所有实例

默认的情况下是不启动  security manager的

```
permission java.security.AllPermission;    //权限类型
permission java.lang.RuntimePermission "stopThread";    //权限类型+权限名
permission java.io.FilePermission "/tmp/foo" "read";    //权限类型+权限名+允许的操作
```



### 启动安全管理器

这个SecurityManager已经被声明弃用

启动安全管理有两种方式，建议使用启动参数方式

1 启动参数方式

```shell
-Djava.security.manager

## 若要同时指定配置文件的位置那么示例如下：
-Djava.security.manager -Djava.security.policy="E:/java.policy"
```

2 编码方式启动

```java
System.setSecurityManager(new SecurityManager());


// Finally, install a security manager if requested
String s = System.getProperty("java.security.manager");
if (s != null) {
    SecurityManager sm = null;
    if ("".equals(s) || "default".equals(s)) {
        sm = new java.lang.SecurityManager();
    } else {
        try {
            sm = (SecurityManager)loader.loadClass(s).newInstance();
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (ClassCastException e) {
        }
    }
    if (sm != null) {
        System.setSecurityManager(sm);
    } else {
        throw new InternalError(
            "Could not create SecurityManager: " + s);
    }
}




```





### 实战

默认情况下是不启用沙箱的并且SecurityManager以被声明抛弃



默认policy文件，支持"java.version"的不支持写操作

```shell
grant {
        ... ...       
        permission java.util.PropertyPermission "java.version", "read";
        ... ...
};
```



```java
public static void main(String... args) {
    String javaVersion=System.getProperty("java.version");
    System.err.println(javaVersion);
    System.setProperty("java.version","1.7.0_45");
    String javaNewVersion=System.getProperty("java.version");
    System.err.println(javaNewVersion);
  }
// 上面代码默认不报错的因为默认不启动

public static void main(String... args) {
    // 启用安全管理器
    System.setSecurityManager(new SecurityManager());
    String javaVersion=System.getProperty("java.version");
    System.err.println(javaVersion);
    System.setProperty("java.version","1.7.0_45");
    String javaNewVersion=System.getProperty("java.version");
    System.err.println(javaNewVersion);
  }

1.8.0_45
Exception in thread "main" java.security.AccessControlException: access denied ("java.util.PropertyPermission" "java.version" "write")
    at java.security.AccessControlContext.checkPermission(AccessControlContext.java:457)
    at java.security.AccessController.checkPermission(AccessController.java:884)
    at java.lang.SecurityManager.checkPermission(SecurityManager.java:549)
    at java.lang.System.setProperty(System.java:792)
    at test.Test.main(Test.java:9)


```



修改支持"java.version"的写操作

```java
grant {
        ... ...       
        permission java.util.PropertyPermission "java.version", "write";
        ... ...
};

此时再执行
public static void main(String... args) {
    // 启用安全管理器
    System.setSecurityManager(new SecurityManager());
    String javaVersion=System.getProperty("java.version");
    System.err.println(javaVersion);
    System.setProperty("java.version","1.7.0_45");
    String javaNewVersion=System.getProperty("java.version");
    System.err.println(javaNewVersion);
  }
输出结果为：
1.8.0_45
1.7.0_45    
```



可以支持"java.version"的写操作了



### 参数启动

java.security.manager"系统属性为空字符串""时会启用jdk的默认安全管理器SecurityManager

源码：sun.misc.Launcher

```java
public Launcher(){
        ExtClassLoader extclassloader;
        try {
            extclassloader = ExtClassLoader.getExtClassLoader();
        } catch(IOException ioexception) {
            throw new InternalError("Could not create extension class loader", ioexception);
        }
        try {
            loader = AppClassLoader.getAppClassLoader(extclassloader);
        } catch(IOException ioexception1) {
            throw new InternalError("Could not create application class loader", ioexception1);
        }
        Thread.currentThread().setContextClassLoader(loader);
        String s = System.getProperty("java.security.manager");
        if(s != null) {
            SecurityManager securitymanager = null;
            if("".equals(s) || "default".equals(s))
                securitymanager = new SecurityManager();
            else
                try {
                    securitymanager = (SecurityManager)loader.loadClass(s).newInstance();
                }
                catch(IllegalAccessException illegalaccessexception) { }
                catch(InstantiationException instantiationexception) { }
                catch(ClassNotFoundException classnotfoundexception) { }
                catch(ClassCastException classcastexception) { }
            if(securitymanager != null)
                System.setSecurityManager(securitymanager);
            else
                throw new InternalError((new StringBuilder()).append("Could not create SecurityManager: ").append(s).toString());
        }
    }
```











