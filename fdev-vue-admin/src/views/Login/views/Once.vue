<template>
  <form @submit.prevent="handleOnce" class="form relative-position">
    <!--<router-link to="/login" class="back">
      <fdev-icon name="reply" color="primary" size="24px" />
    </router-link>-->
    <Loading :visible="loading">
      <div class="relative-position">
        <fdev-input
          ref="password"
          dense
          class="col"
          v-model="$v.password.$model"
          type="password"
          placeholder="请输入新密码"
          autofocus
          :rules="[() => !$v.password.$error || '请输入新密码']"
          autocomplete="new-password"
        />
        <fdev-icon name="help_outline" color="blue" class="tooltip">
          <fdev-tooltip
            anchor="top right"
            self="bottom left"
            class="bg-grey-11"
          >
            <div class="tip">
              <div>密码由数字，字母，符号至少任意两者组</div>
              <div>合，不少于8位数并且与前一次密码不同</div>
            </div>
          </fdev-tooltip>
        </fdev-icon>
      </div>
      <div class="relative-position">
        <fdev-input
          ref="password_repeat"
          dense
          class="col"
          v-model="$v.password_repeat.$model"
          type="password"
          placeholder="再次输入新密码"
          :rules="[
            () => $v.password_repeat.required || '请再次输入新密码',
            () => $v.password_repeat.isRepeat || '两次密码不一致'
          ]"
          autocomplete="new-password"
        />
      </div>
      <div class="relative-position" v-if="needGitlabToken">
        <fdev-input
          ref="token"
          dense
          class="col q-mb-md "
          v-model="$v.token.$model"
          type="text"
          placeholder="gitlab token"
          :rules="[() => !$v.token.$error || '请输入gitlab token']"
        >
        </fdev-input>
        <fdev-tooltip
          anchor="bottom right"
          self="bottom left"
          class="bg-grey-11"
        >
          <div class="tip">
            <div>
              获取方式：使用本人账号登录gitlab，点击页面右上角个人头像，选择
            </div>
            <div>
              “setting”进入个人中心设置页面，点击页面左侧菜单栏“Access Tokens”，
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
        <div
          class="q-field__messages col row absolute-bottom items-start relative-position"
        >
          <div class="links">
            <a
              href="http://xxx/profile/personal_access_tokens"
              target="_blank"
            >
              <i class="mdi-set mdi-gitlab"></i>&nbsp;&nbsp;GitLab
            </a>
          </div>
        </div>
      </div>

      <fdev-btn type="submit" color="primary" class="full-width" label="设置" />
    </Loading>
  </form>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import { validate } from '@/utils/utils';
import Loading from '@/components/Loading';
import { mapState } from 'vuex';

export default {
  name: 'Once',
  components: { Loading },
  data() {
    return {
      password: '',
      password_repeat: '',
      token: '',
      loading: false
    };
  },
  validations: {
    password: {
      required
    },
    password_repeat: {
      required,
      isRepeat(value) {
        if (!value || !this.password) {
          return true;
        }
        let password = this.password;
        if (value == password) {
          return true;
        }
        return false;
      }
    },
    token: {
      required(val) {
        if (this.needGitlabToken) {
          return !!val.trim();
        }
        return true;
      }
    }
  },
  computed: {
    ...mapState('login', ['group']),
    needGitlabToken() {
      return (
        this.group !== '业务部门' &&
        this.group !== '测试中心' &&
        this.group !== '规划部门'
      );
    }
  },
  methods: {
    async handleOnce() {
      this.$v.$touch();
      validate(this.$refs);
      if (this.$v.$invalid) {
        return;
      }
      this.loading = true;
      try {
        await this.$store.dispatch('login/once', {
          type: 'account',
          password: this.password,
          token: this.token
        });
      } finally {
        this.loading = false;
      }
    }
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.form
  margin: 0 auto;
  padding: 24px;
  border-radius: 3px;
  width: 368px;
  background: white;
@media screen and (max-width: $sizes.sm)
  .form
    width: 95%;
.back
  position: absolute;
  top: 4px;
  left: 4px;
.tooltip
  position: absolute;
  top: 44px;
  right: 0;
  font-size: 20px;
.tip
  color: $tertiary;
</style>
