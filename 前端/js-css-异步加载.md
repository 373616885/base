js 异步加载 defer 或者 async

css 异步加载 prefetch 或者 preload



defer : 不阻塞dom解析，dom解析完成后执行（延迟执行）

async : 不阻塞dom解析，下载完成后立即执行



prefetch : 预加载（不阻塞dom解析），立即加载（使用的时候使用本地缓存）

preload : 预加载（不阻塞dom解析），空闲的时候加载



```html
<script src="./requestIdleCallback.js" defer></script>

<script src="./requestIdleCallback.js" async></script>

<link rel="prefetch" href="style.css">

<link rel="preload" href="style.css">
```

