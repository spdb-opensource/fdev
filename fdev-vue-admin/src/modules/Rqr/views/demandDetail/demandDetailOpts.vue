<template>
  <div class="q-gutter-md fixed-height">
    <div class="row items-start justify-between">
      <div class="flex">
        <!--归档 已投产,已归档状态显示归档按钮 -->
        <fdev-btn
          v-if="
            demandModel.demand_status_normal == 7 &&
              (isDemandManager || isDemandLeader())
          "
          dialog
          ficon="keep_file"
          label="归档"
          class="q-ml-md"
          @click="handleFile"
        />
        <!--编辑 需求管理员和需求牵头人可编辑，暂缓不可编辑 -->
        <div>
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
            class="q-ml-md"
            ficon="edit"
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
        <!-- 评估 -->
        <div v-if="tabs != 'developNo'">
          <div v-if="demandModel.isTransferRqrmnt !== '1'">
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
              class="q-ml-md"
              dialog
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
          <div v-else>
            <fdev-tooltip
              v-if="demandModel.demand_status_normal === 8"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>已归档的存量需求不可评估</span>
            </fdev-tooltip>
            <fdev-btn
              class="q-ml-md"
              dialog
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
        <div>
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
            class="q-ml-md"
            ficon="repeal"
            label="撤销"
            @click="handleDeleteRequirement"
            v-if="
              demandModel.demand_status_normal !== 9 &&
                (isDemandManager || isDemandLeader()) &&
                (demand_type === 'tech' || demand_type === 'daily')
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
          class="q-ml-md"
          ficon="repeal"
          label="已撤销"
          v-if="demandModel.demand_status_normal === 9"
          disable
        />
      </div>
    </div>
    <f-dialog v-model="editFlag" title="去编辑信息">
      <span>
        {{ editFlaTip }}
      </span>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="去编辑" @click="goRqrEdit()" v-close-popup />
      </template>
    </f-dialog>
    <f-dialog right v-model="helpFlag" title="需求详情帮助">
      <div>
        <img src="@/assets/demand-help.jpg" alt="" />
      </div>
    </f-dialog>
  </div>
</template>
<script>
import {
  createDemandModel,
  demandStatus,
  demandStatusDaily,
  demandStatusSpecial
} from '@/modules/Rqr/model.js';
import { mapActions, mapState, mapGetters } from 'vuex';
import { successNotify } from '@/utils/utils';
import { resolveResponseError } from '@/utils/utils';
import { queryDemandFile } from '@/modules/Rqr/services/methods.js';
export default {
  name: 'RqrProfile',
  data() {
    return {
      id: '',
      demandModel: createDemandModel(),
      demand_type: '',
      isDeleted: false,
      demandStatusNormal: '',
      editFlag: false,
      editFlaTip: '',
      helpFlag: false
    };
  },
  props: {
    tabs: {
      type: String,
      default: ''
    },
    demandDetailFromIndex: {
      type: Object,
      default: () => {}
    }
  },
  watch: {
    'demandModel.demand_status_normal': {
      handler: function(val) {
        if (this.demand_type == 'daily') {
          this.demandStatusNormal = demandStatusDaily[val]
            ? demandStatusDaily[val]
            : '';
        } else {
          this.demandStatusNormal = demandStatus[val] ? demandStatus[val] : '';
        }
      }
    },
    demandDetailFromIndex: {
      handler: function(n, o) {
        //删除 研发单元 回更新 需求状态
        this.demandModel = n; //更新后的值
      },
      deep: true
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
  methods: {
    ...mapActions('demandsForm', {
      deleteRqr: 'deleteRqr',
      fileRqr: 'fileRqr',
      queryDemandInfoDetail: 'queryDemandInfoDetail'
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
        this.$emit('goTodevelopNoFun');
      }
    },
    goRqrEdit() {
      this.$router.push(`/rqrmn/rqrEdit/${this.demandModel.id}`);
    },
    editShowAble() {
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
    // 编辑
    handleDialogOpen(id) {
      this.$router.push({
        path: `/rqrmn/rqrEdit/${id}`
      });
    },
    async checkIsExit() {
      let respone = await queryDemandFile({
        id: this.demandModel.id
      });
      return respone;
    },
    //执行归档
    async handleFile() {
      //检查需求监管文件是否存在
      let data = await resolveResponseError(() =>
        queryDemandFile({ id: this.demandModel.id })
      );
      if (data.length > 0) {
        this.$q
          .dialog({
            title: `需求文档缺失`,
            message: '缺失:' + '\t' + data.join('、') + '，请到需求文档库上传',
            ok: '确定',
            cancel: '取消'
          })
          .onOk(() => {
            //跳转链接到文档库
            this.$emit('goToFileFun');
          });
      } else {
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
            await this.queryDemandInfoDetail({ id: this.id });
            this.demandModel = this.demandInfoDetail;
            this.$emit('updateHeadAndTable', 'update');
          });
      }
    },
    // 撤销
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
    async initOpt() {
      // await this.queryDemandInfoDetail({ id: this.id });
      this.demandModel = this.demandDetailFromIndex;
      this.demand_type = this.demandDetailFromIndex.demand_type;
    },
    helpDailog() {
      this.helpFlag = true;
    }
  },
  mounted() {
    this.id = this.$route.params.id || this.$route.query.id;
    this.initOpt();
  }
};
</script>
<style lang="stylus" scoped>
.fixed-height
  margin-bottom 15px
  height 52px
</style>
