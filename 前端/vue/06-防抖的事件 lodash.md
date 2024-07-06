预置防抖的事件 lodash

```java
import { debounce } from 'lodash-es'

export default {
  methods: {
    // 使用 Lodash 的防抖函数 
    // 防抖（debounce） 函数节流（throttle）
    click: debounce(function () {
      // ... 对点击的响应 ...
    }, 500)
  }
}
```

不过这种方法对于被重用的组件来说是有问题的，因为这个预置防抖的函数是 **有状态的**：它在运行时维护着一个内部状态。如果多个组件实例都共享这同一个预置防抖的函数，那么它们之间将会互相影响。

要保持每个组件实例的防抖函数都彼此独立，我们可以改为在 `created` 生命周期钩子中创建这个预置防抖的函数：

```js
export default {
  created() {
    // 每个实例都有了自己的预置防抖的处理函数
    this.debouncedClick = _.debounce(this.click, 500)
  },
  unmounted() {
    // 最好是在组件卸载时
    // 清除掉防抖计时器
    this.debouncedClick.cancel()
  },
  methods: {
    click() {
      // ... 对点击的响应 ...
    }
  }
}
```



```js
<template>
  <button @click="btnClick">节流</button>
  <button @click="btnClick2">防抖</button>
</template>
 
<script>
import _ from "lodash";
 
export default {
  name: "",
  setup() {
    // 防抖
    const btnClick = _.debounce(function() {
      // ....
      console.log('防抖');
    }, 2000);
    // 节流
    const btnClick2 = _.throttle(function() {
      // ....
      console.log('节流');
    }, 2000);
    return {
      btnClick,
      btnClick2,
    };
  },
};
</script>
 
<style scoped></style>
```



