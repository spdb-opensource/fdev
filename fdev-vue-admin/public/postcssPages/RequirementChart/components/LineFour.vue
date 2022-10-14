<template>
  <fdev-card class="flex row no-wrap" square flat>
    <div>
      <span class="title">
        互联网条线已投产需求总量
      </span>
      <FourGroupShow :groupData="everyGroupList" class="pie" />
    </div>
    <div class="flex column tables">
      <div class="row no-warp justify-end">
        <f-formitem label="起始时间:" page>
          <f-date clearable v-model="startDate" class="q-mt-md" />
        </f-formitem>
        <f-formitem label="截止时间:" page>
          <f-date clearable v-model="endDate" class="q-mt-md" />
        </f-formitem>
        <fdev-btn class="q-ml-lg" label="查询" normal @click="query" />
      </div>
      <div class="row self-end items-center"></div>
      <span class="table-title">各组已投产需求数</span>
      <table border="1" class="table">
        <tr class="table-line-one">
          <td></td>
          <td
            style="width:175px"
            v-for="(group, key) in allNames[0]"
            :key="key"
          >
            {{ group }}
          </td>
        </tr>
        <tr class="table-line-two">
          <td class="table-num-td">数量</td>
          <td v-for="(item, index) in everyGroupList" :key="index">
            {{ item ? item : '0' }}
          </td>
        </tr>
      </table>

      <span class="table-title">零售组各板块已投产需求数</span>
      <table border="1" class="table">
        <tr class="table-line-one">
          <td></td>
          <td v-for="(group, key) in allNames[1]" :key="key">
            {{ group }}
          </td>
        </tr>
        <tr class="table-line-two">
          <td class="table-num-td">数量</td>
          <td v-for="(item, index) in everyPartList" :key="index">
            {{ item }}
          </td>
        </tr>
      </table>
    </div>
  </fdev-card>
</template>
<script>
import FourGroupShow from './FourGroupShow';
import { mapState, mapActions } from 'vuex';

export default {
  name: 'LineFour',
  components: {
    FourGroupShow
  },
  props: {
    allNames: {
      type: Array
    },
    ids: {
      type: Array
    }
  },
  data() {
    return {
      everyGroupList: [],
      everyPartList: [],
      //（初始化）起始时间
      startDate: (() => {
        let now = new Date();
        now.setMonth(now.getMonth() - 3);
        return this.initDate(now);
      })(),
      //（初始化）截止时间
      endDate: (() => {
        return this.initDate(new Date());
      })()
    };
  },
  computed: {
    ...mapState('rqrChartForm', ['allDemands']),
    ...mapState('dashboard', ['EndDemands'])
  },
  watch: {},
  methods: {
    ...mapActions('dashboard', ['queryEndDemandDashboard']),
    query() {
      this.init();
    },
    //初始化日期
    initDate(now) {
      let month =
        now.getMonth() < 9 ? '0' + (now.getMonth() + 1) : now.getMonth() + 1;
      let date = now.getDate() < 10 ? '0' + now.getDate() : now.getDate();
      return now.getFullYear() + '/' + month + '/' + date;
    },
    //选择符合日期的需求
    selectDemand(demand) {
      let date = new Date(demand.real_product_date);
      let startDatefmt = new Date(this.startDate.replace(/\//g, '-'));
      let endDatefmt = new Date(this.endDate.replace(/\//g, '-'));
      return date - startDatefmt >= 0 && endDatefmt - date >= 0 ? true : false;
    },
    async init() {
      this.everyGroupList = [];
      this.everyPartList = [];
      await this.queryEndDemandDashboard({
        startDate: this.startDate,
        endDate: this.endDate
      });
      let newArr = this.EndDemands.queryGroupDemand;
      this.ids[0].forEach(item => {
        newArr.forEach(val => {
          if (val.groupId == item) {
            this.everyGroupList.push(val.demandAmt);
          }
        });
      });

      let newArr2 = this.EndDemands.queryPartDemand;
      this.ids[1].forEach(item => {
        newArr2.forEach(val => {
          if (val.groupId == item) {
            this.everyPartList.push(val.demandAmt);
          }
        });
      });
    }
  },
  async mounted() {
    await this.init();
  }
};
</script>
<style scoped lang="stylus">
.pie
  margin-top 12px
  margin-left 19px
.title
  margin-left 38px
  font-family PingFangSC
  font-weight 500
  font-size 24px
  color #394853
  letter-spacing 0
.time-label
  color #394853
  font-family PingFangSC
  font-weight 400
  letter-spacing 0
  font-size 15px
.time-input
  width 193px
  margin-left 15px
  padding 0
.tables
  color #394853
  font-family PingFangSC
  font-weight 400
  letter-spacing 0
  margin-left 40px
.table-title
  font-size 20px
  letter-spacing 0
  margin-bottom 15px
.table
  width: 1064px
  text-align center
  margin-bottom 30px
  border-collapse collapse
  border-top none !important
  td
    border 1px solid rgba(0,0,0,0.10)
    border-top none !important
.table-line-one
  background-color #E9F5FF
  font-size 14px
  height 34px
  td
    border-bottom  none !important
.table-line-two
  font-size 18px
  height 38px
.table-num-td
  width 66px
  font-size 15px
.self-end label
  padding: 0 12px
  border: 1px solid #ccc
  border-radius: 4px
  box-sizing: content-box
.btn
  margin-left: 28px
.q-field--with-bottom,
.q-field
  margin: 0
  padding: 0
>>> .label
  flex-direction: row-reverse
</style>
