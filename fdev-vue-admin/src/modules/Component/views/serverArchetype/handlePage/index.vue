<template>
  <f-block>
    <Loading :visible="loading">
      <div class="q-mt-md row justify-end">
        <fdev-btn
          label="废弃"
          normal
          ficon="delete"
          @click="deleteArchetpe"
          v-if="archetypeIssueDetail.stage < 3"
        />
      </div>
      <div class="row justify-center q-mt-md">
        <f-formitem class="col-4" label="需求标题：">
          {{ archetypeIssueDetail.title }}
        </f-formitem>
        <f-formitem class="col-4" label="开发人员：">
          <router-link
            :to="{ path: `/user/list/${archetypeIssueDetail.assignee}` }"
            class="link"
            v-if="archetypeIssueDetail.assignee"
          >
            <span>{{ archetypeIssueDetail.name_cn }}</span>
          </router-link>
        </f-formitem>
        <f-formitem class="col-4" label="开发分支：">
          {{ archetypeIssueDetail.feature_branch }}
        </f-formitem>
        <f-formitem class="col-4" label="目标版本号：">
          {{ archetypeIssueDetail.target_version }}
        </f-formitem>
        <f-formitem class="col-4" label="计划完成日期：">
          {{ archetypeIssueDetail.due_date }}
        </f-formitem>
        <f-formitem class="col-4" label="需求描述：">
          <span v-html="desc" />
        </f-formitem>
      </div>

      <!-- 正式发布（stage = 4）后，不能回退 -->
      <fdev-stepper
        v-model="step"
        ref="stepper"
        animated
        flat
        :header-nav="step !== 3"
        @input="findHistory"
      >
        <fdev-step :name="0" title="开发中" icon="settings" :done="step > 0" />

        <fdev-step
          :name="1"
          title="alpha(内测)"
          icon="settings"
          :done="step > 1"
          :header-nav="Number(archetypeIssueDetail.stage) > 0"
        />

        <fdev-step
          :name="2"
          title="正式发布(Release)"
          icon="settings"
          :done="step > 2"
          :header-nav="Number(archetypeIssueDetail.stage) > 1"
        />
      </fdev-stepper>

      <!-- 正式发布后(3),隐藏表单按钮，只显示记录 -->
      <div
        v-if="archetypeIssueDetail.stage !== '3' && (isManger || isDeveloper)"
      >
        <!-- 开发中(0)，隐藏表单，只显示’下一步‘按钮 -->
        <f-formitem label="发布日志" v-if="step !== 0" required full-width>
          <fdev-input
            type="textarea"
            ref="packageModel.release_log"
            v-model="$v.packageModel.release_log.$model"
            :rules="[
              () => $v.packageModel.release_log.required || '请输入发布日志'
            ]"
          />
        </f-formitem>
        <div class="q-gutter-x-md row justify-center q-my-lg">
          <fdev-btn
            label="编辑环境参数"
            ficon="edit"
            @click="modelDeployOpened = true"
            v-if="isManger && step === 1"
          />
          <!-- 当前组件管理员||当前需求开发人员，并且不处于开发（0）阶段 -->
          <span>
            <fdev-tooltip v-if="step === 2 && notArchetypeFirstVersion">
              当前有版本{{ archetypeFirstVersion }}未完成Release发布
            </fdev-tooltip>
            <fdev-btn
              label="发布"
              ficon="process_c_o"
              :disable="step === 2 && notArchetypeFirstVersion"
              @click="pubilsh"
              :loading="globalLoading[`componentForm/archetypePackage`]"
              v-if="(isManger || isDeveloper) && step !== 0"
            />
          </span>
          <!-- 当前组件管理员，并且不处于正式发布（2） -->
          <span v-if="step < 2 && isManger">
            <fdev-tooltip
              v-if="archetypePackageList.length === 0 && step !== 0"
            >
              至少有一条发布记录
            </fdev-tooltip>
            <!-- 历史记录为空时，并且不处于开发阶段（0），不能点击 -->
            <fdev-btn
              label="下一阶段"
              @click="next"
              :loading="globalLoading[`componentForm/archetypeChangeStage`]"
              :disable="archetypePackageList.length === 0 && step !== 0"
            />
          </span>
        </div>
      </div>

      <!-- 开发阶段（0）不展示 -->
      <fdev-table
        v-if="step > 0"
        title="历史版本"
        :data="archetypePackageList"
        :columns="columns"
        :pagination.sync="pagination"
        titleIcon="list_s_f"
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
            <span v-else>{{ props.row.name_cn }}</span>
          </fdev-td>
        </template>

        <!-- <template v-slot:body-cell-release_log="props">
          <fdev-td :title="props.value" class="text-ellipsis">
            {{ props.value }}
          </fdev-td>
        </template> -->

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
    <EnvConfigDialog
      :value="modelDeployOpened"
      @click="init"
      v-model="modelDeployOpened"
      :isEditable="true"
    />
    <!-- 废弃骨架优化需求 -->
    <DestroyDialog
      v-model="archetypeDialogOpen"
      :issueBbranch="archetypeIssueDetail.feature_branch"
      :issueStage="stageArchetype"
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
  serverArchetypeHandlePageColumns
} from '@/modules/Component/utils/constants.js';
import EnvConfigDialog from '@/modules/Component/views/serverArchetype/components/envConfigFile';
import DestroyDialog from '@/modules/Component/views/serverComponent/components/destroyOptimize';

export default {
  name: 'HandlePage',
  components: {
    Loading,
    EnvConfigDialog,
    DestroyDialog
  },
  data() {
    return {
      modelDeployOpened: false,
      step: 0,
      loading: false,
      id: '',
      archetype_id: '',
      packageModel: packageModel(),
      columns: serverArchetypeHandlePageColumns,
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
      release_log: { required }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('componentForm', [
      'archetypeIssueDetail',
      'archetypePackageList',
      'archetypeReleaseLog',
      'archetypeFirstVersion'
    ]),
    ...mapGetters('componentForm', ['notArchetypeFirstVersion']),
    // 当前组件的管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.archetypeIssueDetail.manager_id
        ? this.archetypeIssueDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    // 当前组件的开发人员
    isDeveloper() {
      return this.archetypeIssueDetail.assignee === this.currentUser.id;
    },
    desc() {
      const desc = this.archetypeIssueDetail.desc
        ? this.archetypeIssueDetail.desc
        : '';
      return desc
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'archetypeChangeStage',
      'archetypePackage',
      'queryArchetypeIssueRecord',
      'queryArchetypeIssueDetail',
      'queryArchetypeReleaseLog',
      'queryArchetypeFirstVersion',
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

      if (this.$v.packageModel.$invalid) {
        return;
      }
      /* 发布，正式发布（3）时，要发送接口查询邮件内容 */
      const params = {
        ...this.packageModel,
        ...this.archetypeIssueDetail,
        update_user: this.currentUser.id,
        stage: this.step.toString()
      };

      await this.archetypePackage(params);
      successNotify('已提交分支合并请求，请在gitlab完成分支合并。');
    },
    // 下一步按钮
    async next() {
      // 如果回退了，不发接口更改状态，但要查回退stage的历史记录
      if (this.step.toString() !== this.archetypeIssueDetail.stage) {
        this.step = this.step + 1;
        this.findHistory();
        return;
      }
      await this.archetypeChangeStage({
        stage: this.step.toString(),
        id: this.archetypeIssueDetail.id
      });
      this.packageModel = packageModel();
      this.init();
    },
    logFilter(val) {
      val = val ? val : '';
      return val.replace(/<\/?[^>]*/g, '').replace(/\n/g, '<br/>');
    },
    async findHistory() {
      if (this.step === 0) {
        return;
      }
      // 已完成（3）查不到历史记录，要改为2
      this.queryArchetypeIssueRecord({
        archetype_id: this.archetype_id,
        stage: this.step.toString() === '3' ? '2' : this.step.toString(),
        target_version: this.archetypeIssueDetail.target_version
      });
    },
    /* 查询是否是第一个开发版本 */
    queryIsFirstVersion() {
      if (this.archetypeIssueDetail.stage > 1) {
        this.queryArchetypeFirstVersion({
          archetype_id: this.archetype_id
        });
      }
    },
    deleteArchetpe() {
      this.archetypeDialogOpen = true;
      this.page = this.$route.path;
      if (this.archetypeIssueDetail.stage === '0')
        this.stageArchetype = '新增阶段(create)';
      if (this.archetypeIssueDetail.stage === '1')
        this.stageArchetype = '内测阶段(alpha)';
      if (this.archetypeIssueDetail.stage === '2')
        this.stageArchetype = '正式发布(Release)';
    },
    async handleDelete() {
      await this.destroyArchetypeIssue({
        id: this.id
      });
      successNotify('废弃成功！');
      this.$router.push({
        path: `/archetypeManage/server/archetype/${this.archetype_id}`
      });
    },
    async init() {
      this.loading = true;
      await this.queryArchetypeIssueDetail({
        id: this.id
      });
      this.queryIsFirstVersion();
      // 进入页面时，设置step
      this.step = Number(this.archetypeIssueDetail.stage);
      // 处于’开发中‘不查询历史记录
      if (this.archetypeIssueDetail.stage === '0') {
        this.loading = false;
        return;
      }
      this.findHistory();
      await this.queryArchetypeReleaseLog({
        archetype_id: this.archetype_id,
        target_version: this.archetypeIssueDetail.target_version
      });
      this.packageModel.release_log = this.archetypeReleaseLog.release_log
        ? this.archetypeReleaseLog.release_log
        : '';
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
