<template>
  <Loading :visible="loading">
    <div :style="{ width: '100%' }">
      <div class="lineOne-blocks flex row no-wrap">
        <fdev-card class="lineOne-block" square flat>
          <fdev-card-section class="title">
            互联网条线实施中需求总量
          </fdev-card-section>
          <fdev-card-section
            class="flex row no-wrap"
            :style="{ 'margin-top': '48px' }"
          >
            <div class="flex column" :style="{ 'margin-left': '62px' }">
              <span class="requirement-num-title">业务需求数</span>
              <span
                class="requirement-num"
                :style="{ color: '#FAD236', 'margin-bottom': '50px' }"
              >
                {{ businessTotal }}
              </span>
              <span class="requirement-num-title">科技需求数</span>
              <span class="requirement-num" :style="{ color: '#3AA0FF' }">
                {{ techTotal }}
              </span>
            </div>
            <AllRequirtPie :AllRequiredata="AllRequiredata" />
          </fdev-card-section>
        </fdev-card>
        <fdev-card class="lineOne-block" square flat>
          <fdev-card-section class="title">
            各阶段实施中需求
          </fdev-card-section>
          <BarChart
            barRef="doingRequire"
            :barOpt="doingRequireOpt"
            class="doing-require-bar"
          />
        </fdev-card>
      </div>
      <LineTwo
        v-if="groupNames.length > 0"
        class="one-line-block"
        :ids="groupIds"
        :names="groupNames"
        :groupdemandAmt="groupdemandAmt"
        :dataTwo="dataTwo"
      />
      <LineThree
        v-if="partNames.length > 0"
        class="one-line-block"
        :ids="partIds"
        :names="partNames"
        :partdemandAmt="partdemandAmt"
        :dataThr="dataThr"
      />
      <LineFour
        class="one-line-block"
        :ids="allIds"
        :allNames="allNames"
        v-if="allIds.length > 0"
      />
    </div>
  </Loading>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import AllRequirtPie from './components/AllRequirePie';
import BarChart from './components/BarChart';
import LineTwo from './components/LineTwo';
import LineThree from './components/LineThree';
import LineFour from './components/LineFour';
import Loading from '@/components/Loading';
import { createGroupDataModel } from './model';
export default {
  name: 'RequirementChart',
  components: {
    BarChart,
    AllRequirtPie,
    LineTwo,
    LineThree,
    LineFour,
    Loading
  },
  data() {
    return {
      allNames: [],
      groupNames: [],
      partNames: [],
      dataTwo: [],
      dataThr: [],
      groupIds: [],
      partIds: [],
      allIds: [],
      groupdemandAmt: [],
      partdemandAmt: [],
      businessTotal: '',
      techTotal: '',
      demandsIngData: [],
      //所有需求饼状图
      AllRequiredata: [],
      loading: false,
      internetGroupData: createGroupDataModel(),
      queryparam: {
        datetype: '',
        delayNum: '',
        demandType: '',
        descending: false,
        designState: '',
        featureNum: '',
        featureType: '',
        groupState: '',
        groupid: [],
        index: 1,
        keyword: '',
        priority: '',
        relDateType: [],
        relEndDate: '',
        relStartDate: '',
        relevant: false,
        size: 100000,
        sortBy: '',
        stateNum: '',
        states: [],
        userid: ''
      },
      requireDataDetail: ['250', '200', '100', '4.5/1'],
      //进行中需求柱状图
      doingRequireOpt: {
        xAxisData: ['预评估', '待实施', '开发中', '业测', '准生产'],
        series: []
      }
    };
  },
  computed: {
    ...mapState('dashboard', [
      'statis',
      'groupRqrmnt',
      'ImpingDemands',
      'GroupPartsIds'
    ]),
    ...mapState('userForm', {
      groups: 'groups',
      groupsAll: 'groupsAll'
    })
  },
  methods: {
    ...mapActions('dashboard', [
      'queryStatis',
      'queryImpingDemandDashboard',
      'queryIntGroupId'
    ]),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('rqrChartForm', {
      queryAllDemands: 'queryAllDemands'
    }),
    ...mapActions('userForm', {
      queryGroupPeople: 'queryGroupPeople',
      fetchGroupAll: 'fetchGroupAll',
      fetchGroup: 'fetchGroup'
    }),
    async queryRqrmt(type) {
      await this.queryIntGroupId();
      this.groupIds = this.GroupPartsIds.groupIds;
      this.partIds = this.GroupPartsIds.partIds;
      this.allIds = [this.groupIds, this.partIds];

      await this.queryImpingDemandDashboard();

      this.demandsIngData = this.ImpingDemands.intImpingDemand;
      this.demandsIngData.forEach(item => {
        if (item.demandType == 'business') {
          this.businessTotal = item.total;
        } else if (item.demandType == 'tech') {
          this.techTotal = item.total;
        }
      });
      this.AllRequiredata = [
        { name: '科技需求数', value: this.techTotal },
        { name: '业务需求数', value: this.businessTotal }
      ];

      let businessArr = this.demandsIngData.filter(item => {
        return item.demandType == 'business';
      })[0];
      let techArr = this.demandsIngData.filter(item => {
        return item.demandType == 'tech';
      })[0];
      this.doingRequireOpt.series = [
        [
          businessArr.preEvaluat,
          businessArr.toImp,
          businessArr.developing,
          businessArr.busiTest,
          businessArr.staging
        ],
        [
          techArr.preEvaluat,
          techArr.toImp,
          techArr.developing,
          techArr.busiTest,
          techArr.staging
        ]
      ];

      this.dataTwo = this.ImpingDemands.preGroupImpDemand;
      this.dataThr = this.ImpingDemands.retailImpDemand;

      let groupdemandAmt = [];
      let partdemandAmt = [];
      this.groupNames = [];
      this.partNames = [];
      this.groupIds.forEach(item => {
        this.ImpingDemands.preGroupImpDemand.forEach(val => {
          if (val.groupId == item) {
            this.groupNames.push(val.groupName);
            groupdemandAmt.push(val.demandAmt);
          }
        });
      });
      this.partIds.forEach(item => {
        this.ImpingDemands.retailImpDemand.forEach(val => {
          if (val.groupId == item) {
            this.partNames.push(val.groupName);
            partdemandAmt.push(val.demandAmt);
          }
        });
      });
      this.groupdemandAmt = groupdemandAmt;
      this.partdemandAmt = partdemandAmt;
      this.allNames = [this.groupNames, this.partNames];
    }
  },
  async created() {
    this.loading = true;
    try {
      await this.queryRqrmt();
    } finally {
      this.loading = false;
    }
  }
};
</script>
<style scoped lang="stylus">
.scroll

.title
  font-family PingFangSC-Medium
  font-weight 500
  font-size 24px
  color #394853
  letter-spacing 0
  margin-left 37px
  margin-top 15px
.requirement-num-title
  white-space nowrap
  font-family PingFangSC-Regular
  font-weight: 400
  font-size 20px
  color #394853
  letter-spacing 0
  margin-bottom 11px
.requirement-num
  white-space nowrap
  font-family PingFangSC-Medium
  font-weight 500
  font-size 48px
  letter-spacing 0
.lineOne-block
  width 40.333333vw
  height 486px
.lineOne-block:last-child
  margin-left 30px
.doing-require-bar
  margin-left 83px
  margin-top 21px
.one-line-block
  margin 30px 0
  width 82.4375vw
  padding-top 36px
</style>
