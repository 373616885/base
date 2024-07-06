$refs vue2中配合 ref 获取 DOM元素

 **必须在mounted中才能使用**

```vue
<template>
  <div>
    <p ref="myElement">Hello Vue 3!</p>
  </div>
</template>
 
<script>
export default {
  mounted() {
    console.log(this.$refs.myElement); // 访问DOM元素
  }
};
</script>
```



vue3中直接 const myElement = ref(null);

 **必须在onMounted 中才能使用**

```vue
<template>
  <div>
    <p ref="myElement">Hello Vue 3!</p>
  </div>
</template>
 
<script>
import { ref, onMounted } from 'vue';
 
export default {
  setup() {
    const myElement = ref(null);
 
    onMounted(() => {
      console.log(myElement.value); // 访问DOM元素
    });
 
    return {
      myElement
    };
  }
};
</script>
```

