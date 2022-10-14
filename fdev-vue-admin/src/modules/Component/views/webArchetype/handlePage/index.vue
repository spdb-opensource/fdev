<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <!-- <fdev-card flat square>
        <fdev-card-section class="q-pa-md row bg-white"> -->
      <div class="row justify-center q-mt-md">
        <f-formitem class="col-4" label="需求标题">
          {{ mpassArchetypeIssueDetail.title }}
        </f-formitem>
        <f-formitem class="col-4" label="开发人员">
          <router-link
            :to="{ path: `/user/list/${mpassArchetypeIssueDetail.assignee}` }"
            class="link"
            v-if="mpassArchetypeIssueDetail.assignee"
          >
            <span>{{ mpassArchetypeIssueDetail.name_cn }}</span>
          </router-link>
        </f-formitem>
        <f-formitem class="col-4" label="开发分支">
          {{ mpassArchetypeIssueDetail.feature_branch }}
        </f-formitem>
        <f-formitem class="col-4" label="计划完成日期">
          {{ mpassArchetypeIssueDetail.due_date }}
        </f-formitem>
        <f-formitem class="col-4" label="需求描述">
          <span v-html="desc" />
        </f-formitem>
      </div>
      <!-- </fdev-card-section> -->

      <!-- <fdev-card-section class="q-mt-md bg-white"> -->
      <fdev-stepper
        v-model="step"
        ref="stepper"
        animated
        flat
        @input="findHistory()"
        :header-nav="step !== 3"
      >
        <fdev-step :name="0" title="开发中" icon="settings" :done="step > 0" />

        <fdev-step :name="1" title="发布" icon="settings" :done="step > 1" />

        <fdev-step :name="2" title="拉取tag" icon="settings" :done="step > 2" />
      </fdev-stepper>
      <!-- </fdev-card-section> -->
      <!-- </fdev-card> -->

      <!-- 正式发布后(3),隐藏表单按钮，只显示记录 -->
      <div
        v-if="
          mpassArchetypeIssueDetail.stage !== '3' && (isManger || isDeveloper)
        "
      >
        <f-formitem full-width label="目标版本" v-if="step == 2">
          <fdev-input
            type="input"
            autofocus
            ref="packageModel.tag_name"
            v-model="$v.packageModel.tag_name.$model"
            :rules="[
              () =>
                $v.packageModel.tag_name.control ||
                '只能输入数字、字母、_、-和.'
            ]"
          />
        </f-formitem>
        <!-- 开发中(0)，隐藏表单，只显示’下一步‘按钮 -->
        <f-formitem label="发布日志" v-if="step == 2" required full-width>
          <fdev-input
            type="textarea"
            ref="packageModel.release_log"
            v-model="$v.packageModel.release_log.$model"
            :rules="[
              () => $v.packageModel.release_log.required || '请输入发布日志'
            ]"
          />
        </f-formitem>
        <div class="btn text-center q-my-lg">
          <!-- 当前组件管理员||当前需求开发人员，并且不处于开发（0）阶段 -->
          <div class="q-ml-md inline-block">
            <fdev-btn
              label="发布"
              @click="pubilsh"
              :loading="globalLoading[`componentForm/mpassArchetypePackage`]"
              v-if="(isManger || isDeveloper) && step !== 0"
            />
          </div>
          <!-- 当前组件管理员，并且不处于正式发布（2） -->
          <div class="q-ml-md inline-block" v-if="step < 2 && isManger">
            <!-- 历史记录为空时，并且不处于开发阶段（0），不能点击 -->
            <fdev-btn label="下一阶段" @click="next" />
          </div>
        </div>
      </div>
      <!-- 开发阶段（0）不展示 -->
      <fdev-table
        v-if="step > 1"
        title="历史版本"
        titleIcon="list_s_f"
        :data="issueTag"
        :columns="columns"
        :pagination.sync="pagination"
        no-export
        no-select-cols
      >
        <template v-slot:body-cell-update_user="props">
          <fdev-td :title="props.row.name_cn" class="text-ellipsis">
            <router-link
              :to="`/user/list/${props.row.update_user}`"
              class="link"
              v-if="props.row.update_user"
            >
              <span>{{ props.row.name_cn }}</span>
            </router-link>
            <span v-else>{{ props.row.name_cn || '-' }}</span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-release_log="props">
          <fdev-td class="text-ellipsis">
            <fdev-tooltip anchor="top middle" v-if="props.value">
              <span v-html="logFilter(props.value)">{{ props.value }}</span>
            </fdev-tooltip>
            {{ props.value }}
          </fdev-td>
        </template>

        <!-- <template v-slot:body-cell-release_log="props">
          <fdev-td :title="props.value" class="text-ellipsis">
            {{ props.value || '-' }}
          </fdev-td>
        </template> -->
      </fdev-table>
    </Loading>
    <EnvConfigDialog
      :value="modelDeployOpened"
      @click="init"
      v-model="modelDeployOpened"
      :isEditable="true"
    />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import {
  mpassPackageModel,
  webArchetypeHandlePageColums
} from '@/modules/Component/utils/constants.js';
import EnvConfigDialog from '@/modules/Component/views/serverArchetype/components/envConfigFile';

export default {
  name: 'HandlePage',
  components: {
    Loading,
    EnvConfigDialog
  },
  data() {
    return {
      modelDeployOpened: false,
      step: 0,
      loading: false,
      id: '',
      archetype_id: '',
      packageModel: mpassPackageModel(),
      columns: webArchetypeHandlePageColums,
      pagination: {
        rowsPerPage: 0
      },
      stageArchetype: '',
      archetypeDialogOpen: false,
      page: ''
    };
  },
  validations: {
    packageModel: {
      release_log: { required },
      tag_name: {
        control(val) {
          if (!val) {
            return true;
          }
          const reg = /^[a-zA-Z0-9-_.]+$/;
          return reg.test(val);
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('componentForm', [
      'mpassArchetypeIssueDetail',
      'issueTag',
      'archetypeReleaseLog'
    ]),
    // 当前组件的管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.mpassArchetypeIssueDetail.manager
        ? this.mpassArchetypeIssueDetail.manager.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    // 当前组件的开发人员
    isDeveloper() {
      return this.mpassArchetypeIssueDetail.assignee === this.currentUser.id;
    },
    desc() {
      const desc = this.mpassArchetypeIssueDetail.desc
        ? this.mpassArchetypeIssueDetail.desc
        : '';
      return desc
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'changeMpassArchetypeStage',
      'mpassArchetypePackage',
      'queryIssueTag',
      'queryMpassArchetypeIssueDetail',
      'queryArchetypeReleaseLog',
      'destroyArchetypeIssue'
    ]),
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

      if (this.$v.packageModel.$invalid && this.step == 2) {
        return;
      }
      /* 发布，正式发布（3）时，要发送接口查询邮件内容 */
      const params = {
        ...this.packageModel,
        ...this.mpassArchetypeIssueDetail,
        update_user: this.currentUser.id,
        stage: this.step.toString()
      };

      await this.mpassArchetypePackage(params);
      const msg =
        this.step === 2
          ? 'tag已拉取!'
          : '已发起合并分支请求，请联系骨架管理员进行分支合并！';
      successNotify(msg);
    },
    // 下一步按钮
    async next() {
      // 如果回退了，不发接口更改状态，但要查回退stage的历史记录
      if (this.step.toString() !== this.mpassArchetypeIssueDetail.stage) {
        this.step = this.step + 1;
        this.findHistory();
        return;
      }
      await this.changeMpassArchetypeStage({
        stage: this.step.toString(),
        id: this.mpassArchetypeIssueDetail.id
      });
      this.init();
    },
    logFilter(val) {
      val = val ? val : '';
      return val.replace(/<\/?[^>]*/g, '').replace(/\n/g, '<br/>');
    },
    async findHistory() {
      if (this.step === 0 || this.step === 1) {
        return;
      }
      // 已完成（3）查不到历史记录，要改为2
      this.queryIssueTag({
        issue_id: this.id
      });
    },
    async init() {
      this.loading = true;
      await this.queryMpassArchetypeIssueDetail({
        id: this.id
      });
      // 进入页面时，设置step
      this.step = Number(this.mpassArchetypeIssueDetail.stage);
      this.findHistory();
      this.loading = false;
    }
  },
  async created() {
    this.archetype_id = this.$route.params.archetype_id;
    this.id = this.$route.params.id;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.q-stepper >>> .q-stepper__content
  display none!important
</style>
