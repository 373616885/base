<template>
  <h3>模板语法</h3>
  <h3>{{ msg }}</h3>
  <h3>{{ number + 1 }}</h3>
  <h3>{{ ok ? "yes" : "no" }}</h3>
  <h3>{{ message.split("").reverse().join("") }}</h3>
  <!-- <h3>{{ var a = 1 }}</h3> -->
  <!-- <h3>{{ if (ok) { return message } }}</h3> -->
  <p>{{ rawHtml }}</p>
  <p v-html="rawHtml"> </p>
  <div v-bind:title="d_title">短标题</div>
  <div title="爱学者论坛呵呵，看到了吧！！">鼠标移到这里来(将＆改为半角)</div>
  <a title="这是一个div元素">Hello, World!</a>

  <div class="acss">
    "hello"
  </div>
</template>

<script>
export default {
  data() {
    return {
      msg: "神奇的语法",
      hello: "hello world",
      number: 10,
      ok: true,
      message: "大家好",
      rawHtml: "<a href = 'https://www.baidu.com' target ='_blank'>百度</a>",
      d_title: "测试标题"
    };
  },
};
</script>
<style>
.acss {
  width: 100px;
  height: 100px;
  margin-left: 20px;
  background-color: #eee;
}

.acss::before {
  content: "♥";
  color: red;
}

.acss::after {
  content: "♥";
  color: blue;
}
</style>
