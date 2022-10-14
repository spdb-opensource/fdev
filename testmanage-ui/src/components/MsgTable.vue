<template>
  <div>
    <div v-if="type">
      <el-select v-model="msgtype" placeholder="消息类型" clearable>
        <el-option
          v-for="item in msgTypeList"
          :key="item"
          :label="item"
          :value="item"
        ></el-option>
      </el-select>
      <el-button type="primary" @click="redMsg">一键已读</el-button>
    </div>
    <el-table
      stripe
      :data="allMsg.slice((currentPage - 1) * pagesize, currentPage * pagesize)"
      tooltip-effect="dark"
      style="width: 100%;color:black"
      :header-cell-style="{ color: '#545c64' }"
      @row-click="linkTo"
      :row-class-name="className"
    >
      <el-table-column
        prop="content"
        label="消息内容"
        :show-overflow-tooltip="true"
      >
        <template slot-scope="scope">
          <el-link :underline="false" type="primary" v-if="scope.row.linkUri">
            {{ scope.row.content }}
          </el-link>
          <span v-else>{{ scope.row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="create_time"
        label="创建时间"
        width="200"
      ></el-table-column>
      <el-table-column
        prop="type"
        label="消息类型"
        width="120"
      ></el-table-column>
      <el-table-column label="操作" width="80" v-if="type === 'unread'">
        <template slot-scope="scope">
          <el-button
            size="mini"
            @click.stop="changeMsgState(scope.$index, scope.row)"
            >知道啦</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[5, 10, 50, 100, 200, 500]"
        :page-size="pagesize"
        layout="sizes, prev, pager, next, jumper"
        :total="allMsg.length"
      />
    </div>
  </div>
</template>
<script>
export default {
  props: {
    tables: {
      type: Array
    },
    type: {
      type: String,
      default: ''
    },
    msgTypeList: {
      type: Array
    }
  },
  data() {
    return {
      currentPage: 1,
      pagesize: 10,
      msgtype: '',
      checkedMsgIds: [],
      allMsg: []
    };
  },
  watch: {
    tables: {
      immediate: true,
      handler: 'filterTable'
    },
    msgtype: [
      function(val) {
        this.checkedMsgIds = this.tables
          .filter(msg => msg.type === val)
          .map(msg => msg.id);
      },
      {
        handler: 'filter'
      }
    ]
  },
  methods: {
    filterTable(val) {
      this.allMsg = this.tables.filter(msg => msg);
    },
    filter(val) {
      if (!val) {
        this.allMsg = this.tables;
      } else {
        this.allMsg = this.tables.filter(msg => msg.type === val);
      }
    },
    handleSizeChange: function(size) {
      this.pagesize = size;
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    changeMsgState(index, row) {
      this.$emit('changeMsgState', row.id);
    },
    redMsg() {
      let msg = this.msgtype ? `${this.msgtype}类型` : '所有';
      this.$confirm(`此操作将把${msg}消息全部置为已读, 是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (this.msgtype) {
          this.$emit('redMsgs', this.checkedMsgIds);
          this.msgtype = '';
        } else {
          let ids = this.tables.map(msg => msg.id);
          this.$emit('redMsgs', ids);
        }
      });
    },
    linkTo({ linkUri }, column, event) {
      if (linkUri && linkUri.includes('/tui/#/')) {
        const index = linkUri.indexOf('#/') + 1;
        const path = linkUri.substring(index);
        this.$router.push(path);
      }
      if (linkUri && linkUri.includes('MantisIssue')) {
        const url = linkUri.substring(linkUri.indexOf('#') + 14);
        this.$router.push({
          name: 'MantisIssue',
          query: { id: url }
        });
      }
    },
    className({ row, rowIndex }) {
      const { linkUri } = row;
      if (linkUri && linkUri.includes('tui/#/')) return 'clickable';
    }
  }
};
</script>
<style scoped>
.pagination {
  margin-top: 3%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
.el-table >>> .clickable {
  cursor: pointer;
}
</style>
