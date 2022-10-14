<template>
  <fdev-table
    :columns="logCol"
    :data="logData"
    :title="subtitle ? `运行日志:${subtitle}` : '运行日志'"
    class="my-sticky-column-table"
    noExport
    @request="getLogData"
    :pagination.sync="LogPagination"
    titleIcon="log_s_f"
    :hide-bottom="logData.length > 0 ? false : true"
  >
    <template v-slot:top-bottom-opt>
      <slot />
    </template>
    <template v-slot:body-cell-status="props">
      <fdev-td
        :props="props"
        class="row no-wrap items-center cursor-pointer"
        @click="goLogPanorama(props.row.exeId)"
      >
        <div
          class="log-circle q-mr-sm"
          :class="`${props.row.status}-gradient`"
        />
        <span :class="`text-${statusCfg[props.row.status].color}`">{{
          statusCfg[props.row.status].label
        }}</span>
      </fdev-td>
    </template>
    <template v-slot:body-cell-stages="props">
      <fdev-td :props="props">
        <f-scrollarea horizontal class="row q-mt-sm no-wrap">
          <div class="row no-wrap">
            <div
              v-for="(stage, i) in props.row.stages"
              :key="i"
              class="q-mr-xs cursor-pointer"
            >
              <f-icon
                :width="20"
                :height="20"
                :class="`text-${statusCfg[stage.status].color}`"
                :name="`${statusCfg[stage.status].icon}_c_o`"
              />
              <fdev-menu auto-close anchor="bottom right" :offset="[-80, 10]">
                <div
                  @click="goJobLogPage(stage.jobs[0].jobExes)"
                  class="row no-wrap cursor-pointer text-subtitle2 items-center"
                >
                  <f-icon
                    :width="20"
                    :height="20"
                    :name="`${statusCfg[stage.status].icon}_c_o`"
                    :class="`q-mr-xs text-${statusCfg[stage.status].color}`"
                  /><span>{{ stage.name }}</span>
                </div>
              </fdev-menu>
            </div>
          </div>
        </f-scrollarea>
      </fdev-td>
    </template>
    <template v-slot:body-cell-pipelineNumber="props">
      <fdev-td :props="props">
        <fdev-btn
          flat
          :label="props.row.pipelineNumber"
          @click="goLogPanorama(props.row.exeId)"
        />
      </fdev-td>
    </template>
    <template v-slot:body-cell-manage="props">
      <fdev-td :props="props">
        <template v-if="props.row.retry === '1'">
          <fdev-btn
            flat
            label="重试"
            v-if="['error', 'success', 'cancel'].includes(props.row.status)"
            @click="getRetryPipeline(props.row.exeId)"/>
          <fdev-btn
            flat
            v-else
            label="停止"
            @click="getStopPipeline(props.row.exeId)"
        /></template>
        <template v-else
          ><fdev-btn flat label="无权限" disable
        /></template>
      </fdev-td>
    </template>
  </fdev-table>
</template>

<script>
import {
  queryFdevciLogList,
  retryPipeline,
  stopPipeline
} from '../../services/method';
import { STATUS_CFG } from '../../utils/constants';
import { debounce } from '../../utils/utils';

export default {
  name: 'LogTable',
  data() {
    return {
      statusCfg: STATUS_CFG,
      timer: null,
      logData: [],
      logCol: [
        { name: 'pipelineNumber', label: '构建号', field: 'pipelineNumber' },
        {
          name: 'status',
          label: '状态',
          field: 'status'
        },
        { name: 'stages', label: '运行详情', field: 'stages' },
        { name: 'branch', label: '分支', field: 'branch', copy: true },
        {
          name: 'user',
          label: '构建人',
          field: row => {
            let { nameEn, nameCn } = row.user;
            return `${nameCn}（${nameEn}）`;
          }
        },
        {
          name: 'startTime',
          label: '开始时间',
          field: 'startTime',
          sortable: true
        },
        {
          name: 'costTime',
          label: '持续时间',
          field: 'costTime',
          sortable: true
        },
        {
          name: 'version',
          label: '流水线版本号',
          field: 'version',
          align: 'center'
        },
        { name: 'manage', label: '操作', field: 'exeId' }
      ],
      LogPagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      }
    };
  },
  props: { id: String, subtitle: String },
  computed: {
    queryData() {
      return (
        this.logData &&
        this.logData.length > 0 &&
        this.logData.some(x =>
          ['pending', 'waiting', 'running'].includes(x.status)
        )
      );
    }
  },
  watch: {
    queryData: {
      handler: function(val) {
        val
          ? (this.timer = setInterval(this.getLogData, 3000))
          : clearInterval(this.timer);
      },
      immediate: true
    },
    id() {
      this.getLogData();
    }
  },
  methods: {
    getStopPipeline(pipelineExeId) {
      debounce(() => {
        stopPipeline({ pipelineExeId });
      }, 500);
    },
    async getLogData(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.LogPagination.page = page;
        this.LogPagination.rowsPerPage = rowsPerPage;
      }
      let { page, rowsPerPage } = this.LogPagination;
      if (this.id) {
        let { pipelineExeList, total } = await queryFdevciLogList({
          pipelineId: this.id,
          pageNum: page,
          pageSize: rowsPerPage
        });
        this.logData = pipelineExeList;
        this.LogPagination.rowsNumber = total;
      } else {
        this.logData = [];
        this.LogPagination.rowsNumber = 0;
      }
    },

    getRetryPipeline(id) {
      debounce(() => {
        retryPipeline({ pipelineExeId: id });
        setTimeout(() => {
          this.LogPagination.page = 1;
          this.getLogData();
        }, 1000);
      }, 500);
    },
    goLogPanorama(id) {
      this.$router.push({
        name: 'logPanorama',
        params: { id }
      });
    },
    goJobLogPage(jobExes) {
      let id = jobExes.slice(jobExes.length - 1)[0].jobExeId;
      this.$router.push(`/configCI/jobLogProfile/${id}`);
    }
  },
  async mounted() {
    await this.getLogData();
  },
  beforeDestroy() {
    clearInterval(this.timer);
  }
};
</script>

<style lang="stylus" scoped>
.log-circle
  border-radius: 12px
  width 10px
  height 10px
.error-gradient
  background-image: linear-gradient(270deg, #EF9A9A 0%, #EF5350 100%);
.running-gradient
  background-image: linear-gradient(270deg, #9ED3FF 0%, #42A5F5 100%);
.jump-gradient
  background-image: linear-gradient(270deg, #B0C3CD 0%, #78909C 100%);
.cancel-gradient
  background-image: linear-gradient(270deg, #78909C 0%, #022140 100%);
.success-gradient
  background-image: linear-gradient(270deg, #A5D6A7 0%, #66BB6A 100%);
.waiting-gradient
  background-image: linear-gradient(270deg, #FFDDAC 0%, #FFA726 100%);
.pending-gradient
  background-image: linear-gradient(270deg, #FFDDAC 0%, #FFA726 100%);
</style>
