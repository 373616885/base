1. export与export default均可用于导出常量、函数、文件、模块等

2. 在一个文件或模块中，export  、import可以有多个，export default仅有一个

3. 通过export方式导出，在导入时要加{ }，export default则不需要

4. export能直接导出变量表达式，export default不行

5. export default 默认导出就不需要name了，但是一个js文件中只能有一个export default

6. as 别名 import * as mycow from './cow.js' 



demo1.js

```js
export const str = 'hello world'

export function f(a) {
  console.log(a)
}

```

对应的导入模式

demo2.js

```js
import { str, f } from './demo1.js'

console.log(str)
f()
```





demo1.js

```js
export default const str = 'hello world'
```

对应的导入模式

demo2.js

```
import str from './demo1.js'
console.log(str)
```



虽然export default只能有一个，但也可以导出多个方法

cow.js

```js
export default {
  speak () {
    return 'moo'
  },
  eat () {
    return 'cow eats'
  },
  drink () {
    return 'cow drinks'
  }
}
```

引入与命名空间引入类似

```js

import cow from './cow.js'

cow.speak() // moo

```

别名 

```js
import * as mycow from './cow.js' // import this file into mycow 

mycow.speak() // moo

```

#### 暴露方式

1.分别暴露
这是我个人的export.js 文件

```js
export let obj={
    name:"导出"
}

export const fuc=()=>{
    console.log('导出')
}

```

2.统一暴露
将需要暴露方法写在export对象内

```js
let obj={
    name:"导出"
}

 const fuc=()=>{
    console.log('导出')
}
export{
    obj,
    fuc,
}
```


3.默认暴露

```js
export default{
    obj={
        name:"导出"
    },
    fuc:function(){
        console.log('导出')
    }
}
```


注：通用引入方式调用时需加default属性

```js
javascript复制代码//这里是通用引用方式
import * as ex from ‘./js/export.js’ //个人文件夹
console.log(ex.default.obj.name) //导出
```



引入方式
1.通用引入
//使用关键字as引入

```js
import * as ex from './js/export.js' //个人文件夹
console.log(ex.obj.name) //导出
```


ps:将export文件内的所有方法以 ex的属性来引入调用

2.解构赋值

```js
import { obj,fuc}from './js/export.js'
```


ps:将需要用的方法分别以解构赋值的方式引入

默认暴露的解构赋值引入写法

```js
import {default as ex} from './js/export.js' 
console.log(ex.obj.name)//导出
```


ps:这种方式引用，调用时不需要加default

3.简便形式 （只针对默认暴露）

```js
import ex from './js/export.js'  
console.log(ex.obj.name)//导出
```

