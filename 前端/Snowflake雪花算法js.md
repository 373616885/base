### JavaScript新增基本类型BigInt

ES2020新增 一个基本类型BigInt,表示任意精度格式的整数，无符号整数类型，存储任意长度的整数。

在 JavaScript 中，普通的数字（Number 类型）是基于 IEEE 754 双精度浮点数格式存储的，

这导致它们只能安全地表示 -9007199254740991（即 -(2^53 - 1)）到 9007199254740991（即 2^53 - 1）之间的整数。

超出这个范围的整数在表示时可能会失去精度。

Long型数（ 2^64-1）数据会超出Number类型能安全表示的大整数，因此有精度问题



BigInt 就是解决了Number整数溢出的，导致的精度问题

BigInt 表示任意大的整数，而且不会丢失精度，大小看内存来的（最大值是2的(2的53次方)次方减1，即2的270次方减1）

Long型数据可以使用BigInt 表示



### snowflake.js

ES6 使用

import bigInt from "big-integer"

原生使用

```js
export const randomLenNum = () => {
  return tempSnowflake.getId()
}
export const Snowflake = /** @class */ (function () {
  function Snowflake(_workerId, _dataCenterId, _sequence) {
    //41位时间戳69年-1970+69=2039，
    //1288834974657 = 2010-11-04 09:42:54  
    //1722444914572 = 2024-08-01 00:55:14  
    this.twepoch = 1722444914572n
    //this.twepoch = 0n;
      
    /** 机器id所占的位数 */  
    this.workerIdBits = 5n
    /** 数据标识id所占的位数 */
    this.dataCenterIdBits = 5n
    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */  
    this.maxWrokerId = -1n ^ (-1n << this.workerIdBits) // 值为：31
    /** 支持的最大数据标识id，结果是31 */
    this.maxDataCenterId = -1n ^ (-1n << this.dataCenterIdBits) // 值为：31
    /** 序列在id中占的位数 */  
    this.sequenceBits = 12n
    /** 机器ID向左移12位 */  
    this.workerIdShift = this.sequenceBits // 值为：12
    /** 数据标识id向左移17位(12+5) */  
    this.dataCenterIdShift = this.sequenceBits + this.workerIdBits // 值为：17
    /** 时间截向左移22位(5+5+12) */
    this.timestampLeftShift = this.sequenceBits + this.workerIdBits + this.dataCenterIdBits // 值为：22
    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    this.sequenceMask = -1n ^ (-1n << this.sequenceBits) // 值为：4095
    
    this.lastTimestamp = -1n
    //设置默认值,从环境变量取
    /** 工作机器ID(0~31) */
    this.workerId = 1n
    /** 数据中心ID(0~31) */
    this.dataCenterId = 1n
      
    this.sequence = 0n
    if (this.workerId > this.maxWrokerId || this.workerId < 0) {
      throw new Error(
        '_workerId must max than 0 and small than maxWrokerId-[' + this.maxWrokerId + ']'
      )
    }
    if (this.dataCenterId > this.maxDataCenterId || this.dataCenterId < 0) {
      throw new Error(
        '_dataCenterId must max than 0 and small than maxDataCenterId-[' +
          this.maxDataCenterId +
          ']'
      )
    }

    this.workerId = BigInt(_workerId)
    this.dataCenterId = BigInt(_dataCenterId)
    this.sequence = BigInt(_sequence)
  }
  Snowflake.prototype.tilNextMillis = function (lastTimestamp) {
    var timestamp = this.timeGen()
    while (timestamp <= lastTimestamp) {
      timestamp = this.timeGen()
    }
    return BigInt(timestamp)
  }
  Snowflake.prototype.timeGen = function () {
    return BigInt(Date.now())
  }
  Snowflake.prototype.nextId = function () {
    var timestamp = this.timeGen()
    if (timestamp < this.lastTimestamp) {
      throw new Error(
        'Clock moved backwards. Refusing to generate id for ' + (this.lastTimestamp - timestamp)
      )
    }
    if (this.lastTimestamp === timestamp) {
      this.sequence = (this.sequence + 1n) & this.sequenceMask
      if (this.sequence === 0n) {
        timestamp = this.tilNextMillis(this.lastTimestamp)
      }
    } else {
      this.sequence = 0n
    }
    this.lastTimestamp = timestamp
    return (
      ((timestamp - this.twepoch) << this.timestampLeftShift) |
      (this.dataCenterId << this.dataCenterIdShift) |
      (this.workerId << this.workerIdShift) |
      this.sequence
    )
  }
  Snowflake.prototype.getId = function () {
    var timestamp = this.timeGen()
    if (timestamp < this.lastTimestamp) {
      throw new Error(
        'Clock moved backwards. Refusing to generate id for ' + (this.lastTimestamp - timestamp)
      )
    }
    if (this.lastTimestamp === timestamp) {
      this.sequence = (this.sequence + 1n) & this.sequenceMask
      if (this.sequence === 0n) {
        timestamp = this.tilNextMillis(this.lastTimestamp)
      }
    } else {
      this.sequence = 0n
    }
    this.lastTimestamp = timestamp
    let id =
      ((timestamp - this.twepoch) << this.timestampLeftShift) |
      (this.dataCenterId << this.dataCenterIdShift) |
      (this.workerId << this.workerIdShift) |
      this.sequence
    return BigInt(id).toString()
  }
  return Snowflake
})()

export const tempSnowflake = new Snowflake(1n, 1n, 0n)

```



### 报错

文件ts不认js，解决

tsconfig.json 中 "compilerOptions" 下面 加上

```js
"allowJs": true,
"checkJs": false
```

最后在 env.d.ts  加上你的 js 文件

declare module '@/utils/Snowflake'



### 报错

不认 BigInt

.eslintrc.cjs  加上 

```
env: {
    es2020: true, // <- activate “es2020” globals
    browser: true,
    node: true,
    mocha: true
}
```



```
/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
  root: true,
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier/skip-formatting'
  ],
  parserOptions: {
    ecmaVersion: 'latest'
  },
  //添加 rules
  rules: {
    //在rules中添加自定义规则
    //关闭组件命名规则
    'vue/multi-word-component-names': 'off'
  },
  env: {
    es2020: true, // <- activate “es2020” globals
    browser: true,
    node: true,
    mocha: true
  }
}

```





