<template>
  <f-dialog
    :value="value"
    @input="$emit('input', false)"
    :title="`编辑${title}属性映射值`"
  >
    <f-formitem
      v-for="item in mapList"
      :key="item.label"
      :label="getLabel(item)"
      :required="item.required == '1' ? true : false"
    >
      <fdev-input
        :ref="`attrMapList.${item.key}`"
        v-model="item.value"
        :rules="[
          val => {
            if (item.required && !val) {
              return `请输入${item.label}`;
            }
          }
        ]"
      />
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="confirmToClose"/>
      <fdev-btn label="确定" dialog @click="saveMap"
    /></template>
  </f-dialog>
</template>
<script>
export default {
  name: 'listDialog',
  data() {
    return {};
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    mapList: {
      type: Array,
      required: true
    },
    title: {
      type: String,
      required: true
    }
  },
  methods: {
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.$emit('input', false);
        });
    },
    getLabel(item) {
      let label = item.label ? '(' + item.label + ')' : '';
      return item.key + label;
    },
    saveMap() {
      let obj = {};
      this.mapList.map(item => {
        obj[item['key']] = item['value'];
      });
      this.$emit('getListDate', obj);
    }
  }
};
</script>

<style lang="stylus" scoped></style>
