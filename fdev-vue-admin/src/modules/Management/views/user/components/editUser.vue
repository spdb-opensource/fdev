<template>
  <f-dialog
    :value="isOpen"
    title="用户信息修改"
    @input="closeDialog"
    @before-show="beforeShow"
    right
  >
    <fdev-form
      @submit.prevent="edit"
      ref="userInfo"
      :greedy="true"
      :model="userInfo"
    >
      <div class=" rdia-dc-w row justify-between">
        <div class="row boxStyle full-width">
          <div class="row items-center">
            <f-icon
              name="basic_msg_s_f"
              class="q-mr-sm q-ml-md text-primary"
              :width="16"
            ></f-icon>
            <span class="titleStyle">个人信息</span>
          </div>
        </div>
        <f-formitem label="姓名" required>
          <fdev-input v-model="userInfo.name" disable hint="" />
        </f-formitem>

        <f-formitem label="邮箱" required>
          <fdev-input
            ref="userInfo.email"
            v-model="userInfo.email_pre"
            :suffix="email_append"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="手机号" required>
          <fdev-input
            ref="userInfo.telephone"
            v-model="userInfo.telephone"
            :rules="[
              val => (val && val.length === 11) || '请输入11位数字手机号'
            ]"
          />
        </f-formitem>
        <f-formitem label="地域" required>
          <fdev-select
            ref="userInfo.area_id"
            v-model="userInfo.area_id"
            :options="areaList"
            option-label="name"
            option-value="id"
            map-options
            emit-value
            :rules="[val => !!val || '请选择地域']"
          />
        </f-formitem>
        <f-formitem label="人员职能" required
          ><fdev-select
            ref="userInfo.function_id"
            v-model="userInfo.function_id"
            :options="functionList"
            option-label="name"
            option-value="id"
            map-options
            emit-value
            :rules="[val => !!val || '请选择人员职能']"
        /></f-formitem>
        <f-formitem label="入职时间" required>
          <f-date
            v-model="userInfo.create_date"
            :options="timeOptions"
            :rules="[val => !!val || '请选择入职时间']"
          />
        </f-formitem>
        <f-formitem label="工作开始时间" required>
          <f-date
            v-model="userInfo.start_time"
            :options="timeOptions"
            :rules="[val => !!val || '请选择工作开始时间']"
          />
        </f-formitem>
        <f-formitem label="是否在职" required>
          <fdev-select
            ref="userInfo.isLeave"
            :display-value="formatSelectDisplay(isLeaves, userInfo.isLeave)"
            v-model="userInfo.isLeave"
            :options="isLeaves"
            :rules="[val => !!val || '请选择是否在职']"
        /></f-formitem>
        <f-formitem label="离职/换岗时间">
          <f-date
            v-model="userInfo.leave_date"
            :options="timeOptions"
            hint=""
          />
        </f-formitem>
        <f-formitem label="备注" full-width
          ><fdev-input
            type="textarea"
            rows="2"
            v-model="userInfo.remark"
            hint=""
        /></f-formitem>
        <div class="row boxStyle full-width">
          <div class="row items-center">
            <f-icon
              name="basic_msg_s_f"
              class="q-mr-sm q-ml-md text-primary"
              :width="16"
            ></f-icon>
            <span class="titleStyle">公司信息</span>
          </div>
        </div>
        <f-formitem label="公司" required>
          <fdev-select
            ref="userInfo.company"
            v-model="userInfo.company"
            option-label="name"
            option-value="id"
            :options="companies"
            :rules="[val => !!val || '请选择公司']"
            disable
        /></f-formitem>
        <f-formitem label="工号" required v-if="isSpdbUser(userInfo)">
          <fdev-input
            ref="userInfo.work_num"
            v-model="userInfo.work_num"
            :rules="[val => !!val || '请输入工号']"
          />
        </f-formitem>
        <f-formitem label="工号" v-if="!isSpdbUser(userInfo)">
          <fdev-input v-model="userInfo.work_num" hint="" />
        </f-formitem>
        <!-- 所属条线 -->
        <f-formitem label="所属条线" required>
          <fdev-select
            ref="userInfo.section"
            v-model="userInfo.section"
            :options="sectionAll"
            option-value="id"
            option-label="sectionNameCn"
            map-options
            emit-value
            :rules="[val => !!val || '请选择所属条线']"
          />
        </f-formitem>
        <!-- 所属小组 -->
        <f-formitem label="小组" required>
          <fdev-select
            ref="userInfo.group"
            use-input
            @filter="groupInputFilter"
            v-model="userInfo.group"
            option-label="name"
            :options="filterGroup"
            :rules="[val => !!val || '请选择小组']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select></f-formitem
        >
        <f-formitem label="是否为小组负责人" required>
          <fdev-select
            ref="userInfo.is_manage_group"
            v-model="userInfo.is_manage_group"
            :options="isManageOptions"
            option-value="value"
            option-label="label"
            map-options
            emit-value
            :display-value="
              formatSelectDisplay(isManageOptions, userInfo.is_manage_group)
            "
            :rules="[val => !!val || '请选择是否为负责人']"
        /></f-formitem>
        <div class="row boxStyle full-width">
          <div class="row items-center">
            <f-icon
              name="basic_msg_s_f"
              class="q-mr-sm q-ml-md text-primary"
              :width="16"
            ></f-icon>
            <span class="titleStyle">角色信息</span>
          </div>
        </div>
        <f-formitem label="角色" required full-width
          ><fdev-select
            use-chips
            use-input
            multiple
            input-debounce="0"
            :disable="isAdmin"
            @filter="roleInputFilter"
            ref="userInfo.role"
            :display-value="formatSelectDisplay(roles, userInfo.role.id)"
            v-model="userInfo.role"
            :options="newRoles"
            :rules="[
              () => $v.userInfo.role.required || '请选择角色',
              () =>
                $v.userInfo.role.cannotModifyEnvRole ||
                '没有权限更改“环境配置管理”角色',
              () =>
                $v.userInfo.role.cannotModifyBaseRole ||
                '没有权限更改“基础架构管理员”角色'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.label }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select></f-formitem
        >
        <div class="row boxStyle full-width">
          <div class="row items-center">
            <f-icon
              name="basic_msg_s_f"
              class="q-mr-sm q-ml-md text-primary"
              :width="16"
            ></f-icon>
            <span class="titleStyle">网络审核信息</span>
          </div>
        </div>
        <f-formitem label="KF白名单开通" required>
          <fdev-tooltip>
            <span>此网络权限只针对上海地区的SPDB-KF网络开通</span>
          </fdev-tooltip>
          <fdev-radio :val="true" v-model="userInfo.is_kfApproval" label="是"/>
          <fdev-radio
            :val="false"
            v-model="userInfo.is_kfApproval"
            label="否"
            class="q-ml-md"
        /></f-formitem>
        <f-formitem label="手机型号" required v-if="userInfo.is_kfApproval">
          <fdev-input
            ref="userInfo.phone_type"
            v-model="userInfo.phone_type"
            :rules="[val => !!val || '请输入手机型号']"
          />
        </f-formitem>
        <f-formitem label="手机mac地址" required v-if="userInfo.is_kfApproval">
          <fdev-input
            ref="userInfo.phone_mac"
            v-model="userInfo.phone_mac"
            :rules="[val => !!val || '请输入手机mac地址']"
          />
        </f-formitem>
        <f-formitem
          label="是否为行内测试设备"
          required
          v-if="userInfo.is_kfApproval"
        >
          <fdev-select
            ref="userInfo.is_spdb_mac"
            v-model="userInfo.is_spdb_mac"
            :options="isSpdbMacOptions"
            option-label="label"
            option-value="value"
            map-options
            emit-value
            :rules="[val => !!val || '请选择是否为行内测试设备']"
        /></f-formitem>
        <f-formitem label="虚机网段迁移" required>
          <fdev-radio :val="true" v-model="userInfo.is_vmApproval" label="是"/>
          <fdev-radio
            :val="false"
            v-model="userInfo.is_vmApproval"
            label="否"
            class="q-ml-md"
        /></f-formitem>
        <f-formitem
          :label="isSpdbUser(userInfo) ? 'VDI虚机ip地址:' : '虚机ip地址:'"
          required
          v-if="userInfo.is_vmApproval"
        >
          <fdev-input
            ref="userInfo.vm_ip"
            v-model="userInfo.vm_ip"
            :rules="[
              val =>
                !!val ||
                (isSpdbUser(userInfo)
                  ? '请输入VDI虚机ip地址'
                  : '请输入虚机ip地址')
            ]"
          />
        </f-formitem>
        <f-formitem
          label="虚机名"
          required
          v-if="userInfo.is_vmApproval && !isSpdbUser(userInfo)"
        >
          <fdev-input
            ref="userInfo.vm_name"
            v-model="userInfo.vm_name"
            :rules="[val => !!val || '请输入虚机名']"
          />
        </f-formitem>
        <f-formitem
          :label="isSpdbUser(userInfo) ? '域用户:' : '虚机用户名:'"
          required
          v-if="userInfo.is_vmApproval"
        >
          <fdev-input
            ref="userInfo.vm_user_name"
            v-model="userInfo.vm_user_name"
            :rules="[
              val =>
                !!val ||
                (isSpdbUser(userInfo) ? '请输入域用户' : '请输入虚机用户名')
            ]"
            hint="只需输入用户名"
          />
        </f-formitem>
      </div>
    </fdev-form>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog"/>
      <fdev-btn
        label="确定"
        v-forbidMultipleClick
        dialog
        @click="edit(userInfo)"
    /></template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { formatSelectDisplay, deepClone } from '@/utils/utils';
import {
  educationOptions,
  // isLeaveOptions,
  isSpdbMacOptions,
  findAuthority
  //is_party_memberMap,
} from '@/modules/User/utils/model';

export default {
  name: 'editUser',
  props: {
    isOpen: {
      default: false,
      type: Boolean
    },
    dataSource: {
      type: Object
    }
  },
  data() {
    return {
      userInfo: {},
      educationOptions: educationOptions,
      is_party_memberOptions: [
        {
          label: '中共党员',
          value: '0'
        },
        {
          label: '共青团员',
          value: '1'
        },
        {
          label: '群众',
          value: '2'
        }
      ],
      isManageOptions: [
        {
          label: '是',
          value: '0'
        },
        {
          label: '否',
          value: '1'
        }
      ],
      filterGroup: [],
      isSpdbMacOptions: isSpdbMacOptions,
      deepCloneGroups: [],
      newRoles: [],
      envManagerRole: {},
      baseArchitectureRole: {},
      manageGroups: [], //负责小组
      deepCloneManageGroups: [],
      email_append: ''
    };
  },
  validations: {
    userInfo: {
      role: {
        required,
        cannotModifyEnvRole(roles) {
          return this.roleRules(roles, '环境配置管理员', this.envManagerRole);
        },
        cannotModifyBaseRole(roles) {
          return this.roleRules(
            roles,
            '基础架构管理员',
            this.baseArchitectureRole
          );
        }
      }
    }
  },

  watch: {
    isOpen(val) {
      if (val === true) {
        setTimeout(() => {
          this.userInfo = { ...this.dataSource };
          this.email_append = this.userInfo.email.split('@')[1];
          this.userInfo.email_pre = this.userInfo.email.split('@')[0];
          this.envManagerRole = this.userInfo.role.find(role => {
            return role.label === '环境配置管理员';
          });
          this.baseArchitectureRole = this.userInfo.role.find(role => {
            return role.label === '基础架构管理员';
          });
          this.deepCloneGroups = deepClone(this.groups);
          this.filterGroup = this.deepCloneGroups;
          this.fetchRole();
          if (this.currentUser.user_name_en !== 'admin') {
            this.newRoles = this.roles.filter(role => {
              return role.label !== '卡点管理员';
            });
          } else {
            this.newRoles = this.roles;
          }
        }, 50);
      }
    }
  },
  computed: {
    ...mapState('userForm', [
      'groups',
      'companies',
      'roles',
      'isLeaves',
      'tags',
      'childGroup',
      'areaList',
      'functionList',
      'rankList',
      'userInPage',
      'reBuildGroupName'
    ]),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('managementForm', ['sectionAll']),
    isAdmin() {
      return (
        this.currentUser.user_name_en !== 'admin' &&
        findAuthority(this.currentUser) !== 'admin'
      );
    }
  },

  methods: {
    ...mapActions('userForm', [
      'fetchGroup',
      'fetchCompany',
      'fetchRole',
      'fetchAuth',
      'fetchTag',
      'addTag',
      'removeTag',
      'queryChildGroupById',
      'queryArea',
      'queryFunction',
      'queryRank',
      'queryUserPagination'
    ]),
    closeDialog() {
      this.$emit('close');
    },
    timeOptions(date) {
      const now = moment(new Date()).format('YYYY/MM/DD');
      return date < now;
    },
    formatSelectDisplay,
    isSpdbUser({ company }) {
      return company && company.name === '浦发';
    },
    groupInputFilter(val, update) {
      update(() => {
        // 小组管理员新增只能新增自己小组人员
        this.filterGroup = this.deepCloneGroups.filter(
          tag => tag.label.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    manageGroupsFilter(val, update) {
      update(() => {
        // 小组管理员新增只能新增自己小组人员

        this.manageGroups = this.deepCloneManageGroups.filter(
          tag => tag.label.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    roleInputFilter(val, update) {
      update(() => {
        if (this.currentUser.user_name_en === 'admin') {
          this.newRoles = this.roles;
        } else {
          this.newRoles = this.roles.filter(role => {
            return role.label !== '卡点管理员';
          });
        }
        this.newRoles = this.newRoles.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    roleRules(roles, label, oldRole) {
      if (
        findAuthority(this.currentUser) !== 'admin' &&
        this.currentUser.user_name_en !== 'admin'
      ) {
        let role = roles.find(role => {
          return role.label === label;
        });
        if ((oldRole && !role) || (!oldRole && role)) {
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    },
    edit(val) {
      this.$refs.userInfo.validate().then(res => {
        if (!res) return;
        this.$emit('edit', val);
      });
    },
    async beforeShow() {}
  },
  async created() {
    this.userInfo = { ...this.dataSource };
    this.userInfo.email_pre = this.userInfo.email.split('@')[0];
    this.email_append = this.userInfo.email.split('@')[1];
    this.envManagerRole = this.userInfo.role.find(role => {
      return role.label === '环境配置管理员';
    });
    this.baseArchitectureRole = this.userInfo.role.find(role => {
      return role.label === '基础架构管理员';
    });
    this.deepCloneGroups = deepClone(this.groups);
    this.filterGroup = this.deepCloneGroups;
    this.fetchRole();
    if (this.currentUser.user_name_en !== 'admin') {
      this.newRoles = this.roles.filter(role => {
        return role.label !== '卡点管理员';
      });
    } else {
      this.newRoles = this.roles;
    }
  }
};
</script>

<style lang="stylus" scoped>

.boxStyle
  height: 54px;
  background: rgba(30, 84, 213, 0.05);
  border-radius: 2px;
  border-radius: 2px;
  margin-bottom: 20px;
.titleStyle
  font-weight: 600;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
</style>
