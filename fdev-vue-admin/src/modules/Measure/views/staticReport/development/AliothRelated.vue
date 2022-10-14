<template>
  <div>
    <!-- 查询条件 -->
    <f-block class="toBottom">
      <f-gstree
        title="所属小组"
        title-icon="member_s_f"
        :data-source="groupData"
        @select-group="selectGroup"
        @click-group="clickGroup"
        @reset-tree="resetTree"
        v-if="groupData && groupData.length > 0"
      >
        <!-- gstree自带重置按钮，两边插槽 -->
        <template #topLeft>
          <!-- 包含子组 -->
          <fdev-toggle
            size="lg"
            :value="isIncludeChildren"
            @input="updateIsIncludeChildren($event)"
            s
            label="包含子组"
            left-label
        /></template>
        <template #topRight>
          <!-- 查询按钮 -->
          <fdev-btn
            dialog
            label="查询"
            @click="filterWithTime"
            ficon="search"
            v-forbidMultipleClick
          /> </template
      ></f-gstree>
    </f-block>
    <f-block>
      <Loading :visible="loading">
        <!-- 选择日期 、下载按钮-->
        <div class="row no-wrap">
          <f-formitem
            label="选择日期"
            label-style="width:56px;margin-right:32px"
            value-style="width:144px"
            class="q-mr-sm"
          >
            <f-date
              :options="startOptions"
              v-model="startDate"
              @input="changeStartDate($event)"
            />
          </f-formitem>
          <f-formitem
            label="至"
            label-style="width:14px;margin-right:8px"
            value-style="width:144px"
          >
            <f-date
              :options="endOptions"
              v-model="endDate"
              @input="changeEndDate($event)"
            />
          </f-formitem>
          <fdev-space />
          <fdev-btn
            normal
            label="下载"
            ficon="download"
            @click="downloadExcel"
            :loading="btnLoading"
          />
        </div>
        <!-- 标题 -->
        <div class="title q-mt-md row">
          <div class="vertical-middle">
            <f-icon
              name="dashboard_s_f"
              class="text-primary "
              style="margin-right:12px"
            />
          </div>
          <span>玉衡测试相关表格</span>
          <div class="vertical-middle">
            <f-icon
              name="help_c_o"
              class="cursor-pointer text-primary q-ml-xs"
            />
            <fdev-tooltip>有效缺陷比例 = 有效缺陷数 / 执行案例数</fdev-tooltip>
          </div>
        </div>
        <div class="q-py-md">
          <!-- 表格数据 -->
          <table
            width="100%"
            border="1"
            bordercolor="#DDDDDD"
            class="table-style"
            id="table"
            v-if="tableData.length !== 0"
          >
            <tr>
              <td>团队</td>
              <td>时间段</td>
              <td>测试人数</td>
              <td>需求数</td>
              <td>编写案例数</td>
              <td>执行案例数</td>
              <td>缺陷数</td>
              <td>有效缺陷数</td>
              <td>有效缺陷比例</td>
            </tr>
            <tr v-for="(row, i) in tableData" :key="i">
              <td>{{ row.fdevGroupName }}</td>
              <td>{{ time }}</td>
              <td>{{ row.tester }}</td>
              <td>{{ row.requestNumber }}</td>
              <td>{{ row.caseNumber }}</td>
              <td>{{ row.performCaseNumber }}</td>
              <td>{{ row.countMantis }}</td>
              <td>{{ row.validCountMantis }}</td>
              <td>{{ row.proportion || '-' }}</td>
            </tr>
          </table>

          <!-- 无数据时 -->
          <div v-else class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </div> </Loading
    ></f-block>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { deepClone } from '@/utils/utils';
import { queryDefaultGroupIds } from '@/modules/Dashboard/services/methods.js';
import moment from 'moment';
export default {
  name: 'AliothRelated',
  components: { Loading },
  data() {
    return {
      groupTreeData: [], //树
      loading: false,
      listModel: {},
      tableData: [],
      btnLoading: false,
      startDate: moment(new Date() - 24 * 60 * 60 * 30000).format('YYYY/MM/DD'),
      endDate: moment(new Date()).format('YYYY/MM/DD'),
      resetList: []
    };
  },
  computed: {
    ...mapState('userActionSaveDashboard/projectTeamManage/aliothRelated', [
      'isIncludeChildren',
      'selectedGroups',
      'groupsTree'
    ]),
    ...mapGetters('userActionSaveDashboard/projectTeamManage/aliothRelated', [
      'groupData'
    ]),
    ...mapGetters('userForm', ['groupsTree']),
    ...mapState('dashboard', ['resourceManagement']),
    time() {
      const { endDate, startDate } = this.listModel;
      if (!endDate && !startDate) return '-';
      return startDate + '-' + endDate;
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('aliothRelatedTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('aliothRelatedTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  },
  methods: {
    ...mapMutations('userActionSaveDashboard/projectTeamManage/aliothRelated', [
      'updateIsIncludeChildren',
      'updateSelectedGroups',
      'updateGroupsTree',
      'updateStartDate',
      'updateEndDate'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryResourceManagement']),
    //日期
    changeStartDate(val) {
      this.updateStartDate(val);
      return this.$refs.qDateProxyStart.hide();
    },
    changeEndDate(val) {
      this.updateEndDate(val);
      return this.$refs.qDateProxyEnd.hide();
    },
    //选择组方法
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('aliothRelatedTree', JSON.stringify(tree));
    },
    //重置整棵树：
    resetTree(tree) {
      this.updateSelectedGroups([...this.resetList]);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    downloadExcel() {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('table', '玉衡测试相关表格.xlsx');
      });
      this.btnLoading = false;
    },
    endOptions(date) {
      this.updateStartDate(this.startDate ? this.startDate : '');
      return date >= this.startDate.replace(/-/g, '/');
    },
    startOptions(date) {
      if (this.endDate) {
        return date <= this.endDate.replace(/-/g, '/');
      }
      return true;
    },
    filterWithTime() {
      this.init();
    },
    async init() {
      this.listModel = {
        startDate: this.startDate.replace(/-/g, '/'),
        endDate: this.endDate.replace(/-/g, '/')
      };
      this.loading = true;
      if (this.selectedGroups.length === 0) {
        this.tableData = [];
      } else {
        await this.queryResourceManagement({
          fdevGroupId: this.selectedGroups,
          ...this.listModel,
          ifContainsSubGroup: this.isIncludeChildren
        });
        this.tableData = this.resourceManagement;
      }
      this.loading = false;
    }
  },
  async created() {
    // 查询默认的组
    let result = await queryDefaultGroupIds();
    this.resetList = deepClone(result);
    if (!this.selectedGroups.length) {
      this.updateSelectedGroups(this.resetList);
    }
    this.fetchGroup();
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
tr
  td, th
    text-align center
    padding 5px
.input
  min-width 230px
  display inline-block

.l-right{
  display:flex;
}
.toBottom
   margin-bottom:10px
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
.table-style
  border-collapse:collapse !important
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 24px;
table td{
  padding:8px 10px;
}
</style>
