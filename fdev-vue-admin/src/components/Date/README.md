###引用

import FDate from '@/components/Date';


###API

| 属性   | 说明   |
| ------- | ------ |
| rules | 数据校验规则,传参同quasar select 中的rules |
| value | 日期选择器所选值 |
| iconName | icon图标name值 |
| ref | 组件ref值，用于校验rules |


###event

| 事件名     | 说明     | 参数
| ------ | ---------------------- | --------- |
| setOptions | 同q-date的options属性 | date校验函数 |
        

##使用demo：  

```html
  template:
        <FDate
          ref="due_dateQdate"
          @setOptions="setOptionsDue"
          v-model="$v.devComponentModel.due_date.$model"
          :rules="[()=> $v.devComponentModel.due_date.required || '请选择计划完成日期']"
        />
```
```js
  js: 
    setOptionsDue(date,callback) {
      callback(new Date(date).getTime() >= new Date(this.optimizeModel.uat_date).getTime());
    }

```