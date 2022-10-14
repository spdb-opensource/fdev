<template>
  <Loading :visible="loading">
    <f-block class="column">
      <div class="row no-wrap q-mb-lg justify-between">
        <div class="text-h6">
          {{ user.name }}
          <f-icon
            v-if="user.status === '0'"
            name="preson_c_o"
            :class="{
              'text-positive': user.status === '0',
              'text-negative': user.status === '1'
            }"
          />
          <div class="text-subtitle1">{{ user.email }}</div>
        </div>
        <fdev-btn
          ficon="edit"
          normal
          label="编辑"
          @click="handleUpdateUserModalOpen"
        />
      </div>
      <div class="row q-gutter-md">
        <f-formitem label="公司">
          {{ user.company ? user.company.name : '' }}
        </f-formitem>
        <f-formitem label="小组">
          {{ user.groupFullName }}
        </f-formitem>
        <f-formitem label="所属条线">
          <span
            :title="user.sectionInfo ? user.sectionInfo.sectionNameCn : '-'"
          >
            {{ user.sectionInfo ? user.sectionInfo.sectionNameCn : '-' }}</span
          >
        </f-formitem>
        <f-formitem label="角色">
          <fdev-chip
            v-for="tag in user.role"
            :key="tag.id"
            square
            dense
            color="teal"
            text-color="white"
            class="q-mt-none"
          >
            {{ tag.name }}
          </fdev-chip>
        </f-formitem>
        <!-- <f-formitem label="权限">
          {{ user.auth.name_cn }}
        </f-formitem> -->
        <f-formitem label="手机号">
          {{ user.telephone }}
        </f-formitem>
        <f-formitem label="工号" v-if="showArea">
          {{ user.work_num ? user.work_num : '-' }}
        </f-formitem>
        <f-formitem label="地域">
          {{ user.area.name ? user.area.name : '-' }}
        </f-formitem>
        <f-formitem label="政治面貌">
          {{ user.is_party_member | isPartyMember }}
        </f-formitem>
        <f-formitem label="人员职能">
          {{ user.function ? user.function.name : '' }}
        </f-formitem>
        <f-formitem label="职级" v-if="!isSpdbUser">
          {{ user.rank ? user.rank.name : '' }}
        </f-formitem>
        <f-formitem label="学历">
          {{ user.education }}
        </f-formitem>
        <f-formitem label="入职时间">
          {{ user.create_date }}
        </f-formitem>
        <f-formitem label="工作开始时间">
          {{ user.start_time }}
        </f-formitem>

        <f-formitem label="离职/换岗时间">
          {{ user.leave_date }}
        </f-formitem>
        <f-formitem label="标签">
          <fdev-chip
            v-for="tag in user.tagSelected"
            :key="tag.id"
            square
            dense
            color="teal"
            text-color="white"
            class="q-mt-none"
          >
            {{ tag.name }}
          </fdev-chip>
        </f-formitem>

        <f-formitem label="备注">
          <span v-html="descFilter(user.remark)" />
        </f-formitem>
      </div>
      <fdev-separator class="q-my-xl" />
      <div class="text-h6 q-mb-lg">账号</div>
      <div class="row q-gutter-md">
        <f-formitem label="gitlab Id">
          {{ user.git_user_id }}
        </f-formitem>
        <f-formitem label="gitlab username">
          {{ user.git_user }}
        </f-formitem>
        <f-formitem label="access token">
          {{ user.git_token }}
        </f-formitem>
      </div>
    </f-block>
    <fdev-form>
      <f-dialog
        right
        v-model="updateUserModalOpen"
        @shake="confirmToClose"
        title="修改用户"
      >
        <div class="rdia-dc-w row justify-between">
          <f-formitem label="姓名" required>
            <fdev-input
              ref="updateUserModel.name"
              v-model="$v.updateUserModel.name.$model"
              :autofocus="$q.platform.is.desktop"
              :rules="[() => $v.updateUserModel.name.required || '请输入姓名']"
            />
          </f-formitem>
          <f-formitem label="邮箱" required>
            <fdev-input
              ref="updateUserModel.email"
              v-model="$v.updateUserModel.email.$model"
              :rules="[
                () =>
                  $v.updateUserModel.email.required || '请输入正确的邮箱地址'
              ]"
              disable
            />
          </f-formitem>
          <f-formitem label="地域" required>
            <fdev-select
              ref="userModel.area_id"
              v-model="$v.updateUserModel.area_id.$model"
              :options="areaList"
              option-label="name"
              option-value="id"
              map-options
              emit-value
              :rules="[
                () => $v.updateUserModel.area_id.required || '请选择地域'
              ]"
            />
          </f-formitem>
          <f-formitem label="工号" v-if="isSpdbUser" required>
            <fdev-input
              ref="updateUserModel.work_num"
              v-model="$v.updateUserModel.work_num.$model"
              :rules="[() => $v.updateUserModel.work_num.isOk || '请输入工号']"
            />
          </f-formitem>
          <f-formitem label="政治面貌" required>
            <fdev-select
              ref="updateUserModel.is_party_member"
              v-model="$v.updateUserModel.is_party_member.$model"
              :options="is_party_memberOptions"
              option-value="value"
              option-label="label"
              map-options
              emit-value
              :display-value="
                formatSelectDisplay(
                  is_party_memberOptions,
                  updateUserModel.is_party_member
                )
              "
              :rules="[
                () =>
                  $v.updateUserModel.is_party_member.required ||
                  '请选择政治面貌'
              ]"
          /></f-formitem>
          <f-formitem label="gitlab Id" required>
            <fdev-input
              ref="updateUserModel.git_user_id"
              v-model="$v.updateUserModel.git_user_id.$model"
              type="text"
              :rules="[
                () =>
                  $v.updateUserModel.git_user_id.required ||
                  '请输入gitlab账号Id',
                () => $v.updateUserModel.git_user_id.integer || '只能输入数字'
              ]"
            />
          </f-formitem>
          <f-formitem label="git token" required>
            <fdev-input
              ref="updateUserModel.gitToken"
              v-model="$v.updateUserModel.gitToken.$model"
              type="text"
              :rules="[
                () =>
                  $v.updateUserModel.gitToken.required || '请输入gitlab token'
              ]"
            />
          </f-formitem>
          <f-formitem label="手机号" required>
            <fdev-input
              ref="updateUserModel.telephone"
              v-model="$v.updateUserModel.telephone.$model"
              type="text"
              :rules="[
                () => $v.updateUserModel.telephone.required || '请输入手机号',
                $v.updateUserModel.telephone.integer || '只能输入数字',
                $v.updateUserModel.telephone.maxLength || '手机号过长'
              ]"
            />
          </f-formitem>

          <f-formitem label="人员职能" required>
            <fdev-select
              ref="updateUserModel.function_id"
              v-model="$v.updateUserModel.function_id.$model"
              :options="functionList"
              option-label="name"
              option-value="id"
              map-options
              emit-value
              :rules="[
                () =>
                  $v.updateUserModel.function_id.required || '请选择人员职能'
              ]"
            />
          </f-formitem>

          <f-formitem label="职级" required v-if="!isSpdbUser">
            <fdev-select
              ref="updateUserModel.rank_id"
              v-model="$v.updateUserModel.rank_id.$model"
              :options="rankList"
              option-value="id"
              option-label="name"
              map-options
              emit-value
              :rules="[
                () => $v.updateUserModel.rank_id.required || '请选择职级'
              ]"
            />
          </f-formitem>
          <f-formitem label="学历" required>
            <fdev-select
              ref="updateUserModel.education"
              v-model="$v.updateUserModel.education.$model"
              :options="educationOptions"
              clearable
              :rules="[
                () => $v.updateUserModel.education.required || '请选择学历'
              ]"
            />
          </f-formitem>
          <f-formitem label="入职时间" required>
            <f-date
              v-model="updateUserModel.create_date"
              ref="updateUserModel.create_date"
              :rules="[
                () =>
                  $v.updateUserModel.create_date.required || '请选择入职时间'
              ]"
            />
          </f-formitem>
          <f-formitem label="工作开始时间" required>
            <f-date
              ref="updateUserModel.start_time"
              v-model="updateUserModel.start_time"
              :rules="[
                () =>
                  $v.updateUserModel.start_time.required || '请选择工作开始时间'
              ]"
            />
          </f-formitem>
          <f-formitem label="离职/换岗时间">
            <f-date
              ref="updateUserModel.leave_date"
              v-model="$v.updateUserModel.leave_date.$model"
              hint=""
            />
          </f-formitem>
          <f-formitem label="标签">
            <fdev-select
              use-input
              multiple
              input-debounce="0"
              v-model="$v.updateUserModel.tagSelected.$model"
              :options="filteredTags"
              @filter="tagFilter"
              hint=""
              @new-value="handleAddTag"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.label">{{
                      scope.opt.label
                    }}</fdev-item-label>
                  </fdev-item-section>
                  <fdev-item-section side>
                    <f-icon
                      name="substract_r_o"
                      style="color:red"
                      @click.prevent.stop="
                        () => handleRemoveTag(scope.opt, updateUserModel)
                      "
                    />
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem label="备注" full-width>
            <fdev-input
              type="textarea"
              v-model="$v.updateUserModel.remark.$model"
              hint=""
            />
          </f-formitem>
        </div>
        <template v-slot:btnSlot
          ><fdev-btn
            label="取消"
            dialog
            outline
            @click="updateUserModalOpen = false"/>
          <fdev-btn
            dialog
            @click="handleUpdateUser"
            label="修改用户"
            :loading="globalLoading['user/update']"
        /></template>
      </f-dialog>
    </fdev-form>
  </Loading>
</template>
<script>
import { required, integer, maxLength } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
import moment from 'moment';
import { mapState, mapActions } from 'vuex';
import { successNotify, validate, formatSelectDisplay } from '@/utils/utils';
import { createUserModel, educationOptions } from '@/modules/User/utils/model';

export default {
  name: 'myInfo',
  components: { Loading },
  data() {
    return {
      updateUserModalOpen: false,
      updateUserModel: createUserModel(),
      filteredTags: [],
      terms: [],
      filter: '',
      loading: false,
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
      ]
    };
  },
  validations: {
    updateUserModel: {
      area_id: {
        required(val) {
          if (this.isSpdbUser) {
            return !!val;
          }
          return true;
        }
      },
      name: {
        required
      },
      email: {
        required
      },
      git_user_id: {
        required(val) {
          const { name } = this.user.group;
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
      telephone: {
        required,
        integer,
        maxLength: maxLength(11)
      },
      gitToken: {
        required(val) {
          const { name } = this.user.group;
          if (
            name === '业务部门' ||
            name === '测试中心' ||
            name === '规划部门'
          ) {
            return true;
          }
          return !!val.trim();
        }
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
      education: {
        required
      },
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
      },
      is_party_member: {
        required
      },
      work_num: {
        isOk(val) {
          if (this.user.company && this.user.company.name !== '浦发') {
            return true;
          } else {
            return val;
          }
        }
      }
    }
  },
  props: {
    display: {
      type: Boolean,
      default: true
    }
  },
  filters: {
    isPartyMember(val) {
      if (val == '0') {
        return '中共党员';
      } else if (val == '1') {
        return '共青团员';
      } else if (val == '2') {
        return '群众';
      } else {
        return '-';
      }
    }
  },
  watch: {
    terms(val) {
      this.filter = val.map(item => item.trim()).toString();
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapState('userForm', [
      'groups',
      'tags',
      'areaList',
      'functionList',
      'rankList'
    ]),
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
  methods: {
    ...mapActions('user', {
      updateUser: 'update'
    }),
    ...mapActions('userForm', [
      'fetchGroup',
      'fetchTag',
      'addTag',
      'removeTag',
      'queryArea',
      'queryFunction',
      'queryRank'
    ]),
    async handleUpdateUserModalOpen() {
      this.updateUserModalOpen = true;
      this.loading = true;
      this.querySelectItem();
      this.updateUserModel = {
        ...createUserModel(),
        ...this.user
      };
      this.$v.updateUserModel.$reset();
      this.loading = false;
    },
    formatSelectDisplay,
    //表单校验
    verifyModel() {
      try {
        let unitModuleKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.indexOf('updateUserModel') > -1;
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
        if (this.$v.updateUserModel.$invalid) {
          const validateRes = unitModuleKeys.every(item => {
            if (item.indexOf('.') === -1) {
              return true;
            }
            const itemArr = item.split('.');
            return !_this.$v.updateUserModel[itemArr[1]].$invalid;
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
    async handleUpdateUser() {
      if (!this.verifyModel()) {
        return;
      }
      this.updateUsers();
    },
    async updateUsers() {
      await this.updateUser({
        ...this.updateUserModel,
        git_token: this.updateUserModel.gitToken,
        personCenter: 'personCenter'
      });
      successNotify('修改成功');
      this.user.groupFullName = this.groups[0].fullName;
      this.updateUserModalOpen = false;
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
    async handleRemoveTag(opt, model) {
      if (opt.count !== 0) {
        return this.$q
          .dialog({
            title: `删除标签`,
            message: `有 ${opt.count} 个人属于 ${opt.label} , 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.removeTag(opt);
            this.filteredTags = this.tags;
            successNotify('标签删除成功');
            let ind = model.tagSelected.findIndex(tag => tag.id === opt.id);
            if (ind > -1) {
              model.tagSelected.splice(ind, 1);
            }
          });
      } else {
        return this.$q
          .dialog({
            title: `删除标签`,
            message: `确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.removeTag(opt);
            this.filteredTags = this.tags;
            successNotify('标签删除成功');
            let ind = model.tagSelected.findIndex(tag => tag.id === opt.id);
            if (ind > -1) {
              model.tagSelected.splice(ind, 1);
            }
          });
      }
    },
    async handleAddTag(val, done) {
      if (val.length > 0) {
        if (!this.tags.some(tag => tag.label === val)) {
          await this.addTag({
            name: val
          });
          let tag = this.tags.find(tag => tag.label === val);
          done(tag);
        }
      }
    },
    timeOptions(date) {
      const now = moment(new Date()).format('YYYY/MM/DD');
      return date < now;
    },
    querySelectItem() {
      this.queryArea();
      this.queryFunction();
      this.queryRank();
      this.fetchTag();
    },
    descFilter(input) {
      if (!input) {
        return input;
      }
      input = input.replace(/<[^>]+>/g, '');
      const reg = new RegExp(/\n/g);
      return input.replace(reg, '</br>');
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
          this.updateUserModalOpen = false;
        });
    }
  },
  async created() {
    this.loading = true;
    await this.fetchGroup({ id: this.user.group.id });
    this.user.groupFullName = this.groups[0].fullName;
    this.loading = false;
  }
};
</script>

<style lang="stylus" scoped></style>
