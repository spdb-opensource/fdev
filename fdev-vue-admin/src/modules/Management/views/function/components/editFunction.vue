<template>
  <f-dialog :value="isOpen" :title="title" @input="closeDialog" f-sl-sc>
    <fdev-form
      @submit.prevent="add"
      greedy
      ref="functionInfo"
      :model="functionInfo"
    >
      <f-formitem diaS label="职能名称" required>
        <fdev-input
          v-model="functionInfo.name"
          :rules="[
            val =>
              (val && val.length < 11) || '请输入职能名称且字符长度不大于10'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="Git信息" required>
        <fdev-select
          :options="gitOption"
          map-options
          emit-value
          v-model="functionInfo.type"
          :rules="[val => !!val || '请选择Git信息']"
        />
      </f-formitem>
      <f-formitem diaS label="职能范围" required>
        <fdev-select
          :options="rangeOption"
          map-options
          emit-value
          v-model="functionInfo.spdbFlag"
          :rules="[val => !!val || '请选择职能范围']"
        />
      </f-formitem>
    </fdev-form>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog"/>
      <fdev-btn label="确定" v-forbidMultipleClick dialog @click="edit"
    /></template>
  </f-dialog>
</template>
<script>
import { gitOption, rangeOption } from '../../../utils/constants';
export default {
  name: 'editFunction',
  data() {
    return {
      functionInfo: {
        name: ''
      },
      gitOption,
      rangeOption
    };
  },
  watch: {
    isOpen(val) {
      if (val) {
        this.functionInfo = Object.assign({}, this.dataSource);
      }
    }
  },
  computed: {
    title() {
      let obj = {
        add: '新增',
        edit: '编辑'
      };
      return `${obj[this.dailogType]}职能`;
    }
  },
  props: {
    isOpen: {
      default: false,
      type: Boolean
    },
    dataSource: {
      type: Object
    },
    dailogType: {
      default: '',
      type: String
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close');
    },
    edit() {
      this.$refs.functionInfo.validate().then(res => {
        if (!res) return;
        this.$emit('edit', this.functionInfo);
      });
    }
  },
  created() {}
};
</script>
<style lang="stylus" scoped></style>
