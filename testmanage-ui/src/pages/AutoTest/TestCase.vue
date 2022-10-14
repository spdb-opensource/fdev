<template>
  <div class="page-container">
    <strong class="title">测试案例维护</strong>
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
      @getDetail="getDetail"
      showExpand
      title="案例名称"
    >
      <!-- <template>
        <el-button
          type="primary"
          @click="runCase"
          :disabled="selection.length === 0"
          >执行案例</el-button
        >
      </template> -->
      <template v-if="testCaseNo" v-slot:detail>
        <keep-alive>
          <CaseDetail :searchData="testCaseNo" />
        </keep-alive>
      </template>
    </MainList>
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form
        :inline="true"
        :model="caseInfoModel"
        label-position="right"
        :rules="rule"
        label-width="100px"
        ref="caseInfoModel"
      >
        <el-form-item v-if="!isEditModel" label="案例名称">
          <el-select
            filterable
            clearable
            v-model="currentCase"
            :label="currentCase.testcaseName"
            value-key="testcaseNo"
          >
            <el-option
              v-for="(item, index) in tableInfo"
              :key="index"
              :label="item.testcaseName"
              :value="item"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEditModel" label="复用明细">
          <el-checkbox v-model="isMulti"></el-checkbox>
        </el-form-item>
        <el-divider v-if="!isEditModel"></el-divider>
        <el-form-item prop="testcaseName" label="案例名称">
          <el-input v-model="caseInfoModel.testcaseName" />
        </el-form-item>
        <el-form-item prop="funcPoint" label="功能点">
          <el-input v-model="caseInfoModel.funcPoint" />
        </el-form-item>
        <el-form-item prop="precondition" label="前置条件">
          <el-input v-model="caseInfoModel.precondition" />
        </el-form-item>
        <el-form-item prop="expectedResult" label="预期结果">
          <el-input v-model="caseInfoModel.expectedResult" />
        </el-form-item>
        <el-form-item prop="testcaseDesc" label="案例描述">
          <el-input
            type="textarea"
            :rows="3"
            v-model="caseInfoModel.testcaseDesc"
          />
        </el-form-item>
        <el-form-item prop="menuSheetId" label="菜单项">
          <el-select filterable v-model="caseInfoModel.menuSheetId" clearable>
            <el-option
              v-for="(item, index) in menuData.menu"
              :key="index"
              :label="getMenuLabel(item)"
              :value="item.menuSheetId.toString()"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addMenuDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="userId" label="测试用户">
          <el-select filterable v-model="caseInfoModel.userId" clearable>
            <el-option
              v-for="(item, index) in userList.user"
              :key="index"
              :label="item.userName"
              :value="item.userId.toString()"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addUserDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="dataId" label="案例数据">
          <el-select filterable v-model="caseInfoModel.dataId" clearable>
            <el-option
              v-for="(item, index) in dataList.data"
              :key="index"
              :label="item.label"
              :value="item.dataId"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addDataDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="assertId" label="断言">
          <el-select filterable v-model="caseInfoModel.assertId" clearable>
            <el-option
              v-for="(item, index) in assertList.assert"
              :key="index"
              :label="item.label"
              :value="item.assertId"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addAssertDialog">新增</el-button>
        </el-form-item>
        <el-form-item label="有效">
          <el-checkbox
            @change="checkChange"
            :checked="caseInfoModel.validFlag"
          ></el-checkbox>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="案例详情"
      :visible.sync="showCaseDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <description align="center" :span="9" label="案例名称" class="q-mt-md">{{
        caseDetail.testcaseName
      }}</description>
      <description align="center" :span="9" label="功能点" class="q-mt-md">{{
        caseDetail.funcPoint
      }}</description>
      <description align="center" :span="9" label="前置条件" class="q-mt-md">{{
        caseDetail.precondition
      }}</description>
      <description align="center" :span="9" label="预期结果" class="q-mt-md">{{
        caseDetail.expectedResult
      }}</description>
      <description align="center" :span="9" label="案例描述" class="q-mt-md">{{
        caseDetail.testcaseDesc
      }}</description>
      <description align="center" :span="9" label="菜单项" class="q-mt-md">{{
        caseDetail.menuNo
      }}</description>
      <description align="center" :span="9" label="测试用户" class="q-mt-md">{{
        caseDetail.userName
      }}</description>
      <description align="center" :span="9" label="案例数据" class="q-mt-md">{{
        caseDetail.dataLabel
      }}</description>
      <description align="center" :span="9" label="断言" class="q-mt-md">{{
        caseDetail.assertLabel
      }}</description>
      <description align="center" :span="9" label="有效" class="q-mt-md">{{
        caseDetail.validFlag == 0 ? '是' : '否'
      }}</description>
    </Dialog>
    <Dialog
      title="菜单新增"
      :visible.sync="showMenuDialog"
      width="60%"
      :before-close="closeMenuDialog"
    >
      <el-form
        :inline="true"
        :model="menuInfoModel"
        label-position="right"
        :rules="ruleMenu"
        label-width="110px"
        ref="menuInfoModel"
      >
        <el-form-item prop="menuNo" label="一级菜单编号">
          <el-input v-model="menuInfoModel.menuNo" />
        </el-form-item>
        <el-form-item prop="menuName" label="一级菜单名称">
          <el-input v-model="menuInfoModel.menuName" />
        </el-form-item>
        <el-form-item prop="secondaryMenuNo" label="二级菜单编号">
          <el-input v-model="menuInfoModel.secondaryMenuNo" />
        </el-form-item>
        <el-form-item prop="secondaryMenu" label="二级菜单名称">
          <el-input v-model="menuInfoModel.secondaryMenu" />
        </el-form-item>
        <el-form-item prop="thirdMenuNo" label="三级菜单编号">
          <el-input v-model="menuInfoModel.thirdMenuNo" />
        </el-form-item>
        <el-form-item prop="thirdMenu" label="三级菜单名称">
          <el-input v-model="menuInfoModel.thirdMenu" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirmMenu">确定</el-button>
        <el-button @click="closeMenuDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="用户新增"
      :visible.sync="showUserDialog"
      width="60%"
      :before-close="closeUserDialog"
    >
      <el-form
        :inline="true"
        :model="userInfoModel"
        label-position="right"
        :rules="userRule"
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
        <el-button @click="confirmUser">确定</el-button>
        <el-button @click="closeUserDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="数据新增"
      :visible.sync="showDataDialog"
      width="60%"
      :before-close="closeDataDialog"
    >
      <el-form
        :inline="true"
        :model="dataInfoModel"
        label-position="right"
        :rules="ruleData"
        ref="dataInfoModel"
      >
        <el-form-item label="数据标签" style="display:block;">
          <el-select
            filterable
            clearable
            v-model="currentData"
            value-key="label"
          >
            <el-option
              v-for="(item, index) in dataList.data"
              :key="index"
              :label="item.label"
              :value="item"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-divider></el-divider>
        <el-row style="display:flex">
          <el-form-item prop="label" label="数据标签" style="display:block">
            <el-input v-model="dataInfoModel.label" />
          </el-form-item>
          <el-button-group>
            <el-button type="primary" @click="addCaseItem">
              新增CASE
            </el-button>
            <el-button type="primary" @click="reduceCase">
              删除CASE
            </el-button>
          </el-button-group>
        </el-row>

        <el-form-item
          v-for="(item, key, index) in dataInfoModel.caseList"
          :key="index"
          :label="key"
        >
          <el-input v-model="dataInfoModel.caseList[key]" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirmData">确定</el-button>
        <el-button @click="closeDataDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="断言新增"
      :visible.sync="showAssertDialog"
      width="60%"
      :before-close="closeAssertDialog"
    >
      <el-form
        :inline="true"
        :model="assertInfoModel"
        label-position="right"
        :rules="ruleAssert"
        ref="assertInfoModel"
      >
        <el-form-item label="断言标签" style="display:block;">
          <el-select
            filterable
            clearable
            v-model="currentAssert"
            value-key="assertId"
          >
            <el-option
              v-for="(item, index) in assertList.assert"
              :key="index"
              :label="item.label"
              :value="item"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-divider></el-divider>
        <el-form-item prop="label" label="断言标签" style="display:block">
          <el-input v-model="assertInfoModel.label" />
        </el-form-item>

        <el-form-item
          :prop="assertList[key]"
          v-for="(item, key, index) in assertInfoModel.assertList"
          :key="index"
          :label="key"
        >
          <el-input v-model="assertInfoModel.assertList[key]" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirmAssert">确定</el-button>
        <el-button @click="closeAssertDialog">取消</el-button>
      </div>
    </Dialog>
  </div>
</template>
<script>
import { validate, deepClone } from '@/common/utlis';
import MainList from './Components/MainList';
import CaseDetail from './CaseDetail';
import { mapState, mapActions } from 'vuex';
export default {
  name: 'MenuInfo',
  components: { MainList, CaseDetail },
  data() {
    return {
      caseInfoModel: {
        dataId: '',
        userId: '',
        funcPoint: '',
        testcaseName: '',
        precondition: '',
        testcaseDesc: '',
        expectedResult: '',
        validFlag: true,
        menuSheetId: '',
        assertId: ''
      },
      menuInfoModel: {
        menuNo: '',
        menuName: '',
        secondaryMenuNo: '',
        secondaryMenu: '',
        thirdMenuNo: '',
        thirdMenu: ''
      },
      param: {
        search: '',
        valid: '0',
        flag: ''
      },
      isMulti: true,
      rule: {},
      showDialog: false,
      showCaseDetail: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      currentCase: {},
      selection: [],
      caseDetail: {},
      checked: true,
      json: [
        { prop: 'testcaseNo', label: '案例编号', sortable: true },
        { prop: 'testcaseName', label: '案例名称', sortable: true },
        { prop: 'testcaseDesc', label: '案例描述' },
        { prop: 'funcPoint', label: '功能点' },
        { prop: 'precondition', label: '前置条件' },
        { prop: 'expectedResult', label: '预期结果' },
        { prop: 'lastOpr', label: '操作人', sortable: true }
      ],
      checkedArr: ['yes'],
      testCaseNo: '',
      menuModel: false,
      showMenuDialog: false,
      ruleMenu: {
        menuNo: [
          { required: true, message: '请填写一级菜单编号', trigger: 'blur' }
        ],
        menuName: [
          { required: true, message: '请填写一级菜单名称', trigger: 'blur' }
        ]
      },
      ruleData: {
        label: [{ required: true, message: '请填写数据标签', trigger: 'blur' }]
      },
      showUserDialog: false,
      showDataDialog: false,
      userInfoModel: {
        userName: '',
        queryPwd: ''
      },
      userRule: {
        userName: [
          { required: true, message: '请填写用户名', trigger: 'blur' }
        ],
        queryPwd: [{ required: true, message: '请填写密码', trigger: 'blur' }]
      },
      dataInfoModel: {
        label: '',
        caseList: {}
      },
      initValue: {},
      currentData: {},
      showAssertDialog: false,
      assertInfoModel: {
        label: '',
        assertId: '',
        assertList: {}
      },
      ruleAssert: {
        label: [{ required: true, message: '请填写断言标签', trigger: 'blur' }],
        assertList: [
          { required: true, message: '请填写断言数据', trigger: 'blur' }
        ]
      },
      currentAssert: {
        label: '',
        assertId: '',
        assertList: {}
      },
      initValueAssert: {}
    };
  },
  computed: {
    ...mapState('autoTestForm', [
      'caseList',
      'folder',
      'menuData',
      'userList',
      'dataList',
      'assertList'
    ])
  },
  watch: {
    currentCase: {
      deep: true,
      handler(val) {
        this.caseInfoModel = deepClone(val);
        let item = val.validFlag == '0' ? true : false;
        this.$set(this.caseInfoModel, 'validFlag', item);
      }
    },
    currentData: {
      deep: true,
      handler(val) {
        let key = '';
        Object.keys(val).forEach((item, index) => {
          if (item.indexOf('CASE') !== -1) {
            key = item.toLowerCase();
            this.$set(this.dataInfoModel.caseList, key, val[item]);
          }
        });
        this.dataInfoModel.label = val.label;
      }
    },
    currentAssert: {
      deep: true,
      handler(val) {
        if (!val) {
          this.assertInfoModel = {
            label: '',
            assertList: deepClone(this.initValueAssert)
          };
          return;
        }
        this.assertInfoModel = {
          label: val.label,
          assertList: {}
        };
        let key = '';
        this.assertInfoModel.assertList = deepClone(this.initValueAssert);
        Object.keys(val).forEach((item, index) => {
          if (item.indexOf('ASSERTDATA') !== -1) {
            key = item.toLowerCase().replace('d', 'D');
            this.$set(this.assertInfoModel.assertList, key, val[item]);
          }
        });
      }
    }
  },
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'queryCase',
      'addCase',
      'deleteCase',
      'updateCase',
      'genFile',
      'excuteCase',
      'queryMenu',
      'queryUser',
      'queryData',
      'queryAssert',
      'addMenu',
      'addUser',
      'addData',
      'addAssert'
    ]),
    getMenuLabel(item) {
      let { menuName, secondaryMenu, thirdMenu } = item;
      let res = menuName;
      if (secondaryMenu) {
        res = menuName + ' - ' + secondaryMenu;
      }
      if (thirdMenu) {
        res = res + ' - ' + thirdMenu;
      }
      return res;
    },
    async confirmMenu() {
      await validate(this.$refs['menuInfoModel']);
      await this.addMenu(this.menuInfoModel);
      await this.queryMenu(this.param);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      this.showMenuDialog = false;
    },
    async confirmUser() {
      await validate(this.$refs['userInfoModel']);
      await this.addUser(this.userInfoModel);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      await this.queryUser(this.param);
      this.showUserDialog = false;
    },
    async confirmData() {
      await validate(this.$refs['dataInfoModel']);
      let param = {
        label: this.dataInfoModel.label,
        ...this.dataInfoModel.caseList
      };
      await this.addData(param);
      await this.queryData(this.param);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      this.showDataDialog = false;
    },
    async confirmAssert() {
      await validate(this.$refs['assertInfoModel']);
      let param = {
        label: this.assertInfoModel.label,
        ...this.assertInfoModel.assertList
      };
      await this.addAssert(param);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      await this.queryAssert(this.param);
      this.showAssertDialog = false;
    },
    addAssertDialog() {
      let item = {};
      for (let i = 0; i < 10; i++) {
        item['assertData' + (i + 1)] = '';
      }
      this.assertInfoModel = {
        label: '',
        assertId: '',
        assertList: item
      };
      this.showAssertDialog = true;
    },
    closeAssertDialog() {
      this.showAssertDialog = false;
    },
    addMenuDialog() {
      this.menuInfoModel = {
        menuNo: '',
        menuName: '',
        secondaryMenuNo: '',
        secondaryMenu: '',
        thirdMenuNo: '',
        thirdMenu: ''
      };
      this.showMenuDialog = true;
    },
    closeMenuDialog() {
      this.showMenuDialog = false;
    },
    addUserDialog() {
      this.userInfoModel = {
        userName: '',
        queryPwd: ''
      };
      this.showUserDialog = true;
    },
    closeUserDialog() {
      this.showUserDialog = false;
    },
    addDataDialog() {
      let item = {};
      for (let i = 0; i < 8; i++) {
        item['case' + (i + 1)] = '';
      }
      this.dataInfoModel = {
        label: '',
        caseList: item
      };
      this.showDataDialog = true;
    },
    closeDataDialog() {
      this.dataInfoModel = {
        label: '',
        caseList: deepClone(this.initValue)
      };
      this.showDataDialog = false;
    },
    addCaseItem() {
      let num = Object.keys(this.dataInfoModel.caseList).length;
      this.$set(this.dataInfoModel.caseList, 'case' + (num + 1), '');
    },
    reduceCase() {
      let num = Object.keys(this.dataInfoModel.caseList).length;
      this.$delete(this.dataInfoModel.caseList, 'case' + num);
    },
    getDetail(row) {
      this.testCaseNo = row.testcaseNo;
    },
    checkChange(val) {
      this.checked = val;
    },
    showAddDialog() {
      this.caseInfoModel = {
        dataId: '',
        userId: '',
        funcPoint: '',
        testcaseName: '',
        precondition: '',
        testcaseDesc: '',
        expectedResult: '',
        validFlag: '0',
        menuSheetId: '',
        assertId: ''
      };
      this.currentCase = { validFlag: '0' };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '案例新增';
    },
    showEditDialog(data) {
      this.caseInfoModel = {
        dataId: data.row.dataId.toString(),
        userId: data.row.userId.toString(),
        funcPoint: data.row.funcPoint,
        testcaseName: data.row.testcaseName,
        precondition: data.row.precondition,
        testcaseDesc: data.row.testcaseDesc,
        expectedResult: data.row.expectedResult,
        menuSheetId: data.row.menuSheetId,
        assertId: data.row.assertId,
        testcaseNo: data.row.testcaseNo,
        validFlag: data.row.validFlag == '0' ? true : false
      };
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '案例编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showCaseDetail = false;
    },
    async confirm() {
      await validate(this.$refs['caseInfoModel']);
      let param = {
        dataId: this.caseInfoModel.dataId,
        userId: this.caseInfoModel.userId,
        funcPoint: this.caseInfoModel.funcPoint,
        testcaseName: this.caseInfoModel.testcaseName,
        precondition: this.caseInfoModel.precondition,
        testcaseDesc: this.caseInfoModel.testcaseDesc,
        expectedResult: this.caseInfoModel.expectedResult,
        validFlag: this.caseInfoModel.validFlag ? '0' : '1',
        menuSheetId: this.caseInfoModel.menuSheetId,
        assertId: this.caseInfoModel.assertId
      };
      if (this.isEditModel) {
        param.testcaseNo = this.caseInfoModel.testcaseNo
          ? this.caseInfoModel.testcaseNo.toString()
          : '';
        param.validFlag = this.checked ? '0' : '1';
        await this.updateCase(param);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        if (this.isMulti) {
          param = {
            ...param,
            isMulti: '1',
            testcaseNo: this.caseInfoModel.testcaseNo
              ? this.caseInfoModel.testcaseNo.toString()
              : ''
          };
        } else {
          param = {
            ...param,
            isMulti: '0'
          };
        }
        await this.addCase(param);
        this.$message({
          type: 'success',
          message: '新增成功!'
        });
      }
      await this.query(this.searchValue);
      this.showDialog = false;
    },
    async query(searchValue) {
      this.searchValue = searchValue;
      let param = {
        search: searchValue || '',
        valid: '0',
        flag: ''
      };
      await this.queryCase(param);
      this.tableInfo = this.caseList.case || [];
    },
    async showItem(row) {
      this.caseDetail = row;
      this.showCaseDetail = true;
    },
    selectChange(selection) {
      this.selection = selection;
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.testcaseNo.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该菜单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteCase({ testcaseNo: [row.testcaseNo.toString()] });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除菜单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteCase({ testcaseNo: this.deleteIds });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async runCase() {
      let param = {
        caseList: []
      };
      this.selection.forEach((item, index) => {
        param.caseList.push({
          menuNo: item.menuNo,
          testCaseNo: item.testcaseNo
        });
      });
      await this.genFile(param);
      let itemArr = [];
      param.caseList.forEach((val, index) => {
        if (itemArr.indexOf(val.menuNo) == -1) {
          itemArr.push(val.menuNo);
        }
      });
      await this.excuteCase({
        folder: this.folder.folder,
        menuNo: itemArr.toString()
      });
      this.$message({
        type: 'success',
        message: '案例案例成功!'
      });
    }
  },
  created() {
    for (let i = 0; i < 8; i++) {
      this.$set(this.dataInfoModel.caseList, 'case' + (i + 1), '');
    }
    for (let i = 0; i < 10; i++) {
      this.$set(this.assertInfoModel.assertList, 'assertData' + (i + 1), '');
    }
    this.initValueAssert = deepClone(this.assertInfoModel.assertList);
    this.initValue = deepClone(this.dataInfoModel.caseList);
  },
  async mounted() {
    let param = {
      search: '',
      valid: '0',
      flag: ''
    };
    await this.queryCase(param);
    await this.queryMenu(param);
    await this.queryUser(param);
    await this.queryData(param);
    await this.queryAssert(param);
    this.tableInfo = this.caseList.case || [];
  }
};
</script>
<style scoped>
.el-form-item__error {
  min-width: 100px !important;
}
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
