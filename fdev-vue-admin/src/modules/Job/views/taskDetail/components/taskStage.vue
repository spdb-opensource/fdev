<template>
  <div>
    <f-block class="q-ptb-20">
      <fdev-stepper
        v-if="step >= 0 && step < 4"
        v-model="step"
        ref="stepper"
        header-nav
        flat
        alternative-labels
        class="stepper"
        animated
        transition-prev="fade"
        transition-next="fade"
      >
        <fdev-step
          :name="1"
          prefix="1"
          active-prefix="1"
          title="SIT测试"
          :done="step > 1"
          :header-nav="
            job.stage.code > 0 ||
              (job.stage.code == 0 && isTest === '0' && step > 1)
          "
        >
        </fdev-step>
        <fdev-step
          :name="2"
          prefix="2"
          active-prefix="2"
          title="UAT测试"
          :done="step > 2"
          :header-nav="job.stage.code > 1 || taskCanEntryUat"
        >
        </fdev-step>
        <fdev-step
          :name="3"
          prefix="3"
          active-prefix="3"
          title="REL测试"
          :done="step > 3"
          :header-nav="job.stage.code > 2"
        >
        </fdev-step>
      </fdev-stepper>
    </f-block>
    <div class="q-mt-10" v-if="taskType != 1">
      <div class="has-code-box">
        <template v-if="step >= 0 && step < 4">
          <div :class="`codeinfo-btns-test-box-${step}`">
            <!-- SIT阶段页面 begin-->
            <template
              class="sit-stage-template"
              v-if="step === 1 || step === 0"
            >
              <!-- 代码审核弹窗 -->
              <f-dialog
                v-model="SITDialog"
                transition-show="slide-up"
                transition-hide="slide-down"
                title="代码审核"
              >
                <div>
                  <span v-if="merging && mergerData.status_code !== '2'">
                    请联系应用负责人在gitlab上进行代码审核并合并
                  </span>
                  <span v-if="mergerData.status_code === '2'">
                    目前正在执行持续集成的过程，还请耐心等待应用的部署
                  </span>
                </div>
                <template v-slot:btnSlot>
                  <fdev-btn dialog label="确定" @click="SITDialog = false" />
                </template>
              </f-dialog>
              <!-- 代码信息加按钮提测 -->
              <div class="sit-wrap-box">
                <div class="sit-codeinfo-btns-test-box">
                  <f-block
                    class="sit-codeinfo-btns-box"
                    :style="
                      !(isManagerOrDeveloper && isTest === '1') && 'height:100%'
                    "
                  >
                    <!-- 代码信息 -->
                    <div class="sit-codeinfo">
                      <div class="row q-mt-16 items-center line-title">
                        <f-icon name="basic_msg_s_f" class="titleimg" />
                        <span class="titlename">代码信息</span>
                      </div>
                      <div class="row q-mt-10 new-ui-style">
                        <f-formitem
                          label-style="margin-right:20px;font-size: 12px;color: #000000;"
                          value-style="font-size: 12px;color: #333333;"
                          class="col-6 border-top-style border-bottom-style"
                          label="测试类型"
                          value-class="ellipsis"
                          label-class="formitem-label-style"
                          bottom-page
                        >
                          SIT
                        </f-formitem>
                        <f-formitem
                          label-style="margin-right:20px;font-size: 12px;color: #000000;"
                          value-style="font-size: 12px;color: #333333;"
                          class="col-6 border-top-style border-bottom-style"
                          label="feature分支"
                          value-class="ellipsis"
                          label-class="formitem-label-style"
                          bottom-page
                        >
                          {{ job.branch }}
                        </f-formitem>
                        <f-formitem
                          v-if="isAppTask"
                          label-style="margin-right:20px;font-size: 12px;color: #000000;"
                          value-style="font-size: 12px;color: #333333;"
                          class="col-6   border-bottom-style"
                          label="SIT分支"
                          value-class="ellipsis"
                          label-class="formitem-label-style"
                          bottom-page
                        >
                          <div v-if="isTestRunApp">
                            <span>{{ job.branch | filterBranch }}</span>
                          </div>
                          <div v-else>
                            <!-- 不涉及环境部署 -->
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
                        <!-- 任务关联的是 非应用 -->
                        <f-formitem
                          v-else
                          label-style="margin-right:20px;font-size: 12px;color: #000000;"
                          value-style="font-size: 12px;color: #333333;"
                          class="col-6 border-bottom-style"
                          label="SIT分支"
                          value-class="ellipsis"
                          label-class="formitem-label-style"
                          bottom-page
                        >
                          <span>SIT</span>
                        </f-formitem>
                        <f-formitem
                          label-style="margin-right:20px;font-size: 12px;color: #000000;"
                          value-style="font-size: 12px;color: #333333;"
                          class="col-6  border-bottom-style"
                          value-class="ellipsis"
                          label-class="formitem-label-style"
                          bottom-page
                          label="gitlab地址"
                        >
                          <a
                            class="link"
                            target="_blank"
                            :href="job.gitlabUrl"
                            :title="job.gitlabUrl"
                            >{{ job.gitlabUrl }}</a
                          >
                        </f-formitem>
                      </div>
                    </div>
                    <!-- 按钮们 -->
                    <div class="sit-btns-box row justify-center  q-mt-20">
                      <Authorized
                        class="q-ml-md"
                        v-if="needDesignExamine() && isDesignManager"
                      >
                        <fdev-btn
                          dialog
                          label="设计还原审核"
                          @click="toDesignReview"
                        />
                      </Authorized>
                      <Authorized
                        class="q-ml-md"
                        v-if="examReviewBtnShow"
                        :include-me="joinPeople"
                      >
                        <fdev-btn
                          dialog
                          label="发起审核"
                          @click="showReviewDialog"
                          :disable="examReviewBtnDisable"
                          :loading="globalLoading['jobForm/createFirstReview']"
                        />
                        <fdev-tooltip anchor="top middle" v-if="!isSPDBManager">
                          请联系该任务行内负责人或者卡点管理员发起审核
                        </fdev-tooltip>
                        <fdev-tooltip
                          anchor="top middle"
                          v-if="examReviewAfter"
                        >
                          任务正在审核中，不允许发起审核
                        </fdev-tooltip>
                        <fdev-tooltip anchor="top middle" v-if="examDbFile">
                          该任务下缺少数据库审核材料,请上传数据库审核材料至审核类/数据库审核目录下后再发起审核
                        </fdev-tooltip>
                      </Authorized>
                      <Authorized class="q-ml-md" :include-me="joinPeople">
                        <!--1检测到应用在使用挡板
                                2涉及任务部署的应用,未绑定部署信息
                                3发起的数据库审核尚未成功
                                4 loading
                                5合并请求 为待合并或者已合并
                                6 true:应用中有未申请的接口false:没有未申请的接口
                                7还原审核不通过或者不需要审核-->
                        <fdev-btn
                          dialog
                          @click="diffGrops()"
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
                        />
                        <fdev-tooltip
                          anchor="bottom middle"
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
                            designExaminePass() && !examineStatus && examEnv
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
                        <!-- 测试环境部署申请 查询是否构建完成disable -->
                        <fdev-btn
                          dialog
                          class="q-ml-md"
                          label="sit2部署申请"
                          color="primary"
                          @click="sit2ApplyDialog"
                          v-if="appGroup"
                          :disable="
                            !mergerData.merged ||
                              (deployInfo.deploy_status != null &&
                                deployInfo.deploy_status != '2')
                          "
                        />
                        <fdev-tooltip
                          anchor="top middle"
                          self="bottom middle"
                          :offest="[-70, 0]"
                          v-if="
                            !mergerData.merged ||
                              (deployInfo.deploy_status != null &&
                                deployInfo.deploy_status != '2')
                          "
                        >
                          <span v-if="!mergerData.merged">
                            该任务未成功合并过SIT,请点击[分支合并]
                          </span>
                          <span v-if="deployInfo.deploy_status == '1'">
                            该任务sit2部署失败，请联系部署申请审批人员在已完成审批列表中，进行重新部署
                          </span>
                          <span v-if="deployInfo.deploy_status == '0'">
                            该任务部署申请未审批，请联系部署申请审批人员进行申请
                          </span>
                          <span v-if="deployInfo.deploy_status == '3'">
                            该任务还在部署中，请稍后再试.....
                          </span>
                        </fdev-tooltip>
                      </Authorized>
                      <!-- 安卓 或者 ios  可以出测试包 -->
                      <Authorized
                        v-if="
                          isTestRunApp &&
                            (step === 1 || step === null || step === 0)
                        "
                        class="q-ml-md"
                        :include-me="joinPeople"
                      >
                        <div class="inline-block">
                          <fdev-btn
                            dialog
                            @click="openTestPackageDia"
                            label="出测试包"
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
                      <!-- 组件出测试包 -->
                      <Authorized
                        v-if="
                          job.applicationType &&
                            job.applicationType.startsWith('com')
                        "
                        class="q-ml-md"
                        :include-me="joinPeople"
                      >
                        <fdev-btn
                          dialog
                          @click="openTestPackageDia"
                          label="出测试包"
                        />
                      </Authorized>
                      <Authorized
                        class="q-ml-md"
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
                        <!-- 
                            SIT阶段或者开发阶段 挂载投产窗口
                            厂商或者行内任务负责人
                           -->
                        <fdev-btn
                          dialog
                          label="挂载投产窗口"
                          :disable="!!tooptip"
                          @click="next"
                        />
                      </Authorized>
                      <!-- 
                        任务负责人 || 行内任务负责人 || 开发人员 
                        应用类型是Android应用或者IOS应用
                        -->
                      <Authorized
                        class="q-ml-md"
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
                          dialog
                          class="q-mr-md"
                          label="进入试运行"
                          @click="toTestRun"
                          :disable="!!toopTipToTestRun"
                        />
                      </Authorized>
                    </div>
                  </f-block>

                  <f-block
                    ref="sit-test-box"
                    v-if="isManagerOrDeveloper && isTest === '1'"
                    class="sit-test-box testData q-mt-10 "
                    :style="
                      job.env.length && 'height:calc(100% - 220px - 10px)'
                    "
                  >
                    <SafeTest
                      :testData="testData"
                      :job="job"
                      :checkList="checkList"
                      :mergerData="mergerData"
                      :appGroup="appGroup"
                      :deployStatus="deployInfo.deploy_status"
                      :isLoginUserList="isLoginUserList"
                      :jobProfile="jobProfile"
                      @updateDetail="updateDetail"
                    />
                  </f-block>
                </div>
                <!-- 右侧 配置信息 -->
                <f-block
                  class="sit-peizhixinxi-box q-ml-10"
                  v-if="isAppTask"
                  style="max-height:760px"
                >
                  <div class="row  items-center line-title">
                    <f-icon name="basic_msg_s_f" class="titleimg" />
                    <span class="titlename">配置信息</span>
                  </div>
                  <div
                    :class="`sit-peizhixinxi-box-${idx}`"
                    v-for="(env, idx) in job.env"
                    :key="env.name"
                  >
                    <div class="second-title">
                      {{ env.name }}
                    </div>
                    <div class="col q-mt-dy">
                      <f-formitem
                        :label="each.name_zh"
                        v-for="each in env.var_list"
                        :key="each.key"
                        v-show="showData.includes(each.key)"
                        :title="each.value"
                        class="border-top-style"
                        label-class="bg-indigogrey-0"
                        label-style="width:130px;padding-left:12px;min-height:30px;line-height:30px;font-size: 14px;color: #333333;"
                        value-style="flex:1;line-height:30px;font-size: 14px;color: #333333;"
                      >
                        <div :title="each.value">
                          {{ each.value }}
                        </div>
                        <fdev-popup-proxy context-menu>
                          <fdev-banner style="max-width:300px">
                            {{ each.value }}
                          </fdev-banner>
                        </fdev-popup-proxy>
                      </f-formitem>
                    </div>
                  </div>
                </f-block>
              </div>
            </template>
            <!-- UAT阶段页面 begin-->
            <template class="uat-stage-template" v-else-if="step === 2">
              <!-- uat代码合并审核 -->
              <f-dialog
                v-model="UATDialog"
                transition-show="slide-up"
                transition-hide="slide-down"
                title="代码审核"
              >
                <fdev-card-section>
                  <span v-if="mergerData.status_code === '1'">
                    请联系应用负责人在gitlab上进行代码审核并合并
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
                    @click="UATDialog = false"
                  />
                </template>
              </f-dialog>
              <div :uat-desc="`uat-task_status:${job.releaseNode.task_status}`">
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
                  <!-- 2-拒绝投产， 或者 4-已取消投产， -->
                  <f-block class="col">
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
                            <f-formitem
                              label="测试类型"
                              label-style="margin-right:20px;font-size: 12px;color: #000000;"
                              value-style="font-size: 12px;color: #333333;"
                              class="col-6"
                            >
                              <div style="text-align:left">UAT</div>
                            </f-formitem>
                            <f-formitem
                              label="应用"
                              label-style="margin-right:20px;font-size: 12px;color: #000000;"
                              value-style="font-size: 12px;color: #333333;"
                              class="col-6"
                            >
                              <div style="text-align:left">
                                {{ job.project_name }}
                              </div>
                            </f-formitem>
                            <f-formitem
                              label="投产窗口"
                              label-style="margin-right:20px;font-size: 12px;color: #000000;"
                              value-style="font-size: 12px;color: #333333;"
                              class="col-6"
                            >
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
                            dialog
                            :disable="merging || !!tooptip"
                            :loading="
                              globalLoading['releaseForm/addJobReleaseNode']
                            "
                          />
                          <fdev-tooltip anchor="top middle" v-if="merging">
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
                      <!-- 拒绝投产或者取消投产 && 不是任务负责人 显示 -->
                      <template v-slot:exception>
                        <div class="data-none row flex-center q-my-xl">
                          请联系任务负责人挂载投产窗口
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
                                @click="openRefuseReleaseDialog = false"
                                label="确定"
                              />
                            </template>
                          </f-dialog>
                        </div>
                      </template>
                      <!-- 拒绝投产弹窗 -->
                    </Authorized>
                  </f-block>
                </div>
                <div
                  v-else-if="job.releaseNode.task_status === '0'"
                  class="row items-center"
                >
                  <!--0-投产待审核，  -->
                  <f-block class="col">
                    <div class="data-none row flex-center q-my-xl">
                      <f-icon class="text-primary" name="member" />
                      请联系任务的行内项目负责人
                      {{ getSpdbMaster(jobProfile.spdb_master) }}
                      在fdev投产模块下
                      <a href="javascript:void(0)" @click="goRel" class="link">
                        {{ job.releaseNode.release_node_name }}
                      </a>
                      投产窗口确认投产。
                    </div>
                  </f-block>
                </div>

                <div
                  v-else-if="
                    job.releaseNode.task_status === '1' ||
                      job.releaseNode.task_status === '3'
                  "
                >
                  <!-- 1-已确认投产， 3-取消投产待审核 -->
                  <template>
                    <div class="uat-wrap-box">
                      <div class="uat-codeinfo-btns-test-box">
                        <f-block class="uat-codeinfo-btns-box">
                          <!-- 代码信息 -->
                          <div class="uat-codeinfo">
                            <div class="row q-mt-16 items-center line-title">
                              <f-icon name="basic_msg_s_f" class="titleimg" />
                              <span class="titlename">代码信息</span>
                            </div>
                            <div class="row  new-ui-style">
                              <f-formitem
                                value-class="ellipsis"
                                label-class="formitem-label-style"
                                bottom-page
                                label="测试类型"
                                style="text-align:left"
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6 border-top-style border-bottom-style"
                              >
                                UAT
                              </f-formitem>
                              <f-formitem
                                value-class="ellipsis"
                                label-class="formitem-label-style"
                                bottom-page
                                :label="getTaskChineseName"
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6 border-top-style border-bottom-style"
                                style="text-align:left"
                              >
                                {{ job.project_name }}
                              </f-formitem>
                              <f-formitem
                                value-class="ellipsis"
                                label-class="formitem-label-style"
                                bottom-page
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6  border-bottom-style"
                                label="投产窗口"
                                style="text-align:left"
                              >
                                {{ job.releaseNode.release_node_name }}
                              </f-formitem>

                              <f-formitem
                                value-class="ellipsis"
                                label-class="formitem-label-style"
                                bottom-page
                                v-if="isAppTask"
                                label="UAT环境"
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6 border-bottom-style"
                                style="text-align:left"
                              >
                                {{ job.releaseNode.uat_env_name }}
                              </f-formitem>
                              <f-formitem
                                value-class="ellipsis"
                                label-class="formitem-label-style"
                                bottom-page
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6 border-bottom-style"
                                label="release分支"
                                style="text-align:left"
                                v-if="job.releaseNode.release_application"
                              >
                                {{
                                  job.releaseNode.release_application
                                    .release_branch
                                }}
                              </f-formitem>
                              <f-formitem
                                value-class="ellipsis"
                                label-class="formitem-label-style"
                                bottom-page
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6 border-bottom-style"
                                label="gitlab地址"
                              >
                                <a
                                  class="link"
                                  target="_blank"
                                  :href="job.gitlabUrl"
                                  :title="job.gitlabUrl"
                                  >{{ job.gitlabUrl }}</a
                                >
                              </f-formitem>
                              <f-formitem
                                label-class="formitem-label-style"
                                bottom-page
                                label="扫描接口"
                                label-style="margin-right:20px;font-size: 12px;color: #000000;"
                                value-style="font-size: 12px;color: #333333;"
                                class="col-6 border-bottom-style"
                                style="text-align:left"
                                v-if="
                                  job.releaseNode.release_application &&
                                    isAppTask
                                "
                              >
                                <span
                                  class="text-primary pointer"
                                  @click="handleScanDialogOpen"
                                  v-if="taskManagerLimit.limit"
                                >
                                  扫描接口
                                </span>
                              </f-formitem>
                            </div>
                          </div>
                          <!-- 按钮 -->
                          <div class="uat-btns-box row justify-center  q-mt-20">
                            <Authorized
                              v-if="isAppTask"
                              class="q-ml-md"
                              :include-me="joinPeople"
                            >
                              <fdev-tooltip
                                v-if="job.env.length === 0"
                                anchor="top middle"
                              >
                                未指定UAT测试环境
                              </fdev-tooltip>
                              <!-- 
                              发起的数据库审核尚未成功
                              接口发送中
                              合并请求为待合并或者已合并
                              部署未成功
                              UI审核未通过
                            -->
                              <fdev-btn
                                @click="handleUAT"
                                label="分支合并"
                                :disable="
                                  !!examineStatus ||
                                    codeBtnFlag ||
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
                                dialog
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
                                v-if="codeBtnFlag"
                              >
                                存在待审核的分支合并请求,请联系投产变更小组人员进行审核
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
                              v-if="!isAppTask"
                              class="q-ml-md"
                              :include-me="joinPeople"
                            >
                              <!-- 
                              发起的数据库审核尚未成功
                              接口发送中
                              合并请求为待合并或者已合并
                              部署未成功
                              UI审核未通过
                            -->
                              <fdev-btn
                                @click="handleUAT"
                                label="分支合并"
                                :disable="
                                  !!examineStatus ||
                                    codeBtnFlag ||
                                    loading ||
                                    merging ||
                                    (needUiAuditing() &&
                                      (this.job.review_status === 'nopass' ||
                                        this.job.review_status === 'fixing' ||
                                        this.job.review_status === 'uploaded' ||
                                        this.job.review_status ===
                                          'wait_allot' ||
                                        this.job.review_status ===
                                          'wait_upload'))
                                "
                                dialog
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
                                v-if="codeBtnFlag"
                              >
                                存在待审核的分支合并请求,请联系投产变更小组人员进行审核
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
                              class="q-ml-md"
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
                                label="进入投产窗口"
                                :disable="job.stage.code < 2"
                                dialog
                                @click="linkToReleaseApps"
                              />
                            </Authorized>
                          </div>
                        </f-block>
                        <!-- 提测重点 uat阶段才有 -->
                        <f-block
                          class="uat-test-box testData q-mt-10"
                          v-if="job.stage.code === 2 || step == 2"
                          :style="
                            job.env.length && 'height:calc(100% - 306px - 10px)'
                          "
                        >
                          <div class="row  items-center line-title">
                            <f-icon name="basic_msg_s_f" class="titleimg" />
                            <span class="titlename">UAT提测重点</span>
                          </div>
                          <fdev-input
                            v-model.trim="testKeynote"
                            type="textarea"
                            ref="testKeynote"
                            class="q-mt-md"
                            placeholder="请填写需要重点测试的功能"
                          />
                          <div class="row justify-center">
                            <fdev-btn
                              type="button"
                              label="提交"
                              class="q-mt-md"
                              color="primary"
                              :disable="!testKeynote"
                              @click="submitTestKeynote"
                            />
                            <fdev-tooltip v-if="!testKeynote">
                              请填写需要重点测试的功能
                            </fdev-tooltip>
                          </div>
                        </f-block>
                      </div>
                      <!-- 右侧配置信息 -->
                      <f-block
                        class="uat-peizhixinxi-box q-ml-10"
                        v-if="isAppTask"
                      >
                        <div class="row  items-center line-title">
                          <f-icon name="basic_msg_s_f" class="titleimg" />
                          <span class="titlename">配置信息</span>
                        </div>
                        <div v-for="env in job.env" :key="env.name">
                          <div class="second-title">
                            {{ env.name }}
                          </div>
                          <div class="col q-mt-dy">
                            <f-formitem
                              :label="each.name_zh"
                              v-for="each in env.var_list"
                              :key="each.key"
                              v-show="showData.includes(each.key)"
                              class="border-top-style"
                              label-class="bg-indigogrey-0"
                              label-style="width:130px;padding-left:12px;min-height:30px;line-height:30px;font-size: 14px;color: #333333;"
                              value-style="flex:1;line-height:30px;font-size: 14px;color: #333333;"
                            >
                              <div :title="each.value">
                                {{ each.value }}
                              </div>
                              <fdev-popup-proxy context-menu>
                                <fdev-banner style="max-width:300px">
                                  {{ each.value }}
                                </fdev-banner>
                              </fdev-popup-proxy>
                            </f-formitem>
                          </div>
                        </div>
                      </f-block>
                    </div>
                  </template>
                </div>
              </div>
            </template>
            <!-- REL阶段页面 begin-->
            <template class="rel-stage-template" v-else-if="step === 3">
              <div class="rel-wrap-box">
                <f-block class="rel-codeinfo-btns-test-box">
                  <div class="row q-mt-16  items-center line-title">
                    <f-icon name="basic_msg_s_f" class="titleimg" />
                    <span class="titlename">代码信息</span>
                  </div>
                  <div class="row  new-ui-style">
                    <f-formitem
                      value-class="ellipsis"
                      label-class="formitem-label-style"
                      bottom-page
                      label="测试类型"
                      label-style="margin-right:20px;font-size: 12px;color: #000000;"
                      value-style="font-size: 12px;color: #333333;"
                      class="col-6 border-top-style"
                    >
                      <div style="text:align-right">REL</div>
                    </f-formitem>
                    <f-formitem
                      value-class="ellipsis"
                      label-class="formitem-label-style"
                      bottom-page
                      :label="getTaskChineseName"
                      label-style="margin-right:20px;font-size: 12px;color: #000000;"
                      value-style="font-size: 12px;color: #333333;"
                      class="col-6 border-top-style"
                    >
                      <div style="text:align-right">
                        {{ job.project_name }}
                      </div>
                    </f-formitem>
                    <f-formitem
                      value-class="ellipsis"
                      label-class="formitem-label-style"
                      bottom-page
                      label="投产窗口"
                      label-style="margin-right:20px;font-size: 12px;color: #000000;"
                      value-style="font-size: 12px;color: #333333;"
                      class="col-6 border-top-style border-bottom-style"
                    >
                      <div>{{ job.releaseNode.release_node_name }}</div>
                    </f-formitem>
                    <f-formitem
                      value-class="ellipsis"
                      label-class="formitem-label-style"
                      bottom-page
                      label="pro tag分支"
                      v-if="job.product_tag"
                      label-style="margin-right:20px;font-size: 12px;color: #000000;"
                      value-style="font-size: 12px;color: #333333;"
                      class="col-6 border-top-style border-bottom-style"
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
                    <f-formitem
                      value-class="ellipsis"
                      label-class="formitem-label-style"
                      bottom-page
                      v-if="isAppTask"
                      label="REL环境"
                      label-style="margin-right:20px;font-size: 12px;color: #000000;"
                      value-style="font-size: 12px;color: #333333;"
                      class="col-6  border-bottom-style"
                    >
                      <div style="text:align-right">
                        {{ job.releaseNode.rel_env_name }}
                      </div>
                    </f-formitem>
                    <f-formitem
                      value-class="ellipsis"
                      label-class="formitem-label-style"
                      bottom-page
                      label-style="margin-right:20px;font-size: 12px;color: #000000;"
                      value-style="font-size: 12px;color: #333333;"
                      :class="!isAppTask && 'border-top-style'"
                      class="col-6 border-bottom-style"
                      label="gitlab地址"
                    >
                      <a
                        class="link"
                        target="_blank"
                        :href="job.gitlabUrl"
                        :title="job.gitlabUrl"
                        >{{ job.gitlabUrl }}</a
                      >
                    </f-formitem>
                  </div>

                  <div class="rel-btns-box row justify-center  q-mt-20">
                    <Authorized
                      :include-me="
                        job.partyB
                          .map(user => user.id)
                          .concat(job.partyA.map(user => user.id))
                      "
                    >
                      <fdev-btn
                        label="进入投产窗口"
                        :disable="!!!this.job.releaseNode.release_node_name"
                        dialog
                        @click="goRel"
                      />
                      <fdev-tooltip
                        anchor="top middle"
                        v-if="!!!this.job.releaseNode.release_node_name"
                      >
                        该任务还未挂载投产窗口，请到uat阶段挂载投产窗口
                      </fdev-tooltip>
                    </Authorized>
                  </div>
                </f-block>
                <f-block
                  class="rel-peizhixinxi-box q-ml-10"
                  v-if="isAppTask"
                  :style="step == '3' && 'height:260px'"
                >
                  <div class="row  items-center line-title">
                    <f-icon name="basic_msg_s_f" class="titleimg" />
                    <span class="titlename">配置信息</span>
                  </div>
                  <div v-for="(env, index) in job.env" :key="index">
                    <div class="second-title">
                      {{ env.env_name }}
                    </div>
                    <div class="col q-mt-dy">
                      <f-formitem
                        :label="each.name_zh"
                        v-for="each in env.var_list"
                        :key="each.key"
                        v-show="showData.includes(each.key)"
                        class="border-top-style"
                        label-class="bg-indigogrey-0"
                        label-style="width:130px;padding-left:12px;min-height:30px;line-height:30px;font-size: 14px;color: #333333;"
                        value-style="flex:1;line-height:30px;font-size: 14px;color: #333333;"
                      >
                        <div :title="each.value">
                          {{ each.value }}
                        </div>
                      </f-formitem>
                    </div>
                  </div>
                </f-block>
              </div>
            </template>
          </div>
        </template>
      </div>
    </div>
    <!-- 非代码变更页面 无代码任务-->
    <f-block v-else class="no-code-box  row no-wrap content-stretch q-mt-10">
      <div class="row">
        <fdev-form class="row " @submit.prevent="updateNocode">
          <f-formitem label="任务名称" value-style="margin-right:16px">
            <div>
              <router-link
                tag="span"
                :to="`/job/list/${jobProfileNOCode.id}`"
                class="normal-link"
              >
                {{ jobProfileNOCode.name }}
              </router-link>
            </div>
          </f-formitem>
          <f-formitem label="任务负责人" value-style="margin-right:16px">
            <div>
              <router-link
                tag="span"
                :to="`/user/list/${each.id}`"
                v-for="(each, index) in jobProfileNOCode.master"
                :key="index"
                class="normal-link"
              >
                {{ each.user_name_cn }}
              </router-link>
            </div>
          </f-formitem>
          <f-formitem label="行内项目负责人" value-style="margin-right:16px">
            <div class="">
              <router-link
                tag="span"
                :to="`/user/list/${each.id}`"
                v-for="(each, index) in jobProfileNOCode.spdb_master"
                :key="index"
                class="normal-link"
              >
                {{ each.user_name_cn }}
              </router-link>
            </div>
          </f-formitem>
          <f-formitem label="开发人员" value-style="margin-right:16px">
            <div class="">
              <router-link
                tag="span"
                :to="`/user/list/${each.id}`"
                v-for="(each, index) in jobProfileNOCode.developer"
                :key="index"
                class="normal-link"
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
            <fdev-tooltip v-if="disInput" anchor="top middle">
              请检查是否有编辑权限或任务阶段是否正确
            </fdev-tooltip>
          </f-formitem>
          <f-formitem :col="1" label="描述信息" value-style="margin-right:16px">
            <fdev-input
              :disable="disInput"
              v-model="updateFinishTimeModel.taskDesc"
              ref="updateFinishTimeModel.taskDesc"
              :rules="[
                () =>
                  $v.updateFinishTimeModel.taskDesc.required || '请填写描述信息'
              ]"
              type="textarea"
            />
            <fdev-tooltip v-if="disInput" anchor="top middle">
              请检查是否有编辑权限或任务阶段是否正确
            </fdev-tooltip>
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
        <div style="width:100%">
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
                  class="normal-link"
                >
                  {{ props.value }}
                </router-link>
              </fdev-td>
            </template>
            <template v-slot:body-cell-currentName="props">
              <fdev-td>
                <router-link
                  :to="`/user/list/${props.row.currentId}`"
                  class="normal-link"
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
              jobProfileNOCode.stage == 'sit' || jobProfileNOCode.stage == 'uat'
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
    </f-block>
    <!-- uat rel 未指定测试环境 弹窗-->
    <f-dialog
      persistent
      title="未指定环境"
      v-if="isAppTask && lackOfEnvWarning"
      v-model="lackOfEnvWarning"
    >
      <fdev-card-section v-if="job.releaseNode">
        <div style="width:30vw">
          当前任务在
          <router-link
            class="text-primary"
            :to="`/release/list/${job.releaseNode.release_node_name}/${path}`"
          >
            {{ job.releaseNode.release_node_name }}
          </router-link>
          投产窗口并未指定
          {{ lackOfEnv }}
          环境，请联系对应应用的行内/厂商项目负责人前往投产模块指定测试环境!
          地址：
          <router-link
            class="text-primary"
            :to="`/release/list/${job.releaseNode.release_node_name}/${path}`"
          >
            {{ url }}release/list/
            {{ job.releaseNode.release_node_name }}
            /{{ path }}
          </router-link>
        </div>
      </fdev-card-section>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="确定" @click="lackOfEnvWarning = false" />
      </template>
    </f-dialog>
    <!-- 数据库初次审核 弹窗 -->
    <f-dialog v-model="reviewDialog" right title="发起审核">
      <form>
        <fdev-select
          v-model="$v.reviewData.type.$model"
          placeholder="数据库类型"
          :options="dataTypeOptions"
          clearable
          ref="reviewDataType"
          :rules="[() => $v.reviewData.type.required || '请选择数据库类型']"
        />
        <fdev-select
          use-input
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
          :rules="[() => $v.reviewData.firstReview.required || '请指派初审人']"
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

        <f-formitem label="是否已通知关联供数系统配套改造" v-if="isShowNotify">
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
            <fdev-radio val="否" v-model="hasDone" label="否" class="q-pr-lg" />
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
        <fdev-btn label="关闭" outline dialog @click="reviewDialog = false" />
      </template>
    </f-dialog>
    <!-- 数据库二次审核 弹窗 -->
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
        <fdev-btn
          dialog
          outline
          label="关闭"
          @click="secondReviewDialog = false"
        />
      </template>
    </f-dialog>
    <!-- 分支合并二次审核 弹窗 -->
    <f-dialog
      title="申请合并"
      f-sc
      v-model="featureReview2rdDialog"
      @hide="closeReview2Dialog()"
    >
      <f-formitem required label="申请合并原因类型" diaS>
        <fdev-select
          v-model="mergeReasonType"
          :options="reasonOptions"
          :rules="[val => !!val || '申请原因类型不能为空']"
        />
      </f-formitem>
      <f-formitem
        v-if="mergeReasonType.value === 'other'"
        required
        label="申请合并原因描述"
        diaS
      >
        <fdev-input
          ref="applyReason"
          v-model="applyReason"
          clearable
          type="textarea"
          @keydown.native="noSpace($event)"
          placeholder="请填写再次发起合并的原因(不少于5个字)"
          :rules="[val => !!val || '申请原因不能为空']"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="approveLoad"
          @click="diffClick()"
        />
        <fdev-btn dialog outline label="取消" @click="closeReview2Dialog" />
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
                  v-if="modelArr[item.key] === '1' && item.key === 'rnversion'"
                  :label="item.children[1].title"
                  required
                >
                  <fdev-input
                    v-model="reRnVersionNo"
                    :placeholder="item.children[1].placehoder"
                  />
                </f-formitem>
                <f-formitem
                  v-if="modelArr[item.key] === '2' && item.key === 'rnversion'"
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
    <!--任务关联的类型是组件出测试包 -->
    <!--1分支合并前，可以出测试包，点击“出测试包”按钮，  -->
    <!--分支下拉框默认选中任务绑定的开发分支， -->
    <!--类型下拉框可以选择alpha和beta， -->
    <!--目标版本非必输。 -->
    <!--tag名：版本号-alpa -->
    <!--2分支合并后，可以出测试包，点击“出测试包”按钮，  -->
    <!--分支下拉框可以选择任务绑定的开发分支（默认被选中）以及SIT分支， -->
    <!--类型下拉框可以选择alpha和beta-->
    <!--目标版本非必输-->
    <!--tag名：版本号-beta-->
    <f-dialog
      v-if="isShowTestPackge"
      v-model="isShowTestPackge"
      @show="initTestPackge()"
      title="组件出测试包"
      f-sc
    >
      <fdev-form ref="testPackage" greedy @submit="submitForm" class="clo">
        <f-formitem label="分支" required>
          <fdev-select
            :disable="!mergerData.merged"
            ref="testPackge.branch"
            v-model="testPackge.branch"
            :options="testPackgeBranchOptions"
            :rules="[val => !!val || '请选择分支']"
          />
        </f-formitem>
        <f-formitem label="类型" required>
          <fdev-select
            ref="testPackge.type"
            v-model="testPackge.type"
            :options="testPackgeTypeOptions"
            :rules="[val => !!val || '请选择类型']"
          />
        </f-formitem>
        <f-formitem label="目标版本">
          <fdev-input
            placeholder="格式:*.x.x, 只能输入数字,如:1.1.1"
            @blur="checkComponentVersion"
            v-model="testPackge.version"
          />
        </f-formitem>
      </fdev-form>
      <template v-slot:btnSlot>
        <fdev-btn
          outline
          dialog
          label="取消"
          @click="isShowTestPackge = false"
        />
        <fdev-btn
          @click="handlerTestPackage"
          :loading="wait"
          dialog
          label="确定"
        />
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
          :loading="globalLoading['jobForm /nextStage']"
          label="确定"
          @click="confirmProduct"
        />
      </template>
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
</template>

<script>
import date from '#/utils/date.js';
import { required } from 'vuelidate/lib/validators';
import Authorized from '@/components/Authorized';
// import configCI from '@/modules/App/views/pipelines/configCI';
import ChangeBatchDialog from '@/modules/Release/Components/ChangeBatchDialog';
import AddBatchMessage from '@/modules/Release/Components/AddBatchMessage';
import { formatUser } from '@/modules/User/utils/model';
import { checkComponentV } from '@/services/job';
import {
  perform,
  createJobModel,
  createRedmineData,
  testData,
  createdFinishModel,
  createdSelectRelationModel,
  jgOption,
  relMergeReasonOptions,
  sitMergeReasonOptions
} from '@/modules/Job/utils/constants';
import { formatJob } from '@/modules/Job/utils/utils';
import ScanDialog from '@/modules/interface/components/scanDialog';
import PlanDateUpdateDialog from '@/modules/Job/components/PlanDateUpdateDialog.vue';
import {
  validate,
  successNotify,
  formatOption,
  errorNotify,
  baseUrl,
  resolveResponseError,
  getIdsFormList
} from '@/utils/utils';
import { mapState, mapActions, mapGetters } from 'vuex';
import { queryParamFile } from '@/services/job';

// queryTaskDetail  查询任务详情
// stages: [
//     { code: 0, label: '开发', value: 'develop' },
//     { code: 1, label: 'sit测试', value: 'sit' },
//     { code: 2, label: 'uat测试', value: 'uat' },
//     { code: 3, label: 'rel测试', value: 'rel' },
//     { code: 4,label: '投产',value: 'production',type: 'success',title: '任务已投产',description: '任务已投产'},
//     { code: 5,label: '已删除',value: 'abort',},
//     { code: 6,label: '归档',value: 'file',type: 'success', title: '任务已归档',description: '任务已归档'},
//     { code: '7',label: '废弃', value: 'discard', type: 'success',title: '任务已废弃', description: '任务已废弃'},
//     { code: -1, label: '录入信息完成', value: 'create-info' },
//     { code: -2, label: '录入应用完成', value: 'create-app' },
//     { code: -3, label: '录入分支完成', value: 'create-feature' }
// ],

// queryDetailByTaskId > queryReleaseNodeByJob > task_status
// 2 4 undefined  挂载窗口
// 0        前去 确认投产 提示
// 1 3  窗口信息

// /frelease/api/releasenode/queryReleaseNodes
// /frelease/api/releasebatch/queryBatchInfoByTaskId
// /frelease/api/task/queryDetailByTaskId
// /frelease/api/releasebatch/addBatch
// /frelease/api/releasebatch/queryBatchInfoByAppId
// /frelease/api/task/add

// /finterface/api/interface/isTaskManager
// 扫描接口 isTaskManager  taskManagerLimit
// /finterface/api/interfaceApplication/queryIsNoApplyInterface

// {
//     label: this.jobProfile && this.jobProfile.feature_branch,
//     value: this.jobProfile && this.jobProfile.feature_branch
// },
// /frelease/api/releasenode/queryReleaseNodes  < queryReleaseNode  UAT 查询 投产窗口下拉

// /fapp/api/app/query  appdata

// /ftask/api/task/checkSccOrCaas  查应用的部署信息

// 无  applicationType 字段的情况是  无代码任务

// 配置信息  job.env  /ftask/api/task/queryEnvDetail  queryEnv envData

// /frelease/api/task/queryDetailByTaskId  uat_status

// /release/list/20211210_002/componentlist

// queryDetailByTaskId -> queryReleaseNodeByJob  -> releaseNodeData
// 历史的开发任务 一定有 applicationType 字段 并且是 app 开头

// appJava-Java微服务
// appVue-Vue应用
// 	appIos-IOS应用
// 	appAndroid-Android应用
// appDocker-容器化项目
// appOldService-老版服务
// componentWeb-前端组件
// componentServer-后端组件
// archetypeWeb-前端骨架
// archetypeServer-后端骨架
// image-镜像

// 1：微服务窗口
// 2：原生窗口
// 3：试运行窗口
// 4：组件
// 5：骨架
// 6：镜像
export default {
  name: 'Manage',
  components: {
    Authorized,
    ScanDialog,
    PlanDateUpdateDialog,
    ChangeBatchDialog,
    AddBatchMessage,
    SafeTest: () => import('@/modules/Job/components/safeTest')
  },
  data() {
    return {
      confirmProductDialog: false,
      isToFile: true, // 是否归档
      featureReview2rdDialog: false,
      codeBtnFlag: '',
      applyReason: '',
      wait: false,
      isShowTestPackge: false,
      testPackge: {
        branch: '',
        type: '',
        version: ''
      },
      testPackgeBranchOptions: ['SIT'],
      testPackgeTypeOptions: ['alpha', 'beta'],
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
      uploadFile: '',

      releaseNodes: [],
      sitModel: {},
      uatModel: {
        releaseNode: ''
      },

      merger: {},
      merging: false,
      testData: testData(),
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
      jgOption: jgOption,
      jgStyle: '0',
      reasonOptions: [],
      mergeReasonType: {
        label: '需求功能问题',
        value: 'demandIssue'
      },
      testPackageType: '0',
      currentUploadFile: [],
      testKeynote: '',
      setBatchDialogOpened: false,
      setBatchModel: {},
      resolve: null,
      ysCode: {},
      modelArr: {},
      serviceid: '',
      versionNo: '',
      reRnVersionNo: '',
      deRnVersionNo: '',
      approveLoad: false
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
    async 'job.stage'(val) {
      if (val) {
        if (val.code === 1 || (val.code === 0 && this.job.isTest === '0')) {
          this.step = await this.queryTaskCanEntryUat();
          return;
        }
        if (val.code == 2) {
          try {
            await this.queryEnv({
              id: this.$route.params.id,
              type: 'uat'
            });
            this.job.env = this.envData;
          } catch (e) {
            // console.log(e)
          }
        }
        this.step = val.code;
      }
    },
    codeBtnFlag(val) {
      // if (val && val > 0) {
      //   return true;
      // } else {
      //   return false;
      // }
      return val;
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
        // 只用应用考虑 部署信息
        if (
          this.job.applicationType &&
          this.job.applicationType.startsWith('app')
        ) {
          await this.queryApplication({ id: this.job.project_id });
          this.appinfo = this.appData;
        } else {
          this.appinfo = [];
        }
        await this.queryAppDeployByTaskId({ taskId: this.job.id });
        await this.queryAppAscriptionGroup({ name: this.job.project_name });
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
          if (this.isAppTask) {
            await this.isTaskManager({
              id: id,
              service_id: this.job.project_name
            });
          }

          // type传参，ios的传2：原生窗口，其他的传1：微服务
          // 查询投产窗口
          //  /frelease/api/releasenode/queryReleaseNodes

          await this.queryReleaseNode({
            start_date: date.formatDate(Date.now(), 'YYYY-MM-DD'),
            type: this.getTaskType()
          });
          this.releaseNodes = formatOption(
            this.releaseNodeList,
            'release_node_name'
          );
          this.releaseNodes.forEach(item => {
            let label = item.owner_group_name;
            let value = item.release_node_name;
            item.optionLabel = `${label}：${value}`;
          });
          status = parseInt(this.job.uat_merge_status.status_code);
          // /frelease/api/task/queryDetailByTaskId
          // queryDetailByTaskId-根据任务id查询任务详情
          await this.queryReleaseNodeByJob({
            task_id: id,
            type: this.getTaskType()
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
            task_id: id,
            type: this.getTaskType()
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
    async 'job.releaseNode.task_status'(val) {
      // 弹出拒绝投产 弹框
      if (val === '2') {
        this.openRefuseReleaseDialog = true;
      }
      if (val == undefined) {
        await this.queryReleaseNodeByJob({
          task_id: this.$route.params.id,
          type: this.getTaskType()
        });
        this.job.releaseNode = this.releaseNodeData;
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
    // 任务关联的 应用类型
    getTaskChineseName() {
      if (this.isAppTask) {
        return '应用';
      } else {
        let type = this.job.applicationType || '';
        if (type.startsWith('com')) {
          return '组件';
        } else if (type.startsWith('arc')) {
          return '骨架';
        } else if (type.startsWith('ima')) {
          return '镜像';
        } else {
          return '组件';
        }
      }
    },
    isAppTask() {
      if (this.job.applicationType) {
        return this.job.applicationType.startsWith('app');
      } else {
        return true;
      }
    },
    path() {
      if (this.job.applicationType) {
        return this.job.applicationType.startsWith('app')
          ? 'applist'
          : 'componentlist';
      } else {
        return 'applist';
      }
    },
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
      'deployInfo',
      'appGroup',
      'codeApproveList',
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
    sieFeature() {
      return 'dev' + this.jobProfile.feature_branch.substring(7);
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
      //任务关联的应用类型是 非 应用 分支合并按钮不卡点
      if (
        this.appData[0] &&
        this.appData[0].label.indexOf('不涉及环境部署') > -1
      ) {
        return false;
      } else {
        if (this.isAppTask) {
          return !this.deployFlag;
        } else {
          // 任务关联的是 组件或者 骨架 或者 镜像
          return false;
        }
      }
    },
    examLabel() {
      return (
        !!this.appData[0] &&
        this.appData[0].label.indexOf('不涉及环境部署') > -1
      );
    },
    examineStatus() {
      // 发起的数据库审核尚未成功
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
    /* 任务负责人或行内项目负责人或卡点管理员*/
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
    /* 行内项目负责人 或卡点管理员 */
    isSPDBManager() {
      return (
        this.job.spdb_master.some(user => user.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    /* 任务负责人或行内项目负责人或卡点管理员 || 开发者 ||  卡点管理员 */
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
      return !this.job.spdb_master.some(
        user => user.id === this.currentUser.id
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
      // 任务关联文档 有一种 审核类-数据库审核材料 类型
      let existDbFile = this.job.doc.some(
        item => item.type === '审核类-数据库审核材料'
      );
      //不是(行内项目负责人或卡点管理员) || 任务关联文档没有审核类-数据库审核材料 类型 文件
      return (
        !this.isSPDBManager ||
        this.reviewRecordStatus === '初审中' ||
        this.reviewRecordStatus === '复审中' ||
        !existDbFile
      );
    },
    tooptip() {
      // SIT 阶段 挂载投产窗口按钮 提示语
      // 或者 进入 UAT 发起审核(审核投产窗口) 按钮不可点击提示语
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
      //如果是简单任务且已点击跳过内测可以直接挂载窗口
      if (
        this.job.simpleDemand === '0' &&
        this.job.stage.code > 0 &&
        this.job.skipFlag === '1'
      ) {
        return false;
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
      // 进入试运行按钮不可点击提示语
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
    }
  },
  methods: {
    // 组件出测试包 初始化
    initTestPackge() {
      // mergerData
      // status_code
      // 0 - 未发起合并
      // 1 - 待合并
      // 2 - 已合并，pipline进行中
      // 3 - 已合并，pipline构建成功
      // 4 - 已合并，pipline构建失败
      // 5 - 合并请求被关闭，可重新发起合并请求
      // merged	boolean
      // true 成功合并国至少一次。false没有
      this.testPackge.branch = this.jobProfile.feature_branch;
    },
    diffGrops() {
      if (this.appGroup) {
        this.appMergeApply();
      } else {
        //非零售组分支合并  featureMergeApply
        this.openStrengthenDialog();
      }
    },
    diffClick() {
      if (this.appGroup) {
        this.sendDeployApply();
      } else {
        //非零售组分支合并  featureMergeApply
        this.featureMergeApply();
      }
    },
    //校验组件版本号
    async checkComponentVersion() {
      // mpass-前端组件；back-后端组件
      // 没有输入 不发接口
      if (!this.testPackge.version) {
        return true;
      }
      let params = {
        type: this.job.applicationType == 'componentWeb' ? 'mpass' : 'back',
        component_id: this.job.project_id,
        target_version: this.testPackge.version
      };

      try {
        // 返回 'AAAAAAA' 代表校验成功
        await resolveResponseError(() => checkComponentV(params));
        return true;
      } catch {
        // 非 'AAAAAAA' 后端抛出错误
        return false;
      }
    },
    handlerTestPackage() {
      this.$refs.testPackage.submit();
    },
    async submitForm() {
      const keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.indexOf('testPackge') > -1
      );

      let res = keys.map(key => {
        if (!this.$refs[key].validate()) return 'error';
      });
      // 目标版本号输入的话需要后端校验 格式, 不输入的话不需要校验
      //校验未通过  阻止代码执行
      if (res.includes('error')) return;
      if (!(await this.checkComponentVersion())) return;
      this.wait = true;
      try {
        await this.iOSOrAndroidAppPackage({
          id: this.jobProfile.id, //任务 id
          ref: this.testPackge.branch, //分支名称
          packageType: this.testPackge.type, //beta、alpha
          targetVersion: this.testPackge.version //目标版本号，组件类型时用户输入，非必输
        });
        successNotify('出测试包成功！');
        this.isShowTestPackge = false;
      } catch {
        this.wait = false;
      } finally {
        this.wait = false;
      }
      this.wait = false;
    },
    getTaskType() {
      // 1
      // appJava-Java微服务
      // appVue-Vue应用
      // appIos-IOS应用
      // appAndroid-Android应用
      // appDocker-容器化项目
      // appOldService-老版服务
      // 4
      // componentWeb-前端组件
      // componentServer-后端组件
      // 5
      // archetypeWeb-前端骨架
      // archetypeServer-后端骨架
      // 6
      // image-镜像

      // 1：微服务窗口
      // 2：原生窗口
      // 3：试运行窗口
      // 4：组件
      // 5：骨架
      // 6：镜像
      let type = '';
      let taskType = this.job.applicationType || '';
      if (taskType.startsWith('app')) {
        if (
          taskType.startsWith('appIos') ||
          taskType.startsWith('appAndroid')
        ) {
          type = '2';
        } else {
          type = '1';
        }
      } else if (taskType.startsWith('com')) {
        type = '4';
      } else if (taskType.startsWith('arc')) {
        type = '5';
      } else if (taskType.startsWith('ima')) {
        type = '6';
      } else {
        type = '1';
      }
      return type;
    },
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
      'queryAppAscriptionGroup',
      'queryAppDeployByTaskId',
      'appDeploy',
      'addApprove',
      'releaseApproveList',
      'queryMerger',
      'queryJobProfile',
      'queryDocDetail',
      'putSitTest',
      'putUatTest',
      'queryEnv',
      'checkSccOrCaas',
      'createFirstReview',
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
      'bafflePoint', //查询应用是否开启挡板
      'deleteTaskDoc',
      'testKeyNote',
      'updateJobStage'
    ]),
    ...mapActions('appForm', ['queryApplication']),
    ...mapActions('releaseForm', [
      'queryReleaseNodeByJob',
      'queryReleaseNode',
      'addJobReleaseNode',
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
        //更新头部信息
        this.$root.$emit('updateTopTaskStage');
      } else {
        //  确认已投产弹框
        this.confirmProductDialog = true;
      }
    },
    async confirmProduct() {
      const params = {
        id: this.jobProfileNOCode.id
      };
      this.loading = true;
      await this.nextStage(params);
      this.loading = false;
      this.btnToGray = true;
      // this.$router.push(`/job/list/${this.jobProfileNOCode.id}`);
      if (this.isToFile) {
        await this.keepInFile();
        this.init();
      } else {
        // 不归档
        this.init();
      }
      this.confirmProductDialog = false;
      successNotify('确认投产成功!');
      this.$root.$emit('updateTopTaskStage');
    },
    async keepInFile() {
      await this.updateJobStage({
        id: this.jobProfileNOCode.id,
        stage: 'file'
      });
      successNotify('任务已归档');
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
      if (
        this.job.applicationType &&
        this.job.applicationType.startsWith('com')
      ) {
        this.isShowTestPackge = true;
      } else {
        // 安卓/ios
        // 组件出测试包
        await this.getParamFile();
        this.testPackageDia = true;
      }
    },
    goRel() {
      // 投产任务列表
      this.$router.push(
        `/release/list/${this.job.releaseNode.release_node_name}/joblist`
      );
    },
    //sit2部署申请的弹窗
    sit2ApplyDialog() {
      this.reasonOptions = sitMergeReasonOptions;
      this.featureReview2rdDialog = true;
    },
    async openStrengthenDialog() {
      let flag = false;
      if (this.job.tag && this.job.tag.length > 0) {
        flag = ['已提内测', '内测完成'].some(item => {
          return this.job.tag.includes(item);
        });
      }
      if (flag) {
        this.reasonOptions = sitMergeReasonOptions;
        this.featureReview2rdDialog = true;
      } else {
        await this.handleSIT();
      }
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
      if (
        this.job.applicationType &&
        this.job.applicationType.startsWith('app')
      ) {
        await this.bafflePoint({
          branch_name: this.job.branch,
          gitlab_project_id: this.appinfo[0].gitlab_project_id.toString()
        });
      } else {
        // saveBafflePointFlag
        // 非应用  不考虑挡板是否开启
        this.$store.commit('jobForm/saveBafflePointFlag', true);
      }
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
      if (this.job.releaseMergeFlag && this.job.releaseMergeFlag == '1') {
        //为1 需要卡点  为0 不需要卡点
        this.reasonOptions = relMergeReasonOptions;
        this.featureReview2rdDialog = true;
      } else {
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
      }
    },
    async handleREL() {
      this.loading = true;

      await this.putUatTest({
        id: this.job.id
      });

      this.loading = false;
      this.merging = true;
    },
    getArray(val) {
      // 计划日期等不用改null为[]，
      // applicationType不用改null为[]，
      // 日期控件选择时要用到
      for (let key in val) {
        if (
          this.dateList.indexOf(key) === -1 &&
          val[key] === null &&
          key != 'applicationType'
        ) {
          val[key] = [];
        }
      }
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
    //零售组分支合并申请
    async appMergeApply() {
      try {
        if (this.step === 1 || this.step === 0) {
          await this.handleSIT();
        }
      } catch (error) {
        throw error;
      }
    },

    //分支合并申请
    async featureMergeApply() {
      if (
        this.mergeReasonType.value === 'other' &&
        !this.$refs['applyReason'].validate()
      ) {
        return 'e';
      } else if (
        this.mergeReasonType.value === 'other' &&
        this.$refs['applyReason'].validate() &&
        this.applyReason.length < 5
      ) {
        errorNotify('申请原因不少于5个字');
        return;
      } else {
        let env = this.step === 1 || this.step === 0 ? 'sit' : 'uat';
        this.approveLoad = true;
        try {
          if (this.step === 1 || this.step === 0) {
            await this.handleSIT();
            await this.addApprove({
              taskId: this.job.id,
              applyDesc:
                this.mergeReasonType.value === 'other' ? this.applyReason : '',
              merge_reason: this.mergeReasonType.value,
              env
            });
          } else {
            await this.addApprove({
              taskId: this.job.id,
              applyDesc:
                this.mergeReasonType.value === 'other' ? this.applyReason : '',
              merge_reason: this.mergeReasonType.value,
              env
            });
            successNotify('已发起审核');
            this.queryTodoList();
          }
          this.featureReview2rdDialog = false;
          this.approveLoad = false;
        } catch (error) {
          this.featureReview2rdDialog = false;
          this.approveLoad = false;
          throw error;
        }
      }
    },

    //sit2测试环境部署申请
    async sendDeployApply() {
      if (
        this.mergeReasonType.value === 'other' &&
        !this.$refs['applyReason'].validate()
      ) {
        return 'e';
      } else if (
        this.mergeReasonType.value === 'other' &&
        this.$refs['applyReason'].validate() &&
        this.applyReason.length < 5
      ) {
        errorNotify('申请原因不少于5个字');
        return;
      } else {
        let env = this.step === 1 || this.step === 0 ? 'sit' : 'uat';
        this.approveLoad = true;
        try {
          if (this.step === 1 || this.step === 0) {
            await this.addApprove({
              taskId: this.job.id,
              applyDesc:
                this.mergeReasonType.value === 'other' ? this.applyReason : '',
              merge_reason: this.mergeReasonType.value,
              env
            });
            //补充参数
            await this.appDeploy({
              taskId: this.job.id,
              taskName: this.job.name,
              demandId: this.job.rqrmnt_no, //需求id
              oaContactNo: this.job.demand.oa_contact_name, //需求编号
              applicationId: this.job.project_id,
              applicationNameEn: this.job.project_name,
              //applicationNameZh:this.job.
              applayUserId: this.currentUser.id,
              applayUserNameZh: this.currentUser.user_name_cn,
              applayUserOwnerGroupId: this.currentUser.group.id,
              applayUserOwnerGroup: this.currentUser.group.fullName,
              applyDesc:
                this.mergeReasonType.value === 'other'
                  ? this.applyReason
                  : this.mergeReasonType.label,
              merge_reason: this.mergeReasonType.value
            });
            successNotify('已发起审核');
            //刷新按钮控制的权限，计算属性控制deploy_status的值
            await this.queryAppDeployByTaskId({ taskId: this.job.id });
            //this.queryTodoList();
          } else {
            await this.addApprove({
              taskId: this.job.id,
              applyDesc:
                this.mergeReasonType.value === 'other' ? this.applyReason : '',
              merge_reason: this.mergeReasonType.value,
              env
            });
            successNotify('已发起审核');
            this.queryTodoList();
          }
          this.featureReview2rdDialog = false;
          this.approveLoad = false;
        } catch (error) {
          this.featureReview2rdDialog = false;
          this.approveLoad = false;
          throw error;
        }
      }
    },
    closeReview2Dialog() {
      this.mergeReasonType = {
        label: '需求功能问题',
        value: 'demandIssue'
      };
      this.featureReview2rdDialog = false;
    },
    async queryTodoList() {
      this.loading = true;
      let params = {
        pageSize: 5,
        currentPage: 1,
        status: [0],
        taskId: this.job.id,
        requestSource: '1'
      };
      await this.releaseApproveList(params);
      this.codeBtnFlag = this.codeApproveList.count > 0;
      this.loading = false;
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
    async init() {
      const id = this.$route.params.id || this.$route.query.id;
      const noCode = this.$route.params.noCode;
      await this.queryJobProfile({ id }); // 查询任务详情
      this.jobProfileNOCode = this.jobProfile;
      this.testPackgeBranchOptions.push(this.jobProfile.feature_branch);
      this.testKeynote = this.jobProfile.testKeyNote;
      if (this.jobProfileNOCode.taskType == 1 || noCode == 1) {
        // 无代码任务
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
        // 开发任务
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
        // 任务负责人  行内任务负责人  开发人员
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
        await this.queryTodoList();
        this.job.doc = this.docDetail.doc;
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
      // vue 应用 或者 前端组件
      return (
        (this.appinfo[0] && this.appinfo[0].type_name === 'Vue应用') ||
        this.job.applicationType == 'componentWeb'
      );
    },
    // SIT合并权限 还原审核通过或者不需要审核must
    designExaminePass() {
      if (
        this.job.applicationType &&
        this.job.applicationType.startsWith('app')
      ) {
        // 任务关联的 是应用
        if (
          this.appinfo[0] &&
          this.jobProfile.demand &&
          this.jobProfile.demand.ui_verify &&
          this.appinfo[0].type_name === 'Vue应用' &&
          (this.job.review_status === 'wait_upload' || //待上传
          this.job.review_status === 'uploaded' || //上传了
            this.job.review_status === 'irrelevant') //不涉及
        ) {
          return false;
        } else {
          return true;
        }
      } else {
        // 任务关联的是 前端组件
        if (
          this.jobProfile.demand &&
          this.jobProfile.demand.ui_verify &&
          this.job.applicationType == 'componentWeb' &&
          (this.job.review_status === 'wait_upload' ||
            this.job.review_status === 'uploaded' ||
            this.job.review_status === 'irrelevant')
        ) {
          return false;
        } else {
          return true;
        }
      }
    },
    //判断是否是ui审核任务
    needUiAuditing() {
      // 需求涉及 UI审核且是vue应用或
      // 需求涉及 UI审核且是前端组件
      if (
        (this.appinfo[0] &&
          this.jobProfile.demand &&
          this.jobProfile.demand.ui_verify &&
          this.appinfo[0].type_name === 'Vue应用') ||
        (this.jobProfile.demand &&
          this.jobProfile.demand.ui_verify &&
          this.job.applicationType == 'componentWeb')
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
        task_id: this.$route.params.id,
        type: this.getTaskType()
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
      this.$router.push(`/release/list/${release_node_name}/${this.path}`);
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
          application_id: this.job.project_id,
          type: this.getTaskType()
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
    },
    //禁止输入空格
    noSpace(event) {
      if (event.keyCode == 32) {
        event.returnValue = false;
      }
    }
  },
  async created() {
    window.this_ = this;
    // setTimeout(() => {
    //   this.job.releaseNode.task_status = '2';
    // }, 1000);
    await this.init();
    await this.fetchUser();
    //取值查询详情接口 queryTaskDetail
    this.isTest = this.job.isTest;
    // 任务修改成功 执行init方法
    this.$root.$on('updateTaskStage', this.init);
  }
};
</script>

<style lang="stylus" scoped>
.merge
  padding: 2px;
  background: white;
  color: #0663BE;
  font-size: 13px;
  font-weight: 600;
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
.form
  padding: 16px;
  max-width: 820px;
  margin: 0 auto;
.text-wrapper
  word-break:break-all;
  word-wrap: break-word;
.pointer
  cursor: pointer
.scroll
  width: 380px;
  margin: 0 auto;
.reviewDesc
  font-size: 16px;
  color: #666;
.testData
  .center
    align-items center
.normal-link
  cursor: pointer;
  color: #2196f3;
.next-btn
  margin: 20px auto
.edit-btn
  margin: 0 auto
  display: block
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.upload >>> .upload-body
  max-height 393px
  overflow auto
.word-break
  word-break break-all
.stepper
  width 780px
  margin 0 auto
>>> .q-stepper__header--alternative-labels .q-stepper__dot
  width 32px
  height 32px
>>> .q-stepper__content.q-panel-parent
  display:none
>>> .q-stepper__header--alternative-labels .q-stepper__tab
  padding 0
  min-height auto
.sit-wrap-box,
.uat-wrap-box,
.rel-wrap-box
  display flex
  flex-wrap nowrap
  justify-content space-between
.sit-codeinfo-btns-test-box,
.uat-codeinfo-btns-test-box,
.rel-codeinfo-btns-test-box
  flex 1
.sit-peizhixinxi-box,
.uat-peizhixinxi-box,
.rel-peizhixinxi-box
  width calc(35% - 10px);
  // height 578px
  overflow-y scroll
  .border-top-style:last-child
    border-bottom 1px solid #DDDDDD
.sit-peizhixinxi-box::-webkit-scrollbar,
.uat-peizhixinxi-box::-webkit-scrollbar,
.rel-peizhixinxi-box::-webkit-scrollbar
  width: 4px;
  height: 4px;
.q-mt-10
  margin-top 10px
.q-mt-dy
  margin-top 8px
.q-ml-10
  margin-left 10px
.q-mt-20
  margin-top 20px
.q-ptb-20
  padding 20px 0
>>> .q-mt-16
  margin-bottom 16px
>>> .titleimg
    width: 16px;
    height: 16px;
    color: #0663BE;
    border-radius: 4px;
    margin-top -4px
    margin-right:10px
>>> .titlename
    font-size: 14px;
    color: #333333;
    line-height: 22px;
    font-weight 600
>>> .second-title
  font-size: 14px;
  color: #333333;
  line-height: 22px;
  margin-top 10px
  font-weight 600
>>> .new-ui-style
  div
    height 43px
    line-height 43px
  .formitem-label-style
    font-size: 14px;
    background-color: rgb(244, 246, 253)
    padding-left 12px
    width 100px
>>> .border-top-style
  border-top 1px solid #ddd
  .input-height
    height:auto;
  &.items-start
    align-items: normal
>>> .border-bottom-style
  border-bottom  1px solid #ddd
  .input-height
    height: auto;
  &.items-start
    align-items: normal
</style>
