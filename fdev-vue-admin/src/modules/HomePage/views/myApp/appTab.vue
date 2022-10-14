<template>
  <Loading :visible="loading">
    <fdev-table
      title="应用列表"
      :data="MyAppData"
      :columns="columns"
      :filter="searchValue"
      :filter-method="appFilter"
      :pagination.sync="pagination"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
      class="my-sticky-column-table"
      no-export
      titleIcon="list_s_f"
    >
      <template v-slot:top-bottom>
        <f-formitem label="搜索条件">
          <fdev-select
            use-input
            multiple
            hide-dropdown-icon
            :value="termsApp"
            @input="saveTermsApp($event)"
            @new-value="addTerm"
            placeholder="输入关键字，回车区分"
            class="table-head-input"
            ref="terms"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="addNewValue"
              />
            </template>
          </fdev-select>
        </f-formitem>
      </template>

      <!-- 应用中文名 -->
      <template v-slot:body-cell-name_zh="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/app/list/${props.row.id}`"
            :title="props.value"
            class="link"
          >
            {{ props.value }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </fdev-td>
      </template>

      <!-- 应用英文名 -->
      <template v-slot:body-cell-name_en="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/app/list/${props.row.id}`"
            :title="props.value"
            class="link"
          >
            {{ props.value }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </fdev-td>
      </template>

      <!-- 行内应用负责人 -->
      <template v-slot:body-cell-hangnei="props">
        <fdev-td>
          <div
            class="td-width"
            :title="props.row.spdb_managers.map(v => v.user_name_cn).join('，')"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.id"
                class="link"
                :to="{ path: `/user/list/${item.id}` }"
              >
                {{ item.user_name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      props.row.spdb_managers.map(v => v.user_name_cn).join(' ')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <span v-else>{{ item.user_name_cn }}</span>
            </span>
          </div>
        </fdev-td>
      </template>

      <!-- 应用负责人 -->
      <template v-slot:body-cell-yingyong="props">
        <fdev-td>
          <div
            class="td-width"
            :title="props.row.dev_managers.map(v => v.user_name_cn).join('，')"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.id"
                class="link"
                :to="{ path: `/user/list/${item.id}` }"
              >
                {{ item.user_name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      props.row.dev_managers.map(v => v.user_name_cn).join(' ')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <span v-else>{{ item.user_name_cn }}</span>
            </span>
          </div>
        </fdev-td>
      </template>

      <!-- 标签 -->
      <template v-slot:body-cell-label="props">
        <fdev-td class="text-ellipsis">
          <span v-if="props.row.label" :title="props.value.join('，')">
            {{ props.value.join('，') }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value.join('，') }}
              </fdev-banner>
            </fdev-popup-proxy>
          </span>
          <span v-else title="-">-</span>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { createAppColumns } from '../../utils/constants';
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations, mapGetters } from 'vuex';

export default {
  name: 'appTap',
  components: {
    Loading
  },
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
      pagination: {},
      selected: [],
      filter: '',
      columns: createAppColumns()
    };
  },
  computed: {
    ...mapState('appForm', ['MyAppData']),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userActionSaveHomePage/myAppPage', [
      'termsApp',
      'visibleCols',
      'currentPage'
    ]),
    ...mapGetters('userActionSaveHomePage/myAppPage', ['searchValue'])
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  methods: {
    ...mapActions('user', {
      fetchUser: 'fetch',
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapActions('appForm', {
      queryMyApps: 'queryMyApps'
    }),
    ...mapMutations('userActionSaveHomePage/myAppPage', [
      'saveTermsApp',
      'saveVisibleColumns',
      'saveCurrentPage'
    ]),
    addNewValue() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    appFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term === '') {
            return true;
          }
          let hasCol = cols.some(col => {
            let value = '';
            if (Array.isArray(cellValue(col, row))) {
              value = cellValue(col, row).map(user => user.user_name_cn);
              return (
                value
                  .toString()
                  .toLowerCase()
                  .indexOf(term) > -1
              );
            } else {
              return (
                (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1
              );
            }
          });
          row.label = row.label ? row.label : [];
          let hasLable = row.label.find(lab => {
            if (lab.toLowerCase().indexOf(term) > -1) {
              return row;
            }
          });
          return hasLable || hasCol;
        });
      });
    },
    async appUsing() {
      this.loading = true;
      await this.queryMyApps({ user_id: this.currentUser.id });
      this.loading = false;
    }
  },
  async created() {
    this.fetchCurrent();
    this.appUsing();
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString()) {
      this.saveVisibleColumns([
        'name_zh',
        'name_en',
        'group',
        'hangnei',
        'yingyong'
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
