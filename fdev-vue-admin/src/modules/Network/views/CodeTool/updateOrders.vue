<template>
  <f-block page>
    <Loading class="orderPage" :visible="loading">
      <div class="tileName">编辑工单</div>
      <div v-if="!isCheckRole()">
        <div class="row items-center titleBox">
          <f-icon name="list_s_f" class="titleimg" />
          <div class="titlename">基础信息</div>
        </div>
        <div class="row justify-between mItem ">
          <f-formitem
            label="牵头人"
            bottom-page
            required
            label-style="width:86px"
          >
            <fdev-select
              :disable="isDisableBtn()"
              use-input
              ref="createModel.leader"
              v-model="$v.createModel.leader.$model"
              :options="userOptions"
              @filter="userFilter"
              :rules="[() => $v.createModel.leader.isOk || '请选择牵头人']"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label
                      :title="
                        `${scope.opt.user_name_en}--${scope.opt.groupName}`
                      "
                      caption
                    >
                      {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="计划投产日期"
            bottom-page
            required
            label-style="width:86px"
          >
            <f-date
              :disable="isDisableBtn()"
              ref="createModel.planProductDate"
              mask="YYYY-MM-DD"
              v-model="$v.createModel.planProductDate.$model"
              :rules="[
                () =>
                  $v.createModel.planProductDate.isOk || '请选择计划投产时间'
              ]"
            />
          </f-formitem>
          <f-formitem label="期望审核日期" bottom-page label-style="width:86px">
            <f-date
              mask="YYYY-MM-DD"
              hint=""
              :disable="isDisableBtn()"
              v-model="createModel.expectAuditDate"
              :options="expectAuditDateOptions"
            />
          </f-formitem>
        </div>
        <div class="row items-center titleBox">
          <f-icon name="list_s_f" class="titleimg" />
          <div class="titlename">相关需求及任务</div>
        </div>
        <div class="row justify-between mItem ">
          <f-formitem
            class="mRqrname"
            label="对应需求"
            bottom-page
            required
            label-style="width:86px;height:22px"
            value-style="height:22px;"
          >
            <div class="rqrName ellipsis" :title="detailObj.demandName">
              <router-link
                class="link"
                v-if="detailObj.demandId"
                :to="`/rqrmn/rqrProfile/${detailObj.demandId}`"
                >{{ detailObj.demandName || '-' }}</router-link
              >
              <span v-else> {{ detailObj.demandName || '-' }}</span>
            </div>
          </f-formitem>
        </div>
        <div class="row justify-between mItem ">
          <f-formitem
            label="涉及任务"
            class="mRqrTask"
            bottom-page
            required
            label-style="width:86px"
            value-style="width:800px"
          >
            <fdev-select
              :disable="isDisableBtn()"
              ref="createModel.temTaskValue"
              :options="taskOptions"
              @filter="taskFilter"
              option-label="name"
              option-value="id"
              v-model="$v.createModel.temTaskValue.$model"
              @input="updateTaskLists"
              :rules="[
                () => $v.createModel.temTaskValue.isHas || '请选择涉及任务'
              ]"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name">{{
                      scope.opt.name
                    }}</fdev-item-label>
                    <fdev-item-label caption>
                      <span
                        class="te-desc ellipsis"
                        :title="scope.opt.redmine_id"
                      >
                        {{ scope.opt.redmine_id }}
                      </span>
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
            <div
              class="row taskItem items-center justify-between mb-16"
              v-for="item in createModel.taskIds"
              :title="item.name"
              :key="item.id"
            >
              <div class="taskName ellipsis">{{ item.name }}</div>
              <f-icon
                style="cursor:pointer;"
                name="close"
                class="taskImg"
                @click="removeTask(item)"
              />
            </div>
          </f-formitem>
        </div>
        <div class="row items-center titleBox">
          <f-icon name="list_s_f" class="titleimg" />
          <div class="titlename">审核内容</div>
        </div>
        <f-formitem
          class="formItem"
          label="审核内容"
          bottom-page
          required
          label-style="width:86px"
        >
          <fdev-input
            ref="createModel.auditContent"
            :disable="isDisableBtn()"
            v-model="$v.createModel.auditContent.$model"
            type="textarea"
            :rules="[
              () => $v.createModel.auditContent.required || '请输入审核内容',
              () =>
                $v.createModel.auditContent.maxLen || '输入内容不能超过400字'
            ]"
          />
        </f-formitem>
      </div>
      <div v-else>
        <div class="row items-center titleBox">
          <f-icon name="list_s_f" class="titleimg" />
          <div class="titlename">审核内容及结论</div>
        </div>
        <div class="row justify-between mItem">
          <f-formitem
            label="工单状态"
            bottom-page
            required
            label-style="width:86px"
          >
            <fdev-select
              :disable="isDisableBtn()"
              ref="createModel.orderStatus"
              v-model="$v.createModel.orderStatus.$model"
              :options="statusOptions"
              :rules="[
                () => $v.createModel.orderStatus.isOk || '请选择工单状态'
              ]"
            >
            </fdev-select>
          </f-formitem>
          <f-formitem label="审核完成时间" bottom-page label-style="width:86px">
            <span>{{ detailObj.auditFinishTime }}</span>
          </f-formitem>
        </div>
        <div>
          <f-formitem
            class="formItem"
            label="审核内容"
            bottom-page
            required
            label-style="width:86px"
          >
            <fdev-input
              ref="createModel.auditContent"
              :disable="isDisableBtn()"
              v-model="$v.createModel.auditContent.$model"
              type="textarea"
              :rules="[
                () => $v.createModel.auditContent.required || '请输入审核内容',
                () =>
                  $v.createModel.auditContent.maxLen || '输入内容不能超过400字'
              ]"
            />
          </f-formitem>
          <f-formitem
            class="formItem"
            label="审核结论"
            bottom-page
            label-style="width:86px"
          >
            <fdev-input
              :disable="isDisableBtn()"
              v-model="createModel.auditResult"
              type="textarea"
            />
          </f-formitem>
          <f-formitem
            class="formItem"
            label="投产问题描述"
            bottom-page
            label-style="width:86px"
          >
            <fdev-input
              :disable="isDisableBtn(true)"
              v-model="createModel.productProblem"
              type="textarea"
            />
          </f-formitem>
        </div>
      </div>
      <div :class="[isCheckRole() ? 'mtop40' : 'mtop24', 'op-btn']">
        <fdev-btn
          outline
          class="q-mr-lg"
          label="取消"
          @click="confirmToCancle"
        />
        <span>
          <fdev-tooltip
            v-if="isDisableBtn(null, true)"
            anchor="top middle"
            self="center middle"
            :offest="[-20, 0]"
          >
            <span>{{ getTipMsg() }}</span>
          </fdev-tooltip>
          <fdev-btn
            :disable="isDisableBtn(null, true)"
            :loading="loading"
            label="确定"
            @click="submitForm"
          />
        </span>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState } from 'vuex';
import {
  resolveResponseError,
  successNotify,
  formatOption,
  validate
} from '@/utils/utils';
import {
  createOrdersModel,
  queryUserOptionsParams,
  statusOptions
} from '@/modules/Network/utils/constants';
import {
  updateOrders,
  queryOrderById
} from '@/modules/Network/services/methods';
import { queryUserCoreData } from '@/modules/Rqr/services/methods';
import { queryTaskByDemandId } from '@/services/job';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'updateOrder',
  components: { Loading },
  data() {
    return {
      loading: false,
      id: this.$route.params.id, //工单id
      createModel: createOrdersModel(),
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      detailObj: {},
      taskLists: [],
      taskOptions: [],
      statusOptions: statusOptions,
      updateButton: '3'
      /*非代码审核角色，工单非终态，返回0，仅能编辑部分字段。
           代码审核角色，工单非终态，返回1，可编辑所有字段。
           代码审核角色，工单终态，返回2，仅可编辑生产问题描述。
           非代码审核角色，工单终态，返回3，不可编辑*/
    };
  },
  validations: {
    createModel: {
      leader: {
        isOk(val) {
          if (this.isCheckRole()) {
            return true;
          } else {
            return val;
          }
        }
      },
      planProductDate: {
        isOk(val) {
          if (this.isCheckRole()) {
            return true;
          } else {
            return val;
          }
        }
      },
      temTaskValue: {
        isHas(val) {
          if (this.isCheckRole()) {
            return true;
          } else {
            return this.createModel.taskIds.length > 0;
          }
        }
      },
      orderStatus: {
        isOk(val) {
          if (!this.isCheckRole()) {
            return true;
          } else {
            return val;
          }
        }
      },
      auditContent: {
        required,
        maxLen(val) {
          return val.length <= 400;
        }
      }
    }
  },
  props: {},
  watch: {},
  computed: {
    ...mapState('user', ['currentUser'])
  },
  methods: {
    async userFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.users.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    taskFilter(val, update) {
      update(() => {
        this.taskOptions = this.taskLists.filter(
          tag =>
            tag.taskType != '1' &&
            tag.taskType != '2' &&
            tag.name.indexOf(val) > -1
        );
      });
    },
    updateTaskLists() {
      let isHas = false;
      if (this.createModel.temTaskValue) {
        for (let i = 0; i < this.createModel.taskIds.length; i++) {
          if (
            this.createModel.taskIds[i].id === this.createModel.temTaskValue.id
          ) {
            isHas = true;
            break;
          }
        }
        if (!isHas) {
          this.createModel.taskIds.push({
            id: this.createModel.temTaskValue.id,
            name: this.createModel.temTaskValue.name
          });
        }
      }
    },
    verifyModel() {
      try {
        let unitModuleKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.indexOf('createModel') > -1;
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
    getTipMsg() {
      //0可编辑部分字段，1可编辑全部字段，2仅可编辑生产问题描述，3不可编辑（终态下非代码审核角色不可编辑）
      if (this.updateButton == '3') {
        return '终态下非代码审核角色不可编辑';
      } else {
        return '';
      }
    },
    isDisableBtn(isDesc, isbtn) {
      //0可编辑部分字段，1可编辑全部字段，2仅可编辑生产问题描述，3不可编辑（终态下非代码审核角色不可编辑）
      if (this.updateButton == '0') {
        return false;
      } else if (this.updateButton == '1') {
        return false;
      } else if (this.updateButton == '2') {
        if (isDesc) {
          return false;
        } else if (isbtn) {
          return false;
        } else return true;
      } else if (this.updateButton == '3') {
        return true;
      }
    },
    submitForm() {
      if (!this.verifyModel()) {
        return;
      }
      this.saveOrders();
    },
    isCheckRole() {
      return this.updateButton === '1' || this.updateButton === '2';
    },
    // 编辑
    async saveOrders() {
      this.loading = true;
      let params = {
        id: this.id,
        leader: this.createModel.leader.value,
        planProductDate: this.createModel.planProductDate,
        orderStatus: this.createModel.orderStatus.value,
        auditContent: this.createModel.auditContent,
        auditResult: this.createModel.auditResult,
        productProblem: this.createModel.productProblem,
        expectAuditDate: this.createModel.expectAuditDate
      };
      //涉及任务
      if (this.createModel.taskIds.length > 0) {
        params.taskIds = [];
        for (let i = 0; i < this.createModel.taskIds.length; i++) {
          params.taskIds.push(this.createModel.taskIds[i].id);
        }
      }
      await resolveResponseError(() => updateOrders(params));
      this.loading = false;
      // 成功
      successNotify('修改成功!');
      this.$router.go(-1);
    },
    confirmToCancle() {
      this.$router.go(-1);
    },
    async getUsers() {
      let params = {
        status: queryUserOptionsParams.status
      };
      const res = await queryUserCoreData(params);
      this.users = res.map(user => formatOption(user, 'user_name_cn'));
      this.userOptions = this.users;
      this.getDetail();
    },
    getItemById(id, lists) {
      for (let i = 0; i < lists.length; i++) {
        if (id === lists[i].id) {
          return lists[i];
        }
      }
      return null;
    },
    getStatusName(val) {
      for (let i = 0; i < this.statusOptions.length; i++) {
        if (val == this.statusOptions[i].value) {
          return this.statusOptions[i].label;
        }
      }
      return '';
    },
    getNameById(id, lists) {
      for (let i = 0; i < lists.length; i++) {
        if (id == lists[i].id) {
          return lists[i].taskName;
        }
      }
      return '';
    },
    async getDetail() {
      let params = { id: this.id };
      const res = await queryOrderById(params);
      if (res) {
        this.detailObj = res;
        this.createModel.leader = { value: res.leader, label: res.leaderName };
        this.createModel.planProductDate = res.planProductDate;
        this.createModel.expectAuditDate = res.expectAuditDate;
        this.createModel.applyTime = res.applyTime;
        if (res.taskIds && res.taskIds.length > 0) {
          this.createModel.taskIds = [];
          for (let i = 0; i < res.taskIds.length; i++) {
            let item = { id: res.taskIds[i] };
            item.name = this.getNameById(res.taskIds[i], res.tasksInfo);
            this.createModel.taskIds.push(item);
          }
        }
        this.createModel.orderStatus = {
          value: res.orderStatus,
          label: this.getStatusName(res.orderStatus)
        };
        this.createModel.auditContent = res.auditContent;
        this.createModel.auditResult = res.auditResult;
        this.createModel.productProblem = res.productProblem;
        this.updateButton = res.updateButton;
        await this.getTaskLists(res.demandId);
      }
    },
    async init() {
      this.createModel = createOrdersModel();
      await this.getUsers();
    },
    async getTaskLists(demandId) {
      let params = { demandId };
      let res = await queryTaskByDemandId(params);
      if (res) {
        this.taskLists = res;
        this.taskOptions = res.filter(item => {
          return item.taskType != '1' && item.taskType != '2';
        });
      }
    },
    removeTask(row) {
      this.createModel.taskIds = this.createModel.taskIds.filter(item => {
        return item.id !== row.id;
      });
      if (this.createModel.taskIds.length === 0)
        this.createModel.temTaskValue = '';
    },
    //期望审核时间选项不大于申请时间
    expectAuditDateOptions(date) {
      return (
        date >= this.createModel.applyTime.substring(0, 10).replace(/-/g, '/')
      );
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.orderPage{
  padding-top: 10px ;
  padding-bottom: 24px;
}
.tileName{
  font-family: PingFangSC-Semibold;
  font-size: 18px;
  color: #333333;
  letter-spacing: 0;
  line-height: 18px;
  font-weight: 600;
  margin-bottom: 36px;
}
.titleBox{
  background: rgba(30,84,213,0.05);
  border-radius: 2px;
  border-radius: 2px;
  height: 54px;
  margin-bottom: 20px;
  padding: 0px 0px 0px 16px;
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
    }
}
.mItem{
  margin-right: 102px;
  margin-left: 36px;
}
.mb-16{
  margin-bottom: 16px;
}
.mRqrname{
  margin-bottom: 20px;
  .rqrName{
    height: 22px;
    line-height: 22px;
  }
}
.mRqrTask >>> .q-field--with-bottom{
  padding-bottom: 16px !important;
}
.taskItem{
  border: 1px solid #BBBBBB;
  border-radius: 2px;
  border-radius: 2px;
  height: 36px;
  padding: 10px 13px 11px 16px;
  .taskName{
    font-family: PingFangSC-Regular;
    font-size: 14px;
    color: #333333;
    letter-spacing: 0;
    line-height: 14px;
    max-width: 750px;
    height: 14px;
  }
  .taskImg{
    width: 10px;
    height: 10px;
  }
}
.formItem{
  padding: 0px 48px 20px 36px;
}
.mtop24{
    margin-top: 24px;
  }
.mtop40{
  margin-top: 40px;
}
.op-btn{
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
