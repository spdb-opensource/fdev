<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        class="my-sticky-column-table"
        title="后端组件列表"
        titleIcon="list_s_f"
        :data="componentList"
        :columns="columns"
        row-key="component_id"
        :pagination.sync="pagination"
        :filter="filterValue"
        :filter-method="filter"
        ref="table"
        :visible-columns="visibleColumns"
        :onSelectCols="updatevisibleColumns"
        title-icon="list_s_f"
        :export-func="handleDownloadAll"
      >
        <template v-slot:top-right>
          <fdev-btn
            normal
            label="组件新增"
            ficon="add"
            @click="handleDialogOpen(0)"
            v-if="isManger(null)"
          />
          <fdev-btn
            normal
            label="组件录入"
            ficon="add"
            @click="handleDialogOpen(null)"
            v-if="isManger(null)"
          />
          <fdev-btn
            normal
            label="集成现状"
            ficon="link_icon"
            v-if="componentList.length > 0"
            @click="
              $router.push({
                name: 'Intergration'
              })
            "
          />
        </template>

        <template v-slot:top-bottom>
          <!-- 搜索条件 -->
          <f-formitem
            label="搜索条件"
            class="q-pr-sm"
            help="选择列中已选中的列均可支持模糊查询"
          >
            <fdev-select
              :value="selectValue"
              @input="updateSelectValue($event)"
              placeholder="输入关键字，回车区分"
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

          <f-formitem label="组件类型" class="q-pr-sm">
            <fdev-select
              @input="updateType($event)"
              :value="type"
              :options="filtertype"
              option-label="label"
              @filter="typeInputFilter"
              use-input
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.label">
                      {{ scope.opt.label }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </template>

        <!-- 组件英文名列 -->
        <template v-slot:body-cell-name_en="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :to="{ path: `/componentManage/server/list/${props.row.id}` }"
              class="link"
            >
              <span :title="props.row.name_en">
                {{ props.row.name_en }}
              </span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_en }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <!-- 组件中文名列 -->
        <template v-slot:body-cell-name_cn="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :to="{ path: `/componentManage/server/list/${props.row.id}` }"
              class="link"
            >
              <span :title="props.row.name_cn">
                {{ props.row.name_cn }}
              </span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <!-- 负责人列 -->
        <template v-slot:body-cell-manager_id="props">
          <fdev-td>
            <div
              class="q-gutter-x-sm text-ellipsis"
              :title="jointNameCn(props.row.manager_id)"
            >
              <span v-for="user in props.row.manager_id" :key="user.id">
                <router-link
                  :to="`/user/list/${user.id}`"
                  class="link"
                  v-if="user.id"
                >
                  <span>
                    {{ user.user_name_cn }}
                  </span>
                </router-link>
                <span v-else>
                  {{ user.user_name_cn }}
                </span>
              </span>
            </div>
          </fdev-td>
        </template>

        <!-- gitlab_url列 -->
        <template v-slot:body-cell-gitlab_url="props">
          <fdev-td class="text-ellipsis">
            <a
              :href="props.row.gitlab_url"
              target="_blank"
              class="text-primary"
              :title="props.row.gitlab_url"
              v-if="props.row.gitlab_url"
            >
              {{ props.row.gitlab_url }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.gitlab_url }}
                </fdev-banner>
              </fdev-popup-proxy>
            </a>
            <span v-else>-</span>
          </fdev-td>
        </template>

        <!-- wiki地址列 -->
        <template v-slot:body-cell-wiki_url="props">
          <fdev-td class="text-ellipsis">
            <a
              :href="props.row.wiki_url"
              target="_blank"
              class="text-primary"
              :title="props.row.wiki_url"
              v-if="props.row.wiki_url"
            >
              {{ props.row.wiki_url }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.wiki_url }}
                </fdev-banner>
              </fdev-popup-proxy>
            </a>
            <span v-else>-</span>
          </fdev-td>
        </template>

        <!-- 操作列 -->
        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <div
              class="q-gutter-x-sm row no-wrap"
              v-if="isManger(props.row.manager_id)"
            >
              <fdev-btn
                label="信息维护"
                @click="handleDialogOpen(props.row, 1)"
                flat
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>

      <!-- 组件录入、更新弹窗 -->
      <UpdateDialog v-model="dialogOpen" :data="updateData" @click="init" />
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import {
  getPagination,
  setPagination
} from '@/modules/Component/utils/setting.js';
import {
  serverComponentColums,
  typeOptions,
  sourceOptions
} from '@/modules/Component/utils/constants.js';
import {
  jointNameCn,
  handleDownload
} from '@/modules/Component/utils/utils.js';
import UpdateDialog from '@/modules/Component/views/serverComponent/components/addAndUpdate.vue';

export default {
  name: 'ComponentList',
  components: { Loading, UpdateDialog },
  data() {
    return {
      loading: false,
      dialogOpen: false,
      pagination: getPagination(),
      filterValue: '',
      updateData: {},
      filtertype: []
    };
  },
  computed: {
    ...mapState('userActionSaveComponent/componentManage/componentList', [
      'selectValue',
      'type',
      'visibleColumns'
    ]),
    ...mapState('userForm', ['groups']),
    ...mapState('componentForm', ['componentList']),
    ...mapState('user', ['currentUser']),
    btnDisable() {
      if (this.rowSelected.length === 0) {
        return true;
      }
      return this.rowSelected[0].component_id === this.currentUser.id
        ? false
        : true;
    },
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    },
    columns() {
      return serverComponentColums(this.groups);
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    // selectValue(val) {
    //   this.filterValue = val.toString();
    // },
    selectValue(val) {
      this.fetchFilter();
    },
    type(val) {
      this.fetchFilter();
    }
  },
  methods: {
    ...mapMutations('userActionSaveComponent/componentManage/componentList', [
      'updateSelectValue',
      'updateType',
      'updatevisibleColumns'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('componentForm', ['queryComponent']),
    fetchFilter() {
      this.filterValue = [
        ...this.selectValue,
        (this.type && this.type.label) || ''
      ]
        .filter(item => item)
        .toString()
        .toLowerCase();
    },
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
    handleDialogOpen(data, update) {
      this.dialogOpen = true;
      // 修改页面 没有小组的时候显示异常
      if (update) {
        data.group = data.group ? data.group : '';
      }
      this.updateData = data;
    },
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = [
        { key: 'search', value: this.selectValue },
        {
          key: 'type',
          value: this.type ? [this.type.label] : ''
        }
      ].filter(item => item.value);
      lowerTerms.forEach(
        searchTerms =>
          (searchTerms.value = searchTerms.value.map(item =>
            item.toLowerCase()
          ))
      );
      return rows.filter(row => {
        return lowerTerms.every(searchTerms => {
          if (searchTerms.key === 'type') {
            // 组件类型精准查询
            return searchTerms.value.every(item => {
              return cols.some(col => {
                return getCellValue(col, row) === item;
              });
            });
          } else {
            // 其他查询条件模糊查询
            return searchTerms.value.every(item => {
              return cols.some(col => {
                if (Array.isArray(getCellValue(col, row))) {
                  return getCellValue(col, row).some(v => {
                    return v.user_name_cn.toLowerCase().indexOf(item) > -1;
                  });
                } else {
                  return (
                    (getCellValue(col, row) + '')
                      .toLowerCase()
                      .indexOf(item.trim()) > -1
                  );
                }
              });
            });
          }
        });
      });
    },
    handleDownloadAll() {
      const dictObj = {
        source: sourceOptions,
        type: typeOptions,
        sonar_scan_switch: [
          { label: '关', value: '0' },
          { label: '开', value: '1' }
        ],
        isTest: [{ label: '否', value: '0' }, { label: '是', value: '1' }]
      };
      handleDownload(
        this.columns,
        this.componentList,
        this.groups,
        dictObj,
        'manager_id',
        '后端组件列表'
      );
    },
    async init() {
      this.loading = true;
      await this.fetchGroup();
      this.filtertype = typeOptions;
      await this.queryComponent();
      this.loading = false;
    },
    typeInputFilter(val, update) {
      update(() => {
        this.filtertype = typeOptions.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    isManger(managerList) {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManger = managerList
        ? managerList.some(user => user.id === this.currentUser.id)
        : false;
      return isManger || haveRole;
    },
    jointNameCn
  },
  created() {
    this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
  },
  beforeRouteEnter(to, from, next) {
    const { params, path } = from;
    if (Object.keys(params).length === 0) {
      if (path !== '/component/intergration') {
        sessionStorage.removeItem('componentsTerms');
      }
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params, path } = to;
    if (Object.keys(params).length || path === '/component/intergration') {
      sessionStorage.setItem(
        'componentsTerms',
        JSON.stringify(this.selectValue)
      );
    }
    next();
  }
};
</script>
<style lang="stylus" scoped></style>
