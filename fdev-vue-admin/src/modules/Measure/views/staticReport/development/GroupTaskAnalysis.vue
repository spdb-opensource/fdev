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
          <!-- 是否包含子组 -->
          <fdev-toggle
            :value="isParent"
            @input="updateIsParent($event)"
            label="包含子组"
            left-label
            size="lg"
          />
        </template>
        <template #topRight>
          <!-- 查询按钮 -->
          <fdev-btn
            label="查询"
            dialog
            @click="initAll(groupType)"
            ficon="search"
            v-forbidMultipleClick/></template
      ></f-gstree>
    </f-block>
    <!-- 表格数据 -->
    <f-block>
      <Loading :visible="loading">
        <!-- 标题、下载按钮、统计规则 -->
        <div class="row no-wrap q-mb-md">
          <!-- 标题 -->
          <div class="title">
            <f-icon
              name="list_s_f"
              class="text-primary"
              style="margin-right:8px"
            />
            <span>小组任务数量统计</span>
          </div>
          <fdev-space />
          <!-- 是否重要 -->
          <fdev-select
            :value="groupType"
            :options="groupTypeOptions"
            borderless
            option-value="val"
            option-label="label"
            map-options
            emit-value
            @input="initAll($event)"
          />
          <!-- 下载按钮 -->
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
        <!-- 表格 -->
        <div class="scroll">
          <!-- 有数据时 -->
          <table
            width="100%"
            border="1"
            bordercolor="#DDDDDD"
            class="table-style"
            :class="groupStatisList.length !== 0 ? 'table-width' : ''"
            id="groupTaskTable"
          >
            <thead>
              <tr>
                <td rowspan="2">团队</td>
                <td rowspan="2">待实施(排队)</td>
                <td rowspan="2">开发阶段</td>
                <td colspan="3">测试阶段</td>
                <td colspan="3">超期任务</td>
                <td rowspan="2">暂缓任务</td>
                <td rowspan="2">开发人均负荷</td>
                <td rowspan="2">已投产</td>
                <td rowspan="2">合计</td>
              </tr>
              <tr>
                <td>SIT</td>
                <td>UAT</td>
                <td>REL</td>
                <td>开发启动超期</td>
                <td>提测超期</td>
                <td>投产超期</td>
              </tr>
            </thead>
            <!--具体的数据值-->
            <tr v-for="(item, index) in groupStatisList" :key="index">
              <td class="text-center" style="min-width:100px">
                {{ item.group }}
              </td>
              <td class="text-center">{{ item.todo }}</td>
              <td class="text-center">{{ item.develop }}</td>
              <td class="text-center">{{ item.sit }}</td>
              <td class="text-center">{{ item.uat }}</td>
              <td class="text-center">{{ item.rel }}</td>
              <td class="text-center">{{ item.dev_delay }}</td>
              <td class="text-center">{{ item.test_delay }}</td>
              <td class="text-center">{{ item.pro_delay }}</td>
              <td class="text-center">{{ item.deferTask }}</td>
              <td class="text-center">{{ item.avg }}</td>
              <td class="text-center">{{ item.production }}</td>
              <td class="text-center">{{ item.sum }}</td>
            </tr>
          </table>

          <!-- 无数据时 -->
          <div v-if="groupStatisList.length === 0" class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </div> </Loading
    ></f-block>
    <!-- 统计规则弹窗 -->
    <f-dialog
      v-model="ruleOpenHandle"
      @before-close="ruleOpenHandle = false"
      title="任务统计规则"
    >
      <!-- 备注 -->
      <div class="indent">
        <div>
          1、每列 “数字1+数字2” 格式表示 业务任务个数+科技任务个数；
        </div>
        <div>2、待实施列 “数字1(数字2)+数字3(数字4) 格式表示 ”</div>
        <div>
          业务任务个数(待排队业务任务个数)+科技任务个数(待排队科技任务个数)
        </div>
        <div>3、合计项不重复统计超期数据</div>
        <div>
          4、开发人均负荷 = 开发阶段任务 / 组内开发资源（保留两位小数）;
        </div>
        <div>5、∞代表该小组下没有角色为开发人员的人</div>
        <div>
          6、此统计表中的任务数量是基于挂载了需求的任务进行统计的。
        </div>
        <div>
          7、排队任务：计划启动日期在七天内的任务
        </div>
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
  name: 'GroupTaskAnalysis',
  components: { Loading },
  data() {
    return {
      groupTreeData: [], //树
      loading: false,
      groupStatisList: [],
      groupTypeOptions: [
        { label: '全部', val: '0' },
        { label: '重要', val: '1' }
      ],
      open: false,
      btnLoading: false,
      resetList: [],
      ruleOpenHandle: false
    };
  },
  computed: {
    ...mapState('userActionSaveDashboard/projectTeamManage/groupTaskAnalysis', [
      'isParent',
      'selectedGroups',
      'groupsTree',
      'groupType'
    ]),
    ...mapGetters(
      'userActionSaveDashboard/projectTeamManage/groupTaskAnalysis',
      ['groupData']
    ),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('dashboard', ['groupStatis']),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    }
  },
  // 进入页面前
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('groupTaskTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('groupTaskTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  },

  methods: {
    ...mapMutations(
      'userActionSaveDashboard/projectTeamManage/groupTaskAnalysis',
      [
        'updateIsParent',
        'updateSelectedGroups',
        'updateGroupsTree',
        'updateGroupType'
      ]
    ),
    ...mapActions('dashboard', ['queryGroupStatis']),
    ...mapActions('userForm', ['fetchGroup']),
    //选择组
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('groupTaskTree', JSON.stringify(tree));
    },
    //重置整棵树：
    resetTree(tree) {
      this.updateSelectedGroups(deepClone(this.resetList));
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    // 下载
    downloadExcel() {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('groupTaskTable', '小组任务数量统计.xlsx');
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
    async initAll(val) {
      //更新（全部/重要）状态
      this.updateGroupType(val);
      this.init(this.selectedGroups);
    },
    // 查询
    async init(ids) {
      this.loading = true;
      ids = Array.from(new Set(ids));
      this.ids = ids;
      await this.queryGroupStatis({
        ids: ids,
        priority: this.groupType === '1' ? true : false,
        isIncludeChildren: this.isParent
      });
      this.groupStatisList = this.groupStatis;
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
    // 获取小组列表
    await this.fetchGroup();
    this.initAll(this.groupType);
  }
};
</script>

<style lang="stylus" scoped>
tr
  td, th
    text-align center
    padding 8px
.indent
  text-indent 3em
  line-height:30px
.table-width
  width 130%
.scroll
  overflow auto
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
