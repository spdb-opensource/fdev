<template>
  <div class="page-container">
    <el-container>
      <el-header>
        <el-form :inline="true">
          <el-form-item>
            <el-input
              v-model="searchName"
              placeholder="请输入组名/人名"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search" class="queryBtn"
              >查询</el-button
            >
            <el-button
              type="primary"
              @click="assign"
              class="queryBtn"
              v-if="userRole >= 50"
              >分配设置</el-button
            >
          </el-form-item>
        </el-form>
      </el-header>
      <el-main style="padding:10px">
        <el-table
          stripe
          :data="assignList"
          style="width: 100%;color:black"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column prop="name" label="组名" />
          <el-table-column prop="work_leader.user_name_cn" label="工单负责人" />
          <el-table-column label="功能测试组长">
            <template slot-scope="scope">
              <div>
                {{ getUserName(scope.row.groupleader) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="安全测试组长">
            <template slot-scope="scope">
              <div>
                {{ getUserName(scope.row.security_leader) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="uatContact.user_name_cn" label="业测联系人" />
        </el-table>
      </el-main>
    </el-container>
    <el-dialog
      title="分配管理"
      :visible.sync="assignDialogShow"
      width="35%"
      :before-close="closeAssignDia"
      class="small_dialog"
    >
      <el-form :rules="assignchRules" ref="assignModel" :model="assignModel">
        <t-select
          prop="groupId"
          filterable
          :options="groupNameList"
          placeholder="请选择"
          v-model="assignModel.groupId"
          @input="groupNameChange"
          option-label="name"
          option-value="id"
          label="小组"
          label-width="30%"
          :full-width="false"
        >
          <template v-slot:options="item">
            <span>{{ item.name }}</span>
          </template>
        </t-select>
        <t-select
          prop="workManager"
          filterable
          :options="workManagerList"
          placeholder="请选择"
          v-model="assignModel.workManager"
          option-label="user_name_cn"
          option-value="user_name_en"
          label="工单负责人"
          label-width="30%"
          :full-width="false"
        >
          <template v-slot:options="item">
            <span>{{ item.user_name_cn }}</span>
          </template>
        </t-select>
        <t-select
          prop="tester"
          filterable
          :options="testerList"
          placeholder="请选择"
          v-model="assignModel.tester"
          option-label="user_name_cn"
          option-value="user_name_en"
          label="功能测试组长"
          label-width="30%"
          :full-width="false"
          multiple
        >
          <template v-slot:options="item">
            <span>{{ item.user_name_cn }}</span>
          </template>
        </t-select>
        <t-select
          prop="securityTester"
          filterable
          :options="securityList"
          placeholder="请选择"
          v-model="assignModel.securityTester"
          option-label="user_name_cn"
          option-value="user_name_en"
          label="安全测试组长"
          label-width="30%"
          :full-width="false"
          multiple
          clearable
        >
          <template v-slot:options="item">
            <span>{{ item.user_name_cn }}</span>
          </template>
        </t-select>
        <t-select
          v-if="userRole > 40"
          prop="contacts"
          filterable
          :options="contactsList"
          placeholder="请选择"
          v-model="assignModel.contacts"
          option-label="user_name_cn"
          option-value="user_name_en"
          label="业测联系人"
          label-width="30%"
          :full-width="false"
          clearable
        >
          <template v-slot:options="item">
            <span>{{ item.user_name_cn }}</span>
          </template>
        </t-select>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeAssignDia">返 回</el-button>
        <el-button type="primary" @click="updateAssign('assignModel')"
          >确 定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { getUserListByRole } from '@/common/utlis';
export default {
  name: 'AssignOrder',

  data() {
    return {
      userRole: sessionStorage.getItem('Trole'),
      searchName: '',
      groupNameList: [],
      workManagerList: [],
      testerList: [], //功能测试组长名单
      securityList: [], //安全测试组长名单
      assignDialogShow: false,
      assignModel: {
        securityTester: [],
        tester: [],
        groupId: '',
        workManager: '',
        contacts: ''
      },
      assignList: [],
      assignListStorage: [],
      assignchRules: {
        groupId: [{ required: true, message: '请选择组名', trigger: 'blur' }],
        workManager: [
          { required: true, message: '请选择工单负责人', trigger: 'blur' }
        ],
        tester: [{ required: true, message: '请选择测试组长', trigger: 'blur' }]
      },
      contactsList: [],
      tt: []
    };
  },
  computed: {
    ...mapState('autoAssignForm', [
      'assignOrderList',
      'developGroup',
      'groupList',
      'allOption',
      'userInfo'
    ])
  },
  watch: {},
  methods: {
    ...mapActions('autoAssignForm', [
      'queryAssignList',
      'queryDevelopGroup',
      'queryAllOption',
      'assignUpdate',
      'queryAllGroup',
      'queryUserCoreData'
    ]),
    //初始化
    async initAssignList() {
      await this.queryAssignList();
      this.assignList = this.assignOrderList;
      this.assignListStorage = this.assignOrderList;
    },
    //搜索
    search() {
      const searchName = this.searchName;
      let assignList = this.assignListStorage;
      if (searchName) {
        this.assignList = assignList.filter(data => {
          return Object.keys(data).some(key => {
            return (
              String(data[key])
                .toLowerCase()
                .indexOf(searchName) > -1
            );
          });
        });
      } else {
        this.assignList = this.assignListStorage;
      }
    },
    //查询所有组
    async queryGroupList() {
      await this.queryAllGroup({ status: 1 });
      this.groupNameList = this.groupList;
    },
    //打开分配弹窗
    assign() {
      this.assignModel = {
        securityTester: [],
        tester: [],
        groupId: '',
        workManager: '',
        contacts: ''
      };
      this.queryGroupList();
      this.assignDialogShow = true;
    },
    closeAssignDia() {
      this.assignDialogShow = false;
    },
    //工单负责人，测试组长
    async groupNameChange() {
      let bool = this.assignOrderList.some(group => {
        return group.groupId === this.assignModel.groupId;
      });
      let currentGroup = {};
      if (bool) {
        currentGroup = this.assignOrderList.find(group => {
          return group.groupId === this.assignModel.groupId;
        });
      } else {
        currentGroup = {
          work_leader: '',
          groupId: this.assignModel.groupId,
          groupleader: [],
          security_leader: [],
          uatContact: ''
        };
      }
      this.assignModel.workManager = currentGroup.work_leader.user_name_en;
      this.assignModel.contacts = currentGroup.uatContact.user_name_en;
      let testerArr = [];
      let securityArr = [];
      currentGroup.groupleader.forEach(group => {
        testerArr.push(group.user_name_en);
      });
      currentGroup.security_leader.forEach(group => {
        securityArr.push(group.user_name_en);
      });
      this.assignModel.tester = testerArr;
      this.assignModel.securityTester = securityArr;
      this.assignModel.groupId = currentGroup.groupId;
    },
    //分配修改新增
    updateAssign(assignModel) {
      this.$refs[assignModel].validate(async valid => {
        if (valid) {
          await this.$confirm('是否保存设置?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });
          await this.assignUpdate({
            work_leader: this.assignModel.tester,
            group_id: this.assignModel.groupId,
            work_manager: this.assignModel.workManager,
            uatContact: this.assignModel.contacts,
            security_leader: this.assignModel.securityTester
          });
          await this.initAssignList();
          this.assignDialogShow = false;
        }
      });
    },
    getUserName(arr) {
      let res = [];
      arr.forEach(val => {
        res.push(val.user_name_cn);
      });
      return res.join(',');
    }
  },
  async mounted() {
    await this.initAssignList();
    this.testerList = await getUserListByRole(['玉衡-测试组长']);
    this.securityList = await getUserListByRole(['玉衡-安全测试组长']);
    this.workManagerList = await getUserListByRole(['玉衡-测试管理员']);
    this.contactsList = await getUserListByRole();
  }
};
</script>

<style scoped>
.page-container {
  padding-top: 40px;
}
.headerCss {
  font-size: 16px;
  font-weight: 600;
}
.text {
  font-size: 14px;
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

.box-card {
  border: gainsboro solid 1px;
}
.el-card__header {
  border-bottom: 1px solid gainsboro;
}
.el-tabs__item {
  font-size: 16px !important;
  color: red;
}
.groupSpace {
  margin-top: -15px !important ;
}
.optionSpace {
  margin-top: 25px;
}
.small_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.small_dialog >>> .el-dialog__header {
  background: #409eff;
}

.small_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
</style>
