<template>
  <f-block>
    <Loading :visible="testResultLoading">
      <div>
        <f-icon
          name="log_s_f"
          class="text-primary"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">当前任务缺陷情况</span>
      </div>
      <Count :defectList="isTest === '1' ? defectList : []" :fromApp="false" />
      <div class="q-mt-llg">
        <Defect
          :jobName="taskName"
          :defectList="defectList"
          :isLoginUserList="isLoginUserList"
          @update-status="updateStatus"
          @update-sourcestatus="updateSourceStatus"
          @assignUser="assignUser($event)"
          @init="getTestResult"
          :userNameLoading="globalLoading['user/updateAssignUser']"
          :needSelect="false"
        />
      </div>
      <div>
        <fdev-table
          titleIcon="log_s_f"
          title="当前任务SIT测试执行情况"
          :data="moduleData"
          :columns="planColumns"
          @request="onRequest"
          :pagination.sync="pagination"
          class="my-sticky-column-table q-mt-md"
          no-select-cols
          no-export
        >
        </fdev-table>
      </div>
    </Loading>
  </f-block>
</template>
<script>
import Defect from '@/components/Defect';
import Loading from '@/components/Loading';
import { successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import Count from '@/components/Count';
export default {
  name: 'testResult',
  components: {
    Loading,
    Count,
    Defect
  },
  computed: {
    ...mapState('jobForm', ['defectList', 'testDetail']),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('global', {
      globalLoading: 'loading'
    })
  },
  props: {
    taskId: String,
    isTest: String,
    taskName: String
  },
  data() {
    return {
      testResultLoading: false,
      moduleData: [],
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 1,
        page: 1
      },
      planColumns: [
        {
          name: 'testplanName',
          label: '测试计划名称',
          field: 'testplanName'
        },
        {
          name: 'allCount',
          label: '测试案例总数',
          field: 'allCount'
        },
        {
          name: 'allPassed',
          label: '执行通过数',
          field: 'allPassed'
        },
        {
          name: 'allFailed',
          label: '执行计划失败数',
          field: 'allFailed'
        },
        {
          name: 'allBlocked',
          label: '执行案例阻碍数',
          field: 'allBlocked'
        }
      ]
    };
  },
  created() {
    this.init();
  },
  methods: {
    ...mapActions('jobForm', ['queryFtaskMantis', 'queryTestDetail']),
    ...mapActions('user', ['updateFdevMantis', 'updateAssignUser']),
    async getTestResult() {
      this.testResultLoading = true;
      let id = this.taskId;
      this.drewPageBySitTest(id);
      try {
        await this.queryFtaskMantis({ id });
        this.testResultLoading = false;
      } catch (e) {
        this.testResultLoading = false;
      }
    },
    onRequest(props) {
      const { page, rowsNumber, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsNumber = rowsNumber;
      this.pagination.rowsPerPage = rowsPerPage;
      this.drewPageBySitTest(this.taskId);
    },
    //渲染测试执行计划情况
    async drewPageBySitTest(id) {
      this.testResultLoading = true;
      await this.queryTestDetail({
        id: id,
        pageSize: this.pagination.rowsPerPage,
        pageNum: this.pagination.page
      });
      this.testResultLoading = false;
      this.moduleData = this.testDetail.testPlan
        ? this.testDetail.testPlan
        : [];
      this.pagination.rowsNumber = this.testDetail.count;
    },
    async updateStatus(defect) {
      let id = '';
      if (this.$route.params.id) {
        id = this.$route.params.id;
      } else {
        id = defect.task_id;
      }
      this.testResultLoading = true;
      await this.updateFdevMantis(defect);
      successNotify('修改成功');
      await this.queryFtaskMantis({ id });
      this.testResultLoading = false;
    },
    async updateSourceStatus(defect) {
      const id = this.$route.params.id;
      this.testResultLoading = true;
      await this.updateFdevMantis(defect);
      successNotify('修改缺陷来源成功');
      await this.queryFtaskMantis({ id });
      this.testResultLoading = false;
    },
    async assignUser(defect) {
      await this.updateAssignUser(defect);
      let params = {
        userList: [
          {
            user_name_cn: this.currentUser.user_name_cn,
            user_name_en: this.currentUser.user_name_en
          }
        ],
        includeCloseFlag: '0'
      };
      await this.queryFuserMantis(params);
      // 刷新 SIT缺陷列表
      const id = this.$route.params.id;
      await this.queryFtaskMantis({ id });
    },
    init() {
      this.getTestResult();
    }
  }
};
</script>
<style lang="stylus" scoped>
.infoStyle {
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight: 600;
  margin-left: 8px;
}
</style>
