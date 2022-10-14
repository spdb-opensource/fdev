<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-card flat square>
        <fdev-card-section class="q-pa-md row bg-white">
          <f-formitem label="开发人员" class="col-6">
            <router-link
              :to="{ path: `/user/list/${mpassDevIssueDetail.assignee}` }"
              class="link"
              v-if="mpassDevIssueDetail.assignee"
            >
              <span>{{ mpassDevIssueDetail.name_cn }}</span>
            </router-link>
          </f-formitem>

          <f-formitem label="开发分支" class="col-6">
            {{ mpassDevIssueDetail.feature_branch }}
          </f-formitem>

          <f-formitem label="计划完成日期" class="col-6">
            {{ mpassDevIssueDetail.due_date }}
          </f-formitem>

          <f-formitem label="需求描述" class="col-6">
            <span v-html="desc" />
          </f-formitem>
        </fdev-card-section>

        <fdev-separator />

        <fdev-card-section class="q-mt-md bg-white">
          <fdev-stepper
            v-model="step"
            ref="stepper"
            animated
            flat
            :header-nav="step !== 4"
            @input="findHistory"
          >
            <fdev-step
              :name="0"
              title="开发中"
              icon="settings"
              :done="step > 0"
            />

            <fdev-step
              :name="1"
              title="内测(alpha)"
              icon="settings"
              :done="step > 1"
              :header-nav="Number(mpassDevIssueDetail.stage) > 0"
            />

            <fdev-step
              :name="2"
              title="公测(beta)"
              icon="settings"
              :done="step > 2"
              :header-nav="Number(mpassDevIssueDetail.stage) > 1"
            />

            <fdev-step
              :name="3"
              title="试运行(rc)"
              icon="settings"
              :done="step > 3"
              :header-nav="Number(mpassDevIssueDetail.stage) > 2"
            />
          </fdev-stepper>
        </fdev-card-section>
      </fdev-card>

      <!-- 发布日志,发布,下一阶段展示逻辑: 除阶段判断外,当前人员非基础架构管理、组件管理员、开发人员不展示 -->
      <div class="form-message q-pb-md">
        <div v-if="(step == 2 || step == 3) && (isManger || isDeveloper)">
          <!-- 发布日志 -->
          <f-formitem page label="发布日志">
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
          <!-- 当前组件管理员||当前需求开发人员，并且不处于开发（0）阶段 -->
          <div class="inline-block">
            <fdev-btn
              label="发布"
              @click="pubilsh"
              :loading="globalLoading['componentForm/devPackageServer']"
              v-if="step !== 0 && (isManger || isDeveloper)"
            />
          </div>
          <div class="q-ml-md inline-block" v-if="step < 3">
            <fdev-tooltip v-if="multiIssueRecord.length === 0 && step !== 0">
              至少有一条发布记录
            </fdev-tooltip>
            <!-- 历史记录为空时，并且不处于开发阶段（0），不能点击 -->
            <fdev-btn
              label="下一阶段"
              :loading="globalLoading[`componentForm/${changeMpassStage}`]"
              @click="next"
              v-if="step + 1 <= 3 && (isManger || isDeveloper)"
              :disable="multiIssueRecord.length === 0 && step !== 0"
            />
          </div>
        </div>
      </div>
      <!-- 开发阶段（0）不展示 -->
      <fdev-table
        class="my-sticky-column-table"
        v-if="step > 0"
        title="历史版本"
        titleIcon="list_s_f"
        :data="multiIssueRecord"
        :columns="columns"
        :pagination.sync="pagination"
        no-export
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

        <!-- 发布日志 -->
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
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import {
  mpassPackageModel,
  serverComponentHandleRelColums
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'HandlePageRel',
  components: { Loading },
  data() {
    return {
      step: 0,
      loading: false,
      id: '',
      packageModel: mpassPackageModel(),
      columns: serverComponentHandleRelColums,
      pagination: {
        rowsPerPage: 0
      },
      stageComponent: '',
      componentDialogOpen: false,
      issueStage: '',
      page: ''
    };
  },
  validations() {
    const packageModel = {
      packageModel: {
        release_log: { required }
      }
    };
    return packageModel;
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', [
      'mpassDevIssueDetail',
      'multiIssueRecord',
      'componentDetail'
    ]),
    ...mapState('user', ['currentUser']),
    // 当前组件的管理员或者基础架构管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.componentDetail
        ? this.componentDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    // 当前组件的开发人员
    isDeveloper() {
      return this.mpassDevIssueDetail.assignee === this.currentUser.id;
    },
    desc() {
      const desc = this.mpassDevIssueDetail.desc
        ? this.mpassDevIssueDetail.desc
        : '';
      return desc
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'devPackageServer',
      'changeMpassStage',
      'queryComponentFirstVersion',
      'queryMpassDevIssueDetail',
      'queryMultiIssueRecord',
      'queryComponentDetail'
    ]),

    async init() {
      this.loading = true;
      await this.queryMpassDevIssueDetail({
        id: this.id
      });
      // 进入页面时，设置step
      this.step = Number(this.mpassDevIssueDetail.stage);
      if (this.mpassDevIssueDetail.stage === '0') {
        this.loading = false;
        return;
      }
      await this.queryComponentDetail({
        id: this.mpassDevIssueDetail.component_id
      });
      this.findHistory();
      this.loading = false;
    },

    async pubilsh() {
      this.$v.packageModel.$touch();

      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('packageModel') > -1;
      });

      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );

      if (this.$v.packageModel.$invalid && (this.step == 2 || this.step == 3)) {
        return;
      }
      /* 发布，正式发布（3）时，要发送接口查询邮件内容 */
      const params = {
        ...this.packageModel,
        ...this.mpassDevIssueDetail,
        update_user: this.currentUser.id,
        stage: this.step.toString()
      };

      await this.devPackageServer(params);
      const msg =
        this.step === 2 || this.step === 3
          ? 'tag已拉取，请等待pipelines成功!'
          : '已发起合并分支请求，请联系组件管理员进行分支合并！';
      successNotify(msg);
    },
    // 下一步按钮
    async next() {
      // 如果回退了，不发接口更改状态，但要查回退stage的历史记录
      if (this.step.toString() !== this.mpassDevIssueDetail.stage) {
        this.step = this.step + 1;
        this.findHistory();
        return;
      }
      await this.changeMpassStage({
        stage: this.step.toString(),
        id: this.mpassDevIssueDetail.id
      });
      this.packageModel = mpassPackageModel();
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
      // 已完成（4）查不到历史记录，要改为3 todo
      this.queryMultiIssueRecord({
        id: this.mpassDevIssueDetail.id,
        stage: this.step.toString(),
        type: 'mpass_dev'
      });
    },
    queryisFirstVersion() {
      if (this.mpassDevIssueDetail.stage > 1) {
        this.queryComponentFirstVersion({
          component_id: this.mpassDevIssueDetail.component_id
        });
      }
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
</style>
