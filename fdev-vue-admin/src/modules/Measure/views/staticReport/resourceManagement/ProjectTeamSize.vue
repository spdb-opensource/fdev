<template>
  <div>
    <!-- 查询条件 -->
    <f-block page class="block-style">
      <!-- 所属小组 -->
      <f-gstree
        title="所属小组"
        title-icon="member_s_f"
        v-if="groupData && groupData.length > 0"
        :data-source="groupData"
        @select-group="selectGroup"
        @click-group="clickGroup"
        @reset-tree="resetTree"
      >
        <!-- gstree自带重置按钮，两边插槽分别放开关和查询按钮 -->
        <template #topLeft
          ><fdev-toggle
            size="lg"
            label="包含子组"
            left-label
            :value="isIncludeChildren"
            @input="updateIsIncludeChildren($event)"
        /></template>
        <template #topRight
          ><fdev-btn
            dialog
            label="查询"
            @click="init"
            ficon="search"
            v-forbidMultipleClick/></template></f-gstree
    ></f-block>
    <!-- 表格 -->
    <f-block page>
      <Loading :visible="loading">
        <!-- 标题 -->
        <div class="row">
          <div>
            <f-icon name="list_s_f" class="text-primary icon-style" />
            <span class="title-font q-mr-sm">项目组规模</span>
            <f-icon
              name="help_c_o"
              class="cursor-pointer text-primary"
              style="vertical-align:middle"
            />
            <fdev-tooltip
              >满足以下条件的用户将被列在“备注”列中,而不会被统计在“项目组规模”列中<br />
              1:用户角色中不包含“开发人员”或“测试人员”<br />
              2:用户标签中包含“非项目组资源”的人员</fdev-tooltip
            >
          </div>
          <fdev-space />
          <fdev-btn
            ficon="download"
            label="下载"
            normal
            @click="downloadExcel"
            :loading="btnLoading"
            :disable="tableData.groups.length === 0"
          />
        </div>
        <div class="q-pb-llg scroll">
          <table
            width="100%"
            border="1"
            bordercolor="#DDDDDD"
            cellspacing="0"
            cellpadding="0"
            id="projectTeamizeTable"
            class="table-style"
            v-if="tableData.groups.length !== 0"
          >
            <tr>
              <td rowspan="2">团队</td>
              <td :colspan="tableData.bank.length">行内</td>
              <td :colspan="tableData.dev.length">厂商</td>
              <td rowspan="2">项目组规模</td>
              <td rowspan="2">备注</td>
            </tr>
            <tr>
              <td v-for="item in tableData.bank" :key="item.id">
                {{ item.name }}
              </td>
              <td v-for="item in tableData.dev" :key="item.id">
                {{ item.name }}
              </td>
            </tr>
            <tr v-for="row in tableData.groups" :key="row.groupName">
              <!-- 组名 -->
              <td>{{ row.groupName }}</td>
              <!-- 行内各区域人数 -->
              <td v-for="bank in tableData.bank" :key="bank.id">
                {{ row.bank[bank.id] }}
              </td>
              <!-- 厂商各区域人数 -->
              <td v-for="dev in tableData.dev" :key="dev.id">
                {{ row.dev[dev.id] }}
              </td>
              <!-- 项目组规模 -->
              <td>{{ row.userSum }}</td>
              <!-- 备注 -->
              <td>
                <div class="td-width">
                  <fdev-tooltip
                    v-if="row.remark"
                    anchor="top middle"
                    self="center middle"
                  >
                    {{ row.remark }}
                  </fdev-tooltip>
                  {{ row.remark }}
                </div>
              </td>
            </tr>
            <!-- 合计 -->
            <tr v-if="tableData.groups.length > 0">
              <td>合计</td>
              <td v-for="bank in tableData.bank" :key="bank.id">
                {{ sum.bank[bank.id] }}
              </td>

              <td v-for="dev in tableData.dev" :key="dev.id">
                {{ sum.dev[dev.id] }}
              </td>

              <td>{{ sum.userSum }}</td>
              <td>-</td>
            </tr>
          </table>

          <!-- 无数据时 -->
          <div v-else class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </div>
      </Loading>
    </f-block>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';

export default {
  name: 'ProjectTeamSize',
  components: { Loading },
  data() {
    return {
      groupTreeData: [], //树
      loading: false,
      btnLoading: false,
      tableData: {
        bank: [],
        dev: [],
        groups: []
      }
    };
  },
  computed: {
    ...mapState('userActionSaveMeasure/projectTeamSize', [
      'isIncludeChildren',
      'selectedGroups',
      'groupsTree'
    ]),
    ...mapGetters('userActionSaveMeasure/projectTeamSize', ['groupData']),
    ...mapGetters('userForm', ['groupsTree']),
    ...mapState('measureForm', ['teamSize']),
    ...mapState('user', ['currentUser']),
    // 计算合计
    sum() {
      const obj = {
        bank: {},
        dev: {},
        userSum: 0
      };
      const { bank, dev, groups } = this.tableData;
      groups.forEach(group => {
        bank.forEach(item => {
          if (!obj.bank[item.id]) {
            obj.bank[item.id] = 0;
          }
          obj.bank[item.id] += Number(group.bank[item.id]);
        });
        dev.forEach(item => {
          if (!obj.dev[item.id]) {
            obj.dev[item.id] = 0;
          }
          obj.dev[item.id] += Number(group.dev[item.id]);
        });
        obj.userSum += Number(group.userSum);
      });
      return obj;
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('projectTeamSizeTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('projectTeamSizeTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  },

  methods: {
    ...mapMutations('userActionSaveMeasure/projectTeamSize', [
      'updateIsIncludeChildren',
      'updateSelectedGroups',
      'updateGroupsTree'
    ]),

    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('measureForm', ['queryPersonStatistics']),
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('projectTeamSizeTree', JSON.stringify(tree));
    },
    //重置整棵树：
    resetTree(tree) {
      this.updateSelectedGroups([]);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    // 下载
    downloadExcel() {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('projectTeamizeTable', '项目组规模.xlsx');
      });
      this.btnLoading = false;
    },
    async init() {
      this.loading = true;
      try {
        await this.queryPersonStatistics({
          groupIds: this.selectedGroups,
          includeChild: this.isIncludeChildren
        });
        this.tableData = this.teamSize;
        this.loading = false;
      } catch (e) {
        this.loading = false;
      }
    }
  },
  created() {
    // 获取小组
    this.fetchGroup();
    // 获取当前用户所在小组
    this.updateSelectedGroups([this.currentUser.group_id]);
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.block-style
   padding-bottom:12px
   margin-bottom:10px
tr
  td, th
    text-align center
    padding 8px
    max-width 300px
.td-width
  width 100%
  overflow hidden
  text-overflow ellipsis
  white-space: nowrap;
.table-style
  margin-top:22px
  border-collapse:collapse !important
table,tr,td{
  min-height:30px
  height:30px
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
}
.title-font
  font-family: PingFangSC-Medium;
  font-weight:600
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 36px;
  vertical-align:middle
  padding-left:12px
.icon-style
  width:16px;
  height:16px;
  line-height:36px
  vertical-align:middle
.scroll
  overflow: auto
  overflow-y hidden
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
