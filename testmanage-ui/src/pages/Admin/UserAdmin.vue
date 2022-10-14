<template>
  <div class="page-container">
    <el-container>
      <el-header>
        <el-form :inline="true" class="demo-form-inline">
          <el-col :span="5">
            <el-form-item>
              <el-input
                v-model="userName"
                placeholder="请输入姓名"
                maxlength="90"
                clearable
                @keyup.enter.native="search()"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item>
              <el-select
                v-model="groupName"
                placeholder="请选择小组"
                @keyup.enter.native="search()"
                clearable
                filterable
              >
                <el-option
                  v-for="(item, index) in groupAll"
                  :key="index"
                  :label="item.fullName"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item>
              <el-select
                v-model="role"
                placeholder="请选择角色"
                @keyup.enter.native="search()"
                clearable
                filterable
              >
                <el-option
                  v-for="(item, index) in roleAdd"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="2">
            <el-form-item>
              <el-button type="primary" @click="search()">查询</el-button>
            </el-form-item>
          </el-col>
        </el-form>
      </el-header>

      <el-main style="padding:0px">
        <el-table
          stripe
          :data="userList.list"
          tooltip-effect="dark"
          :cell-style="cellStyle"
          style="width: 100%;color:black"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column prop="user_name_cn" label="姓名" width="130">
            <template slot-scope="scope">
              <el-link
                @click="goDetail(scope.row)"
                :class="scope.row.status === '0' ? 'link' : 'red'"
              >
                {{ scope.row.user_name_cn }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column
            prop="user_name_en"
            label="用户名"
            width="140"
          ></el-table-column>
          <el-table-column prop="group.name" label="小组"></el-table-column>
          <el-table-column label="角色">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="getRole(scope.row.role).join(',')"
                placement="top-start"
                class="tooltip"
              >
                <el-link :underline="false">
                  {{ getRole(scope.row.role).join(',') }}
                </el-link>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="ftms_level" label="级别"> </el-table-column>
          <el-table-column
            prop="email"
            label="邮箱"
            width="210"
          ></el-table-column>
          <el-table-column prop="telephone" label="电话"></el-table-column>
          <el-table-column label="操作" v-if="userRole > 20">
            <template slot-scope="scope">
              <el-tooltip effect="dark" content="修改用户状态" placement="top">
                <el-button
                  icon="el-icon-edit"
                  circle
                  @click="handleEdit(scope.$index, scope.row)"
                  :disabled="!checkDisable(scope.row)"
                ></el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 50, 100, 200, 500]"
            :page-size="pageSize"
            layout="sizes, prev, pager, next, jumper"
            :total="total"
          ></el-pagination>
        </div>
      </el-main>
    </el-container>

    <!-- 修改用户弹窗 -->
    <el-dialog
      title="修改用户"
      :visible.sync="editFormVisible"
      :close-on-click-modal="false"
      width="35%"
      class="abow_dialog"
    >
      <el-form :model="editForm" ref="editForm" :label-width="formLabelWidth">
        <el-form-item label="用户名 ：">
          <el-input
            v-model="editForm.user_name_en"
            autocomplete="off"
            style="width:70%"
            class="dialogMantis"
            disabled
          ></el-input>
          <span class="message" v-if="!$v.editForm.user_name_en.required"
            >用户名不能为空</span
          >
        </el-form-item>
        <el-form-item label="姓名 ：">
          <el-input
            v-model="editForm.user_name_cn"
            autocomplete="off"
            style="width:70%"
            class="dialogMantis"
            disabled
          ></el-input>
        </el-form-item>
        <el-form-item label="电话 ：" prop="telephone">
          <el-input
            v-model="editForm.telephone"
            autocomplete="off"
            style="width:70%"
            class="dialogMantis"
            disabled
          ></el-input>
        </el-form-item>
        <el-form-item label="邮箱 ：" prop="email">
          <el-input
            v-model="editForm.email"
            autocomplete="off"
            style="width:70%"
            class="dialogMantis"
            disabled
          ></el-input>
        </el-form-item>
        <el-form-item label="角色 ：">
          <el-select
            v-model="editForm_role_en_name"
            multiple
            placeholder="请选择"
            style="width:70%"
          >
            <el-option
              v-for="item in rolesFilter"
              :key="item.name"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="级别 ：">
          <el-select
            v-model="editForm.level"
            placeholder="请选择"
            style="width:70%"
          >
            <el-option
              v-for="item in levelOptions"
              :key="item.label"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所在组 ：">
          <el-select
            v-model="editForm.group_id"
            placeholder="请选择"
            style="width:70%"
            disabled
          >
            <el-option
              v-for="item in groupAll"
              :key="item.id"
              :label="item.fullName"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="editFormVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click.native="(editFormVisible = false), userEditConfirm()"
          >确 定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import { mapState, mapActions } from 'vuex';
import { createAdminModel } from './model';

export default {
  name: 'UserAdmin',
  data() {
    return {
      //用户权限
      userRole: sessionStorage.getItem('Trole'),
      //用户英文名
      userEnName: '' + sessionStorage.getItem('user_en_name'),
      value: 'all',
      editFormVisible: false,
      form: createAdminModel(),
      editForm: {
        rank: {}
      },
      roleAdd: [],
      formLabelWidth: '30%',
      currentPage: 1,
      pageSize: 10,
      editForm_role_en_name: [],
      userName: '',
      groupName: '',
      role: '',
      levelOptions: [
        { label: '初级', value: '初级' },
        { label: '中级', value: '中级' },
        { label: '高级', value: '高级' },
        { label: '资深', value: '资深' }
      ],
      currentUser: JSON.parse(sessionStorage.getItem('userInfo')),
      rolesNoTui: []
    };
  },
  validations: {
    editForm: {
      user_name_en: {
        required
      }
    }
  },
  computed: {
    ...mapState('adminForm', [
      'groupList',
      'userAdminData',
      'userCount',
      'userUpdataData',
      'groupAll',
      'roleList',
      'userExport',
      'userList',
      'userInfo'
    ]),
    total() {
      return this.userList.total || 0;
    },
    rolesFilter() {
      let arr = [
        '测试人员',
        '玉衡-测试管理员',
        '玉衡-测试组长',
        '玉衡超级管理员',
        '玉衡-审批人员',
        '玉衡-自动化',
        '玉衡-安全测试组长'
      ];
      return this.roleList.filter(role => {
        return arr.indexOf(role.name) !== -1;
      });
    }
  },
  methods: {
    ...mapActions('adminForm', [
      'queryGroup',
      'queryUserAdmin',
      'countUser',
      'updateUser',
      'queryAllGroup',
      'queryAllRole',
      'exportUserList',
      'queryAllUserName',
      'queryUser',
      'update'
    ]),
    handleSizeChange: function(size) {
      this.pageSize = size;
      this.queryUserList();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.queryUserList();
    },

    queryUserList() {
      let search = [];
      if (this.role) {
        search.push(this.role);
      }
      if (this.userName) {
        search.push(this.userName);
      }
      let param = {
        search: search,
        page: this.currentPage,
        per_page: this.pageSize,
        group_id: this.groupName
      };
      this.queryUser(param);
    },

    search() {
      this.currentPage = 1;
      this.queryUserList();
    },

    cellStyle(row, column, rowIndex, columnIndex) {
      if (row.row.is_leave === '1' && row.columnIndex == 1) {
        return 'color:red';
      }
    },
    // 修改，删除按钮权限控制test(params) &&
    checkDisable(user) {
      if (
        !this.currentUser.role.some(val => {
          return (
            val.name === '玉衡超级管理员' || val.name === '玉衡-测试管理员'
          );
        })
      ) {
        return false;
      }

      let check1 = false;
      let check2 = false;
      if (
        this.currentUser.role.some(val => {
          return val.name === '玉衡-测试管理员';
        })
      ) {
        if (
          user.role.some(val => {
            return (
              (val.name === '玉衡-测试管理员' ||
                val.name === '玉衡超级管理员') &&
              this.currentUser.id !== user.id
            );
          })
        ) {
          check1 = false;
        } else {
          check1 = true;
        }
      }

      if (
        this.currentUser.role.some(val => {
          return val.name === '玉衡超级管理员';
        })
      ) {
        if (
          user.role.some(val => {
            return (
              val.name === '玉衡超级管理员' && this.currentUser.id !== user.id
            );
          })
        ) {
          check2 = false;
        } else {
          check2 = true;
        }
      }
      return check1 || check2;
    },

    getRole(roles) {
      return roles.reduce((total, current) => {
        total.push(current.name);
        return total;
      }, []);
    },

    getRolesId(roles) {
      return roles.reduce((total, current) => {
        total.push(current.id);
        return total;
      }, []);
    },

    // 修改用户信息,打开按钮事件
    handleEdit(index, row) {
      //查询当前用户组
      this.editFormVisible = true;
      this.editForm = Object.assign({}, row);
      let arr = [
        '测试人员',
        '玉衡-测试管理员',
        '玉衡-测试组长',
        '玉衡超级管理员',
        '玉衡-审批人员',
        '玉衡-自动化',
        '玉衡-安全测试组长'
      ];
      this.editForm_role_en_name = this.getRolesId(
        row.role.filter(item => {
          return arr.indexOf(item.name) !== -1;
        })
      );
      this.rolesNoTui = this.getRolesId(
        row.role.filter(item => {
          return arr.indexOf(item.name) === -1;
        })
      );
      this.$set(this.editForm, 'level', row.ftms_level || '');
    },
    // 点击修改信息弹框的确定按钮
    async userEditConfirm() {
      await this.update({
        user_id: this.editForm.id,
        role_id: [...this.editForm_role_en_name, ...this.rolesNoTui],
        level: this.editForm.level
      });
      // 如果修改的是自己，则session中更新userInfo信息
      if (this.editForm.id === this.currentUser.id) {
        sessionStorage.setItem('userInfo', JSON.stringify(this.userInfo));
      }
      this.queryUserList();
      this.editForm = this.userUpdataData;
      if (this.userEnName == this.editForm.user_name_en) {
        if (this.editForm_role_en_name.indexOf('assessor') > -1) {
          sessionStorage.setItem('Trole', 9);
        }
        if (this.editForm_role_en_name.indexOf('visitor') > -1) {
          sessionStorage.setItem('Trole', 10);
        }
        if (this.editForm_role_en_name.indexOf('member') > -1) {
          sessionStorage.setItem('Trole', 20);
        }
        if (this.editForm_role_en_name.indexOf('groupleader') > -1) {
          sessionStorage.setItem('Trole', 30);
        }
        if (this.editForm_role_en_name.indexOf('admin') > -1) {
          sessionStorage.setItem('Trole', 40);
        }
        if (this.editForm_role_en_name.indexOf('master') > -1) {
          sessionStorage.setItem('Trole', 50);
        }
        this.userRole = sessionStorage.getItem('Trole');
      }

      this.$message({
        showClose: true,
        type: 'success',
        message: '修改成功!'
      });
    },

    // 查询所有角色
    async queryRoleList() {
      await this.queryAllRole();
      this.roleAdd = this.roleList.map(role => {
        return {
          value: role.name,
          label: role.name
        };
      });
    },

    goDetail(userInfo) {
      this.$router.push({
        name: 'UserProfile',
        query: {
          userInfo: userInfo
        }
      });
    }
  },

  async mounted() {
    await this.queryRoleList();
    this.queryUserList();
    this.queryAllGroup({ status: 1 });
  }
};
</script>

<style scoped>
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}
.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.abow_dialog >>> .el-dialog__header {
  background: #409eff;
}
.el-dialog .el-form {
  max-height: 480px;
  overflow: auto;
}

.pagination {
  margin-top: 3%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
#el-title .el-dialog__title {
  color: red !important;
}
.message {
  color: red;
  display: inline-block;
  font-size: 12px;
}
.abow_dialog >>> .el-form-item__content {
  line-height: 12px;
}
.abow_dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.abow_dialog >>> .el-dialog {
  margin: 0 auto !important;
  height: 85%;
  overflow: hidden;
}

.abow_dialog >>> .el-dialog__body {
  position: absolute;
  left: 10px;
  top: 80px;
  bottom: 60px;
  right: 0;
  padding: 0;
  z-index: 1;
  overflow: hidden;
  overflow-y: auto;
}

.abow_dialog >>> .el-dialog__footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
}
.dialogMantis >>> .el-input__inner {
  color: #000 !important;
}
.link {
  text-decoration: none;
  color: #2196f3 !important;
}
.red {
  text-decoration: none;
  color: red !important;
}
.tooltip {
  width: 100% !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tooltip >>> .el-link--inner {
  width: 100% !important;
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.el-tag >>> .el-icon-close {
  display: none;
}
</style>
