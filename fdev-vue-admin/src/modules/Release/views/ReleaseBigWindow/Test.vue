<template>
  <Loading :visible="loading['releaseForm/queryRqrmntInfoListByType']">
    <div>
      <div class="group-fixed shadow-1 btn-wrapper bg-white q-my-sm q-pa-sm">
        <fdev-btn
          ficon="download"
          flat
          :disable="tableData.length === 0"
          @click="exportExcel()"
        >
          <fdev-tooltip>导出提测重点列表</fdev-tooltip>
        </fdev-btn>
      </div>
    </div>
    <fdev-table
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      title="提测重点列表"
      noExport
    >
      <template v-slot:body-cell-rqrmnt_no="props">
        <fdev-td class="text-ellipsis">
          <span
            v-if="props.value"
            class="text-blue link cursor-pointer"
            @click="demandDetails(props.row.id)"
          >
            {{ props.value }}
          </span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-rqrmnt_spdb_manager="props">
        <fdev-td
          :title="props.value.map(v => v.user_name_cn).join(',')"
          class="text-ellipsis"
        >
          <div
            class="inline-block"
            v-for="(item, index) in props.value"
            :key="index"
          >
            <router-link
              v-if="item.id"
              :to="{ path: `/user/list/${item.id}` }"
              class="link"
              :title="item.user_name_cn"
            >
              {{ item.user_name_cn }}
            </router-link>

            <span v-else :title="item.user_name_en">{{
              item.user_name_en || '-'
            }}</span>
            &nbsp;
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog title="详情" right v-model="demandDetailsOpen">
      <fdev-table
        titleIcon="list_s_f"
        :data="demandDetailsColumns"
        :columns="detailsColumns"
        ref="table"
        class="table-max-width"
        title="需求详情列表"
      >
        <template v-slot:body-cell-task_name="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :title="props.value"
              :to="`/job/list/${props.row.task_id}`"
              class="link"
            >
              {{ props.value }}
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-master="props">
          <fdev-td
            :title="props.value.map(v => v.user_name_cn).join(',')"
            class="text-ellipsis"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                :title="item.user_name_cn"
                v-if="item.user_name_cn"
                :to="`/user/list/${item.id}`"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
              &nbsp;
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-project_name="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :title="props.value"
              :to="`/app/list/${props.row.project_id}`"
              class="link"
            >
              {{ props.value }}
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-specialCase="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            {{ props.value || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-other_system="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            {{ props.value || '-' }}
          </fdev-td>
        </template>
      </fdev-table>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import {
  getBigReleaseTestPagination,
  setBigReleaseTestPagination
} from '../../utils/setting';
import { watchRouteParams } from '../../utils/mixin';
import { baseUrl } from '@/utils/utils';
import LocalStorage from '#/plugins/LocalStorage';
import { deepClone } from '@/utils/utils';
import {
  demandDetailListColumns,
  testListColumns
} from '../../utils/constants';

export default {
  name: 'Test',
  mixins: [watchRouteParams],
  components: { Loading },
  data() {
    return {
      demandDetailsColumns: [],
      demandDetailsOpen: false,
      isIncludeChildren: true, //是否包含子组
      groupsData: [],
      open: false,
      release_date: '',
      type: '1',
      tableData: [],
      detailsColumns: demandDetailListColumns,
      columns: testListColumns,
      pagination: getBigReleaseTestPagination()
    };
  },
  computed: {
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('releaseForm', [
      'testInfoList',
      'queryRqrmntInfo',
      'selectedGourpList'
    ]),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    },
    importLoading() {
      if (this.fileType === '1') return 'exportSpecialRqrmntInfoList';
      return 'exportRqrmntInfoListByType';
    }
  },
  watch: {
    pagination(val) {
      setBigReleaseTestPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    nodes(val) {
      this.groupsData = deepClone(val);
    },
    testInfoList() {
      this.tableData = this.testInfoList;
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'queryRqrmntInfoListByType',
      'exportRqrmntInfoListByType',
      'exportSpecialRqrmntInfoList',
      'queryRqrmntInfoTasks'
    ]),
    appendNode(parent, set, depth = 2) {
      if (!Array.isArray(parent) || !Array.isArray(set)) {
        return [];
      }
      if (parent.length === 0 || set.length === 0) {
        return [];
      }
      if (depth === 0) {
        return parent;
      }
      const child = parent.reduce((pre, next) => {
        const nodes = set.filter(group => group.parent === next.id);
        nodes.forEach(node => (node.header = 'nodes'));

        next.children = nodes;
        return pre.concat(nodes);
      }, []);

      if (child.length > 0) {
        this.appendNode(child, set, --depth);
      }
      return parent;
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: false,
          children: this.addAttribute(item.children)
        };
      });
    },
    exportExcel(ids) {
      this.exportRqrmntInfoListByType({
        groupIds: this.selectedGourpList,
        release_date: this.release_date,
        type: this.type,
        isParent: this.isIncludeChildren
      });
    },
    async demandDetails(rqrmntId) {
      this.demandDetailsOpen = true;
      await this.queryRqrmntInfoTasks({ id: rqrmntId });
      this.demandDetailsColumns = this.queryRqrmntInfo;
    },
    routeTo(id) {
      let Authorization = LocalStorage.getItem('fdev-vue-admin-jwt');
      if (process.env.NODE_ENV === 'production') {
        window.open(
          `
          ${baseUrl}/rqrmnt/#/requirement/profile/${id}?Authorization=${Authorization}`,
          '_blank'
        );
      }
    },
    async init() {
      await this.queryRqrmntInfoListByType({
        groupIds: this.selectedGourpList,
        release_date: this.release_date,
        type: this.type,
        isParent: this.isIncludeChildren
      });
      this.tableData = this.testInfoList;
    }
  },
  async created() {
    this.release_date = this.$route.params.release_date;
    if (this.selectedGourpList && this.selectedGourpList.length > 0) {
      this.init();
    }
  }
};
</script>
<style lang="stylus" scoped>
.table-max-width
  max-width 1000px
</style>
