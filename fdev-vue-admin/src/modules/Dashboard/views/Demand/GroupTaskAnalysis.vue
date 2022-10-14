<template>
  <f-block page>
    <div class="q-pa-sm bg-white">
      <div class="float-right float-switch">
        <span class="text-primarycursor-pointer">是否包含子组：</span>
        <fdev-toggle
          :value="isParent"
          @input="updateIsParent($event)"
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
      <div class="text-center">
        <fdev-btn
          dialog
          label="查询"
          @click="initAll(groupType)"
          ficon="search"
        />
      </div>
    </div>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md table-wrapper">
        <div class="q-pb-sm">
          <div class="group-right">
            <fdev-select
              :value="groupType"
              :options="groupTypeOptions"
              borderless
              option-value="val"
              option-label="label"
              map-options
              emit-value
              options-dense
              @input="initAll($event)"
            />
            <fdev-btn
              ficon="download"
              label="下载"
              normal
              @click="downloadExcel"
              :loading="btnLoading"
            />
          </div>
          <div class="column items-center">
            <div class="text-h5 text-title q-ma-md">小组任务数量统计</div>
          </div>
        </div>
        <fdev-separator inset />
        <div class="bg-white scroll">
          <table
            width="100%"
            border="1"
            bordercolor="black"
            cellspacing="0"
            cellpadding="0"
            :class="groupStatisList.length !== 0 ? 'table-width' : ''"
            id="table"
          >
            <thead>
              <tr>
                <td rowspan="2">序号</td>
                <td rowspan="2">分组</td>
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
              <td class="text-center">{{ item.num }}</td>
              <td class="text-center">{{ item.group }}</td>
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

          <div class="div-msg" v-if="groupStatisList.length === 0">
            <fdev-icon name="warning" class="warn"></fdev-icon>
            <span>没有可用数据</span>
          </div>
        </div>
        <div class="q-mt-lg">
          <div>
            备注：1、每列 “数字1+数字2” 格式表示 业务任务个数+科技任务个数；
          </div>
          <div class="indent">
            2、待实施列 “数字1(数字2)+数字3(数字4) 格式表示 ”
            业务任务个数(待排队任务个数)+科技任务个数(待排队任务个数)；
          </div>
          <div class="indent">3、合计项不重复统计超期数据；</div>
          <div class="indent">
            4、开发人均负荷 = 开发阶段任务 / 组内开发资源（保留两位小数）；
          </div>
          <div class="indent">5、∞代表该小组下没有角色为开发人员的人；</div>
          <div class="indent">
            6、此统计表中的任务数量是基于挂载了需求的任务进行统计的。
          </div>
          <div class="indent">
            7、排队任务说明：计划启动日期 &lt;= 当前日期 + 7
          </div>
        </div>
      </fdev-card>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';
import GroupSelectTree from '@/components/UI/GroupSelectTree';
import { deepClone, appendNode } from '@/utils/utils';
import { queryDefaultGroupIds } from '@/modules/Dashboard/services/methods.js';
export default {
  name: 'GroupTaskAnalysis',
  components: { Loading, GroupSelectTree },
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
      resetList: []
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
    sumAnalyze(data) {
      let num = 1;
      data.forEach(item => {
        item.num = num++;
      });
      return data;
    },
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

    downloadExcel() {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('table', '小组任务数量统计.xlsx');
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
    appendNode(parent, set, depth = 2) {
      return appendNode(parent, set, depth);
    },
    async initAll(val) {
      //更新（全部/重要）状态
      this.updateGroupType(val);
      this.init(this.selectedGroups);
    },
    async init(ids) {
      this.loading = true;
      ids = Array.from(new Set(ids));
      this.ids = ids;
      await this.queryGroupStatis({
        ids: ids,
        priority: this.groupType === '1' ? true : false,
        isIncludeChildren: this.isParent
      });
      this.groupStatisList = this.sumAnalyze(this.groupStatis);
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
    await this.fetchGroup();
    //this.init(this.selectedGroups);
    this.initAll(this.groupType);
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
.table-width
  width 110%
.scroll
  overflow auto
.div-msg
  font-size 12px
  color #000
  line-height 30px
.warn
  margin-right 5px
.select-group
  position absolute
  right 50px
  top 18px
.table-wrapper
  position relative
.group-right
  display: flex;
  align-items: center;
  float: right;
  margin: 16px 0;
.float-switch{
  display: flex;
  align-items: center;
  margin-top: 6px;
  margin-left: 20px;
}
</style>
