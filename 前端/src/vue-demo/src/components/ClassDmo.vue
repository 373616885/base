<template>
  <p :class="{ active: isActive, textDanger: hasError }">Class样式绑定1</p>
  <!-- vue class 增强 -->
  <p :class="classObject">Class样式绑定2</p>
  <p :class="[arrActive, arrHasError]">Class样式绑定3</p>
  <p :class="[isActive ? 'active textDanger' : '']">Class样式绑定4</p>
  <p :class="[isActive ? 'active textDanger' : '', { textDanger: hasError }]">Class样式绑定5</p>
  <!-- 数组和对象嵌套，只能是数组嵌套对象 ,不能对象嵌套数组-->
</template>

<script>
export default {
  data() {
    return {
      myclass: 'demo',
      isActive: true,
      hasError: true,
      classObject: {
        active: true,
        textDanger: true
      },
      arrActive: 'active',
      arrHasError: 'textDanger'
    }
  }
}
</script>

<style>
.active {
  font-size: 30px;
}
.textDanger {
  color: red;
}
</style>
