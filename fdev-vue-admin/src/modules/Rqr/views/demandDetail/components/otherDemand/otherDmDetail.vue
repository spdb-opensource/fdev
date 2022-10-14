<template>
  <div class="q-gutter-md">
    <f-block block>
      <div class="row items-start">
        <div class="text-h5 text-title">
          {{ itemDetail.taskName }}
          <fdev-badge>{{ getStatusName(itemDetail.status) }}</fdev-badge>
          <div class="text-subtitle1 text-title">
            {{ itemDetail.taskNum }}
          </div>
        </div>
        <fdev-space />
        <div class="row">
          <div>
            <fdev-tooltip
              v-if="isEdit()"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ showEditDis }}</span>
            </fdev-tooltip>
            <fdev-btn
              dialog
              outline
              ficon="compile"
              label="编辑"
              :disable="isEdit()"
              @click="updateUnit"
            />
          </div>
          <!-- <div v-if="demandModel.isTransferRqrmnt !== '1'" class="mgl10">
            <fdev-tooltip
              v-if="isAssessment().flag"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ isAssessment().msg }}</span>
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
              :disable="isAssessment().flag"
              @click="assessmentBtnClick"
            />
          </div> -->
          <!-- 老需求评估阶段不限权限 -->
          <!-- <div class="mgl10" v-else>
            <fdev-tooltip
              v-if="isAssessment().flag"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>isAssessment().msg</span>
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
              :disable="isAssessment().flag"
              @click="assessmentBtnClick"
            />
          </div> -->
        </div>
      </div>
    </f-block>
    <!-- 基础数据 -->
    <f-block block>
      <div class="text-h6">其他需求任务基础信息</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem page label="任务负责人:"
            >{{ itemDetail.taskLeaderName }}
          </f-formitem>
          <f-formitem page label="实施牵头单位:"
            >{{ itemDetail.headerUnitName }}
          </f-formitem>
          <f-formitem page label="实施牵头团队:">
            {{ itemDetail.headerTeamName }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem page label="项目/任务集名称:">
            {{ itemDetail.planPrjName }}
          </f-formitem>
          <f-formitem page label="项目编号:">
            {{ itemDetail.prjNum }}
          </f-formitem>
          <f-formitem page label="牵头小组:"
            >{{ itemDetail.leaderGroupName }}
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
            label-style="width:190px"
            label="行内人员预期工作量（人月）:"
          >
            {{ itemDetail.expectOwnWorkload || '' }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem
            label-style="width:190px"
            label="公司人员预期工作量（人月）:"
          >
            {{ itemDetail.expectOutWorkload || '' }}
          </f-formitem>
        </div>
      </div>
    </f-block>
    <!-- 安排与实施 -->
    <f-block block>
      <div class="text-h6">实施安排及情况</div>
      <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
        <div class="col">
          <f-formitem label="计划启动日期:">
            {{ itemDetail.planStartDate }}
          </f-formitem>
          <f-formitem label="实际启动日期:">
            {{ itemDetail.actualStartDate }}
          </f-formitem>
        </div>
        <div class="col">
          <f-formitem label="计划完成日期:">
            {{ itemDetail.planDoneDate }}
          </f-formitem>
          <f-formitem label="实际完成日期:">
            {{ itemDetail.actualDoneDate }}
          </f-formitem>
        </div>
      </div>
    </f-block>
    <updateOtherDmTaskDlg
      v-if="users"
      :userLists="users"
      :leadGroups="leadGroups"
      :demandDetail="demandModel"
      :taskNum="taskNum"
      v-model="editFlag"
      @close="closeEditDlg"
    ></updateOtherDmTaskDlg>
  </div>
</template>

<script>
import { formatOption } from '@/utils/utils';
import { formatUser } from '@/modules/User/utils/model';
import updateOtherDmTaskDlg from './updateOtherDmTaskDlg';
import { mapState, mapGetters, mapActions } from 'vuex';
import { queryOtherDemandTask } from '@/modules/Rqr/services/methods';
export default {
  name: 'otherDmTaskDetail',
  components: {
    updateOtherDmTaskDlg
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    ...mapGetters('user', ['isDemandManager', 'isLoginUserList']),
    ...mapState('demandsForm', ['demandInfoDetail'])
  },
  data() {
    return {
      itemDetail: {},
      editFlag: false,
      demandModel: {},
      taskNum: '',
      showEditDis: '',
      leadGroups: [],
      users: null
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    ...mapActions('demandsForm', {
      queryDemandInfoDetail: 'queryDemandInfoDetail'
    }),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    ...mapActions('userForm', ['fetchGroup']),
    getStatusName(val) {
      if (val === 'notStart') return '未开始';
      else if (val === 'going') return '进行中';
      else if (val === 'done') return '已完成';
      else if (val === 'delete') return '删除';
      else return '-';
    },
    async getODTaskDetail() {
      let data = await queryOtherDemandTask({
        taskNum: this.taskNum
      });
      this.itemDetail = data;
    },
    async init() {
      await this.fetchGroup();
      this.leadGroups = formatOption(this.groups);
      await this.getUserList();
      this.taskNum = this.$route.params.id;
      await this.queryDemandInfoDetail({ id: this.$route.params.demandId });
      this.demandModel = this.demandInfoDetail;
      this.getODTaskDetail();
    },
    isEdit() {
      if (this.demandModel.demand_status_normal == '8') {
        this.showEditDis = '已归档阶段不可编辑';
        return true;
      } else if (!this.itemDetail.status === 'done') {
        this.showEditDis = '当前任务状态不可编辑';
        return true;
      } else if (!this.itemDetail.isUpdate) {
        this.showEditDis = '当前用户不可编辑';
        return true;
      } else {
        return false;
      }
    },
    isUnitLeader() {
      if (!this.itemDetail.implLeader) return false;
      let arr = this.itemDetail.implLeader.split(',');
      return arr.some(item => {
        return (
          item.toLowerCase() === this.currentUser.user_name_en.toLowerCase()
        );
      });
    },
    closeEditDlg(flag) {
      this.editFlag = false;
      if (flag) {
        this.getODTaskDetail();
      }
    },
    updateUnit() {
      this.editFlag = true;
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
    isAssessment() {
      let rtn = { flag: false, msg: '' };
      if (
        this.demandModel &&
        (this.demandModel.demand_status_special === 1 ||
          this.demandModel.demand_status_normal >= 7) &&
        this.demandModel.demand_status_special !== 2
      ) {
        rtn.flag = true;
        rtn.msg = '该阶段不可评估';
        return rtn;
      } else if (
        this.demandModel &&
        this.demandModel.demand_status_normal === 8
      ) {
        rtn.flag = true;
        rtn.msg = '已归档的存量需求不可评估';
        return rtn;
      } else if (
        this.demandModel.demand_leader_group == '' ||
        !this.demandModel.demand_leader ||
        this.demandModel.demand_leader.length === 0 ||
        !this.demandModel.relate_part ||
        this.demandModel.relate_part.length === 0 ||
        this.isEditMessage()
      ) {
        rtn.flag = true;
        rtn.msg =
          '需求牵头小组、实施牵头人、涉及小组及涉及小组评估人信息不全，请前往编辑页面补充完整';
        return rtn;
      }
      return rtn;
    },
    //点击评估按钮
    assessmentBtnClick() {
      // 评估 路由更换为 需求详情 下的 研发单元(developNo)tab
      this.$router.push({
        path: `/rqrmn/rqrProfile/${this.demandModel.id}`,
        query: { tab: 'developNo' }
      });
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
    async getUserList() {
      await this.queryUser();
      this.users = this.isLoginUserList.map(user =>
        formatOption(formatUser(user), 'name')
      );
    }
  },
  filters: {
    modelFlag(val) {
      let model = '';
      if (val === 'implunit.dev.mode.01') {
        model = '敏态';
      } else if (val === 'implunit.dev.mode.02') {
        model = '稳态';
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
  color: #616161
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
</style>
