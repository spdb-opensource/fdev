<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      title="应用使用现状列表"
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      :filter="filterValue"
      :filter-method="filter"
      ref="table"
      :visible-columns="visibleColumns"
      no-export
      :onSelectCols="
        imageManageProfile ? updatevisibleColumns_ima : updatevisibleColumns_com
      "
    >
      <template v-slot:top-right>
        <fdev-btn
          normal
          ficon="search_s_o"
          label="实时扫描"
          @click="scanSelectedComponent"
        />
        <fdev-btn
          v-if="!isFrameWork"
          ficon="exit"
          normal
          label="导出"
          @click="download"
          :loading="globalLoading['componentForm/exportExcelByComponent']"
        />
      </template>

      <template v-slot:top-bottom>
        <!-- 应用中文/英文名 -->
        <f-formitem class="col-4 q-pr-sm" bottomPage label="应用中文/英文名">
          <fdev-select
            :value="selectValue"
            @input="changeSelectValue($event)"
            placeholder="请输入"
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
        <f-formitem class="col-4 q-pr-sm" bottomPage label="使用版本">
          <fdev-select
            :value="useVersion"
            @input="changeUseVersion($event)"
            :options="versionOptions"
          />
        </f-formitem>

        <!-- 版本类型 -->
        <f-formitem class="col-4" bottomPage label="版本类型">
          <fdev-select
            :value="type"
            @input="changeType($event)"
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
          <span v-else :title="props.row.name_en">{{ props.row.name_en }}</span>
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
          <span v-else :title="props.row.name_zh">{{ props.row.name_zh }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.name_zh }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <!-- 当前版本类型列 -->
      <template v-slot:body-cell-type="props" v-if="!isFrameWork">
        <fdev-td class="text-ellipsis">
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

      <!-- 当前版本类型列 -->
      <template v-slot:body-cell-stage="props" v-else>
        <fdev-td class="text-ellipsis">
          <span :title="props.value ? imageTypeDict[props.value] : '-'">
            {{ props.value ? imageTypeDict[props.value] : '-' }}
          </span>
        </fdev-td>
      </template>

      <!-- 所属小组列 -->
      <template v-slot:body-cell-group="props">
        <fdev-td class="text-ellipsis">
          <span :title="props.value.name">
            {{ props.value.name }}
          </span>
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
import { successNotify, exportExcel } from '@/utils/utils';
import {
  getUsingPagination,
  setUsingPagination
} from '@/modules/Component/utils/setting.js';
import {
  typeDict,
  imageTypeDict,
  serverComponentProfileAppUsingStatusColums
} from '@/modules/Component/utils/constants.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';

export default {
  name: 'UsingStatus',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: getUsingPagination(),
      filterValue: '',
      versionOptions: [],
      typeOptions: [],
      imageManageProfile: this.$route.name === 'imageManageProfile',
      ComponentProfile: this.$route.name === 'ComponentProfile',
      component_id: '',
      typeDict: typeDict,
      columns: serverComponentProfileAppUsingStatusColums(this.isFrameWork),
      imgNameEn: '',
      imageTypeDict: imageTypeDict
    };
  },
  props: {
    isFrameWork: {
      type: Boolean,
      default: false
    },
    nameImage: {
      type: String,
      default: ''
    }
  },
  watch: {
    pagination(val) {
      setUsingPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString().toLowerCase();
      if (val.length === 0) {
        this.filterValue = ',';
      }
    },
    useVersion(val) {
      this.filterValue += ',table';
    },
    type(val) {
      this.filterValue += ',table';
    },
    '$route.params': {
      deep: true,
      handler(val) {
        this.component_id = val.id;
        this.init();
      }
    }
  },
  computed: {
    ...mapState(
      'userActionSaveComponent/componentManage/componentList/appUsingStatus',
      {
        selectValue_com: 'selectValue',
        visibleColumns_com: 'visibleColumns',
        useVersion_com: 'useVersion',
        type_com: 'type'
      }
    ),
    ...mapState('userActionSaveComponent/imageManageProfile/appUsingStatus', {
      selectValue_ima: 'selectValue',
      visibleColumns_ima: 'visibleColumns',
      useVersion_ima: 'useVersion',
      type_ima: 'type'
    }),
    ...mapState('componentForm', [
      'usingStatusList',
      'componentExport',
      'baseImageDetail'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    selectValue() {
      if (this.imageManageProfile) {
        return this.selectValue_ima;
      } else {
        return this.selectValue_com;
      }
    },
    visibleColumns() {
      if (this.imageManageProfile) {
        return this.visibleColumns_ima;
      } else {
        return this.visibleColumns_com;
      }
    },
    useVersion() {
      if (this.imageManageProfile) {
        return this.useVersion_ima;
      } else {
        return this.useVersion_com;
      }
    },
    type() {
      if (this.imageManageProfile) {
        return this.type_ima;
      } else {
        return this.type_com;
      }
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/appUsingStatus',
      {
        updateSelectValue_com: 'updateSelectValue',
        updatevisibleColumns_com: 'updatevisibleColumns',
        updateType_com: 'updateType',
        updateUseVersion_com: 'updateUseVersion'
      }
    ),
    ...mapMutations(
      'userActionSaveComponent/imageManageProfile/appUsingStatus',
      {
        updateSelectValue_ima: 'updateSelectValue',
        updatevisibleColumns_ima: 'updatevisibleColumns',
        updateType_ima: 'updateType',
        updateUseVersion_ima: 'updateUseVersion'
      }
    ),
    ...mapActions('componentForm', [
      'queryUsingStatusList',
      'scanComponent',
      'exportExcelByComponent',
      'queryApplicationByImage',
      'scanImage'
    ]),
    changeSelectValue(params) {
      if (this.imageManageProfile) {
        this.updateSelectValue_ima(params);
      } else {
        this.updateSelectValue_com(params);
      }
    },
    changeUseVersion(params) {
      if (this.imageManageProfile) {
        this.updateUseVersion_ima(params);
      } else {
        this.updateUseVersion_com(params);
      }
    },
    changeType(params) {
      if (this.imageManageProfile) {
        this.updateType_ima(params);
      } else {
        this.updateType_com(params);
      }
    },
    changevisibleColumns(params) {
      if (this.imageManageProfile) {
        this.updatevisibleColumns_ima(params);
      } else {
        this.updatevisibleColumns_com(params);
      }
    },
    // 表格筛选，组件监听filterValue
    filter(rows, terms, cols, getCellValue) {
      const lowerTerms = terms.split(',').filter(item => {
        return item !== 'table' && item !== '';
      });
      return rows.filter(row => {
        let isUseVersion = true;
        let isType = true;
        let isExist = true;

        cols.forEach(col => {
          if (
            !this.isFrameWork &&
            col.name === 'component_version' &&
            this.useVersion !== '全部'
          ) {
            isUseVersion = row.component_version === this.useVersion;
          } else if (
            this.isFrameWork &&
            col.name === 'image_tag' &&
            this.useVersion !== '全部'
          ) {
            isUseVersion = row.image_tag === this.useVersion;
          }

          if (col.name === 'type' && this.type !== '全部') {
            const filterType = row.type ? typeDict[row.type] : '-';
            isType = filterType === this.type;
          }
          if (col.name === 'stage' && this.type !== '全部') {
            const filterType = row.stage ? this.imageTypeDict[row.stage] : '-';
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
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    }, // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    getVersionOptions() {
      if (this.isFrameWork) {
        this.versionOptions = [
          '全部',
          ...new Set(
            this.usingStatusList.map(item => {
              return item.image_tag;
            })
          )
        ];
        this.typeOptions = [
          '全部',
          ...new Set(
            this.usingStatusList.map(item => {
              return item.stage ? imageTypeDict[item.stage] : '-';
            })
          )
        ];
      } else {
        this.versionOptions = [
          '全部',
          ...new Set(
            this.usingStatusList.map(item => {
              return item.component_version;
            })
          )
        ];
        this.typeOptions = [
          '全部',
          ...new Set(
            this.usingStatusList.map(item => {
              return item.type ? typeDict[item.type] : '-';
            })
          )
        ];
      }
    },
    async scanSelectedComponent() {
      if (this.isFrameWork) {
        await this.scanImage({ image_name: this.baseImageDetail.name });
      } else {
        await this.scanComponent({ component_id: this.component_id });
      }
      successNotify('发起扫描成功，请耐心等待扫描结果!');
    },
    async download() {
      await this.exportExcelByComponent({
        component_id: this.component_id
      });
      exportExcel(this.componentExport);
    },
    async init() {
      this.loading = true;
      if (this.isFrameWork) {
        await this.queryApplicationByImage({
          image_name: this.nameImage
        });
      } else {
        await this.queryUsingStatusList({
          component_id: this.component_id
        });
      }

      this.tableData = this.usingStatusList;
      this.getVersionOptions();
      this.loading = false;
    },
    jointNameCn
  },
  created() {
    this.component_id = this.$route.params.id;
    this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
  }
};
</script>
