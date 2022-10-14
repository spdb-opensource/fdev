<template>
  <loading :visible="loading">
    <div>
      <div>
        <div class="row header">
          <div class="Headleft"></div>
          <div class="felx1">
            <div class="h66 row items-center border-bottom">
              <div
                :title="job.name"
                class="q-mr-sm titleStyle felx1 ellipsis-2-lines"
              >
                任务名称：{{ job.name }}
              </div>
              <div
                v-for="(lab, index) in job.tag"
                :key="index"
                class="badgeStyle"
              >
                <span v-if="job && job.simpleDemand !== '0'"> {{ lab }}</span>
              </div>
            </div>
            <div class="h70 row items-center justify-between">
              <div class="row">
                <div>
                  <div class="row items-center h22">
                    <span class="numTitle q-mr-md">任务编号</span>
                    <span class="idStyle">{{ job.id }}</span>
                    <div v-if="taskType != 1 && taskType != 2" class="q-ml-xs">
                      <f-icon
                        name="help_c_o"
                        class="text-primary cursor-pointer q-ml-xs"
                        :width="16"
                        :height="16"
                      />
                      <fdev-tooltip
                        position="right"
                        anchor="top right"
                        self="center middle"
                        :offest="[0, 0]"
                      >
                        1、提交业务测试后,点击"修改"按钮,填写"实际提交用户测试日期",可将任务状态改为UAT。<br />
                        2、业务测试完成后,点击"修改"按钮,填写"实际用户测试完成日期",可将任务状态改为REL。
                      </fdev-tooltip>
                    </div>
                  </div>
                  <div class="q-pt-xs">
                    <span class="numTitle q-mr-md">任务类型</span>
                    <span :class="typeColor(jobSatuts(job.taskType))">{{
                      jobSatuts(job.taskType)
                    }}</span>
                  </div>
                </div>
                <div class="q-ml-lt">
                  <div class="numTitle row items-center">
                    <span class="q-mr-md">任务状态</span>
                    <div
                      class="statuStyle flex items-center"
                      :class="statusBackground(job.stage.value).color"
                    >
                      <div class="dian q-mr-xs"></div>
                      <div class="statuC">
                        {{
                          job.taskSpectialStatus == '1' ||
                          job.taskSpectialStatus == '2'
                            ? '暂缓'
                            : statusBackground(job.stage.value).label
                        }}
                      </div>
                    </div>
                  </div>
                  <div v-if="job.applicationType" class="q-pt-xs">
                    <span class="numTitle q-mr-md">应用类型</span>
                    <span class="taskTypeS">{{
                      applicationSatuts(job.applicationType)
                    }}</span>
                  </div>
                </div>
              </div>
              <div>
                <span class="btn-radius">
                  <span v-if="taskType == 2">
                    <Authorized
                      v-if="job.stage.code > -1 && job.stage.code < 5"
                      :include-me="addJobRole"
                    >
                      <div class="q-gutter-md">
                        <div class="row no-wrap">
                          <fdev-btn
                            class="q-ml-md"
                            dialog
                            ficon="confirm"
                            v-if="!onlyCreator && job.stage.code !== 4"
                            label="确认完成"
                            :disable="!isManager"
                            @click="confirmFinish"
                          />
                          <fdev-btn
                            class="q-ml-md"
                            dialog
                            ficon="edit"
                            label="修改"
                            :disable="
                              job.taskSpectialStatus == 1 ||
                                job.taskSpectialStatus == 2
                            "
                            v-if="job.stage.code !== 4"
                            @click="updateDetail()"
                          />
                          <fdev-btn
                            dialog
                            ficon="keep_file"
                            class="q-ml-md"
                            label="归档"
                            v-if="job.stage.value === 'production'"
                            @click="handleHint()"
                          />
                          <fdev-btn
                            dialog
                            ficon="delete_o"
                            label="删除"
                            class="q-ml-md"
                            v-if="job.stage.code !== 4"
                            :disable="
                              job.taskSpectialStatus == 1 ||
                                job.taskSpectialStatus == 2
                            "
                            @click="handleDeleteJob()"
                          />
                        </div>
                      </div>
                    </Authorized>
                    <Authorized
                      :include-me="addJobRole"
                      v-if="job.stage.code < 0"
                    >
                      <template>
                        <div class="btnFlex">
                          <div>
                            <fdev-tooltip
                              v-if="
                                job.taskSpectialStatus == 1 ||
                                  job.taskSpectialStatus == 2
                              "
                              anchor="top middle"
                              self="center middle"
                              :offest="[-20, 0]"
                            >
                              <span>处于暂缓状态</span>
                            </fdev-tooltip>
                            <fdev-btn
                              dialog
                              ficon="delete_o"
                              class="q-ml-md"
                              label="删除"
                              :disable="
                                job.taskSpectialStatus == 1 ||
                                  job.taskSpectialStatus == 2
                              "
                              @click="handleDeleteJob()"
                            />
                          </div>
                        </div>
                      </template>
                    </Authorized>
                  </span>
                  <span v-if="taskType != 2">
                    <Authorized
                      v-if="
                        job.stage.code < 5 && job.stage.code > -1 && btnDisplay
                      "
                    >
                      <div class="q-gutter-md">
                        <div class="row no-wrap">
                          <fdev-btn
                            dialog
                            ficon="confirm"
                            v-if="job.stage.value === 'rel' && taskType != 2"
                            label="确认已投产"
                            :disable="!isManager"
                            @click="confirmHadRelease"
                          />
                          <fdev-btn
                            dialog
                            ficon="cancel_c_o"
                            class="q-ml-md"
                            v-if="taskType != 2"
                            label="任务废弃"
                            :disable="!isManager"
                            @click="abandonedTaskDialogOpened = true"
                          />
                          <fdev-tooltip
                            anchor="top middle"
                            self="center middle"
                            :offest="[0, 0]"
                            v-if="!isManager"
                          >
                            请联系负责人进行操作。
                          </fdev-tooltip>
                          <fdev-btn
                            class="q-ml-md"
                            dialog
                            ficon="edit"
                            label="修改"
                            :disable="
                              job.taskSpectialStatus == 1 ||
                                job.taskSpectialStatus == 2
                            "
                            v-if="job.stage.code !== 4 && addJobRole"
                            @click="updateDetail()"
                          />
                        </div>
                      </div>
                    </Authorized>
                    <Authorized
                      :include-me="role"
                      v-if="
                        job.stage.code < 5 && job.stage.code > -1 && !btnDisplay
                      "
                    >
                      <template>
                        <Authorized
                          :include-me="
                            job.partyB
                              .map(user => user.id)
                              .concat(job.partyA.map(user => user.id))
                              .concat(job.creator.id)
                          "
                        >
                          <template>
                            <div class="q-gutter-md">
                              <div class="row no-wrap">
                                <fdev-btn
                                  dialog
                                  ficon="confirm"
                                  v-if="
                                    (!onlyCreator || isKaDianManager) &&
                                      job.stage.value === 'rel' &&
                                      confirmIsRelease &&
                                      taskType != 2
                                  "
                                  label="确认已投产"
                                  :disable="!isManager"
                                  @click="confirmHadRelease"
                                />
                                <div>
                                  <fdev-tooltip
                                    v-if="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    anchor="top middle"
                                    self="center middle"
                                    :offest="[-20, 0]"
                                  >
                                    <span>处于暂缓状态</span>
                                  </fdev-tooltip>
                                  <fdev-btn
                                    dialog
                                    ficon="tool"
                                    class="q-ml-md"
                                    @click="tab = 'taskStage'"
                                    :disable="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    v-if="
                                      job.stage.code < 4 &&
                                        job.stage.code > -1 &&
                                        taskType != 2 &&
                                        tab !== 'taskStage'
                                    "
                                    label="处理"
                                  />
                                </div>
                                <div>
                                  <fdev-tooltip
                                    v-if="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    anchor="top middle"
                                    self="center middle"
                                    :offest="[-20, 0]"
                                  >
                                    <span>处于暂缓状态</span>
                                  </fdev-tooltip>
                                  <fdev-btn
                                    dialog
                                    ficon="edit"
                                    label="修改"
                                    class="q-ml-md"
                                    :disable="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    v-if="job.stage.code !== 4 && addJobRole"
                                    @click="updateDetail()"
                                  />
                                </div>
                                <fdev-btn
                                  dialog
                                  ficon="keep_file"
                                  class="q-ml-md"
                                  label="归档"
                                  v-if="job.stage.value === 'production'"
                                  @click="handleHint()"
                                />
                                <fdev-btn
                                  dialog
                                  ficon="parameters"
                                  class="q-ml-md"
                                  v-if="taskType != 1 && taskType != 2 && isApp"
                                  @click="modelSetting"
                                  label="环境配置参数"
                                />
                                <div>
                                  <fdev-tooltip
                                    v-if="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    anchor="top middle"
                                    self="center middle"
                                    :offest="[-20, 0]"
                                  >
                                    <span>处于暂缓状态</span>
                                  </fdev-tooltip>
                                  <fdev-btn
                                    dialog
                                    ficon="delete_o"
                                    label="删除"
                                    class="q-ml-md"
                                    :disable="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    v-if="job.stage.code !== 4"
                                    @click="handleDeleteJob"
                                  />
                                </div>
                              </div>
                            </div>
                          </template>
                          <template v-slot:exception>
                            <div class="q-gutter-md">
                              <div class="row no-wrap">
                                <div>
                                  <fdev-tooltip
                                    v-if="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    anchor="top middle"
                                    self="center middle"
                                    :offest="[-20, 0]"
                                  >
                                    <span>处于暂缓状态</span>
                                  </fdev-tooltip>
                                  <fdev-btn
                                    dialog
                                    ficon="tool"
                                    class="q-ml-md"
                                    @click="tab = 'taskStage'"
                                    v-if="
                                      job.stage.code < 4 &&
                                        job.stage.code > -1 &&
                                        taskType != 2 &&
                                        tab !== 'taskStage'
                                    "
                                    :disable="
                                      job.taskSpectialStatus == 1 ||
                                        job.taskSpectialStatus == 2
                                    "
                                    label="处理"
                                  />
                                </div>
                                <fdev-btn
                                  dialog
                                  ficon="parameters"
                                  class="q-ml-md"
                                  v-if="taskType != 1 && taskType != 2 && isApp"
                                  @click="modelSetting"
                                  label="环境配置参数"
                                />
                              </div>
                            </div>
                          </template>
                        </Authorized>
                      </template>
                      <template v-slot:exception>
                        <div class="q-gutter-md">
                          <div class="row no-wrap">
                            <div>
                              <fdev-tooltip
                                v-if="
                                  job.taskSpectialStatus == 1 ||
                                    job.taskSpectialStatus == 2
                                "
                                anchor="top middle"
                                self="center middle"
                                :offest="[-20, 0]"
                              >
                                <span>处于暂缓状态</span>
                              </fdev-tooltip>
                              <fdev-btn
                                dialog
                                ficon="tool"
                                class="q-ml-md"
                                @click="tab = 'taskStage'"
                                v-if="
                                  job.stage.code < 4 &&
                                    job.stage.code > -1 &&
                                    taskType != 2 &&
                                    tab !== 'taskStage'
                                "
                                :disable="
                                  job.taskSpectialStatus == 1 ||
                                    job.taskSpectialStatus == 2
                                "
                                label="处理"
                              />
                            </div>
                            <fdev-btn
                              dialog
                              ficon="parameters"
                              v-if="taskType != 1 && taskType != 2 && isApp"
                              class="q-ml-md"
                              @click="modelSetting"
                              label="环境配置参数"
                            />
                          </div>
                        </div>
                      </template>
                    </Authorized>
                    <Authorized
                      :include-me="addJobRole"
                      v-if="job.stage.code < 0"
                    >
                      <template>
                        <div class="btnFlex">
                          <div>
                            <fdev-tooltip
                              v-if="
                                job.taskSpectialStatus == 1 ||
                                  job.taskSpectialStatus == 2
                              "
                              anchor="top middle"
                              self="center middle"
                              :offest="[-20, 0]"
                            >
                              <span>处于暂缓状态</span>
                            </fdev-tooltip>
                            <fdev-btn
                              dialog
                              ficon="delete_o"
                              class="q-ml-md"
                              label="删除"
                              :disable="
                                job.taskSpectialStatus == 1 ||
                                  job.taskSpectialStatus == 2
                              "
                              @click="handleDeleteJob()"
                            />
                          </div>
                        </div>
                      </template>
                    </Authorized>
                  </span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <f-block class="q-mt-10 task-detail-tabs">
        <fdev-tabs v-model="tab" align="left" class="tabs-border-bottom">
          <!-- 基本信息 -->
          <fdev-tab name="taskInfo" label="基本信息" />
          <!-- 阶段处理 -->
          <fdev-tab
            name="taskStage"
            v-if="
              (taskType == null || taskType == '1') &&
                job.stage.code < 4 &&
                job.stage.code > -1
            "
            label="阶段处理"
          />
          <!-- 流水线 -->
          <fdev-tab
            name="pipeline"
            v-if="job.taskType == null"
            label="流水线"
          />
          <!-- 关联文档 -->
          <fdev-tab name="translation" label="关联文档" />
          <!--  -->
          <fdev-tab
            v-if="job.taskType != 2 && job.taskType != 1"
            name="testMsgs"
            label="提测信息"
          />
          <!-- sit测试结果 -->
          <fdev-tab
            name="testResult"
            v-if="job.taskType == null"
            label="sit测试结果"
          />
          <!-- 'uat测试结果' -->
          <fdev-tab
            name="uATtestResult"
            v-if="job.taskType == null"
            label="uat测试结果"
          />
          <!-- '代码分析' -->
          <fdev-tab
            name="codeAnalizy"
            v-if="isJavaTypeApp && job.branch && job.taskType != 2"
            label="代码分析"
          />
          <!-- '代码审批' -->
          <fdev-tab name="codeApprove" label="分支合并审批" />
          <fdev-tab name="productionProblems" label="生产问题" />
        </fdev-tabs>
      </f-block>
      <div class="q-mt-10">
        <!-- 基本信息 -->
        <TaskInfo
          ref="taskInfo"
          @updateDetail="updateDetailSuccess"
          v-show="tab == 'taskInfo'"
        />
        <!-- 阶段处理 -->
        <!-- null  开发任务  1 无代码任务  2 日常任务(无阶段处理) -->
        <!-- applicationType 无字段 没有关联应用   -->
        <TaskStage v-if="tab == 'taskStage'" />
        <!-- 流水线 -->
        <Pipeline v-if="tab == 'pipeline'" />
        <!-- 关联文档 -->
        <Translation
          v-if="tab == 'translation'"
          :groupName="job.group.name"
          :taskId="job.id"
          :taskName="job.name"
        />
        <!-- 提测信息 -->
        <TestMsgs v-if="tab == 'testMsgs'" />
        <!-- sit测试结果 -->
        <TestResult
          v-if="tab == 'testResult'"
          :isTest="job.isTest"
          :taskId="job.id"
          :taskName="job.name"
        />
        <!-- uat测试结果 -->
        <UATtestResult
          v-if="tab == 'uATtestResult'"
          :taskId="job.id"
          :taskName="job.name"
        />
        <!-- 代码分析 -->
        <CodeAnalizy v-if="tab == 'codeAnalizy'" />
        <CodeApprove v-if="tab == 'codeApprove'" :taskId="job.id" />
        <!-- 生产问题 -->
        <ProductionProblems
          v-if="tab == 'productionProblems'"
          isTask
          :unitNo="job.unitNo"
          :taskId="id"
        />
      </div>
      <!-- 删除任务 -->
      <f-dialog
        v-model="deleteModalOpened"
        transition-show="slide-up"
        transition-hide="slide-down"
        full-height
        title="删除任务"
      >
        <div class="q-px-lg">
          <p class="text-red">
            <f-icon name="alert_t_f" style="color:red" />
            <span class="red-tip">您确定删除该任务吗？</span>
          </p>
          <p v-if="queryDeleteJobProfile.feature_branch">
            当前任务所属分支：
            {{ queryDeleteJobProfile.feature_branch }}
          </p>
          <p>当前任务阶段：{{ job.stage.value }}</p>
          <p v-if="job.stage.code > 1 && queryDeleteJobProfile.release_branch">
            任务对应的UAT分支：{{ queryDeleteJobProfile.release_branch }}
          </p>
          <div
            v-if="
              queryDeleteJobProfile.product_tag &&
                queryDeleteJobProfile.product_tag.length > 0
            "
          >
            <span>TAG列表：</span>
            <div class="col-md-9 col-sm-8 text-grey-8 div-margin">
              {{ queryDeleteJobProfile.product_tag[0] }}
              <f-icon name="arrow_d_f" />
              <fdev-popup-proxy class="fixheight">
                <div
                  v-for="(item, index) in queryDeleteJobProfile.product_tag"
                  :key="index"
                >
                  <p class="q-mx-md q-my-sm">{{ item }}</p>
                  <fdev-separator />
                </div>
              </fdev-popup-proxy>
            </div>
          </div>
        </div>
        <template v-slot:btnSlot>
          <fdev-btn
            dialog
            :loading="globalLoading['jobForm/deleteJob']"
            label="确定"
            @click="deleteTask"
          />
        </template>
      </f-dialog>
      <!-- 确认投产 -->
      <f-dialog
        v-model="confirmProductDialog"
        transition-show="slide-up"
        transition-hide="slide-down"
        full-height
        title="确认投产"
      >
        <div>
          <div style="text-indent: 8px;">确认任务已投产？</div>
          <fdev-checkbox v-model="isToFile" label="是否归档?" />
        </div>
        <template v-slot:btnSlot>
          <fdev-btn
            dialog
            :loading="globalLoading['releaseForm/updateTaskArchived']"
            label="确定"
            @click="confirmProduct"
          />
        </template>
      </f-dialog>
      <UpdateTaskDetail
        ref="updateDetail"
        :taskType="taskType"
        :job="job"
        :id="id"
        :updateLoading="updateLoading"
        @updateSuccess="updateDetailSuccess"
      />
      <!-- 废弃任务 -->
      <AbandonedTaskDialog
        v-model="abandonedTaskDialogOpened"
        :id="id"
        :taskName="job.name"
        @complete="init"
      />
      <!-- 环境配置参数 -->
      <EnvSetting ref="envSetting" :job="job" />
    </div>
  </loading>
</template>
<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import { formatJob } from '@/modules/Job/utils/utils';
import { successNotify, getIdsFormList, errorNotify } from '@/utils/utils';
import { createJobModel, perform } from '@/modules/Job/utils/constants';
export default {
  name: 'taskDetail',
  data() {
    return {
      confirmProductDialog: false,
      isToFile: true, // 是否归档
      ...perform,
      id: this.$route.params.id || this.$route.query.id,
      tab: 'taskInfo',
      taskDetail: {},
      loading: false,
      job: createJobModel(),
      taskType: null,
      deleteModalOpened: false,
      isDesignManager: false,
      addJobRole: [],
      role: [],
      onlyCreator: Boolean,
      abandonedTaskDialogOpened: false,
      updateLoading: false
    };
  },
  components: {
    Loading: () => import('@/components/Loading/index.vue'),
    TaskInfo: () =>
      import(/* webpackChunkName: 'TaskInfo-tab' */ '@/modules/Job/views/taskDetail/components/taskInfo'),
    TaskStage: () =>
      import(/* webpackChunkName: 'TaskStage-tab' */ '@/modules/Job/views/taskDetail/components/taskStage.vue'),
    Pipeline: () =>
      import(/* webpackChunkName: 'Pipeline-tab' */ '@/modules/Job/views/taskDetail/components/pipeline.vue'),
    Translation: () =>
      import(/* webpackChunkName: 'Translation-doc-tab' */ '@/modules/Job/views/taskDetail/components/translation.vue'),
    TestResult: () =>
      import(/* webpackChunkName: 'TestResult-tab' */ '@/modules/Job/views/taskDetail/components/testResult.vue'),
    UATtestResult: () =>
      import(/* webpackChunkName: 'UATtestResult-tab' */ '@/modules/Job/views/taskDetail/components/uATtestResult.vue'),
    ProductionProblems: () => import('@/modules/HomePage/views/myProdProblem'),
    UpdateTaskDetail: () =>
      import(/* webpackChunkName: 'taskInfo-UpdateTaskDetail' */ '@/modules/Job/views/taskDetail/components/updateTaskDetail.vue'),
    CodeAnalizy: () =>
      import('@/modules/Job/views/taskDetail/components/codeAnalizy.vue'),
    CodeApprove: () =>
      import('@/modules/Job/views/taskDetail/components/codeApprove.vue'),
    Authorized: () => import('@/components/Authorized'),
    AbandonedTaskDialog: () =>
      import('@/modules/Job/components/AbandonedTaskDialog'),
    EnvSetting: () =>
      import('@/modules/Job/views/taskDetail/components/envSetting.vue'),
    TestMsgs: () =>
      import('@/modules/Job/views/taskDetail/components/taskInfo/TestMsgs.vue')
  },
  created() {
    this.init();
    // 提交功能测试 更新头部信息
    // 无代码任务 点击下一阶段或者 确认投产 更新头部信息
    this.$root.$on('updateTopTaskStage', this.queryDetail);
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('releaseForm', ['confirmIsRelease', 'isOverdue']),
    ...mapState('jobForm', ['jobProfile', 'queryDeleteJobProfile']),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('user', ['currentUser']),
    // 应用是否容器化项目
    isJavaTypeApp() {
      return (
        this.jobProfile.applicationType &&
        this.jobProfile.applicationType === 'appJava'
      );
    },
    // 扫描权限控制，只能当前任务的行内项目负责人，任务负责人，开发人员
    isManager() {
      const { partyA, partyB } = this.job;
      return (
        partyA.concat(partyB).some(user => user.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    btnDisplay() {
      if (this.isOverdue) {
        return (
          this.job.stage.value !== 'production' &&
          this.job.stage.value !== 'discard'
        );
      } else {
        return false;
      }
    },
    isApp() {
      const reg = /^app/;
      return reg.test(this.job.applicationType);
    },
    isComponent() {
      const reg = /^component/;
      return reg.test(this.job.applicationType);
    },
    isArchetype() {
      const reg = /^archetype/;
      return reg.test(this.job.applicationType);
    }
  },
  watch: {
    tab(val) {
      if (val && val === 'taskInfo') {
        this.changTaskInfo();
      }
    }
  },
  beforeRouteLeave(to, from, next) {
    import('#/plugins/SessionStorage').then(res => {
      res.default.set(`${from.fullPath}_tab`, this.tab);
    });
    next();
  },
  methods: {
    ...mapActions('jobForm', [
      'queryJobProfile',
      'deleteJob',
      'updateJobStage',
      'queryDeleteJob'
    ]),
    ...mapActions('releaseForm', ['updateTaskArchived', 'queryDetailByTaskId']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('user', ['fetch']),
    async init() {
      window.info = this;
      this.loading = true;
      await this.queryJobProfile({ id: this.id });
      this.$nextTick();
      this.queryIsOverdue();
      this.switchTab();
      this.$refs.taskInfo.init();
      this.taskType = this.jobProfile.taskType;
      this.drewPageByJobProfile();
      this.fetch();
      this.loading = false;
    },
    switchTab() {
      this.job = formatJob(this.jobProfile); //找出 job.stage.code
      let tabType = this.$route.query.tab || this.$route.params.tab;
      if (tabType == 'productionProblems') {
        this.tab = 'productionProblems';
      } else if (tabType == 'manage') {
        //投产 归档 删除 废弃 的任务不显示阶段处理
        this.tab =
          this.job.stage.code < 4 && this.job.stage.code > -1
            ? 'taskStage'
            : 'taskInfo';
      } else {
        this.tab = 'taskInfo';
      }
      // 地址栏未带 tab参数
      if (!tabType) {
        import('#/plugins/SessionStorage').then(res => {
          this.tab =
            res.default.getItem(`${this.$route.fullPath}_tab`) || 'taskInfo';
        });
      }
    },
    async queryDetail() {
      await this.queryJobProfile({ id: this.id });
      this.drewPageByJobProfile();
      if (this.tab == 'taskInfo') {
        this.$refs.taskInfo.drewPageByJobProfile();
      }
    },
    drewPageByJobProfile() {
      this.job = formatJob(this.jobProfile);
      this.addJobRole = getIdsFormList([
        this.job.creator,
        this.job.partyA,
        this.job.partyB
      ]);
      this.role = getIdsFormList([
        this.job.creator,
        this.job.partyA,
        this.job.partyB,
        this.job.developer,
        this.job.tester
      ]);
      let otherRole = getIdsFormList([
        this.job.partyA,
        this.job.partyB,
        this.job.developer
      ]);
      // ['行内任务负责人,厂商任务负责人','开发人员'].indexof('UI团队负责人')==-1
      this.onlyCreator = otherRole.indexOf(this.currentUser.id) < 0;
      this.job.reviewer = this.job.reviewer ? this.job.reviewer : {};
      if (
        (this.currentUser.role.some(item => item.name === 'UI团队负责人') &&
          this.job.review_status &&
          this.job.review_status !== 'irrelevant') ||
        this.job.reviewer.id === this.currentUser.id ||
        this.isKaDianManager
      ) {
        this.isDesignManager = true;
      }
    },
    jobSatuts(val) {
      let name = '';
      switch (val) {
        case 0:
          name = '开发任务';
          break;
        case 1:
          name = '无代码任务';
          break;
        case 2:
          name = '日常任务';
          break;
        default:
          name = '开发任务';
      }
      return name;
    },
    applicationSatuts(val) {
      let name = '';
      switch (val) {
        case 'appJava':
          name = 'Java微服务';
          break;
        case 'appVue':
          name = 'Vue应用';
          break;
        case 'appIos':
          name = 'IOS应用';
          break;
        case 'appAndroid':
          name = 'Android应用';
          break;
        case 'appDocker':
          name = '容器化项目';
          break;
        case 'appOldService':
          name = '老版服务';
          break;
        case 'image':
          name = '镜像';
          break;
        case 'componentWeb':
          name = '前端组件';
          break;
        case 'componentServer':
          name = '后端组件';
          break;
        case 'archetypeWeb':
          name = '前端骨架';
          break;
        case 'archetypeServer':
          name = '后端骨架';
          break;
        default:
          name = '';
      }
      return name;
    },
    // 删除任务时先查询出 任务分支 应用 投产窗口 rel分支等需要展示信息
    async handleDeleteJob() {
      if (this.job.taskType == 1) {
        if (
          (this.jobProfile.stage =
            'sit' && this.jobProfile.nocodeInfoMap.sit.relators.length == 0)
        ) {
          await this.deleteTask();
        } else {
          errorNotify('任务已开始,不能删除!');
        }
      } else {
        let id = this.job.id;
        await this.queryDeleteJob({ id });
        this.deleteModalOpened = true;
      }
    },
    async deleteTask() {
      let id = this.job.id;
      let params = {
        id: id
      };
      await this.deleteJob(params);
      successNotify('删除成功');
      //删除任务后关闭弹框
      this.deleteModalOpened = false;
      window.history.back(-1);
    },
    async updateDetail() {
      this.updateLoading = true;
      try {
        Promise.all([
          this.$refs.updateDetail.handUpdateModalOpened(),
          this.queryDetail(),
          // 小组
          this.fetchGroup()
        ]).then(() => {
          this.updateLoading = false;
        });
      } catch (e) {
        this.updateLoading = false;
      }
      // await this.$refs.updateDetail.handUpdateModalOpened();
      // await this.queryDetail();
    },
    async updateDetailSuccess() {
      await this.queryDetail();
      // 修改成功更新 tab 内容
      if (this.tab == 'taskStage') {
        this.$root.$emit('updateTaskStage');
      }
    },
    /* 确认已投产 */
    confirmHadRelease(task_id, name) {
      this.confirmProductDialog = true;
    },
    async confirmProduct() {
      let type = '';
      if (this.isComponent) {
        type = '4';
      } else if (this.isArchetype) {
        type = '5';
      } else if (this.job.applicationType === 'image') {
        type = '6';
      }
      await this.updateTaskArchived({
        task_id: this.id,
        type
      });
      successNotify('已确认投产');
      this.confirmProductDialog = false;
      if (this.isToFile) {
        // 归档
        this.keepInFile();
      } else {
        // 不归档
        this.queryDetail();
      }
    },
    //归档
    async handleHint() {
      let name = this.job.name;
      return this.$q
        .dialog({
          title: `确认归档`,
          message: `任务归档后，<span class="text-negative">${name}</span>的操作均无法执行且无法重新打开！归档后的任务只允许查看！请确认是否进行任务归档？`,
          ok: '确认',
          cancel: '取消',
          html: true
        })
        .onOk(async () => {
          await this.keepInFile();
        });
    },
    async keepInFile() {
      let next = this.stages[6];
      await this.updateJobStage({
        id: this.job.id,
        stage: next.value
      });
      successNotify('任务已归档');
      this.queryDetail();
    },
    //确认完成
    async confirmFinish() {
      await this.updateJobStage({
        id: this.job.id,
        stage: 'production'
      });
      successNotify('任务已完成');
      this.queryDetail();
    },
    queryIsOverdue() {
      let type = '';
      let appType = this.jobProfile.applicationType;
      if (appType === 'image') {
        type = '6';
      } else if (appType === 'componentWeb' || appType === 'componentServer') {
        type = '4';
      } else if (appType === 'archetypeWeb' || appType === 'archetypeServer') {
        type = '5';
      }
      this.queryDetailByTaskId({
        task_id: this.id,
        type
      });
    },
    modelSetting() {
      this.$refs.envSetting.handleModelSettingOpen();
    },
    changTaskInfo() {
      this.$refs.taskInfo.queryTaskDetail();
    },
    //不同状态不同颜色
    typeColor(val) {
      let color = '';
      switch (val) {
        case '开发任务':
          color = 'taskTypeS';
          break;
        case '无代码任务':
          color = 'taskTypeN';
          break;
        case '日常任务':
          color = 'taskTypeD';
          break;
        default:
          color = 'taskTypeS';
          break;
      }
      return color;
    },
    statusBackground(val) {
      let obj = {
        label: '',
        color: ''
      };
      switch (val) {
        case 'develop':
          obj.label = this.taskType === 2 ? '进行中' : '开发中';
          obj.color = 'developC';
          break;
        case 'sit':
          obj.label = 'SIT';
          obj.color = 'sitC';
          break;
        case 'uat':
          obj.label = 'UAT';
          obj.color = 'uatC';
          break;
        case 'rel':
          obj.label = 'REL';
          obj.color = 'relC';
          break;
        case 'production':
          obj.label = this.taskType === 2 ? '已完成' : '已投产';
          obj.color = 'productionC';
          break;
        case 'abort':
          obj.label = '已删除';
          obj.color = 'abortC';
          break;
        case 'file':
          obj.label = '已归档';
          obj.color = 'fileC';
          break;
        case 'discard':
          obj.label = '已废弃';
          obj.color = 'discardC';
          break;
        case 'create-info':
          obj.label = '录入信息完成';
          obj.color = 'createInfoC';
          break;
        case 'create-app':
          obj.label = '录入应用完成';
          obj.color = 'createAppC';
          break;
        case 'create-feature':
          obj.label = '录入分支完成';
          obj.color = 'createFeatureC';
          break;
        default:
          break;
      }
      return obj;
    }
  }
};
</script>
<style lang="stylus" scoped>
.titleStyle {
  font-family: PingFangSC-Medium;
  font-size: 22px;
  margin-right: 12px;
  color: #333333;
  letter-spacing: 0;
  font-weight: 600;
}

.subtitleStyle {
  margin-top: 8px;
  margin-right: 4px;
  font-size: 14px;
  color: #666666;
  letter-spacing: 0;
  line-height: 14px;
}

>>> .tabs-border-bottom {
  border-bottom: 1px solid #eceff1;
}

.q-mt-10 {
  margin-top: 10px;
}

>>> .no-block {
  padding: 20px 32px;
}

.badgeStyle {
  background: #0663BE;
  border-radius: 2px;
  font-family: PingFangSC-Medium;
  font-size: 12px;
  line-height 22px
  color: #fff;
  padding: 0 8px;
  margin-left: 16px;
}

.tabs-border-bottom {
  border-bottom: 1px solid #eceff1;
}

.q-mt-10 {
  margin-top: 10px;
}


.Headleft {
  width: 8px;
  height: 140px;
  background: #E29C46;
  border-radius: 8px 0 0 8px;
}

.h70 {
  height: 70px;
  margin: 0 32px 0 24px;
}

.h66 {
  height: 66px;
  margin: 0 32px 0 24px;
}

.felx1 {
  flex: 1;
}

.border-bottom {
  border-bottom: 1px solid #ECEFF1;
}

.dian {
  background: #fff;
  width: 6px;
  height: 6px;
  margin-top 2px
  border-radius: 50%;
}

.statuStyle {
  background: #E29C46;
  padding: 0 8px;
  border-radius: 14px;
}

.numTitle {
  font-size: 12px;
  color: #999;
  line-height: 18px;
}

.idStyle {
  font-size: 12px;
  line-height: 18px;
}

.statuC {
  color: #fff;
  line-height 22px
}

.taskTypeS {
  color: #FD8D00;
  border: 1px solid #FD8D00;
  border-radius: 6px;
  font-size: 12px;
  line-height: 12px;
  padding: 2px 8px;
}

.taskTypeN {
  color: #8CBC48;
  border: 1px solid #8CBC48;
  border-radius: 6px;
  font-size: 12px;
  line-height: 12px;
  padding: 2px 8px;
}

.taskTypeD {
  color: #4D8FD0;
  border: 1px solid #4D8FD0;
  border-radius: 6px;
  font-size: 12px;
  line-height: 12px;
  padding: 2px 8px;
}

.header {
  background: #fff;
  border-radius: 8px;
}
.developC
  background #FD8D00
.sitC
  background #0378EA
.uatC
  background #4386CA
.relC
  background #04488C
.productionC
  background #4DBB59
.abortC
  background #999999
.fileC
  background #8CBC48
.discardC
  background #B0BEC5
.createInfoC
  background #E29C46
.createAppC
  background #AF6F02
.createFeatureC
  background #B35C26
.h22
  height 22px
.task-detail-tabs
  padding-bottom 10px
>>> .q-tab
  margin-right 46px
  .q-tab__content
    height 39px
</style>
