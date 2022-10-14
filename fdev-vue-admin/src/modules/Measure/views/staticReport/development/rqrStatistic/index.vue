<template>
  <div>
    <!-- 查询条件 -->
    <f-block block class="toBottom">
      <!-- 所属小组 -->
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
            :value="isParent"
            @input="updateIsParent($event)"
            label="包含子组"
            left-label
        /></template>
        <template #topRight>
          <!-- 查询按钮 -->
          <fdev-btn
            label="查询"
            dialog
            @click="init()"
            ficon="search"
            v-forbidMultipleClick/></template
      ></f-gstree>
    </f-block>
    <!-- 数据 -->
    <f-block>
      <div class="row no-wrap">
        <fdev-tabs v-model="tab" align="left">
          <fdev-tab name="lead" label="各组牵头需求统计" />
          <fdev-tab name="particapate" label="各组对应阶段实施需求数量统计" />
        </fdev-tabs>
        <fdev-space />
        <!-- 优先级 -->
        <fdev-select
          :value="groupType"
          :options="groupTypeOptions"
          borderless
          option-value="val"
          option-label="label"
          map-options
          emit-value
          options-dense
          @input="updateGroupType($event)"
        />
        <!-- 下载 -->
        <fdev-btn
          ficon="download"
          label="下载"
          normal
          class="q-mr-sm"
          @click="downloadExcel()"
          :loading="btnLoading"
        />
        <!-- 统计规则 -->
        <fdev-btn
          label="统计规则"
          normal
          ficon="eye"
          class="q-mb-xs"
          @click="ruleOpenHandle = !ruleOpenHandle"
        />
      </div>

      <fdev-separator class="q-mb-md" />
      <!-- 牵头需求统计 -->
      <Loading class="q-mb-md" :visible="tableLoading">
        <!-- 表格 -->
        <div>
          <div class="scroll">
            <!-- 有数据时 -->
            <components
              :render="render"
              :ref="tab"
              :is="tab === 'lead' ? 'lead' : 'participate'"
              :dataSource="tab === 'lead' ? rqrStatisData : rqrmntByTaskData"
            />
          </div>
        </div>
      </Loading>
    </f-block>
    <!-- 统计规则弹窗 -->
    <f-dialog
      v-model="ruleOpenHandle"
      @before-close="ruleOpenHandle = false"
      title="需求统计规则"
    >
      <!-- 备注 -->
      <div class="indent">
        <div>
          1、合计项不包含已投产需求与暂缓的需求；
        </div>
        <div>
          2、业务需求人均负荷 = 业务需求 / 项目组规模；
        </div>
        <div>
          3、开发总人均负荷 = （开发阶段（牵头） + 开发阶段（参与））/
          项目组规模；
        </div>
        <div>4、∞代表该小组下没有纳入项目组规模的人员。</div>
        <div>
          5、排队：计划启动日期在七天内的需求则为排队需求。
        </div>
        <div>
          6、开发阶段（参与）列：根据【所选小组】查询组下全部的【任务】,再查询组下所有的【任务】的所属【牵头需求】
        </div>
        <div>
          ,统计这些【牵头需求】中处于【开发阶段】的需求数量
        </div>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="知道了" dialog @click="ruleOpenHandle = false"
      /></template>
    </f-dialog>
  </div>
</template>

<script>
import lead from './components/lead'; // 牵头需求维度
import participate from './components/participate'; // 牵头需求维度
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';
import { deepClone, appendNode } from '@/utils/utils';
import { queryDefaultGroupIds } from '@/modules/Dashboard/services/methods.js';
export default {
  name: 'RequirementAnalysis',
  components: { Loading, lead, participate },
  data() {
    return {
      groupTreeData: [], //树
      rqrStatisData: [],
      groupTypeOptions: [
        { label: '优先级', val: '4' },
        { label: '高', val: '0' },
        { label: '中', val: '1' },
        { label: '一般', val: '2' },
        { label: '低', val: '3' }
      ],
      open: false,
      btnLoading: false,
      tableLoading: false,
      resetList: [], //默认查的组
      ruleOpenHandle: false,
      tab: 'lead', //默认为牵头投产需求维度
      render: false,
      rqrmntByTaskData: [] //各组各阶段需求统计
    };
  },
  watch: {
    tab(val) {
      val === 'lead' ? this.queryRqrmt() : this.queryRqrmtByTask();
    },
    //优先级
    groupType(val) {
      this.tab === 'lead' ? this.queryRqrmt() : this.queryRqrmtByTask();
    }
  },
  computed: {
    ...mapState('userActionSaveMeasure/rqrStatistic', [
      'selectedGroups',
      'isParent',
      'groupsTree',
      'groupType'
    ]),
    ...mapGetters('userActionSaveMeasure/rqrStatistic', ['groupData']),
    ...mapState('global', ['loading']),
    ...mapState('measureForm', ['rqrStatistic', 'groupRqrmnt']),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    }
  },
  // 进入页面时操作
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('requirementTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('requirementTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/rqrStatistic', [
      'updateIsParent',
      'updateSelectedGroups',
      'updateGroupsTree',
      'updateGroupType'
    ]),
    ...mapActions('measureForm', ['queryDemandStatistics', 'queryGroupRqrmnt']),
    ...mapActions('userForm', ['fetchGroup']),

    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('requirementTree', JSON.stringify(tree));
    },
    //重置整棵树：
    resetTree(tree) {
      this.updateSelectedGroups(deepClone(this.resetList));
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: this.selectedGroups.indexOf(item.id) > -1 ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    appendNode(parent, set) {
      return appendNode(parent, set);
    },
    // 下载表格
    downloadExcel(id, name) {
      this.btnLoading = true;
      this.$refs[this.tab].childExport();
      this.btnLoading = false;
    },
    // 查询需求统计
    async queryRqrmt(type, val) {
      this.tableLoading = true;
      const params = {
        groupIds: this.selectedGroups,
        includeChild: this.isParent,
        priority: this.groupType == '4' ? undefined : this.groupType
      };
      try {
        await this.queryDemandStatistics(params);
        this.rqrStatisData = this.rqrStatistic;
        this.tableLoading = false;
      } catch (e) {
        this.tableLoading = false;
      }
    },
    // 查询各组各阶段需求
    async queryRqrmtByTask(type, val) {
      this.tableLoading = true;
      const params = {
        groupIds: this.selectedGroups,
        isParent: this.isParent,
        priority: this.groupType == '4' ? undefined : this.groupType
      };
      try {
        await this.queryGroupRqrmnt(params);
        this.rqrmntByTaskData = this.groupRqrmnt;
        this.handleSum('groupRqrmnt', 'rqrmntByTaskData');
        this.tableLoading = false;
      } catch (e) {
        this.tableLoading = false;
      }
    },
    //各组各阶段需求计算合计
    handleSum(handle, target) {
      const obj = {
        groupName: '合计'
      };
      this[handle].forEach(item => {
        const itemKey = Object.keys(item);
        itemKey.forEach(key => {
          if (!obj[key]) {
            obj[key] = 0;
          }
          if (key !== 'groupName' && !isNaN(Number(item[key]))) {
            obj[key] += Number(item[key]);
          }
        });
      });
      /* 开发总人均负荷 = （开发阶段（牵头） + 开发阶段（参与））/ 项目组规模；
          kaifarjfh = (nogdevelopkj + nogdevelopyw + kaifayw + kaifakj) / count
      */
      obj.kaifarjfh =
        (obj.nogdevelopkj + obj.nogdevelopyw + obj.kaifayw + obj.kaifakj) /
        obj.count;
      obj.kaifarjfh = obj.kaifarjfh.toFixed(2);
      /* 业务需求人均负荷 = 业务合计 / 项目组规模
          ywxqrjfh = totalyw / count
       */
      obj.ywxqrjfh = obj.totalyw / obj.count;
      obj.ywxqrjfh = obj.ywxqrjfh.toFixed(2);

      this[target].push(obj);
    },
    init() {
      this.tab === 'lead' ? this.queryRqrmt() : this.queryRqrmtByTask();
    }
  },
  async created() {
    // 查询默认的组
    let result = await queryDefaultGroupIds();
    this.resetList = deepClone(result);
    // 如果不选组就展示默认组
    if (!this.selectedGroups.length) {
      this.updateSelectedGroups(this.resetList);
    }
    // 获取小组列表
    await this.fetchGroup();
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.indent
  text-indent 3em
  line-height:30px
  div
    padding-top:4px
.scroll
  overflow auto

.toBottom
   margin-bottom:10px

// 滚动条样式
.scroll::-webkit-scrollbar {
    /*滚动条整体样式*/
    width: 8px !important;
    /*高宽分别对应横竖滚动条的尺寸*/
    height: 8px !important;
    background: #ffffff !important;
    cursor: pointer;
}
 .scroll::-webkit-scrollbar-thumb {
    /*滚动条里面小方块*/
    border-radius: 10px !important;
    box-shadow: inset 0 0 5px rgba(240, 240, 240, 0.5) !important;
    background: rgba(176,190,197,0.5) !important;
    cursor: pointer !important;
}
 .scroll::-webkit-scrollbar-track {
    /*滚动条里面轨道*/
    box-shadow: inset 0 0 5px rgba(240, 240, 240, 0.5) !important;
    border-radius: 4 !important;
    background: rgba(240, 240, 240, 0.5) !important;
    cursor: pointer !important;
}
</style>
