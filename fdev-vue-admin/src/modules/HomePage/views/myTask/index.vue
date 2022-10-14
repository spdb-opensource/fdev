<template>
  <Loading :visible="loading">
    <f-block>
      <fdev-table
        class="my-sticky-column-table"
        :data="jobs"
        title="我的任务列表"
        titleIcon="todo_list_s_f"
        :columns="columns"
        row-key="job.id"
        :filter="terms"
        :filter-method="taskFilter"
        :pagination.sync="pagination"
        :visible-columns="visibleCols"
        :on-select-cols="saveVisibleColumns"
        no-export
      >
        <template v-slot:top-bottom>
          <f-formitem label="搜索条件" label-style="width:auto">
            <fdev-select
              use-input
              placeholder="输入关键字，回车区分"
              multiple
              hide-dropdown-icon
              :value="searchValue"
              @input="saveTerms($event)"
              @new-value="addTerm"
              class="table-head-input"
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
        <template v-slot:top-right>
          <fdev-btn label="批量归档" normal ficon="add" @click="batchFileFun" />
        </template>
        <template v-slot:body-cell-name="props">
          <fdev-td class="td-desc" :title="props.value">
            <f-icon
              v-if="props.row.delayFlag == true"
              name="alert_t_f"
              style="color:red"
              title="延期告警！"
            />
            <router-link :to="`/job/list/${props.row.id}`" class="link">
              {{ props.value }}
            </router-link>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <!-- 所属应用 -->
        <template v-slot:body-cell-project_name="props">
          <fdev-td class="td-desc">
            <router-link
              v-if="props.row.project_id"
              :title="props.row.project_name"
              :to="
                `${getPath(props.row.applicationType)}/${props.row.project_id}`
              "
              class="link"
            >
              {{ props.row.project_name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.project_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else title="-">-</span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-test_tag="props">
          <fdev-td class="td-desc" :title="props.value | filterTag">
            <span
              v-if="Array.isArray(props.row.tag) && props.row.tag.length > 0"
              class="text-ellipsis"
              :title="props.row.tag | filterTag"
            >
              {{ props.row.tag | filterTag }}
            </span>
            <span v-else title="-">-</span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-creator="props">
          <fdev-td class="td-desc">
            <router-link
              :to="`/user/list/${props.value.id}`"
              :title="props.value.user_name_cn"
              class="link"
            >
              {{ props.value.user_name_cn }}
            </router-link>
          </fdev-td>
        </template>

        <!--开发人员-->
        <template v-slot:body-cell-developer="props">
          <fdev-td>
            <div
              v-if="
                Array.isArray(props.row.developer) &&
                  props.row.developer.length > 0
              "
              :title="props.row.developer.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="(item, index) in props.value" :key="index">
                <router-link
                  v-show="item.user_name_cn"
                  :to="`/user/list/${item.id}`"
                  class="link"
                >
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-master="props">
          <fdev-td>
            <div
              v-if="
                Array.isArray(props.row.master) && props.row.master.length > 0
              "
              :title="props.row.master.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="(item, index) in props.value" :key="index">
                <router-link
                  v-show="item.user_name_cn"
                  :to="`/user/list/${item.id}`"
                  class="link"
                >
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-spdb_master="props">
          <fdev-td>
            <div
              v-if="
                Array.isArray(props.row.spdb_master) &&
                  props.row.spdb_master.length > 0
              "
              :title="props.row.spdb_master.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="(item, index) in props.value" :key="index">
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-tester="props">
          <fdev-td>
            <div
              v-if="
                Array.isArray(props.row.tester) && props.row.tester.length > 0
              "
              :title="props.row.tester.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="(item, index) in props.value" :key="index">
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-stage="props">
          <fdev-td
            class="td-desc"
            :title="taskFilters(props.row.stage, props.row.taskType)"
          >
            <f-status-color
              :gradient="props.row.stage | statusFilter"
            ></f-status-color>
            {{ taskFilters(props.row.stage, props.row.taskType) }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-operation="props">
          <fdev-td>
            <div class="q-gutter-x-sm row no-wrap">
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
                >
                  <span>处于暂缓状态</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="处理"
                  :disable="
                    props.row.taskSpectialStatus == 1 ||
                      props.row.taskSpectialStatus == 2
                  "
                  @click="handleBtn(props.row)"
                />
              </div>
              <fdev-btn
                flat
                label="归档"
                v-if="
                  isProduction(
                    props.row.stage,
                    props.row.creator,
                    props.row.master,
                    props.row.spdb_master
                  )
                "
                @click="
                  handleHint(props.row.id, props.row.__index, props.row.name)
                "
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </f-block>
    <BehindMasterDialog
      :value.sync="handleShow"
      @changeHandleShow="changeDialogShow"
      :commitTabledata="commitTabledata"
    />
    <!-- 批量归档 -->
    <batch-file
      v-if="ifOpenBatchFileDialog"
      @exposureOperationFun="exposureOperationFun"
    />
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import {
  formatSelectDisplay,
  successNotify,
  getIdsFormList,
  taskStage,
  taskStage1
} from '@/utils/utils';
import {
  createJobModel,
  perform,
  taskStageColorMap
} from '@/modules/Job/utils/constants';
import { taskColumns } from '../../utils/constants';
import BehindMasterDialog from '@/modules/Job/components/BehindMasterDialog';
import batchFile from './components/batchFile';

export default {
  name: 'myTask',
  components: { Loading, BehindMasterDialog, batchFile },
  data() {
    return {
      incloudFile: '否',
      incloudFileOption: ['否', '是'],
      ...perform,
      job: createJobModel,
      handleShow: false,
      rowIdToPush: '',
      commitTabledata: [],
      master: '',
      spdb_master: '',
      columns: taskColumns,
      jobs: [],
      filter: '',
      loading: false,
      pagination: {
        rowsPerPage: 5
      },
      ifOpenBatchFileDialog: false, // 是否开启批量归档弹窗 控制阀
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
    statusFilter(val) {
      return taskStageColorMap[val];
    },
    filterTag(val) {
      val = val == '-' ? [] : val;
      let tags = ['已提内测', '提测打回', '内测完成'];
      let tagArry = val.filter(item => tags.indexOf(item) > -1).toString();
      return tagArry ? tagArry : '-';
    }
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    },
    async delayStage(val) {
      this.loading = true;
      //调用加载数据方法
      await this.fetchUserTask();
      this.loading = false;
    }
  },
  computed: {
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapState('jobForm', {
      userTaskData: 'userTaskData'
    }),
    ...mapState('jobForm', ['commitTips']),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('userActionSaveHomePage/mytaskPage', [
      'visibleCols',
      'currentPage',
      'searchValue',
      'delayStage'
    ]),
    ...mapGetters('userActionSaveHomePage/mytaskPage', ['terms']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    }
  },
  methods: {
    ...mapActions('jobForm', ['updateJobStage', 'queryCommitTips']),
    ...mapActions('jobForm', {
      queryUserTask: 'queryUserTask'
    }),
    ...mapMutations('userActionSaveHomePage/mytaskPage', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveTerms',
      'saveDelayStage'
    ]),
    formatSelectDisplay,
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
    // 点击commit落后数dialog确定按钮后
    changeDialogShow(val) {
      this.handleShow = val;
      this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
    },
    // 点击处理按钮先进行dialog提示冲突数
    async handleBtn(row) {
      this.rowIdToPush = row.id;
      if (row.stage == 'sit' || row.stage == 'develop') {
        if (row.project_name && row.feature_branch) {
          await this.queryCommitTips({
            taskId: row.id
          });
          // 打开commit提交情况dialog
          if (this.commitTips.commits != 0) {
            this.commitTabledata = this.commitTips.fileList.map(item => {
              return { fileName: item };
            });
            this.handleShow = true;
          } else {
            this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
          }
        }
      } else {
        this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
      }
      if (row.taskType == 1) {
        this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
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
    taskFilters(val, type) {
      return type !== 2 ? taskStage[val] : taskStage1[val];
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
    //判断处理按钮是否显示
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
    //加载jobs数据
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
    // 批量归档
    batchFileFun() {
      this.ifOpenBatchFileDialog = true;
    },
    // 批量归档弹窗暴露的方法 ----- 方便做一些外部操作
    async exposureOperationFun(val) {
      this.ifOpenBatchFileDialog = false;
      if (val) {
        this.loading = true;
        await this.fetchUserTask();
        this.loading = false;
      }
    }
  },
  async created() {
    this.loading = true;
    //调用加载数据方法
    await this.fetchUserTask();
    this.loading = false;
  },
  mounted() {
    // 搜索回显（看完展示页面 跳回搜索页面根据缓存的搜索条件过滤表格里面的数据）
    this.setSelect();
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns(this.visibleColumnOptions);
    }
  }
};
</script>
<style lang="stylus" scoped>

.archive-btn
  margin-left: 5px;
.width-select
  min-width 150px
.td-desc
  max-width 350px
  overflow hidden
  text-overflow ellipsis
</style>
