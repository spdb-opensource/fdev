<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-tabs v-model="tab" @input="init">
        <fdev-tab name="Applications" label="应用维度" />
        <fdev-tab name="Component" label="组件维度" />
      </fdev-tabs>
      <fdev-tab-panels v-model="tab" animated>
        <fdev-tab-panel name="Applications">
          <fdev-table
            :columns="columns"
            ref="table"
            :data="tableData"
            title="应用列表"
            titleIcon="list_s_f"
            no-export
            no-select-cols
          >
            <template v-slot:top-right>
              <fdev-btn
                label="实时扫描"
                normal
                ficon="search_s_o"
                v-if="tab === 'Applications'"
                @click="scan"
              />
            </template>
            <template v-slot:body-cell-name_en="props">
              <fdev-td :title="props.value" class="text-ellipsis">
                <router-link
                  :to="{ path: `/app/list/${props.row.application_id}` }"
                  class="link"
                  v-if="props.row.application_id"
                >
                  <span>{{ props.value }}</span>
                </router-link>
                <span v-else>{{ props.value }}</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-name_zh="props">
              <fdev-td :title="props.value" class="text-ellipsis">
                <router-link
                  :to="{ path: `/app/list/${props.row.application_id}` }"
                  class="link"
                  v-if="props.row.application_id"
                >
                  <span>{{ props.value }}</span>
                </router-link>
                <span v-else>{{ props.value }}</span>
              </fdev-td>
            </template>

            <template v-slot:body-cell-type="props">
              <fdev-td
                :title="props.row.type ? typeDict[props.row.type] : '-'"
                class="text-ellipsis"
              >
                <span :class="{ 'text-red': props.row.type === '2' }">
                  {{ props.row.type ? typeDict[props.row.type] : '-' }}
                </span>
              </fdev-td>
            </template>
          </fdev-table>
        </fdev-tab-panel>

        <fdev-tab-panel name="Component">
          <fdev-table
            :columns="columns"
            titleIcon="list_s_f"
            title="组件列表"
            :data="tableData"
            no-export
            no-select-cols
          >
            <template v-slot:body-cell-name_en="props">
              <fdev-td :title="props.row.name_en" class="text-ellipsis">
                <router-link
                  :to="{
                    path: `/componentManage/server/list/${
                      props.row.component_id
                    }`
                  }"
                  class="link"
                >
                  <span>{{ props.row.name_en }}</span>
                </router-link>
              </fdev-td>
            </template>

            <template v-slot:body-cell-name_cn="props">
              <fdev-td :title="props.row.name_cn" class="text-ellipsis">
                <router-link
                  :to="{
                    path: `/componentManage/server/list/${
                      props.row.component_id
                    }`
                  }"
                  class="link"
                >
                  <span>{{ props.row.name_cn }}</span>
                </router-link>
              </fdev-td>
            </template>

            <template v-slot:body-cell-type="props">
              <fdev-td
                :title="props.row.type ? typeDict[props.row.type] : '-'"
                class="text-ellipsis"
              >
                <span :class="{ 'text-red': props.row.type === '2' }">
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
import { mapActions, mapState } from 'vuex';
import {
  typeDict,
  serverArchetypeIntergrationBaseColums,
  serverArchetypeIntergrationApplicationsColums,
  serverArchetypeIntergrationComponentColums
} from '@/modules/Component/utils/constants.js';
import { successNotify } from '@/utils/utils';

export default {
  name: 'ArchetypeIntergration',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      tab: 'Applications',
      typeDict: typeDict
    };
  },
  computed: {
    ...mapState('componentForm', ['archetypeRelative']),
    columns() {
      const baseColums = serverArchetypeIntergrationBaseColums;
      const Applications = serverArchetypeIntergrationApplicationsColums;
      const Component = serverArchetypeIntergrationComponentColums;
      return this.tab === 'Component'
        ? Component.concat(baseColums)
        : Applications.concat(baseColums);
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryComponentByArchetype',
      'queryApplicationsByArchetype',
      'scanArchetype'
    ]),
    async scan() {
      await this.scanArchetype({
        ...this.$route.params
      });
      successNotify('发起扫描成功，请耐心等待扫描结果!');
    },
    async init() {
      this.loading = true;
      this.tableData = [];
      await this[`query${this.tab}ByArchetype`]({
        ...this.$route.params
      });
      this.tableData = this.archetypeRelative;
      this.loading = false;
    }
  },
  created() {
    this.init();
  }
};
</script>
