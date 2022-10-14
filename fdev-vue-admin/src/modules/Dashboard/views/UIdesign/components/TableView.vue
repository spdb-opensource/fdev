<template>
  <div class="content" :style="addScrollFun(listStyle)">
    <div
      v-for="(rowDex, index) in reviewList"
      :class="index == 0 ? '' : 'mrt20'"
      :key="index"
    >
      <table class="my-table" :style="getWidthFun(col)">
        <thead :class="index == 0 ? '' : 'hiddenHeader'">
          <tr>
            <th class="static-th-col1" colspan="2">项目小组</th>
            <th
              class="static-th-col31"
              :class="ifAddShadowFun(listStyle)"
              v-if="tabType == 'demand'"
            >
              需求名称
            </th>
            <th class="static-th-col2" v-if="tabType == 'task'">
              需求编号
            </th>
            <th class="static-th-col3" v-if="tabType == 'task'">
              任务名称
            </th>
            <th
              class="static-th-col4"
              :class="ifAddShadowFun(listStyle)"
              v-if="tabType == 'task'"
            >
              卡点状态
            </th>
            <template>
              <th class="text-left" v-for="item in col" :key="item.val">
                {{ item.label }}
              </th>
            </template>
          </tr>
        </thead>
        <tbody>
          <tr style="height:1px">
            <td
              class="groupName static-col1"
              :rowspan="
                rowDex.unfinishedList.length == 0
                  ? rowDex.finishedList.length +
                    rowDex.unfinishedList.length +
                    2
                  : rowDex.finishedList.length +
                    rowDex.unfinishedList.length +
                    1
              "
            >
              <div class="text-center">
                <span v-html="isMatch(rowDex.groupName)"></span>
                <br />
                <br />
                <span>总数</span>
                <br />
                <span>
                  {{
                    rowDex.finishedList.length + rowDex.unfinishedList.length
                  }}
                </span>
              </div>
            </td>
            <td
              class="pass static-col2"
              :rowspan="rowDex.finishedList.length + 1"
            >
              <div class="text-center">
                <span>通过</span>
                <br />
                <span>
                  {{ rowDex.finishedList.length }}
                </span>
              </div>
            </td>
            <td
              v-show="!rowDex.finishedList.length"
              class="border-b"
              :class="[
                tabType === 'task'
                  ? `static-col${i + 2}`
                  : `static-col${i + 30}`,
                tabType === 'demand'
                  ? i + 30 === 31
                    ? ifAddShadowFun(listStyle)
                    : ''
                  : i + 2 === 5
                  ? ifAddShadowFun(listStyle)
                  : ''
              ]"
              v-for="i in falseTd"
              :key="i + 1"
            >
              -
            </td>
          </tr>
          <tr v-for="item in rowDex.finishedList" :key="item.id">
            <!-- 需求名称 -->
            <td
              class="oa-contact-name static-col-name"
              :class="ifAddShadowFun(listStyle)"
              v-if="tabType == 'demand'"
            >
              <div
                :title="titleFun(item.oa_contact_name)"
                @click="toRqrReview(item.id)"
                class="fixedW ellipisis-t set-link"
              >
                {{ item.oa_contact_name || '-' }}
              </div>
            </td>
            <!-- 需求编号 -->
            <td class="demand-no static-col3" v-if="tabType == 'task'">
              <div
                :title="titleFun(item.demandNo)"
                @click="toRqrReview(item.rqrmnt_no)"
                class="fixedW ellipisis-t set-link"
              >
                {{ item.demandNo || '-' }}
              </div>
            </td>
            <!-- 任务名称 -->
            <td class="name static-col4" v-if="tabType == 'task'">
              <div
                :title="titleFun(item.name)"
                @click="toDesignReview(item.id)"
                class="fixedW ellipisis-t set-link"
              >
                {{ item.name || '-' }}
              </div>
            </td>
            <!-- 卡点状态 -->
            <td
              class="point-state static-col5"
              :class="ifAddShadowFun(listStyle)"
              v-if="tabType == 'task'"
            >
              <span v-if="item.positionStatus == 'ok'">
                <span class="sucessPoint"> </span>
                <span class="sucesstext"> 正常</span>
              </span>
              <span v-if="item.positionStatus == 'fail'">
                <span class="failPoint"> </span>
                <span class="failtext"> 失败</span>
              </span>
            </td>
            <!-- 可视部分 -->
            <!-- 完成情况 -->
            <td class="finsh-flag" v-if="tableColums.includes('finshFlag')">
              <span v-if="item.finshFlag == 'pass'">
                <span class="sucessPoint"> </span>
                <span class="sucesstext"> 通过</span>
              </span>
              <span v-if="item.finshFlag == 'noPass'">
                <span class="failPoint"> </span>
                <span class="failtext"> 未通过</span>
              </span>
            </td>
            <!-- 当前阶段(有多个值和样式) -->
            <td
              class="current-stage"
              v-if="tableColums.includes('currentStage')"
            >
              <span
                :class="
                  currentStageShow(item.currentStage)
                    ? currentStageShow(item.currentStage).point
                    : ''
                "
              ></span>
              <span
                :class="
                  currentStageShow(item.currentStage)
                    ? currentStageShow(item.currentStage).color
                    : ''
                "
                v-if="currentStageShow(item.currentStage)"
                >{{ currentStageShow(item.currentStage).word }}</span
              >
            </td>
            <!-- 当前阶段开始时间 -->
            <td
              class="current-stage-time"
              v-if="tableColums.includes('currentStageTime')"
            >
              <div :title="titleFun(item.currentStageTime)">
                {{ item.currentStageTime || '-' }}
              </div>
            </td>
            <!-- 审核次数 -->
            <td class="check-count" v-if="tableColums.includes('checkCount')">
              <div :title="titleFun(item.checkCount)">
                {{ item.checkCount || '-' }}
              </div>
            </td>
            <!-- 开发人员 -->
            <td
              class="ui-verify-reporter"
              v-if="tableColums.includes('uiVerifyReporter')"
            >
              <div :title="titleFun(item.uiVerifyReporter)">
                {{ item.uiVerifyReporter || '-' }}
              </div>
            </td>
            <!-- 需求开发人员 -->
            <td
              class="first-uploader"
              v-if="tableColums.includes('firstUploader')"
            >
              <div :title="titleFun(item.firstUploader)">
                {{ item.firstUploader || '-' }}
              </div>
            </td>
            <!-- 计划提交内测日期 -->
            <td
              class="plan-inner-test-time"
              v-if="tableColums.includes('plan_inner_test_time')"
            >
              <div :title="titleFun(item.plan_inner_test_time)">
                {{ item.plan_inner_test_time || '-' }}
              </div>
            </td>
            <td
              class="plan_inner_test_date"
              v-if="tableColums.includes('plan_inner_test_date')"
            >
              <div :title="titleFun(item.plan_inner_test_date)">
                {{ item.plan_inner_test_date || '-' }}
              </div>
            </td>
            <!-- 计划提交业测日期 -->
            <td
              class="plan-uat-test-start-time"
              v-if="tableColums.includes('plan_uat_test_start_time')"
            >
              <div :title="titleFun(item.plan_uat_test_start_time)">
                {{ item.plan_uat_test_start_time || '-' }}
              </div>
            </td>
            <!-- 需求计划提交业测日期 -->
            <td
              class="plan-test-date"
              v-if="tableColums.includes('plan_test_date')"
            >
              <div :title="titleFun(item.plan_test_date)">
                {{ item.plan_test_date || '-' }}
              </div>
            </td>
            <!-- 实际提交内测日期 -->
            <td
              class="start-inner-test-time"
              v-if="tableColums.includes('start_inner_test_time')"
            >
              <div :title="titleFun(item.start_inner_test_time)">
                {{ item.start_inner_test_time || '-' }}
              </div>
            </td>
            <!-- 需求实际提交内测日期 -->
            <td
              class="real-inner-test-date"
              v-if="tableColums.includes('real_inner_test_date')"
            >
              <div :title="titleFun(item.real_inner_test_date)">
                {{ item.real_inner_test_date || '-' }}
              </div>
            </td>
            <!-- 实际提交业测日期 -->
            <td
              class="start-uat-test-time"
              v-if="tableColums.includes('start_uat_test_time')"
            >
              <div :title="titleFun(item.start_uat_test_time)">
                {{ item.start_uat_test_time || '-' }}
              </div>
            </td>
            <!-- 需求实际提交业测日期 -->
            <td
              class="real-test-date"
              v-if="tableColums.includes('real_test_date')"
            >
              <div :title="titleFun(item.real_test_date)">
                {{ item.real_test_date || '-' }}
              </div>
            </td>
            <!-- 研发单元牵头人-->
            <td
              class="implement-leader"
              v-if="tableColums.includes('implementLeaderNameCN')"
            >
              <div :title="titleFun(item.implementLeaderNameCN)">
                {{ leaderFun(item.implementLeaderNameCN) }}
              </div>
            </td>
            <!-- 实施单元编号 -->
            <td class="ipmpUnitNo" v-if="tableColums.includes('ipmpUnitNo')">
              <div
                :title="titleFun(item.ipmpUnitNo)"
                class="fixedW ellipisis-t"
              >
                {{ item.ipmpUnitNo || '-' }}
              </div>
            </td>
            <!-- 研发单元编号 -->
            <td
              class="fdev_implement_unit_no"
              v-if="tableColums.includes('fdev_implement_unit_no')"
            >
              <div
                :title="titleFun(item.fdev_implement_unit_no)"
                @click="
                  toUnitReview(item.fdevUnitId, item.fdev_implement_unit_no)
                "
                class="fixedW ellipisis-t set-link"
              >
                {{ item.fdev_implement_unit_no || '-' }}
              </div>
            </td>
            <!-- UI还原审核状态 -->
            <td
              class="review_status"
              v-if="tableColums.includes('review_status')"
            >
              <div>
                {{ UIstage(item.review_status) || '-' }}
              </div>
            </td>
          </tr>
          <template v-if="rowDex.unfinishedList.length == 0">
            <tr>
              <td class="no-pass static-col2" rowspan="1">
                <div class="text-center">
                  <div>
                    <span>未通过</span>
                    <br />
                    <span>0</span>
                  </div>
                </div>
              </td>
              <td
                v-show="!rowDex.unfinishedList.length"
                class="border-b"
                :class="[
                  tabType === 'task'
                    ? `static-col${i + 2}`
                    : `static-col${i + 30}`,
                  tabType === 'demand'
                    ? i + 30 === 31
                      ? ifAddShadowFun(listStyle)
                      : ''
                    : i + 2 === 5
                    ? ifAddShadowFun(listStyle)
                    : ''
                ]"
                v-for="i in falseTd"
                :key="i"
              >
                -
              </td>
            </tr>
          </template>
          <template v-else>
            <tr v-for="(item, idx) in rowDex.unfinishedList" :key="item.id">
              <td
                class="no-pass static-col2"
                v-if="idx == 0"
                :rowspan="rowDex.unfinishedList.length"
              >
                <div class="text-center">
                  <span>未通过</span>
                  <br />
                  <span>
                    {{ rowDex.unfinishedList.length }}
                  </span>
                </div>
              </td>
              <!-- 展示需求/任务名称列 -->
              <td
                class="oa-contact-name static-col-name"
                :class="ifAddShadowFun(listStyle)"
                v-if="tabType == 'demand'"
              >
                <div @click="toRqrReview(item.id)">
                  <div
                    class="fixedW ellipisis-t set-link"
                    :title="titleFun(item.oa_contact_name)"
                  >
                    {{ item.oa_contact_name || '-' }}
                  </div>
                </div>
              </td>
              <!-- 需求编号 -->
              <td class="demand-no static-col3" v-if="tabType == 'task'">
                <div
                  :title="titleFun(item.demandNo)"
                  @click="toRqrReview(item.rqrmnt_no)"
                  class="fixedW ellipisis-t set-link"
                >
                  {{ item.demandNo || '-' }}
                </div>
              </td>
              <td class="name static-col4" v-if="tabType == 'task'">
                <div
                  :title="titleFun(item.name)"
                  @click="toDesignReview(item.id)"
                  class="fixedW ellipisis-t set-link"
                >
                  {{ item.name || '-' }}
                </div>
              </td>
              <!-- 卡点状态 -->
              <td
                class="point-state static-col5"
                :class="ifAddShadowFun(listStyle)"
                v-if="tabType == 'task'"
              >
                <span v-if="item.positionStatus == 'ok'">
                  <span class="sucessPoint"> </span>
                  <span class="sucesstext"> 正常</span>
                </span>
                <span v-if="item.positionStatus == 'fail'">
                  <span class="failPoint"> </span>
                  <span class="failtext"> 失败</span>
                </span>
              </td>
              <!-- 完成情况 -->
              <td class="finsh-flag" v-if="tableColums.includes('finshFlag')">
                <span v-if="item.finshFlag == 'pass'">
                  <span class="sucessPoint"> </span>
                  <span class="sucesstext"> 通过</span>
                </span>
                <span v-if="item.finshFlag == 'noPass'">
                  <span class="failPoint"> </span>
                  <span class="failtext"> 未通过</span>
                </span>
              </td>
              <!-- 当前阶段(有多个值和样式) -->
              <td
                class="current-stage"
                v-if="tableColums.includes('currentStage')"
              >
                <span
                  :class="
                    currentStageShow(item.currentStage)
                      ? currentStageShow(item.currentStage).point
                      : ''
                  "
                ></span>
                <span
                  :class="
                    currentStageShow(item.currentStage)
                      ? currentStageShow(item.currentStage).color
                      : ''
                  "
                  v-if="currentStageShow(item.currentStage)"
                  >{{ currentStageShow(item.currentStage).word }}</span
                >
              </td>
              <!-- 当前阶段开始时间 -->
              <td
                class="current-stage-time"
                v-if="tableColums.includes('currentStageTime')"
              >
                <div :title="titleFun(item.currentStageTime)">
                  {{ item.currentStageTime || '-' }}
                </div>
              </td>
              <!-- 审核次数 -->
              <td class="check-count" v-if="tableColums.includes('checkCount')">
                <div :title="titleFun(item.checkCount)">
                  {{ item.checkCount || '-' }}
                </div>
              </td>
              <!-- 开发人员 -->
              <td
                class="ui-verify-reporter"
                v-if="tableColums.includes('uiVerifyReporter')"
              >
                <div :title="titleFun(item.uiVerifyReporter)">
                  {{ item.uiVerifyReporter || '-' }}
                </div>
              </td>
              <!-- 需求的开发人员 -->
              <td
                class="first-uploader"
                v-if="tableColums.includes('firstUploader')"
              >
                <div :title="titleFun(item.firstUploader)">
                  {{ item.firstUploader || '-' }}
                </div>
              </td>
              <!-- 任务计划提交内测日期 -->
              <td
                class="plan-inner-test-time"
                v-if="tableColums.includes('plan_inner_test_time')"
              >
                <div :title="titleFun(item.plan_inner_test_time)">
                  {{ item.plan_inner_test_time || '-' }}
                </div>
              </td>
              <!-- 需求计划提交内测日期 -->
              <td
                class="plan_inner_test_date"
                v-if="tableColums.includes('plan_inner_test_date')"
              >
                <div :title="titleFun(item.plan_inner_test_date)">
                  {{ item.plan_inner_test_date || '-' }}
                </div>
              </td>
              <!-- 计划提交业测日期 -->
              <td
                class="plan-uat-test-start-time"
                v-if="tableColums.includes('plan_uat_test_start_time')"
              >
                <div :title="titleFun(item.plan_uat_test_start_time)">
                  {{ item.plan_uat_test_start_time || '-' }}
                </div>
              </td>
              <!-- 需求计划提交业测日期 -->
              <td
                class="plan-test-date"
                v-if="tableColums.includes('plan_test_date')"
              >
                <div :title="titleFun(item.plan_test_date)">
                  {{ item.plan_test_date || '-' }}
                </div>
              </td>
              <!-- 实际提交内测日期 -->
              <td
                class="start-inner-test-time"
                v-if="tableColums.includes('start_inner_test_time')"
              >
                <div :title="titleFun(item.start_inner_test_time)">
                  {{ item.start_inner_test_time || '-' }}
                </div>
              </td>
              <!-- 需求实际提交内测日期 -->
              <td
                class="real-inner-test-date"
                v-if="tableColums.includes('real_inner_test_date')"
              >
                <div :title="titleFun(item.real_inner_test_date)">
                  {{ item.real_inner_test_date || '-' }}
                </div>
              </td>
              <!-- 实际提交业测日期 -->
              <td
                class="start-uat-test-time"
                v-if="tableColums.includes('start_uat_test_time')"
              >
                <div :title="titleFun(item.start_uat_test_time)">
                  {{ item.start_uat_test_time || '-' }}
                </div>
              </td>
              <!-- 需求实际提交业测日期 -->
              <td
                class="real-test-date"
                v-if="tableColums.includes('real_test_date')"
              >
                <div :title="titleFun(item.real_test_date)">
                  {{ item.real_test_date || '-' }}
                </div>
              </td>
              <!-- 研发单元牵头人-->
              <td
                class="implement-leader"
                v-if="tableColums.includes('implementLeaderNameCN')"
              >
                <div :title="titleFun(item.implementLeaderNameCN)">
                  {{ leaderFun(item.implementLeaderNameCN) }}
                </div>
              </td>
              <!-- 实施单元编号 -->
              <td class="ipmpUnitNo" v-if="tableColums.includes('ipmpUnitNo')">
                <div
                  :title="titleFun(item.ipmpUnitNo)"
                  class="fixedW ellipisis-t"
                >
                  {{ item.ipmpUnitNo || '-' }}
                </div>
              </td>
              <!-- 研发单元编号 -->
              <td
                class="fdev_implement_unit_no"
                v-if="tableColums.includes('fdev_implement_unit_no')"
              >
                <div
                  :title="titleFun(item.fdev_implement_unit_no)"
                  @click="
                    toUnitReview(item.fdevUnitId, item.fdev_implement_unit_no)
                  "
                  class="fixedW ellipisis-t set-link"
                >
                  {{ item.fdev_implement_unit_no || '-' }}
                </div>
              </td>
              <!-- UI还原审核状态 -->
              <td
                class="review_status"
                v-if="tableColums.includes('review_status')"
              >
                <div>
                  {{ UIstage(item.review_status) || '-' }}
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>
<script>
export default {
  name: 'report',
  data() {
    return {
      vw: null, // 列表内容可视宽度
      listStyle: null // 列表样式
    };
  },
  props: {
    reviewList: {
      default: () => [],
      type: Array
    },
    tabType: {
      type: String
    },
    visibleColumns: {
      default: () => [],
      type: Array
    }
  },
  computed: {
    // thead th中的列
    col() {
      let arr = JSON.parse(JSON.stringify(this.visibleColumns));
      return arr.sort((a, b) => a.sort - b.sort);
    },
    // tbody td中的列
    tableColums() {
      let arr = JSON.parse(JSON.stringify(this.visibleColumns));
      return arr.sort((a, b) => a.sort - b.sort).map(it => it.val);
    },
    // 通过0 未通过0 时假数据用的
    falseTd() {
      let len;
      if (this.tabType === 'task') len = this.tableColums.length + 3;
      if (this.tabType === 'demand') len = this.tableColums.length + 1;
      return len;
    }
  },
  mounted() {
    // 获取屏幕宽度
    this.vw = document.querySelector('.content').clientWidth;
  },
  methods: {
    titleFun(title) {
      if (Array.isArray(title)) return title.join();
      if (!title) return '';
      return title;
    },
    leaderFun(arg) {
      if (arg) return arg.join();
      return '-';
    },
    // 小组名称换行
    isMatch(str) {
      return str.split('').join('</br>');
    },
    // 列表自适应宽度
    getWidthFun(col) {
      let arr = col.map(it => it.val);
      let colWidth = {
        finshFlag: 90,
        currentStage: 90,
        currentStageTime: 180,
        checkCount: 90,
        uiVerifyReporter: 120,
        firstUploader: 120,
        plan_inner_test_time: 180,
        plan_inner_test_date: 180,
        plan_uat_test_start_time: 180,
        plan_test_date: 180,
        start_inner_test_time: 180,
        real_inner_test_date: 180,
        start_uat_test_time: 180,
        real_test_date: 180,
        implementLeaderNameCN: 180,
        ipmpUnitNo: 200,
        fdev_implement_unit_no: 200,
        review_status: 180
      };
      let colWidthSum = 0,
        fixedW = 0;
      arr.forEach(it => {
        colWidthSum += colWidth[it];
      });
      if (this.tabType === 'task') fixedW = 600;
      if (this.tabType === 'demand') fixedW = 310;
      let w =
        colWidthSum + fixedW <= this.vw ? '100%' : `${colWidthSum + fixedW}px`;
      this.listStyle = w;
      return {
        width: w
      };
    },
    // 是否添加滚动条
    addScrollFun(arg) {
      if (arg) {
        return arg === '100%' ? { overflow: 'auto' } : { overflowX: 'scroll' };
      }
      return { overflow: 'auto' };
    },
    ifAddShadowFun(arg) {
      if (arg) return arg === '100%' ? '' : 'box-shadow';
    },
    toUnitReview(id, num) {
      if (id && num) {
        this.$router.push({
          path: '/rqrmn/devUnitDetails',
          query: { id: id, dev_unit_no: num }
        });
      }
    },
    toRqrReview(no) {
      if (no) this.$router.push(`/rqrmn/rqrProfile/${no}`);
    },
    toDesignReview(id) {
      if (id) this.$router.push(`/job/list/${id}/design`);
    },
    UIstage(arg) {
      let UIstage = {
        wait_allot: '待分配',
        uploaded: '已上传',
        fixing: '待审核',
        nopass: '未通过',
        finished: '已完成',
        wait_upload: '待上传',
        uninvolved: '不涉及'
      };
      return UIstage[arg];
    },
    currentStageShow(arg) {
      let stageType = {
        alloting: {
          word: '分配中',
          color: 'allotColor',
          point: 'allotPoint'
        },
        checking: {
          word: '审核中',
          color: 'checkColor',
          point: 'checkPoint'
        },
        updateing: {
          word: '修改中',
          color: 'updateColor',
          point: 'updatePoint'
        },
        finish: {
          word: '完成',
          color: 'finishColor',
          point: 'finishPoint'
        }
      };
      return stageType[arg];
    }
  }
};
</script>
<style scoped lang="stylus">
.my-table {
  border-collapse: collapse;
}

.my-table thead tr th {
  height: 54px;
  font-weight: 600;
  background: #F4F6FD;
}

.my-table thead tr th:nth-child(n + 2) {
  text-align: left;
  padding-left: 15px;
  box-sizing: border-box;
}

.my-table tbody tr td {
  height: 54px;
  background: #FFFFFF;
}

.my-table tbody tr td:nth-child(n + 2) {
  text-align: left;
  padding-left: 15px;
  box-sizing: border-box;
}

.set-link {
  color: #0663BE;
  cursor: pointer;
}

.set-link:hover {
  cursor: pointer;
  color: #2196f3;
}

.border-b {
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.fixedW {
  width: 160px;
}

.ellipisis-t {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

// 列表多个隐藏
.hiddenHeader {
  display: none;
}

// 粘性列
.static-col1, .static-col2, .static-col3, .static-col4, .static-col5, .static-col31, .static-col-name, .static-th-col1, .static-th-col2, .static-th-col3, .static-th-col4, .static-th-col31 {
  position: sticky;
  z-index: 999;
}

.static-col1 {
  left: 0;
}

.static-col2 {
  left: 60px;
}

.static-col3 {
  left: 110px;
}

.static-col4 {
  left: 310px;
}

.static-col5 {
  left: 510px;
}

.static-col31, .static-col-name {
  left: 110px;
}

.static-th-col1 {
  left: 0;
}

.static-th-col2 {
  left: 110px;
}

.static-th-col3 {
  left: 310px;
}

.static-th-col4 {
  left: 510px;
}

.static-th-col31 {
  left: 110px;
}

.box-shadow {
  box-shadow: 2px 0 4px 0 rgba(51, 51, 51, 0.15);
}

// 列样式
.groupName {
  width: 60px;
  font-weight: 600;
  padding: 10px 0;
  background: #EFEFEF !important;
}

.pass, .no-pass {
  width: 50px;
  font-weight: 600;
  padding-left: 0 !important;
}

.pass {
  background: #d2f6d5 !important;
}

.no-pass {
  background: #fddee3 !important;
}

.demand-no, .oa-contact-name, .name {
  width: 200px;
  text-align: left;
  padding-left: 15px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
  box-sizing: border-box;
}

.point-state {
  width: 90px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.finsh-flag, .current-stage{
  width: 90px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.current-stage-time {
  width: 180px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.check-count {
  width: 90px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.plan-inner-test-time, .plan_inner_test_date, .plan-uat-test-start-time, .plan-test-date, .start-inner-test-time, .real-inner-test-date, .start-uat-test-time, .real-test-date {
  width: 180px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.ui-verify-reporter, .first-uploader {
  width: 120px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.implement-leader, .review_status {
  width: 180px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.fdev_implement_unit_no, .ipmpUnitNo {
  width: 200px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.finishColor {
  color: #4DBB59;
}

.finishPoint {
  background-image: linear-gradient(270deg, rgba(77, 187, 89, 0.5) 0%, #4DBB59 100%);
  border-radius: 12px;
  border-radius: 12px;
  margin-right: 8px;
  display: inline-block;
  width: 10px;
  height: 10px;
}

.allotPoint {
  background-image: linear-gradient(270deg, rgba(249, 129, 110, 0.5) 0%, #F9816E 100%);
  border-radius: 12px;
  border-radius: 12px;
  margin-right: 8px;
  display: inline-block;
  width: 10px;
  height: 10px;
}

.allotColor {
  color: #F9816E;
}

.checkColor {
  color: #FD8D00;
}

.checkPoint {
  background-image: linear-gradient(270deg, rgba(253, 141, 0, 0.5) 0%, #FD8D00 100%);
  border-radius: 12px;
  border-radius: 12px;
  display: inline-block;
  margin-right: 8px;
  width: 10px;
  height: 10px;
}

.updatePoint {
  background-image: linear-gradient(270deg, rgba(77, 187, 89, 0.5) 0%, #42A5F5 100%);
  border-radius: 12px;
  border-radius: 12px;
  display: inline-block;
  margin-right: 8px;
  width: 10px;
  height: 10px;
}

.updateColor {
  color: #42A5F5;
}

.mrt20 {
  margin-top: 20px;
}

.sucessPoint {
  background-image: linear-gradient(270deg, rgba(77, 187, 89, 0.5) 0%, #4DBB59 100%);
  border-radius: 12px;
  border-radius: 12px;
  display: inline-block;
  margin-right: 8px;
  width: 10px;
  height: 10px;
}

.failPoint {
  background-image: linear-gradient(270deg, rgba(239, 83, 80, 0.5) 0%, #EF5350 100%);
  border-radius: 12px;
  border-radius: 12px;
  margin-right: 8px;
  display: inline-block;
  width: 10px;
  height: 10px;
}

.failtext {
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #EF5350;
  letter-spacing: 0;
  line-height: 22px;
}

.sucesstext {
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #4DBB59;
  letter-spacing: 0;
  line-height: 22px;
}
</style>
