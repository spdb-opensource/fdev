<template>
  <div class="page-container">
    <el-row>
      <el-col :span="8">
        <el-input
          clearable
          style="width:100%"
          placeholder="请输入功能菜单名称进行查询"
          v-model="filterText"
        >
        </el-input>
      </el-col>
    </el-row>
    <br />
    <el-row>
      <el-scrollbar>
        <div class="aside">
          <el-tree
            :data="data"
            :props="props"
            :expand-on-click-node="false"
            class="filter-tree"
            :filter-node-method="filterNode"
            ref="tree"
          >
            <span class="custom-tree-node" slot-scope="{ node, data }">
              <span>{{ node.label }}</span>
              <span>
                <!-- 添加功能菜单弹框 -->
                <el-button
                  type="text"
                  @click="
                    (dialogAddFuncMenu = true), getFuncData(data.$index, data)
                  "
                >
                  <i class="el-icon-plus tabel-icon" />
                </el-button>
                <!-- 编辑功能菜单弹框 -->
                <el-button
                  type="text"
                  @click="
                    (dialogEditFuncMenu = true), getFuncData(data.$index, data)
                  "
                >
                  <div v-if="data.func_id">
                    <i class="el-icon-edit tabel-icon" />
                  </div>
                </el-button>
                <!-- 删除按钮 -->
                <el-button type="text" @click="deleteFunctionMenu(data)">
                  <div
                    v-if="
                      data.children.length === 0 &&
                        data.level !== -1 &&
                        isDeleteAdmin
                    "
                  >
                    <i class="el-icon-delete tabel-icon" />
                  </div>
                </el-button>
              </span>
            </span>
          </el-tree>
        </div>
      </el-scrollbar>
    </el-row>

    <!-- 添加功能菜单弹框 -->
    <el-dialog
      title="添加功能菜单"
      :visible.sync="dialogAddFuncMenu"
      width="35%"
      class="abow_dialog"
    >
      <el-form :model="addForm">
        <el-form-item label="功能名称 :" :label-width="labelWidth">
          <el-input
            type="text"
            v-model="addFuncName"
            autocomplate="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogAddFuncMenu = false">取消</el-button>
        <el-button type="primary" @click="addFunctionMenu(addForm, addFuncName)"
          >确定</el-button
        >
      </div>
    </el-dialog>
    <!-- 编辑功能菜单弹框 -->
    <el-dialog
      title="编辑功能菜单"
      :visible.sync="dialogEditFuncMenu"
      width="35%"
      class="abow_dialog"
    >
      <el-form :model="editForm">
        <el-form-item label="功能名称 :" :label-width="labelWidth">
          <el-input
            type="text"
            v-model="editForm.label"
            autocomplate="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogEditFuncMenu = false">取消</el-button>
        <el-button
          type="primary"
          @click="editFunctionMenu(editForm, editForm.label)"
          >确定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: 'FunctionMenu',
  data() {
    return {
      form: {
        name: ''
      },
      systemName: {
        systemName: '',
        id: ''
      },
      data: [],
      props: {
        children: 'children',
        label: 'label'
      },
      index: -1,
      dialogAddFuncMenu: false,
      dialogEditFuncMenu: false,
      labelWidth: '80px',
      addForm: {},
      editForm: [],
      filterText: '',
      addFuncName: '',
      visible: false
    };
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    ...mapActions('adminForm', [
      'updateFunctionMenu',
      'addMenu',
      'listAll',
      'delFunctionMenu',
      'queryAllSystem'
    ]),
    //新增功能菜单
    async addFunctionMenu(param1, param2) {
      this.dialogAddFuncMenu = false;
      await this.addMenu({
        sys_func_id: param1.sys_id,
        level: param1.level == -1 ? 1 : param1.level + 1,
        func_model_name: param2,
        parent_id: param1.parent_id == -1 ? 0 : param1.func_id
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '添加功能菜单成功'
      });
      this.initlistAll();
      this.addFuncName = '';
    },

    //查询新增功能菜单中的系统名称
    async initqueryListAll() {
      await this.queryAllSystem();
      this.systemName = this.systemList.map(item => {
        return {
          ...item,
          value: { id: item.sys_id, name_cn: item.sys_module_name }
        };
      });
    },

    // 查询系统列表
    async initlistAll(index, row) {
      this.index = index;
      await this.listAll();
      var map = {};
      var list = [];
      for (var i = 0; i < this.listAllData.length; i++) {
        if (this.listAllData[i].func_id)
          map[this.listAllData[i].func_id] = {
            label: this.listAllData[i].func_model_name,
            children: [],
            parent_id: this.listAllData[i].parent_id,
            sys_id: this.listAllData[i].sys_id,
            func_id: this.listAllData[i].func_id,
            level: this.listAllData[i].level
          };
        else
          map[this.listAllData[i].sys_id] = {
            label: this.listAllData[i].sys_module_name,
            children: [],
            parent_id: -1,
            sys_id: this.listAllData[i].sys_id,
            level: -1
          };
      }
      for (var field in map) {
        var obj = map[field];
        var parent_id = obj.parent_id;
        if (parent_id == 0) parent_id = obj.sys_id;
        if (parent_id == -1) {
          list[list.length] = obj;
        } else {
          map[parent_id].children[map[parent_id].children.length] = obj;
        }
      }
      this.data = list;
    },
    // 删除叶子节点
    async deleteFunctionMenu(row) {
      await this.$confirm('此操作将永久删除该节点, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.delFunctionMenu({
        funcId: row.func_id.toString()
      });
      this.initlistAll();
      this.$message({
        showClose: true,
        type: 'success',
        message: '删除成功!'
      });
    },

    //功能菜单编辑 数据回显
    getFuncData(index, row) {
      this.editForm = JSON.parse(JSON.stringify(row));
      this.editForm.label = row.label;
      this.addForm = JSON.parse(JSON.stringify(row));
    },
    //功能菜单编辑 发后台
    async editFunctionMenu(param1, param2) {
      this.dialogEditFuncMenu = false;
      await this.updateFunctionMenu({
        funcId: param1.func_id,
        funcName: param2
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '修改成功！'
      });
      this.initlistAll();
    },

    //树节点过滤
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    }
  },

  mounted() {
    this.initqueryListAll();
    this.initlistAll();
  },

  computed: {
    ...mapState('adminForm', ['listAllData', 'systemList']),
    isDeleteAdmin() {
      let userRole = JSON.parse(sessionStorage.getItem('userRole'));
      return userRole.some(user => {
        return (
          user.name === '玉衡-测试管理员' || user.name === '玉衡超级管理员'
        );
      });
    }
  }
};
</script>

<style scoped>
.addTag {
  height: 40px;
  line-height: 40px;
}
.addBtn {
  float: right;
  margin-right: 1%;
}
.queryBtn {
  float: right;
  margin-right: 1%;
}
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}
.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.abow_dialog >>> .el-dialog__header {
  background: #409eff;
}
.el-dialog .el-form {
  max-height: 380px;
  overflow: auto;
}
.pagination {
  margin-top: 3%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
.page-container {
  width: 100%;
  margin-left: 10%;
}
.aside {
  height: 100vh;
  overflow: auto;
  margin-left: 0;
}
</style>
