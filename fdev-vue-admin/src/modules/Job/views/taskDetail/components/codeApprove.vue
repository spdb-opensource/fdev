<template>
  <f-block>
    <Loading :visible="loading">
      <div>
        <fdev-table
          titleIcon="list_s_f"
          title="审批记录"
          :data="tableData"
          :columns="showColumns"
          @request="onRequest"
          :pagination.sync="pagination"
          class="my-sticky-column-table"
          no-select-cols
          no-export
        >
          <!-- 申请人 -->
          <template v-slot:body-cell-applicant_name="props">
            <fdev-td>
              <div
                class="text-ellipsis"
                :title="props.value"
                v-if="props.row.applicant"
              >
                <router-link
                  :to="{ path: `/user/list/${props.row.applicant}` }"
                  class="link"
                >
                  {{ props.value }}
                </router-link>
              </div>
              <div v-else>-</div>
            </fdev-td>
          </template>
          <!-- 申请说明 -->
          <template v-slot:body-cell-apply_desc="props">
            <fdev-td class="td-width" :title="props.value">
              <div
                class="text-ellipsis"
                :title="props.value"
                v-if="props.row.apply_desc"
              >
                {{ props.row.apply_desc }}
              </div>
              <div v-else>-</div>
            </fdev-td>
          </template>
          <!-- 审批人 -->
          <template v-slot:body-cell-auditor_name="props">
            <fdev-td>
              <div
                class="text-ellipsis"
                :title="props.value"
                v-if="props.row.auditor"
              >
                <router-link
                  :to="{ path: `/user/list/${props.row.auditor}` }"
                  class="link"
                >
                  {{ props.value }}
                </router-link>
              </div>
              <div v-else>-</div>
            </fdev-td>
          </template>
          <!-- 审批说明 -->
          <template v-slot:body-cell-result_desc="props">
            <fdev-td class="td-width" :title="props.value">
              <div
                class="text-ellipsis"
                :title="props.value"
                v-if="props.row.result_desc"
              >
                {{ props.row.result_desc }}
              </div>
              <div v-else>-</div>
            </fdev-td>
          </template>
          <!-- 审批结果 -->
          <template v-slot:body-cell-status="props">
            <fdev-td class="text-ellipsis">
              <div class="row no-wrap items-center">
                <f-status-color
                  :gradient="
                    approveStatus(props.row.status) | approveStatusFilter
                  "
                ></f-status-color>

                <span :title="approveStatus(props.row.status)">
                  {{ approveStatus(props.row.status) }}
                </span>
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
  </f-block>
</template>
<script>
import { mapActions, mapState } from 'vuex';
import Loading from '@/components/Loading';
import { codeDoneColumns } from '@/modules/Job/utils/constants';
export default {
  name: 'codeApprove',
  props: {
    taskId: String
  },
  computed: {
    ...mapState('jobForm', ['codeApproveList'])
  },
  components: { Loading },
  data() {
    return {
      tableData: [],
      loading: false,
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 1,
        page: 1
      },
      showColumns: codeDoneColumns()
    };
  },
  filters: {
    approveStatusFilter(val) {
      const obj2 = {
        待审批:
          'linear-gradient(270deg, rgba(36,200,249,0.50) 0%, #24C8F9 100%)',
        通过: 'linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%)',
        拒绝: 'linear-gradient(270deg, rgba(176,190,197,0.50) 0%, #B0BEC5 100%)'
      };
      return obj2[val];
    }
  },
  methods: {
    ...mapActions('jobForm', ['releaseApproveList']),
    approveStatus(status) {
      const obj1 = {
        0: '待审批',
        1: '通过',
        2: '拒绝'
      };
      return obj1[status];
    },
    onRequest(props) {
      const { page, rowsNumber, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsNumber = rowsNumber;
      this.pagination.rowsPerPage = rowsPerPage;
      this.queryList();
    },
    async queryList() {
      this.loading = true;
      let params = {
        pageSize: this.pagination.rowsPerPage,
        currentPage: this.pagination.page,
        status: [0, 1, 2],
        taskId: this.taskId,
        requestSource: '1'
      };
      await this.releaseApproveList(params);
      // 设置数据总条数
      this.pagination.rowsNumber = this.codeApproveList.count;
      this.tableData = this.codeApproveList.list;
      this.loading = false;
    }
  },
  created() {
    this.queryList();
  }
};
</script>
