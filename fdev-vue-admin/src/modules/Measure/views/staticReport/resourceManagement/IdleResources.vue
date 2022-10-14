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
    <!-- 表格数据 -->
    <f-block page
      ><Loading :visible="loading">
        <!-- 标题 -->
        <div class="row">
          <!-- 标题 -->
          <div>
            <f-icon name="list_s_f" class="text-primary icon-style" />
            <span class="title-font">项目组资源闲置情况</span>
          </div>
          <fdev-space />
          <fdev-btn
            ficon="download"
            label="下载"
            normal
            @click="downloadExcel"
            :loading="btnLoading"
            :disable="tableData.length === 0"
          />
        </div>
        <div class="q-pb-llg">
          <!-- 表格 -->
          <div v-if="tableData.length !== 0" class="scroll">
            <table
              width="100%"
              border="1"
              bordercolor=" #DDDDDD"
              cellspacing="0"
              cellpadding="0"
              id="IdleResourcesTable"
              class="table-style"
            >
              <tr v-for="(item, index) in tableData" :key="index">
                <td v-for="(i, index) in item" :key="index">
                  {{ i }}
                </td>
              </tr>
            </table>

            <!-- 说明 -->
            <div class="tip-style q-mb-md">
              说明：X/Y/Z的含义，单位总人数/开发任务为0的人数/涉及任务数为0的人数
              （涉及：凡参与开发、测试、牵头等任一角色均为涉及）
            </div>
          </div>

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
export default {
  name: 'IdleResources',
  components: { Loading },
  data() {
    return {
      loading: false,
      groupTreeData: [], //树
      tableData: [],
      btnLoading: false
    };
  },
  computed: {
    ...mapState('userActionSaveMeasure/fdevTaskSum', [
      'isIncludeChildren',
      'selectedGroups',
      'groupsTree'
    ]),
    ...mapGetters('userActionSaveMeasure/fdevTaskSum', ['groupData']),
    ...mapGetters('userForm', ['groupsTree']),
    ...mapState('measureForm', ['personFreeStatistics']),
    ...mapState('user', ['currentUser'])
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('fdevTaskSumTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('fdevTaskSumTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/fdevTaskSum', [
      'updateIsIncludeChildren',
      'updateSelectedGroups',
      'updateGroupsTree'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('measureForm', ['queryPersonFreeStatistics']),
    //选择组方法
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('fdevTaskSumTree', JSON.stringify(tree));
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
        excel.export_table_to_excel(
          'IdleResourcesTable',
          '项目组资源闲置情况.xlsx'
        );
      });
      this.btnLoading = false;
    },
    async init() {
      this.loading = true;
      try {
        await this.queryPersonFreeStatistics({
          groupIds: this.selectedGroups,
          includeChild: this.isIncludeChildren
        });
        this.tableData = this.personFreeStatistics;
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
.tip-style
  font-family: PingFangSC-Regular;
  font-size: 12px;
  color: #666666;
  line-height: 12px;
  padding-top:16px
.scroll
  overflow:auto
  overflow-y:hidden
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
