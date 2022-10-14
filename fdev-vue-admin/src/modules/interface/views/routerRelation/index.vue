<template>
  <f-block class="wrapper">
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="routerRelationList"
          :columns="columns"
          row-key="routerRelationList.id"
          :loading="loading"
          :pagination.sync="pagination"
          @request="query"
          noExport
          no-select-cols
          :on-search="searchByTermS"
          title="路由关系列表"
          titleIcon="list_s_f"
          :visible-columns="visibleColumns"
        >
          <template v-slot:top-bottom>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="场景名称">
              <fdev-input
                ref="routerName"
                :value="routerName"
                @input="updateRouterName($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="分支">
              <fdev-input
                ref="routerBranch"
                :value="routerBranch"
                @input="updateRouterBranch($event)"
                type="text"
                clearable
                @keyup.enter="searchByTermS"
              />
            </f-formitem>
            <f-formitem class="col-4 q-pr-sm" bottom-page label="调用项目名称">
              <fdev-select
                use-input
                clearable
                ref="sourceProject"
                :value="sourceProject"
                @input="updateSourceProject($event)"
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
            <f-formitem
              class="col-4 q-pr-sm"
              bottom-page
              label="路由提供项目名称"
            >
              <fdev-select
                use-input
                clearable
                ref="targetProject"
                :value="targetProject"
                @input="updateTargetProject($event)"
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
          <template v-slot:body-cell-name="props">
            <fdev-td class="text-ellipsis" :title="props.value || '-'">
              <router-link
                v-if="props.row.routeId"
                :to="
                  `/interfaceAndRoute/routerList/routerProfile/${
                    props.row.routeId
                  }`
                "
                class="link"
                :title="props.value"
                >{{ props.value }}</router-link
              >
              <span v-else :title="props.value">{{ props.value }}</span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-sourceProject="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.sourceId"
                :to="`/app/list/${props.row.sourceId}`"
                class="link"
                :title="props.value"
                >{{ props.value }}</router-link
              >
              <span v-else :title="props.value">{{ props.value }}</span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-targetProject="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.targetId"
                :to="`/app/list/${props.row.targetId}`"
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
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { routerRelationColumns } from '../../utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';
import { errorNotify } from '@/utils/utils';

export default {
  name: 'RouterRealtion',
  components: { Loading },
  data() {
    return {
      columns: routerRelationColumns(),
      loading: false,
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      routerRelationList: [],
      filterProject: []
    };
  },
  validations: {},
  watch: {
    routerName(val) {
      if (!val) {
        this.searchByTermS();
      }
    },
    sourceProject(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.searchByTermS();
      }
    },
    targetProject(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.searchByTermS();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveInterface/frontRouter/routerRelation', [
      'routerName',
      'routerBranch',
      'sourceProject',
      'targetProject',
      'visibleColumns'
    ]),
    ...mapState('interfaceForm', {
      routesRelationList: 'routesRelationList'
    }),
    ...mapState('appForm', {
      appTypeData: 'appTypeData',
      vueAppData: 'vueAppData'
    })
  },
  methods: {
    ...mapMutations('userActionSaveInterface/frontRouter/routerRelation', [
      'updateRouterName',
      'updateRouterBranch',
      'updateSourceProject',
      'updateTargetProject',
      'updateVisibleColumns'
    ]),
    ...mapActions('interfaceForm', {
      queryRoutesRelation: 'queryRoutesRelation'
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
      if (this.routerName || this.sourceProject || this.targetProject) {
        this.query(props);
        if (!this.routerBranch) {
          this.updateRouterBranch('master');
        }
      } else {
        errorNotify('除分支外，请至少输入一项查询条件！');
      }
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
        pageNum: this.pagination.rowsPerPage,
        name: this.routerName,
        sourceProject: this.sourceProject ? this.sourceProject.name_en : '',
        targetProject: this.targetProject ? this.targetProject.name_en : '',
        branch: this.routerBranch || 'master'
      };
      try {
        await this.queryRoutesRelation(params);
        this.routerRelationList = this.routesRelationList.list;
        this.pagination.rowsNumber = this.routesRelationList.total;
      } finally {
        this.loading = false;
      }
    }
  },
  async created() {
    await this.queryAppType();
    let params = this.appTypeData.find(type => type.label === 'Vue应用');
    await this.queryApps({ type_id: params.value });
    this.filterProject = this.vueAppData;
  },
  mounted() {
    if (
      this.routerName ||
      this.routerBranch ||
      this.sourceProject ||
      this.targetProject
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
  width 240px
.clearfix:after
  content ''
  display block
  visibility hidden
  clear both
</style>
