<template>
  <f-block>
    <Loading :visible="loading">
      <div class="q-mt-md row justify-end">
        <fdev-btn
          v-if="componentIssueDetail.stage < 4"
          label="废弃"
          normal
          ficon="delete"
          @click="deleteComponent"
        />
      </div>

      <div class="row justify-center q-mt-md">
        <f-formitem label="需求标题" class="col-6">
          {{ componentIssueDetail.title }}
        </f-formitem>

        <f-formitem label="开发人员" class="col-6">
          <router-link
            :to="{ path: `/user/list/${componentIssueDetail.assignee}` }"
            class="link"
            v-if="componentIssueDetail.assignee"
          >
            <span>{{ componentIssueDetail.name_cn }}</span>
          </router-link>
        </f-formitem>

        <f-formitem label="开发分支" class="col-6">
          {{ componentIssueDetail.feature_branch }}
        </f-formitem>

        <f-formitem label="目标版本号" class="col-6">
          {{ componentIssueDetail.target_version }}
        </f-formitem>

        <f-formitem label="计划完成日期" class="col-6">
          {{ componentIssueDetail.due_date }}
        </f-formitem>

        <f-formitem label="需求描述" class="col-6">
          <span v-html="desc" />
        </f-formitem>
      </div>

      <fdev-stepper
        v-model="step"
        ref="stepper"
        animated
        flat
        header-nav
        @input="findHistory"
      >
        <fdev-step :name="0" title="开发中" icon="settings" :done="step > 0" />

        <fdev-step
          :name="1"
          title="alpha(内测)"
          icon="settings"
          :done="step > 1"
          :header-nav="Number(componentIssueDetail.stage) > 0"
        />

        <fdev-step
          :name="2"
          title="候选发布(RC)"
          icon="setting"
          :done="step > 2"
          :header-nav="Number(componentIssueDetail.stage) > 1"
        />

        <fdev-step
          :name="3"
          title="正式发布(Release)"
          icon="setting"
          :done="step > 3"
          :header-nav="Number(componentIssueDetail.stage) > 2"
        />
      </fdev-stepper>

      <div class="form-message q-pb-md" v-if="isManger || isDeveloper">
        <!-- 开发中(0)和正式发布（3），隐藏表单，只显示’下一步‘按钮 -->
        <div v-if="step > 0 && step < 4">
          <f-formitem label="jdk版本">
            <fdev-select
              ref="packageModel.jdk_version"
              :options="['1.8', '1.7']"
              v-model="$v.packageModel.jdk_version.$model"
              :rules="[
                () => $v.packageModel.jdk_version.required || '请选择jdk版本'
              ]"
            />
          </f-formitem>
          <f-formitem label="发布日志">
            <fdev-input
              type="textarea"
              ref="packageModel.release_log"
              v-model="$v.packageModel.release_log.$model"
              :rules="[
                () => $v.packageModel.release_log.required || '请输入发布日志'
              ]"
            />
          </f-formitem>
        </div>

        <div class="btn text-center q-mt-md">
          <!-- 当前组件管理员||当前需求开发人员，并且处于内测（1）阶段、候选发布（2）阶段 或者正式发布（3)阶段-->
          <div class="inline-block">
            <fdev-tooltip
              anchor="top middle"
              v-if="step > 1 && notComponentFirstVersion"
            >
              当前有版本{{ componentFirstVersion }}未完成Release发布
            </fdev-tooltip>
            <fdev-btn
              label="发布"
              @click="pubilsh"
              :disable="step > 1 && notComponentFirstVersion"
              :loading="globalLoading[`componentForm/${pubilshLoading}`]"
              v-if="(isManger || isDeveloper) && (step > 0 && step < 4)"
            />
          </div>
          <!-- 当前组件管理员，并且不处于正式发布（3） -->
          <div class="q-ml-md inline-block" v-if="step < 3 && isManger">
            <fdev-tooltip
              anchor="top middle"
              v-if="IssueRecord.length === 0 && step !== 0"
            >
              至少有一条发布记录
            </fdev-tooltip>
            <!-- 历史记录为空时，并且不处于开发阶段（0），不能点击, 正式发布（3）阶段，隐藏按钮 -->
            <fdev-btn
              label="下一阶段"
              :loading="globalLoading[`componentForm/${changeStage}`]"
              @click="next"
              :disable="IssueRecord.length === 0 && step !== 0"
              v-if="step !== 3"
            />
          </div>
        </div>
      </div>
      <!-- 开发阶段（0）不展示 -->
      <fdev-table
        v-if="step > 0"
        title="历史版本列表"
        titleIcon="list_s_f"
        :data="IssueRecord"
        :columns="columns"
        :pagination.sync="pagination"
        no-export
        no-select-cols
      >
        <!-- 更新人员列 -->
        <template v-slot:body-cell-update_user="props">
          <fdev-td>
            <router-link
              :to="`/user/list/${props.row.update_user}`"
              class="link"
              v-if="props.row.update_user"
            >
              <span :title="props.row.name_cn">{{ props.row.name_cn }}</span>
            </router-link>
            <span v-else :title="props.row.name_cn">
              {{ props.row.name_cn }}
            </span>
          </fdev-td>
        </template>

        <!-- 发布日志列 -->
        <template v-slot:body-cell-release_log="props">
          <fdev-td class="text-ellipsis">
            <fdev-tooltip anchor="top middle" v-if="props.value">
              <span v-html="logFilter(props.value)">{{ props.value }}</span>
            </fdev-tooltip>
            {{ props.value }}
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>

    <f-dialog
      f-sl-sc
      title="编辑更新通知邮件"
      v-model="emailDialogOpen"
      @shake="confirmToClose"
    >
      <!-- 更新类容 -->
      <f-formitem required diaS label="更新类容">
        <fdev-input
          ref="packageModel.email_content"
          type="textarea"
          v-model="$v.packageModel.email_content.$model"
          :rules="[
            () => $v.packageModel.email_content.required || '请输入更新内容'
          ]"
        />
      </f-formitem>

      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="globalLoading[`componentForm/packageLog`]"
          @click="pubilsh(true)"
        />
      </template>
    </f-dialog>
    <!-- 废弃组件优化需求 -->
    <DestroyDialog
      v-model="componentDialogOpen"
      :issueBbranch="componentIssueDetail.feature_branch"
      :issueStage="stageComponent"
      :page="page"
      @deleteIssue="handleDelete"
    />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapGetters } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import {
  packageModel,
  serverComponentHandlePageColums
} from '@/modules/Component/utils/constants.js';
import DestroyDialog from '@/modules/Component/views/serverComponent/components/destroyOptimize.vue';

export default {
  name: 'HandlePage',
  components: { Loading, DestroyDialog },
  data() {
    return {
      step: 0,
      loading: false,
      id: '',
      packageModel: packageModel(),
      columns: serverComponentHandlePageColums,
      pagination: {
        rowsPerPage: 0
      },
      emailDialogOpen: false,
      stageComponent: '',
      componentDialogOpen: false,
      issueStage: '',
      page: ''
    };
  },
  validations() {
    const packageModel = {
      packageModel: {
        jdk_version: { required },
        release_log: { required },
        email_content: {}
      }
    };
    if (this.emailDialogOpen) {
      packageModel.packageModel.email_content = { required };
    }
    return packageModel;
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', [
      'componentIssueDetail',
      'IssueRecord',
      'emailContent',
      'releaseLog',
      'componentFirstVersion'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapGetters('componentForm', ['notComponentFirstVersion']),
    // 当前组件的管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.componentIssueDetail.manager_id
        ? this.componentIssueDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    // 当前组件的开发人员
    isDeveloper() {
      return this.componentIssueDetail.assignee === this.currentUser.id;
    },
    desc() {
      const desc = this.componentIssueDetail.desc
        ? this.componentIssueDetail.desc
        : '';
      return desc
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    pubilshLoading() {
      return this.step === 3 ? 'mailContent' : 'packageLog';
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryComponentIssueDetail',
      'packageLog',
      'changeStage',
      'queryIssueRecord',
      'mailContent',
      'queryReleaseLog',
      'queryComponentFirstVersion',
      'destroyComponentIssue'
    ]),

    deleteComponent() {
      this.componentDialogOpen = true;
      this.page = this.$route.path;
      if (this.componentIssueDetail.stage === '0')
        this.stageComponent = '新增阶段(create)';
      if (this.componentIssueDetail.stage === '1')
        this.stageComponent = '内测阶段(alpha)';
      if (this.componentIssueDetail.stage === '2')
        this.stageComponent = '候选发布阶段(RC)';
      if (this.componentIssueDetail.stage === '3')
        this.stageComponent = '正式发布(Release)';
    },
    async handleDelete() {
      await this.destroyComponentIssue({
        id: this.id
      });
      successNotify('废弃成功！');
      this.$router.push({
        path: `/componentManage/server/list/${
          this.componentIssueDetail.component_id
        }`
      });
    },
    async init() {
      this.loading = true;
      await this.queryComponentIssueDetail({
        id: this.id
      });
      this.queryisFirstVersion();
      // 进入页面时，设置step
      this.step = Number(this.componentIssueDetail.stage);
      // 处于’开发中‘不查询历史记录
      if (this.componentIssueDetail.stage === '0') {
        this.loading = false;
        return;
      }
      this.findHistory();
      await this.queryReleaseLog({
        component_id: this.componentIssueDetail.component_id,
        target_version: this.componentIssueDetail.target_version
      });
      this.packageModel.release_log = this.releaseLog.release_log
        ? this.releaseLog.release_log
        : '';
      this.loading = false;
    },

    async pubilsh(isOk) {
      this.$v.packageModel.$touch();

      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('packageModel') > -1;
      });

      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );

      if (this.$v.packageModel.$invalid) {
        return;
      }
      /* 发布，正式发布（3）时，要发送接口查询邮件内容 */
      const params = {
        ...this.packageModel,
        ...this.componentIssueDetail,
        update_user: this.currentUser.id,
        stage: this.step.toString()
      };
      this.emailDialogOpen = false;

      if (this.step === 3 && isOk !== true) {
        await this.emailPromiseDialog(params);
        return;
      }

      await this.packageLog(params);
      const msg =
        this.step === 2
          ? '已发起分支合并请求，请联系组件管理员进行分支合并!'
          : '发布成功,请等待pipelines成功！';
      successNotify(msg);
    },
    // 下一步按钮
    async next() {
      // 如果回退了，不发接口更改状态，但要查回退stage的历史记录
      if (this.step.toString() !== this.componentIssueDetail.stage) {
        this.step = this.step + 1;
        this.findHistory();
        return;
      }
      await this.changeStage({
        stage: this.step.toString(),
        id: this.componentIssueDetail.id
      });
      this.packageModel = packageModel();
      await this.init();
    },
    logFilter(val) {
      val = val ? val : '';
      return val.replace(/<\/?[^>]*/g, '').replace(/\n/g, '<br/>');
    },
    async findHistory() {
      if (this.step === 0) {
        return;
      }
      // 已完成（4）查不到历史记录，要改为3
      this.queryIssueRecord({
        component_id: this.componentIssueDetail.component_id,
        stage: this.step.toString() === '4' ? '3' : this.step.toString(),
        target_version: this.componentIssueDetail.target_version
      });
    },
    async emailPromiseDialog(params) {
      const emailParams = {
        component_id: params.component_id,
        jdk_version: params.jdk_version,
        target_version: params.target_version,
        release_log: params.release_log,
        update_user: this.currentUser.id
      };
      await this.mailContent(emailParams);
      this.packageModel.email_content = this.emailContent.email_content;
      this.emailDialogOpen = true;
    },
    queryisFirstVersion() {
      if (this.componentIssueDetail.stage > 1) {
        this.queryComponentFirstVersion({
          component_id: this.componentIssueDetail.component_id
        });
      }
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.emailDialogOpen = false;
        });
    }
  },
  async created() {
    this.id = this.$route.params.id;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.form-message
  width 500px
  margin 0 auto
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.dialog-height
  height 470px
.textarea
  min-height 300px
  overflow auto
.q-stepper >>> .q-stepper__content
  display none!important
</style>
