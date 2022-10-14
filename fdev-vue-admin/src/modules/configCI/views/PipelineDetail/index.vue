<template>
  <div class="column">
    <f-block block class="q-mb-md column no-wrap" style="height:500px">
      <div
        class="row no-wrap items-center justify-between title"
        v-if="pipelineData"
      >
        <span
          class="text-subtitle3 text-ellipsis text-no-wrap"
          style="max-width:600px"
          :title="name"
          >流水线: {{ name }}</span
        >
        <div class="row no-wrap items-center q-gutter-x-md">
          <template v-if="pipelineData.updateRight">
            <fdev-btn
              normal
              @click="selectBranchDia = true"
              label="执行"
              ficon="run"
            />
            <fdev-btn
              normal
              @click="toManage(id)"
              label="编辑"
              ficon="compile"
            />
            <fdev-btn normal @click="copyPipeline" label="复制" ficon="copy" />
            <fdev-btn
              normal
              @click="confirmDelPipe"
              label="删除"
              ficon="delete_o"
            />
          </template>
          <fdev-btn
            dialog
            @click="collectPipe"
            :label="collectFlag ? '已收藏' : '收藏'"
            :ficon="collectFlag ? 'collect' : 'uncollect'"
          />
        </div>
      </div>
      <div class="row no-wrap" v-if="pipelineData">
        <div class="column q-mr-lg">
          <div class="q-mb-lg row items-center">
            <f-icon
              name="log_s_f"
              :width="16"
              :height="16"
              class="text-primary"
            /><span class="text-subtitle3 q-ml-sm">基本信息</span>
          </div>
          <div
            class="message-block project-block q-ml-sm q-pl-lg q-mb-md row no-wrap q-py-md q-pr-md items-center"
          >
            <div class="column items-center no-wrap">
              <f-image name="bind_blue_c" />
              <span class="text-subtitle2 text-no-wrap q-mt-sm">绑定应用</span>
            </div>
            <f-scrollarea
              class="column text-caption project-block-child q-ml-md"
            >
              <span class="q-mb-sm">{{ bindProject.nameEn }}</span
              ><br />
              <span class="q-mb-sm">{{ bindProject.nameCn }}</span>
            </f-scrollarea>
          </div>
          <div
            class="message-block q-ml-sm row no-wrap q-pl-lg q-py-md q-pr-md trigger-block"
          >
            <div
              class="column items-center q-mr-md no-wrap cursor-pointer"
              @click="goTriggerRules"
            >
              <f-image name="rule_orange_c" />
              <span class="text-subtitle2 text-no-wrap q-mt-smy">触发规则</span>
              <span class="text-subtitle4 text-no-wrap text-primary"
                >（点此设置）</span
              >
            </div>
            <f-scrollarea
              bothXY
              class="column no-wrap trigger-block-child full-width"
            >
              <div v-if="schedule" class="column no-wrap q-mb-sm">
                <span class="text-subtitle4 q-mb-xs">定时触发</span>
                <div
                  v-for="(p, i) in schedule"
                  :key="i"
                  class="text-caption q-mb-xs"
                >
                  {{ branchCn[p.branchType] }}：{{ p.branchName }}<br />
                  cron：<br /><span class="text-no-wrap">{{ p.cron }}</span>
                </div>
              </div>
              <div v-if="push" class="column no-wrap">
                <span class="text-subtitle4 q-mb-xs">Push事件触发</span>
                <div
                  v-for="(p, i) in push"
                  :key="i"
                  class="text-caption q-mb-xs"
                >
                  {{ branchCn[p.branchType] }}：{{ p.branchName }}
                </div>
              </div>
              <span v-if="!(push || schedule)" class="text-caption q-ma-sm"
                >暂未配置触发规则，点击左侧图标去配置</span
              >
            </f-scrollarea>
          </div>
        </div>
        <div class="column no-wrap col" style="width:0">
          <div class="q-mb-md row items-center">
            <f-icon
              name="pic_s_f"
              :width="16"
              :height="16"
              class="text-primary"
            /><span class="text-subtitle3 q-ml-sm q-mr-md">当前版本预览图</span>
            <fdev-btn
              ficon="eye"
              label="详情"
              flat
              @click="goPipelineDetail(id)"
            />
          </div>
          <DetailPic :graphData="previewData" style="height:338px" />
        </div>
      </div>
    </f-block>
    <div class="row no-wrap q-mb-md full-width">
      <f-block block class="q-mr-md" style="width:530px">
        <fdev-table
          class="my-sticky-column-table"
          :columns="versionCol"
          :data="versionData"
          title="版本记录"
          noExport
          titleIcon="log_s_f"
        >
          <template v-slot:body-cell-version="props">
            <fdev-td :props="props">
              <fdev-btn
                flat
                :label="props.row.version"
                @click="goPipelineDetail(props.row.id)"
              />
            </fdev-td>
          </template>
          <template v-slot:body-cell-diffEntity="props">
            <fdev-td :props="props">
              <fdev-btn flat label="查看">
                <fdev-menu>
                  <div class="diffEntity-block column no-wrap">
                    <span class="text-subtitle3 q-mb-md">修改动态</span>
                    <div
                      v-if="
                        props.row.diffEntity &&
                          props.row.diffEntity.diff &&
                          props.row.diffEntity.diff.length > 0
                      "
                      class="column no-wrap"
                    >
                      <div
                        v-for="(diff, i) in props.row.diffEntity.diff"
                        :key="i"
                        class="q-mb-sm"
                      >
                        {{ diff.diffLabel }}：{{ diff.diffInfo }}
                      </div>
                    </div>
                    <div v-else class="row items-center">
                      <f-icon
                        width="12px"
                        name="alert_t_f"
                        class="q-mr-xs text-orange-7"
                      /><span>暂无动态</span>
                    </div>
                  </div>
                </fdev-menu>
              </fdev-btn>
            </fdev-td>
          </template>
        </fdev-table>
      </f-block>
      <f-block block class="col">
        <log-table :id="id" />
      </f-block>
    </div>
    <SelectBranchDia
      noReplace
      :id="id"
      v-if="pipelineData"
      :pipeData="pipelineData"
      v-model="selectBranchDia"
    />
  </div>
</template>

<script>
import {
  queryPipelineDetailById,
  getPipelineHistoryVersion,
  copyPipeline,
  deletePipeline,
  updateFollowStatus
} from '../../services/method';
import DetailPic from './DetailPic';
import LogTable from '../../components/LogTable';
import SelectBranchDia from '../../components/SelectBranchDia';
export default {
  name: 'PipelineDetail',
  props: {
    id: String
  },
  components: { DetailPic, SelectBranchDia, LogTable },
  data() {
    return {
      branchCn: { tag: '标签', branch: '分支' },
      selectBranchDia: false,
      timer: null,
      pipelineData: null,
      name: null,
      bindProject: null,
      push: null,
      schedule: null,
      previewData: null,
      versionData: [],
      collectFlag: null,
      versionCol: [
        {
          name: 'version',
          field: 'version',
          label: '版本号'
        },
        {
          name: 'author',
          field: row => row.author.nameCn,
          label: '更新人'
        },
        {
          name: 'updateTime',
          field: row => row.updateTime.split(' ')[0],
          label: '更新时间'
        },
        {
          name: 'diffEntity',
          field: 'diffEntity',
          label: '改动记录'
        }
      ]
    };
  },
  async mounted() {
    this.pipelineData = await queryPipelineDetailById({ id: this.id });
    let {
      name,
      bindProject,
      triggerRules,
      stages,
      collectedOrNot
    } = this.pipelineData;
    this.name = name;
    this.collectFlag = collectedOrNot;
    this.bindProject = bindProject;
    if (triggerRules) {
      let { push, schedule } = triggerRules;
      push && push.switchFlag && (this.push = push.pushParams);
      schedule &&
        schedule.switchFlag &&
        (this.schedule = schedule.scheduleParams);
    }
    this.previewData = stages;
    this.versionData = await getPipelineHistoryVersion({ pipelineId: this.id });
  },
  methods: {
    toManage(pId) {
      let type = this.pipelineData.fixedModeFlag ? 'fixed' : 'pipeline';
      this.$router.replace({
        path: `/configCI/pipelineManage/${pId}/${type}`,
        query: { type: 'editPipeline', fromDetail: true }
      });
    },
    goPipelineDetail(id) {
      this.$router.push(`/configCI/historyPanorama/${id}`);
    },
    goTriggerRules() {
      this.$router.push(`/configCI/triggerRule/${this.id}`);
    },
    async copyPipeline() {
      let cpId = await copyPipeline({ id: this.id });
      this.$q.notify({
        type: 'positive',
        message: '已成功复制流水线',
        position: 'top'
      });
      this.toManage(cpId);
    },
    async delPipeline() {
      await deletePipeline({ pipelineId: this.id });
      this.$q.notify({
        type: 'positive',
        message: '已成功删除流水线',
        position: 'top'
      });
      this.$router.back();
    },
    confirmDelPipe() {
      this.$q
        .dialog({
          title: '删除流水线',
          message: '是否确定删除本条流水线？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(this.delPipeline)
        .onCancel(() => {});
    },
    async collectPipe() {
      let rsp = await updateFollowStatus({ pipelineId: this.id });
      rsp === 'SUCCESS'
        ? (this.collectFlag = !this.collectFlag)
        : this.$q.notify({
            type: 'negative',
            message: '收藏操作失败，请重试',
            position: 'top'
          });
    }
  }
};
</script>

<style lang="stylus" scoped>
.title
  margin-bottom 32px
.message-block
  box-shadow 0 2px 10px 0 rgba(21,101,192,0.20)
  border-radius 8px
  width 234px
.project-block
  height 110px
.project-block-child
  max-height 78px
.trigger-block
  max-height 200px
.trigger-block-child
  max-height 170px
.pic-block
  height 346px
.diffEntity-block
  padding 32px
</style>
