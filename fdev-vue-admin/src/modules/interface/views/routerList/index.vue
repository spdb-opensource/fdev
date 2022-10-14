<template>
  <f-block class="wrapper">
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="routerList"
          :columns="columns"
          row-key="routerList.id"
          :loading="loading"
          :pagination.sync="pagination"
          @request="query"
          noExport
          :on-search="searchByTermS"
          title="路由列表"
          titleIcon="list_s_f"
          :visible-columns="visibleColumns"
        >
          <template v-slot:top-bottom>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="场景名称">
              <fdev-input
                :value="name"
                @input="updateName($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="加载容器">
              <fdev-input
                :value="module"
                @input="updateModule($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="所属项目名称">
              <fdev-select
                use-input
                clearable
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
            <f-formitem class="col-4 q-pr-sm" bottom-page label="分支">
              <fdev-input
                :value="routerBranch"
                @input="updateRouterBranch($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="版本">
              <fdev-input
                :value="ver"
                @input="updateVer($event)"
                type="text"
                :rules="[() => $v.routerModel.ver.integer || '只能输入数字']"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
          </template>
          <template v-slot:top-right>
            <fdev-btn normal @click="handleRouteDialog" label="查看路由规范" />
          </template>
          <template v-slot:body-cell-name="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.id"
                :to="
                  `/interfaceAndRoute/routerList/routerProfile/${props.row.id}`
                "
                class="link"
                :title="props.value"
                >{{ props.value }}</router-link
              >
              <span v-else :title="props.value">{{ props.value }}</span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-ver="props">
            <fdev-td class="text-ellipsis">
              <span v-if="props.row.ver" :title="props.value">
                {{ props.value }}
              </span>
              <span v-else title="0">0</span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-projectName="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.appId"
                :to="`/app/list/${props.row.appId}`"
                class="link"
                :title="props.value"
                >{{ props.value }}</router-link
              >
              <span v-else :title="props.value">{{ props.value }}</span>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
    <RouteDialog v-model="routeDialogOpen" />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { routerApplyColumns } from '../../utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';
import { errorNotify } from '@/utils/utils';
import RouteDialog from '../../components/routeDialog';
import { integer } from 'vuelidate/lib/validators';

export default {
  name: 'RouterList',
  components: { Loading, RouteDialog },
  data() {
    return {
      columns: routerApplyColumns(),
      loading: false,
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      routerList: [],
      filterProject: [],
      routeDialogOpen: false
    };
  },
  validations: {
    routerModel: {
      ver: {
        integer
      }
    }
  },
  watch: {
    name(val) {
      if (!val) {
        this.searchByTermS();
      }
    },
    module(val) {
      if (!val) {
        this.searchByTermS();
      }
    },
    projectName(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.searchByTermS();
      }
    },
    ver(val) {
      if (!val) {
        this.searchByTermS();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveInterface/frontRouter/routerList', [
      'name',
      'module',
      'ver',
      'routerBranch',
      'projectName',
      'visibleColumns'
    ]),
    ...mapState('interfaceForm', {
      routesList: 'routesList'
    }),
    ...mapState('appForm', {
      appTypeData: 'appTypeData',
      vueAppData: 'vueAppData'
    })
  },
  methods: {
    ...mapMutations('userActionSaveInterface/frontRouter/routerList', [
      'updateName',
      'updateModule',
      'updateVer',
      'updateRouterBranch',
      'updateProjectName',
      'updateVisibleColumns'
    ]),
    ...mapActions('interfaceForm', {
      queryRoutes: 'queryRoutes'
    }),
    ...mapActions('appForm', {
      queryAppType: 'queryAppType',
      queryApps: 'queryApps'
    }),
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
      if (this.name || this.projectName || this.module) {
        this.query(props);
        if (!this.routerBranch) {
          this.updateRouterBranch('master');
        }
      } else {
        errorNotify('除分支和版本外，请至少输入一项查询条件！');
      }
    },
    async query(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      let project;
      if (typeof this.projectName === 'string') {
        project = this.projectName;
      } else {
        project = this.projectName ? this.projectName.name_en : '';
      }
      let params = {
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage,
        name: this.name ? this.name : '',
        projectName: project,
        branch: this.routerBranch || 'master',
        path: this.routerPath ? this.routerPath : '',
        module: this.module ? this.module : '',
        ver: this.ver ? Number(this.ver) : null
      };
      try {
        await this.queryRoutes(params);
        this.routerList = this.routesList.list;
        this.pagination.rowsNumber = this.routesList.total;
      } finally {
        this.loading = false;
      }
    },
    handleRouteDialog() {
      this.routeDialogOpen = true;
    }
  },
  async created() {
    await this.queryAppType();
    let params = this.appTypeData.find(type => type.label === 'Vue应用');
    await this.queryApps({ type_id: params.value });
    this.filterProject = this.vueAppData;
    if (
      this.name ||
      this.module ||
      this.ver ||
      this.routerBranch ||
      this.projectName
    ) {
      this.searchByTermS();
    }
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
.clearfix:after
  content ''
  display block
  clear:both
  visibility hidden
.project-width
 width 260px
</style>
