<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    title="新建其他需求任务"
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
        <fdev-select
          :options="taskCsOptions"
          ref="createModel.taskClassify"
          v-model="$v.createModel.taskClassify.$model"
          :rules="[
            () => $v.createModel.taskClassify.required || '请选择任务分类'
          ]"
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
          option-label="name"
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
          :rules="[]"
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
        <f-date
          ref="createModel.planStartDate"
          mask="YYYY-MM-DD"
          :options="startTimeOptions"
          v-model="$v.createModel.planStartDate.$model"
          :rules="[
            () => $v.createModel.planStartDate.required || '请选择计划启动日期'
          ]"
        />
      </f-formitem>
      <f-formitem label="计划完成日期" required>
        <f-date
          ref="createModel.planDoneDate"
          mask="YYYY-MM-DD"
          :options="endTimeOptions"
          v-model="$v.createModel.planDoneDate.$model"
          :rules="[
            () => $v.createModel.planDoneDate.required || '请选择计划完成日期'
          ]"
        />
      </f-formitem>
      <f-formitem label="项目/任务集" required>
        <fdev-select
          ref="createModel.prjNum"
          use-input
          :options="ipmpProjectOptios"
          option-label="project_name"
          @filter="filterProNo"
          v-model="$v.createModel.prjNum.$model"
          :rules="[() => $v.createModel.prjNum.required || '请选择项目/任务集']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label>
                  {{ scope.opt.project_no }}
                </fdev-item-label>
                <fdev-item-label caption>
                  {{ scope.opt.project_name }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template></fdev-select
        >
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog label="新增" @click="submitForm" />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { createOtherDmTaskModel, taskCsLists } from '@/modules/Rqr/model.js';
import { validate, formatOptionAdd } from '@/utils/utils';
import {
  addOtherDemandTask,
  queryIpmpLeadTeam
} from '@/modules/Rqr/services/methods';
export default {
  name: 'addOtherDmTaskDlg',
  data() {
    return {
      createModel: createOtherDmTaskModel(),
      taskCsOptions: taskCsLists,
      leadDeptOptions: [],
      allDeptObj: {},
      leadTeamOptions: [],
      ipmpProjectOptios: [],
      proLists: [],
      allUserOptions: [], //全部负责人
      userOptions: [], //负责人选项
      allUserFirmOptions: [], //全部厂商
      userFirmOptions: [], //厂商
      groupOptions: []
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
    demandDetail: {
      type: Object
    },
    value: {
      type: Boolean
    },
    confirm: {
      type: Boolean,
      default: true
    }
  },
  watch: {
    value(val) {
      if (val) {
        this.createModel = createOtherDmTaskModel();
        this.getUnitLists();
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('demandsForm', ['ipmpProject'])
  },
  methods: {
    ...mapActions('releaseForm', ['addReview']),
    ...mapActions('demandsForm', ['queryIpmpProject']),
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
    // 新增
    async saveOtherDmTask() {
      let params = {
        demandId: this.demandDetail.id, //需求编号
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
      }
      await addOtherDemandTask(params);
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
      await this.queryIpmpProject();
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
        for (let i = 0; i < res.length; i++) {
          let item = res[i];
          if (item.dept_id === item.dept_name) {
            continue;
          }
          let key = item.dept_id;
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
