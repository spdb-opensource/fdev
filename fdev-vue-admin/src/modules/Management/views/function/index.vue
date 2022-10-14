<template>
  <f-block>
    <div class="bg-white">
      <fdev-table :data="table" title="职能管理" :columns="columns">
        <template v-slot:top-right>
          <fdev-btn
            class="q-ml-md btn-height"
            normal
            icon="add"
            v-if="adminAuth"
            label="新增职能"
            @click="add"
          />
        </template>
        <template v-slot:top-bottom>
          <fdev-input
            v-model="functionName"
            placeholder="请输入职能名称"
            clearable
            @keyup.13="searchFunction"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="searchFunction"
              />
            </template>
          </fdev-input>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td :props="props">
            <fdev-btn
              flat
              class="q-mr-lg"
              label="编辑"
              @click="update(props.row)"
            />
            <fdev-btn flat label="删除" @click="remove(props.row)" />
          </fdev-td>
        </template>
      </fdev-table>
    </div>
    <editFunction
      :isOpen="isEdit"
      :dataSource="functionInfo"
      :dailogType="type"
      @close="isEdit = false"
      @edit="editFunction"
    />
  </f-block>
</template>

<script>
import { functionColumn } from '../../utils/constants';
import editFunction from './components/editFunction';
import { successNotify } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';
export default {
  name: 'functionManagement',
  components: {
    editFunction
  },
  data() {
    return {
      table: [],
      functionInfo: {},
      functionName: '',
      isEdit: false,
      type: '',
      columns: functionColumn()
    };
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('managementForm', ['functionTableList']),
    adminAuth() {
      return this.currentUser.user_name_en === 'admin';
    }
  },
  methods: {
    ...mapActions('managementForm', ['query', 'update', 'add', 'deteleFun']),
    add() {
      this.type = 'add';
      this.functionInfo = {};
      this.isEdit = true;
    },
    update(val) {
      this.type = 'edit';
      this.functionInfo = val;
      this.isEdit = true;
    },
    async searchFunction() {
      await this.query({ name: this.functionName });
      this.table = this.functionTableList;
    },
    async editFunction(val) {
      if (this.type === 'add') {
        await this.add(val);
      } else {
        await this.update(val);
      }
      await this.query();
      this.table = this.functionTableList;
      this.isEdit = false;
    },
    remove(val) {
      const { count, name } = val;
      if (count) {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `${name}有 ${count}人,不可删除！`,
            persistent: true,
            ok: '返回'
          })
          .onOk(() => {});
      } else {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `${name}岗位有 ${count}人, 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.deteleFun(val);
            successNotify('删除成功');
            await this.query();
            this.table = this.functionTableList;
          });
      }
    }
  },
  async created() {
    await this.query();
    this.table = this.functionTableList;
    if (!this.adminAuth) {
      this.columns.pop();
    }
  }
};
</script>
<style scoped lang="stylus"></style>
