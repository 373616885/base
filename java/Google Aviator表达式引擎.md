Aviator是一个用于动态[表达式求值](https://so.csdn.net/so/search?q=表达式求值&spm=1001.2101.3001.7020)的高性能、轻量级Java引擎

财务系统中的动态计算



```xml
<!-- https://mvnrepository.com/artifact/com.googlecode.aviator/aviator -->
		<dependency>
			<groupId>com.googlecode.aviator</groupId>
			<artifactId>aviator</artifactId>
			<version>5.4.3</version>
		</dependency>

```



##### 简单的使用

```java
    @Test
    public void test1() {
        //发送规则表达式
        String expression = "(age > 35 && age < 75) && avgYear > 10000 && level == 1";
        //编译表达式
        Expression compileExp = AviatorEvaluator.compile(expression);
        //设置变量
        Map<String, Object> map = new HashMap<>();
        map.put("age", 40);
        map.put("level", 2);
        map.put("avgYear", 20000);
        //执行表达式
        System.out.println(compileExp.execute(map));//结果: false
    }

	//表达式传值
	@Test
    public void test(){
        //表达式传值
        Map<String, Object> env = new HashMap<>();
        env.put("name", "world");
        String str = "'hello ' + name";
        String r  = (String) AviatorEvaluator.execute(str, env);
        System.out.println(r);//hello world
    }
	
	@Test
    public void test3(){
        //算数表达式
        Long sum = (Long) AviatorEvaluator.execute("1 + 2 + 3");
        System.out.println(sum);//6
        //逻辑表达式
        boolean result = (boolean) AviatorEvaluator.execute("3 > 1");
        System.out.println(result);//true
        String r1  = (String) AviatorEvaluator.execute("100 > 80 ? 'yes' : 'no'");
        System.out.println(r1);//yes
    }

	//函数调用
	 @Test
    public void function() {
        //函数调用
        Long r2  = (Long) AviatorEvaluator.execute("string.length('hello')");
        System.out.println(r2);//5
        //调用自定义函数
        //注册函数
        AviatorEvaluator.addFunction(new CustomFunction());
        //调用函数
        System.out.println(AviatorEvaluator.execute("add(2,3)"));//5.0
        //删除函数
        //AviatorEvaluator.removeFunction("multi");
    }

```



##### 规则引擎

```java
public class AviatorExampleTwo {

    //规则可以保存在数据库中，mysql或者redis等等
    Map<Integer, String> ruleMap = new HashMap<>();
    public AviatorExampleTwo() {
        //秒数计算公式
        ruleMap.put(1, "hour * 3600 + minute * 60 + second");
        //正方体体积计算公式
        ruleMap.put(2, "height * width * length");
        //判断一个人是不是资深顾客
        ruleMap.put(3, "age >= 18 && sumConsume > 2000 && vip");
        //资深顾客要求修改
        ruleMap.put(4, "age > 10 && sumConsume >= 8000 && vip && avgYearConsume >= 1000");
        //判断一个人的年龄是不是大于等于18岁
        ruleMap.put(5, "age  >= 18 ? 'yes' : 'no'");
    }

    public Object getResult(int ruleId, Object... args) {
        String rule = ruleMap.get(ruleId);
        return AviatorEvaluator.exec(rule, args);
    }
    
    public static void main(String[] args) {
        AviatorExampleTwo aviatorExample = new AviatorExampleTwo();
        //选择规则，传入规则所需要的参数
        System.out.println("公式1：" + aviatorExample.getResult(1, 1, 1, 1));
        System.out.println("公式2：" + aviatorExample.getResult(2, 3, 3, 3));
        System.out.println("公式3：" + aviatorExample.getResult(3, 20, 3000, false));
        System.out.println("公式4：" + aviatorExample.getResult(4, 23, 8000, true, 2000));
        System.out.println("公式5：" + aviatorExample.getResult(5, 12));
    }
}


```

