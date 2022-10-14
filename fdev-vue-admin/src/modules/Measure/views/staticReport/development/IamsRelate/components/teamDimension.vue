<template>
  <div>
    <!-- 查询条件 -->
    <div class="q-mb-md">
      <f-gstree
        title="所属小组"
        title-icon="dashboard_s_f"
        :data-source="analysisData"
        @select-group="selectGroup"
        @click-group="clickGroup"
        @reset-tree="resetTree"
        v-if="analysisData && analysisData.length > 0"
      >
        <!-- gstree自带重置按钮，两边插槽 -->
        <template #topLeft>
          <!-- 包含子组 -->
          <fdev-toggle
            size="lg"
            :value="isIncludeChildren"
            @input="updateIsIncludeChildren($event)"
            label="包含子组"
            left-label
        /></template>
        <template #topRight>
          <!-- 查询按钮 -->
          <fdev-btn
            label="查询"
            dialog
            @click="init(selectedGroups)"
            ficon="search"
            v-forbidMultipleClick/></template
      ></f-gstree>
      <div class="row">
        <fdev-space />
        <fdev-btn-toggle
          :value="showSwitch"
          @input="updateShowSwitch($event)"
          :options="[
            { label: '图表', value: 'chartShow' },
            { label: '表格', value: 'formShow' }
          ]"
        />
      </div>
    </div>
    <!-- 挡板相关数据 -->
    <Loading :visible="dangbanLoading">
      <div class="row no-wrap">
        <!-- 各组挡板使用人数占比 -->
        <div class="col-6">
          <!-- 标题 -->
          <div class="row">
            <div class="title">
              <f-icon
                :name="
                  showSwitch === 'chartShow' ? 'dashboard_s_f' : 'list_s_f'
                "
                class="text-primary"
                style="margin-right:8px"
              />
              各组上月挡板使用人数占比({{ date }})
            </div>
            <f-icon
              name="download"
              @click="downloadUserNumExcel"
              class="text-primary q-ml-xs cursor-pointer"
            />
          </div>
          <!-- 图表 -->
          <StackBarChart
            id="percent"
            width="95%"
            :chart-top="chartTop"
            :chart-data="iamsGroupChartAll"
            :height="barChartHeight"
            :showSwitch="showSwitch"
            v-show="showSwitch === 'chartShow'"
          />
          <!-- 表格 -->
          <IamsGroupForm
            id="userNum"
            class="q-mt-md"
            :formHeader="formUserHeader"
            :iamsFormDate="iamsGroupForm"
            v-show="showSwitch === 'formShow'"
          />
        </div>

        <!-- 各小组挡板人均交易量 -->
        <div class="col-6 q-ml-md">
          <div class="row">
            <!-- 标题 -->
            <div class="title">
              <f-icon
                :name="
                  showSwitch === 'chartShow' ? 'dashboard_s_f' : 'list_s_f'
                "
                class="text-primary"
                style="margin-right:12px"
              />
              各小组上月挡板人均交易量({{ date }})
            </div>
            <f-icon
              name="download"
              @click="downloadUserAverageExcel"
              class="text-primary q-ml-xs cursor-pointer"
            />
          </div>
          <!-- 图表 -->
          <StackBarChart
            id="average"
            width="95%"
            :chart-top="chartTop"
            :chart-data="iamsGroupChartAverage"
            :height="barChartHeight"
            :showSwitch="showSwitch"
            v-show="showSwitch === 'chartShow'"
          />
          <!--表格 -->
          <IamsGroupForm
            id="userAverage"
            class="q-mt-md"
            :formHeader="formAverageHeader"
            :iamsFormDate="iamsGroupForm"
            v-show="showSwitch === 'formShow'"
          />
        </div>
      </div>
    </Loading>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { appendNode } from '@/utils/utils';
import StackBarChart from '@/modules/Measure/components/Chart/StackBarChart';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import moment from 'moment';
import IamsGroupForm from '@/modules/Dashboard/components/form/IamsGroupForm';
export default {
  name: 'Analysis',
  data() {
    return {
      analysisData: [],
      groupTreeData: [], //树
      chartTop: '2%',
      ids: [],
      dangbanChart: {}, //挡板交易量
      averageChart: {}, //人均挡板交易量
      date: '',
      dangbanLoading: false,
      formUserHeader: ['总人数', '使用人数', '占比'],
      formAverageHeader: ['总人数', '总交易量', '人均交易量']
    };
  },
  components: {
    Loading,
    StackBarChart,
    IamsGroupForm
  },
  computed: {
    ...mapState('userActionSaveDashboard/analysis', [
      'selectedGroups',
      'isIncludeChildren',
      'showSwitch',
      'groupsTree'
    ]),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('dashboard', {
      iamsGroupChart: 'iamsGroupChart',
      iamsGroupForm: 'iamsGroupForm'
    }),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapGetters('dashboard', ['iamsGroupChartAll', 'iamsGroupChartAverage']),
    ...mapGetters('userActionSaveDashboard/analysis', ['groupData']),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    },
    barChartHeight() {
      return String(350 + this.selectedGroups.length * 10) + 'px';
    }
  },
  methods: {
    ...mapMutations('userActionSaveDashboard/analysis', [
      'updateSelectedGroups',
      'updateIsIncludeChildren',
      'updateShowSwitch',
      'updateGroupsTree'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryIamsGroupChart']),
    ...mapActions('user', ['fetch']),
    //选择组
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击groupSelectTree中的组触发的callback
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('IamsTree', JSON.stringify(tree));
    },
    appendNode(parent, set) {
      return appendNode(parent, set);
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: item.id === '5c81c4d0d3e2a1126ce30049' ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    //重置整棵树
    resetTree(tree) {
      this.updateSelectedGroups(['5c81c4d0d3e2a1126ce30049']);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    // 查询
    async init(ids) {
      ids = Array.from(new Set(ids));
      this.analysisData = this.groupData.slice(0);
      this.analysisData.filter(item => {
        ids.filter(id => {
          if (id === item.id) {
            this.$set(item, 'selected', true);
          }
        });
      });
      this.ids = ids;
      this.dangbanInit(ids);
    },
    // 下载各组挡板使用人数占比
    downloadUserNumExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'userNum',
          '各组挡板使用人数占比' + this.date + '.xlsx'
        );
      });
    },
    // 下载各小组人均交易量
    downloadUserAverageExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'userAverage',
          '各小组挡板人均交易量' + this.date + '.xlsx'
        );
      });
    },
    // 获取前最近一个月挡板数据
    async dangbanInit(ids) {
      this.date = moment(new Date())
        .subtract(1, 'month')
        .format('YYYY-MM');
      this.dangbanLoading = true;
      try {
        await this.queryIamsGroupChart({
          date: this.date,
          isIncludeChildren: this.isIncludeChildren,
          ids: ids
        });
        this.dangbanLoading = false;
      } catch (e) {
        this.dangbanLoading = false;
      }
    },
    //离开页面时原进原出
    saveData() {
      let item = JSON.parse(sessionStorage.getItem('IamsTree'));
      if (item) {
        this.updateGroupsTree(item);
      }
    }
  },
  async created() {
    //获取小组
    await this.fetchGroup();
    this.init(this.selectedGroups);
  }
};
</script>

<style lang="stylus" scoped>
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
</style>
