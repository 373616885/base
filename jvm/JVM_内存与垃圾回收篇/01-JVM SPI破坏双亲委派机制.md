### SPI

Service Provider Interface 服务提供接口

JDK SPI ：通过ServiceLoader获取 META-INF/services 文件夹下的文件加载实现类

文件名：接口类的 包名+类名

内容：实现类的 包名+类名



很多框架都有使用，例如 jdbc、sl4j、Dubbo、Spring  等，是一种基于接口编程的思想。

JDK中 jdbc 破坏双亲委派机制 

我们平时使用的 标准的jdbc接口

但它的实现类，是通过应用类加载器去加载的

矛盾点：要在BootstrapClassLoader加载的类里，调用AppClassLoader去加载实现类 就是破坏双亲委派机制



很多框架都有使用，例如 jdbc、sl4j、Dubbo、Spring  等，是一种基于接口编程的思想。

JDK中 jdbc 破坏双亲委派机制 

我们平时使用的 标准的jdbc接口

但它的实现类，是通过应用类加载器去加载的

矛盾点：要在BootstrapClassLoader加载的类DriverManager里，调用AppClassLoader去加载实现类

下面是相关代码流程

```java
        private static String className = "com.mysql.jdbc.Driver";
        private static String url = "jdbc:mysql://localhost:3306/jsd1307db";
        private static String username = "root";
        private static String pwd = "1234";		

		/*
		 * step1,加载驱动
		 * 	   JVM在加载OracleDriver类时,会执行
		 * 该类的static块(静态初始化块),static块
		 * 会完成驱动的注册。
		 * 可以不写这句：DriverManager里有确保机制 ensureDriversInitialized
		 */
		Class.forName(className);
		/*
		 * step2,获得连接
		 * 		getConnection方法要依据参数提供的信息，
		 * 建立与数据库之间的物理连接，并且返回
		 * 一个符合Connection接口要求的对象。
		 * ClassLoader classLoader = DriverManager.class.getClassLoader();
         * System.out.println("PlatformClassLoader : " + classLoader);
         * DriverManager是PlatformClassLoader加载器
         * 
         * 破坏双亲委派模型的情况:就是我们应用使用DriverManager
         * DriverManager由PlatformClassLoader去加载，里面居然使用了AppClassLoader加载的类
		 */
		Connection conn = 
			DriverManager.getConnection(
					url,username,pwd);
		System.out.println(conn);
		/*
		 * step3,创建Statement
		 */
		Statement stat = conn.createStatement();
		/*
		 * step4,执行sql,如果是查询，要处理
		 * 结果集。
		 */
		String sql = "select * from t_student";
		ResultSet rst = stat.executeQuery(sql);
		while(rst.next()){
			int id = rst.getInt("id");
			String name = rst.getString("name");
			int age = rst.getInt("age");
			System.out.println("id:" 
					+ id + " name:" + name + " age:" + age);
		}
		/*
		 * step5,关闭资源
		 */
		rst.close();
		stat.close();
		conn.close();



// static块加载驱动的注册
com.mysql.cj.jdbc.Driver
static {
    try {
        DriverManager.registerDriver(new Driver());
    } catch (SQLException var1) {
        throw new RuntimeException("Can't register driver!");
    }
}

// 获取连接时，是通过SPI机制获取实现类的
DriverManager.getConnection(url,username,pwd);

ensureDriversInitialized();

AccessController.doPrivileged(new PrivilegedAction<Void>() {
    public Void run() {
		// 这里就是SPI机制获取实现类：破坏了双亲委派机制
        // 由于上面的省去了Class.forName()
        // 这里nextProviderClass里面使用了：Class.forName(cn, false, loader);
        // 里面的loader 是 AppClassLoader 
        // ServiceLoader是启动类加载器：ServiceLoader里面使用 AppClassLoader 加载去加载实现类就是破坏双亲委派机制
        // 正常情况下，是拿不到的
        ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
        Iterator<Driver> driversIterator = loadedDrivers.iterator();
        try {
            while (driversIterator.hasNext()) {
                driversIterator.next();
            }
        } catch (Throwable t) {
            // Do nothing
        }
        return null;
    }
});

//使用 ：callerCL = AppClassLoader
ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
if (callerCL == null || callerCL == ClassLoader.getPlatformClassLoader()) {
    callerCL = Thread.currentThread().getContextClassLoader();
}

for (DriverInfo aDriver : registeredDrivers) {
    // isDriverAllowed里面使用 AppClassLoader 加载
    // 为了校验里面aClass =  Class.forName(driver.getClass().getName(), true, classLoader);
    // 做到了父级类加载器加载了子级路径中的类:也是破坏双亲委派模型  虽然只是为了校验
    if (isDriverAllowed(aDriver.driver, callerCL)) {
        try {
            println("    trying " + aDriver.driver.getClass().getName());
            Connection con = aDriver.driver.connect(url, info);
            if (con != null) {
                // Success!
                println("getConnection returning " + aDriver.driver.getClass().getName());
                return (con);
            }
        } catch (SQLException ex) {
            if (reason == null) {
                reason = ex;
            }
        }

    } else {
        println("    skipping: " + aDriver.getClass().getName());
    }

}

private static boolean isDriverAllowed(Driver driver, ClassLoader classLoader) {
    boolean result = false;
    if (driver != null) {
        Class<?> aClass = null;
        try {
            // 破坏双亲委派机制就在这里
            // AppClassLoader类加载器加载实现类并返回实例
            // 正常情况下：PlatformClassLoader加载器加载的DriverManager拿不到实现类的
            aClass =  Class.forName(driver.getClass().getName(), true, classLoader);
        } catch (Exception ex) {
            result = false;
        }

        result = ( aClass == driver.getClass() ) ? true : false;
    }

    return result;
}

```



Spring 也有这种基于接口编程的方式（SPI）

例如Spring自动配置加载

Spring会去加载 META-INF/spring.factories里面的 

org.springframework.boot.autoconfigure.EnableAutoConfiguration 属性来加载自动配置类



