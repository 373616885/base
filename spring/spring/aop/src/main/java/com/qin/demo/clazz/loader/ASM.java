package com.qin.demo.clazz.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ASM {

	public static void main(String[] args) throws Exception {
		ClassWriter classWriter = new ClassWriter(0);
		// 通过visit方法确定类的头部信息
		classWriter.visit(Opcodes.V1_8, // java版本
				Opcodes.ACC_PUBLIC, // 类修饰符
				"Programmer", // 类的全限定名
				null, "java/lang/Object", null);

		// 创建构造函数
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		// 定义code方法
		MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "code", "()V", null, null);
		methodVisitor.visitCode();
		methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		methodVisitor.visitLdcInsn("I'm a Programmer,Just Coding.....");
		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		methodVisitor.visitInsn(Opcodes.RETURN);
		methodVisitor.visitMaxs(2, 2);
		methodVisitor.visitEnd();
		classWriter.visitEnd();
		// 使classWriter类已经完成
		// 将classWriter转换成字节数组写到文件里面去
		byte[] data = classWriter.toByteArray();

		Path path = Paths.get("Programmer.class");

		Files.write(Paths.get("Programmer.class"), data, StandardOpenOption.CREATE);

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
