<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      titleIcon="list_s_f"
      :data="list"
      :columns="columns"
      title="投产窗口列表"
      row-key="release_node_name"
      :pagination.sync="pagination"
      :filter="filterValue"
      :filter-method="filter"
      :visible-columns="visibleColumns"
      ref="table"
    >
      <template v-slot:top-bottom>
        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="名称"
        >
          <fdev-select
            :value="selectValue"
            multiple
            use-input
            hide-dropdown-icon
            ref="select"
            @new-value="addSelect"
            @input="updateSelectValue($event)"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="setSelect($refs.select)"
              />
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="开始时间"
        >
          <f-date
            :options="sitOptions"
            v-model="jobModel.sitStartDate"
            mask="YYYY-MM-DD"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="结束时间"
        >
          <f-date
            :options="relOptions"
            v-model="jobModel.relStartDate"
            mask="YYYY-MM-DD"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="窗口类型"
        >
          <fdev-select
            :value="releaseType"
            :options="releaseTypeOptions"
            options-dense
            v-show="visibleColumns.includes('type')"
            @input="updateReleaseType($event)"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="小组"
        >
          <fdev-select
            use-input
            @filter="filterGroup"
            :value="group"
            :options="groupOptions"
            options-dense
            option-label="label"
            option-value="id"
            map-options
            emit-value
            @input="updateGroup($event)"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="科技负责人"
        >
          <fdev-select
            :value="spdbManage"
            :options="spdbManagerOptions"
            options-dense
            @input="updateSpdbManage($event)"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:75px"
          bottom-page
          label="投产负责人"
        >
          <fdev-select
            :value="manager"
            :options="managerOptions"
            options-dense
            @input="updateManager($event)"
          />
        </f-formitem>
      </template>

      <template v-slot:top-right>
        <span>
          <fdev-btn
            normal
            ficon="add"
            :disable="!(isProdManager || isKaDianManager)"
            label="新增投产窗口"
            @click="handleDialogOpen('create')"
          />
          <fdev-tooltip v-if="!(isProdManager || isKaDianManager)">
            请联系投产管理员
          </fdev-tooltip>
        </span>
      </template>

      <template v-slot:body-cell-release_node_name="props">
        <fdev-td class="text-ellipsis" :title="props.row.release_node_name">
          <div
            class="text-blue link cursor-pointer"
            @click="saveReleaseType(props.row)"
          >
            <span>{{ props.row.release_node_name }}</span>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-release_spdb_manager_name_cn="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_spdb_manager_name_cn"
        >
          <router-link
            v-if="props.row.release_spdb_manager_id"
            :to="{
              path: `/user/list/${props.row.release_spdb_manager_id}`
            }"
            class="link"
          >
            {{ props.row.release_spdb_manager_name_cn }}
          </router-link>
          <span v-else>
            {{ props.row.release_spdb_manager_name_cn || '-' }}
          </span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-release_manager_name_cn="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_manager_name_cn"
        >
          <router-link
            v-if="props.row.release_manager_id"
            :to="{ path: `/user/list/${props.row.release_manager_id}` }"
            class="link"
          >
            {{ props.row.release_manager_name_cn }}
          </router-link>
          <span v-else>{{ props.row.release_manager_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-create_user_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.create_user_name_cn">
          <router-link
            v-if="props.row.create_user"
            :to="{ path: `/user/list/${props.row.create_user}` }"
            class="link"
          >
            {{ props.row.create_user_name_cn }}
          </router-link>
          <span v-else>{{ props.row.create_user_name_cn || '-' }}</span>
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
                  isOverdue(props.row.release_date) ||
                    !(
                      (isProdManager && isthirdLevelGroup(props.row)) ||
                      isKaDianManager
                    )
                "
                @click="handleDialogOpen('update', props.row)"
              />
              <fdev-tooltip
                v-if="
                  isOverdue(props.row.release_date) ||
                    !(
                      (isProdManager && isthirdLevelGroup(props.row)) ||
                      isKaDianManager
                    )
                "
              >
                <span v-if="isOverdue(props.row.release_date)">
                  投产窗口已过期
                </span>
                <span
                  v-if="
                    !isOverdue(props.row.release_date) &&
                      !(
                        (isProdManager && isthirdLevelGroup(props.row)) ||
                        isKaDianManager
                      )
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </span>

            <!-- 权限:当前投产窗口所属组成员的第三层级组及其子组的投产管理员;或卡点管理员 -->
            <span>
              <fdev-btn
                flat
                :disable="
                  !(
                    (isProdManager && isthirdLevelGroup(props.row)) ||
                    isKaDianManager
                  )
                "
                label="删除"
                @click="deleteRelease(props.row)"
              />
              <fdev-tooltip
                v-if="
                  !(
                    (isProdManager && isthirdLevelGroup(props.row)) ||
                    isKaDianManager
                  )
                "
              >
                请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
              </fdev-tooltip>
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <ReleaseDialog
      :releaseDetail="curRowData"
      :thirdLevelGroups="thirdLevelGroups && thirdLevelGroups.groups"
      :type="type"
      v-model="releaseModalOpen"
      @click="handleDialog"
    />
  </Loading>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { setSelector, getSelector } from '../../utils/setting';
import Loading from '@/components/Loading';
import { getGroupFullName, isValidReleaseDate, perform } from '@/utils/utils';
import ReleaseDialog from '../Manage/components/releaseDialog';
import { successNotify } from '@/utils/utils';
export default {
  name: 'Release',
  components: { Loading, ReleaseDialog },
  data() {
    return {
      releaseModalOpen: false,
      curRowData: null, // 当前编辑的投产窗口数据
      tableData: [],
      list: [],
      loading: false,
      pagination: {
        rowsPerPage: 5
      },
      filterValue: '',
      //selector: ['spdbManager', 'group', 'manager'],
      selector: getSelector() || [],
      spdbManagerOptions: ['全部'],
      groupOptions: [{ label: '全部', id: '' }],
      managerOptions: ['全部'],
      releaseTypeOptions: [],
      type: 'create',
      deepCloneGroups: [],
      typeFilter: perform.typeFilter,
      columns: [
        {
          name: 'release_node_name',
          label: '投产窗口名称',
          field: 'release_node_name'
        },
        {
          name: 'release_date',
          label: '投产日期',
          field: 'release_date',
          sortable: true
        },
        {
          name: 'type',
          label: '窗口类型',
          field: field => this.typeFilter[field.type],
          sortable: true
        },
        {
          name: 'owner_group_name',
          label: '小组',
          field: 'owner_group_name',
          copy: true
        },
        {
          name: 'release_spdb_manager_name_cn',
          label: '科技负责人',
          field: 'release_spdb_manager_name_cn'
        },
        {
          name: 'release_manager_name_cn',
          label: '投产负责人',
          field: 'release_manager_name_cn'
        },
        {
          name: 'create_user_name_cn',
          label: '创建人',
          field: 'create_user_name_cn'
        },
        {
          name: 'btn',
          label: '操作',
          field: 'btn'
        }
      ]
    };
  },
  watch: {
    selector(val) {
      setSelector(val);
      if (val.indexOf('manager') < 0) {
        this.manager = this.managerOptions[0];
      }
      if (val.indexOf('spdbManager') < 0) {
        this.spdbManage = this.spdbManagerOptions[0];
      }
    },
    pagination(val) {
      this.updateCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    },
    'jobModel.sitStartDate': {
      handler(val) {
        this.init();
      }
    },
    'jobModel.relStartDate': {
      handler(val) {
        this.init();
      }
    },
    spdbManage(val) {
      this.filterValue += ',tabel,';
      this.updateTerms(this.filterValue);
    },
    group(val) {
      if (val != this.currentUser.group.id) {
        this.updateIsGroupChange('1');
        this.updateGroup(val);
      }
      this.init();
    },
    manager(val) {
      this.filterValue += ',tabel,';
      this.updateTerms(this.filterValue);
    },
    releaseType(val) {
      this.filterValue += ',tabel,';
      this.updateTerms(this.filterValue);
    },
    selectValue(val) {
      this.filterValue = val.toString();
      if (val == '') {
        this.filterValue = ',';
      }
      this.updateTerms(this.filterValue);
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/releaseList', [
      'jobModel',
      'selectValue',
      'releaseType',
      'spdbManage',
      'manager',
      'group',
      'visibleColumns',
      'currentPage',
      'terms',
      'isGroupChange'
    ]),
    ...mapState('releaseForm', ['releaseList', 'thirdLevelGroups']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    isReleaseManager() {
      return (
        this.currentUser.role.some(role => {
          return role.name === '投产管理员';
        }) || this.isKaDianManager
      );
    },
    // 判断当前用户是否为投产管理员
    isProdManager() {
      return this.currentUser.role.some(role => role.name === '投产管理员');
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/releaseList', [
      'updateJobModelSitStartDate',
      'updateJobModelRelStartDate',
      'updateSelectValue',
      'updateReleaseType',
      'updateSpdbManage',
      'updateManager',
      'updateGroup',
      'updateVisibleColumns',
      'updateCurrentPage',
      'updateTerms',
      'updateIsGroupChange'
    ]),
    ...mapActions('releaseForm', [
      'queryRelease',
      'deleteReleaseNode',
      'queryThreeLevelGroups'
    ]),
    ...mapMutations('releaseForm', ['saveReleaseDetail']),
    ...mapActions('userForm', ['fetchGroup']),
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
    filterGroup(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = terms
        .toLowerCase()
        .split(',')
        .filter(item => {
          return item !== 'tabel' && item !== '';
        });
      this.tableData = rows.filter(row => {
        let isSpdbManager = true; // 科技负责人
        let isManager = true; // 投产负责人
        let isReleaseType = true;

        cols.some(col => {
          if (
            col.name === 'release_spdb_manager_name_cn' &&
            this.spdbManage !== '全部'
          ) {
            isSpdbManager =
              row.release_spdb_manager_name_cn === this.spdbManage;
          }

          if (
            col.name === 'release_manager_name_cn' &&
            this.manager !== '全部'
          ) {
            isManager = row.release_manager_name_cn === this.manager;
          }

          if (col.name === 'type' && this.releaseType !== '全部') {
            isReleaseType = perform.typeFilter[row.type] === this.releaseType;
          }
        });
        return (
          isReleaseType &&
          isSpdbManager &&
          isManager &&
          lowerTerms.every(term => {
            if (term === '') {
              return true;
            }
            let col = cols.some(col => {
              if (typeof getCellValue(col, row) === 'object') {
                return (
                  getCellValue(col, row)
                    .user_name_cn.toLowerCase()
                    .indexOf(term.trim()) > -1
                );
              } else {
                return (
                  (getCellValue(col, row) + '')
                    .toLowerCase()
                    .indexOf(term.trim()) > -1
                );
              }
            });
            return col;
          })
        );
      });
      this.getOptions();
      return this.tableData;
    },
    // 获取筛选条件的下拉选项
    getOptions() {
      this.releaseTypeOptions = [
        '全部',
        ...perform.typeOptions.map(item => {
          return item.label;
        })
      ];
      this.spdbManagerOptions = [
        '全部',
        ...new Set(
          this.list.map(item => {
            return item.release_spdb_manager_name_cn;
          })
        )
      ];
      this.managerOptions = [
        '全部',
        ...new Set(
          this.list.map(item => {
            return item.release_manager_name_cn;
          })
        )
      ];
    },
    async init() {
      this.loading = true;
      const params = {
        start_date: this.jobModel.sitStartDate,
        end_date: this.jobModel.relStartDate,
        owner_groupId: this.group
      };
      await this.queryRelease(params);
      this.loading = false;
      this.list = this.releaseList.map(item => {
        return {
          ...item,
          owner_group_name: getGroupFullName(this.groups, item.owner_groupId)
        };
      });
      this.tableData = this.list;
      this.getOptions();
    },
    relOptions(date) {
      this.jobModel.sitStartDate = this.jobModel.sitStartDate
        ? this.jobModel.sitStartDate
        : '';
      return date > this.jobModel.sitStartDate.replace(/-/g, '/');
    },
    sitOptions(date) {
      if (this.jobModel.relStartDate) {
        return date < this.jobModel.relStartDate.replace(/-/g, '/');
      }
      return true;
    },

    // 判断当前投产窗口是否过期
    isOverdue(date) {
      return !isValidReleaseDate(date);
    },

    // 判断当前投产窗口所属组是否属于当前用户的第三层级组及子组
    isthirdLevelGroup(data) {
      return !!this.thirdLevelGroups.groups.find(
        item => item.owner_groupId === data.owner_groupId
      );
    },

    // 获取当前用户的第三层级组及其子组
    getThirdLevelGroups() {
      const thirdLevelGroups = this.thirdLevelGroups;
      if (
        !thirdLevelGroups ||
        thirdLevelGroups.user_id !== this.currentUser.id
      ) {
        this.queryThreeLevelGroups();
      }
    },
    deleteRelease(data) {
      return this.$q
        .dialog({
          title: `删除确认`,
          message: `请确认是否删除${data.release_node_name}投产窗口`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.deleteReleaseNode({
            release_node_name: data.release_node_name
          });
          successNotify('删除成功！');
          await this.init();
        });
    },
    handleDialogOpen(type, curRowData) {
      if (curRowData) {
        this.curRowData = curRowData;
      }
      this.type = type;
      this.releaseModalOpen = true;
    },
    handleDialog() {
      this.init();
      this.releaseModalOpen = false;
      this.rowSelected = [];
    },
    saveReleaseType(data) {
      this.saveReleaseDetail({
        type: data.type
      });
      this.$router.push(`/release/list/${data.release_node_name}`);
    }
  },
  async created() {
    await this.getThirdLevelGroups();
    await this.fetchGroup();
    this.deepCloneGroups = this.groups.map(item => {
      return { label: item.fullName, id: item.id };
    });
    this.groupOptions = [{ label: '全部', id: '' }, ...this.deepCloneGroups];
    if (this.isGroupChange === '0') {
      this.updateGroup(this.currentUser.group.id);
    }
    await this.init();
  },
  mounted() {
    if (this.terms) {
      this.filterValue = this.terms;
    }
    this.pagination = this.currentPage;
  }
};
</script>

<style lang="stylus" scoped></style>
