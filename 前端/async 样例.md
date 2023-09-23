```js
// async 在修饰function 表示这函数为异步函数:返回值为 Promise的函数
async function f() {
  // await 异步函数里调用异步函数：只能在async函数中使用
  // await表面上是会等待，但实际上 cpu 会被让出来
  // 如果要使用 await 就必须在 async 函数中，需要先定义一个async函数
  // async await 就可以不用 Promise 这和繁琐的对象
  const responseA = await fetch(
    "https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"
  )
    .then((res) => res.json())
    .then((data) => {
      console.log("responseA", data);
    })
    .catch((error) => {
      console.error("error", error);
    })
    .finally(() => {
      console.log("responseA end");
    });
  // await 异步等待结果
  const responseB = await fetch(
    "https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"
  );
  // await 异步等待结果
  const json = await responseB.json();
  console.log("responseB", json);

  // 高级两个并行
  const [a, b] = await Promise.all([responseA, responseB]);

  const promises = [
    fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"),
    fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"),
    fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"),
  ];
  // for循环并行
  for await (let result of promises) {
    result.json().then((data) => {
      console.log("for await", data);
    });
  }

  console.log("end");
}

f();

//上面的简化版
(async () => {
  const responseA = await fetch(
    "https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"
  );
})();

fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1")
  .then(function (response) {
    return response.json();
  })
  .then(function (myJson) {
    console.log("myJson", myJson);
  })
  .finally(() => {
    console.log("myJson end");
  });

```

