<template>
  <Loading>
    <fdev-table
      :data="tableData"
      :columns="columns"
      ref="table"
      :visible-columns="visibleColumns"
      :onSelectCols="updatevisibleColumns"
      title="优化需求列表"
      title-icon="list_s_f"
      no-export
    >
      <template v-slot:top-right
        ><fdev-btn
          label="新增优化需求"
          normal
          ficon="add"
          @click="OptimizeDialogOpen = true"
          v-if="isManger"
      /></template>
      <template v-slot:body-cell-manager="props">
        <fdev-td>
          <router-link
            :to="`/user/list${user.id}`"
            class="link"
            v-for="user in props.row.manager"
            :key="user.id"
          >
            {{ user.user_name_cn }}
          </router-link>
        </fdev-td>
      </template>

      <template v-slot:body-cell-issue_type="props">
        <fdev-td>
          {{ issueTypeDict[props.row.issue_type] }}
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td class="text-center" auto-width>
          <fdev-btn label="处理" @click="linkTo(props)" flat />
        </fdev-td>
      </template>
    </fdev-table>

    <OptimizeDialog
      v-model="OptimizeDialogOpen"
      :component="mpassArchetypeDetail.name_en"
      :archetype_id="mpassArchetypeDetail.id"
      @refresh="init"
    />
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import OptimizeDialog from '@/modules/Component/views/webArchetype/components/addOptimize';
import {
  issueTypeDict,
  webArchetypeProfileOptimizeColums
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'WebOptimize',
  components: { Loading, OptimizeDialog },
  data() {
    return {
      OptimizeDialogOpen: false,
      tableData: [],
      issueTypeDict,
      columns: webArchetypeProfileOptimizeColums
    };
  },
  computed: {
    ...mapState(
      'userActionSaveComponent/archetypeManage/webArchetype/webArchetypeOptimize',
      ['visibleColumns']
    ),
    ...mapState('componentForm', [
      'mpassArchetypeIssue',
      'mpassArchetypeDetail'
    ]),
    ...mapState('user', ['currentUser']),
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.mpassArchetypeDetail.manager
        ? this.mpassArchetypeDetail.manager.some(
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
      'userActionSaveComponent/archetypeManage/webArchetype/webArchetypeOptimize',
      ['updatevisibleColumns']
    ),
    ...mapActions('componentForm', [
      'queryMpassArchetypeIssue',
      'queryMpassArchetypeDetail'
    ]),
    async init() {
      await this.queryMpassArchetypeIssue({
        archetype_id: this.$route.params.id
      });
      this.tableData = this.mpassArchetypeIssue;
      await this.queryMpassArchetypeDetail({
        id: this.$route.params.id
      });
    },
    linkTo(props) {
      this.$router.push({
        name: 'WebArchetypeHandlePage',
        params: {
          id: props.row.id,
          archetype_id: props.row.archetype_id
        }
      });
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped></style>
