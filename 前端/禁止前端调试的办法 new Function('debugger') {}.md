### 禁止前端调试的技巧

在页面上加入

```js
<script>
    window.setInterval(() => {
    new Function('debugger')()
}, 1000);
</script>
```



