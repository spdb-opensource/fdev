<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <fdev-table
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        noExport
        title="部署信息列表"
        titleIcon="list_s_f"
        ref="table"
        @request="init"
        :on-search="filterWithData"
        class="my-sticky-column-table"
        :visible-columns="visibleCols"
      >
        <template v-slot:top-bottom>
          <f-formitem label-style="width:40px" label="环境" class="q-pr-sm">
            <fdev-select
              use-input
              clearable
              :value="terms.env"
              @input="saveEnv($event)"
              :options="envOptions"
              option-label="name_en"
              option-value="name_en"
              @filter="filterEnv"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                  <fdev-item-section
                    side
                    v-if="
                      scope.opt.labels.includes('biz') ||
                        scope.opt.labels.includes('dmz')
                    "
                  >
                    <fdev-chip
                      dense
                      flat
                      square
                      class="text-white"
                      :color="
                        scope.opt.labels.includes('biz')
                          ? 'green-5'
                          : 'orange-5'
                      "
                    >
                      {{ scope.opt.labels.includes('biz') ? '业务' : '网银' }}
                    </fdev-chip>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem label-style="width:40px" label="应用">
            <fdev-select
              use-input
              clearable
              :value="terms.app"
              @input="saveApp($event)"
              :options="appOptions"
              option-label="name_en"
              option-value="name_en"
              @filter="filterApp"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </template>
        <template v-slot:top-right>
          <fdev-btn
            normal
            icon="add"
            label="新增绑定"
            @click="
              $router.push({
                name: 'DeployMessageHandlePage'
              })
            "
            v-if="isManager"
          />
          <fdev-btn
            normal
            label="绑定当前应用"
            v-if="bindDeployBtnDisplay && (isManager || isAppManager)"
            @click="
              $router.push({
                name: 'DeployMessageHandlePage',
                query: { appId: listModel.app.id }
              })
            "
          />
        </template>
        <template v-slot:body-cell-appInfo="props">
          <fdev-td class="text-ellipsis">
            <router-link
              class="link"
              :to="`/app/list/${props.row.appInfo.id}`"
              :title="props.row.appInfo.name_en"
              v-if="props.row.appInfo.name_en"
              >{{ props.row.appInfo.name_en }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.appInfo.name_en }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <div v-else title="-">-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-modelSet="props">
          <fdev-td class="text-ellipsis" :title="props.value.nameCn || '-'">
            {{ props.value.nameCn || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value.nameCn || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <template v-slot:body-cell-testEnv="props">
          <fdev-td class="text-ellipsis" :title="props.value || '-'">
            {{ props.value || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <template v-slot:body-cell-productEnv="props">
          <fdev-td class="text-ellipsis" :title="props.value || '-'">
            {{ props.value || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <template v-slot:body-cell-btn="props">
          <fdev-td auto-width>
            <div class="q-gutter-x-sm">
              <fdev-btn
                label="详情"
                flat
                @click="
                  $router.push({
                    name: 'DeployMessageProfile',
                    params: { appId: props.row.appInfo.id }
                  })
                "
              />
              <div class="inline-block">
                <fdev-tooltip
                  anchor="top middle"
                  self="center middle"
                  :offest="[0, 0]"
                  v-if="
                    !(
                      isManager ||
                      props.row.managers.some(
                        user => user.id === currentUser.id
                      )
                    )
                  "
                >
                  请联系环境管理员或应用负责人
                </fdev-tooltip>
                <fdev-btn
                  label="编辑"
                  flat
                  @click="
                    $router.push({
                      name: 'DeployMessageHandlePage',
                      query: { appId: props.row.appInfo.id }
                    })
                  "
                  :disable="
                    !(
                      isManager ||
                      props.row.managers.some(
                        user => user.id === currentUser.id
                      )
                    )
                  "
                  :color="
                    isManager ||
                    props.row.managers.some(user => user.id === currentUser.id)
                      ? 'primary'
                      : 'grey'
                  "
                />
              </div>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { deployListModel, deployMessageColumns } from '../../utils/constants';
import { successNotify } from '@/utils/utils';
export default {
  name: 'DeployMessage',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      envOptions: [],
      appOptions: [],
      filterDataModel: {
        env_id: '',
        app_id: ''
      },
      listModel: deployListModel(),
      pagination: {},
      columns: deployMessageColumns().columns
    };
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentPage(val);
      },
      deep: true
    },
    'terms.env'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    },
    'terms.app'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    }
  },
  computed: {
    ...mapState('environmentForm', ['appProInfo', 'envList']),
    ...mapState('appForm', ['appData']),
    ...mapState('user', ['currentUser']),
    ...mapState('userActionSaveEnv/deployMessage', [
      'visibleCols',
      'currentPage',
      'terms'
    ]),
    bindDeployBtnDisplay() {
      return this.filterDataModel.app_id && this.appProInfo.total === 0;
    },
    isManager() {
      return this.currentUser.role.some(role => role.name === '环境配置管理员');
    },
    isAppManager() {
      if (!this.listModel.app) return false;
      const { dev_managers, spdb_managers } = this.listModel.app;
      return [...dev_managers, ...spdb_managers].some(
        user => user.id === this.currentUser.id
      );
    },
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  methods: {
    ...mapActions('environmentForm', ['queryAppProInfo', 'getEnvList']),
    ...mapActions('appForm', ['queryApplication']),
    ...mapMutations('userActionSaveEnv/deployMessage', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveEnv',
      'saveApp'
    ]),
    filterEnv(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.envOptions = this.envList.filter(v => {
          return (
            v.name_en.toLowerCase().includes(needle) || v.name_cn.includes(val)
          );
        });
      });
    },
    filterApp(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.appData.filter(v => {
          return (
            v.name_en.toLowerCase().includes(needle) || v.name_zh.includes(val)
          );
        });
      });
    },
    filterWithData() {
      this.filterDataModel = {
        env_id: this.terms.env ? this.terms.env.id : '',
        app_id: this.terms.app ? this.terms.app.id : ''
      };
      this.init();
    },
    async init(props) {
      if (props && props.pagination) {
        const { page, rowsPerPage } = props.pagination;

        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      await this.queryAppProInfo({
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        ...this.filterDataModel
      });
      this.pagination.rowsNumber = this.appProInfo.total;
      this.tableData = this.appProInfo.list;
      this.loading = false;

      if (
        this.bindDeployBtnDisplay &&
        !this.filterDataModel.env_id &&
        (this.isAppManager || this.isManager)
      ) {
        successNotify('这个应用还没有绑定过部署信息呢，点击新增绑定一个吧!');
        return;
      }
      if (this.bindDeployBtnDisplay && (this.isAppManager || this.isManager)) {
        successNotify('该应用还没有部署过该环境，如果需要，赶快去绑定一下吧!');
      }
    }
  },
  created() {
    this.getEnvList();
    this.queryApplication();
    this.pagination = {
      ...this.currentPage
    };
    this.filterWithData();
  },
  mounted() {
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'appInfo',
        'group',
        'modelSet',
        'testEnv',
        'productEnv',
        'btn'
      ]);
    }
  }
};
</script>

<style lang="stylus" scoped>
.input
  min-width 270px
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
// .my-sticky-column-table >>>
//   th:first-child,td:first-child,td:last-child,th:last-child
//     background-color #fff
//     opacity 1
  th:first-child,th:last-child
    color #9c9a9a
  th:first-child,td:first-child
    position sticky
    left 0
    z-index 1
  td:last-child,th:last-child
    position sticky
    right 0
    z-index 1
  .td-desc
    max-width 300px
    overflow hidden
    text-overflow ellipsis
</style>
