<template>
  <Loading :visible="loading">
    <div class="linesWrapper" v-if="appPipelineShow">
      <div class="operate">
        <fdev-btn label="+ 新增流水线" @click="addNewPipeline" />
      </div>
      <h5 class="tit">流水线列表</h5>

      <fdev-table
        key="appPipelineList"
        class="my-sticky-column-table"
        :data="pipelinesListData"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        separator="cell"
        @request="queryApplist"
        noExport
      >
        <template v-slot:body-cell-name="props">
          <fdev-td class="text-center">
            <router-link
              class="link"
              :to="`/configCI/CIDetail/${props.row.id}/pipeline`"
            >
              {{ props.row.name }}
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-desc="props">
          <fdev-td class="text-ellipsis" :title="props.row.desc">
            {{ props.row.desc }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-btns="props">
          <fdev-td class="text-center">
            <div class="icon-wrapper">
              <fdev-tooltip>
                {{ props.row.collectStatus == '0' ? '收藏' : '取消收藏' }}
              </fdev-tooltip>
              <f-icon
                v-if="props.row.collectStatus == '0'"
                name="star_border"
                class="icon-size"
                @click="updateFollow(props.row)"
              />
              <f-icon
                v-if="props.row.collectStatus == '1'"
                name="star"
                class="text-orange"
                @click="updateFollow(props.row)"
              />
            </div>
            <div class="icon-wrapper">
              <fdev-tooltip>以此新增流水线</fdev-tooltip>
              <f-icon
                class="icon-size"
                name="add"
                @click="toCopy(props.row.id, props.row.bindProject.projectId)"
              />
            </div>
            <div class="icon-wrapper" v-if="props.row.updateRight">
              <fdev-tooltip>删除</fdev-tooltip>
              <f-icon
                class="icon-size"
                name="delete"
                color="red"
                @click="delPipeline(props.row.id)"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </div>

    <div v-else>
      <selectTemp v-model="appPipelineShow"></selectTemp>
    </div>
  </Loading>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import { successNotify } from '@/utils/utils';
import Loading from '@/components/Loading';
import selectTemp from './selectTemp';
import { appPipelColumns } from '@/modules/App/utils/constants';
export default {
  name: '',
  components: {
    selectTemp,
    Loading
  },
  props: ['applicationId'],
  filters: {},
  data() {
    return {
      appPipelineShow: true,
      searchContent: '',
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      loading: false,
      columns: appPipelColumns(),
      pipelinesListData: []
    };
  },
  computed: {
    ...mapState('configCIForm', ['AppPipelineList']),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapGetters('user', ['isSpecialGroup'])
  },
  watch: {},
  methods: {
    ...mapActions('configCIForm', [
      'queryAppPipelineList',
      'deletePipeline',
      'updateFollowStatus'
    ]),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    async addNewPipeline() {
      this.appPipelineShow = false;
    },
    async updateFollow(val) {
      let stateTitle = '';
      let stateMsg = '';
      let collectStatus = '';
      if (val.collectStatus == 0) {
        stateTitle = '收藏';
        stateMsg = '收藏后可在流水线管理-我的收藏中查看';
        collectStatus = '0';
      } else if (val.collectStatus == 1) {
        stateTitle = '取消收藏';
        stateMsg = '取消收藏后将从我的收藏列表移除';
        collectStatus = '1';
      }
      this.$q
        .dialog({
          title: '是否确认' + stateTitle + '该流水线？',
          message: stateMsg,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.updateFollowStatus({
            nameId: val.nameId,
            pipelineId: val.id,
            state: collectStatus
          });
          successNotify(stateTitle + '成功');
          this.init();
        });
    },
    toCopy(id, projectId) {
      this.$router.push({
        path: `/configCI/CIManage/${id}/pipeline/pipeline`,
        query: { projectId }
      });
    },
    async delPipeline(id) {
      this.$q
        .dialog({
          title: '删除流水线',
          message: '确定删除该条流水线吗？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deletePipeline({
            pipelineId: id
          });
          successNotify('删除成功');
          this.init();
        });
    },
    queryApplist(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.init();
    },
    async init() {
      this.loading = true;
      let params = {
        applicationId: this.applicationId,
        pageNum: this.pagination.page,
        pageSize: this.pagination.rowsPerPage,
        searchContent: this.searchContent
      };
      try {
        await this.queryAppPipelineList(params);
        this.pipelinesListData = this.AppPipelineList.pipelineList;
        this.pagination.rowsNumber = this.AppPipelineList.total;
      } finally {
        this.loading = false;
      }
    }
  },
  async created() {
    this.loading = true;
    await this.init();
    await this.fetchCurrent();
    // 管理员权限
    // let managerUser = this.user.role.find(item => {
    //   return item.label === '环境配置管理员';
    // });
    this.loading = false;
  }
};
</script>
<style lang="stylus" scoped>
// 样式调整
.text-orange {
    color: #ff9800 !important;
    font-size: 1.5em;
}

.icon-size {
    font-size: 1.3em;
}

.icon-wrapper {
  display: inline-block;
  margin: 0 8px;
}

.linesWrapper {
  position:relative;
  margin-top: -28px;
}

.operate {
  display: flex;
  justify-content: space-between;
}

.addNewPipelines {
  position:absolute;
  left:0;
  top:35px;
  width:162px;
  height: 36px;
}

.tit {
  font-size: 14px;
  color: #001629;
  line-height: 20px;
  margin: 95px 0 20px;
}
</style>
