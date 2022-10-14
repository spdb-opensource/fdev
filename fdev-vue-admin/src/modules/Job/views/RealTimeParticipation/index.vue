<template>
  <f-block page>
    <Loading :visible="loading['jobForm/taskCardDisplay']">
      <UserSelected @queryClick="init" :toggle="false" />
      <div class="bg-white q-mt-md q-pt-md">
        <div class="row items-center q-mr-md justify-between">
          <div class="rowleft">
            <fdev-avatar class="q-mr-xs" square size="24px" color="red" />高
            <fdev-avatar
              class="q-ml-md q-mr-xs"
              square
              size="24px"
              color="orange"
            />中
            <fdev-avatar
              class="q-ml-md q-mr-xs"
              square
              size="24px"
              color="green"
            />一般
            <fdev-avatar
              class="q-ml-md q-mr-xs"
              square
              border
              size="24px"
              color="grey"
            />低
            <span class="user-sum q-ml-md"
              >共{{ allTaskListNum }} 个任务、{{ allRrqListNum }} 个需求</span
            >
          </div>
          <div class="rowright">
            <fdev-select
              flat
              v-model="visibleColumns"
              multiple
              options-dense
              display-value="选择列"
              emit-value
              map-options
              :options="stages"
              option-value="value"
              options-cover
            />
            <fdev-btn
              class="q-ml-md"
              ficon="download"
              normal
              label="下载"
              :disable="dowmloadLoading"
              @click="handleDownload('xlsx')"
            >
            </fdev-btn>
          </div>
        </div>
        <div class="row items-center">
          <fdev-select
            :value="delayStage"
            :options="delayStageLists"
            borderless
            options-dense
            class="table-head-input q-mr-sm"
            @input="filter($event, 'delayStage')"
            map-options
            emit-value
            option-label="label"
            option-value="value"
          />
          <fdev-select
            :value="rqrmntType"
            :options="typeList"
            borderless
            options-dense
            map-options
            emit-value
            class="table-head-input q-mr-sm"
            option-value="value"
            option-label="label"
            @input="filter($event, 'rqrmntType')"
          />
        </div>
        <div class="row q-col-gutter-xs position  q-pt-md">
          <div class="col th" v-for="th in visibleColumns" :key="th.value">
            {{ taskStagesSum(th) }} {{ th | stagesFilters }}
          </div>
        </div>

        <fdev-separator v-show="filterData.length === 0" />

        <div
          v-show="filterData.length > 0"
          v-for="item in filterData"
          :key="item.name_en"
        >
          <div>
            <fdev-separator />
            <div class="list">
              <fdev-btn
                size="sm"
                round
                flat
                @click="item.expand = !item.expand"
                :icon="item.expand ? 'expand_more' : 'keyboard_arrow_right'"
              />
              <span>{{ item.name_cn }}</span>
              <span class="user-sum">{{ taskSum(item) }} 个任务</span>
            </div>
          </div>

          <div v-show="item.expand">
            <fdev-separator />

            <div class="list-wrapper row q-col-gutter-xs">
              <div
                class="col card-wrapper"
                v-for="list in visibleColumns"
                :key="list"
              >
                <fdev-card
                  class="card"
                  v-for="(card, i) in item[list]"
                  :key="i"
                >
                  <fdev-card-section
                    :class="
                      card.rqrmntInfo
                        ? colorClass[card.rqrmntInfo.priority]
                        : ''
                    "
                    class="ellipsis q-px-sm card-header"
                  >
                    <router-link
                      v-if="card.rqrmntInfo && card.rqrmntInfo.id"
                      class="link td-desc ellipsis"
                      :to="
                        `/rqrmn/rqrProfile/${
                          card.rqrmntInfo ? card.rqrmntInfo.id : ''
                        }`
                      "
                      :title="card.rqrmntInfo.oa_contact_name"
                    >
                      {{
                        card.rqrmntInfo ? card.rqrmntInfo.oa_contact_name : ''
                      }}
                    </router-link>
                    <div v-else :title="card.rqrmntInfo.oa_contact_name">
                      <span>{{ card.rqrmntInfo.oa_contact_name || '-' }}</span>
                    </div>
                  </fdev-card-section>
                  <fdev-separator />
                  <fdev-card-section class="card-body q-px-sm">
                    <router-link
                      v-if="card.taskInfo.id"
                      :to="`/job/list/${card.taskInfo.id}`"
                      class="link ellipsis"
                      :title="card.taskInfo.name"
                    >
                      {{ card.taskInfo.name }}
                    </router-link>
                    <div v-else :title="card.taskInfo.name">
                      <span>{{ card.taskInfo.name || '-' }}</span>
                    </div>
                    <div class="time">
                      应用:
                      <router-link
                        v-if="card.taskInfo.project_id"
                        :to="`/app/list/${card.taskInfo.project_id}`"
                        class="link inlintItem"
                        :title="card.taskInfo.project_name"
                      >
                        {{ card.taskInfo.project_name }}
                      </router-link>
                      <div v-else :title="card.taskInfo.project_name">
                        <span>{{ card.taskInfo.project_name || '-' }}</span>
                      </div>
                    </div>
                    <div v-if="card.postpone">
                      <fdev-chip
                        class="q-ma-none q-mt-sm q-mr-xs"
                        outline
                        size="xs"
                        square
                        color="red"
                        text-color="white"
                        v-for="delayStage in card.postpone"
                        :key="delayStage"
                      >
                        {{ delayStage | delayStageFilters }}
                      </fdev-chip>
                    </div>

                    <div class="time">
                      计划启动: {{ card.taskInfo.plan_start_time }}
                    </div>
                    <div class="time">
                      计划 SIT: {{ card.taskInfo.plan_inner_test_time }}
                    </div>
                    <div class="time">
                      计划UAT: {{ card.taskInfo.plan_uat_test_start_time }}
                    </div>
                    <div class="time">
                      投产意向窗口: {{ card.taskInfo.proWantWindow }}
                    </div>
                  </fdev-card-section>
                </fdev-card>
              </div>
            </div>
          </div>
        </div>

        <div v-show="filterData.length === 0" class="q-pa-md">
          <fdev-icon class="icon-size" name="warning" />没有可用数据
        </div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import UserSelected from '@/modules/Dashboard/components/UserSelected';
import { jobStages, delayStageLists } from '@/modules/Job/utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';
export default {
  name: '',
  components: {
    Loading,
    UserSelected
  },
  data() {
    return {
      visibleColumns: [
        'todoList',
        'devList',
        'sitList',
        'uatList',
        'relList',
        'proList'
      ],
      stages: jobStages,
      data: [],
      filterData: [],
      delayStageLists: [
        { label: '延期选项', value: '延期选项' },
        ...delayStageLists
      ],
      searchObj: {},
      dowmloadLoading: false,
      typeList: [
        { value: 'init', label: '需求类型' },
        { value: 'tech', label: '科技内部需求' },
        { value: 'business', label: '业务需求' }
      ]
    };
  },
  computed: {
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('jobForm', ['realTimeTask']),
    ...mapState('userActionSaveJob/realTimeParticipation', [
      'delayStage',
      'rqrmntType'
    ]),
    colorClass() {
      return {
        2: 'generic',
        0: 'high',
        1: 'middle',
        3: 'low',
        '': 'low'
      };
    },
    taskNum() {
      return this.filterData.reduce((total, current) => {
        return (total = total + current.devList.length);
      }, 0);
    },
    allTaskListNum() {
      let list = this.filterData.map(dataItem => {
        return this.visibleColumns.map(list => {
          return dataItem[list].map(devInfo => {
            return devInfo;
          });
        });
      });
      if (Array.isArray(list)) {
        return this.flatten(list).length;
      } else {
        return 0;
      }
    },
    allRrqListNum() {
      if (Array.isArray(this.filterData) && this.filterData[0]) {
        return this.filterData[0].rqrmnNum || 0;
      }
      return 0;
    }
  },
  filters: {
    stagesFilters(val) {
      const item = jobStages.find(item => item.value === val);
      return item && item.label;
    },
    delayStageFilters(val) {
      return delayStageLists.find(item => item.value === val).label;
    }
  },
  methods: {
    ...mapActions('jobForm', ['taskCardDisplay']),
    ...mapMutations('userActionSaveJob/realTimeParticipation', [
      'updateDelayStage',
      'updateRqrmntType'
    ]),
    switchShow(data) {
      this.showSwitch = data;
    },
    taskSum(data) {
      const { visibleColumns } = this;

      return visibleColumns.reduce((sum, item) => {
        return (sum += data[item].length);
      }, 0);
    },
    taskStagesSum(key) {
      return this.filterData.reduce((sum, item) => {
        return (sum += item[key].length);
      }, 0);
    },
    filter(event, type) {
      if (type === 'delayStage') {
        this.updateDelayStage(event);
      } else if (type === 'rqrmntType') {
        this.updateRqrmntType(event);
      }
      const { rqrmntType, data, visibleColumns, delayStage } = this;
      this.filterData = data.map(item => {
        const taskObj = {};
        visibleColumns.forEach(key => {
          if (!taskObj[key]) {
            taskObj[key] = [];
          }

          taskObj[key] = item[key].filter(task => {
            let isRqrmntType = true;
            let isDelayStage = true;
            if (
              rqrmntType !== 'init' &&
              task.rqrmntInfo &&
              task.rqrmntInfo.demand_type !== rqrmntType
            ) {
              isRqrmntType = false;
            }
            if (
              delayStage !== '延期选项' &&
              (!task.postpone || !task.postpone.includes(delayStage))
            ) {
              isDelayStage = false;
            }
            return isRqrmntType && isDelayStage;
          });
        });
        return { ...item, ...taskObj };
      });
    },
    formatJson(val) {
      return this.filterData.map(dataItem => {
        return this.visibleColumns.map(list => {
          return dataItem[list].map(devInfo => {
            return val.map(col => {
              if (col === 'name') {
                return devInfo.taskInfo[col];
              } else if (col === 'name_cn') {
                return dataItem[col];
              } else if (col === 'oa_contact_name') {
                return devInfo.rqrmntInfo && devInfo.rqrmntInfo[col];
              } else if (col === 'oa_contact_no') {
                return devInfo.rqrmntInfo && devInfo.rqrmntInfo[col];
              } else {
                return devInfo.taskInfo[col];
              }
            });
          });
        });
      });
    },
    handleDownload() {
      this.dowmloadLoading = true;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '用户名',
          '任务名称',
          '需求名称',
          '需求编号',
          '计划启动',
          '计划SIT',
          '计划UAT',
          '投产意向窗口',
          '应用'
        ];
        const filterVal = [
          'name_cn',
          'name',
          'oa_contact_name',
          'oa_contact_no',
          'plan_start_time',
          'plan_inner_test_time',
          'plan_uat_test_start_time',
          'proWantWindow',
          'project_name'
        ];
        const data = this.formatJson(filterVal);
        excel.export_json_to_excel({
          header: tHeader,
          data: this.flattenTwo(data),
          filename: '实时任务列表',
          bookType: 'xlsx'
        });
      });
      this.dowmloadLoading = false;
    },
    async init(users, roles, selectedCompanyList, selectedList) {
      const param = {
        ids: users,
        roles: roles,
        companyList: selectedCompanyList,
        groupList: selectedList
      };
      this.searchObj = param;
      await this.taskCardDisplay({
        ids: users.map(user => user.id),
        roles
      });
      this.data = this.realTimeTask.map(item => {
        return { ...item, expand: false };
      });
      this.filter();
    },
    flatten(arr) {
      let res = arr.reduce((res, item) => {
        return res.concat(Array.isArray(item) ? this.flatten(item) : item);
      }, []);
      return res;
    },
    flattenTwo(arr) {
      let result = arr.reduce((res, item) => {
        if (
          Array.isArray(item) &&
          item.every(val => {
            return Array.isArray(val);
          })
        ) {
          res = res.concat(this.flattenTwo(item));
        } else if (
          Array.isArray(item) &&
          !item.every(val => {
            return Array.isArray(val);
          })
        ) {
          res.push(item);
        }
        return res;
      }, []);
      return result;
    }
  },
  mounted() {
    let item = JSON.parse(sessionStorage.getItem('reamtimeObj'));
    if (item && item.ids && item.ids.length > 0) {
      this.init(item.ids, item.roles, item.companyList, item.groupList);
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('reamtimeObj');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('reamtimeObj', JSON.stringify(this.searchObj));
    next();
  }
};
</script>

<style lang="stylus" scoped>

.list
  line-height: 50px;
  height: 50px;
  padding 0 8px
  .user-sum
    color #757575
    margin-left 16px
.select
  width 210px
.th
  text-align center
  padding 7px 16px
  font-size 12px
  font-weight 700
.card-wrapper
  border-left 1px solid rgba(0,0,0,0.12)
  padding 5px 3px
  .card
    font-size 12px
    margin-top: 5px;
    &:last-child
      margin-bottom: 5px;
    .card-header
      font-weight 700
      cursor: pointer;
      &.high
        background #f44336;
        color white
      &.middle
        background #ff9800;
        color white
      &.generic
        background #4caf50;
        color white
      &.low
        background #9e9e9e
        color white
    .card-header, .time, a
      display block
      width 100%
    .card-body .time
      margin-top 8px
      word-break: break-all;
      &:first-child
        margin 0
.position
  position: sticky;
  top: 50px;
  border-bottom: 1px solid rgba(0,0,0,0.12);
  z-index: 999;
  background: white;
.inlintItem
  display: inline !important
.rowleft{
  display:flex;
  align-items: center;
}
.rowright{
  display:flex;
  align-items: center;
}
</style>
