<template>
  <f-dialog :value="isOpen" :title="title" @input="closeDialog" f-sl-sc>
    <fdev-form
      @submit.prevent="add"
      :greedy="true"
      ref="companyInfo"
      :model="companyInfo"
    >
      <f-formitem diaS label="公司名称" required>
        <fdev-input
          v-model="companyInfo.name"
          :rules="[
            val =>
              (val.trim() && val.trim().length < 21) ||
              '请输入公司名称且字符长度不大于20'
          ]"
        />
      </f-formitem>
    </fdev-form>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog"/>
      <fdev-btn label="确定" dialog @click="add"
    /></template>
  </f-dialog>
</template>
<script>
export default {
  name: 'editCompany',
  data() {
    return {
      companyInfo: {
        name: ''
      }
    };
  },
  watch: {
    isOpen(val) {
      if (val) {
        this.companyInfo = Object.assign({}, this.dataSource);
      }
    }
  },
  computed: {
    title() {
      let obj = {
        add: '新增',
        edit: '编辑'
      };
      return `${obj[this.dailogType]}公司`;
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
    add() {
      this.$refs.companyInfo.validate().then(res => {
        if (!res) return;
        this.$emit('add', this.companyInfo);
      });
    }
  },
  created() {}
};
</script>
<style lang="stylus" scoped></style>
