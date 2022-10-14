<template>
  <div class="q-gutter-md">
    <f-block block>
      <div class="row items-start">
        <div class="text-h5 text-title flex1">
          <div class="flex">
            <div
              class="ellipsis-2-lines break-word pdr5 maxWidth"
              :title="unitDetail.implContent"
            >
              {{ unitDetail.implContent }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ unitDetail.implContent }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
            <div>
              <fdev-badge>{{ unitDetail.implStatusName }}</fdev-badge>
            </div>
          </div>
          <div class="text-subtitle1 text-title">
            {{ unitDetail.implUnitNum }}
          </div>
        </div>
        <div class="row" v-if="!hideDemandInfo">
          <div>
            <fdev-tooltip
              v-if="
                !(isDemandManager || isUnitLeader()) ||
                  unitDetail.leaderFlag === '3' ||
                  (!!unitDetail.usedSysCode &&
                    unitDetail.usedSysCode !== 'ZH-0748' &&
                    unitDetail.usedSysCode !== 'stockUnit') ||
                  unitDetail.implStatusName === '已撤销' ||
                  unitDetail.implStatusName === '暂缓' ||
                  unitDetail.implStatusName === '暂存'
              "
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ getTip() }}</span>
            </fdev-tooltip>
            <!-- 1.当前用户不为本实施单元牵头人、需求管理员 不展示按钮
          2.实施牵头人均不是fdev 不展示按钮 -->
            <fdev-btn
              dialog
              outline
              ficon="compile"
              label="编辑"
              v-if="demandModel.demand_type === 'business'"
              :disable="
                !(isDemandManager || isUnitLeader()) ||
                  unitDetail.leaderFlag === '3' ||
                  (!!unitDetail.usedSysCode &&
                    unitDetail.usedSysCode !== 'ZH-0748' &&
                    unitDetail.usedSysCode !== 'stockUnit') ||
                  unitDetail.implStatusName === '已撤销' ||
                  unitDetail.implStatusName === '暂缓' ||
                  unitDetail.implStatusName === '暂存'
              "
              @click="updateUnit"
            />
          </div>
          <div v-if="demandModel.isTransferRqrmnt !== '1'" class="mgl10">
            <fdev-tooltip
              v-if="
                (demandModel.demand_status_normal >= 7 ||
                  demandModel.demand_status_special === 1) &&
                  demandModel.demand_status_special != 2
              "
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>该阶段不可评估</span>
            </fdev-tooltip>
            <fdev-btn
              dialog
              outline
              ficon="requires"
              label="评估"
              v-if="
                isDemandManager ||
                  isDemandLeader() ||
                  isIncludeCurrentUser() ||
                  demandModel.is_ipmp_unit_leader
              "
              :disable="
                (demandModel.demand_status_special === 1 ||
                  demandModel.demand_status_normal >= 7) &&
                  demandModel.demand_status_special !== 2
              "
              @click="assessmentBtnClick"
            />
          </div>
          <!-- 老需求评估阶段不限权限 -->
          <div class="mgl10" v-else>
            <fdev-tooltip
              v-if="demandModel.demand_status_normal === 8"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>已归档的存量需求不可评估</span>
            </fdev-tooltip>
            <fdev-btn
              dialog
              outline
              ficon="requires"
              label="评估"
              v-if="
                isDemandManager ||
                  isDemandLeader() ||
                  isIncludeCurrentUser() ||
                  demandModel.is_ipmp_unit_leader
              "
              :disable="demandModel.demand_status_normal === 8"
              @click="assessmentBtnClick"
            />
          </div>
        </div>
      </div>
    </f-block>
    <!-- 基础数据 -->
    <f-block block v-if="!hideDemandInfo">
      <div class="text-h6">需求基本信息</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem page label="需求编号:">
            <router-link
              :to="`/rqrmn/rqrProfile/${demandModel.id}`"
              class="link"
            >
              {{ demandModel.oa_contact_no }}
            </router-link>
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="需求名称:">
            <router-link
              :to="`/rqrmn/rqrProfile/${demandModel.id}`"
              class="link"
            >
              {{ demandModel.oa_contact_name }}
            </router-link>
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="需求牵头人:"
            ><div class="text-ellipsis">
              <span
                v-for="(item, index) in demandModel.demand_leader_all"
                :key="index"
              >
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
          </f-formitem>
        </div>
      </div>
    </f-block>
    <f-block block>
      <div class="text-h6">实施单元基础信息</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem page label="实施牵头人:"
            >{{ unitDetail.implLeaderName }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ unitDetail.implLeaderName }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>
          <f-formitem page label="牵头单位:"
            >{{ unitDetail.headerUnitName }}
          </f-formitem>
          <f-formitem page label="涉及系统编号:">
            {{ unitDetail.relateSysCode }}
          </f-formitem>
          <f-formitem page label="测试牵头人邮箱:">
            {{ unitDetail.testLeaderEmail }}
          </f-formitem>
          <f-formitem page label="测试实施部门:">
            {{ unitDetail.testImplDeptName }}
          </f-formitem>
          <f-formitem page label="评估状态:">
            {{ unitDetail.usedSysCode | fileterCode }}
          </f-formitem>
          <f-formitem
            page
            v-if="unitDetail.projectType == 2"
            label="技术方案编号:"
            >{{ unitDetail.techSchemeNo }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="实施牵头人域账号:" label-style="width:140px"
            >{{ unitDetail.implLeader }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ unitDetail.implLeader }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>
          <f-formitem page label="牵头团队:" label-style="width:140px">
            {{ unitDetail.headerTeamName }}
          </f-formitem>
          <f-formitem page label="拟纳入项目名称:" label-style="width:140px">
            {{ unitDetail.planPrjName }}
          </f-formitem>
          <f-formitem page label="项目编号:" label-style="width:140px">
            {{ unitDetail.prjNum }}
          </f-formitem>
          <f-formitem
            page
            v-if="unitDetail.projectType == 2"
            label-style="width:140px"
            label="审核人:"
            >{{ unitDetail.checkerUserNames }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="牵头小组:"
            >{{ unitDetail.leaderGroupName }}
          </f-formitem>
          <f-formitem page label="涉及系统名称:">
            {{ unitDetail.relateSysName }}
          </f-formitem>
          <f-formitem page label="测试牵头人:"
            >{{ unitDetail.testLeaderName }}
          </f-formitem>
          <f-formitem page label="研发模式:"
            >{{ unitDetail.unitDevMode | modelFlag }}
          </f-formitem>
          <f-formitem page v-if="unitDetail.projectType == 2" label="是否上云:"
            >{{ unitDetail.cloudFlagName }}
          </f-formitem>
        </div>
      </div>
    </f-block>
    <!-- 工作量 -->
    <f-block block>
      <div class="text-h6">工作量</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem
            label="行内人员预期工作量（人月）:"
            label-style="width:200px"
            >{{ unitDetail.expectOwnWorkload }}
          </f-formitem>
          <f-formitem
            label="行内人员实际工作量（非功能性）（人月）:"
            label-style="width:410px"
            >{{ unitDetail.actualOwnUfworkload }}
          </f-formitem>
          <f-formitem
            label="行内人员实际工作量（人月）:"
            label-style="width:200px"
            >{{ unitDetail.actualOwnFworkload }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem
            label="公司人员预期工作量（人月）:"
            label-style="width:200px"
            >{{ unitDetail.expectOutWorkload }}
          </f-formitem>

          <f-formitem
            label="公司人员实际工作量（非功能性）（人月）:"
            label-style="width:410px"
            >{{ unitDetail.actualOutUfworkload }}</f-formitem
          >

          <f-formitem
            label="公司人员实际工作量（人月）:"
            label-style="width:200px"
          >
            {{ unitDetail.actualOutFworkload }}
          </f-formitem>
        </div>
      </div>
    </f-block>
    <!-- 安排与实施 -->
    <f-block block>
      <div class="text-h6">实施安排及情况</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem page label="计划启动开发日期:" label-style="width:200px">
            {{ unitDetail.planDevelopDate }}
          </f-formitem>
          <f-formitem page label="计划提交内测日期:" label-style="width:200px">
            {{ unitDetail.planInnerTestDate }}
          </f-formitem>
          <f-formitem
            label="计划提交用户测试日期:"
            page
            label-style="width:200px"
          >
            {{ unitDetail.planTestStartDate }}
          </f-formitem>
          <f-formitem
            label="计划用户测试完成日期:"
            page
            label-style="width:200px"
          >
            {{ unitDetail.planTestFinishDate }}
          </f-formitem>
          <f-formitem label="计划投产日期:" page label-style="width:200px">
            {{ unitDetail.planProductDate }}
          </f-formitem>
          <f-formitem
            label="ipmp实施延期原因分类:"
            page
            label-style="width:200px"
          >
            {{ unitDetail.implDelayTypeName }}
          </f-formitem>
          <f-formitem label="ipmp实施延期原因:" page label-style="width:200px">
            {{ unitDetail.implDelayReason }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem
            label="计划启动开发日期(调):"
            page
            label-style="width:200px"
          >
            {{ unitDetail.planDevelopDateAdjust }}
          </f-formitem>
          <f-formitem label=""><span></span> </f-formitem>
          <f-formitem
            label="计划提交用户测试日期(调):"
            page
            label-style="width:200px"
          >
            {{ unitDetail.planTestStartDateAdjust }}
          </f-formitem>
          <f-formitem
            label="计划用户测试完成日期(调):"
            page
            label-style="width:200px"
          >
            {{ unitDetail.planTestFinishDateAdjust }}
          </f-formitem>
          <f-formitem label="计划投产日期(调):" page label-style="width:200px">
            {{ unitDetail.planProductDateAdjust }}
          </f-formitem>
          <f-formitem
            label="fdev实施计划变更原因分类:"
            page
            label-style="width:200px"
          >
            {{
              unitDetail.implChangeTypeName &&
                unitDetail.implChangeTypeName.join(', ')
            }}
          </f-formitem>
          <f-formitem
            label="fdev实施计划变更原因:"
            page
            label-style="width:200px"
          >
            {{ unitDetail.implChangeReason }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem label="实际启动开发日期:" page label-style="width:200px">
            {{ unitDetail.acturalDevelopDate }}
          </f-formitem>

          <f-formitem label="实际提交内测日期:" page label-style="width:200px">
            {{ unitDetail.actualInnerTestDate }}
          </f-formitem>

          <f-formitem
            label="实际提交用户测试日期:"
            page
            label-style="width:200px"
          >
            {{ unitDetail.acturalTestStartDate }}
          </f-formitem>

          <f-formitem
            label="实际用户测试完成日期:"
            page
            label-style="width:200px"
          >
            {{ unitDetail.acturalTestFinishDate }}
          </f-formitem>

          <f-formitem label="实际投产日期:" page label-style="width:200px">
            {{ unitDetail.acturalProductDate }}
          </f-formitem>
          <f-formitem label="确认延期阶段:" page label-style="width:200px">
            {{ getConfirmDelayStageName(unitDetail.confirmDelayStage) }}
          </f-formitem>
          <f-formitem label="业务确认邮件:" page label-style="width:200px">
            <div
              class="text-ellipsis"
              :title="
                unitDetail.businessEmail &&
                  unitDetail.businessEmail
                    .map(v => v.businessEmailName)
                    .join('，')
              "
            >
              <span
                v-for="(item, index) in unitDetail.businessEmail"
                :key="index"
              >
                <a
                  v-if="unitDetail.businessEmail && item.businessEmailPath"
                  @click="download(item.businessEmailPath)"
                  class="link div-3 cursor-pointer"
                >
                  {{ item.businessEmailName }}
                </a>
                <span v-else class="span-margin">{{
                  unitDetail.businessEmail && item.businessEmailName
                }}</span>
              </span>
            </div>
          </f-formitem>
        </div>
      </div>
    </f-block>
    <f-block block v-if="!hideDemandInfo">
      <fdev-tabs align="left" v-model="tab">
        <fdev-tab name="developNo" label="研发单元" />
        <fdev-tab name="task" label="任务" />
      </fdev-tabs>
      <fdev-tab-panels v-model="tab">
        <fdev-tab-panel name="developNo">
          <UnitNo
            ref="unitNo"
            v-if="unitDetail.implUnitNum && demandModel"
            @refresh="init()"
            :demandModel="demandModel"
            :unitNum="unitDetail.implUnitNum"
          />
        </fdev-tab-panel>
        <fdev-tab-panel name="task">
          <div style="height:10px;width:100px"></div>
          <Task :demandDetail="demandModel" :unitNum="unitDetail.implUnitNum" />
        </fdev-tab-panel>
      </fdev-tab-panels>
    </f-block>
    <update-unit-detail
      v-if="assessmentDialogShow"
      :showFlag="assessmentDialogShow"
      :data="unitDetail"
      @updateSuccess="updateSucc"
    />
    <f-dialog v-model="editFlag" title="去编辑信息">
      <span>
        {{ editFlaTip }}
      </span>
      <template v-slot:btnSlot>
        <fdev-btn flat label="去编辑" @click="goRqrEdit()" v-close-popup />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { unitStatusSpecial } from '../model';

import UnitNo from './UnitNo';
import Task from './Task';
import { mapState, mapGetters, mapActions } from 'vuex';
import UpdateUnitDetail from './updateUnitDetail';
import { queryIpmpUnitById } from '@/services/demand';
export default {
  name: 'UnitDetail',
  components: {
    UnitNo,
    UpdateUnitDetail,
    Task
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isDemandManager']),
    ...mapState('demandsForm', ['demandInfoDetail'])
  },
  data() {
    return {
      tab: 'developNo',
      unitDetail: {},
      assessmentDialogShow: false,
      editFlag: false,
      editFlaTip: '',
      demandModel: {},
      hideDemandInfo: window.location.href.includes('noDemandInfo') //包含noDemandInfo 不显示编辑评估 研发单元列表 任务列表
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    ...mapActions('demandsForm', {
      queryDemandInfoDetail: 'queryDemandInfoDetail'
    }),
    ...mapActions('jobForm', {
      downExcel: 'downExcel'
    }),
    async init() {
      if (!this.hideDemandInfo) {
        await this.queryDemandInfoDetail({ id: this.$route.params.demandId });
        this.demandModel = this.demandInfoDetail;
      }
      let data = await queryIpmpUnitById({
        implUnitNum: this.$route.params.id
      });
      this.unitDetail = data;
    },
    isUnitLeader() {
      if (!this.unitDetail.implLeader) return false;
      let arr = this.unitDetail.implLeader.split(',');
      return arr.some(item => {
        return (
          item.toLowerCase() === this.currentUser.user_name_en.toLowerCase()
        );
      });
    },
    updateUnit() {
      this.assessmentDialogShow = true;
      //暂时方案 隐藏编辑实施单元title，因为title过长导致的弹窗过长 待组建修改后删除
      setTimeout(() => {
        document.querySelector('.title-line').style.display = 'none';
      }, 50);
    },
    updateSucc() {
      this.assessmentDialogShow = false;
      this.init();
    },
    getTip() {
      let tip = '';
      if (!(this.isDemandManager || this.isUnitLeader())) {
        tip = '当前用户不为本实施单元牵头人、需求管理员， 不允许修改';
      } else if (this.unitDetail.leaderFlag === '3') {
        tip = '实施牵头人均不是fdev人员， 不允许修改';
      } else if (
        !!this.unitDetail.usedSysCode &&
        this.unitDetail.usedSysCode !== 'ZH-0748' &&
        this.unitDetail.usedSysCode !== 'stockUnit'
      ) {
        tip = '该实施单元不属于 fdev平台, 不允许修改';
      } else if (
        this.unitDetail.implStatusName === '已撤销' ||
        this.unitDetail.implStatusName === '暂缓' ||
        this.unitDetail.implStatusName === '暂存'
      ) {
        tip = '实施单元状态为已撤销、暂缓、暂存，不允许修改';
      }
      return tip;
    },
    isIncludeCurrentUser() {
      if (
        this.demandModel.relate_part_detail &&
        Array.isArray(this.demandModel.relate_part_detail)
      ) {
        return this.demandModel.relate_part_detail.some(part => {
          return (
            part.assess_user &&
            part.assess_user.some(id => {
              return id === this.currentUser.id;
            })
          );
        });
      }
    },
    //判断用户集合中是否包含当前用户，当前用户是否是需求牵头人
    isDemandLeader() {
      if (
        this.demandModel.demand_leader_all &&
        Array.isArray(this.demandModel.demand_leader_all)
      ) {
        return this.demandModel.demand_leader_all.some(user => {
          return user.id === this.currentUser.id;
        });
      }
    },
    //点击评估按钮
    assessmentBtnClick() {
      //若需求无牵头小组或实施牵头人，涉及小组及涉及小组评估人
      if (
        this.demandModel.demand_leader_group == '' ||
        this.demandModel.demand_leader.length === 0 ||
        this.demandModel.relate_part.length === 0 ||
        this.isEditMessage()
      ) {
        this.editFlag = true;
        this.editFlaTip =
          '需求牵头小组、实施牵头人、涉及小组及涉及小组评估人信息不全，请前往编辑页面补充完整';
      } else {
        // 评估 路由更换为 需求详情 下的 研发单元(developNo)tab
        this.$router.push({
          path: `/rqrmn/rqrProfile/${this.demandModel.id}`,
          query: { tab: 'developNo' }
        });
      }
    },
    goRqrEdit() {
      this.$router.push(`/rqrmn/rqrEdit/${this.demandModel.id}`);
    },
    //判断涉及小组及涉及小组评估人
    isEditMessage() {
      if (this.demandModel.relate_part_detail.length === 0) {
        return true;
      } else {
        return !this.demandModel.relate_part_detail.some(
          item => item.assess_user.length > 0
        );
      }
    },
    async download(val) {
      let param = {
        path: val,
        moduleName: 'fdev-demand'
      };
      await this.downExcel(param);
    },
    getConfirmDelayStageName(row) {
      if (row && row.length > 0) {
        let confirmDelayStageNameList = [];
        let list = [
          { label: '提交用户测试延期', value: 'testStartDelay' },
          { label: '启动延期', value: 'developDelay' },
          { label: '提交测试完成延期', value: 'testFinishDelay' },
          { label: '投产延期', value: 'productDelay' }
        ];
        list.forEach(val => {
          row.filter(item => {
            if (val.value === item) {
              confirmDelayStageNameList.push(val.label);
            }
          });
        });
        return confirmDelayStageNameList.join('，');
      }
    }
  },
  filters: {
    unitStatusSpecialFilter(val) {
      return unitStatusSpecial[val] ? unitStatusSpecial[val] : '';
    },
    modelFlag(val) {
      let model = '';
      if (val === 'implunit.dev.mode.01') {
        model = '敏态';
      } else if (val === 'implunit.dev.mode.02') {
        model = '稳态';
      }
      return model;
    },
    fileterCode(val) {
      let model = '';
      if (val === 'ZH-0748') {
        model = 'fdev评估';
      } else if (val === 'stockUnit') {
        model = 'ipmp评估';
      } else {
        model = '未评估';
      }
      return model;
    }
  }
};
</script>
<style lang="stylus" scoped>


.relatePartClass {
  font-size: 14px;
  color: #DDDDDD;
  cursor: pointer;
}

.content > .column
  flex-wrap: nowrap;

.detail
  word-break: break-all;
  font-size: 14px;
  color: $grey-8;
.btn-radius .q-btn
 border-radius 0 0 0 0
.mgT16
  margin-top 16px
.title
  font-size 20px
  padding 10px
  font-weight 700
.mgl10
  margin-left 10px
.pdr5
  padding-right 8px
.flex1
  flex 1
.break-word
  word-break: break-all;
.maxWidth
  max-width calc(100% - 100px)
</style>
