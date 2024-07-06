<template>
  <div>
    <h1>loadingData</h1>
    <p v-if="showLoading">loading...</p>
    <p v-if="!showLoading">loaded</p>
  </div>
</template>
<script>
import mixin from '../utils/loadingMixin'
export default {
  mixins: [mixin],
  data() {
    return {
      list: [],
      form: {}
    }
  },
  mounted() {
    // 调用混入显示loading的方法
    this.showLoadingFun()
    // // 调用混入隐藏loading的方法
    this.getList(this.form).then(() => {
      this.hideLoadingFun()
    })
  },
  methods: {
    getList(form) {
      //此方法用来整理查询条件，并调用查询方法
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          resolve()
        }, 5000)
      })
    }
  }
}
</script>
