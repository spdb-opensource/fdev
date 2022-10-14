<template>
  <div class="page-container">
    <strong class="title">菜单项维护</strong>
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
      :showDetail="false"
      title="菜单名称"
    />
    <Dialog
      :title="title"
      :visible.sync="showDialog"
      width="60%"
      :before-close="closeDialog"
    >
      <el-form
        :inline="true"
        :model="menuInfoModel"
        label-position="right"
        :rules="rule"
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
        <el-button @click="confirm">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </div>
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
      menuInfoModel: {
        menuNo: '',
        menuName: '',
        secondaryMenuNo: '',
        secondaryMenu: '',
        thirdMenuNo: '',
        thirdMenu: ''
      },
      rule: {
        menuNo: [
          { required: true, message: '请填写一级菜单编号', trigger: 'blur' }
        ],
        menuName: [
          { required: true, message: '请填写一级菜单名称', trigger: 'blur' }
        ]
      },
      showDialog: false,
      deleteIds: [],
      tableInfo: [],
      searchValue: '',
      isEditModel: false,
      title: '',
      json: [
        { prop: 'menuNo', label: '一级菜单编号' },
        { prop: 'menuName', label: '一级菜单名称' },
        { prop: 'secondaryMenuNo', label: '二级菜单编号' },
        { prop: 'secondaryMenu', label: '二级菜单名称' },
        { prop: 'thirdMenuNo', label: '三级菜单编号' },
        { prop: 'thirdMenu', label: '三级菜单名称' }
      ]
    };
  },
  computed: {
    ...mapState('autoTestForm', ['menuData'])
  },
  watch: {},
  props: {},
  methods: {
    ...mapActions('autoTestForm', [
      'queryMenu',
      'addMenu',
      'deleteMenu',
      'updateMenu'
    ]),
    showAddDialog() {
      this.menuInfoModel = {
        menuNo: '',
        menuName: '',
        secondaryMenuNo: '',
        secondaryMenu: '',
        thirdMenuNo: '',
        thirdMenu: ''
      };
      this.showDialog = true;
      this.isEditModel = false;
      this.title = '菜单新增';
    },
    showEditDialog(data) {
      this.menuInfoModel = {
        menuNo: data.row.menuNo,
        menuName: data.row.menuName,
        secondaryMenuNo: data.row.secondaryMenuNo,
        secondaryMenu: data.row.secondaryMenu,
        thirdMenuNo: data.row.thirdMenuNo,
        thirdMenu: data.row.thirdMenu,
        menuSheetId: data.row.menuSheetId
      };
      this.showDialog = true;
      this.isEditModel = true;
      this.title = '菜单编辑';
    },
    closeDialog() {
      this.showDialog = false;
    },
    async confirm() {
      await validate(this.$refs['menuInfoModel']);
      if (this.isEditModel) {
        await this.updateMenu(this.menuInfoModel);
        this.$message({
          type: 'success',
          message: '修改成功!'
        });
      } else {
        await this.addMenu(this.menuInfoModel);
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
      await this.queryMenu(param);
      this.tableInfo = this.menuData.menu || [];
    },
    selectChange(selection) {
      this.deleteIds = [];
      selection.forEach(item => {
        this.deleteIds.push(item.menuSheetId.toString());
      });
    },
    async delOneItem(row) {
      await this.$confirm('是否删除该菜单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.deleteMenu({ menuSheetId: [row.menuSheetId.toString()] });
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
      await this.deleteMenu({ menuSheetId: this.deleteIds });
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
    await this.queryMenu(param);
    this.tableInfo = this.menuData.menu || [];
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
