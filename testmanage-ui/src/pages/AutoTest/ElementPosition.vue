<template>
  <div class="page-container">
    <strong class="title">元素定位维护</strong>
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
      title="元素名称"
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
        <el-form-item prop="elementName" label="元素名称">
          <el-input v-model="elementDicInfoModel.elementName" />
        </el-form-item>
        <el-form-item prop="elementType" label="元素类型">
          <el-select
            filterable
            clearable
            v-model="elementDicInfoModel.elementType"
          >
            <el-option
              v-for="(item, index) in selData"
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
            v-model="elementDicInfoModel.elementContent"
          />
        </el-form-item>
        <el-form-item prop="elementDir" label="元素目录">
          <el-input
            type="textarea"
            :rows="3"
            v-model="elementDicInfoModel.elementDir"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="元素定位详情"
      :visible.sync="showDataDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <description align="center" :span="9" label="元素名称" class="q-mt-md">{{
        elementDicInfoModel.elementName
      }}</description>
      <description align="center" :span="9" label="元素类型" class="q-mt-md">{{
        elementDicInfoModel.elementType
      }}</description>
      <description align="center" :span="9" label="元素xpath" class="q-mt-md">{{
        elementDicInfoModel.elementContent
      }}</description>
      <description align="center" :span="9" label="元素目录" class="q-mt-md">{{
        elementDicInfoModel.elementDir
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
        elementName: '',
        elementType: 'xpath',
        elementDir: '',
        elementContent: ''
      },
      rule: {
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
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      json: [
        { prop: 'elementName', label: '元素名称', sortable: true },
        { prop: 'elementType', label: '元素类型' },
        { prop: 'elementDir', label: '元素目录' }
      ],
      showDataDetail: false,
      selData: [
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
    ...mapState('autoTestForm', ['elementList'])
  },
  watch: {},
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'queryElement',
      'addElement',
      'deleteElement',
      'updateElement'
    ]),
    showAddDialog() {
      this.elementDicInfoModel = {
        elementName: '',
        elementType: 'xpath',
        elementDir: '',
        elementContent: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '元素定位新增';
    },
    showEditDialog(data) {
      this.elementDicInfoModel = {
        elementName: data.row.elementName,
        elementType: data.row.elementType,
        elementDir: data.row.elementDir,
        elementId: data.row.elementId.toString(),
        elementContent: data.row.elementContent
      };
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '元素定位编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showDataDetail = false;
    },
    async confirm() {
      await validate(this.$refs['elementDicInfoModel']);
      if (this.isEditModel) {
        await this.updateElement(this.elementDicInfoModel);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addElement(this.elementDicInfoModel);
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
      await this.queryElement(param);
      this.tableInfo = this.elementList.element || [];
    },
    async showItem(row) {
      this.elementDicInfoModel = {
        elementName: row.elementName,
        elementType: row.elementType,
        elementDir: row.elementDir,
        elementContent: row.elementContent
      };
      this.showDataDetail = true;
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.elementId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该元素定位?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteElement({ elementId: [row.elementId.toString()] });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除元素定位?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteElement({ elementId: this.deleteIds });
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
    await this.queryElement(param);
    this.tableInfo = this.elementList.element || [];
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
