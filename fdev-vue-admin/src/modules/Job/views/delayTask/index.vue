<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="delayTaskList"
        :columns="columns"
        :export-func="handleDownload"
        :visible-columns="visibleColumns"
        :onSelectCols="updateVisibleColumns"
        row-key="job.id"
        :pagination.sync="pagination"
        titleIcon="list_s_f"
        title="延期任务列表"
        :onSearch="queryDelayTask"
        @request="onTableRequest"
      >
        <!-- 操作按钮  -->
        <template v-slot:top-right>
          <fdev-btn
            label="全部导出"
            ficon="exit"
            dialog
            @click="exportLists"
            normal
          />
        </template>
        <template v-slot:top-bottom>
          <!-- 任务名称 -->
          <f-formitem
            class="col-4 q-pr-sm"
            bottom-page
            label="任务名称"
            label-style="width:120px"
          >
            <fdev-input
              placeholder="请输入任务名称"
              :value="delayTask.name"
              @input="updateTaskName($event)"
              type="text"
            />
          </f-formitem>
          <!-- 需求编号/需求名称 -->
          <f-formitem
            class="col-4 q-pr-sm"
            bottom-page
            label="需求编号/需求名称"
            label-style="width:120px"
          >
            <fdev-select
              use-input
              option-label="oa_contact_no"
              option-value="id"
              :value="delayTask.rqrmnt"
              @input="updatedelayTaskRqrmnt($event)"
              :options="filterRqrmnt"
              @filter="rqrmntInputFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.oa_contact_name">
                      {{ scope.opt.oa_contact_name }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.oa_contact_no">
                      {{ scope.opt.oa_contact_no }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <!-- 研发单元编号 -->
          <f-formitem
            class="col-4 q-pr-sm"
            bottom-page
            label="研发单元编号"
            label-style="width:120px"
          >
            <fdev-input
              placeholder="请输入研发单元编号"
              :value="delayTask.redmine_id"
              @input="updateDelayTaskRedmineId($event)"
              type="text"
            />
          </f-formitem>
          <!-- 延期阶段 -->
          <f-formitem
            class="col-4 q-pr-sm"
            bottom-page
            label="延期阶段"
            label-style="width:120px"
          >
            <fdev-select
              multiple
              map-options
              emit-value
              option-label="label"
              option-value="value"
              :options="delayStageLists"
              :value="delayTask.postCondition"
              @input="updateDelayTaskPostCondition($event)"
            >
            </fdev-select>
          </f-formitem>
          <!-- 所属小组 -->
          <f-formitem
            class="col-4 q-pr-sm"
            bottom-page
            label="所属小组"
            label-style="width:120px"
          >
            <fdev-select
              use-input
              @filter="filterGroups"
              option-label="name"
              option-value="id"
              :options="groupList"
              :value="delayTask.groupObj"
              @input="updateDelayTaskGroupObj($event)"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name">
                      {{ scope.opt.name }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.fullName">
                      {{ scope.opt.fullName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <!-- 参与人 -->
          <f-formitem
            class="col-4 q-pr-sm"
            bottom-page
            label="参与人"
            label-style="width:120px"
          >
            <fdev-select
              use-input
              option-label="user_name_cn"
              option-value="id"
              @filter="filterUser"
              :options="userList"
              :value="delayTask.memberObj"
              @input="updateDelayTaskMemberObj($event)"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.user_name_en">
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem label="显示历史阶段延期" class="col-4 q-pr-sm" bottom-page
            ><fdev-toggle
              :value="delayTask.history"
              @input="showOldDelayTask($event)"
              left-label
          /></f-formitem>
        </template>
        <template v-slot:top-bottom-opt>
          <div class="row no-wrap flex-center q-mt-xs">
            <div class="in-term-delay">
              已进入阶段但延期
            </div>
            <div class="out-term-delay q-ml-xl">
              未进入阶段延期
            </div>
          </div>
        </template>
        <template v-slot:body-cell-name="props">
          <fdev-td class="td-desc ellipsis">
            <router-link
              v-if="props.row.id"
              :to="`/job/list/${props.row.id}`"
              class="link"
              :title="props.value"
            >
              {{ props.value }}
            </router-link>
            <span v-else :title="props.value"> {{ props.value || '-' }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-group="props">
          <fdev-td class="ellipsis td-desc" :title="props.value">
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-rqrmnt="props">
          <fdev-td class="ellipsis td-desc" :title="props.value">
            <div
              @click="routeTo(props.row.rqrmnt_no)"
              class="link cursor-pointer text-primary"
              v-if="props.row.rqrmnt_no"
            >
              {{ props.value }}
            </div>
            <span v-else> {{ props.value || '-' }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-master="props">
          <fdev-td
            class="td-desc ellipsis"
            :title="showTitleNames(props.value)"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link :to="`/user/list/${item.id}`" class="link">
                {{ item.user_name_cn }}
              </router-link>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-spdb_master="props">
          <fdev-td
            class="td-desc ellipsis"
            :title="showTitleNames(props.value)"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link :to="`/user/list/${item.id}`" class="link">
                {{ item.user_name_cn }}
              </router-link>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-develop="props">
          <fdev-td
            class="td-desc ellipsis"
            :class="
              less('develop', props.row.stage) ? 'text-red-5' : 'text-orange-7'
            "
            :title="props.value"
          >
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-sit="props">
          <fdev-td
            class="td-desc ellipsis"
            :class="
              less('sit', props.row.stage) ? 'text-red-5' : 'text-orange-7'
            "
          >
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-uat="props">
          <fdev-td
            class="td-desc ellipsis"
            :class="
              less('uat', props.row.stage) ? 'text-red-5' : 'text-orange-7'
            "
          >
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-rel="props">
          <fdev-td
            class="td-desc ellipsis"
            :class="
              less('rel', props.row.stage) ? 'text-red-5' : 'text-orange-7'
            "
          >
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-production="props">
          <fdev-td
            class="td-desc ellipsis"
            :class="
              less('production', props.row.stage)
                ? 'text-red-5'
                : 'text-orange-7'
            "
          >
            {{ props.value }}
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  setDelayTaskPagination,
  getDelayTaskPagination
} from '../../utils/setting';
import { deepClone } from '@/utils/utils';
import { code, delayStageLists } from '@/modules/Job/utils/constants';

export default {
  name: 'DelayTask',
  components: { Loading },
  data() {
    return {
      pagination: {
        page: 1,
        rowsPerPage: getDelayTaskPagination().rowsPerPage || 5,
        rowsNumber: 10
      }, //增加分页
      delayStageLists: delayStageLists, //延期阶段选项
      delayTaskList: [], //延期任务列表
      cloneGroups: [],
      columns: [
        {
          name: 'name',
          label: '任务名称',
          field: 'name',
          align: 'left'
        },
        {
          name: 'rqrmnt',
          label: '需求编号',
          field: row => row.oa_contact_no,
          align: 'left'
        },
        {
          name: 'develop',
          label: '启动延期(天)',
          field: row => row.postpone.develop,
          align: 'left'
        },
        {
          name: 'sit',
          label: '内测延期(天)',
          field: row => row.postpone.sit,
          align: 'left'
        },
        {
          name: 'uat',
          label: '业测延期(天)',
          field: row => row.postpone.uat,
          align: 'left'
        },
        {
          name: 'rel',
          label: '准生产延期(天)',
          field: row => row.postpone.rel,
          align: 'left'
        },
        {
          name: 'production',
          label: '投产延期(天)',
          field: row => row.postpone.production,
          align: 'left'
        },
        {
          name: 'group',
          label: '所属小组',
          field: row => row.group.fullName,
          align: 'left'
        },
        {
          name: 'master',
          label: '任务负责人',
          field: 'master',
          align: 'left'
        },
        {
          name: 'spdb_master',
          label: '行内负责人',
          field: 'spdb_master',
          align: 'left'
        }
      ],
      groupFilter: [],
      groupList: [],
      filterRqrmnt: [],
      userList: [],
      rqrmntIdArr: [],
      loading: false //页面加载
    };
  },
  watch: {
    pagination(val) {
      setDelayTaskPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    // 需求名称/编号
    'delayTask.rqrmnt'(val) {
      this.queryDelayTask();
    },
    // 所属小组
    'delayTask.groupObj'(val) {
      this.queryDelayTask();
    },
    // 参与人
    'delayTask.memberObj'(val) {
      this.queryDelayTask();
    },
    // 延期阶段
    'delayTask.postCondition'(val) {
      this.queryDelayTask();
    }
  },
  computed: {
    ...mapState('userActionSaveJob/delayTask', {
      delayTask: 'createDelayTaskModel',
      visibleColumns: 'visibleColumns',
      range: 'range'
    }),
    ...mapState('userForm', ['groups']),
    ...mapState('jobForm', ['rqrmntsList', 'postponeTaskList']),
    // ...mapGetters('user', ['isLoginUserList']),
    ...mapState('measureForm', {
      usersList: 'userList'
    })
  },
  methods: {
    ...mapMutations('userActionSaveJob/delayTask', [
      'updatedelayTaskRqrmnt',
      'updateDelayTaskGroupObj',
      'updateDelayTaskMemberObj',
      'updateDelayTaskPostCondition',
      'updateDelayTaskRedmineId',
      'updateDelayTaskHistory',
      'updateVisibleColumns',
      'updateTaskName'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('jobForm', ['queryRqrmnts', 'queryPostponeTask']),
    // ...mapActions('user', ['fetch']),
    ...mapActions('measureForm', ['queryUserCoreData']),
    //新增分页功能
    async onTableRequest({ pagination }) {
      const { page, rowsNumber, rowsPerPage } = pagination;
      this.pagination = {
        page,
        rowsPerPage,
        rowsNumber
      };
      await this.queryDelayTask();
    },
    // 查询延期任务
    async queryDelayTask() {
      this.loading = true;
      //增加分页条件
      let params = {
        group: this.delayTask.groupObj ? this.delayTask.groupObj.id : '', //所属小组
        member: this.delayTask.memberObj ? this.delayTask.memberObj.id : '', //参与人
        rqrmnt_no: this.delayTask.rqrmnt ? this.delayTask.rqrmnt.id : '', //需求编号
        redmine_id: this.delayTask.redmine_id, //研发单元编号
        history: this.delayTask.history, //是否显示历史延期阶段
        postCondition: this.delayTask.postCondition //延期范围
          ? this.delayTask.postCondition
          : '0',
        name: this.delayTask.name, //任务名称
        pageSize: this.pagination.rowsPerPage,
        pageNum: this.pagination.page
      };
      try {
        await this.queryPostponeTask(params);
        this.delayTaskList = this.postponeTaskList.taskList;
        //定义总条数
        this.pagination.rowsNumber = this.postponeTaskList.total;
        this.loading = false;
      } catch (e) {
        this.loading = false;
      }
    },
    rqrmntInputFilter(val, update) {
      update(() => {
        this.filterRqrmnt = this.rqrmntIdArr.filter(tag => {
          return (
            tag.oa_contact_name.toLowerCase().includes(val.toLowerCase()) ||
            tag.oa_contact_no.toLowerCase().includes(val.toLowerCase())
          );
        });
      });
    },
    filterGroups(val, update) {
      update(() => {
        this.groupList = this.cloneGroups.filter(v => {
          return (
            v.fullName.toLowerCase().includes(val.toLowerCase()) ||
            v.name.toLowerCase().includes(val.toLowerCase())
          );
        });
      });
    },
    filterUser(val, update) {
      update(() => {
        this.userList = this.usersList.filter(v => {
          return (
            v.user_name_cn.includes(val.toLowerCase()) ||
            v.user_name_en.toLowerCase().includes(val.toLowerCase())
          );
        });
      });
    },
    // 显示历史延期阶段
    async showOldDelayTask(val) {
      this.updateDelayTaskHistory(val);
      await this.queryDelayTask();
    },
    less(value, stage) {
      let num = parseInt(code[stage]) < 3 ? 3 : parseInt(code[stage]);
      return num + 1 <= parseInt(code[value]);
    },
    // 跳转需求详情
    routeTo(id) {
      this.$router.push({ name: 'rqrProfile', params: { id: id } });
    },
    // 负责人处理
    showTitleNames(names) {
      let sName = '';
      if (names.length > 0) {
        for (let i = 0; i < names.length; i++) {
          sName += `${names[i].user_name_cn} `;
        }
        sName = sName.substring(0, sName.length - 1);
      }
      return sName;
    },
    formatJson(taskList, filterVal) {
      return taskList.map(row => {
        return filterVal.map(col => {
          if (col === 'group') {
            return row[col].name;
          } else if (col === 'rqrmnt') {
            return row.oa_contact_no;
          } else if (col === 'develop') {
            return row.postpone.develop;
          } else if (col === 'sit') {
            return row.postpone.sit;
          } else if (col === 'uat') {
            return row.postpone.uat;
          } else if (col === 'rel') {
            return row.postpone.rel;
          } else if (col === 'production') {
            return row.postpone.production;
          } else if (col === 'master' || col === 'spdb_master') {
            let nameList = row[col].map(item => item.user_name_cn);
            return nameList.join(' ');
          } else {
            return row[col];
          }
        });
      });
    },
    //全部导出
    async exportLists() {
      this.loading = true;
      //增加分页条件
      let params = {
        group: this.delayTask.groupObj ? this.delayTask.groupObj.id : '', //所属小组
        member: this.delayTask.memberObj ? this.delayTask.memberObj.id : '', //参与人
        rqrmnt_no: this.delayTask.rqrmnt ? this.delayTask.rqrmnt.id : '', //需求编号
        redmine_id: this.delayTask.redmine_id, //研发单元编号
        history: this.delayTask.history, //是否显示历史延期阶段
        postCondition: this.delayTask.postCondition //延期范围
          ? this.delayTask.postCondition
          : '0',
        name: this.delayTask.name //任务名称
      };
      try {
        await this.queryPostponeTask(params);
        const taskList = this.postponeTaskList.taskList;
        this.downloadLists(taskList);
        this.loading = false;
      } catch (e) {
        this.loading = false;
      }
    },
    handleDownload() {
      this.downloadLists(this.delayTaskList);
    },
    // 导出方法
    downloadLists(taskList) {
      let _this = this;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '任务名称',
          '需求编号',
          '启动延期(天)',
          '内测延期(天)',
          '业测延期(天)',
          '准生产延期(天)',
          '投产延期(天)',
          '所属小组',
          '任务负责人',
          '行内负责人'
        ];
        const filterVal = [
          'name',
          'rqrmnt',
          'develop',
          'sit',
          'uat',
          'rel',
          'production',
          'group',
          'master',
          'spdb_master'
        ];
        const data = _this.formatJson(taskList, filterVal);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          autoWidth: false,
          filename: '延期任务列表',
          bookType: 'xlsx'
        });
      });
    }
  },
  async created() {
    this.loading = true;
    try {
      //查询延期任务列表
      await this.queryDelayTask();
      // 查询条件接口
      Promise.all([
        // 需求列表
        this.queryRqrmnts(),
        // 所属小组
        this.fetchGroup(),
        //参与人列表
        this.queryUserCoreData({ status: '0' })
      ]).then(() => {
        this.rqrmntIdArr = this.rqrmntsList;
        this.cloneGroups = deepClone(this.groups);
        this.groupList = this.cloneGroups;
        this.userList = this.usersList;
        this.loading = false;
      });
    } catch (e) {
      this.loading = false;
    }
  }
};
</script>

<style lang="stylus" scoped>

.in-term-delay
  position relative
  &:before
    position absolute
    content ''
    left -15px
    top 6px
    width 10px
    height 10px
    border-radius 5px
    background #FD8D00
.out-term-delay
  position relative
  &:before
    position absolute
    content ''
    left -15px
    top 6px
    width 10px
    height 10px
    border-radius 5px
    background #f44336
.flex-center{
  display: flex;
  align-items: center;
}
</style>
