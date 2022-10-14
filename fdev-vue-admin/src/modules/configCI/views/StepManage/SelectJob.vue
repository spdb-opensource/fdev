<template>
  <div class="selectJob">
    <f-dialog
      :value="value"
      @input="$emit('input', false)"
      right
      title="选择任务组"
    >
      <f-formitem label="搜索" class="q-mb-md">
        <fdev-input
          clearable
          placeholder="请输入任务名称"
          v-model="jobName"
          @keyup.enter.native="jobFilter"
        >
          <template v-slot:append>
            <f-icon name="search" class="cursor-pointer" @click="jobFilter" />
          </template>
        </fdev-input>
      </f-formitem>
      <fdev-splitter v-model="splitterModel" unit="px" class="w750">
        <template v-slot:before>
          <fdev-tabs v-model="tab" dense vertical>
            <fdev-tab
              v-for="(job, index) in allJobs"
              :key="index"
              :name="job.category.categoryName"
              :label="job.category.categoryName"
            />
          </fdev-tabs>
        </template>
        <template v-slot:after>
          <fdev-tab-panels v-model="tab" vertical>
            <fdev-tab-panel
              v-for="(jobInfo, index) in allJobs"
              :key="index"
              :name="jobInfo.category.categoryName"
              class="row"
            >
              <div
                class="col-6"
                v-for="(item, index) in jobInfo.jobList"
                :key="index"
              >
                <fdev-card
                  class="q-ma-sm hand"
                  @click="selectJobInfo(item, jobInfo.category)"
                >
                  <fdev-item>
                    <fdev-item-section avatar>
                      <fdev-icon color="primary" name="settings_input_hdmi" />
                    </fdev-item-section>
                    <fdev-item-section>
                      <div class="text-subtitle1 ellipsis">
                        {{ item.jobName }}
                        <fdev-tooltip>
                          {{ item.jobName }}
                        </fdev-tooltip>
                      </div>
                      <div class="text-caption ellipsis">
                        {{ item.jobDesc }}
                        <fdev-tooltip>
                          {{ item.jobDesc }}
                        </fdev-tooltip>
                      </div>
                    </fdev-item-section>
                  </fdev-item>
                </fdev-card>
              </div>
            </fdev-tab-panel>
          </fdev-tab-panels>
        </template>
      </fdev-splitter>
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
export default {
  name: 'selectJob',
  components: {},
  data() {
    return {
      splitterModel: 140,
      tab: '',
      jobName: '',
      allJobs: []
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    jobName(val) {
      this.allJobs = this.jobList.slice();
    }
  },
  computed: {
    ...mapState('configCIForm', {
      jobList: 'jobList',
      jobListDetail: 'jobListDetail'
    })
  },
  methods: {
    ...mapActions('configCIForm', {
      getAllJobs: 'getAllJobs',
      getFullJobsByIds: 'getFullJobsByIds'
    }),
    closeDialog() {
      this.$emit('close');
    },
    async selectJobInfo(job, info) {
      await this.getFullJobsByIds({ id: job.jobId });
      // TODO step plugin deleteFlag 组装
      let jobInfo = this.jobListDetail;
      jobInfo.steps = jobInfo.steps.map((step, index) => {
        return {
          ...step,
          deleteFlag: job.steps[index].deleteFlag
        };
      });
      this.$emit('getJob', { job: jobInfo, info });

      // jobInfo.steps =  this.jobListDetail.steps;
      // jobInfo.name = jobInfo.jobName
      // this.$emit('getJob', { job: jobInfo, info });
    },
    jobFilter() {
      let _this = this;
      _this.allJobs = [];
      this.jobList.forEach(item => {
        let jobList = item.jobList.filter(job => {
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
    clearInput() {
      this.pluginName = '';
      this.allJobs = this.jobList.slice();
    }
  },
  async mounted() {
    await this.getAllJobs({
      pageNum: 0,
      pageSize: 0
    });
    this.tab =
      this.jobList.length > 0 ? this.jobList[0].category.categoryName : '';
    this.allJobs = this.jobList.slice();
  }
};
</script>

<style lang="stylus" scoped>
.w750
 width 750px

.my-layout
  min-width 750px
  height 800px
  border-radius 16px
  font-size 14px   //影响了包含input和按钮的div的高度
  >>> .q-toolbar__title
    padding 0
/deep/.q-tab-panel
  padding 0 16px 16px 16px;
.hand
  cursor pointer
  width 250px
.ellipsis
  word-break break-all
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  width: 100%;
</style>
