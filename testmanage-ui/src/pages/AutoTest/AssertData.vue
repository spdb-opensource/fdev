<template>
  <div class="page-container">
    <strong class="title">断言数据维护</strong>
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
      title="断言标签"
    />
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form
        :inline="true"
        :model="assertInfoModel"
        label-position="right"
        :rules="rule"
        ref="assertInfoModel"
      >
        <el-form-item
          v-if="!isEditModel"
          label="断言标签"
          style="display:block;"
        >
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
        <el-divider v-if="!isEditModel"></el-divider>

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
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
    </Dialog>
    <Dialog
      title="断言数据详情"
      :visible.sync="showCaseDetail"
      width="60%"
      :before-close="closeDialog"
    >
      <el-row class="q-mt-md">
        <el-col align="right" :span="12">
          <el-row>
            <el-col :span="8">断言标签:</el-col>
            <el-col :offset="1" :span="14" align="left">{{
              assertDetail.label
            }}</el-col>
          </el-row>
        </el-col>
      </el-row>
      <el-row class="q-mt-md">
        <el-col
          v-for="(item, index) in assertDetailFilter"
          :key="index"
          align="right"
          :span="12"
        >
          <el-row>
            <el-col :span="8">{{ item.label }}：</el-col>
            <el-col :offset="1" :span="14" align="left">{{
              item.value
            }}</el-col>
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
      assertInfoModel: {
        label: '',
        assertId: '',
        assertList: {}
      },
      rule: {
        label: [{ required: true, message: '请填写断言标签', trigger: 'blur' }],
        assertList: [
          { required: true, message: '请填写断言数据', trigger: 'blur' }
        ]
      },
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      currentAssert: {
        label: '',
        assertId: '',
        assertList: {}
      },
      json: [{ prop: 'label', label: '断言标签' }],
      showCaseDetail: false,
      assertDetail: {
        label: '',
        assertList: []
      },
      initValue: {}
    };
  },
  computed: {
    ...mapState('autoTestForm', ['assertList']),
    assertDetailFilter() {
      return this.assertDetail.assertList.filter((item, index) => {
        return item.label.indexOf('ASSERTDATA') !== -1;
      });
    }
  },
  watch: {
    currentAssert: {
      deep: true,
      handler(val) {
        if (!val) {
          this.assertInfoModel = {
            label: '',
            assertList: deepClone(this.initValue)
          };
          return;
        }
        this.assertInfoModel = {
          label: val.label,
          assertList: {}
        };
        let key = '';
        this.assertInfoModel.assertList = deepClone(this.initValue);
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
      'addAssert',
      'queryAssert',
      'deleteAssert',
      'updateAssert'
    ]),
    showAddDialog() {
      this.assertInfoModel = {
        label: '',
        assertList: deepClone(this.initValue),
        assertId: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '断言数据新增';
    },
    showEditDialog(data) {
      this.assertInfoModel = {
        label: data.row.label,
        assertList: {},
        assertId: data.row.assertId
      };
      let key = '';
      this.assertInfoModel.assertList = deepClone(this.initValue);
      Object.keys(data.row).forEach((item, index) => {
        if (item.indexOf('ASSERTDATA') !== -1) {
          key = item.toLowerCase().replace('d', 'D');
          this.$set(this.assertInfoModel.assertList, key, data.row[item]);
        }
      });
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '断言数据编辑';
    },
    closeDialog() {
      this.showDialog = false;
      this.showCaseDetail = false;
      this.assertInfoModel = {
        label: '',
        assertList: this.initValue,
        assertId: ''
      };
    },
    async confirm() {
      await validate(this.$refs['assertInfoModel']);
      let param = {
        label: this.assertInfoModel.label,
        ...this.assertInfoModel.assertList
      };
      if (this.isEditModel) {
        param = {
          ...param,
          assertId: this.assertInfoModel.assertId
        };
        await this.updateAssert(param);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addAssert(param);
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
      await this.queryAssert(param);
      this.tableInfo = this.assertList.assert || [];
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.assertId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该断言数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteAssert({ assertId: [row.assertId.toString()] });
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
      await this.query();
    },
    async delItems() {
      await this.$confirm('确定批量删除断言数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });

      await this.deleteAssert({ assertId: this.deleteIds });
      await this.query();
      this.$message({
        type: 'success',
        message: '删除成功!'
      });
    },
    showItem(row) {
      this.assertDetail.label = row.label;
      this.assertDetail.assertList = [];
      Object.keys(row).forEach((val, index) => {
        this.assertDetail.assertList.push({
          label: val,
          value: row[val]
        });
      });
      this.showCaseDetail = true;
    }
  },
  created() {
    for (let i = 0; i < 10; i++) {
      this.$set(this.assertInfoModel.assertList, 'assertData' + (i + 1), '');
    }
    this.initValue = this.assertInfoModel.assertList;
  },
  async mounted() {
    let param = {
      search: '',
      valid: '0'
    };
    await this.queryAssert(param);
    let param1 = {
      search: '',
      valid: '0',
      flag: ''
    };
    await this.queryAssert(param1);
    this.tableInfo = this.assertList.assert || [];
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
