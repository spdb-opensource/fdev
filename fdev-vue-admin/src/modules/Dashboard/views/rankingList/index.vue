<template>
  <div>
    <Loading :visible="loading">
      <!-- 按代码比例计算，查看全部 -->
      <f-block block class="q-my-md q-pt-md">
        <div>
          <fdev-toggle
            :value="percentOpened"
            @input="updatepercentOpened($event)"
            label="按代码行数比例计算:"
            left-label
          />
          <a
            class="float-right text-primary"
            href="xxx/projects?sort=-reliability"
            target="_blank"
          >
            查看全部
          </a>
        </div>
      </f-block>
      <div class="row no-wrap q-mb-md">
        <!-- bugs前十应用 -->
        <f-block block class="col-6 q-mr-md">
          <div class="inline-block title q-ml-sm">
            bugs前十应用
            <fdev-btn
              ficon="download"
              class="q-ma-sm "
              label="下载"
              normal
              @click="downloadbugExcel"
            />
          </div>
          <StackBarChart
            id="bugs"
            :chartData="bugs"
            width="100%"
            @click="data => linkToApp(data, 'bugs')"
          />
          <RankingForm
            v-show="false"
            id="bug"
            liHeader="bugs前十应用"
            secondLiHeader="bugs"
            :rankType="bugsType"
          />
        </f-block>
        <!-- 漏洞前十应用 -->
        <f-block block class="col-grow">
          <div class="inline-block title q-ml-sm ">
            漏洞前十应用
            <fdev-btn
              class="q-ma-sm"
              ficon="download"
              normal
              label="下载"
              @click="downloadvulnerabilitieExcel"
            />
          </div>
          <StackBarChart
            id="vulnerabilities"
            :chartData="vulnerabilities"
            width="100%"
            @click="data => linkToApp(data, 'vulnerabilities')"
          />
          <RankingForm
            v-show="false"
            id="vulnerabilitie"
            liHeader="漏洞前十应用"
            secondLiHeader="漏洞"
            :rankType="vulnerabilitiesType"
          />
        </f-block>
      </div>
      <div class="row no-wrap q-mb-md">
        <!-- 异味前十应用 -->
        <f-block block class="col-6 q-mr-md">
          <div class="inline-block title q-ml-sm ">
            异味前十应用
            <fdev-btn
              class="q-ma-sm"
              ficon="download"
              normal
              label="下载"
              @click="downloadSmellExcel"
            />
          </div>
          <StackBarChart
            id="code_smells"
            :chartData="code_smells"
            width="100%"
            @click="data => linkToApp(data, 'code_smells')"/>
          <RankingForm
            v-show="false"
            id="smell"
            liHeader="异味前十应用"
            secondLiHeader="异味"
            :rankType="smellsType"
        /></f-block>
        <!-- 重复率前十应用 -->
        <f-block block class="col-grow">
          <div class="inline-block title q-ml-sm">
            重复率前十应用
            <fdev-btn
              class="q-ma-sm"
              ficon="download"
              normal
              label="下载"
              @click="downloadDuplicateExcel"
            />
          </div>
          <StackBarChart
            id="duplicated"
            :chartData="duplicated_lines_density"
            width="100%"
            @click="data => linkToApp(data, 'duplicated_lines_density')"/>
          <RankingForm
            v-show="false"
            id="duplicate"
            liHeader="重复率前十应用"
            secondLiHeader="重复率"
            rankType="duplicated_lines_density"
        /></f-block>
      </div>
    </Loading>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import StackBarChart from '@/modules/Dashboard/components/Chart/StackBarChart';
import { sonar } from '@/modules/Dashboard/utils/constants';
import RankingForm from '@/modules/Dashboard/components/form/RankingForm';
export default {
  name: 'RankingList',
  components: { Loading, StackBarChart, RankingForm },
  data() {
    return {
      loading: false,
      bugs: {},
      vulnerabilities: {},
      code_smells: {},
      duplicated_lines_density: {},

      chartTop: '1px',
      bugType: 'bugs',
      bugRadioType: 'bugs_radio',
      vulnerabilitieType: 'vulnerabilities',
      vulnerabilitieRadioType: 'vulnerabilities_radio',
      smellType: 'code_smells',
      smellRadioType: 'code_smells_radio'
    };
  },
  watch: {
    percentOpened: {
      handler(val) {
        this.init(val);
      }
    }
  },
  computed: {
    ...mapState('dashboard', ['ranking']),
    ...mapState('userActionSaveDashboard/rankingList', ['percentOpened']),
    bugsType() {
      if (!this.percentOpened) {
        return this.bugType;
      } else {
        return this.bugRadioType;
      }
    },
    vulnerabilitiesType() {
      if (!this.percentOpened) {
        return this.vulnerabilitieType;
      } else {
        return this.vulnerabilitieRadioType;
      }
    },
    smellsType() {
      if (!this.percentOpened) {
        return this.smellType;
      } else {
        return this.smellRadioType;
      }
    }
  },
  methods: {
    ...mapActions('dashboard', {
      query: 'searchProject'
    }),
    ...mapMutations('userActionSaveDashboard/rankingList', [
      'updatepercentOpened'
    ]),

    sonarChartFilter(arr, name, percent, color) {
      const xAxis = [];
      const data = [];
      arr.forEach(item => {
        const name = item.name.includes(':')
          ? item.name.substring(item.name.indexOf(':') + 1)
          : item.name;
        xAxis.unshift(name);
        data.unshift(item.value);
      });
      const series = [
        {
          name: sonar[name],
          type: 'bar',
          stack: null,
          data: data,
          label: {
            normal: {
              show: true,
              formatter: percent ? '{c}%' : '{c}'
            }
          },
          itemStyle: {
            normal: {
              color: color
            }
          }
        }
      ];
      return { xAxis, series };
    },
    linkToApp(data, target) {
      const appId = this.ranking[target].find(item => {
        const appName = item.name.includes(':')
          ? item.name.substring(item.name.indexOf(':') + 1)
          : item.name;
        return appName === data.group;
      }).id;

      this.$router.push({
        path: `/app/list/${appId}`,
        query: { tab: 'sonar' }
      });
    },
    init(percent) {
      const radio = percent ? '_radio' : '';
      this.bugs = this.sonarChartFilter(
        this.ranking[`bugs${radio}`],
        'bugs',
        percent,
        '#59C3BA'
      );
      this.vulnerabilities = this.sonarChartFilter(
        this.ranking[`vulnerabilities${radio}`],
        'vulnerabilities',
        percent,
        '#F4B24D'
      );
      this.code_smells = this.sonarChartFilter(
        this.ranking[`code_smells${radio}`],
        'code_smells',
        percent,
        '#5C2FF3'
      );
      this.duplicated_lines_density = this.sonarChartFilter(
        this.ranking.duplicated_lines_density.map(item => {
          return { ...item, value: item.value / 100 };
        }),
        'duplicated_lines_density',
        true,
        '#E63453'
      );
    },
    // 下载表格
    downloadbugExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('bug', 'bugs前十应用.xlsx');
      });
    },
    downloadvulnerabilitieExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('vulnerabilitie', '漏洞前十应用.xlsx');
      });
    },
    downloadSmellExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('smell', '异味前十应用.xlsx');
      });
    },
    downloadDuplicateExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('duplicate', '重复率前十应用.xlsx');
      });
    }
  },
  async created() {
    this.loading = true;
    await this.query({
      key: 'all'
    });
    this.init(this.percentOpened);
    this.loading = false;
  }
};
</script>

<style lang="stylus" scoped>
.float-right
  line-height 40px
  margin-right: 8px;
.title
  height 40px
  line-height 40px
  color #008acd
  font-size 18px
  width 100%
</style>
