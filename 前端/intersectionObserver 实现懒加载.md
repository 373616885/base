### intersectionObserver

监听目标元素与其祖先元素或视口交叉状态



### 可以实现懒加载

```html
<div style={{ width: 300, height: 300, overflow: 'auto' }}>
  <div id='1' className='lazy-loaded' 
       style={{ width: 300, height: 150, boxSizing: 'border-box', backgroundColor: 'gray' }}>
  </div>
  <div id='2' className='lazy-loaded' 
       style={{ width: 300, height: 150, boxSizing: 'border-box', backgroundColor: 'gray' }}>
  </div>
  <div id='3' className='lazy-loaded' 
       style={{ width: 300, height: 150, boxSizing: 'border-box', backgroundColor: 'gray' }}>
  </div>
  <div id='4' className='lazy-loaded' 
       style={{ width: 300, height: 150, boxSizing: 'border-box', backgroundColor: 'gray' }}>
  </div>
  <div id='5' className='lazy-loaded' 
       style={{ width: 300, height: 150, boxSizing: 'border-box', backgroundColor: 'gray' }}>
  </div>
</div>
```



```js
 observeVisible = () => {
    /*
     * @desc 实例方法
     - observer.observe(document.getElementById('example')); // 开始观察
     		参数是一个DOM节点，观察多个节点需多次调用 e.g. observer.observe(elementA); observer.observe(elementB);
     - observer.unobserve(element);                          // 停止观察
     - observer.disconnect();                                // 关闭观察器
     * */
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((item) => {
        // 元素可见
        if (item.intersectionRatio > 0) {
          const element = item.target;

          element.innerText = 'Done';
          // 停止观察  
          observer.unobserve(element);
        }
      })
    });

    const lazyloadItems = document.querySelectorAll('.lazy-loaded');

    Array.from(lazyloadItems).forEach((item) => {
        // 开始观察
      observer.observe(item);
    })
  };
```







### 无限下拉加载更多

```html
 <div id='scrollView' style={{ width: 300, height: 300, overflow: 'auto' }}>
   <div style={{ width: 300, height: 150, backgroundColor: 'gray' }}></div>
   <div style={{ width: 300, height: 150, backgroundColor: 'gray' }}></div>
   <div style={{ width: 300, height: 150, backgroundColor: 'gray' }}></div>
   <div style={{ width: 300, height: 150, backgroundColor: 'gray' }}></div>
   <div style={{ width: 300, height: 150, backgroundColor: 'gray' }}></div>
   <div id='sentinels'></div>
 </div>

```

```js
  observeVisible = () => {
    // 最后一个元素  
    const endItem = document.querySelector('#sentinels');
    const scrollView = document.querySelector('#scrollView');

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((item) => {

        // 底部元素可见
        if (item.intersectionRatio > 0) {

          const newItem = document.createElement('div');
          newItem.style.cssText = 'width:300px; height:150px; background-color:gray';
	      // 最后一个元素之前插人一个新元素 ，endItem 永远最后一个 
          scrollView.insertBefore(newItem, endItem);
        }
      })
    });

    observer.observe(endItem);
  };


```

