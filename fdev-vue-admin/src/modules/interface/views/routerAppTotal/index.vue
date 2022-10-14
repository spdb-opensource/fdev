<template>
  <f-block class="wrapper">
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="routerAppTotalList"
          :columns="columns"
          row-key="routerAppTotalList.id"
          :loading="loading"
          :visible-columns="visibleColumns"
          :on-search="searchByTermS"
          noExport
          no-select-cols
          title="总路由配置介质列表"
          titleIcon="list_s_f"
        >
          <template v-slot:top-bottom>
            <f-formitem label="所属项目名称">
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

          <template v-slot:body-cell-env="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              <span @click="openEnv(props.row.env)" class="link-style">
                {{ props.value }}
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-appNum="props">
            <fdev-td
              v-if="props.row.appNum !== 0"
              @click="openAppName(props.row.centralJson)"
              class="link-style"
              :title="props.value"
            >
              {{ props.value }}
            </fdev-td>
            <fdev-td v-else :title="props.value">
              {{ props.value }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-totalTarName="props">
            <fdev-td class="text-ellipsis">
              <span
                v-if="props.row.totalTarUrl"
                @click="openTarUrl(props.row.totalTarUrl)"
                class="link-style"
                :title="props.value"
              >
                {{ props.value }}
              </span>
              <span v-else :title="props.value">{{ props.value }}</span>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
    <f-dialog v-model="checkDialogOpened" right f-sc title="历史记录">
      <fdev-table
        title="介质提交历史记录列表"
        :columns="mediaColumns"
        :data="envHistoryData"
        titleIcon="list_s_f"
        noExport
        no-select-cols
      >
        <template v-slot:body-cell-totalTarName="props">
          <fdev-td class="ellipsis" :title="props.row.totalTarName">
            <span
              class="text-primary"
              label="props.row.totalTarName"
              @click="openTarUrl(props.row.totalTarUrl)"
              >{{ props.row.totalTarName }}</span
            >
          </fdev-td>
        </template>
      </fdev-table>
    </f-dialog>
    <f-dialog v-model="checkAppNameOpened" right f-sc title="包含项目">
      <div class="table-wrapper bg-white">
        <fdev-table
          title="应用列表"
          :columns="appColumns"
          :data="appNameData"
          titleIcon="list_s_f"
          noExport
          no-select-cols
        >
        </fdev-table>
      </div>
    </f-dialog>
    <f-dialog v-model="isEditor" right f-sc title="central.json">
      <div class="bg-white">
        <pre class="pre-margin " v-text="centerJson" />
      </div>
    </f-dialog>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { routerTotalColumns, mediaEnvColumns } from '../../utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';

export default {
  name: 'routerAppTotal',
  components: { Loading },
  data() {
    return {
      mediaColumns: mediaEnvColumns,
      appColumns: [
        {
          name: 'en_name',
          label: '英文名',
          field: 'en_name'
        },
        {
          name: 'name',
          label: '中文名',
          field: 'name'
        }
      ],
      columns: routerTotalColumns(),
      loading: false,
      routerAppTotalList: [],
      filterProject: [],
      routeDialogOpen: false,
      checkDialogOpened: false,
      envHistoryData: [],
      checkAppNameOpened: false,
      appNameData: [],
      isEditor: false,
      centerJson: {}
    };
  },
  watch: {
    projectName(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.searchByTermS();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveInterface/frontRouter/routerAppTotal', [
      'projectName',
      'visibleColumns'
    ]),
    ...mapState('interfaceForm', ['appTotalDatList', 'envHistoryList']),
    ...mapState('appForm', {
      appTypeData: 'appTypeData',
      vueAppData: 'vueAppData'
    })
  },
  methods: {
    ...mapMutations('userActionSaveInterface/frontRouter/routerAppTotal', [
      'updateProjectName',
      'updateVisibleColumns'
    ]),
    ...mapActions('interfaceForm', [
      'queryTotalJsonList',
      'queryTotalJsonHistory'
    ]),
    ...mapActions('appForm', {
      queryAppType: 'queryAppType',
      queryApps: 'queryApps'
    }),
    async openEnv(data) {
      await this.queryTotalJsonHistory({ env: data });
      this.envHistoryData = this.envHistoryList;
      this.checkDialogOpened = true;
    },
    openAppName(data) {
      this.appNameData = [];
      for (let [k, v] of Object.entries(data.repos)) {
        this.appNameData.push({ en_name: k, name: v.name });
      }
      this.checkAppNameOpened = true;
    },
    openJson(data) {
      let jsonData = JSON.stringify(data);
      this.centerJson = JSON.stringify(JSON.parse(jsonData), null, 4);
      this.isEditor = true;
    },
    openTarUrl(url) {
      window.open(url);
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
    async query() {
      this.loading = true;
      try {
        await this.queryTotalJsonList({
          project_name: this.projectName ? this.projectName.name_en : ''
        });
        this.routerAppTotalList = this.appTotalDatList;
      } finally {
        this.loading = false;
      }
    }
  },
  async created() {
    this.query();
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
  width 270px
.clearfix:after
  content ''
  display block
  clear:both
  visibility hidden
.link-style
  color #2196f3
  cursor pointer
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
    text-align center
  tr:nth-of-type(2n)
    td, th
      background #eee
.margin
 margin 0 5px 0 5px
.pre-margin
  margin 0 10px
  font 15px Microsoft YaHei
  line-height 25px
  padding-left 35px
</style>
