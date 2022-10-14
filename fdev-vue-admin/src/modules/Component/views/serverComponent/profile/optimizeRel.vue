<template>
  <Loading>
    <fdev-table
      class="my-sticky-column-table"
      title="投产窗口列表"
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      ref="table"
      :visible-columns="visibleColumns"
      no-export
      no-select-cols
    >
      <template v-slot:top-right>
        <fdev-btn
          normal
          ficon="add"
          label="新增投产窗口"
          @click="OptimizeDialogOpen = true"
          v-if="isManger"
        />
      </template>

      <!-- 版本管理员列 -->
      <template v-slot:body-cell-manager="props">
        <fdev-td>
          <div
            class="q-gutter-x-sm text-ellipsis"
            :title="jointNameCn(props.row.manager)"
          >
            <router-link
              :to="`/user/list/${user.id}`"
              class="link"
              v-for="user in props.row.manager"
              :key="user.id"
            >
              {{ user.user_name_cn }}
            </router-link>
          </div>
        </fdev-td>
      </template>

      <!-- 当前优化需求类型 -->
      <template v-slot:body-cell-issue_type="props">
        <fdev-td>
          <span :title="issueTypeDict[props.row.issue_type]">
            {{ issueTypeDict[props.row.issue_type] }}
          </span>
        </fdev-td>
      </template>

      <!-- 操作列 -->
      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <fdev-btn label="处理" @click="linkTo(props)" flat />
            <fdev-btn
              label="信息维护"
              @click="updateRelease(props.row)"
              flat
              v-if="isComManager"
            />
            <fdev-btn
              label="废弃"
              @click="destroyRelease(props.row)"
              flat
              v-if="isComManager"
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
    <RelUpdateDialog v-model="dialogOpen" :data="updateData" @input="init" />
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import OptimizeDialog from '@/modules/Component/views/webComponent/components/addRel.vue';
import RelUpdateDialog from '@/modules/Component/views/webComponent/components/relUpdate.vue';
import {
  issueTypeDict,
  serverComponentProfileOptimizeRelColums
} from '@/modules/Component/utils/constants.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';
import { successNotify } from '@/utils/utils';

export default {
  name: 'WebOptimize',
  components: { Loading, OptimizeDialog, RelUpdateDialog },
  data() {
    return {
      OptimizeDialogOpen: false,
      tableData: [],
      issueTypeDict,
      columns: serverComponentProfileOptimizeRelColums,
      dialogOpen: false,
      updateData: {}
    };
  },
  computed: {
    ...mapState(
      'userActionSaveComponent/componentManage/componentList/OptimizeRel',
      ['visibleColumns']
    ),
    ...mapState('componentForm', ['mpassReleaseIssue', 'componentDetail']),
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
    //是否为组件管理员
    isComManager() {
      return this.componentDetail.manager_id
        ? this.componentDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
    },
    columnsOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/OptimizeRel',
      ['updatevisibleColumns']
    ),
    ...mapActions('componentForm', [
      'destroyIssueServer',
      'queryMpassReleaseIssue'
    ]),
    async init() {
      await this.queryMpassReleaseIssue({
        component_id: this.$route.params.id
      });
      this.tableData = this.mpassReleaseIssue;
    },
    linkTo(props) {
      if (this.$route.path.includes('web')) {
        this.$router.push({
          path: `/componentManage/web/webIssuePage/${props.row.id}`,
          query: {
            component_id: this.$route.params.id,
            branch: props.row.feature_branch
          }
        });
      } else {
        this.$router.push({
          path: `/componentManage/server/issuePage/${props.row.id}`,
          query: {
            component_id: this.$route.params.id,
            branch: props.row.feature_branch
          }
        });
      }
    },
    updateRelease(data) {
      this.dialogOpen = true;
      this.updateData = data;
    },
    destroyRelease(data) {
      return this.$q
        .dialog({
          title: '废弃提示',
          message: '确定废弃该投产窗口吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.destroyIssueServer({
            id: data.id,
            type: 'mpass_release'
          });
          successNotify('废弃成功');
          this.init();
        });
    },
    jointNameCn
  },
  created() {
    this.init();
  }
};
</script>

<style></style>
<style lang="stylus" scoped>
.w150
  width 150px
.btn-width
  width 38px
.btn1-width
  width 60px
</style>
