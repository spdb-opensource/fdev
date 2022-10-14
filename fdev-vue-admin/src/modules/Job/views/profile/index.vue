<template>
  <f-block>
    <!-- 最上方信息以及按钮展示 -->
    <div class="row q-mt-xs justify-between justify-content-flex-end">
      <div>
        <div class="row items-center">
          <span class="mr12 titleStyle">{{ job.name }}</span>
          <span
            class="badgeStyle"
            v-if="job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2"
          >
            暂缓
          </span>
          <span v-else class="badgeStyle">
            {{ job.stage.value }}
          </span>
        </div>
        <div class="subtitleStyle row items-center">
          <span>{{ job.id }}</span>
          <f-icon
            name="help_c_o"
            class="text-primary cursor-pointer q-ml-xs"
            width="16px"
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
      <fdev-space />
      <span class="btn-radius">
        <span v-if="taskType == 2">
          <Authorized
            v-if="job.stage.code > -1 && job.stage.code < 5"
            :include-me="addJobRole"
          >
            <div class="q-gutter-md">
              <div class="row no-wrap">
                <fdev-btn
                  class="mr-right"
                  dialog
                  ficon="success_c_o"
                  v-if="!onlyCreator && job.stage.code !== 4"
                  label="确认完成"
                  :disable="!isManager"
                  @click="confirmFinish"
                />
                <fdev-btn
                  class="mr-right"
                  dialog
                  ficon="edit"
                  label="修改"
                  :disable="
                    job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
                  "
                  v-if="job.stage.code !== 4"
                  @click="handUpdateModalOpened()"
                />
                <fdev-btn
                  dialog
                  ficon="keep_file"
                  class="mr-right"
                  label="归档"
                  v-if="job.stage.value === 'production'"
                  @click="handleHint()"
                />
                <fdev-btn
                  dialog
                  ficon="delete_o"
                  class="mr-right"
                  label="删除"
                  v-if="job.stage.code !== 4"
                  :disable="
                    job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
                  "
                  @click="handleDeleteJob()"
                />
              </div>
            </div>
          </Authorized>
          <Authorized :include-me="addJobRole" v-if="job.stage.code < 0">
            <template>
              <div class="btnFlex">
                <div>
                  <fdev-tooltip
                    v-if="
                      job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
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
                    class="mr-right"
                    label="删除"
                    :disable="
                      job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
                    "
                    @click="handleDeleteJob()"
                  />
                </div>
                <div>
                  <fdev-btn
                    dialog
                    ficon="exit"
                    class="mr-right"
                    label="返回"
                    @click="goBack"
                  />
                </div>
              </div>
            </template>
          </Authorized>
        </span>
        <span v-if="taskType != 2">
          <Authorized
            v-if="job.stage.code < 5 && job.stage.code > -1 && btnDisplay"
          >
            <div class="q-gutter-md">
              <div class="row no-wrap">
                <fdev-btn
                  dialog
                  ficon="success_c_o"
                  class="mr-right"
                  v-if="job.stage.value === 'rel' && taskType != 2"
                  label="确认已投产"
                  :disable="!isManager"
                  @click="confirmHadRelease"
                />
                <fdev-btn
                  dialog
                  ficon="cancel_c_o"
                  class="mr-right"
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
                  class="mr-right"
                  dialog
                  ficon="edit"
                  label="修改"
                  :disable="
                    job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
                  "
                  v-if="job.stage.code !== 4 && addJobRole"
                  @click="handUpdateModalOpened()"
                />
              </div>
            </div>
          </Authorized>
          <Authorized
            :include-me="role"
            v-if="job.stage.code < 5 && job.stage.code > -1 && !btnDisplay"
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
                        class="mr-right"
                        dialog
                        ficon="success_c_o"
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
                          class="mr-right"
                          :to="`/job/list/${job.id}/manage`"
                          :disable="
                            job.taskSpectialStatus == 1 ||
                              job.taskSpectialStatus == 2
                          "
                          v-if="
                            (!onlyCreator || isDesignManager) &&
                              job.stage.code !== 4 &&
                              taskType != 2
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
                          class="mr-right"
                          :disable="
                            job.taskSpectialStatus == 1 ||
                              job.taskSpectialStatus == 2
                          "
                          v-if="job.stage.code !== 4 && addJobRole"
                          @click="handUpdateModalOpened()"
                        />
                      </div>
                      <fdev-btn
                        dialog
                        ficon="keep_file"
                        class="mr-right"
                        label="归档"
                        v-if="job.stage.value === 'production'"
                        @click="handleHint()"
                      />
                      <fdev-btn
                        dialog
                        ficon="tools"
                        class="mr-right"
                        v-if="taskType != 1 && taskType != 2"
                        @click="handleModelSettingOpen"
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
                          class="mr-right"
                          :disable="
                            job.taskSpectialStatus == 1 ||
                              job.taskSpectialStatus == 2
                          "
                          v-if="job.stage.code !== 4"
                          @click="handleDeleteJob"
                        />
                      </div>
                      <fdev-btn
                        dialog
                        ficon="exit"
                        class="mr-right"
                        label="返回"
                        @click="goBack"
                      />
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
                          class="mr-right"
                          :to="`/job/list/${job.id}/manage`"
                          v-if="
                            (!onlyCreator || isDesignManager) &&
                              job.stage.code !== 4 &&
                              taskType != 2
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
                        ficon="tools"
                        class="mr-right"
                        v-if="taskType != 1 && taskType != 2"
                        @click="handleModelSettingOpen"
                        label="环境配置参数"
                      />
                      <fdev-btn
                        dialog
                        ficon="exit"
                        class="mr-right"
                        label="返回"
                        @click="goBack"
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
                      class="mr-right"
                      :to="`/job/list/${job.id}/manage`"
                      v-if="
                        isDesignManager && job.stage.code !== 4 && taskType != 2
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
                    ficon="tools"
                    v-if="taskType != 1 && taskType != 2"
                    class="mr-right"
                    @click="handleModelSettingOpen"
                    label="环境配置参数"
                  />
                  <fdev-btn
                    dialog
                    ficon="exit"
                    class="mr-right"
                    label="返回"
                    @click="goBack"
                  />
                </div>
              </div>
            </template>
          </Authorized>
          <Authorized :include-me="addJobRole" v-if="job.stage.code < 0">
            <template>
              <div class="btnFlex">
                <div>
                  <fdev-tooltip
                    v-if="
                      job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
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
                    class="mr-right"
                    label="删除"
                    :disable="
                      job.taskSpectialStatus == 1 || job.taskSpectialStatus == 2
                    "
                    @click="handleDeleteJob()"
                  />
                </div>
                <div>
                  <fdev-btn
                    dialog
                    ficon="exit"
                    class="mr-right"
                    label="返回"
                    @click="goBack"
                  />
                </div>
              </div>
            </template>
          </Authorized>
        </span>
      </span>
    </div>

    <!-- 日常任务的详情页面 -->
    <NormalTaskInfo v-if="taskType == 2" :job="job" />

    <!-- 开发任务的详情页面 -->
    <Loading :visible="loading" v-if="taskType != 2">
      <div class="icontitle">
        <f-icon
          name="basic_msg_s_f"
          class="text-primary mr12"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">任务基础信息</span>
      </div>
      <div class="row border-bottom full-width">
        <!-- 基础信息按钮图 -->
        <div class="row full-width border-top">
          <!-- 需求名称 -->
          <f-formitem
            class="col-4"
            label="需求名称"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <router-link
              :to="{ path: `/rqrmn/rqrProfile/${job.rqrmnt_no}` }"
              class="link"
              :title="job.demand && job.demand.oa_contact_name"
            >
              {{ job.demand && job.demand.oa_contact_name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ job.demand && job.demand.oa_contact_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </f-formitem>
          <!-- 需求编号 -->
          <f-formitem
            class="col-4"
            label="需求编号"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span
              v-if="job.demand"
              class="normal-link"
              @click="routeTo(job.rqrmnt_no)"
              :title="job.demand.oa_contact_no"
            >
              {{ job.demand && job.demand.oa_contact_no }}
            </span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ job.demand && job.demand.oa_contact_no }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>
          <!-- 研发单元 -->
          <f-formitem
            class="col-4"
            label="研发单元"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="job.unitNo">
              {{ job.unitNo }}
            </span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ job.unitNo }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <!-- 所属小组 -->
          <f-formitem
            class="col-4"
            label="所属小组"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="job.groupFullName">
              {{ job.groupFullName }}
            </span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ job.groupFullName }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>
          <!-- 所属应用 -->
          <f-formitem
            class="col-4"
            label="所属应用"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <router-link
              :to="`/app/list/${job.project_id}`"
              class="link"
              :title="job.project_name"
            >
              {{ job.project_name }}
            </router-link>
          </f-formitem>
          <!-- 分支 -->
          <f-formitem
            class="col-4"
            label="分支"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="job.feature_branch">{{ job.feature_branch }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="任务创建人"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <div v-if="job.creator">
              <router-link
                :to="`/user/list/${job.creator.id}`"
                class="link"
                :title="job.creator.user_name_cn"
              >
                {{ job.creator.user_name_cn }}
              </router-link>
            </div>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="任务负责人"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <router-link
              :to="`/user/list/${each.id}`"
              v-for="(each, index) in job.partyB"
              :key="index"
              class="link"
              :title="each.user_name_cn"
            >
              {{ each.user_name_cn }}
            </router-link>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="行内项目负责人"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <router-link
              :to="`/user/list/${each.id}`"
              v-for="(each, index) in job.partyA"
              :key="index"
              class="link"
              :title="each.user_name_cn"
            >
              {{ each.user_name_cn }}
            </router-link>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="开发人员"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <router-link
              :to="`/user/list/${each.id}`"
              v-for="(each, index) in job.developer"
              :key="index"
              class="link"
              :title="each.user_name_cn"
            >
              {{ each.user_name_cn }}
            </router-link>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="测试人员"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <router-link
              :to="`/user/list/${each.id}`"
              v-for="(each, index) in job.tester"
              :key="index"
              class="link"
              :title="each.user_name_cn"
            >
              {{ each.user_name_cn }}
            </router-link>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="UAT承接方"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            {{ job.uat_testObject }}
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="开发方向"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="job.direction | direction" v-if="job.direction">{{
              job.direction | direction
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="任务难度描述"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <fdev-rating
              v-model="job.difficulty"
              size="1.5em"
              :max="6"
              color="yellow-8"
              readonly
              icon="star_border"
              icon-selected="star"
            />
            <span
              v-if="job.direction"
              class="rules text-grey-7"
              :title="difficulty[job.difficulty - 1]"
            >
              {{ difficulty[job.difficulty - 1] }}
            </span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="任务难度修改原因"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span
              v-if="job.modify_reason"
              :title="descFilter(job.modify_reason)"
              v-html="descFilter(job.modify_reason)"
            />
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="用户故事id"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="job.storyId" v-if="job.storyId">{{
              job.storyId
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="分支扫描"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span
              class="normal-link"
              @click="handleScanDialogOpen"
              v-if="
                job.branch &&
                  taskManager.limit &&
                  job.project_name.includes('-cli-')
              "
            >
              路由/交易扫描
            </span>
            <span
              class="margin-l-r normal-link"
              @click="handleScanDialogOpen"
              v-else-if="job.branch && taskManager.limit"
            >
              接口扫描
            </span>
            <span
              class="normal-link"
              @click="handleSonarDialogOpen"
              v-if="
                isJavaTypeApp &&
                  job.branch &&
                  (isSonarManager || isKaDianManager)
              "
            >
              sonar扫描
            </span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="标签"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="job.tag">
              <fdev-chip
                v-for="(lab, index) in job.tag"
                :key="index"
                square
                :color="labelColor(lab)"
                text-color="white"
                class="q-mt-none"
                :title="lab"
              >
                {{ lab }}
              </fdev-chip>
            </span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="测试管理平台"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span v-if="includeMe(job.tester)">
              <a
                href="xxx/tui/"
                class="link"
                target="_blank"
                :title="点我"
              >
                点我
              </a>
            </span>
            <span v-else title="暂时只对测试人员展示">
              <span>暂时只对测试人员展示</span>
            </span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="UI设计还原审核状态"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span
              v-if="job.review_status"
              :title="job.review_status | filterReview"
            >
              <router-link
                :to="`/job/list/${job.id}/design`"
                class="link"
                v-if="job.review_status !== 'irrelevant'"
              >
                {{ job.review_status | filterReview }}
              </router-link>
              <span v-else>{{ job.review_status | filterReview }}</span>
            </span>
            <span v-else>-</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="上线确认书"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <fdev-toggle
              v-if="
                job.confirmShow && job.groupFullName.indexOf('互联网') != -1
              "
              v-model="job.confirmBtn"
              @input="confirmBookOpen"
              left-label
              :disable="Boolean(parseInt(job.confirmBtn)) || !isManager"
              true-value="1"
              false-value="0"
            />
          </f-formitem>
        </div>
      </div>

      <!-- 实施安排及情况 -->
      <!-- 图标位置 -->
      <div class="icontitle">
        <f-icon
          name="schedule_s_f"
          class="text-primary mr12"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">任务实施安排及情况</span>
      </div>
      <div class="row border-bottom full-width">
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="计划启动日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.plan_start_time">{{ job.plan_start_time }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="实际启动日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.start_time">{{ job.start_time }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="计划提交内测日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.plan_inner_test_time">{{
              job.plan_inner_test_time
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="实际提交内测日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.start_inner_test_time">{{
              job.start_inner_test_time
            }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="计划提交用户测试日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.plan_uat_test_start_time">{{
              job.plan_uat_test_start_time
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="实际提交用户测试日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.start_uat_test_time">{{
              job.start_uat_test_time
            }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="计划用户测试完成日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.plan_rel_test_time">{{
              job.plan_rel_test_time
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="实际用户测试完成日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.start_rel_test_time">{{
              job.start_rel_test_time
            }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="计划投产日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.plan_fire_time">{{ job.plan_fire_time }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="实际投产日期"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.fire_time">{{ job.fire_time }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="投产意向窗口"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.proWantWindow" v-if="job.proWantWindow">{{
              job.proWantWindow
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="投产窗口"
            profile
            label-auto
            bottom-page
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <div :title="release_node_name">
              {{ release_node_name }}
            </div>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <f-formitem
            class="col-4"
            label="UAT提测日期"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span :title="job.uat_test_time" v-if="job.uat_test_time">{{
              job.uat_test_time
            }}</span>
          </f-formitem>
          <f-formitem
            class="col-4"
            label="已合并到release分支"
            bottom-page
            profile
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="q-px-lg"
          >
            <span v-if="job.uat_merge_time">是</span>
            <span v-else>否</span>
          </f-formitem>
        </div>
      </div>
    </Loading>

    <!-- 下方tabs -->
    <div class="mr-t">
      <fdev-tabs
        align="left"
        v-model="tab"
        class="text-grey-7"
        active-color="primary"
        indicator-color="primary"
      >
        <fdev-tab
          name="UATtestResult"
          v-if="taskType != 2"
          label="uat测试结果"
        />
        <fdev-tab name="testResult" v-if="taskType != 2" label="sit测试结果" />
        <fdev-tab name="assessment" v-if="taskType != 2" label="关联项评估" />
        <fdev-tab name="translation" label="关联文档" />
        <!-- <fdev-tab name="file" label="归档" v-if="job.stage.value === 'file'" /> -->
        <fdev-tab name="productionProblems" label="生产问题" />
        <fdev-tab
          name="codeAnalizy"
          label="代码分析"
          v-if="isJavaTypeApp && job.branch && taskType != 2"
        />
        <fdev-tab name="testMsgs" v-if="taskType != 2" label="提测信息" />
        <!-- <fdev-tab name="property" label="模版配置" /> -->
      </fdev-tabs>
      <div>
        <fdev-tab-panels v-model="tab">
          <fdev-tab-panel
            name="UATtestResult"
            v-if="taskType != 2"
            class="q-pa-none"
          >
            <Loading :visible="testResultLoading">
              <p class="p-margin">当前任务缺陷情况：</p>
              <UATCount :fromApp="false" />
              <UATDefect
                :jobName="job.name"
                :defectList="UATdefectList"
                :isLoginUserList="isLoginUserList"
                @updateStatus="updateUATStatus"
              />
            </Loading>
          </fdev-tab-panel>

          <fdev-tab-panel
            name="testResult"
            v-if="taskType != 2"
            class="q-pa-none"
          >
            <Loading :visible="testResultLoading">
              <p class="p-margin">当前任务缺陷情况：</p>
              <Count
                :defectList="isTest === '1' ? defectList : []"
                :fromApp="false"
              />
              <Defect
                @init="queryList"
                :jobName="job.name"
                :defectList="defectList"
                :isLoginUserList="isLoginUserList"
                @updateStatus="updateStatus"
                @assignUser="assignUser($event)"
                :needSelect="false"
              />
              <p class="p-margin">当前任务SIT测试执行情况：</p>
              <div class="row">
                <div class="col relative-position q-pa-md padding-div">
                  <div class=" row form items-start line-height">
                    <div
                      v-for="(prop, index) in moduleData"
                      class="col-6"
                      :key="index"
                    >
                      <f-formitem label="测试计划名称">
                        {{ prop.testplanName }}
                      </f-formitem>
                      <f-formitem label="测试案例总数">
                        {{ prop.allCount }}
                      </f-formitem>
                      <f-formitem label="执行通过数">
                        {{ prop.allPassed }}
                      </f-formitem>
                      <f-formitem label="执行计划失败数">
                        {{ prop.allFailed }}
                      </f-formitem>
                      <f-formitem label="执行案例阻碍数">
                        {{ prop.allBlocked }}
                      </f-formitem>
                    </div>
                  </div>
                </div>
              </div>
            </Loading>
          </fdev-tab-panel>

          <fdev-tab-panel name="assessment" class="q-pa-none">
            <Loading :visible="assessmentLoading">
              <div class="row">
                <div class="col relative-position q-pa-md">
                  <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
                    <f-formitem :col="1" label="数据库变更" label-width="200px">
                      <f-icon
                        v-if="job.review.data_base_alter.length === 0"
                        name="close"
                      />
                      <div class="q-gutter-x-sm" v-else>
                        <span
                          v-for="(each, index) in job.review.data_base_alter"
                          :key="index"
                        >
                          {{ each.name }}
                        </span>
                      </div>
                    </f-formitem>
                    <f-formitem
                      :col="1"
                      label="公共配置文件更新"
                      label-width="200px"
                    >
                      {{ job.review.commonProfile ? '涉及' : '不涉及' }}
                    </f-formitem>
                    <f-formitem
                      :col="1"
                      label="其他系统变更"
                      label-width="200px"
                    >
                      <f-icon
                        v-if="
                          job.review.other_system == null ||
                            job.review.other_system.length === 0
                        "
                        name="close"
                      />
                      <div class="q-gutter-x-sm" v-else>
                        <span
                          v-for="(each, index) in job.review.other_system"
                          :key="index"
                        >
                          {{ each.name }}
                        </span>
                      </div>
                    </f-formitem>
                    <f-formitem :col="1" label="安全测试" label-width="200px">
                      <f-icon v-if="!job.review.securityTest" name="close" />
                      <span v-else>{{ job.review.securityTest }}</span>
                    </f-formitem>
                    <f-formitem
                      :col="1"
                      label="是否涉及特殊情况"
                      label-width="200px"
                    >
                      <f-icon
                        v-if="job.review.specialCase.length === 0"
                        name="close"
                      />
                      <div class="q-gutter-x-sm" v-else>
                        <span
                          v-for="(each, index) in job.review.specialCase"
                          :key="index"
                        >
                          {{ each }}
                        </span>
                      </div>
                    </f-formitem>
                  </div>
                </div>
              </div>
            </Loading>
          </fdev-tab-panel>

          <fdev-tab-panel name="translation" class="q-pa-none">
            <Loading
              class="row"
              :visible="globalLoading['jobForm/queryDocDetail']"
            >
              <div class="col-5 q-py-md row tree">
                <div class="col q-mx-md">
                  <fdev-input ref="filter" type="text" v-model="filter">
                    <template v-slot:prepend>
                      <f-icon name="search" color="primary cursor-pointer" />
                    </template>
                  </fdev-input>

                  <fdev-tree
                    ref="tree"
                    :nodes="nodes"
                    node-key="label"
                    :filter="filter"
                  >
                    <template v-slot:header-level-1="prop">
                      <div class="text-weight-bold">
                        {{ prop.node.label }}
                      </div>
                    </template>
                    <template v-slot:header-level-2="prop">
                      <a class="normal-link" @click="download(prop.node.path)">
                        <div>{{ prop.node.label }}</div>
                        <div class="text-subtitle2 text-grey-7">
                          {{ prop.node.date }}
                        </div>
                      </a>
                      <fdev-space />
                      <fdev-btn
                        class="q-btn--dialog-width"
                        icon="delete"
                        @click="deleteUpload(prop.node)"
                        flat
                        :loading="globalLoading['jobForm/deleteFile']"
                        color="red"
                        v-if="examDelete(prop.node)"
                      />
                    </template>
                  </fdev-tree>
                </div>
              </div>

              <fdev-separator vertical inset v-if="isSonarManager" />

              <Uploader
                multiple
                class="col q-mx-md q-pt-sm"
                icon-size="64px"
                @success="uploadSuccess"
                @beforeUpload="beforeUpload"
                draggable
                :method="updateTaskDoc"
                v-if="isSonarManager"
                max-total-size="31457280"
                :uploadedFiles="uploadedFiles"
                :fileType="filesType"
              >
                <template v-slot:header="props">
                  <div class="row items-center">
                    <f-formitem
                      label="类型"
                      :label-col="3"
                      class="q-ma-sm col-5"
                    >
                      <el-cascader
                        :props="{
                          checkStrictly: true,
                          emitPath: false
                        }"
                        class="full-width"
                        v-model="filesType"
                        :options="fileTypeOptions"
                      />
                    </f-formitem>
                    <fdev-space />

                    <div class="text-grey-7">
                      已选文件大小：{{ props.totalSize }} /
                      {{ props.limitSize }}
                    </div>
                  </div>
                </template>
              </Uploader>
            </Loading>
          </fdev-tab-panel>

          <!-- <fdev-tab-panel name="file">
            <div class="data-none row flex-center">
              <f-icon name="alert_c_f" />暂无数据
            </div>
          </fdev-tab-panel> -->

          <fdev-tab-panel name="productionProblems">
            <ProductionProblems is-task :unitNo="job.unitNo" />
            <div class="row justify-center">
              <fdev-btn
                label="新建生产问题"
                :to="`/job/list/addProductionProblem/${$route.params.id}`"
              />
            </div>
          </fdev-tab-panel>
          <fdev-tab-panel
            name="codeAnalizy"
            :class="{ 'codeAnalizy-style': sonarScan }"
          >
            <Sonar :appId="job.project_id" :branch="job.branch" />
          </fdev-tab-panel>
          <fdev-tab-panel name="testMsgs">
            <TestMsgs />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </div>
    </div>
    <!-- 模板配置 -->
    <fdev-dialog
      v-model="modelSettingOpened"
      persistent
      :maximized="maximizedToggle"
    >
      <fdev-layout view="Lhh lpR fff" container class="bg-white">
        <fdev-header class="q-pt-sm q-pb-sm bg-cyan-6">
          <fdev-bar class="bg-cyan-6 text-white">
            <fdev-btn-group>
              <fdev-btn
                class="q-btn--dialog-width"
                v-if="hasPermissions"
                icon="build"
                color="cyan-6"
                :label="`${toggleTool ? '隐藏' : '打开'}模板工具`"
                size="md"
                @click="toggleTool = !toggleTool"
              />
              <fdev-btn
                class="q-btn--dialog-width"
                v-if="hasPermissions"
                icon="save"
                color="cyan-6"
                label="保存配置模板"
                size="md"
                @click="handleSubmitModel"
              />
              <fdev-btn
                class="q-btn--dialog-width"
                icon="priority_high"
                color="cyan-6"
                label="优先生效配置参数"
                size="md"
                @click="handleExtreConfigDialogOpen"
              >
                <div>
                  <f-icon name="help" style="font-size: 12px"> </f-icon>
                  <fdev-tooltip
                    anchor="top middle"
                    self="center middle"
                    :offest="[0, 0]"
                  >
                    只是临时测试使用，长期使用需要配置实体参数。
                  </fdev-tooltip>
                </div>
              </fdev-btn>
              <fdev-btn
                class="q-btn--dialog-width"
                icon="autorenew"
                color="cyan-6"
                label="预览各环境配置文件"
                size="md"
                @click="getPreviewFile"
              />
              <!-- 保存到配置中心按钮 -->
              <div>
                <fdev-btn
                  class="q-btn--dialog-width"
                  icon="save"
                  color="cyan-6"
                  size="md"
                  @click="saveConfig"
                  :disable="!hasEditPermissions || !isDefaultDevEnv"
                  label="上传到开发配置中心"
                >
                </fdev-btn>
                <fdev-tooltip
                  anchor="bottom middle"
                  self="top middle"
                  :offset="[10, 10]"
                  v-if="hasEditPermissions && isDefaultDevEnv"
                >
                  开发环境实体映射值与SIT环境相等，开发阶段可使用常量值调试
                </fdev-tooltip>
                <fdev-tooltip
                  anchor="bottom middle"
                  self="top middle"
                  :offset="[10, 10]"
                  v-else
                >
                  该功能只在预览选择开发环境后可用，开发环境实体映射值与SIT环境相等，开发阶段可使用常量值调试
                </fdev-tooltip>
              </div>
            </fdev-btn-group>
            <fdev-space />
            <fdev-btn
              class="q-btn--dialog-width"
              flat
              icon="close"
              @click="closeDialog"
            />
          </fdev-bar>
        </fdev-header>
        <div class="property-editor">
          <div class="row editor-title">
            <div class="col-3" v-if="toggleTool">
              <span class="q-pl-lg">模板工具</span>
            </div>
            <div class="col">
              编辑外部配置模板
              <span>
                <fdev-tooltip
                  anchor="top middle"
                  self="center middle"
                  :offest="[0, 0]"
                >
                  <span>配置项左侧为应用key,右侧为fdev实体key</span>
                </fdev-tooltip>
                <f-icon name="help" />
              </span>
            </div>
            <div class="col row">
              <div class="col-4">预览各环境配置文件</div>
              <div class="row input form-item">
                <span class="form-label">环境</span>
                <fdev-select
                  use-input
                  placeholder="环境"
                  ref="envModel.env"
                  class="input select-width inline-block"
                  v-model="envModel.env"
                  @filter="envFilter"
                  :options="filterEnvList"
                  option-label="name_en"
                  option-value="name_en"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.name_en">
                          {{ scope.opt.name_en }}
                        </fdev-item-label>
                        <fdev-item-label caption :title="scope.opt.name_cn">
                          {{ scope.opt.name_cn }}
                        </fdev-item-label>
                      </fdev-item-section>
                      <fdev-item-section
                        side
                        v-if="
                          scope.opt.labels.includes('biz') ||
                            scope.opt.labels.includes('dmz')
                        "
                      >
                        <fdev-chip
                          flat
                          square
                          class="text-white"
                          :color="
                            scope.opt.labels.includes('biz')
                              ? 'green-5'
                              : 'orange-5'
                          "
                        >
                          {{
                            scope.opt.labels.includes('biz') ? '业务' : '网银'
                          }}
                        </fdev-chip>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </div>
            </div>
          </div>
          <div class="row editor-warpper">
            <div class="col-3" v-if="toggleTool">
              <form class="form">
                <div class="job-top-select">
                  <fdev-select
                    placeholder="环境实体名称"
                    use-input
                    clearable
                    ref="envModel.term"
                    v-model="envModel.term"
                    @filter="filterFn"
                    :options="nameEnList"
                    :option-value="opt => opt"
                    option-label="name_en"
                  >
                    <template v-slot:option="scope">
                      <fdev-item
                        v-bind="scope.itemProps"
                        v-on="scope.itemEvents"
                      >
                        <fdev-item-section>
                          <fdev-item-label :title="scope.opt.name_en">
                            {{ scope.opt.name_en }}
                          </fdev-item-label>
                          <fdev-item-label :title="scope.opt.name_cn" caption>
                            {{ scope.opt.name_cn }}
                          </fdev-item-label>
                        </fdev-item-section>
                      </fdev-item>
                    </template>
                  </fdev-select>
                </div>
                <!-- 导入原始配置文件 -->
                <div class="row justify-center q-pt-md">
                  <fdev-btn-group>
                    <fdev-btn
                      style="height:auto"
                      class="q-btn--dialog-width"
                      icon="call_missed_outgoing"
                      color="primary"
                      label="选择该实体配置模板"
                      size="md"
                      v-if="hasPermissions"
                      @click="appendModelHtml"
                    />
                    <fdev-btn
                      style="height:auto"
                      class="q-btn--dialog-width"
                      icon="cloud_upload"
                      color="primary"
                      label="导入外部配置文档"
                      size="md"
                      v-if="hasPermissions"
                      @click="showUploader = !showUploader"
                    />
                  </fdev-btn-group>
                  <fdev-uploader
                    label="限properties文件"
                    field-name="file"
                    :multiple="false"
                    ref="upload"
                    v-show="showUploader"
                    @added="startRead"
                    accept=".properties"
                    class="uploader"
                    hide-upload-btn
                  />
                </div>
                <!-- 实体 -->
                <div class="row">
                  <pre class="scroll-x">
                    <code class="text-left block" v-html="modelHtml"/>
                  </pre>
                </div>
              </form>
            </div>
            <div class="col" :style="{ width: toggleTool ? '74.5%' : '100%' }">
              <PropertyEditor
                v-model="editor"
                ref="propertyEdit"
                :properties="modelProperties"
                :disable="permission"
                @noPermission="changeStatus"
                @error="handleError"
              />
            </div>
          </div>
        </div>
      </fdev-layout>
    </fdev-dialog>
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
          @click="handleDelete"
        />
      </template>
    </f-dialog>

    <f-dialog
      v-model="updateModalOpened"
      transition-show="slide-up"
      transition-hide="slide-down"
      persistent
      right
      title="修改任务"
      @shake="confirmToClose"
    >
      <div class="rdia-dc-w row justify-between">
        <f-formitem label="任务名称">
          <fdev-input v-model="jobModel.name" disable type="text" hint=""
        /></f-formitem>
        <f-formitem
          label="所属应用"
          v-if="taskType != 2 || jobModel.project_name"
        >
          <fdev-input
            v-model="jobModel.project_name"
            disable
            type="text"
            hint=""
        /></f-formitem>
        <f-formitem label="所属小组">
          <fdev-select
            ref="jobModel.group"
            v-model="jobModel.group"
            :options="groupList"
            option-value="id"
            option-label="name"
            @filter="groupFilter"
            hint=""
            use-input
            :rules="[() => $v.jobModel.group.required || '请输入所属小组']"
          />
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="分支">
          <fdev-input
            v-model="jobModel.feature_branch"
            disable
            type="text"
            hint=""
        /></f-formitem>
        <f-formitem label="研发单元">
          <fdev-select
            use-input
            ref="jobModel.fdev_implement_unit_no"
            v-model="jobModel.fdev_implement_unit_no"
            :options="filterRqrmnt"
            option-label="fdev_implement_unit_no"
            option-value="fdev_implement_unit_no"
            @filter="rqrmntInputFilter"
            :disable="taskType == 2 || canNotModify"
            map-options
            emit-value
            :rules="[
              () =>
                $v.jobModel.fdev_implement_unit_no.required || '请选择研发单元'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item
                v-bind="scope.itemProps"
                v-on="scope.itemEvents"
                :disabled="scope.opt.disable"
              >
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.fdev_implement_unit_no">
                    {{ scope.opt.fdev_implement_unit_no }}
                  </fdev-item-label>
                  <fdev-item-label
                    caption
                    :title="scope.opt.implement_unit_content"
                  >
                    {{ scope.opt.implement_unit_content }}
                  </fdev-item-label>
                </fdev-item-section>
                <template v-if="scope.opt.disable">
                  <fdev-tooltip
                    anchor="top middle"
                    self="bottom middle"
                    :offest="[10, 0]"
                  >
                    {{ scope.opt.tooltip }}
                  </fdev-tooltip>
                </template>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!-- 显示字段修改 -->
        <f-formitem v-if="taskType == 2" label="计划启动日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_start_time"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="实际启动日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.start_time"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际提交内测日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.start_inner_test_time"
            disable
            hint=""
          />
          <fdev-tooltip anchor="top middle">
            提交内测后生成，用户不可编辑
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际提交用户测试日期">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            :disable="jobModel.stage.code < 1"
            v-model="jobModel.start_uat_test_time"
            :options="uatStartDateOptions"
            :rules="[
              val =>
                !jobProfile.start_uat_test_time ||
                !!val ||
                '实际提交用户测试日期不能为空'
            ]"
          >
          </f-date>
          <fdev-tooltip anchor="top middle" v-if="jobModel.stage.code < 1">
            任务尚未提交内测不可修改,请先提交内测
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际用户测试完成日期">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            v-model="jobModel.start_rel_test_time"
            :disable="jobModel.stage.code < 1"
            :options="relStartDateOptions"
            hint=""
          >
          </f-date>
          <fdev-tooltip anchor="top middle" v-if="jobModel.stage.code < 1">
            任务尚未提交内测不可修改,请先提交内测
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际投产日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.fire_time"
            disable
            hint=""
          />
          <fdev-tooltip anchor="top middle">
            确认投产后生成，用户不可编辑
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType == 2" label="计划完成日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_fire_time"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="实际完成日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.fire_time"
            disable
            hint=""
          />
          <fdev-tooltip anchor="top middle">
            确认完成后生成，用户不可编辑
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="投产意向窗口" required>
          <f-date
            mask="YYYY/MM/DD"
            ref="jobModel.proWantWindow"
            v-model="jobModel.proWantWindow"
            :rules="[
              () => $v.jobModel.proWantWindow.required || '投产意向窗口不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem label="任务负责人" required>
          <fdev-select
            use-input
            multiple
            ref="jobModel.partyB"
            v-model="jobModel.partyB"
            :options="
              userOptionsFilter(
                ['厂商项目负责人', '行内项目负责人'],
                job.partyB
              )
            "
            @filter="userFilter"
            :rules="[() => $v.jobModel.partyB.required || '任务负责人不能为空']"
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

        <f-formitem label="行内项目负责人" required>
          <fdev-select
            use-input
            multiple
            ref="jobModel.partyA"
            v-model="jobModel.partyA"
            :options="userOptionsFilter(['行内项目负责人'], job.partyA)"
            @filter="userFilter"
            :rules="[
              () => $v.jobModel.partyA.required || '行内项目负责人不能为空'
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

        <f-formitem label="开发人员">
          <fdev-select
            use-input
            multiple
            hint=""
            ref="jobModel.developer"
            v-model="jobModel.developer"
            :options="userOptionsFilter(['开发人员'], job.developer)"
            @filter="userFilter"
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

        <f-formitem
          label="开发方向"
          :optional="!job.direction"
          v-if="isSpdbManager && taskType != 2"
        >
          <fdev-tooltip v-if="job.stage.code > 2" anchor="top middle">
            任务进入rel阶段，不能修改
          </fdev-tooltip>
          <fdev-select
            hint=""
            ref="jobModel.direction"
            v-model="jobModel.direction"
            :options="directionOptions"
            :clearable="!job.direction"
            option-value="value"
            option-label="label"
            map-options
            :disable="job.stage.code > 2"
            emit-value
          />
        </f-formitem>

        <f-formitem
          label="任务难度描述"
          v-if="jobModel.direction && isSpdbManager && taskType != 2"
        >
          <fdev-tooltip v-if="job.stage.code > 2" anchor="top middle">
            任务进入rel阶段，不能修改
          </fdev-tooltip>

          <fdev-select
            :disable="job.stage.code > 2"
            ref="jobModel.difficulty"
            v-model="$v.jobModel.difficulty.$model"
            :options="difficultyOptions"
            option-value="value"
            option-label="label"
            map-options
            emit-value
            :rules="[
              () => $v.jobModel.difficulty.required || '请选择任务难度描述'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section :title="scope.opt.label">
                  {{ scope.opt.label }}
                </fdev-item-section>
                <fdev-item-section side>
                  <fdev-rating
                    :value="scope.opt.value"
                    size="1em"
                    :max="scope.opt.value"
                    color="yellow-8"
                    no-reset
                    icon="star_border"
                    readonly
                    icon-selected="star"
                  />
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem label="任务难度" v-if="jobModel.direction && taskType != 2">
          <fdev-rating
            :value="$v.jobModel.difficulty.$model"
            size="2em"
            :max="6"
            color="yellow-8"
            no-reset
            icon="star_border"
            disable
            hint=""
            icon-selected="star"
          />
        </f-formitem>

        <f-formitem
          label="修改原因"
          v-show="difficultyIsChange"
          v-if="job.direction && taskType != 2"
        >
          <fdev-input
            ref="jobModel.modify_reason"
            v-model="$v.jobModel.modify_reason.$model"
            type="textarea"
            :rules="[
              () =>
                $v.jobModel.modify_reason.required ||
                '请输入任务难度描述的修改原因'
            ]"
          />
        </f-formitem>

        <f-formitem label="任务描述">
          <fdev-input
            ref="jobModel.desc"
            v-model="jobModel.desc"
            type="textarea"
            hint=""
          />
        </f-formitem>

        <f-formitem
          label="是否涉及数据库变更"
          v-if="jobModel.review.db[0] && taskType != 2"
        >
          <div class="text-left padding-form">
            <fdev-radio
              val="否"
              v-model="jobModel.review.db[0].name"
              label="否"
              class="q-pr-lg"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
            <fdev-radio
              val="是"
              v-model="jobModel.review.db[0].name"
              label="是"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
          </div>
        </f-formitem>
        <!-- A -->
        <f-formitem
          label="相关库表变更是否涉及通知数据仓库等关联供数系统配套改造"
          v-if="isShowFirst && taskType != 2"
        >
          <div class="padding-form">
            <fdev-radio
              val="否"
              v-model="jobModel.system_remould"
              label="否"
              class="q-pr-lg"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
            <fdev-radio
              val="是"
              v-model="jobModel.system_remould"
              label="是"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
          </div>
        </f-formitem>
        <!-- C -->
        <f-formitem
          label="是否涉及在库表关联应用暂停服务期间实施数据库变更操作"
          v-if="isShowFirst && taskType != 2"
        >
          <div class="padding-form">
            <fdev-radio
              val="否"
              v-model="jobModel.impl_data"
              label="否"
              class="q-pr-lg"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
            <fdev-radio
              val="是"
              v-model="jobModel.impl_data"
              label="是"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
          </div>
        </f-formitem>

        <f-formitem label="公共配置文件更新" v-if="taskType != 2">
          <div class="padding-form">
            <fdev-radio
              :val="false"
              v-model="jobModel.review.commonProfile"
              label="否"
              class="q-pr-lg"
            />
            <fdev-radio
              :val="true"
              v-model="jobModel.review.commonProfile"
              label="是"
            />
          </div>
        </f-formitem>
        <f-formitem label="其他系统变更" v-if="taskType != 2">
          <fdev-select
            use-input
            multiple
            hint=""
            hide-dropdown-icon
            v-model="jobModel.review.other_system"
            @new-value="addReviewTerm"
            ref="jobModel.review.other_system"
          />
        </f-formitem>
        <f-formitem label="是否涉及特殊情况" v-if="taskType != 2">
          <fdev-select
            multiple
            option-label="label"
            option-value="value"
            map-options
            emit-value
            hint=""
            ref="jobModel.review.specialCase"
            v-model="jobModel.review.specialCase"
            :options="specialCaseOptions"
          />
        </f-formitem>
        <f-formitem label="是否涉及安全测试" v-if="taskType != 2">
          <fdev-select
            :options="securityTestOptions"
            ref="jobModel.review.securityTest"
            v-model="$v.jobModel.review.securityTest.$model"
            :rules="[
              () =>
                $v.jobModel.review.securityTest.required ||
                '请选择是否涉及安全测试'
            ]"
          >
          </fdev-select>
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          @click="modifyTask"
          :loading="globalLoading['jobForm/updateJob']"
          label="修改任务"
        />
      </template>
    </f-dialog>

    <!-- 投产意向窗口日期卡点提示操作 -->
    <f-dialog
      v-model="dataAlert"
      transition-show="slide-up"
      transition-hide="slide-down"
      class="dialog-style dataAlertDialog"
      title="提示"
    >
      <div>
        该任务投产意向日期为{{
          taskRqrmntAlert
        }}，暂未确认上线确认书已到达，请确认以下两项事宜:
      </div>
      <div>
        1、上线确认书已到达、请联系<router-link
          :to="`/user/list/${each.id}`"
          v-for="(each, index) in job.partyB"
          :key="index"
          class="link"
        >
          {{ each.user_name_cn }} </router-link
        >尽快打开上线确认书开关，否则将会影响投产统计和最终的投产；
      </div>
      <div>
        2、上线确认书未到达，请确认是否于{{
          taskRqrmntAlert
        }}投产，如否，请及时联系<router-link
          :to="`/user/list/${each.id}`"
          v-for="(each, index) in job.partyB"
          :key="index"
          class="link"
        >
          {{ each.user_name_cn }} </router-link
        >修改投产意向窗口日期"。
      </div>
      <div></div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          color="primary"
          label="关闭"
          @click="closeDataDialog"
        />
      </template>
    </f-dialog>
    <!-- 继续任务 -->
    <Authorized
      :role-authority="['厂商项目负责人', '行内项目负责人']"
      v-if="job.creator"
      :include-me="addJobRole"
    >
      <f-dialog
        v-model="jobStageCode"
        transition-show="slide-up"
        transition-hide="slide-down"
        title="继续任务"
      >
        <fdev-card-section v-if="step2End && !step4End">
          任务基本信息录入成功，是否继续创建分支？
        </fdev-card-section>
        <fdev-card-section v-if="step4End">
          创建分支成功，是否继续创建任务？
        </fdev-card-section>
        <div align="right">
          <fdev-btn
            class="q-btn--dialog-width"
            flat
            label="就这样吧"
            @click="jobStageCode = false"
            color="primary"
          />
          <fdev-btn
            class="q-btn--dialog-width"
            v-if="taskType != 1"
            flat
            label="其他任务"
            @click="handleStepNext(6, 1)"
            color="primary"
          />
          <fdev-btn
            flat
            label="创建分支"
            @click="handleStepNext(3, 3)"
            v-if="!step4End"
            color="primary"
          />
          <!-- todo -->
          <!-- <fdev-btn
            flat
            label="创建任务"
            @click="handleStepNext(4, 4)"
            v-if="step4End"
            v-close-popup
            color="primary"
          /> -->
        </div>
      </f-dialog>
    </Authorized>
    <!-- 环境配置弹框 -->
    <ExtreConfigParam
      :extreConfigDialogOpened.sync="extreConfigDialogOpened"
      :extraConfig="extraConfigParam[0]"
      @listenExtreConfigEvent="changeExtreConfigEvent"
      :noWrite="noWrite"
      :project_id="job.project_id"
      :branch="job.feature_branch"
    />
    <!-- 扫描 -->
    <ScanDialog
      v-model="scanDialogOpen"
      :appName="job.project_name"
      :branchName="job.branch"
      v-if="taskManager.limit"
      :id="job.id"
    />
    <!--
      批量任务
     -->
    <PlanDateUpdateDialog
      v-model="planDateUpdateDialogOpen"
      :job="job"
      @closePlanDateUpdateDialog="closePlanDateUpdateDialog"
    />
    <!-- 废弃任务 -->
    <AbandonedTaskDialog
      v-model="abandonedTaskDialogOpened"
      :id="id"
      :taskName="job.name"
      @complete="init"
    />
    <!--Sonar扫描  -->
    <SonarDialog
      v-model="sonarDialogOpen"
      :appName="job.project_name"
      :appId="job.project_id"
      :branchName="job.branch"
    />
    <!-- 落后master提交分支dialog -->
    <BehindMasterDialog
      :value.sync="handleShow"
      @changeHandleShow="changeDialogShow"
      :commitTabledata="commitTabledata"
    />
    <!-- 保存失败 -->
    <f-dialog v-model="saveResultDialogOpened" title="保存失败">
      <div v-for="(arr, title) in result" :key="title">
        <fdev-chip
          icon="warning"
          square
          color="red"
          text-color="white"
          :label="title"
        />
        <p class="result-wrapper" v-for="res in arr" :key="res">
          <f-icon name="alert_t_f" style="color:red" />{{ res }}
        </p>
      </div>
    </f-dialog>
    <!-- 上线确认书 -->
    <f-dialog
      @before-close="closeBookTime"
      v-model="confirmBookTimeOpen"
      title="上线确认书"
    >
      <fdev-form @submit.prevent="updateConfirmBook">
        <div class="q-pa-lg bg-white">
          <p>1.开关打开后请尽快上传上线确认书到关联文档>投产类>变更材料类！</p>
          <p>
            2.开关打开后将
            <span class="text-red">不允许修改</span
            >，请确认业务是否已发出上线确认书至机构邮箱！
          </p>
          <f-formitem
            label="3.上线确认书到达时间"
            :label-col="4"
            align="left"
            class="q-mb-md Field-select"
            v-if="job.confirmDateShow"
          >
            <f-date
              ref="bookModel.confirmFileDate"
              mask="YYYY-MM-DD"
              v-model="$v.bookModel.confirmFileDate.$model"
              :rules="[
                () =>
                  $v.bookModel.confirmFileDate.required ||
                  '请选择上线确认书到达时间'
              ]"
            />
          </f-formitem>
        </div>
        <div class="confirm-btn row">
          <fdev-btn
            outline
            dialog
            label="取消"
            @click="
              confirmBookTimeOpen = false;
              closeBookTime();
            "
            style="margin-right:16px"
          />
          <fdev-btn
            type="submit"
            dialog
            label="确定"
            :loading="globalLoading['jobForm/confirmBtn']"
            @click="handleAllTip"
          />
        </div>
      </fdev-form>
    </f-dialog>
    <!-- 创建分支弹窗 -->
    <AddBranch v-model="openBranch" :id="job.id" @finished="init"></AddBranch>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import Sonar from '@/modules/App/components/Sonar/Sonar';
import { required } from 'vuelidate/lib/validators';
import ExtreConfigParam from '@/components/ExtraConfigParam';
import PropertyEditor from '@/components/PropertyEditor';
import ScanDialog from '@/modules/interface/components/scanDialog';
import SonarDialog from './components/SonarDialog';
import Defect from '@/components/Defect';
import UATDefect from '@/components/Defect/UATDefect';
import Count from '@/components/Count';
import UATCount from '@/components/UATCount';
import Uploader from '@/components/Uploader';
import LocalStorage from '#/plugins/LocalStorage';
import ProductionProblems from '@/modules/HomePage/views/myProdProblem';
import TestMsgs from './TestMsgs';

import moment from 'moment';
import {
  perform,
  createJobModel,
  createEnvModel,
  rqrmntState,
  difficultyOptions,
  directionOptions,
  securityTestOptions,
  specialCaseOptions,
  fileTypeOptions
} from '../../utils/constants';
import { formatJob, formatMyJob } from '../../utils/utils';
import {
  formatOption,
  getGroupFullName,
  baseUrl,
  successNotify,
  getIdsFormList,
  validate,
  deepClone,
  errorNotify,
  exportExcel
} from '@/utils/utils';
import { formatUser } from '@/modules/User/utils/model';
import { mapState, mapActions, mapGetters } from 'vuex';
import AbandonedTaskDialog from '../../components/AbandonedTaskDialog';
import PlanDateUpdateDialog from '../../components/PlanDateUpdateDialog';
import BehindMasterDialog from '../../components/BehindMasterDialog';
import { tipContent } from '../../utils/constants';
import { reviewMap } from '@/modules/Rqr/model';
import NormalTaskInfo from './components/NormalTaskInfo';
import AddBranch from './components/AddBranch';
export default {
  name: 'Profile',
  components: {
    Loading,
    Authorized,
    ExtreConfigParam,
    PropertyEditor,
    ScanDialog,
    SonarDialog,
    Defect,
    UATDefect,
    Count,
    UATCount,
    AbandonedTaskDialog,
    ProductionProblems,
    PlanDateUpdateDialog,
    BehindMasterDialog,
    Sonar,
    TestMsgs,
    Uploader,
    NormalTaskInfo,
    AddBranch
  },
  data() {
    return {
      groupList: this.groups,
      ...perform,
      ...rqrmntState,
      taskType: '',
      openBranch: false,
      isTest: '',
      loading: false,
      ipmpUnitCloneList: [],
      dataAlert: false,
      assessmentLoading: false,
      testResultLoading: false,
      UATtestResultLoading: false,
      planDateUpdateDialogOpen: false,
      tab: 'UATtestResult',
      job: createJobModel(),
      securityTestOptions: securityTestOptions,
      specialCaseOptions: specialCaseOptions,
      deleteModalOpened: false,
      filter: '',
      nodes: [{ label: '关联文档', children: [] }],
      moduleData: [],
      UATmoduleData: [],
      updateModalOpened: false,
      jobModel: createJobModel(),
      users: [],
      baseUrl: baseUrl,
      step2End: false,
      step4End: false,
      commitCol: [
        {
          name: 'fileName',
          label: '冲突文件',
          field: 'fileName',
          align: 'left'
        }
      ],
      querydeleteJob: [],
      extreConfigDialogOpened: false,
      noWrite: true,
      jobStageCode: false,
      role: [],
      editor: '',
      canNotModify: false,
      envModel: createEnvModel(),
      typeList: [],
      firstCategories: [],
      secondCategories: [],
      nameEnList: [],
      cloneSecCategories: [],
      modelHtml: '',
      modelSettingOpened: false,
      maximizedToggle: true,
      modelProperties: [],
      toggleTool: true,
      showUploader: false,
      hasPermissions: false,
      hasEditPermissions: false,
      managerIds: [],
      scanDialogOpen: false,
      sonarDialogOpen: false,
      isOnceComing: true,
      addJobRole: [],
      updateRole: [],
      onlyCreator: Boolean,
      isDesignManager: false,
      isUpdateRole: Boolean,
      filterEnvList: [],
      filterRqrmnt: [],
      taskManager: false,
      abandonedTaskDialogOpened: false,
      id: '',
      isDisable: false,
      filterModelList: [],
      permission: false,
      oldDbModel: '',
      fileType: '1',
      color: 'primary',
      newDoc: [],
      reviewChild: false,
      releaseChild: false,
      isReleaseUp: false,
      handleShow: false,
      commitTabledata: [],
      orignalModelProperties: [],
      saveResultDialogOpened: false,
      test: [
        '涉及操作系统、中间件升级/补丁修复',
        '涉及存储策略变更',
        '涉及网络策略变更'
      ],
      filesType: '设计类',
      fileTypeOptions: fileTypeOptions,
      directionOptions: directionOptions,
      confirmBookTimeOpen: false,
      bookModel: {
        confirmFileDate: ''
      }
    };
  },
  validations: {
    jobModel: {
      proWantWindow: {
        required
      },
      partyB: {
        required
      },
      partyA: {
        required
      },
      group: {
        required
      },
      fdev_implement_unit_no: {
        required
      },
      difficulty: {
        required(val) {
          if (!this.job.direction) return true;
          return !!val;
        }
      },
      modify_reason: {
        required(val) {
          if (this.difficultyIsChange && this.job.direction) {
            return val ? !!val.trim() : false;
          }
          return true;
        }
      },
      review: {
        securityTest: {
          required
        }
      }
    },
    bookModel: {
      confirmFileDate: {
        required
      }
    }
  },
  watch: {
    'job.stage.code'(val) {
      if (val < 0) {
        this.jobStageCode = true;
      }
    },
    modelSettingOpened(val) {
      this.permission = !val ? false : this.permission;
    },
    'envModel.first_category'(val) {
      this.modelConstant.category.forEach(cate => {
        for (let key in cate) {
          if (key === val) {
            this.secondCategories = cate[key];
          }
        }
      });
    },
    'envModel.term'(val) {
      if (val === null) {
        val = '';
        this.modelHtml = '';
      }
      if (typeof val === 'object') {
        this.modelHtml = '';
        let envNameCn = val.name_cn;
        let envNameEn = val.name_en;
        let envKeys = val.env_key;
        let modelHtmlStart = '## ' + envNameCn + '\n';
        envKeys.forEach(key => {
          let modelDesc = key.name_cn;
          // let modelKey = `${envNameEn}.${key.name_en}`;
          let modelValue = '$&lt;' + `${envNameEn}.${key.name_en}` + '&gt;';
          this.modelHtml += '# ' + envNameCn + '.' + modelDesc + '\n';
          this.modelHtml += 'appKey' + '=' + modelValue + '\n';
        });
        let modelHtmlEnd = '';
        this.modelHtml = modelHtmlStart + this.modelHtml + modelHtmlEnd;
      }
    },
    'envModel.env'(val) {
      this.getPreviewFile();
    },
    tab: {
      immediate: true,
      async handler(val) {
        const id = this.$route.params.id;
        if (val === 'testResult') {
          this.getTestResult(id);
        } else if (val === 'translation') {
          this.handleDocTree();
        } else if (val === 'codeAnalizy') {
          this.getTips();
        } else if (val === 'UATtestResult') {
          this.getUATTestResult(id);
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups', 'abandonGroups']),
    ...mapState('appForm', ['appData', 'sonarScan']),
    ...mapState('jobForm', [
      'taskRqrmntAlert',
      'jobProfile',
      'docDetail',
      'testDetail',
      'UATtestDetail',
      'queryDeleteJobProfile',
      'jobEnvModelList',
      'modelConstant',
      'envList',
      'envListByAppId',
      'previewFile',
      'configTemplate',
      'pirvateModelList',
      'extraConfigParam',
      'rqrmntsList',
      'defectList',
      'UATdefectList',
      'configProperties',
      'reviewRecordStatus',
      'commitTips',
      'scanProcess',
      'sonarLog',
      'result'
    ]),
    ...mapState('interfaceForm', ['taskManagerLimit']), // 扫描权限控制，fdev应用不扫描，xxx-web-xxx和vue应用只能当前任务的行内项目负责人，任务负责人，开发人员
    ...mapGetters('user', ['isLoginUserList', 'isKaDianManager']),
    ...mapState('releaseForm', [
      'confirmIsRelease',
      'isOverdue',
      'taskReview',
      'releaseNodeData',
      'release_node_name'
    ]),
    ...mapState('demandsForm', ['ipmpUnitList', 'fdevNOinfo']),
    // 扫描权限控制，只能当前任务的行内项目负责人，任务负责人，开发人员
    isManager() {
      const { partyA, partyB } = this.job;
      return (
        partyA.concat(partyB).some(user => user.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    isSpdbManager() {
      const { partyA } = this.job;
      return partyA.some(user => user.id === this.currentUser.id);
    },
    isSonarManager() {
      let managerEditIds = getIdsFormList([
        this.job.spdb_master,
        this.job.master,
        this.job.developer,
        this.job.creator,
        this.job.tester
      ]);
      return managerEditIds.indexOf(this.currentUser.id) > -1;
    },
    haveReleaseFile() {
      const releaseFile = this.docDetail.doc.filter(
        file => file.type === '投产类'
      );
      return releaseFile.length > 1;
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
    tip() {
      if (this.isBusiness) {
        return tipContent.BusinessTip;
      } else {
        return tipContent.TechnologyTip;
      }
    },
    // 计算值控制配置按钮的显示
    isDefaultDevEnv() {
      if (!this.envModel.env) {
        return false;
      }
      const { labels } = this.envModel.env;
      if (labels) {
        return labels.indexOf('dev') > -1 && labels.indexOf('default') > -1;
      } else {
        return false;
      }
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
    isShowFirst() {
      return this.jobModel.review.db[0]
        ? this.jobModel.review.db[0].name === '是'
        : false;
    },
    // 应用是否容器化项目
    isJavaTypeApp() {
      return this.job.type_name && this.job.type_name === 'Java微服务';
    },
    difficulty() {
      return this.jobModel.direction
        ? difficultyOptions[this.jobModel.direction]
        : [];
    },
    difficultyOptions() {
      return this.difficulty.map((item, i) => {
        return { label: item, value: i + 1 };
      });
    },
    difficultyIsChange() {
      return (
        this.jobModel.difficulty !== this.job.difficulty ||
        (this.job.direction && this.jobModel.direction !== this.job.direction)
      );
    },
    uploadedFiles() {
      const { filesType, docDetail } = this;
      const files = [];
      if (!docDetail.doc) return files;
      docDetail.doc.forEach(file => {
        if (file.type === filesType) {
          files.push(file.name);
        }
      });
      return files;
    }
  },

  filters: {
    direction(val) {
      const directionObj = directionOptions.find(item => item.value === val);
      return directionObj.label;
    },
    filterReview(val) {
      return reviewMap[val];
    }
  },
  methods: {
    ...mapActions('user', {
      fetchUser: 'fetch',
      updateFdevMantis: 'updateFdevMantis',
      updateAssignUser: 'updateAssignUser',
      queryFuserMantis: 'queryFuserMantis'
    }),
    ...mapActions('interfaceForm', {
      isTaskManager: 'isTaskManager'
    }),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('jobForm', [
      'queryTaskRqrmntAlert',
      'queryJobProfile',
      'queryDocDetail',
      'queryTestDetail',
      'queryUATTestDetail',
      'updateJobStage',
      'updateJob',
      'queryDeleteJob',
      'deleteJob',
      'getEnvModelList',
      'getModelConstant',
      'saveConfigTemplate',
      'getEnvList',
      'getEnvListByAppId',
      'previewConfigFile',
      'queryConfigTemplate',
      'queryExcludePirvateModelList',
      'queryExtraConfigParam',
      'queryFtaskMantis',
      'queryJiraIssues',
      'saveDevConfigProperties',
      'deleteFile',
      'queryReviewRecordStatus',
      'queryCommitTips',
      'getScanProcess',
      'downloadSonarLog',
      'downExcel',
      'deleteFileNew',
      'nocodeTask',
      'deleteTaskDoc',
      'confirmBtn',
      'updateTaskDoc'
    ]),
    ...mapActions('appForm', ['queryApplication']),
    ...mapActions('releaseForm', [
      'queryDetailByTaskId',
      'updateTaskArchived',
      'queryTaskReview',
      'queryReleaseNodeByJob',
      'taskChangeNotise'
    ]),
    ...mapActions('demandsForm', [
      'queryAvailableIpmpUnit',
      'queryByFdevNoAndDemandId'
    ]),
    groupFilter(val, update, abort) {
      update(() => {
        if (
          this.groups &&
          Array.isArray(this.groups) &&
          this.groups.length > 0
        ) {
          this.groupList = this.groups.filter(group => {
            return group.name.indexOf(val) > -1;
          });
        }
      });
    },
    formatMyJob,
    closeBookTime() {
      this.job.confirmBtn = '0';
    },
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    //开始Uat的时间控制
    uatStartDateOptions(date) {
      date = moment(date).valueOf();
      let currentDate = new Date().getTime();
      return (
        date <= currentDate &&
        date >= moment(this.jobModel.start_inner_test_time).valueOf()
      );
    },
    //开始Uat的时间控制
    relStartDateOptions(date) {
      date = moment(date).valueOf();
      let currentDate = new Date().getTime();
      if (this.jobModel.start_uat_test_time) {
        return (
          date <= currentDate &&
          date >= moment(this.jobModel.start_uat_test_time).valueOf() &&
          date >= moment(this.jobModel.start_inner_test_time).valueOf()
        );
      } else {
        return (
          date <= currentDate &&
          date >= moment(this.jobModel.start_inner_test_time).valueOf()
        );
      }
    },
    async updateConfirmBook() {
      await this.confirmBook();
      this.init();
      this.confirmBookTimeOpen = false;
    },
    async handleAllTip() {
      this.$v.bookModel.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('bookModel') > -1;
      });
      validate(Keys.map(key => this.$refs[key]));
      if (this.$v.bookModel.$invalid) {
        return;
      }
    },
    changeStatus() {
      this.permission = !this.hasEditPermissions;
    },
    changeDialogShow(val) {
      this.handleShow = val;
    },
    closeDataDialog() {
      this.dataAlert = false;
    },
    // 上线确认书
    confirmBookOpen(val) {
      this.confirmBookTimeOpen = true;
    },
    async confirmBook() {
      await this.confirmBtn({
        confirmBtn: '1',
        taskId: this.job.id,
        confirmFileDate: this.bookModel.confirmFileDate
      });
      successNotify('上线确认书确认按钮已经打开');
    },
    descFilter(input) {
      if (!input) {
        return '-';
      }
      input = input.replace(/<[^>]+>/g, '');
      const reg = new RegExp(/\n/g);
      return input.replace(reg, '</br>');
    },
    labelColor(val) {
      if (val.includes('提测打回')) {
        return 'red';
      } else {
        return 'teal';
      }
    },
    // 提交实体模板
    async handleSubmitModel() {
      return this.$q
        .dialog({
          title: `保存配置模板`,
          message: `确定保存此配置模板吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.submitModel();
        });
    },
    async submitModel() {
      const content = this.editor.replace(/\n{2,}/g, $1 => {
        const nArr = $1.split('\n');
        const res = nArr.slice(0, nArr.length / 2 + 1).join('\n');
        return res;
      });
      let params = {
        project_id: this.job.project_id,
        feature_branch: this.job.feature_branch,
        content: content
      };

      await this.saveConfigTemplate(params);
      if (this.result) {
        this.saveResultDialogOpened = true;
        return;
      }
      successNotify('保存模板成功');
      this.modelSettingOpened = false;
    },
    appendModelHtml() {
      let reg1 = new RegExp(/&lt;/g);
      let reg2 = new RegExp(/&gt;/g);
      let modelHtml = this.modelHtml.replace(reg1, '<').replace(reg2, '>');
      this.$refs.propertyEdit.runCmd('insertText', modelHtml);
    },
    compare(property) {
      return (one, two) => {
        let before = one[property];
        let after = two[property];
        return before >= after ? 1 : -1;
      };
    },
    async handleModelSettingOpen() {
      this.isOnceComing = true;
      this.modelSettingOpened = true;
      //获取有权限的用户id
      await this.queryApplication({ id: this.job.project_id });
      this.managerIds = getIdsFormList([
        this.job.spdb_master,
        this.job.master,
        this.appData[0].dev_managers,
        this.appData[0].spdb_managers
      ]);
      let managerEditIds = getIdsFormList([
        this.job.spdb_master,
        this.job.master,
        this.job.developer,
        this.appData[0].dev_managers,
        this.appData[0].spdb_managers
      ]);
      if (
        managerEditIds.indexOf(this.currentUser.id) > -1 ||
        this.isKaDianManager
      ) {
        this.hasEditPermissions = true;
      }
      if (
        this.managerIds.indexOf(this.currentUser.id) > -1 ||
        this.isKaDianManager
      ) {
        this.hasPermissions = true;
      }
      // 查询环境列表
      //await this.getEnvList();
      // 根据应用查询环境列表
      await this.getEnvListByAppId({ app_id: this.job.project_id });
      this.filterEnvList = deepClone(this.envListByAppId);
      //this.filterEnvList = deepClone(this.envList);
      // 查询常量
      await this.getModelConstant();
      this.firstCategories = [];
      this.secondCategories = [];
      this.typeList = this.modelConstant.type;
      let categories = this.modelConstant.category
        ? this.modelConstant.category
        : [];
      categories.forEach(item => {
        for (let key in item) {
          this.firstCategories.push(key);
          this.secondCategories = this.secondCategories.concat(item[key]);
        }
      });
      this.cloneSecCategories = deepClone(this.secondCategories);
      // 查询自己应用的私有实体用作关联项
      await this.queryExcludePirvateModelList({
        name_en: this.jobModel.project_name
      });
      // 过滤掉所有的含ci的数据
      this.filterModelList = this.pirvateModelList.filter(item => {
        return item.name_en.split('_')[0].indexOf('ci') < 0;
      });
      let list = this.filterModelList; // 外部编辑器过滤掉ci环境实体
      this.filterModelList.sort(this.compare('name_en'));
      let modelProperties = [];
      list.forEach(model => {
        let modelNameEn = model.name_en;
        let modelNameCn = model.name_cn;
        let envKeys = model.env_key;
        envKeys.forEach(key => {
          let modelKey = `${modelNameEn}.${key.name_en}`;
          let modelProNameCn = key.name_cn;
          modelProperties.push({
            isVarivale: true,
            name: modelKey,
            value: modelKey,
            label: modelKey,
            desc: modelNameCn + '.' + modelProNameCn
          });
        });
      });
      // 查询已有配置模板
      let params = {
        project_id: this.job.project_id,
        feature_branch: this.job.feature_branch
      };
      // 已有参数配置模板Property
      let configTemplateProperties = [];
      try {
        await this.queryConfigTemplate(params);
        this.editor = this.configTemplate;
        this.isOnceComing = false;
      } catch (e) {
        throw e;
      } finally {
        this.orignalModelProperties = this.modelProperties = modelProperties.concat(
          configTemplateProperties
        );
        this.$refs.propertyEdit.runCmd('selectAll');
        this.$refs.propertyEdit.runCmd('insertText', this.editor, 'initEditor');
      }
    },
    async getPreviewFile() {
      if (!this.isOnceComing) {
        if (
          this.editor.trim() &&
          this.envModel.env &&
          this.envModel.env.name_en
        ) {
          let params = {
            type: '0',
            env_name: this.envModel.env.name_en,
            content: this.editor,
            project_id: this.job.project_id
          };
          await this.previewConfigFile(params);
          this.modelProperties = deepClone(this.orignalModelProperties);

          const { outSideParam, modelParam } = this.previewFile;
          const modelParamKeys = Object.keys(modelParam);
          const outSideParamKeys = Object.keys(outSideParam);

          modelParamKeys.forEach(key => {
            const index = this.modelProperties.findIndex(
              item => item.label === key
            );
            if (index > -1) {
              this.modelProperties[index].value = modelParam[key];
            } else {
              this.modelProperties.push({
                label: key,
                name: key,
                isVarivale: true,
                value: modelParam[key]
              });
            }
          });

          outSideParamKeys.forEach(key => {
            this.modelProperties.push({
              label: key,
              name: key,
              value: outSideParam[key]
            });
          });
        } else {
          errorNotify('外部配置模板或者环境不能为空');
        }
      }
    },
    // 实体选择过滤
    async filterFn(val, update, abort) {
      update(() => {
        this.nameEnList = this.filterModelList.filter(item => {
          return (
            item.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            item.name_cn.toLowerCase().indexOf(val.toLowerCase()) > -1
          );
        });
      });
    },
    async download(path) {
      let param = {
        path: path,
        moduleName: 'fdev-task'
      };
      await this.downExcel(param);
    },
    async getTestResult(id) {
      this.testResultLoading = true;
      await this.queryTestDetail({ id });
      this.drewPageBySitTest();
      try {
        await this.queryFtaskMantis({ id });
        this.testResultLoading = false;
      } catch (e) {
        this.testResultLoading = false;
      }
    },
    async getUATTestResult(id) {
      this.UATtestResultLoading = true;
      try {
        await this.queryJiraIssues({ taskId: id });
        this.UATtestResultLoading = false;
      } catch (e) {
        this.UATtestResultLoading = false;
      }
    },
    async getUserOptions() {
      await this.fetchUser();
      let users = this.isLoginUserList;
      this.users = users.map(user => formatOption(formatUser(user), 'name'));
      this.userOptions = this.users.slice(0);
    },
    async handUpdateModalOpened() {
      await this.getUserOptions();
      //查任务所属研发单元的状态
      await this.queryByFdevNoAndDemandId({
        demand_id: this.jobProfile.rqrmnt_no,
        fdev_implement_unit_no: this.jobProfile.fdev_implement_unit_no
      });
      this.getCanNotModify();
      this.queryAvailableIpmpUnit();
      //查询任务最新状态\
      await this.queryReviewRecordStatus({ id: this.id });
      this.updateModalOpened = true;
      this.jobModel = deepClone(this.formatMyJob(this.job, this.users));
      this.jobModel.review.commonProfile = this.jobModel.review.commonProfile
        ? this.jobModel.review.commonProfile
        : false;
      this.jobModel.review.securityTest = this.jobModel.review.securityTest
        ? this.jobModel.review.securityTest
        : '不涉及';
      this.jobModel.review.specialCase = this.jobModel.review.specialCase
        ? this.jobModel.review.specialCase
        : ['不涉及'];
      this.jobModel.fdev_implement_unit_no = this.jobModel.fdev_implement_unit_no;
      this.ipmpUnitCloneList =
        this.ipmpUnitList.length > 0 ? deepClone(this.ipmpUnitList) : [];
      this.ipmpUnitCloneList.map(val => {
        if (val.implement_unit_status_normal === 1) {
          this.$set(val, 'disable', true);
          this.$set(
            val,
            'tooltip',
            '该研发单元状态为评估中，无法关联，请联系需求管理员/牵头人完成评估'
          );
        } else if (val.implement_unit_status_normal === 8) {
          this.$set(val, 'disable', true);
          this.$set(val, 'tooltip', '该研发单元已归档');
        } else if (val.implement_unit_status_normal === 9) {
          this.$set(val, 'disable', true);
          this.$set(val, 'tooltip', '该研发单元已撤销');
        } else if (
          val.implement_unit_status_special === 1 ||
          val.implement_unit_status_special === 2
        ) {
          this.$set(val, 'disable', true);
          this.$set(val, 'tooltip', '该研发单元处于暂缓状态');
        }
      });
      this.jobModel.proWantWindow = this.jobModel.proWantWindow
        ? this.jobModel.proWantWindow
        : this.jobModel.productionDate;
      if (!this.jobModel.review.db[0]) {
        this.jobModel.review.db = [
          {
            name: '否'
          }
        ];
      }
      this.oldDbModel = this.jobModel.review.db[0].name;
    },
    getCanNotModify() {
      let normalStatus = [1, 8, 9];
      let specialStatus = [1, 2];
      let fdevNormal = this.fdevNOinfo.implement_unit_info
        .implement_unit_status_normal;
      let fdevSpecial = this.fdevNOinfo.implement_unit_info
        .implement_unit_status_special;
      this.canNotModify = fdevSpecial
        ? specialStatus.indexOf(fdevSpecial) > -1
        : normalStatus.indexOf(fdevNormal) > -1;
    },
    async handleHint() {
      return this.$q
        .dialog({
          title: `确认归档`,
          message: `任务归档后，<span class="text-negative">"${
            this.job.name
          }"</span>的操作均无法执行且无法重新打开！归档后的任务只允许查看！请确认是否进行任务归档？`,
          ok: '确认',
          cancel: '取消',
          html: true
        })
        .onOk(async () => {
          await this.keepInFile();
        });
    },
    async confirmFinish() {
      await this.updateJobStage({
        id: this.job.id,
        stage: 'production'
      });
      successNotify('任务已完成');
      this.drewPageByJobProfile();
    },
    async keepInFile() {
      let next = this.stages[6];
      await this.updateJobStage({
        id: this.job.id,
        stage: next.value
      });
      successNotify('任务已归档');
      this.drewPageByJobProfile();
    },
    async handleStepNext(step, noCode) {
      if (step == 6 && noCode == 1) {
        this.otherTaskIdToPush = this.jobProfile.id;
        // 点击其他任务 请求  参数id,taskType  任务id  任务类型 1为无代码变更
        const params = {
          id: this.otherTaskIdToPush,
          taskType: 1
        };
        await this.nocodeTask(params);
        this.$router.push({
          path: '/job/list/' + this.otherTaskIdToPush + '/manage',
          query: { noCode: noCode }
        });
      } else {
        //创建分支的弹窗
        this.openBranch = true;
      }
    },
    async modifyTask() {
      if (
        this.jobProfile.start_uat_test_time &&
        !this.jobModel.start_uat_test_time
      )
        return;
      if (this.isReleaseUp) {
        const task_id = this.$route.params.id;
        await this.queryReleaseNodeByJob({ task_id });
        const { release_node_name } = this.releaseNodeData;
        if (release_node_name) {
          this.taskChangeNotise({
            release_node_name,
            task_id,
            type: '1'
          });
        }
      }
      this.$v.jobModel.$touch();
      let jobModelKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('jobModel') > -1;
      });
      validate(jobModelKeys.map(key => this.$refs[key]));
      if (this.$v.jobModel.$invalid) {
        return;
      }
      //后端传值字段转换
      this.jobModel.start_rel_test_time = this.jobModel.start_rel_test_time
        ? this.jobModel.start_rel_test_time
        : '';
      const params = {
        ...this.jobModel,
        group: this.jobModel.group.id,
        difficulty_desc: this.jobModel.direction
          ? this.jobModel.direction + this.jobModel.difficulty
          : '',
        modify_reason: this.difficultyIsChange
          ? this.jobModel.modify_reason
          : this.job.modify_reason
      };
      await this.updateJob(params);
      this.updateModalOpened = false;
      successNotify('修改成功');
      this.newDoc = [];
      const id = this.$route.params.id;
      await this.queryJobProfile({ id });
      this.drewPageByJobProfile();
    },
    // 过滤人员
    userOptionsFilter(param, thisManager = []) {
      thisManager = thisManager === null ? [] : thisManager;
      let userOptions = this.users.filter(item => {
        let flag = false;
        let roleLabels = [];
        item.role.forEach(ele => {
          roleLabels.push(ele.name);
        });
        param.forEach(roleName => {
          if (roleLabels.includes(roleName)) {
            flag = true;
          }
        });
        thisManager.forEach(manager => {
          if (manager.id === item.id) {
            flag = true;
          }
        });
        return flag;
      });
      return userOptions;
    },
    async userFilter(val, update, abort) {
      update(() => {
        this.users = this.userOptions.filter(
          user =>
            user.name.indexOf(val) > -1 || user.user_name_en.indexOf(val) > -1
        );
      });
    },
    addReviewTerm(val, done) {
      if (val.length > 0) {
        val = { name: val, label: val, audit: false };
        done(val);
      }
    },
    // 删除任务时先查询出 任务分支 应用 投产窗口 rel分支等需要展示信息
    async handleDeleteJob() {
      if (this.taskType == 1) {
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
    // 删除任务
    async handleDelete() {
      await this.deleteTask();
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
    // 将数组值为null的转为[]
    getArray(val) {
      for (let key in val) {
        if (Array.isArray(val[key]) && val[key] === null) {
          val[key] = [];
        }
      }
    },
    // 测试人员中是否包含当前用户
    includeMe(list) {
      if (Object.prototype.toString.call(list) === '[object Array]') {
        let includeMe = list.find(each => each.id === this.currentUser.id);
        if (includeMe) {
          return true;
        }
        return false;
      }
      return false;
    },
    //sit测试结果渲染页面
    drewPageBySitTest() {
      this.moduleData = this.testDetail.testPlan
        ? this.testDetail.testPlan
        : [];
    },
    //uat测试结果渲染页面
    drewPageByUATTest() {
      this.UATmoduleData = this.UATtestDetail.testPlan
        ? this.UATtestDetail.testPlan
        : [];
    },
    //关联文档树
    drewPageByDoc() {
      this.nodes[0].children = this.docDetail.doc.reduce((pre, next) => {
        let label = next.type;
        let filtered = pre.filter(each => each.label === label);
        if (filtered.length === 0) {
          pre.push({
            label,
            header: 'level-1',
            children: [
              {
                ...next,
                header: 'level-2',
                label: next.name
              }
            ]
          });
        } else {
          filtered[0].children.push({
            ...next,
            header: 'level-2',
            label: next.name
          });
        }

        return pre;
      }, []);
    },
    getTree() {
      const arr = [];
      this.docDetail.doc.forEach((item, test) => {
        let num = 0;
        let val = '';
        let port = baseUrl.substr(baseUrl.length - 5, 4);
        if (port !== '8080') {
          val = 'fdev-resources-test';
        } else {
          val = 'fdev-resources';
        }
        let path =
          `${val}/` +
          this.jobProfile.group.name +
          '/' +
          encodeURI(this.jobProfile.name)
            .substring(0, 10)
            .replace(/%/g, 'a') +
          `-${this.jobProfile.id}` +
          `/${item.type}` +
          `/${item.name}`;
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
            //还得继续判断是否存在 '数据库审核材料' 节点
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
    // 根据任务详情渲染页面
    drewPageByJobProfile() {
      this.job = formatJob(this.jobProfile);
      if (this.jobProfile.difficulty_desc) {
        const [direction, difficulty] = this.jobProfile.difficulty_desc.split(
          ''
        );
        this.job = {
          ...this.job,
          direction,
          difficulty: Number(difficulty)
        };
      }
      // 设置小组全量名
      let allGroups = [...this.groups, ...this.abandonGroups];
      let groupFullName = getGroupFullName(allGroups, this.job.group.id);
      let reVerseGroupFullName = groupFullName
        .split('-')
        .reverse()
        .join('-');
      this.$set(this.job, 'groupFullName', reVerseGroupFullName);

      this.getArray(this.job.review);
      this.jobModel = this.formatMyJob(this.job, this.users);
      this.role = getIdsFormList([
        this.job.creator,
        this.job.partyA,
        this.job.partyB,
        this.job.developer,
        this.job.tester
      ]);
      this.addJobRole = getIdsFormList([
        this.job.creator,
        this.job.partyA,
        this.job.partyB
      ]);
      let otherRole = getIdsFormList([
        this.job.partyA,
        this.job.partyB,
        this.job.developer
      ]);
      this.updateRole = getIdsFormList([
        this.job.creator,
        this.job.master,
        this.job.spdb_master
      ]);
      // 只是创建者创建者不显示处理按钮
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
      //具有修改权限权限的人
      this.isUpdateRole = this.updateRole.indexOf(this.currentUser.id) > -1;
    },
    async init(val) {
      if (val === 'addBranchFlag') {
        this.jobStageCode = false;
      }
      this.assessmentLoading = true;
      const id = this.$route.params.id;
      await this.fetchGroup();
      await this.queryJobProfile({ id });
      if (this.jobProfile.taskType && this.jobProfile.taskType == 1) {
        this.taskType = 1;
      }
      if (this.jobProfile.taskType && this.jobProfile.taskType == 2) {
        this.taskType = 2;
        this.tab = 'translation';
      }
      if (this.jobProfile.project_name && this.jobProfile.feature_branch) {
        await this.queryCommitTips({
          taskId: id
        });
        // 打开commit提交情况dialog
        if (
          this.commitTips.commits != 0 &&
          (this.jobProfile.stage == 'sit' || this.jobProfile.stage == 'develop')
        ) {
          this.commitTabledata = this.commitTips.fileList.map(item => {
            return { fileName: item };
          });
          this.handleShow = true;
        }
      }
      try {
        await this.isTaskManager({
          id: id,
          service_id: this.jobProfile.project_name
        });
        this.taskManager = this.taskManagerLimit;
      } catch (e) {
        this.taskManager = false;
      }
      this.queryDetailByTaskId({
        task_id: id
      });
      this.drewPageByJobProfile();
      if (this.job.stage.code === -1) {
        this.step2End = true;
      }
      if (this.job.stage.code === -3) {
        this.step4End = true;
      }
      if (this.job.taskType && this.job.taskType == 2) {
        if (this.job.devStartDate && this.job.productionDate) {
          this.planDateUpdateDialogOpen = false;
        } else if (this.isUpdateRole) {
          this.planDateUpdateDialogOpen = true;
        } else {
          this.planDateUpdateDialogOpen = false;
        }
      } else {
        if (
          this.job.devStartDate &&
          this.job.sitStartDate &&
          this.job.uatStartDate &&
          this.job.relStartDate &&
          this.job.productionDate
        ) {
          this.planDateUpdateDialogOpen = false;
        } else if (this.isUpdateRole) {
          this.planDateUpdateDialogOpen = true;
        } else {
          this.planDateUpdateDialogOpen = false;
        }
      }
      this.assessmentLoading = false;
    },
    async handleExtreConfigDialogOpen() {
      await this.queryExtraConfigParam({
        project_id: this.job.project_id
      });
      this.extreConfigDialogOpened = true;
    },
    changeExtreConfigEvent() {
      this.extreConfigDialogOpened = false;
    },
    startRead(files) {
      const reader = new FileReader();
      reader.readAsText(files[0]);
      reader.onload = $event => {
        this.$refs.propertyEdit.runCmd('insertText', $event.target.result);
      };
    },
    handleScanDialogOpen() {
      this.scanDialogOpen = true;
    },
    async getTips(isBTn) {
      await this.getScanProcess({ task_id: this.job.id });
      //触发时间是否在两个小时之前
      let timeDiff =
        new Date().getTime() -
        new Date(this.scanProcess.last_scan_time).getTime();
      let hour = timeDiff / (3600 * 1000);
      if (this.scanProcess.scan_stage === 0 && hour > 2) {
        this.$q
          .dialog({
            title: '温馨提示',
            message: 'Sonar扫描超时，请点击重试！',
            ok: '重新扫描',
            cancel: true
          })
          .onOk(async () => {
            this.sonarDialogOpen = true;
          })
          .onCancel(() => {
            return;
          });
      } else if (this.scanProcess.scan_stage === 4) {
        this.$q
          .dialog({
            title: 'sonar描述错误提示',
            message: `${this.scanProcess.tips}`,
            options: {
              type: 'radio',
              model: 'opt',
              inline: true,
              items: [
                { label: '下载错误日志', value: 'download' },
                { label: '重新扫描', value: 'retry' }
              ]
            },
            ok: '确定',
            cancel: true
          })
          .onOk(data => {
            this.sonarErrFn(data);
          })
          .onCancel(() => {
            return;
          });
      } else if (this.scanProcess.scan_stage === 0) {
        this.$q
          .dialog({
            title: 'sonar扫描提示',
            message: `${this.scanProcess.tips}`,
            ok: '知道啦'
          })
          .onOk(() => {
            return;
          });
      } else {
        if (this.scanProcess.scan_stage === 1 && !isBTn) {
          this.$q.dialog({
            title: 'sonar扫描提示',
            message: '当前分支未执行过sonar扫描',
            ok: '知道啦'
          });
        }
        if (isBTn) {
          this.sonarDialogOpen = true;
        }
      }
    },
    async sonarErrFn(data) {
      if (data === 'retry') {
        this.sonarDialogOpen = true;
      } else if (data === 'download') {
        await this.downloadSonarLog({
          taskId: this.job.id,
          fileName: 'sonar'
        });
        exportExcel(this.sonarLog, 'sonar.log');
      }
    },
    async handleSonarDialogOpen() {
      this.getTips(true);
    },
    nameFilter(spdbManager) {
      return spdbManager
        .map(item => {
          return item.user_name_cn;
        })
        .join('，');
    },
    envFilter(val, update) {
      update(() => {
        this.filterEnvList = this.envListByAppId.filter(tag => {
          return (
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    routeTo(id) {
      this.$router.push({ name: 'rqrProfile', params: { id: id } });
    },
    rqrmntInputFilter(val, update) {
      update(() => {
        this.ipmpUnitCloneList.forEach(tag => {
          if (tag.tooltip) {
            tag.disable = true;
          }
        });
        this.filterRqrmnt = this.ipmpUnitCloneList.filter(tag => {
          return (
            (tag.implement_unit_content &&
              tag.implement_unit_content.indexOf(val) > -1) ||
            tag.fdev_implement_unit_no.toLowerCase().includes(val.toLowerCase())
          );
        });
      });
    },
    async updateStatus(defect) {
      const id = this.$route.params.id;
      await this.updateFdevMantis(defect);
      this.queryFtaskMantis({ id });
    },
    async queryList() {
      const id = this.$route.params.id;
      await this.queryFtaskMantis({ id });
    },
    async updateUATStatus(defect) {
      const id = this.$route.params.id;
      await this.queryJiraIssues({ taskId: id });
    },
    async assignUser(defect) {
      await this.updateAssignUser(defect);
      let params = {
        userList: [
          {
            user_name_cn: this.currentUser.user_name_cn,
            user_name_en: this.currentUser.user_name_en
          }
        ],
        includeCloseFlag: '0'
      };
      await this.queryFuserMantis(params);
      // 刷新 SIT缺陷列表
      const id = this.$route.params.id;
      await this.queryFtaskMantis({ id });
    },
    /* 确认已投产 */
    async confirmHadRelease(task_id, name) {
      this.$q
        .dialog({
          title: '确认已投产',
          message: `确认任务已投产？`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.updateTaskArchived({
            task_id: this.$route.params.id
          });
          this.init();
          successNotify('已确认投产');
        });
    },
    // 保存到开发环境配置中心
    async saveConfig() {
      if (this.editor.trim() && this.envModel.env.name_en) {
        let params = {
          labels: this.envModel.env.labels,
          content: this.editor,
          project_id: this.job.project_id,
          feature_branch: this.job.feature_branch
        };
        await this.saveDevConfigProperties(params);
        successNotify('配置文件保存成功,上传路径:' + this.configProperties);
      } else {
        errorNotify('配置文件保存失败');
      }
    },
    closePlanDateUpdateDialog() {
      this.planDateUpdateDialogOpen = false;
      this.drewPageByJobProfile();
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
          await this.deleteTaskDoc(delFileParam);
          successNotify('删除成功');
          this.handleDocTree();
        });
    },
    async handleDocTree() {
      await this.queryDocDetail({
        id: this.id
      });
      this.getTree();
    },
    closeDialog() {
      let initEditor = LocalStorage.getItem('initEditor');
      if (this.editor !== initEditor) {
        return this.$q
          .dialog({
            message: `配置模板有修改但未保存，是否继续关闭？`,
            ok: '取消',
            cancel: '确定'
          })
          .onCancel(() => {
            this.modelSettingOpened = false;
          });
      } else {
        this.modelSettingOpened = false;
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
          this.updateModalOpened = false;
        });
    },
    handleError() {
      errorNotify(
        `部分实体在当前环境${
          this.envModel.env.name_en
        }未配置映射值，详见下方值为#ERROR#的字段`
      );
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
        const path = `${val}/${group.name}/${name}-${id}/${this.filesType}/${
          file.name
        }`;
        return { path, name: file.name, taskId: id, type: this.filesType };
      });

      formData.append('datas', JSON.stringify(datas));
      formData.append('taskId', id);
    },
    uploadSuccess() {
      successNotify('上传成功!');
      this.handleDocTree();
    },
    goReleaseFun() {
      this.$router.push(`/release/list/${this.release_node_name}`);
    }
  },
  async created() {
    if (this.$route.query.tab === 'productionProblems') {
      this.tab = 'productionProblems';
    }
    this.id = this.$route.params.id;
    this.loading = true;
    await this.queryReviewRecordStatus({ id: this.id });
    await this.init();
    this.loading = false;
    await this.fetchUser();
    await this.queryTaskRqrmntAlert({ task_id: this.id });
    this.dataAlert = this.taskRqrmntAlert.length > 0 ? true : false;
    await this.queryAvailableIpmpUnit();
    this.isTest = this.job.isTest;
  },
  beforeRouteUpdate(to, from, next) {
    next();
    this.init();
  }
};
</script>

<style lang="stylus" scoped>

.mr-right
  margin-right: 16px;
.infoStyle
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight: 600;
.titleStyle
  font-family: PingFangSC-Medium;
  font-size: 22px;
  margin-right: 12px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;
.subtitleStyle
  margin-top: 8px;
  margin-right: 4px;
  font-size: 14px;
  color: #666666;
  letter-spacing: 0;
  line-height: 14px;
.icontitle
  margin-top:33px;
  margin-bottom:20px;
.badgeStyle
  background: #DDEEFF;
  border-radius: 2px;
  border-radius: 2px;
  font-family: PingFangSC-Medium;
  font-size: 12px;
  color: #0378EA;
  padding:0px 8px;

.line-height
  line-height: 2em;
.normal-link
  cursor: pointer;
  color: #0663BE;
.normal-link:hover
  cursor: pointer;
  color: #2196f3;
.delete
  height: 350px !important;
.property-editor
  margin-top: 50px;
  height calc(100vh - 50px)
  overflow-y hidden
.uploader
  position: relative;
  top: 20px;
  max-width: 300px;
  z-index: 1;
.div-margin
  display: inline-block;
  margin-bottom: 15px;
.content > .column
  flex-wrap: nowrap;
.p-margin
  margin 16px
.padding-div
  padding-top 0
.editor-title
  height: 40px;
  line-height: 40px;
  // position: fixed;
  top: 48px;
  width: 100%;
  z-index: 2;
  background: #FFF;
.form
  padding: 0 20px;
.editor-warpper
  height: 100%;
  width 100%
  .col-3, .col
    display inline-block;
    height: 100%;
    overflow auto
    vertical-align: top;
    box-sizing border-box
  .col-3
    padding-bottom 20px
    width 25.5%;
  .col
    overflow auto
    padding-bottom 60px
    position relative
.shadow
  box-shadow none !important
.red-tip
  color: #f00;
  font-weight: 600;
  letter-spacing: 4px;
  position: relative;
  top: 3px;
  left: 8px;
  font-size: 20px;
.codeAnalizy-style
  padding 0
  background #f0f2f5
.codeAnalizy-style >>> .second_wrapper
  margin-top 0
.rules
  line-height: 1;
  font-size: 11px;
  line-height: 1;
  padding: 8px 12px;
  min-height 12px
  box-sizing: content-box;
  &.text-grey-7
    padding-left 0
.result-wrapper
  margin 0
  margin-left 16px
  margin-top 5px
  color #757575
.upload >>> .upload-body
    max-height 500px
    overflow auto
.tree
  max-height 610px
  overflow auto
.btnFlex
  display flex
.dataAlert
  height 300px
.dataAlertDialog
  postion relative
.btn-radius .q-btn
 border-radius 0 0 0 0
.Field-select >>> .required:empty
  display:none
.field-select >>> .input-field
 color black
.time-select
  width 300px
.book-btn
  width 100px
.q-field--with-bottom,
.q-field {
  margin: 0;
  // padding: 5px;
}
.mr12
  margin-right:12px;
.mr-t
  margin-top 32px
.margin-l-r
  margin:auto 2px
.confirm-btn
  justify-content flex-end
  button:ntn-child(1)
    margin-right:16px
.padding-form
  padding-bottom 20px
.form-item
  align-items center
  .form-label
    margin-right 16px
    position relative
  .select-width
    min-width 200px
.justify-content-flex-end
  justify-content flex-end !important
border(align='all')
  border-top 1px solid #ddd if align == 'top' || align == 'all'
  border-bottom 1px solid #ddd if align == 'bottom' || align == 'all'
.border-top
  border('top')
.border-bottom
  border('bottom')
.border
  border()
</style>
