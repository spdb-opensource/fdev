<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    :title="isAdd ? '新增' : '编辑'"
  >
    <f-formitem diaS label="所属组">
      <fdev-select
        use-input
        disable
        v-model="reviewData.group"
        :rules="[() => true]"
      />
    </f-formitem>
    <f-formitem diaS label="申请人">
      <fdev-select
        use-input
        disable
        hide-dropdown-icon
        v-model="reviewData.applicantName"
        :rules="[() => true]"
      />
    </f-formitem>
    <f-formitem diaS label="任务名">
      <fdev-input
        :disable="!isAdd"
        v-model="$v.reviewData.taskName.$model"
        ref="reviewData.taskName"
        type="text"
        :rules="[() => $v.reviewData.taskName.required || '请输入任务名']"
      />
    </f-formitem>
    <f-formitem diaS label="投产日期">
      <f-date
        mask="YYYY/MM/DD"
        :options="timeOptions"
        v-model="reviewData.plan_fire_time"
        :rules="[
          () => $v.reviewData.plan_fire_time.required || '请选择投产日期'
        ]"
      />
    </f-formitem>
    <f-formitem diaS label="数据库类型">
      <fdev-select
        :disable="!isAdd"
        v-model="$v.reviewData.dbType.$model"
        :options="dataTypeOptions"
        clearable
        ref="reviewDataType"
        :rules="[() => $v.reviewData.dbType.required || '请选择数据库类型']"
      />
    </f-formitem>
    <f-formitem
      diaS
      :label="
        reviewData.reviewStatus === AuditStatus.seconedReviewReject
          ? '复审人'
          : '指派初审人'
      "
    >
      <fdev-select
        :disable="reviewData.reviewStatus === AuditStatus.seconedReviewReject"
        use-input
        v-model="$v.reviewData.reviewers.$model"
        :options="firstReviewOptionsCopy"
        option-value="id"
        option-label="name"
        map-options
        emit-value
        ref="reviewDataFirst"
        @filter="userFilter"
        @input="handleFirstReviewer"
        :rules="[() => $v.reviewData.reviewers.required || '请指派初审人']"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label :title="scope.opt.user_name_cn">
                {{ scope.opt.user_name_cn }}
              </fdev-item-label>
              <fdev-item-label caption :title="scope.opt.user_name_en">
                {{ scope.opt.user_name_en }}
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
    </f-formitem>
    <f-formitem diaS label="申请描述">
      <fdev-input
        v-model="reviewData.reason"
        ref="reviewData.reason"
        type="textarea"
        :rules="[() => $v.reviewData.reason.required || '请输入申请描述']"
      />
    </f-formitem>
    <f-formitem diaS label="上传文件">
      <div class="text-left">
        <p class="text-grey-7" v-show="reviewData.docInfo.length === 0">
          暂未选择文件
        </p>
        <div
          v-for="file in reviewData.docInfo"
          :key="file.name"
          class="q-pb-sm"
        >
          <p class="file-wrapper">{{ file.name }}</p>
          <f-icon name="close" @click="deleteFiles(file)" />
        </div>
      </div>

      <div class="q-mt-md bg-white">
        <fdev-btn
          :label="reviewData.docInfo.length > 0 ? '重新选择' : '选择文件'"
          flat
          @click="openFiles"
        />
      </div>
      <div class="text-grey-7">
        注意事项：上传文件必须为zip类型;
      </div>
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        :label="isAdd ? '确定' : '重新审核'"
        @click="validations"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import moment from 'moment';
import { createExamineModel, AuditStatus, TaskStatus } from '../utils/model.js';

import { validate, errorNotify } from '@/utils/utils';
export default {
  name: 'ExamineDialog',
  data() {
    return {
      TaskStatus,
      stock: TaskStatus.deStock,
      change: false, // 判断用户编辑任务时，是否重新选择上传文件
      dataTypeOptions: ['informix', 'mysql', 'oracle'],
      reviewData: createExamineModel(),
      AuditStatus,
      firstReviewOptions: [],
      firstReviewOptionsCopy: []
    };
  },
  validations: {
    reviewData: {
      dbType: {
        required(val) {
          // 编辑页面的“数据库类型”为非必需
          return !(this.isAdd && !val);
        }
      },
      reviewers: {
        required
      },
      plan_fire_time: {
        required
      },
      taskName: {
        required
      },
      reason: {
        required(val) {
          // 编辑页面的“审核理由”为非必需
          return !(this.isAdd && !val);
        }
      }
    }
  },
  props: {
    value: {
      type: Boolean
    },
    isAdd: {
      type: Boolean,
      default: true
    },
    confirm: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('user', ['currentUser'])
  },
  methods: {
    ...mapActions('releaseForm', ['addReview']),
    ...mapActions('user', ['fetch']),
    //把上传内容转成base64编码
    getBase64(file) {
      return new Promise(function(resolve, reject) {
        let reader = new FileReader();
        let imgResult = '';
        reader.readAsDataURL(file);
        reader.onload = function() {
          imgResult = reader.result;
        };
        reader.onerror = function(error) {
          reject(error);
        };
        reader.onloadend = function() {
          resolve(imgResult);
        };
      });
    },
    handleFirstReviewer(val) {
      if (val === this.currentUser.id) {
        this.reviewData.reviewers = '';
        errorNotify('系统内初审人不能与数据库变更申请人是同一人！');
      }
    },
    // 模糊搜索‘指派初审人’
    userFilter(val, update, abort) {
      update(() => {
        this.firstReviewOptionsCopy = this.firstReviewOptions.filter(
          user =>
            user.name.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    // 日期选项
    timeOptions(date) {
      const now = moment(new Date()).format('YYYY/MM/DD');
      return date >= now;
    },
    validations() {
      this.$v.reviewData.$touch();
      const keys = Object.keys(this.$refs).filter(item =>
        item.includes('reviewData')
      );
      validate(
        keys.map(key => {
          return this.$refs[key];
        })
      );

      if (this.$v.reviewData.$invalid) {
        return;
      }

      this.handleAddExamineDialog();
    },
    // 打开文件选择界面
    openFiles() {
      const input = document.createElement('input');
      input.setAttribute('type', 'file');
      input.onchange = file => this.uploadFile(input);
      input.click();
    },
    // 上传文件
    uploadFile({ files }) {
      this.change = true;
      this.reviewData.docInfo = Array.from(files);
    },
    // 删除上传的文件
    deleteFiles(file) {
      this.reviewData.docInfo = this.reviewData.docInfo.filter(
        item => item.name !== file.name
      );
    },
    confirmToClose(e) {
      if (this.confirm) {
        this.$q
          .dialog({
            title: '关闭弹窗',
            message: '关闭弹窗后数据将会丢失，确认要关闭？',
            cancel: true,
            persistent: true
          })
          .onOk(() => {
            this.$emit('input', false);
          });
      }
    },
    // 新增审核/修改审核
    async handleAddExamineDialog() {
      let params = {
        taskName: this.reviewData.taskName,
        plan_fire_time: this.reviewData.plan_fire_time,
        dbType: this.reviewData.dbType,
        reason: this.reviewData.reason,
        reviewer: this.reviewData.reviewers
      };
      if (this.isAdd) {
        // 新增审核记录
        if (this.reviewData.docInfo.length === 0) {
          errorNotify('请选择上传文件');
          return;
        }
        params = {
          ...params,
          type: '0', // 表示该任务为“新增”
          group: this.currentUser.group_id,
          applicantName: this.currentUser.id,
          docInfo: {
            name: this.reviewData.docInfo[0].name,
            content: await this.getBase64(this.reviewData.docInfo[0])
          }
        };
      } else {
        // 编辑
        params = {
          ...params,
          type: '1', // 表示该任务为“修改”
          task_id: this.reviewData.taskId,
          group: this.reviewData.groupId,
          applicantName: this.reviewData.applicant,
          docInfo: this.change
            ? {
                name: this.reviewData.docInfo[0].name,
                content: await this.getBase64(this.reviewData.docInfo[0])
              }
            : {}
        };
      }
      await this.addReview(params);
      this.$emit('handleExamine', this.isAdd);
    }
  },
  async created() {
    // 获取“指派初审人”数据
    await this.fetch();
    const arr = this.isLoginUserList.filter(user => {
      let flag = false;
      user.role.forEach(item => {
        if (item.name === 'DBA审核人') {
          flag = true;
        }
      });
      return flag;
    });
    this.firstReviewOptions = arr;
    this.firstReviewOptionsCopy = this.firstReviewOptions.slice(0);
  }
};
</script>

<style lang="stylus" scoped>

.dialog-wrapper
  margin-top 50px
  box-sizing border-box
  max-height calc(100vh - 98px)
  overflow auto
.file-wrapper
  display inline-block
  width calc(100% - 20px)
  margin 0
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: bottom;
.icon
  cursor pointer
  padding 3px
  border-radius 50%
.icon:hover
  background #BBBBBB
</style>
