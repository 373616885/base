<template>
  <div class="article">
    <h3>{{ msg }}</h3>
  </div>
</template>

<script>
export default {
  data() {
    return {
      msg: 'Article'
    }
  }
}
</script>
<style scoped>
.article {
  width: 50%;
  margin: 0 auto;
  margin-top: 10px;
  height: 100px;
  border: 5px solid #699;
  text-align: center;
  line-height: 50px;
  background: #999;
}
</style>
