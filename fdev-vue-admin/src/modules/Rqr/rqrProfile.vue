<template>
  <div class="q-gutter-md">
    <f-block block>
      <div class="row items-start justify-between">
        <div class="text-h5 text-title flex1">
          <div class="flex">
            <div
              class="ellipsis-2-lines break-word pdr5 maxWidth"
              :title="demandModel.oa_contact_name"
            >
              {{ demandModel.oa_contact_name }}
            </div>
            <div
              v-if="
                demandModel.demand_status_special != 1 &&
                  demandModel.demand_status_special != 2
              "
            >
              <fdev-badge>{{ demandStatusNormal }}</fdev-badge>
            </div>
            <div
              v-if="
                demandModel.demand_status_special == 1 ||
                  demandModel.demand_status_special == 2
              "
            >
              <fdev-badge>{{
                demandModel.demand_status_special | demandStatusSpecialFilter
              }}</fdev-badge>
            </div>
          </div>
          <div class="text-subtitle1 text-title">
            <span class="pdr5">{{ demandModel.oa_contact_no }}</span>
            <f-icon
              name="help_c_o"
              class="text-primary cursor-pointer"
              @click="helpDailog()"
            />
          </div>
        </div>
        <!-- <Authorized
            v-if="job.stage.code < 5 && job.stage.code > -1 && btnDisplay"
          > -->
        <div class="flex">
          <!-- 已投产,已归档状态显示归档按钮 -->
          <fdev-btn
            v-if="
              demandModel.demand_status_normal == 7 &&
                (isDemandManager || isDemandLeader())
            "
            dialog
            outline
            ficon="time_r"
            label="归档"
            @click="handleFile"
            class="mgl10"
          />
          <!-- 需求管理员和需求牵头人可编辑，暂缓不可编辑 -->
          <div class="mgl10">
            <fdev-tooltip
              v-if="
                demandModel.demand_status_normal > 7 ||
                  demandModel.demand_status_special === 1 ||
                  demandModel.demand_status_special === 2
              "
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ demandStatu }}阶段不可编辑</span>
            </fdev-tooltip>
            <fdev-btn
              dialog
              outline
              ficon="compile"
              label="编辑"
              v-if="editShowAble()"
              :disable="
                demandModel.demand_status_normal > 7 ||
                  demandModel.demand_status_special === 1 ||
                  demandModel.demand_status_special === 2
              "
              @click="handleDialogOpen(demandModel.id)"
            />
          </div>
          <div v-if="demandModel.isTransferRqrmnt !== '1'" class="mgl10">
            <fdev-tooltip
              v-if="
                (demandModel.demand_status_normal > 7 ||
                  demandModel.demand_status_special === 1) &&
                  demandModel.demand_status_special != 2
              "
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ demandStatu }}阶段不可评估</span>
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
                  demandModel.demand_status_normal > 7) &&
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
          <div class="mgl10">
            <fdev-tooltip
              v-if="
                demandModel.demand_status_special === 1 ||
                  demandModel.demand_status_special === 2 ||
                  demandModel.demand_status_normal > 5
              "
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ demandStatu }}阶段不可撤销</span>
            </fdev-tooltip>
            <fdev-btn
              dialog
              outline
              ficon="replace"
              label="撤销"
              @click="handleDeleteRequirement"
              v-if="
                demandModel.demand_status_normal !== 9 &&
                  isDemandManager &&
                  demand_type === 'tech'
              "
              :disable="
                demandModel.demand_status_special === 1 ||
                  demandModel.demand_status_special === 2 ||
                  demandModel.demand_status_normal > 5
              "
            />
          </div>
          <fdev-btn
            dialog
            outline
            ficon="replace"
            label="已撤销"
            v-if="demandModel.demand_status_normal === 9"
            disable
            class="mgl10"
          />
        </div>
      </div>
      <!-- </Authorized> -->
    </f-block>
    <!-- 基础数据 -->
    <!-- 基础数据 -->
    <f-block block>
      <div class="text-h6">需求基础信息</div>
      <div
        class="row q-col-gutter-x-md q-col-gutter-y-sm"
        v-if="demand_type !== 'tech'"
      >
        <div class="col">
          <f-formitem page label="需求信息单编号:"
            >{{ demandModel.oa_contact_no }}
          </f-formitem>
          <f-formitem page label="需求提出部门:">
            <div
              :title="demandModel.propose_demand_dept"
              class="ellipsis-3-lines break-word"
            >
              {{ demandModel.propose_demand_dept }}
            </div>
          </f-formitem>
          <f-formitem page label="需求计划名称:">
            <div
              :title="demandModel.demand_plan_name"
              class="ellipsis-3-lines break-word"
            >
              {{ demandModel.demand_plan_name }}
            </div>
          </f-formitem>
          <f-formitem page label="优先级:">
            {{ demandModel.priority | priorityFilter }}
          </f-formitem>
          <f-formitem page label="需求属性:">
            {{ demandModel.demand_property | demand_propertyFilter }}
          </f-formitem>
          <f-formitem page label="需求说明:">
            <div
              :title="demandModel.demand_desc"
              class="ellipsis-3-lines break-word"
            >
              {{ demandModel.demand_desc }}
            </div>
          </f-formitem>
          <f-formitem page label="行内人员预期工作量（人月）:"
            >{{ demandModel.dept_workload }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="需求信息单标题:">
            <div
              :title="demandModel.oa_contact_name"
              class="ellipsis-3-lines break-word"
            >
              {{ demandModel.oa_contact_name }}
            </div>
          </f-formitem>
          <f-formitem page label="实施单元跟踪人:">
            {{
              demandModel.impl_track_user &&
                demandModel.impl_track_user.join(',')
            }}
          </f-formitem>
          <f-formitem page label="对应需求计划编号:"
            >{{ demandModel.demand_plan_no }}
          </f-formitem>
          <f-formitem page label="需求总牵头负责人:">
            <div class="q-gutter-x-sm" v-if="demandModel.demand_leader_all">
              <router-link
                v-for="(each, index) in demandModel.demand_leader_all"
                :to="`/user/list/${each.id}`"
                :key="index"
                class="link"
              >
                {{ each.user_name_cn }}
              </router-link>
            </div>
          </f-formitem>
          <f-formitem
            page
            label="UI设计稿审核状态:"
            v-if="demandModel.ui_verify"
          >
            <!-- 异常关闭状态 -->
            <span v-if="designStatus === 'abnormalShutdown'">
              {{ demandModel.design_status | designStatusFilter }}
            </span>
            <!-- 其他正常状态 -->
            <router-link
              v-else
              :to="`/rqrmn/designReviewRqr/${demandModel.id}`"
              class="link"
            >
              {{ demandModel.design_status | designStatusFilter }}
            </router-link>
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="需求书名称:">
            <div
              :title="demandModel.demand_instruction"
              class="ellipsis-3-lines break-word"
            >
              {{ demandModel.demand_instruction }}
            </div>
          </f-formitem>
          <f-formitem page label="需求信息单我部接收日期:">
            {{ demandModel.oa_receive_date }}
          </f-formitem>
          <f-formitem page label="需求类型:"
            >{{ demandModel.demand_type === 'tech' ? '科技需求' : '业务需求' }}
          </f-formitem>
          <f-formitem page label="牵头小组:">
            {{ demandModel.demand_leader_group_cn }}
          </f-formitem>
          <f-formitem page label="公司人员预期工作量（人月）:"
            >{{ demandModel.company_workload }}
          </f-formitem>
        </div>
      </div>
      <div class="row q-col-x-md q-col-y-sm full-width" v-else>
        <div class="col">
          <f-formitem page label="需求说明书名称:">
            <div
              class="ellipsis-3-lines break-word"
              :title="demandModel.demand_instruction"
            >
              {{ demandModel.demand_instruction }}
            </div>
          </f-formitem>
          <f-formitem page label="公司人员预期工作量（人月）:"
            >{{ demandModel.company_workload }}
          </f-formitem>
          <f-formitem page label="受理日期:"
            >{{ demandModel.accept_date }}
          </f-formitem>
          <f-formitem page label="需求总牵头负责人:">
            <div class="q-gutter-x-sm" v-if="demandModel.demand_leader_all">
              <router-link
                v-for="(each, index) in demandModel.demand_leader_all"
                :to="`/user/list/${each.id}`"
                :key="index"
                class="link"
              >
                {{ each.user_name_cn }}
              </router-link>
            </div>
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="需求创建人:">
            <div class="q-gutter-x-sm">
              <router-link
                v-if="demandModel.demand_create_user_all"
                :to="`/user/list/${demandModel.demand_create_user_all.id}`"
                class="link"
              >
                {{ demandModel.demand_create_user_all.user_name_cn }}
              </router-link>
              <div v-else>-</div>
            </div>
          </f-formitem>
          <f-formitem page label="行内人员预期工作量（人月）:"
            >{{ demandModel.dept_workload }}
          </f-formitem>
          <f-formitem page label="优先级:">
            {{ demandModel.priority | priorityFilter }}
          </f-formitem>
          <f-formitem page label="需求属性:">
            {{ demandModel.demand_property | demand_propertyFilter }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="需求类型:"
            >{{ demandModel.demand_type === 'tech' ? '科技需求' : '业务需求' }}
          </f-formitem>
          <f-formitem label="科技类型:" page>
            {{ demandModel.tech_type }}
          </f-formitem>
          <f-formitem
            v-if="demand_type === 'tech' && isOtherType(demandModel.tech_type)"
            label="备注:"
            page
          >
            {{ demandModel.tech_type_desc }}
          </f-formitem>
          <f-formitem page label="需求说明:" v-if="demand_type === 'tech'">
            <div
              class="ellipsis-3-lines break-word"
              :title="demandModel.demand_desc"
            >
              {{ demandModel.demand_desc }}
            </div>
          </f-formitem>
          <f-formitem page label="牵头小组:">
            {{ demandModel.demand_leader_group_cn }}
          </f-formitem>
        </div>
        <!-- <f-formitem page label="需求是否可行">
            {{ demandModel.demand_available | demandAvalableFilter }}
          </f-formitem> -->
      </div>
    </f-block>
    <!-- 评估安排 -->
    <f-block block>
      <div class="text-h6">评估安排</div>
      <div class="row justify-between">
        <!-- <f-formitem page  label="实施团队可行性评估补充意见">
            {{ demandModel.extra_idea }}
          </f-formitem> -->
        <div class="labStyle">涉及小组:</div>
        <div class="row" style="flex: 1">
          <div
            class="mgr10"
            v-for="(item, index) in demandModel.relate_part_detail"
            :key="index"
          >
            <span class="relatePartClass">
              {{ `${item.part_name} —` }}
            </span>
            <span class="detail">
              {{ item.assess_user_all | assessUserFilter }}
            </span>
          </div>
        </div>
      </div>
    </f-block>
    <!-- 安排与实施 -->
    <f-block block>
      <div class="text-h6">需求排期</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem label="计划启动开发日期:" label-style="width:180px">
            {{ demandModel.plan_start_date }}
          </f-formitem>
          <f-formitem label="计划提交内测日期:" label-style="width:180px">
            {{ demandModel.plan_inner_test_date }}
          </f-formitem>
          <f-formitem label="计划提交用户测试日期:" label-style="width:180px">
            {{ demandModel.plan_test_date }}
          </f-formitem>
          <f-formitem label="计划用户测试完成日期:" label-style="width:180px">
            {{ demandModel.plan_test_finish_date }}
          </f-formitem>
          <f-formitem label="计划投产日期:" label-style="width:180px">
            {{ demandModel.plan_product_date }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem label="实际启动开发日期:" label-style="width:180px">
            {{ demandModel.real_start_date }}
          </f-formitem>
          <f-formitem label="实际提交内测日期:" label-style="width:180px">
            {{ demandModel.real_inner_test_date }}
          </f-formitem>
          <f-formitem label="实际提交用户测试日期:" label-style="width:180px">
            {{ demandModel.real_test_date }}
          </f-formitem>
          <f-formitem label="实际用户测试完成日期:" label-style="width:180px">
            {{ demandModel.real_test_finish_date }}
          </f-formitem>
          <f-formitem label="实际投产日期:" label-style="width:180px">
            {{ demandModel.real_product_date }}
          </f-formitem>
        </div>
      </div>
    </f-block>

    <f-block block>
      <fdev-tabs
        align="left"
        v-model="tab"
        class="text-grey-7"
        active-color="primary"
        indicator-color="primary"
      >
        <fdev-tab name="unitNo" label="实施单元" />
        <fdev-tab name="developNo" label="研发单元" />
        <fdev-tab name="task" label="任务" />
        <fdev-tab name="file" label="需规" />
      </fdev-tabs>
      <fdev-tab-panels v-model="tab">
        <fdev-tab-panel name="unitNo">
          <UnitList
            :demandModel="demandModel"
            v-if="demandModel.oa_contact_name"
            :isDemandManager="isDemandManager"
            :isDemandLeader="isDemandLeader()"
            :isIncludeCurrentUser="isIncludeCurrentUser()"
          />
        </fdev-tab-panel>
        <fdev-tab-panel name="developNo">
          <UnitNo
            ref="unitNo"
            v-if="demandModel"
            @refresh="init()"
            :demandModel="demandModel"
          />
        </fdev-tab-panel>
        <fdev-tab-panel name="task">
          <Task />
        </fdev-tab-panel>
        <fdev-tab-panel name="file">
          <!-- 
            //上传文件的身份限制
            <File
            :isDemandManager="isDemandManager"
            :isDemandLeader="isDemandLeader()"
            :isIncludeCurrentUser="isIncludeCurrentUser()"
          /> -->
          <File />
        </fdev-tab-panel>
      </fdev-tab-panels>
    </f-block>

    <updateDialog v-model="dialogOpen" :rqr-id="id" />
    <AssessmentDialog
      v-model="assessmentDialogShow"
      :data="detailData"
    ></AssessmentDialog>
    <f-dialog v-model="editFlag" title="去编辑信息">
      <span>
        {{ editFlaTip }}
      </span>
      <template v-slot:btnSlot>
        <fdev-btn flat label="去编辑" @click="goRqrEdit()" v-close-popup />
      </template>
    </f-dialog>
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
import AssessmentDialog from './Components/AssessmentDialog';
import {
  createDemandModel,
  demandStatus,
  assessStatusMap,
  demandStatusSpecial,
  designStatusMap,
  demandAssessWayMap,
  futrueAssessMap,
  priValue,
  demandAvalableMap
} from './model';
import { mapActions, mapState, mapGetters } from 'vuex';
import { successNotify, validate } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import UnitNo from './Components/UnitNo';
import UnitList from './Components/UnitList';
import Task from './Components/Task';
import File from './Components/File';
import updateDialog from './Components/updateDialog';
import { queryIpmpUnitIsCheck } from './services/methods.js';

export default {
  name: 'RqrProfile',
  components: {
    UnitNo,
    UnitList,
    Task,
    File,
    updateDialog,
    AssessmentDialog
  },
  data() {
    return {
      id: '',
      tab: 'developNo',
      demandModel: createDemandModel(),
      dialogOpen: false,
      //rimplTask_state: null,
      flag: false,
      demand_type: '',
      deferModel: {
        deferReason: ''
      },
      fileFlag: false,
      isDeleted: false,
      currentName: '',
      demandStatusNormal: '',
      assessmentDialogShow: false,
      detailData: {},
      editFlag: false,
      editFlaTip: '',
      helpFlag: false,
      unitCheck: false,
      checkImplUnitNum: '',
      checkUrl: ''
    };
  },
  validations: {
    deferModel: {
      deferReason: {
        required
      }
    }
  },
  watch: {
    'demandModel.demand_status_normal': {
      handler: function(val) {
        this.demandStatusNormal = demandStatus[val] ? demandStatus[val] : '';
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('demandsForm', ['demandInfoDetail', 'assessExcel']),
    ...mapGetters('user', ['isDemandManager']),
    checkStatus() {
      return (
        this.demandInfoDetail.demand_status_normal === 2 ||
        !this.isDemandManager
      );
    },
    demandStatu() {
      let status = '';
      if (
        this.demandModel.demand_status_special != 1 &&
        this.demandModel.demand_status_special != 2
      ) {
        status = this.demandStatusNormal;
      }
      if (
        this.demandModel.demand_status_special == 1 ||
        this.demandModel.demand_status_special == 2
      ) {
        status =
          demandStatusSpecial[this.demandModel.demand_status_special] || '';
      }
      return status;
    }
  },
  filters: {
    assessStatusFilter(val) {
      return assessStatusMap[val] ? assessStatusMap[val] : '';
    },
    demandStatusSpecialFilter(val) {
      return demandStatusSpecial[val] ? demandStatusSpecial[val] : '';
    },
    assessUserFilter(val) {
      if (val) {
        let assessUserAll = val;
        let assessUserList = [];
        assessUserAll.forEach(item => {
          assessUserList.push(item.user_name_cn);
        });
        return (val = assessUserList.join(','));
      }
    },
    designStatusFilter(val) {
      return designStatusMap[val] ? designStatusMap[val] : '';
    },
    demandAssessWayFilter(val) {
      return demandAssessWayMap[val] ? demandAssessWayMap[val] : '';
    },
    futrueAssessFilter(val) {
      return futrueAssessMap[val] ? futrueAssessMap[val] : '';
    },
    priorityFilter(val) {
      return priValue[val] ? priValue[val] : '';
    },
    demand_propertyFilter(val) {
      let obj1 = {
        advancedResearch: '预研',
        keyPoint: '重点',
        routine: '常规'
      };
      return obj1[val] ? obj1[val] : '';
    },
    demandAvalableFilter(val) {
      return demandAvalableMap[val] ? demandAvalableMap[val] : '';
    },
    statesFilter(val) {
      return demandStatus[val] ? demandStatus[val] : '';
    }
  },
  methods: {
    ...mapActions('demandsForm', {
      deleteRqr: 'deleteRqr',
      fileRqr: 'fileRqr',
      queryDemandInfoDetail: 'queryDemandInfoDetail',
      defer: 'defer',
      recover: 'recover'
    }),
    ...mapActions('user', {
      queryCurrent: 'fetchCurrent'
    }),
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
    isOtherType(key) {
      if (key && key.indexOf('其他') !== -1) {
        return true;
      } else return false;
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
    editShowAble() {
      // return (
      //   this.isDemandManager ||
      //   this.isKaDianManager ||
      //   this.isDemandLeader() ||
      //   this.isIncludeCurrentUser()
      // );
      if (this.isDemandManager || this.isDemandLeader()) {
        return true;
      } else {
        return false;
      }
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
    handleDialogOpen(id) {
      this.$router.push({
        path: `/rqrmn/rqrEdit/${id}`
      });
    },
    //执行归档
    async handleFile() {
      this.$q
        .dialog({
          title: `归档确认`,
          message: `确认要归档本条需求信息吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.fileRqr({ id: this.id });
          successNotify('归档成功！');
          this.fileFlag = true;
          await this.queryDemandInfoDetail({ id: this.id });
          this.demandModel = this.demandInfoDetail;
        });
    },
    handleDeleteRequirement() {
      this.$q
        .dialog({
          title: `撤销确认`,
          message: `确认要撤销本条需求信息吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          //执行删除
          await this.deleteRqr({ id: this.id });
          successNotify('撤销成功！');
          this.isDeleted = true;
          this.$router.push('/rqrmn/list');
        });
    },
    handleTipAll() {
      this.$v.deferModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('deferModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.deferModel.$invalid) {
        return;
      }
    },
    async init() {
      await this.queryDemandInfoDetail({ id: this.id });
      this.demandModel = this.demandInfoDetail;
      this.demand_type = this.demandInfoDetail.demand_type;
      this.currentName = this.currentUser.user_name_cn;
      //获取设计稿状态
      this.designStatus = this.demandInfoDetail.design_status;
      this.queryUnitIsCheck();
    },
    helpDailog() {
      this.helpFlag = true;
    },
    //查询需求下当前登录人是否有牵头的实施单元未核算
    async queryUnitIsCheck() {
      let respone = await queryIpmpUnitIsCheck({
        informationNum: this.demandModel.oa_contact_no
      });
      this.unitCheck = respone.implUnitNum ? true : false;
      this.checkImplUnitNum = respone.implUnitNum;
      this.checkUrl = respone.url;
    },
    goIpmp() {
      window.open(this.checkUrl);
    }
  },
  created() {
    //如果是从任务详情pipeline提交测试弹窗跳转过来,tab页展示需规页。
    if (this.$route.query.tab === 'file') {
      this.tab = 'file';
    } else if (this.$route.query.tab === 'developNo') {
      this.tab = 'developNo';
    }
  },
  mounted() {
    this.id = this.$route.params.id;
    this.init();
  }
};
</script>
<style lang="stylus" scoped>


.relatePartClass {
  font-size: 14px;
  color: #DDDDDD;
}

.content > .column
  flex-wrap: nowrap;

.detail
  word-break: break-all;
  font-size: 14px;
  color: #616161
.btn-radius .q-btn
 border-radius 0 0 0 0
.mgT16
  margin-top 16px
.title
  font-size 16px
  padding 10px
  font-weight 700
.mgr10
  margin-right 10px
  line-height 36px
.mgl10
  margin-left 10px
.labStyle
  margin-left 13px
  width 136px
  line-height 36px
.textStyle
  text-indent 36px
  text-shadow 5px 5px 5px #ccc
.pdr5
  padding-right 8px
.flex1
  flex 1
.break-word
  word-break: break-all;
.maxWidth
  max-width calc(100% - 100px)
</style>
