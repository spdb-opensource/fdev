<template>
  <f-block>
    <div>
      <fdev-stepper
        v-if="step >= 0 && step < 4"
        v-model="step"
        ref="stepper"
        alternative-labels
        header-nav
        flat
      >
        <fdev-step
          :name="1"
          title="SIT测试"
          :done="step > 1"
          :header-nav="
            job.stage.code > 0 ||
              (job.stage.code == 0 && isTest === '0' && step > 1)
          "
          class="hidden"
        >
        </fdev-step>
        <fdev-step
          :name="2"
          title="UAT测试"
          :done="step > 2"
          :header-nav="job.stage.code > 1 || taskCanEntryUat"
          class="hidden"
        >
        </fdev-step>
        <fdev-step
          :name="3"
          title="REL测试"
          :done="step > 3"
          :header-nav="job.stage.code > 2"
          class="hidden"
        >
        </fdev-step>
      </fdev-stepper>

      <div class="bg-white row content-stretch" v-if="taskType != 1">
        <div class="col relative-position">
          <Page>
            <div class="col row">
              <div class="col-12">
                <template v-if="step >= 0 && step < 4">
                  <div class="q-pa-md q-px-llg">
                    <!-- SIT阶段页面 begin-->
                    <template v-if="step === 1 || step === 0">
                      <!-- 代码审核弹窗 -->
                      <f-dialog
                        v-model="SITDialog"
                        transition-show="slide-up"
                        transition-hide="slide-down"
                        title="代码审核"
                      >
                        <div>
                          <span
                            v-if="merging && mergerData.status_code !== '2'"
                          >
                            请联系应用负责人在gitlab上进行代码审核并合并。。。
                          </span>
                          <span v-if="mergerData.status_code === '2'">
                            目前正在执行持续集成的过程，还请耐心等待应用的部署
                          </span>
                        </div>
                        <template v-slot:btnSlot>
                          <fdev-btn
                            dialog
                            label="确定"
                            @click="SITDialog = false"
                          />
                        </template>
                      </f-dialog>
                      <!-- 概况加按钮们 -->
                      <form
                        @submit.prevent="openStrengthenDialog"
                        class="q-pb-md"
                      >
                        <fdev-card-section class="q-pl-none q-pt-none">
                          <div class="text-h6 text-title">概况</div>
                        </fdev-card-section>
                        <fdev-card-section class="row">
                          <div class="col">
                            <f-formitem label="测试类型">
                              SIT
                            </f-formitem>
                            <f-formitem label="feature分支">
                              {{ job.branch }}
                            </f-formitem>
                            <f-formitem label="SIT分支">
                              <div class="q-gutter-x-sm" v-if="isTestRunApp">
                                <span>{{ job.branch | filterBranch }}</span>
                              </div>
                              <div class="q-gutter-x-sm" v-else>
                                <span v-if="examLabel">SIT</span>
                                <span
                                  v-else
                                  v-for="(each, index) in job.env"
                                  :key="index"
                                >
                                  {{ each.branch_name }}
                                </span>
                              </div>
                            </f-formitem>
                          </div>
                        </fdev-card-section>
                        <template>
                          <div v-for="env in job.env" :key="env.name">
                            <fdev-card-section class="q-pl-none">
                              <div class="text-h6 text-title">
                                {{ env.name }}
                              </div>
                            </fdev-card-section>
                            <fdev-card-section>
                              <div
                                class="row q-col-gutter-x-md q-col-gutter-y-sm"
                              >
                                <f-formitem
                                  :label="each.name_zh"
                                  v-for="each in env.var_list"
                                  :key="each.key"
                                  v-show="showData.includes(each.key)"
                                  :title="each.value"
                                >
                                  <div
                                    class="text-ellipsis"
                                    :title="each.value"
                                  >
                                    {{ each.value }}
                                  </div>
                                  <fdev-popup-proxy context-menu>
                                    <fdev-banner style="max-width:300px">
                                      {{ each.value }}
                                    </fdev-banner>
                                  </fdev-popup-proxy>
                                </f-formitem>
                              </div>
                            </fdev-card-section>
                          </div>
                        </template>
                        <!-- 按钮们 -->
                        <div
                          class="row justify-center q-gutter-x-sm q-mt-lg q-mb-lg"
                        >
                          <Authorized>
                            <fdev-btn
                              label="设计还原审核"
                              color="primary"
                              class="q-mr-lg"
                              @click="toDesignReview"
                              v-if="needDesignExamine() && isDesignManager"
                            />
                          </Authorized>
                          <Authorized :include-me="joinPeople">
                            <div class="inline-block">
                              <fdev-btn
                                label="发起审核"
                                color="primary"
                                @click="showReviewDialog"
                                class="q-mr-lg"
                                :disable="examReviewBtnDisable"
                                :loading="
                                  globalLoading['jobForm/createFirstReview']
                                "
                                v-if="examReviewBtnShow"
                              />
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="examReviewBefore"
                              >
                                请联系该任务行内负责人发起审核
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="examReviewAfter"
                              >
                                任务正在审核中，不允许发起审核
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="examDbFile"
                              >
                                该任务下缺少数据库审核材料,请上传数据库审核材料至审核类/数据库审核目录下后再发起审核
                              </fdev-tooltip>
                            </div>
                            <div class="inline-block">
                              <fdev-btn
                                type="submit"
                                label="分支合并"
                                color="primary"
                                :disable="
                                  !bafflePointFlag ||
                                    examEnv ||
                                    !!examineStatus ||
                                    loading ||
                                    merging ||
                                    flag ||
                                    !designExaminePass()
                                "
                                class="btn-lg"
                              />
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="!bafflePointFlag"
                              >
                                检测到应用在使用挡板，合并代码前请在挡板客户端点击停用挡板，以恢复正常状态，再重新合并代码
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="!designExaminePass()"
                              >
                                此任务涉及UI设计稿还原审核，合并分支前，请先上传UI设计稿并点击下一步按钮!
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="!!examineStatus && designExaminePass()"
                              >
                                {{ examineStatus }}
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="
                                  designExaminePass() &&
                                    !examineStatus &&
                                    examEnv
                                "
                              >
                                <span
                                  v-if="
                                    currentUser.role.some(
                                      item => item.name === '环境配置管理员'
                                    )
                                  "
                                >
                                  您的应用尚未绑定过部署信息，请移步到环境配置管理下的部署信息进行绑定！
                                </span>
                                <span v-else
                                  >请联系环境配置管理员/应用负责人到环境配置管理下的部署信息进行绑定！</span
                                >
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="merging && mergerData.status_code !== '2'"
                              >
                                请联系应用负责人在gitlab上进行代码审核并合并
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="mergerData.status_code === '2'"
                              >
                                目前正在执行持续集成的过程，还请耐心等待应用的部署
                              </fdev-tooltip>
                              <fdev-tooltip anchor="top middle" v-if="flag">
                                当前应用调用的REST接口(
                                {{ transId | transList }}
                                )尚未审批通过，请于“接口模块/接口列表”页面发起申请(注提供方应用必须维护在fdev上)。
                              </fdev-tooltip>
                            </div>
                          </Authorized>
                          <Authorized :include-me="joinPeople">
                            <div
                              class="inline-block"
                              v-if="
                                isTestRunApp &&
                                  (step === 1 || step === null || step === 0)
                              "
                            >
                              <fdev-btn
                                @click="openTestPackageDia"
                                label="出测试包"
                                color="primary"
                                class="btn-lg"
                                :disable="
                                  job.sit_merge_id.length === 0 ||
                                    (mergerData.status_code === '5' &&
                                      !mergerData.status)
                                "
                              />
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="
                                  job.sit_merge_id.length === 0 ||
                                    (mergerData.status_code === '5' &&
                                      !mergerData.status)
                                "
                              >
                                进入下一阶段的前置条件(至少有一次成功的代码合并及部署)尚未完成，无法点击！
                              </fdev-tooltip>
                            </div>
                          </Authorized>
                          <Authorized
                            :include-me="
                              job.partyB
                                .map(user => user.id)
                                .concat(job.partyA.map(user => user.id))
                            "
                            v-if="job.stage.code === 1 || job.stage.code === 0"
                          >
                            <fdev-tooltip
                              anchor="top middle"
                              self="bottom middle"
                              :offest="[0, 0]"
                              v-if="!!tooptip"
                            >
                              {{ tooptip }}
                            </fdev-tooltip>
                            <fdev-btn
                              flat
                              label="挂载投产窗口"
                              color="primary"
                              :disable="!!tooptip"
                              class="btn-lg"
                              @click="next"
                            />
                          </Authorized>
                          <Authorized
                            :include-me="joinPeopleExceptTester"
                            v-if="
                              (step === 1 || step === null || step === 0) &&
                                isTestRunApp
                            "
                          >
                            <fdev-tooltip
                              anchor="top middle"
                              self="bottom middle"
                              :offest="[0, 0]"
                              v-if="!!toopTipToTestRun"
                            >
                              {{ toopTipToTestRun }}
                            </fdev-tooltip>
                            <fdev-btn
                              flat
                              label="进入试运行"
                              color="primary"
                              class="btn-lg"
                              @click="toTestRun"
                              :disable="!!toopTipToTestRun"
                            />
                          </Authorized>
                        </div>

                        <div
                          v-if="isManagerOrDeveloper && isTest === '1'"
                          class="testData q-mt-lg q-mb-md"
                        >
                          <SafeTest
                            :testData="testData"
                            :job="job"
                            :checkList="checkList"
                            :mergerData="mergerData"
                            :isLoginUserList="isLoginUserList"
                            :jobProfile="jobProfile"
                            @updateDetail="updateDetail"
                          />
                        </div>

                        <template v-if="showPipeline">
                          <div class="pipeline-mrgin">
                            <div class="text-h6 text-title">Pipeline</div>
                            <fdev-tab-panel name="pipelines">
                              <pipelinesLogs
                                :hide-title="true"
                                :application-id="appinfo[0].id"
                                :app-info="appinfo[0]"
                              />
                            </fdev-tab-panel>
                          </div>
                        </template>
                      </form>
                    </template>
                    <!-- UAT阶段页面 begin-->
                    <template v-else-if="step === 2">
                      <!-- uat代码合并审核 -->
                      <f-dialog
                        v-model="UATDialog"
                        transition-show="slide-up"
                        transition-hide="slide-down"
                        title="代码审核"
                      >
                        <fdev-card-section>
                          <span v-if="mergerData.status_code === '1'">
                            请联系应用负责人在gitlab上进行代码审核并合并。。。
                          </span>
                          <span v-if="mergerData.status_code === '2'">
                            目前正在执行持续集成的过程，还请耐心等待应用的部署
                          </span>
                        </fdev-card-section>
                        <template v-slot:btnSlot>
                          <fdev-btn
                            flat
                            label="确定"
                            color="primary"
                            v-close-popup
                          />
                        </template>
                      </f-dialog>
                      <!--
                      0-投产待审核，
                      1-已确认投产，
                      2-拒绝投产，
                      3-取消投产待审核，
                      4-已取消投产，
                      5-更换投产点待审核，
                      6-已投产
                     -->
                      <div
                        v-if="
                          job.releaseNode.task_status === '2' ||
                            job.releaseNode.task_status === '4' ||
                            job.releaseNode.task_status === undefined
                        "
                        class="row items-center"
                      >
                        <div class="col">
                          <!-- 厂商项目负责人 -->
                          <Authorized
                            :include-me="
                              job.partyB
                                .map(user => user.id)
                                .concat(job.partyA.map(user => user.id))
                            "
                          >
                            <div class="form">
                              <fdev-card-section class="text-center">
                                <div class="text-h5 text-title">
                                  {{ job.name }}
                                </div>
                              </fdev-card-section>
                              <fdev-card-section class="row">
                                <div class="col-sm-9 col-xs-12">
                                  <f-formitem label="测试类型">
                                    <div style="text-align:left">UAT</div>
                                  </f-formitem>
                                  <f-formitem label="应用">
                                    <div style="text-align:left">
                                      {{ job.project_name }}
                                    </div>
                                  </f-formitem>
                                  <f-formitem label="投产窗口">
                                    <fdev-select
                                      ref="uatModel.releaseNode"
                                      :label="
                                        $q.platform.is.desktop
                                          ? undefined
                                          : '投产窗口'
                                      "
                                      use-input
                                      @filter="releaseNodeFilter"
                                      v-model="$v.uatModel.releaseNode.$model"
                                      :options="filterReleaseNodes"
                                      option-label="optionLabel"
                                      option-value="release_node_name"
                                      :rules="[
                                        () =>
                                          !$v.uatModel.releaseNode.$error ||
                                          '请选择投产窗口'
                                      ]"
                                    />
                                  </f-formitem>
                                </div>
                              </fdev-card-section>
                              <fdev-card-actions align="center">
                                <fdev-btn
                                  color="primary"
                                  label="发起审核"
                                  @click="handleAddJobReleaseNode"
                                  class="btn-lg"
                                  :disable="merging || !!tooptip"
                                  :loading="
                                    globalLoading[
                                      'releaseForm/addJobReleaseNode'
                                    ]
                                  "
                                />
                                <fdev-tooltip
                                  anchor="top middle"
                                  v-if="merging"
                                >
                                  请联系应用负责人在gitlab上进行代码审核并合并
                                </fdev-tooltip>
                                <fdev-tooltip
                                  anchor="top middle"
                                  self="bottom middle"
                                  :offest="[0, 0]"
                                  v-if="!!tooptip"
                                >
                                  {{ tooptip }}
                                </fdev-tooltip>
                              </fdev-card-actions>
                            </div>
                            <template v-slot:exception>
                              <div class="data-none row flex-center q-my-xl">
                                <f-icon name="member" class="text-primary" />
                                请联系任务的行内项目负责人
                                {{ getSpdbMaster(jobProfile.spdb_master) }}
                                在fdev投产模块下
                                {{ job.releaseNode.release_node_name }}
                                投产窗口确认投产
                              </div>
                            </template>
                            <!-- 拒绝投产弹窗 -->
                            <template>
                              <div class="q-pa-md q-gutter-sm">
                                <f-dialog
                                  v-model="openRefuseReleaseDialog"
                                  transition-show="slide-up"
                                  transition-hide="slide-down"
                                  title="拒绝投产"
                                >
                                  <fdev-card-section class="scroll">
                                    <f-formitem label="投产窗口" :label-col="3">
                                      {{ job.releaseNode.release_node_name }}
                                    </f-formitem>
                                    <f-formitem label="拒绝人员" :label-col="3">
                                      <div class="text-wrapper">
                                        {{
                                          job.releaseNode.reject_reason
                                            ? job.releaseNode.reject_reason.split(
                                                '--'
                                              )[1]
                                            : '1'
                                        }}
                                      </div>
                                    </f-formitem>
                                    <f-formitem label="拒绝原因" :label-col="3">
                                      <div class="text-wrapper">
                                        {{
                                          job.releaseNode.reject_reason
                                            ? job.releaseNode.reject_reason.split(
                                                '--'
                                              )[0]
                                            : ''
                                        }}
                                      </div>
                                    </f-formitem>
                                  </fdev-card-section>
                                  <template v-slot:btnSlot>
                                    <fdev-btn
                                      dialog
                                      v-close-popup
                                      label="确定"
                                    />
                                  </template>
                                </f-dialog>
                              </div>
                            </template>
                            <!-- 拒绝投产弹窗 -->
                          </Authorized>
                        </div>
                      </div>

                      <div
                        v-else-if="job.releaseNode.task_status === '0'"
                        class="row items-center"
                      >
                        <div class="col">
                          <div class="data-none row flex-center q-my-xl">
                            <f-icon class="text-primary" name="member" />
                            请联系任务的行内项目负责人
                            {{ getSpdbMaster(jobProfile.spdb_master) }}
                            在fdev投产模块下
                            {{ job.releaseNode.release_node_name }}
                            投产窗口确认投产。
                          </div>
                        </div>
                      </div>

                      <div
                        v-else-if="
                          job.releaseNode.task_status === '1' ||
                            job.releaseNode.task_status === '3'
                        "
                      >
                        <form @submit.prevent="handleUAT" class="q-pb-md">
                          <fdev-card-section class="q-pl-none q-pt-none">
                            <div class="text-h6 text-title">概况</div>
                          </fdev-card-section>
                          <fdev-card-section class="row">
                            <div class="col">
                              <f-formitem
                                label="测试类型"
                                style="text-align:left"
                              >
                                UAT
                              </f-formitem>
                              <f-formitem label="应用" style="text-align:left">
                                {{ job.project_name }}
                              </f-formitem>
                              <f-formitem
                                label="投产窗口"
                                style="text-align:left"
                              >
                                {{ job.releaseNode.release_node_name }}
                              </f-formitem>
                              <f-formitem
                                label="release分支"
                                style="text-align:left"
                                v-if="job.releaseNode.release_application"
                              >
                                {{
                                  job.releaseNode.release_application
                                    .release_branch
                                }}
                                <span
                                  class="text-primary pointer q-ml-md"
                                  @click="handleScanDialogOpen"
                                  v-if="taskManagerLimit.limit"
                                >
                                  扫描接口
                                </span>
                              </f-formitem>
                              <f-formitem
                                label="UAT环境"
                                style="text-align:left"
                              >
                                {{ job.releaseNode.uat_env_name }}
                              </f-formitem>
                            </div>
                          </fdev-card-section>

                          <template>
                            <div v-for="env in job.env" :key="env.name">
                              <fdev-card-section class="q-pl-none">
                                <div class="text-h6 text-title">
                                  {{ env.name }}
                                </div>
                              </fdev-card-section>
                              <fdev-card-section>
                                <div
                                  class="row q-col-gutter-x-md q-col-gutter-y-sm"
                                >
                                  <f-formitem
                                    ellipsis
                                    :label="each.name_zh"
                                    v-for="each in env.var_list"
                                    :key="each.key"
                                    v-show="showData.includes(each.key)"
                                  >
                                    <div
                                      class="text-ellipsis"
                                      :title="each.value"
                                    >
                                      {{ each.value }}
                                    </div>
                                  </f-formitem>
                                </div>
                              </fdev-card-section>
                            </div>
                          </template>
                          <div
                            class="row justify-center q-gutter-x-sm q-mt-lg q-mb-lg"
                          >
                            <Authorized :include-me="joinPeople">
                              <fdev-tooltip
                                v-if="job.env.length === 0"
                                anchor="top middle"
                              >
                                未指定UAT测试环境
                              </fdev-tooltip>
                              <fdev-btn
                                type="submit"
                                label="分支合并"
                                color="primary"
                                :disable="
                                  !!examineStatus ||
                                    loading ||
                                    merging ||
                                    job.env.length === 0 ||
                                    (needUiAuditing() &&
                                      (this.job.review_status === 'nopass' ||
                                        this.job.review_status === 'fixing' ||
                                        this.job.review_status === 'uploaded' ||
                                        this.job.review_status ===
                                          'wait_allot' ||
                                        this.job.review_status ===
                                          'wait_upload'))
                                "
                                class="btn-lg"
                              />
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="!!examineStatus"
                              >
                                {{ examineStatus }},请到sit阶段发起审核
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="
                                  needUiAuditing() &&
                                    this.job.review_status === 'nopass'
                                "
                              >
                                此任务UI设计稿还原审核未通过，请根据UI审核人员反馈意见，重新完成设计还原审核流程！
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="
                                  needUiAuditing() &&
                                    this.job.review_status === 'wait_upload'
                                "
                              >
                                此任务涉及UI设计稿还原审核，合并UAT分支前，请先上传UI设计稿！
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="
                                  needUiAuditing() &&
                                    (this.job.review_status === 'fixing' ||
                                      this.job.review_status === 'wait_allot' ||
                                      this.job.review_status === 'uploaded')
                                "
                              >
                                此任务涉及UI设计稿还原审核，请督办UI负责团队执行完成设计还原审核流程！
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="mergerData.status_code === '1'"
                              >
                                请联系应用负责人在gitlab上进行代码审核并合并
                              </fdev-tooltip>
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="mergerData.status_code === '2'"
                              >
                                目前正在执行持续集成的过程，还请耐心等待应用的部署
                              </fdev-tooltip>
                            </Authorized>
                            <Authorized
                              :include-me="job.partyB.map(user => user.id)"
                              v-if="job.stage.code >= 2"
                            >
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="job.stage.code < 2"
                              >
                                请先点击“分支合并”，使任务进入UAT阶段！
                              </fdev-tooltip>
                              <fdev-tooltip anchor="top middle" v-else>
                                点击进入投产窗口应用列表，提交发布后，任务将进入rel阶段！
                              </fdev-tooltip>
                              <fdev-btn
                                flat
                                label="进入投产窗口"
                                color="primary"
                                :disable="job.stage.code < 2"
                                class="btn-lg"
                                @click="linkToReleaseApps"
                              />
                            </Authorized>
                          </div>

                          <!-- 提测重点 uat阶段才有 -->
                          <div
                            class="testData q-mt-lg q-mb-md"
                            v-if="job.stage.code === 2"
                          >
                            <div class="text-h6 text-title">UAT提测重点</div>
                            <fdev-input
                              v-model.trim="testKeynote"
                              type="textarea"
                              ref="testKeynote"
                              class="q-mt-md q-ml-lg"
                              hint="!请填写需要重点测试的功能"
                            />
                            <div class="row justify-center">
                              <fdev-btn
                                type="button"
                                label="提交"
                                class="uat-submit"
                                color="primary"
                                :disable="!testKeynote"
                                @click="submitTestKeynote"
                              />
                              <fdev-tooltip v-if="!testKeynote">
                                请填写需要重点测试的功能
                              </fdev-tooltip>
                            </div>
                          </div>
                          <template v-if="appinfo.length != 0">
                            <div style="margin-top:32px;margin-bottom:32px">
                              <div class="text-h6 text-title">Pipeline</div>
                              <fdev-tab-panel name="pipelines">
                                <pipelinesLogs
                                  :hide-title="true"
                                  :application-id="appinfo[0].id"
                                  :app-info="appinfo[0]"
                                />
                              </fdev-tab-panel>
                            </div>
                          </template>
                        </form>
                      </div>
                    </template>
                    <!-- REL阶段页面 begin-->
                    <template v-else-if="step === 3">
                      <div class="row items-center">
                        <div class="col">
                          <fdev-card-section class="q-pl-none q-pt-none">
                            <div class="text-h6 text-title">概况</div>
                          </fdev-card-section>
                          <fdev-card-section class="row">
                            <div class="col">
                              <f-formitem label="测试类型">
                                <div style="text:align-right">REL</div>
                              </f-formitem>
                              <f-formitem label="应用">
                                <div style="text:align-right">
                                  {{ job.project_name }}
                                </div>
                              </f-formitem>
                              <f-formitem label="投产窗口">
                                <div>
                                  {{ job.releaseNode.release_node_name }}
                                </div>
                              </f-formitem>
                              <f-formitem
                                label="pro tag分支"
                                v-if="job.product_tag"
                              >
                                <div
                                  style="text:align-right"
                                  class="col-md-9 col-sm-8 text-grey-8 overflow clickable"
                                >
                                  {{ job.product_tag[0] }}
                                  <fdev-popup-proxy class="fixheight">
                                    <div
                                      v-for="(item, index) in job.product_tag"
                                      :key="index"
                                    >
                                      <p class="q-mx-md q-my-sm">{{ item }}</p>
                                    </div>
                                  </fdev-popup-proxy>
                                </div>
                              </f-formitem>
                              <f-formitem label="REL环境">
                                <div style="text:align-right">
                                  {{ job.releaseNode.rel_env_name }}
                                </div>
                              </f-formitem>
                            </div>
                          </fdev-card-section>
                          <template>
                            <div v-for="(env, index) in job.env" :key="index">
                              <fdev-card-section class="q-pl-none">
                                <div class="text-h6 text-title">
                                  {{ env.env_name }}
                                </div>
                              </fdev-card-section>
                              <fdev-card-section>
                                <div
                                  class="row q-col-gutter-x-md q-col-gutter-y-sm"
                                >
                                  <f-formitem
                                    ellipsis
                                    :label="each.name_zh"
                                    v-for="each in env.var_list"
                                    :key="each.key"
                                    v-show="showData.includes(each.key)"
                                  >
                                    <div
                                      class="text-ellipsis"
                                      :title="each.value"
                                    >
                                      {{ each.value }}
                                    </div>
                                  </f-formitem>
                                </div>
                              </fdev-card-section>
                            </div>
                          </template>
                          <div class="row justify-center q-gutter-x-sm q-mt-lg">
                            <Authorized
                              :include-me="
                                job.partyB
                                  .map(user => user.id)
                                  .concat(job.partyA.map(user => user.id))
                              "
                            >
                              <fdev-btn
                                label="进入投产窗口"
                                :disable="
                                  !!!this.job.releaseNode.release_node_name
                                "
                                class="btn-lg"
                                @click="goRel"
                              />
                              <fdev-tooltip
                                anchor="top middle"
                                v-if="!!!this.job.releaseNode.release_node_name"
                              >
                                该任务还未挂载投产窗口，请到uat阶段挂载投产窗口
                              </fdev-tooltip>
                            </Authorized>
                            <fdev-btn
                              type="button"
                              label="返回"
                              @click="goBack"
                              color="primary"
                              text-color="white"
                            />
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                </template>

                <template v-else-if="step >= 4">
                  <Result :type="job.stage.type" class="q-my-xl col">
                    <template v-slot:title>{{ job.stage.title }}</template>
                    <template v-slot:fdev-formitem>
                      {{ job.stage.fdev - formitem }}
                    </template>
                    <template v-slot:actions>
                      <div class="row justify-center q-gutter-x-sm">
                        <fdev-btn
                          dialog
                          color="primary"
                          to="/job/list"
                          label="返回列表"
                        />
                        <fdev-btn
                          flat
                          dialog
                          outline
                          color="primary"
                          :to="`/job/list/${job.id}`"
                          label="查看项目"
                        />
                      </div>
                    </template>
                  </Result>
                </template>
              </div>
            </div>
          </Page>
          <fdev-btn
            v-if="job.stage.value !== 'file'"
            flat
            color="primary"
            :to="`/job/list/${job.id}`"
            label="任务详情"
            class="profile"
          />
          <fdev-toggle
            icon="mdi-set mdi-file-document-outline"
            v-model="toggleFile"
            color="primary"
            class="toggle-file"
            v-show="$q.screen.gt.sm"
            label="关联文档:"
            left-label
          />
        </div>
        <!-- 文件处理  文件树 -->
        <transition name="fade">
          <div
            class="gt-sm q-py-md row max-width"
            v-show="toggleFile && $q.screen.gt.sm"
          >
            <div class="col q-mx-md">
              <fdev-input ref="filter" type="text" v-model="filter">
                <template v-slot:prepend>
                  <f-icon name="search" class="cursor-pointer" />
                </template>
              </fdev-input>

              <fdev-tree
                ref="tree"
                :nodes="nodes"
                node-key="label"
                :filter="filter"
                :expanded.sync="expanded"
              >
                <template v-slot:header-level-1="prop">
                  <div class="text-weight-bold">{{ prop.node.label }}</div>
                </template>
                <template v-slot:header-level-2="prop">
                  <a
                    class="link word-break"
                    @click="download(prop.node.path)"
                    target="_blank"
                  >
                    <div>{{ prop.node.label }}</div>
                    <div class="text-subtitle2 text-grey-7">
                      {{ prop.node.date }}
                    </div>
                  </a>
                  <fdev-space />
                  <fdev-btn
                    icon="delete"
                    @click="deleteUpload(prop.node)"
                    flat
                    color="red"
                    :loading="globalLoading['jobForm/deleteFile']"
                    v-if="examDelete(prop.node)"
                  />
                </template>
              </fdev-tree>

              <fdev-page-sticky
                position="bottom-right"
                class="btn-uploader btn-style"
                :offset="[36, 160]"
              >
                <fdev-fab
                  color="blue-5"
                  icon="cloud_upload"
                  direction="left"
                  class="q-fab-fix"
                >
                  <div class="row justify-center uploader-wrapper">
                    <Uploader
                      multiple
                      class="shadow-3"
                      @success="uploadSuccess"
                      @beforeUpload="beforeUpload"
                      :method="updateTaskDoc"
                      max-total-size="31457280"
                      :uploadedFiles="uploadedFiles"
                      :fileType="pickFileTypeName"
                    >
                      <f-formitem label="类型" :label-col="3" class="q-ma-sm">
                        <el-cascader
                          :props="{
                            checkStrictly: true,
                            emitPath: false
                          }"
                          class="full-width"
                          v-model="pickFileTypeName"
                          :options="fileTypeOptions"
                        />
                      </f-formitem>
                    </Uploader>
                  </div>
                </fdev-fab>
              </fdev-page-sticky>
            </div>
          </div>
        </transition>
      </div>
      <!-- 非代码变更页面 -->
      <div v-else class="bg-white row no-wrap content-stretch">
        <div>
          <div class="col relative-position">
            <Page>
              <div
                class="row q-col-gutter-x-md q-col-gutter-y-sm q-pl-md q-pt-lg"
              >
                <fdev-form class="row q-pa-md" @submit.prevent="updateNocode">
                  <f-formitem label="任务名称" value-style="margin-right:16px">
                    <div>
                      <router-link
                        :to="`/job/list/${jobProfileNOCode.id}`"
                        class="link"
                      >
                        {{ jobProfileNOCode.name }}
                      </router-link>
                    </div>
                  </f-formitem>
                  <f-formitem
                    label="任务负责人"
                    value-style="margin-right:16px"
                  >
                    <div>
                      <router-link
                        :to="`/user/list/${each.id}`"
                        v-for="(each, index) in jobProfileNOCode.master"
                        :key="index"
                        class="link"
                      >
                        {{ each.user_name_cn }}
                      </router-link>
                    </div>
                  </f-formitem>
                  <f-formitem
                    label="行内项目负责人"
                    value-style="margin-right:16px"
                  >
                    <div>
                      <router-link
                        :to="`/user/list/${each.id}`"
                        v-for="(each, index) in jobProfileNOCode.spdb_master"
                        :key="index"
                        class="link"
                      >
                        {{ each.user_name_cn }}
                      </router-link>
                    </div>
                  </f-formitem>
                  <f-formitem label="开发人员" value-style="margin-right:16px">
                    <div>
                      <router-link
                        :to="`/user/list/${each.id}`"
                        v-for="(each, index) in jobProfileNOCode.developer"
                        :key="index"
                        class="link"
                      >
                        {{ each.user_name_cn }}
                      </router-link>
                    </div>
                  </f-formitem>
                  <f-formitem label="截止日期" value-style="margin-right:16px">
                    <f-date
                      :disable="disInput"
                      mask="YYYY/MM/DD"
                      :hide-dropdown-icon="true"
                      ref="updateFinishTimeModel.finishTime"
                      :rules="[
                        () =>
                          $v.updateFinishTimeModel.finishTime.required ||
                          '请选择截止日期'
                      ]"
                      v-model="updateFinishTimeModel.finishTime"
                    />
                  </f-formitem>
                  <f-formitem
                    :col="1"
                    label="描述信息"
                    value-style="margin-right:16px"
                  >
                    <fdev-input
                      :disable="disInput"
                      v-model="updateFinishTimeModel.taskDesc"
                      ref="updateFinishTimeModel.taskDesc"
                      :rules="[
                        () =>
                          $v.updateFinishTimeModel.taskDesc.required ||
                          '请填写描述信息'
                      ]"
                      type="textarea"
                    />
                  </f-formitem>
                  <div style="width:100%">
                    <fdev-btn
                      v-if="noShowBtns"
                      type="submit"
                      @click="updateFinishTime"
                      :loading="globalLoading['jobForm/updateNocodeInfo']"
                      color="primary"
                      label="确认修改"
                      class="edit-btn"
                    />
                  </div>
                </fdev-form>
                <div class="q-pa-md" style="width:100%">
                  <fdev-table
                    noExport
                    no-select-cols
                    class="no-code-table"
                    row-key="id"
                    :rows-per-page-options="[0]"
                    hide-bottom
                    :data="noCodeData"
                    :columns="noCodeColumns"
                  >
                    <template v-slot:top-bottom>
                      <fdev-btn
                        v-if="noShowBtns"
                        @click="selsectRelationDialogOpen()"
                        color="primary"
                        label="选择相关人员"
                      />
                    </template>
                    <template v-slot:body-cell-relatorName="props">
                      <fdev-td :title="props.value">
                        <router-link
                          :to="`/user/list/${props.row.relatorId}`"
                          class="link"
                        >
                          {{ props.value }}
                        </router-link>
                      </fdev-td>
                    </template>
                    <template v-slot:body-cell-currentName="props">
                      <fdev-td>
                        <router-link
                          :to="`/user/list/${props.row.currentId}`"
                          class="link"
                        >
                          {{ props.value }}
                        </router-link>
                      </fdev-td>
                    </template>
                    <template v-slot:body-cell-fileName="props">
                      <fdev-td :title="props.value">
                        <div class="td-width">
                          {{ props.value }}
                        </div>
                      </fdev-td>
                    </template>
                    <template v-slot:body-cell-operation="props">
                      <fdev-td>
                        <div class="q-gutter-x-sm row no-wrap">
                          <fdev-btn
                            size="sm"
                            flat
                            color="primary"
                            label="文件管理"
                            @click="openWpsFileManger(props.row)"
                          />
                          <fdev-btn
                            flat
                            v-if="showBtn(props.row)"
                            size="sm"
                            color="primary"
                            label="完成"
                            @click="handleBtn(props.row)"
                          />
                          <fdev-btn
                            v-if="isManagerOrDeveloper && noShowBtns"
                            size="sm"
                            flat
                            label="删除"
                            @click="handleDelBtn(props.row)"
                          />
                        </div>
                      </fdev-td>
                    </template>
                  </fdev-table>
                  <div
                    class="col row items-center"
                    v-if="
                      jobProfileNOCode.stage == 'sit' ||
                        jobProfileNOCode.stage == 'uat'
                    "
                  >
                    <fdev-btn
                      v-if="isManagerOrDeveloper && noShowBtns"
                      class="next-btn"
                      color="primary"
                      label="下一阶段"
                      :disable="btnToGray || nextStageStatus"
                      :loading="globalLoading['jobForm/nextStage']"
                      @click="noCodeTaskNext()"
                    />
                    <fdev-tooltip
                      anchor="top middle"
                      self="bottom middle"
                      :offest="[-20, 0]"
                      v-if="nextStageStatus"
                    >
                      相关任务未完成
                    </fdev-tooltip>
                    <fdev-tooltip
                      anchor="top middle"
                      self="bottom middle"
                      :offest="[-20, 0]"
                      v-if="btnToGray"
                    >
                      请先点击“确认修改”
                    </fdev-tooltip>
                  </div>
                  <div
                    class="col row items-center"
                    v-if="jobProfileNOCode.stage == 'rel'"
                  >
                    <fdev-btn
                      v-if="isManagerOrDeveloper && noShowBtns"
                      class="next-btn"
                      color="primary"
                      label="确认投产"
                      :disable="nextStageStatus || btnToGray"
                      :loading="globalLoading['jobForm/nextStage']"
                      @click="noCodeTaskNext()"
                    />
                    <fdev-tooltip
                      anchor="top middle"
                      self="bottom middle"
                      :offest="[-20, 0]"
                      v-if="nextStageStatus"
                    >
                      相关任务未完成
                    </fdev-tooltip>
                    <fdev-tooltip
                      anchor="top middle"
                      self="bottom middle"
                      :offest="[-20, 0]"
                      v-if="btnToGray"
                    >
                      请点击“确认修改”
                    </fdev-tooltip>
                  </div>
                </div>
              </div>
            </Page>
          </div>
        </div>
      </div>

      <!-- uat rel 未指定测试环境 弹窗-->
      <f-dialog persistent title="未指定环境" v-model="lackOfEnvWarning">
        <fdev-card-section v-if="job.releaseNode">
          <div style="width:30vw">
            当前任务在
            <router-link
              class="link"
              :to="`/release/list/${job.releaseNode.release_node_name}/applist`"
            >
              {{ job.releaseNode.release_node_name }}
            </router-link>
            投产窗口并未指定
            {{ lackOfEnv }}
            环境，请联系对应应用的行内/厂商项目负责人前往投产模块指定测试环境!
            地址：
            <router-link
              class="link"
              :to="`/release/list/${job.releaseNode.release_node_name}/applist`"
            >
              {{ url }}release/list/
              {{ job.releaseNode.release_node_name }}
              /applist
            </router-link>
          </div>
        </fdev-card-section>
        <template v-slot:btnSlot>
          <fdev-btn dialog label="确定" color="primary" v-close-popup />
        </template>
      </f-dialog>
      <!-- 初次审核 弹窗 -->
      <f-dialog v-model="reviewDialog" right title="发起审核">
        <form>
          <fdev-select
            v-model="$v.reviewData.type.$model"
            placeholder="数据库类型"
            :options="dataTypeOptions"
            clearable
            class="width-280"
            ref="reviewDataType"
            :rules="[() => $v.reviewData.type.required || '请选择数据库类型']"
          />
          <fdev-select
            use-input
            class="width-280"
            placeholder="指派初审人"
            clearable
            v-model="$v.reviewData.firstReview.$model"
            :options="firstReviewOptionsCopy"
            option-value="id"
            option-label="name"
            map-options
            emit-value
            ref="reviewDataFirst"
            @filter="userFilter"
            @input="handleFirstReviewer"
            :rules="[
              () => $v.reviewData.firstReview.required || '请指派初审人'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label :title="scope.opt.user_name_en" caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>

          <f-formitem
            label="是否已通知关联供数系统配套改造"
            v-if="isShowNotify"
          >
            <div class="text-right">
              <fdev-radio
                val="否"
                v-model="hasNotified"
                label="否"
                class="q-pr-lg"
              />
              <fdev-radio val="是" v-model="hasNotified" label="是" />
            </div>
          </f-formitem>
          <f-formitem
            label="是否已梳理完所有需要暂服务的库表关联应用"
            v-if="isShowServe"
          >
            <div class="text-right">
              <fdev-radio
                val="否"
                v-model="hasDone"
                label="否"
                class="q-pr-lg"
              />
              <fdev-radio val="是" v-model="hasDone" label="是" />
            </div>
          </f-formitem>
          <div>
            <div class="reviewDesc">请输入申请描述</div>
            <fdev-editor min-height="80px" v-model="reviewData.doc" />
          </div>
        </form>
        <template v-slot:btnSlot>
          <fdev-btn
            label="确定"
            @click="startReview"
            :loading="globalLoading['jobForm/createFirstReview']"
            dialog
          />
          <fdev-btn label="关闭" outline dialog v-close-popup />
        </template>
      </f-dialog>
      <!-- 二次审核 弹窗 -->
      <f-dialog title="发起审核" v-model="secondReviewDialog">
        <div class="reviewDesc">请输入申请描述</div>
        <fdev-editor min-height="80px" v-model="reviewData.doc" />
        <template v-slot:btnSlot>
          <fdev-btn
            dialog
            label="确定"
            :loading="globalLoading['jobForm/saveReviewRecord']"
            @click="startSecondReview"
          />
          <fdev-btn dialog outline label="关闭" v-close-popup />
        </template>
      </f-dialog>
      <!-- 代码扫描 ? -->
      <ScanDialog
        v-model="scanDialogOpen"
        :appName="job.project_name"
        :id="job.id"
        :branchName="
          job.releaseNode.release_application
            ? job.releaseNode.release_application.release_branch
            : ''
        "
        v-if="job.releaseNode && taskType != 1"
      />
      <!-- 批量任务计划日期补足 弹窗 -->
      <PlanDateUpdateDialog
        v-model="planDateUpdateDialogOpen"
        :job="job"
        @closePlanDateUpdateDialog="closePlanDateUpdateDialog"
      />
      <!-- 点击出测试包的时候弹框选择是否为特殊测试包，以及是否加固 弹窗-->
      <f-dialog
        v-model="testPackageDia"
        persistent
        transition-show="scale"
        transition-hide="scale"
        right
        title="测试出包"
      >
        <div class="q-gutter-y-diaLine">
          <f-formitem :label="ysCode.title">
            <fdev-select
              emit-value
              map-options
              v-model="testPackageType"
              option-value="id"
              option-label="label"
              :options="options1"
            />
          </f-formitem>
        </div>
        <div v-if="ysCode.children">
          <div v-for="(val, key, ind) in ysCode.children" :key="ind">
            <div v-if="val.children && Array.isArray(val.children)">
              <div
                v-for="(item, index) in val.children"
                :key="index"
                class="q-mt-md q-gutter-y-diaLine"
              >
                <div class="q-gutter-y-diaLine" v-if="testPackageType === key">
                  <f-formitem :label="item.title">
                    <fdev-select
                      emit-value
                      map-options
                      v-model="modelArr[item.key]"
                      option-value="id"
                      option-label="label"
                      :options="checkOptions(item.children)"
                    />
                  </f-formitem>
                  <f-formitem
                    v-if="
                      modelArr[item.key] === '7' && item.key === 'Environment'
                    "
                    :label="item.children[7].children.serviceid.title"
                    required
                  >
                    <fdev-input
                      v-model="item.children[7].children.serviceid.placehoder"
                    />
                  </f-formitem>
                  <f-formitem
                    v-if="modelArr[item.key] === '1' && item.key === 'version'"
                    :label="item.children[1].title"
                    required
                  >
                    <fdev-input
                      v-model="versionNo"
                      :placeholder="item.children[1].placehoder"
                    />
                  </f-formitem>
                  <f-formitem
                    v-if="
                      modelArr[item.key] === '1' && item.key === 'rnversion'
                    "
                    :label="item.children[1].title"
                    required
                  >
                    <fdev-input
                      v-model="reRnVersionNo"
                      :placeholder="item.children[1].placehoder"
                    />
                  </f-formitem>
                  <f-formitem
                    v-if="
                      modelArr[item.key] === '2' && item.key === 'rnversion'
                    "
                    :label="item.children[2].title"
                    required
                  >
                    <fdev-input
                      v-model="deRnVersionNo"
                      :placeholder="item.children[2].placehoder"
                    />
                  </f-formitem>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          class="q-mt-md"
          v-if="
            testPackageType === 'auto' ||
              testPackageType === 'jrcs' ||
              testPackageType === 'test' ||
              testPackageType === 'yace'
          "
        >
          <f-formitem label="是否加固">
            <fdev-select
              emit-value
              map-options
              v-model="jgStyle"
              option-value="id"
              option-label="label"
              :options="jgOption"
            />
          </f-formitem>
        </div>
        <!-- <div class="q-pl-lg testPacWid">
        <fdev-select
          emit-value
          map-options
          v-model="testPackageType"
          option-value="id"
          option-label="testPackageType"
          :options="testPackageTypeOptions"
        />
      </div>
      <fdev-card-section>
        <div class="text-h6 q-pl-sm">是否加固？</div>
      </fdev-card-section>
      <div class="q-pl-lg">
        <fdev-radio val="1" v-model="reinforce" label="是" class="q-mr-lg" />
        <fdev-radio val="0" v-model="reinforce" label="否" />
      </div> -->
        <template v-slot:btnSlot>
          <fdev-btn outline dialog label="取消" @click="closePackageDia" />
          <div>
            <fdev-tooltip v-if="canSubmit && canSubmit.tip">
              {{ canSubmit.tip }}
            </fdev-tooltip>
            <fdev-btn
              dialog
              label="确定"
              :disable="canSubmit && canSubmit.flag"
              @click="handleIOSOrAndroid"
              :loading="globalLoading['jobForm/iOSOrAndroidAppPackage']"
            />
          </div>
        </template>
      </f-dialog>
      <!-- 非代码变更文件管理 弹窗-->
      <f-dialog
        right
        persistent
        v-model="showDialogOpen"
        transition-show="slide-up"
        transition-hide="slide-down"
        class="q-dialog-per"
        @shake="confirmToClose"
        @before-close="init"
        title="文件管理"
      >
        <div v-if="showFileUpload">
          <form>
            <f-formitem
              label-style="width:auto"
              label="请选择文件"
              :label-col="3"
              align="right"
            >
              <input
                ref="fileInput"
                type="file"
                hint="请选择文件"
                id="file_input"
              />
              <fdev-btn
                ficon="upload"
                normal
                label="上传"
                @click="uploadFiles()"
              />
            </f-formitem>
          </form>
        </div>
        <div class="filetable">
          <fdev-table
            :rows-per-page-options="[0]"
            hide-bottom
            :data="fileList"
            :columns="filesColumns"
            row-key="id"
            title="文件管理列表"
            noExport
            no-select-cols
            titleIcon="list_s_f"
            class="my-sticky-head-table"
          >
            <template v-slot:body-cell-operation="props">
              <fdev-td class="q-gutter-x-sm row no-wrap">
                <fdev-btn flat @click="download(props.row.path)" label="下载" />
                <fdev-btn
                  flat
                  v-if="showFileUpload"
                  @click="handleDeleteFile(props.row)"
                  label="删除"
                />
              </fdev-td>
            </template>
          </fdev-table>
        </div>
      </f-dialog>
      <!-- 添加相关人员窗口 -->
      <f-dialog v-model="selsectRelationDialog" title="添加任务相关人员">
        <fdev-form>
          <f-formitem label="相关人员">
            <fdev-select
              clearable
              use-input
              input-debounce="0"
              ref="addSelectRelationModel.selectUser"
              v-model="$v.addSelectRelationModel.selectUser.$model"
              :options="userOptions"
              @filter="noCodeUserFilter"
              :rules="[
                () =>
                  $v.addSelectRelationModel.selectUser.required ||
                  '请选择相关人员'
              ]"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label :title="scope.opt.user_name_en" caption>
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <div class="text-right">
            <fdev-btn
              dialog
              label="确定"
              @click="addRelationHandleBtn()"
              :loading="globalLoading['jobForm/addNoCodeRelator']"
            />
          </div>
        </fdev-form>
      </f-dialog>

      <ChangeBatchDialog
        title="变更应用批次设置"
        :data="setBatchModel"
        v-model="setBatchDialogOpened"
        @submit="handleSetBatchDialog"
      />

      <AddBatchMessage
        :data="appUserInfo"
        v-model="AddBatchMessageDialogOpened"
      />
    </div>
  </f-block>
</template>

<script>
import date from '#/utils/date.js';
import { required } from 'vuelidate/lib/validators';
import Authorized from '@/components/Authorized';
// import configCI from '@/modules/App/views/pipelines/configCI';
import pipelinesLogs from '@/modules/App/components/pipelinesLogs';
import Result from '@/components/Result';
import Page from '@/components/Page';
import ChangeBatchDialog from '@/modules/Release/Components/ChangeBatchDialog';
import AddBatchMessage from '@/modules/Release/Components/AddBatchMessage';
import Uploader from '@/components/Uploader';
import { formatUser } from '@/modules/User/utils/model';
import {
  perform,
  createJobModel,
  createRedmineData,
  testData,
  createdFinishModel,
  createdSelectRelationModel,
  fileTypeOptions
} from '../../utils/constants';
import { formatJob } from '../../utils/utils';
import ScanDialog from '@/modules/interface/components/scanDialog';
import PlanDateUpdateDialog from '../../components/PlanDateUpdateDialog';
import SafeTest from '../../components/safeTest';
import {
  validate,
  successNotify,
  formatOption,
  errorNotify,
  baseUrl,
  resolveResponseError,
  getIdsFormList
} from '@/utils/utils';
import {
  setToggleFile,
  getToggleFile,
  setPagination,
  getPagination
} from '../../utils/setting';
import { mapState, mapActions, mapGetters } from 'vuex';
import { queryParamFile } from '@/services/job';
export default {
  name: 'Manage',
  components: {
    Authorized,
    Result,
    Page,
    // configCI,
    pipelinesLogs,
    ScanDialog,
    SafeTest,
    PlanDateUpdateDialog,
    ChangeBatchDialog,
    AddBatchMessage,
    Uploader
  },
  data() {
    return {
      isTest: '', //为0表示不涉及内测，1表示涉及内测
      AddBatchMessageDialogOpened: false,
      selsectRelationDialog: false,
      updateFinishTimeModel: createdFinishModel(),
      addSelectRelationModel: createdSelectRelationModel(),
      finishBtn: false,
      nextStageStatus: false,
      selectRowRelatorId: '',
      selectRowCurrentId: '',
      fileNameTmp: '',
      pathTmp: '',
      selectRowRid: '',
      fileList: [],
      showDialogOpen: false,
      pagination: getPagination(),
      noCodeData: [],
      users: [],
      userOptions: [],
      noCodeUsers: [],
      jobProfileNOCode: createJobModel(),
      taskType: '',
      ...perform,
      url: baseUrl,
      loading: false,
      stepLoading: false,
      uatReceivingParty: {},
      planDateUpdateDialogOpen: false,
      step: null,
      btnToGray: true, //下一阶段按钮置灰
      job: createJobModel(),
      joinPeople: [],
      joinPeopleExceptTester: [],
      filesColumns: [
        {
          name: 'fileName',
          align: 'left',
          label: '文件名称',
          field: 'fileName'
        },
        {
          name: 'createTime',
          align: 'left',
          label: '创建时间',
          field: 'createTime'
        },
        {
          name: 'operation',
          align: 'left',
          label: '操作',
          field: 'operation'
        }
      ],
      noCodeColumns: [
        {
          name: 'relatorName',
          label: '相关人员',
          align: 'left',
          field: 'relatorName'
        },
        {
          name: 'currentName',
          label: '发起人',
          align: 'left',
          field: 'currentName'
        },
        {
          name: 'taskStartTime',
          label: '任务开始时间',
          align: 'left',
          field: 'taskStartTime'
        },
        {
          name: 'taskEndTime',
          label: '任务结束时间',
          align: 'left',
          field: 'taskEndTime'
        },
        {
          name: 'taskStatus',
          label: '状态',
          align: 'left',
          field: 'taskStatus'
        },
        {
          name: 'fileName',
          label: '文件名',
          align: 'left',
          field: 'fileName'
        },
        {
          name: 'operation',
          label: '操作',
          align: 'left',
          field: 'operation'
        }
      ],
      columns: [
        { name: 'name', label: '环境', align: 'center', field: 'name' },
        {
          name: 'registryAddress',
          label: '注册中心地址',
          align: 'center',
          field: 'registryAddress'
        },
        {
          name: 'caasAddress',
          label: 'caas地址',
          align: 'center',
          field: 'caasAddress'
        },
        {
          name: 'configAddress',
          label: '配置中心地址',
          align: 'center',
          field: 'configAddress'
        },
        {
          name: 'zuulAddress',
          label: 'zuul网关地址',
          align: 'center',
          field: 'zuulAddress'
        }
      ],

      filter: '',
      nodes: [{ label: '关联文档', children: [] }],
      expanded: ['关联文档'],
      toggleFile: false,
      uploadFile: '',

      releaseNodes: [],
      sitModel: {},
      uatModel: {
        releaseNode: ''
      },

      merger: {},
      merging: false,
      testData: testData(),
      pickFileTypeName: '设计类',
      percentage: 0,
      appinfo: [],
      UATDialog: false,
      SITDialog: false,
      lackOfEnvWarning: false,
      lackOfEnv: '',
      scanDialogOpen: false,
      openRefuseReleaseDialog: false,
      syncRedmineData: createRedmineData(),
      filterReleaseNodes: [],
      dateList: [
        'plan_start_time',
        'plan_inner_test_time',
        'plan_uat_test_start_time',
        'plan_rel_test_time',
        'plan_fire_time'
      ],
      reviewDialog: false,
      reviewData: {
        type: '',
        firstReview: '',
        doc: ''
      },
      dataTypeOptions: ['informix', 'mysql', 'oracle'],
      firstReviewOptions: [],
      firstReviewOptionsCopy: [],
      secondReviewDialog: false,
      hasNotified: '否',
      hasDone: '否',
      childShow: false,
      testPackageDia: false,
      reinforce: '0',
      testPackageTypeOptions: [
        { testPackageType: '否', id: 'test' },
        { testPackageType: '兼容性测试包', id: 'jrcs' },
        { testPackageType: '自动化测试包', id: 'auto' },
        { testPackageType: '压测包', id: 'yace ' }
      ],
      jgOption: [{ label: '加固', id: '1' }, { label: '不加固', id: '0' }],
      jgStyle: '0',
      testPackageType: '0',
      currentUploadFile: [],
      testKeynote: '',
      setBatchDialogOpened: false,
      setBatchModel: {},
      resolve: null,
      fileTypeOptions: fileTypeOptions,
      ysCode: {},
      modelArr: {},
      serviceid: '',
      versionNo: '',
      reRnVersionNo: '',
      deRnVersionNo: ''
    };
  },
  validations: {
    updateFinishTimeModel: {
      finishTime: {
        required
      },
      taskDesc: {
        required
      }
    },
    addSelectRelationModel: {
      selectUser: {
        required
      }
    },
    reviewData: {
      type: {
        required
      },
      firstReview: {
        required
      }
    },
    sitModel: {},
    uatModel: {
      releaseNode: {
        required
      }
    }
  },
  watch: {
    ysCode(val) {
      if (val && Object.getOwnPropertyNames(val).length > 0) {
        let arr = {};
        for (let key in this.ysCode.children) {
          if (
            this.ysCode.children[key].children &&
            this.ysCode.children[key].children.length > 0
          ) {
            this.ysCode.children[key].children.map(item => {
              Reflect.set(arr, item.key, Object.keys(item.children)[0]);
            });
          }
        }
        this.modelArr = arr;
      }
    },
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    async 'job.stage'(val) {
      if (val) {
        if (val.code === 1 || (val.code === 0 && this.job.isTest === '0')) {
          this.step = await this.queryTaskCanEntryUat();
          return;
        }
        this.step = val.code;
      }
    },
    merging(val) {
      this.UATDialog = val;
      this.SITDialog = val;
    },
    async step(val) {
      if (this.jobProfileNOCode.taskType == 1) {
        this.taskType = 1;
        // uat rel 都完成  点回sit显示sit阶段数据
        if (
          val === 1 &&
          (this.jobProfileNOCode.nocodeInfoMap.uat.nextStageStatus == true ||
            this.jobProfileNOCode.nocodeInfoMap.rel.nextStageStatus == true)
        ) {
          let sitNOCodeInfo = this.jobProfileNOCode.nocodeInfoMap.sit;
          this.updateFinishTimeModel.taskDesc = sitNOCodeInfo.taskDesc;
          this.updateFinishTimeModel.finishTime = sitNOCodeInfo.finishTime
            ? sitNOCodeInfo.finishTime
            : null;
          let noCodeData = sitNOCodeInfo.relators;
          if (noCodeData) {
            this.noCodeData = sitNOCodeInfo.relators;
          }
        }
        //sit uat rel 都完成  点回uat显示uat阶段数据
        if (
          val === 2 &&
          (this.jobProfileNOCode.nocodeInfoMap.rel.nextStageStatus == true ||
            this.jobProfileNOCode.nocodeInfoMap.uat.nextStageStatus == true ||
            this.jobProfileNOCode.nocodeInfoMap.sit.nextStageStatus == true)
        ) {
          let uatNOCodeInfo = this.jobProfileNOCode.nocodeInfoMap.uat;
          this.updateFinishTimeModel.taskDesc = uatNOCodeInfo.taskDesc;
          this.updateFinishTimeModel.finishTime = uatNOCodeInfo.finishTime
            ? uatNOCodeInfo.finishTime
            : null;
          let noCodeData = uatNOCodeInfo.relators;
          if (noCodeData) {
            this.noCodeData = uatNOCodeInfo.relators;
          }
        }
        //sit uat rel 都完成  点回rel显示rel阶段数据
        if (
          val === 3 &&
          (this.jobProfileNOCode.nocodeInfoMap.rel.nextStageStatus == true ||
            this.jobProfileNOCode.nocodeInfoMap.uat.nextStageStatus == true ||
            this.jobProfileNOCode.nocodeInfoMap.sit.nextStageStatus == true)
        ) {
          let relNOCodeInfo = this.jobProfileNOCode.nocodeInfoMap.rel;
          this.updateFinishTimeModel.taskDesc = relNOCodeInfo.taskDesc;
          this.updateFinishTimeModel.finishTime = relNOCodeInfo.finishTime
            ? relNOCodeInfo.finishTime
            : null;
          let noCodeData = relNOCodeInfo.relators;
          if (noCodeData) {
            this.noCodeData = relNOCodeInfo.relators;
          }
        }
      } else {
        this.merging = false;
        const id = this.$route.params.id;
        let status;
        await this.queryApplication({ id: this.job.project_id });
        this.appinfo = this.appData;
        await this.getBfflePoint();
        if (val === 1 || val === 0) {
          await this.queryMerger({
            id: id,
            type: 'sit'
          });
          status = parseInt(this.job.sit_merge_status.status_code);
          await this.queryEnv({
            id,
            type: 'sit'
          });
          await this.checkSccOrCaas({
            id,
            type: 'sit'
          });
          this.job.env = this.envData;
          await this.queryIsNoApplyInterface({
            serviceCalling: this.job.project_name,
            branch: this.job.branch
          });
        } else if (val === 2) {
          if (
            this.taskCanEntryUat ||
            this.jobProfile.stage === 'uat' ||
            this.jobProfile.stage === 'rel'
          ) {
            await this.queryMerger({
              id: id,
              type: 'uat'
            });
          }
          await this.isTaskManager({
            id: id,
            service_id: this.job.project_name
          });
          // type传参，ios的传2：原生窗口，其他的传1：微服务
          await this.queryReleaseNode({
            start_date: date.formatDate(Date.now(), 'YYYY-MM-DD'),
            type: this.isTestRunApp ? '2' : '1'
          });
          this.releaseNodes = formatOption(
            this.releaseNodeList,
            'release_node_name'
          );
          this.releaseNodes.forEach(item => {
            item.optionLabel = `${item.owner_group_name}：${
              item.release_node_name
            }`;
          });
          status = parseInt(this.job.uat_merge_status.status_code);

          await this.queryReleaseNodeByJob({
            task_id: id
          });
          this.job.releaseNode = this.releaseNodeData;
          if (
            this.job.releaseNode.task_status &&
            this.job.releaseNode.task_status === '1'
          ) {
            await this.queryEnv({
              id,
              type: 'uat'
            });
            this.job.env = this.envData;
            if (this.job.env.length === 0) {
              this.lackOfEnv = 'UAT';
              this.lackOfEnvWarning = true;
            }
          }
        } else if (val === 3) {
          await this.queryReleaseNodeByJob({
            task_id: id
          });
          this.job.releaseNode = this.releaseNodeData;
          this.$set(
            this.job,
            'product_tag',
            this.job.releaseNode.release_application.product_tag
          );
          await this.queryEnv({
            id,
            type: 'rel'
          });
          this.job.env = this.envData;
          if (this.job.env.length === 0) {
            this.lackOfEnv = 'REL';
            this.lackOfEnvWarning = true;
          }
        }
        this.merging = status > 0 && status < 3;
      }
    },
    toggleFile(val) {
      setToggleFile(val);
      if (val) {
        this.$refs.tree.setExpanded(this.expanded[0], true);
      }
    },
    'job.releaseNode.task_status'(val) {
      if (val === '2') {
        this.openRefuseReleaseDialog = true;
      }
    }
  },
  filters: {
    transList(val) {
      return val.join('、');
    },
    filterBranch(val) {
      return 'dev' + val.substring(7);
    }
  },
  computed: {
    options1() {
      return this.checkOptions(this.ysCode.children);
    },
    canSubmit() {
      let type = {
        tip: '',
        flag: false
      };
      if (this.modelArr) {
        if (
          this.modelArr.Environment === '7' &&
          !this.ysCode.children[0].children[0].children[7].children.serviceid
            .placehoder
        ) {
          type.tip = '请输入交易服务器';
          type.flag = true;
        }
        if (this.modelArr.version === '1' && !this.versionNo) {
          type.tip = '请输入版本号';
          type.flag = true;
        }
        if (this.modelArr.rnversion === '1' && !this.reRnVersionNo) {
          type.tip = '请输入RNBundle版本号';
          type.flag = true;
        }
        if (this.modelArr.rnversion === '2' && !this.deRnVersionNo) {
          type.tip = '请输入RNBundle版本号';
          type.flag = true;
        }
      }
      return type;
    },
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser', 'userList']),
    ...mapGetters('user', ['isLoginUserList', 'isKaDianManager']),
    ...mapState('userForm', ['groups', 'redmineData']),
    ...mapState('jobForm', [
      'mergerData',
      'jobProfile',
      'docDetail',
      'envData',
      'deployFlag',
      'reviewRecordStatus',
      'secondReviewers',
      'taskCanEntryUat',
      'codeQuality',
      'upNoCodeInfo',
      'deleteFileRidInfo',
      'delNoCodeRelatorInfo',
      'uploadFilesRidInfo',
      'bafflePointFlag',
      'checkList',
      'mountUnitFlag'
    ]),
    ...mapState('appForm', ['appData', 'pipelinesList']),
    ...mapState('interfaceForm', ['taskManagerLimit', 'flag', 'transId']), // 扫描权限控制，只能当前任务的行内项目负责人，任务负责人，开发人员
    ...mapState('releaseForm', [
      'releaseNodeData',
      'releaseNodeList',
      'jobReleaseNodeData',
      'returnMsg',
      'appUserInfo'
    ]),
    disInput() {
      return (
        !this.isManagerOrDeveloper ||
        (this.isManagerOrDeveloper &&
          this.jobProfileNOCode.stage == 'uat' &&
          this.step == 1) ||
        (this.isManagerOrDeveloper &&
          this.jobProfileNOCode.stage == 'rel' &&
          this.step != 3) ||
        (this.isManagerOrDeveloper &&
          this.jobProfileNOCode.stage == 'production' &&
          (this.step != 1 || this.step != 2 || this.step != 3))
      );
    },
    showFileUpload() {
      return (
        this.selectRowRelatorId == this.currentUser.id &&
        !(this.jobProfileNOCode.stage == 'uat' && this.step == 1) &&
        !(
          this.jobProfileNOCode.stage == 'rel' &&
          (this.step == 1 || this.step == 2)
        ) &&
        !(this.jobProfileNOCode.stage == 'production' && this.step != 4)
      );
    },
    // 点回sit uat rel 判断按钮下拉框是否显示
    noShowBtns() {
      return (
        this.isManagerOrDeveloper &&
        !(this.jobProfileNOCode.stage == 'uat' && this.step == 1) &&
        !(
          this.jobProfileNOCode.stage == 'rel' &&
          (this.step == 1 || this.step == 2)
        ) &&
        !(this.jobProfileNOCode.stage == 'production' && this.step != 4)
      );
    },
    showPipeline() {
      return this.appinfo.length > 0 && this.appinfo[0].appCiType !== 'fdev-ci';
    },
    sieFeature() {
      return 'dev' + this.jobProfile.feature_branch.substring(7);
    },
    releaseNodeTaskStatus() {
      return this.job.releaseNode.task_status === '2';
    },
    // 设计审核按钮的权限---任务相关人员（除创建者）
    isDesignManager() {
      let reviewer = this.jobProfile.reviewer ? this.jobProfile.reviewer : {};
      let role = getIdsFormList([
        this.jobProfile.master,
        this.jobProfile.spdb_master,
        this.jobProfile.developer,
        this.jobProfile.tester,
        reviewer
      ]);
      return (
        role.indexOf(this.currentUser.id) >= 0 ||
        this.isKaDianManager ||
        this.currentUser.role.some(item => item.name === 'UI团队负责人')
      );
    },
    examEnv() {
      //对于不涉及环境部署的应用，不进行卡点
      if (
        this.appData[0] &&
        this.appData[0].label.indexOf('不涉及环境部署') > -1
      ) {
        return false;
      } else {
        return !this.deployFlag;
      }
    },
    examLabel() {
      return (
        !!this.appData[0] &&
        this.appData[0].label.indexOf('不涉及环境部署') > -1
      );
    },
    examineStatus() {
      if (
        this.job.review.data_base_alter[0] &&
        this.job.review.data_base_alter[0].name === '是' &&
        this.reviewRecordStatus === '待审核'
      ) {
        return '分支合并的前置条件（至少发起一次审核）尚未完成，无法点击，请联系行内负责人发起审核';
      } else {
        return false;
      }
    },
    examineType() {
      if (this.examineStatus) {
        return false;
      }
      if (
        this.job.review.data_base_alter[0] &&
        this.job.review.data_base_alter[0].name === '是' &&
        (this.reviewRecordStatus === '初审中' ||
          this.reviewRecordStatus === '初审拒绝' ||
          this.reviewRecordStatus === '复审拒绝')
      ) {
        return '数据库变更初审未通过，可联系对应的DBA审核人';
      } else {
        return false;
      }
    },
    /* 任务负责人、行内项目负责人 */
    isManager() {
      if (this.job.spdb_master) {
        const manager = this.job.spdb_master.concat(this.job.master);
        return (
          manager.some(user => user.id === this.currentUser.id) ||
          this.isKaDianManager
        );
      }
      return false;
    },
    /* 行内项目负责人 */
    isSPDBManager() {
      return (
        this.job.spdb_master.some(user => user.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    /* 开发人员、任务负责人、行内项目负责人 */
    isManagerOrDeveloper() {
      const isDeveloper = this.job.developer.some(
        user => user.id === this.currentUser.id
      );
      return this.isManager || isDeveloper || this.isKaDianManager;
    },
    haveReleaseFile() {
      const releaseFile = this.job.doc.filter(file => file.type === '投产类');
      return releaseFile.length > 1;
    },
    examReviewBtnShow() {
      return (
        this.job.review.data_base_alter &&
        this.job.review.data_base_alter[0] &&
        this.job.review.data_base_alter[0].name === '是'
      );
    },
    examReviewBefore() {
      //非行内负责人提示
      //有数据库变更且发起审核前
      return (
        !this.isSPDBManager &&
        this.job.review.data_base_alter[0] &&
        this.job.review.data_base_alter[0].name === '是' &&
        (this.reviewRecordStatus === '待审核' ||
          this.reviewRecordStatus === '初审拒绝' ||
          this.reviewRecordStatus === '复审拒绝')
      );
    },
    examReviewAfter() {
      //所有人提示
      //有数据库变更且发起审核后
      return (
        this.reviewRecordStatus === '初审中' ||
        this.reviewRecordStatus === '复审中'
      );
    },
    examDbFile() {
      let existDbFile = this.job.doc.some(
        item => item.type === '审核类-数据库审核材料'
      );
      return this.isSPDBManager && !existDbFile;
    },
    examReviewBtnDisable() {
      let existDbFile = this.job.doc.some(
        item => item.type === '审核类-数据库审核材料'
      );
      return (
        !this.isSPDBManager ||
        this.reviewRecordStatus === '初审中' ||
        this.reviewRecordStatus === '复审中' ||
        !existDbFile
      );
    },
    examDelete: function() {
      return function(params) {
        if (
          params.type === '审核类-数据库审核材料' &&
          (this.reviewRecordStatus === '初审中' ||
            this.reviewRecordStatus === '复审中')
        ) {
          return false;
        }
        return true;
      };
    },
    tooptip() {
      if (this.examineType) {
        return this.examineType;
      }
      if (this.flat) {
        return `当前应用调用的REST接口${this.transId.join(
          '、'
        )}尚未审批通过，请于“接口及路由/接口列表”页面发起申请。`;
      }
      if (
        this.job.sit_merge_id.length === 0 ||
        (this.mergerData.status_code === '5' && !this.mergerData.status)
      ) {
        return '进入下一阶段的前置条件(至少有一次成功的代码合并及部署)尚未完成，无法点击！';
      }
      if (this.mergerData.status_code === '1') {
        return '请联系应用负责人在gitlab上进行代码审核并合并';
      }
      if (this.mergerData.status_code === '2') {
        return '目前正在执行持续集成的过程，还请耐心等待应用的部署';
      }
      if (this.job.stage.code === 0 && this.isTest === '1') {
        return '该任务尚未走fdev线上提测，请点击下方“提测按钮”提测！';
      }
      if (this.testData.length === 0 && this.isTest === '1') {
        return '任务工单异常';
      } else {
        if (
          this.testData.status !== '3' &&
          this.testData.status !== '9' &&
          this.isTest === '1'
        ) {
          return `SIT测试尚未完成，可联系对应的内测人员${this.nameFilter(
            this.job.tester
          )}咨询！`;
        }
      }
      return false;
    },
    isShowNotify() {
      return this.job.system_remould === '是';
    },
    isShowServe() {
      return this.job.impl_data === '是';
    },
    toopTipToTestRun() {
      if (
        this.job.sit_merge_id.length === 0 ||
        (this.mergerData.status_code === '5' && !this.mergerData.status)
      ) {
        return '进入试运行的前置条件(至少有一次成功的代码合并及部署)尚未完成，无法点击！';
      }
      if (this.mergerData.status_code === '1') {
        return '请联系应用负责人在gitlab上进行代码审核并合并';
      }
      if (this.mergerData.status_code === '2') {
        return '目前正在执行持续集成的过程，还请耐心等待应用的部署';
      }
      return false;
    },
    isTestRunApp() {
      // 任务绑定的应用类型是Android应用或者IOS应用，可以进行试运行，其他任务不可以。
      if (this.appinfo[0] && this.appinfo[0].type_name) {
        return (
          this.appinfo[0].type_name === 'Android应用' ||
          this.appinfo[0].type_name === 'IOS应用'
        );
      }
      return false;
    },
    uploadedFiles() {
      const { pickFileTypeName, docDetail } = this;
      const files = [];

      if (!docDetail.doc) return files;

      docDetail.doc.forEach(file => {
        if (file.type === pickFileTypeName) {
          files.push(file.name);
        }
      });
      return files;
    }
  },
  methods: {
    checkOptions(val) {
      let arr = [];
      if (val) {
        if (Array.isArray(val)) {
          val.map((item, index) => {
            for (let key in item) {
              arr.push({
                id: key,
                label: item[key].title
              });
            }
          });
        } else if (Object.prototype.toString.call(val) === '[object Object]') {
          Object.keys(val).map(item => {
            arr.push({
              id: item,
              label: typeof val[item] === 'object' ? val[item].title : val[item]
            });
          });
        }
      }
      return arr;
    },
    ...mapActions('databaseForm', ['upload']),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    ...mapActions('userForm', ['queryRedmineById']),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('interfaceForm', {
      isTaskManager: 'isTaskManager',
      queryIsNoApplyInterface: 'queryIsNoApplyInterface'
    }),
    ...mapActions('jobForm', [
      'queryMerger',
      'queryJobProfile',
      'queryDocDetail',
      'putSitTest',
      'putUatTest',
      'queryEnv',
      'checkSccOrCaas',
      'createFirstReview',
      'deleteFile',
      'queryReviewRecordStatus',
      'saveReviewRecord',
      'getJobUser',
      'queryByTaskIdNode',
      'getCodeQuality',
      'iOSOrAndroidAppPackage',
      'downExcel',
      'deleteFileNew',
      'updateNocodeInfo',
      'noCodeRelator',
      'deleteFileRid',
      'delNoCodeRelator',
      'filesUpload',
      'addNoCodeRelator',
      'nextStage',
      'uploadFilesRid',
      'bafflePoint',
      'updateTaskDoc',
      'deleteTaskDoc'
    ]),
    ...mapActions('appForm', ['queryApplication', 'queryPipelines']),
    ...mapActions('releaseForm', [
      'queryReleaseNodeByJob',
      'queryReleaseNode',
      'addJobReleaseNode',
      'taskChangeNotise',
      'addBatch',
      'queryBatchInfoByAppId'
    ]),
    async updateFinishTime() {
      this.$v.updateFinishTimeModel.$touch();
      let updateFinishTimeKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('updateFinishTimeModel') > -1
      );
      validate(updateFinishTimeKeys.map(key => this.$refs[key]));

      if (this.$v.updateFinishTimeModel.$invalid) {
        return;
      }
      this.btnToGray = false;
    },
    async submitTestKeynote() {
      await this.testKeyNote({
        testKeyNote: this.testKeynote,
        id: this.job.id
      });
      successNotify('提测重点提测成功');
    },
    //下一阶段
    async noCodeTaskNext() {
      this.$v.updateFinishTimeModel.$touch();
      let updateFinishTimeKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('updateFinishTimeModel') > -1
      );
      validate(updateFinishTimeKeys.map(key => this.$refs[key]));
      if (this.$v.updateFinishTimeModel.$invalid) {
        return;
      }
      if (
        this.jobProfileNOCode.stage == 'sit' ||
        this.jobProfileNOCode.stage == 'uat'
      ) {
        const params = {
          id: this.jobProfileNOCode.id
        };
        this.loading = true;
        await this.nextStage(params);
        this.loading = false;
        this.btnToGray = true;
        await this.init();
        successNotify('进入下一阶段!');
      } else {
        const params = {
          id: this.jobProfileNOCode.id
        };
        this.loading = true;
        await this.nextStage(params);
        this.loading = false;
        this.btnToGray = true;
        await this.init();
        this.$router.push(`/job/list/${this.jobProfileNOCode.id}`);
        successNotify('确认投产成功!');
      }
    },
    //根据相关人员id删除文件
    async handleDeleteFile(row) {
      const param = {
        id: this.jobProfileNOCode.id,
        fileName: row.fileName,
        rid: this.selectRowRid
      };
      await this.deleteFileRid(param);
      this.fileList = this.deleteFileRidInfo;
      successNotify('删除成功');
    },
    async uploadFiles(event) {
      let formData = new FormData();
      var fileName = this.$refs.fileInput.files[0].name;
      this.fileNameTmp = fileName;
      var fileSize = this.$refs.fileInput.files[0].size;
      var path = '/fdev-task' + '/' + this.selectRowRid + '/' + fileName;
      this.pathTmp = path;
      if (fileSize > 0 && fileSize <= 31457280 && path) {
        var moduleName = 'fdev-task';
        formData.append('moduleName', moduleName);
        formData.append('files', this.$refs.fileInput.files[0]);
        formData.append('path', path);
        formData.append('user', '');
        if (this.selectRowRelatorId != this.currentUser.id) {
          errorNotify('您暂无权限上传！');
          return;
        }
        if (
          this.fileList.length > 0 &&
          this.fileList.some(file => {
            return file.fileName === this.fileNameTmp;
          })
        ) {
          this.$q
            .dialog({
              title: '上传提示',
              message: `该任务下已存在同名文件已为您替换`
            })
            .onOk(() => {
              this.filesUpload(formData);
            });
        } else {
          await this.filesUpload(formData);
        }
        successNotify('上传成功！');
      }
      const params = {
        id: this.jobProfileNOCode.id,
        rid: this.selectRowRid,
        fileName: this.fileNameTmp,
        path: path
      };
      await this.uploadFilesRid(params);
      if (this.uploadFilesRidInfo.length > 0) {
        this.fileList = this.uploadFilesRidInfo;
      }
    },
    confirmToClose(e) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.showDialogOpen = false;
          this.fileList = [];
          this.init();
        });
    },
    async selsectRelationDialogOpen() {
      this.selsectRelationDialog = true;
    },
    async fileManageClose() {
      this.showDialogOpen = false;
      this.fileList = [];
    },
    //文件管理保存参数
    openWpsFileManger(row) {
      this.showDialogOpen = true;
      this.selectRowRid = row.rid;
      if (row.files) {
        this.fileList = row.files;
      } else {
        this.fileList = [];
      }
      this.selectRowCurrentId = row.currentId;
      this.selectRowRelatorId = row.relatorId;
    },
    // 添加相关人员的确认
    async addRelationHandleBtn() {
      this.$v.addSelectRelationModel.$touch();
      let addSelectRelationKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('addSelectRelationModel') > -1
      );
      validate(addSelectRelationKeys.map(key => this.$refs[key]));

      if (this.$v.addSelectRelationModel.$invalid) {
        return;
      }
      const params = {
        id: this.jobProfileNOCode.id,
        relatorId: this.addSelectRelationModel.selectUser.id,
        relatorName: this.addSelectRelationModel.selectUser.user_name_cn,
        currentId: this.currentUser.id,
        currentName: this.currentUser.user_name_cn
      };
      await this.addNoCodeRelator(params);
      this.selsectRelationDialog = false;
      successNotify('添加相关人员成功！');
      await this.init();
    },
    //完成按钮是否显示
    showBtn(row) {
      return (
        (row.taskStatus === '未完成' && row.relatorId == this.currentUser.id) ||
        (row.taskStatus === '完成' && this.finishBtn == true)
      );
    },
    //文件管理的删除按钮是否显示
    showDelFileBtn(row) {
      return this.selectRowRelatorId == this.currentUser.id;
    },
    //删除相关人员
    async handleDelBtn(row) {
      const params = {
        id: this.jobProfileNOCode.id,
        rid: row.rid
      };
      await this.delNoCodeRelator(params);
      await this.init();
      this.nextStageStatus = false;
      successNotify('删除成功！');
    },
    // 完成按钮
    async handleBtn(row) {
      const params = {
        id: this.jobProfileNOCode.id,
        rid: row.rid
      };
      await this.noCodeRelator(params);
      await this.init();
    },
    //确认修改按钮
    async updateNocode() {
      this.$v.updateFinishTimeModel.$touch();
      let updateFinishTimeKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('updateFinishTimeModel') > -1
      );
      validate(updateFinishTimeKeys.map(key => this.$refs[key]));

      if (this.$v.updateFinishTimeModel.$invalid) {
        return;
      }
      const params = {
        id: this.jobProfileNOCode.id,
        taskDesc: this.updateFinishTimeModel.taskDesc,
        finishTime: this.updateFinishTimeModel.finishTime
      };
      await this.updateNocodeInfo(params);
      await this.init();
      successNotify('修改成功！');
    },
    async noCodeUserFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    async handleIOSOrAndroid() {
      let type = '';
      if (this.testPackageType === '0') {
        type = this.modelArr.isbangbang || '';
      } else if (
        this.testPackageType === 'auto' ||
        this.testPackageType === 'jrcs' ||
        this.testPackageType === 'test' ||
        this.testPackageType === 'yace'
      ) {
        type = this.jgStyle;
      }
      let objectParams = Object.assign({}, this.modelArr);
      Reflect.set(objectParams, 'Type', this.testPackageType);
      Reflect.set(objectParams, 'isbangbang', type);
      Reflect.set(
        objectParams,
        'service_id',
        this.ysCode.children[0].children[0].children[7].children.serviceid
          .placehoder
      );
      Reflect.set(objectParams, 'appversion', this.versionNo);
      Reflect.set(objectParams, 'release_rnversion', this.reRnVersionNo);
      Reflect.set(objectParams, 'debug_rnversion', this.deRnVersionNo);
      await this.iOSOrAndroidAppPackage({
        id: this.jobProfile.id,
        ref: this.sieFeature,
        multi_env: objectParams,
        desc: this.jobProfile.name
      });
      this.closePackageDia();
      successNotify('出测试包成功！');
    },
    closePackageDia() {
      this.versionNo = '';
      this.reRnVersionNo = '';
      this.deRnVersionNo = '';
      this.jgStyle = '0';
      this.testPackageDia = false;
    },
    descFilter(input) {
      if (!input) {
        return input;
      }
      var reg = new RegExp(/\n/g);
      return input.replace(reg, '</br>');
    },
    async download(path) {
      let param = {
        path: path,
        moduleName: 'fdev-task'
      };
      await this.downExcel(param);
    },
    nameFilter(nameList) {
      if (Array.isArray(nameList)) {
        let names = [];
        nameList.forEach(item => {
          if (item.user_name_cn) {
            names.push(item.user_name_cn);
          }
        });
        return names.join('，') ? ` ( ${names.join('，')} ) ` : '';
      } else {
        return ` ( ${nameList} ) `;
      }
    },
    relNext() {
      this.stepLoading = true;
      this.step = 2;
      this.stepLoading = false;
    },
    async next() {
      //如果不涉及内测，sonar卡点从提测按钮移到挂载投产窗口按钮;
      if (this.isTest === '0') {
        await this.getCodeQuality({ task_id: this.job.id });
        // tips 不为空 都提示 然后开关状态 开 卡点  tips为空  直接过 正常流程
        if (this.codeQuality.tips) {
          this.$q
            .dialog({
              title: 'sonar描述提示',
              message: `${this.codeQuality.tips}`,
              ok: '知道啦'
            })
            .onOk(() => {
              if (this.codeQuality.qube_switch === '1') {
                return;
              } else {
                this.relNext();
              }
            });
        } else {
          this.relNext();
        }
      } else {
        this.relNext();
      }
    },
    async openTestPackageDia() {
      await this.getParamFile();
      this.testPackageDia = true;
    },
    goRel() {
      this.$router.push(
        `/release/list/${this.job.releaseNode.release_node_name}/joblist`
      );
    },
    async openStrengthenDialog() {
      await this.handleSIT();
    },
    async handleSIT() {
      this.loading = true;
      try {
        await this.putSitTest({
          id: this.job.id
        });
      } catch (e) {
        this.loading = false;
        throw e;
      }
      let id = this.$route.params.id;
      await this.queryMerger({
        id: id,
        type: 'sit'
      });
      this.loading = false;
      this.merging = true;
    },
    async getBfflePoint() {
      await this.bafflePoint({
        branch_name: this.job.branch,
        gitlab_project_id: this.appinfo[0].gitlab_project_id.toString()
      });
    },
    async handleAddJobReleaseNode() {
      this.$v.uatModel.$touch();
      let uatKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('uatModel') > -1
      );
      validate(uatKeys.map(key => this.$refs[key]));

      if (this.$v.uatModel.$invalid) {
        return;
      }

      await this.handleSetBatchDialogOpen();

      await this.addJobReleaseNode({
        release_node_name: this.uatModel.releaseNode.release_node_name,
        task_id: this.job.id
      });
      this.job.releaseNode = this.jobReleaseNodeData;
    },
    async handleUAT() {
      this.loading = true;
      await this.putUatTest({
        id: this.job.id
      });
      let id = this.$route.params.id;
      await this.queryMerger({
        id: id,
        type: 'uat'
      });

      this.loading = false;
      this.merging = true;
    },
    async handleREL() {
      this.loading = true;

      await this.putUatTest({
        id: this.job.id
      });

      this.loading = false;
      this.merging = true;
    },
    beforeUpload(files, formData) {
      const port = baseUrl.substr(baseUrl.length - 5, 4);
      const val =
        port === '8080' && process.env.NODE_ENV === 'production'
          ? 'fdev-resources'
          : 'fdev-resources-test';
      let { group, name, id } = this.jobProfile;
      name = encodeURI(name)
        .substring(0, 10)
        .replace(/%/g, 'a');
      const datas = files.map(file => {
        const path = `${val}/${group.name}/${name}-${id}/${
          this.pickFileTypeName
        }/${file.name}`;
        return {
          path,
          name: file.name,
          taskId: id,
          type: this.pickFileTypeName
        };
      });

      formData.append('datas', JSON.stringify(datas));
      formData.append('taskId', id);
    },
    async uploadSuccess() {
      let { id } = this.jobProfile;
      successNotify('上传成功!');
      await this.queryDocDetail({ id });
      this.job.doc = this.docDetail.doc;
      this.getTree2(this.docDetail.doc);
      this.expanded[1] = this.pickFileTypeName;
      //任务投产文件有变动或废弃通知
      const isReleaseUp = !!this.docDetail.doc.find(
        item => item.type === '投产类-变更材料类'
      );
      await this.queryReleaseNodeByJob({
        task_id: id
      });
      const { release_node_name } = this.releaseNodeData;
      if (isReleaseUp && release_node_name) {
        this.taskChangeNotise({
          release_node_name,
          task_id: id,
          type: '1'
        });
      }
    },
    getArray(val) {
      // 计划日期等不用改null为[]，日期控件选择时要用到
      for (let key in val) {
        if (this.dateList.indexOf(key) === -1 && val[key] === null) {
          val[key] = [];
        }
      }
    },
    getTree2(doc) {
      const arr = [];
      doc.forEach((item, test) => {
        let num = 0;
        let path = item.path;
        let existType = arr.slice(0).some((val, index) => {
          num = index;
          return val.label === item.type;
        });
        const typeArr = item.type.split('-');
        if (typeArr.length > 1) {
          //判断是否已有 '审核类' 节点
          let count = 0;
          let existReview = arr.slice(0).some((val, countNum) => {
            count = countNum;
            return val.label === typeArr[0];
          });
          if (existReview) {
            //若已存在 '审核类'节点, 直接改造 '审核类节点的子节点'
            //继续判断是否存在 '数据库审核材料' 节点
            let existDbType = arr[count].children.some(
              //这里判断是判断已渲染的arr中的label 所以是  '数据库审核材料'
              e => e.label === typeArr[1]
            );
            if (existDbType) {
              //存在 '数据库审核材料' 子节点
              arr[count].children
                .find(e => e.label === typeArr[1])
                .children.push({
                  header: 'level-2',
                  label: item.name,
                  name: item.name,
                  path: path,
                  type: item.type
                });
            } else {
              //不存在 '数据库审核材料' 子节点
              arr[count] = {
                ...arr[count],
                children: [
                  ...arr[count].children.slice(0), //审核类下的文件
                  {
                    //子类数据库审核材料
                    label: item.type,
                    header: 'level-1',
                    children: [
                      {
                        header: 'level-2',
                        label: item.name,
                        name: item.name,
                        path: path,
                        type: item.type
                      }
                    ]
                  }
                ]
              };
            }
          } else {
            //不存在审核类节点
            arr.push({
              label: typeArr[0],
              header: 'level-1',
              children: [
                {
                  header: 'level-1',
                  label: typeArr[1],
                  name: item.name,
                  children: [
                    {
                      header: 'level-2',
                      label: item.name,
                      name: item.name,
                      path: path,
                      type: item.type
                    }
                  ]
                }
              ]
            });
          }
        } else {
          if (!existType) {
            //type在已有的标签中不存在时
            arr.push({
              label: item.type,
              header: 'level-1',
              children: [
                {
                  header: 'level-2',
                  label: item.name,
                  name: item.name,
                  path: path,
                  type: item.type
                }
              ]
            });
          } else {
            //存在时
            arr[num] = {
              ...arr[num],
              children: [
                ...arr[num].children,
                {
                  header: 'level-2',
                  label: item.name,
                  name: item.name,
                  path: path,
                  type: item.type
                }
              ]
            };
          }
        }
      });
      this.nodes[0].children = arr;
    },
    handleScanDialogOpen() {
      this.scanDialogOpen = true;
    },
    getSpdbMaster(spdb_master) {
      return spdb_master
        .map(item => {
          return item.user_name_cn;
        })
        .join('，');
    },
    releaseNodeFilter(val, update) {
      update(() => {
        this.filterReleaseNodes = this.releaseNodes.filter(tag => {
          return tag.optionLabel.indexOf(val) > -1;
        });
      });
    },
    canClick() {
      let code = this.mergerData.status_code;
      let status = this.mergerData.status ? this.mergerData.status : false;
      return code === '3' || code === '4' || (code === '5' && status === true);
    },
    showTooltip() {
      return this.job.sit_merge_id.length === 0
        ? true
        : Object.keys(this.mergerData).includes('status')
        ? this.mergerData.status === false
        : this.testData.status !== '3' && this.testData.status !== '9';
    },
    closePlanDateUpdateDialog() {
      this.planDateUpdateDialogOpen = false;
    },
    // 数据库审核初审
    async startReview() {
      this.$refs.reviewDataType.validate();
      this.$refs.reviewDataFirst.validate();
      if (this.$v.reviewData.$invalid) return;
      const task_id = this.$route.params.id,
        doc = this.reviewData.doc,
        init_auditor_id = this.reviewData.firstReview,
        review_type = '数据库变更',
        db_type = this.reviewData.type;
      if (!this.reviewData.doc.trim()) {
        errorNotify('请输入审核描述信息');
        return;
      }
      //做卡点判断
      let systemFlag =
          this.job.system_remould === '是' && this.hasNotified === '否',
        serveFlag = this.job.impl_data === '是' && this.hasDone === '否';
      if (systemFlag && serveFlag) {
        errorNotify(
          '请先通知到关联供数系统进行配套改造,并梳理完所有需要暂服务的库表关联应用'
        );
        return;
      }
      if (systemFlag) {
        errorNotify('请先通知到关联供数系统进行配套改造');
        return;
      }
      if (serveFlag) {
        errorNotify('请先梳理完所有需要暂服务的库表关联应用');
        return;
      }
      //发起初审
      await this.createFirstReview({
        id: task_id,
        init_auditor_id
      });
      this.reviewDialog = false;
      const review_status = this.reviewRecordStatus;
      //保存记录
      //接口增加字段 auditor: {id:..,auditor_name_cn:...}
      await this.saveReviewRecord({
        task_id,
        doc,
        review_type,
        db_type,
        review_status,
        init_auditor_id,
        initiator: {
          id: this.currentUser.id,
          initiator_name_cn: this.currentUser.user_name_cn
        }
      });
      successNotify('已发起审核');
      this.queryReviewRecordStatus({
        id: this.$route.params.id
      });
    },
    // 数据库审核复审
    async startSecondReview() {
      const task_id = this.$route.params.id,
        doc = this.reviewData.doc,
        review_status = this.reviewRecordStatus;
      if (!doc.trim()) {
        errorNotify('请输入审核描述信息');
        return;
      }
      await this.saveReviewRecord({
        task_id,
        doc,
        review_status,
        initiator: {
          id: this.currentUser.id,
          initiator_name_cn: this.currentUser.user_name_cn
        }
      });
      successNotify('已发起审核');
      this.queryReviewRecordStatus({
        id: this.$route.params.id
      });
      this.secondReviewDialog = false;
    },
    handleFirstReviewer(val) {
      if (val === this.currentUser.id) {
        this.reviewData.firstReview = '';
        errorNotify('系统内初审人不能与数据库变更申请人是同一人！');
      }
    },
    userFilter(val, update, abort) {
      update(() => {
        this.firstReviewOptionsCopy = this.firstReviewOptions.filter(
          user =>
            user.name.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    // 数据库审核复审拒绝弹窗
    async showReviewDialog() {
      //状态是复审拒绝 弹另一个窗
      if (this.reviewRecordStatus === '复审拒绝') {
        this.secondReviewDialog = true;
        return;
      }
      this.reviewDialog = true;
      await this.getJobUser();
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
    },
    deleteUpload({ path }) {
      let delFileParam = {
        path: path,
        taskId: this.$route.params.id
      };
      this.$q
        .dialog({
          title: '删除文件',
          message: `确认删除此文件？`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          const id = this.$route.params.id;
          await this.deleteTaskDoc(delFileParam);
          await this.queryDocDetail({ id });
          this.job.doc = this.docDetail.doc;
          this.getTree2(this.docDetail.doc);
          successNotify('删除成功');
        });
    },
    async init() {
      const id = this.$route.params.id;
      const noCode = this.$route.params.noCode;
      await this.queryJobProfile({ id }); // 查询任务详情
      this.jobProfileNOCode = this.jobProfile;
      this.testKeynote = this.jobProfile.testKeyNote;
      if (this.jobProfileNOCode.taskType == 1 || noCode == 1) {
        this.getArray(this.jobProfileNOCode);
        this.getArray(this.jobProfileNOCode.review);
        this.joinPeople = Array.from(
          new Set([
            ...this.jobProfileNOCode.master.map(user => user.id),
            ...this.jobProfileNOCode.spdb_master.map(user => user.id),
            ...this.jobProfileNOCode.developer.map(user => user.id),
            ...this.jobProfileNOCode.tester.map(user => user.id)
          ])
        );
        this.joinPeopleExceptTester = Array.from(
          new Set([
            ...this.jobProfileNOCode.master.map(user => user.id),
            ...this.jobProfileNOCode.spdb_master.map(user => user.id),
            ...this.jobProfileNOCode.developer.map(user => user.id)
          ])
        );
        this.job = formatJob(this.jobProfileNOCode);
        this.taskType = 1;
        this.loading = true;
        await this.queryUser();
        //添加相关人员下拉列表
        this.users = this.isLoginUserList.map(user =>
          formatOption(formatUser(user), 'name')
        );
        this.userOptions = this.users.slice(0);
        //无代码变更sit阶段
        if (
          this.jobProfileNOCode.stage == 'sit' ||
          this.jobProfileNOCode.stage == 'production'
        ) {
          let nextStageStatusa = this.jobProfileNOCode.nocodeInfoMap.sit
            .nextStageStatus;
          // 若添加了相关人员 nextStageStatus为true时（状态都为完成，否则为未完成）下一阶段按钮可点
          if (this.jobProfileNOCode.nocodeInfoMap.sit.relators.length >= 0) {
            if (nextStageStatusa == true) {
              this.nextStageStatus = false;
            } else {
              this.nextStageStatus = true;
            }
          }
          let sitNOCodeInfo = this.jobProfileNOCode.nocodeInfoMap.sit;
          this.updateFinishTimeModel.taskDesc = sitNOCodeInfo.taskDesc;
          this.updateFinishTimeModel.finishTime = sitNOCodeInfo.finishTime
            ? sitNOCodeInfo.finishTime
            : null;
          let noCodeData = sitNOCodeInfo.relators;
          if (noCodeData) {
            this.noCodeData = sitNOCodeInfo.relators;
          }
        }
        if (
          this.jobProfileNOCode.stage == 'uat' ||
          this.jobProfileNOCode.stage == 'production'
        ) {
          this.step = 2;
          let nextStageStatusb = this.jobProfileNOCode.nocodeInfoMap.uat
            .nextStageStatus;
          // 若添加了相关人员 nextStageStatus为true时（状态都为完成，否则为未完成）下一阶段按钮可点
          if (this.jobProfileNOCode.nocodeInfoMap.uat.relators.length >= 0) {
            if (nextStageStatusb == true) {
              this.nextStageStatus = false;
            } else {
              this.nextStageStatus = true;
            }
          }
          let uatNOCodeInfo = this.jobProfileNOCode.nocodeInfoMap.uat;
          this.updateFinishTimeModel.taskDesc = uatNOCodeInfo.taskDesc;
          this.updateFinishTimeModel.finishTime = uatNOCodeInfo.finishTime
            ? uatNOCodeInfo.finishTime
            : null;
          let noCodeData = uatNOCodeInfo.relators;
          if (noCodeData) {
            this.noCodeData = uatNOCodeInfo.relators;
          }
        }
        // 若添加了相关人员 nextStageStatus为true时（状态都为完成，否则为未完成）下一阶段按钮可点
        if (
          this.jobProfileNOCode.stage == 'rel' ||
          this.jobProfileNOCode.stage == 'production'
        ) {
          this.step = 3;
          let nextStageStatusc = this.jobProfileNOCode.nocodeInfoMap.rel
            .nextStageStatus;
          if (this.jobProfileNOCode.nocodeInfoMap.rel.relators.length >= 0) {
            if (nextStageStatusc == true) {
              this.nextStageStatus = false;
            } else {
              this.nextStageStatus = true;
            }
          }
          let relNOCodeInfo = this.jobProfileNOCode.nocodeInfoMap.rel;
          this.updateFinishTimeModel.taskDesc = relNOCodeInfo.taskDesc;
          this.updateFinishTimeModel.finishTime = relNOCodeInfo.finishTime
            ? relNOCodeInfo.finishTime
            : null;
          let noCodeData = relNOCodeInfo.relators;
          if (noCodeData) {
            this.noCodeData = relNOCodeInfo.relators;
          }
        }
      } else {
        await this.queryReviewRecordStatus({ id });
        // 1.开启  2.关闭   3.通过
        // "1":"未开始","2":"sit","3":"uat"
        let status = this.jobProfile.test_info.stageStatus;
        if (status) {
          // 存在，重新赋值
          this.testData.status = status;
        }
        this.getArray(this.jobProfile);
        this.getArray(this.jobProfile.review);
        this.joinPeople = Array.from(
          new Set([
            ...this.jobProfile.master.map(user => user.id),
            ...this.jobProfile.spdb_master.map(user => user.id),
            ...this.jobProfile.developer.map(user => user.id),
            ...this.jobProfile.tester.map(user => user.id)
          ])
        );
        this.joinPeopleExceptTester = Array.from(
          new Set([
            ...this.jobProfile.master.map(user => user.id),
            ...this.jobProfile.spdb_master.map(user => user.id),
            ...this.jobProfile.developer.map(user => user.id)
          ])
        );
        this.loading = true;
        this.job = formatJob(this.jobProfile);
        this.uatReceivingParty = {
          label: this.job.uat_testObject,
          value: this.job.uat_testObject
        };
        await this.queryDocDetail({ id });
        this.job.doc = this.docDetail.doc;
        this.getTree2(this.docDetail.doc);
        this.toggleFile = !!getToggleFile();

        this.loading = false;
        if (
          this.job.devStartDate &&
          this.job.sitStartDate &&
          this.job.uatStartDate &&
          this.job.relStartDate &&
          this.job.productionDate
        ) {
          this.planDateUpdateDialogOpen = false;
        } else {
          this.planDateUpdateDialogOpen = true;
        }
      }
    },
    toDesignReview() {
      this.$router.push(`/job/list/${this.job.id}/design`);
    },
    toTestRun() {
      this.$router.push(`/job/list/${this.job.id}/testRun`);
    },
    // 还原审核权限        this.job.review_status !== 'irrelevant' &&this.job.review_status !== 'finished'
    needDesignExamine() {
      return this.appinfo[0] && this.jobProfile.type_name === 'Vue应用';
    },
    // SIT合并权限 还原审核通过或者不需要审核must
    designExaminePass() {
      if (
        this.appinfo[0] &&
        this.jobProfile.demand.ui_verify &&
        this.jobProfile.type_name === 'Vue应用' &&
        (this.job.review_status === 'wait_upload' ||
          this.job.review_status === 'uploaded' ||
          this.job.review_status === 'irrelevant')
      ) {
        return false;
      }
      return true;
    },
    //判断是否是ui审核任务
    needUiAuditing() {
      if (
        this.appinfo[0] &&
        this.jobProfile.demand.ui_verify &&
        this.jobProfile.type_name === 'Vue应用'
      ) {
        return true;
      }
      return false;
    },
    async queryTaskCanEntryUat() {
      /* job.stage === 1(任务阶段sit)时，
       * 查是否挂载投产窗口，已挂载则发接口
       * 查是否 确认投产，“是”则将step改为2。
       */
      await this.queryReleaseNodeByJob({
        task_id: this.$route.params.id
      });
      const { release_node_name } = this.releaseNodeData;
      if (!release_node_name) {
        return 1;
      }
      await this.queryByTaskIdNode({
        release_node_name: release_node_name,
        task_id: this.$route.params.id
      });
      if (this.taskCanEntryUat) return 2;
      return 1;
    },
    linkToReleaseApps() {
      const { release_node_name } = this.releaseNodeData;
      this.$router.push(`/release/list/${release_node_name}/applist`);
    },
    async handleTestConfirmDialogOpened() {
      await this.submitTestData();
      this.testConfirmDialogOpened = false;
    },
    logFilter(val) {
      val = val ? val : '';
      return val
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    handleSetBatchDialogOpen() {
      return new Promise((resolve, reject) => {
        const { releaseNode } = this.uatModel;
        this.setBatchModel = {
          release_node_name: releaseNode.label,
          application_id: this.job.project_id
        };
        this.setBatchDialogOpened = true;
        this.resolve = resolve;
      });
    },
    async handleSetBatchDialog(data) {
      await this.addBatch(data);
      this.setBatchDialogOpened = false;
      this.resolve();

      await this.queryBatchInfoByAppId({
        release_node_name: data.release_node_name,
        application_id: data.applications
      });

      if (this.appUserInfo.length > 0) {
        this.AddBatchMessageDialogOpened = true;
      }
    },
    //获取参数
    async getParamFile() {
      let response = await resolveResponseError(() => queryParamFile());
      this.ysCode = response;
    },
    //更新参数
    updateDate() {
      this.testData = testData();
      let status = this.jobProfile.test_info.stageStatus;
      if (status) {
        // 存在，重新赋值
        this.testData.status = status;
      }
    },
    //提交功能测试后刷新任务详情里面状态
    async updateDetail() {
      const id = this.$route.params.id;
      await this.queryJobProfile({ id }); // 查询任务详情
    }
  },
  async created() {
    await this.init();
    await this.fetchUser();
    this.isTest = this.job.isTest;
  }
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.min-width
  min-width: 400px
  width: 400px!important
.testPacWid
  max-width 300px
.spinner
  text-align: center
.merge
  padding: 2px;
  background: white;
  color: #0663BE;
  font-size: 13px;
  font-weight: $text-weights.medium;
  letter-spacing: 2px;
  opacity: 0.87;
.profile
  position: absolute;
  top: 9px;
  right: 30px;
  height 36px
.toggle-file
  position: absolute;
  top: 40px;
  right: 4px;
  height: 28px;
.btn-lg
  padding-left: 20px !important;
  padding-right: 20px !important;
.form
  padding: 16px;
  max-width: 820px;
  margin: 0 auto;
.btn-uploader
  z-index: 200;
.q-card
  width: 500px;
  background: white;
.text-wrapper
  word-break:break-all;
  word-wrap: break-word;
.q-item__label
  min-width: 60px;
.q-item__section--thumbnail img
  width: 80px;
.q-item__section--main ~ .q-item__section--side
  position:relative
  .delete
    position: relative;
    top: 50%;
    transform: translateY(-50%);
.warning
  margin: 10px auto;
.p-red
  color: red;
  .warn
    margin-left: 20px;
    margin-top: 20px;
  .item
    margin-left: 44px;
    margin-bottom: 6px;
.pointer
  cursor: pointer
.scroll
  width: 380px;
  margin: 0 auto;
.divitem
  margin-left: 45px;
.dialog-container
  width: 700px;
  max-width: 80vw;
.sit-content
  margin: 0 auto;
  width: 80%;
.reviewDesc
  font-size: 16px;
  color: #666;
.testData
  width 100%
  .center
    align-items center
.q-textarea >>> textarea
  word-break break-all;
.link
  cursor: pointer;
  color: #2196f3;
.archive-btn
  margin-left: 5px;
.td-padding
  padding-right 16px!important
.fdev-desc
    max-width 300px
    overflow hidden
    text-overflow ellipsis
.next-btn
  margin: 20px auto
.edit-btn
  margin: 0 auto
  display: block
.desc-input
  width 100%
  min-height 60px
  max-height 120px
  position relative
  top -8px
.finsh-time
  width 180px
  position relative
  top -8px
.select-relation
  height 80px
  width 220px
  position relative
  top -16px
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.dialog-relation
  margin-left: 120px
.upload >>> .upload-body
  max-height 393px
  overflow auto
.pipeline-mrgin
  margin-top 20px
  margin-bottom 32px
.uat-submit
  margin-left 350px
.btn-style >>>   span.q-btn__wrapper.col.row.q-anchor--skip
    padding: 0;
    min-height: 10px;
.btn-style >>> .upload
  width: auto
  max-width 432px
.width-280
  width:280px
.word-break
  word-break break-all
.max-width
  max-width 432px
  .q-tree
    background #fff
</style>
