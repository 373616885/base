### Set

Set 集合用于存储无序(存入和取出的顺序不一定相同)元素，值不能重复。对象的相等性本质是对象hashCode值（java是依据对象的内存地址计算出的此序号）判断的，如果想要让两个不同的对象视为相等的，就必须覆盖Object的hashCode方法和equals方 法。 

### HashSet（Hash 表） 

HashSet 存储无序值不能重复的数据。内部实现是 HashMap 的 key 来实现的

HashSet首先判断两个元素的hashCode，如果hashCode一样，接着会比较 equals方法 如果 equls结果为true ，HashSet就视为同一个元素。如果equals 为false就不是同一个元素。 

所以想要让两个不同的对象视为相等的，就必须覆盖Object的hashCode方法和equals方 法

```java
// 通过 HashMap 来实现
private transient HashMap<E,Object> map;

// 假设一个 默认值 
private static final Object PRESENT = new Object();
// 通过 HashMap 来实现
public HashSet() {
    map = new HashMap<>();
}
// 添加元素
public boolean add(E e) {
    // 通过HashMap内部的 Key 判断是否重复
    return map.put(e, PRESENT)==null;
}    

```

### TreeSet 

TreeSet 存储不可重复--但有序 的数据 默认是升序--可以自己实现覆写compare()函数

 Treeset能排序是因为底层是二叉树，数据越多越慢,TreeSet是依靠TreeMap来实现的 

TreeSet()是使用二叉树的原理对新add()的对象按照指定的顺序排序（升序、降序），每增 加一个对象都会进行排序，将对象插入的二叉树指定的位置。 

Integer和String对象都可以进行默认的TreeSet排序，而自定义类的对象是不可以的，自己定义的类必须实现Comparable接口，并且覆写相应的compareTo()函数，才可以正常使 用。 

