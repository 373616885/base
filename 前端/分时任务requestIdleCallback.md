### 为了不阻塞渲染使用requestIdleCallback进行分时任务

requestIdleCallback 任务只在一帧的空闲时间执行任务，防止阻塞主线程造成卡顿

若当前帧的空闲时间结束，则暂停批量任务时间，让出主线程，让页面的渲染变的丝滑



首先明确requestIdleCallback是 `render`（渲染）完后这一帧如果还有剩余空闲时间，才会调用 requestIdleCallback 的任务，属于低优先级任务

如果没有剩余空闲时间，不会执行



如果是耗时任务，也可以使用 setTimeout 延迟执行（渲染都完成才去执行）区别setTimeout 很难判断页面渲染都完成



大量计算的，不涉及DOM的，可以使用 new  worker 去执行（）区别worker 不能涉及DOM





一帧执行16.6毫秒，如果 setTimeout 不延迟执行，全局的task和setTimeout的task有可能在一帧之内，所以要延迟执行

例如：

```js
document.body.style.background = 'red';
setTimeout(function () {
    document.body.style.background = 'black';
})
```

`全局代码执行`和`setTimeout`为不同的2个`task`。

如果这2个`task`在同一帧中执行，则页面渲染一次，直接显示黑色

如果这2个`task`被分在不同帧中执行，则每一帧页面会渲染一次，屏幕会先显示红色再显示黑色





一般setTimeout 延迟执行都渲染完毕了，不需要配合requestIdleCallback了（也可以配合确保不影响渲染--毕竟渲染有没有完毕很难判断）

如果是业务不相关的，如上传日志，可以配合使用

上传日志是耗时的网络请求，setTimeout 延迟执行第一层确保不影响渲染，加上requestIdleCallback第二层保险

```js
setTimeout(() => {
  requestIdleCallback(() => console.log(1))
}, 1000)
```



### 主要用法

```js
requestIdleCallback((idle) => {
    // idle.timeRemaining() > 0 表示有空闲时间 
    // 第一次进入肯定是空闲的：idle.timeRemaining() > 0 = true
    // while循环就是执行完这次任务，还剩空闲时间 ，可以继续执行任务
    // 如果怕极限值，可以idle.timeRemaining() > 1 （以毫秒为单位，1毫秒对应计算来说是很长的）
    while (idle.timeRemaining() > 0 || idle.didTimeout) {
        // 执行耗时任务
        console.log('执行任务', i)
    }
	
})
```





### 使用场景

**1. 埋点日志相关**

配合 setTimeout 延迟执行第一层确保不影响渲染，加上requestIdleCallback第二层保险

```
setTimeout(() => {
  requestIdleCallback(() => console.log(1))
}, 1000)
```

**2. 预加载CSS，JS**

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

**3. 首次渲染屏卡顿的情况**

当你有一些非必须立刻执行的代码时，比如初始化某些非关键的UI组件

可以使用 `requestIdleCallback` 来推迟这些任务的执行

例如：

页面有很多用户的评论要处理完毕后才能看到内容，首次渲染时间就会因为等待处理而变的卡顿

将评论处理分解多个小任务，利用`requestIdleCallback` 在浏览器空闲的时候去执行，避免一次性处理导致的主线程阻塞







### 分时任务例子

使用最多的就是非关键的UI组件渲染，一般也就在这里用到

就是将过多的交互，导致页面渲染不能及时执行

```js
// tasks 任务
const tasks = Array.from({ length: 10000 }, (_, i) => () => {
  const div = document.createElement('div')
  div.innerText = i
  document.body.appendChild(div)
  return div
})
// 点击执行
const btn = document.querySelector('#btn')
btn.onclick = () => {
  //如果是for循环就由于，执行耗时过长导致页面渲染不及时
  //for (let i = 0; i < tasks.length; i++) {
  //  tasks[i]()
  //}
  //使用 requestIdleCallback 进行分段执行
  perfromTasks(tasks)
}


function perfromTasks(tasks) {
  if (tasks.length === 0) {
    return
  }

  let i = 0
  function _run() {
    // 等待浏览器空闲的时候回调
    requestIdleCallback((idle) => {
      //还有任务需要执行  
      if (i < tasks.length) {
        // idle.timeRemaining() > 0 表示有空闲时间 
        // 第一次进入肯定是空闲的：idle.timeRemaining() > 0 = true
        // while循环就是执行完这次任务，还剩空闲时间 ，可以继续执行任务
        // 如果怕极限值，可以idle.timeRemaining() > 1 （以毫秒为单位，1毫秒对应计算来说是很长的）
        while (idle.timeRemaining() > 0) {
          // 执行任务
          tasks[i++]()
          console.log('执行任务', i)
        }
        console.log('剩余空闲时间不多了，等待下一帧的空闲时间')
        // 如果还有任务，继续等待下一帧的空闲时间
        _run()
      }
    })
  }
  // 第一次执行
  _run()
}

```

































