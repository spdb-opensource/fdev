<template>
  <Loading :visible="loading">
    <fdev-table
      title="后端骨架列表"
      title-icon="list_s_f"
      :data="myArchetypeList"
      :columns="columns"
      row-key="archetype_id"
      :pagination.sync="pagination"
      :filter="searchValue"
      :filter-method="filter"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
      ref="table"
      class="my-sticky-column-table"
      no-export
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
              :to="{
                path: `/archetypeManage/server/archetype/${props.row.id}`
              }"
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
              :to="{
                path: `/archetypeManage/server/archetype/${props.row.id}`
              }"
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
              </router-link>
              <span v-else>{{ user.user_name_cn }}</span>
            </span>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-type="props">
        <fdev-td class="text-ellipsis">
          <span :title="props.value">{{ props.value }}</span>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { createArchetypeColumns } from '../../utils/constants';
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations, mapGetters } from 'vuex';
export default {
  name: 'archetypeTab',
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
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 1
      },
      loading: false
    };
  },
  computed: {
    ...mapState('componentForm', ['myArchetypeList']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', ['currentUser']),
    ...mapState('userActionSaveHomePage/archetypeTab', [
      'termsApp',
      'visibleCols',
      'currentPage'
    ]),
    ...mapGetters('userActionSaveHomePage/archetypeTab', ['searchValue']),
    columns() {
      return createArchetypeColumns(this.groups);
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
    ...mapActions('componentForm', ['queryMyArchetypes']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapMutations('userActionSaveHomePage/archetypeTab', [
      'saveTermsApp',
      'saveVisibleColumns',
      'saveCurrentPage'
    ]),
    async init() {
      this.loading = true;
      await this.queryMyArchetypes({ user_id: this.currentUser.id });
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
    }
  },
  async created() {
    await this.fetchGroup();
    this.init();
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 1) {
      this.saveVisibleColumns([
        'name_en',
        'name_cn',
        'manager_id',
        'group',
        'recommend_version',
        'type',
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
