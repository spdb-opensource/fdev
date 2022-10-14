<template>
  <f-dialog :value="isOpen" :title="title" @input="closeDialog" f-sl-sc>
    <fdev-form
      @submit.prevent="add"
      :greedy="true"
      ref="groupInfo"
      :model="groupInfo"
    >
      <f-formitem
        diaS
        label="父节点"
        class="q-mb-md"
        v-if="groupInfo.groupType !== 'root'"
      >
        <fdev-input v-model="groupInfo.parentName" disable />
      </f-formitem>
      <!-- <f-formitem diaS label="机构类型" required>
        <fdev-select
          v-model="groupInfo.groupType"
          :options="groupTypeOptions"
          :disable="dailogType === 'edit'"
          :hide-dropdown-icon="dailogType === 'edit'"
          map-options
          emit-value
          :rules="[val => !!val || '请选择机构类型']"
        />
      </f-formitem> -->
      <f-formitem diaS label="小组名称" required>
        <fdev-input
          v-model="groupInfo.name"
          :rules="[
            val =>
              (val && val.trim() && val.trim().length < 11) ||
              '请输入小组名称且字符长度不大于10'
          ]"
        />
      </f-formitem>
    </fdev-form>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog"/>
      <fdev-btn label="确定" v-forbidMultipleClick dialog @click="add"
    /></template>
  </f-dialog>
</template>
<script>
export default {
  name: 'editOrg',
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
      groupInfo: {
        name: ''
      },
      groupTypeOptions: []
    };
  },
  watch: {
    isOpen(val) {
      if (val) {
        this.groupInfo = Object.assign({}, this.dataSource);
        if (this.parentNode) {
          this.groupInfo.parentName = this.parentNode.name;
        } else {
          this.groupInfo.parentName = 'root';
        }

        if (this.dailogType === 'add') {
          let { id } = this.parentNode;
          this.groupInfo.parent_id = id;
        }
      } else {
        this.groupInfo = {};
      }
    }
  },
  computed: {
    title() {
      let obj = {
        add: '新增',
        edit: '编辑'
      };
      return `${obj[this.dailogType]}机构小组`;
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close');
    },
    add() {
      this.$refs.groupInfo.validate().then(res => {
        if (!res) return;
        // if (this.parentNode) {
        //   Reflect.set(this.groupInfo, 'parent_id', this.parentNode.id);
        // } else {
        //   Reflect.set(this.groupInfo, 'parent_id', 'root');
        // }

        this.$emit('edit', this.groupInfo);
      });
    }
  },
  created() {}
};
</script>
<style lang="stylus" scoped></style>
