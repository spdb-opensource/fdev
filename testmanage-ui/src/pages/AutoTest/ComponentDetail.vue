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
      title="组件名称"
    />
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form
        :inline="true"
        :model="componentDetailModel"
        label-position="right"
        :rules="rule"
        label-width="100px"
        ref="componentDetailModel"
      >
        <el-form-item prop="moduleId" label="组件名称">
          <el-select
            filterable
            v-model="componentDetailModel.moduleId"
            clearable
          >
            <el-option
              v-for="(item, index) in moduleList.module"
              :key="index"
              :label="item.moduleName"
              :value="item.moduleId.toString()"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="elementStepNo" label="步骤序号">
          <el-input v-model="componentDetailModel.elementStepNo" />
        </el-form-item>
        <el-form-item prop="elementId" label="元素名称">
          <el-select
            filterable
            v-model="componentDetailModel.elementId"
            clearable
          >
            <el-option
              v-for="(item, index) in elementList.element"
              :key="index"
              :label="item.elementName"
              :value="item.elementId.toString()"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="elementType" label="元素操作">
          <el-select
            filterable
            v-model="componentDetailModel.elementType"
            clearable
          >
            <el-option
              v-for="(item, index) in elementDicList.elementDic"
              :key="index"
              :label="item.elementMethod"
              :value="item.elementMethod"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="elementData" label="数据">
          <el-select
            filterable
            v-model="componentDetailModel.elementData"
            clearable
          >
            <el-option
              v-for="(item, key, index) in dataList"
              :key="index"
              :label="item"
              :value="key"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="exeTimes" label="操作数">
          <el-input v-model="componentDetailModel.exeTimes" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="组件数据详情"
      :visible.sync="showDataDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <description align="center" :span="9" label="组件名称" class="q-mt-md">{{
        componentDetailModel.moduleName
      }}</description>
      <description align="center" :span="9" label="步骤序号" class="q-mt-md">{{
        componentDetailModel.elementStepNo
      }}</description>
      <description align="center" :span="9" label="元素名称" class="q-mt-md">{{
        componentDetailModel.elementName
      }}</description>
      <description align="center" :span="9" label="元素操作" class="q-mt-md">{{
        componentDetailModel.elementType
      }}</description>
      <description align="center" :span="9" label="数据" class="q-mt-md">{{
        componentDetailModel.elementData
      }}</description>
      <description align="center" :span="9" label="操作数" class="q-mt-md">{{
        componentDetailModel.exeTimes
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
      componentDetailModel: {
        elementStepNo: '',
        elementId: '',
        elementType: '',
        elementData: '',
        exeTimes: '',
        moduleId: ''
      },
      rule: {},
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      json: [
        { prop: 'elementStepNo', label: '步骤' },
        { prop: 'elementName', label: '元素' },
        { prop: 'elementType', label: '元素操作' }
      ],
      showDataDetail: false,
      dataList: {}
    };
  },
  computed: {
    ...mapState('autoTestForm', [
      'moduleDetailList',
      'moduleList',
      'elementList',
      'elementDicList',
      'moduleDataList'
    ])
  },
  watch: {},
  props: ['searchData'],
  methods: {
    ...mapActions('autoTestForm', [
      'addModuleDetail',
      'deleteModuleDetail',
      'updateModuleDetail',
      'queryElement',
      'queryModule',
      'queryElementDic',
      'queryModuleDetailByModuleId',
      'queryDataByModuleId'
    ]),
    showAddDialog() {
      this.componentDetailModel = {
        elementStepNo: '',
        elementId: '',
        elementType: '',
        elementData: '',
        exeTimes: '',
        moduleId: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '组件数据新增';
    },
    showEditDialog(data) {
      this.componentDetailModel = {
        elementStepNo: data.row.elementStepNo,
        elementId: data.row.elementId,
        elementType: data.row.elementType,
        elementData: data.row.elementData,
        exeTimes: data.row.exeTimes,
        moduleId: data.row.moduleId,
        moduleDetailId: data.row.moduleDetailId
      };
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '组件数据编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showDataDetail = false;
    },
    async confirm() {
      await validate(this.$refs['componentDetailModel']);
      if (this.isEditModel) {
        await this.updateModuleDetail(this.componentDetailModel);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addModuleDetail(this.componentDetailModel);
        this.$message({
          type: 'success',
          message: '新增成功!'
        });
      }
      await this.query();
      this.showDialog = false;
    },
    async query(searchValue) {
      await this.queryModuleDetailByModuleId({
        moduleId: this.searchData,
        valid: '0'
      });
      this.tableInfo = this.moduleDetailList.module || [];
    },
    async showItem(row) {
      this.componentDetailModel = row;
      this.showDataDetail = true;
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.moduleDetailId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该组件数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteModuleDetail({
        moduleDetailId: [row.moduleDetailId.toString()]
      });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除组件数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteModuleDetail({ moduleDetailId: this.deleteIds });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    }
  },
  async mounted() {
    let param = {
      search: '',
      valid: '0'
    };
    if (this.searchData) {
      await this.queryModuleDetailByModuleId({
        moduleId: this.searchData,
        valid: '0'
      });
      await this.queryDataByModuleId({
        moduleId: this.searchData
      });
      this.moduleDataList.data.map(item => {
        Object.assign(this.dataList, item);
      });
    }
    await this.queryModule(param);
    await this.queryElement(param);
    await this.queryElementDic(param);
    this.tableInfo = this.moduleDetailList.module || [];
  }
};
</script>
<style scoped></style>
