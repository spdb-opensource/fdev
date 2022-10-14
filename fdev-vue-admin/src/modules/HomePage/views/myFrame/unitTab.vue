<template>
  <Loading :visible="loading">
    <fdev-table
      :data="myComponentList"
      :columns="columns"
      title="后端组件列表"
      title-icon="list_s_f"
      row-key="myComponent_id"
      :pagination.sync="pagination"
      :filter="searchValue"
      :filter-method="filter"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
      class="my-sticky-column-table"
      no-export
      ref="table"
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
              :to="{ path: `/componentManage/server/list/${props.row.id}` }"
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
          <div :title="props.row.name_cn" class="text-ellipsis">
            <router-link
              :to="{ path: `/componentManage/server/list/${props.row.id}` }"
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

      <template v-slot:body-cell-manager_id="props">
        <fdev-td auto-width>
          <div
            class="q-gutter-x-sm td-width"
            :title="props.row.manager_id.map(v => v.user_name_cn).join('，')"
          >
            <span v-for="user in props.row.manager_id" :key="user.id">
              <router-link
                :to="`/user/list/${user.id}`"
                class="link"
                v-if="user.id"
              >
                <span>{{ user.user_name_cn }}</span>
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      props.row.manager_id.map(v => v.user_name_cn).join(' ')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <span v-else>{{ user.user_name_cn }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { createUnitColumns } from '../../utils/constants';
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations, mapGetters } from 'vuex';

export default {
  name: 'unitTab',
  components: { Loading },
  props: {
    role: {
      type: Boolean
    },
    id: {
      type: String
    }
  },
  data() {
    return {
      loading: false,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 1
      },
      columns: createUnitColumns(),
      visibleColumns: [
        'name_en',
        'name_cn',
        'manager_id',
        'recommond_version',
        'source'
      ]
    };
  },
  computed: {
    ...mapState('componentForm', ['myComponentList']),
    ...mapState('user', ['currentUser']),
    ...mapState('userActionSaveHomePage/unitTab', [
      'visibleCols',
      'currentPage',
      'termsApp'
    ]),
    ...mapGetters('userActionSaveHomePage/unitTab', ['searchValue']),
    btnDisable() {
      if (this.rowSelected.length === 0) {
        return true;
      }
      return this.rowSelected[0].myComponent_id === this.currentUser.id
        ? false
        : true;
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
    ...mapActions('componentForm', ['queryMyComponent']),
    ...mapMutations('userActionSaveHomePage/unitTab', [
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
      this.loading = true;
      await this.queryMyComponent({ user_id: this.currentUser.id });
      this.loading = false;
    },
    isManger(managerList) {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManger = managerList
        ? managerList.some(user => user.id === this.currentUser.id)
        : false;
      return isManger || haveRole;
    }
  },
  async created() {
    this.init();
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns(this.visibleColumns);
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
