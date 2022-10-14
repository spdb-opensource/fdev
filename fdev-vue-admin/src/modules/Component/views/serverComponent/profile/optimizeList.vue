<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      title="优化需求列表"
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      ref="table"
      :visible-columns="visibleColumns"
      no-export
      no-select-cols
    >
      <template v-slot:top-right>
        <div class="q-gutter-md">
          <fdev-btn
            normal
            ficon="add"
            label="新增优化需求"
            @click="OptimizeDialogOpen = true"
            v-if="isManger && componentDetail.type !== '1'"
          />
        </div>
      </template>

      <!-- 开发人员列 -->
      <template v-slot:body-cell-name_cn="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/user/list/${props.row.assignee}`"
            class="link"
            v-if="props.row.assignee"
          >
            <span :title="props.value">
              {{ props.value }}
            </span>
          </router-link>
          <span v-else :title="props.value">{{ props.value }}</span>
        </fdev-td>
      </template>

      <!-- 开发分支列 -->
      <template v-slot:body-cell-feature_branch="props">
        <fdev-td class="text-ellipsis">
          <span :title="props.row.feature_branch || '-'">
            {{ props.row.feature_branch || '-' }}
          </span>
        </fdev-td>
      </template>

      <!-- 需求阶段列 -->
      <template v-slot:body-cell-stage="props">
        <fdev-td class="text-ellipsis">
          <span :title="props.row.stage | textFilters">
            {{ props.row.stage | textFilters }}
          </span>
        </fdev-td>
      </template>

      <!-- 操作列 -->
      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <div class="q-gutter-x-sm row no-wrap">
            <fdev-btn
              label="处理"
              flat
              @click="linkTo(props.row.id)"
              v-if="isManger || props.row.assignee === currentUser.id"
            />
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <OptimizeDialog
      v-model="OptimizeDialogOpen"
      :component="componentDetail.name_en"
      :component_id="componentDetail.id"
      @refresh="init"
    />
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import OptimizeDialog from '@/modules/Component/views/serverComponent/components/addOptimize.vue';
import {
  getOptimizePagination,
  setOptimizePagination
} from '@/modules/Component/utils/setting.js';
import {
  stage,
  serverComponentProfileOptimizeListColums
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'Optimize',
  components: { Loading, OptimizeDialog },
  data() {
    return {
      loading: false,
      OptimizeDialogOpen: false,
      component_id: '',
      tableData: [],
      columns: serverComponentProfileOptimizeListColums,
      pagination: getOptimizePagination()
    };
  },
  watch: {
    pagination(val) {
      setOptimizePagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    '$route.params': {
      deep: true,
      handler(val) {
        this.component_id = val.id;
        this.init();
      }
    }
  },
  computed: {
    ...mapState(
      'userActionSaveComponent/componentManage/componentList/ArcheOptimize',
      ['visibleColumns']
    ),
    ...mapState('componentForm', ['optimizeList', 'componentDetail']),
    ...mapState('componentForm', ['history', 'componentDetail']),
    ...mapState('user', ['currentUser']),
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.componentDetail.manager_id
        ? this.componentDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    columnsOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/ArcheOptimize',
      ['updatevisibleColumns']
    ),
    ...mapActions('componentForm', [
      'optimizeComponent',
      'queryComponentIssues'
    ]),
    async init() {
      this.loading = true;
      await this.queryComponentIssues({
        component_id: this.component_id
      });
      this.tableData = this.optimizeList;
      this.loading = false;
    },
    linkTo(id) {
      this.$router.push({ name: 'HandlePage', params: { id: id } });
    }
  },

  created() {
    this.component_id = this.$route.params.id;
    this.init();
  },
  filters: {
    textFilters(val) {
      return stage[val];
    }
  }
};
</script>
