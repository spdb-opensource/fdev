<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-tabs v-model="tab" class="q-mb-lg">
        <fdev-tab name="application" label="应用维度" />
        <fdev-tab name="component" label="组件维度" />
      </fdev-tabs>

      <fdev-tab-panels v-model="tab" animated>
        <fdev-tab-panel name="application">
          <fdev-table
            class="my-sticky-column-table"
            title="应用维度组件列表"
            titleIcon="list_s_f"
            :columns="columns"
            :pagination.sync="pagination"
            ref="table"
            :data="applicationsList"
            :visible-columns="visibleColumns"
            :onSelectCols="updatevisibleColumns"
            no-export
            no-select-cols
            title-icon="list_s_f"
          >
            <template v-slot:top-right>
              <fdev-btn
                normal
                ficon="search_s_o"
                label="实时扫描"
                @click="scanSelectedApplication"
              />
            </template>

            <template v-slot:top-bottom>
              <!-- 现有应用 -->
              <f-formitem label="现有应用">
                <fdev-select
                  use-input
                  :options="applicationOptions"
                  option-label="name_en"
                  option-value="id"
                  :value="appId"
                  @input="application($event)"
                  @filter="applicationChoice"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.name_en">
                          {{ scope.opt.name_en }}
                        </fdev-item-label>
                        <fdev-item-label caption :title="scope.opt.name_zh">
                          {{ scope.opt.name_zh }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
            </template>

            <!-- 组件名称列 -->
            <template v-slot:body-cell-name="props">
              <fdev-td class="text-ellipsis">
                <router-link
                  v-if="props.row.name_en"
                  :to="{
                    path: `/componentManage/server/list/${
                      props.row.component_id
                    }`
                  }"
                  class="link"
                >
                  <span :title="props.row.name_en">
                    {{ props.row.name_en }}
                  </span>
                </router-link>
                <span v-else>
                  {{ props.row.name_en }}
                </span>
              </fdev-td>
            </template>

            <!-- 当前版本类型列 -->
            <template v-slot:body-cell-type="props">
              <fdev-td>
                <span
                  :class="[
                    { 'text-red': props.row.type === '2' },
                    { 'text-yellow-9': props.row.type === '3' }
                  ]"
                  :title="props.row.type ? typeDict[props.row.type] : '-'"
                >
                  {{ props.row.type ? typeDict[props.row.type] : '-' }}
                </span>
              </fdev-td>
            </template>
          </fdev-table>
        </fdev-tab-panel>

        <fdev-tab-panel name="component">
          <fdev-table
            class="my-sticky-column-table"
            title="组件维度应用列表"
            titleIcon="list_s_f"
            :columns="columns"
            :pagination.sync="pagination"
            ref="table"
            :data="appsByComponent"
            :visible-columns="visibleColumnsCom"
            :onSelectCols="updatevisibleColumnsCom"
            no-export
            no-select-cols
            title-icon="list_s_f"
          >
            <template v-slot:top-right>
              <fdev-btn
                normal
                ficon="search_s_o"
                label="实时扫描"
                @click="scanSelectedComponent"
              />
            </template>

            <template v-slot:top-bottom>
              <!-- 选择组件 -->
              <f-formitem label="选择组件">
                <fdev-select
                  use-input
                  :options="componentsOptions"
                  option-label="name_en"
                  option-value="id"
                  :value="comId"
                  @input="component($event)"
                  @filter="componentChoice"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.name_en">
                          {{ scope.opt.name_en }}
                        </fdev-item-label>
                        <fdev-item-label caption :title="scope.opt.name_cn">
                          {{ scope.opt.name_cn }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
            </template>

            <!-- 应用名称列 -->
            <template v-slot:body-cell-name="props">
              <fdev-td>
                <router-link
                  :to="{ path: `/app/list/${props.row.application_id}` }"
                  class="link"
                  v-if="!props.row.archetype"
                >
                  <span :title="props.row.name_en">
                    {{ props.row.name_en }}
                  </span>
                </router-link>
                <span v-else :title="props.row.name_en">
                  {{ props.row.name_en }}
                </span>
              </fdev-td>
            </template>

            <!-- 当前版本类型列 -->
            <template v-slot:body-cell-type="props">
              <fdev-td>
                <span
                  :class="[
                    { 'text-red': props.row.type === '2' },
                    { 'text-yellow-9': props.row.type === '3' }
                  ]"
                  :title="props.row.type ? typeDict[props.row.type] : '-'"
                >
                  {{ props.row.type ? typeDict[props.row.type] : '-' }}
                </span>
              </fdev-td>
            </template>
          </fdev-table>
        </fdev-tab-panel>
      </fdev-tab-panels>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import {
  typeDict,
  serverComponentIntergrationColums
} from '@/modules/Component/utils/constants.js';
import { successNotify } from '@/utils/utils';
export default {
  name: 'Intergration',
  components: { Loading },
  data() {
    return {
      loading: false,
      tab: 'application',
      applicationOptions: [],
      componentsOptions: [],
      pagination: {
        rowsPerPage: 0
      },
      typeDict: typeDict,
      cloneVisibleColumns: ['name', 'component_version', 'type', 'update_time']
    };
  },
  watch: {
    tab(val) {
      this.loading = true;
      this.init();
    }
  },
  computed: {
    ...mapState('componentForm', [
      'applicationsList',
      'appsByComponent',
      'componentList'
    ]),
    ...mapState(
      'userActionSaveComponent/componentManage/componentList/intergration',
      ['visibleColumns', 'visibleColumnsCom', 'appId', 'comId']
    ),
    ...mapState('appForm', ['vueAppData']),
    columns() {
      const name = this.tab === 'application' ? '组件名称' : '应用名称';
      const name_cn =
        this.tab === 'application' ? '组件中文名称' : '应用中文名称';
      const name_zh = this.tab === 'application' ? 'name_cn' : 'name_zh';
      return serverComponentIntergrationColums(name, name_cn, name_zh);
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/intergration',
      [
        'updatevisibleColumns',
        'updatevisibleColumnsCom',
        'updateAppId',
        'updateComId'
      ]
    ),
    ...mapActions('componentForm', [
      'scanApplication',
      'scanComponent',
      'queryComponentsByApplicaton',
      'queryApplicatonsByComponent',
      'queryComponent'
    ]),
    ...mapActions('appForm', {
      queryApps: 'queryApps'
    }),
    applicationChoice(val, update, abort) {
      const needle = val.toLowerCase();
      update(() => {
        this.applicationOptions = this.vueAppData.filter(
          v =>
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_zh.toLowerCase().indexOf(needle) > -1
        );
        if (!this.applicationOptions[0]) {
          this.applicationOptions = this.vueAppData;
        }
      });
    },
    componentChoice(val, update, abort) {
      const needle = val.toLowerCase();
      update(() => {
        this.componentsOptions = this.componentList.filter(
          v =>
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.toLowerCase().indexOf(needle) > -1
        );
        if (!this.componentsOptions[0]) {
          this.componentsOptions = this.componentList;
        }
      });
    },
    async init() {
      let id = this.tab === 'application' ? this.appId : this.comId;
      await this[this.tab](id);
    },
    async application(app) {
      if (app === null) {
        return;
      } else {
        this.loading = true;
        await this.queryApps();
        if (this.appId === '') {
          this.updateAppId(this.vueAppData[0]);
        } else {
          this.updateAppId(app);
        }
        this.applicationOptions = this.vueAppData;
        await this.queryComponentsByApplicaton({
          application_id: this.appId.id
        });
        this.loading = false;
      }
    },
    async component(component) {
      if (component === null) {
        return;
      } else {
        this.loading = true;
        await this.queryComponent();
        if (this.comId === '') {
          this.updateComId(this.componentList[0]);
        } else {
          this.updateComId(component);
        }
        this.componentsOptions = this.componentList;
        await this.queryApplicatonsByComponent({
          component_id: this.comId.id
        });
        this.loading = false;
      }
    },
    async scanSelectedComponent() {
      this.loading = true;
      try {
        await this.scanComponent({ component_id: this.comId.id });
      } finally {
        this.loading = false;
      }
      successNotify('发起扫描成功，请耐心等待扫描结果!');
    },
    async scanSelectedApplication() {
      this.loading = true;
      try {
        await this.scanApplication({ application_id: this.appId.id });
      } finally {
        this.loading = false;
      }
      successNotify('发起扫描成功，请耐心等待扫描结果!');
    }
  },
  created() {
    this.init();
  }
};
</script>
<style lang="stylus" scoped>
.clearfix:after
  content ''
  display block
  clear:both
  visibility hidden
.w150
  width 150px
</style>
