<template>
  <f-block>
    <Loading :visible="loading">
      <div class="row no-warp">
        <div class="txtStyle">
          应用英文名<span class="ml10">{{ jobProfile.project_name }}</span>
        </div>
        <fdev-space />
        <div class="row no-warp" v-if="jobProfile.appCiType">
          <div class="txtStyle">日志类型:</div>
          <div class="divBlcok">
            <span class="gitChip">{{ jobProfile.appCiType }}</span>
          </div>
        </div>
      </div>
      <fdev-table
        class="my-sticky-column-table q-mt-md"
        :data="gitsData"
        :columns="columnsGit"
        row-key="id"
        :pagination.sync="pagination"
        noExport
        no-select-cols
        @request="onRequest"
      >
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
              <f-icon name="branch" color="light" class="mr8" />
              <a
                class="text-black"
                target="_blank"
                :href="`${url(props.row.web_url)}commits/${props.row.ref}`"
              >
                {{ props.row.ref }}
              </a>
              <fdev-icon
                name="ion-git-commit"
                color="light"
                class="padControl"
              />
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
          <fdev-td class="stages">
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
  </f-block>
</template>
<script>
import Loading from '@/components/Loading';
import { icon, gitColumns } from '@/modules/App/utils/constants';
import { mapActions, mapState } from 'vuex';
import moment from 'moment';
export default {
  name: 'pipeline',
  components: { Loading },
  computed: {
    ...mapState('appForm', ['pipelinesListPage']),
    ...mapState('jobForm', ['jobProfile'])
  },
  data() {
    return {
      icon: icon,
      loading: false,
      columnsGit: gitColumns(),
      gitsData: [],
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 1,
        page: 1
      }
    };
  },
  filters: {
    timeFormat(val) {
      return moment(val).format('YYYY-MM-DD HH:mm:ss');
    },
    pass(val) {
      return val == 'success' ? 'pass' : val;
    }
  },
  async created() {
    this.loading = true;
    await this.init();
    this.loading = false;
  },
  methods: {
    ...mapActions('appForm', ['queryPipelinesWithJobsPage']),
    jump(id) {
      this.$router.push({
        name: 'Jobs',
        params: {
          job_id: id,
          project_id: this.jobProfile.gitlabId
        }
      });
    },
    url(web_url) {
      return web_url.substring(0, web_url.indexOf('pipelines'));
    },
    open(url) {
      window.open(url);
    },
    onRequest(props) {
      const { page, rowsNumber, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsNumber = rowsNumber;
      this.pagination.rowsPerPage = rowsPerPage;
      this.init();
    },
    async init() {
      this.loading = true;
      try {
        await this.queryPipelinesWithJobsPage({
          project_id: this.jobProfile.gitlabId,
          pageNum: this.pagination.page,
          pageSize: this.pagination.rowsPerPage
        });
        this.gitsData = this.pipelinesListPage.data;
        this.pagination.rowsNumber = this.pipelinesListPage.count;
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
<style lang="stylus" scoped>
.gitChip {
  font-size: 12px;
  color: #0047FF;
  letter-spacing: 0;
  line-height: 12px;
}

.ml10 {
  margin-left: 10px;
}

.txtStyle {
  font-weight: 600;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
}

.divBlcok {
  background: rgba(0, 71, 255, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 65px;
  height: 22px;
  text-align: center;
  margin-left: 16px;
}

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
  text-align:center;
  width: 100px;
}

.commit-msg {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  color: #000000;
  letter-spacing: 0;
  line-height: 22px;
}

.stages {
  max-width: max-content
  font-size: 24px;
  width: 300px;
}

.icon-wrapper {
  display: inline-block;
  margin: 0 12px 0 0;
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
  display: inline-flex;
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
.commit-width{
  min-width:410px;
  width :auto;
}

.comSty {
  margin: 0;
  min-width: 346px;
  width :auto;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0;
  line-height: 22px;
}
.text-black {
  font-size: 14px;
  color: #000000;
  letter-spacing: 0;
  line-height: 22px;
}
.padControl{
  padding: 0 8px;
}
.text-primary {
  font-size: 14px;
  color: #0663BE;
  letter-spacing: 0;
  line-height: 22px;
}
.mr8{
  margin-right: 8px;
}
</style>
