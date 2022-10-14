<template>
  <div class="page-container">
    <strong class="title">测试数据维护</strong>
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
      title="数据标签"
    />
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form
        :inline="true"
        :model="dataInfoModel"
        label-position="right"
        :rules="rule"
        ref="dataInfoModel"
      >
        <el-form-item
          v-if="!isEditModel"
          label="数据标签"
          style="display:block;"
        >
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
        <el-divider v-if="!isEditModel"></el-divider>

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
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="案例数据详情"
      :visible.sync="showCaseDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <el-row>
        <el-col style="text-align:center" :span="3">
          数据标签:
        </el-col>
        <el-col :span="8">
          {{ caseDetail.label }}
        </el-col>
      </el-row>
      <el-row class="q-mt-md">
        <el-col
          v-for="(item, index) in caseDetailFilter"
          :key="index"
          align="center"
          :span="12"
        >
          <el-row>
            <el-col :span="6">{{ item.label }}:</el-col>
            <el-col :span="16" align="left">{{ item.value }}</el-col>
          </el-row>
        </el-col>
      </el-row>
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
      dataInfoModel: {
        label: '',
        caseList: {}
      },
      rule: {
        label: [{ required: true, message: '请填写数据标签', trigger: 'blur' }]
      },
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      currentData: {},
      json: [{ prop: 'label', label: '数据标签' }],
      showCaseDetail: false,
      caseDetail: {
        caseDetailList: [],
        label: ''
      },
      initValue: {}
    };
  },
  computed: {
    ...mapState('autoTestForm', ['dataList']),
    caseDetailFilter() {
      return this.caseDetail.caseDetailList.filter((item, index) => {
        return item.label.indexOf('CASE') !== -1;
      });
    }
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
    }
  },
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'addData',
      'queryData',
      'deleteData',
      'updateData'
    ]),
    showAddDialog() {
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '案例数据新增';
    },
    showEditDialog(data) {
      this.dataInfoModel = {
        label: data.row.label,
        caseList: {},
        dataId: data.row.dataId
      };
      let key = '';
      Object.keys(data.row)
        .sort()
        .forEach((item, index) => {
          if (item.indexOf('CASE') !== -1) {
            key = item.toLowerCase();
            this.$set(this.dataInfoModel.caseList, key, data.row[item]);
          }
        });
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '案例数据编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showCaseDetail = false;
      this.dataInfoModel = {
        label: '',
        caseList: deepClone(this.initValue)
      };
    },
    async confirm() {
      await validate(this.$refs['dataInfoModel']);
      let param = {
        label: this.dataInfoModel.label,
        ...this.dataInfoModel.caseList
      };
      if (this.isEditModel) {
        param.dataId = this.dataInfoModel.dataId;
        await this.updateData(param);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addData(param);
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
        valid: '0'
      };
      await this.queryData(param);
      this.tableInfo = this.dataList.data || [];
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.dataId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该案例数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteData({ dataId: [row.dataId.toString()] });
      await this.query(this.searchValue);
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    async delItems() {
      await this.$confirm('确定批量删除案例数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteData({ dataId: this.deleteIds });
      await this.query(this.searchValue);
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    showItem(row) {
      this.caseDetail = {
        caseDetailList: [],
        label: ''
      };
      Object.keys(row)
        .sort()
        .forEach((val, index) => {
          this.caseDetail.caseDetailList.push({
            label: val,
            value: row[val]
          });
        });
      this.caseDetail.label = row.label;
      this.showCaseDetail = true;
    },
    addCase() {
      let num = Object.keys(this.dataInfoModel.caseList).length;
      this.$set(this.dataInfoModel.caseList, 'case' + (num + 1), '');
    },
    reduceCase() {
      let num = Object.keys(this.dataInfoModel.caseList).length;
      this.$delete(this.dataInfoModel.caseList, 'case' + num);
    }
  },
  created() {
    for (let i = 0; i < 8; i++) {
      this.$set(this.dataInfoModel.caseList, 'case' + (i + 1), '');
    }
    this.initValue = deepClone(this.dataInfoModel.caseList);
  },
  async mounted() {
    let param = {
      search: '',
      valid: '0'
    };
    await this.queryData(param);
    this.tableInfo = this.dataList.data || [];
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
