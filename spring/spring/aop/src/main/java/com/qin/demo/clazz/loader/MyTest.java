package com.qin.demo.clazz.loader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyTest {
	public static void main(String[] args) throws Exception {
		Path path = Paths.get("Programmer.class");
		System.out.println(path.toUri().toString());
	}

}
