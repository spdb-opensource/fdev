<template>
  <f-dialog
    right
    title="选择流水线模板"
    v-if="tempData"
    :value="value"
    @input="$emit('input', false)"
  >
    <div class="column">
      <f-formitem label="搜索模板" class="self-end q-mb-md">
        <fdev-input
          placeholder="请输入模板名称"
          v-model="searchContent"
          @keyup.enter.native="jobFilter"
        >
          <template v-slot:append>
            <f-icon name="search" @click="jobFilter" class="cursor-pointer" />
          </template>
        </fdev-input>
      </f-formitem>
      <div class="row no-wrap">
        <fdev-tabs vertical class="sticky" v-model="tab">
          <fdev-tab
            v-for="tab in tabs"
            :key="tab.categoryId"
            :name="tab.categoryId"
            :label="tab.categoryName"
          />
        </fdev-tabs>
        <fdev-separator vertical class="q-mx-lg" />
        <fdev-tab-panels
          v-model="tab"
          animated
          swipeable
          vertical
          transition-prev="jump-up"
          transition-next="jump-up"
          style="height:100%"
        >
          <fdev-tab-panel
            class="q-pa-none q-gutter-y-md no-scroll"
            v-for="tab in tabs"
            :key="tab.categoryId"
            :name="tab.categoryId"
          >
            <div
              v-for="CIData in pipelineTemplateList"
              :key="CIData.id"
              @click="selectTemp(CIData)"
            >
              <TempPreView
                @deleteTemp="deleteTemp"
                @copyTemp="copyTemp"
                @editTemp="editTemp"
                :CIData="CIData"
                :class="[
                  CIData.id === selectedInd
                    ? 'selected-panel'
                    : 'unselected-panel'
                ]"
              />
            </div>
          </fdev-tab-panel>
        </fdev-tab-panels>
      </div>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" dialog outline @click="$emit('input', false)" />
      <fdev-btn
        label="创建"
        dialog
        :disable="!selectedInd"
        @click="createPipeline"
      />
    </template>
  </f-dialog>
</template>
<script>
import {
  queryMinePipelineTemplateList,
  delTemplate,
  copyPipelineTemplate
} from '../services/method';
import TempPreView from './TempPreView';
import { ResPrompt } from '../utils/utils';
import { successNotify } from '@/utils/utils';
export default {
  name: 'TempSelect',
  components: {
    TempPreView
  },
  props: {
    value: Boolean,
    appId: String
  },
  data() {
    return {
      tab: null,
      splitterModel: 18,
      tempData: null,
      selectedInd: undefined,
      fixedModeFlag: false,
      footBtns: { sure: this.createPipeline, cancel: this.closeDialog },
      searchContent: ''
    };
  },
  computed: {
    tabs() {
      return this.tempData.map(x => {
        return {
          categoryName: x.categoryName,
          categoryId: x.categoryId
        };
      });
    },
    pipelineTemplateList() {
      let tempData = this.tempData.filter(x => x.categoryId === this.tab);
      return tempData.length === 0 ? null : tempData[0].pipelineTemplateList;
    }
  },
  methods: {
    jobFilter() {
      this.queryMinePipelineTemplateList();
    },
    async queryMinePipelineTemplateList() {
      let response = await queryMinePipelineTemplateList({
        pageNum: '0',
        pageSize: '0',
        searchContent: this.searchContent
      });
      this.tempData = response.categoryList || [];
      (!this.tab || !this.pipelineTemplateList) &&
        (this.tab = this.tempData[0].categoryId);
    },
    async deleteTemp(evt) {
      await ResPrompt(
        delTemplate,
        { pipelineTemplateId: evt },
        this.queryMinePipelineTemplateList
      );
      successNotify('模板删除成功');
    },
    async copyTemp(id) {
      await ResPrompt(
        copyPipelineTemplate,
        { id: id },
        this.queryMinePipelineTemplateList
      );
      successNotify('模板复制成功');
    },
    editTemp(id) {
      this.$router.push({
        path: `/configCI/pipelineManage/${id}/template`
      });
    },
    selectTemp(data) {
      this.selectedInd = data.id;
      this.fixedModeFlag = data.fixedModeFlag;
    },
    closeDialog() {
      this.$emit('input', false);
    },
    createPipeline() {
      // fixed固定模式，只允许在原有结构修改，不可删除和新增
      let type = this.fixedModeFlag ? 'fixed' : 'temp';
      this.$emit('input', false);
      this.$router.push({
        path: `/configCI/pipelineManage/${this.selectedInd}/${type}`,
        query: { appId: this.appId }
      });
    }
  },
  async mounted() {
    await this.queryMinePipelineTemplateList();
  }
};
</script>
<style scoped lang="stylus">
.dialog-style {
  width: 1200px;
  max-width: 1200px !important;
  border-radius: 16px;
}

.temps {
  margin-top: 35px;
  height: 600px;
}

.foot-btn {
  width: 230px;
  margin-top: 35px;
}

.selected-panel {
  border: 2px solid #1565C0;
  background-color: #E3F2FD;
  cursor: pointer
}

.sticky
  position -webkit-sticky
  position sticky
  top 0
</style>
