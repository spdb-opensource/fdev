<template>
  <Loading :visible="loading">
    <fdev-table
      title="流水线列表"
      :data="piplineData"
      :columns="columns"
      :rows-per-page-options="[5, 10, 20, 25, 50, 100]"
      :pagination.sync="pagination"
      @request="changePagination"
    >
      <template v-slot:top-right>
        <fdev-btn
          label="新建流水线"
          normal
          @click="tempSelect = true"
          ficon="add"
        />
      </template>
      <template v-slot:top-bottom>
        <f-formitem page label="筛选">
          <fdev-select @input="queryNow" :value="model" :options="options" />
        </f-formitem>
        <f-formitem page label="搜索">
          <fdev-input
            placeholder="流水线名称,所属应用"
            v-model="searchContent"
            @keyup.enter="searchPipeline"
            clearable
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="searchPipeline"
              />
            </template>
          </fdev-input>
        </f-formitem>
      </template>
      <template v-slot:title-icon>
        <fdev-img :src="require('../assets/pipeline_icon.svg')" />
      </template>
      <template v-slot:body="props">
        <fdev-tr :props="props">
          <fdev-td key="select">
            <fdev-radio v-model="rowSelected" :val="props.row" />
          </fdev-td>
          <fdev-td
            key="name"
            class="text-ellipsis"
            :title="props.row.name"
            :props="props"
            @click="queryLog(props.row)"
          >
            <router-link :to="`/configCI/pipelineDetail/${props.row.id}`">{{
              props.row.name
            }}</router-link>
          </fdev-td>
          <fdev-td
            key="bindProjectName"
            :title="
              `props.row.bindProject.nameEn(${props.row.bindProject.nameCn})`
            "
            class="text-ellipsis"
            :props="props"
          >
            {{ props.row.bindProject.nameEn }}({{
              props.row.bindProject.nameCn
            }})
          </fdev-td>
          <fdev-td key="author" :props="props">
            {{ props.row.author.nameCn }}({{ props.row.author.nameEn }})
          </fdev-td>
          <fdev-td key="updateTime" :props="props">
            {{ props.row.updateTime }}
          </fdev-td>
          <fdev-td key="manage" :props="props">
            <fdev-btn
              flat
              label="复制ID"
              @click="textToClipboard(props.row.id)"
            />
          </fdev-td>
        </fdev-tr>
      </template>
      <template v-slot:pagination="scope">
        <div>
          {{ `${scope.pagination.page}/${scope.pagesNumber}` }}
        </div>
        <fdev-btn
          icon="chevron_left"
          color="grey-8"
          round
          dense
          flat
          :disable="scope.isFirstPage"
          @click="scope.prevPage"
        />
        <fdev-btn
          icon="chevron_right"
          color="grey-8"
          round
          dense
          flat
          :disable="scope.isLastPage"
          @click="scope.nextPage"
        />
      </template>
    </fdev-table>
    <TempSelect v-model="tempSelect" />
  </Loading>
</template>
<script>
import TempSelect from '../components/TempSelect';
import { textToClipboard } from '@/modules/configCI/utils/utils';
import { SAVE_USER_CFG_MIXIN } from '@/modules/configCI/utils/mixin';
import {
  queryAllPipelineList,
  queryMinePipelineList,
  queryCollectionPipelineList
} from '../services/method';
import { ResPrompt } from '../utils/utils';
import Loading from '@/components/Loading';
export default {
  name: 'PipelineTable',
  mixins: [SAVE_USER_CFG_MIXIN],
  components: {
    TempSelect,
    Loading
  },
  async mounted() {
    await this.queryNow();
  },
  data() {
    return {
      saveUserCfg_: ['searchContent', 'model', 'firstId'],
      firstId: '',
      queryFunctions: {
        queryAllPipelineList: queryAllPipelineList,
        queryMinePipelineList: queryMinePipelineList,
        queryCollectionPipelineList: queryCollectionPipelineList
      },
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 10
      },
      loading: false,
      tempSelect: false,
      piplineData: [],
      model: {
        label: '我负责的流水线',
        value: 'queryMinePipelineList'
      },
      searchContent: '',
      options: [
        { label: '我收藏的流水线', value: 'queryCollectionPipelineList' },
        { label: '我负责的流水线', value: 'queryMinePipelineList' },
        { label: '全部流水线', value: 'queryAllPipelineList' }
      ],
      cllectStatus: {
        0: { icon: 'star_border', label: '收藏' },
        1: { icon: 'star', label: '取消收藏' }
      },
      columns: [
        { name: 'select', label: '选择' },
        { name: 'name', label: '流水线名称' },
        { name: 'bindProjectName', label: '所属应用' },
        { name: 'author', label: '修改人', align: 'left' },
        { name: 'updateTime', label: '最近更新时间' },
        { name: 'manage', label: '操作' }
      ],
      rowSelected: ''
    };
  },
  computed: {
    nowQuery() {
      return this.queryFunctions[this.model.value];
    }
  },
  watch: {
    searchContent(val) {
      if (!val) {
        this.queryNow();
      }
    },
    rowSelected(val) {
      if (val) {
        this.queryLog(val);
      }
    }
  },
  methods: {
    textToClipboard,
    changePagination(props) {
      let { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.queryNow();
    },
    queryLog(data) {
      this.rowSelected = data;
      this.piplelineId = data.id;
      this.firstId = data.id;
      this.$emit('clickPipeline', data.id);
    },
    async searchPipeline() {
      await this.queryNow();
    },
    async queryNow(evt) {
      evt ? (this.model = evt) : null;
      let { searchContent } = this;
      let params = {
        pageNum: this.pagination.page,
        pageSize: this.pagination.rowsPerPage,
        searchContent
      };
      this.loading = true;
      try {
        let response = await ResPrompt(this.nowQuery, params);
        this.piplineData = response.pipelineList || [];
        this.pagination.rowsNumber = response.total;
        // 获取表格第一条流水线id,默认展示第一条流水线的运行日志
        this.firstId ||
          (this.firstId = this.piplineData.length
            ? this.piplineData[0].id
            : '');
        this.rowSelected = this.piplineData.length
          ? this.piplineData.filter(item => item.id === this.firstId)[0]
          : '';
        this.$emit('getFirstData', this.firstId);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
<style scoped lang="stylus">
a
  text-decoration none
  color #1976D2
</style>
