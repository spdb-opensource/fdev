<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    dense
    title="编辑会议记录"
  >
    <div>
      <f-formitem
        required
        label="会议时间"
        label-style="width:119px"
        bottom-page
      >
        <fdev-select
          ref="createModel.meetingTime"
          hide-dropdown-icon
          v-model="$v.createModel.meetingTime.$model"
          :rules="[
            () => $v.createModel.meetingTime.required || '请选择会议时间'
          ]"
          @customer-click="showDateAndTime"
        >
          <template v-slot:append>
            <fdev-icon
              name="access_time"
              size="xs"
              class="cursor-pointer"
              style="width:16px;height:16px"
              @click="showTimePicker"
            >
              <el-time-picker
                v-model="time"
                format="HH:mm"
                value-format="HH:mm"
                :clearable="false"
                style="visibility:hidden;width:0;margin-left:0"
                ref="meeting-timepicker"
              >
              </el-time-picker>
            </fdev-icon>
          </template>
          <template v-slot:prepend>
            <f-icon
              name="calendar"
              class="cursor-pointer fdevdate"
              :width="16"
              :height="16"
            />
          </template>
          <fdev-popup-proxy
            ref="qDate"
            target=".fdevdate"
            transition-show="scale"
            transition-hide="scale"
            @before-show="bfshow"
          >
            <fdev-date
              class="fdate"
              @input="() => $refs.qDate.hide()"
              v-model="$v.createModel.meetingTime.$model"
              mask="YYYY-MM-DD HH:mm"
            />
          </fdev-popup-proxy>
        </fdev-select>
      </f-formitem>
      <f-formitem
        label="会议地点"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-input
          v-model="$v.createModel.address.$model"
          ref="createModel.address"
          maxlength
          :rules="[() => $v.createModel.address.required || '请输入会议地点']"
        />
      </f-formitem>
      <f-formitem
        label="会议类型"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-select
          ref="createModel.meetingType"
          :options="meetingTypeOptions"
          v-model="createModel.meetingType"
          :rules="[() => createModel.meetingType || '请选择会议类型']"
        />
      </f-formitem>

      <f-formitem label="审核人" bottom-page required label-style="width:119px">
        <fdev-select
          multiple
          use-input
          ref="createModel.auditeUsers"
          v-model="$v.createModel.auditeUsers.$model"
          :options="userOptions"
          @filter="userFilter"
          :rules="[() => $v.createModel.auditeUsers.required || '请选择审核人']"
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
    </div>

    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog :loading="loading" label="确定" @click="submitForm" />
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
  successNotify
  //errorNotify
} from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  createCodeToolListModel,
  meetingTypeOptions
  //queryUserOptionsParams
} from '@/modules/Network/utils/constants';
import { queryUserCoreData } from '@/modules/Rqr/services/methods';
import moment from 'moment';
export default {
  name: 'saveEvaMgtDlg',
  data() {
    return {
      createModel: createCodeToolListModel(),
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      groups: [], //牵头小组,
      groupOptions: [], //牵头小组下拉选项
      meetingTypeOptions: meetingTypeOptions,
      loading: false,
      time: ''
    };
  },
  validations: {
    createModel: {
      meetingTime: {
        required
      },
      address: {
        required
      },
      meetingType: {
        required
      },
      auditeUsers: {
        required
      }
    }
  },
  props: {
    value: {
      type: Boolean
    },
    editData: {
      type: Object
    }
  },
  watch: {
    value(val) {
      if (val) {
        this.createModel = createCodeToolListModel();
        this.createModel.meetingTime = moment(new Date()).format('YYYY-MM-DD');
        this.getUsersAndGroups();
        this.getMeetingDatas();
      }
    },
    time(val) {
      let currentTime = this.createModel.meetingTime.split(' ')[0];
      this.createModel.meetingTime = currentTime + ' ' + val;
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupsData: 'groups'
    })
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('networkForm', ['updateMeeting']),
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
    bfshow(e) {
      this.$nextTick(() => {
        this.$refs['qDate'].$children[0].$children[0].$el.style.padding = 0;
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
      this.saveMeeting();
    },
    // 新增
    async saveMeeting() {
      let params = {
        meetingTime: this.createModel.meetingTime, //会议时间
        address: this.createModel.address, //会议地点
        meetingType: this.createModel.meetingType.value, //会议类型
        orderId: this.$route.params.id, //工单id
        id: this.editData.id //会议id
      };
      //审核人
      if (this.createModel.auditeUsers.length > 0) {
        params.auditeUsers = [];
        for (let i = 0; i < this.createModel.auditeUsers.length; i++) {
          params.auditeUsers.push(this.createModel.auditeUsers[i].value);
        }
      }
      this.loading = true;
      await this.updateMeeting(params);
      successNotify('修改会议成功!');
      this.loading = false;
      this.$emit('close', true);
    },
    confirmToClose() {
      this.$emit('close', false);
    },
    getMeetingDatas() {
      this.createModel.meetingTime = this.editData.meetingTime;
      this.createModel.address = this.editData.address;
      this.createModel.meetingType = this.getMeetingTypeItem(
        this.editData.meetingType
      );
      // 审核人赋值
      let userList = [];
      this.users.forEach(val => {
        this.editData.auditeUsers.filter(item => {
          if (val.id === item) {
            userList.push(val);
          }
        });
      });
      this.createModel.auditeUsers = userList;
    },
    getMeetingTypeItem(val) {
      for (let i = 0; i < this.meetingTypeOptions.length; i++) {
        if (val == this.meetingTypeOptions[i].value) {
          return this.meetingTypeOptions[i];
        }
      }
      return null;
    },
    async getUsersAndGroups() {
      //分组获取
      await this.fetchGroup();
      const temGroups = formatOption(this.groupsData);
      this.groups = deepClone(temGroups);
      this.groups.map(item => {
        let groupFullName = getGroupFullName(temGroups, item.id);
        item.label = groupFullName;
      });
      // let params = {
      //   company_id: queryUserOptionsParams.company_id,
      //   status: queryUserOptionsParams.status
      // };
      const res = await queryUserCoreData();
      this.users = res.map(user => formatOption(user, 'user_name_cn'));
    },
    showTimePicker() {
      this.$refs['meeting-timepicker'].$vnode.componentInstance.handleFocus();
    },
    showDateAndTime() {
      this.$refs['qDate'].show();
      this.showTimePicker();
    }
  },
  created() {
    this.getUsersAndGroups();
    this.getMeetingDatas();
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.fdate >>> .q-date__header
  display none
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
