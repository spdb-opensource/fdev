<template>
  <f-dialog :value="isOpen" :title="title" @input="closeDialog" right>
    <fdev-form
      @submit.prevent="add"
      ref="menuInfo"
      :greedy="true"
      :model="menuInfo"
    >
      <!-- <div class="q-gutter-y-md q-mb-md"> -->
      <f-formitem diaS label="父菜单">
        <fdev-input v-model="menuInfo.parentName" hint="" disable />
      </f-formitem>
      <f-formitem diaS label="菜单级别">
        <fdev-input
          :value="menuInfo.level | filterLevel(menuInfo.level)"
          disable
          hint=""
        />
      </f-formitem>
      <!-- </div> -->
      <f-formitem diaS label="菜单排序" required
        ><fdev-input
          v-model="menuInfo.sort"
          :disable="dailogType === 'add'"
          hint=""
          :rules="[val => (val > 0 && val < 99) || '请输入菜单排序']"
        />
      </f-formitem>
      <!-- <f-formitem diaS label="菜单项类型" required>
        <fdev-select
          :options="menuTypeOption"
          multiple
          :disable="menuInfo.level === '1'"
          :hide-dropdown-icon="menuInfo.level === '1'"
          map-options
          emit-value
          v-model="menuInfo.menuType"
          :rules="[val => (val && val.length) || '请选择菜单项类型']"
        />
      </f-formitem> -->

      <f-formitem
        diaS
        label="菜单项中文名"
        v-if="menuInfo.level === '1'"
        required
      >
        <fdev-input
          v-model="menuInfo.nameCn"
          :rules="[
            val =>
              (val && val.trim().length < 7) ||
              '请输入菜单项中文名且字符长度不大于6',
            val => testCn(val) || '请至少输入一个中文'
          ]"
        />
      </f-formitem>
      <f-formitem
        diaS
        label="菜单项中文名"
        v-if="menuInfo.level !== '1'"
        required
      >
        <fdev-input
          v-model="menuInfo.nameCn"
          :rules="[
            val =>
              (val && val.trim().length < 11) ||
              '请输入菜单项中文名且字符长度不大于10',
            val => testCn(val) || '请至少输入一个中文'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="菜单项英文名" required>
        <fdev-input
          v-model="menuInfo.nameEn"
          :disable="dailogType !== 'add'"
          :rules="[val => (val && testEn(val)) || '请输入菜单项英文名']"
        />
      </f-formitem>

      <f-formitem diaS label="菜单项路径" :required="menuInfo.level !== '1'">
        <fdev-input
          v-model="menuInfo.path"
          :rules="[
            val =>
              menuInfo.level === '1' ? true : !!val || '请输入菜单项英文名'
          ]"
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
import { menuTypeOption, filterLevel } from '../../../utils/constants';
export default {
  name: 'editFunction',
  props: {
    isOpen: {
      default: false,
      type: Boolean
    },
    dataSource: {
      type: Object
    },
    parentNode: {
      type: Object
    },
    dailogType: {
      default: '',
      type: String
    }
  },
  data() {
    return {
      menuInfo: {},
      parentInfo: {},
      menuTypeOption,
      sortNum: ''
    };
  },
  computed: {
    title() {
      let obj = {
        add: '新增',
        edit: '编辑'
      };
      return `${obj[this.dailogType]}菜单项`;
    }
  },
  filters: {
    filterLevel
  },
  watch: {
    isOpen(val) {
      if (val) {
        this.menuInfo = Object.assign({}, this.dataSource);
        if (this.dailogType === 'add') {
          const { level, childrenList, menuId, nameCn } = this.parentNode;
          this.menuInfo.level = parseInt(level) + 1 + '';
          if (this.menuInfo.level === '1') {
            this.menuInfo.menuType = ['1', '2'];
          }
          const menuIdList = [];
          if (childrenList.length > 0) {
            for (let index = 0; index < childrenList.length; index++) {
              menuIdList.push(
                childrenList[index].menuId.substr(
                  childrenList[index].menuId.length - 1,
                  1
                )
              );
            }
            if (menuIdList.length > 0) {
              this.menuInfo.sortNum = Math.max(...menuIdList) + 1;
            } else {
              this.menuInfo.sortNum = 1;
            }
            this.menuInfo.sort = childrenList.length + 1;
          } else {
            this.menuInfo.sortNum = 1;
            this.menuInfo.sort = 1;
          }

          this.menuInfo.menuId = `${menuId}-${this.menuInfo.sortNum}`;
          this.menuInfo.parentId = menuId;
          this.menuInfo.parentName = nameCn;
        }

        const { nameCn } = this.parentNode;
        this.menuInfo.parentName = nameCn;
      }
    }
  },

  methods: {
    testEn(val) {
      return /^[a-zA-Z0-9]+$/.test(val);
    },
    testCn(val) {
      if (!val) {
        return true;
      }
      let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
      let flag = reg.test(val);
      return flag;
    },

    closeDialog() {
      this.$emit('close');
    },
    edit() {
      this.$refs.menuInfo.validate().then(res => {
        if (!res) return;

        this.$emit('edit', this.menuInfo);
      });
    }
  },
  created() {}
};
</script>
<style lang="stylus" scoped></style>
