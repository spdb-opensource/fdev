<template>
  <f-block class="wrapper">
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="routerAppList"
          :columns="columns"
          row-key="routerAppList.id"
          :loading="loading"
          :pagination.sync="pagination"
          @request="query"
          :on-search="searchByTermS"
          noExport
          title="应用路由配置介质列表"
          titleIcon="list_s_f"
          :visible-columns="visibleColumns"
        >
          <template v-slot:top-bottom>
            <f-formitem
              class="col-4 q-pr-sm"
              bottom-page
              label="场景名称"
              label-style="padding-left:60px"
            >
              <fdev-input
                ref="name"
                :value="name"
                @input="updateName($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem
              class="col-4 q-pr-sm"
              bottom-page
              label="分支"
              label-style="padding-left:80px"
            >
              <fdev-input
                ref="routerBranch"
                :value="routerBranch"
                @input="updateRouterBranch($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem
              class="col-4 q-pr-sm"
              bottom-page
              label="所属项目名称"
              label-style="padding-left:30px"
            >
              <fdev-select
                use-input
                clearable
                ref="projectName"
                :value="projectName"
                @input="updateProjectName($event)"
                :options="filterProject"
                @filter="projectFilter"
                option-label="name_en"
                option-value="name_en"
                type="text"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_en">{{
                        scope.opt.name_en
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.name_zh" caption>
                        {{ scope.opt.name_zh }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </template>
          <template v-slot:body-cell-projectName="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.id"
                :to="`/app/list/${props.row.appId}`"
                class="link"
                :title="props.value"
                >{{ props.value }}</router-link
              >
              <div v-else :title="props.value">
                {{ props.value }}
              </div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-branch="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              <span
                @click="openBranch(props.row.projectName, props.row.branch)"
                class="link-style"
              >
                {{ props.value }}
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-routeNum="props">
            <fdev-td
              v-if="props.row.repoJson"
              @click="openJson(props.row.repoJson, props.row.projectName)"
              class="link-style"
              :title="props.value"
            >
              {{ props.value }}
            </fdev-td>
            <fdev-td v-else :title="props.value">
              {{ props.value }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-routesVersion="props">
            <fdev-td>
              <router-link
                :to="{
                  path: '/interfaceAndRoute/routerList/',
                  query: {
                    ver: String(props.row.routesVersion),
                    branch: props.row.branch,
                    projectName: props.row.projectName
                  }
                }"
                class="link"
                :title="props.row.routesVersion"
                >{{ props.row.routesVersion }}</router-link
              >
            </fdev-td>
          </template>
          <template v-slot:body-cell-repoTarName="props">
            <fdev-td class="text-ellipsis">
              <span
                v-if="props.row.repoTarUrl"
                @click="openTarUrl(props.row.repoTarUrl)"
                class="link-style"
                :title="props.value"
              >
                {{ props.value }}
                <fdev-popup-proxy context-menu v-if="props.value">
                  <fdev-banner style="max-width:300px">
                    {{ props.value }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </span>
              <span v-else :title="props.value">
                {{ props.value }}
                <fdev-popup-proxy context-menu v-if="props.value">
                  <fdev-banner style="max-width:300px">
                    {{ props.value }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </span>
            </fdev-td>
          </template>
        </fdev-table>
        <f-dialog v-model="isEditor" right f-sc :title="repoName">
          <div class="bg-white">
            <pre class="pre-margin " v-text="routeNumJson" />
          </div>
          <template v-slot:btnSlot>
            <fdev-btn dialog label="确认" @click="isEditor = false" />
          </template>
        </f-dialog>
        <f-dialog v-model="checkBranch" right f-sc title="介质提交历史记录">
          <fdev-table
            title="介质提交历史记录列表"
            :columns="dialogColumns"
            :data="branchHistoryData"
            titleIcon="list_s_f"
            noExport
            no-select-cols
          >
            <template v-slot:body-cell-opt="props">
              <fdev-td>
                <fdev-btn
                  flat
                  label="repo.json"
                  @click="
                    openRepoJson(props.row.repoJson, props.row.projectName)
                  "
                />
              </fdev-td>
            </template>
          </fdev-table>
          <template v-slot:btnSlot>
            <fdev-btn dialog label="确认" @click="checkBranch = false" />
          </template>
        </f-dialog>
        <f-dialog v-model="showRepoJson" right f-sc :title="jsonTitle">
          <div class="bg-white">
            <pre class="pre-margin " v-text="branchRepoJson" />
          </div>
          <template v-slot:btnSlot>
            <fdev-btn dialog label="确认" @click="showRepoJson = false" />
          </template>
        </f-dialog>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import {
  routerAppColumns,
  mediaPublishHistoryRecordColumns
} from '../../utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';

export default {
  name: 'routerAppConfig',
  components: { Loading },
  data() {
    return {
      dialogColumns: mediaPublishHistoryRecordColumns,
      columns: routerAppColumns(),
      loading: false,
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      routerAppList: [],
      filterProject: [],
      routeDialogOpen: false,
      isEditor: false,
      routeNumJson: '',
      repoName: '',
      checkBranch: false,
      branchHistoryData: [],
      showRepoJson: false,
      jsonTitle: '',
      branchRepoJson: ''
    };
  },
  watch: {
    name(val) {
      if (!val) {
        this.searchByTermS();
      }
    },
    routerBranch(val) {
      if (!val) {
        this.searchByTermS();
      }
    },
    projectName(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.searchByTermS();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveInterface/frontRouter/routerAppConfig', [
      'name',
      'routerBranch',
      'projectName',
      'visibleColumns'
    ]),
    ...mapState('interfaceForm', ['appDatList']),
    ...mapState('appForm', {
      appTypeData: 'appTypeData',
      vueAppData: 'vueAppData'
    })
  },
  methods: {
    ...mapMutations('userActionSaveInterface/frontRouter/routerAppConfig', [
      'updateName',
      'updateRouterBranch',
      'updateProjectName',
      'updateVisibleColumns'
    ]),
    ...mapActions('interfaceForm', ['queryAppJsonList']),
    ...mapActions('appForm', {
      queryAppType: 'queryAppType',
      queryApps: 'queryApps'
    }),
    openJson(data, name) {
      this.repoName = 'repo_' + name + '.json';
      let jsonData = JSON.stringify(data);
      this.routeNumJson = JSON.stringify(JSON.parse(jsonData), null, 4);
      this.isEditor = true;
    },
    openTarUrl(url) {
      window.open(url);
    },
    async openBranch(projectName, branch) {
      let params = {
        branch: branch,
        project_name: projectName,
        is_history: true
      };
      await this.queryAppJsonList(params);
      this.branchHistoryData = this.appDatList.list;
      this.checkBranch = true;
    },
    openRepoJson(data, name) {
      this.jsonTitle = 'repo_' + name + '.json';
      let jsonData = JSON.stringify(data);
      this.branchRepoJson = JSON.stringify(JSON.parse(jsonData), null, 4);
      this.showRepoJson = true;
    },
    projectFilter(val, update) {
      update(() => {
        this.filterProject = this.vueAppData.filter(
          tag =>
            tag.name_zh.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    searchByTermS(props) {
      this.query(props);
    },
    async query(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      let params = {
        page: this.pagination.page,
        page_num: this.pagination.rowsPerPage,
        name: this.name ? this.name : '',
        project_name: this.projectName ? this.projectName.name_en : '',
        branch: this.routerBranch
      };
      try {
        await this.queryAppJsonList(params);
        this.routerAppList = this.appDatList.list;
        this.pagination.rowsNumber = this.appDatList.total;
      } finally {
        this.loading = false;
      }
    }
  },
  created() {
    this.searchByTermS();
  },
  async mounted() {
    await this.queryAppType();
    let params = this.appTypeData.find(type => type.label === 'Vue应用');
    await this.queryApps({ type_id: params.value });
    this.filterProject = this.vueAppData;
  }
};
</script>

<style lang="stylus" scoped>
form
  width 100%
  .row
    align-items self-start
.select
  width 210px
.select-project
  width 270px
.clearfix:after
  content ''
  display block
  clear:both
  visibility hidden
.link-style
  color #2196f3
  cursor pointer
.pre-margin
  margin 0 10px
  font 15px Microsoft YaHei
  line-height 25px
  padding-left 35px
.table-wrapper
  width 100%
  padding 25px
  overflow-x auto
.table-wrapper .table
  width 100%
  border-radius 5px
  border-collapse collapse
  border 1px solid #bdbdbd
  td, th
    height 40px
    border 1px solid #bdbdbd
    color #616161
    min-width 80px
  tr:nth-of-type(2n)
    td, th
      background #eee
</style>
