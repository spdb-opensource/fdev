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
        <fdev-btn dialog label="查询" @click="init" ficon="search" />
      </div>
    </div>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md">
        <div class="q-pb-sm table-wrapper">
          <div class="group-right">
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
            <fdev-btn
              ficon="download"
              label="下载"
              normal
              @click="downloadExcel"
              :loading="btnLoading"
            />
          </div>
          <div class="column items-center">
            <div class="text-h5 text-title q-ma-md">研发单元统计</div>
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
            :class="rqrStatisData.length !== 0 ? 'table-width' : ''"
            id="table"
          >
            <thead>
              <tr>
                <td rowspan="2">序号</td>
                <td rowspan="2">分组</td>
                <td rowspan="2">评估中</td>
                <td rowspan="2">待实施(排队)</td>
                <td rowspan="2">开发阶段</td>
                <td colspan="3">测试阶段</td>
                <td colspan="3">超期需求</td>
                <td colspan="2" rowspan="1">暂缓</td>
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
                <td>业务</td>
                <td>科技</td>
              </tr>
            </thead>
            <!--具体的数据值-->
            <tr v-for="(item, index) in rqrStatisData" :key="index">
              <td class="text-center">{{ item.num }}</td>
              <td class="text-center">{{ item.groupName }}</td>
              <!-- 待评估 -->
              <td class="text-center">
                {{ item.pgzyw }}+{{ item.pgzkj }} =
                {{ item.pgzyw + item.pgzkj }}
              </td>
              <td class="text-center">
                {{ item.dssyw }} ({{ item.dsspdyw }}) + {{ item.dsskj }} ({{
                  item.dsspdkj
                }}) = {{ item.dssyw + item.dsskj }} ({{
                  item.dsspdyw + item.dsspdkj
                }})
              </td>
              <td class="text-center">
                {{ item.kaifayw }} + {{ item.kaifakj }} =
                {{ item.kaifayw + item.kaifakj }}
              </td>
              <td class="text-center">
                {{ item.cssityw }} + {{ item.cssitkj }} =
                {{ item.cssityw + item.cssitkj }}
              </td>
              <td class="text-center">
                {{ item.csuatyw }} + {{ item.csuatkj }} =
                {{ item.csuatyw + item.csuatkj }}
              </td>
              <td class="text-center">
                {{ item.csrelyw }} + {{ item.csrelkj }} =
                {{ item.csrelyw + item.csrelkj }}
              </td>
              <td class="text-center">
                {{ item.kfqdcqyw }} + {{ item.kfqdcqkj }} =
                {{ item.kfqdcqyw + item.kfqdcqkj }}
              </td>
              <td class="text-center">
                {{ item.tccqyw }} + {{ item.tccqkj }} =
                {{ item.tccqyw + item.tccqkj }}
              </td>
              <td class="text-center">
                {{ item.procqyw }} + {{ item.procqkj }} =
                {{ item.procqyw + item.procqkj }}
              </td>
              <td class="text-center">{{ item.deferyw }}</td>
              <td class="text-center">{{ item.deferkj }}</td>
              <td class="text-center">{{ item.kaifarjfh }}</td>
              <td class="text-center">
                {{ item.csproyw }} + {{ item.csprokj }} =
                {{ item.csproyw + item.csprokj }}
              </td>
              <td class="text-center">
                {{ item.totalyw }} + {{ item.totalkj }} =
                {{ item.totalyw + item.totalkj }}
              </td>
            </tr>
          </table>

          <div class="div-msg" v-if="rqrStatisData.length === 0">
            <fdev-icon name="warning" class="warn"></fdev-icon>
            <span>没有可用数据</span>
          </div>
        </div>
        <div class="q-mt-lg">
          <div>
            备注：1、每列 “数字1+数字2” 格式表示
            业务需求研发单元个数+科技需求研发单元个数；
          </div>
          <div class="indent">
            2、待实施列 “数字1(数字2)+数字3(数字4)” 格式表示
            业务需求研发单元个数(待排队个数)+科技需求研发单元个数个数(待排队个数)
          </div>
          <div class="indent text-green">
            排队：指在待实施中的研发单元中,启动日期在未来7天的范围内的（数字1包括数字2）
          </div>
          <div class="indent">3、合计项不重复统计超期数据；</div>
          <div class="indent">
            4、开发人均负荷 = 开发阶段需求 / 组内开发资源（保留两位小数）；
          </div>
          <div class="indent">5、∞代表该小组下没有角色为开发人员的人。</div>
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
  name: 'RequirementAnalysis',
  components: { Loading, GroupSelectTree },
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
      resetList: []
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
    sumAnalyze(data) {
      let num = 1;
      data.forEach(item => {
        item.num = num++;
      });
      return data;
    },
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
        excel.export_table_to_excel('table', '需求实施单元统计.xlsx');
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
    async init() {
      this.loading = true;
      const params = {
        groupIds: this.selectedGroups,
        isParent: this.isParent,
        priority: this.groupType == 4 ? undefined : this.groupType
      };
      await this.queryImplUnitStatis(params);
      this.rqrStatisData = this.sumAnalyze(this.implUnitStatis);
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
.table-width
  width 120%
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
  right 60px
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
