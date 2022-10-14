<template>
  <f-block>
    <div class="row no-wrap box f_cba">
      <div class="row no-wrap lbox">
        <div class="column no-wrap items-start">
          <div
            class="q-mb-md q-mr-md row justify-between no-wrap items-center"
            style="width:280px"
          >
            <div class="text-subtitle3 row no-wrap items-center">
              <f-icon
                name="list_s_f"
                class="text-primary q-mr-sm"
                :width="16"
                :height="16"
              />
              流水线列表
            </div>
            <fdev-btn
              normal
              ficon="add"
              label="新建流水线"
              @click="tempSelectDia = true"
            />
          </div>
          <fdev-btn-toggle
            v-if="!appId"
            :options="queryOpts"
            @input="updateQueryOpts"
            v-model="queryType"
            class="q-mb-md"
          ></fdev-btn-toggle>
          <fdev-input
            style="width:280px"
            class="q-mb-md"
            @keyup.enter="updateQueryOpts"
            placeholder="请输入流水线名称，所属应用"
            v-model="searchContent"
            ><template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="updateQueryOpts"
              /> </template
          ></fdev-input>
          <f-scrollarea
            class="q-pl-md q-py-sm"
            style="margin-left:-16px;padding-right:12px"
          >
            <div
              class="q-px-md q-pt-md q-pb-sm cursor-pointer column no-wrap pipeline-card q-mb-sm"
              :class="{ 'selected-card': selectedPipeId === pipeline.nameId }"
              v-for="pipeline in pipeListData"
              :key="pipeline.nameId"
              @click="selectPipe(pipeline)"
            >
              <div
                class="text-caption q-mb-sm row items-cemter justify-between"
              >
                <router-link
                  class="text-no-wrap text-subtitle2 text-ellipsis"
                  style="max-width:180px"
                  :title="pipeline.name"
                  :to="`/configCI/pipelineDetail/${pipeline.id}`"
                  >{{ pipeline.name }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ pipeline.name }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </router-link>
                <div class="row items-center no-wrap">
                  <div
                    class="bg-blue-7 text-subtitle4 cursor-default column items-center q-mr-xs first-name text-white"
                  >
                    {{ pipeline.author.nameCn[0] }}
                  </div>
                  <span class="cursor-text"> {{ pipeline.author.nameCn }}</span>
                </div>
              </div>
              <div
                class="row items-center q-mb-sm  no-wrap text-caption justify-between"
              >
                <div
                  class="row items-center cursor-text no-wrap "
                  style="max-width:180px"
                  :title="
                    `${pipeline.bindProject.nameEn}\n${
                      pipeline.bindProject.nameCn
                    }`
                  "
                >
                  <f-icon
                    name="app"
                    class="q-mr-xs text-primary"
                    width="13px"
                    height="13px"
                  />
                  <span class="text-no-wrap text-ellipsis">
                    {{ pipeline.bindProject.nameEn }}
                  </span>
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ pipeline.bindProject.nameEn }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </div>
                <span
                  class="cursor-text"
                  :title="`最近更新时间\n${pipeline.updateTime}`"
                  >{{
                    pipeline.updateTime
                      .split(' ')[0]
                      .replace('-', '/')
                      .replace('-', '/')
                  }}</span
                >
              </div>
              <div
                class="row no-wrap text-caption items-center justify-between"
              >
                <div class="row no-wrap items-center">
                  <progress-map
                    :percent="pipeline.successRate || '0.00%'"
                    class="q-mr-xs"
                  /><span class="cursor-text">成功率(%)</span>
                </div>
                <div class="row q-mt-xs no-wrap items-center text-primary">
                  <div
                    class="q-gutter-x-sm row no-wrap items-center q-mr-sm"
                    :class="{
                      'cursor-not-allowed text-grey-3': !pipeline.updateRight
                    }"
                  >
                    <div @click="runPipeline(pipeline)">
                      <f-icon name="run" :width="16" :height="16" />
                      <fdev-tooltip position="bottom">执行</fdev-tooltip>
                    </div>
                    <div>
                      <f-icon
                        name="copy"
                        @click="copyPipeline(pipeline)"
                        :width="16"
                        :height="16"
                      />
                      <fdev-tooltip position="bottom">复制</fdev-tooltip>
                    </div>
                    <div>
                      <f-icon
                        name="edit"
                        @click="toManage(pipeline)"
                        :width="16"
                        :height="16"
                      /><fdev-tooltip position="bottom">编辑</fdev-tooltip>
                    </div>
                    <div @click="confirmDelPipe(pipeline)">
                      <f-icon name="delete_o" :width="16" :height="16" />
                      <fdev-tooltip position="bottom">删除</fdev-tooltip>
                    </div>
                  </div>
                  <div @click="collectPipe(pipeline.id)">
                    <f-icon
                      :name="pipeline.collectedOrNot ? 'collect' : 'uncollect'"
                      :width="16"
                      :height="16"
                    />
                    <fdev-tooltip position="bottom">{{
                      pipeline.collectedOrNot ? '已收藏' : '收藏'
                    }}</fdev-tooltip>
                  </div>
                </div>
              </div>
            </div>
            <div
              class="cursor-pointer bg-grey-0 column show-more text-body2 row items-center"
              v-if="showMore"
              @click="loadMore"
            >
              <div class="row items-center">
                <f-icon
                  name="arrow_d_o"
                  :width="10"
                  :height="10"
                  class="q-mr-sm"
                />
                <span class="q-my-sm">展开更多</span>
              </div>
            </div>
          </f-scrollarea>
        </div>
        <fdev-separator vertical />
      </div>

      <log-table
        class="col f_fr"
        :id="selectedPipeId"
        :subtitle="selectedPipeName"
        ><div class="count-block full-width justify-around  row items-center">
          <div
            class="row items-center"
            v-for="count in countData"
            :key="count.label"
          >
            <div class="count-circle q-mr-md" />
            <div class="column items-start">
              <span class="text-subtitle2 q-mb-xs"
                >{{ count.value }}{{ count.unit }}</span
              ><span class="text-grey-3 text-caption">{{ count.label }}</span>
            </div>
          </div>
        </div>
      </log-table>
    </div>
    <SelectBranchDia
      noReplace
      :id="runPipelineData.id"
      v-if="runPipelineData"
      :pipeData="runPipelineData"
      v-model="selectBranchDia"
    />
    <temp-select v-model="tempSelectDia" :appId="appId" />
  </f-block>
</template>
<script>
import SelectBranchDia from '../../components/SelectBranchDia';
import LogTable from '../../components/LogTable';
import TempSelect from '../../components/TempSelect';
import ProgressMap from '../../components/ProgressMap';
import { textToClipboard } from '@/modules/configCI/utils/utils';
import { SAVE_USER_CFG_MIXIN } from '@/modules/configCI/utils/mixin';
import {
  queryAllPipelineList,
  queryMinePipelineList,
  queryCollectionPipelineList,
  queryAppPipelineList,
  copyPipeline,
  deletePipeline,
  updateFollowStatus
} from '../../services/method';

export default {
  name: 'PipelineList',
  mixins: [SAVE_USER_CFG_MIXIN],
  components: {
    LogTable,
    TempSelect,
    SelectBranchDia,
    ProgressMap
  },
  props: {
    appId: String
  },
  data() {
    return {
      saveUserCfg_: [
        'selectedPipeId',
        'searchContent',
        'queryType',
        'currentPage',
        'selectedPipeName'
      ],
      runPipelineData: null,
      selectBranchDia: false,
      tempSelectDia: false,
      selectedPipeId: '',
      selectedPipeName: '',
      searchContent: '',
      pipeListData: [],
      currentPage: 1,
      queryType: 'queryMinePipelineList',
      queryTypes: {
        queryAllPipelineList,
        queryMinePipelineList,
        queryCollectionPipelineList
      },
      queryOpts: [
        { label: '全部', value: 'queryAllPipelineList' },
        { label: '我负责的', value: 'queryMinePipelineList' },
        { label: '我收藏的', value: 'queryCollectionPipelineList' }
      ],
      total: 0
    };
  },
  computed: {
    showMore() {
      return this.total > this.pipeListData.length;
    },
    countData() {
      let ind = this.pipeListData.findIndex(
        x => x.nameId === this.selectedPipeId
      );
      let data = this.pipeListData[ind];
      let exeTotal, successExeTotal, errorExeTotal, successRate, aveErrorTime;
      data &&
        ({
          exeTotal,
          successExeTotal,
          errorExeTotal,
          successRate,
          aveErrorTime
        } = data);
      exeTotal || (exeTotal = '0');
      successExeTotal || (successExeTotal = '0');
      errorExeTotal || (errorExeTotal = '0');
      successRate || (successRate = '0.00%');
      aveErrorTime || (aveErrorTime = '0');
      return [
        { label: '总计', value: exeTotal, unit: '次' },
        { label: '成功', value: successExeTotal, unit: '次' },
        { label: '失败', value: errorExeTotal, unit: '次' },
        { label: '成功率', value: successRate, unit: '' },
        {
          label: '失败平均修复时长',
          value: aveErrorTime,
          unit: 'h'
        }
      ];
    }
  },
  methods: {
    textToClipboard,
    async loadMore() {
      this.currentPage = this.currentPage + 1;
      await this.getPipeListData(true);
    },
    async getPipeListData(append) {
      let query = this.appId
        ? queryAppPipelineList
        : this.queryTypes[this.queryType];
      let pageSize = append ? 10 : this.currentPage * 10;
      let pageNum = append ? this.currentPage : 1;
      let { pipelineList, total } = await query({
        pageNum,
        pageSize,
        searchContent: this.searchContent,
        applicationId: this.appId
      });
      this.pipeListData = append
        ? this.pipeListData.concat(pipelineList)
        : pipelineList;
      this.total = total;
    },
    async updateQueryOpts() {
      this.currentPage = 1;
      this.pipeListData = [];
      this.total = 0;
      await this.getPipeListData();
    },
    selectPipe(pipeline) {
      let { nameId, name } = pipeline;
      this.selectedPipeId = nameId;
      this.selectedPipeName = name;
    },
    toManage(pipeline) {
      let { id, fixedModeFlag, updateRight } = pipeline;
      if (updateRight) {
        let type = fixedModeFlag ? 'fixed' : 'pipeline';
        this.$router.push({
          path: `/configCI/pipelineManage/${id}/${type}`,
          query: { type: 'editPipeline' }
        });
      } else return;
    },
    runPipeline(pipelineData) {
      if (pipelineData.updateRight) {
        this.runPipelineData = pipelineData;
        this.selectBranchDia = true;
      } else return;
    },
    async copyPipeline(pipeline) {
      let { updateRight, fixedModeFlag, id } = pipeline;
      if (updateRight) {
        let cpId = await copyPipeline({ id });
        this.$q.notify({
          type: 'positive',
          message: '已成功复制流水线',
          position: 'top'
        });
        this.toManage({ id: cpId, fixedModeFlag, updateRight: true });
      } else return;
    },
    async delPipeline(pipelineId) {
      await deletePipeline({ pipelineId });
      this.$q.notify({
        type: 'positive',
        message: '已成功删除流水线',
        position: 'top'
      });
      await this.getPipeListData();
    },
    confirmDelPipe(pipeline) {
      let { id, updateRight } = pipeline;
      if (updateRight) {
        this.$q
          .dialog({
            title: '删除流水线',
            message: '是否确定删除本条流水线？',
            ok: '确定',
            cancel: '取消'
          })
          .onOk(() => this.delPipeline(id))
          .onCancel(() => {});
      } else return;
    },
    async collectPipe(pipelineId) {
      let rsp = await updateFollowStatus({ pipelineId });
      rsp === 'SUCCESS'
        ? await this.getPipeListData()
        : this.$q.notify({
            type: 'negative',
            message: '收藏操作失败，请重试',
            position: 'top'
          });
    }
  },
  async mounted() {
    // this.queryType = this.queryOpts.filter(
    //   x => x.label === this.queryType
    // )[0].value;
    await this.getPipeListData();
    if (
      !this.selectedPipeId &&
      this.pipeListData &&
      this.pipeListData.length > 0
    ) {
      this.selectPipe(this.pipeListData[0]);
    }
  }
};
</script>
<style scoped lang="stylus">

.pipeline-card
  box-shadow 0 2px 10px 0 rgba(21,101,192,0.20)
  border-radius 4px
  width 280px
.selected-card
  border solid 1px #0378EA
.first-name
  width 20px
  height 20px
  border-radius 10px
.count-block
  box-shadow 0 2px 10px 0 rgba(21,101,192,0.20)
  border-radius 8px
  height 88px
.count-circle
  height 12px
  width 12px
  border-radius 6px
  border solid 2px #0378EA
.show-more
  width 280px
  &:hover
    color #0663BE

.box{
	position: relative;
  min-height 600px
}
.f_cba:after{
  content: "";
  display: block;
  clear: both;
}
.f_fr{
  float: right;
  margin-left 320px
}
.lbox{
  position: absolute
  left: 0px
  width: 320px
  height: 100% - 6px
}
</style>
