<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="currentCompanies"
        :columns="columns"
        row-key="name"
        binary-state-sort
        :pagination="{
          sortBy: 'count',
          descending: true,
          page: 1,
          rowsPerPage: 5
        }"
        titleIcon="list_s_f"
        title="公司列表"
        :export-func="handleDownload"
        no-select-cols
      >
        <template v-slot:top-right>
          <fdev-select
            :value="companyType"
            @input="updateCompanyType($event)"
            :options="companyTypeOptions"
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
import { required } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
//import Authorized from '@/components/Authorized';

const entityType = {
  company: {
    label: '公司',
    remove: 'removeCompany',
    add: 'addCompany',
    update: 'updateCompany'
  },
  role: {
    label: '角色',
    remove: 'removeRole',
    add: 'addRole',
    update: 'updateRole'
  }
};

export default {
  name: 'Other',
  components: { Loading },
  data() {
    return {
      columns: [
        {
          name: 'name',
          label: '公司名称',
          sortable: true,
          align: 'left',
          field: row => row.name,
          format: val => `${val}`
        },
        {
          name: 'count',
          label: '公司人数',
          sortable: true,
          align: 'left',
          field: 'count'
        },
        {
          name: 'createTime',
          label: '创建时间',
          sortable: true,
          align: 'left',
          field: 'createTime',
          format: val => val || '-'
        },
        {
          name: 'status',
          label: '公司状态',
          align: 'left',
          sortable: true,
          field: 'status',
          format: val => {
            let state = {
              '0': { label: '已删除', color: 'red' },
              '1': { label: '使用中', color: 'green' }
            };
            return state[val];
          }
        },
        {
          name: 'deleteTime',
          label: '删除时间',
          sortable: true,
          align: 'left',
          field: 'deleteTime',
          format: val => val || '-'
        }
      ],
      loading: true,
      addModel: '',
      openedType: '',
      addDialogOpened: false,
      companyTypeOptions: [
        { label: '使用', val: '0' },
        { label: '废弃', val: '1' }
      ],
      currentCompanies: [],
      currentRoles: []
    };
  },
  validations: {
    addModel: {
      required
    }
  },
  watch: {
    companies() {
      this.currentCompanies = this.companies;
    },
    companyType(val) {
      this.currentCompanies =
        val === '0' ? this.companies : this.abandonCompanies;
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
    async handleUpdate(row, entity, value, initialValue) {
      let type = entityType[entity];
      try {
        await this[type.update](row);
      } catch (e) {
        row.name = initialValue;
      }
    },
    async handleRemove(row, entity) {
      let type = entityType[entity];
      if (row.count !== 0) {
        return this.$q
          .dialog({
            title: `删除${type.label}`,
            message: `有 ${row.count} 个人属于这个${type.label}, 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this[type.remove](row);
          });
      } else {
        return this.$q
          .dialog({
            title: `删除${type.label}`,
            message: `确认删除该${type.label}么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this[type.remove](row);
          });
      }
    },
    handleDownload() {
      let _this = this;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '公司',
          '公司人数',
          '创建时间',
          '公司状态',
          '删除时间'
        ];
        const filterVal = [
          'name',
          'count',
          'createTime',
          'status',
          'deleteTime'
        ];
        const data = _this.formatJson(filterVal, this.currentCompanies);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '公司列表',
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
    handleAddDialogOpened(entity) {
      this.openedType = entityType[entity];
      this.addModel = '';
      this.$v.addModel.$reset();

      this.addDialogOpened = true;
    },
    async queryCompany(val) {
      await this.fetchCompany();
      this.currentCompanies =
        val === '0' ? this.companies : this.abandonCompanies;
    },
    async handleAdd() {
      if (this.$v.addModel.$invalid) {
        return;
      }
      await this[this.openedType.add]({
        name: this.addModel
      });
      this.addDialogOpened = false;
    },
    required(val) {
      return !!val;
    }
  },

  async created() {
    await this.fetchCompany();
    this.currentCompanies =
      this.companyType === '0' ? this.companies : this.abandonCompanies;
    //this.currentCompanies = this.companies;
    this.loading = false;
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped></style>
