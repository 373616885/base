```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.16</version>
</dependency>

使用

 System.out.println(ClassLayout.parseInstance(obj).toPrintable());
```



