### 为了不阻塞渲染使用requestIdleCallback进行分时任务



首先明确requestIdleCallback是 `render`（渲染）完后这一帧如果还有剩余空闲时间，才会调用 requestIdleCallback 的任务，属于低优先级任务

如果没有剩余空闲时间，不会执行



如果是耗时任务，使用 setTimeout 延迟执行（渲染都完成才去执行）



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

缺点：

如果setTimeout 延迟执行加上requestIdleCallback之后有渲染帧（交互产生的渲染帧），上传日志的耗时操作就会影响到下一帧

所以有说法requestIdleCallback不适合做一些耗时的长任务

```js
setTimeout(() => {
  requestIdleCallback(() => console.log(1))
}, 1000)
```





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

































