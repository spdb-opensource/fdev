<template>
  <div>
    <Loading :visible="loading">
      <fdev-table
        titleIcon="list_s_f"
        :data="applylist"
        :pagination.sync="paginationTable"
        :columns="tableColumns"
        hide-bottom
        :filter="filterValue"
        :filter-method="filterMethods"
        title="试运行应用列表"
        grid
        noExport
        no-select-cols
      >
        <template v-slot:top-bottom>
          <f-formitem class="col-4" bottom-page label="搜索条件">
            <fdev-select
              v-model="selectValue"
              multiple
              use-input
              hide-dropdown-icon
              ref="select"
              @new-value="addSelect"
              class="inline-block"
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

          <f-formitem class="col-4" label="" bottom-page>
            <span class="text-grey-6"> 检索到{{ filterLength }}个应用 </span>
          </f-formitem>

          <f-formitem class="col-4" label="" bottom-page>
            <span class="text-grey-6"> 共{{ applylist.length }}个应用 </span>
          </f-formitem>
        </template>

        <template v-slot:item="props">
          <div class="col-4 q-pa-sm">
            <fdev-card
              square
              :class="props.row.new_add_sign === '0' ? 'div-border' : ''"
            >
              <fdev-card-section class="row no-wrap items-start">
                <div class="col">
                  <router-link
                    :to="`/app/list/${props.row.application_id}`"
                    class="link"
                  >
                    <div class="text-h6 inline-block">
                      {{ props.row.app_name_en }}
                    </div>
                  </router-link>
                  <fdev-btn
                    flat
                    label="faketime镜像"
                    @click="openFakeDiolag(props.row)"
                    class="float-right"
                    v-if="
                      isBankMaster(
                        props.row.app_dev_managers.concat(
                          props.row.app_spdb_managers
                        )
                      ) && compareTime
                    "
                  />
                </div>
              </fdev-card-section>
              <fdev-card-section>
                <div class="text-grey-9">
                  <div class="q-mb-sm row">
                    <div class="col-md-4 col-sm-5">
                      应用名称
                    </div>
                    <div
                      v-if="props.row.app_name_zh"
                      class="col-md-8 col-sm-7 link ellipsis"
                    >
                      <router-link
                        :title="props.row.app_name_zh"
                        :to="`/app/list/${props.row.application_id}`"
                        class="link"
                      >
                        {{ props.row.app_name_zh }}
                      </router-link>
                    </div>
                  </div>
                  <div class="q-mb-sm row">
                    <div class="col-md-4 col-sm-5">
                      行内项目负责人
                    </div>
                    <div
                      class="col-md-8 col-sm-7 ellipsis"
                      v-if="props.row.app_name_zh"
                      :title="
                        props.row.app_spdb_managers
                          .map(v => v.user_name_cn)
                          .join(',')
                      "
                    >
                      <span
                        v-for="(user, index) in props.row.app_spdb_managers"
                        :key="index"
                      >
                        <router-link
                          :to="`/user/list/${user.user_id}`"
                          class="col-md-9 col-sm-8 link ellipsis"
                        >
                          {{ user.user_name_cn }}
                        </router-link>
                      </span>
                    </div>
                  </div>
                  <div class="q-mb-sm row">
                    <div class="col-md-4 col-sm-5">
                      应用负责人
                    </div>
                    <div
                      class="col-md-8 col-sm-7 ellipsis"
                      v-if="props.row.app_name_zh"
                      :title="
                        props.row.app_dev_managers
                          .map(v => v.user_name_cn)
                          .join(',')
                      "
                    >
                      <span
                        v-for="(user, index) in props.row.app_dev_managers"
                        :key="index"
                      >
                        <router-link
                          :to="`/user/list/${user.user_id}`"
                          class="col-md-9 col-sm-8 link ellipsis"
                        >
                          {{ user.user_name_cn }}
                        </router-link>
                      </span>
                    </div>
                  </div>
                  <div class="q-mb-sm row">
                    <div class="col-md-4 col-sm-5">
                      应用类型
                    </div>
                    <div
                      class="col-md-8 col-sm-7 text-grey-8 overflow"
                      :title="props.row.type_name"
                    >
                      {{ props.row.type_name }}
                    </div>
                  </div>
                  <div class="q-mb-sm row">
                    <div class="col-md-4 col-sm-5">
                      testrun分支
                    </div>
                    <div
                      class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                      :title="props.row.release_branch"
                    >
                      {{ props.row.release_branch }}
                    </div>
                  </div>
                </div>
              </fdev-card-section>

              <fdev-separator
                v-if="
                  (isBankMaster(
                    props.row.app_dev_managers.concat(
                      props.row.app_spdb_managers
                    )
                  ) ||
                    isKaDianManager) &&
                    compareTime
                "
              />
              <div class="q-px-sm q-pb-sm">
                <div
                  v-if="
                    (isBankMaster(
                      props.row.app_dev_managers.concat(
                        props.row.app_spdb_managers
                      )
                    ) ||
                      isKaDianManager) &&
                      compareTime
                  "
                >
                  <fdev-btn
                    flat
                    class="q-mt-sm q-mr-sm"
                    label="试运行发布"
                    v-if="
                      props.row.type_name &&
                        (props.row.type_name.toLowerCase().includes('ios') ||
                          props.row.type_name.toLowerCase().includes('android'))
                    "
                    @click="handleIOSOrAndroidPublish(props.row)"
                  />
                  <fdev-btn
                    flat
                    label="CI/CD"
                    @click="handlePipelineOpen(props.row)"
                    class="q-mt-sm"
                  />
                </div>
              </div>
            </fdev-card>
          </div>
        </template>
      </fdev-table>
    </Loading>

    <f-dialog right v-model="pipelinesOpen" v-if="pipelinesOpen" title="CI/CD">
      <Pipelines
        v-show="!jobOpen"
        :rowNumber="5"
        :application_id="application_id"
        :release_node_name="release_node_name"
        @job="handleJobOpen"
      />
      <Job class="q-mt-lg" v-if="jobOpen" :project_id="params" />
      <template v-slot:btnSlot>
        <fdev-btn
          v-show="jobOpen"
          label="返回"
          dialog
          @click="jobOpen = false"
        />
      </template>
    </f-dialog>

    <!-- 点击准生产发布的时候弹框选择是否为特殊测试包，以及是否加固 -->
    <f-dialog v-model="testRunPublish" persistent title="是否为特殊测试包？">
      <f-formitem label="是否为特殊测试包？" diaS>
        <fdev-select
          emit-value
          map-options
          v-model="testRunPublishType"
          option-value="id"
          option-label="testRunPublishType"
          :options="testRunPublishTypeOptions"
        />
      </f-formitem>
      <f-formitem label="是否加固？" diaS>
        <fdev-radio val="1" v-model="reinforce" label="是" class="q-mr-lg" />
        <fdev-radio val="0" v-model="reinforce" label="否" />
      </f-formitem>

      <template v-slot:btnSlot>
        <fdev-btn label="取消" dialog @click="testRunPublish = false" />
        <fdev-btn
          label="确定"
          dialog
          @click="goTestRunPublish"
          :loading="globalLoading['releaseForm/iOSOrAndroidAppPublish']"
        />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { testEnv } from '../../utils/model';
import { mapState, mapGetters, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import Pipelines from '@/modules/App/components/pipelines';
import Job from '@/modules/App/views/pipelines/jobs';
import { successNotify, validate, deepClone, errorNotify } from '@/utils/utils';
import {
  setSelector,
  getSelector,
  setPagination,
  getPagination
} from '../../utils/setting';
import {
  taskListColumn,
  appListColumn,
  taskColumns,
  runTaskColumns
} from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'JobList',
  components: { Loading, Pipelines, Job },
  data() {
    return {
      loading: true,
      idInfo: {},
      pagination: getPagination(),
      userOptions: [],
      filter: '',
      rowSelected: [],
      tableGroup: '',
      tableGroups: [],
      selector: getSelector() || [],
      selection: 'multiple',
      terms: [],
      applylist: [],
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
        tag_version: ''
      },
      examine: false, // 审核窗口
      examineData: [],
      confirmEnv: false,
      columns: taskListColumn,
      cloneData: {},
      testReason: [
        { label: '正常', value: '1' },
        { label: '需求变更', value: '3' }
      ],
      testData: {
        id: [],
        desc: '',
        stage: '2', // 1.SIT  2.REL
        reason: '1' // 1.正常  2.缺陷  3. 需求变更
      },
      selectEnvDialogOpen: false,
      testEnv: testEnv(),
      release_branch: '',
      releaseNodeName: '',
      pipelinesOpen: false,
      jobOpen: false,
      params: {},
      tableColumns: appListColumn,
      selectValue: [],
      rowData: {},
      filterValue: '',
      paginationTable: {
        rowsPerPage: 0
      },
      filterLength: 0,
      testRuningDialogOpened: false,
      selectedTask: [],
      appTaskListLoading: false,
      taskColumns: taskColumns,
      runTaskColumns: runTaskColumns,
      testrun_applications: [],
      testrun_id: '',
      testrunApplicationsOptions: [],
      runTaskListLoading: false,
      isClient: false,
      confirmConfigOpen: false,
      checkData: '',
      network: '',
      fakeTimeDialog: false,
      imageNameOptions: [],
      imageModel: { fakeImageName: '', fakeImageVersion: '', fake_flag: '0' },
      imageVersionOptions: [],
      appSelected: {},
      testRunPublish: false,
      testRunPublishTypeOptions: [
        { testRunPublishType: '否', id: 'testrun' },
        { testRunPublishType: '兼容性测试包', id: 'jrcs' },
        { testRunPublishType: '自动化测试包', id: 'auto' },
        { testRunPublishType: '压测包', id: 'yace ' }
      ],
      testRunPublishType: 'testrun',
      reinforce: '0'
    };
  },
  validations: {
    publishModel: {
      description: {
        required
      },
      tag_version: {
        require(val) {
          if (this.isClient) {
            return val.trim();
          }
          return true;
        },
        format(val) {
          if (!val) return true;
          const reg = /^(?!\.)([a-zA-Z0-9.]*)$/;
          return reg.test(val);
        }
      }
    },
    testData: {
      reason: {
        required
      },
      desc: {
        required
      }
    },
    imageModel: {
      fakeImageName: {
        required
      },
      fakeImageVersion: {
        required
      }
    }
  },
  filters: {
    devops_status_Filter: function(input) {
      let json = {
        '0': '未合并',
        '1': '已发起merge request,待合并',
        '2': 'merge request发起失败',
        '3': '合并完成,待拉取product tag',
        '4': '拒绝合并',
        '5': '已拉取product tag',
        '6': '已打包'
      };
      return json[input];
    },
    filterComfirm(val) {
      return val === '1' ? '已确认' : '未确认';
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selector(val) {
      setSelector(val);
    },
    terms(val, oldVal) {
      this.filter = val.toString();
    },
    selectEnvDialogOpen(val) {
      if (val === false) {
        this.testEnv = testEnv();
      }
    },
    pipelinesOpen(val) {
      this.jobOpen = false;
    },
    selectValue(val) {
      this.filterValue = val.toString().toLowerCase();
      if (val.length === 0) {
        this.filterValue = ',';
      }
    }
  },
  computed: {
    ...mapState('userForm', [
      'groups',
      'companies',
      'roles',
      'isLeaves',
      'tags'
    ]),
    ...mapState('authorized', {
      auths: 'list'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('releaseForm', [
      'compareTime',
      'labelsFuzzy',
      'applyList',
      'examineList',
      'tagsList',
      'tasksOfAppType',
      'testRunBranch',
      'appTaskList',
      'taskByTestRunId'
    ]),
    ...mapGetters('userForm', ['wrapTotal']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('appForm', ['vueAppData']),
    // ...mapState('environmentForm', ['selectAppEnv']),
    ...mapState('componentForm', ['imageManageList', 'imageRecordList']),
    ...mapState('releaseForm', ['FakeInfoData', 'releaseDetail'])
  },
  methods: {
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('appForm', ['queryApps']),
    ...mapActions('releaseForm', {
      queryByLabelsFuzzy: 'queryByLabelsFuzzy',
      setTestEnvs: 'setTestEnvs',
      queryApply: 'queryApply',
      packageFromTag: 'packageFromTag',
      relDevops: 'relDevops',
      queryTasksReviews: 'queryTasksReviews',
      queryApplicationTags: 'queryApplicationTags',
      sendEmailForTaskManagers: 'sendEmailForTaskManagers',
      tasksInSitStage: 'tasksInSitStage',
      createTestrunBranch: 'createTestrunBranch',
      mergeTaskBranch: 'mergeTaskBranch',
      queryTaskByTestRunId: 'queryTaskByTestRunId',
      queryNotinlineTasksByAppId: 'queryNotinlineTasksByAppId',
      changeConfirmConf: 'changeConfirmConf',
      iOSOrAndroidAppPublish: 'iOSOrAndroidAppPublish'
    }),
    // ...mapActions('environmentForm', ['queryProEnvByAppId']),
    ...mapActions('componentForm', ['queryBaseImage', 'queryBaseImageRecord']),
    ...mapActions('releaseForm', ['editFakeInfo']),
    handleIOSOrAndroidPublish(row) {
      this.rowData = row;
      this.testRunPublish = true;
    },
    // 点击dialog内的确定按钮
    async goTestRunPublish() {
      await this.iOSOrAndroidAppPublish({
        release_node_name: this.releaseDetail.release_node_name,
        is_reinforce: this.reinforce,
        env: this.testRunPublishType,
        id: this.rowData.application_id,
        type: 'testrun'
      });
      successNotify('试运行发布成功！');
      this.testRunPublish = false;
    },
    // 打开faketime弹窗,映射之前的基础镜像值（如有）
    async openFakeDiolag(val) {
      this.appSelected = val;
      this.fakeTimeDialog = true;
      this.imageModel.fake_flag = val.fake_flag ? val.fake_flag : '0';
      (this.imageModel.fakeImageVersion = val.fake_image_version),
        await this.queryBaseImage();
      this.imageNameOptions = this.imageManageList;
      let ImageName = this.imageManageList.filter(item => {
        if (item.name === val.fake_image_name) {
          return item.name_cn;
        }
      });
      this.imageModel.fakeImageName = ImageName[0];
    },
    imageNameFilter(val, update) {
      update(() => {
        this.imageNameOptions = this.imageManageList.filter(
          tag =>
            tag.name_cn.indexOf(val) > -1 ||
            tag.name.includes(val.toLowerCase())
        );
      });
    },
    imageVersionFilter(val, update) {
      update(() => {
        this.imageVersionOptions = this.imageRecordList.filter(tag =>
          tag.image_tag.includes(val.toLowerCase())
        );
      });
    },

    async imageNameTouch(val) {
      await this.queryBaseImageRecord({
        name: this.imageModel.fakeImageName.name
      });
      this.imageModel.fakeImageVersion = '';
      this.imageVersionOptions = this.imageRecordList;
    },
    // faketime 必输校验
    handleFakeFlagAllTip() {
      this.$v.imageModel.$touch();
      // 获取要校验的dom并传入validate，此步骤要求每个ref唯一
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('imageModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      // 只要有校验未通过的，$invalid会被validate设为true
      if (this.$v.imageModel.$invalid) {
        return;
      }
    },
    async handleFakeFlag() {
      await this.editFakeInfo({
        release_node_name: this.appSelected.release_node_name,
        application_id: this.appSelected.application_id,
        fake_flag: this.imageModel.fake_flag,
        fake_image_name: this.imageModel.fakeImageName.name || '',
        fake_image_version: this.imageModel.fakeImageVersion.image_tag
          ? this.imageModel.fakeImageVersion.image_tag
          : this.imageModel.fakeImageVersion
      });
      successNotify(this.FakeInfoData);
      this.init();
      this.fakeTimeDialog = false;
    },
    // 清理弹窗信息
    clear() {
      this.publishModel.description = '';
      this.publishModel.tag_version = '';
    },
    /* 打开确认操作modal */
    async handleConfirmModalOpen(num, data) {
      this.application_id = data.application_id;
      const { app_name_en } = data;
      await this.queryApps({ name_en: app_name_en });
      // if (
      //   this.vueAppData[0] &&
      //   this.vueAppData[0].label.indexOf('不涉及环境部署') < 0
      // ) {
      // 包含 '不涉及环境部署'字段,正常流程,不包含字段 继续判断
      //   await this.queryProEnvByAppId({ app_id: this.application_id });
      //   if (this.selectAppEnv.length === 0) {
      //     this.confirmEnv = true;
      //     return;
      //   }
      // }
      this.isClient = false;
      this.clear();
      const params = {
        application_id: data.application_id,
        release_node_name: data.release_node_name
      };
      if (data.type_name) {
        const type_name = data.type_name.toLowerCase();
        this.isClient =
          type_name.includes('ios') || type_name.includes('android');
      }
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
        if (!this.tasksOfAppType.flag) {
          errorNotify(
            `"${
              this.tasksOfAppType.task_name
            }"任务尚未进入UAT阶段，无法进行生产打包`
          );
          return;
        }
      }
      this.confirmModalOpen = true;
      this.product_tag = data.product_tag[0];
      this.detail = data;
      this.operateType = num;
      this.tipMsg =
        num == '0'
          ? '输入本次合并请求提交信息'
          : '请选择已有tag分支重新进行打包操作';
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
    async operate(msg, status) {
      if (msg === '0') {
        let params = {
          application_id: status.application_id,
          release_node_name: this.$route.params.id,
          description: this.publishModel.description,
          tag_version: this.publishModel.tag_version
        };
        this.confirmModalOpen = false;
        this.disabled = false;
        await this.relDevops(params);
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
        this.disabled = false;
        await this.packageFromTag(params);
        successNotify('正在进行tag打包,请等待镜像打包完成。');
        this.init();
      }
    },
    isBankMaster(bankMasterList) {
      let isBankMasterFlag = false;
      for (var i = 0; i < bankMasterList.length; i++) {
        if (bankMasterList[i].user_name_en == this.currentUser.user_name_en) {
          isBankMasterFlag = true;
          break;
        }
      }
      return isBankMasterFlag;
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    userFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        let isNum = true;
        let isGroup = true;
        let companyId = (row.company + '').toLowerCase();
        let groupId = (row.group + '').toLowerCase();
        cols.some(col => {
          if (col.name === 'num') {
            isNum = companyId;
          }
          if (col.name === 'group') {
            isGroup = groupId;
          }
        });
        return (
          isNum &&
          isGroup &&
          lowerTerms.every(term => {
            return cols.some(
              col => (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1
            );
          })
        );
      });
    },
    async init() {
      this.loading = true;
      await this.queryApply(this.$route.params.id);
      this.applylist = this.applyList;
      this.loading = false;
    },
    chineseName(val) {
      return val
        .map(item => {
          return item.user_name_cn;
        })
        .join(',');
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
    openSelectEnvDialog(item) {
      this.application_id = item.application_id;
      this.testEnv = {
        uat_env_name: item.uat_env_name,
        rel_env_name: item.rel_env_name
      };
      this.network = item.network;
      this.selectEnvDialogOpen = true;
    },
    async handleSelectEnv() {
      const params = {
        ...this.testEnv,
        application_id: this.application_id,
        release_node_name: this.$route.params.id
      };
      await this.setTestEnvs(params);
      successNotify('设置成功');
      this.init();
      this.selectEnvDialogOpen = false;
    },
    async queryUATEnv(val, update, abort) {
      let labels = this.network.split(',');
      labels.push('uat');
      await this.queryByLabelsFuzzy({ labels });
      update();
    },
    async queryRELEnv(val, update, abort) {
      let labels = this.network.split(',');
      labels.push('rel');
      await this.queryByLabelsFuzzy({ labels });
      update();
    },
    handlePipelineOpen(item) {
      this.application_id = item.application_id;
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
    filterMethods(rows, terms, cols, getCellValue) {
      let lowerTerms = terms
        .toLowerCase()
        .split(',')
        .filter(item => {
          return item !== 'tabel' && item !== '';
        });
      const filterData = rows.filter(row => {
        return lowerTerms.every(item => {
          if (item === '') {
            return true;
          }
          return cols.some(col => {
            const tableData = getCellValue(col, row);
            if (!Array.isArray(tableData)) {
              return (tableData + '').toLowerCase().indexOf(item) > -1;
            } else {
              return tableData.some(data => {
                const value = data.user_name_cn ? data.user_name_cn : data;
                return value.indexOf(item) > -1;
              });
            }
          });
        });
      });
      this.filterLength = filterData.length;
      return filterData;
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    /* 试运行发布 */
    async handleCreateTestrunBranch(data) {
      this.testrun_id = '';
      const { application_id, testrun_applications } = data;
      /* 如果有试运行包，选择最新一个包，发接口查该运行包已试运行任务 */
      if (testrun_applications.length > 0) {
        this.testrun_id = testrun_applications[testrun_applications.length - 1];
        this.queryRunTask();
      }
      this.application_id = application_id;
      this.testrun_applications = this.testrunApplicationsOptions = testrun_applications;
      /* 查询拉取的分支 */
      await this.createTestrunBranch({
        application_id: application_id,
        release_node_name: this.release_node_name
      });
      this.testRuningDialogOpened = true;
      this.appTaskListLoading = true;
      /* 查询未发布任务 */
      await this.queryNotinlineTasksByAppId({
        id: application_id
      });
      this.appTaskListLoading = false;
    },
    async handleTestRuningDialog() {
      const params = this.selectedTask.map(task => {
        return {
          task_id: task.id,
          task_name: task.name,
          task_branch: task.feature_branch
        };
      });
      await this.mergeTaskBranch({
        testrun_task: params,
        application_id: this.application_id,
        release_node_name: this.release_node_name,
        transition_branch: this.testRunBranch.transition_branch,
        testrun_branch: this.testRunBranch.testrun_branch
      });
      successNotify('成功');
      this.testRuningDialogOpened = false;
      this.init();
      this.selectedTask = [];
    },
    testrunApplicationsFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.testrunApplicationsOptions = this.testrun_applications.filter(v =>
          v.testrun_branch.toLowerCase().includes(needle)
        );
      });
    },
    async queryRunTask() {
      this.runTaskListLoading = true;
      await this.queryTaskByTestRunId({
        testrun_id: this.testrun_id.id
      });
      this.runTaskListLoading = false;
    },
    handleCheck(data) {
      this.confirmConfigOpen = true;
      this.checkData = data;
    },
    async checkOk() {
      let params = {
        application_id: this.checkData.application_id,
        release_node_name: this.checkData.release_node_name
      };
      await this.changeConfirmConf(params);
      await this.queryApply(this.$route.params.id);
      this.applylist = this.applyList;
      this.confirmConfigOpen = false;
    },
    goCheckEnv() {
      this.$router.push({
        name: 'DeployMessageHandlePage',
        query: { appId: this.application_id }
      });
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    }
  },
  async created() {
    this.release_node_name = this.$route.params.id;
    await this.init();
    this.filterLength = this.applylist.length;
  }
};
</script>

<style lang="stylus" scoped>
.div-border
  border-top solid 4px #21BA45
</style>
