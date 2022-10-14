<template>
  <f-dialog :value="isOpen" :title="title" @input="closeDialog" right>
    <fdev-form
      @submit.prevent="add"
      :greedy="true"
      ref="roleInfo"
      :model="roleInfo"
    >
      <f-formitem diaS label="角色名称" required>
        <fdev-input
          v-model="roleInfo.name"
          :disable="dailogType !== 'add'"
          :rules="[
            val =>
              (val && val.length < 11) || '请输入角色名称且字符长度不大于10'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="角色功能权限">
        <fdev-input v-model="roleInfo.functions" hint="" />
      </f-formitem>
      <!-- <f-formitem diaS label="角色类型" required>
        <fdev-select
          v-model="roleInfo.roleType"
          :options="roleOption"
          option-value="id"
          option-label="name"
          emit-value
          map-options
          :rules="[val => !!val || '请选择角色类型']"
        />
      </f-formitem> -->
      <f-formitem diaS label="菜单权限">
        <fdev-tree
          :nodes="menuOption"
          accordion
          node-key="menuId"
          children-key="childrenList"
          tick-strategy="strict"
          :ticked.sync="roleInfo.menus"
          class="qtree"
        >
          <template v-slot:default-header="prop">
            <div class="row items-center q-ml-sm">
              {{ prop.node.nameCn || prop.node.label }}
            </div>
          </template>
        </fdev-tree>
      </f-formitem>
    </fdev-form>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog"/>
      <fdev-btn
        label="确定"
        v-forbidMultipleClick
        dialog
        @click="edit(roleInfo)"
    /></template>
  </f-dialog>
</template>
<script>
export default {
  name: 'editRole',
  props: {
    isOpen: {
      default: false,
      type: Boolean
    },
    dailogType: {
      default: '',
      type: String
    },
    dataSource: {
      type: Object
    },
    menuOption: {
      type: Array
    },
    roleOption: {
      type: Array
    }
  },
  watch: {
    isOpen(val) {
      if (val === true) {
        this.roleInfo = { ...this.dataSource };
      }
    }
  },
  data() {
    return {
      roleInfo: {
        name: '',
        roleType: ''
      }
    };
  },
  computed: {
    title() {
      let obj = {
        add: '新增',
        edit: '编辑'
      };
      return `${obj[this.dailogType]}角色`;
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close');
    },
    tickedNode(nodes) {
      let allList = [];
      nodes.forEach(item => {
        if (item.length > 6) {
          let parent = item.substr(0, 6);
          allList.push(parent);
        }
      });
      let s1 = new Set(allList);
      let parentmenu = [...s1];
      let menus = [...nodes, ...parentmenu];
      return menus;
    },
    edit(val) {
      this.$refs.roleInfo.validate().then(res => {
        if (!res) return;
        this.$emit('edit', val);
      });
    }
  }
};
</script>
<style lang="stylus" scoped>
.qtree >>> .q-tree__arrow
  color #0663BE
.qtree >>> .q-checkbox__bg
  color #0663BE
</style>
