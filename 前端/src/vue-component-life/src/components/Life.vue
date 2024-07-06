<template>
  <div>
    <h3>{{ msg }}</h3>
    <p ref="name">qinjp</p>
    <ul>
      <li v-for="(item, index) of banner" :key="index">
        <h4>{{ item }}</h4>
      </li>
    </ul>
  </div>
</template>
<script>
export default {
  data() {
    return {
      msg: '组件的生命周期应用',
      banner: {}
    }
  },
  beforeMount() {
    // 渲染之前拿到不数据
    console.log(this.$refs.name)
  },
  mounted() {
    // 渲染之后才能拿到数据
    console.log(this.$refs.name)
    // 网络请求
    fetch('http://127.0.0.1:8000/index', {
      method: 'get'
      //mode: 'no-cors' //防止跨域
    })
      // .then((response) => {
      //   if (response.ok) {
      //     return response.json()
      //   }
      //   throw new Error('Network response was not ok.')
      // })
      .then((res) => res.json())
      .then((data) => {
        // 处理获取到的数据
        console.log(data)
        this.banner = data.data
        console.log(this.banner)
      })
      .catch((error) => {
        console.log('error', error)
      })
      .finally(() => {
        console.log('请求完成')
      })
  }
}
</script>
