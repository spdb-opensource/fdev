<template>
  <!-- 任务的id是跳转的参数 -->
  <!-- 任务的unitno是新建任务需要的参数（研发单元的编号） -->
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      :data="tableData"
      :columns="columns"
      title="生产问题列表"
      titleIcon="list_s_f"
      row-key="id"
      :filter-method="filterMethod"
      :pagination.sync="pagination"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
      no-export
    >
      <template v-slot:top-right>
        <fdev-btn
          label="新建生产问题"
          :to="`/job/list/addProductionProblem/${id}`"
        />
      </template>
      <!-- 问题现象 -->
      <template v-slot:body-cell-problem_phenomenon="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="{ path: `/dashboard/productionProblems/${props.row.id}` }"
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
              class="text-red"
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
  </Loading>
</template>
<script>
import { createProdProblemsColumns } from '@/modules/Job/utils/constants';
import Loading from '@/components/Loading';
export default {
  name: 'productionProblems',
  components: { Loading },
  props: {
    unitNo: {
      type: String,
      default: ''
    },
    taskId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      pagination: {},
      tableData: [],
      columns: createProdProblemsColumns()
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {}
  }
};
</script>
<style lang="stylus" scoped></style>
