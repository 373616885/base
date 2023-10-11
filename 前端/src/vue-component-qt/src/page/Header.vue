<template>
  <div>
    <h3>{{ msg }}</h3>
  </div>
</template>

<script>
export default {
  data() {
    return {
      msg: 'Header'
    }
  }
}
</script>

<style scoped>
h3 {
  width: 100%;
  height: 100px;
  border: 5px solid #699;
  text-align: center;
  line-height: 90px;
  box-sizing: border-box;
}
</style>
