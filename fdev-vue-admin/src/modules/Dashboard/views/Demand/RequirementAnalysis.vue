<template>
  <f-block page>
    <div class="q-pa-sm q-mb-md bg-white">
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

    <Loading class="q-mb-md" :visible="loading['dashboard/queryStatis']">
      <fdev-card flat square class="q-pa-md table-wrapper">
        <div class="q-pb-sm f-pos">
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
              @input="queryRqrmt($event, 'changeType')"
            />
            <fdev-btn
              ficon="download"
              label="下载"
              normal
              @click="downloadExcel('table', '需求统计')"
              :loading="btnLoading"
            />
          </div>
          <div class="column items-center">
            <div class="text-h5 text-title q-ma-md">需求统计</div>
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
                <td rowspan="3">序号</td>
                <td rowspan="3">分组</td>
                <td colspan="4">需求阶段</td>
                <td rowspan="2" colspan="4">待实施(排队)</td>
                <td colspan="6">测试阶段</td>
                <td colspan="2" rowspan="2">开发阶段(牵头)</td>
                <td colspan="2" rowspan="2">开发阶段(参与)</td>
                <td colspan="2" rowspan="2">暂缓</td>
                <td rowspan="3">业务需求人均负荷</td>
                <td rowspan="3">开发总人均负荷</td>
                <td rowspan="2" colspan="2">已投产</td>
                <td rowspan="2" colspan="3">合计</td>
              </tr>
              <tr>
                <td colspan="2">需求预评估</td>
                <td colspan="2">需求评估</td>
                <td colspan="2">SIT</td>
                <td colspan="2">UAT</td>
                <td colspan="2">REL</td>
              </tr>
              <tr>
                <td>业务</td>
                <td>科技</td>
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
                <td>总合计</td>
              </tr>
            </thead>
            <!--具体的数据值-->
            <tr v-for="(item, index) in rqrStatisData" :key="index">
              <td class="text-center" v-if="item.groupName !== '合计'">
                {{ index + 1 }}
              </td>
              <td
                class="text-center"
                :colspan="item.groupName === '合计' ? 2 : 1"
              >
                {{ item.groupName }}
              </td>
              <td class="text-center">
                {{ item.ypgyw }}
              </td>
              <td class="text-center">
                {{ item.ypgkj }}
              </td>
              <td class="text-center">
                {{ item.pgyw }}
              </td>
              <td class="text-center">
                {{ item.pgkj }}
              </td>
              <td class="text-center">{{ item.dssyw }}</td>
              <td class="text-center">{{ item.dsspdyw }}</td>
              <td class="text-center">{{ item.dsskj }}</td>
              <td class="text-center">{{ item.dsspdkj }}</td>

              <td class="text-center">{{ item.cssityw }}</td>
              <td class="text-center">{{ item.cssitkj }}</td>
              <td class="text-center">{{ item.csuatyw }}</td>
              <td class="text-center">{{ item.csuatkj }}</td>
              <td class="text-center">{{ item.csrelyw }}</td>
              <td class="text-center">{{ item.csrelkj }}</td>

              <td class="text-center">{{ item.kaifayw }}</td>
              <td class="text-center">{{ item.kaifakj }}</td>

              <td class="text-center">{{ item.nogdevelopyw }}</td>
              <td class="text-center">{{ item.nogdevelopkj }}</td>

              <td class="text-center">{{ item.waityw }}</td>
              <td class="text-center">{{ item.waitkj }}</td>

              <td class="text-center">{{ item.ywxqrjfh }}</td>
              <td class="text-center">{{ item.kaifarjfh }}</td>

              <td class="text-center">{{ item.csproyw }}</td>
              <td class="text-center">{{ item.csprokj }}</td>

              <td class="text-center">{{ item.totalyw }}</td>
              <td class="text-center">{{ item.totalkj }}</td>
              <td class="text-center">
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
            备注：1、合计项不包含已投产需求；
          </div>
          <div class="indent">
            2、业务需求人均负荷 = 业务需求 / 项目组规模；
          </div>
          <div class="indent">
            3、开发总人均负荷 = （开发阶段（牵头） + 开发阶段（参与））/
            项目组规模；
          </div>
          <div class="indent">4、∞代表该小组下没有纳入项目组规模的人员。</div>
          <div class="indent">
            5、排队：判断需求是否7天内启动，若是则为排队需求。
          </div>
        </div>
      </fdev-card>
    </Loading>

    <Loading class="q-mb-md" :visible="loading['dashboard/queryGroupRqrmnt']">
      <fdev-card flat square class="q-pa-md table-wrapper">
        <div class="q-pb-sm">
          <div class="column items-center f-pos">
            <div class="group-right">
              <fdev-select
                :value="taskGroupType"
                :options="groupTypeOptions"
                borderless
                option-value="val"
                option-label="label"
                map-options
                emit-value
                options-dense
                @input="queryRqrmtByTask($event, 'changeType')"
              />
              <fdev-btn
                ficon="download"
                label="下载"
                normal
                @click="downloadExcel('task', '各组对应阶段实施需求数量')"
                :loading="btnLoading"
              />
            </div>
            <div class="text-h5 text-title q-ma-md">
              各组对应阶段实施需求数量
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
              :class="rqrmntByTaskData.length !== 0 ? 'table-width' : ''"
              id="task"
            >
              <thead>
                <tr>
                  <td rowspan="3">序号</td>
                  <td rowspan="3">分组</td>
                  <td rowspan="2" colspan="2">待实施(排队)</td>
                  <td colspan="6">测试阶段</td>
                  <td colspan="2" rowspan="2">开发阶段(牵头)</td>
                  <td colspan="2" rowspan="2">开发阶段(参与)</td>
                  <td colspan="2" rowspan="2">暂缓</td>
                  <td rowspan="3">业务需求人均负荷</td>
                  <td rowspan="3">开发总人均负荷</td>
                  <td rowspan="2" colspan="2">已投产</td>
                  <td rowspan="2" colspan="3">合计</td>
                </tr>
                <tr>
                  <td colspan="2">SIT</td>
                  <td colspan="2">UAT</td>
                  <td colspan="2">REL</td>
                </tr>
                <tr>
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
              <tr v-for="(item, index) in rqrmntByTaskData" :key="index">
                <td class="text-center" v-if="item.groupName !== '合计'">
                  {{ index + 1 }}
                </td>
                <td
                  class="text-center"
                  :colspan="item.groupName === '合计' ? 2 : 1"
                >
                  {{ item.groupName }}
                </td>
                <td class="text-center">{{ item.dssyw }}</td>
                <td class="text-center">{{ item.dsskj }}</td>

                <td class="text-center">{{ item.cssityw }}</td>
                <td class="text-center">{{ item.cssitkj }}</td>
                <td class="text-center">{{ item.csuatyw }}</td>
                <td class="text-center">{{ item.csuatkj }}</td>
                <td class="text-center">{{ item.csrelyw }}</td>
                <td class="text-center">{{ item.csrelkj }}</td>

                <td class="text-center">{{ item.kaifayw }}</td>
                <td class="text-center">{{ item.kaifakj }}</td>

                <td class="text-center">{{ item.nogdevelopyw }}</td>
                <td class="text-center">{{ item.nogdevelopkj }}</td>

                <td class="text-center">{{ item.waityw }}</td>
                <td class="text-center">{{ item.waitkj }}</td>

                <td class="text-center">{{ item.ywxqrjfh }}</td>
                <td class="text-center">{{ item.kaifarjfh }}</td>

                <td class="text-center">{{ item.csproyw }}</td>
                <td class="text-center">{{ item.csprokj }}</td>

                <td class="text-center">{{ item.totalyw }}</td>
                <td class="text-center">{{ item.totalkj }}</td>
                <td class="text-center">
                  {{ item.totalyw + item.totalkj }}
                </td>
              </tr>
            </table>

            <div class="div-msg" v-if="rqrmntByTaskData.length === 0">
              <fdev-icon name="warning" class="warn"></fdev-icon>
              <span>没有可用数据</span>
            </div>
          </div>
          <div class="q-mt-lg">
            <div>
              备注：规则同上
            </div>
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
  name: 'RequirementAnalysis',
  components: { Loading, GroupSelectTree },
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
      rqrmntByTaskData: [],
      resetList: [] //默认查的组
    };
  },
  computed: {
    ...mapState('userActionSaveDashboard/projectTeamManage/reqAnalysis', [
      'selectedGroups',
      'groupsTree',
      'isParent',
      'taskGroupType',
      'groupType'
    ]),
    ...mapGetters('userActionSaveDashboard/projectTeamManage/reqAnalysis', [
      'groupData'
    ]),
    ...mapState('global', ['loading']),
    ...mapState('dashboard', ['statis', 'groupRqrmnt']),
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
    ...mapMutations('userActionSaveDashboard/projectTeamManage/reqAnalysis', [
      'updateIsParent',
      'updateSelectedGroups',
      'updateGroupsTree',
      'updateTaskGroupType',
      'updateGroupType'
    ]),
    ...mapActions('dashboard', ['queryStatis', 'queryGroupRqrmnt']),
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
    downloadExcel(id, name) {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_html_table(id, name);
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
          selected: this.selectedGroups.indexOf(item.id) > -1 ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    appendNode(parent, set, depth = 2) {
      return appendNode(parent, set, depth);
    },
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
    async queryRqrmt(type, val) {
      if (val == 'changeType') {
        //更新优先级状态
        this.updateGroupType(type);
      }
      const params = {
        groupIds: this.selectedGroups,
        isParent: this.isParent,
        priority: this.groupType == '4' ? undefined : this.groupType
      };
      await this.queryStatis(params);
      this.rqrStatisData = this.statis;
      this.handleSum('statis', 'rqrStatisData');
    },
    async queryRqrmtByTask(type, val) {
      if (val == 'changeType') {
        //更新优先级状态
        this.updateTaskGroupType(type);
      }
      const params = {
        groupIds: this.selectedGroups,
        isParent: this.isParent,
        priority: this.taskGroupType == '4' ? undefined : this.taskGroupType
      };
      await this.queryGroupRqrmnt(params);
      this.rqrmntByTaskData = this.groupRqrmnt;
      this.handleSum('groupRqrmnt', 'rqrmntByTaskData');
    },
    init() {
      this.queryRqrmtByTask();
      this.queryRqrmt();
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
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.indent
  text-indent 3em
.table-width
  width 130%
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
tr
  td, th
    text-align center
    padding 5px
.table-wrapper
  position relative
.f-pos{
  position: relative;
}
.group-right{
  display: flex;
  align-items: center;
  top: 16px;
  right: 0px;
  position: absolute;
}
.float-switch{
  display: flex;
  align-items: center;
  margin-top: 6px;
  margin-left: 20px;
}
</style>
