<template>
  <div>
    <!-- 查询条件 -->
    <f-block class="toBottom"
      ><f-gstree
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
            v-forbidMultipleClick/></template></f-gstree
    ></f-block>

    <!-- 表格数据 -->
    <f-block>
      <Loading :visible="loading">
        <!-- 标题/优先级/下载 -->
        <div class="row no-wrap q-mb-md">
          <!-- 标题 -->
          <div class="title">
            <f-icon
              name="list_s_f"
              class="text-primary"
              style="margin-right:8px"
            />
            研发单元统计
          </div>
          <fdev-space />
          <!-- 优先级 -->
          <fdev-select
            :value="groupType"
            @input="updateGroupType($event)"
            :options="groupTypeOptions"
            borderless
            option-value="val"
            option-label="label"
            map-options
            emit-value
            options-dense
          />
          <!-- 下载 -->
          <fdev-btn
            ficon="download"
            label="下载"
            normal
            @click="downloadExcel"
            :loading="btnLoading"
            class="q-mr-sm"
          />
          <!-- 统计规则 -->
          <fdev-btn
            label="统计规则"
            normal
            ficon="eye"
            @click="ruleOpenHandle = !ruleOpenHandle"
          />
        </div>
        <!-- 表格数据 -->
        <div class="scroll">
          <!-- 有数据时 -->
          <table
            width="100%"
            border="1"
            bordercolor="#DDDDDD"
            class="table-style"
            :class="rqrStatisData.length !== 0 ? 'table-width' : ''"
            id="table"
            v-if="rqrStatisData.length !== 0"
          >
            <thead>
              <tr>
                <td rowspan="3">团队</td>
                <td rowspan="2" colspan="2">评估中</td>
                <td rowspan="2" colspan="4">待实施(排队)</td>
                <td rowspan="2" colspan="2">开发阶段</td>
                <td colspan="6">测试阶段</td>
                <td colspan="6">超期需求</td>
                <td rowspan="2" colspan="2">暂缓</td>
                <td rowspan="3">开发人均负荷</td>
                <td rowspan="2" colspan="2">已投产</td>
                <td rowspan="2" colspan="3">合计</td>
              </tr>
              <tr>
                <td colspan="2">SIT</td>
                <td colspan="2">UAT</td>
                <td colspan="2">REL</td>
                <td colspan="2">开发启动超期</td>
                <td colspan="2">提测超期</td>
                <td colspan="2">投产超期</td>
              </tr>
              <tr>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>排队</td>
                <td>科技</td>
                <td>排队</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>业务</td>
                <td>科技</td>
                <td>总合计</td>
              </tr>
            </thead>
            <!--具体的数据值-->
            <tr v-for="(item, index) in rqrStatisData" :key="index">
              <td class="text-center">{{ item.groupName }}</td>
              <!-- 待评估 -->
              <td class="text-center">
                {{ item.pgzyw }}
              </td>
              <td class="text-center">{{ item.pgzkj }}</td>
              <!-- 待实施 -->
              <td class="text-center">
                {{ item.dssyw }}
              </td>
              <td class="text-center">{{ item.dsspdyw }}</td>
              <td class="text-center">{{ item.dsskj }}</td>
              <td class="text-center">{{ item.dsspdkj }}</td>
              <!-- 开发阶段 -->
              <td class="text-center">
                {{ item.kaifayw }}
              </td>
              <td class="text-center">{{ item.kaifakj }}</td>
              <!-- 测试阶段SIT -->
              <td class="text-center">
                {{ item.cssityw }}
              </td>
              <td class="text-center">{{ item.cssitkj }}</td>
              <!-- 测试阶段UAT -->
              <td class="text-center">
                {{ item.csuatyw }}
              </td>
              <td class="text-center">{{ item.csuatkj }}</td>
              <!-- 测试阶段REL -->
              <td class="text-center">
                {{ item.csrelyw }}
              </td>
              <td class="text-center">{{ item.csrelkj }}</td>
              <!-- 开发启动超期 -->
              <td class="text-center">
                {{ item.kfqdcqyw }}
              </td>
              <td class="text-center">{{ item.kfqdcqkj }}</td>
              <!-- 提测超期 -->
              <td class="text-center">
                {{ item.tccqyw }}
              </td>
              <td class="text-center">{{ item.tccqkj }}</td>
              <!-- 投产超期 -->
              <td class="text-center">
                {{ item.procqyw }}
              </td>
              <td class="text-center">{{ item.procqkj }}</td>
              <!-- 暂缓 -->
              <td class="text-center">{{ item.deferyw }}</td>
              <td class="text-center">{{ item.deferkj }}</td>
              <!-- 开发人均负荷 -->
              <td class="text-center">{{ item.kaifarjfh }}</td>
              <!-- 已投产 -->
              <td class="text-center">
                {{ item.csproyw }}
              </td>
              <td class="text-center">{{ item.csprokj }}</td>
              <!-- 合计 -->
              <td class="text-center">{{ item.totalyw }}</td>
              <td class="text-center">{{ item.totalkj }}</td>
              <td class="text-center">{{ item.totalyw + item.totalkj }}</td>
            </tr>
          </table>

          <!-- 无数据时 -->
          <div v-else class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </div> </Loading
    ></f-block>
    <!-- 统计规则弹窗 -->
    <f-dialog
      v-model="ruleOpenHandle"
      @before-close="ruleOpenHandle = false"
      title="研发单元统计规则"
    >
      <!-- 备注 -->
      <div class="indent">
        <div>1、业务：业务需求研发单元个数</div>
        <div>
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;科技：科技需求研发单元个数
        </div>
        <div class="text-green">
          2、排队：指在待实施中的研发单元中,启动日期在未来7天的范围内的
        </div>
        <div>3、合计项不重复统计超期数据</div>
        <div>
          4、开发人均负荷 = 开发阶段需求 / 组内开发资源（保留两位小数）；
        </div>
        <div>5、∞代表该小组下没有角色为开发人员的人。</div>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn label="知道了" dialog @click="ruleOpenHandle = false"
      /></template>
    </f-dialog>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';
import { deepClone, appendNode } from '@/utils/utils';
import { queryDefaultGroupIds } from '@/modules/Dashboard/services/methods.js';
export default {
  name: 'RequirementAnalysis',
  components: { Loading },
  data() {
    return {
      loading: false,
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
      resetList: [],
      ruleOpenHandle: false
    };
  },
  computed: {
    ...mapGetters(
      'userActionSaveDashboard/projectTeamManage/implUnitAnalysis',
      ['groupData']
    ),

    ...mapState('userActionSaveDashboard/projectTeamManage/implUnitAnalysis', [
      'isParent',
      'selectedGroups',
      'groupsTree',
      'groupType'
    ]),
    ...mapState('dashboard', ['implUnitStatis']),
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
  watch: {
    async groupType(val) {
      this.init();
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('impiUnitTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('impiUnitTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  },

  methods: {
    ...mapMutations(
      'userActionSaveDashboard/projectTeamManage/implUnitAnalysis',
      [
        'updateIsParent',
        'updateSelectedGroups',
        'updateGroupsTree',
        'updateGroupType'
      ]
    ),
    ...mapActions('dashboard', ['queryImplUnitStatis']),
    ...mapActions('userForm', ['fetchGroup']),
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('impiUnitTree', JSON.stringify(tree));
    },
    //重置整棵树：
    resetTree(tree) {
      this.updateSelectedGroups(deepClone(this.resetList));
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    downloadExcel() {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('table', '需求研发单元统计.xlsx');
      });
      this.btnLoading = false;
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: this.resetList.indexOf(item.id) > -1 ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    appendNode(parent, set) {
      return appendNode(parent, set);
    },
    async init() {
      this.loading = true;
      const params = {
        groupIds: this.selectedGroups,
        isParent: this.isParent,
        priority: this.groupType == 4 ? undefined : this.groupType
      };
      await this.queryImplUnitStatis(params);
      this.rqrStatisData = this.implUnitStatis;
      this.loading = false;
    }
  },
  async created() {
    // 查询默认的组
    let result = await queryDefaultGroupIds();
    this.resetList = deepClone(result);
    // 初始进入页面时如果selectedGroups已选择则使用selectedGroups，否则使用默认小组（防止原进原出不生效）
    if (!this.selectedGroups.length) {
      this.updateSelectedGroups(this.resetList);
    }
    await this.fetchGroup();
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
tr
  td, th
    text-align center
    padding 5px
    max-width 300px
.indent
  text-indent 3em
  line-height:20px
.table-width
  width 120%
.scroll
  overflow auto
.select-group
  position absolute
  right 60px
  top 18px
.table-style
  border-collapse:collapse !important
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 24px;
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
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
