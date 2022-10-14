<template>
  <f-block>
    <fdev-table
      :data="currentRoles"
      title="角色管理"
      :columns="columns"
      :export-func="handleDownload"
      titleIcon="member_s_f"
      no-select-cols
    >
      <template v-slot:top-bottom>
        <f-formitem page label="角色名称">
          <fdev-input
            v-model="searchInfo.name"
            clearable
            @keyup.13="searchRole(searchInfo)"
            ><template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="searchRole(searchInfo)"
              />
            </template>
          </fdev-input>
        </f-formitem>
      </template>
      <template v-slot:top-right>
        <fdev-btn
          normal
          icon="add"
          label="新增角色"
          v-if="adminAuth"
          @click="add"
        />
      </template>
      <template v-slot:body-cell-status="props">
        <fdev-td :props="props" :title="props.value.label">
          <fdev-badge
            class="text-weight-medium q-pa-sm"
            :color="props.value.color"
            :label="props.value.label"
          />
        </fdev-td>
      </template>
      <template v-slot:body-cell-count="props">
        <fdev-td :props="props" :title="props.row.count">
          {{ props.row.count }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-operation="props">
        <fdev-td :props="props">
          <span>
            <fdev-tooltip v-if="props.row.status === '0'" position="top">
              已废弃的角色不可编辑
            </fdev-tooltip>
            <fdev-btn
              flat
              label="编辑"
              :disable="props.row.status === '0'"
              @click="editRole(props.row)"
            />
          </span>
          <span>
            <fdev-tooltip v-if="props.row.status === '0'" position="top">
              已废弃的角色不可删除
            </fdev-tooltip>
            <fdev-btn
              flat
              class="q-ml-lg"
              :disable="props.row.status === '0'"
              label="删除"
              @click="deleteRole(props.row)"
            />
          </span>
        </fdev-td>
      </template>
    </fdev-table>
    <editRole
      :isOpen="isEdit"
      :menuOption="menus"
      :roleOption="roleTypes"
      :dataSource="roleInfo"
      :dailogType="type"
      @close="isEdit = false"
      @edit="confirmEdit"
    />
  </f-block>
</template>
<script>
import editRole from './components/editRole';
import {
  queryMenu //查询所有菜单
} from '@/modules/Management/services/methods';
import { successNotify } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';
import { findAuthority } from '@/modules/User/utils/model';
export default {
  name: 'roleManagement',
  components: { editRole },
  data() {
    return {
      //  tableInfo
      currentRoles: [],
      columns: [
        {
          name: 'name',
          label: '角色名称',
          align: 'left',
          sortable: true,
          field: 'name'
        },
        {
          name: 'count',
          label: '人数',
          align: 'left',
          sortable: true,
          field: 'count'
        },
        {
          name: 'functions',
          label: '角色对应功能',
          align: 'left',
          field: 'functions'
        },
        {
          name: 'status',
          label: '状态',
          align: 'left',
          field: 'status',
          sortable: true,
          format: val => {
            let state = {
              '0': { label: '已废弃', color: 'red' },
              '1': { label: '使用中', color: 'green' }
            };
            return state[val];
          }
        },
        {
          name: 'operation',
          label: '操作',
          align: 'left',
          field: 'operation'
        }
      ],
      searchInfo: {
        name: ''
      },
      isEdit: false,
      // dialogInfo
      roleInfo: {},
      menus: [],
      type: ''
    };
  },
  computed: {
    ...mapState('user', ['roles', 'currentUser', 'roleTypes']),
    ...mapState('userForm', ['roles', 'abandonRoles']),
    adminAuth() {
      return (
        this.currentUser.user_name_en === 'admin' ||
        findAuthority(this.currentUser) === 'admin'
      );
      // return this.currentUser.user_name_en === 'c-chengp1';
    }
  },
  filters: {},
  watch: {
    'searchInfo.name'(val) {
      !val && this.searchRole(val);
    }
  },
  methods: {
    ...mapActions('userForm', [
      'fetchRole',
      //'queryRoleType',
      //'roleUpdate',
      'updateRole',
      'addRole',
      'removeRole'
    ]),
    editRole(val) {
      this.type = 'edit';
      if (val.menus === '') {
        val.menus = [];
      }
      if (val.permissions === '') {
        val.permissions = [];
      }
      this.roleInfo = val;
      this.isEdit = true;
    },
    add() {
      this.roleInfo = {
        menus: []
      };
      this.type = 'add';
      this.isEdit = true;
    },
    async confirmEdit(val) {
      if (this.type === 'add') {
        await this.addRole(val);
        successNotify('新增成功');
        this.isEdit = false;
        this.searchInfo.name = '';
        await this.fetchRole();
        this.currentRoles = [...this.roles, ...this.abandonRoles];
      } else {
        await this.updateRole(val);
        successNotify('编辑成功');
        this.isEdit = false;
        !this.searchInfo.name
          ? await this.fetchRole()
          : await this.fetchRole({ name: this.searchInfo.name });
        this.currentRoles = [...this.roles, ...this.abandonRoles];
      }
    },
    deleteRole(val) {
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
            message: `${name}有 ${count}人, 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            const { id } = val;
            await this.removeRole({ id });
            successNotify('删除成功');
            !this.searchInfo.name
              ? await this.fetchRole()
              : await this.fetchRole({ name: this.searchInfo.name });
            this.currentRoles = [...this.roles, ...this.abandonRoles];
          });
      }
      this.companyName = '';
    },
    async searchRole(val) {
      await this.fetchRole(val);
      this.currentRoles = [...this.roles, ...this.abandonRoles];
    },
    async queryMenuList() {
      this.menus = await queryMenu();
    },
    handleDownload() {
      let _this = this;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = ['角色名称', '人数', '角色对应功能', '角色状态'];
        const filterVal = ['name', 'count', 'functions', 'status'];
        const data = _this.formatJson(filterVal, this.currentRoles);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '角色列表',
          bookType: 'xlsx'
        });
      });
    },
    formatJson(filterVal, appData) {
      return appData.map(row => {
        return filterVal.map(col => {
          if (col === 'status') {
            if (row[col] === '0') {
              return '已删除';
            } else {
              return '使用中';
            }
          } else {
            return row[col];
          }
        });
      });
    }
  },
  async created() {
    // tableInfo
    //await this.queryRoleType();
    await this.fetchRole();
    this.currentRoles = [...this.roles, ...this.abandonRoles];
    // dialogInfo
    await this.queryMenuList();
    if (!this.adminAuth) {
      this.columns.pop();
    }
  }
};
</script>
<style lang="stylus" scoped></style>
