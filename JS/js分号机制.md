### JavaScript 中的 -自动分号插入-机制（ASI）

**在不加分号语法会出错的时候，JS 解析的时候自动添加分号**

示例：

```js
let a
a
= 
1
console.log(a)
```

实际解析成

```js
let a; a = 1; console.log(a);
```

坑点

```js
let x = 1 + foo
(a+b).toString()
```

解析成

```js
let x = 1 + foo(a+b).toString();
```



类似效果的操作符还有这几个： `[`、`/`、`+`、`-` 。



解决方案

在这几个操作符开头的语句前面加一个 `;`：

```js
let x = 1 + foo
;(a+b).toString()
```

这代码丑陋

谁写的代码出现二异性就必须要由编写者来解决

不应该由另外的人或者程序来猜测

为了不让解释器或别人猜测代码的执行状况

请在js代码中需要添加分号的地方填写分号

出现bug 因为是奇葩的书写方式导致的，会很奔溃的

有可能出现bug，那就应该加



**还有几个流程控制作用关键词**

分别是：`return`、`throw`、 `yield` 、`break`、 `continue`

例如：

```
return 
true
```

解析成

```
return; true;
```







