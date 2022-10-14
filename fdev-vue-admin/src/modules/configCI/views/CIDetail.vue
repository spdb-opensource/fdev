<template>
  <div v-if="CIDetail">
    <div class="items-center row justify-between q-mb-md">
      <span class="page-title-style">流水线名称：{{ CIDetail.name }}</span>
      <div class="row items-center no-wrap">
        <div v-for="(item, index) in btns" :key="index">
          <fdev-btn
            normal
            class="q-mr-sm"
            :color="item.icon.indexOf('star') >= 0 ? 'orange-7' : 'blue-9'"
            :ficon="item.icon"
            :label="item.label"
            @click="item.callback"
          />
        </div>
        <fdev-btn-dropdown label="更多" v-if="CIDetail.updateRight">
          <fdev-list>
            <fdev-item clickable v-close-popup>
              <fdev-item-section>
                <fdev-item-label @click="getCopyPipeline"
                  >复制流水线</fdev-item-label
                >
              </fdev-item-section>
            </fdev-item>

            <fdev-item clickable v-close-popup>
              <fdev-item-section>
                <fdev-item-label @click="getDeletePipeline"
                  >删除流水线</fdev-item-label
                >
              </fdev-item-section>
            </fdev-item>
          </fdev-list>
        </fdev-btn-dropdown>
      </div>
    </div>
    <div class="main">
      <div class="col-5">
        <div class="row">
          <div class="col-3">
            <div class="row">
              <fdev-img
                :src="require('../assets/pipeline_icon.svg')"
                class="img-style"
              />
              <span class="text-subtitle1">基本信息</span>
            </div>
            <fdev-card class="q-mt-md">
              <fdev-card-section>
                <div class="app-main">
                  <div class="col-4  q-ml-md">
                    <fdev-img
                      :src="require('../assets/app.svg')"
                      class="img-card"
                    />
                    <div class="q-mt-sm">绑定应用</div>
                  </div>
                  <div class="q-mt-md app-right">
                    <div class="ellipsis">
                      {{ CIDetail.bindProject.nameEn }}
                      <fdev-tooltip
                        anchor="top middle"
                        self="center middle"
                        :offest="[-20, 0]"
                        v-if="CIDetail.bindProject.nameEn"
                      >
                        {{ CIDetail.bindProject.nameEn }}
                      </fdev-tooltip>
                    </div>
                    <div class="ellipsis">
                      {{ CIDetail.bindProject.nameCn }}
                      <fdev-tooltip
                        anchor="top middle"
                        self="center middle"
                        :offest="[-20, 0]"
                        v-if="CIDetail.bindProject.nameCn"
                      >
                        {{ CIDetail.bindProject.nameCn }}
                      </fdev-tooltip>
                    </div>
                  </div>
                </div>
              </fdev-card-section>
            </fdev-card>
            <fdev-card class="q-mt-md">
              <fdev-card-section>
                <div class="app-main">
                  <div class="col-4  q-ml-md">
                    <fdev-img
                      :src="require('../assets/trigger-rules.svg')"
                      class="img-card"
                    />
                    <div
                      class="q-mt-sm text-blue-8 cursor-pointer"
                      @click="goToTriggerRules"
                    >
                      触发规则
                    </div>
                  </div>
                  <div class="trigger-container scroll-thin app-right">
                    <span class="text-weight">定时触发</span><br />
                    <div v-for="(item, index) in scheduleParams" :key="index">
                      {{ item.cron }}
                    </div>
                    <span class="text-weight">push事件触发</span><br />
                    <div v-for="(item, index) in pushParams" :key="index.id">
                      <span v-if="item.branchName">
                        {{ item.branchType }}, {{ item.branchName }}
                      </span>
                    </div>
                  </div>
                </div>
              </fdev-card-section>
            </fdev-card>
          </div>
          <div class="col q-ml-md">
            <div class="row">
              <fdev-img
                :src="require('../assets/pipeline_icon.svg')"
                class="img-style"
              />
              <div class="text-subtitle1">当前版本预览图</div>
            </div>
            <PipelinePanorama :id="id" />
          </div>
        </div>
      </div>
      <!-- 页面下面两个表格区域 -->
      <div class="col">
        <div class="tab-main">
          <div class="tab-left"><HistoryVersion :id="id" /></div>
          <div class="tab-right"><LogList /></div>
        </div>
      </div>
    </div>
    <SelectBranchDia
      v-if="selectBranchDia"
      :id="id"
      :pipeData="CIDetail"
      v-model="selectBranchDia"
    />
  </div>
</template>
<script>
import SelectBranchDia from '../components/SelectBranchDia';
import { updateFollowStatus } from '../services/method';
import { mapState, mapActions } from 'vuex';
import { successNotify } from '@/utils/utils';
import HistoryVersion from '../components/HistoryVersion';
import LogList from '../components/logList';
import PipelinePanorama from './PipelinePanorama';
export default {
  name: 'CIDetail',
  components: {
    SelectBranchDia,
    HistoryVersion,
    LogList,
    PipelinePanorama
  },
  props: {
    id: String
  },
  data() {
    return {
      selectBranchDia: null,
      CIDetail: null
    };
  },
  computed: {
    ...mapState('configCIForm', [
      'saveAsPipeTemp',
      'pipelineDetail',
      'copyPipelineId'
    ]),
    scheduleParams() {
      return (
        this.CIDetail.triggerRules &&
        this.CIDetail.triggerRules.schedule &&
        this.CIDetail.triggerRules.schedule.scheduleParams
      );
    },
    pushParams() {
      return (
        this.CIDetail.triggerRules &&
        this.CIDetail.triggerRules.push &&
        this.CIDetail.triggerRules.push.pushParams
      );
    },
    bindApp() {
      let { nameEn, nameCn } = this.CIDetail.bindProject;
      return nameEn + '(' + nameCn + ')';
    },
    collectStatus() {
      return this.CIDetail
        ? this.CIDetail.collectedOrNot
          ? {
              label: '取消收藏',
              icon: 'star'
            }
          : { label: '收藏', icon: 'star_border' }
        : null;
    },
    btns() {
      let btns = [
        {
          ...this.collectStatus,
          style: 'text-orange',
          callback: this.updateCollect
        },
        {
          label: '返回',
          icon: 'exit',
          callback: this.goBackTable
        }
      ];
      this.CIDetail.updateRight &&
        btns.splice(
          1,
          0,
          {
            label: '执行',
            icon: 'run',
            callback: this.openSelectBranchDia
          },
          {
            label: '编辑',
            icon: 'edit',
            callback: this.toManage
          }
        );
      return btns;
    }
  },
  methods: {
    ...mapActions('configCIForm', [
      'saveAsPipelineTemplate',
      'queryPipelineDetailById',
      'copyPipeline',
      'deletePipeline'
    ]),
    goBackTable() {
      this.$router.back();
    },
    goToTriggerRules() {
      this.$router.push(`/configCI/triggerRule/${this.id}`);
    },
    async savePipelineTemplate() {
      await this.saveAsPipelineTemplate({ id: this.id });
      await successNotify('已成功复制为模板');
      this.$router.replace({
        path: `/configCI/pipelineManage/${this.saveAsPipeTemp}/template`
      });
    },
    async getCopyPipeline() {
      await this.copyPipeline({ id: this.id });
      this.$q.notify({
        type: 'positive',
        message: '已成功复制流水线',
        position: 'center'
      });
      this.$router.replace({
        path: `/configCI/pipelineManage/${this.copyPipelineId}/pipeline`,
        query: { type: 'editPipeline' }
      });
    },
    getDeletePipeline() {
      return this.$q
        .dialog({
          title: '删除流水线',
          message: '请确认是否删除该流水线',
          ok: '删除',
          cancel: '再想想'
        })
        .onOk(async () => {
          await this.deletePipeline({ pipelineId: this.id });
          successNotify('删除成功');
          this.$router.push('/configCI/pipelineLogTable');
        });
    },
    openSelectBranchDia() {
      this.selectBranchDia = true;
    },
    async updateCollect() {
      await updateFollowStatus({ pipelineId: this.id });
      await this.queryPipelineDetailById({ id: this.id });
      this.CIDetail = this.pipelineDetail;
    },
    toManage() {
      // fixed固定模式，只允许在原有结构修改，不可删除和新增
      let type = this.CIDetail.fixedModeFlag ? 'fixed' : 'pipeline';
      this.$router.push({
        path: `/configCI/pipelineManage/${this.id}/${type}`,
        query: { type: 'editPipeline' }
      });
    }
  },
  async mounted() {
    await this.queryPipelineDetailById({ id: this.id });
    this.CIDetail = this.pipelineDetail;
  }
};
</script>
<style scoped lang="stylus">
.component-fade-enter-active, .component-fade-leave-active {
  transition: opacity .2s ease;
}
.component-fade-enter, .component-fade-leave-to
/* .component-fade-leave-active for below version 2.1.8 */ {
  opacity: 0;
}
.page-title-style
  position relative
  font-size 16px
  color #333333
  line-height 36px
  font-weight 500
  max-width 500px
  text-overflow ellipsis
.img-style
  width 32px
  height 32px
  margin-top -2px
.img-card
  width 48px
  height 48px
.text-weight
 font-weight 500
.trigger-container
 max-height 130px
 height 130px
 overflow auto
 min-width 150px
.tab-main
  display flex
  margin-top 41px
.tab-left
  width 474px
  margin-right 16px
.tab-right
  flex 1
  overflow auto
.main
  flex-wrap: nowrap;
  flex-direction: column;
.app-right
  flex: 1
  padding: 0 24px;
  overflow: hidden;
.app-main
  display: flex;
</style>
