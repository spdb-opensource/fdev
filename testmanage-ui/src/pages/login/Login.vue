<template>
  <el-container>
    <el-main class="main">
      <el-image
        style="width: 100%; height: 100%; margin: 0; padding: 0;"
        :src="imgUrl"
      ></el-image>

      <div class="logo-container">
        <el-row>
          <el-col>
            <el-image
              class="block"
              style="width: 400px; height:200px;"
              :src="logoUrl"
            ></el-image>
          </el-col>
        </el-row>
      </div>

      <div class="login-container">
        <el-button
          v-show="isDevelopment"
          round
          type="primary"
          class="loginBtn"
          @click="submit('loginForm')"
          >&nbsp;开发登录&nbsp;
        </el-button>
        <el-row>
          <el-col>
            <el-card class="box-card">
              <div slot="header" class="center-div">
                <span class="text-20">平台跳转</span>
              </div>
              <el-button
                round
                plain
                type="primary"
                class="loginBtn"
                @click="submitFDEV('loginForm')"
              >
                f '(dev)
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <div class="bottom-logo-container">
        <p>@f’(sit)</p>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import { Login, OauthGetToken } from '@/services/login';
import { getUserRole } from '@/common/utlis';
export default {
  name: 'Login',
  data() {
    return {
      logoUrl: require('../../assets/sit.gif'),
      imgUrl: require('../../assets/go_test_new.jpg'),
      labelPosition: 'right',
      isDevelopment: process.env.NODE_ENV === 'development',
      // loginForm: {
      //   username: "",
      //   password: ""
      // },
      redirect: ''
      // rules: {
      //   username: [
      //     { required: true, message: "请输入用户名", trigger: "blur" }
      //   ],
      //   password: [{ required: true, message: "请输入密码", trigger: "blur" }]
      // }
    };
  },

  methods: {
    async submitFDEV(formName) {
      //this.$refs[formName].validate((valid) => {
      //if (valid) {
      OauthGetToken({
        // username: this.loginForm.username,
        // password: this.loginForm.password,
      }).then(res => {
        window.location.href = res;
      });
      //});
    },

    async submit(formName) {
      Login({
        user_name_en: 'T-lizy',
        password: 'xxx'
      }).then(res => {
        sessionStorage.setItem('userInfo', JSON.stringify(res));
        let userRole = getUserRole();
        localStorage.setItem('userToken', res.userToken);
        sessionStorage.setItem('Trole', userRole);
        sessionStorage.setItem('TuserName', res.user_name_cn);
        sessionStorage.setItem('user_en_name', res.user_name_en);
        localStorage.setItem('user_en_name', res.user_name_en);
        sessionStorage.setItem('isAssessor', res.isAssessor);
        sessionStorage.setItem('userId', res.user_id);
        sessionStorage.setItem('mantisToken', res.mantis_token);
        sessionStorage.setItem('role_en_name', res.role_en_name);
        sessionStorage.setItem('group_id', res.group_id);
        sessionStorage.setItem('userRole', JSON.stringify(res.role));
        sessionStorage.setItem(
          'groupName',
          res.group ? res.group.fullName : ''
        );
        this.$router.push({ path: '/Torder' });
      });
    }
  }
};
</script>

<style scoped>
.line-height {
  margin-top: 10px;
}
.line-right {
  margin-right: 40px;
}
.main {
  position: relative;
  padding: 0px;
  overflow: hidden;
  height: 100vh;
}
.logo-container {
  position: absolute;
  top: 1%;
  right: 70%;
}

.el-card >>> .el-card__header {
  color: #3385ff;
  font-size: 25px;
  font-weight: 500;
}
.el-card >>> .el-card__header {
  border-bottom: 1px solid #409eff;
}

.login-container {
  position: absolute;
  top: 35%;
  right: 8%;
  width: 400px;
  height: 400px;
  filter: alpha(opacity=92);
  opacity: 0.92;
}
.loginBtn {
  font-size: 15px;
  width: 200px;
}
.box-card {
  width: 280px;
  background: rgba(255, 255, 255, 0.8);
}
.text-14 {
  font-size: 14px;
}
.text-20 {
  font-size: 20px;
}
.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: '';
}
.clearfix:after {
  clear: both;
}
.el-button-group {
  display: flex;
}
.notifySubmit {
  color: red;
  font-size: 12px;
}
.bottom-logo-container {
  position: absolute;
  top: 90%;
  right: 5%;
  color: white;
}
.center-div {
  margin: 0 auto;
}
</style>
