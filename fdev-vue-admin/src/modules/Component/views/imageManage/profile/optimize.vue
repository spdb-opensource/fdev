<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      title="优化需求列表"
      :data="imageIssueList"
      :columns="columns"
      :pagination.sync="pagination"
      :visible-columns="visibleColumns"
      title-icon="list_s_f"
      :onSelectCols="updatevisibleColumns"
      no-export
      no-select-cols
    >
      <template v-slot:top-right>
        <div class="q-gutter-md">
          <fdev-tooltip v-if="!isManger">
            需要镜像管理员或基础架构管理员权限
          </fdev-tooltip>
          <fdev-btn
            :disable="!isManger"
            normal
            label="新增优化需求"
            ficon="add"
            @click="optimizeDialogOpen = true"
          />
        </div>
      </template>
      <!-- 需求阶段列 -->
      <template v-slot:body-cell-stage="props">
        <fdev-td>
          <span :title="props.row.stage | textFilters">
            {{ props.row.stage | textFilters }}
          </span>
        </fdev-td>
      </template>

      <!-- 开发人员列 -->
      <template v-slot:body-cell-name_cn="props">
        <fdev-td>
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

      <!-- 操作列 -->
      <template v-slot:body-cell-btn="props">
        <fdev-td class="text-center" auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <fdev-tooltip
              v-if="!isManger && props.row.assignee !== currentUser.id"
            >
              需要镜像管理员或基础架构管理员权限
            </fdev-tooltip>
            <fdev-btn
              :disable="!isManger && props.row.assignee !== currentUser.id"
              label="处理"
              @click="linkTo(props.row.id)"
              flat
            />
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <OptimizeDialog
      v-model="optimizeDialogOpen"
      :value="optimizeDialogOpen"
      @refresh="init"
    />
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import OptimizeDialog from '@/modules/Component/views/imageManage/components/addOptimize';
import {
  getOptimizePagination,
  setOptimizePagination
} from '@/modules/Component/utils/setting.js';
import {
  imageStage,
  imageManageProfileOptimizeColumns
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'Optimize',
  components: { Loading, OptimizeDialog },
  data() {
    return {
      loading: false,
      optimizeDialogOpen: false,
      id: '',
      stage: '0',
      columns: imageManageProfileOptimizeColumns,
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
        this.id = val.id;
        this.init();
      }
    }
  },
  computed: {
    ...mapState(
      'userActionSaveComponent/imageManageProfile/imageManageOptimize',
      ['visibleColumns']
    ),
    ...mapState('componentForm', ['imageIssueList']),
    ...mapState('componentForm', ['baseImageDetail']),

    ...mapState('user', ['currentUser']),
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );

      const isManager = this.baseImageDetail.manager
        ? this.baseImageDetail.manager.some(
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
      'userActionSaveComponent/imageManageProfile/imageManageOptimize',
      ['updatevisibleColumns']
    ),
    ...mapActions('componentForm', ['queryBaseImageDetail']),
    ...mapActions('componentForm', [
      'optimizeComponent',
      'queryBaseImageIssue'
    ]),
    ...mapActions('user', ['fetch']),
    async init() {
      this.loading = true;
      await this.queryBaseImageDetail({
        id: this.id
      });
      await this.queryBaseImageIssue({
        name: this.baseImageDetail.name
      });
      this.loading = false;
    },
    linkTo(id) {
      this.$router.push({ name: 'HandleImageManagePage', params: { id: id } });
    }
  },

  created() {
    this.id = this.$route.params.id;
    this.init();
  },

  filters: {
    textFilters(val) {
      return imageStage[val];
    }
  }
};
</script>
