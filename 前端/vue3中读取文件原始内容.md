vue3中读取文件的原始内容 

在 import from 后面加上 ?raw

```js
import avataruUrl from './img/avatar.db64?raw'
```



vue2中需要插件，不然被默认被js脚本

```js
import avataruUrl from './img/avatar.db64'
```



可以是使用变量的形式

```js
avatar.js

const avataruUrl = ''
然后引入
import avataruUrl from './img/avatar.js'
```

