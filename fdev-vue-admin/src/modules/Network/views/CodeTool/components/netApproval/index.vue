<template>
  <div class="meetContent">
    <Loading :visible="loading">
      <div class="meettingTitle">
        <div class="meetLeft">
          <span class="tname">会议记录</span>
          <span>
            <fdev-tooltip
              v-if="addButtonFlag"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ getToolTipMsg }}</span>
            </fdev-tooltip>
            <fdev-btn
              :disable="addButtonFlag"
              flat
              dialog
              ficon="add"
              class="q-mr-sm"
              label="新增"
              @click="addMeetting"
            />
          </span>
        </div>
        <fdev-btn
          dialog
          ficon="exit"
          normal
          label="问题导出"
          @click="handleExportDiaOpen"
        />
      </div>
      <div class="listItem" v-for="(item, index) in meetLists" :key="index">
        <div class="row items-center titleBox">
          <f-icon name="list_s_f" class="titleimg" />
          <div class="lcontent row items-center">
            <div class="clabel">会议时间</div>
            <div class="cvalue ellipsis" :title="item.meetingTime">
              {{ item.meetingTime }}
            </div>
          </div>
          <div class="lcontent row items-center">
            <div class="clabel">会议地点</div>
            <div class="cvalue ellipsis" :title="item.address">
              {{ item.address }}
            </div>
          </div>
          <div class="lcontent row items-center">
            <div class="clabel">会议批次</div>
            <div
              class="cvalue ellipsis"
              :title="getMeettingTypeName(item.meetingType)"
            >
              {{ getMeettingTypeName(item.meetingType) }}
            </div>
          </div>
          <div class="lcontent row items-center">
            <div class="clabel">审核人</div>
            <div class="cvalue ellipsis" :title="item.auditorUsersCn">
              {{ item.auditorUsersCn }}
            </div>
          </div>
          <div class="lbtn">
            <span>
              <fdev-tooltip
                v-if="isDisableBtn(item, 2)"
                anchor="top middle"
                self="center middle"
                :offest="[-20, 0]"
              >
                <span>{{ getErrorMsg(item, 2) }}</span>
              </fdev-tooltip>
              <fdev-btn
                flat
                :disable="isDisableBtn(item, 2)"
                dialog
                ficon="edit"
                label="编辑"
                class="q-mr-sm"
                @click="editMeetting(item)"
              />
            </span>
            <span>
              <fdev-tooltip
                v-if="isDisableBtn(item, 3)"
                anchor="top middle"
                self="center middle"
                :offest="[-20, 0]"
              >
                <span>{{ getErrorMsg(item, 3) }}</span>
              </fdev-tooltip>
              <fdev-btn
                flat
                :disable="isDisableBtn(item, 3)"
                dialog
                ficon="delete_o"
                label="删除"
                @click="delMeetting(item)"
              />
            </span>
          </div>
        </div>
        <problemList
          class="proTable"
          :datas="item.pLists"
          :addButton="addBugButtonFlag"
          :id="item.id"
          @rebrash="getMeettings(false)"
        />
      </div>
    </Loading>
    <addDlg v-model="isApply" @close="handleApplyClose" />
    <editDlg
      v-model="isClose"
      :editData="meetingData"
      @close="handleEditClose"
    />
    <exportDlg
      v-model="exportFlag"
      v-if="exportFlag"
      :orderId="orderId"
      @close="handleExportClose"
    ></exportDlg>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import addDlg from './addDlg';
import editDlg from './editDlg';
import problemList from './problemList';
import exportDlg from './exportDlg';
import { successNotify, resolveResponseError } from '@/utils/utils';
import { meetingTypeOptions } from '@/modules/Network/utils/constants';
import { mapActions, mapState } from 'vuex';
import {
  queryMeetings,
  deleteMeetingById,
  queryProblems
} from '@/modules/Network/services/methods';
import moment from 'moment';

export default {
  name: 'NetApproval',
  props: {
    params: {
      type: Object
    }
  },
  components: {
    Loading,
    addDlg,
    editDlg,
    problemList,
    exportDlg
  },
  data() {
    return {
      loading: false,
      meetLists: [],
      addButtonFlag: false,
      addBugButtonFlag: '1',
      proItemObj: {},
      isApply: false,
      isClose: false,
      getToolTipMsg: '',
      meetingData: {},
      meetingTypeOptions: meetingTypeOptions,
      orderId: '',
      exportFlag: false //导出问题弹窗标识
    };
  },
  computed: {
    ...mapState('networkForm', ['problemItem'])
  },
  watch: {},
  methods: {
    ...mapActions('networkForm', ['queryProblemItem']),
    getMeettingTypeName(val) {
      for (let i = 0; i < this.meetingTypeOptions.length; i++) {
        if (val == this.meetingTypeOptions[i].value) {
          return this.meetingTypeOptions[i].label;
        }
      }
      return '';
    },
    getMeettingTime(val) {
      if (val) {
        let currentDate = new Date(val);
        return moment(currentDate).format('YYYY-MM-DD HH:mm');
      }
      return '';
    },
    addMeetting() {
      this.isApply = true;
    },
    editMeetting(row) {
      this.isClose = true;
      this.meetingData = row;
    },
    async getMeettings(update) {
      let params = { id: this.params.id };
      const bugRes = await queryProblems(params);
      if (bugRes && bugRes.problemList) {
        this.addBugButtonFlag = bugRes.addButton;
        this.proItemObj = {};
        for (let i = 0; i < bugRes.problemList.length; i++) {
          let item = bugRes.problemList[i];
          if (this.proItemObj[item.meetingId]) {
            this.proItemObj[item.meetingId].push(item);
          } else {
            this.proItemObj[item.meetingId] = [item];
          }
        }
      }
      let meetParam = { orderId: this.params.id };
      let res = await queryMeetings(meetParam);
      if (res && res.meetingList) {
        this.meetLists = res.meetingList.filter(item => {
          if (this.proItemObj[item.id]) {
            item.pLists = this.proItemObj[item.id];
          } else {
            item.pLists = [];
          }
          return item;
        });
        if (res.addButton === '0') {
          this.addButtonFlag = false;
        } else if (res.addButton === '1') {
          this.addButtonFlag = true;
          this.getToolTipMsg = '仅代码审核角色可新增';
        } else {
          this.addButtonFlag = true;
          this.getToolTipMsg = '工单终态下不可新增会议';
        }
      }
      if (update) {
        this.$emit('init');
      }
    },
    async delMeetting(row) {
      this.$q
        .dialog({
          title: `确认删除`,
          message: `是否确认删除此会议记录？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          await resolveResponseError(() => deleteMeetingById({ id: row.id }));
          // 成功
          successNotify('删除成功!');
          this.getMeettings(true);
        });
    },
    handleApplyClose() {
      this.isApply = false;
      this.getMeettings(true);
    },
    handleExportClose() {
      this.exportFlag = false;
    },
    handleEditClose() {
      this.isClose = false;
      //编辑会议不会改变工单状态，getMeettings传false
      this.getMeettings(false);
    },
    getErrorMsg(row, type) {
      if (type == '2') {
        //编辑
        if (row.updateButton === '1') {
          return '仅代码审核角色可编辑';
        } else if (row.updateButton === '2') {
          return '工单终态不可编辑';
        } else {
          return '';
        }
      } else {
        //删除
        if (row.deleteButton === '1') {
          return '仅代码审核角色可删除';
        } else if (row.deleteButton === '2') {
          return '工单终态下不可删除会议';
        } else if (row.deleteButton === '3') {
          return '会议下存在问题，不可删除';
        } else {
          return '';
        }
      }
    },
    isDisableBtn(row, type) {
      if (type == '2') {
        //编辑
        if (row.updateButton !== '0') {
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
    handleExportDiaOpen() {
      this.exportFlag = true;
    }
  },
  async created() {
    this.getMeettings();
    this.orderId = this.params.id;
    await this.queryProblemItem({ type: 'problemItem' });
  }
};
</script>
<style lang="stylus" scoped>
.meetContent{
  margin-top: 10px;
  padding: 0px 32px 10px 32px;
  background: #fff;
}
.meettingTitle{
  height: 68px;
  padding: 20px 0px 0px 0px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .meetLeft{
    display: flex;
    align-items: center;
    .tname{
      font-family: PingFangSC-Semibold;
      font-size: 16px;
      color: #333333;
      letter-spacing: 0;
      line-height: 16px;
      font-weight:600;
    }
  }
}
.listItem{
  border: 1px solid #DDDDDD;
  margin-bottom: 20px;
  .titleBox{
    padding: 0px 32px 0px 32px;
    background: rgba(30,84,213,0.05);
    border-radius: 2px;
    border-radius: 2px;
    height: 54px;
    .titleimg{
        width: 16px;
        height: 16px;
        color: #0378EA;
        border-radius: 2px;
        border-radius: 2px;
        margin-bottom: 0px !important;
      }
    .titlename{
      margin-left: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #333333;
      width: 108px;
    }
    .lcontent{
      font-family: PingFangSC-Regular;
      font-size: 14px;
      color: #333333;
      letter-spacing: 0;
      line-height: 14px;
      margin-left: 8px;
      margin-right: 24px;
      .clabel{
        margin-right: 16px;
      }
      .cvalue{
        max-width: 142px;
      }
    }
    .lbtn{
      position: absolute;
      margin-right: 20px;
      right: 0px;
    }
 }
}
</style>
