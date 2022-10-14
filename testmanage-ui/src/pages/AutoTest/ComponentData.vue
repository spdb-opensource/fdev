<template>
  <div class="page-container">
    <strong class="title">组件维护</strong>
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
      title="组件数据名称"
    >
      <template v-if="moduleId" v-slot:detail>
        <keep-alive>
          <ComponentDetail :searchData="moduleId" />
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
        :model="componentInfoModel"
        label-position="right"
        :rules="rule"
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
      <description align="center" :span="9" label="组件小组" class="q-mt-md">{{
        componentInfoModel.moduleGroup
      }}</description>
      <description align="center" :span="9" label="组件编号" class="q-mt-md">{{
        componentInfoModel.moduleNo
      }}</description>
      <description align="center" :span="9" label="组件名" class="q-mt-md">{{
        componentInfoModel.moduleName
      }}</description>
      <description
        align="center"
        :span="9"
        label="组件中文名"
        class="q-mt-md"
        >{{ componentInfoModel.moduleNameCn }}</description
      >
    </Dialog>
  </div>
</template>
<script>
import { validate } from '@/common/utlis';
import MainList from './Components/MainList';
import ComponentDetail from './ComponentDetail';
import { mapState, mapActions } from 'vuex';
export default {
  name: 'MenuInfo',
  components: { MainList, ComponentDetail },
  data() {
    return {
      componentInfoModel: {
        moduleNo: '',
        moduleGroup: '',
        moduleName: '',
        moduleNameCn: '',
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
        { prop: 'moduleGroup', label: '组件小组', sortable: true },
        { prop: 'moduleNo', label: '组件编号' },
        { prop: 'moduleName', label: '组件名' },
        { prop: 'moduleNameCn', label: '组件中文名' }
      ],
      showDataDetail: false,
      moduleId: ''
    };
  },
  computed: {
    ...mapState('autoTestForm', ['moduleList'])
  },
  watch: {},
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'queryModule',
      'addModule',
      'deleteModule',
      'updateModule'
    ]),
    getDetail(row) {
      this.moduleId = row.moduleId;
    },
    showAddDialog() {
      this.componentInfoModel = {
        moduleNo: '',
        moduleGroup: '',
        moduleName: '',
        moduleNameCn: '',
        moduleId: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '组件数据新增';
    },
    showEditDialog(data) {
      this.componentInfoModel = {
        moduleNo: data.row.moduleNo,
        moduleGroup: data.row.moduleGroup,
        moduleName: data.row.moduleName,
        moduleNameCn: data.row.moduleNameCn,
        moduleId: data.row.moduleId
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
      await validate(this.$refs['componentInfoModel']);
      if (this.isEditModel) {
        await this.updateModule(this.componentInfoModel);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addModule(this.componentInfoModel);
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
        search: searchValue || '',
        valid: '0'
      };
      await this.queryModule(param);
      this.tableInfo = this.moduleList.module || [];
    },
    async showItem(row) {
      this.componentInfoModel = row;
      this.showDataDetail = true;
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.moduleId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该组件数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteModule({ moduleId: [row.moduleId.toString()] });
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
      await this.deleteModule({ moduleId: this.deleteIds });
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
    await this.queryModule(param);
    this.tableInfo = this.moduleList.module || [];
  }
};
</script>
<style scoped>
.title {
  font-size: 20px;
  text-align: left;
  margin-bottom: 16px;
  display: inline-block;
}
</style>
