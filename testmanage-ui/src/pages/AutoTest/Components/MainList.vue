<template>
  <div>
    <el-form :inline="true" v-if="!isChild">
      <el-form-item :label="title">
        <el-input clearable maxlength="90" v-model="searchValue"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="query">查询</el-button>
        <el-button type="primary" @click="add">
          新增
        </el-button>
        <el-button type="primary" @click="delItems" :disabled="btnDisable">
          批量删除
        </el-button>
        <slot></slot>
      </el-form-item>
    </el-form>
    <el-table
      :data="tableDataFilter"
      ref="mainTable"
      @selection-change="selectChange"
      @expand-change="getDetail"
      :stripe="isChild"
      :border="isChild"
    >
      <template v-if="isChild" v-slot:append>
        <div style="float:right;padding: 10px 50px 10px 0;">
          <el-button type="primary" size="small" @click="add">
            新增
          </el-button>
        </div>
      </template>
      <el-table-column v-if="!isChild" type="selection" label="全选">
      </el-table-column>
      <el-table-column v-if="showExpand" type="expand" label="">
        <slot name="detail"></slot>
      </el-table-column>
      <el-table-column
        :sortable="showItem[0].sortable"
        :label="showItem[0].label"
        :prop="showItem[0].prop"
      >
        <template slot-scope="scope">
          <el-tooltip
            effect="dark"
            :content="scope.row[showItem[0].prop]"
            placement="top-start"
            class="tooltip"
          >
            <el-link
              v-if="showDetail"
              type="primary"
              :underline="false"
              @click="showItemDetail(scope.row)"
            >
              {{ scope.row[showItem[0].prop] }}
            </el-link>
            <span v-else>{{ scope.row[showItem[0].prop] }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column
        v-for="(col, index) in showItem.slice(1)"
        :prop="col.prop"
        :label="col.label"
        :key="index"
        :sortable="col.sortable"
      >
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="edit(scope)"
            >修改</el-button
          >
          <el-button type="text" size="small" @click="delOneItem(scope.row)"
            >删除</el-button
          >
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
        :total="tableData.length"
      />
    </div>
  </div>
</template>
<script>
export default {
  name: 'MainList',
  data() {
    return {
      btnDisable: true,
      searchValue: '',
      currentPage: 1,
      pageSize: 10
    };
  },
  props: {
    tableData: {
      default: () => [],
      type: Array
    },
    title: {
      default: '',
      type: String
    },
    showItem: {
      default: () => [],
      type: Array
    },
    showDetail: {
      default: true,
      type: Boolean
    },
    showExpand: {
      default: false,
      type: Boolean
    },
    isChild: {
      default: false,
      type: Boolean
    }
  },
  methods: {
    selectChange(selection) {
      if (selection.length > 0) {
        this.btnDisable = false;
      } else {
        this.btnDisable = true;
      }
      this.$emit('selectChange', selection);
    },
    async query() {
      this.currentPage = 1;
      this.$emit('query', this.searchValue);
    },
    async delOneItem(row) {
      this.$emit('delOneItem', row);
    },
    async delItems() {
      this.$emit('delItems');
    },
    add() {
      this.$emit('add');
    },
    edit(data) {
      this.$emit('edit', data);
    },
    handleSizeChange(size) {
      this.pageSize = size;
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    },
    showItemDetail(row) {
      this.$emit('showDetail', row);
    },
    getDetail(row1, row2) {
      this.$emit('getDetail', row1);
    }
  },
  computed: {
    tableDataFilter() {
      return this.tableData.slice(
        (this.currentPage - 1) * this.pageSize,
        this.currentPage * this.pageSize
      );
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
</style>
