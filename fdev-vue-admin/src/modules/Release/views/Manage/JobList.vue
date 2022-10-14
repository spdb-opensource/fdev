<template>
  <div>
    <Loading :visible="loading" class="q-pa-sm">
      <fdev-table
        title="投产任务"
        titleIcon="list_s_f"
        :data="tableData"
        :columns="columns"
        :filter="filterValue"
        :selected.sync="rowSelected"
        row-key="id"
        class="my-sticky-column-table"
        :pagination.sync="pagination"
        :visible-columns="visibleColumns"
        :filter-method="filterMethod"
      >
        <template v-slot:top-bottom>
          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="搜索条件"
          >
            <fdev-select
              :value="selectValue"
              multiple
              use-input
              placeholder="输入关键字，回车区分"
              hide-dropdown-icon
              ref="select"
              @new-value="addSelect"
              @input="updateSelectValue($event)"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="text-primary"
                  @click="setSelect($refs.select)"
                />
              </template>
            </fdev-select>
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="任务阶段"
          >
            <fdev-select
              :value="taskStage"
              :options="taskStageOptions"
              options-dense
              @input="updateTaskStage($event)"
            />
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="类型"
          >
            <fdev-select
              :value="mine"
              :options="mineOptions"
              options-dense
              @input="updateMine($event)"
            />
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            :label="componentReleaseType"
          >
            <fdev-select
              :value="application"
              :options="applicationOptions"
              options-dense
              @input="updateApplication"
            />
          </f-formitem>
          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="合并状态"
          >
            <fdev-select
              v-model="mergeReleaseFlag"
              :options="mergeReleaseOptions"
              options-dense
              emit-value
              map-options
              @input="init"
            />
          </f-formitem>
        </template>

        <template v-slot:body-cell-taskName="props">
          <fdev-td auto-width class="text-ellipsis">
            <router-link
              :title="props.row.task_name"
              :to="{ path: `/job/list/${props.row.task_id}` }"
              class="link"
            >
              <span>{{ props.row.task_name }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.task_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-taskProject="props">
          <fdev-td auto-width class="text-ellipsis">
            <router-link
              :title="props.row.task_project"
              :to="{
                path: `${props.row.routerPath}/${props.row.application_id}`
              }"
              class="link"
            >
              <span>{{ props.row.task_project }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.task_project }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-merge_release_flag="props">
          <fdev-td
            auto-width
            class="text-ellipsis"
            :title="props.row.merge_release_flag | mergeFlagFilter"
          >
            <span>{{ props.row.merge_release_flag | mergeFlagFilter }}</span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-reject_reason="props">
          <fdev-td cclass="text-ellipsis">
            <span :title="props.value">{{ props.value || '-' }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-dev_managers="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.value.map(v => v.user_name_cn).join(',')"
          >
            <span v-for="(item, index) in props.row.dev_managers" :key="index">
              <router-link
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
                v-if="item.id"
              >
                <span>{{ item.user_name_cn }}</span>
              </router-link>
              <span v-else>{{ item.user_name_cn }}</span>
              &nbsp;
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-bank_master="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.value.map(v => v.user_name_cn).join(',')"
          >
            <span v-for="(item, index) in props.row.bank_master" :key="index">
              <router-link
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
                v-if="item.id"
              >
                <span>{{ item.user_name_cn }}</span>
              </router-link>
              <span v-else>{{ item.user_name_cn }}</span>
              &nbsp;
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-name="props">
          <fdev-td class="text-right text-primary">
            <fdev-btn
              flat
              color="primary"
              label="操作"
              v-if="
                props.row.task_stage !== 'production' &&
                  props.row.task_stage !== 'discard' &&
                  props.row.task_stage !== 'file' &&
                  (((props.row.task_status === '0' ||
                    props.row.task_status === '1') &&
                    (compareTime ||
                      (props.row.task_stage === 'rel' &&
                        !confirmHadReleaseTime))) ||
                    !compareTime)
              "
              @click="btnRole(props.row)"
            >
              <fdev-menu>
                <fdev-list>
                  <fdev-item
                    v-if="props.row.task_status === '0' && compareTime"
                    :clickable="isBankManager"
                    @click="handleConfirmUATOpened('1', props.row)"
                    v-close-popup
                    :class="{ 'text-grey-7': !isBankManager }"
                  >
                    <fdev-tooltip anchor="top middle" v-if="!isBankManager">
                      请联系行内项目负责人
                    </fdev-tooltip>
                    <fdev-item-section>确认{{ confirmTip }}</fdev-item-section>
                  </fdev-item>
                  <fdev-item
                    v-if="props.row.task_status === '0' && compareTime"
                    :clickable="isBankManager"
                    @click="handleConfirmModalOpen('2', props.row)"
                    v-close-popup
                    :class="{ 'text-grey-7': !isBankManager }"
                  >
                    <fdev-tooltip anchor="top middle" v-if="!isBankManager">
                      请联系行内项目负责人
                    </fdev-tooltip>
                    <fdev-item-section>拒绝{{ confirmTip }}</fdev-item-section>
                  </fdev-item>

                  <fdev-item
                    v-if="props.row.task_status === '1' && compareTime"
                    :clickable="isBankManager"
                    @click="unbindRelease(props.row.task_id)"
                    v-close-popup
                    :class="{ 'text-grey-7': !isBankManager }"
                  >
                    <fdev-tooltip anchor="top middle" v-if="!isBankManager">
                      请联系行内项目负责人
                    </fdev-tooltip>
                    <fdev-item-section>取消{{ confirmTip }}</fdev-item-section>
                  </fdev-item>

                  <fdev-item
                    v-if="props.row.task_status === '1' && compareTime"
                    :clickable="isBankManager"
                    @click="changeRelease(props.row)"
                    v-close-popup
                    :class="{ 'text-grey-7': !isBankManager }"
                  >
                    <fdev-tooltip anchor="top middle" v-if="!isBankManager">
                      请联系行内项目负责人
                    </fdev-tooltip>
                    <fdev-item-section
                      >更换{{ confirmTip }}窗口</fdev-item-section
                    >
                  </fdev-item>

                  <fdev-item
                    v-if="
                      props.row.task_status === '1' &&
                        props.row.task_stage === 'rel' &&
                        !confirmHadReleaseTime
                    "
                    :clickable="isManager"
                    @click="confirmHadRelease(props.row)"
                    v-close-popup
                    :class="{ 'text-grey-7': !isManager }"
                  >
                    <fdev-tooltip anchor="top middle" v-if="!isManager">
                      请联系行内项目负责人/任务负责人
                    </fdev-tooltip>
                    <fdev-item-section>确认已投产</fdev-item-section>
                  </fdev-item>

                  <fdev-item
                    v-if="!compareTime"
                    :clickable="isManager"
                    @click="
                      abandonedTask(
                        props.row.task_id,
                        props.row.task_name,
                        props.row
                      )
                    "
                    v-close-popup
                    :class="{ 'text-grey-7': !isManager }"
                  >
                    <fdev-tooltip anchor="top middle" v-if="!isManager">
                      请联系行内项目负责人/任务负责人
                    </fdev-tooltip>
                    <fdev-item-section>任务废弃</fdev-item-section>
                  </fdev-item>
                  <fdev-separator />
                </fdev-list>
              </fdev-menu>
            </fdev-btn>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>

    <Loading
      :visible="globalLoading['releaseForm/queryProWantTasks']"
      class="q-pa-sm"
    >
      <fdev-table
        title="投产意向任务"
        titleIcon="list_s_f"
        :data="intentionTableData"
        class="my-sticky-column-table"
        :columns="intentionColumns"
      >
        <template v-slot:body-cell-task_name="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :title="props.value"
              :to="{ path: `/job/list/${props.row.task_id}` }"
              class="link"
            >
              <span>{{ props.value }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-task_project="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :title="props.value"
              :to="{
                path: `${props.row.routerPath}/${props.row.application_id}`
              }"
              class="link"
            >
              <span>{{ props.value }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-dev_managers="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.value.map(v => v.user_name_cn).join(',')"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
                v-if="item.id"
              >
                <span>{{ item.user_name_cn }}</span>
              </router-link>
              <span v-else>{{ item.user_name_cn || '-' }}</span>
              &nbsp;
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-bank_master="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.value.map(v => v.user_name_cn).join(',')"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
                v-if="item.id"
              >
                <span>{{ item.user_name_cn }}</span>
              </router-link>
              <span v-else>{{ item.user_name_cn }}</span>
              &nbsp;
            </span>
          </fdev-td>
        </template>
      </fdev-table>

      <p class="font">
        投产意向任务根据任务创建时的计划投产日期获取，如需修改，请前往修改任务信息。
      </p>
    </Loading>
    <!-- 确认投产 -->
    <f-dialog
      v-model="confirmProductDialog"
      transition-show="slide-up"
      transition-hide="slide-down"
      full-height
      title="确认投产"
    >
      <div>
        <div>
          确认任务<span class="text-negative">{{ selectedTask.task_name }}</span
          >已投产？
        </div>
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

    <f-dialog v-model="confirmModalOpen" :title="tipMsg" persistent>
      <f-formitem label="拒绝原因" diaS v-if="operateType == '2'">
        <fdev-input
          v-model="$v.rejectModel.rejectReason.$model"
          ref="rejectModel.rejectReason"
          autofocus
          :rules="[
            () => !$v.rejectModel.rejectReason.$error || '请输入拒绝原因'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :disable="disabled"
          @click="submit(operateType)"
        />
      </template>
    </f-dialog>

    <f-dialog
      right
      v-model="changeReleaseOpen"
      :title="'更换' + confirmTip + '窗口'"
    >
      <f-formitem diaS :label="confirmTip + '窗口'">
        <fdev-select
          input-debounce="0"
          emit-value
          map-options
          use-input
          option-value="release_node_name"
          :option-label="
            opt =>
              opt ? `${opt.owner_group_name} - ${opt.release_node_name}` : ''
          "
          :options="releaseOptions"
          v-model="params.release_node_name"
          @input="getNodeType"
          @filter="filterRelease"
        />
      </f-formitem>
      <div v-if="changeNodetype === 1" class="q-mt-md">
        <p>
          当前窗口<span class="text-teal">{{ release_node_name }}</span
          >下{{ componentReleaseType
          }}<span class="text-teal">{{ selectedTask.task_project }}</span
          >有多个投产任务，无法直接更换至新窗口！
        </p>
        <p>
          确认后，任务阶段回退至SIT,且需联系内测人员评估是否需要重新进行SIT测试!
        </p>

        <span class="text-red q-my-sm block">风险提示：</span>
        <ol>
          <li class="q-mb-md">
            若开发分支已合并至release分支，请自行评估是否手动回退release分支代码。
          </li>
          <li class="q-mb-md">
            若release分支已合并至master分支，请自行评估是否手动回退master分支代码、删除TAG以及相关镜像。
          </li>
          <li>
            若当前窗口此任务所属{{
              componentReleaseType
            }}只有此投产任务，所属投产{{ componentReleaseType }}会同步删除。
          </li>
        </ol>
      </div>

      <div v-else-if="changeNodetype === 2" class="q-mt-md">
        <p>
          所选窗口<span class="text-teal">{{ params.release_node_name }}</span
          >下已有{{ componentReleaseType
          }}<span class="text-teal">{{ selectedTask.task_project }}</span
          >投产，无法直接更换至新窗口！
        </p>
        <p>
          确认后，任务阶段回退至SIT,且需联系内测人员评估是否需要重新进行SIT测试!
        </p>

        <span class="text-red q-my-md block">风险提示：</span>
        <ol>
          <li class="q-mb-md">
            若开发分支已合并至release分支，请自行评估是否手动回退release分支代码。
          </li>
          <li class="q-mb-md">
            若release分支已合并至master分支，请自行评估是否手动回退master分支代码、删除TAG以及相关镜像。
          </li>
          <li>
            若当前窗口此任务所属{{
              componentReleaseType
            }}只有此投产任务，所属投产{{ componentReleaseType }}会同步删除。
          </li>
        </ol>
      </div>

      <div v-else class="q-mt-md">
        <span class="text-red q-my-sm block">风险提示：</span>
        <ol v-if="!releaseDetail.type || releaseDetail.type !== '3'">
          <li class="q-mb-md">
            此窗口投产任务及投产{{ componentReleaseType }}将删除 。
          </li>
          <li>
            新窗口中release分支（release-{{
              params.release_node_name
            }}）将从当前release分支（release-{{
              $route.params.id
            }}）拉取，无需重新进行uat测试 。
          </li>
        </ol>
        <ol v-else>
          <li class="q-mb-md">
            此窗口试运行任务将删除 。
          </li>
          <li>
            新窗口中testrun分支（testrun-{{
              params.release_node_name
            }}）将从当前testrun分支（testrun-{{ $route.params.id }}）拉取。
          </li>
        </ol>
      </div>
      <f-formitem diaS label="已知晓风险">
        <fdev-checkbox v-model="confirmRisk" />
      </f-formitem>
      <template v-slot:btnSlot>
        <div>
          <fdev-btn
            dialog
            label="确定"
            :disable="!confirmRisk || !params.release_node_name"
            :loading="globalLoading['releaseForm/changeReleaseNode']"
            @click="handleChangeReleaseNodeOpen"
          />
          <fdev-tooltip v-if="!confirmRisk || !params.release_node_name">
            {{ disableText }}
          </fdev-tooltip>
        </div>
      </template>
    </f-dialog>

    <f-dialog right v-model="riskAlertOpen" title="风险提示">
      <fdev-layout view="Lhh lpR fff" container class="dialog-height">
        <fdev-page-container>
          <fdev-page padding>
            <div v-if="!releaseDetail.type || releaseDetail.type !== '3'">
              <p class="q-ml-sm">
                1、若开发分支已合并至release分支，请自行评估是否手动回退release分支代码。
              </p>
              <p class="q-ml-sm">
                2、若release分支已合并至master分支，请自行评估是否手动回退master分支代码、删除TAG以及相关镜像。
              </p>
              <p class="q-ml-sm">
                3、相关投产{{ componentReleaseType }}会同步删除。
              </p>
            </div>
            <div v-else>
              <p class="q-ml-sm">
                若开发分支已合并至testrun分支，请自行评估是否手动回退testrun分支代码。
              </p>
            </div>
            <fdev-checkbox v-model="confirmRisk" label="已知晓风险" />
          </fdev-page>
        </fdev-page-container>
      </fdev-layout>
      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          dialog
          :disable="!confirmRisk"
          :loading="globalLoading['releaseForm/deleteTask']"
          @click="resolve('确定')"
        />
      </template>
    </f-dialog>

    <AbandonedTaskDialog
      v-model="AbandonedTaskDialogOpened"
      :taskName="abandonedTaskName"
      :id="taskId"
      :release_node_name="release_node_name"
      @complete="init"
    />
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import {
  successNotify,
  validate,
  isValidReleaseDate,
  getGroupFullName
} from '@/utils/utils';
import {
  jobListColumns,
  intentionJobListColumns,
  mergeReleaseOptions
} from '../../utils/constants';
import { getSelector } from '../../utils/setting';
import { taskStage } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import moment from 'moment';
import AbandonedTaskDialog from '@/modules/Job/components/AbandonedTaskDialog';
export default {
  name: 'JobList',
  components: { Loading, AbandonedTaskDialog },
  data() {
    return {
      confirmProductDialog: false,
      isToFile: true, // 是否归档
      mergeReleaseFlag: '',
      mergeReleaseOptions: mergeReleaseOptions,
      loading: true,
      pagination: {
        rowsPerPage: 5
      },
      rowSelected: [],
      selector: getSelector() || [],
      applicationOptions: ['全部'],
      mineOptions: ['全部', '与我相关'],
      tableData: [],
      compareTime: false,
      disabled: false,
      tipMsg: '拒绝原因',
      operateType: '1',
      rejectModel: {
        rejectReason: ''
      },
      confirmModalOpen: false,
      detail: {}, // 拒绝时拒绝详情
      changeReleaseOpen: false,
      params: {
        release_node_name: ''
      },
      riskAlertOpen: false,
      resolve: null,
      reject: null,
      confirmRisk: false,
      uat_testObject: '',
      releaseOptions: [],
      applicationId: '',
      changeNodetype: null,
      release_node_name: null,
      selectedTask: {},
      isBankManager: false,
      isManager: false,
      AbandonedTaskDialogOpened: false,
      abandonedTaskName: '',
      taskId: '',
      intentionTableData: []
    };
  },
  validations: {
    rejectModel: {
      rejectReason: {
        required
      }
    }
  },
  filters: {
    mergeFlagFilter(val) {
      return val === '' ? '-' : val ? '已合并' : '未合并';
    }
  },
  watch: {
    pagination(val) {
      this.updateCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    },
    computedFilter: {
      immediate: true,
      handler(val) {
        if (val) {
          this.updateTerms(val);
        }
      }
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/releaseJobList', [
      'selectValue',
      'taskStage',
      'mine',
      'application',
      'terms',
      'visibleColumns',
      'currentPage'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('userForm', ['groups']),
    ...mapState('releaseForm', [
      'releaseList',
      'taskList',
      'applyList',
      'componentList',
      'releaseDetail',
      'wantToReleaseTasks'
    ]),
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
          return '应用';
      }
    },
    columns() {
      return jobListColumns(this.componentReleaseType);
    },
    intentionColumns() {
      return intentionJobListColumns(this.componentReleaseType);
    },
    filterValue() {
      return this.terms;
    },
    disableText() {
      return !this.params.release_node_name
        ? '当前无试运行窗口'
        : !this.confirmRisk
        ? '请勾选以知晓风险'
        : '';
    },
    computedFilter() {
      return (
        this.selectValue +
        ',' +
        this.mine +
        ',' +
        this.taskStage +
        ',' +
        this.application
      );
    },
    releaseListFilter() {
      return this.releaseList.filter(
        v => v.release_node_name !== this.release_node_name
      );
    },
    confirmHadReleaseTime() {
      return isValidReleaseDate(this.$route.params.id.split('_')[0], null, 0);
    },
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    },
    taskStageOptions() {
      const options = Object.values(taskStage).slice(4);
      return ['全部', ...options];
    },
    confirmTip() {
      return this.releaseDetail.type && this.releaseDetail.type === '3'
        ? '试运行'
        : '投产';
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/releaseJobList', [
      'updateSelectValue',
      'updateTaskStage',
      'updateMine',
      'updateApplication',
      'updateTerms',
      'updateVisibleColumns',
      'updateCurrentPage'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('jobForm', ['updateJobStage']),

    ...mapActions('releaseForm', [
      'changeReleaseNode',
      'updateTaskArchived',
      'deleteTask',
      'queryRelease',
      'queryTasks',
      'auditAdd',
      'queryApply',
      'queryComponent',
      'queryReleaseNodeDetail',
      'queryProWantTasks'
    ]),
    /* 小组名 */
    optinons() {
      this.tableData.forEach((item, index) => {
        if (
          this.applicationOptions.indexOf(item.task_project) < 0 &&
          item.task_project
        ) {
          this.applicationOptions.push(item.task_project);
        }
        let groupFullName = getGroupFullName(this.groups, item.task_group_id);
        this.$set(
          this.tableData[index],
          'routerPath',
          this.handlecomponentName(item)
        );
        this.$set(this.tableData[index], 'groupFullName', groupFullName);
      });
    },
    /* 获取组件/骨架/镜像路由 */
    handlecomponentName(item) {
      let componentReleasePath = '';
      switch (item.applicationType) {
        case 'componentServer':
          // 后端组件
          componentReleasePath = '/componentManage/server/list';
          break;
        case 'componentWeb':
          // 前端组件
          componentReleasePath = '/componentManage/web/weblist';
          break;
        case 'archetypeServer':
          // 后端骨架
          componentReleasePath = '/archetypeManage/server/archetype';
          break;
        case 'archetypeWeb':
          // 前端骨架
          componentReleasePath = '/archetypeManage/web/webArchetype';
          break;
        case 'image':
          //镜像
          componentReleasePath = '/imageManage';
          break;
        default:
          // 应用
          componentReleasePath = '/app/list';
      }
      return componentReleasePath;
    },
    /* 打开确认操作modal */
    handleConfirmModalOpen(num, data) {
      this.confirmModalOpen = true;
      this.detail = data;
      this.operateType = num;
      this.tipMsg = '拒绝原因';
    },
    /* 点击确认投产 */
    handleConfirmUATOpened(num, data) {
      this.detail = data;
      if (!this.releaseDetail.type || this.releaseDetail.type !== '3') {
        this.$q
          .dialog({
            title: '请选择UAT测试的承接方',
            options: {
              model: '业务部门',
              type: 'radio',
              items: [
                { label: '业务部门', value: '业务部门' },
                { label: '测试中心', value: '测试中心' },
                { label: '其他', value: '其他' }
              ]
            },
            cancel: true,
            ok: {
              label: '确认投产'
            }
          })
          .onOk(res => {
            this.uat_testObject = res;
            this.submit('1');
          });
      } else {
        this.submit('1');
      }
    },
    /* confirm框提交提交 */
    submit(type) {
      if (type == '2') {
        this.$v.rejectModel.$touch();
        let jobKeys = Object.keys(this.$refs).filter(
          key => key.indexOf('rejectModel') > -1
        );
        validate(jobKeys.map(key => this.$refs[key]));
        this.disabled = false;
        if (this.$v.rejectModel.$invalid) {
          return;
        }
        this.operate('2', this.detail);
      } else {
        this.disabled = true;
        this.operate('1', this.detail);
      }
    },
    async operate(num, data) {
      let params = {
        release_node_name: this.$route.params.id,
        task_id: data.task_id,
        operation_type: num,
        source_branch: '',
        uat_testObject: this.uat_testObject
      };
      if (num == 2) {
        params.reject_reason = this.rejectModel.rejectReason;
      }
      this.disabled = false;
      this.confirmModalOpen = false;
      await this.auditAdd(params);
      this.rejectModel.rejectReason = '';
      if (!this.releaseDetail.type || this.releaseDetail.type !== '3') {
        let msg = num == 1 ? '已确认投产' : '已拒绝投产';
        successNotify(msg);
      } else {
        successNotify('已确认');
      }
      this.init();
    },
    async init() {
      this.loading = true;
      let params = {
        release_node_name: this.$route.params.id,
        type: this.releaseDetail.type,
        merge_release_flag: this.mergeReleaseFlag
      };
      this.queryProWantTasks(params).then(() => {
        this.intentionTableData = this.wantToReleaseTasks;
        this.intentionTableData.forEach((item, index) => {
          this.$set(
            this.intentionTableData[index],
            'routerPath',
            this.handlecomponentName(item)
          );
        });
      });
      await this.queryTasks(params);
      this.tableData = this.taskList;
      this.optinons();
      this.loading = false;
    },
    // 打开审核窗口
    async openExamine(id) {
      this.examine = true;
      this.examineData = {};
      await this.queryTaskReview({ ids: [id] });
      this.examineData = this.taskReview.taskList[0];
    },
    /* 取消投产 */
    async unbindRelease(task_id) {
      this.confirmRisk = false;
      await this.handleRiskAlertOpen();
      let params = {
        task_id: task_id
      };
      if (this.releaseDetail.type && this.releaseDetail.type >= 3) {
        params.type = this.releaseDetail.type;
      }
      await this.deleteTask(params);
      this.init();
      successNotify('投产已取消');
      this.riskAlertOpen = false;
    },
    /* 确认已投产 */
    async confirmProduct() {
      let params = {
        task_id: this.selectedTask.task_id
      };
      if (this.releaseDetail.type && this.releaseDetail.type >= 4) {
        params.type = this.releaseDetail.type;
      }
      await this.updateTaskArchived(params);
      successNotify('确认已投产');
      this.confirmProductDialog = false;
      if (this.isToFile) {
        // 归档
        this.keepInFile();
      } else {
        // 不归档
        this.init();
      }
    },
    async keepInFile() {
      await this.updateJobStage({
        id: this.selectedTask.task_id,
        stage: 'file'
      });
      successNotify('任务已归档');
      this.init();
    },

    confirmHadRelease(data) {
      this.selectedTask = data;
      this.confirmProductDialog = true;
    },
    /* 更换投产窗口 */
    async changeRelease(data) {
      const taskId = data.task_id;
      this.confirmRisk = false;
      this.selectedTask = data;
      await this.queryRelease({
        start_date: moment(new Date()).format('YYYY-MM-DD'),
        type: this.releaseDetail.type ? this.releaseDetail.type : '1'
      });
      this.releaseOptions = this.releaseListFilter;
      const defaultRelease = this.releaseListFilter.find(item => {
        return item.owner_groupId === this.currentUser.group_id;
      });
      const finalRelease = defaultRelease || this.releaseOptions[0];
      this.params = {
        release_node_name: finalRelease ? finalRelease.release_node_name : '',
        task_id: taskId
      };

      /* 当前选择的任务下的应用/组件/骨架/镜像，只有一条任务时，发查询接口 */
      this.applicationId = data.application_id;
      this.changeReleaseOpen = true;

      if (this.params.release_node_name) {
        this.getNodeType();
      }
    },
    async handleChangeReleaseNodeOpen() {
      await this.changeReleaseNode(this.params);
      this.changeReleaseOpen = false;
      this.init();
      successNotify('更换成功');
    },
    handleRiskAlertOpen() {
      this.riskAlertOpen = true;
      return new Promise((resolve, reject) => {
        this.resolve = resolve;
        this.reject = reject;
      });
    },
    /* 
      1、投产审核拒绝(2)、已投产(6)，不显示
      2、投产待审核时(0)，只能是行内项目负责人
      3、其他状态时，是行内项目负责人和应用负责人
      4、取消投产，只能时行内负责人 
    */
    btnRole(item) {
      this.isBankManager =
        item.bank_master.some(user => user.id === this.currentUser.id) ||
        this.isKaDianManager;
      const isTaskManager = item.dev_managers.some(
        user => user.id === this.currentUser.id
      );
      this.isManager = this.isBankManager || isTaskManager;
    },
    /* 投产窗口搜索 */
    filterRelease(val, update, abort) {
      update(() => {
        const needle = val.trim().toLowerCase();
        this.releaseOptions = this.releaseListFilter.filter(v => {
          return (
            v.release_node_name.toLowerCase().indexOf(needle) > -1 ||
            v.owner_group_name.toLowerCase().indexOf(needle) > -1
          );
        });
      });
    },
    async getNodeType(release_node_name) {
      this.confirmRisk = false;
      /* 所选任务的应用，在当前窗口下的任务数量 */
      const taskNum = this.tableData.filter(
        item => item.application_id === this.applicationId
      ).length;
      /* 多个任务 */
      if (taskNum > 1) {
        this.changeNodetype = 1;
        return;
      }
      /* 查询任务所属投产应用/组件，在当前窗口的投产任务数量 */
      let existApp;
      if (this.releaseDetail.type <= 3) {
        // 窗口类型为微服务，原生，试运行时，请求应用接口
        await this.queryApply({
          release_node_name: release_node_name
            ? release_node_name
            : this.params.release_node_name
        });
        /* 如果没有当前应用 ，则改提示语*/
        existApp = this.applyList.some(
          item => item.application_id === this.applicationId
        );
      } else {
        // 窗口类型为组件，骨架，镜像时，请求组件接口
        await this.queryComponent({
          release_node_name: release_node_name
            ? release_node_name
            : this.params.release_node_name,
          type: this.releaseDetail.type
        });
        /* 如果没有当前组件 ，则改提示语*/
        existApp = this.componentList.some(
          item => item.component_id === this.applicationId
        );
      }
      if (existApp) {
        this.changeNodetype = 2;
      } else {
        this.changeNodetype = 3;
      }
    },
    /* 
    1、task_status = 1||0，
    2、投产窗口时间：
      a、未过期时显示；
      b、过期后，task_stage为rel时显示
     */
    operationBtnDisplay(item) {
      const status = item.task_status;
      const stage = item.task_stage;

      if (status !== '1' && status !== '0') {
        return false;
      }

      if (!this.compareTime && stage !== 'rel') {
        return false;
      }

      return true;
    },
    /* 任务废弃 */
    abandonedTask(id, name, row) {
      this.AbandonedTaskDialogOpened = true;
      this.abandonedTaskName = name;
      this.taskId = id;
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    filterMethod(rows, terms, cols, getCellValue) {
      terms = terms.toLowerCase().split(',');
      terms = terms.slice(0, terms.length - 3);
      const data = rows.filter(row => {
        const name = this.currentUser.user_name_cn;
        if (
          this.mine !== '全部' &&
          (!JSON.stringify(row['dev_managers']).includes(name) &&
            !JSON.stringify(row['bank_master']).includes(name))
        ) {
          return false;
        }
        if (
          this.taskStage !== '全部' &&
          taskStage[row.task_stage] !== this.taskStage
        ) {
          return false;
        }
        if (
          this.application !== '全部' &&
          row.task_project !== this.application
        ) {
          return false;
        }
        if (terms.length === 1 && !terms[0]) return true;
        return terms.every(term =>
          cols.some(col =>
            (JSON.stringify(getCellValue(col, row)) + '')
              .toLowerCase()
              .includes(term.trim())
          )
        );
      });
      return data;
    }
  },
  async created() {
    this.init();
    this.release_node_name = this.$route.params.id;
    this.compareTime = isValidReleaseDate(
      this.$route.params.id.split('_')[0],
      null,
      1
    );
  },
  mounted() {
    this.pagination = this.currentPage;
  }
};
</script>

<style lang="stylus" scoped>
.dialog-height
  height 550px!important
ol
  padding-left 28px
.font
  color #757575
  font-size 12px
  margin 0
</style>
