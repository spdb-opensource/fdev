<template>
  <f-block>
    <div>
      <fdev-table
        :data="currentCompanies"
        title="公司管理"
        :columns="columns"
        titleIcon="list_s_f"
        row-key="name"
        :export-func="handleDownload"
        no-select-cols
      >
        <!-- <template v-slot:top-bottom>
          <fdev-input
            v-model="companyName"
            clearable
            placeholder="请输入公司名称"
            @keyup.13="searchCompany"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                @click="searchCompany"
                class="cursor-pointer"
              />
            </template>
          </fdev-input>
        </template> -->
        <template v-slot:top-right>
          <fdev-btn
            class="q-ml-md btn-height"
            normal
            v-if="adminAuth"
            icon="add"
            label="新增公司"
            @click="addCompanies"
          />
        </template>
        <template v-slot:body-cell-status="props">
          <fdev-td :props="props">
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
                @click="editCompanies(props.row)"
                class="q-mr-lg"
              />
            </span>
            <span>
              <fdev-tooltip v-if="props.row.status === '0'" position="top">
                已废弃的角色不可删除
              </fdev-tooltip>
              <fdev-btn
                flat
                label="删除"
                :disable="props.row.status === '0'"
                @click="deletCompanies(props.row)"
              />
            </span>
          </fdev-td>
        </template>
      </fdev-table>
      <editCompany
        :isOpen="isEdit"
        :dataSource="companyInfo"
        :dailogType="type"
        @close="isEdit = false"
        @add="addComp"
      />
    </div>
  </f-block>
</template>
<script>
import editCompany from './components/editCompany';
import { successNotify } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';
//import { queryCompany } from '../services/methods';
import { findAuthority } from '@/modules/User/utils/model';
export default {
  name: 'companyManagement',
  components: {
    editCompany
  },
  data() {
    return {
      currentCompanies: [],
      companyInfo: {},
      companyName: '',
      searchCompanies: [],
      showAllGroup: true,
      type: '',
      isEdit: false,
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
        },
        {
          name: 'operation',
          label: '操作',
          align: 'left',
          field: 'operation',
          requried: true
        }
      ]
    };
  },
  watch: {
    // companies() {
    //   if (this.showAllGroup) {
    //     this.currentCompanies = [...this.companies, ...this.abandonCompanies];
    //   } else {
    //     this.currentCompanies = this.companies;
    //   }
    // },
    // showAllGroup(val) {
    //   if (!this.companyName) {
    //     if (val) {
    //       this.currentCompanies = [...this.companies, ...this.abandonCompanies];
    //     } else {
    //       this.currentCompanies = this.companies;
    //     }
    //   } else {
    //     if (val) {
    //       this.currentCompanies = this.searchCompanies;
    //     } else {
    //       this.currentCompanies = this.searchCompanies.filter(
    //         item => item.status === '1'
    //       );
    //     }
    //   }
    // },
    companyName(val) {
      !val && this.searchCompany();
    },
    adminAuth(val) {}
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['companies', 'abandonCompanies']),
    adminAuth() {
      return (
        this.currentUser.user_name_en === 'admin' ||
        findAuthority(this.currentUser) === 'admin'
      );
    }
  },

  methods: {
    // ...mapActions('user', [
    //   'fetchCompany',
    //   'addCompany',
    //   'removeCompany',
    //   'updateCompany'
    // ]),
    ...mapActions('userForm', [
      'fetchCompany',
      'removeCompany',
      'addCompany',
      'updateCompany'
    ]),
    addCompanies() {
      this.companyInfo = {};
      this.type = 'add';
      this.isEdit = true;
    },
    editCompanies(val) {
      this.companyInfo = val;
      this.type = 'edit';
      this.isEdit = true;
    },
    async addComp(val) {
      // addComp(val) {
      if (this.type === 'add') {
        let param = { name: val.name };
        await this.addCompany(param);
        successNotify('新增成功');
      } else {
        let param = { name: val.name, id: val.id };
        await this.updateCompany(param);
        successNotify('编辑成功');
      }
      this.isEdit = false;
    },
    async searchCompany() {
      this.searchCompanies = await this.fetchCompany({
        name: this.companyName
      });
      if (this.showAllGroup) {
        this.currentCompanies = this.searchCompanies;
      } else {
        this.currentCompanies = this.searchCompanies.filter(
          item => item.status === '1'
        );
      }
    },
    deletCompanies(val) {
      const { count, label } = val;
      if (count) {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `${label}有 ${count}人,不可删除！`,
            persistent: true,
            ok: '返回'
          })
          .onOk(() => {});
      } else {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `${val.label}有 ${val.count}人, 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.removeCompany(val);
            await this.fetchCompany();
            successNotify('删除成功');
          });
      }
      this.companyName = '';
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
    }
  },
  async created() {
    await this.fetchCompany();
    this.currentCompanies = [...this.companies, ...this.abandonCompanies];
    if (!this.adminAuth) {
      this.columns.pop();
    }
  }
};
</script>
<style lang="stylus" scoped></style>
