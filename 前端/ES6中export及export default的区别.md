1. export与export default均可用于导出常量、函数、文件、模块等

2. 在一个文件或模块中，export  、import可以有多个，export default仅有一个

3. 通过export方式导出，在导入时要加{ }，export default则不需要

4. export能直接导出变量表达式，export default不行

5. export default 默认导出就不需要name了，但是一个js文件中只能有一个export default

6. as 别名 import * as mycow from './cow.js' // import this file into mycow 



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

