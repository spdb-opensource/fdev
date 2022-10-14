<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    title="修改任务"
  >
    <div class="rdia-dc-w row justify-between">
      <f-formitem class="q-pr-sm" label="名称" required>
        <fdev-input
          v-model="$v.createModel.taskName.$model"
          ref="createModel.taskName"
          maxlength
          :rules="[
            () => $v.createModel.taskName.required || '请输入任务名',
            () => $v.createModel.taskName.maxText || '输入不能超过100'
          ]"
        />
      </f-formitem>
      <f-formitem label="任务分类" required>
        <fdev-input
          disable
          v-model="createModel.taskClassify"
          :rules="[() => true]"
        />
      </f-formitem>
      <f-formitem label="实施牵头单位" required>
        <fdev-select
          ref="createModel.headerUnitName"
          :options="leadDeptOptions"
          @input="getteamLists"
          v-model="$v.createModel.headerUnitName.$model"
          :rules="[
            () => $v.createModel.headerUnitName.required || '请选择实施牵头单位'
          ]"
        />
      </f-formitem>
      <f-formitem label="任务类型">
        <span class="task-type">{{ createModel.taskType }}</span>
      </f-formitem>
      <f-formitem label="实施牵头团队" required>
        <fdev-select
          ref="createModel.headerTeamName"
          :options="leadTeamOptions"
          v-model="$v.createModel.headerTeamName.$model"
          :rules="[
            () => $v.createModel.headerTeamName.required || '请选择实施牵头团队'
          ]"
        />
      </f-formitem>
      <f-formitem label="任务负责人" required>
        <fdev-select
          ref="createModel.taskLeader"
          use-input
          v-model="$v.createModel.taskLeader.$model"
          :options="userOptions"
          @filter="userFilter"
          :rules="[
            () => $v.createModel.taskLeader.required || '请选择任务负责人'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label>
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption>
                  {{ scope.opt.user_name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template></fdev-select
        >
      </f-formitem>
      <f-formitem label="牵头小组" required>
        <fdev-select
          ref="createModel.leaderGroup"
          use-input
          :options="groupOptions"
          @filter="groupFilter"
          v-model="$v.createModel.leaderGroup.$model"
          :rules="[
            () => $v.createModel.leaderGroup.required || '请选择牵头小组'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label>{{ scope.opt.name }}</fdev-item-label>
                <fdev-item-label caption>
                  <span class="te-desc ellipsis">
                    {{ scope.opt.fullName }}
                  </span>
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem label="厂商负责人">
        <fdev-select
          use-input
          v-model="createModel.firmLeader"
          :options="userFirmOptions"
          @filter="userfirmFilter"
          :rules="[() => true]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label>
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption>
                  {{ scope.opt.user_name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template></fdev-select
        >
      </f-formitem>
      <f-formitem label="计划启动日期" required>
        <fdev-input
          disable
          v-model="createModel.planStartDate"
          :rules="[() => true]"
        />
      </f-formitem>
      <f-formitem label="计划完成日期" required>
        <fdev-input
          disable
          v-model="createModel.planDoneDate"
          :rules="[() => true]"
        />
      </f-formitem>
      <f-formitem label="项目/任务集" required>
        <fdev-input disable v-model="planPrjName" :rules="[() => true]" />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog label="修改" @click="submitForm" />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { createOtherDmTaskModel, taskCsLists } from '@/modules/Rqr/model.js';
import { validate, formatOptionAdd } from '@/utils/utils';
import {
  updateDemandTask,
  queryOtherDemandTask,
  queryIpmpLeadTeam
} from '@/modules/Rqr/services/methods';
export default {
  name: 'updateOtherDmTaskDlg',
  data() {
    return {
      createModel: createOtherDmTaskModel(),
      taskCsOptions: taskCsLists,
      leadDeptOptions: [],
      allDeptObj: {},
      allTeamLists: [],
      leadTeamOptions: [],
      ipmpProjectOptios: [],
      proLists: [],
      allUserOptions: [], //全部负责人
      userOptions: [], //负责人选项
      allUserFirmOptions: [], //全部厂商
      userFirmOptions: [], //厂商
      groupOptions: [],
      planPrjName: '' //任务集名称
    };
  },
  validations: {
    createModel: {
      taskName: {
        required,
        maxText(val) {
          return val.length <= 100;
        }
      },
      taskClassify: {
        required
      },
      headerUnitName: {
        required
      },
      prjNum: {
        required
      },
      headerTeamName: {
        required
      },
      taskLeader: {
        required
      },
      leaderGroup: {
        required
      },
      planStartDate: {
        required
      },
      planDoneDate: {
        required
      }
    }
  },
  props: {
    userLists: {
      type: Array
    },
    leadGroups: {
      type: Array
    },
    value: {
      type: Boolean
    },
    confirm: {
      type: Boolean,
      default: true
    },
    taskNum: {
      type: String
    },
    demandDetail: {
      type: Object
    }
  },
  watch: {
    value(val) {
      if (val && this.taskNum) {
        this.getTaskDetail(this.taskNum);
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('demandsForm', ['ipmpProject'])
  },
  methods: {
    ...mapActions('releaseForm', ['addReview']),
    // 日期选项
    startTimeOptions(date) {
      if (this.createModel.planDoneDate) {
        return date <= this.createModel.planDoneDate.replace(/-/g, '/');
      }
      return true;
    },
    endTimeOptions(date) {
      if (this.createModel.planStartDate) {
        return date >= this.createModel.planStartDate.replace(/-/g, '/');
      }
      return true;
    },
    userOptionsFilter(param) {
      let myuser = this.userLists.filter(item => {
        let flag = false;
        let role_labels = [];
        item.role.forEach(ele => {
          role_labels.push(ele.name);
        });
        param.forEach(r => {
          if (role_labels.includes(r)) {
            flag = true;
          }
        });
        return flag;
      });
      return myuser;
    },
    async userFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.allUserOptions.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async userfirmFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userFirmOptions = this.allUserFirmOptions.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async groupFilter(val, update, abort) {
      if (!val) {
        update();
        this.groupOptions = this.leadGroups;
        return;
      }
      update(() => {
        const needle = val.toLowerCase();
        this.groupOptions = this.leadGroups.filter(
          v =>
            v.fullName.toLowerCase().indexOf(needle) > -1 ||
            v.fullName.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    filterProNo(val, update, abort) {
      if (!val) {
        update();
        this.ipmpProjectOptios = this.proLists;
        return;
      }
      update(() => {
        const needle = val.toLowerCase();
        this.ipmpProjectOptios = this.proLists.filter(
          v =>
            v.project_name.toLowerCase().indexOf(needle) > -1 ||
            v.project_no.toLowerCase().indexOf(needle) > -1
        );
      });
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
    submitForm() {
      if (!this.verifyModel()) {
        return;
      }
      this.saveOtherDmTask();
    },
    getStatusName(val) {
      if (val === 'notStart') return '未开始';
      else if (val === 'going') return '进行中';
      else if (val === 'done') return '已完成';
      else if (val === 'delete') return '删除';
      else return '-';
    },
    //获取详情信息
    async getTaskDetail(taskNum) {
      await this.getUnitLists();
      const res = await queryOtherDemandTask({ taskNum });
      this.createModel.id = res.id;
      this.createModel.demandId = res.demandId;
      this.createModel.taskNum = res.taskNum;
      //牵头团队
      this.createModel.headerTeamName = res.headerTeamName;
      this.createModel.headerUnitName = res.headerUnitName;
      this.createModel.taskName = res.taskName;
      this.createModel.taskClassify = res.taskClassify;
      this.createModel.status = this.getStatusName(res.status);
      //项目/任务集
      this.planPrjName = res.planPrjName || '';
      this.createModel.prjNum = {
        value: res.prjNum,
        project_name: res.planPrjName || ''
      };
      //牵头小组
      this.createModel.leaderGroup = {
        value: res.leaderGroup,
        label: res.leaderGroupName || ''
      };

      //任务负责人
      this.createModel.taskLeader = {
        value: res.taskLeaderId,
        label: res.taskLeaderName || ''
      };
      //厂商负责人
      this.createModel.firmLeader = {
        value: res.firmLeaderId,
        label: res.firmLeaderName || ''
      };

      this.createModel.planDoneDate = res.planDoneDate;
      this.createModel.planStartDate = res.planStartDate;
      this.getteamLists();
    },
    // 修改
    async saveOtherDmTask() {
      let params = {
        id: this.createModel.id,
        demandId: this.demandDetail.id, //需求编号
        taskNum: this.createModel.taskNum, //任务编号
        taskName: this.createModel.taskName, //任务名称
        taskType: this.createModel.taskType, //任务类型
        taskClassify: this.createModel.taskClassify, //任务分类
        prjNum: this.createModel.prjNum.value, //项目/任务集
        planPrjName: this.createModel.prjNum.project_name, //项目/任务集名称
        headerUnitName: this.createModel.headerUnitName, //牵头单位
        headerTeamName: this.createModel.headerTeamName, //牵头团队
        leaderGroup: this.createModel.leaderGroup.value, //牵头小组
        leaderGroupName: this.createModel.leaderGroup.label, //牵头小组中文名
        taskLeaderId: this.createModel.taskLeader.value, //任务负责人ID
        taskLeaderName: this.createModel.taskLeader.label, //任务负责人中文姓名
        planStartDate: this.createModel.planStartDate, //计划启动日期
        planDoneDate: this.createModel.planDoneDate //计划完成日期
      };
      if (this.createModel.firmLeader) {
        params.firmLeaderId = this.createModel.firmLeader.value; //厂商负责人ID
        params.firmLeaderName = this.createModel.firmLeader.label; //厂商负责人中文姓名
      } else {
        params.firmLeaderId = ''; //厂商负责人ID
        params.firmLeaderName = ''; //厂商负责人中文姓名
      }
      await updateDemandTask(params);
      this.$emit('close', true);
    },
    confirmToClose() {
      this.$emit('close', false);
    },
    async usersAndGroups() {
      this.groupOptions = this.leadGroups;
      this.userOptions = this.userOptionsFilter(['行内项目负责人']);
      this.allUserOptions = this.userOptionsFilter(['行内项目负责人']);
      this.userFirmOptions = this.userOptionsFilter(['厂商项目负责人']);
      this.allUserFirmOptions = this.userOptionsFilter(['厂商项目负责人']);
    },
    //项目编号列表
    async projectList() {
      this.proLists = formatOptionAdd(
        this.ipmpProject,
        'project_no',
        'project_no'
      );
      this.ipmpProjectOptios = this.proLists;
    },
    async getUnitLists() {
      const res = await queryIpmpLeadTeam({});
      if (res) {
        this.leadDeptOptions = [];
        let temObj = {};
        this.allDeptObj = {};
        this.allTeamLists = [];
        for (let i = 0; i < res.length; i++) {
          let item = res[i];
          if (item.dept_id === item.dept_name) {
            continue;
          }
          let key = item.dept_id;
          this.allTeamLists.push(item.team_name);
          temObj[key] = item.dept_name;
          let name = item.dept_name;
          if (this.allDeptObj[name]) {
            this.allDeptObj[name].push(item);
          } else {
            this.allDeptObj[name] = [item];
          }
        }
        for (let key in temObj) {
          this.leadDeptOptions.push(temObj[key]);
        }
      }
    },
    getteamLists() {
      let name = this.createModel.headerUnitName;
      let list = this.allDeptObj[name];
      if (list) {
        this.leadTeamOptions = [];
        for (let i = 0; i < list.length; i++) {
          this.leadTeamOptions.push(list[i].team_name);
        }
      } else {
        this.leadTeamOptions = this.allTeamLists;
      }
    }
  },
  created() {
    //查询项目任务集
    this.projectList();
  },
  mounted() {
    this.usersAndGroups();
  }
};
</script>

<style lang="stylus" scoped>

.dialog-wrapper
  margin-top 50px
  box-sizing border-box
  max-height calc(100vh - 98px)
  overflow auto
.file-wrapper
  display inline-block
  width calc(100% - 20px)
  margin 0
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: bottom;
.icon
  cursor pointer
  padding 3px
  border-radius 50%
.icon:hover
  background #BBBBBB
.task-type{
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
}
</style>
