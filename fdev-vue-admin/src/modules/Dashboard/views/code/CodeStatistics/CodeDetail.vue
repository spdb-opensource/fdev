<template>
  <f-block>
    <!-- 表格数据 -->
    <Loading :visible="loading['dashboard/queryGitlabCommitDetail']">
      <fdev-table
        :data="detailData"
        :columns="columns"
        title="代码统计详情"
        row-key="job.id"
        titleIcon="list_s_f"
        :pagination.sync="pagination"
      >
        <!-- 文件差异链接 -->
        <template v-slot:body-cell-fileDiffUrl="props">
          <fdev-td @click="openUrl(props.row)" class="link-style">
            查看差异文件详情
          </fdev-td>
        </template>
        <!-- 变更文件列表 -->
        <template v-slot:body-cell-fileNameList="props">
          <fdev-td
            v-if="props.row.fileNameList.length > 0"
            @click="openList(props.row.fileNameList)"
            class="link-style"
          >
            查看文件变更列表
          </fdev-td>
          <fdev-td v-else>
            -
          </fdev-td>
        </template>
      </fdev-table>
      <div class="text-center">
        <fdev-btn label="返回" @click="goBack" />
      </div>
    </Loading>
    <!-- 查看变更列表弹窗 -->
    <f-dialog v-model="fileListOpen" title="文件变更列表">
      <div class="scroll-thin pathHeight">
        <p class="font">文件路径</p>
        <fdev-list boardered separator class="q-mb-md">
          <fdev-item v-ripple v-for="(val, index) in fileList" :key="index.id">
            <fdev-item-section>
              {{ val }}
            </fdev-item-section>
          </fdev-item>
        </fdev-list>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="知道了" dialog @click="fileListOpen = false" />
      </template>
    </f-dialog>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import {
  seDetailPagination,
  getDetailPagination
} from '@/modules/Dashboard/utils/setting';

export default {
  name: 'CodeDetail',
  components: { Loading },
  data() {
    return {
      detailData: [],
      pagination: getDetailPagination(),
      columns: [
        {
          name: 'nickName',
          label: '提交人',
          field: 'nickName',
          align: 'left'
        },
        {
          name: 'projectName',
          label: '项目名',
          field: 'projectName',
          align: 'left',
          copy: true
        },
        {
          name: 'total',
          label: '影响总行数',
          field: row => row.status.total,
          align: 'left',
          sortable: true
        },
        {
          name: 'additions',
          label: '添加行数',
          field: row => row.status.additions,
          align: 'left',
          sortable: true
        },
        {
          name: 'deletions',
          label: '删除行数',
          field: row => row.status.deletions,
          align: 'left',
          sortable: true
        },
        {
          name: 'committedDate',
          label: '提交日期',
          field: 'committedDate',
          align: 'left',
          sortable: true
        },
        {
          name: 'fileDiffUrl',
          label: '文件差异链接',
          field: 'fileDiffUrl',
          align: 'left'
        },
        {
          name: 'fileNameList',
          label: '变更文件列表',
          field: 'fileNameList',
          align: 'left'
        }
      ],
      fileListOpen: false,
      fileList: []
    };
  },
  watch: {
    pagination(val) {
      seDetailPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('global', ['loading']),
    ...mapState('dashboard', ['gitlabCommitDetail', 'projectUrl'])
  },
  methods: {
    ...mapActions('dashboard', ['queryGitlabCommitDetail', 'getProjectUrl']),
    async openUrl(row) {
      await this.getProjectUrl({
        projectName: row.projectName,
        short_id: row.short_id
      });
      window.open(this.projectUrl);
    },
    openList(data) {
      this.fileListOpen = true;
      this.fileList = data;
    },
    goBack() {
      this.$router.push('/dashboard/code/statistics');
    }
  },
  async created() {
    await this.queryGitlabCommitDetail({
      userName: this.$route.params.name,
      startDate: this.$route.query.time.start_date,
      endDate: this.$route.query.time.end_date
    });
    this.detailData = this.gitlabCommitDetail;
  }
};
</script>

<style lang="stylus" scoped>
.link-style
  color #2196f3
  cursor pointer
.font
 font-weight 600
 font-size 14px
 font-family: PingFangSC-Regular;
 font-size: 14px;
 color: #333333;
 letter-spacing: 0;
 line-height: 14px;
.pathHeight
  max-height:500px
</style>
