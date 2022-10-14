<template>
  <f-block>
    <!-- 表格数据 -->
    <Loading :visible="loading['measureForm/queryCommitByUser']">
      <fdev-table
        :data="detailData"
        :columns="columns"
        title="代码统计详情"
        row-key="job.id"
        titleIcon="detail_s_f"
        :pagination.sync="pagination"
      >
        <template v-slot:top-bottom>
          <f-formitem
            class="col-6 q-pr-md"
            bottom-page
            label-style="width:60px"
            label="统计范围"
          >
            <fdev-select
              map-options
              multiple
              emit-value
              clearable
              option-label="label"
              option-value="value"
              :value="tableAppType"
              :options="appOptions"
              @input="onTableRequest($event, 'tableAppType')"
            />
          </f-formitem>
        </template>
        <!-- 文件差异链接 -->
        <template v-slot:body-cell-fileDiffUrl="props">
          <fdev-td @click="openUrl(props.row)" class="link-style">
            查看差异文件详情
          </fdev-td>
        </template>
        <!-- 变更文件列表 -->
        <template v-slot:body-cell-fileNameList="props">
          <fdev-td @click="openList(props.row)" class="link-style">
            查看文件变更列表
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <!-- 查看变更列表弹窗 -->
    <f-dialog v-model="fileListOpen" title="变更文件路径列表">
      <Loading :visible="pathLoading">
        <div class="scroll-thin pathHeight">
          <fdev-list boardered separator class="q-mb-md">
            <fdev-item
              v-ripple
              v-for="(val, index) in fileList"
              :key="index.id"
            >
              <fdev-item-section>
                {{ val }}
              </fdev-item-section>
            </fdev-item>
          </fdev-list>
        </div>
      </Loading>
      <template v-slot:btnSlot>
        <fdev-btn label="知道了" dialog @click="fileListOpen = false" />
      </template>
    </f-dialog>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  seDetailPagination,
  getDetailPagination
} from '@/modules/Measure/utils/setting';
import { appOptions } from '@/modules/Measure/utils/constants';
export default {
  name: 'CodeDetail',
  components: { Loading },
  data() {
    return {
      appOptions: appOptions(),
      detailData: [],
      pagination: getDetailPagination(),
      columns: [
        {
          name: 'committerName',
          label: '提交人',
          field: 'committerName',
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
          field: row => row.stats.total,
          align: 'left',
          sortable: true
        },
        {
          name: 'additions',
          label: '添加行数',
          field: row => row.stats.additions,
          align: 'left',
          sortable: true
        },
        {
          name: 'deletions',
          label: '删除行数',
          field: row => row.stats.deletions,
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
      fileList: [],
      pathLoading: false
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
    ...mapState('measureForm', ['gitlabCommitDetail', 'commitDiff']),
    ...mapState('userActionSaveMeasure/codeStatistics', ['tableAppType'])
  },
  methods: {
    ...mapActions('measureForm', ['queryCommitByUser', 'queryCommitDiff']),
    ...mapMutations('userActionSaveMeasure/codeStatistics', ['updateAppType']),
    // 跳转差异链接
    async openUrl(row) {
      window.open(row.webUrl);
    },
    // 打开文件差异链接
    async openList(data) {
      this.fileListOpen = true;
      this.pathLoading = true;
      try {
        let { projectId, shortId } = data;
        await this.queryCommitDiff({ projectId, shortId });
        this.fileList = this.commitDiff;
        this.pathLoading = false;
      } catch (e) {
        this.pathLoading = false;
      }
    },
    onTableRequest(val, type) {
      if (type == 'tableAppType') {
        this.updateAppType(val);
      }
      this.queryTable();
    },
    async queryTable() {
      await this.queryCommitByUser({
        user: {
          email: this.$route.params.name
        },
        startDate: this.$route.query.time.start_date,
        endDate: this.$route.query.time.end_date,
        statisticRange: this.tableAppType
      });
      this.detailData = this.gitlabCommitDetail.data;
    }
  },
  created() {
    this.queryTable();
  }
};
</script>

<style lang="stylus" scoped>
.link-style
  color #2196f3
  cursor pointer
.pathHeight
  max-height:400px
</style>
