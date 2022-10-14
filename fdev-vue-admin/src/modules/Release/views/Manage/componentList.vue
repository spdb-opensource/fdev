<template>
  <div>
    <Loading :visible="loading">
      <div class="row q-pa-md items-center">
        <div class="col">
          <fdev-select
            :value="selectValue"
            multiple
            use-input
            hide-dropdown-icon
            ref="select"
            @new-value="addSelect"
            class="inline-block"
            @input="updateSelectValue($event)"
          >
            <template v-slot:append>
              <f-icon name="search" flat @click="setSelect($refs.select)" />
            </template>
          </fdev-select>
          <span class="text-grey-6 inline-block search q-ml-sm">
            检索到
            <span class="text-h6">
              {{ filterLength }}
            </span>
            个{{ componentReleaseType }}
          </span>
        </div>
        <div class="col text-right">
          <span class="text-grey-6">
            共
            <span class="text-h6">
              {{ filterLength }}
            </span>
            个{{ componentReleaseType }}
          </span>
        </div>
      </div>
      <fdev-list
        class="q-pa-sm"
        v-if="componentList && componentList.length > 0"
      >
        <fdev-separator />

        <template v-for="(item, i) in filterData">
          <fdev-expansion-item
            default-opened
            :key="item.type"
            v-show="item.children.length > 0"
          >
            <fdev-table
              noExport
              :title="item.type"
              :data="item.children"
              :pagination.sync="paginationTable"
              :columns="tableColumns"
              hide-bottom
              grid
              no-select-cols
              no-top-bottom
            >
              <template v-slot:item="props">
                <div class="col-4 q-pa-sm">
                  <fdev-card
                    class="shadow-1"
                    square
                    :class="props.row.new_add_sign === '0' ? 'div-border' : ''"
                  >
                    <fdev-card-section class="row no-wrap items-start">
                      <div class="col">
                        <router-link
                          :to="
                            `${props.row.componentReleasePath}/${
                              props.row.component_id
                            }`
                          "
                        >
                          <div class="text-h6 text-title inline-block">
                            {{ props.row.module_name_en }}
                          </div>
                        </router-link>
                      </div>
                    </fdev-card-section>
                    <fdev-card-section>
                      <div class="text-grey-9">
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            {{ componentReleaseType }}名称
                          </div>
                          <div
                            v-if="props.row.module_name_zh"
                            class="col-md-8 col-sm-7 text-grey-8 overflow"
                            :title="props.row.module_name_zh"
                          >
                            <router-link
                              :to="
                                `${props.row.componentReleasePath}/${
                                  props.row.component_id
                                }`
                              "
                              class="link"
                            >
                              {{ props.row.module_name_zh }}
                              <fdev-popup-proxy context-menu>
                                <fdev-banner style="max-width:300px">
                                  {{ props.row.module_name_zh }}
                                </fdev-banner>
                              </fdev-popup-proxy>
                            </router-link>
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            {{ componentReleaseType }}负责人
                          </div>
                          <div
                            class="col-md-8 col-sm-7 overflow"
                            v-if="props.row.module_name_zh"
                            :title="
                              props.row.module_spdb_managers &&
                                props.row.module_spdb_managers
                                  .map(v => v.user_name_cn)
                                  .join(',')
                            "
                          >
                            <span
                              v-for="(user, index) in props.row
                                .module_spdb_managers"
                              :key="index"
                            >
                              <router-link
                                :to="`/user/list/${user.user_id}`"
                                class="col-md-9 col-sm-8 link overflow"
                              >
                                {{ user.user_name_cn }}
                              </router-link>
                            </span>
                            <fdev-popup-proxy context-menu>
                              <fdev-banner style="max-width:300px">
                                {{
                                  props.row.module_spdb_managers &&
                                    props.row.module_spdb_managers
                                      .map(v => v.user_name_cn)
                                      .join(',')
                                }}
                              </fdev-banner>
                            </fdev-popup-proxy>
                          </div>
                        </div>
                        <div
                          class="q-mb-sm row"
                          v-if="releaseDetail.type !== '6'"
                        >
                          <div class="col-md-4 col-sm-5">
                            {{ componentReleaseType }}类型
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 overflow"
                            :title="
                              (props.row.module_type === 'back'
                                ? '后端'
                                : '前端') + componentReleaseType
                            "
                          >
                            {{ props.row.module_type | filterType
                            }}{{ componentReleaseType }}
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            release分支
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 overflow"
                            :title="props.row.release_branch || '-'"
                          >
                            {{ props.row.release_branch || '-' }}
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            TAG列表
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 overflow cursor-pointer"
                            v-if="props.row.product_tag.length > 0"
                          >
                            {{ props.row.product_tag[0] }}
                            <f-icon name="arrow_d_f" />
                            <fdev-popup-proxy class="fixheight">
                              <div
                                v-for="data in props.row.product_tag"
                                :key="data"
                              >
                                <p class="q-mx-md q-my-sm">{{ data }}</p>
                                <fdev-separator />
                              </div>
                            </fdev-popup-proxy>
                          </div>
                        </div>
                      </div>
                    </fdev-card-section>

                    <fdev-separator
                      v-if="
                        (isBankMaster(props.row.module_spdb_managers) ||
                          isKaDianManager) &&
                          compareTime
                      "
                    />
                    <fdev-card-section
                      v-if="
                        (isBankMaster(props.row.module_spdb_managers) ||
                          isKaDianManager) &&
                          compareTime
                      "
                    >
                      <div class="q-gutter-x-md">
                        <div class="inline-block">
                          <!-- <fdev-tooltip
                            anchor="top middle"
                            v-if="!props.row.rel_env_name"
                          >
                            请选择REL测试环境
                          </fdev-tooltip> -->
                          <fdev-btn
                            label="提交发布"
                            flat
                            @click="handleConfirmModalOpen('0', props.row)"
                          />
                        </div>
                        <fdev-btn
                          label="CI/CD"
                          flat
                          @click="handlePipelineOpen(props.row)"
                        />
                        <div
                          class="inline-block"
                          v-if="props.row.product_tag[0]"
                        >
                          <!-- <fdev-tooltip
                            anchor="top middle"
                            v-if="!props.row.rel_env_name"
                          >
                            请选择REL测试环境
                          </fdev-tooltip> -->
                          <!-- <fdev-btn
                            label="重新打包"
                            flat
                            :disable="!props.row.product_tag"
                            @click="handleConfirmModalOpen('1', props.row)"
                            color="primary"
                          /> -->
                        </div>
                      </div>
                    </fdev-card-section>
                  </fdev-card>
                </div>
              </template>
            </fdev-table>
          </fdev-expansion-item>

          <fdev-separator :key="i" v-show="item.children.length > 0" />
        </template>
      </fdev-list>

      <div class="text-center q-mt-md" v-else>
        <f-image name="no_data" />
      </div>
    </Loading>

    <f-dialog v-model="confirmModalOpen" right :title="tipMsg">
      <div v-show="operateType == '0'">
        <f-formitem
          fullWidth
          label="实际发布版本"
          class="text-left normal-font"
          style="padding-bottom:20px"
          v-if="
            releaseDetail.type === '4' ||
              (releaseDetail.type === '5' && this.module_type === 'back')
          "
        >
          <fdev-toggle
            v-model="$v.publishModel.versionStatus.$model"
            true-value="1"
            false-value="0"
            left-label
          />
        </f-formitem>
        <f-formitem
          diaS
          label="预设版本号"
          required
          v-if="
            (releaseDetail.type === '4' ||
              (releaseDetail.type === '5' && this.module_type === 'back')) &&
              publishModel.versionStatus === '1'
          "
        >
          <fdev-select
            v-if="
              (releaseDetail.type === '4' ||
                (releaseDetail.type === '5' && this.module_type === 'back')) &&
                publishModel.versionStatus === '1'
            "
            use-input
            @filter="predictVersionFilter"
            :options="versionListOption"
            option-label="name"
            v-model="$v.publishModel.predict_version.$model"
            ref="publishModel.predict_version"
            :rules="[
              () =>
                $v.publishModel.predict_version.require || '请选择预设版本号'
            ]"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          diaS
          label="目标版本号"
          required
          v-if="
            ((releaseDetail.type === '4' ||
              (releaseDetail.type === '5' && this.module_type === 'back')) &&
              publishModel.versionStatus === '0') ||
              (releaseDetail.type === '5' && this.module_type === 'mpass')
          "
          :help="
            releaseDetail.type === '4' ||
            (releaseDetail.type === '5' && this.module_type === 'back')
              ? '回显上一次提交的目标版本号，请用户自行修改'
              : ''
          "
        >
          <fdev-input
            v-if="
              ((releaseDetail.type === '4' ||
                (releaseDetail.type === '5' && this.module_type === 'back')) &&
                publishModel.versionStatus === '0') ||
                (releaseDetail.type === '5' && this.module_type === 'mpass')
            "
            hint=""
            autofocus
            v-model="$v.publishModel.target_version.$model"
            ref="publishModel.target_version"
            :rules="[
              () => $v.publishModel.target_version.require || '请选择目标版本号'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="历史版本"
          diaS
          style="padding-bottom:20px"
          v-if="
            releaseDetail.type === '4' ||
              (releaseDetail.type === '5' && this.module_type === 'back')
          "
        >
          <div
            class="col-md-8 col-sm-7 text-grey-8 overflow cursor-pointer"
            v-if="versionHistory.length > 0"
          >
            {{ versionHistory[0] }}
            <f-icon name="arrow_d_f" />
            <fdev-popup-proxy class="fixheight">
              <div v-for="data in versionHistory" :key="data">
                <p class="q-mx-md q-my-sm">{{ data }}</p>
                <fdev-separator />
              </div>
            </fdev-popup-proxy>
          </div>
        </f-formitem>
        <f-formitem label="描述信息" diaS required>
          <fdev-input
            v-model="$v.publishModel.description.$model"
            ref="publishModel.description"
            type="textarea"
            :rules="[
              () => !$v.publishModel.description.$error || '请输入描述信息'
            ]"
          />
        </f-formitem>
        功能说明：
        <ol v-if="releaseDetail.type <= '3'">
          <li>提交后将发起此release分支至master分支的合并请求</li>
          <li>
            合并完毕后将从master分支拉取tag，tag名称：pro-{窗口名称}-{3位序号}
          </li>
          <li>
            tag分支拉取后将触发gitlab
            CI/CD,打包生成docker镜像并发布至准生产测试环境
          </li>
          <li>
            投产相关文件请至gitlab项目获取，通过变更详情页上传
          </li>
        </ol>
        <ol v-else>
          <li>提交后将发起此release分支至master分支的合并请求</li>
          <li>
            合并完毕后将从master分支拉取tag
          </li>
          <li>
            tag分支拉取后将触发gitlab CI/CD,打包生成{{
              componentReleaseType
            }}的发布版本
          </li>
        </ol>
      </div>
      <div v-show="operateType == '1'">
        <f-formitem label="tag分支" diaS>
          <fdev-select
            v-model="product_tag"
            :options="options"
            @filter="filterFn"
          />
        </f-formitem>
        功能说明：
        <p>
          本功能适用于镜像仓库误删除情境，从选择tag分支直接打出生产docker镜像
        </p>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :disable="disabled"
          :loading="
            releaseDetail.type === '6'
              ? globalLoading['releaseForm/relDevopsBaseImage']
              : validateVsersionLoading ||
                globalLoading['releaseForm/relDevopsComponent'] ||
                globalLoading['releaseForm/relDevopsArchetype']
          "
          @click="submit(operateType)"
        />
      </template>
    </f-dialog>

    <f-dialog v-model="examine" title="待审核任务关联" right>
      <p>
        该应用有任务未完成关联项审核，请联系DBA审核人、对应的任务负责人或行内项目负责人审核，点击
        <fdev-btn flat ficon="board_s_f" @click="sendEmail" />
        发送邮件通知
      </p>

      <fdev-table
        fdev-table
        title="待审核任务关联项"
        :data="examineData"
        :columns="columns"
        class="bg-white"
        titleIcon="list_s_f"
        noExport
      >
      </fdev-table>
    </f-dialog>

    <f-dialog v-model="pipelinesOpen" v-if="pipelinesOpen" title="CI/CD" right>
      <Pipelines
        v-show="!jobOpen"
        :rowNumber="5"
        class="pipline-width"
        :application_id="application_id"
        :release_node_name="release_node_name"
        @job="handleJobOpen"
      />
      <Job class="q-mt-lg" v-if="jobOpen" :project_id="params" />
      <template v-slot:btnSlot>
        <fdev-btn label="返回" dialog @click="jobOpen = false" />
      </template>
    </f-dialog>

    <f-dialog
      right
      v-model="publishFailureMessageDialogOpened"
      title="提交发布失败"
    >
      <div class="message-wrapper" v-if="publishFailureMessage.stage_task_name">
        <div class="header q-mb-sm">以下任务尚未进入 UAT 阶段：</div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.stage_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="publishFailureMessage.report_task_name"
      >
        <div class="header q-mb-sm">
          以下任务业测报告未到达，请前往需求文档库上传业测报告！
        </div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.report_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="publishFailureMessage.confirm_task_name"
      >
        <div class="header q-mb-sm">
          以下任务确认书未到达，请前往需求文档库上传上线确认书、任务详情打开上线确认书开关！
        </div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.confirm_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="publishFailureMessage.safety_task_name"
      >
        <div class="header q-mb-sm">以下任务未通过安全测试：</div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.safety_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="
          publishFailureMessage.system &&
            publishFailureMessage.system.length > 0
        "
      >
        <div class="header q-mb-md">
          当前{{ componentReleaseType }}未绑定所属系统，请绑定:
          <fdev-form @submit.prevent="handleBindSystem" ref="bindModel">
            <div class="row">
              <fdev-select
                use-input
                @filter="systemInputFilter"
                ref="bindModel.systemId"
                filled
                label="系统名称"
                v-model="$v.bindModel.systemId.$model"
                :options="filterSystem"
                class="col-3 bind-width q-pb-md q-mt-md"
                option-label="name"
                :rules="[
                  () => $v.bindModel.systemId.required || '请选择系统名称'
                ]"
              >
              </fdev-select>
              <div class="q-pb-md q-mt-md q-ml-lg">
                <fdev-btn
                  :loading="globalLoading['appForm/bindSystem']"
                  color="primary"
                  label="绑定"
                  type="submit"
                  @click="handleAddUserAllTip"
                />
              </div>
            </div>
          </fdev-form>
        </div>
      </div>
    </f-dialog>
  </div>
</template>

<script>
import { taskListColumn, appListColumn } from '../../utils/constants';
import { testEnv, batchArr } from '../../utils/model';
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import Pipelines from '@/modules/App/components/pipelines';
import Job from '@/modules/App/views/pipelines/jobs';
import {
  successNotify,
  validate,
  deepClone,
  resolveResponseError
} from '@/utils/utils';
import {
  judgeTargetVersionComponent,
  judgeTargetVersionarchetype
} from '@/services/release';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'JobList',
  components: {
    Loading,
    Pipelines,
    Job
  },
  data() {
    return {
      filterSystem: [],
      versionListOption: [],
      loading: true,
      rowData: {},
      filter: '',
      tableData: [],
      filterData: [],
      tipMsg: '拒绝原因',
      operateType: '0',
      confirmModalOpen: false,
      detail: {},
      disabled: false,
      product_tag: '',
      application_id: '',
      options: [],
      publishModel: {
        description: '',
        target_version: '',
        predict_version: '',
        versionStatus: '1'
      },
      examine: false, // 审核窗口
      examineData: [],
      columns: taskListColumn,
      cloneData: {},
      testReason: [
        { label: '正常', value: '1' },
        { label: '需求变更', value: '3' }
      ],
      testEnv: testEnv(), // 注释
      release_branch: '',
      releaseNodeName: '',
      pipelinesOpen: false,
      jobOpen: false,
      params: {},
      tableColumns: appListColumn,
      filterValue: '',
      paginationTable: {
        rowsPerPage: 0
      },
      testRuningDialogOpened: false,
      selectedTask: [],
      confirmConfigOpen: false,
      reinforce: '0',
      publishFailureMessageDialogOpened: false,
      bindModel: {
        systemId: ''
      },
      systemOptions: [],
      module_type: '', // 前端还是后端mpass-前端；back-后端
      validateVsersionLoading: false,
      versionHistory: [] // 历史版本号数据
    };
  },
  validations: {
    publishModel: {
      description: {
        required
      },
      predict_version: {
        require(val) {
          if (
            (this.releaseDetail.type === '4' ||
              (this.releaseDetail.type === '5' &&
                this.module_type === 'back')) &&
            this.publishModel.versionStatus === '1'
          ) {
            return val.trim();
          }
          return true;
        }
      },
      target_version: {
        require(val) {
          if (
            (this.releaseDetail.type === '4' ||
              this.releaseDetail.type === '5') &&
            this.publishModel.versionStatus === '0'
          ) {
            return val.trim();
          }
          return true;
        }
      },
      versionStatus: {}
    },
    bindModel: {
      systemId: {
        required
      }
    }
  },
  filters: {
    filterComfirm(val) {
      return val === '1' ? '已确认' : '未确认';
    },
    filterType(type) {
      let name = '';
      switch (type) {
        case 'back':
          name = '后端';
          break;
        case 'mpass':
          name = '前端';
          break;
        default:
          name = '';
      }
      return name;
    }
  },
  watch: {
    pipelinesOpen(val) {
      this.jobOpen = false;
    },
    selectValue(val) {
      this.filterMethods(val);
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/releaseAppList', ['selectValue']),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('releaseForm', [
      'compareTime',
      'componentList',
      'examineList',
      'tagsList',
      'releaseDetail',
      'targetVersionList',
      'tasksOfAppType',
      'releaseVersionComponent',
      'releaseVersionArchetype',
      'versionComponent',
      'versionArchetype'
    ]),
    ...mapGetters('userForm', ['wrapTotal']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    filterLength() {
      return this.filterData.reduce((sum, i) => {
        return sum + i.children.length;
      }, 0);
    },
    publishFailureMessage() {
      if (!this.tasksOfAppType) {
        return {};
      }
      // this.systemOptions = this.tasksOfAppType.system;
      const {
        stage_task_name,
        report_task_name,
        confirm_task_name,
        safety_task_name,
        system,
        application_id
      } = this.tasksOfAppType;
      return {
        stage_task_name:
          Object.keys(stage_task_name).length === 0 ? '' : stage_task_name,
        report_task_name:
          Object.keys(report_task_name).length === 0 ? '' : report_task_name,
        confirm_task_name:
          Object.keys(confirm_task_name).length === 0 ? '' : confirm_task_name,
        safety_task_name:
          Object.keys(safety_task_name).length === 0 ? '' : safety_task_name,
        system: system ? system : [],
        application_id: application_id
      };
    },
    // 窗口类型
    componentReleaseType() {
      switch (this.releaseDetail.type) {
        case '4':
          return '组件';
        case '5':
          return '骨架';
        case '6':
          return '镜像';
        default:
          return '';
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/releaseAppList', [
      'updateSelectValue'
    ]),
    ...mapActions('appForm', ['queryApps', 'bindSystem']),
    ...mapActions('releaseForm', {
      queryApply: 'queryApply',
      queryComponent: 'queryComponent',
      packageFromTag: 'packageFromTag',
      queryReleaseVersionComponent: 'queryReleaseVersionComponent',
      queryReleaseVersionArchetype: 'queryReleaseVersionArchetype',
      queryLatestVersionComponent: 'queryLatestVersionComponent',
      queryLatestVersionArchetype: 'queryLatestVersionArchetype',
      relDevopsComponent: 'relDevopsComponent',
      relDevopsArchetype: 'relDevopsArchetype',
      relDevopsBaseImage: 'relDevopsBaseImage',
      queryTasksReviews: 'queryTasksReviews',
      queryApplicationTags: 'queryApplicationTags',
      sendEmailForTaskManagers: 'sendEmailForTaskManagers',
      tasksInSitStage: 'tasksInSitStage',
      queryTargetVersion: 'queryTargetVersion'
    }),
    systemInputFilter(val, update) {
      update(() => {
        this.filterSystem = this.publishFailureMessage.system.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    predictVersionFilter(val, update) {
      update(() => {
        this.versionListOption = this.targetVersionList;
      });
    },
    handleAddUserAllTip() {
      this.$v.bindModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('bindModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.bindModel.$invalid) {
        return;
      }
    },
    async handleBindSystem() {
      await this.bindSystem({
        systemId: this.bindModel.systemId.id,
        appId: this.publishFailureMessage.application_id
      }),
        successNotify('绑定成功');
      this.publishFailureMessageDialogOpened = false;
    },

    // 清理弹窗信息
    clear() {
      this.publishModel.description = '';
      this.publishModel.target_version = '';
      this.publishModel.predict_version = '';
      this.publishModel.versionStatus = '1';
    },
    /* 打开确认操作modal */
    async handleConfirmModalOpen(num, data) {
      this.application_id = data.component_id;
      this.module_type = data.module_type;
      this.clear();
      const params = {
        application_id: data.component_id,
        release_node_name: this.release_node_name
      };
      await this.queryTasksReviews(params);
      if (this.examineList.length > 0) {
        this.examineData = this.examineList;
        this.examine = true;
        this.cloneData = deepClone(data);
        return;
      }
      /* 重新打包前，发送接口查询该应用下是否有任务未进入rel阶段 */
      if (num === '0') {
        await this.tasksInSitStage(params);
        const { status } = this.tasksOfAppType;
        if (status !== '0') {
          this.publishFailureMessageDialogOpened = true;
          return;
        }
      }
      this.confirmModalOpen = true;
      this.tipMsg =
        num == '0'
          ? '输入本次合并请求提交信息'
          : '请选择已有tag分支重新进行打包操作';
      if (
        this.releaseDetail.type === '4' ||
        (this.releaseDetail.type === '5' && this.module_type === 'back')
      ) {
        // 查询预设版本号
        await this.queryTargetVersion({
          ...params,
          type: this.releaseDetail.type
        });
      }
      if (this.releaseDetail.type === '4') {
        // 查询历史版本
        await this.queryReleaseVersionComponent({
          component_id: data.component_id,
          type: this.module_type
        });
        this.versionHistory = this.releaseVersionComponent;
        // 查询提交发布时前后端组件输入的最近目标版本号
        await this.queryLatestVersionComponent({
          release_node_name: this.release_node_name,
          component_id: data.component_id,
          type: this.module_type
        });
        this.publishModel.target_version = this.versionComponent
          ? this.versionComponent
          : '';
      }
      if (this.releaseDetail.type === '5' && this.module_type === 'back') {
        // 查询历史版本
        await this.queryReleaseVersionArchetype({
          archetype_id: data.component_id
        });
        this.versionHistory = this.releaseVersionArchetype;
        // 查询提交发布时后端骨架输入的最近目标版本号
        await this.queryLatestVersionArchetype({
          release_node_name: this.release_node_name,
          archetype_id: data.component_id
        });
        this.publishModel.target_version = this.versionArchetype
          ? this.versionArchetype
          : '';
      }
      this.product_tag = data.product_tag[0];
      this.detail = data;
      this.operateType = num;
    },
    /* confirmModal 确认 并调交易 */
    async submit(type) {
      this.disabled = true;
      if (type == '0') {
        this.$v.publishModel.$touch();
        let jobKeys = Object.keys(this.$refs).filter(
          key => key.indexOf('publishModel') > -1
        );
        validate(jobKeys.map(key => this.$refs[key]));
        this.disabled = false;
        if (this.$v.publishModel.$invalid) {
          return;
        }
        this.operate('0', this.detail);
      } else {
        this.operate('1', this.detail);
      }
    },
    // 校验版本号
    async validateVsersion(status) {
      let params = {
        target_version: this.publishModel.target_version
      };
      try {
        this.validateVsersionLoading = true;
        if (this.releaseDetail.type === '4') {
          // 校验前后端组件版本号
          await resolveResponseError(() =>
            judgeTargetVersionComponent({
              ...params,
              component_id: status.component_id,
              type: this.module_type
            })
          );
        } else {
          // 校验后端骨架版本号
          await resolveResponseError(() =>
            judgeTargetVersionarchetype({
              ...params,
              archetype_id: status.component_id
            })
          );
        }
        this.validateVsersionLoading = false;
        return true;
      } catch {
        this.validateVsersionLoading = false;
        return false;
      }
    },
    async operate(msg, status) {
      if (msg === '0') {
        let params = {
          branch: status.release_branch,
          release_node_name: this.release_node_name,
          description: this.publishModel.description
        };
        if (
          (this.releaseDetail.type === '4' &&
            this.publishModel.versionStatus === '0' &&
            this.publishModel.target_version) ||
          (this.releaseDetail.type === '5' &&
            this.module_type === 'back' &&
            this.publishModel.versionStatus === '0' &&
            this.publishModel.target_version)
        ) {
          if (!(await this.validateVsersion(status))) return;
        }
        if (this.releaseDetail.type === '4') {
          // 组件提交发布窗口
          await this.relDevopsComponent({
            ...params,
            type: this.module_type,
            component_id: status.component_id,
            target_version:
              this.publishModel.versionStatus === '0'
                ? this.publishModel.target_version || ''
                : '',
            predict_version:
              this.publishModel.versionStatus === '1'
                ? this.publishModel.predict_version || ''
                : ''
          });
        } else if (this.releaseDetail.type === '5') {
          let archetypeparams;
          if (this.module_type === 'back') {
            archetypeparams = {
              target_version:
                this.publishModel.versionStatus === '0'
                  ? this.publishModel.target_version || ''
                  : '',
              predict_version:
                this.publishModel.versionStatus === '1'
                  ? this.publishModel.predict_version || ''
                  : ''
            };
          } else {
            archetypeparams = {
              target_version: this.publishModel.target_version || '',
              predict_version: ''
            };
          }
          // 骨架提交发布窗口
          await this.relDevopsArchetype({
            ...params,
            type: this.module_type,
            archetype_id: status.component_id,
            ...archetypeparams
          });
        } else {
          // 镜像提交发布窗口
          await this.relDevopsBaseImage({
            ...params,
            image_id: status.component_id
          });
        }
        this.disabled = false;
        this.confirmModalOpen = false;
        this.publishModel.description = '';
        successNotify('已发起release分支至master分支的合并请求');
        this.init();
      }
      if (msg === '1') {
        let params = {
          application_id: status.application_id,
          release_node_name: this.$route.params.id,
          product_tag: this.product_tag
        };
        this.confirmModalOpen = false;
        this.validateVsersionLoading = false;
        this.disabled = false;
        await this.packageFromTag(params);
        successNotify('正在进行tag打包,请等待镜像打包完成。');
        this.init();
      }
    },
    isBankMaster(bankMasterList) {
      if (!Array.isArray(bankMasterList) || bankMasterList.length === 0)
        return false;
      return bankMasterList.some(
        item => item.user_name_en === this.currentUser.user_name_en
      );
    },
    async init() {
      this.loading = true;
      // await this.queryApply(this.$route.params.id);
      await this.queryComponent({
        release_node_name: this.$route.params.id,
        type: this.releaseDetail.type
      });
      this.handleTableData();
      this.loading = false;
    },
    async sendEmail() {
      // 控制当前应用不能连续点击邮件发送
      if (!this.cloneData.sendEmailFlag) {
        this.cloneData.sendEmailFlag = true;
      } else {
        return;
      }
      let params = {
        release_tasks: this.examineData
      };
      await this.sendEmailForTaskManagers(params);
      successNotify('发送邮件成功');
      this.examine = false;
    },
    // tag下拉懒加载
    filterFn(val, update, abort) {
      update(async () => {
        await this.queryApplicationTags({
          application_id: this.application_id,
          release_node_name: this.$route.params.id
        });
        this.options = this.tagsList;
      });
    },
    handlePipelineOpen(item) {
      this.application_id = item.component_id;
      this.pipelinesOpen = true;
    },
    handleJobOpen(params) {
      this.params = params;
      this.jobOpen = true;
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    handleTableData() {
      this.tableData = this.componentList.reduce((sum, i) => {
        switch (this.releaseDetail.type) {
          case '4':
            if (i.module_type === 'back') {
              // 后端组件
              i.componentReleasePath = '/componentManage/server/list';
            } else {
              // 前端组件
              i.componentReleasePath = '/componentManage/web/weblist';
            }
            break;
          case '5':
            if (i.module_type === 'back') {
              // 后端骨架
              i.componentReleasePath = '/archetypeManage/server/archetype';
            } else {
              // 前端骨架
              i.componentReleasePath = '/archetypeManage/web/webArchetype';
            }
            break;
          case '6':
            i.componentReleasePath = '/imageManage';
            break;
          default:
            i.componentReleasePath = '';
        }
        const batch = i.batch_id ? Number(i.batch_id) : 4;
        sum[batch - 1].children.push(i);
        return sum;
      }, batchArr());
      this.filterMethods();
    },
    filterMethods() {
      if (this.selectValue.length === 0) {
        this.filterData = this.tableData;
        return;
      }
      this.filterData = this.tableData.map(item => {
        const itemData = item.children.filter(data => {
          return this.selectValue.some(selected =>
            this.handleSearchData(Object.values(data), selected)
          );
        });
        return { ...item, children: itemData };
      });
    },
    handleSearchData(data, search) {
      return data.some(item => {
        if (typeof item === 'object' && item !== null) {
          const isArray = Array.isArray(item);
          const arr = isArray ? item : Object.values(item);
          return this.handleSearchData(arr, search);
        }
        return typeof item === 'string' && item.includes(search);
      });
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    }
  },
  async created() {
    this.release_node_name = this.$route.params.id;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>

.overflow
  overflow hidden
  white-space nowrap
  text-overflow ellipsis
.icon
  font-size: 20px
.fixheight
  max-height 800px
  overflow auto
.imageUrl
  width calc(100% - 20px)
  overflow hidden
  white-space nowrap
  text-overflow ellipsis
  display inline-block
  vertical-align top
.search
  vertical-align: text-top;
.inline-block
  &.q-mt-sm
    vertical-align: top
.span-link
  color #2196f3
  cursor pointer
.min-width
  min-width: 400px
.div-border
 border-top solid 4px #21BA45
.header
  font-weight 700
  color #616161
.bind-width
 width 70%
.pipline-width
  width 900px
</style>
