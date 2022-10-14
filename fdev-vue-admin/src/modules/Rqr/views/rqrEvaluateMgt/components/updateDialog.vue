<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    dense
    title="编辑需求评估表"
  >
    <div>
      <f-formitem
        label="需求名称"
        required
        bottom-page
        label-style="width:119px"
      >
        <fdev-input
          disable
          v-model="$v.createModel.oa_contact_name.$model"
          ref="createModel.oa_contact_name"
          maxlength
          :rules="[() => true]"
        />
      </f-formitem>
      <f-formitem
        label="需求编号"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-input
          disable
          v-model="$v.createModel.oa_contact_no.$model"
          ref="createModel.oa_contact_no"
          maxlength
          :rules="[() => true]"
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
          option-value="id"
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
        label="超期分类"
        :required="
          this.detailObj.assess_days && this.detailObj.assess_days > 14
        "
        bottom-page
        label-style="width:119px"
      >
        <fdev-select
          ref="createModel.overdue_type"
          :options="overdueCalOptions"
          option-label="value"
          option-value="code"
          v-model="$v.createModel.overdue_type.$model"
          :rules="[
            () =>
              $v.createModel.overdue_type.overTime ||
              '您的需求已评估超期，请选择超期分类原因'
          ]"
        />
      </f-formitem>
      <f-formitem label="评估现状" bottom-page label-style="width:119px">
        <fdev-input
          ref="createModel.assess_present"
          type="textarea"
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
  successNotify,
  errorNotify,
  deepClone,
  getGroupFullName,
  formatOption,
  validate
} from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  createEvaMgtModel,
  priorityOptions,
  queryUserOptionsParams
} from '@/modules/Rqr/model.js';
import { updateEvaMgt, queryById } from '@/modules/Rqr/services/methods';
export default {
  name: 'updateEvaMgtDlg',
  data() {
    return {
      createModel: createEvaMgtModel(),
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      groups: [], //牵头小组,
      groupOptions: [], //牵头小组下拉选项
      priorityOptions: priorityOptions,
      detailObj: {}
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
      overdue_type: {
        overTime(val) {
          if (this.detailObj.assess_days && this.detailObj.assess_days > 14) {
            if (!val) {
              return false;
            } else {
              return true;
            }
          }
          return true;
        }
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
    },
    id: {
      type: String
    },
    overdueCalOptions: {
      type: Array
    }
  },
  watch: {
    value(val) {
      if (val) {
        this.createModel = createEvaMgtModel();
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
            v.user_name_en.includes(val) ||
            v.user_name_cn.toLowerCase().includes(needle)
        );
      });
    },
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.groups.filter(tag => tag.label.includes(val));
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
    // 编辑
    async saveEvaMgt() {
      let params = {
        id: this.id,
        oa_contact_no: this.createModel.oa_contact_no, //需求编号
        oa_contact_name: this.createModel.oa_contact_name, //任务名称
        demand_leader_group: this.createModel.demand_leader_group.id, //牵头小组
        priority: this.createModel.priority.value, //优先级
        assess_present: this.createModel.assess_present //评估现状
      };
      //超期分类
      if (this.createModel.overdue_type) {
        params.overdue_type = this.createModel.overdue_type.code;
      }
      //牵头人员
      if (this.createModel.demand_leader.length > 0) {
        params.demand_leader = [];
        for (let i = 0; i < this.createModel.demand_leader.length; i++) {
          params.demand_leader.push(this.createModel.demand_leader[i].value);
        }
      }
      const res = await updateEvaMgt(params);
      if (res && res.code && res.code != 'AAAAAAA') {
        // 失败
        errorNotify(res.msg);
      } else {
        // 成功
        successNotify('修改成功!');
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
      // 先获取小组和人员
      Promise.all([
        // 小组
        this.fetchGroup(),
        // 人员
        this.queryUserCoreData(params)
      ]).then(() => {
        // 处理小组数据
        const temGroups = formatOption(this.groupsData);
        this.groups = deepClone(temGroups);
        this.groups.map(item => {
          let groupFullName = getGroupFullName(temGroups, item.id);
          item.label = groupFullName;
        });
        this.groupOptions = this.groups;
        // 处理人员数据
        this.users = this.userList.map(user =>
          formatOption(user, 'user_name_cn')
        );
        this.userOptions = this.users;
        // 最后发送查询详情的接口
        this.getDetail();
      });
    },
    getItemById(id, lists) {
      for (let i = 0; i < lists.length; i++) {
        if (id === lists[i].id) {
          return lists[i];
        }
      }
      return null;
    },
    getPriorityItem(val) {
      for (let i = 0; i < this.priorityOptions.length; i++) {
        if (val == this.priorityOptions[i].value) {
          return this.priorityOptions[i];
        }
      }
      return null;
    },
    getOverduetypeItem(val) {
      for (let i = 0; i < this.overdueCalOptions.length; i++) {
        if (val == this.overdueCalOptions[i].code) {
          return this.overdueCalOptions[i];
        }
      }
      return null;
    },
    async getDetail() {
      let params = { id: this.id };
      const res = await queryById(params);
      this.detailObj = res;
      this.createModel.oa_contact_name = res.oa_contact_name; //需求名称;
      this.createModel.oa_contact_no = res.oa_contact_no; //需求编号;
      // let gitem = this.getItemById(res.demand_leader_group, this.groupOptions);
      // if (gitem) {
      //   this.createModel.demand_leader_group = gitem;
      // }
      //牵头小组
      this.createModel.demand_leader_group = {
        id: res.demand_leader_group,
        name: res.demand_leader_group_cn
      };
      //牵头人员
      for (let i = 0; i < res.demand_leader_info.length; i++) {
        res.demand_leader_info[i].value = res.demand_leader_info[i].id;
        res.demand_leader_info[i].label =
          res.demand_leader_info[i].user_name_cn;
      }
      this.createModel.demand_leader = res.demand_leader_info;
      // this.createModel.demand_leader = [];
      // for (let i = 0; i < res.demand_leader.length; i++) {
      //   let item = this.getItemById(res.demand_leader[i], this.userOptions);
      //   if (item) {
      //     this.createModel.demand_leader.push(item);
      //   }
      // }
      this.createModel.priority = this.getPriorityItem(res.priority); //优先级
      this.createModel.overdue_type = this.getOverduetypeItem(res.overdue_type); //超期分类;
      this.createModel.assess_present = res.assess_present; //评估现状
    }
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
