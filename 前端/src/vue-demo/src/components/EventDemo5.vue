<template>
  <h3>事件修饰符：阻止默认事件和事件冒泡</h3>
  <a @click.prevent="tiaozhuang" href="https://go.itab.link/">itab</a>
  <h3>事件冒泡:点击子元素，父级元素的事件也被触发了</h3>
  <div @click="clickdiv">
    <p @click.stop="clickp">测试冒泡</p>
  </div>
  <h3>once:只触发一次</h3>
  <a @click.once="clickonce">once</a>
</template>

<script>
export default {
  data() {
    return {
      names: ["qin", "jie", "peng"],
    };
  },
  // 所有的方法或者函数都放这里
  methods: {
    tiaozhuang(e) {
      // 阻止不让继续跳转了
      //e.preventDefault();
      console.log(e);
    },
    clickdiv() {
      console.log("div");
    },
    clickp(e) {
      // 阻止冒泡事件
      //e.stopPropagation();
      console.log("p");
    },
    clickonce(){
      console.log("once");
    }
  }
};
</script>
