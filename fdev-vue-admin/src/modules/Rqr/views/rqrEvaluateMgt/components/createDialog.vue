<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    dense
    title="新增需求评估表"
  >
    <div>
      <f-formitem
        label="需求名称"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-input
          v-model="$v.createModel.oa_contact_name.$model"
          ref="createModel.oa_contact_name"
          maxlength
          :rules="[
            () => $v.createModel.oa_contact_name.required || '请输入需求名称'
          ]"
        />
      </f-formitem>
      <f-formitem
        label="需求编号"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-input
          v-model="$v.createModel.oa_contact_no.$model"
          ref="createModel.oa_contact_no"
          maxlength
          :rules="[
            () => $v.createModel.oa_contact_no.required || '请输入需求编号'
          ]"
        />
      </f-formitem>
      <f-formitem
        label="牵头小组"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-select
          ref="createModel.demand_leader_group"
          use-input
          :options="groupOptions"
          @filter="groupInputFilter"
          option-label="name"
          v-model="$v.createModel.demand_leader_group.$model"
          :rules="[
            () =>
              $v.createModel.demand_leader_group.required || '请选择牵头小组'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name">{{
                  scope.opt.name
                }}</fdev-item-label>
                <fdev-item-label caption>
                  <span class="te-desc ellipsis" :title="scope.opt.fullName">
                    {{ scope.opt.fullName }}
                  </span>
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem
        label="牵头人员"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-select
          multiple
          use-input
          ref="createModel.demand_leader"
          v-model="$v.createModel.demand_leader.$model"
          :options="userOptions"
          @filter="userFilter"
          :rules="[
            () => $v.createModel.demand_leader.required || '请选择牵头人员'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.user_name_cn">
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
                <fdev-item-label
                  :title="`${scope.opt.user_name_en}--${scope.opt.groupName}`"
                  caption
                >
                  {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem label="优先级" bottom-page required label-style="width:119px">
        <fdev-select
          ref="createModel.priority"
          :options="priorityOptions"
          v-model="$v.createModel.priority.$model"
          :rules="[() => $v.createModel.priority.required || '请选择优先级']"
        />
      </f-formitem>
      <f-formitem
        label="起始评估日期"
        bottom-page
        required
        label-style="width:119px"
      >
        <f-date
          ref="createModel.start_assess_date"
          mask="YYYY-MM-DD"
          v-model="$v.createModel.start_assess_date.$model"
          :rules="[
            () =>
              $v.createModel.start_assess_date.required || '请选择起始评估日期'
          ]"
        />
      </f-formitem>
      <f-formitem label="评估现状" bottom-page label-style="width:119px">
        <fdev-input
          type="textarea"
          ref="createModel.assess_present"
          v-model="createModel.assess_present"
          maxlength
          :rules="[
            () => $v.createModel.assess_present.maxText || '输入不能超过120'
          ]"
        />
      </f-formitem>
    </div>

    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog label="确定" @click="submitForm" />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import {
  deepClone,
  getGroupFullName,
  formatOption,
  validate,
  successNotify,
  errorNotify
} from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  createEvaMgtModel,
  priorityOptions,
  queryUserOptionsParams
} from '@/modules/Rqr/model.js';
import { saveEvaMgt } from '@/modules/Rqr/services/methods';
import moment from 'moment';
export default {
  name: 'saveEvaMgtDlg',
  data() {
    return {
      createModel: createEvaMgtModel(),
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      groups: [], //牵头小组,
      groupOptions: [], //牵头小组下拉选项
      priorityOptions: priorityOptions
    };
  },
  validations: {
    createModel: {
      oa_contact_name: {
        required
      },
      oa_contact_no: {
        required
      },
      demand_leader_group: {
        required
      },
      demand_leader: {
        required
      },
      priority: {
        required
      },
      start_assess_date: {
        required
      },
      assess_present: {
        maxText(val) {
          if (val) {
            return val.length <= 120;
          } else return true;
        }
      }
    }
  },
  props: {
    value: {
      type: Boolean
    }
  },
  watch: {
    value(val) {
      if (val) {
        this.createModel = createEvaMgtModel();
        this.createModel.start_assess_date = moment(new Date()).format(
          'YYYY-MM-DD'
        );
        this.getUsersAndGroups();
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('dashboard', ['userList'])
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryUserCoreData']),
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
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.groups.filter(
          tag => tag.label.indexOf(val) > -1
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
      this.saveEvaMgt();
    },
    // 新增
    async saveEvaMgt() {
      let params = {
        oa_contact_no: this.createModel.oa_contact_no, //需求编号
        oa_contact_name: this.createModel.oa_contact_name, //任务名称
        demand_leader_group: this.createModel.demand_leader_group.id, //牵头小组
        priority: this.createModel.priority.value, //优先级
        start_assess_date: this.createModel.start_assess_date, //计划启动日期
        assess_present: this.createModel.assess_present //评估现状
      };
      //牵头人员
      if (this.createModel.demand_leader.length > 0) {
        params.demand_leader = [];
        for (let i = 0; i < this.createModel.demand_leader.length; i++) {
          params.demand_leader.push(this.createModel.demand_leader[i].value);
        }
      }
      const res = await saveEvaMgt(params);
      if (res && res.code && res.code != 'AAAAAAA') {
        // 失败
        errorNotify(res.msg);
      } else {
        // 成功
        successNotify('新增成功!');
        this.$emit('close', true);
      }
    },
    confirmToClose() {
      this.$emit('close', false);
    },
    async getUsersAndGroups() {
      let params = {
        company_id: queryUserOptionsParams.company_id,
        status: queryUserOptionsParams.status
      };
      Promise.all([
        //获取小组
        this.fetchGroup(),
        //获取用户
        this.queryUserCoreData(params)
      ]).then(() => {
        // 小组数据处理
        const temGroups = formatOption(this.groupsData);
        this.groups = deepClone(temGroups);
        this.groups.map(item => {
          let groupFullName = getGroupFullName(temGroups, item.id);
          item.label = groupFullName;
        });
        //人员数据处理
        this.users = this.userList.map(user =>
          formatOption(user, 'user_name_cn')
        );
      });
    }
  },
  created() {
    this.createModel.start_assess_date = moment(new Date()).format(
      'YYYY-MM-DD'
    );
  },
  mounted() {}
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
