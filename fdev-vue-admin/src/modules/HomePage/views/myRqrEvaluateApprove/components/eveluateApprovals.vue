<template
  ><Loading>
    <!-- 审批列表 -->
    <fdev-table
      title-icon="todo_list_s_f"
      title="需求评估定稿日期审批列表"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      selection="multiple"
      :selected.sync="rowSelected"
      @request="pageTableRequest"
      :on-search="findMyApproveList"
      class="my-sticky-column-table"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
      row-key="id"
      no-export
      ><template #top-right>
        <!-- 查询全部 -->
        <span>
          <fdev-btn normal label="查询全部" @click="jumpToApproveList" />
        </span>
        <!-- 批量审批 -->
        <span>
          <fdev-tooltip v-if="rowSelected.length === 0" position="top">
            请先勾选需要审批的需求
          </fdev-tooltip>
          <fdev-btn
            :disable="rowSelected.length === 0"
            normal
            label="批量审批"
            :loading="allAprovePassLoading"
            @click="allAprovePass"
          /> </span></template
      ><template v-slot:top-bottom
        ><f-formitem label="搜索条件">
          <fdev-input
            :value="terms"
            placeholder="请输入需求编号/内容"
            clearable
            @input="saveTerms($event)"
            @keyup.enter="findMyApproveList()"
            @clear="clearTerms()"
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
      </template>
      <!-- 操作列 -->
      <template v-slot:body-cell-operation="props">
        <fdev-td :auto-width="true" class="td-padding">
          <div class="border-right">
            <div class="inline-block" style="display: inline-block;">
              <fdev-btn
                flat
                label="审批通过"
                class="q-mr-sm"
                @click="clickApprovePass(props.row)"
              />
            </div>
            <div class="inline-block" style="display: inline-block;">
              <fdev-btn
                flat
                label="审批拒绝"
                class="q-mr-sm"
                @click="clickApproveReject(props.row)"
              />
            </div>
          </div>
        </fdev-td> </template
    ></fdev-table>
    <!-- 拒绝审批原因弹窗 -->
    <f-dialog title="审批拒绝" f-sc v-model="rejectOpen">
      <f-formitem label="审批拒绝说明" diaS required>
        <fdev-input
          v-model="approveRejectReason"
          clearable
          autofocus
          type="textarea"
          :rules="[val => !!val || '拒绝原因不能为空']"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="rejectOpen = false"/>
        <fdev-btn
          label="确定"
          dialog
          @click="approveRejectsure"
          :loading="btnloading"/></template
    ></f-dialog>
  </Loading>
</template>
<script>
import { successNotify, resolveResponseError } from '@/utils/utils';
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import { eveluateApprovalColumns } from '@/modules/HomePage/utils/constants';
import { refuse, agree } from '@/modules/Rqr/services/methods';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'unitApprovals',
  props: ['label', 'name'],
  components: {
    Loading
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentTodoPage(val);
      },
      deep: true
    }
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
  validations: {
    approveRejectReason: {
      required
    }
  },
  data() {
    return {
      loading: false,
      tableData: [],
      columns: eveluateApprovalColumns(),
      rowSelected: [],
      pagination: this.currentTodoPage,
      allAprovePassLoading: false,
      rejectOpen: false,
      rejectId: '',
      eventTodoNum: 0,
      eventNum: {
        todo: 0,
        done: 0
      },
      approveRejectReason: '',
      btnloading: false
    };
  },
  computed: {
    ...mapState('userActionSaveHomePage/myEveluatePage', [
      'terms',
      'currentTodoPage',
      'visibleCols'
    ]),
    ...mapState('demandsForm', ['myApprovalList', 'myRqrProvalCount'])
  },
  methods: {
    ...mapMutations('userActionSaveHomePage/myEveluatePage', [
      'saveTerms',
      'saveVisibleColumns',
      'saveCurrentTodoPage'
    ]),
    ...mapActions('demandsForm', ['queryMyList', 'queryCount']),
    // 审批状态
    approveStatus(status) {
      const obj1 = {
        undetermined: '待审批',
        agree: '通过',
        disagree: '拒绝'
      };
      return obj1[status];
    },
    //清空搜索条件
    clearTerms() {
      this.saveTerms('');
      this.init();
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.init();
    },
    //查询数据
    async init() {
      this.loading = true;
      // 送参
      let params = {
        pageSize: this.pagination.rowsPerPage,
        pageNum: this.pagination.page,
        oaContactNameNo: this.terms,
        status: 'undetermined'
      };
      //查询列表数据
      Promise.all([this.queryMyList(params), this.queryCount()]).then(() => {
        // 设置数据总条数
        this.pagination.rowsNumber = this.myApprovalList.count;
        // 表格数据
        this.tableData = this.myApprovalList.approveList;
        // 待审批条数
        this.eventNum.todo = this.myRqrProvalCount.waitCount;
        // 已完成条数
        this.eventNum.done = this.myRqrProvalCount.doneCount;
        this.$emit('input', this.eventNum);
        this.loading = false;
      });
    },
    // 列表查询
    findMyApproveList() {
      this.init();
    },
    //查询全部跳转到定稿日期审核列表
    jumpToApproveList() {
      this.$router.push('/rqrmn/rqrEvaluateMgt');
    },
    // 批量审批需求定稿
    allAprovePass() {
      this.$q
        .dialog({
          title: `批量审批通过确认`,
          message: `是否确认需求定稿日期批量审批通过？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          try {
            this.allAprovePassLoading = true;
            await resolveResponseError(() =>
              agree({
                ids: this.rowSelected.map(item => item.id)
              })
            );
            successNotify('批量审批通过!');
            this.allAprovePassLoading = false;
            //刷新列表
            this.init();
          } catch (er) {
            this.allAprovePassLoading = false;
          }
        });
    },
    //需求定稿审批通过
    async clickApprovePass(row) {
      this.$q
        .dialog({
          title: `需求定稿日期审批通过确认`,
          message: `是否确认定稿日期审批通过？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          let rowId = [];
          rowId.push(row.id);
          await resolveResponseError(() => agree({ ids: rowId }));
          successNotify('审批通过!');
          //刷新列表
          this.init();
        });
    },
    //点击拒绝审批按钮
    clickApproveReject(row) {
      this.rejectOpen = true;
      this.rejectId = row.id;
    },
    //填写确认拒绝原因并拒绝
    async approveRejectsure() {
      // 没填写确认拒绝原因不发接口
      if (this.$v.approveRejectReason.$invalid) {
        return;
      }
      try {
        this.btnloading = true;
        await resolveResponseError(() =>
          refuse({
            id: this.rejectId,
            state: this.approveRejectReason
          })
        );
        this.rejectOpen = false;
        this.btnloading = false;
        successNotify('审批拒绝成功!');
        //刷新列表
        this.init();
      } catch (err) {
        this.btnloading = false;
      }
    }
  },
  created() {
    this.saveTerms('');
    this.pagination = this.currentTodoPage;
    this.init();
  },
  mounted() {
    const tempVisibleColumns = this.visibleCols;
    if (!this.visibleCols || this.visibleCols.length <= 1) {
      this.saveVisibleColumns(tempVisibleColumns);
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
