<template>
  <div class="tempWrapper">
    <div class="inpt">
      <fdev-input v-model="searchTemp" label="模板名称、描述、标签">
        <template v-slot:append>
          <f-icon name="search" @click="searchData" />
        </template>
      </fdev-input>
    </div>
    <h5 class="checkTemp">
      请选项目流水线模板
      <!-- <span @click="createTemp"><img src="@/assets/icon-add.png" />新建模版</span> -->
    </h5>
    <fdev-table
      key="tempList"
      class="my-sticky-column-table"
      :data="tempListData"
      :columns="tempColumns"
      selection="single"
      :selected.sync="selected"
      row-key="id"
      :pagination.sync="tempsPagination"
      separator="cell"
      @request="queryTemps"
      noExport
    >
      <template v-slot:body-cell-name="props">
        <fdev-td class="text-center">
          <router-link
            class="link"
            :to="`/configCI/CIDetail/${props.row.id}/temp`"
          >
            {{ props.row.name }}
          </router-link>
        </fdev-td>
      </template>

      <template v-slot:body-cell-desc="props">
        <fdev-td class="text-ellipsis" :title="props.row.desc">
          {{ props.row.desc }}
        </fdev-td>
      </template>

      <template v-slot:body-cell-label="props">
        <fdev-td class="text-center">
          <span v-for="(val, index) in props.row.label" :key="index">
            {{ val }}
          </span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btns="props">
        <fdev-td class="text-center">
          <div class="icon-wrapper">
            <f-icon class="icon-size" name="visibility" />
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <div class="next">
      <fdev-btn label="取消" @click="cancelPick" />
      <fdev-btn label="下一步" @click="toNext" />
    </div>
  </div>
</template>

<script>
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { selectTempColumns } from '@/modules/App/utils/constants';
export default {
  name: '',
  components: {},
  props: ['applicationId', 'appPipelineShow'],
  filters: {},
  data() {
    return {
      searchTemp: '',
      selected: [],
      tempsPagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      tempColumns: selectTempColumns(),
      tempListData: []
    };
  },
  computed: {
    ...mapState('userActionSavePipelinesManage/pipelinesManage', [
      'backFromDetail',
      'isAdd',
      'showTabs'
    ]),
    ...mapState('configCIForm', ['PipelineTemplateList']),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapGetters('user', ['isSpecialGroup'])
  },
  watch: {
    selected: {
      handler(val) {},
      deep: true
    }
  },
  methods: {
    ...mapMutations('userActionSavePipelinesManage/pipelinesManage', [
      'updateBackFromDetail',
      'updateIsAdd',
      'updateShowTabs',
      'updateshowList'
    ]),
    ...mapActions('configCIForm', ['queryMinePipelineTemplateList']),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    searchData() {
      this.init();
    },
    cancelPick() {
      this.updateshowList(true); // 控制父组件中class="linesWrapper"的div
      this.$emit('showTabs');
      this.updateShowTabs(true); // 控制父组件的父组件的tab页
      this.updateIsAdd(false); // 删除从“新增流水线进入”的标记
      this.updateBackFromDetail(false); // 删除“返回标记”
      this.selected = [];
    },
    createTemp() {
      // this.toNext({ from: 'temp', to: 'temp' });
      this.$router.push({ path: `/configCI/CIManage/emptyTemp/temp/temp` });
    },
    toNext({ from = 'temp', to = 'pipeline' }) {
      if (this.selected.length > 0) {
        let id = this.selected[0].id;
        this.$router.push({
          path: '/configCI/CIManage/' + id + '/' + from + '/' + to
        });
      } else {
        this.$q.notify({
          message: '请先选择模板',
          position: 'top',
          timeout: 2000,
          color: 'positive'
        });
      }
    },
    async queryTemps(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.tempsPagination.page = page;
        this.tempsPagination.rowsPerPage = rowsPerPage;
      }
      this.init();
    },
    async init() {
      let params = {
        searchContent: this.searchTemp,
        pageNum: this.tempsPagination.page,
        pageSize: this.tempsPagination.rowsPerPage
      };
      await this.queryMinePipelineTemplateList(params);
      this.tempListData = this.PipelineTemplateList.templateList;
      this.tempsPagination.rowsNumber = this.PipelineTemplateList.total;
    }
  },
  async created() {
    await this.init();
  }
};
</script>
<style lang="stylus" scoped>
.icon-wrapper {
  display: inline-block;
  margin: 0 8px;
}

.icon-size {
    font-size: 1.3em;
}

.menus span {
  color: #1A76D2;
  cursor: pointer;
}

.menus > img {
  width: 15px;
  cursor: pointer;
}

.next {
  margin: 60px auto 30px;
  width: 230px;
  display: flex;
  justify-content: space-between;
}

.tempWrapper {
  margin-top: -18px;
  margin-left: -20px;
  .inpt {
    display: flex;
    display:inline-block;
    position:absolute;
    top:30px;
    right:18px;
    justify-content: flex-end;
    .searchInt {
      width: 338px;
    }
  }
  .checkTemp {
    display:inline-block;
    position:absolute;
    top:60px;
    left:-15px;
    font-size: 14px;
    color: #001629;
    margin-left: 28px;
    margin-bottom: 28px;
    position: relative;
    span {
      position: absolute;
      right: 0;
      top: 10px;
      display: flex;
      align-items: center;
      color: #1A76D2;
      cursor: pointer;
    }
    img {
      margin-right: 5px;
    }
  }
}
</style>
