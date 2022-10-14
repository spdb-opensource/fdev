<template>
  <Loading :visible="loading">
    <fdev-table
      title-icon="check_s_f"
      title="审批已完成列表"
      class="my-sticky-column-table"
      :visible-columns="visibleColsDone"
      :onSelectCols="saveVisibleColumnsDone"
      @request="pageTableRequest"
      :on-search="findMyApproveList"
      :pagination.sync="pagination"
      :data="tableData"
      :columns="columns"
      row-key="id"
      no-export
      ><template #top-right>
        <span>
          <fdev-btn normal label="查询全部" @click="jumpToApproveList" />
        </span> </template
      ><template v-slot:top-bottom>
        <f-formitem label="搜索条件">
          <fdev-input
            :value="keywords"
            placeholder="请输入需求编号/内容"
            clearable
            @input="saveKeywords($event)"
            @keyup.enter="findMyApproveList()"
            @clear="clearKeywords()"
            ><template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="findMyApproveList"
              />
            </template>
          </fdev-input> </f-formitem
      ></template>
      <!-- 需求名称 -->
      <template v-slot:body-cell-oa_contact_name="props">
        <fdev-td :title="props.row.oa_contact_name">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.access_id && props.row.oa_contact_name"
              :to="`/rqrmn/rqrEvaluateMgt/${props.row.access_id}`"
              class="link"
            >
              {{ props.row.oa_contact_name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.oa_contact_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.oa_contact_name || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 申请人 -->
      <template v-slot:body-cell-apply_user="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.apply_user_id"
                :to="{ path: `/user/list/${props.row.apply_user_id}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 申请原因 -->
      <template v-slot:body-cell-apply_reason="props">
        <fdev-td :title="props.row.apply_reason">
          <div class="text-ellipsis">
            <span
              >{{ props.row.apply_reason || '-'
              }}<fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.apply_reason }}
                </fdev-banner>
              </fdev-popup-proxy></span
            >
          </div>
        </fdev-td>
      </template>
      <!-- 拒绝原因 -->
      <template v-slot:body-cell-state="props">
        <fdev-td :title="props.row.state">
          <div class="text-ellipsis">
            <span
              >{{ props.row.state || '-'
              }}<fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.state }}
                </fdev-banner>
              </fdev-popup-proxy></span
            >
          </div>
        </fdev-td>
      </template>
      <!-- 审批人 -->
      <template v-slot:body-cell-operate_user="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.operate_user_id"
                :to="{ path: `/user/list/${props.row.operate_user_id}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 审批状态 -->
      <template v-slot:body-cell-operate_status="props">
        <fdev-td class="text-ellipsis">
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="
                approveStatus(props.row.operate_status) | approveStatusFilter
              "
            ></f-status-color>

            <span :title="approveStatus(props.row.operate_status)">
              {{ approveStatus(props.row.operate_status) }}
            </span>
          </div>
        </fdev-td>
      </template></fdev-table
    ></Loading
  >
</template>
<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { doneApprovalColumns } from '@/modules/HomePage/utils/constants';
export default {
  components: { Loading },
  props: ['label', 'name'],
  computed: {
    ...mapState('userActionSaveHomePage/myEveluatePage', [
      'keywords',
      'visibleColsDone',
      'currentDonePage'
    ]),
    ...mapState('demandsForm', ['myApprovalList', 'myRqrProvalCount'])
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
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentDonePage(val);
      },
      deep: true
    }
  },
  data() {
    return {
      tableData: [],
      loading: false,
      columns: doneApprovalColumns(),
      pagination: this.currentDonePage,
      eventDoneNum: 0,
      eventNum: {
        todo: 0,
        done: 0
      }
    };
  },
  methods: {
    ...mapMutations('userActionSaveHomePage/myEveluatePage', [
      'saveKeywords',
      'saveCurrentDonePage',
      'saveVisibleColumnsDone'
    ]),
    ...mapActions('demandsForm', ['queryMyList', 'queryCount']),
    approveStatus(status) {
      const obj1 = {
        undetermined: '待审批',
        agree: '通过',
        disagree: '拒绝'
      };
      return obj1[status];
    },
    // 清空搜索条件
    clearKeywords() {
      this.saveKeywords('');
      this.init();
    },
    //查询全部跳转到定稿日期审核列表
    jumpToApproveList() {
      this.$router.push('/rqrmn/rqrEvaluateMgt');
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.init();
    },
    findMyApproveList() {
      this.init();
    },
    async init() {
      this.loading = true;
      // 上送参数
      let params = {
        pageSize: this.pagination.rowsPerPage, //页码
        pageNum: this.pagination.page,
        oaContactNameNo: this.keywords,
        status: 'approve'
      };
      Promise.all([
        // 获取数据
        this.queryMyList(params),
        this.queryCount()
      ]).then(() => {
        // 设置数据总条数
        this.pagination.rowsNumber = this.myApprovalList.count;
        // 列表数据
        this.tableData = this.myApprovalList.approveList;
        // 待审批条数
        this.eventNum.todo = this.myRqrProvalCount.waitCount;
        // 已完成条数
        this.eventNum.done = this.myRqrProvalCount.doneCount;
        this.$emit('input', this.eventNum);
        this.loading = false;
      });
    }
  },
  created() {
    this.saveKeywords('');
    this.pagination = this.currentDonePage;
    this.init();
  },
  mounted() {
    const tempVisibleColumns = this.visibleColsDone;
    if (!this.visibleCols || this.visibleColsDone.length <= 1) {
      this.saveVisibleColumnsDone(tempVisibleColumns);
    }
  }
};
</script>
<style lang="stylus" scoped>
.border-right button:after
  content: '';
  border-right: 1px solid #DDDDDD;
  display: inline-block;
  height: 14px;
  width: 1px;
  position: absolute;
  right: -5px;
  top: 11px;
.border-right .inline-block:last-child button:after
  display:none !important
</style>
