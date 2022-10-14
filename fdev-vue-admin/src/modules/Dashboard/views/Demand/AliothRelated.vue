<template>
  <f-block page>
    <Loading :visible="loading">
      <div class="bg-white shadow-1 q-my-sm q-pa-sm">
        <div class="float-right">
          <fdev-toggle
            label="包含子组:"
            :value="isIncludeChildren"
            @input="updateIsIncludeChildren($event)"
            left-label
          />
        </div>
        <GroupSelectTree
          v-if="groupData && groupData.length > 0"
          :dataSource="groupData"
          @selectGroup="selectGroup"
          @clickGroup="clickGroup"
          @resetTree="resetTree"
        />
      </div>

      <div class="bg-white q-pa-md">
        <div class="text-h6 text-center q-pb-md">
          玉衡测试相关表格
        </div>
        <div class="row q-pa-sm table-wrapper justify-between ">
          <div class="line-left">
            <f-formitem page label="开始日期">
              <f-date
                :options="startOptions"
                v-model="startDate"
                @input="changeStartDate($event)"
              />
            </f-formitem>
            <f-formitem page label="结束日期">
              <f-date
                :options="endOptions"
                v-model="endDate"
                @input="changeEndDate($event)"
              />
            </f-formitem>
          </div>
          <div class="l-right">
            <fdev-btn
              dialog
              label="查询"
              @click="filterWithTime"
              ficon="search"
            />
            <fdev-btn
              class="q-ml-md"
              dialog
              label="导出"
              @click="downloadExcel"
              :loading="btnLoading"
            />
          </div>
        </div>
        <table
          width="100%"
          border="1"
          bordercolor="black"
          cellspacing="0"
          cellpadding="0"
          id="table"
        >
          <tr>
            <td>序号</td>
            <td>组别</td>
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
            <td>{{ i + 1 }}</td>
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

        <div class="div-msg" v-if="tableData.length === 0">
          <fdev-icon name="warning" class="warn" />
          <span>没有可用数据</span>
        </div>

        <div>备注:有效缺陷比例 = 有效缺陷数 / 执行案例数</div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import GroupSelectTree from '@/components/UI/GroupSelectTree';
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { deepClone } from '@/utils/utils';
import { queryDefaultGroupIds } from '@/modules/Dashboard/services/methods.js';
import moment from 'moment';
export default {
  name: 'AliothRelated',
  components: { GroupSelectTree, Loading },
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
.float-right
  display flex;
  align-items center;
  margin-top 6px;
  margin-left 20px;
  float right;
tr
  td, th
    text-align center
    padding 5px
.input
  min-width 230px
  display inline-block
.div-msg
  font-size 12px
  color #000
  line-height 30px
.line-left{
  display:flex;
}
.l-right{
  display:flex;
}
</style>
