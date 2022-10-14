<template>
  <div class="q-px-lg bg-grey-4">
    <f-dialog
      v-if="showDialog"
      :value="value"
      @input="$emit('input', $event)"
      @shake="confirmToClose"
      title="用户信息完善"
      right
    >
      <div v-if="isHaveToken">
        <div class="text-subtitle3 q-pb-md">个人信息</div>
        <fdev-form ref="ldapInfo">
          <div class="q-gutter-y-diaLine rdia-dc-w row justify-between">
            <f-formitem label="fdev登录账号">
              <fdev-input v-model="fdevAccount" readonly type="text" />
            </f-formitem>

            <f-formitem label="邮箱">
              <fdev-input
                ref="userModel.email_pre"
                v-model="userModel.email_pre"
                type="text"
                :suffix="userModel.email_append"
                readonly
                :rules="[
                  () =>
                    $v.userModel.email_pre.required || '请输入正确的邮箱地址'
                ]"
              />
            </f-formitem>

            <f-formitem label="姓名" required>
              <fdev-input
                ref="userModel.name"
                v-model="$v.userModel.name.$model"
                type="text"
                :rules="[
                  () => $v.userModel.name.required || '请输入姓名',
                  () => $v.userModel.name.onlyWord || '只能输入中文'
                ]"
              />
            </f-formitem>

            <f-formitem label="地域" required>
              <fdev-select
                ref="userModel.area_id"
                v-model="$v.userModel.area_id.$model"
                :options="areaList"
                option-label="name"
                option-value="id"
                map-options
                emit-value
                :rules="[() => $v.userModel.area_id.required || '请选择地域']"
              />
            </f-formitem>
            <f-formitem label="政治面貌" required>
              <fdev-select
                ref="userModel.is_party_member"
                v-model="$v.userModel.is_party_member.$model"
                :options="isPartyMemberOptions"
                option-value="value"
                option-label="label"
                map-options
                emit-value
                :rules="[
                  () =>
                    $v.userModel.is_party_member.required || '请选择政治面貌'
                ]"
              />
            </f-formitem>
            <f-formitem label="所属条线" required>
              <fdev-select
                ref="userModel.section"
                v-model="$v.userModel.section.$model"
                :options="sectionAll"
                option-value="id"
                option-label="sectionNameCn"
                map-options
                emit-value
                :rules="[
                  () => $v.userModel.section.required || '请选择所属条线'
                ]"
              />
            </f-formitem>
            <f-formitem label="小组" required>
              <fdev-select
                use-input
                ref="userModel.group"
                v-model="$v.userModel.group.$model"
                :options="filterGroup"
                @filter="groupInputFilter"
                option-label="name"
                option-value="id"
                :rules="[() => $v.userModel.group.required || '请选择小组']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.name }}</fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
                <template v-slot:no-option>
                  <fdev-item>
                    <fdev-item-section>
                      当前机构无该小组
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>

            <f-formitem label="角色" required>
              <fdev-select
                use-chips
                use-input
                multiple
                input-debounce="0"
                ref="userModel.role"
                v-model="$v.userModel.role.$model"
                :options="newRoles"
                :rules="[() => $v.userModel.role.required || '请选择角色']"
              />
            </f-formitem>

            <f-formitem
              label="gitlab账号Id"
              required
              v-if="newUserIsNeedGitlabId"
            >
              <fdev-input
                ref="userModel.git_user_id"
                v-model="$v.userModel.git_user_id.$model"
                type="text"
                :rules="[
                  () =>
                    $v.userModel.git_user_id.required || '请输入gitlab账号Id',
                  () => $v.userModel.git_user_id.integer || '只能输入数字'
                ]"
              />
              <fdev-tooltip>
                <div>
                  获取方式：使用本人账号登录gitlab，点击页面右上角个人头像，
                </div>
                <div>
                  选择“用户资料”菜单项，复制个人信息用户ID至此即可
                </div>
              </fdev-tooltip>
            </f-formitem>

            <f-formitem
              label="gitlab token"
              required
              v-if="needGitlabToken && newUserIsNeedGitlabId"
            >
              <fdev-input
                ref="userModel.git_token"
                v-model="$v.userModel.git_token.$model"
                type="text"
                :rules="[
                  () => $v.userModel.git_token.required || '请输入gitlab token'
                ]"
              />
              <fdev-tooltip>
                <div class="tip">
                  <div>
                    获取方式：使用本人账号登录gitlab，点击页面右上角个人头像，选择
                  </div>
                  <div>
                    “setting”进入个人中心设置页面，点击页面左侧菜单栏“Access
                    Tokens”，
                  </div>
                  <div>
                    填写添加“Access
                    Tokens”的Name，过期时间可选填(建议不填)，Scopes
                  </div>
                  <div>
                    选择“api”点击“Create personal access
                    token”。页面上方会显示本次新
                  </div>
                  <div>增的Token，复制至此即可</div>
                </div>
              </fdev-tooltip>
            </f-formitem>

            <f-formitem label="手机号" required>
              <fdev-input
                ref="userModel.telephone"
                v-model="$v.userModel.telephone.$model"
                type="text"
                :rules="[
                  () => $v.userModel.telephone.required || '请输入手机号',
                  () => $v.userModel.telephone.integer || '只能输入数字',
                  () => $v.userModel.telephone.maxLength || '手机号过长'
                ]"
              />
            </f-formitem>
            <f-formitem
              label="工号"
              required
              v-if="this.$v.userModel.email_append.$model === 'xxx'"
            >
              <fdev-input
                ref="userModel.work_num"
                v-model="$v.userModel.work_num.$model"
                type="text"
                :rules="[
                  () => $v.userModel.work_num.required || '请输入手机号'
                ]"
              />
            </f-formitem>
            <f-formitem
              label="工号"
              v-if="this.$v.userModel.email_append.$model !== 'xxx'"
            >
              <fdev-input v-model="userModel.work_num" type="text" hint="" />
            </f-formitem>
            <f-formitem label="公司" required>
              <fdev-select
                ref="userModel.company"
                v-model="$v.userModel.company.$model"
                :options="companyList"
                :readonly="
                  this.$v.userModel.email_append.$model === 'xxx'
                "
                :rules="[() => $v.userModel.company.required || '请选择公司']"
              />
            </f-formitem>

            <f-formitem label="人员职能" required>
              <fdev-select
                ref="userModel.function_id"
                v-model="$v.userModel.function_id.$model"
                :options="statementList"
                option-label="name"
                option-value="id"
                map-options
                emit-value
                :rules="[
                  () => $v.userModel.function_id.required || '请选择人员职能'
                ]"
              />
            </f-formitem>

            <f-formitem label="学历" required>
              <fdev-select
                ref="userModel.education"
                v-model="$v.userModel.education.$model"
                :options="educationOptions"
                clearable
                :rules="[() => $v.userModel.education.required || '请选择学历']"
              />
            </f-formitem>

            <f-formitem label="入职时间" required>
              <f-date
                ref="userModel.create_date"
                v-model="$v.userModel.create_date.$model"
                mask="YYYY/MM/DD"
                :hide-dropdown-icon="true"
                :rules="[
                  () => $v.userModel.create_date.required || '请选择入职时间'
                ]"
              />
            </f-formitem>

            <f-formitem label="工作开始时间" required>
              <f-date
                ref="userModel.start_time"
                v-model="$v.userModel.start_time.$model"
                mask="YYYY/MM/DD"
                :hide-dropdown-icon="true"
                :rules="[
                  () => $v.userModel.start_time.required || '请选择工作开始时间'
                ]"
              />
            </f-formitem>

            <f-formitem label="是否在职" required>
              <fdev-select
                ref="userModel.isLeave"
                v-model="$v.userModel.isLeave.$model"
                :options="isLeaves"
                :rules="[
                  () => $v.userModel.isLeave.required || '请选择是否在职'
                ]"
              />
            </f-formitem>

            <f-formitem label="标签">
              <fdev-select
                use-chips
                use-input
                multiple
                v-model="$v.userModel.tagSelected.$model"
                :options="filteredTags"
                @filter="tagFilter"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.label }}</fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>

            <f-formitem label="备注">
              <fdev-input
                type="textarea"
                v-model="$v.userModel.remark.$model"
              />
            </f-formitem>
          </div>
        </fdev-form>
      </div>

      <div v-else class="token-container">
        <div class="text-subtitle3 q-pb-md">补充git_token信息</div>

        <f-formitem label="gitlab token" required>
          <fdev-input
            ref="git_token"
            v-model="git_token"
            type="text"
            :rules="[() => $v.git_token.required || '请输入gitlab token']"
          />
          <fdev-tooltip>
            <div class="tip">
              <div>
                获取方式：使用本人账号登录gitlab，点击页面右上角个人头像，选择
              </div>
              <div>
                “setting”进入个人中心设置页面，点击页面左侧菜单栏“Access
                Tokens”，
              </div>
              <div>
                填写添加“Access Tokens”的Name，过期时间可选填(建议不填)，Scopes
              </div>
              <div>
                选择，“api”点击“Create personal access
                token”。页面上方会显示本次新
              </div>
              <div>增的Token，复制至此即可</div>
            </div>
          </fdev-tooltip>
        </f-formitem>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn
          v-if="isHaveToken"
          label="新增用户"
          v-forbidMultipleClick
          @click="handleAddUser"
          dialog
        />
        <fdev-btn v-else label="确定" dialog @click="addGitTokenTip" />
      </template>
    </f-dialog>
  </div>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import { required, email, integer, maxLength } from 'vuelidate/lib/validators';
import moment from 'moment';
import {
  createUserModel,
  educationOptions,
  isLeaveOptions,
  isSpdbMacOptions
} from '@/modules/User/utils/model';
import { validate, successNotify, deepClone } from '@/utils/utils';
import SessionStorage from '#/plugins/SessionStorage';

export default {
  name: 'LdapRegisterUser',
  data() {
    return {
      showDialog: true,
      emailAppend: ['xxx'],
      fdevAccount: '',
      ldapInfo: {},
      filter: '',
      userModel: createUserModel(),
      filteredTags: [],
      permissions: [],
      filterGroup: [],
      newRoles: [],
      deepCloneGroups: [],
      isLeaveOptions: isLeaveOptions,
      isSpdbMacOptions: isSpdbMacOptions,
      educationOptions: educationOptions,
      //isPartyMemberOptions: ['是', '否'],
      isPartyMemberOptions: [
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
      is_spdb: '',
      statementList: [],
      companyList: [],
      isHaveToken: true,
      git_token: ''
    };
  },
  props: ['value'],
  validations: {
    userModel: {
      name: {
        required,
        onlyWord(val) {
          if (!val) {
            return true;
          }
          let re = new RegExp(/[\u4e00-\u9fa5]/gm);
          return re.test(val.replace(/(^\s*)|(\s*$)/g, ''));
        }
      },
      git_user_id: {
        required(val) {
          if (this.newUserIsNeedGitlabId) {
            return !!val.trim();
          }
          return true;
        },
        integer
      },
      git_token: {
        required
      },
      area_id: {
        required(val) {
          return !!val;
        }
      },
      telephone: {
        required,
        integer,
        maxLength: maxLength(11)
      },
      group: {
        required
      },
      role: {
        required
      },
      auth: {
        required
      },
      company: {
        required
      },
      isLeave: {
        required
      },
      is_party_member: {
        required
      },
      section: {
        required
      },
      work_num: {
        required
      },
      tagSelected: {},
      email: {
        required,
        email
      },
      email_pre: {
        required
      },
      email_append: {
        required
      },
      create_date: {
        required
      },
      leave_date: {},
      remark: {},
      start_time: {
        required
      },
      education: {
        required
      },
      function_id: {
        required
      }
    },
    git_token: {
      required
    }
  },
  watch: {
    'userModel.email_pre': {
      deep: true,
      handler: function(val) {
        if (val) {
          this.$v.userModel.email.$model =
            val + this.$v.userModel.email_append.$model;
        }
        this.fdevAccount = this.$v.userModel.email_pre.$model;
      }
    },
    value(val) {
      if (val) {
        this.showDialog = true;
      }
    }
  },
  computed: {
    needGitlabToken() {
      return (
        this.group !== '业务部门' &&
        this.group !== '测试中心' &&
        this.group !== '规划部门'
      );
    },
    ...mapState('userForm', [
      'groups',
      'companies',
      'roles',
      'isLeaves',
      'tags',
      'ldapRoles',
      'areaList',
      'functionList',
      'rankList'
    ]),
    ...mapState('managementForm', ['sectionAll']),
    ...mapState('login', {
      ldapUserInfo: 'ldapUserInfo'
    }),
    ...mapState('authorized', {
      auths: 'list'
    }),
    newUserIsNeedGitlabId() {
      if (!this.userModel.group) {
        return true;
      }
      return (
        this.userModel.group.name !== '测试中心' &&
        this.userModel.group.name !== '业务部门' &&
        this.userModel.group.name !== '规划部门'
      );
    }
  },
  methods: {
    ...mapActions('user', {
      addUser: 'add',
      updateGitToken: 'updateGitToken'
    }),
    ...mapActions('managementForm', ['queryAllSection']),
    ...mapActions('userForm', [
      'fetchGroup',
      'fetchCompany',
      'queryRoleForLDAP',
      'fetchTag',
      'queryArea',
      'queryFunction',
      'queryRank'
    ]),
    ...mapActions('authorized', {
      fetchAuth: 'fetch'
    }),
    async addGitTokenTip() {
      this.$v.git_token.$touch();
      this.$refs['git_token'].validate();
      if (this.$v.git_token.$invalid) return;
      await this.updateGitToken({
        user_name_en: this.ldapInfo.userName,
        git_token: this.git_token
      });
      successNotify('添加成功');
      this.$router.push('/account/center');
    },
    showLdapDialog() {
      this.$emit('input', false);
      this.showDialog = false;
    },
    confirmToClose(e) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.showLdapDialog();
        });
    },
    async handleAddUser() {
      this.$v.userModel.$touch();
      // 获取要校验的dom并传入validate，此步骤要求每个ref唯一
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('userModel') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key].$refs.date) {
            return this.$refs[key].$children[0];
          }
          return this.$refs[key];
        })
      );
      // 只要有校验未通过的，$invalid会被validate设为true
      if (this.$v.userModel.$invalid) {
        let _this = this;
        let validateRes = Keys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.userModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }

      let ldapInfo = JSON.parse(SessionStorage.getItem('ldapInfo'));

      let params = {
        ...this.userModel,
        is_spdb: this.is_spdb,
        ldapRegister: true,
        password: ldapInfo.password,
        user_name_en: ldapInfo.userName
      };
      await this.addUser(params);
      successNotify('添加成功');
      this.$router.push('/account/center');
    },
    groupInputFilter(val, update) {
      update(() => {
        this.filterGroup = this.deepCloneGroups.filter(
          tag => tag.label.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    timeOptions(date) {
      const now = moment(new Date()).format('YYYY/MM/DD');
      return date < now;
    },
    tagFilter(val, update) {
      update(() => {
        if (val === '') {
          this.filteredTags = this.tags;
        } else {
          const term = val.toLowerCase();
          this.filteredTags = this.tags.filter(
            tag => tag.label.toLowerCase().indexOf(term) > -1
          );
        }
      });
    },
    async querySelectItem() {
      this.queryArea();
      this.queryFunction();
      this.queryRank();
      this.queryAllSection();
      //this.fetchAuth(); // 权限
      this.fetchTag();
      await this.fetchCompany();
    }
  },
  async mounted() {
    this.ldapInfo = JSON.parse(SessionStorage.getItem('ldapInfo'));
    if (this.ldapInfo.is_once_login !== '4' && !this.ldapInfo.git_token) {
      //  此种情况针对之前在用户模块建了用户,但未登过fdev,还未补充git_token的用户
      this.isHaveToken = false;
    } else {
      await this.querySelectItem();
      await this.queryRoleForLDAP({
        is_spdb: this.ldapInfo.is_spdb ? true : false
      });
      this.is_spdb = this.ldapInfo.is_spdb;
      this.newRoles = deepClone(this.ldapRoles);
      this.statementList = deepClone(this.functionList);
      //人员职能过滤
      if (!this.is_spdb) {
        this.statementList = this.functionList.filter(func => {
          return func.name === '开发岗位' || func.name === '测试岗位';
        });
        this.companyList = this.companies.filter(company => {
          return company.name !== '浦发';
        });
      } else {
        this.userModel.company = this.companies.find(company => {
          return company.label === '浦发';
        });
      }

      this.userModel.email_append = this.is_spdb
        ? 'xxx'
        : 'xxx';
      this.userModel.isLeave = this.isLeaves.find(isLeave => {
        return isLeave.label === '在职';
      });
      this.userModel.email_pre = this.ldapInfo.userName;
      await this.fetchGroup();
      this.deepCloneGroups = deepClone(this.groups);
      this.deepCloneGroups.forEach((group, index) => {
        this.$set(this.deepCloneGroups[index], 'label', group.fullName);
      });
      this.filterGroup = this.deepCloneGroups;
    }
    // 新增用户的时候，不是超级管理员则过滤掉“环境配置管理员”和“基础架构管理员”角色
    this.$v.userModel.$reset();
  }
};
</script>
<style lang="stylus" scoped>

.form
  margin: 0px auto;
  background: white;
  padding: 30px;
.container
  width 800px
.dialog-wrapper
  padding 0 20px
.token-container
 width 480px
.field-select
  width 300px
.input-token
  width 300px
</style>
