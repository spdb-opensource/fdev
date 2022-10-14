<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      :filter="searchValue"
      no-export
      :filter-method="filter"
      :pagination.sync="pagination"
      :title-icon="label === 'todo' ? 'todo_list_s_f' : 'check_s_f'"
      :title="`${tableName[label]}列表`"
      no-select-cols
      class="my-sticky-column-table"
    >
      <template v-slot:top-bottom>
        <f-formitem label="搜索条件">
          <fdev-select
            use-input
            placeholder="输入关键字，回车区分"
            use-chips
            multiple
            hide-dropdown-icon
            @input="saveTerms($event)"
            :value="terms"
            @new-value="addTerm"
            ref="terms"
          >
            <template v-slot:append>
              <f-icon
                class="cursor-pointer"
                name="search"
                @click="setSelect($refs.terms)"
              />
            </template>
          </fdev-select>
        </f-formitem>
      </template>
      <template v-slot:body="props">
        <router-link
          tag="tr"
          :to="props.row.link.substring(props.row.link.indexOf('#') + 1)"
          v-if="props.row.link.indexOf('/fdev/#/') > -1"
        >
          <td
            key="module"
            class="text-ellipsis"
            :title="props.row.module || '-'"
          >
            {{ props.row.module || '-' }}
          </td>
          <td
            key="description"
            :auto-width="false"
            class="text-ellipsis"
            :title="props.row.description || '-'"
          >
            {{ props.row.description || '-' }}
          </td>

          <td key="user_list">
            <div
              :title="props.row.user_list.map(v => v.user_name_cn).join('，')"
              class="text-ellipsis"
            >
              <span v-for="(item, index) in props.row.user_list" :key="index">
                <router-link
                  :to="`/user/list/${item.id}`"
                  class="link"
                  v-if="item.id"
                >
                  {{ item.user_name_cn }}
                </router-link>
                <span v-else>{{ item.user_name_cn }}</span>
              </span>
            </div>
          </td>

          <td
            key="createTime"
            class="text-ellipsis"
            :title="props.row.createTime || '-'"
          >
            {{ props.row.createTime || '-' }}
          </td>

          <td
            key="status"
            class="text-ellipsis"
            :title="props.row.status | filterStatus"
          >
            {{ props.row.status | filterStatus }}
          </td>

          <td key="executor" class="text-ellipsis">
            <router-link
              v-if="props.row.executor_id"
              :to="`/user/list/${props.row.executor_id}`"
              class="link"
              :title="props.row.executor_name_cn"
            >
              {{ props.row.executor_name_cn }}
            </router-link>
            <span v-else :title="props.row.executor_name_cn">{{
              props.row.executor_name_cn
            }}</span>
          </td>

          <td
            key="executeTime"
            class="text-ellipsis"
            :title="props.row.executeTime || '-'"
          >
            {{ props.row.executeTime || '-' }}
          </td>

          <td key="btn">
            <fdev-btn
              flat
              :label="btnLabel"
              @click.stop="updateTodoStatus(props.row.id)"
            />
          </td>
        </router-link>

        <tr class="cursor-pointer" v-else @click="linkTo(props.row.link)">
          <td
            key="module"
            class="text-ellipsis"
            :title="props.row.module || '-'"
          >
            {{ props.row.module || '-' }}
          </td>

          <td
            key="description"
            :title="props.row.description || '-'"
            class="text-ellipsis"
          >
            {{ props.row.description || '-' }}
          </td>

          <td key="user_list">
            <div
              :title="props.row.user_list.map(v => v.user_name_cn).join('，')"
              class="text-ellipsis"
            >
              <span
                v-for="(item, index) in props.row.user_list"
                :key="index"
                @click.stop=""
              >
                <router-link
                  :to="`/user/list/${item.id}`"
                  class="link q-mr-xs"
                  v-if="item.id"
                >
                  {{ item.user_name_cn }}
                </router-link>
                <span v-else>{{ item.user_name_cn }}</span>
              </span>
            </div>
          </td>

          <td
            key="createTime"
            class="text-ellipsis"
            :title="props.row.createTime || '-'"
          >
            {{ props.row.createTime || '-' }}
          </td>

          <td
            key="status"
            class="text-ellipsis"
            :title="props.row.status | filterStatus"
          >
            {{ props.row.status | filterStatus }}
          </td>

          <td key="executor" @click.stop="" class="text-ellipsis">
            <router-link
              v-if="props.row.executor_id"
              :to="`/user/list/${props.row.executor_id}`"
              class="link"
              :title="props.row.executor_name_cn"
            >
              {{ props.row.executor_name_cn }}
            </router-link>
            <span v-else :title="props.row.executor_name_cn">{{
              props.row.executor_name_cn
            }}</span>
          </td>

          <td
            key="executeTime"
            class="text-ellipsis"
            :title="props.row.createTime || '-'"
          >
            {{ props.row.executeTime || '-' }}
          </td>

          <td key="btn">
            <fdev-btn
              flat
              :label="btnLabel"
              @click.stop="updateTodoStatus(props.row.id)"
            />
          </td>
        </tr>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';
import Loading from '@/components/Loading';
import { createTodoColumns } from '@/modules/User/utils/model';
export default {
  name: 'Todo',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {},
      tableName: { todo: '待办', done: '已完成' },
      columns: createTodoColumns(),
      eventNum: {
        todos: 0,
        done: 0
      }
    };
  },
  props: ['label', 'name'],
  computed: {
    ...mapState('userForm', ['todosList']),
    ...mapState('userActionSaveHomePage/myTodoPage', ['terms', 'currentPage']),
    ...mapGetters('userActionSaveHomePage/myTodoPage', ['searchValue']),
    btnLabel() {
      return this.label === 'todo' ? 'done' : 'todo';
    }
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentPage(val);
      },
      deep: true
    }
  },
  filters: {
    filterStatus(val) {
      return val === '0' ? '未操作' : '已操作';
    }
  },
  methods: {
    ...mapActions('userForm', ['queryTodos', 'updateLabelById']),
    ...mapMutations('userActionSaveHomePage/myTodoPage', [
      'saveTerms',
      'saveCurrentPage'
    ]),
    async init() {
      this.loading = true;
      await this.queryTodos(this.label);
      this.tableData = this.todosList.eventList;
      const num = this.tableData.length;
      this.eventNum.todos =
        this.label === 'todo' ? num : this.todosList.total - num;
      this.eventNum.done =
        this.label === 'done' ? num : this.todosList.total - num;
      this.$emit('input', this.eventNum);
      this.loading = false;
    },
    filter(rows, terms, cols, getCellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          return cols.some(col => {
            if (Array.isArray(getCellValue(col, row))) {
              return getCellValue(col, row).some(item => {
                return item.user_name_cn.toLowerCase().indexOf(term) > -1;
              });
            } else {
              return (
                (getCellValue(col, row) + '').toLowerCase().indexOf(term) > -1
              );
            }
          });
        });
      });
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    async updateTodoStatus(id) {
      await this.updateLabelById(id);
      this.init();
    },
    /* 打开外部连接 */
    linkTo(link) {
      window.open(link);
    }
  },
  created() {
    this.pagination = this.currentPage;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.todos-table
  position relative
  top 0
.td-descName
  color: #2196f3
.td-desc
  max-width 180px!important
  overflow hidden!important
  text-overflow ellipsis!important
  color: #2196f3
</style>
