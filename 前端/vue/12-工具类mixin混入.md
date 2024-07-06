### 混入 (`mixin`) 

是一种分发Vue组件中可复用功能的一种方式

简单理解：

将组件中script项中的任意功能选项，如`data、components、created、methods 、computed、watch`等等-公共部分抽离出来

**注意事项**

- **mixin对象的data数据与组件选项会进行合并，如果有冲突，组件优先级高于mixin。**
- **mixin和组件中存在相同方法时，组件的方法的优先级大于mixin中**
- **mixin的事件执行顺序要优先于组件的**
- **多个组件调用同一个mixin，每个变量都是单独独立的，不会项目污染改变变量**





### 局部混入

```js
// loadingMixin.js
export default {
  data() {
    return {
      showLoading: false,
    };
  },
  methods: {
    showLoadingFun() {
      this.showLoading = true;
    },
    hideLoadingFun() {
      this.showLoading = false;
    },
  },
};

```



### 组件中混入此类

```js
<template>
  <div>
    <h1>loadingData</h1>
    <p v-if="showLoading">loading...</p>
    <p v-if="!showLoading">loaded</p>
  </div>
</template>
<script>
        
import mixin from "../mixin/loadingMixin";
export default {
  mixins: [mixin],
  data() {
    return {
      list: [],
      form: {},
    };
  },
  mounted() {
    // 调用混入显示loading的方法
    this.showLoadingFun();
    // // 调用混入隐藏loading的方法
    this.getList(this.form).then(() => {
      this.hideLoadingFun();
    });
  },
  methods: {
    getList(form) {
      //此方法用来整理查询条件，并调用查询方法
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          resolve();
        }, 5000)
      })
    },
  },
};
</script>

```





### 全局混入

全局混入我们只需要把loadingMixin.js引入到`main.js`中，然后将`mixin`放入到`Vue.mixin()`方法中即可；

```vue
import Vue from 'vue';
import App from './App.vue';
import mixin from "./mixin/loadingMixin";

Vue.config.productionTip = false
Vue.mixin(mixin)

new Vue({
  el: '#app',
  render: h => h(App)
});

```

全局混入之后，我们可以直接在组件中通过`this.变量/方法`来调用`mixin混入对象`的`变量/方法`









