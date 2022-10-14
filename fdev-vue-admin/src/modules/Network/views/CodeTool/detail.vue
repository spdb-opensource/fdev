<template>
  <div class="container">
    <!-- 主体 -->
    <div class="rqrHeader">
      <div
        class="rqrleft"
        :style="{
          background: getOrderStatusCor(ordersDetail.orderStatus)
        }"
      ></div>
      <div class="rqrcenter ">
        <div>
          <div class="maxWidth ellipsis">
            <span
              class="rqrName "
              :title="ordersDetail.orderNo ? ordersDetail.orderNo : ''"
              >{{ ordersDetail.orderNo }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ ordersDetail.orderNo }}
                </fdev-banner>
              </fdev-popup-proxy>
            </span>
          </div>
          <div class="maxWidth ellipsis-2-lines">
            <span class="rqrNum row items-center">
              <span
                class="rqrNum "
                :title="ordersDetail.demandName ? ordersDetail.demandName : ''"
                >{{ ordersDetail.demandName }}</span
              >
            </span>
          </div>
        </div>
      </div>
      <div class="rqrRight">
        <img
          class="statusLogo"
          :src="getOrderStatusLogo(ordersDetail.orderStatus)"
          alt=""
        />
        <div>
          <div class="demand-status-normal">
            {{ getOrderName(ordersDetail.orderStatus) }}
          </div>
          <span class="demand-status-normal-caption">工单状态</span>
        </div>
      </div>
    </div>
    <div
      :class="
        tabs === 'baseInfo' || tabs === 'netApproval'
          ? 'rqrContent2'
          : 'rqrContent'
      "
    >
      <div class="row items-center" style="margin-left: 14px;">
        <fdev-tabs class="orderTabs" v-model="tabs" @input="switchFun($event)">
          <div class="row" v-for="(item, i) in tabsList" :key="i">
            <fdev-tab :name="item.name" :label="item.label" />
          </div>
        </fdev-tabs>
        <fdev-space />
        <!-- 按钮 -->
        <div>
          <fdev-btn
            dialog
            v-if="applyRecheckButton == '0'"
            class="q-ml-md"
            ficon="recheck_apply"
            label="申请复审"
            @click="openRecheckDlg()"
          />
          <fdev-btn
            dialog
            v-if="recheckRemindButton == '0'"
            class="q-ml-md"
            ficon="recheck_alert"
            label="代码复审提醒"
            @click="recheckRemindClick()"
          />
          <span>
            <fdev-tooltip
              v-if="isDisableBtn(ordersDetail, 1)"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ getErrorMsg(ordersDetail, 1) }}</span>
            </fdev-tooltip>
            <fdev-btn
              dialog
              :disable="isDisableBtn(ordersDetail, 1)"
              class="q-ml-md"
              ficon="edit"
              label="编辑"
              @click="editHandleDialogOpen()"
            />
          </span>
          <span>
            <fdev-tooltip
              v-if="isDisableBtn(ordersDetail, 2)"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ getErrorMsg(ordersDetail, 2) }}</span>
            </fdev-tooltip>
            <fdev-btn
              :disable="isDisableBtn(ordersDetail, 2)"
              dialog
              class="q-ml-md"
              ficon="delete_o"
              label="删除"
              @click="delHandleDialogOpen()"
          /></span>
        </div>
      </div>
      <fdev-separator class="separator" />
    </div>
    <div v-if="displayUtilFun()" class="content-box">
      <component
        ref="demandContent"
        @init="init"
        :is="currentTabComponent"
        :params="ordersDetail"
      >
      </component>
    </div>
    <f-dialog title="申请信息" right v-model="openCheckDlg">
      <div class="q-gutter-y-lg">
        <f-formitem label="复审内容" bottom-page required>
          <fdev-input
            ref="createModel.recheckContent"
            type="textarea"
            placeholder="请输入复审内容"
            v-model="$v.createModel.recheckContent.$model"
            :rules="[
              () => $v.createModel.recheckContent.required || '请输入复审内容',
              () =>
                $v.createModel.recheckContent.maxLen || '输入内容不能超过400字'
            ]"
          />
        </f-formitem>
      </div>
      <template #btnSlot>
        <fdev-btn label="取消" outline dialog @click="openCheckDlg = false"/>
        <fdev-btn label="确认" dialog @click="recheckClick"
      /></template>
    </f-dialog>
  </div>
</template>
<script>
// 引入tab总入口组件
import baseInfo from '@/modules/Network/views/CodeTool/components/codeInfo'; // 工单信息
import netApproval from '@/modules/Network/views/CodeTool/components/netApproval'; // 审核记录
import file from '@/modules/Network/views/CodeTool/components/file'; // 文档库
import logs from '@/modules/Network/views/CodeTool/components/logs'; // 日志
// 引入需求tab栏
import { tabsList, statusOptions } from '@/modules/Network/utils/constants.js';
// 引入方法
import {
  queryOrderById,
  deleteOrderById,
  recheckRemind,
  applyRecheck
} from '@/modules/Network/services/methods.js';
import {
  resolveResponseError,
  successNotify,
  errorNotify,
  validate
} from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';

export default {
  components: {
    baseInfo,
    netApproval,
    logs,
    file
  },
  name: 'demandDetail',
  data() {
    return {
      loading: false, // 等待层
      tabs: this.getDefTabs(), // 默认tabs
      tabsList: tabsList, // tab集合
      currentTabComponent: '', // 默认组件
      ordersDetail: {}, // 工单详情
      checkImplUnitNum: '',
      checkUrl: '',
      orderStatusOptions: statusOptions, //工单状态
      openCheckDlg: false,
      createModel: {
        recheckContent: '' //复审内容
      },
      applyRecheckButton: 1,
      recheckRemindButton: 1
    };
  },
  validations: {
    createModel: {
      recheckContent: {
        required,
        maxLen(val) {
          return val.length <= 400;
        }
      }
    }
  },
  filters: {},
  created() {
    // 调试勿删 后续删除
    this.init();
  },
  methods: {
    async updateHeadAndTable() {
      // 操作按钮操作成功  更新 头部信息
      const { id } = this.getRouteParamsFun();
      await this.getDetail(id);
      // 更新研发单元列表
      // 执行子组件 里面的方法
      // console.log('父组件调用子组件 init方法');
      this.$refs.demandContent.$children[0].$children[0].init();
    },
    async getDetail(id) {
      this.ordersDetail = await queryOrderById({ id });
      if (this.ordersDetail.applyRecheckButton == '0') {
        this.applyRecheckButton = 0;
      } else {
        this.applyRecheckButton = 1;
      }
      if (this.ordersDetail.recheckRemindButton == '0') {
        this.recheckRemindButton = 0;
      } else {
        this.recheckRemindButton = 1;
      }
    },
    // 初始化操作总入口
    async init(type) {
      const { id, tab } = this.getRouteParamsFun();
      try {
        this.loading = true;
        await this.getDetail(id);
      } catch (e) {
        throw new Error(e);
      } finally {
        this.loading = false;
      }
      this.composeTabListFun(id, tab);
    },
    // 获取路由参数
    getRouteParamsFun() {
      return { ...this.$route.params, ...this.$route.query };
    },
    // tabsList数据操作总入口
    composeTabListFun(id, tab) {
      this.currentTabComponent = this.getDefTabs();
      this.addComponentsFun();
    },
    // tabs缓存本地
    memoryTabInfoFun() {
      localStorage.setItem('codetool-tab', this.tabs);
    },
    // 先从缓存获取没有缓存在取data
    getDefTabs() {
      const tab = localStorage.getItem('codetool-tab');
      if (tab) return tab;
      else return 'baseInfo';
    },
    // 添加组件信息
    addComponentsFun() {
      let list = ['baseInfo', 'netApproval', 'file', 'logs'];
      for (let i in this.tabsList) {
        this.$set(this.tabsList[i], 'components', list[i]);
      }
    },
    // 切换tab修改对应组件
    switchFun(v) {
      this.currentTabComponent = this.tabsList.filter(
        e => e.name === v
      )[0].components;
      this.memoryTabInfoFun();
    },
    // 取消子组件需要监听props
    displayUtilFun() {
      return JSON.stringify(this.ordersDetail) !== '{}';
    },
    getOrderStatusLogo(status) {
      if (status) {
        try {
          if (status == '6' || status == '7') status = '5';
          else if (status == '3' || status == '4') status = '2';
          return require(`@/modules/Network/assets/orderstaus/orderstatus${status}.svg`);
        } catch (error) {
          return require(`@/modules/Network/assets/orderstaus/orderstatus1.svg`);
        }
      } else {
        return require(`@/modules/Network/assets/orderstaus/orderstatus1.svg`);
      }
    },
    getOrderStatusCor(status) {
      if (status == '1') {
        return '#FEC400';
      } else if (status == '2') {
        return '#02E01A';
      } else if (status == '3') {
        //return '#4DBB59';
        return '#02E01A';
      } else if (status == '4') {
        //return '#00830E';
        return '#02E01A';
      } else if (status == '5') {
        return '#42A5F5';
        //return '#24C8F9';
      } else if (status == '6') {
        //return '#0378EA';
        return '#42A5F5';
      } else if (status == '7') {
        //return '#04488C';
        return '#42A5F5';
      } else if (status == '8') {
        return '#b0bec5';
      } else {
        return '#FEC400';
      }
    },
    getOrderName(val) {
      for (let i = 0; i < this.orderStatusOptions.length; i++) {
        if (this.orderStatusOptions[i].value == val) {
          return this.orderStatusOptions[i].label;
        }
      }
      return '';
    },
    editHandleDialogOpen() {
      this.$router.push({
        path: `/aAndA/updateOrders/${this.ordersDetail.id}`,
        query: {}
      });
    },
    getErrorMsg(row, type) {
      if (type === 1) {
        //编辑
        if (row.updateButton == '3') {
          return '终态下非代码审核角色不可编辑';
        } else {
          return '';
        }
      } else {
        //删除
        if (row.deleteButton == '1') {
          return '工单终态下不可删除';
        } else if (row.deleteButton == '2') {
          return '工单下有会议记录不可删除';
        } else {
          return '';
        }
      }
    },
    isDisableBtn(row, type) {
      if (type === 1) {
        //编辑
        if (row.updateButton == '3') {
          return true;
        } else {
          return false;
        }
      } else {
        //删除
        if (row.deleteButton !== '0') {
          return true;
        } else {
          return false;
        }
      }
    },
    async delHandleDialogOpen() {
      this.$q
        .dialog({
          title: `确认删除`,
          message: `是否确认删除此工单？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          const res = await deleteOrderById({ id: this.ordersDetail.id });
          if (res && res.code && res.code != 'AAAAAAA') {
            // 失败
            errorNotify(res.msg);
          } else {
            // 成功
            successNotify('删除成功!');
            this.$router.go(-1);
          }
        });
    },
    verifyModel(nameKey) {
      try {
        let unitModuleKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.indexOf(nameKey) > -1;
        });
        validate(
          unitModuleKeys.map(key => {
            if (this.$refs[key] instanceof Array) {
              return this.$refs[key][0];
            }
            if (
              this.$refs[key].$children.length > 0 &&
              this.$refs[key].$children[0].$children.length > 0 &&
              this.$refs[key].$children[0].validate
            ) {
              return this.$refs[key].$children[0].validate();
            }
            return this.$refs[key].validate();
          })
        );
        const _this = this;
        if (this.$v.createModel.$invalid) {
          const validateRes = unitModuleKeys.every(item => {
            if (item.indexOf('.') === -1) {
              return true;
            }
            const itemArr = item.split('.');
            return !_this.$v.createModel[itemArr[1]].$invalid;
          });
          if (!validateRes) {
            return false;
          }
        }
      } catch (error) {
        return false;
      }
      return true;
    },
    async recheckClick() {
      if (!this.verifyModel('createModel.recheckContent')) {
        return;
      }
      await resolveResponseError(() =>
        applyRecheck({
          id: this.ordersDetail.id,
          recheckContent: this.createModel.recheckContent
        })
      );
      successNotify('邮件发送成功！');
      this.openCheckDlg = false;
      this.init();
    },
    openRecheckDlg() {
      this.openCheckDlg = true;
    },
    async recheckRemindClick() {
      this.$q
        .dialog({
          title: `确认发送`,
          message: `确认发送代码复审提醒邮件？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            recheckRemind({ id: this.ordersDetail.id })
          );
          successNotify('邮件发送成功！');
        });
    }
  },
  async mounted() {}
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
  transform: translateY(10px) !important;
>>> .q-tab
  outline none
.rqrHeader{
  background: #FFFFFF;
  border-radius: 8px;
  margin-bottom:10px;
}
.rqrHeader{
  width: 100%;
  display:flex;
  align-items: center;
  .rqrleft{
    width: 40px;
    height: 90px;
    border-radius: 8px;
  }
  .rqrcenter{
    padding: 0  24px 0 33px;
    height: 90px;
    background: #FFFFFF;
    position: absolute;
    left: 8px;
    display: flex;
    align-items: center;
    .rqrName{
      font-size: 20px;
      color: #333333;
      letter-spacing: 0;
      line-height: 30px;
      font-weight: 600;
    }
    .rqrNum{
      font-family: PingFangSC-Regular;
      font-size: 12px;
      color: #999999;
      letter-spacing: 0;
      line-height: 20px;
    }
    .maxWidth{
      max-width: 760px;
    }
  }
  .rqrRight{
    background: url("../../assets/codeToolBack.png") no-repeat 10px center;
    display:flex;
    align-items: center;
    position: absolute;
    right: 0px;
    width: 338.8px;
    height: 90px;
    justify-content: center;
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
  height 59px
  padding 12px 32px 0px 32px
  background: #fff;
  border-top-right-radius 8px
  border-top-left-radius 8px
.rqrContent2
  height 72px
  padding 12px 32px 0px 32px
  background: #fff;
  border-top-right-radius 8px
  border-top-left-radius 8px
  border-bottom-right-radius 8px
  border-bottom-left-radius 8px
.content-box
  .f-block
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
</style>
