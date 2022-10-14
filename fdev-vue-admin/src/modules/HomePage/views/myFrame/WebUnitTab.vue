<template>
  <Loading class="bg-white">
    <fdev-table
      ref="table"
      title="前端组件"
      title-icon="list_s_f"
      :data="comList"
      :columns="columns"
      :filter="searchValue"
      :filter-method="filter"
      :visible-columns="visibleCols"
      :pagination.sync="pagination"
      :on-select-cols="saveVisibleColumns"
      no-export
      class="my-sticky-column-table"
    >
      <template v-slot:top-bottom>
        <f-formitem label="搜索条件">
          <fdev-select
            :value="termsApp"
            @input="saveTermsApp($event)"
            multiple
            use-input
            hide-dropdown-icon
            placeholder="输入关键字，回车区分"
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

      <template v-slot:body-cell-name_en="props">
        <fdev-td auto-width>
          <div :title="props.row.name_en" class="text-ellipsis">
            <router-link
              :to="{ path: `/componentManage/web/weblist/${props.row.id}` }"
              class="link"
            >
              <span>{{ props.row.name_en }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_en }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-name_cn="props">
        <fdev-td auto-width>
          <div :title="props.row.name_en" class="text-ellipsis">
            <router-link
              :to="{ path: `/componentManage/web/weblist/${props.row.id}` }"
              class="link"
            >
              <span>{{ props.row.name_cn }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-manager="props">
        <fdev-td auto-width>
          <div
            class="q-gutter-x-sm td-width"
            :title="props.row.manager.map(v => v.user_name_cn).join('，')"
          >
            <span v-for="user in props.row.manager" :key="user.id">
              <router-link
                :to="`/user/list/${user.id}`"
                class="link"
                v-if="user.id"
              >
                <span>{{ user.user_name_cn }}</span>
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.manager.map(v => v.user_name_cn).join(' ') }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <span v-else>{{ user.user_name_cn }}</span>
            </span>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-gitlab_url="props">
        <fdev-td auto-width>
          <div :title="props.row.gitlab_url || '-'" class="text-ellipsis">
            <a
              :href="props.row.gitlab_url"
              target="_blank"
              class="text-primary"
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
          </div>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { createWebUnitColumns } from '../../utils/constants';
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations, mapGetters } from 'vuex';

export default {
  name: 'WebUnitTab',
  components: {
    Loading
  },
  data() {
    return {
      loading: false,
      comList: [],
      selectValue: [],
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 1
      },
      columns: createWebUnitColumns()
    };
  },
  computed: {
    ...mapState('componentForm', ['myWebComponentList']),
    ...mapState('user', ['currentUser']),
    ...mapState('userActionSaveHomePage/webUnitTab', [
      'termsApp',
      'visibleCols',
      'currentPage'
    ]),
    ...mapGetters('userActionSaveHomePage/webUnitTab', ['searchValue']),
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
    }
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryMyMpassComponents']),
    ...mapMutations('userActionSaveHomePage/webUnitTab', [
      'saveTermsApp',
      'saveVisibleColumns',
      'saveCurrentPage'
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
      let lowerTerms = this.termsApp.map(item => item.toLowerCase());
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
    async init() {
      await this.queryMyMpassComponents({ user_id: this.currentUser.id });
      this.comList = this.myWebComponentList;
    }
  },
  async created() {
    this.loading = true;
    await this.init();
    this.loading = false;
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 1) {
      this.saveVisibleColumns([
        'name_en',
        'name_cn',
        'manager',
        'recommond_version',
        'source',
        'npm_name',
        'npm_group',
        'type',
        'group',
        'gitlab_url',
        'root_dir',
        'desc'
      ]);
    }
  }
};
</script>

<style lang="stylus" scoped>
.td-width
  max-width 400px !important
  overflow hidden
  text-overflow ellipsis
</style>
