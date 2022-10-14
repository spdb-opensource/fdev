<template>
  <div>
    <div class="column">
      <div class="row q-mb-lg justify-between">
        <span class="text-subtitle3">流水线模板列表</span>
        <div class="row no-wrap q-gutter-x-md">
          <f-formitem label="模板名称">
            <fdev-input v-model="searchContent" @keyup.enter.native="jobFilter">
              <template v-slot:append>
                <f-icon
                  name="search"
                  @click="jobFilter"
                  class="cursor-pointer"
                />
              </template>
            </fdev-input>
          </f-formitem>
          <fdev-btn
            normal
            ficon="add"
            label="新增流水线模板"
            @click="addTemp"
          />
        </div>
      </div>
      <div class="row no-wrap q-mt-lg">
        <fdev-tabs vertical v-model="tab">
          <fdev-tab
            v-for="tab in tabs"
            :key="tab.categoryId"
            :name="tab.categoryId"
            :label="tab.categoryName"
          />
        </fdev-tabs>
        <fdev-separator vertical class="q-mr-lg q-ml-xl" />
        <fdev-tab-panels
          v-model="tab"
          style="height:100%"
          animated
          swipeable
          vertical
          transition-prev="jump-up"
          transition-next="jump-up"
        >
          <fdev-tab-panel
            class="q-pa-none q-gutter-y-lg no-scroll text-subtitle1 panel-font"
            v-for="tab in tabs"
            :key="tab.categoryId"
            :name="tab.categoryId"
          >
            <div
              v-for="CIData in pipelineTemplateList"
              :key="CIData.id"
              @click="selectTemp(CIData.id)"
            >
              <TempPreView
                page
                @deleteTemp="deleteTemp"
                @copyTemp="copyTemp"
                @editTemp="editTemp"
                @selectRange="selectRange"
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
  </div>
</template>
<script>
import {
  queryMinePipelineTemplateList,
  delTemplate,
  copyPipelineTemplate
} from '../../services/method';
import TempPreView from '../../components/TempPreView';
import { ResPrompt } from '../../utils/utils';
import { mapState, mapActions } from 'vuex';
import { successNotify, resolveResponseError } from '@/utils/utils';
import { SAVE_USER_CFG_MIXIN } from '@/modules/configCI/utils/mixin';

export default {
  name: 'pipelineTemp',
  mixins: [SAVE_USER_CFG_MIXIN],
  components: {
    TempPreView
  },
  props: {
    value: Boolean,
    appId: String
  },
  data() {
    return {
      saveUserCfg_: ['searchContent', 'tab'],
      tab: null,
      splitterModel: 18,
      tempData: [],
      selectedInd: undefined,
      searchContent: ''
    };
  },
  computed: {
    ...mapState('configCIForm', ['visibleRangeData']),
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
    ...mapActions('configCIForm', ['updateVisibleRange']),
    addTemp() {
      // 603f1ca8e18171d090b1c012 空模板id 固定
      this.$router.push({
        path: `/configCI/pipelineManage/603f1ca8e18171d090b1c012/addTemplate`,
        query: {
          categoryName: this.tabs.find(item => item.categoryId === this.tab)
            .categoryName,
          categoryId: this.tab
        }
      });
    },
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
      if (!this.tab) {
        this.tab = this.tempData.length ? this.tempData[0].categoryId : '';
      }
    },
    async deleteTemp(evt) {
      await resolveResponseError(() =>
        ResPrompt(
          delTemplate,
          { pipelineTemplateId: evt },
          this.queryMinePipelineTemplateList
        )
      );
      successNotify('模板删除成功');
    },
    async copyTemp(id) {
      await resolveResponseError(() =>
        ResPrompt(
          copyPipelineTemplate,
          { id: id },
          this.queryMinePipelineTemplateList
        )
      );
      successNotify('模板复制成功');
    },
    editTemp(id) {
      this.$router.push({
        path: `/configCI/pipelineManage/${id}/template`,
        query: {
          categoryName: this.tabs.find(item => item.categoryId === this.tab)
            .categoryName,
          categoryId: this.tab
        }
      });
    },
    selectTemp(id) {
      this.selectedInd = id;
    },
    async selectRange(data, id) {
      await this.updateVisibleRange({
        id: id,
        visibleRange: data
      });
      successNotify('更新成功');
    }
  },
  async mounted() {
    await this.queryMinePipelineTemplateList();
  }
};
</script>
<style scoped lang="stylus">

.temps {
  margin-top: 35px;
  height: 600px;
}

.foot-btn {
  width: 230px;
  margin-top: 35px;
}

.align-right
  position: absolute;
  right: 1%;
.panel-font
  color: #333333;
  letter-spacing: 0;
</style>
