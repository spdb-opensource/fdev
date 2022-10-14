<template>
  <div class="container">
    <!-- 主体 -->
    <div class="rqrHeader">
      <div
        class="rqrBack"
        :style="{
          backgroundImage: getPrioritySrc(demandInfoDetail.demand_status_normal)
        }"
      >
        <div class="rqrleft ">
          <div>
            <div class="maxWidth ellipsis-2-lines">
              <span class="rqrName">
                需求名称：
                <span
                  class="rqrName "
                  :title="
                    demandInfoDetail.oa_contact_name
                      ? demandInfoDetail.oa_contact_name
                      : ''
                  "
                  >{{ demandInfoDetail.oa_contact_name }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ demandInfoDetail.oa_contact_name }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </span>
              </span>
            </div>
            <div class="maxWidth ellipsis-2-lines">
              <span class="rqrNum row items-center">
                <span>需求编号：</span>
                <span
                  class="rqrNum "
                  :title="
                    demandInfoDetail.oa_contact_no
                      ? demandInfoDetail.oa_contact_no
                      : ''
                  "
                  >{{ demandInfoDetail.oa_contact_no }}</span
                >
                <f-icon
                  name="help_c_o"
                  class="helpIcon text-primary cursor-pointer"
                  @click="helpDailog()"
                />
              </span>
            </div>
          </div>
        </div>
        <div class="rqrRight">
          <img
            class="statusLogo"
            :src="getStatusSrc(demandInfoDetail.demand_status_normal)"
            alt=""
          />
          <div class="">
            <div class="demand-status-normal">
              {{ filterdemandStatus(demandInfoDetail.demand_status_normal) }}
            </div>
            <span class="demand-status-normal-caption">需求状态</span>
          </div>
          <div class="statusType">
            <div class="up">
              {{ demandInfoDetail.demand_type | filterDemandType }}
            </div>
            <span class="down">需求类型</span>
          </div>
          <f-image
            v-if="demandInfoDetail.priority == '0'"
            name="priority_high_font"
            class="statusPriorityImg"
          />
          <f-image
            v-else-if="demandInfoDetail.priority == '1'"
            name="priority_medium_font"
            class="statusPriorityImg"
          />
          <f-image
            v-else-if="demandInfoDetail.priority == '2'"
            name="priority_normal_font"
            class="statusPriorityImg"
          />
          <f-image
            v-else-if="demandInfoDetail.priority == '3'"
            name="priority_low_font"
            class="statusPriorityImg"
          />
        </div>
      </div>
    </div>
    <div :class="tabs === 'demandInfo' ? 'rqrContent2' : 'rqrContent'">
      <div class="row">
        <fdev-tabs v-model="tabs" @input="switchFun($event)">
          <div class="row" v-for="(item, i) in tabsList" :key="i">
            <fdev-tab :name="item.name" :label="item.label" />
          </div>
        </fdev-tabs>
        <fdev-space />
        <!-- 按钮 -->
        <demandDetailOpts
          v-if="demandInfoDetail && demandInfoDetail.id"
          ref="demandDetailOpts"
          :tabs="tabs"
          :demandDetailFromIndex="demandInfoDetail"
          @goToFileFun="goToFileFun"
          @goTodevelopNoFun="goTodevelopNoFun"
          @updateHeadAndTable="updateHeadAndTable"
        />
      </div>
      <fdev-separator class="separator" />
    </div>
    <div v-if="displayUtilFun()" class="content-box">
      <component
        ref="demandContent"
        @init="init"
        :is="currentTabComponent"
        :params="demandInfoDetail"
      >
      </component>
    </div>
    <f-dialog right v-model="helpFlag" title="需求详情帮助">
      <div>
        <img src="@/assets/demand-help.jpg" alt="" />
      </div>
    </f-dialog>
    <f-dialog v-model="unitCheck" title="实施单元核算">
      <div>
        请前往<a href="#" @click="goIpmp">IPMP</a>发起{{ checkImplUnitNum }}核算
      </div>
    </f-dialog>
  </div>
</template>
<script>
// 引入tab总入口组件
import demandInfo from '@/modules/Rqr/views/demandDetail/components/demandInfo'; // 基础信息
import otherDemand from '@/modules/Rqr/views/demandDetail/components/otherDemand'; // 日常需求信息
import unit from '@/modules/Rqr/views/demandDetail/components/otherDemand'; //实施单元
import developNo from '@/modules/Rqr/views/demandDetail/components/developNo'; // 研发单元
import task from '@/modules/Rqr/views/demandDetail/components/task'; // 任务
import file from '@/modules/Rqr/views/demandDetail/components/file'; // 文档库

// 引入需求tab栏
import { tabsList } from '@/modules/Rqr/utils/constants.js';
// 引入方法
import { queryDemandInfoDetail } from '@/modules/Rqr/services/methods.js';
// 引入  编辑 评估  撤销  操作类 组件
import demandDetailOpts from '@/modules/Rqr/views/demandDetail/demandDetailOpts.vue';
import { demandStatusSpecial } from '@/modules/Rqr/model';

import { queryIpmpUnitIsCheck } from '@/modules/Rqr/services/methods.js';

export default {
  components: {
    demandDetailOpts,
    demandInfo,
    unit,
    otherDemand,
    developNo,
    task,
    file
  },
  name: 'demandDetail',
  data() {
    return {
      loading: false, // 等待层
      demandType: '', // 需求类型 tech--科技需求；business--业务需求；daily--日常需求；
      tabs: 'demandInfo', // 默认tabs
      tabsList: tabsList, // tab集合
      currentTabComponent: '', // 默认组件
      demandInfoDetail: {}, // 需求详情
      helpFlag: false,
      unitCheck: false,
      checkImplUnitNum: '',
      checkUrl: ''
    };
  },
  filters: {
    filterDemandType(val) {
      const obj = {
        business: '业务需求',
        tech: '科技需求',
        daily: '日常需求'
      };
      return obj[val];
    }
  },
  created() {
    // 调试勿删 后续删除
    this.init();
  },
  methods: {
    async updateHeadAndTable() {
      // 操作按钮操作成功  更新 头部信息
      const { id } = this.getRouteParamsFun();
      this.demandInfoDetail = await queryDemandInfoDetail({ id });
      // 更新研发单元列表
      // 执行子组件 里面的方法
      // console.log('父组件调用子组件 init方法');
      this.$refs.demandContent.$children[0].$children[0].init();
    },
    helpDailog() {
      this.helpFlag = true;
    },
    filterdemandStatus(val) {
      if (
        this.demandInfoDetail.demand_status_special != 1 &&
        this.demandInfoDetail.demand_status_special != 2
      ) {
        if (val == '0') {
          return '预评估';
        } else if (val == '1') {
          return '评估中';
        } else if (val == '2') {
          return '待实施';
        } else if (val == '3') {
          if (this.demandInfoDetail.demand_type === 'daily') {
            return '进行中';
          } else {
            return '开发中';
          }
        } else if (val == '4') {
          return 'sit';
        } else if (val == '5') {
          return 'uat';
        } else if (val == '6') {
          return 'rel';
        } else if (val == '7') {
          if (this.demandInfoDetail.demand_type === 'daily') {
            return '已完成';
          } else {
            return '已投产';
          }
        } else if (val == '8') {
          return '已归档';
        } else if (val == '9') {
          return '已撤销';
        } else {
          return '';
        }
      } else {
        return (
          demandStatusSpecial[this.demandInfoDetail.demand_status_special] || ''
        );
      }
    },
    // 初始化操作总入口
    async init(type) {
      const { id, tab } = this.getRouteParamsFun();
      try {
        this.loading = true;
        this.demandInfoDetail = await queryDemandInfoDetail({ id });
        this.demandType = this.demandInfoDetail.demand_type;
      } catch (e) {
        throw new Error(e);
      } finally {
        this.loading = false;
      }
      // 研发单元 增删改查  刷新  操作按钮组件
      if (type == 'refresh') {
        this.$refs.demandDetailOpts.initOpt();
      } else {
        this.composeTabListFun(id, tab);
      }
    },
    // 获取路由参数
    getRouteParamsFun() {
      return { ...this.$route.params, ...this.$route.query };
    },
    // tabsList数据操作总入口（科技/业务需求 ----- 日常需求）
    composeTabListFun(id, tab) {
      this.currentTabComponent = this.defaultComponentsFun(id, tab);
      this.tabsList = this.filterTabFun();
      this.addComponentsFun(this.componentsInfoFun());
    },
    // 获取默认组件/默认tab
    defaultComponentsFun(id, tab) {
      this.$once('hook:beforeDestroy', () => {
        this.memoryTabInfoFun(id);
      });
      return (this.tabs = tab || this.getTabsFun(id));
    },
    // tabs缓存本地
    memoryTabInfoFun(id) {
      localStorage.setItem(
        'demand-tab',
        JSON.stringify({ id, tab: this.tabs })
      );
    },
    // 先从缓存获取没有缓存在取data
    getTabsFun(id) {
      let memory = JSON.parse(localStorage.getItem('demand-tab'));
      if (memory && memory.id === id) return memory.tab;
      return this.tabs;
    },
    // 根据需求类型tab显示其他需求任务 还是 实施单元
    filterTabFun() {
      return this.demandType === 'daily'
        ? this.tabsList.filter(e => e.name !== 'unit')
        : this.tabsList.filter(e => e.name !== 'otherDemand');
    },
    // 总入口组件信息
    componentsInfoFun() {
      return ['demandInfo', 'otherDemand', 'developNo', 'task', 'file'];
    },
    // 添加组件信息
    addComponentsFun(list) {
      for (let i in this.tabsList) {
        this.$set(this.tabsList[i], 'components', list[i]);
      }
    },
    // 切换tab修改对应组件
    switchFun(v) {
      this.currentTabComponent = this.tabsList.filter(
        e => e.name === v
      )[0].components;
    },
    // 取消子组件需要监听props
    displayUtilFun() {
      return JSON.stringify(this.demandInfoDetail) !== '{}';
    },
    goTodevelopNoFun() {
      // 切换到研发单元
      this.tabs = 'developNo';
      this.switchFun(this.tabs);
    },
    //切换到文档库
    goToFileFun() {
      this.tabs = 'file';
      this.switchFun(this.tabs);
    },
    getStatusSrc(status) {
      if (status) {
        try {
          return require(`@/modules/Rqr/assets/status${status}.svg`);
        } catch (error) {
          return require(`@/modules/Rqr/assets/status12.svg`);
        }
      } else {
        return require(`@/modules/Rqr/assets/status12.svg`);
      }
    },
    getPrioritySrc(status) {
      const pData = [
        `url(${require('@/modules/Rqr/assets/status12bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status12bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status2bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status3bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status12bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status12bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status12bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status7bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status8bj.svg')})`,
        `url(${require('@/modules/Rqr/assets/status9bj.svg')})`
      ];
      if (status) {
        try {
          let index = parseInt(status);
          return pData[index];
        } catch (error) {
          return `url(${require('@/modules/Rqr/assets/status12bj.svg')})`;
        }
      } else {
        return `url(${require('@/modules/Rqr/assets/status12bj.svg')})`;
      }
    },
    goIpmp() {
      window.open(this.checkUrl);
    }
  },
  async mounted() {
    const { id } = this.getRouteParamsFun();
    let demandInfo = await queryDemandInfoDetail({ id });
    //查询需求下当前登录人是否有牵头的实施单元未核算
    let respone = await queryIpmpUnitIsCheck({
      informationNum: demandInfo.oa_contact_no
    });
    this.unitCheck = respone.implUnitNum ? true : false;
    this.checkImplUnitNum = respone.implUnitNum;
    this.checkUrl = respone.url;
  }
};
</script>
<style lang="stylus" scoped>
.wrap
    word-break break-all
.mr-30
  margin-right 30px
.flat-btn
  height 16px
  line-height 16px
  >>>
    .q-btn__wrapper
      padding 0
      min-height 0
    .on-left
      margin-right 0
.separator
  margin 0
  background-color alpha(#ECEFF1, 1)
>>> .q-tab
  outline none
.rqrHeader{
  background: #FFFFFF;
  border-radius: 8px;
  margin-bottom:10px;
}
.rqrHeader .rqrBack{
  padding: 0  20px 0 33px;
  min-height: 90px;
  width: 100%;
  border-radius: 8px;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: left center;
  display:flex;
  justify-content: space-between;
  .rqrleft{
    display: flex;
    align-items: center;
    .helpIcon{
      margin-left: 4px;
      width: 16px;
      height: 16px;
    }
    .rqrName{
      font-size: 20px;
      color: #333333;
      letter-spacing: 0;
      line-height: 30px;
      font-weight: 400;
    }
    .rqrNum{
      font-family: PingFangSC-Regular;
      font-size: 12px;
      color: #999999;
      letter-spacing: 0;
      line-height: 20px;
    }
    .maxWidth{
      max-width: 600px;
    }
  }
  .rqrRight{
    display:flex;
    align-items: center;
    width: 381px;
    justify-content: flex-end;
    .statusLogo{
      width: 32px;
      height: 32px;
      margin-right: 16px;
    }
    .statusName{
      font-size: 18px;
      font-weight: 600;
      color: #333333;
      letter-spacing: 0;
      line-height: 28px;
    }
    .statusType{
      margin-left: 49px;
      height: 48px;
      .up{
        font-size: 18px;
        font-weight: 600;
        color: #333333;
        letter-spacing: 0;
        line-height: 28px;
      }
      .down{
        font-family: PingFangSC-Regular;
        font-size: 12px;
        color: #999999;
        letter-spacing: 0;
        line-height: 20px;
      }
    }
    .statusPriorityImg{
        margin-left: 30px;
        width: 92px;
        height: 24px;
    }
  }
}
.rqrContent
  height 87px
  padding 20px 32px 0px 46px
  background: #fff;
  border-top-right-radius 8px
  border-top-left-radius 8px
.rqrContent2
  height 87px
  padding 20px 32px 0px 46px
  background: #fff;
  border-top-right-radius 8px
  border-top-left-radius 8px
  border-bottom-right-radius 8px
  border-bottom-left-radius 8px
.content-box
  .fdev-block
      border-top-right-radius 0
      border-top-left-radius 0
      padding-top 6px//第一版 4px 第二版6px
.demand-status-normal
  font-size: 18px;
  font-weight: 600;
  color: #333333;
  letter-spacing: 0;
  line-height: 28px;
.demand-status-normal-caption
  font-family: PingFangSC-Regular;
  font-size: 12px;
  color: #999;
  letter-spacing: 0;
  line-height: 20px;
.content-box  >>> .my-sticky-column-table /deep/ .q-table__top
  margin-bottom: 19.5px !important;
>>> .q-separator
  transform translateY(-6px)
</style>
