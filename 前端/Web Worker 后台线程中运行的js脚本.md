### Web Worker

一个 **在后台线程中运行的js脚本**



### 使用方法

Web Worker 使用非常简单，只需创建一个新的 `Worker` 实例，指定一个外部的 JavaScript 文件即可。主线程和 Worker 线程通过 `postMessage` 和 `onmessage` 进行数据传递

1. **创建 Web Worker**： 首先，创建一个独立的 JavaScript 文件，专门用于 Web Worker 的逻辑处理。例如，我们创建一个 `worker.js` 文件：

```
// worker.js
//原始写法 onmessage 在全局对象上
onmessage = function(event) {
  console.log('Message received from main thread:', event.data);
  let result = event.data * 2; // 简单的数值处理
  postMessage(result); // 发送结果回主线程
};
// 写法一
self.addEventListener('message', function (e) {
  self.postMessage('You said: ' + e.data);
}, false);

// 写法二
this.addEventListener('message', function (e) {
  this.postMessage('You said: ' + e.data);
}, false);

// 写法三
addEventListener('message', function (e) {
  postMessage('You said: ' + e.data);
}, false);

```

​	2.**在主线程中使用 Web Worker**：

```js
// 主线程代码
if (window.Worker) {
  const myWorker = new Worker('worker.js'); // 创建一个 Worker 实例
 
  // 向 Worker 发送数据
  myWorker.postMessage(10);
 
  // 接收 Worker 的返回消息
  myWorker.onmessage = function(event) {
    console.log('Message from Worker:', event.data);
  };
   
  // 这里的worker.onmessage 也可以换成self.addEventListener ,self代表子线程自身，即子线程的全局对象  等同于
    self.addEventListener('message', function (e) {
      console.log('Message from Worker:', event.data);
    }, false);
    
 
  // 错误处理
  myWorker.onerror = function(error) {
    console.error('Worker error:', error);
  };
}
```

3. **终止 Web Worker**

如果某个任务不再需要执行，我们可以通过 `terminate()` 方法主动关闭 Web Worker：

```js
myWorker.terminate(); // 终止 Worker 线
```

4. **不指定js文件**（在vite打包单页面应用中的时候使用）

#### **不指定js文件方法三个：**

一个使用dataUrl

​	var dataUrl = 'data:application/javascript;charset=utf-8,' + code;

一个使用ObjectUrl

​	const blob = new Blob([code], { type: 'application/javascript' });

​	var blobURL = URL.createObjectURL(blob);

一个使用同页面的 Web Worker,然后使用ObjectUrl



```js
<script>
    var code = 'onmessage = function (event) { ' +
        'console.log( event.data); ' +
        'result = event.data * 2; ' +
        'postMessage(result) ;' +
        '} '

    var dataUrl = 'data:application/javascript;charset=utf-8,' + code;
    console.log(dataUrl)

	const blob = new Blob([code], { type: 'application/javascript' });
    var blobURL = URL.createObjectURL(blob);
	
	console.log(blobURL)

    // 主线程代码
    if (window.Worker) {
        const myWorker = new Worker(dataUrl); // 创建一个 Worker 实例
		//const myWorker = new Worker(blobURL); 或者
        // 向 Worker 发送数据
        myWorker.postMessage(10);

        // 接收 Worker 的返回消息
        myWorker.onmessage = function (event) {
            console.log('Message from Worker:', event.data);
        };

        // 错误处理
        myWorker.onerror = function (error) {
            console.error('Worker error:', error);
        };
    }

</script>
```

使用同页面的 Web Worker 

通常情况下，Worker 载入的是一个单独的 JavaScript 脚本文件，但是也可以载入与主线程在同一个网页的代码。

```html
 <body>
    <script id="worker" type="app/worker">
      addEventListener('message', function () {
        postMessage('some message');
      }, false);
    </script>
  </body>
```

上面是一段嵌入网页的脚本，注意必须指定`<script>`标签的`type`属性是一个浏览器不认识的值，上例是`app/worker`。

然后，读取这一段嵌入页面的脚本，用 Worker 来处理。

```js
var blob = new Blob([document.querySelector('#worker').textContent]);
var url = window.URL.createObjectURL(blob);
var worker = new Worker(url);

worker.onmessage = function (e) {
  // e.data === 'some message'
};
```

上面代码中，先将嵌入网页的脚本代码，转成一个二进制对象，然后为这个二进制对象生成 URL，再让 Worker 加载这个 URL。这样就做到了，主线程和 Worker 的代码都在同一个网页上面。



### 实例：Worker 线程完成轮询 

```js
function createWorker(f) {
  var blob = new Blob(['(' + f.toString() + ')()']);
  var url = window.URL.createObjectURL(blob);
  var worker = new Worker(url);
  return worker;
}

var pollingWorker = createWorker(function (e) {
  var cache;

  function compare(new, old) { 
  	// 比较新旧值进行业务处理 
  };

  setInterval(function () {
    fetch('/my-api-endpoint').then(function (res) {
      var data = res.json();

      if (!compare(data, cache)) {
        cache = data;
        self.postMessage(data);
      }
    })
  }, 1000)
});

pollingWorker.onmessage = function () {
  // render data
}

pollingWorker.postMessage('init');

```

上面代码中，Worker 每秒钟轮询一次数据，然后跟缓存做比较。如果不一致，就说明服务端有了新的变化，因此就要通知主线程。



### 关闭

```
/// main.js ///
worker.terminate();
/// worker.js ///
self.close();
close(); // 直接使用close()其实一样
```

在主线程或者worker线程都可以关闭worker



### 使用场景

1. **大数据处理**：当我们需要在前端处理大量数据时，Web Worker 可以将这些任务放在后台进行，避免主线程被阻塞。例如，数据可视化场景中，处理上百万条记录的计算工作可以通过 Web Worker 来完成。
2. **离屏渲染**：将canvas的控制权转交给worker，让空闲的它来执行动画避免卡顿。
3. **复杂计算**：复杂的算法（如加密算法、图像处理、AI 模型推理等）会占用大量的 CPU 资源，通过 Web Worker，可以将这类任务交给后台线程，提升页面的响应速度。
4. **文件处理**：处理大文件时，如上传或解析文件内容，可以通过 Web Worker 分块处理数据，确保用户界面保持流畅。
5. **文件生成**：生成Excel文件，数据量较大的情况下表格文件生成是否会阻塞主线程，这时候我们就可以使用Web Worker来实现
6. **网络请求**：大量并发的 API 请求或 WebSocket 数据处理，可以利用 Web Worker 来异步处理，不影响页面的正常渲染
7. **Worker 线程完成轮询** 







#### 后台线程

后台线程，顾名思义就是在web程序主执行线程之外的线程（一个独立的线程）

不会阻塞主线程的操作比如页面的渲染和用户的交互



#### js脚本

Web Worker中能够执行所有的js标准函数集，是一个正常的js环境

但是它与主线程的js环境也有一些不同：

1. `window => WorkerGlobalScope`
   - worker所处的全局上下文并不是window，而是WorkerGlobalScope，其中有很多window的方法是可用的：

| 名称                                                         | 作用                                              |
| ------------------------------------------------------------ | ------------------------------------------------- |
| `atob()`                                                     | 解码base64                                        |
| `btoa()`                                                     | 编码为base64                                      |
| interval (`setInterval()`, `clearInterval()`)                | 定时器                                            |
| timeout (`setTimeout()`, `clearTimeout()`)                   | 定时器                                            |
| `structuredClone()`                                          | 结构化克隆（深拷贝）                              |
| `queueMicrotask()`                                           | 添加微任务到队列（类似于setTimeout(callback, 0)） |
| animationFrame (`requestAnimationFrame`,`clearAnimationFrame`) | 下次重绘前执行动画                                |

1. worker的全局顶层对象为`self`
   - var声明的全局变量可以通过`self.XXX`找到
   - 可以用`self.addEventListener`来全局监听事件
   
   ```js
   // worker.js
   //原始写法 onmessage 在全局对象上
   onmessage = function(event) {
     console.log('Message received from main thread:', event.data);
     let result = event.data * 2; // 简单的数值处理
     postMessage(result); // 发送结果回主线程
   };
   // 写法一
   self.addEventListener('message', function (e) {
     self.postMessage('You said: ' + e.data);
   }, false);
   
   // 写法二
   this.addEventListener('message', function (e) {
     this.postMessage('You said: ' + e.data);
   }, false);
   
   // 写法三
   addEventListener('message', function (e) {
     postMessage('You said: ' + e.data);
   }, false);
   ```
   
   
   
   - 实际上就是[DedicatedWorkerGlobalScope](https://link.juejin.cn?target=https%3A%2F%2Fdeveloper.mozilla.org%2Fen-US%2Fdocs%2FWeb%2FAPI%2FDedicatedWorkerGlobalScope)
   
2. worker也有自己独有的函数
   - `importScripts()`能在worker里面实现`import`
   
   ```js
   importScripts('script1.js', 'script2.js');
   
   内部如果要加载其他脚本，有一个专门的方法importScripts()
   ```
   
   - `postMessage`能让worker与主线程进行通信
   
3. **worker 无法进行 DOM 操作**（在work.js中无法进行dom操作，可以在）
   
   - 一个例外是`OffscreenCanvas`，它是一个可以在worker环境中生效的canvas对象，在下文会详细介绍





### 特点

1. **多线程并行**：与 JavaScript 的单线程不同，Web Worker 运行在独立的线程中，执行任务时不阻塞主线程。
2. **数据通信**：主线程与 Worker 线程之间通过 `postMessage()` 和 `onmessage` 事件进行消息通信。每次通信都将信息克隆到对方线程，避免共享数据带来的安全问题。
3. **限制**：Web Worker 不能访问 DOM，也不能直接操作 `window` 对象或浏览器的全局变量。但它可以访问网络请求（如 `XMLHttpRequest`），进行数据计算、处理文件等任务。

