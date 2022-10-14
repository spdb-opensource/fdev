<template>
  <div>
    <div class="bg-white q-py-lg">
      <div v-if="joinPeopleExceptTester">
        <div class="btn-row">
          <fdev-btn
            dialog
            v-if="jobProfile.stage !== 'file'"
            :to="`/job/list/${jobProfile.id}`"
            label="任务详情"
            class="q-mr-lg"
          />
        </div>
        <fdev-card-section class="text-center">
          <div class="text-h5 text-title">
            {{ jobProfile.name }}
          </div>
        </fdev-card-section>
        <fdev-card-section class="row">
          <div class="col-sm-9 col-xs-12">
            <f-formitem label="测试类型" style="text-align:left">
              试运行
            </f-formitem>
            <f-formitem label="分支" style="text-align:left">
              <span
                v-if="
                  testRunModel.releaseNode &&
                    testRunModel.releaseNode.release_application
                "
              >
                {{
                  testRunModel.releaseNode.release_application.release_branch
                }}
              </span>
            </f-formitem>
            <f-formitem
              label="试运行窗口"
              style="text-align:left"
              v-if="Object.keys(releaseNodeData).length === 0"
            >
              <fdev-select
                ref="testRunModel.releaseNode"
                use-input
                @filter="releaseNodeFilter"
                v-model="$v.testRunModel.releaseNode.$model"
                :options="filterReleaseNodes"
                option-label="optionLabel"
                option-value="release_node_name"
                :rules="[
                  () =>
                    !$v.testRunModel.releaseNode.$error || '请选择试运行窗口'
                ]"
              />
            </f-formitem>
            <f-formitem
              label="试运行窗口"
              style="text-align:left"
              v-if="
                Object.keys(releaseNodeData).length > 0 &&
                  testRunModel.releaseNode
              "
            >
              {{ testRunModel.releaseNode.release_node_name }}
            </f-formitem>
          </div>
        </fdev-card-section>
        <fdev-card-actions align="center">
          <div>
            <fdev-btn
              v-if="
                !testRunModel.releaseNode ||
                  testRunModel.releaseNode.task_status !== '1'
              "
              label="挂载"
              @click="mountWindow"
              :loading="globalLoading['releaseForm/addJobReleaseNode']"
              :disable="
                (testRunModel.releaseNode &&
                  testRunModel.releaseNode.task_status === '0') ||
                  !isMasterManager
              "
            />
            <fdev-tooltip
              anchor="top middle"
              v-if="
                Object.keys(releaseNodeData).length === 0 && !isMasterManager
              "
            >
              请联系任务的负责人
              {{ getJobMaster(jobProfile.spdb_master, jobProfile.master) }}
              挂载
            </fdev-tooltip>
            <fdev-tooltip
              anchor="top middle"
              v-if="
                testRunModel.releaseNode &&
                  testRunModel.releaseNode.task_status === '0'
              "
            >
              <f-icon name="member" class="text-primary" />
              请联系任务的行内项目负责人
              {{ getSpdbMaster(jobProfile.spdb_master) }}
              在fdev投产模块下
              {{ testRunModel.releaseNode.release_node_name }}
              试运行窗口确认
            </fdev-tooltip>
          </div>
          <div class="margin-l-r">
            <fdev-btn
              label="分支合并"
              :disable="
                !testRunModel.releaseNode ||
                  testRunModel.releaseNode.task_status !== '1' ||
                  testMergeInfo.status_code === '1'
              "
              :loading="globalLoading['jobForm/createTestRunMerge']"
              @click="putTestRun"
            />
            <fdev-tooltip
              anchor="top middle"
              v-if="Object.keys(releaseNodeData).length === 0"
            >
              请先挂载试运行窗口
            </fdev-tooltip>
            <fdev-tooltip
              anchor="top middle"
              v-if="
                testRunModel.releaseNode &&
                  testRunModel.releaseNode.task_status === '0'
              "
            >
              <f-icon name="member" class="text-primary" />
              请联系任务的行内项目负责人
              {{ getSpdbMaster(jobProfile.spdb_master) }}
              在fdev投产模块下
              {{ testRunModel.releaseNode.release_node_name }}
              试运行窗口确认
            </fdev-tooltip>
            <fdev-tooltip
              anchor="top middle"
              v-if="testMergeInfo.status_code === '1'"
            >
              请联系应用负责人在gitlab上进行代码审核并合并。。。
            </fdev-tooltip>
          </div>
          <div>
            <fdev-btn
              flat
              color="primary"
              label="进入试运行"
              class="btn-lg q-px-sm"
              @click="enterTestRun"
            />
          </div>
        </fdev-card-actions>
      </div>
      <div v-else>
        <Exception
          type="403"
          desc="抱歉，你无权访问该页面"
          backText="返回首页"
        />
      </div>
    </div>
    <div class="q-pa-md q-gutter-sm" v-if="testRunModel.releaseNode">
      <f-dialog
        v-model="openRefuseReleaseDialog"
        transition-show="slide-up"
        transition-hide="slide-down"
        title="拒绝投产"
      >
        <f-formitem label="投产窗口" style="text-align:left">
          {{ testRunModel.releaseNode.release_node_name }}
        </f-formitem>
        <f-formitem label="拒绝人员" style="text-align:left">
          <div class="text-wrapper">
            {{
              testRunModel.releaseNode.reject_reason
                ? testRunModel.releaseNode.reject_reason.split('--')[1]
                : '1'
            }}
          </div>
        </f-formitem>
        <f-formitem label="拒绝原因" style="text-align:left">
          <div class="text-wrapper">
            {{
              testRunModel.releaseNode.reject_reason
                ? testRunModel.releaseNode.reject_reason.split('--')[0]
                : ''
            }}
          </div>
        </f-formitem>
        <template v-slot:btnSlot>
          <fdev-btn dialog v-close-popup label="确定" />
        </template>
      </f-dialog>
    </div>
    <f-dialog
      v-model="waitDialog"
      transition-show="slide-up"
      transition-hide="slide-down"
      title="代码审核"
    >
      <fdev-card-section>
        <span>
          请联系应用负责人在gitlab上进行代码审核并合并。。。
        </span>
      </fdev-card-section>
      <template v-slot:btnSlot>
        <fdev-btn flat label="确定" v-close-popup />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import date from '#/utils/date.js';
import { formatOption, validate } from '@/utils/utils';
import Exception from '@/components/Exception';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'TestRun',
  components: { Exception },
  data() {
    return {
      jobId: '',
      filterReleaseNodes: [],
      releaseNodes: [],
      testRunModel: {
        releaseNode: null
      },
      isMasterManager: true,
      openRefuseReleaseDialog: false,
      waitDialog: false,
      joinPeopleExceptTester: true
    };
  },
  validations: {
    testRunModel: {
      releaseNode: {
        required
      }
    }
  },
  computed: {
    ...mapState('jobForm', ['jobProfile', 'testMergeInfo', 'testRunMerge']),
    ...mapState('releaseForm', ['releaseNodeList', 'releaseNodeData']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', ['isKaDianManager'])
  },
  methods: {
    ...mapActions('jobForm', [
      'queryJobProfile',
      'queryTestMergeInfo', //查询分支合并情况
      'createTestRunMerge'
    ]),
    ...mapActions('releaseForm', [
      'queryReleaseNode', // 查询试运行投产窗口列表
      'addJobReleaseNode', // 挂载试运行投产窗口
      'queryReleaseNodeByJob' // 查询任务挂载投产情况
    ]),
    enterTestRun() {
      this.$v.testRunModel.$touch();
      let testRunModelKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('testRunModel') > -1;
      });
      validate(testRunModelKeys.map(key => this.$refs[key]));
      if (this.$v.testRunModel.$invalid) {
        return;
      }
      this.$router.push(
        `/release/list/${
          this.testRunModel.releaseNode.release_node_name
        }/joblist`
      );
    },
    releaseNodeFilter(val, update) {
      update(() => {
        this.filterReleaseNodes = this.releaseNodes.filter(tag => {
          return tag.optionLabel.indexOf(val) > -1;
        });
      });
    },
    getSpdbMaster(spdb_master) {
      return spdb_master
        .map(item => {
          return item.user_name_cn;
        })
        .join('，');
    },
    getJobMaster(spdb_master, master) {
      let array = spdb_master.concat(master);
      let nameArray = array.map(item => {
        return item.user_name_cn;
      });
      const set = new Set(nameArray);
      nameArray = Array.from(set);
      return nameArray;
    },
    async mountWindow() {
      this.$v.testRunModel.$touch();
      let testRunModelKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('testRunModel') > -1;
      });
      validate(testRunModelKeys.map(key => this.$refs[key]));
      if (this.$v.testRunModel.$invalid) {
        return;
      }
      await this.addJobReleaseNode({
        release_node_name: this.testRunModel.releaseNode.release_node_name,
        task_id: this.jobId
      });
      await this.queryReleaseNodeByJob({
        task_id: this.jobId,
        type: '3'
      });
      this.testRunModel.releaseNode = this.releaseNodeData;
    },
    async putTestRun() {
      await this.createTestRunMerge({ id: this.jobId });
      await this.queryTestMergeInfo({ id: this.jobId });
      if (this.testMergeInfo.status_code === '1') {
        this.waitDialog = true;
      }
    }
  },
  async created() {
    this.jobId = this.$route.params.id;
    await this.queryJobProfile({ id: this.jobId });
    this.isMasterManager =
      this.isKaDianManager ||
      this.jobProfile.master.some(item => item.id === this.currentUser.id) ||
      this.jobProfile.spdb_master.some(item => item.id === this.currentUser.id);

    this.joinPeopleExceptTester =
      this.isKaDianManager ||
      this.jobProfile.master.some(item => item.id === this.currentUser.id) ||
      this.jobProfile.developer.some(item => item.id === this.currentUser.id) ||
      this.jobProfile.spdb_master.some(item => item.id === this.currentUser.id);
    await this.queryTestMergeInfo({ id: this.jobId });
    // 0 - 未发起合并 1 - 待合并 2 - 已合并 3- 合并请求被关闭
    // 待合并弹框提示
    if (this.testMergeInfo.status_code === '1') {
      this.waitDialog = true;
    }
    await this.queryReleaseNodeByJob({
      task_id: this.jobId,
      type: '3'
    });
    if (Object.keys(this.releaseNodeData).length > 0) {
      this.testRunModel.releaseNode = this.releaseNodeData;
    }
    // 拒绝（task_status  2） 打开拒绝提示框
    if (
      this.testRunModel.releaseNode &&
      this.testRunModel.releaseNode.task_status === '2'
    ) {
      this.openRefuseReleaseDialog = true;
    }
    // type传参，3：试运行窗口列表
    await this.queryReleaseNode({
      start_date: date.formatDate(Date.now(), 'YYYY-MM-DD'),
      type: '3'
    });
    this.releaseNodes = formatOption(this.releaseNodeList, 'release_node_name');
    this.releaseNodes.forEach(item => {
      item.optionLabel = `${item.owner_group_name}：${item.release_node_name}`;
    });
  }
};
</script>

<style lang="stylus" scoped>
.margin-l-r
  margin 0 20px

.width-card
  width 500px
.btn-row
  display flex
  flex-direction row-reverse
</style>
