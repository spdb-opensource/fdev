<template>
  <f-block>
    <Loading :visible="loading['dashboard/queryMergeInfo']">
      <!-- 表格 -->
      <fdev-table
        class="my-sticky-column-table"
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        :onSearch="filterData"
        @request="init"
        row-key="name"
        title="合并请求列表"
        titleIcon="list_s_f"
        noExport
      >
        <!-- 筛选条件 -->
        <template v-slot:top-bottom>
          <div class="row">
            <!-- 应用所属小组 -->
            <f-formitem
              class="col-6 q-pr-md"
              bottom-page
              label="应用所属小组"
              label-style="width:100px"
            >
              <fdev-select
                use-input
                option-label="name"
                option-value="id"
                map-options
                emit-value
                @filter="filterGroup"
                :options="groupOptions"
                :value="filterModel.group_id"
                @input="filterModelGroupId($event)"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name">{{
                        scope.opt.name
                      }}</fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.fullName">
                        {{ scope.opt.fullName }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 用户 -->
            <f-formitem
              class="col-6 q-pr-md"
              label-style="width:100px"
              bottom-page
              label="用户"
            >
              <fdev-select
                use-input
                @filter="filterUser"
                :options="userOptions"
                option-label="user_name_cn"
                option-value="git_user_id"
                map-options
                emit-value
                :value="filterModel.git_user_id"
                @input="filterModelGitUserId($event)"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.user_name_cn">
                        {{ scope.opt.user_name_cn }}
                      </fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.user_name_en">
                        {{ scope.opt.user_name_en }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </div>
          <div class="row">
            <!-- 开始时间 -->
            <f-formitem
              class="col-6 q-pr-md"
              label-style="width:100px"
              bottom-page
              label="开始时间"
            >
              <f-date
                :value="filterModel.start_time"
                :options="startOptions"
                @input="filterModelStartTime($event)"
                mask="YYYY/MM/DD"
              />
            </f-formitem>
            <!-- 结束时间 -->
            <f-formitem
              class="col-6 q-pr-md"
              label-style="width:100px"
              bottom-page
              label="结束时间"
            >
              <f-date
                :value="filterModel.end_time"
                :options="endOptions"
                @input="filterModelEndTime($event)"
                mask="YYYY/MM/DD"
              />
            </f-formitem>
          </div>
        </template>
        <!-- 目标分支 -->
        <template v-slot:body-cell-targetBranch="props">
          <fdev-td class="ellipsis" :title="props.value">
            {{ props.value }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <!-- 源分支 -->
        <template v-slot:body-cell-sourceBranch="props">
          <fdev-td class="ellipsis" :title="props.value">
            {{ props.value }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <!-- 应用名 -->
        <template v-slot:body-cell-appName="props">
          <fdev-td>
            <router-link :to="`/app/list/${props.row.appId}`" class="link">
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>
        <!-- 创建人 -->
        <template v-slot:body-cell-creator="props">
          <fdev-td>
            <router-link
              v-if="props.value.id"
              :to="`/user/list/${props.value.id}`"
              class="link"
            >
              {{ props.value.user_name_cn }}
            </router-link>
            <span v-else>{{ props.value.gitlab_id }}</span>
          </fdev-td>
        </template>
        <!-- 处理人 -->
        <template v-slot:body-cell-handler="props">
          <fdev-td>
            <router-link
              v-if="props.value.id"
              :to="`/user/list/${props.value.id}`"
              class="link"
            >
              {{ props.value.user_name_cn }}
            </router-link>

            <span v-else>{{ props.value.gitlab_id }}</span>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import { mergeRequestModel } from '@/modules/Dashboard/utils/constants';
import {
  getMergeRequestPagination,
  setMergeRequestPagination
} from '@/modules/Dashboard/utils/setting';
export default {
  name: 'MergeRequest',
  components: { Loading },
  data() {
    return {
      listModel: mergeRequestModel(),

      userOptions: [],
      groupList: [],
      groupOptions: [],
      tableData: [],
      columns: [
        {
          name: 'targetBranch',
          label: '目标分支',
          field: 'targetBranch',
          align: 'left',
          copy: true
        },
        {
          name: 'sourceBranch',
          label: '源分支',
          field: 'sourceBranch',
          align: 'left',
          copy: true
        },
        {
          name: 'appName',
          label: '应用名',
          field: 'appName',
          align: 'left',
          copy: true
        },
        {
          name: 'fullName',
          label: '应用所属小组',
          field: 'fullName',
          align: 'left',
          copy: true
        },
        {
          name: 'creator',
          label: '创建人',
          field: 'creator',
          align: 'left'
        },
        {
          name: 'handler',
          label: '处理人',
          field: 'handler',
          align: 'left'
        },
        {
          name: 'createTime',
          label: '创建时间',
          field: 'createTime',
          align: 'left'
        },
        {
          name: 'handleTime',
          label: '处理时间',
          field: 'handleTime',
          align: 'left'
        }
      ],
      pagination: {
        rowsPerPage: getMergeRequestPagination(),
        rowsNumber: 0,
        page: 1
      }
    };
  },
  watch: {
    pagination: {
      deep: true,
      handler(val) {
        setMergeRequestPagination(val.rowsPerPage);
      }
    }
  },
  computed: {
    ...mapState('userActionSaveDashboard/codeStatistic/mergeRequest', {
      filterModel: 'mergeRequestModel'
    }),
    ...mapState('global', ['loading']),
    ...mapState('dashboard', ['mergeInfo']),
    ...mapState('user', ['list']),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    userList() {
      return this.list.filter(user => !!user.git_user_id);
    }
  },
  methods: {
    ...mapMutations('userActionSaveDashboard/codeStatistic/mergeRequest', [
      'filterModelGroupId',
      'filterModelGitUserId',
      'filterModelStartTime',
      'filterModelEndTime'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryMergeInfo']),
    ...mapActions('user', ['fetch']),
    filterData() {
      const { git_user_id } = this.filterModel;
      this.listModel = {
        ...this.filterModel,
        git_user_id: git_user_id ? Number(git_user_id) : ''
      };
      this.pagination.page = 1;
      this.init();
    },
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    filterGroup(val, update, abort) {
      update(() => {
        this.groupOptions = this.groups.filter(
          tag => tag.fullName.indexOf(val) > -1
        );
      });
    },
    endOptions(date) {
      this.filterModel.start_time = this.filterModel.start_time
        ? this.filterModel.start_time.split(' ')[0]
        : '';
      return date >= this.filterModel.start_time;
    },
    startOptions(date) {
      if (this.filterModel.end_time) {
        return date <= this.filterModel.end_time.split(' ')[0];
      }
      return true;
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      let params = { ...this.filterModel };
      params.git_user_id = parseInt(params.git_user_id);
      await this.queryMergeInfo({
        ...params,
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage
      });
      this.pagination.rowsNumber = this.mergeInfo.total;
      this.tableData = this.mergeInfo.mergedInfo;
    }
  },
  async created() {
    this.init();
    this.fetch().then(res => {
      this.userOptions = this.userList;
    });
    this.fetchGroup().then(res => {
      this.groupOptions = this.groups;
    });
  }
};
</script>

<style lang="stylus" scoped>
.ellipsis
  max-width 250px
  overflow hidden
  text-overflow ellipsis
</style>
