<template>
  <div class="page-container">
    <strong class="title">元素字典维护</strong>
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
      title="方法名"
    />
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
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
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="元素字典详情"
      :visible.sync="showDataDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <description align="center" :span="9" label="方法名" class="q-mt-md">{{
        elementDicInfoModel.elementDicMethod
      }}</description>
      <description align="center" :span="9" label="参数" class="q-mt-md">{{
        elementDicInfoModel.elementDicParam
      }}</description>
      <description align="center" :span="9" label="简介" class="q-mt-md">{{
        elementDicInfoModel.methodDesc
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
      elementDicInfoModel: {
        elementDicMethod: '',
        elementDicParam: '',
        methodDesc: ''
      },
      rule: {
        elementDicMethod: [
          { required: true, message: '请填写方法名', trigger: 'blur' }
        ],
        elementDicParam: [
          { required: true, message: '请填写参数', trigger: 'blur' }
        ],
        methodDesc: [{ required: true, message: '请填写简介', trigger: 'blur' }]
      },
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      json: [
        { prop: 'elementMethod', label: '方法名', sortable: true },
        { prop: 'elementParam', label: '参数名' },
        { prop: 'methodDesc', label: '简介' }
      ],
      showDataDetail: false
    };
  },
  computed: {
    ...mapState('autoTestForm', ['elementDicList'])
  },
  watch: {},
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'queryElementDic',
      'addElementDic',
      'deleteElementDic',
      'updateElementDic'
    ]),
    showAddDialog() {
      this.elementDicInfoModel = {
        elementDicMethod: '',
        elementDicParam: '',
        methodDesc: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '元素字典新增';
    },
    showEditDialog(data) {
      this.elementDicInfoModel = {
        elementDicMethod: data.row.elementMethod,
        elementDicParam: data.row.elementParam,
        methodDesc: data.row.methodDesc,
        elementDicId: data.row.elementDicId.toString()
      };
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '元素字典编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showDataDetail = false;
    },
    async confirm() {
      await validate(this.$refs['elementDicInfoModel']);
      if (this.isEditModel) {
        await this.updateElementDic(this.elementDicInfoModel);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addElementDic(this.elementDicInfoModel);
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
      await this.queryElementDic(param);
      this.tableInfo = this.elementDicList.elementDic || [];
    },
    async showItem(row) {
      this.elementDicInfoModel = {
        elementDicMethod: row.elementMethod,
        elementDicParam: row.elementParam,
        methodDesc: row.methodDesc
      };
      this.showDataDetail = true;
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.elementDicId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该元素字典?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteElementDic({
        elementDicId: [row.elementDicId.toString()]
      });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除元素字典?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteElementDic({ elementDicId: this.deleteIds });
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
    await this.queryElementDic(param);
    this.tableInfo = this.elementDicList.elementDic || [];
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
