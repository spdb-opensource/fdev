<template>
  <fdev-table
    title="运行日志列表"
    :data="tableData"
    :columns="columns"
    flat
    :pagination.sync="pagination"
    @request="query"
    class="my-sticky-column-table"
    noExport
  >
    <template v-slot:title-icon>
      <fdev-img :src="require('../assets/run_tip.svg')" />
    </template>
    <template v-slot:body="props">
      <fdev-tr :props="props">
        <fdev-td key="pipelineNumber" :props="props">
          <router-link
            class="link"
            :to="{
              name: 'logPanorama',
              params: { id: props.row.exeId }
            }"
            >{{ props.row.pipelineNumber }}</router-link
          >
        </fdev-td>
        <fdev-td key="status" :props="props" class="cursor-pointer">
          <fdev-badge
            :label="props.row.status"
            outline
            class="text-subtitle2 bandge-class"
            :style="{ color: ciColors[props.row.status] }"
            @click="toLogPanorama(props.row.exeId)"
          />
        </fdev-td>
        <fdev-td key="stages" :props="props">
          <div
            class="icon-wrapper"
            v-for="item in props.row.stages"
            :key="item.id"
          >
            <span v-if="item.status === 'waiting'">
              <img
                src="../assets/pending.png"
                class="radius img-width img-margin"
              />
              <fdev-menu anchor="bottom right" :offset="[-80, 10]">
                <fdev-list>
                  <fdev-item
                    clickable
                    v-close-popup
                    @click="jump(item.jobs[0].jobExes)"
                  >
                    <fdev-item-section side>
                      <img
                        src="../assets/pending.png"
                        class="radius img-width"
                      />
                    </fdev-item-section>
                    <fdev-item-section>
                      {{ item.name }}
                    </fdev-item-section>
                  </fdev-item>
                </fdev-list>
              </fdev-menu>
            </span>
            <fdev-icon
              v-else
              class="radius"
              :name="icon[item.status]"
              :class="[item.status, 'cursor-pointer']"
            >
              <fdev-menu anchor="bottom right" :offset="[-80, 10]">
                <fdev-list>
                  <fdev-item
                    clickable
                    v-close-popup
                    @click="jump(item.jobs[0].jobExes)"
                  >
                    <fdev-item-section side>
                      <fdev-icon
                        class="radius"
                        :name="icon[item.status]"
                        :class="item.status"
                      />
                    </fdev-item-section>
                    <fdev-item-section>
                      {{ item.name }}
                    </fdev-item-section>
                  </fdev-item>
                </fdev-list>
              </fdev-menu>
            </fdev-icon>
            <fdev-tooltip anchor="top middle" :offset="[10, 25]">
              {{ item.name }}: {{ item.status | pass }}
            </fdev-tooltip>
          </div>
        </fdev-td>
        <fdev-td key="branch" :props="props">
          {{ props.row.branch }}
        </fdev-td>
        <fdev-td key="app" :props="props">
          {{ props.row.bindProject.nameCn }} <br />
          {{ props.row.bindProject.nameEn }}
        </fdev-td>
        <fdev-td key="user" :props="props">
          {{ props.row.user.nameCn }}<br />
          {{ props.row.user.nameEn }}
        </fdev-td>
        <fdev-td key="costTime" :props="props">
          {{ props.row.costTime }} <br />
          {{ props.row.startTime }}
        </fdev-td>
        <fdev-td key="retry" :props="props">
          <fdev-btn
            v-if="
              props.row.status === 'pending' || props.row.status === 'running'
            "
            icon="o_stop_circle"
            round
            flat
            size="md"
            :ripple="false"
            dense
            :disabled="props.row.retry === '0'"
            color="grey-9"
            @click="cancelPipeline(props.row.exeId)"
          >
            <fdev-tooltip
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>停止</span>
            </fdev-tooltip>
          </fdev-btn>
          <fdev-btn
            v-else
            icon="cached"
            round
            flat
            color="negative"
            size="md"
            :disabled="props.row.retry === '0'"
            :ripple="false"
            dense
            @click="retryPipelineRun(props.row.exeId)"
          >
            <fdev-tooltip
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>重试</span>
            </fdev-tooltip>
          </fdev-btn>
        </fdev-td>
      </fdev-tr>
    </template>
  </fdev-table>
</template>
<script>
import { CI_COLORS, LOG_REFREASH } from '../utils/constants';
import { queryFdevciLogList } from '../services/method';
import { ResPrompt } from '../utils/utils';
import { mapState, mapActions } from 'vuex';
import { icon } from '@/modules/App/utils/constants';

export default {
  name: 'LogList',
  beforeDestroy() {
    //清除定时器
    clearInterval(this.timer);
  },
  methods: {
    ...mapActions('configCIForm', ['stopPipeline', 'retryPipeline']),
    searhPipeline() {},
    async query(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      await this.queryFdevciLogList();
    },
    async queryFdevciLogList() {
      let pipelineId = this.$route.params.id;
      this.logList = await ResPrompt(queryFdevciLogList, {
        pipelineId: pipelineId ? pipelineId : '',
        pageNum: this.pagination.page,
        pageSize: this.pagination.rowsPerPage
      });
      this.pagination.rowsNumber = this.logList.total;
      this.tableData = this.logList.pipelineExeList;
    },

    async retryMethods() {
      clearInterval(this.timer);
      await this.queryFdevciLogList();
      if (!this.stopTimer) {
        this.timer = setInterval(this.queryFdevciLogList, LOG_REFREASH);
      }
    },
    async retryPipelineRun(id) {
      await this.retryPipeline({
        pipelineExeId: id
      });
      this.pagination.page = 1;
      this.retryMethods();
    },
    async cancelPipeline(id) {
      await this.stopPipeline({
        pipelineExeId: id
      });
      this.queryFdevciLogList();
    },
    jump(jobExes) {
      let id = jobExes.slice(jobExes.length - 1)[0].jobExeId;
      this.$router.push(`/configCI/jobLogProfile/${id}`);
    },
    toLogPanorama(id) {
      this.$router.push({
        name: 'logPanorama',
        params: { id }
      });
    }
  },
  async mounted() {
    await this.queryFdevciLogList();
    if (!this.stopTimer) {
      this.timer = setInterval(this.queryFdevciLogList, LOG_REFREASH);
    }
  },
  filters: {
    pass(val) {
      return val === 'success' ? 'pass' : val;
    }
  },
  data() {
    return {
      timer: null,
      logList: null,
      ciColors: CI_COLORS,
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      tableData: [],
      //构建号，状态，分支，运行详情，构建人，持续时间，开始时间
      columns: [
        { name: 'pipelineNumber', label: '构建号' },
        {
          name: 'status',
          label: '状态'
        },
        { name: 'branch', label: '分支' },
        { name: 'stages', label: '运行详情' },
        { name: 'app', label: '所属应用' },
        { name: 'user', label: '构建人' },
        { name: 'costTime', label: '持续/开始时间' },
        { name: 'retry', label: '操作' }
      ].map(x => Object.assign(x, { align: 'center' })),
      icon: icon
    };
  },
  watch: {
    //如果运行失败，清除定时器
    stopTimer(val) {
      val && clearInterval(this.timer);
    }
  },
  computed: {
    ...mapState('configCIForm', ['stopPipelineInfo', 'FdevciLogList']),
    stopTimer() {
      return this.tableData
        ? this.tableData.every(x =>
            ['error', 'success', 'cancel'].includes(x.status)
          )
        : false;
    },
    // 流水线详情页
    isPanorama() {
      return this.$route.path.includes('panorama');
    }
  }
};
</script>
<style scoped lang="stylus">
/deep/.block
  // font-size 14px
  font-weight 500
  line-height 22px
a
  text-decoration none
  color #1976D2
.bandge-class
  height 34px
.radius
  border-radius 50%
.icon-wrapper
  display inline-block
.icon-wrapper > i
  font-size 25px
.stages
  font-size 24px
.success, .succeeded {
  color: #1aaa55;
}
.canceled {
  color: #2e2e2e;
}
.failed, .error {
  color: #db3b21;
}
.running, .process {
  color: #1f78d1;
}
.pending {
  color: #1f78d1;
}
.waiting {
  color: #FD9405;
}
.skipped {
  color: #a7a7a7;
  border: 1px solid #a7a7a7;
  cursor: pointer;
  width: 18px;
  height: 18px;
}
/deep/.q-field__control
  height 40px
  min-height 0px
  padding-left 16px
/deep/.q-field__marginal
  height 40px
  color #1565C0
.img-width
 width 23px
 height 23px
.img-margin
 position relative
 top 8px
</style>
