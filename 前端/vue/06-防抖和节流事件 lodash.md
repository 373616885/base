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



### 防抖(Debounce)

确保在指定的时间间隔内，无论连续触发了多少次事件，只有最后一次事件会在该间隔结束后执行。

（触发事件后 n 秒后才执行函数，如果在 n 秒内又触发了事件，则会重新计算函数执行时间。）



### 节流(Throttle)

确保在指定的时间间隔内，无论触发了多少次事件，只有第一次事件会被执行，后续事件在这个间隔内都不会执行。

（连续触发事件但是在 n 秒中只执行第一次触发函数）



### 应用场景



#### 防抖：

**搜索框输入**：当用户在搜索框中输入文本时，通常会有一些实时搜索建议。

使用防抖可以确保只有在用户停止输入一段时间后才触发搜索请求，避免因为快速连续输入而导致的大量请求。



#### 节流：

**滚动事件**：在处理滚动事件时，如无限滚动加载更多内容，节流可以限制触发事件处理程序的频率，避免过度触发导致性能问题。



### 总结

执行时机：

防抖(Debounce)：确保在指定的时间间隔结束后执行一次函数。如果在这段时间内多次触发事件，则只有最后一次事件会在延迟后执行函数。

节流(Throttle)：确保在指定的时间间隔内最多执行一次函数。无论在这段时间内触发了多少次事件，只有第一次事件会立即执行函数。



应用场景：

防抖：适用于搜索框输入、表单验证等场景，用户完成输入后，才执行相关操作。

节流：适用于滚动事件、按钮点击等，需要在连续事件中合理控制执行频率的场景。



触发逻辑：

防抖：关注一段时间内的连续触发，但只对最后一次操作做出响应。

节流：在一段时间内，无论触发多少次事件，只响应一次。



分辨技巧：

如果您希望在一系列快速操作结束后只执行一次函数，那么使用防抖。

如果您希望在一系列快速操作中合理控制函数的执行频率，那么使用节流。







### 实现方式/原理

#### 防抖

```js
// 创建一个防抖函数，它返回一个新的函数，该新函数在指定的 wait 时间后执行 func
function debounce(func, wait) {
    // 保存定时器的引用
    let timeout;
 
    // 返回的函数是用户实际调用的函数，它包含了防抖逻辑
    return function(...args) {
        // 保存当前的 this 上下文
        const context = this;
        console.log(context);  
 
        // 清除之前的定时器，如果存在
        if (timeout) clearTimeout(timeout);  
 
        // 设置一个新的定时器
        // 当指定的 wait 时间过后，将执行 func 函数
        // 并将当前的 this 上下文和参数传入
        timeout = setTimeout(function() {  
            // 执行原始函数，绑定正确的 this 上下文和参数
            func.apply(context, args);  
        }, wait);  
    };
}
```





- 当防抖函数被触发时，首先会检查是否已经存在一个timeout（即是否有一个定时器在运行）。
- 如果存在，表示之前有触发过防抖函数但还未执行func，此时使用clearTimeout清除之前的定时器。
- 然后，设置一个新的timeout，如果在wait指定的时间内再次触发防抖函数，之前的定时器会被清除并重新设置，这意味着func的执行会被不断推迟。
- 只有当指定的时间间隔wait内没有再次触发防抖函数时，timeout才会到达，此时会执行原始函数func，并且使用apply方法将存储的context和args传递给它。



#### 节流

```js
function throttle(func, limit) {
    let inThrottle = false;
 
    return function(...args) {
        const context = this; // 保存当前的 this 上下文
 
        if (!inThrottle) {
            // 执行传入的函数
            func.apply(context, args);
            inThrottle = true; // 标记为正在节流
 
            // 使用闭包和 setTimeout 来在指定的延迟后重置 inThrottle
            setTimeout(() => {
                inThrottle = false; // 重置节流状态
            }, limit);
        }
    };
}
```



- func：需要被节流的函数。
- limit：表示在指定的时间间隔后，func才能再次被执行的时间（以毫秒为单位）。
- inThrottle：一个布尔值，用来标记func是否处于可执行状态。
- context：保存当前的this上下文，确保在执行func时this指向正确。
- args：使用扩展运算符...来收集所有参数，以便将它们传递给func。
- setTimeout：在指定的limit时间后执行，将inThrottle重置为false，这样func就可以在下一次调用时被执行了。

