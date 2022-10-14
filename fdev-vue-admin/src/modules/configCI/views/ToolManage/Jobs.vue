<template>
  <div>
    <div class="row q-mb-lg justify-between">
      <span class="text-subtitle3">任务模板列表</span>
      <div class="row no-wrap q-gutter-x-md">
        <f-formitem label="任务名称" page>
          <fdev-input
            clearable
            v-model="jobName"
            @keyup.enter.native="jobFilter"
          >
            <template v-slot:append>
              <f-icon name="search" class="cursor-pointer" @click="jobFilter" />
            </template>
          </fdev-input>
        </f-formitem>
        <fdev-btn
          normal
          ficon="add"
          label="新增任务模板"
          @click="addJobsTemp"
        />
      </div>
    </div>
    <div class="row no-wrap q-mt-lg">
      <fdev-tabs v-model="tab" dense vertical>
        <fdev-tab
          v-for="(job, index) in allJobs"
          :key="index"
          :name="job.category.categoryName"
          :label="job.category.categoryName"
        />
      </fdev-tabs>
      <fdev-separator vertical class="q-mr-lg q-ml-xl" />

      <fdev-tab-panels class="full-width" v-model="tab" vertical>
        <fdev-tab-panel
          v-for="(jobInfo, index) in allJobs"
          :key="index"
          :name="jobInfo.category.categoryName"
          class="row q-gutter-md q-pa-md"
        >
          <div
            class="column no-wrap job-card q-pa-md"
            v-for="(item, index) in jobInfo.jobList"
            :key="index"
          >
            <div class="row items-center full-width q-mb-md">
              <div v-if="item.canEdit === '1'" class="row items-center">
                <fdev-select
                  style="width: 125px"
                  :options="options"
                  emit-value
                  map-options
                  option-value="value"
                  option-label="label"
                  @input="selectRange(item.visibleRange, item.jobId)"
                  v-model="item.visibleRange"
                />
                <f-icon
                  name="edit"
                  width="16px"
                  height="16px"
                  class="q-mx-sm text-primary cursor-pointer"
                  @click="editJob(item, jobInfo.category)"
                />
                <f-icon
                  name="delete"
                  width="16px"
                  class="text-primary cursor-pointer"
                  @click="deleteJob(item)"
                />
              </div>
              <span
                class="text-subtitle3 text-no-wrap q-ml-sm text-ellipsis"
                :style="`max-width:${item.canEdit === '1' ? '90px' : '288px'}`"
                :title="item.jobName"
              >
                {{ item.jobName }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ item.jobName }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </span>
            </div>
            <div class="row justify-between no-wrap">
              <fdev-img
                class="q-ml-md"
                :src="jobIcon"
                style="width:60px;height:60px"
              />
              <f-scrollarea style="max-width:162px" class="text-caption">
                <span
                  v-for="(step, ind) in item.steps"
                  class="text-no-wrap q-mr-md"
                  :key="ind"
                >
                  {{ step.name }}
                </span>
              </f-scrollarea>
            </div>
            <div class="self-end">{{ item.author.nameCn }} 提供</div>
          </div>
        </fdev-tab-panel>
      </fdev-tab-panels>
    </div>
    <JobDetail
      v-model="jobDetailDialogOpened"
      @receive-job-info="submitJob"
      type="edit"
      :jobInfo="jobViewData"
    />
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { successNotify } from '@/utils/utils';
import JobDetail from '../JobManage/JobDetail';
const JOB_ICON = require('../../assets/job_icon.svg');
export default {
  name: 'jobs',
  data() {
    return {
      jobIcon: JOB_ICON,
      splitterModel: 140,
      jobDetailDialogOpened: false,
      jobViewData: {},
      tab: '',
      jobName: '',
      allJobs: [],
      options: [
        { label: '全部可见', value: 'public' },
        { label: '个人可见', value: 'private' }
      ],
      jobsType: ''
    };
  },
  components: { JobDetail },
  watch: {},
  computed: {
    ...mapState('configCIForm', {
      jobList: 'jobList',
      jobListDetail: 'jobListDetail'
    })
  },
  methods: {
    ...mapActions('configCIForm', {
      getAllJobs: 'getAllJobs',
      getFullJobsByIds: 'getFullJobsByIds',
      jobsUpdate: 'jobsUpdate',
      updateJobsVisibleRange: 'updateJobsVisibleRange',
      delJobsTemplate: 'delJobsTemplate',
      addJobsTemplate: 'addJobsTemplate'
    }),
    addJobsTemp() {
      this.jobsType = 'add';
      this.jobViewData = {
        name: '',
        steps: []
      };
      this.jobDetailDialogOpened = true;
    },
    async selectRange(data, id) {
      await this.updateJobsVisibleRange({
        jobId: id,
        visibleRange: data
      });
      successNotify('更新成功');
    },
    async submitJob(data) {
      let category = this.allJobs.find(
        item => item.category.categoryName === this.tab
      ).category;
      let params = { ...data, category };
      if (this.jobsType === 'add') {
        await this.addJobsTemplate(params);
        successNotify('新增成功');
      } else {
        await this.jobsUpdate(params);
        successNotify('更新成功');
      }
      this.init();
    },
    deleteJob(data) {
      return this.$q
        .dialog({
          title: '删除任务模板',
          message: '请确认是否删除该任务模板',
          ok: '删除',
          cancel: '再想想'
        })
        .onOk(async () => {
          await this.delJobsTemplate({ jobId: data.jobId });
          successNotify('删除成功');
          this.init();
        });
    },
    async editJob(job, info) {
      this.jobsType = 'edit';
      await this.getFullJobsByIds({ id: job.jobId });
      let jobInfo = this.jobListDetail;
      jobInfo.steps = jobInfo.steps.map((step, index) => {
        return {
          ...step,
          deleteFlag: jobInfo.steps[index].deleteFlag,
          name: step.pluginInfo.name,
          warning: true
        };
      });
      this.jobDetailDialogOpened = true;
      this.jobViewData = {
        name: jobInfo.jobName,
        ...jobInfo
      };
    },
    jobFilter() {
      let _this = this;
      _this.allJobs = [];
      this.jobList.forEach(item => {
        let jobList = item.jobList.filter(job => {
          if (!_this.jobName) {
            return job;
          }
          return (
            job.jobName
              .toLocaleLowerCase()
              .indexOf(_this.jobName.toLocaleLowerCase()) > -1
          );
        });
        if (jobList.length > 0) {
          _this.allJobs.push({
            category: item.category,
            jobList: jobList
          });
        }
      });
      this.tab = this.allJobs.length
        ? this.allJobs[0].category.categoryName
        : '';
    },
    async init() {
      await this.getAllJobs({
        pageNum: 0,
        pageSize: 0
      });
      if (!this.tab) {
        this.tab =
          this.jobList.length > 0 ? this.jobList[0].category.categoryName : '';
      }
      this.allJobs = this.jobList.slice();
    }
  },
  mounted() {
    this.init();
  }
};
</script>
<style lang="stylus" scoped>
/deep/ .q-panel > div
  height auto
.job-card
  box-shadow 0 2px 10px 0 rgba(21,101,192,0.20)
  border-radius 6px
  width 320px
  height 160px
  background url('../../assets/job_bg.png') no-repeat
  background-size 100% 100%
  background-position bottom
.row-height
  height 35px
.align-right
  position: absolute;
  right: 1%;
</style>
