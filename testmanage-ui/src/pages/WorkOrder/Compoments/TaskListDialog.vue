<template>
  <el-dialog
    title="任务列表"
    :visible.sync="openDialog"
    :before-close="closeDialog"
    width="65%"
    class="abow_dialog"
  >
    <el-table
      :data="
        dialogModel.slice((currentPage - 1) * pageSize, currentPage * pageSize)
      "
      tooltip-effect="dark"
      style="width: 100%;color:black"
      :header-cell-style="{ color: '#545c64' }"
    >
      <el-table-column prop="id" label="任务ID" v-if="isShowOpt">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.id"
            placement="top-start"
          >
            <span class="td-width width">{{ scope.row.id }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column width="300" prop="name" label="任务名称">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.name"
            placement="top-start"
          >
            <span class="td-width width">{{ scope.row.name }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="spdb_master" label="行内负责人">
        <template slot-scope="scope">
          <span v-for="(user, index) in scope.row.spdb_master" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="master" label="负责人">
        <template slot-scope="scope">
          <span v-for="(user, index) in scope.row.master" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="developer" label="开发人员">
        <template slot-scope="scope">
          <span v-for="(user, index) in scope.row.developer" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="group" label="所属组">
        <template slot-scope="scope">
          <span>{{ scope.row.group.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="project_name" label="应用名" v-if="isShowOpt">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.project_name"
            placement="top-start"
          >
            <span class="td-width width">{{ scope.row.project_name }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="stage" label="任务阶段"></el-table-column>
      <el-table-column label="操作" v-if="isShowOpt">
        <template slot-scope="scope">
          <el-tooltip
            v-if="showCallBack && isOtherGroup != scope.row.group.id"
            class="item"
            effect="dark"
            content="线上通知fdev相关人员变更实施单元"
            placement="top"
          >
            <span class="call-back" @click="callBack(scope)">拒绝通知</span>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[5, 10, 50, 100, 200, 500]"
        :page-size="pageSize"
        layout="sizes, prev, pager, next, jumper"
        :total="dialogModel.length"
      />
    </div>
  </el-dialog>
</template>
<script>
export default {
  name: 'TaskListDialog',
  data() {
    return {
      developerList: [],
      handlerList: [],
      currentPage: 1,
      pageSize: 5
    };
  },
  props: {
    isOtherGroup: {
      type: String,
      default: ''
    },
    showCallBack: {
      type: Boolean,
      default: false
    },
    openDialog: {
      type: Boolean,
      default: false
    },
    dialogModel: {
      default: () => [],
      type: Array
    },
    isShowOpt: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    callBack(data) {
      this.$emit('callBack', data);
    },
    closeDialog() {
      this.$emit('taskListClose');
    },
    handleSizeChange(size) {
      this.pageSize = size;
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    }
  }
};
</script>
<style scoped>
.pagination {
  margin-top: 3%;
  margin-bottom: 8%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
.tooltip {
  width: 100% !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tooltip >>> .el-link--inner {
  width: 100% !important;
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.td-width {
  overflow: hidden;
  text-overflow: ellipsis;
  word-wrap: none;
  display: inline-block;
  white-space: nowrap;
}
.width {
  width: 100%;
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
.call-back {
  cursor: pointer;
  color: red;
}
</style>
