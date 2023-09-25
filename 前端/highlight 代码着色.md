### 官网

https://highlightjs.org/

https://github.com/highlightjs/highlight.js#getting-started



### 功能

为我的博客网站添加代码高亮功能

替换截图 VsCode 的代码界面的问题





### 例子

选择自己喜欢的样式：我选择了一个比较喜欢的 monokai-sublime 主题

```js
<link href="/js/monokai-sublime.min.css" rel="stylesheet" type="text/css">
<script src="/js/highlight.min.js"></script>
<script>
   hljs.highlightAll();
</script>
```

highlight.js 会自动将文章中的  `<pre><code></code></pre>`代码进行识别语言并且高亮，一切就是这么简单。



自动识别模式不能100%识别出代码所属的开发语言

这种情况下可以手动设置一个 class 来精准控制

```
<pre><code class="language-javascript">...</code></pre>
```



显示行号的支持，需要通过再引入一个库 (highlightjs-line-numbers.js) 



cdn js 导入

```
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/default.min.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>

<!-- and it's easy to individually load additional languages -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/go.min.js"></script>

<script>hljs.highlightAll();</script>
```





完整例子：

```javascript
 	<link rel="stylesheet" href="./github-dark.css" />
    <script src="highlight/highlight.js"></script>
    <script src="highlight/languages/javascript.min.js"></script>
    <script
        src="https://cdn.bootcdn.net/ajax/libs/highlightjs-line-numbers.js/2.8.0/highlightjs-line-numbers.js"></script>
    <script>
    
        //hljs.highlightAll()
        const str = 'export default ErrorBoundary';

        const result = hljs.highlight("import React, { Component } from 'react'", {
            language: "javascript"
        });
        
        window.onload = function () {
            document.getElementById("code-area").innerHTML = result.value;
            document.getElementById("code-area").classList = "hljs language-javascript";

            hljs.highlightElement(document.getElementById("component"));

            hljs.initLineNumbersOnLoad();//加上 行号
        }

    </script>

    <div>Hello world</div>

    <pre>
        <code id="code-area"> 
        </code>  
    </pre>

    <pre>
        <code id="component" class="hljs language-javascript">  
// 这里是需要高亮的代码
import React, { Component } from 'react'
class ErrorBoundary extends Component {
    state = {
    error: null,
    errorInfo: null,
    }
    componentDidCatch (error, errorInfo) {
    this.setState({
        error,
        errorInfo,
        hasError: true,
    })
    }
    render() {
    if (this.state.errorInfo) {
        return (
        <details>
            <summary>Something went wrong.</summary>
            <p>{ JSON.stringify(this.state.errorInfo) }</p>
        </details>
        )
    }
    return this.props.children
    }
}
export default ErrorBoundary
    </code>  
    </pre>

```





































