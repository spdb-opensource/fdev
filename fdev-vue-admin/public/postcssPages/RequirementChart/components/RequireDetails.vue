<template>
  <div class="flex column details">
    <span class="group-name">{{ this.plateDetailName }}</span>
    <div class="flex row no-wrap require-table">
      <div v-for="i in 3" :key="i" class="flex row items-start col-3 no-wrap">
        <div class="flex column items-start">
          <span class="require-tab">{{ requireTab[i - 1] }}</span>
          <span class="require-data">{{ requireData[i - 1] }} </span>
        </div>
        <div class="devide-line-short"></div>
      </div>
      <div class="flex column items-start col-3">
        <span class="require-tab">{{ requireTab[3] }}</span>
        <span class="require-data">{{ requireData[3] }} </span>
      </div>
    </div>
    <div class="flex row no-wrap items-start">
      <BarChart barRef="groupRequireDetail" :barOpt="groupRequireDetailOpt" />
      <div class="devide-line-long self-end"></div>
      <div class="flex column self-start">
        <span class="group-name">{{ group }}人员情况</span>
        <div class="people-title flex row no-wrap justify-between">
          <span>人数</span>
          <span>占比</span>
        </div>
        <div
          class="flex column items-start"
          v-for="i in 3"
          :key="i"
          :style="{ 'margin-bottom': '15px' }"
        >
          <span class="people-tab">{{ peopleTab[i - 1] }}</span>
          <div class="people-data flex row no-wrap justify-between">
            <span>{{ peopleData[i - 1].num }}人</span
            ><span>{{ peopleData[i - 1].percent }}%</span>
          </div>
        </div>
        <div class="flex column items-start">
          <span class="people-tab">{{ peopleTab[3] }}</span>
          <div class="people-data flex row no-wrap justify-between">
            <span>{{ peopleData[3].num }}人</span
            ><span>{{ peopleData[3].percent }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import BarChart from './BarChart';
import { mapState, mapGetters } from 'vuex';
import { getGroupFullName } from '@/utils/utils';

export default {
  name: 'RequireDetails',
  components: {
    BarChart
  },
  props: {
    dataDetail: {
      type: Object
    },
    group: String
  },
  data() {
    return {
      requireTab: ['需求数', '紧急需求数', '开发人员数量', '人员负荷'],
      peopleTab: ['行内开发', '行内测试', '行外开发', '行外测试'],
      groupInfo: {}
    };
  },
  methods: {},
  computed: {
    ...mapState('dashboard', ['statis']),
    ...mapState('jobForm', ['rqrmntsList']),
    ...mapState('user', {
      userList: 'list'
    }),
    ...mapState('userForm', {
      groups: 'groups',
      groupsAll: 'groupsAll'
    }),
    ...mapState('rqrChartForm', ['allDemands']),
    ...mapGetters('rqrChartForm', ['publishedDemands']),
    plateDetailName() {
      return this.group;
    },
    userGroupFullNameList() {
      return this.userList.map(user => {
        let fullName = getGroupFullName(this.groups, user.group.id);
        return {
          ...user,
          fullName
        };
      });
    },
    //需求数
    requireData() {
      return [
        this.dataDetail.demandAmt,
        this.dataDetail.urgAmt,
        this.dataDetail.coderAmt,
        this.dataDetail.staffLoad
      ];
    },
    //人员数据
    peopleData() {
      return [
        {
          num: this.dataDetail.intDev,
          percent: (Number(this.dataDetail.intDevProp) * 100).toFixed(2)
        },
        {
          num: this.dataDetail.intTest,
          percent: (Number(this.dataDetail.intTestProp) * 100).toFixed(2)
        },
        {
          num: this.dataDetail.extDev,
          percent: (Number(this.dataDetail.extDevProp) * 100).toFixed(2)
        },
        {
          num: this.dataDetail.extTest,
          percent: (Number(this.dataDetail.extTestProp) * 100).toFixed(2)
        }
      ];
    },
    //柱状图数据
    groupRequireDetailOpt() {
      let list = [
        [
          this.dataDetail.busiPreEvaluateDemand,
          this.dataDetail.busiToImpDemand,
          this.dataDetail.busiDevelopingDemand,
          this.dataDetail.busiBusiDemand,
          this.dataDetail.busiStagingDemand
        ],
        [
          this.dataDetail.kjPreEvaluateDemand,
          this.dataDetail.kjDToImpDemand,
          this.dataDetail.kjDevelopingDemand,
          this.dataDetail.kjBusiTestDemand,
          this.dataDetail.kjStagingDemand
        ]
      ];
      return {
        title: {
          text: this.plateDetailName + '需求详情'
        },
        xAxisData: ['预评估', '待实施', '开发中', '业测', '准生产'],
        series: list
      };
    }
  }
};
</script>
<style scoped lang="stylus">
.details
  margin-top 32px
  margin-bottom 38px
.group-name
  font-family PingFangSC
  font-weight 400
  font-size 20px
  color #394853
  letter-spacing 0
  margin-bottom 15px
.require-table
  margin-bottom 53px
.devide-line-short
  height 100%
  width 1px
  background-color rgba(0,0,0,0.25)
  margin-left 50px
.devide-line-long
  height 243px
  width 1px
  background-color rgba(0,0,0,0.25)
  margin-left 75px
  margin-right 75px
  margin-bottom 30px
.require-tab
  font-family PingFangSC
  font-weight 500
  font-size 20px
  color #B0B6C3
  letter-spacing 0
.require-data
  font-family PingFangSC
  font-weight 400
  font-size 40px
  color #394853
  letter-spacing 0
  width 75px
.people-title
  font-family PingFangSC
  font-weight 400
  font-size 18px
  color #8F97AA
  width 183px
  margin-bottom 11px
.people-tab
  font-family PingFangSC
  font-weight 400
  font-size: 18px
  color #394853
.people-data
  font-family PingFangSC
  font-weight 400
  font-size 20px
  color #3AA0FF
  width 185px
</style>
