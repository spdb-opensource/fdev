<template>
  <div>
    <loading :visible="loading">
      <tabs-block :data="tabData" :default-tab="tab" @change="changeTab">
        <fdev-tab-panel name="personal">
          <Information :display="false" />
        </fdev-tab-panel>

        <fdev-tab-panel name="tasks">
          <fdev-table
            :data="jobs"
            title="我的任务列表"
            titleIcon="list_s_f"
            :columns="columns"
            row-key="job.id"
            :filter="filter"
            :filter-method="taskFilter"
            :pagination.sync="pagination"
            no-export
            class="my-sticky-column-table"
            :visible-columns="visibleColumns"
          >
            <template v-slot:top-bottom>
              <f-formitem label="搜索条件" label-style="width:auto;">
                <fdev-select
                  use-input
                  use-chips
                  multiple
                  v-model="terms"
                  @new-value="addTerm"
                  placeholder="输入关键字，回车区分"
                  hide-dropdown-icon
                  ref="terms"
                >
                  <template v-slot:append>
                    <f-icon
                      name="search"
                      class="cursor-pointer"
                      @click="setSelect()"
                    />
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem
                label="是否包含归档任务"
                label-style="width:auto;margin-left:10px"
              >
                <fdev-select
                  :options="incloudFileOption"
                  v-model="incloudFile"
                  @input="fetchUserTask($event)"
                >
                </fdev-select>
              </f-formitem>
              <f-formitem
                label="延期阶段"
                bottom-page
                label-style="width:auto;margin-left:10px"
              >
                <fdev-select
                  multiple
                  :value="delayStage"
                  :options="delayStageOptions"
                  option-label="label"
                  option-value="value"
                  @input="saveDelayStage($event)"
                  @clear="saveDelayStage()"
                  clearable
                />
              </f-formitem>
            </template>

            <template v-slot:body-cell-name="props">
              <fdev-td class="text-ellipsis" :title="props.value">
                <f-icon
                  v-if="props.row.delayFlag == true"
                  name="alert_t_f"
                  style="color:red"
                  title="延期告警！"
                />
                <router-link
                  :to="`/job/list/${props.row.id}`"
                  class="link"
                  :title="props.value"
                >
                  {{ props.value }}
                </router-link>
              </fdev-td>
            </template>

            <template v-slot:body-cell-stage="props">
              <fdev-td
                class="text-ellipsis"
                :title="stageFilter(props.row.stage, props.row.taskType)"
              >
                <f-status-color
                  :gradient="props.row.stage | statusColorFilter"
                ></f-status-color>
                {{ stageFilter(props.row.stage, props.row.taskType) }}
              </fdev-td>
            </template>

            <template v-slot:body-cell-project_name="props">
              <fdev-td class="text-ellipsis">
                <router-link
                  :to="
                    `${getPath(props.row.applicationType)}/${
                      props.row.project_id
                    }`
                  "
                  class="link"
                  v-if="props.row.project_name"
                  :title="props.row.project_name"
                >
                  {{ props.row.project_name }}
                </router-link>
                <span v-else title="-">-</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-test_tag="props">
              <fdev-td class="text-ellipsis">
                <span
                  v-if="
                    Array.isArray(props.row.tag) && props.row.tag.length > 0
                  "
                  class="text-ellipsis"
                  :title="props.row.tag | filterTag"
                >
                  {{ props.row.tag | filterTag }}
                </span>
                <span v-else title="-">-</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-creator="props">
              <fdev-td class="text-ellipsis">
                <router-link
                  :to="`/user/list/${props.value.id}`"
                  class="link"
                  :title="props.value.user_name_cn"
                >
                  {{ props.value.user_name_cn }}
                </router-link>
              </fdev-td>
            </template>

            <template v-slot:body-cell-developer="props">
              <fdev-td>
                <div
                  class="text-ellipsis"
                  v-if="
                    Array.isArray(props.row.developer) &&
                      props.row.developer.length > 0
                  "
                  :title="
                    props.row.developer.map(v => v.user_name_cn).join('，')
                  "
                >
                  <span v-for="(item, index) in props.value" :key="index">
                    <router-link :to="`/user/list/${item.id}`" class="link">
                      {{ item.user_name_cn }}
                    </router-link>
                  </span>
                </div>
                <span v-else title="-">-</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-master="props">
              <fdev-td>
                <div
                  class="text-ellipsis"
                  v-if="
                    Array.isArray(props.row.master) &&
                      props.row.master.length > 0
                  "
                  :title="props.row.master.map(v => v.user_name_cn).join('，')"
                >
                  <span v-for="(item, index) in props.value" :key="index">
                    <router-link :to="`/user/list/${item.id}`" class="link">
                      {{ item.user_name_cn }}
                    </router-link>
                  </span>
                </div>
                <span v-else title="-">-</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-spdb_master="props">
              <fdev-td>
                <div
                  class="text-ellipsis"
                  v-if="
                    Array.isArray(props.row.spdb_master) &&
                      props.row.spdb_master.length > 0
                  "
                  :title="
                    props.row.spdb_master.map(v => v.user_name_cn).join('，')
                  "
                >
                  <span v-for="(item, index) in props.value" :key="index">
                    <router-link :to="`/user/list/${item.id}`" class="link">
                      {{ item.user_name_cn }}
                    </router-link>
                  </span>
                </div>
                <span v-else title="-">-</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-tester="props">
              <fdev-td>
                <div
                  class="text-ellipsis"
                  v-if="
                    Array.isArray(props.row.tester) &&
                      props.row.tester.length > 0
                  "
                  :title="props.row.tester.map(v => v.user_name_cn).join('，')"
                >
                  <span v-for="(item, index) in props.value" :key="index">
                    <router-link :to="`/user/list/${item.id}`" class="link">
                      {{ item.user_name_cn }}
                    </router-link>
                  </span>
                </div>
                <span v-else title="-">-</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-operation="props">
              <fdev-td :auto-width="true">
                <div class="row no-wrap q-gutter-x-sm">
                  <div
                    v-if="
                      !(props.row.taskType && props.row.taskType == 2) &&
                        showBtn(props.row)
                    "
                  >
                    <fdev-tooltip
                      v-if="
                        props.row.taskSpectialStatus == 1 ||
                          props.row.taskSpectialStatus == 2
                      "
                      anchor="top middle"
                      self="center middle"
                      :offest="[-20, 0]"
                    >
                      <span>处于暂缓状态</span>
                    </fdev-tooltip>
                    <fdev-btn
                      :to="`/job/list/${props.row.id}?tab=manage`"
                      label="处理"
                      flat
                      :disable="
                        props.row.taskSpectialStatus == 1 ||
                          props.row.taskSpectialStatus == 2
                      "
                    />
                  </div>
                  <fdev-btn
                    label="归档"
                    flat
                    v-if="
                      isProduction(
                        props.row.stage,
                        props.row.creator,
                        props.row.master,
                        props.row.spdb_master
                      )
                    "
                    @click="
                      handleHint(
                        props.row.id,
                        props.row.__index,
                        props.row.name
                      )
                    "
                  />
                </div>
              </fdev-td>
            </template>
          </fdev-table>
        </fdev-tab-panel>

        <fdev-tab-panel name="todos">
          <fdev-btn-toggle
            v-model="todosTab"
            :options="[
              { label: `待办(${eventNum.todos})`, value: 'todos' },
              { label: `已完成(${eventNum.done})`, value: 'done' }
            ]"
            class="q-mb-md"
          />

          <template v-if="todosTab === 'todos'" v-once>
            <todos label="todo" name="todo" v-model="eventNum" />
          </template>

          <template v-if="todosTab === 'done'" v-once>
            <todos label="done" name="done" v-model="eventNum" />
          </template>
        </fdev-tab-panel>
      </tabs-block>
    </loading>
  </div>
</template>
<script>
import { taskStageColorMap } from '@/modules/Job/utils/constants';
import Loading from '@/components/Loading';
import TabsBlock from '@/components/TabsBlock';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import {
  formatSelectDisplay,
  successNotify,
  taskStage,
  taskStage1,
  getIdsFormList
} from '@/utils/utils';
import { perform, taskSpecialStatus } from '@/modules/Job/utils/constants';
import {
  setPagination,
  getPagination,
  getTableCol,
  setTableCol
} from './setting';
import Todos from './todo';
import Information from '@/modules/HomePage/views/myInfo';

export default {
  name: 'Center',
  components: {
    Todos,
    Loading,
    Information,
    TabsBlock
  },
  data() {
    return {
      incloudFile: '否',
      incloudFileOption: ['否', '是'],
      ...perform,
      tab: 'tasks',
      master: '',
      spdb_master: '',
      todosTab: 'todos',
      pagination: getPagination(),
      columns: [
        { name: 'name', label: '任务名称', field: 'name', required: true },
        {
          name: 'project_name',
          label: '所属应用',
          field: 'project_name'
        },
        {
          name: 'feature_branch',
          label: '分支名称',
          field: 'feature_branch'
        },
        {
          name: 'stage',
          label: '任务阶段',
          field: row => this.stageFilter(row.stage, row.taskType),
          sortable: true,
          sort: (a, b) => {
            return this.sort(a, b);
          }
        },
        {
          name: 'taskSpectialStatus',
          label: '暂缓',
          field: row => this.statusFilter(row.taskSpectialStatus)
        },
        {
          name: 'plan_start_time',
          label: '计划启动日期',
          field: 'plan_start_time',
          sortable: true
        },
        {
          name: 'plan_inner_test_time',
          label: '计划内测日期',
          field: 'plan_inner_test_time',
          sortable: true
        },
        {
          name: 'plan_fire_time',
          label: '计划投产日期',
          field: 'plan_fire_time',
          sortable: true
        },
        {
          name: 'test_tag',
          label: '提测状态',
          field: 'tag'
        },
        {
          name: 'creator',
          label: '创建者',
          field: 'creator',
          align: 'left'
        },
        {
          name: 'developer',
          label: '开发人员',
          field: 'developer'
        },
        {
          name: 'master',
          label: '任务负责人',
          field: 'master'
        },
        {
          name: 'spdb_master',
          label: '行内项目负责人',
          field: 'spdb_master'
        },
        {
          name: 'tester',
          label: '测试人员',
          field: 'tester'
        },
        {
          name: 'group',
          label: '所属小组',
          field: row => row.group
        },
        {
          name: 'redmine_id',
          label: '研发单元编号',
          field: 'redmine_id'
        },
        {
          name: 'uat_TestObject',
          label: 'uat测试对象',
          field: 'uat_TestObject'
        },
        {
          name: 'desc',
          label: '描述',
          field: 'desc'
        },
        {
          name: 'operation',
          label: '操作',
          field: row => this.setOperation(row.stage),
          sortable: true,
          required: true
        }
      ],
      filteredTags: [],
      jobs: [],
      terms: [],
      filter: '',
      loading: false,
      eventNum: {
        todos: 0,
        done: 0
      },
      visibleColumns: this.visibleColumnOptions,
      taskSpecialStatus,
      delayStageOptions: [
        { label: '启动延期', value: 'develop' },
        { label: '内测延期', value: 'sit' },
        { label: '业测延期', value: 'uat' },
        { label: '准生产延期', value: 'rel' },
        { label: '投产延期', value: 'production' }
      ]
    };
  },
  filters: {
    filterTag(val) {
      val = val ? val : [];
      if (Object.values(val).some(item => item === '已提内测')) {
        return '已提内测';
      } else {
        return '';
      }
    },
    statusColorFilter(val) {
      return taskStageColorMap[val];
    }
  },
  watch: {
    terms(val) {
      this.filter = val.map(item => item.trim()).toString();
    },
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    visibleColumns(val) {
      setTableCol('centerList', val);
    },
    tab(val) {
      if (val === 'tasks') {
        this.setJobColumns();
      }
    },
    async delayStage(val) {
      this.loading = true;
      //调用加载数据方法
      await this.fetchUserTask();
      this.loading = false;
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('jobForm', {
      userTaskData: 'userTaskData'
    }),
    ...mapState('userActionSaveHomePage/mytaskPage', ['delayStage']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    },
    tabData() {
      return [
        {
          name: 'personal',
          label: '个人信息'
        },
        {
          name: 'tasks',
          label: '我的任务'
        },
        {
          name: 'todos',
          label: 'Todos'
        }
      ];
    }
  },
  methods: {
    formatSelectDisplay,
    ...mapActions('jobForm', {
      queryUserTask: 'queryUserTask',
      updateJobStage: 'updateJobStage'
    }),
    ...mapMutations('userActionSaveHomePage/mytaskPage', ['saveDelayStage']),
    changeTab(tab) {
      this.tab = tab;
    },
    getPath(val) {
      let path = '';
      switch (val) {
        case 'componentWeb':
          // 前端组件
          path = '/componentManage/web/weblist';
          break;
        case 'componentServer':
          // 后端组件
          path = '/componentManage/server/list';
          break;
        case 'archetypeWeb':
          // 前端骨架
          path = '/archetypeManage/web/webArchetype';
          break;
        case 'archetypeServer':
          // 后端骨架
          path = '/archetypeManage/server/archetype';
          break;
        case 'image':
          //镜像
          path = '/imageManage';
          break;
        default:
          //默认跳转应用
          path = '/app/list';
      }
      return path;
    },
    taskFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          return cols.some(col => {
            let value = '';
            if (Array.isArray(cellValue(col, row))) {
              value = cellValue(col, row).map(user => user.user_name_cn);
              return (
                value
                  .toString()
                  .toLowerCase()
                  .indexOf(term) > -1
              );
            } else if (
              Object.prototype.toString.call(cellValue(col, row)) ===
              '[object Object]'
            ) {
              return cellValue(col, row).user_name_cn.indexOf(term) > -1;
            } else {
              return (
                (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1
              );
            }
          });
        });
      });
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 点击搜索按钮,进行模糊搜索
    setSelect() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    // 控制归档按钮是否显示
    // 判断stage是否为production
    // 循环判断当前用户是否为该条任务的创建者 或者负责人或者行内负责人，若两者都满足，显示归档按钮
    isProduction(stage, creator, master, spdb_master) {
      let self = this;
      let isMaster = false;
      let isCreator = false;
      let isSpdbMaster = false;
      if (self.user.id === creator && creator.id) {
        isCreator = true;
      } else {
        isMaster = master.some((item, index) => {
          return self.user.id === master[index].id;
        });
        if (!isMaster) {
          isSpdbMaster = spdb_master.some((item, index) => {
            return self.user.id === spdb_master[index].id;
          });
        }
      }
      return (
        stage === 'production' &&
        (isCreator || isMaster || isSpdbMaster || this.isKaDianManager)
      );
    },
    async handleHint(id, index, name) {
      return this.$q
        .dialog({
          title: `确认归档`,
          message: `任务归档后，<span class="text-negative">"${name}"</span>的操作均无法执行且无法重新打开！归档后的任务只允许查看！请确认是否进行任务归档？`,
          ok: '确认',
          cancel: '取消',
          html: true
        })
        .onOk(async () => {
          await this.keepInFile(id, index);
        });
    },
    async keepInFile(id, index) {
      let next = this.stages[6];
      await this.updateJobStage({
        id: id,
        stage: next.value
      });
      successNotify('任务已归档');
      await this.fetchUserTask();
    },
    showBtn(row) {
      row.reviewer = row.reviewer ? row.reviewer : {};
      let otherRole = getIdsFormList([
        row.spdb_master,
        row.master,
        row.developer,
        row.reviewer
      ]);
      return (
        (row.stage === 'sit' ||
          row.stage === 'uat' ||
          row.stage === 'rel' ||
          row.stage === 'develop') &&
        (otherRole.indexOf(this.user.id) > -1 ||
          (this.user.role.some(item => item.name === 'UI团队负责人') &&
            row.review_status &&
            row.review_status !== 'irrelevant') ||
          this.isKaDianManager)
      );
    },
    async fetchUserTask() {
      this.jobs = [];
      let params = {
        id: this.user.id,
        incloudFile: this.incloudFile == '否' ? false : true,
        delayStage:
          this.delayStage && this.delayStage.length
            ? this.delayStage.map(val => val.value)
            : []
      };
      await this.queryUserTask(params);
      this.jobs = this.userTaskData.data;
    },
    stageFilter(val, type) {
      return type !== 2 ? taskStage[val] : taskStage1[val];
    },
    statusFilter(val) {
      return this.taskSpecialStatus[val];
    },
    sort(a, b) {
      let order = [
        '录入信息完成',
        '录入应用完成',
        '录入分支完成',
        '进行中',
        '开发中',
        'SIT测试',
        'UAT测试',
        'REL测试',
        '已投产',
        '已完成',
        '已归档'
      ];
      return order.indexOf(a) - order.indexOf(b);
    },
    setOperation(stage) {
      let num = 0;
      if (stage === 'production') {
        num = 2;
      } else if (
        stage === 'sit' ||
        stage === 'uat' ||
        stage === 'rel' ||
        stage === 'develop'
      ) {
        num = 1;
      }
      return num;
    },
    setJobColumns() {
      this.visibleColumns = getTableCol('centerList');
      if (!this.visibleColumns || this.visibleColumns.length <= 2) {
        this.visibleColumns = this.visibleColumnOptions;
      }
    }
  },
  async created() {
    this.loading = true;
    await this.fetchUserTask();
    this.loading = false;
  },
  mounted() {
    this.setJobColumns();
  }
};
</script>
<style lang="stylus" scoped>

.tag-remove
  width 2em
  height 2em
.tag-remove >>> .q-btn__wrapper
  min-width 2em
  min-height 2em
.width-select
  min-width 150px
</style>
