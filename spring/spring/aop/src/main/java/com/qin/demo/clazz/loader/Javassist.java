package com.qin.demo.clazz.loader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class Javassist {
	
	public static void main(String[] args) throws Exception {
		ClassPool pool = ClassPool.getDefault();  
        //创建Programmer类       
        CtClass cc= pool.makeClass("com.samples.Programmer");  
        //定义code方法  
        CtMethod method = CtNewMethod.make("public void code(){}", cc);  
        //插入方法代码  
        method.insertBefore("System.out.println(\"I'm a Programmer,Just Coding.....Javassist\");");  
        cc.addMethod(method);  
        //保存生成的字节码  
        cc.writeFile("D:\\Workspaces\\eclipse-oxygen\\Proxy");  
        
        
        Path path = Paths.get("com"+ File.separator +"samples"+ File.separator +"Programmer.class");
        
        byte[] b = Files.readAllBytes(path);// 默认
		MyClassLoader loader = new MyClassLoader();
		Class clazz = loader.defineMyClass(null, b, 0, b.length);
		// 测试加载是否成功，打印class 对象的名称
		System.out.println(clazz.getCanonicalName());
		// 实例化一个Programmer对象
		Object o = clazz.newInstance();

		// 调用Programmer的code方法
		clazz.getMethod("code", null).invoke(o, null);
        
	}

}
