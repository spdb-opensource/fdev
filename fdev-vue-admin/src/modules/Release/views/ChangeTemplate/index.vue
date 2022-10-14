<template>
  <div class="changeTemplate">
    <f-block>
      <div class="bg-white">
        <Loading :visible="loading">
          <fdev-table
            titleIcon="list_s_f"
            :data="tableData"
            :columns="columns"
            row-key="id"
            :pagination.sync="pagination"
            title="变更模板管理列表"
            :filter="filterValue"
            :filter-method="filter"
            :visible-columns="visibleColumns"
            ref="table"
          >
            <template v-slot:top-right>
              <span>
                <fdev-btn
                  normal
                  ficon="add"
                  :disable="!(role || isKaDianManager)"
                  label="新建变更模板"
                  @click="openEditDialog('新建')"
                />
                <fdev-tooltip v-if="!(role || isKaDianManager)">
                  请联系投产管理员
                </fdev-tooltip>
              </span>
            </template>

            <template v-slot:top-bottom>
              <f-formitem
                class="col-4 q-pr-md"
                label-style="width:60px"
                bottom-page
                label="名称"
              >
                <fdev-select
                  @input="updateSelectValue($event)"
                  :value="selectValue"
                  multiple
                  use-input
                  hide-dropdown-icon
                  ref="select"
                  @new-value="addSelect"
                >
                  <template v-slot:append>
                    <f-icon
                      name="search"
                      class="text-primary"
                      @click="setSelect($refs.select)"
                    />
                  </template>
                </fdev-select>
              </f-formitem>

              <f-formitem
                class="col-4 q-pr-md"
                label-style="width:60px"
                bottom-page
                label="所属小组"
              >
                <fdev-select
                  @input="updateTableGroup($event)"
                  :value="tableGroup"
                  :options="groups"
                  options-dense
                />
              </f-formitem>
              <f-formitem
                class="col-4 q-pr-md"
                label-style="width:60px"
                bottom-page
                label="所属系统"
              >
                <fdev-select
                  @input="updateTableSystem($event)"
                  :value="tableSystem"
                  :options="systems"
                  options-dense
                />
              </f-formitem>

              <f-formitem
                class="col-4 q-pr-md"
                label-style="width:60px"
                bottom-page
                label="变更类型"
              >
                <fdev-select
                  @input="updateTableType($event)"
                  :value="tableType"
                  :options="types"
                  options-dense
                />
              </f-formitem>
            </template>

            <template v-slot:body-cell-owner_system_name="props">
              <fdev-td class="text-ellipsis">
                <router-link
                  :to="`/release/templateDetail/${props.row.id}`"
                  class="link"
                  :title="props.value"
                >
                  {{ props.value }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.value }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </router-link>
              </fdev-td>
            </template>
            <template v-slot:body-cell-owner_app_name="props">
              <fdev-td class="text-ellipsis">
                <router-link
                  :to="`/app/list/${props.row.owner_app}`"
                  class="link"
                  :title="props.value"
                >
                  {{ props.value }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.value }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </router-link>
              </fdev-td>
            </template>
            <template v-slot:body-cell-update_username_cn="props">
              <fdev-td class="text-ellipsis">
                <router-link
                  :to="`/user/list/${props.row.update_user}`"
                  class="link"
                  v-if="props.row.update_user"
                >
                  {{ props.value }}
                </router-link>
                <span :title="props.value" v-else>{{
                  props.value || '-'
                }}</span>
              </fdev-td>
            </template>
            <template v-slot:body-cell-template_type="props">
              <fdev-td :title="props.value" class="text-ellipsis">
                {{ typeName(props.value) || '-' }}
              </fdev-td>
            </template>

            <template v-slot:body-cell-btn="props">
              <fdev-td>
                <div class="q-gutter-sm row items-center no-wrap">
                  <span>
                    <fdev-btn
                      flat
                      label="编辑"
                      :disable="
                        !(
                          (role && isthirdLevelGroup(props.row)) ||
                          isKaDianManager
                        )
                      "
                      @click="openEditDialog('编辑', props.row)"
                    />
                    <fdev-tooltip
                      v-if="
                        !(
                          (role && isthirdLevelGroup(props.row)) ||
                          isKaDianManager
                        )
                      "
                    >
                      请联系当前变更模板所属小组成员的第三层级组及其子组的投产管理员
                    </fdev-tooltip>
                  </span>
                  <span>
                    <fdev-btn
                      flat
                      label="删除"
                      :disable="
                        !(
                          (role && isthirdLevelGroup(props.row)) ||
                          isKaDianManager
                        )
                      "
                      @click="deleted(props.row.id)"
                    />
                    <fdev-tooltip
                      v-if="
                        !(
                          (role && isthirdLevelGroup(props.row)) ||
                          isKaDianManager
                        )
                      "
                    >
                      请联系当前变更模板所属小组成员的第三层级组及其子组的投产管理员
                    </fdev-tooltip>
                  </span>
                </div>
              </fdev-td>
            </template>
          </fdev-table>

          <EditDialog
            :title="title"
            :url="url"
            :thirdLevelGroups="thirdLevelGroups && thirdLevelGroups.groups"
            v-model="editTemplate"
            :data="dialogData"
            @submit="handleTemplate"
          />
        </Loading>
      </div>
    </f-block>
  </div>
</template>

<script>
import { successNotify, getGroupFullName } from '@/utils/utils';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';

import Loading from '@/components/Loading';
import EditDialog from './componetns/EditDialog';
import { createTemplate } from '../../utils/model';

export default {
  name: 'ChangeTemplate',
  components: {
    Loading,
    EditDialog
  },
  data() {
    return {
      filterValue: '',
      groups: [],
      systems: [],
      types: [],
      editTemplate: false,
      loading: false,
      pagination: {
        rowsPerPage: 5
      },
      title: '',
      dialogData: {},
      tableData: [],
      filterData: [],
      url: 'create',
      columns: [
        {
          name: 'owner_system_name',
          label: '所属系统',
          field: 'owner_system_name',
          sortable: true
        },
        {
          name: 'owner_group',
          label: '所属小组',
          copy: true,
          field: field =>
            field.owner_group_name
              ? getGroupFullName(this.groupsList, field.owner_group)
              : ''
        },
        {
          name: 'template_type',
          label: '变更类型',
          field: 'template_type',
          sortable: true
        },
        {
          name: 'owner_app_name',
          label: '所属应用',
          field: 'owner_app_name'
        },
        {
          name: 'update_username_cn',
          label: '更新人',
          field: 'update_username_cn'
        },
        {
          name: 'update_time',
          label: '更新时间',
          field: 'update_time',
          sortable: true
        },
        { name: 'btn', label: '操作' }
      ]
    };
  },
  watch: {
    pagination(val) {
      this.updateCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString();
      if (val == '') {
        this.filterValue = ',';
      }
      this.updateTerms(this.filterValue);
    },
    tableGroup(val) {
      this.filterValue += ',tabel,';
      this.updateTerms(this.filterValue);
    },
    tableSystem(val) {
      this.filterValue += ',tabel,';
      this.updateTerms(this.filterValue);
    },
    tableType(val) {
      this.filterValue += ',tabel,';
      this.updateTerms(this.filterValue);
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/changeTemplate', [
      'selectValue',
      'tableGroup',
      'tableSystem',
      'tableType',
      'visibleColumns',
      'currentPage',
      'terms'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapState('releaseForm', ['templateOfChanges', 'thirdLevelGroups']),
    ...mapState('userForm', {
      groupsList: 'groups'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    role() {
      const roler = this.currentUser.role.find(role => {
        return role.name === '投产管理员';
      });
      return Boolean(roler);
    },
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/changeTemplate', [
      'updateSelectValue',
      'updateTableGroup',
      'updateTableSystem',
      'updateTableType',
      'updateVisibleColumns',
      'updateCurrentPage',
      'updateTerms'
    ]),
    ...mapActions('userForm', {
      fetchGroup: 'fetchGroup'
    }),
    ...mapActions('release', ['getSystem']),
    ...mapActions('releaseForm', [
      'getTemplate',
      'deleteTemplate',
      'queryThreeLevelGroups'
    ]),
    // 打开编辑弹窗
    openEditDialog(msg, item) {
      this.editTemplate = true;
      this.title = msg;
      if (msg === '新建') {
        this.url = 'create';
        this.dialogData = Object.assign(createTemplate(), {
          owner_group: this.currentUser.group.id,
          owner_group_name: this.currentUser.group.name
        });
      } else {
        this.url = 'update';
        let data = {
          ...item,
          system_abbr: '',
          resource_giturl: ''
        };
        this.dialogData = data;
      }
    },
    // 提交
    handleTemplate(data) {
      this.getTableData();
      this.editTemplate = data;
      this.getSystem();
    },
    // 查询数据
    async getTableData() {
      this.loading = true;
      await this.getTemplate();
      this.tableData = this.templateOfChanges;
      this.filterData = this.tableData;
      this.getOptions();
      this.loading = false;
    },
    async deleted(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该模板吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteTemplate({ id: id });
          successNotify('删除成功');
          this.getTableData();
        });
    },
    // 表格筛选，组件监听filterValue
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = terms
        .toLowerCase()
        .split(',')
        .filter(item => {
          return item !== 'tabel' && item !== '';
        });
      this.filterData = rows.filter(row => {
        let isSystem = true;
        let isType = true;
        let isGroup = true;

        cols.some(col => {
          if (col.name === 'owner_group' && this.tableGroup !== '全部') {
            isGroup =
              getGroupFullName(this.groupsList, row.owner_group) ===
              this.tableGroup;
          }
          if (col.name === 'owner_system_name' && this.tableSystem !== '全部') {
            isSystem = row.owner_system_name === this.tableSystem;
          }
          if (col.name === 'template_type' && this.tableType !== '全部') {
            isType = this.typeName(row.template_type) === this.tableType;
          }
        });
        let input = lowerTerms.every(item => {
          if (item === '') {
            return true;
          }
          let col = cols.some(col => {
            return (
              this.typeName(
                (getCellValue(col, row) + '').toLowerCase()
              ).indexOf(item) > -1
            );
          });
          return col;
        });
        return isSystem && isType && input && isGroup;
      });
      return this.filterData;
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    // 获取筛选条件的下拉选项
    getOptions() {
      this.groups = [
        ...new Set([
          '全部',
          ...this.filterData.map(item => {
            return item.owner_group_name
              ? getGroupFullName(this.groupsList, item.owner_group)
                ? getGroupFullName(this.groupsList, item.owner_group)
                : '全部'
              : '全部';
          })
        ])
      ];
      this.systems = [
        '全部',
        ...new Set(
          this.filterData.map(item => {
            return item.owner_system_name;
          })
        )
      ];
      this.types = [
        '全部',
        ...new Set(
          this.filterData.map(item => {
            return this.typeName(item.template_type);
          })
        )
      ];
    },
    typeName(val) {
      if (val === 'proc') {
        return '生产';
      } else if (val === 'gray') {
        return '灰度';
      } else {
        return val;
      }
    },

    // 判断当前变更模板所属小组是否属于当前用户的第三层级组及子组
    isthirdLevelGroup(data) {
      return !!this.thirdLevelGroups.groups.find(
        item => item.owner_groupId === data.owner_group
      );
    },

    // 获取当前用户的第三层级组及其子组
    getThirdLevelGroups() {
      const thirdLevelGroups = this.thirdLevelGroups;
      if (
        !thirdLevelGroups ||
        (thirdLevelGroups && thirdLevelGroups.user_id !== this.currentUser.id)
      ) {
        this.queryThreeLevelGroups();
      }
    }
  },
  async created() {
    await this.getThirdLevelGroups();
    await this.fetchGroup();
    await this.getTableData();
  },
  mounted() {
    if (this.terms) {
      this.filterValue = this.terms;
    }
    this.pagination = this.currentPage;
  }
};
</script>
