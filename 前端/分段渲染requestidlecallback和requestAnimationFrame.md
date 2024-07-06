### 分片渲染

浏览器默认16.6 毫秒的渲染帧

js 任务执行时间过长，就会导致掉帧，导致卡顿



浏览器刷新频率为60Hz大概16.6毫秒渲染一次



浏览器的主线程是一个单线程，负责处理页面渲染和执行 JavaScript 代码的线程

任务队列（task queue）中的顺序依次执行任务（FIFO）

当任务被添加到任务队列中时，主线程会处理当前的任务，然后再处理下一个任务



JS线程和渲染线程是互斥的，

所以如果JS线程执行任务时间超过16.6ms的话，就会导致掉帧，导致卡顿





### 浏览器的一帧

一般浏览器的刷新率为60HZ，即1秒钟刷新60次。当然也有120 HZ或144 HZ的。常见的就是60HZ

```text
1000ms / 60hz = 16.6
```

大概每过16.6ms浏览器会渲染一帧画面，也就是说浏览器`一炷香`的时间是16.6ms。





在这段时间内，大体会做两件事：**task 与 render**。

![](images\浏览器的一帧.webp)

**task** : 

其中`task`被称为`宏任务`，就像下墓后我们要做的事一样。

包括`setTimeout`，`setInterval`，`setImmediate`，`postMessage`，`requestAnimationFrame`，`I/O`，`DOM 事件`，`events`: 点击事件、键盘事件、滚动事件等。

代码里面： 

调用 setTimeout 会产生一个新的 task，放到 task queue 队列里等待执行

（不在当前的task执行范围内--不同的 task 会有不同的 task queue，对应执行的线程也会不同）

但是期间产生的 microTask ，会在task之后，一次过全部执行完，清空microTask queue

microTask 耗时或者过多都会导致卡顿



**render** : 

`render`指渲染页面。



### **eventLoop**

`task`按优先级被划分到不同的`task queue`中。就像老祖宗定的“轻重缓急”

比如：为了及时响应用户交互，浏览器会为鼠标键盘（Mouse、Key）事件所在`task queue`分配3/4优先权。

这样可以及时响应用户交互，又不至于不执行其他`task queue`中的`task`。

![](images\浏览器工资指南.webp)



虚线框部分要做的工作是：

1. 将新产生的`task`插入不同`task queue`中。
2. 按优先级从某个`task queue`中选择一个`task`作为本次要执行的`task`。

这就是`事件循环`（`eventLoop`）。

`task`执行过程中如果调用`Promise`、`MutationObserver`、`process.nextTick`会将其作为`microTask`（微任务）保存在`microTask queue`中。

就像`做事`后的琐碎工作。

每当执行完`task`，在执行下一个`task`前，都需要检查`microTask queue`，执行并清空里面的`microTask`。

![](images\微任务.webp)

比如如下代码

```js
setTimeout(() => console.log('timeout'));
Promise.resolve().then(() => {
    console.log('promise1');
    Promise.resolve().then(() => console.log('Promise2'));
});
console.log('global');
```



执行过程为：

1. “全局作用域的代码执行”是第一个`task`。（当前执行的js代码）
2. 执行过程中调用`setTimeout`，`计时器线程`会去处理计时，在计时结束后会将`计时器回调`加入`task queue`中。
3. 调用`Promise.resolve`，产生`microTask`，插入`microTask queue`。
4. 打印`global`。
5. “全局作用域的代码执行”的`task`执行完毕，开始遍历清理`microTask queue`。
6. 打印`promise1`。
7. 调用`Promise.resolve`，产生`microTask`，插入当前`microTask queue`。
8. 继续遍历`microTask queue`，执行`microTask`打印`promise2`。
9. 开始第二个`task`，打印`timeout`。



#### 执行顺序

`Promise`, `setTimeout` , `rAF` 和 `rIC` 对应 4 种队列：微任务队列、宏任务队列、animation 队列和 idle 队列。

- 微任务队列会在 JS 运行栈为空的时候立即执行
- animation 队列会在页面渲染前执行
- 宏任务队列优先级低于微任务队列，一般也会比 animation 队列优先级低，但不是绝对 。
- idle 队列优先级最低，当浏览器有空闲时间的时候才会执行。



#### 队列特性

在一个事件循环内：

- 宏任务队列，每次只会执行队列内的一个任务（当前代码执行的task）

  - 里面的setTimeout , 点击事件 , 产生新的 task （一帧可以有多个task）

- 微任务队列，每次会执行队列里的全部任务。假设微任务队列内有 100 个 Promise，它们会一次过全部执行完。这种情况下极有可能会导致页面卡顿。如果在微任务执行过程中继续往微任务队列中添加任务，新添加的任务也会在当前事件循环中执行，很容易造成死循环, 如：

  ```
  function loop() {
      Promise.resolve().then(loop);
  }
  
  loop();
  ```

- animation 队列，跟微任务队列有点相似，每次会执行队列里的全部任务。但如果在执行过程中往队列中添加新的任务，新的任务不会在当前事件循环中执行，而是在下次事件循环中执行

- dle 队列，每次只会执行一个任务。任务完成后会检查是否还有空闲时间，有的话会继续执行下一个任务，没有则等到下次有空闲时间再执行。需要注意的是此队列中的任务也有可能阻塞页面，当空闲时间用完后任务不会主动退出。如果任务占用时间较长，一般会将任务拆分成多个阶段，执行完一个阶段后检查还有没有空闲时间，有则继续，无则注册一个新的 idle 队列任务，然后退出当前任务。`React Fiber` 就是用这个机制。但最新版的 `React Fiber` 已经不用 `rIC` 了，因为调用的频率太低，改用 `rAF` 了



### **一帧执行多个task**

就像`一炷香`时间可以做多件事，在一帧时间可以执行多个`task`。

执行如下代码后，屏幕会先显示红色再显示黑色，还是直接显示黑色？

```js
document.body.style.background = 'red';
setTimeout(function () {
    document.body.style.background = 'black';
})
```

答案是：不一定。

`全局代码执行`和`setTimeout`为不同的2个`task`。

如果这2个`task`在同一帧中执行，则页面渲染一次，直接显示黑色（如下图情况一）。

如果这2个`task`被分在不同帧中执行，则每一帧页面会渲染一次，屏幕会先显示红色再显示黑色（如下图情况二）。

![img](images\v2-f2f8dfbf1bf5df681c734907fad52e79_720w.webp)

如果我们将`setTimeout`的延迟时间增大到`17ms`，那么基本可以确定这2个`task`会在不同帧执行，

则“屏幕会先显示红色再显示黑色”的概率会大很多。



### **requestAnimationFrame**

会在每一帧`render`前被调用 requestAnimationFrame（简称`rAF`） 属于高优先级任务

一帧`render`前只调用一个 `rAF` 任务

多个就等到下一帧



作用：一般被用来绘制动画，因为当动画代码执行完后接下来就进入`render`。动画效果可以最快被呈现

（可以理解为专门为动画优化而设计的方法）



可以发现，`task`没有办法精准的控制执行时机。那么有什么办法可以保证代码在每一帧都执行呢？

答案是：使用`requestAnimationFrame`（简称`rAF`）。

![](images\requestAnimationFrame.webp)

如下代码执行结果是什么呢：

```js
setTimeout(() => {
  console.log("setTimeout1");
  requestAnimationFrame(() => console.log("rAF1"));
})
setTimeout(() => {
  console.log("setTimeout2");
  requestAnimationFrame(() => console.log("rAF2"));
})

Promise.resolve().then(() => console.log('promise1'));
console.log('global');

向右翻动展示答案                                大概率是：     1. global 2. promise1 3. setTimeout1 4. setTimeout2 5. rAF1 6. rAF2                                 
```

`setTimeout1`与`setTimeout2`作为2个`task`，使用默认延迟时间（不传延迟时间参数时，大概会有4ms延迟），那么大概率会在同一帧调用。

`rAF1`与`rAF2`则一定会在不同帧的`render`前调用。（一帧只调用一个 `rAF` 任务，多个就等下一帧）

所以，大概率我们会在同一帧先后调用`setTimeout1`、`setTimeout2`、`rAF1`，再在另一帧调用`rAF2`。



### **requestIdleCallback**

`render`完后这一帧如果还有剩余时间，就会调用 requestIdleCallback 的任务，属于低优先级任务

（最低，如果没有剩余时间，还不会执行）.

如图中绿色部分：

![img](images\v2-fcd66a6e92d4f1784d224ad174736592_720w.webp)

此时你可以使用`requestIdleCallback`API，**如果渲染完成后还有空闲时间**，则这个`API`会被调用。



```js
let dates = []
for (let i = 0; i < 10000; i++) {
  dates.push(new Date())
}
// 将任务分片
for (let i = 0; i < dates.length; i++) {
  requestIdleCallback((idle) => {
    //还有剩余时间  
    if (idle.timeRemaining() > 0) {
      // 执行耗时的任务--继续执行
      console.log('requestIdleCallback')
    }
    
  })
}
```

某种程度上功能相似，写法也相似

```js
// 某种程度上功能相似，写法也相似
requestIdleCallback(() => console.log(1));
setTimeout(() => console.log(2));
```

requestIdleCallback是一个Web API，允许开发者在主线程空闲时去执行低优先级回调函数。

这个函数的主要目的是使得开发者能够在不影响关键事件如动画和输入响应的情况下，执行后台或低优先级的任务。

window.requestIdleCallback(callback, [options]);

> 返回值 一个ID，可以把它传入 Window.cancelIdleCallback() 方法来结束回调
>
> 
>
> callback 是一个函数，表示在空闲时段执行的回调函数。
>
> 当callback被调用时，会接受一个参数 deadline 对象，
>
> 对象上有两个属性timeRemaining和didTimeout
>
> 
>
> timeRemaining 属性是一个函数，
>
> 函数的返回值表示当前空闲时间还剩下多少时间；（以毫秒为单位）
>
> 
>
> didTimeout属性是一个布尔值，
>
> 如果didTimeout是true，那么表示本次callback的执行是因为超时的原因（配合 options 的 timeout属性）
>
> 
>
> options 是一个可选的配置对象，用于指定更精确的空闲条件，
>
> 例如 timeout（回调函数执行的最长时间）等。
>
> timeout：
>
> 如果浏览器一直处于繁忙状态, 导致回调一直无法执行，配置参数timeout
>
> 告诉调用者，执行 myNonEssentialWork 回调的时候，是否超过了 200 毫秒
>
> `didTimeout` 参数并不会强制执行回调函数，只是一个标识，用于告知回调函数是否超过了可用的空闲时间
>
> requestIdleCallback(myNonEssentialWork, { timeout: 200 });
>
> 这时didTimeout 为 true，告诉回调函数是超过了200，才得以执行
>
> 告知回调函数是否超过了你定义的超时时间
>
> 
>
> requestIdleCallback 注册的回调函数时，该回调函数会作为一个宏任务被添加到事件队列中。
>
> requestIdleCallback 的回调函数在执行时是依赖于事件队列的管理的。当浏览器在空闲时段时调用。
>
> 
>
> **调度和取消回调:** `requestIdleCallback`函数安排一个回调函数在主线程下一次空闲时被执行，并返回一个
>
> ID，可以用这个ID通过`cancelIdleCallback`函数取消回调



**注意：不是每一帧都会执行，只有在浏览器主线程空闲的时候才会执行。就是渲染完成后还有空闲时间，还有requestIdleCallback  执行时间过长也会导致阻塞页面**



#### 使用场景

**1. 埋点日志相关**

- 在用户有操作行为时（如点击按钮、滚动页面）进行数据分析并上报
- 处理数据时往往会调用 JSON.stringify ，如果数据量较大，可能会有性能问题。

```js
const queues = [];

document.querySelectorAll('button').forEach(btn => {
  btn.addEventListener('click', e => {
    // do something...
    pushQueue({
      type: 'click'
      // ...
    }));
  
    schedule(); // 等到空闲再处理
  });
});

function schedule() {
  requestIdleCallback(deadline => {
    // 不停地问，当前时间还剩多少空闲的时间
    while (deadline.timeRemaining() > 1) {
        // 还有空闲时间
        const data = queues.pop();
        // 这里就可以处理数据（格式化数据，清洗数据），上传数据
        // 注意这里必须要不耗时的，上传数据如果耗时，不能再这里做
        // 另起一个setInterval循环任务，去上传，这里只处理数据
        // 总体思路：合并数据，那边一次上传多条，不在一条一条上传
    }
    
    if (queues.length !== 0) {
      // 继续上传调用
      schedule();
    }
  });
}

```

**2. 预加载**

例如当你需要处理一些数据，但这些数据不需要立即展示给用户时，可以在空闲时预处理这些数据

```js
function prefetch(entry: Entry, opts?: ImportEntryOpts): void {
  if (!navigator.onLine || isSlowNetwork) {
    // Don't prefetch if in a slow network or offline
    return;
  }

  // 异步加载js 和 css  
  requestIdleCallback(async () => {
    const { getExternalScripts, getExternalStyleSheets } = await importEntry(entry, opts);
    
    requestIdleCallback(getExternalStyleSheets);
    requestIdleCallback(getExternalScripts);
  });
}

```

**3. 延迟执行**

当你有一些非必须立刻执行的代码时，比如初始化某些非关键的UI组件

可以使用 `requestIdleCallback` 来推迟这些任务的执行



#### 不合适情形

不适合操作dom&更新UI

- 执行时机不确定可能导致视觉难以预测
- 空闲回调执行的时候，当前帧已经结束绘制了，所有布局的更新和计算也已经完成。可能会引发回流重绘。

不适合做一些耗时的长任务

- 虽然是在浏览器空闲执行，但依然运行在主线程上，耗时的长任务同样会导致帧率降低， 造成页面卡顿。



`requestIdleCallback` 不适合执行 DOM 操作

因为修改了 DOM 之后下一帧不一定会触发修改，主线程可能还被占据着



### **掉帧与时间切片**

如果`task`执行时间过长会怎么样呢？

如图`taskA`执行时间超过了16.6ms（比如`taskA`中有个很耗时的`while`循环）。

那么这一帧就没有时间`render`，页面直到下一帧`render`后才会更新。

![img](images\v2-45fff95d10b174bc7ac231149e26a49b_720w.webp)

表现为页面卡顿一帧，或者说`掉帧`。就像下墓后我们没有时间`绘图`。



刚才提到的`requestIdleCallback`是一个解决办法。我们可以将一部分工作放到`空闲时间`中执行。

但是遇到`长时间task`还是会掉帧。



更好的办法是：时间切片。即把`长时间task`分割为`几个短时间task`。



如图我们将`taskA`拆分为2个`task`。则每一帧都有机会`render`。这样就能减少掉帧的可能。

![img](images\v2-653f20bc46fd611cbf3891678957c6f3_720w.webp)

这`React15`中，采用`递归`的方式构建`虚拟DOM树`。

如果`树层级`很深，对应`task`的执行时间很长，就可能出现掉帧的情况。

![img](images\v2-cc917b91fe05296303c6016893755028_720w.webp)

为了解决掉帧造成的卡顿，`React16`将`递归`的构建方式改为可中断的`遍历`。

以`5ms`的执行时间划分`task`，每`遍历`完一个节点，就检查当前`task`是否已经执行了`5ms`。

如果超过`5ms`，则中断本次`task`。

![img](images\v2-2e33d5dcd9d337f3138351e875f8ab54_720w.webp)

通过将`task`执行时间切分为一个个小段，减少`长时间task`造成无法`render`的情况。这就是`时间切片`。