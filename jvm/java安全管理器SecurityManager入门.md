### SecurityManager应用场景

当运行未知的Java程序的时候，该程序可能有恶意代码（删除系统文件、重启系统等），为了防止运行恶意代码对系统产生影响，需要对运行的代码的权限进行控

制，这时候就要启用Java安全管理器

### 管理器配置文件

默认的安全管理器配置文件是 $JAVA_HOME/jre/lib/security/java.policy，即当未指定配置文件时，将会使用该配置。内容如下：

```java
// Standard extensions get all permissions by default

grant codeBase "file:${{java.ext.dirs}}/*" {
    permission java.security.AllPermission;
};

// default permissions granted to all domains

grant { 
    // Allows any thread to stop itself using the java.lang.Thread.stop()
    // method that takes no argument.
    // Note that this permission is granted by default only to remain
    // backwards compatible.
    // It is strongly recommended that you either remove this permission
    // from this policy file or further restrict it to code sources
    // that you specify, because Thread.stop() is potentially unsafe.
    // See the API specification of java.lang.Thread.stop() for more
        // information.
    permission java.lang.RuntimePermission "stopThread";

    // allows anyone to listen on un-privileged ports
    permission java.net.SocketPermission "localhost:1024-", "listen";

    // "standard" properies that can be read by anyone

    permission java.util.PropertyPermission "java.version", "read";
    permission java.util.PropertyPermission "java.vendor", "read";
    permission java.util.PropertyPermission "java.vendor.url", "read";
    permission java.util.PropertyPermission "java.class.version", "read";
    permission java.util.PropertyPermission "os.name", "read";
    permission java.util.PropertyPermission "os.version", "read";
    permission java.util.PropertyPermission "os.arch", "read";
    permission java.util.PropertyPermission "file.separator", "read";
    permission java.util.PropertyPermission "path.separator", "read";
    permission java.util.PropertyPermission "line.separator", "read";

    permission java.util.PropertyPermission "java.specification.version", "read";
    permission java.util.PropertyPermission "java.specification.vendor", "read";
    permission java.util.PropertyPermission "java.specification.name", "read";

    permission java.util.PropertyPermission "java.vm.specification.version", "read";
    permission java.util.PropertyPermission "java.vm.specification.vendor", "read";
    permission java.util.PropertyPermission "java.vm.specification.name", "read";
    permission java.util.PropertyPermission "java.vm.version", "read";
    permission java.util.PropertyPermission "java.vm.vendor", "read";
    permission java.util.PropertyPermission "java.vm.name", "read";
};



```

例如：

对于java.net.SocketPermission，action可以是：listen，accept，connect，read，write；

对于java.io.FilePermission，action可以是：read, write, delete和execute。









### 启动安全管理器

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











