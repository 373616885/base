### 安装

```js
npm i zod
```

### 使用

```vue
<template>
  <h3>{{ message }}</h3>
</template>

<script setup>
import { ref } from 'vue'
import z from 'zod'

const message = ref('Zod!')

const stringSchema = z
  .string({
    required_error: 'obj is required',
    invalid_type_error: 'obj must be a string'
  })
  .min(8, 'obj must be at least 8 characters')
  .max(30, 'obj must be at most 10 characters')
//stringSchema.parse() // throws an exception
//stringSchema.parse(123) // throws an exception
stringSchema.parse('I am a valid password') // returns "I am a valid password"

const User = z.object({
  email: z.string().email(),
  name: z.string(),
  phoneNumber: z.number()
})
const result = User.parse({
  email: 'hi@sample.com',
  name: 'Hello World',
  phoneNumber: 123
})

/***
 * 组合类型
 */
const Hobby = z.object({
  hobbyName: z.string().min(1)
})
// 扩展
const Comment = User.extend({
  title: z.string()
})
// 合并
const Hobbyist = Comment.merge(Hobby)

const result2 = Hobbyist.safeParse({
  email: 'hi@sample.com',
  name: 'Hello World',
  phoneNumber: 123,
  hobbyName: 'Sports'
})
// 如果不想让Zod抛出异常，当解析失败时，可以改用该safeParse方法。这将返回一个包含解析结果的对象
console.log(result2)
console.log(result2.success)
console.log(result2.error)

/***
 * 自定义验证
 */
const schema = z
  .object({
    account: z.string({
      required_error: 'account is required',
      invalid_type_error: 'account must be a string'
    }),
    email: z.string().email().optional(), // 设置email为可选
    age: z.number().default(18), //添加默认值
    isAdmin: z.boolean().default(false),
    gender: z.enum(['男', '女']), // enum 枚举限制输入
    sex: z.union([z.literal('man'), z.literal('female')]), //联合 (Unions) & 字面量 (Literals) 限制输入
    website: z.string().url().optional(),
    password: z
      .number({
        required_error: 'Phone number is required',
        invalid_type_error: 'Phone must be a number'
      })
      .min(8, 'password must be at least 8 characters '),
    confirmPassword: z
      .number()
      .min(8, 'confirmPassword must be at least 8 characters ')
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'confirmPassword and password not match'
  })
  .transform((date) => {
    console.log(date)
    date.birthday = '1991-11-22'
    return date
  })
const payload = {
  account: 'qinjp',
  email: 'hi@sample.com', // 可以有也可以没有
  password: 373616885,
  sex: 'man',
  gender: '男',
  confirmPassword: 373616885
}
const result3 = schema.safeParse(payload)
console.log(result3.data)
console.log(JSON.stringify(result3))
</script>

```







