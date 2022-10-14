<template>
  <f-block>
    <Sonar :appId="job.project_id" :branch="job.branch" />
    <!--Sonar扫描  -->
    <SonarDialog
      v-model="sonarDialogOpen"
      :appName="job.project_name"
      :appId="job.project_id"
      :branchName="job.branch"
    />
  </f-block>
</template>

<script>
import Sonar from '@/modules/App/components/Sonar/Sonar.vue';
import SonarDialog from './taskInfo/SonarDialog';
import { mapState, mapActions } from 'vuex';
import { exportExcel } from '@/utils/utils';
import { formatJob } from '@/modules/Job/utils/utils';
export default {
  name: 'CodeAnalizy',
  data() {
    return {
      job: {}
    };
  },
  components: {
    SonarDialog,
    Sonar
  },
  computed: {
    ...mapState('jobForm', ['scanProcess', 'sonarLog', 'jobProfile'])
  },
  created() {
    this.drewPageByJobProfile(this.jobProfile);
    this.getTips();
  },
  watch: {
    jobProfile(val) {
      if (val) {
        this.drewPageByJobProfile(val);
      }
    }
  },
  methods: {
    ...mapActions('jobForm', ['getScanProcess', 'downloadSonarLog']),
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
    drewPageByJobProfile(val) {
      this.job = formatJob(val);
    }
  }
};
</script>

<style lang="stylus" scoped></style>
