<template>
  <h3>表单输入与绑定</h3>
  <form>
    <input type="text" v-model.lazy="message" />
    <p>{{ message }}</p>
    <!-- v-model 修饰符：lazy trim number -->
    <input type="text" v-model.trim="msg1" />
    <p>{{ message.length > 5 ? 'yes' : 'no' }}</p>
    <input type="text" v-model="msg2" />
    <p>{{ message.length > 5 ? 'yes' : 'no' }}</p>
    <input v-model.number="num" type="number" step="1" />
    <p>{{ num }}</p>

    <input type="checkbox" id="checkbox" v-model="check" />
    <p>{{ check }}</p>

    性别: 男
    <input type="radio" name="sex" id="Man" value="男" checked="checked" v-model="sex" />
    女
    <input type="radio" name="sex" id="Feman" value="女" v-model="sex" />
    <p>{{ sex }}</p>
    <br />

    吃饭
    <input type="checkbox" name="hobby" value="吃饭" v-model="hobbies" />
    睡觉
    <input type="checkbox" name="hobby" value="睡觉" v-model="hobbies" />
    玩游戏
    <input type="checkbox" name="hobby" value="玩游戏" v-model="hobbies" />
    <p>{{ hobbies }}</p>
    <br />

    <label :for="index" v-for="(item, index) in arr" :key="index">
      <input type="checkbox" :name="'hobby' + index" :id="index" :value="item" v-model="arrs" />
      {{ item }}
    </label>
    <h3 v-if="arrs.length > 0">{{ arrs }}</h3>
    <br />

    <select name="fruit" v-model="fruit">
      <option value="苹果" checked>苹果</option>
      <option value="橘子">橘子</option>
      <option value="水蜜桃">水蜜桃</option>
      <option value="橙子">橙子</option>
    </select>
    <h3>你选择的水果有：{{ fruit }}</h3>

    <select name="fruits" v-model="fruits" multiple="multiple">
      <option value="苹果">苹果</option>
      <option value="橘子">橘子</option>
      <option value="水蜜桃">水蜜桃</option>
      <option value="橙子">橙子</option>
    </select>
    <h3 v-show="fruits.length > 0">你买的水果有：{{ fruits }}</h3>
  </form>
</template>

<script>
export default {
  data() {
    return {
      message: 'qinjiepeng',
      msg1: 'qin',
      msg2: 'jie',
      num: 33,
      check: false,
      sex: '男',
      hobbies: [],
      arr: ['篮球', '足球', '羽毛球', '排球'],
      arrs: [],
      fruit: '苹果',
      fruits: []
    }
  },
  watch: {
    msg1(newValue, oldValue) {
      console.log('newValue', newValue)
      console.log('oldValue', oldValue)
    },
    msg2(newValue, oldValue) {
      console.log('newValue', newValue)
      console.log('oldValue', oldValue)
    }
  }
}
</script>
