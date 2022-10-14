<template>
  <Loading :visible="loading">
    <div v-if="!sonarScan && appId">暂无sonar扫描数据</div>
    <div v-else>
      <div class="q-mt-sm row second_wrapper">
        <div class="col q-mr-sm q-px-md bg-white">
          <div class="row q-mt-xs">
            <div class="col-6 q-pb-md text-grey-7">
              最近分析时间：{{ sonarScan.time }}
            </div>
            <div class="col-6 q-pb-md text-grey-7 text-right">
              版本：{{ sonarScan.version }}
            </div>
          </div>

          <div class="row q-pb-md">
            <div class="col-6 text-center auto_center">
              <div class="size_radius">
                {{ sonarDetail.size }}
              </div>
              <h3>{{ sonarDetail.ncloc | format }}</h3>
              <span q-mt-md>代码行</span>
            </div>

            <fdev-separator vertical inset />

            <ul class="col q-ml-sm align-center language">
              <li v-for="item in sonarDetail.language" :key="item.name">
                <span class="language_name">{{ item.name }}：</span>
                <span
                  class="line"
                  :style="{
                    width: (item.value / sonarDetail.ncloc) * 100 + 'px'
                  }"
                ></span>
                <span>{{ item.value | format }}</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="col-5 bg-white chart">
          <LineChart :gitlab_id="gitlab_id" />
        </div>
      </div>

      <div class="row content q-mt-sm">
        <div class="content_detail col q-mr-sm">
          <a
            :href="sonarDetail.bugs.url"
            target="_blank"
            v-if="sonarDetail.bugs"
          >
            <h4>
              {{ sonarDetail.bugs.value }}
              <div
                class="radius"
                :class="`color_${sonarDetail.reliability_rating}`"
              >
                {{ sonarDetail.reliability_rating }}
              </div>
            </h4>
            <p class="text-grey-7">
              <fdev-icon name="ion-ios-bug" />
              Bugs
            </p>
          </a>
        </div>
        <div class="content_detail col q-mr-sm">
          <a
            :href="sonarDetail.vulnerabilities.url"
            target="_blank"
            v-if="sonarDetail.vulnerabilities"
          >
            <h4 v-if="sonarDetail.vulnerabilities">
              {{ sonarDetail.vulnerabilities.value }}
              <div
                class="radius"
                :class="`color_${sonarDetail.security_rating}`"
              >
                {{ sonarDetail.security_rating }}
              </div>
            </h4>
            <p class="text-grey-7">
              <fdev-icon name="ion-unlock" />
              漏洞
            </p>
          </a>
        </div>
        <div class="content_detail col q-mr-sm">
          <a
            :href="sonarDetail.code_smells.url"
            target="_blank"
            v-if="sonarDetail.code_smells"
          >
            <h4 v-if="sonarDetail.code_smells">
              {{ sonarDetail.code_smells.value }}
              <div class="radius" :class="`color_${sonarDetail.sqale_rating}`">
                {{ sonarDetail.sqale_rating }}
              </div>
            </h4>
            <p class="text-grey-7">
              <fdev-icon name="ion-ios-nuclear" />
              异味
            </p>
          </a>
        </div>
        <div class="content_detail col q-mr-sm">
          <h4>
            <fdev-icon name="ion-radio-button-off" color="red" />
            {{ sonarDetail.coverage }}%
          </h4>
          <p class="text-grey-7">
            覆盖率
          </p>
        </div>
        <div class="content_detail col">
          <h4>
            <div class="circle" :class="`color_${sonarDetail.coverage_rating}`">
              <div class="rating"></div>
            </div>
            {{ sonarDetail.duplicated_lines_density }}%
          </h4>
          <p class="text-grey-7">
            重复
          </p>
        </div>
      </div>

      <fdev-table
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        class="q-mt-sm"
        noExport
        no-select-cols
        title="代码分析"
        title-icon="list_s_f"
      >
        <template v-slot:top-bottom-opt>
          <div class="self-start">
            <fdev-breadcrumbs>
              <template v-slot:separator>
                <fdev-icon size="1.5em" name="chevron_right" color="primary" />
              </template>
              <fdev-breadcrumbs-el
                @click="handleBreadcrumbs(path, index)"
                :label="path.name"
                v-for="(path, index) in history"
                :key="index"
              />
            </fdev-breadcrumbs>
          </div>
        </template>
        <template v-slot:body-cell-key="props">
          <td
            class="text-ellipsis"
            :title="
              props.row.qualifier === 'FIL' ? props.row.name : props.value
            "
          >
            <a
              v-if="props.row.qualifier === 'FIL'"
              class="text-primary cursor-pointer"
              target="_blank"
              :href="props.row.path"
            >
              <fdev-icon name="ion-document" />
              {{ props.row.name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </a>
            <span
              v-else
              @click="getPath(props.row)"
              class="text-primary cursor-pointer"
            >
              <fdev-icon name="folder" />
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </span>
          </td>
        </template>
      </fdev-table>
    </div>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import LineChart from './lineChart';
import { rating, snoarColumns } from '@/modules/App/utils/constants';
export default {
  name: 'sonar',
  components: { Loading, LineChart },
  data() {
    return {
      loading: false,
      columns: snoarColumns(),
      pagination: {
        rowsPerPage: 0
      },
      tableData: [],
      history: []
    };
  },
  props: {
    gitlab_id: Number,
    appId: String,
    branch: String
  },
  computed: {
    ...mapState('appForm', [
      'sonarScan',
      'sonarDetail',
      'sonarTableData',
      'appData'
    ])
  },
  filters: {
    format(value) {
      if (value < 1000) {
        return value;
      }

      value = value / 1000;

      if (value < 10) {
        return value.toFixed(1) + 'K';
      } else {
        return parseInt(value) + 'K';
      }
    },
    ratingFilter({ value }) {
      if (!value) {
        return;
      }
      return rating[value];
    }
  },
  methods: {
    ...mapActions('appForm', {
      querySonarScan: 'projectAnalyses',
      querySonarDetail: 'getProjectInfos',
      querySonarTableData: 'componentTree',
      featureProjectAnalyses: 'featureProjectAnalyses',
      getProjectFeatureInfo: 'getProjectFeatureInfo',
      featureComponentTree: 'featureComponentTree',
      queryDetail: 'queryDetail' //为了分支sonar 结果展示连接跳转
    }),
    getPath(item) {
      this.history.push(item);
      this.getCurrentDirOrFile(item);
    },
    getCurrentDirOrFile(item) {
      const { key, children } = item;
      if (children.length === 0) {
        this.tableData = this.sonarTableData.filter(row => {
          return row.key.indexOf(key) > -1 && row.key !== key;
        });
      } else {
        let dirs = this.sonarTableData.filter(row => {
          return (
            row.key.indexOf(key) > -1 &&
            row.key !== key &&
            row.qualifier === 'DIR'
          );
        });
        this.tableData = children.concat(dirs);
      }
    },
    handleBreadcrumbs(item, index) {
      if (index === this.history.length - 1) {
        return;
      }
      this.getCurrentDirOrFile(item);
      this.history = this.history.slice(0, index + 1);
    },
    async init() {
      this.loading = true;
      if (this.gitlab_id) {
        const params = { gitlab_id: this.gitlab_id.toString() };
        this.querySonarScan(params);
        this.querySonarDetail(params);
        await this.querySonarTableData(params);
        this.tableData = this.sonarTableData;
      } else {
        // 查分支的snoar结果
        const params = {
          id: this.appId,
          branch_name: this.branch
        };
        await this.featureProjectAnalyses(params);
        if (!this.sonarScan) {
          this.loading = false;
          return;
        }
        this.getProjectFeatureInfo(params);
        await this.featureComponentTree(params);
        this.tableData = this.sonarTableData;
      }

      this.loading = false;
    }
  },
  async created() {
    await this.init();
    // 为了分支sonar 结果展示连接跳转
    if (this.appId) {
      await this.queryDetail({ id: this.appId });
    }
    this.history.push({
      name: this.appData[0].name_en,
      children: this.sonarTableData
    });
  }
};
</script>

<style lang="stylus" scoped>

h4, p
  margin 0
  color black
.second_wrapper
  .row
    align-items center
  .auto_center
    height auto
    position relative
    h3
      width 100%
      margin 0 auto
    span
      display block
    .size_radius
      display inline-block
      position absolute
      right calc(50% - 84px)
      top -5px
      color white
      border-radius 50%
      height 30px
      width 30px
      line-height 30px
      background #0663BE
.content
  .content_detail
    background white
    padding 15px 8px
    text-align center
    h4
      position relative
      display inline-block
      .q-icon
        vertical-align: initial;
  .radius
    border-radius 50%
    width 25px
    height 25px
    line-height 25px
    text-align center
    color white
    position absolute
    top -5px
    right -28px
    font-size 16px
    &.color_A
      background #009688
    &.color_B
      background #4caf50
    &.color_C
      background #ffeb3b
    &.color_D
      background #ff9800
    &.color_E
      background #f44336
.chart
  min-height 130px
.language
  padding 0
  li
    list-style none
  .language_name
    width 77px
    text-align right
    display inline-block
  .line
    height 10px
    background #5C2FF3
    display inline-block
    margin-right 8px
.circle
  border 2px solid black
  border-radius 50%
  display inline-block
  width 25px
  height 25px
  position relative
  .rating
    border-radius 50%
    width 0%
    position absolute
    overflow hidden
  &.color_A
    border-color #009688
  &.color_B
    border-color #4caf50
    .rating
      width 35%
      padding-bottom 35%
      background #4caf50
      left 50%
      margin-top -17.5%
      margin-left -17.5%
      top 50%
  &.color_C
    border-color #ffeb3b
    .rating
      width 45%
      padding-bottom 45%
      background #ffeb3b
      left 50%
      margin-top -22.5%
      margin-left -22.5%
      top 50%
  &.color_D
    border-color #ff9800
    .rating
      width 65%
      padding-bottom 65%
      background #ff9800
      left 50%
      margin-top -32.5%
      margin-left -33.5%
      top 50%
  &.color_E
    border-color #f44336
    .rating
      width 75%
      padding-bottom 75%
      background #f44336
      left 50%
      margin-top -37.5%
      margin-left -38.5%
      top 50%
</style>
