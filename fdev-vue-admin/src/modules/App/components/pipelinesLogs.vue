<template>
  <Loading :visible="loading">
    <fdev-table
      v-if="appCiType == 'git-ci' || !appCiType"
      key="gitsLog"
      class="my-sticky-column-table q-mt-md"
      :data="gitsData"
      :columns="columnsGit"
      row-key="id"
      :pagination.sync="pagination"
      hide-bottom
      :title="!hideTitle ? '流水线列表' : ''"
      noExport
      no-select-cols
      title-icon="list_s_f"
    >
      <template v-slot:top-right>
        <f-formitem
          page
          label="日志类型："
          v-if="appCiType == 'git-ci' || !appCiType"
        >
          <fdev-btn label="git-ci" />
        </f-formitem>
      </template>
      <template v-slot:body-cell-status="props">
        <fdev-td class="status-width">
          <div
            @click="open(props.row.web_url)"
            class="statusSty"
            :class="props.row.status"
          >
            {{ props.row.status | pass }}
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-id="props">
        <fdev-td class="status-width">
          <a target="_blank" class="text-black" :href="props.row.web_url">
            #{{ props.row.id }}
          </a>
        </fdev-td>
      </template>

      <template v-slot:body-cell-commit="props">
        <fdev-td class="commit-width">
          <div class="comSty">
            <fdev-tooltip>
              <f-icon name="branch" :width="14" :height="14" color="light" />
              {{ props.row.ref }}
              <fdev-icon name="ion-git-commit" color="light" />
              {{ props.row.commit.short_id }}
              <br />
              {{ props.row.commit.author_name }}
              {{ props.row.commit.message }}
            </fdev-tooltip>
            <f-icon name="branch" color="light" />
            <a
              class="text-black"
              target="_blank"
              :href="`${url(props.row.web_url)}commits/${props.row.ref}`"
            >
              {{ props.row.ref }}
            </a>
            <fdev-icon name="ion-git-commit" color="light" />
            <a
              class="text-primary"
              target="_blank"
              :href="`${url(props.row.web_url)}commit/${props.row.sha}`"
            >
              {{ props.row.commit.short_id }}
            </a>
            <div class="commit-msg">
              {{ props.row.commit.author_name }}
              <a
                class="text-black"
                target="_blank"
                :href="`${url(props.row.web_url)}commit/${props.row.sha}`"
              >
                {{ props.row.commit.message }}
              </a>
            </div>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-jobs="props">
        <fdev-td class="stages stage-circle-job">
          <div
            class="icon-wrapper"
            v-for="item in props.row.jobs"
            :key="item.id"
          >
            <fdev-icon
              class="radius"
              :name="icon[item.status]"
              :class="item.status"
            >
              <fdev-menu anchor="bottom left" :offset="[120, 10]">
                <fdev-list>
                  <fdev-item clickable v-close-popup @click="jump(item.id)">
                    <fdev-item-section side>
                      <fdev-icon
                        class="radius"
                        :name="icon[item.status]"
                        :class="item.status"
                      />
                    </fdev-item-section>
                    <fdev-item-section>
                      {{ item.name }}
                    </fdev-item-section>
                  </fdev-item>
                </fdev-list>
              </fdev-menu>
            </fdev-icon>
            <fdev-tooltip position="bottom">
              {{ item.stage }}: {{ item.status | pass }}
            </fdev-tooltip>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-finished_at="props">
        <fdev-td class="status-width">
          {{ props.row.finished_at | timeFormat }}
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import Loading from '@/components/Loading';
import moment from 'moment';
import { icon, gitColumns } from '@/modules/App/utils/constants';
export default {
  name: '',
  components: {
    Loading
  },
  props: {
    applicationId: {
      type: String,
      default: ''
    },
    appInfo: Object,
    hideTitle: {
      type: Boolean,
      default: false
    }
  },
  filters: {
    timeFormat(val) {
      return moment(val).format('YYYY-MM-DD HH:mm:ss');
    },
    pass(val) {
      return val == 'success' ? 'pass' : val;
    }
  },
  data() {
    return {
      project_id: '',
      appCiType: '',
      icon: icon,
      primary: 'primary',
      white: 'white',
      url1: require('@/assets/icon-pass.png'),
      url2: require('@/assets/icon-fail.png'),
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      loading: false,
      columnsGit: gitColumns(),
      gitsData: []
    };
  },
  computed: {
    ...mapState('appForm', ['pipelinesList']),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapGetters('user', ['isSpecialGroup'])
  },
  watch: {},
  methods: {
    ...mapActions('appForm', ['queryPipelines']),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    jumpFdev(item, rowData, index) {
      let pipelineExeId = rowData.exeId;
      let jobExeId = '';
      if (
        item.status === 'success' ||
        item.status === 'pending' ||
        item.status === 'waiting'
      ) {
        jobExeId = item.jobs[0].jobExes[0].job_exe_id;
      } else {
        // error running 取相应状态的job_exe_id
        let jobs = item.jobs.filter(job => {
          return job.jobExes.filter(jobExe => {
            return (
              jobExe.job_exe_status === 'error' ||
              jobExe.job_exe_status === 'running'
            );
          });
        });
        jobExeId = jobs[0].jobExes[0].job_exe_id;
      }
      this.$router.push({
        path: `/configCI/logProfile/${jobExeId}/${pipelineExeId}`,
        query: {
          index: index
        }
      });
    },
    jump(id) {
      this.$router.push({
        name: 'Jobs',
        params: {
          job_id: id,
          project_id: this.project_id
        }
      });
    },
    url(web_url) {
      return web_url.substring(0, web_url.indexOf('pipelines'));
    },
    open(url) {
      window.open(url);
    },
    async init() {
      this.loading = true;
      try {
        await this.queryPipelines({
          project_id: this.project_id
        });
        this.gitsData = this.pipelinesList;
      } finally {
        this.loading = false;
      }
    }
  },
  async created() {
    this.loading = true;
    if (!this.appInfo.gitlab_project_id) {
      const params = JSON.parse(window.sessionStorage.getItem('typeAndproId'));
      this.project_id = params.project_id;
      this.appCiType = params.appCiType;
    } else {
      this.project_id = this.appInfo.gitlab_project_id;
      this.appCiType = this.appInfo.appCiType;
    }
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
.statusSty {
  display: inline-block;
  width: 82px;
  height:30px;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  margin-bottom: unset;
  line-height: 28px;
  &.success, &.succeeded {
    border: 1px solid  #1aaa55;
    color: #1aaa55;
  }
  &.canceled {
    border: 1px solid  #2e2e2e;
    color: #2e2e2e;
  }
  &.failed, &.error {
    border: 1px solid  #db3b21;
    color: #db3b21;
  }
  &.created {
    border: 1px solid #a7a7a7;
    color: #a7a7a7;
  }
  &.running, &.process {
    border 1px solid  #1f78d1;
    color: #1f78d1;
  }
  &.pending {
    border: 1px solid #1abbc1;
    color: #1abbc1;
  }
  &.waiting {
    border: 1px solid #3ab281;
    color: #3ab281;
  }
  &.skipped {
    border: 1px solid #a7a7a7;
    color: #a7a7a7;
  }
}
.status-width{
  // width:150px;
  text-align:center
}
// .commit-width{
//   width:300px;
// }

// .comSty {
//   margin: 0;
//   max-width: 300px;
//   white-space: nowrap;
//   overflow: hidden;
//   text-overflow: ellipsis;
// }

.commit-msg {
  overflow: hidden;
  text-overflow: ellipsis;
}

.stages {
  font-size: 24px;
}
.stage-circle-job{
  width: 300px
  display: flex;
  flex-wrap: wrap;
  min-height: 72px;
}
.icon-wrapper {
  display: inline-block;
  margin: 0 8px;
}

.radius {
  border-radius: 50%;
}

.success, .succeeded {
  color: #1aaa55;
}
.canceled {
  color: #2e2e2e;
}
.failed, .error {
  color: #db3b21;
}
.created {
  color: #a7a7a7;
  cursor: pointer;
}
.running, .process {
  cursor: pointer;
  color: #1f78d1;
}
.pending {
  cursor: pointer;
  color: #1f78d1;
}
.waiting {
  cursor: pointer;
  color: #3ab281;
}
.skipped {
  color: #a7a7a7;
  border: 1px solid #a7a7a7;
  cursor: pointer;
  width: 18px;
  height: 18px;
}


.stageLay {
  display: inline-block;
  margin: 0 15px;
  img {
    display: block;
  }
}
</style>
