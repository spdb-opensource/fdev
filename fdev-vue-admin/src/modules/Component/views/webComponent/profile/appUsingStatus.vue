<template>
  <Loading>
    <fdev-table
      class="my-sticky-column-table"
      title="应用使用现状列表"
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      ref="table"
      :filter="filterValue"
      :filter-method="filter"
      :visible-columns="visibleColumns"
      :onSelectCols="updatevisibleColumns"
      :export-func="handleDownload"
    >
      <template v-slot:top-right>
        <fdev-btn
          label="实时扫描"
          normal
          ficon="search_s_o"
          @click="scanSelectedComponent"
          :loading="globalLoading['componentForm/scanAppByMpassCom']"
        />
        <!-- <fdev-btn
          ficon="keep_file"
          normal
          label="导出"
          @click="download"
          :loading="globalLoading['componentForm/exportExcelByComponent']"
        /> -->
      </template>

      <template v-slot:top-bottom>
        <!-- 应用中文/英文名 -->
        <f-formitem
          class="col-4 q-pr-sm"
          label-style="width:110px"
          bottom-page
          label="应用中文/英文名"
        >
          <fdev-select
            :value="selectValue"
            @input="updateSelectValue($event)"
            multiple
            use-input
            hide-dropdown-icon
            ref="select"
            @new-value="addSelect"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="setSelect($refs.select)"
              />
            </template>
          </fdev-select>
        </f-formitem>

        <!-- 使用版本 -->
        <f-formitem
          label-style="width:110px"
          class="col-4 q-pr-sm"
          bottom-page
          label="使用版本"
        >
          <fdev-select
            :value="useVersion"
            @input="updateUseVersion($event)"
            :options="versionOptions"
          />
        </f-formitem>

        <!-- 版本类型 -->
        <f-formitem
          class="col-4"
          label-style="width:110px"
          bottom-page
          label="版本类型"
        >
          <fdev-select
            :value="type"
            @input="updateType($event)"
            :options="typeOptions"
          />
        </f-formitem>
      </template>

      <!-- 应用英文名列 -->
      <template v-slot:body-cell-name_en="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/app/list/${props.row.application_id}`"
            class="link"
            v-if="props.row.application_id && !props.row.archetype"
          >
            <span :title="props.row.name_en">
              {{ props.row.name_en }}
            </span>
          </router-link>
          <span v-else :title="props.row.name_en">
            {{ props.row.name_en }}
          </span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.name_en }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <!-- 应用中文名列 -->
      <template v-slot:body-cell-name_zh="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/app/list/${props.row.application_id}`"
            class="link"
            v-if="props.row.application_id && !props.row.archetype"
          >
            <span :title="props.row.name_zh">
              {{ props.row.name_zh }}
            </span>
          </router-link>
          <span v-else :title="props.row.name_zh">
            {{ props.row.name_zh }}
          </span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.name_zh }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <!-- 当前版本类型列 -->
      <template v-slot:body-cell-type="props">
        <fdev-td>
          <span
            :class="[
              { 'text-red': props.value === '2' },
              { 'text-yellow-9': props.value === '3' }
            ]"
            :title="props.value ? webTypeDict[props.value] : '-'"
            >{{ props.value ? webTypeDict[props.value] : '-' }}</span
          >
        </fdev-td>
      </template>

      <!-- 应用行内负责人列 -->
      <template v-slot:body-cell-spdb_managers="props">
        <fdev-td>
          <div
            class="q-gutter-x-sm text-ellipsis"
            :title="jointNameCn(props.value)"
          >
            <span v-for="user in props.value" :key="user.id">
              <router-link
                :to="{ path: `/user/list/${user.id}` }"
                class="link"
                v-if="user.id"
              >
                {{ user.user_name_cn }}
              </router-link>
              <span v-else>{{ user.user_name_en }}</span>
            </span>
          </div>
        </fdev-td>
      </template>

      <!-- 应用负责人列 -->
      <template v-slot:body-cell-dev_managers="props">
        <fdev-td>
          <div
            class="q-gutter-x-sm text-ellipsis"
            :title="jointNameCn(props.value)"
          >
            <span v-for="user in props.value" :key="user.id">
              <router-link
                :to="{ path: `/user/list/${user.id}` }"
                class="link"
                v-if="user.id"
              >
                {{ user.user_name_cn }}
              </router-link>
              <span v-else>{{ user.user_name_en }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  webTypeDict,
  webComponentProfileAppUsingStatusColums
} from '@/modules/Component/utils/constants.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';
import { successNotify } from '@/utils/utils';

export default {
  name: 'WebAppUsing',
  components: { Loading },
  data() {
    return {
      dowmloadLoading: false,
      name: 'luo',
      tableData: [],
      filterValue: '',
      webTypeDict,
      columns: webComponentProfileAppUsingStatusColums
    };
  },
  watch: {
    selectValue(val) {
      this.filterValue = val.toString();
    },
    useVersion(val) {
      this.filterValue += ',table';
    },
    type(val) {
      this.filterValue += ',table';
    }
  },
  computed: {
    ...mapState('userActionSaveComponent/componentManage/weblist/webAppUsing', [
      'selectValue',
      'visibleColumns',
      'type',
      'useVersion'
    ]),
    ...mapState('componentForm', ['mpassComByCom']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    versionOptions() {
      return [
        '全部',
        ...new Set(
          this.mpassComByCom.map(item => {
            return item.component_version;
          })
        )
      ];
    },
    typeOptions() {
      return [
        '全部',
        ...new Set(
          this.mpassComByCom.map(item => {
            return item.type ? this.webTypeDict[item.type] : '-';
          })
        )
      ];
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/componentManage/weblist/webAppUsing',
      [
        'updateSelectValue',
        'updateUseVersion',
        'updateType',
        'updatevisibleColumns'
      ]
    ),
    ...mapActions('componentForm', [
      'queryWebcomByComponent',
      'scanAppByMpassCom'
    ]),
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    filter(rows, terms, cols, getCellValue) {
      const lowerTerms = terms.split(',').filter(item => {
        return item !== 'table' && item !== '';
      });
      return rows.filter(row => {
        let isUseVersion = true;
        let isType = true;
        let isExist = true;

        cols.forEach(col => {
          if (col.name === 'component_version' && this.useVersion !== '全部') {
            isUseVersion = row.component_version === this.useVersion;
          }
          if (col.name === 'type' && this.type !== '全部') {
            const filterType = row.type ? webTypeDict[row.type] : '-';
            isType = filterType === this.type;
          }
        });

        isExist = lowerTerms.every(item => {
          return cols.some(col => {
            return (
              (getCellValue(col, row) + '').toLowerCase().indexOf(item.trim()) >
              -1
            );
          });
        });
        return isUseVersion && isExist && isType;
      });
    },
    download() {},
    async scanSelectedComponent() {
      await this.scanAppByMpassCom({ component_id: this.$route.params.id });
      successNotify('发起扫描成功，请耐心等待扫描结果!');
    },
    formatJson(filterVal, appData) {
      return appData.map(row => {
        return filterVal.map(col => {
          if (col === 'group') {
            return row[col].name;
          } else if (col === 'spdb_managers' || col === 'dev_managers') {
            let userList = row[col].map(item => item.user_name_cn);
            return userList.join(' ');
          } else if (col === 'type') {
            return row[col] ? webTypeDict[row[col]] : '-';
          } else {
            return row[col];
          }
        });
      });
    },
    handleDownload() {
      let _this = this;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '应用英文名',
          '应用中文名',
          '使用版本',
          '当前版本类型',
          '所属小组',
          '应用行内负责人',
          '应用负责人',
          '数据更新时间'
        ];
        const filterVal = [
          'name_en',
          'name_zh',
          'component_version',
          'type',
          'group',
          'spdb_managers',
          'dev_managers',
          'update_time'
        ];
        const data = _this.formatJson(filterVal, this.tableData);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '应用使用现状列表',
          bookType: 'xlsx'
        });
      });
    },
    handleDownloadAll() {
      this.dowmloadLoading = true;
      // let { page, sortBy, descending } = this.pagination;
      // this.pagination.page = page;
      // this.pagination.sortBy = sortBy;
      // this.pagination.descending = descending;
      // //sortBy被q-table清空时的处理
      // if (!sortBy) {
      //   sortBy = this.sortBy;
      //   descending = !descending;
      // }
      // await this.queryUserPagination({
      //   ...this.queryParam,
      //   page,
      //   per_page: 0
      // });
      // this.totalPageUserData = this.userInPage.list.slice(0);
      this.handleDownload('全量');
      this.dowmloadLoading = false;
    },
    async init() {
      await this.queryWebcomByComponent({
        component_id: this.$route.params.id
      });
      this.tableData = this.mpassComByCom;
    },
    jointNameCn
  },
  created() {
    this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
  }
};
</script>

<style lang="stylus" scoped>
.w150
  width 150px
</style>
