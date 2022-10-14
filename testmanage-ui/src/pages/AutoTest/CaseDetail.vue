<template>
  <div class="page-container">
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
      isChild
      title="案例名称"
    />
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
        <el-form-item prop="testcaseNo" label="案例名称">
          <el-select filterable clearable v-model="caseInfoModel.testcaseNo">
            <el-option
              v-for="(item, index) in caseList.case"
              :key="index"
              :label="item.testcaseName"
              :value="item.testcaseNo.toString()"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="stepNo" label="步骤序号">
          <el-input v-model="caseInfoModel.stepNo" />
        </el-form-item>
        <el-form-item prop="moduleId" label="组件">
          <el-select filterable clearable v-model="caseInfoModel.moduleId">
            <el-option
              v-for="(item, index) in moduleList.module"
              :key="index"
              :label="item.moduleNameCn"
              :value="item.moduleId.toString()"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addModuleDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="elementId" label="元素名称">
          <el-select filterable clearable v-model="caseInfoModel.elementId">
            <el-option
              v-for="(item, index) in elementList.element"
              :key="index"
              :label="item.elementName"
              :value="item.elementId.toString()"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addEleDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="elementType" label="元素操作">
          <el-select filterable clearable v-model="caseInfoModel.elementType">
            <el-option
              v-for="(item, index) in elementDicList.elementDic"
              :key="index"
              :label="item.elementMethod"
              :value="item.elementMethod"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addDicDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="elementData" label="数据">
          <el-select filterable clearable v-model="caseInfoModel.elementData">
            <el-option
              v-for="(item, key, index) in dataDetailList"
              :key="index"
              :label="item"
              :value="key"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addDataDialog">新增</el-button>
        </el-form-item>
        <el-form-item prop="assertId" label="断言">
          <el-select filterable clearable v-model="caseInfoModel.assertId">
            <el-option
              v-for="(item, key, index) in assertDetailList"
              :key="index"
              :label="item"
              :value="key"
            >
            </el-option>
          </el-select>
          <el-button type="text" @click="addAssertDialog">新增</el-button>
        </el-form-item>
        <el-form-item label="操作数">
          <el-input v-model="caseInfoModel.exeTimes" />
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
      <description align="center" :span="9" label="步骤序号" class="q-mt-md">{{
        caseDetail.stepNo
      }}</description>
      <description align="center" :span="9" label="组件" class="q-mt-md">{{
        caseDetail.moduleNameCn
      }}</description>
      <description align="center" :span="9" label="元素名称" class="q-mt-md">{{
        caseDetail.elementName
      }}</description>
      <description align="center" :span="9" label="元素操作" class="q-mt-md">{{
        caseDetail.elementType
      }}</description>
      <description align="center" :span="9" label="数据" class="q-mt-md">{{
        caseDetail.elementData
      }}</description>
      <description align="center" :span="9" label="断言" class="q-mt-md">{{
        caseDetail.assertId
      }}</description>
      <description align="center" :span="9" label="案例名称" class="q-mt-md">{{
        caseDetail.testcaseName
      }}</description>
      <description align="center" :span="9" label="操作数" class="q-mt-md">{{
        caseDetail.exeTimes
      }}</description>
    </Dialog>
    <Dialog
      title="组件新增"
      :visible.sync="showModuleDialog"
      width="60%"
      :before-close="closeModuleDialog"
    >
      <el-form
        :inline="true"
        :model="componentInfoModel"
        label-position="right"
        ref="componentInfoModel"
      >
        <el-form-item label="组件小组">
          <el-input v-model="componentInfoModel.moduleGroup" />
        </el-form-item>
        <el-form-item label="组件编号">
          <el-input v-model="componentInfoModel.moduleNo" />
        </el-form-item>
        <el-form-item label="组件名">
          <el-input v-model="componentInfoModel.moduleName" />
        </el-form-item>
        <el-form-item label="组件中文名">
          <el-input v-model="componentInfoModel.moduleNameCn" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirmModule">确定</el-button>
        <el-button @click="closeModuleDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="元素新增"
      :visible.sync="showEleDialog"
      width="60%"
      :before-close="closeEleDialog"
    >
      <el-form
        :inline="true"
        :model="elementEleInfoModel"
        label-position="right"
        :rules="ruleEle"
        label-width="100px"
        ref="elementEleInfoModel"
      >
        <el-form-item prop="elementName" label="元素名称">
          <el-input v-model="elementEleInfoModel.elementName" />
        </el-form-item>
        <el-form-item prop="elementType" label="元素类型">
          <el-select
            filterable
            clearable
            v-model="elementEleInfoModel.elementType"
          >
            <el-option
              v-for="(item, index) in elementTypes"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="elementContent" label="元素xpath">
          <el-input
            type="textarea"
            :rows="3"
            v-model="elementEleInfoModel.elementContent"
          />
        </el-form-item>
        <el-form-item prop="elementDir" label="元素目录">
          <el-input
            type="textarea"
            :rows="3"
            v-model="elementEleInfoModel.elementDir"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirmEle">确定</el-button>
        <el-button @click="closeEleDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="元素操作新增"
      :visible.sync="showDicDialog"
      width="60%"
      :before-close="closeDicDialog"
    >
      <el-form
        :inline="true"
        :model="elementDicInfoModel"
        label-position="right"
        :rules="rule"
        label-width="100px"
        ref="elementDicInfoModel"
      >
        <el-form-item prop="elementDicMethod" label="方法名">
          <el-input v-model="elementDicInfoModel.elementDicMethod" />
        </el-form-item>
        <el-form-item prop="elementDicParam" label="参数">
          <el-input v-model="elementDicInfoModel.elementDicParam" />
        </el-form-item>
        <el-form-item prop="methodDesc" label="简介">
          <el-input
            type="textarea"
            :rows="3"
            v-model="elementDicInfoModel.methodDesc"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirmDic">确定</el-button>
        <el-button @click="closeDicDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="案例数据新增"
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
            <el-button type="primary" @click="addCase">
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
import { mapState, mapActions } from 'vuex';
export default {
  name: 'MenuInfo',
  components: { MainList },
  data() {
    return {
      test: '',
      caseInfoModel: {
        testcaseNo: '',
        stepNo: '',
        moduleId: '',
        elementId: '',
        elementType: '',
        elementData: '',
        assertId: '',
        exeTimes: ''
      },
      dataInfoModel: {
        label: '',
        caseList: {}
      },
      currentData: {},
      initValue: {},
      initAssertValue: {},
      ruleData: {
        label: [{ required: true, message: '请填写数据标签', trigger: 'blur' }]
      },
      rule: {
        testcaseNo: [
          { required: true, message: '请填写案例名称', trigger: 'blur' }
        ]
        // stepNo: [
        //   { required: true, message: '请填写步骤序号', trigger: 'blur' }
        // ],
        // moduleId: [
        //   { required: true, message: '请填写组件名', trigger: 'blur' }
        // ],
        // elementId: [
        //   { required: true, message: '请填写元素名称', trigger: 'blur' }
        // ],
        // elementType: [
        //   { required: true, message: '请填写元素操作', trigger: 'blur' }
        // ],
        // elementData: [
        //   { required: true, message: '请填写数据', trigger: 'blur' }
        // ],
        // assertId: [{ required: true, message: '请填写断言', trigger: 'blur' }],
        // exeTimes: [{ required: true, message: '请填写操作数', trigger: 'blur' }]
      },
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      json: [
        { prop: 'stepNo', label: '步骤' },
        { prop: 'elementName', label: '元素' },
        { prop: 'elementType', label: '元素操作' },
        { prop: 'moduleNameCn', label: '组件名' }
      ],
      showCaseDetail: false,
      caseDetail: {},
      assertDetailList: {},
      dataDetailList: {},
      showModuleDialog: false,
      showEleDialog: false,
      showDicDialog: false,
      componentInfoModel: {
        moduleNo: '',
        moduleGroup: '',
        moduleName: '',
        moduleNameCn: '',
        module: ''
      },
      param: {
        search: '',
        valid: '0',
        flag: ''
      },
      elementEleInfoModel: {
        elementName: '',
        elementType: 'xpath',
        elementDir: '',
        elementContent: ''
      },
      ruleEle: {
        elementName: [
          { required: true, message: '请填写元素名称', trigger: 'blur' }
        ],
        elementType: [
          { required: true, message: '请填写元素类型', trigger: 'blur' }
        ],
        elementDir: [
          { required: true, message: '请填写元素目录', trigger: 'blur' }
        ]
      },
      elementDicInfoModel: {
        elementDicMethod: '',
        elementDicParam: '',
        methodDesc: ''
      },
      showDataDialog: false,
      showAssertDialog: false,
      currentAssert: {},
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
      elementTypes: [
        {
          label: 'xpath',
          value: 'xpath'
        },
        {
          label: 'id',
          value: 'id'
        }
      ]
    };
  },
  computed: {
    ...mapState('autoTestForm', [
      'caseDetailList',
      'caseList',
      'moduleList',
      'elementList',
      'elementDicList',
      'caseDataList',
      'caseAssertList',
      'dataList',
      'assertList'
    ])
  },
  watch: {
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
            assertList: deepClone(this.initAssertValue)
          };
          return;
        }
        this.assertInfoModel = {
          label: val.label,
          assertList: {}
        };
        let key = '';
        this.assertInfoModel.assertList = deepClone(this.initAssertValue);
        Object.keys(val).forEach((item, index) => {
          if (item.indexOf('ASSERTDATA') !== -1) {
            key = item.toLowerCase().replace('d', 'D');
            this.$set(this.assertInfoModel.assertList, key, val[item]);
          }
        });
      }
    }
  },
  props: ['searchData'],
  methods: {
    ...mapActions('autoTestForm', [
      'addCaseDetail',
      'deleteCaseDetail',
      'updateCaseDetail',
      'queryCase',
      'queryModule',
      'queryData',
      'queryElement',
      'queryElementDic',
      'queryCaseDetailByTestCaseNo',
      'queryDataByTestCaseNo',
      'queryAssertByTestCaseNo',
      'addModule',
      'addElement',
      'addElementDic',
      'addData',
      'addAssert',
      'queryAssert'
    ]),
    addModuleDialog() {
      this.componentInfoModel = {
        moduleNo: '',
        moduleGroup: '',
        moduleName: '',
        moduleNameCn: '',
        module: ''
      };
      this.showModuleDialog = true;
    },
    closeModuleDialog() {
      this.showModuleDialog = false;
    },
    addEleDialog() {
      this.elementEleInfoModel = {
        elementName: '',
        elementType: 'xpath',
        elementDir: '',
        elementContent: ''
      };
      this.showEleDialog = true;
    },
    closeEleDialog() {
      this.showEleDialog = false;
    },
    addDicDialog() {
      this.elementDicInfoModel = {
        elementDicMethod: '',
        elementDicParam: '',
        methodDesc: ''
      };
      this.showDicDialog = true;
    },
    closeDicDialog() {
      this.showDicDialog = false;
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
      this.showDataDialog = false;
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
    async confirmDic() {
      await validate(this.$refs['elementDicInfoModel']);
      await this.addElementDic(this.elementDicInfoModel);
      await this.queryElementDic(this.param);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      this.showDicDialog = false;
    },
    async confirmEle() {
      await validate(this.$refs['elementEleInfoModel']);
      await this.addElement(this.elementEleInfoModel);
      await this.queryElement(this.param);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      this.showEleDialog = false;
    },
    async confirmData() {
      await validate(this.$refs['dataInfoModel']);
      let param = {
        label: this.dataInfoModel.label,
        ...this.dataInfoModel.caseList
      };
      await this.addData(param);
      await this.queryCaseDetailByTestCaseNo({
        testcaseNo: this.searchData,
        valid: '0'
      });
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
      await this.queryAssertByTestCaseNo({
        testcaseNo: this.searchData
      });
      this.showAssertDialog = false;
    },
    addCase() {
      let num = Object.keys(this.dataInfoModel.caseList).length;
      this.$set(this.dataInfoModel.caseList, 'case' + (num + 1), '');
    },
    reduceCase() {
      let num = Object.keys(this.dataInfoModel.caseList).length;
      this.$delete(this.dataInfoModel.caseList, 'case' + num);
    },
    showAddDialog() {
      this.caseInfoModel = {
        testcaseNo: this.searchData.toString() || '',
        stepNo: '',
        moduleId: '',
        elementId: '',
        elementType: '',
        elementData: '',
        assertId: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '案例明细新增';
    },
    showEditDialog(data) {
      let item = {
        testcaseNo: data.row.testcaseNo,
        stepNo: data.row.stepNo,
        moduleId: data.row.moduleId,
        elementId: data.row.elementId,
        elementType: data.row.elementType,
        elementData: data.row.elementData,
        assertId: data.row.assertId,
        exeTimes: data.row.exeTimes,
        detailId: data.row.detailId
      };
      this.caseInfoModel = item;
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '案例明细编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showCaseDetail = false;
      this.caseInfoModel = {
        testcaseNo: '',
        stepNo: '',
        moduleId: '',
        elementId: '',
        elementType: '',
        elementData: '',
        assertId: '',
        exeTimes: ''
      };
    },
    async confirmModule() {
      await validate(this.$refs['componentInfoModel']);
      await this.addModule(this.componentInfoModel);
      await this.queryModule(this.param);
      this.$message({
        type: 'success',
        message: '新增成功!'
      });
      this.showModuleDialog = false;
    },
    async confirm() {
      await validate(this.$refs['caseInfoModel']);
      let param = {
        testcaseNo: this.caseInfoModel.testcaseNo.toString(),
        stepNo: this.caseInfoModel.stepNo.toString(),
        moduleId: this.caseInfoModel.moduleId.toString(),
        elementType: this.caseInfoModel.elementType.toString(),
        assertId: this.caseInfoModel.assertId.toString(),
        elementId: this.caseInfoModel.elementId.toString(),
        elementData: this.caseInfoModel.elementData,
        exeTimes: this.caseInfoModel.exeTimes
      };
      if (this.isEditModel) {
        param.detailId = this.caseInfoModel.detailId.toString();
        await this.updateCaseDetail(param);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addCaseDetail(param);
        this.$message({
          type: 'success',
          message: '新增成功!'
        });
      }
      await this.query();
      this.showDialog = false;
    },
    async query() {
      await this.queryCaseDetailByTestCaseNo({
        testcaseNo: this.searchData,
        valid: '0'
      });
      this.tableInfo = this.caseDetailList.caseDetail || [];
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.detailId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该案例明细?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteCaseDetail({ detailId: [row.detailId.toString()] });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除案例明细?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteCaseDetail({ detailId: this.deleteIds });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    showItem(row) {
      this.caseDetail = row;
      this.showCaseDetail = true;
    }
  },
  created() {
    for (let i = 0; i < 8; i++) {
      this.$set(this.dataInfoModel.caseList, 'case' + (i + 1), '');
    }
    this.initValue = deepClone(this.dataInfoModel.caseList);
    for (let i = 0; i < 10; i++) {
      this.$set(this.assertInfoModel.assertList, 'assertData' + (i + 1), '');
    }
    this.initAssertValue = this.assertInfoModel.assertList;
  },
  async mounted() {
    let param = {
      search: '',
      valid: '0'
    };
    if (this.searchData) {
      await this.queryCaseDetailByTestCaseNo({
        testcaseNo: this.searchData,
        valid: '0'
      });
      await this.queryDataByTestCaseNo({
        testcaseNo: this.searchData
      });
      await this.queryAssertByTestCaseNo({
        testcaseNo: this.searchData
      });
      this.caseDataList.data.map(item => {
        Object.assign(this.dataDetailList, item);
      });
      this.caseAssertList.assert.map(item => {
        Object.assign(this.assertDetailList, item);
      });
    }

    let param1 = {
      search: '',
      valid: '0',
      flag: ''
    };
    await this.queryCase(param1);
    await this.queryModule(param);
    await this.queryData(param);
    await this.queryElement(param);
    await this.queryElementDic(param);
    await this.queryAssert(param);
    this.tableInfo = this.caseDetailList.caseDetail || [];
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
</style>
