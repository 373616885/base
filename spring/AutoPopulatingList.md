# AutoPopulatingList

**AutoPopulatingList**

防止下标越界的

某个下标可能有数据，也可能没有数据，ArrayList 没有数据就报数组下标越界

AutoPopulatingList：则自动给个无参构造的默认值

```java
public static void main(String[] args) {
		//SpringApplication.run(BootApplication.class, args);
		AutoPopulatingList<User> populatingList = new AutoPopulatingList<>(User.class);

		User s = populatingList.get(2);
		System.out.println(s);

		for (User string : populatingList) {
			System.out.println(string);
		}

		List<User> arrayList = new ArrayList<>();

		arrayList.get(3);

	}
```





AutoPopulatingList<E> implements List<E>, Serializable

获取元素时 get(int) 若为null则自动创建元素并填充的List

 

内部的List<E> backingList是实际的容器，add、remove、clear等等方法都是通过委托backingList完成的

 

ElementFactory<E> elementFactory是元素工厂，默认使用内部实现类ReflectiveElementFactory，也可以构造时给定

 

ReflectiveElementFactory生产元素要求元素类有无参构造方法

 

E get(int index) 当index的元素是null时使用elementFactory创建元素并填充；允许越界，越界是使用null填充越界之前的元素位，index的位置则创建元素并填充



这个类，是线程不安全的，AutoPopulatingList类基本上都是在操作backingList，backingList是final的，所以任何修改都会对其他使用的地方产生影响。



