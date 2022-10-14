<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        class="my-sticky-column-table"
        title="镜像列表"
        :data="imageManageList"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        :filter="filterValue"
        :filter-method="filter"
        ref="table"
        :visible-columns="visibleColumns"
        title-icon="list_s_f"
        :onSelectCols="updatevisibleColumns"
        :export-func="handleDownloadAll"
      >
        <template v-slot:top-right>
          <div>
            <fdev-tooltip anchor="top middle" v-if="!isManger">
              需要基础架构管理员权限
            </fdev-tooltip>
            <fdev-btn
              normal
              ficon="add"
              label="镜像录入"
              :disable="!isManger"
              @click="dialogOpen = true"
            />
          </div>
        </template>

        <template v-slot:top-bottom>
          <!-- 搜索条件 -->
          <f-formitem label="搜索条件">
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
        </template>

        <!-- 镜像英文名列 -->
        <template v-slot:body-cell-name="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :to="{ path: `/imageManage/${props.row.id}` }"
              class="link"
            >
              <span :title="props.row.name">{{ props.row.name }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <!-- 镜像中文名列 -->
        <template v-slot:body-cell-name_cn="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :to="{ path: `/imageManage/${props.row.id}` }"
              class="link"
            >
              <span :title="props.row.name_cn">{{ props.row.name_cn }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <!-- 管理员列 -->
        <template v-slot:body-cell-manager="props">
          <fdev-td>
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

        <!-- gitlab地址列 -->
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
          </fdev-td>
        </template>

        <!-- wiki地址列 -->
        <template v-slot:body-cell-wiki="props">
          <fdev-td class="text-ellipsis">
            <a
              :href="props.row.wiki"
              target="_blank"
              class="text-primary"
              :title="props.row.wiki"
              v-if="props.row.wiki"
            >
              {{ props.row.wiki }}
            </a>
            <span v-else>-</span>
          </fdev-td>
        </template>

        <!-- 操作列 -->
        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <div v-if="isManger(props.row.manager_id)">
              <fdev-btn
                label="信息维护"
                @click="handleDialogOpen(props.row)"
                flat
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>

      <!-- 组件录入、更新 -->
      <AddImageDialog
        v-model="dialogOpen"
        :value="dialogOpen"
        @refresh="init"
      />
    </Loading>
  </f-block>
</template>

<script>
import AddImageDialog from '@/modules/Component/views/imageManage/components/addAndUpdate';
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import {
  getPagination,
  setPagination
} from '@/modules/Component/utils/setting.js';
import { imageManageColumns } from '@/modules/Component/utils/constants.js';
import {
  jointNameCn,
  handleDownload
} from '@/modules/Component/utils/utils.js';

export default {
  name: 'ImageManage',
  components: { Loading, AddImageDialog },
  data() {
    return {
      loading: false,
      dialogOpen: false,
      pagination: getPagination(),
      filterValue: '',
      updateData: {}
    };
  },
  computed: {
    ...mapState('userActionSaveComponent/imageManage', [
      'selectValue',
      'visibleColumns'
    ]),
    ...mapState('userForm', ['groups']),
    ...mapState('componentForm', ['imageManageList']),
    ...mapState('user', ['currentUser']),
    btnDisable() {
      if (this.rowSelected.length === 0) {
        return true;
      }
      return this.rowSelected[0].component_id === this.currentUser.id
        ? false
        : true;
    },
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      return haveRole;
    },
    columns() {
      return imageManageColumns(this.groups);
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString();
    }
  },
  methods: {
    ...mapMutations('userActionSaveComponent/imageManage', [
      'updateSelectValue',
      'updatevisibleColumns'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('componentForm', ['queryBaseImage']),
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
      this.dialogOpen = true;
      this.updateData = data;
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
      handleDownload(
        this.columns,
        this.imageManageList,
        this.groups,
        null,
        'manager',
        '镜像列表'
      );
    },
    async init() {
      this.loading = true;
      await this.queryBaseImage();
      this.loading = false;
    },
    jointNameCn
  },
  async created() {
    await this.fetchGroup();
    this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
  }
};
</script>
<style lang="stylus" scoped></style>
