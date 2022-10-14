<template>
  <Loading :visible="loading">
    <f-block>
      <fdev-table
        class="my-sticky-column-table"
        :data="tableData"
        :columns="columns"
        title="生产问题列表"
        titleIcon="list_s_f"
        row-key="id"
        :filter="searchValue"
        :filter-method="filterMethod"
        :pagination.sync="pagination"
        :visible-columns="visibleCols"
        :on-select-cols="saveVisibleColumns"
        no-export
      >
        <template v-slot:top-right>
          <fdev-btn
            v-if="taskId"
            label="新建生产问题"
            normal
            ficon="add"
            :to="`/job/list/addProductionProblem/${taskId}`"
          />
        </template>
        <template v-if="!isTask" v-slot:top-bottom>
          <f-formitem label="搜索条件">
            <fdev-select
              :value="termsApp"
              @input="saveTermsApp($event)"
              multiple
              use-input
              placeholder="输入关键字，回车区分"
              hide-dropdown-icon
              ref="select"
              @new-value="addSelect"
              class="table-head-input"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="text-primary cursor-pointer"
                  @click="setSelect($refs.select)"
                />
              </template>
            </fdev-select>
          </f-formitem>
        </template>

        <!-- 问题现象 -->
        <template v-slot:body-cell-problem_phenomenon="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :to="{ path: `/dashboard/productionProblems/${props.row.id}` }"
              class="link"
            >
              <span :title="props.value">{{ props.value }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <!-- 应急过程 -->
        <template v-slot:body-cell-emergency_process="props">
          <fdev-td class="text-ellipsis">
            <span :title="props.value">{{ props.value }}</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <!-- 问题原因 -->
        <template v-slot:body-cell-issue_reason="props">
          <fdev-td class="text-ellipsis">
            <span :title="props.value">{{ props.value }}</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <!-- 影响范围 -->
        <template v-slot:body-cell-influence_area="props">
          <fdev-td class="text-ellipsis">
            <span :title="props.value">{{ props.value }}</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <!-- 操作 -->
        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <div class="q-gutter-x-sm row no-wrap">
              <fdev-btn
                label="修改"
                flat
                :to="`/job/list/${props.row.id}/modifyProductionProblem`"
              />
              <fdev-btn
                label="删除"
                flat
                @click="deleteProProblem(props.row.id)"
              />
              <fdev-btn
                label="创建任务"
                flat
                @click="
                  $router.push({
                    path: '/job/add',
                    query: {
                      name: props.row.requirement_name,
                      unitNo: unitNo,
                      taskNo: props.row.task_no
                    }
                  })
                "
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </f-block>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';
import { createProdProblemsColumns } from '../../utils/constants';
import { setProductionPagination } from '../../utils/setting';
import Loading from '@/components/Loading';
import { successNotify } from '@/utils/utils';

export default {
  name: 'myProdProblem',
  components: { Loading },
  data() {
    return {
      loading: false,
      pagination: {},
      tableData: [],
      columns: createProdProblemsColumns()
    };
  },
  props: {
    isTask: {
      type: Boolean,
      default: false
    },
    unitNo: {
      type: String,
      default: ''
    },
    taskNo: {
      type: String,
      default: ''
    },
    taskId: {
      type: String,
      default: ''
    }
  },
  watch: {
    pagination(val) {
      setProductionPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('dashboard', ['userProIssues', 'taskProIssues']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    },
    ...mapState('userActionSaveHomePage/myProduct', [
      'termsApp',
      'visibleCols',
      'currentPage'
    ]),
    ...mapGetters('userActionSaveHomePage/myProduct', ['searchValue'])
  },
  methods: {
    ...mapActions('dashboard', ['queryUserProIssues', 'queryTaskProIssues']),
    ...mapActions('jobForm', ['deleteProIssue']),
    ...mapMutations('userActionSaveHomePage/myProduct', [
      'saveTermsApp',
      'saveVisibleColumns',
      'saveCurrentPage'
    ]),
    addSelect(val, done) {
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
    filterMethod(rows, terms, cols, getCellValue) {
      let lowerTerms = this.termsApp.map(item => item.toLowerCase());
      return rows.filter(row => {
        return lowerTerms.every(term => {
          return cols.some(col => {
            return (getCellValue(col, row) + '')
              .toLowerCase()
              .includes(term.trim());
          });
        });
      });
    },
    deleteProProblem(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '确认删除此条生产问题总结？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteProIssue({
            id: id
          });
          successNotify('删除成功！');
          this.init();
        });
    },
    async init() {
      this.loading = true;
      if (this.isTask) {
        await this.queryTaskProIssues({
          task_id: this.$route.params.id
        });
        this.tableData = this.taskProIssues;
      } else {
        await this.queryUserProIssues({
          user_name_en: this.currentUser.user_name_en
        });
        this.tableData = this.userProIssues;
      }
      this.loading = false;
    }
  },
  created() {
    this.init();
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'problem_phenomenon',
        'requirement_name',
        'occurred_time',
        'issue_type',
        'is_trigger_issue',
        'issue_level',
        'deal_status',
        'btn'
      ]);
    }
  }
};
</script>

<style lang="stylus" scoped>
.text-ellipsis
  text-overflow: ellipsis;
  overflow hidden
  max-width 200px
</style>
