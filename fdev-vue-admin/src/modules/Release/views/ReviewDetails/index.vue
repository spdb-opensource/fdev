<template>
  <f-block>
    <Loading>
      <fdev-card flat square class="q-pa-md bg-white">
        <fdev-card-section class="row">
          <f-formitem class="col-4 text-ellipsis" label="任务名:">
            <router-link
              :to="`/job/list/${$route.params.id}`"
              class="link text-ellipsis"
            >
              {{ reviewBasicMsg.taskName }}
            </router-link>
          </f-formitem>
          <f-formitem class="col-4" label="所属组:">
            {{
              reviewBasicMsg.stock === TaskStatus.deStock
                ? reviewBasicMsg.group
                : group
            }}
          </f-formitem>
          <f-formitem class="col-4" label="审核类型:">
            {{ reviewBasicMsg.review_type }}
          </f-formitem>
          <f-formitem class="col-4" label="发起人:">
            <router-link
              :to="{
                path: `/user/list/${reviewBasicMsg.applicant}`
              }"
              class="link"
            >
              {{ reviewBasicMsg.applicantName }}
            </router-link>
          </f-formitem>
          <f-formitem class="col-4" label="审核人:">
            <span
              v-for="item in reviewBasicMsg.reviewers"
              :key="item.cid"
              class="q-mr-sm"
            >
              <router-link
                :to="{
                  path: `/user/list/${item.cid}`
                }"
                class="link"
              >
                {{ item.name }}
              </router-link>
            </span>
          </f-formitem>
          <div class="row">
            <f-formitem
              label-style="width: 450px"
              class="col-4"
              label="相关库表变更是否涉及通知数据仓库等关联供数系统配套改造:"
            >
              {{ jobProfile.system_remould }}
            </f-formitem>
            <f-formitem
              label-style="width: 450px"
              class="col-4"
              label="是涉及在库表关联应用暂停服务期间实施数据库变更操作:"
            >
              {{ jobProfile.impl_data }}
            </f-formitem>
          </div>
        </fdev-card-section>

        <fdev-card-section class="row justify-center">
          <div class="inline-block">
            <fdev-btn
              @click="handleReveiw"
              label="审核"
              :disable="
                (currentStatus === '通过' ||
                currentStatus === '初审拒绝' ||
                currentStatus === '复审拒绝'
                  ? true
                  : false) || examineBtnDisable
              "
            />
            <fdev-tooltip
              anchor="top middle"
              self="center middle"
              v-if="
                currentStatus === '待审核' ||
                  currentStatus === '初审拒绝' ||
                  currentStatus === '复审拒绝'
              "
            >
              {{
                reviewBasicMsg.applicant === currentUser.id
                  ? '请移步至任务处理页面或数据库变更审核页面发起审核'
                  : `请联系${calApplicant}发起审核`
              }}
            </fdev-tooltip>

            <fdev-tooltip
              anchor="top middle"
              self="center middle"
              v-if="currentStatus === '复审中' && !isSecondReviewer"
            >
              请联系相关复审人进行审核
            </fdev-tooltip>
            <fdev-tooltip
              anchor="top middle"
              self="center middle"
              v-if="
                currentStatus === '初审中' &&
                  reviewBasicMsg.reviewers[0].cid !== currentUser.id
              "
            >
              请联系{{
                reviewBasicMsg.reviewers[reviewBasicMsg.reviewers.length - 1]
                  ? reviewBasicMsg.reviewers[
                      reviewBasicMsg.reviewers.length - 1
                    ].name
                  : '指定负责人'
              }}进行审核
            </fdev-tooltip>
          </div>
          <fdev-btn
            @click="addComment"
            label="补充审核意见"
            v-if="examSecReview"
            class="q-ml-md"
          />
        </fdev-card-section>
      </fdev-card>
      <fdev-card flat square class="q-mt-md bg-white">
        <fdev-tabs class="q-mb-md" v-model="tab" align="left">
          <fdev-tab name="history" label="审核记录" />
        </fdev-tabs>

        <fdev-separator />
        <fdev-timeline layout="comfortable" v-if="!!reviewRecord.length">
          <fdev-timeline-entry
            side="left"
            v-for="item in reviewRecord"
            :key="item.id"
          >
            <template v-slot:subtitle>
              时间： {{ formatTime(item.review_time) }}
            </template>
            <template v-slot:title>
              <span>{{ item.review_status }}</span>
            </template>
            <div>
              <p v-if="!!item.db_type">数据库类型：{{ item.db_type }}</p>
              <div v-if="!!item.auditor">
                <p>
                  审核人：
                  <router-link
                    :to="{
                      path: `/user/list/${item.auditor.id}`
                    }"
                    class="link"
                  >
                    {{ item.auditor.auditor_name_cn }}
                  </router-link>
                </p>
                <div>
                  <p class="left-80">审核意见：</p>
                  <div v-html="item.doc" class="right"></div>
                </div>
              </div>
              <div v-else-if="!!item.opno">
                <p>
                  操作人：
                  <router-link
                    :to="{
                      path: `/user/list/${item.opno.id}`
                    }"
                    class="link"
                  >
                    {{ item.opno.opno_name_cn }}
                  </router-link>
                </p>
                <div>
                  <p class="left-100">补充审核意见：</p>
                  <div v-html="item.doc" class="right"></div>
                </div>
              </div>
              <div v-else>
                <p>
                  申请人：
                  <router-link
                    :to="{
                      path: `/user/list/${reviewBasicMsg.applicant}`
                    }"
                    class="link"
                  >
                    {{ reviewBasicMsg.applicantName }}
                  </router-link>
                </p>
                <div>
                  <p class="left-80">申请内容：</p>
                  <div v-html="item.doc" class="right"></div>
                </div>
              </div>
            </div>
          </fdev-timeline-entry>
        </fdev-timeline>
        <div v-else class="q-pb-sm q-pt-sm q-pl-sm">
          暂无审核记录
        </div>
        <div class="text-center q-pb-lg">
          <fdev-btn
            type="button"
            label="返回"
            @click="goBack"
            color="primary"
            text-color="white"
          />
        </div>
      </fdev-card>
      <f-dialog right v-model="addDialog" title="请输入审核补充意见">
        <fdev-editor
          class="editor-width"
          v-model="addCommentHtml"
        ></fdev-editor>
        <template v-slot:btnSlot>
          <fdev-btn label="确定" dialog @click="saveAddComment" />
        </template>
      </f-dialog>

      <f-dialog right v-model="examineDialogOpened" title="数据库审核">
        <div class="bg-white q-pa-md" v-if="dataBaseAlter">
          <a
            v-for="(file, key) in dataBaseAlter"
            :key="file.id"
            class="link file-name"
            @click="download(file)"
            target="_blank"
          >
            {{ key }}
          </a>
        </div>

        <div class="bg-white q-pa-md" v-else>暂无数据库审核文件</div>

        <template v-slot:btnSlot>
          <fdev-btn label="拒绝" color="red" dialog @click="refused('拒绝')" />
          <fdev-btn label="通过" dialog @click="refused('通过')" />
        </template>
      </f-dialog>

      <f-dialog right v-model="okDialog" title="请输入通过理由">
        <fdev-editor
          class="editor-width"
          v-model="reviewReason"
          ref="reviewReason"
          :rules="[() => $v.reviewReason.required || '通过理由不能为空']"
        ></fdev-editor>

        <template v-slot:btnSlot>
          <fdev-btn
            label="确定"
            :loading="globalLoading['jobForm/saveReviewRecord']"
            @click="reviewConfirm('通过')"
            dialog
          />
        </template>
      </f-dialog>
      <f-dialog right v-model="refuseDialog" title="请输入拒绝理由">
        <fdev-editor class="editor-width" v-model="reviewReason"></fdev-editor>
        <template v-slot:btnSlot>
          <fdev-btn
            label="确定"
            :loading="globalLoading['jobForm/saveReviewRecord']"
            @click="reviewConfirm('拒绝')"
            dialog
          />
        </template>
      </f-dialog>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters } from 'vuex';
import { errorNotify } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import { successNotify } from '@/utils/utils';
import { TaskStatus } from '../../utils/model.js';

export default {
  components: { Loading },
  data() {
    return {
      TaskStatus,
      reviewType: '',
      group: '',
      tab: 'history',
      startPerson: '',
      currentJob: {},
      taskName: '',
      addDialog: false,
      addCommentHtml: '',
      examineDialogOpened: false,
      detail: {},
      okDialog: false,
      refuseDialog: false,
      reviewReason: ''
    };
  },
  validations: {
    reviewReason: {
      required
    }
  },
  computed: {
    ...mapState('jobForm', [
      'reviewRecord',
      'secondReviewers',
      'reviewRecordDetail',
      'jobProfile'
    ]),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('releaseForm', ['reviewBasicMsg']),
    ...mapState('user', ['currentUser']),
    calApplicant() {
      const applicant = this.reviewBasicMsg.applicantName
        ? this.reviewBasicMsg.applicantName
        : '任务负责人';
      return applicant;
    },
    examSecReview() {
      const examRole = this.secondReviewers.some(
        user => user.id === this.currentUser.id
      );
      const review_status = this.reviewRecord[0]
        ? this.reviewRecord[0].review_status
        : '';
      return (
        (examRole || this.isKaDianManager) &&
        (review_status === '复审中' || review_status === '复审拒绝')
      );
    },
    currentStatus() {
      if (this.reviewRecord.length === 0) {
        return '待审核';
      } else {
        return this.reviewRecord[0].review_status;
      }
    },
    isSecondReviewer() {
      return (
        !!this.secondReviewers.find(user => user.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    //是否任务负责人人
    isApplicant() {
      return this.reviewBasicMsg.applicant === this.currentUser.id;
    },
    isManager() {
      return this.currentUser.role.some(role => role.label === 'DBA审核人');
    },
    examineBtnDisable() {
      const reviewStatus = this.currentStatus;
      if (reviewStatus == '通过') {
        return true;
      }
      if (this.isKaDianManager && reviewStatus != '通过') {
        return false;
      }
      // if(当前用户id在复审人列表中) return false
      const index = this.secondReviewers.findIndex(
        reviewer => reviewer.id === this.currentUser.id
      );
      if (!this.isManager) return true;
      if (!this.reviewBasicMsg.reviewers) return;
      const isFirstReviewer = this.reviewBasicMsg.reviewers.find(
        user => user.cid === this.currentUser.id
      );
      if (reviewStatus !== '初审中' && reviewStatus !== '复审中') {
        return true;
      }
      if (
        reviewStatus === '初审中' &&
        (this.isKaDianManager || !!isFirstReviewer)
      ) {
        return false;
      }
      if (reviewStatus === '初审中' && index > -1) {
        return true;
      }
      if (reviewStatus === '复审中' && (this.isKaDianManager || index > -1)) {
        return false;
      } else {
        return true;
      }
    },
    dataBaseAlter() {
      const { data_base_alter } = this.reviewRecordDetail;
      if (!data_base_alter) return null;
      const { files } = data_base_alter[0];
      return Object.keys(files).length === 0 ? null : files;
    }
  },
  methods: {
    ...mapActions('jobForm', [
      'queryReviewRecordHistory',
      'getJobUser',
      'addReviewIdea',
      'queryReviewRecordStatus',
      'saveReviewRecord',
      'queryJobProfile',
      'downExcel'
    ]),
    ...mapActions('releaseForm', ['queryReviewBasicMsg', 'updateReviewRecord']),
    formatTime(time) {
      return time.replace(/-/g, '.');
    },
    addComment() {
      this.addDialog = true;
    },
    async saveAddComment() {
      if (this.addCommentHtml.trim() === '') {
        errorNotify('请输入补充意见!');
        return;
      }
      await this.addReviewIdea({
        task_id: this.$route.params.id,
        doc: this.addCommentHtml,
        review_status: this.reviewRecord[0].review_status,
        opno: {
          id: this.currentUser.id,
          opno_name_cn: this.currentUser.user_name_cn
        }
      });
      this.addDialog = false;
      this.queryReviewRecordHistory({ task_id: this.$route.params.id });
    },
    async handleReveiw() {
      await this.queryReviewRecordStatus({
        id: this.$route.params.id
      });
      this.examineDialogOpened = true;
      this.detail = this.reviewRecordDetail;
      delete this.detail.task_id;
      delete this.detail.auditResult;
    },
    async reviewConfirm(type) {
      let status = '',
        id = this.$route.params.id;
      if (type === '通过') {
        status = this.currentStatus === '初审中' ? '复审中' : '通过';
      } else {
        status = this.currentStatus === '初审中' ? '初审拒绝' : '复审拒绝';
      }
      if (!this.reviewReason.trim()) {
        errorNotify('请输入审核通过或拒绝的理由');
        return;
      }
      await this.saveReviewRecord({
        task_id: id,
        doc: this.reviewReason,
        review_status: status,
        auditor: {
          id: this.currentUser.id,
          auditor_name_cn: this.currentUser.user_name_cn
        }
      });
      this.refuseDialog = false;
      this.okDialog = false;
      this.examineDialogOpened = false;
      this.update(status);
    },
    async update(type) {
      await this.updateReviewRecord({
        id: this.$route.params.id,
        reviewStatus: type,
        reviewIdea: this.reviewReason
      });
      successNotify('已审核');
      this.reviewReason = '';
      await this.queryReviewRecordHistory({ task_id: this.$route.params.id });
    },
    async init() {
      this.group = this.$route.query.groupFullName;
      await this.queryReviewBasicMsg({ taskId: this.$route.params.id });
      await this.queryReviewRecordHistory({ task_id: this.$route.params.id });
      await this.getJobUser();
      await this.queryJobProfile({ id: this.$route.params.id });
    },
    refused(type) {
      if (type === '通过') {
        this.okDialog = true;
      } else {
        this.refuseDialog = true;
      }
    },
    async download(file) {
      let param = {
        path: file,
        moduleName: 'fdev-task'
      };
      await this.downExcel(param);
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.left-100
  float left
  width 100px
.left-80
  float left
  width 70px
.right
  overflow hidden
  max-width 500px
.file-name
  display block
  font-weight 700
  font-size 16px
  line-height: 27px;
.editor-width
  max-width 432px
</style>
