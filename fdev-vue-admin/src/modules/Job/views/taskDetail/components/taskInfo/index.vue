<template>
  <div class="bgcolor">
    <div>
      <f-block>
        <div class="row">
          <div class="row q-pr-llg wd300">
            <f-icon
              class="mt2"
              name="combine"
              :width="16"
              :height="16"
            ></f-icon>
            <div class="q-ml-sm flex1">
              <div class="iconTitle">
                关联需求
              </div>
              <div class="q-mt-xs lh20 breakALL">
                <router-link
                  :to="{ path: `/rqrmn/rqrProfile/${job.rqrmnt_no}` }"
                  class="normal-link"
                  :title="job.demand && job.demand.oa_contact_name"
                >
                  {{ job.demand && job.demand.oa_contact_name }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ job.demand && job.demand.oa_contact_name }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </router-link>
              </div>
              <div class="lh20">
                <span
                  v-if="job.demand"
                  class="normal-link breakALL"
                  @click="routeTo(job.rqrmnt_no)"
                  :title="job.demand.oa_contact_no"
                >
                  {{ job.demand && job.demand.oa_contact_no }}
                </span>
              </div>
            </div>
          </div>
          <div class="row line q-px-llg wd226">
            <f-icon class="mt2" name="numpad" :width="14" :height="14"></f-icon>
            <div
              v-if="
                job.ipmp_implement_unit_no ||
                  (!job.ipmp_implement_unit_no && !job.other_demand_task_num)
              "
              class="q-ml-sm flex1"
            >
              <div class="iconTitle">
                实施单元编号
              </div>
              <div class="q-mt-xs lh20 breakALL">
                <router-link
                  :to="
                    `/rqrmn/unitDetail/${job.ipmp_implement_unit_no}/${
                      job.rqrmnt_no
                    }`
                  "
                  v-if="job.ipmp_implement_unit_no"
                  class="normal-link"
                  style="max-width:300px"
                  :title="job.fdev_implement_unit_no"
                >
                  {{ job.ipmp_implement_unit_no }}
                </router-link>
                <span v-else>-</span>
              </div>
            </div>
            <div
              v-if="!job.ipmp_implement_unit_no && job.other_demand_task_num"
              class="q-ml-sm flex1"
            >
              <div class="iconTitle">
                其他需求任务编号
              </div>
              <div class="q-mt-xs lh20 breakALL">
                <router-link
                  :to="
                    `/rqrmn/ODTaskDetail/${job.other_demand_task_num}/${
                      job.rqrmnt_no
                    }`
                  "
                  class="normal-link"
                  style="max-width:300px"
                  :title="job.other_demand_task_num"
                >
                  {{ job.other_demand_task_num }}
                </router-link>
              </div>
            </div>
          </div>
          <div class="row line q-px-llg wd226">
            <f-icon
              class="mt2"
              name="notepad"
              :width="14"
              :height="14"
            ></f-icon>
            <div class="q-ml-sm flex1">
              <div class="iconTitle">
                研发单元
              </div>
              <div class="q-mt-xs lh20 breakALL">
                <router-link
                  :to="{
                    path: '/rqrmn/devUnitDetails',
                    query: {
                      id: job.rqrmnt_no,
                      dev_unit_no: job.fdev_implement_unit_no
                    }
                  }"
                  v-if="job.unitNo"
                  class="normal-link"
                  :title="job.fdev_implement_unit_no"
                >
                  {{ job.unitNo }}
                </router-link>
                <span v-else>-</span>
              </div>
            </div>
          </div>
          <div class="line q-pl-llg flex1">
            <div class="row items-center">
              <f-icon
                name="codeinfo"
                class="text-primary"
                :width="18"
                :height="18"
              ></f-icon>
              <div class="q-pl-sm fw600 lineh14 mt2">代码信息</div>
            </div>
            <div class="row lh18 q-mt-sm">
              <div class="row item-center col-6 q-pr-sm">
                <div class="q-pr-md">{{ applicationName }}</div>
                <div class="breakALL" style="flex:1">
                  <router-link
                    :to="`${getPath()}/${job.project_id}`"
                    class="normal-link"
                    v-if="job.project_name"
                    :title="job.project_name"
                  >
                    {{ job.project_name }}
                  </router-link>
                  <span v-else>-</span>
                </div>
              </div>
              <div class="row item-center" style="flex:1">
                <div class="q-pr-md">分支</div>
                <div
                  class="breakALL"
                  style="flex:1"
                  :title="job.feature_branch"
                >
                  {{ job.feature_branch || '-' }}
                </div>
              </div>
            </div>
            <div v-if="job.versionNum" class="row lh18 item-center q-mt-xs">
              <div class="q-pr-md">预设版本号</div>
              <div class="breakALL" :title="job.versionNum">
                {{ job.versionNum }}
              </div>
            </div>
            <div v-if="taskType != 2 && isApp" class="row lh18 q-mt-xs">
              <div style="padding-right:10px">分支扫描</div>
              <div>
                <span
                  class="normal-link q-mr-sm"
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
                  class="q-mr-sm normal-link"
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
              </div>
            </div>
          </div>
        </div>
      </f-block>
    </div>
    <div class="row">
      <div class="wd776">
        <f-block class="q-mt-10">
          <!-- 实施安排及情况 -->
          <!-- 图标位置 -->
          <div class="icontitle mb10 row items-center">
            <f-icon
              name="bell_s_f"
              class="text-primary mr10"
              :width="18"
              :height="18"
            ></f-icon>
            <span class="infoStyle">任务实施安排及情况</span>
          </div>
          <div v-if="taskType != 2" class="row border-bottom full-width">
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
                label="计划启动日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.plan_start_time">{{
                  job.plan_start_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="实际启动日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.start_time">{{ job.start_time || '-' }}</span>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
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
                  job.plan_inner_test_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
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
                  job.start_inner_test_time || '-'
                }}</span>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
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
                  job.plan_uat_test_start_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
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
                  job.start_uat_test_time || '-'
                }}</span>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
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
                  job.plan_rel_test_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
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
                  job.start_rel_test_time || '-'
                }}</span>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
                label="计划投产日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.plan_fire_time">{{
                  job.plan_fire_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="实际投产日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.fire_time">{{ job.fire_time || '-' }}</span>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
                label="投产意向窗口"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.proWantWindow">{{
                  job.proWantWindow || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="投产窗口"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <div class="ellipsis" :title="release_node_name">
                  {{ release_node_name || '-' }}
                </div>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
                label="UAT提测日期"
                bottom-page
                profile
                label-auto
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.uat_test_time">{{
                  job.uat_test_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
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
          <div v-else class="row border-bottom full-width">
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
                label="计划启动日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.plan_start_time">{{
                  job.plan_start_time || '-'
                }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="计划完成日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.plan_fire_time">{{
                  job.plan_fire_time || '-'
                }}</span>
              </f-formitem>
            </div>
            <div class="row full-width border-top">
              <f-formitem
                class="col-6"
                label="实际启动日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.start_time">{{ job.start_time || '-' }}</span>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="实际完成日期"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="q-px-lg"
              >
                <span :title="job.fire_time">{{ job.fire_time || '-' }}</span>
              </f-formitem>
            </div>
          </div>
        </f-block>
        <f-block v-if="taskType != 2" class="q-mt-10">
          <Loading :visible="assessmentLoading">
            <div class="icontitle mb10 row items-center">
              <f-icon
                name="message_s_f"
                class="text-primary mr10"
                :width="16"
                :height="16"
              ></f-icon>
              <span class="infoStyle mt2">关联项评估</span>
            </div>
            <div class="row border-top">
              <f-formitem
                class="col-6"
                label="数据库变更"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="ellipsis q-px-lg"
              >
                <div v-if="job.review.data_base_alter.length === 0">否</div>
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
                class="col-6"
                label="公共配置文件更新"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="ellipsis q-px-lg"
              >
                {{ job.review.commonProfile ? '涉及' : '不涉及' }}
              </f-formitem>
            </div>
            <div class="row border-top">
              <f-formitem
                class="col-6"
                label="其他系统变更"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="ellipsis q-px-lg"
              >
                <div
                  v-if="
                    job.review.other_system == null ||
                      job.review.other_system.length === 0
                  "
                >
                  否
                </div>
                <div class="q-gutter-x-sm" v-else>
                  <span
                    v-for="(each, index) in job.review.other_system"
                    :key="index"
                  >
                    {{ each.name }}
                  </span>
                </div>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="安全测试"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="ellipsis q-px-lg"
              >
                <f-icon v-if="!job.review.securityTest" name="close" />
                <span v-else>{{ job.review.securityTest }}</span>
              </f-formitem>
            </div>
            <div class="row border-top border-bottom">
              <f-formitem
                class="col-6"
                label="是否涉及特殊情况"
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="ellipsis q-px-lg"
              >
                <div v-if="job.review.specialCase.length === 0">否</div>
                <div class="q-gutter-x-sm" v-else>
                  <span
                    v-for="(each, index) in job.review.specialCase"
                    :key="index"
                  >
                    {{ each }}
                  </span>
                </div>
              </f-formitem>
              <f-formitem
                class="col-6"
                label=""
                profile
                label-auto
                bottom-page
                label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                label-style="height:43px;width:160px;"
                value-style="line-height:43px;"
                value-class="ellipsis q-px-lg"
              >
                <div></div>
              </f-formitem>
            </div>
          </Loading>
        </f-block>
      </div>
      <div style="width:10px"></div>
      <div style="flex:1">
        <f-block class="q-mt-10">
          <div class="row items-center" style="padding-bottom:14px">
            <f-icon
              name="member_s_f"
              class="text-primary"
              :width="16"
              :height="16"
            ></f-icon>
            <div class="q-pl-sm fw600 lineh14 mt2">人员信息</div>
          </div>
          <div
            v-for="(item, index) in userList"
            :key="index"
            class="row userStyle border-top"
            :class="index === userList.length - 1 ? 'border-bottom' : ''"
          >
            <div class="wd150 bg-indigogrey-0 ellipsis px-12 flex items-center">
              {{ item.label }}
            </div>
            <div class="q-px-md flex1" v-if="item.value === 'creator'">
              <router-link
                tag="span"
                :to="`/user/list/${job[item.value].id}`"
                class="normal-link"
                :title="job[item.value].user_name_cn"
              >
                {{ job[item.value].user_name_cn }}
              </router-link>
            </div>
            <div class="q-px-md flex1" v-else>
              <div
                v-if="job[item.value] && job[item.value].length > 0"
                :title="getName(job[item.value])"
              >
                <router-link
                  tag="span"
                  :to="`/user/list/${each.id}`"
                  v-for="(each, index) in job[item.value]"
                  :key="index"
                  class="normal-link q-mr-sm"
                >
                  {{ each.user_name_cn }}
                </router-link>
              </div>
              <div v-else>-</div>
            </div>
          </div>
        </f-block>
        <f-block class="q-mt-10">
          <div class="row items-center lineh14" style="padding-bottom:14px">
            <f-icon
              name="basic_msg_s_f"
              class="text-primary"
              :width="16"
              :height="16"
            ></f-icon>
            <div class="q-pl-sm fw600 mt2">其他信息</div>
          </div>
          <div class="row userStyle border-top" v-if="job.simpleDemand === '0'">
            <div class="wd150 bg-indigogrey-0 px-12 flex items-center">
              是否已跳过功能测试
            </div>
            <div class="ellipsis flex1 q-px-md">
              {{ job.skipFlag | skipFlagFilter }}
            </div>
          </div>
          <div v-if="taskType != 2">
            <div
              v-for="(item, index) in otherMsg"
              :key="index"
              class="row userStyle border-top"
              :class="index === otherMsg.length - 1 ? 'border-bottom' : ''"
            >
              <div
                :title="item.label"
                class="wd150 bg-indigogrey-0 px-12 flex items-center"
              >
                {{ item.label }}
              </div>
              <div
                :title="job.review_status | filterReview"
                class="flex1 q-px-md"
                v-if="item.value === 'review_status'"
              >
                <router-link
                  :to="`/job/list/${job.id}/design`"
                  class="normal-link"
                  v-if="job.review_status !== 'irrelevant'"
                >
                  {{ job.review_status | filterReview }}
                </router-link>
                <span v-else>{{ job.review_status | filterReview }}</span>
              </div>
              <div
                class="flex1 q-px-md"
                v-else-if="item.value === 'confirmShow'"
              >
                <fdev-toggle
                  v-model="job.confirmBtn"
                  @input="confirmBookOpen"
                  left-label
                  :disable="
                    Boolean(parseInt(job.confirmBtn)) ||
                      !job.confirmShow ||
                      !isManager ||
                      !(
                        job.groupFullName &&
                        job.groupFullName.indexOf('互联网') != -1
                      )
                  "
                  true-value="1"
                  false-value="0"
                />
                <fdev-tooltip
                  anchor="top middle"
                  self="center middle"
                  :offest="[0, 0]"
                  v-if="
                    Boolean(parseInt(job.confirmBtn)) ||
                      !job.confirmShow ||
                      !isManager ||
                      !(
                        job.groupFullName &&
                        job.groupFullName.indexOf('互联网') != -1
                      )
                  "
                >
                  <div
                    v-if="
                      Boolean(parseInt(job.confirmBtn)) ||
                        !job.confirmShow ||
                        !(
                          job.groupFullName &&
                          job.groupFullName.indexOf('互联网') != -1
                        )
                    "
                  >
                    上线确认书：任务状态到rel、投产窗口是在前12天以内的、小组是互联网应用内的任务
                  </div>
                  <div v-else-if="!isManager">您无权操作</div>
                </fdev-tooltip>
              </div>
              <div
                class="flex1 q-px-md"
                :title="job.direction | direction"
                v-else-if="item.value === 'direction' && job.direction"
              >
                {{ job.direction | direction }}
              </div>
              <div
                class="flex1 q-px-md"
                :title="difficulty[job.difficulty - 1]"
                v-else-if="item.value === 'difficulty'"
              >
                {{ difficulty[job.difficulty - 1] || '-' }}
              </div>
              <div
                class="flex1 q-px-md"
                v-else-if="item.value === 'modify_reason'"
              >
                <span
                  v-if="job.modify_reason"
                  :title="descFilter(job.modify_reason)"
                  v-html="descFilter(job.modify_reason)"
                />
                <span v-else>-</span>
              </div>
              <div class="flex1 q-px-md" v-else-if="item.value === 'tester'">
                <span v-if="includeMe(job.tester)">
                  <a
                    href="xxx/tui/"
                    class="normal-link"
                    target="_blank"
                  >
                    点我
                  </a>
                </span>
                <span v-else title="暂时只对测试人员展示">
                  <span>暂时只对测试人员展示</span>
                </span>
              </div>
              <div class="flex1 q-px-md" :title="job[item.value]" v-else>
                {{ job[item.value] || '-' }}
              </div>
            </div>
          </div>
          <div v-else class="lh18 userStyle">
            <div class="row border-top">
              <div class="col-6 bg-indigogrey-0 px-12 flex items-center">
                所属小组
              </div>
              <div :title="job.groupFullName" class="ellipsis flex1 q-px-md">
                {{ job.groupFullName || '-' }}
              </div>
            </div>
            <div class="row border-top border-bottom">
              <div class="col-6 bg-indigogrey-0 px-12 flex items-center">
                任务描述
              </div>
              <div class="col-6">
                <div
                  :title="job.desc"
                  class="ellipsis-2-lines q-px-md breakALL"
                >
                  {{ job.desc || '-' }}
                </div>
              </div>
            </div>
          </div>
        </f-block>
      </div>
    </div>
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
          tag="span"
          :to="`/user/list/${each.id}`"
          v-for="(each, index) in job.partyB"
          :key="index"
          class="normal-link"
        >
          {{ each.user_name_cn }} </router-link
        >尽快打开上线确认书开关，否则将会影响投产统计和最终的投产；
      </div>
      <div>
        2、上线确认书未到达，请确认是否于{{
          taskRqrmntAlert
        }}投产，如否，请及时联系<router-link
          tag="span"
          :to="`/user/list/${each.id}`"
          v-for="(each, index) in job.partyB"
          :key="index"
          class="normal-link"
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
            align="left"
            required
            label-style="width:auto"
            class="q-mb-md Field-select"
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
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import { required } from 'vuelidate/lib/validators';

import ScanDialog from '@/modules/interface/components/scanDialog';
import SonarDialog from './SonarDialog';

import {
  perform,
  createJobModel,
  rqrmntState,
  difficultyOptions,
  directionOptions,
  fileTypeOptions
} from '@/modules/Job/utils/constants';
import { formatJob, formatMyJob } from '@/modules/Job/utils/utils';
import {
  baseUrl,
  successNotify,
  getIdsFormList,
  validate,
  exportExcel
} from '@/utils/utils';

import { mapState, mapActions, mapGetters } from 'vuex';
import PlanDateUpdateDialog from '@/modules/Job/components/PlanDateUpdateDialog';
import BehindMasterDialog from '@/modules/Job/components/BehindMasterDialog';
import { reviewMap } from '@/modules/Rqr/model';
export default {
  name: 'Profile',
  components: {
    Loading,
    Authorized,
    ScanDialog,
    SonarDialog,
    PlanDateUpdateDialog,
    BehindMasterDialog
  },
  data() {
    return {
      groupList: this.groups,
      ...perform,
      ...rqrmntState,
      taskType: '',
      isTest: '',
      ipmpUnitCloneList: [],
      dataAlert: false,
      assessmentLoading: false,
      planDateUpdateDialogOpen: false,
      job: createJobModel(),
      filter: '',
      nodes: [{ label: '关联文档', children: [] }],
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
      jobStageCode: false,
      role: [],
      canNotModify: false,
      scanDialogOpen: false,
      sonarDialogOpen: false,
      addJobRole: [],
      updateRole: [],
      isDesignManager: false,
      isUpdateRole: Boolean,
      taskManager: false,
      id: '',
      isDisable: false,
      fileType: '1',
      color: 'primary',
      newDoc: [],
      reviewChild: false,
      releaseChild: false,
      handleShow: false,
      commitTabledata: [],
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
      },
      applicationName: '应用',
      userList: [
        {
          label: '开发人员',
          value: 'developer'
        },
        {
          label: '行内负责人',
          value: 'partyA'
        },
        {
          label: '任务负责人',
          value: 'partyB'
        },
        {
          label: '测试人员',
          value: 'tester'
        },
        {
          label: '任务创建人',
          value: 'creator'
        }
      ],
      otherMsg: [
        {
          label: '所属小组',
          value: 'groupFullName'
        },
        {
          label: 'UI设计还原审核状态',
          value: 'review_status'
        },
        {
          label: '上线确认书',
          value: 'confirmShow'
        },
        {
          label: '开发方向',
          value: 'direction'
        },
        {
          label: '任务难度描述',
          value: 'difficulty'
        },
        {
          label: '任务难度修改原因',
          value: 'modify_reason'
        },
        {
          label: '测试管理平台',
          value: 'tester'
        },
        {
          label: '用户故事id',
          value: 'storyId'
        },
        {
          label: 'UAT承接方',
          value: 'uat_testObject'
        }
      ]
    };
  },
  validations: {
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
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    ...mapState('appForm', ['appData', 'sonarScan']),
    ...mapState('jobForm', [
      'taskRqrmntAlert',
      'jobProfile',
      'docDetail',
      'reviewRecordStatus',
      'commitTips',
      'scanProcess',
      'sonarLog'
    ]),
    ...mapState('interfaceForm', ['taskManagerLimit']), // 扫描权限控制，fdev应用不扫描，xxx-web-xxx和vue应用只能当前任务的行内项目负责人，任务负责人，开发人员
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('releaseForm', [
      'taskReview',
      'releaseNodeData',
      'release_node_name'
    ]),
    // 扫描权限控制，只能当前任务的行内项目负责人，任务负责人，开发人员
    isManager() {
      const { partyA, partyB } = this.job;
      return (
        partyA.concat(partyB).some(user => user.id === this.currentUser.id) ||
        this.isKaDianManager
      );
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
    // 应用是否容器化项目
    isJavaTypeApp() {
      return this.job.applicationType && this.job.applicationType === 'appJava';
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
    },
    //是否应用类型
    isApp() {
      const reg = /^app/;
      return reg.test(this.job.applicationType);
    }
  },

  filters: {
    direction(val) {
      const directionObj = directionOptions.find(item => item.value === val);
      let direction = directionObj.label || '-';
      return direction;
    },
    filterReview(val) {
      return reviewMap[val];
    },
    skipFlagFilter(val) {
      return val === '1' ? '是' : '否';
    }
  },
  methods: {
    ...mapActions('user', {
      updateFdevMantis: 'updateFdevMantis',
      updateAssignUser: 'updateAssignUser'
    }),
    ...mapActions('interfaceForm', {
      isTaskManager: 'isTaskManager'
    }),
    ...mapActions('jobForm', [
      'queryTaskRqrmntAlert',
      'queryJobProfile',
      'queryCommitTips',
      'getScanProcess',
      'downloadSonarLog',
      'downExcel',
      'nocodeTask',
      'confirmBtn'
    ]),
    ...mapActions('releaseForm', [
      'queryTaskReview',
      'queryReleaseNodeByJob',
      'taskChangeNotise'
    ]),
    ...mapActions('demandsForm', [
      'queryAvailableIpmpUnit',
      'queryByFdevNoAndDemandId'
    ]),
    formatMyJob,
    closeBookTime() {
      this.job.confirmBtn = '0';
    },
    async updateConfirmBook() {
      await this.confirmBook();
      this.$emit('updateDetail');
      this.confirmBookTimeOpen = false;
    },
    async handleAllTip() {
      this.$v.bookModel.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('bookModel') > -1;
      });
      validate(Keys.map(key => this.$refs[key]));
      if (this.$v.bookModel.$invalid) {
        this.$refs['bookModel.confirmFileDate'].$children[0].validate();
        return;
      }
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
      input = input.toString().replace(/<[^>]+>/g, '');
      const reg = new RegExp(/\n/g);
      return input.toString().replace(reg, '</br>');
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
          path: '/job/list/' + this.otherTaskIdToPush,
          query: { noCode: noCode, tab: 'manage' }
        });
      }
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
    // 根据任务详情渲染页面
    async drewPageByJobProfile() {
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
      let groupFullName = this.job.group.fullName
        .split('-')
        .reverse()
        .join('-');
      this.$set(this.job, 'groupFullName', groupFullName);

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
      this.updateRole = getIdsFormList([
        this.job.creator,
        this.job.master,
        this.job.spdb_master
      ]);
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
      if (this.job.taskType === 1) {
        this.userList = this.userList.filter(item => {
          return item.value !== 'tester';
        });
        this.otherMsg = this.otherMsg.filter(itm => {
          return ['confirmShow', 'review_status'].indexOf(itm.value) < 0;
        });
      }
    },
    async init(val) {
      if (val === 'addBranchFlag') {
        this.jobStageCode = false;
      }
      this.assessmentLoading = true;
      const id = this.$route.params.id;

      if (this.jobProfile.taskType && this.jobProfile.taskType == 1) {
        this.taskType = 1;
      }
      if (this.jobProfile.taskType && this.jobProfile.taskType == 2) {
        this.taskType = 2;
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
    routeTo(id) {
      this.$router.push({ name: 'rqrProfile', params: { id: id } });
    },
    closePlanDateUpdateDialog() {
      this.planDateUpdateDialogOpen = false;
      this.drewPageByJobProfile();
    },
    //获取名称列表
    getName(list) {
      let name = [];
      list.map(item => {
        name.push(item.user_name_cn);
      });
      return name.join(',');
    },
    //跳转骨架 镜像 组件详情
    getPath() {
      let path = '';
      switch (this.job.applicationType) {
        case 'componentWeb':
          // 前端组件
          path = '/componentManage/web/weblist';
          this.applicationName = '组件';
          break;
        case 'componentServer':
          // 后端组件
          path = '/componentManage/server/list';
          this.applicationName = '组件';
          break;
        case 'archetypeWeb':
          // 前端骨架
          path = '/archetypeManage/web/webArchetype';
          this.applicationName = '骨架';
          break;
        case 'archetypeServer':
          // 后端骨架
          path = '/archetypeManage/server/archetype';
          this.applicationName = '骨架';
          break;
        case 'image':
          //镜像
          path = '/imageManage';
          this.applicationName = '镜像';
          break;
        default:
          //默认跳转应用
          path = '/app/list';
          this.applicationName = '应用';
      }
      return path;
    },
    async queryTaskDetail() {
      await this.queryJobProfile({ id: this.id });
      this.drewPageByJobProfile();
    }
  },
  async created() {
    this.id = this.$route.params.id;
    await this.queryTaskRqrmntAlert({ task_id: this.id });
    this.dataAlert = this.taskRqrmntAlert.length > 0 ? true : false;
    this.isTest = this.job.isTest;
  },
  beforeRouteUpdate(to, from, next) {
    next();
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.infoStyle
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  font-weight: 600;
.normal-link
  cursor: pointer;
  color: #0663BE;
.normal-link:hover
  cursor: pointer;
  color: #2196f3;

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

.form
  padding: 0 20px;

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
.time-select
  width 300px
.book-btn
  width 100px
.q-field--with-bottom,
.q-field {
  margin: 0;
  // padding: 5px;
}
.mr10
  margin-right:10px;
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
.bgcolor
  background #f4f6fd
.line
  border-left 1px solid #ccc
.flex1
  flex 1
.iconTitle
  font-weight 600
  font-size 14px
.mb10
  margin-bottom 10px
.mt2
  margin-top 2px
.lh20
  font-size 12px
  line-height 20px
.lh18
  font-size 12px
  line-height 20px
.fw600
  font-weight 600
.breakALL
  word-break break-all
.q-mt-10
  margin-top 10px
.lineh14
  line-height 14px
.wd300
  width 26.1%
.wd226
  width 20%
.wd776
  width 65%
.userStyle
  line-height 32px
.px-12
  padding 0 12px
.wd150
  width 150px
</style>
