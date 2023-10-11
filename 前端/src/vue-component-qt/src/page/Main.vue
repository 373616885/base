<template>
  <div class="main">
    <h3>{{ msg }}</h3>
    <Article v-for="n in 4" ::key="n"></Article>
  </div>
</template>

<script>
import Article from './Article.vue'
export default {
  data() {
    return {
      msg: 'Main',
      items: [1, 2, 3, 4]
    }
  },
  components: {
    Article
  }
}
</script>

<style scoped>
.main {
  float: left;
  width: 69%;
  height: 600px;
  border: 5px solid #699;
  box-sizing: border-box;
  text-align: center;
}
</style>
