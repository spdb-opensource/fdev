<template>
  <div class="page-container">
    <strong class="title">用户信息维护</strong>
    <MainList
      @query="query"
      @selectChange="selectChange"
      @delOneItem="delOneItem"
      @delItems="delItems"
      ref="child"
      @add="showAddDialog"
      :tableData="tableInfo"
      :showItem="json"
      @edit="showEditDialog"
      @showDetail="showItem"
      title="用户名"
    />
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form
        :inline="true"
        :model="userInfoModel"
        label-position="right"
        :rules="rule"
        label-width="100px"
        ref="userInfoModel"
      >
        <el-form-item prop="userName" label="用户名">
          <el-input v-model="userInfoModel.userName" />
        </el-form-item>
        <el-form-item prop="queryPwd" label="密码">
          <el-input type="password" v-model="userInfoModel.queryPwd" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="用户详情"
      :visible.sync="showUserDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <description align="center" :span="9" label="用户名" class="q-mt-md">{{
        userDetail.userName
      }}</description>
      <description align="center" :span="9" label="用户密码" class="q-mt-md">{{
        userDetail.queryPwd
      }}</description>
    </Dialog>
  </div>
</template>
<script>
import { validate } from '@/common/utlis';
import MainList from './Components/MainList';
import { mapState, mapActions } from 'vuex';
export default {
  name: 'MenuInfo',
  components: { MainList },
  data() {
    return {
      test: '',
      userInfoModel: {
        userName: '',
        queryPwd: ''
      },
      rule: {
        userName: [
          { required: true, message: '请填写用户名', trigger: 'blur' }
        ],
        queryPwd: [{ required: true, message: '请填写密码', trigger: 'blur' }]
      },
      showDialog: false,
      userIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      currentUser: {
        userId: ''
      },
      json: [{ prop: 'userName', label: '用户名' }],
      showUserDetail: false,
      userDetail: {}
    };
  },
  computed: {
    ...mapState('autoTestForm', ['userList'])
  },
  watch: {},
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'addUser',
      'queryUser',
      'deleteUser',
      'updateUser'
    ]),
    showAddDialog() {
      this.userInfoModel = {
        userName: '',
        queryPwd: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '用户新增';
    },
    showEditDialog(data) {
      let item = {
        userName: data.row.userName,
        queryPwd: data.row.queryPwd
      };
      this.currentUser.userId = data.row.userId;
      this.userInfoModel = item;
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '用户信息编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showUserDetail = false;
      this.userInfoModel = {
        userName: '',
        queryPwd: ''
      };
      this.currentUser = {
        userId: ''
      };
    },
    async confirm() {
      await validate(this.$refs['userInfoModel']);
      if (this.isEditModel) {
        let param = {
          userId: this.currentUser.userId,
          ...this.userInfoModel
        };
        await this.updateUser(param);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addUser(this.userInfoModel);
        this.$message({
          type: 'success',
          message: '新增成功!'
        });
      }
      await this.query();
      this.showDialog = false;
    },
    async query(searchValue) {
      let param = {
        userName: searchValue || '',
        valid: '0'
      };
      await this.queryUser(param);
      this.tableInfo = this.userList.user || [];
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.userIds.push(item.userId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该用户?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteUser({ userId: [row.userId.toString()] });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除用户?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteUser({ userId: this.userIds });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    showItem(row) {
      this.userDetail = row;
      this.showUserDetail = true;
    }
  },
  async mounted() {
    let param = {
      userName: '',
      valid: '0'
    };
    await this.queryUser(param);
    this.tableInfo = this.userList.user || [];
  }
};
</script>
<style scoped>
.el-textarea {
  width: 202px !important;
}
.el-input--suffix .el-input__inner {
  max-width: 202px !important;
}
.title {
  font-size: 20px;
  text-align: left;
  margin-bottom: 16px;
  display: inline-block;
}
</style>
