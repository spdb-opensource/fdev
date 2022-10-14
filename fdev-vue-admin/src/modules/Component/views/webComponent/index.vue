<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        class="my-sticky-column-table"
        title="前端组件列表"
        titleIcon="list_s_f"
        ref="table"
        :data="comList"
        :columns="columns"
        :filter="filterValue"
        :filter-method="filter"
        :visible-columns="visibleColumns"
        :onSelectCols="updatevisibleColumns"
        :pagination.sync="pagination"
        :export-func="handleDownloadAll"
      >
        <template v-slot:top-right>
          <div class="q-gutter-md">
            <!-- 组件新增功能暂时待定 -->
            <!-- <fdev-btn
              label="组件新增"
              @click="handleDialogOpen(0)"
              color="primary"
            /> -->
            <fdev-btn
              ficon="add"
              label="组件录入"
              normal
              @click="handleDialogOpen(null)"
              v-if="isInfrastManager"
            />
            <fdev-btn
              ficon="link_icon"
              normal
              label="集成现状"
              @click="
                $router.push({
                  name: 'WebIntergration'
                })
              "
            />
          </div>
        </template>

        <template v-slot:top-bottom>
          <!-- 搜索条件 -->
          <f-formitem label="搜索条件">
            <fdev-select
              :value="selectValue"
              @input="updateSelectValue($event)"
              multiple
              use-input
              hide-dropdown-icon
              ref="select"
              @new-value="addSelect"
              placeholder="输入关键字，回车区分"
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
        </template>

        <!-- 组件英文名列 -->
        <template v-slot:body-cell-name_en="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :to="{ path: `/componentManage/web/weblist/${props.row.id}` }"
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
              :to="{ path: `/componentManage/web/weblist/${props.row.id}` }"
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

        <!-- 组件管理员列 -->
        <template v-slot:body-cell-manager="props">
          <fdev-td class="text-ellipsis">
            <div
              class="q-gutter-x-sm text-ellipsis"
              :title="jointNameCn(props.row.manager)"
            >
              <span v-for="user in props.row.manager" :key="user.id">
                <router-link
                  :to="`/user/list/${user.id}`"
                  class="link"
                  v-if="user.id"
                >
                  <span>{{ user.user_name_cn }}</span>
                </router-link>
                <span v-else>{{ user.user_name_cn }}</span>
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
            </a>
            <span v-else>-</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.gitlab_url }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <!-- 操作列 -->
        <template v-slot:body-cell-btn="props">
          <fdev-td auto-width>
            <div v-if="isManger(props.row.manager)">
              <fdev-btn
                label="信息维护"
                @click="handleDialogOpen(props.row)"
                flat
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
      <!-- 前端组件录入、更新 -->
      <UpdateDialog v-model="dialogOpen" :data="updateData" @click="init" />
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import UpdateDialog from '@/modules/Component/views/webComponent/components/addAndUpdate';
import {
  webComponentColums,
  webTypeOptions,
  webSourceOptions
} from '@/modules/Component/utils/constants.js';
import {
  jointNameCn,
  handleDownload
} from '@/modules/Component/utils/utils.js';
import {
  getWebPagination,
  setWebPagination
} from '@/modules/Component/utils/setting.js';

export default {
  name: 'WebComponentList',
  components: {
    Loading,
    UpdateDialog
  },
  data() {
    return {
      loading: false,
      comList: [],
      updateData: '0',
      dialogOpen: false,
      filterValue: '',
      pagination: getWebPagination()
    };
  },
  computed: {
    ...mapState('userActionSaveComponent/componentManage/weblist', [
      'selectValue',
      'visibleColumns'
    ]),
    ...mapState('userForm', ['groups']),
    ...mapState('componentForm', ['mpassComponents']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isInfrastManager']),
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    },
    isManger() {
      return function(managerList) {
        const haveRole = this.currentUser.role.some(
          v => v.label === '基础架构管理员'
        );
        const isManger = managerList
          ? managerList.some(user => user.id === this.currentUser.id)
          : false;
        return isManger || haveRole;
      };
    },
    columns() {
      return webComponentColums(this.groups);
    }
  },
  watch: {
    pagination(val) {
      setWebPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString();
    }
  },
  methods: {
    ...mapMutations('userActionSaveComponent/componentManage/weblist', [
      'updateSelectValue',
      'updatevisibleColumns'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('componentForm', ['queryMpassComponents']),
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
    handleDialogOpen(data) {
      this.updateData = data;
      this.dialogOpen = true;
    },
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = this.selectValue.map(item => item.toLowerCase());
      return rows.filter(row => {
        return lowerTerms.every(item => {
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
      });
    },
    handleDownloadAll() {
      const dictObj = {
        source: webSourceOptions,
        type: webTypeOptions,
        isTest: [{ label: '否', value: '0' }, { label: '是', value: '1' }]
      };
      handleDownload(
        this.columns,
        this.comList,
        this.groups,
        dictObj,
        'manager',
        '前端组件列表'
      );
    },
    async init() {
      await this.fetchGroup();
      await this.queryMpassComponents();
      this.comList = this.mpassComponents;
    },
    jointNameCn
  },
  async created() {
    this.loading = true;
    await this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
    this.loading = false;
  }
};
</script>

<style lang="stylus" scoped></style>
