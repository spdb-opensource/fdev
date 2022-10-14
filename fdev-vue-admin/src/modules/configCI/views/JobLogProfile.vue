<template>
  <div v-if="jobExeRsp" class="column">
    <div class="q-px-lg items-center row justify-between">
      <span class="text-blue-9 text-subtitle1 page-title-style">
        任务的运行日志
      </span>
    </div>
    <div class="row full-width no-wrap">
      <div class="column q-ml-lg col">
        <div class="col text-subtitle2 q-my-sm">
          <span>切换任务日志:</span>
          <el-cascader
            v-model="jobExeValues"
            :options="options"
            @change="changeJobExeId"
            :placeholder="placeholder"
            class="cascader-style"
          ></el-cascader>
        </div>
        <fdev-scroll-area
          :visible="true"
          class="bg-black code-style scroll q-px-lg q-py-sm text-white"
        >
          <div v-if="jobExeRsp.log !== ''">
            <code>
              <div
                class="ws-pre-div"
                v-for="(item, index) in logData"
                :key="index"
              >
                <span class="ws-pre-wrap" :class="changeColor(item.content)">
                  {{ item.content | filterContent }}
                </span>
              </div>
            </code>
          </div>
        </fdev-scroll-area>
      </div>
      <div class="q-ml-md column col-3">
        <span class="text-subtitle2 q-my-sm">详细信息</span>
        <f-formitem full-width label="状态">
          <fdev-badge
            :label="jobExeRsp.status"
            :style="{ background: ciColors[jobExeRsp.status] }"
          />
        </f-formitem>
        <f-formitem
          full-width
          v-for="(msg, ind) in jobLogMsg"
          :key="ind"
          :label="msg.label"
          class="justify-start items-center"
        >
          {{ msg.val }}
        </f-formitem>
        <div
          v-if="jobExeRsp.status === 'error' || jobExeRsp.status === 'success'"
          class="footer-btn row full-width justify-around"
        >
          <fdev-btn
            v-forbidMultipleClick
            label="下载日志"
            @click="clickDownload"
          />
          <fdev-btn
            v-forbidMultipleClick
            label="复制"
            @click="clickCopyBtn"
            class="uni-btn"
            :data-clipboard-text="jobExeRsp.log"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { queryLogDetailById } from '../services/method';
import { THUMB_STYLE } from '../utils/constants';
import { uploadOrDownloadFile } from '../utils/utils';
import { CI_COLORS, LOG_REFREASH } from '../utils/constants';
import { successNotify, errorNotify } from '@/utils/utils';
import Clipboard from 'clipboard';
export default {
  name: 'JobLogProfile',
  props: {
    id: String
  },
  data() {
    return {
      ...THUMB_STYLE,
      ciColors: CI_COLORS,
      jobExeValues: null,
      timer: null,
      jobExeId: this.id,
      jobExeRsp: null,
      logData: [],
      content: []
    };
  },
  filters: {
    filterContent(val) {
      let content = ['[1;32m', '[0m', '[1;31m', '[1;33m', '[37m'];
      content.forEach(i => {
        val = val.toString().replace(i, '');
      });
      return val;
    }
  },
  beforeDestroy() {
    //清除定时器
    clearInterval(this.timer);
  },
  async mounted() {
    await this.queryLogDetailById();
    let { stageIndex, jobIndex, stage, jobNumber, jobExeId } = this.jobExeRsp;
    this.jobExeValues = [
      stage[stageIndex].name,
      stage[stageIndex].jobs[jobIndex].name,
      { jobExeId: jobExeId, jobNumber: jobNumber }
    ];
    !this.stopTimer &&
      (this.timer = setInterval(this.queryLogDetailById, LOG_REFREASH));
  },
  //如果运行失败，清除定时器
  stopTimer(val) {
    val && clearInterval(this.timer);
  },
  computed: {
    placeholder() {
      let val = this.jobExeValues || [
        '阶段名名称',
        '任务名称',
        { jobNumber: '任务构建号' }
      ];
      return val[0] + '/' + val[1] + '/' + val[2].jobNumber;
    },
    //是否应该停止重发交易
    stopTimer() {
      return this.jobExeRsp
        ? ['error', 'success'].includes(this.jobExeRsp.status)
        : false;
    },
    jobLogMsg() {
      let val = this.jobExeValues || [
        '阶段名名称',
        '任务名称',
        { jobNumber: '任务构建号' }
      ];
      return [
        { label: '任务名称', val: val[1] },
        { label: '任务构建号', val: val[2].jobNumber },
        { label: '开始时间', val: this.jobExeRsp.jobStartTime },
        { label: '耗时', val: this.jobExeRsp.jobCostTime },
        { label: '任务所在阶段名', val: val[0] }
      ];
    },
    options() {
      return this.jobExeRsp
        ? this.jobExeRsp.stage.map(stage => {
            return {
              label: stage.name,
              value: stage.name,
              children: stage.jobs.map(job => {
                return {
                  label: job.name,
                  value: job.name,
                  children: job.jobExes.map(jobExe => {
                    return {
                      label: jobExe.jobNumber,
                      value: {
                        jobExeId: jobExe.jobExeId,
                        jobNumber: jobExe.jobNumber
                      }
                    };
                  })
                };
              })
            };
          })
        : null;
    }
  },
  methods: {
    changeColor(val) {
      if (val.substring) {
        if (val.substring(1, 7).startsWith('[1;32m')) {
          return 'green';
        } else if (val.substring(1, 7).startsWith('[1;31m')) {
          return 'red';
        } else if (val.substring(1, 7).startsWith('[1;33m')) {
          return 'warn';
        } else if (val.substring(1, 6).startsWith('[37m')) {
          return 'debug';
        } else {
          return 'white';
        }
      }
    },
    goBack() {
      this.$router.back();
    },
    async queryLogDetailById() {
      this.jobExeRsp = await queryLogDetailById({
        jobExeId: this.jobExeId
      });
      this.content = this.jobExeRsp.log.split(/[\n]/g);
      this.logData = [];
      for (let index in this.content) {
        this.logData.push({ content: this.content[index] });
      }
    },
    async changeJobExeId() {
      this.jobExeId = this.jobExeValues[2].jobExeId;
      clearInterval(this.timer);
      await this.queryLogDetailById();
      !this.stopTimer &&
        (this.timer = setInterval(this.queryLogDetailById, LOG_REFREASH));
    },
    //下载日志
    clickDownload() {
      let params = new FormData();
      params.append('jobExeId', this.jobExeRsp.jobExeId); //参数
      let obj = {
        type: 'download',
        url: 'fcipipeline/api/pipelineLog/downLoadLog',
        params
      };
      uploadOrDownloadFile(obj);
    },
    //复制日志
    clickCopyBtn() {
      let clipboard = new Clipboard('.uni-btn');
      clipboard.on('success', e => {
        successNotify('复制成功');
        clipboard.destroy();
      });
      clipboard.on('error', e => {
        errorNotify('复制失败');
        clipboard.destroy();
      });
    }
  }
};
</script>

<style scoped lang="stylus">
.label-style
  font-size 13px
  color #78909C
  line-height 28px
  width 135px
.value-style
  font-size 13px
  color #37474F
  line-height 28px
.code-style
  height 700px
.cascader-style
  width 400px
  margin-left 20px
  z-index 99
.page-title-style
  position relative
.page-title-style::before
  content ' '
  background-color #1565C0
  width 4px
  height 16px
  top 7px
  left: -10px
  position absolute
  border-radius 2px
.ws-pre-wrap
  word-break break-all
.green
  color #76ff03
  font-size 16px
.red
  color #ff3d00
  font-size 16px
.white
  color white
  font-size 13px
.warn
  color #bdbd00
  font-size 16px
.debug
  color #7fd2eb
  font-size 16px
.ws-pre-div
  line-height 2
  font-size 13px
  font-family: Menlo, DejaVu Sans Mono, Liberation Mono, Consolas, Ubuntu Mono, Courier New, andale mono, lucida console, monospace
  display block
/deep/ .q-scrollarea__thumb
  background #FFF
  opacity 0.8
  right 1px
.q-badge
  line-height 20px
.footer-btn
  margin-top 30px
  /deep/
    .q-btn--standard {
      min-width: 100px;
      height: 36px;
}
</style>
