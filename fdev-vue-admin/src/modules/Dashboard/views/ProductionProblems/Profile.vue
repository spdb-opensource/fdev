<template>
  <f-block>
    <Loading :visible="loading" class="q-pa-md">
      <!-- 标题&操作按钮 -->
      <div class="row">
        <div
          class="text-h6 titleLength"
          :title="issueDetail.problem_phenomenon"
        >
          {{ issueDetail.problem_phenomenon }}
          <fdev-badge>{{ issueDetail.deal_status }}</fdev-badge>
        </div>
        <fdev-space />
        <fdev-btn label="返回" dialog @click="goBack" />
        <fdev-btn
          label="修改"
          dialog
          class="q-ml-md"
          :to="`/job/list/${$route.params.id}/modifyProductionProblem`"
        />
      </div>
      <p class="font">详情</p>
      <div class="row q-ml-md">
        <description :col="3" class="q-mb-md" label="需求名称">
          {{ issueDetail.requirement_name }}
        </description>
        <description :col="3" class="q-mb-md" label="fdev任务编号">
          <router-link
            v-if="issueDetail.task_no"
            :to="`/job/list/${issueDetail.task_no}`"
            class="link"
          >
            {{ issueDetail.task_no }}
          </router-link>
          <span v-else>-</span>
        </description>
        <description :col="3" class="q-mb-md" label="发生日期">
          {{ issueDetail.occurred_time }}
        </description>
        <description :col="3" class="q-mb-md" label="问题首次出现时间">
          {{ issueDetail.first_occurred_time }}
        </description>
        <description :col="3" class="q-mb-md" label="定位时间">
          {{ issueDetail.location_time }}
        </description>
        <description :col="3" class="q-mb-md" label="修复时间">
          {{ issueDetail.repair_time }}
        </description>
        <description :col="3" class="q-mb-md" label="评审时间">
          {{ issueDetail.reviewer_time }}
        </description>
        <description :col="3" class="q-mb-md" label="发现阶段">
          {{ issueDetail.discover_stage }}
        </description>
        <description :col="3" class="q-mb-md" label="是否产生生产问题">
          {{ issueDetail.is_trigger_issue }}
        </description>
        <description :col="3" class="q-mb-md" label="生产问题级别">
          {{ issueDetail.issue_level }}
        </description>
        <description :col="3" class="q-mb-md" label="问题类型">
          {{ issueDetail.issue_type }}
        </description>
        <description :col="3" class="q-mb-md" label="所属小组">
          {{ issueDetail.module }}
        </description>
        <description :col="3" class="q-mb-md" label="所属窗口">
          {{ issueDetail.release_node }}
        </description>
        <description :col="3" class="q-mb-md" label="开发责任人">
          {{ issueDetail.dev_responsible }}
        </description>
        <description :col="3" class="q-mb-md" label="评审人">
          {{ issueDetail.reviewer }}
        </description>
        <description :col="3" class="q-mb-md" label="应急负责人">
          {{ issueDetail.emergency_responsible }}
        </description>
        <description :col="3" class="q-mb-md" label="代码审核责任人">
          {{ issueDetail.audit_responsible }}
        </description>
        <description :col="3" class="q-mb-md" label="内测责任人">
          {{ issueDetail.test_responsible }}
        </description>
        <description :col="3" class="q-mb-md" label="牵头任务责任人">
          {{ issueDetail.task_responsible }}
        </description>
        <description :col="3" class="q-mb-md" label="能否UAT复现">
          {{ issueDetail.is_uat_replication }}
        </description>
        <description :col="3" class="q-mb-md" label="能否REL复现">
          {{ issueDetail.is_rel_replication }}
        </description>
        <description :col="3" class="q-mb-md" label="能否灰度复现">
          {{ issueDetail.is_gray_replication }}
        </description>
        <description :col="3" class="q-mb-md" label="是否涉及紧急需求">
          {{ issueDetail.is_involve_urgency }}
        </description>
        <description :col="3" class="q-mb-md" label="评审状态">
          {{ issueDetail.reviewer_status }}
        </description>
        <description :col="3" class="q-mb-md" label="处理状态">
          {{ issueDetail.deal_status }}
        </description>
      </div>
      <div>
        <p class="font">应急过程</p>
        <p class="desc" v-html="descFilter(issueDetail.emergency_process)"></p>
      </div>
      <div>
        <p class="font">影响范围</p>
        <p class="desc" v-html="descFilter(issueDetail.influence_area)"></p>
      </div>
      <div>
        <p class="font">问题现象</p>
        <p class="desc" v-html="descFilter(issueDetail.problem_phenomenon)"></p>
      </div>
      <div>
        <p class="font">问题原因</p>
        <p class="desc" v-html="descFilter(issueDetail.issue_reason)"></p>
      </div>
      <div>
        <p class="font">改进措施</p>
        <p
          class="desc"
          v-html="descFilter(issueDetail.improvement_measures)"
        ></p>
      </div>
      <div>
        <p class="font">评审意见</p>
        <p class="desc" v-html="descFilter(issueDetail.reviewer_comment)"></p>
      </div>
      <div>
        <p class="font">备注</p>
        <p class="desc" v-html="descFilter(issueDetail.remark)"></p>
      </div>
      <div>
        <p class="font">附件列表</p>
        <span v-if="issueFilesList.length > 0">
          <p class="desc" v-for="(item, index) in issueFilesList" :key="index">
            {{ item.name }}
            <fdev-btn
              label="下载"
              ficon="download"
              class="q-ml-md"
              flat
              @click="downloadFiles(item.file_id)"
            />
          </p>
        </span>
        <p v-else class="desc">-</p>
      </div>
      <div>
        <fdev-table
          :data="issueDetail.backlog_schedule_list"
          :columns="backlogColumns"
          title="待办事项列表"
          titleIcon="list_s_f"
          class="q-my-md"
        />
      </div>
      <div>
        <fdev-table
          :data="issueDetail.responsible_list"
          :columns="responsibleColumns"
          class="q-my-md"
          title="问责列表"
          titleIcon="list_s_f"
          noExport
        />
      </div>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import Description from '@/components/Description';
import { exportExcel } from '@/utils/utils';

export default {
  name: 'ProductionProblemsProfile',
  components: { Loading, Description },
  data() {
    return {
      loading: false,
      issueFilesList: [],
      responsibleColumns: [
        {
          name: 'responsible',
          label: '问责人',
          field: 'responsible',
          align: 'left'
        },
        {
          name: 'responsibility_type',
          label: '问责类型',
          field: 'responsibility_type',
          align: 'left'
        },
        {
          name: 'responsibility_content',
          label: '问责内容',
          field: 'responsibility_content',
          align: 'left'
        }
      ],
      backlogColumns: [
        {
          name: 'backlog_schedule',
          label: '待办事项',
          field: 'backlog_schedule',
          align: 'left'
        },
        {
          name: 'backlog_schedule_reviewer',
          label: '负责人',
          field: 'backlog_schedule_reviewer',
          align: 'left'
        },
        {
          name: 'backlog_schedule_complete_time',
          label: '完成时间',
          field: 'backlog_schedule_complete_time',
          align: 'left'
        },
        {
          name: 'backlog_schedule_current_completion',
          label: '当前完成情况',
          field: 'backlog_schedule_current_completion',
          align: 'left'
        },
        {
          name: 'backlog_schedule_complete_percentage',
          label: '完成百分比',
          field: 'backlog_schedule_complete_percentage',
          align: 'left'
        }
      ]
    };
  },
  computed: {
    ...mapState('dashboard', ['issueDetail']),
    ...mapState('jobForm', ['issueFilesDate', 'fileDownloadDate'])
  },
  methods: {
    ...mapActions('dashboard', ['queryIssueDetail']),
    ...mapActions('jobForm', ['queryIssueFiles', 'fileDownload']),
    descFilter(val) {
      if (!val) return '-';
      val = val.replace(/<[^>]+>/g, '');
      const reg = new RegExp(/\n/g);
      return val.replace(reg, '</br>');
    },
    async downloadFiles(id) {
      await this.fileDownload({ id: id });
      exportExcel(this.fileDownloadDate);
    },
    async init() {
      this.loading = true;
      await this.queryIssueDetail({
        id: this.$route.params.id
      });
      await this.queryIssueFiles({
        id: this.$route.params.id
      });
      this.issueFilesList = this.issueFilesDate;
      this.loading = false;
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.font
  font-weight 700
  font-family: PingFangSC-Regular;
  color: #333333;
  letter-spacing: 0;
.desc
  word-break break-all
  font-size 14px
  color #616161
  margin-left 16px
.btn-modify
  float right
.titleLength
  max-width 70%
  overflow hidden
  text-overflow ellipsis
</style>
