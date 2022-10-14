<template>
  <f-block>
    <Loading :visible="loading">
      <div class="row q-mt-xs justify-between ">
        <div>
          <div class="row items-center">
            <span class="mr12 titleStyle"> {{ user.name }}</span>
          </div>
          <div class="subtitleStyle row items-center">
            <span>{{ user.email }}</span>
          </div>
        </div>
      </div>
      <div class="row border-bottom full-width icontitle">
        <div class="row full-width border-top">
          <!-- 手机号 -->
          <f-formitem
            class="col-4"
            label="手机号"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.telephone">{{ user.telephone }}</span>
          </f-formitem>
          <!-- 职能 -->
          <f-formitem
            class="col-4"
            label="职能"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.function ? user.function.name : ''">
              {{ user.function ? user.function.name : '' }}</span
            >
          </f-formitem>
          <!-- 所属条线 -->
          <f-formitem
            class="col-4"
            label="所属条线"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span
              :title="user.sectionInfo ? user.sectionInfo.sectionNameCn : '-'"
            >
              {{
                user.sectionInfo ? user.sectionInfo.sectionNameCn : '-'
              }}</span
            >
          </f-formitem>
          <!-- 职级 -->
          <f-formitem
            class="col-4"
            label="职级"
            profile
            bottom-page
            label-auto
            v-if="false"
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.rank ? user.rank.name : ''">
              {{ user.rank ? user.rank.name : '' }}</span
            >
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <!-- 是否在职 -->
          <f-formitem
            class="col-4"
            label="是否在职"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.status | isStatus">
              {{ user.status | isStatus }}</span
            >
          </f-formitem>
          <!-- 离职/换岗时间 -->
          <f-formitem
            class="col-4"
            label="离职/换岗时间"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.leave_date"> {{ user.leave_date }}</span>
          </f-formitem>
          <!-- 学历 -->
          <f-formitem
            class="col-4"
            label="学历"
            profile
            bottom-page
            v-if="false"
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.education"> {{ user.education }}</span>
          </f-formitem>
          <!-- 是否党员 -->
          <f-formitem
            class="col-4"
            label="政治面貌"
            profile
            bottom-page
            label-auto
            v-if="false"
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.is_party_member | filterIs_party_member">
              {{ user.is_party_member | filterIs_party_member }}</span
            >
          </f-formitem>
          <!-- 入职时间 -->
          <f-formitem
            class="col-4"
            label="入职时间"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.create_date"> {{ user.create_date }}</span>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <!-- 工作开始时间 -->
          <f-formitem
            class="col-4"
            label="工作开始时间"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.start_time"> {{ user.start_time }}</span>
          </f-formitem>
          <!-- 备注 -->
          <f-formitem
            class="col-4"
            label="备注"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.remark"> {{ user.remark }}</span>
          </f-formitem>
        </div>
      </div>
      <div class="icontitles">
        <f-icon
          name="bell_s_f"
          class="text-primary mr12 ficon"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">公司信息</span>
      </div>
      <div class="row border-bottom full-width">
        <div class="row full-width border-top">
          <!-- 公司 -->
          <f-formitem
            class="col-4"
            label="公司"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.company ? user.company.name : ''">
              {{ user.company ? user.company.name : '' }}</span
            >
          </f-formitem>
          <!-- 工号 -->
          <f-formitem
            class="col-4"
            label="工号"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.work_num"> {{ user.work_num }}</span>
          </f-formitem>
          <!-- 小组 -->
          <f-formitem
            class="col-4"
            label="小组"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.group ? user.group.fullName : ''">
              {{ user.group ? user.group.fullName : '' }}</span
            >
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <!-- 地域 -->
          <f-formitem
            class="col-4"
            label="地域"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.area ? user.area.name : '-'">
              {{ user.area ? user.area.name : '-' }}</span
            >
          </f-formitem>
          <!-- 是否为小组负责人 -->
          <f-formitem
            class="col-4"
            label="是否为小组负责人"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.manage_group | manage_groupFilter">
              {{ user.is_manage_group | manage_groupFilter }}</span
            >
          </f-formitem>
          <!-- 负责小组 -->
          <!-- <f-formitem
            class="col-4"
            label="负责小组"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.manage_group ? user.manage_group.name : '-'">
              {{ user.manage_group ? user.manage_group.name : '-' }}</span
            >
          </f-formitem> -->
        </div>
      </div>
      <div class="icontitles">
        <f-icon
          name="bell_s_f"
          class="text-primary mr12 ficon"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">角色信息</span>
      </div>
      <div class="row border-bottom full-width ">
        <div class="row full-width border-top">
          <!-- 角色 -->
          <f-formitem
            class="col-12"
            label="角色"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="width:160px; height:auto"
            value-class="q-px-lg"
            value-style="margin-top:5px; margin-bottom:5px"
          >
            <span :title="user.role.map(item => item.name).join(',')">
              <fdev-chip
                v-for="tag in user.role"
                :key="tag.id"
                color="primary"
                text-color="white"
                square
                class="q-mt-none"
                :title="user.role ? user.role.name : '-'"
              >
                {{ tag.name }}
              </fdev-chip>
            </span>
          </f-formitem>
        </div>
      </div>
      <div class="icontitles">
        <f-icon
          name="bell_s_f"
          class="text-primary mr12 ficon"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">Gitlab账号信息</span>
      </div>
      <div class="row border-bottom full-width ">
        <div class="row full-width border-top">
          <!-- Gitlab ID -->
          <f-formitem
            class="col-4"
            label="Gitlab ID"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.git_user_id"> {{ user.git_user_id }}</span>
          </f-formitem>
          <!-- Gitlab Username -->
          <f-formitem
            class="col-4"
            label="Gitlab Username"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.git_user"> {{ user.git_user }}</span>
          </f-formitem>
        </div>
      </div>
      <div class="icontitles">
        <f-icon
          name="bell_s_f"
          class="text-primary mr12 ficon"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">网络审核信息</span>
      </div>
      <div class="row border-bottom full-width ">
        <div class="row full-width border-top">
          <!-- 手机型号 -->
          <f-formitem
            class="col-4"
            label="手机型号"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.phone_type"> {{ user.phone_type }}</span>
          </f-formitem>
          <!-- 手机mac地址 -->
          <f-formitem
            class="col-4"
            label="手机mac地址"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.phone_mac"> {{ user.phone_mac }}</span>
          </f-formitem>
          <!-- 是否为行内测试设备 -->
          <f-formitem
            class="col-4"
            label="是否为行内测试设备"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.is_spdb_mac | macFilters">
              {{ user.is_spdb_mac | macFilters }}</span
            >
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <!-- 虚机ip地址 -->
          <f-formitem
            class="col-4"
            label="虚机ip地址"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.vm_ip"> {{ user.vm_ip }}</span>
          </f-formitem>
          <!-- 虚机名 -->
          <f-formitem
            class="col-4"
            label="虚机名"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
            v-if="isSpdbUser"
          >
            <span :title="user.vm_name"> {{ user.vm_name }}</span>
          </f-formitem>
          <!-- 域用户 -->
          <f-formitem
            class="col-4"
            :label="isSpdbUser ? '域用户' : '虚机用户名'"
            profile
            bottom-page
            label-auto
            label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
            label-style="height:43px;width:160px;"
            value-style="line-height:43px;"
            value-class="ellipsis q-px-lg"
          >
            <span :title="user.vm_user_name"> {{ user.vm_user_name }}</span>
          </f-formitem>
        </div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex';
import { required, integer, maxLength } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';

import {
  createUserModel,
  educationOptions,
  isSpdbMacOptions,
  isSpdbMac,
  findAuthority,
  is_party_memberMap
} from '@/modules/User/utils/model';

import { formatSelectDisplay } from '@/utils/utils';

export default {
  name: 'Profile',
  components: { Loading },
  data() {
    return {
      updateUserModalOpened: false,
      updateUserModel: createUserModel(),
      loading: false,
      filteredTags: [],
      permissions: [],
      deepCloneGroups: [],
      envManagerRole: {},
      baseArchitectureRole: {},
      filterRoles: [],
      filterGroups: [],
      educationOptions: educationOptions,
      isSpdbMacOptions
    };
  },
  validations: {
    updateUserModel: {
      name: {
        required
      },
      email: {
        required
      },
      git_user_id: {
        required(val) {
          const { name } = this.updateUserModel.group;
          if (
            name === '业务部门' ||
            name === '测试中心' ||
            name === '规划部门'
          ) {
            return true;
          }
          return !!val.trim();
        },
        integer
      },
      area_id: {
        required(val) {
          if (this.isSpdbUser) {
            return !!val;
          }
          return true;
        }
      },
      telephone: {
        required,
        integer,
        maxLength: maxLength(11)
      },
      phone_type: {
        required
      },
      phone_mac: {
        required
      },
      is_spdb_mac: {
        required
      },
      vm_ip: {
        required
      },
      vm_name: {
        required
      },
      vm_user_name: {
        required
      },
      group: {
        required
      },
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
      },

      company: {
        required
      },
      isLeave: {
        required
      },
      tagSelected: {},
      create_date: {
        required
      },
      leave_date: {},
      remark: {},
      start_time: {
        required
      },
      education: {},
      rank_id: {
        required(val) {
          if (!this.isSpdbUser) {
            return !!val;
          }
          return true;
        }
      },
      function_id: {
        required
      }
    }
  },

  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['list']),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userForm', [
      'groups',
      'companies',
      'roles',
      'isLeaves',
      'tags',
      'childGroup',
      'areaList',
      'functionList',
      'rankList'
    ]),
    ...mapState('authorized', {
      auths: 'list'
    }),
    ...mapGetters('userForm', ['wrapTotal']),
    ...mapGetters('authorized', ['checkPermissions', 'checkTransPermissions']),
    user() {
      return this.list[0] || createUserModel();
    },
    showArea() {
      return (
        this.user.area &&
        this.user.email &&
        this.user.email.endsWith('spdb.com.cn')
      );
    },

    isSpdbUser() {
      const { company } = this.user;
      return company && company.name === '浦发';
    }
  },
  filters: {
    macFilters(val) {
      return isSpdbMac[val];
    },
    filterIs_party_member(val) {
      return is_party_memberMap[val];
    },
    isStatus(val) {
      if (val == '0') {
        return '在职';
      } else {
        return '离职';
      }
    },
    manage_groupFilter(val) {
      if (val === '0') {
        return '是';
      } else if (val === '1') {
        return '否';
      } else {
        return '';
      }
    }
  },
  methods: {
    ...mapActions('user', {
      fetchUser: 'fetch',
      removeUser: 'remove',
      updateUser: 'update',
      fetchCurrent: 'fetchCurrent',
      simulateUser: 'simulateUser'
    }),
    ...mapActions('userForm', [
      'fetchGroup',
      'fetchCompany',
      'fetchRole',
      'fetchTag',
      'addTag',
      'removeTag',
      'queryChildGroupById',
      'queryArea',
      'queryFunction',
      'queryRank'
    ]),
    ...mapActions('authorized', {
      fetchAuth: 'fetch'
    }),
    roleRules(roles, label, oldRole) {
      if (findAuthority(this.currentUser) === 'admin') {
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

    formatSelectDisplay,

    async querySelectItem() {
      this.fetchCompany();
      this.queryArea();
      this.queryFunction();
      this.queryRank();
      //this.fetchAuth();
      this.fetchTag();
      await this.fetchRole();
      if (this.currentUser.name !== 'admin') {
        this.filterRoles = this.roles.filter(role => {
          return role.label !== '卡点管理员';
        });
      } else {
        this.filterRoles = this.roles;
      }
    },
    descFilter(input) {
      if (!input) {
        return input;
      }
      input = input.replace(/<[^>]+>/g, '');
      const reg = new RegExp(/\n/g);
      return input.replace(reg, '</br>');
    }
  },
  async created() {
    this.loading = true;
    const id = this.$route.params.id;
    await this.fetchUser({ id });
    await this.fetchGroup();
    //查询父子组，判断编辑按钮是否可用
    // this.queryChildGroupById({ id: this.currentUser.group.id });

    // this.deepCloneGroups = deepClone(this.groups);
    // this.filterGroups = this.deepCloneGroups;
    this.loading = false;
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>

.subtitleStyle
  margin-top: 8px;
  margin-right: 4px;
  font-size: 14px;
  color: #666666;
  letter-spacing: 0;
  line-height: 14px;
.ficon
  margin-bottom: -4px;
.mr12
  margin-right:12px;
.infoStyle
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight: 600;
.titleStyle
  font-family: PingFangSC-Medium;
  font-size: 22px;
  margin-right: 12px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;

border(align='all')
  border-top 1px solid #ddd if align == 'top' || align == 'all'
  border-bottom 1px solid #ddd if align == 'bottom' || align == 'all'
.border-top
  border('top')
.border-bottom
  border('bottom')
.border
  border()
.modal-layout {
  min-width: 40vw;
}
.q-chip
  height:24px
  margin-bottom:5px
.icontitle
  margin-top:32px;
.icontitles
  margin-top:32px;
  margin-bottom:20px;
.tag-remove {
  width: 2em;
  height: 2em;
}

.tag-remove >>> .q-btn__wrapper {
  min-width: 2em;
  min-height: 2em;
}

.p-b-20{
  padding-bottom: 20px;
}
.f-center{
  display: flex;
  justify-content: center;
  width: 100%;
}
</style>
