<template>
  <f-block>
    <Loading>
      <fdev-table
        class="my-sticky-column-table"
        title="多人开发列表"
        titleIcon="list_s_f"
        ref="table"
        :data="tabelData"
        :columns="columns"
        no-export
        no-select-cols
      >
        <!-- 当前阶段列 -->
        <template v-slot:body-cell-stage="props">
          <fdev-td class="text-ellipsis">
            <span :title="webStage[props.row.stage]">
              {{ webStage[props.row.stage] }}
            </span>
          </fdev-td>
        </template>

        <!-- 操作列 -->
        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <div class="q-gutter-x-sm row no-wrap">
              <fdev-btn label="处理" @click="linkTo(props.row.id)" flat />
              <fdev-btn
                label="信息维护"
                @click="updateMpassDev(props.row)"
                flat
                v-if="isVersionManage()"
              />
              <fdev-btn
                label="迁移"
                @click="migrate(props.row.component_id, props.row.id)"
                flat
                v-if="isComManage() && !isRelease"
              />
              <fdev-btn
                label="废弃"
                @click="destroyMpassDev(props.row)"
                flat
                v-if="isVersionManage() && !isRelease"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>

      <div class="form-message q-pb-md">
        <!-- 目标版本 -->
        <f-formitem required diaS label="目标版本">
          <fdev-input
            ref="releasePackageModel.tag_name"
            v-model="$v.releasePackageModel.tag_name.$model"
            :rules="[
              () =>
                $v.releasePackageModel.tag_name.examine ||
                '请输入正确的格式:*.x.x, 只能输入数字'
            ]"
          />
        </f-formitem>

        <!-- 发布日志 -->
        <f-formitem required diaS label="发布日志">
          <fdev-input
            type="textarea"
            ref="releasePackageModel.release_log"
            v-model="$v.releasePackageModel.release_log.$model"
            :rules="[
              () =>
                $v.releasePackageModel.release_log.required || '请输入发布日志'
            ]"
          />
        </f-formitem>

        <div class="row justify-center q-my-lg q-gutter-md">
          <fdev-btn label="新增开发分支" @click="WebAddDevDialogOpen = true" />
          <div>
            <fdev-btn
              label="release发布"
              :disable="!isManager"
              @click="deploy"
            />
            <fdev-tooltip anchor="top middle" v-if="!isManager">
              release发布权限仅限于组件管理员和版本管理员
            </fdev-tooltip>
          </div>
          <fdev-btn label="返回" @click="goBack" />
        </div>
      </div>

      <fdev-table
        class="my-sticky-column-table"
        title="历史版本列表"
        titleIcon="list_s_f"
        :data="issueRecord"
        :columns="recordColumns"
        ref="table"
        no-export
        no-select-cols
      >
        <!-- 发布日志列 -->
        <template v-slot:body-cell-release_log="props">
          <fdev-td class="text-ellipsis">
            <span :title="props.row.release_log">
              {{ props.row.release_log }}
            </span>
          </fdev-td>
        </template>
      </fdev-table>
      <WebAddDevDialog
        v-model="WebAddDevDialogOpen"
        :issue_id="issueId"
        @refresh="init"
      />
      <DevUpdateDialog v-model="dialogOpen" :data="updateData" @click="init" />
    </Loading>
    <!-- 迁移弹框 -->
    <f-dialog title="窗口选择" f-sl-sc v-model="addTransgerOpened">
      <f-formitem label="目标窗口" diaS v-if="transgerIssue.length > 0">
        <fdev-select
          use-input
          emit-value
          map-options
          :options="transgerIssue"
          option-label="feature_branch"
          option-value="id"
          ref="transger.window"
          v-model="$v.transger.window.$model"
          :rules="[() => $v.transger.window.required || '请选择迁移的目标窗口']"
        />
      </f-formitem>
      <p v-else>暂无可选择的窗口</p>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          :loading="globalLoading[`componentForm/${devIssueTransger}`]"
          label="提交"
          @click="handleTransgerTip"
        />
      </template>
    </f-dialog>
  </f-block>
</template>

<script>
import WebAddDevDialog from '@/modules/Component/views/webComponent/components/issueAddBranch';
import DevUpdateDialog from '@/modules/Component/views/webComponent/components/issueUpdate';
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import { successNotify, getIdsFormList, validate } from '@/utils/utils';
import {
  webStage,
  mpassReleasePackageModel,
  serverComponentIssuePageColums,
  serverComponentIssuePageRecordColums
} from '@/modules/Component/utils/constants.js';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'IssuePage',
  components: {
    Loading,
    WebAddDevDialog,
    DevUpdateDialog
  },
  data() {
    return {
      releasePackageModel: mpassReleasePackageModel(),
      issueId: '',
      tabelData: [],
      issueRecord: [],
      webStage,
      WebAddDevDialogOpen: false,
      dialogOpen: false,
      updateData: {},
      columns: serverComponentIssuePageColums,
      recordColumns: serverComponentIssuePageRecordColums,
      addTransgerOpened: false,
      transger: { window: '' },
      transgerId: ''
    };
  },
  validations: {
    releasePackageModel: {
      tag_name: {
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^(\d{1,})\.((?!0)\d{2}|\d{1})\.((?!0)\d{2}|\d{1})$/;
          return reg.test(val);
        }
      },
      release_log: {
        required
      }
    },
    transger: {
      window: {
        required
      }
    }
  },
  computed: {
    ...mapState('componentForm', [
      'mpassDevIssue',
      'mpassReleaseIssue',
      'componentDetail',
      'mpassRelDetail',
      'transgerIssue',
      'multiIssueRecord'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    issueMpassRecord: {
      get() {
        return this.$store.state.componentForm.multiIssueRecord;
      },
      set(val) {
        this.$store.state.componentForm.multiIssueRecord = val;
      }
    },
    // 当前窗口是否有正式发布版本
    isRelease() {
      if (this.multiIssueRecord.length === 0) {
        return false;
      } else {
        let reg = /^[\d.]*$/;
        return this.multiIssueRecord.some(val => {
          return reg.test(val.version);
        });
      }
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryMpassDevIssue',
      'releasePackageServer',
      'queryMultiIssueRecord',
      'queryComponentDetail',
      'queryMpassReleaseIssue',
      'destroyIssue',
      'queryMpassReleaseIssueDetail',
      'queryTransgerReleaseIssue',
      'devIssueTransger',
      'addDevIssue'
    ]),
    getVersionType(val) {
      return val == '0'
        ? '正式版本'
        : val == '1'
        ? '推荐版本'
        : val == '2'
        ? '废弃版本'
        : '测试版本';
    },
    linkTo(id) {
      this.issueMpassRecord = [];
      this.$router.push({ name: 'HandlePageRel', params: { id } });
    },
    updateMpassDev(data) {
      this.dialogOpen = true;
      this.updateData = data;
    },
    async migrate(id, transgerId) {
      this.transgerId = transgerId;
      await this.queryTransgerReleaseIssue({
        component_id: id,
        feature_branch: this.$route.query.branch
      });
      this.addTransgerOpened = true;
    },
    destroyMpassDev(data) {
      return this.$q
        .dialog({
          title: '废弃提示',
          message: '确定废弃该条开发需求吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.destroyIssue({
            id: data.id,
            type: 'mpass_dev'
          });
          successNotify('废弃成功');
          this.init();
        });
    },
    async deploy() {
      await this.releasePackageServer({
        id: this.issueId,
        ...this.releasePackageModel,
        update_user: this.currentUser.id
      });
      successNotify('已发起合并分支请求，请联系组件管理员进行分支合并！');
    },
    isManager() {
      let comIds = this.componentDetail.manager_id.map(user => user.id);
      let managers = this.mpassReleaseIssue.map(issue => {
        if (issue.id === this.issueId) {
          return issue.manager;
        }
      });
      let managerIds = getIdsFormList(managers);
      return comIds.concat(managerIds).includes(this.currentUser.id);
    },
    //该条投产数据的版本管理员权限控制
    isVersionManage() {
      if (this.mpassRelDetail && this.mpassRelDetail.manager) {
        return this.mpassRelDetail.manager.some(
          user => user.id === this.currentUser.id
        );
      }
    },
    //组件管理员权限控制
    isComManage() {
      if (this.componentDetail && this.componentDetail.manager_id) {
        return this.componentDetail.manager_id.some(
          user => user.id === this.currentUser.id
        );
      }
    },
    handleTransgerTip() {
      this.$v.transger.$touch();
      let summaryModelKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('transger') > -1
      );
      validate(summaryModelKeys.map(key => this.$refs[key]));
      if (this.$v.transger.$invalid) {
        return;
      }
      this.handleTransger();
    },
    async handleTransger() {
      await this.devIssueTransger({
        id: this.transgerId,
        issue_id: this.transger.window
      });
      successNotify('迁移成功');
      this.addTransgerOpened = false;
      await this.queryMpassDevIssue({ issue_id: this.$route.params.id });
      this.tabelData = this.mpassDevIssue;
    },
    async init() {
      this.issueId = this.$route.params.id;
      await this.queryMpassDevIssue({ issue_id: this.$route.params.id });
      this.tabelData = this.mpassDevIssue;
      // 查询版本管理员等数据
      if (this.tabelData.length > 0) {
        await this.queryMpassReleaseIssueDetail({
          id: this.tabelData[0].issue_id
        });
      }
      this.isVersionManage();
      await this.queryMultiIssueRecord({
        id: this.$route.params.id,
        type: 'mpass_release'
      });
      await this.queryComponentDetail({
        id: this.$route.query.component_id
      });
      this.isComManage();
      await this.queryMpassReleaseIssue({
        component_id: this.$route.query.component_id
      });
      this.isManager();
      this.issueRecord = this.issueMpassRecord;
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.form-message
  width 500px
  margin 0 auto
.ellipsis
  width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
.btn-width
 width 30px;
.btn1-width
 width 55px;
</style>
