<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="currentRoles"
        :columns="columns"
        row-key="name"
        :pagination="{
          sortBy: 'count',
          descending: true,
          page: 1,
          rowsPerPage: 5
        }"
        titleIcon="member_s_f"
        title="角色列表"
        no-select-cols
        :export-func="handleDownload"
      >
        <template v-slot:top-bottom>
          <f-formitem page label="角色名称">
            <fdev-input v-model="roleName" clearable @keyup.13="searchFunction"
              ><template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="searchFunction"
                />
              </template>
            </fdev-input>
          </f-formitem>
        </template>
        <template v-slot:top-right>
          <fdev-select
            :value="roleType"
            @input="updateRoleType($event)"
            :options="roleTypeOptions"
            option-value="val"
            option-label="label"
            map-options
            emit-value
            options-dense
            flat
            class="q-pr-md"
          ></fdev-select>
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
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';

import Loading from '@/components/Loading';
//import Authorized from '@/components/Authorized';

export default {
  name: 'Other',
  components: { Loading },
  data() {
    return {
      columns: [
        {
          name: 'name',
          label: '角色名称',
          align: 'left',
          field: 'name',
          copy: true
        },
        {
          name: 'count',
          label: '人数',
          align: 'left',
          field: 'count',
          sortable: true
        },
        {
          name: 'functions',
          label: '角色对应功能',
          align: 'left',
          field: 'functions',
          copy: true
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
        }
      ],
      loading: true,
      addModel: '',
      openedType: '',

      roleName: '',
      roleTypeOptions: [
        { label: '使用', val: '0' },
        { label: '废弃', val: '1' }
      ],
      currentCompanies: [],
      currentRoles: []
    };
  },
  watch: {
    roles() {
      this.currentRoles = this.roles;
    },
    roleType(val) {
      this.currentRoles = val === '0' ? this.roles : this.abandonRoles;
    }
  },
  computed: {
    ...mapState('userActionSaveUser/dimensionQuery/companiesAndRoles', [
      'companyType',
      'roleType'
    ]),
    ...mapState('userForm', [
      'companies',
      'abandonCompanies',
      'roles',
      'abandonRoles'
    ])
  },
  methods: {
    ...mapMutations('userActionSaveUser/dimensionQuery/companiesAndRoles', [
      'updateCompanyType',
      'updateRoleType'
    ]),
    ...mapActions('userForm', [
      'fetchCompany',
      'fetchRole',
      'removeCompany',
      'removeRole',
      'addCompany',
      'addRole',
      'updateCompany',
      'updateRole'
    ]),
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
    },
    async searchFunction() {
      await this.fetchRole({ name: this.roleName });
      this.currentRoles = this.roles;
    }
  },
  async created() {
    await this.fetchRole();
    this.currentRoles = this.roleType === '0' ? this.roles : this.abandonRoles;
    this.loading = false;
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.h-table{
 max-height: 38vw;
 width:32vw
}
</style>
