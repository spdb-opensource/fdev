<template>
  <div>
    <Loading>
      <UserSelected @queryClick="init" />
      <f-block block class="q-my-md">
        <Loading :visible="tableLoading">
          <fdev-table
            :data="qrmntsData"
            :columns="columns"
            row-key="qrmntsData.id"
            :visible-columns="visibleColumns"
            :onSelectCols="updateVisibleColumns"
            :pagination.sync="pagination"
            titleIcon="list_s_f"
            title="个人优化需求列表"
            class="my-sticky-column-table"
          >
            <!-- 组件、骨架、镜像名 -->
            <template v-slot:body-cell-name="props">
              <fdev-td>
                <router-link
                  :to="`/${nameType[props.row.type]}/${props.row.id}`"
                  class="link"
                >
                  {{ props.value
                  }}<fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.value }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </router-link>
              </fdev-td>
            </template>
            <template v-slot:body-cell-type="props">
              <fdev-td>
                <span>{{ comType[props.value] }} </span>
              </fdev-td>
            </template>
            <template v-slot:body-cell-rqrmnts_admin="props">
              <fdev-td>
                <span v-for="(item, index) in props.value" :key="index">
                  <router-link
                    v-if="item.user_name_cn"
                    :to="`/user/list/${item.id}`"
                    class="link"
                  >
                    {{ item.user_name_cn }}
                  </router-link>
                </span>
              </fdev-td>
            </template>
            <template v-slot:body-cell-develop="props">
              <fdev-td>
                <router-link
                  :to="`/user/list/${props.row.develop.id}`"
                  class="link"
                >
                  {{ props.value }}
                </router-link>
              </fdev-td>
            </template>
          </fdev-table>
        </Loading></f-block
      >
    </Loading>
  </div>
</template>

<script>
import UserSelected from '@/modules/Dashboard/components/UserSelected';
import {
  setOptPagination,
  getOptPagination
} from '@/modules/Dashboard/utils/setting';
import { nameType, comType } from '@/modules/Dashboard/utils/constants';

import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
export default {
  name: 'UserOptimize',
  components: { Loading, UserSelected },
  data() {
    return {
      pagination: getOptPagination(),
      qrmntsData: [],
      tableLoading: false,
      nameType,
      comType,
      columns: [
        {
          name: 'name',
          label: '组件、骨架、镜像名',
          field: 'name',
          align: 'left',
          copy: true
        },
        {
          name: 'type',
          label: '类型',
          field: 'type',
          align: 'left'
        },
        {
          name: 'rqrmnts_admin',
          label: '管理员',
          field: 'rqrmnts_admin',
          align: 'left'
        },
        {
          name: 'rqrmnts_name',
          label: '需求名',
          field: 'rqrmnts_name',
          align: 'left',
          copy: true
        },
        {
          name: 'develop',
          label: '开发人员',
          field: row => row.develop.user_name_cn,
          align: 'left'
        },
        {
          name: 'feature_branch',
          label: '开发分支',
          field: 'feature_branch',
          align: 'left',
          copy: true
        },
        {
          name: 'recommond_version',
          label: '目标版本',
          field: 'recommond_version',
          align: 'left'
        },
        {
          name: 'predict_version',
          label: '预设版本',
          field: 'predict_version',
          align: 'left'
        },
        {
          name: 'stage',
          label: '当前阶段',
          field: 'stage',
          align: 'left'
        },
        {
          name: 'desc',
          label: '需求描述',
          field: 'desc',
          align: 'left',
          copy: true
        },
        {
          name: 'due_date',
          label: '计划完成日期',
          field: 'due_date',
          align: 'left'
        }
      ],
      searchObj: {}
    };
  },
  watch: {
    pagination(val) {
      setOptPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('userActionSaveDashboard/basicFrame/userOptimize', [
      'visibleColumns'
    ]),
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('dashboard', ['userOptimizeList'])
  },
  methods: {
    ...mapMutations('userActionSaveDashboard/basicFrame/userOptimize', [
      'updateVisibleColumns'
    ]),
    ...mapActions('dashboard', ['queryQrmntsData', 'queryIssueData']),
    async init(userList, roles, selectedCompanyList, selectedList) {
      const param = {
        ids: userList,
        roles: roles,
        companyList: selectedCompanyList,
        groupList: selectedList
      };
      this.searchObj = param;
      userList = userList || [];
      let id = userList.map(user => {
        return user.id;
      });
      this.tableLoading = true;
      await this.queryQrmntsData({ user_ids: id });
      this.qrmntsData = this.userOptimizeList;
      this.tableLoading = false;
    }
  },

  mounted() {
    let item = JSON.parse(sessionStorage.getItem('userOptimizeObj'));
    if (item && item.ids && item.ids.length > 0) {
      this.init(item.ids, item.roles, item.companyList, item.groupList);
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('userOptimizeObj');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('userOptimizeObj', JSON.stringify(this.searchObj));
    next();
  }
};
</script>

<style lang="stylus" scoped>
>>>.q-table td
    max-width 100% !important
</style>
