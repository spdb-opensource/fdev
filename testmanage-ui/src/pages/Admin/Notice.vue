<template>
  <div class="bg-div">
    <el-container class="bg-container">
      <el-header style="margin-left:20px;">
        <el-form :inline="true" class="demo-form-inline">
          <el-col :span="8">
            <el-form-item>
              <el-input v-model="search" placeholder="请输入内容" clearable>
                <el-button
                  slot="append"
                  icon="el-icon-search"
                  class="searchIcon"
                ></el-button>
              </el-input>
            </el-form-item>
          </el-col>
        </el-form>
      </el-header>
      <el-main style="padding:20px">
        <el-table
          stripe
          :data="
            tables().slice((currentPage - 1) * pagesize, currentPage * pagesize)
          "
          tooltip-effect="dark"
          style="width: 100%;color:black"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column
            prop="content"
            label="公告内容"
            :show-overflow-tooltip="true"
          >
            <template slot-scope="scope">
              <el-link :underline="false" @click="contentDetails(scope.row)">{{
                scope.row.content
              }}</el-link>
            </template>
          </el-table-column>
          <el-table-column
            prop="create_time"
            label="创建时间"
            width="200"
          ></el-table-column>
          <el-table-column
            prop="type"
            label="公告类型"
            width="120"
            :formatter="formatType"
          ></el-table-column>
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
            :total="total"
          ></el-pagination>
        </div>
      </el-main>
    </el-container>
    <div class="addNoticeBtn">
      <el-button type="primary" @click="addNotice" v-if="userRole > 30"
        >发 起 公 告</el-button
      >
    </div>

    <!-- 公告详情 -->
    <el-dialog
      title="公 告"
      :visible.sync="dialogNoticeDetail"
      width="40%"
      class="abow_dialog"
      center
    >
      <div v-html="descFilter(message)" class="middleContent" />
      <span v-if="detailExpiryTime" class="span-position"
        >过期时间：{{ detailExpiryTime }}</span
      >
      <div slot="footer" class="dialog-footer">
        <div class="divider"></div>
        <el-button @click="closeNoticeDialog" class="dialogKnowBtn"
          >知道啦</el-button
        >
      </div>
    </el-dialog>

    <!-- 发起公告 -->
    <el-dialog
      title="发起公告"
      :visible.sync="dialogAddNotice"
      width="35%"
      class="small_dialog"
      :before-close="handleClose"
    >
      <el-form :model="form" :rules="dialogRules" ref="form">
        <el-form-item label="公告类型" prop="type">
          <el-select
            v-model="form.type"
            placeholder="请选择公告类型"
            style="width:80%"
            filterable
            clearable
          >
            <el-option
              v-for="(item, index) in typeList"
              :key="index"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间" prop="expiry_time" class="expiryTime">
          <div class="block">
            <el-date-picker
              v-model="form.expiry_time"
              type="datetime"
              placeholder="请选择公告过期时间"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width:81%"
              :picker-options="expireTimeOption"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="公告内容" prop="noticeContent">
          <el-input
            type="textarea"
            :rows="6"
            style="width:80%"
            placeholder="请输入公告内容"
            maxlength="500"
            show-word-limit
            class="inputDialogContent"
            v-model="form.noticeContent"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          @click="confirmAddNotice('form')"
          class="confirmAddNotice"
          >发起公告</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';

export default {
  name: 'Noice',
  data() {
    return {
      userRole: sessionStorage.getItem('Trole'),
      search: '',
      currentPage: 1,
      pagesize: 10,
      total: 0,
      dialogNoticeDetail: false,
      dialogAddNotice: false,
      form: {},
      type: '',
      expiry_time: '',
      noticeContent: '',
      typeList: [
        { label: '更新公告', value: 'announce-update' },
        { label: '停机公告', value: 'announce-halt ' }
      ],
      expireTimeOption: {
        disabledDate(time) {
          return time.getTime() <= Date.now() - 24 * 60 * 60 * 1000;
        }
      },
      dialogRules: {
        type: [{ required: true, message: '请选择公告类型', trigger: 'blur' }],
        noticeContent: [
          { required: true, message: '请输入公告内容', trigger: 'blur' }
        ]
      },
      message: '',
      detailExpiryTime: ''
    };
  },
  computed: {
    ...mapState('adminForm', ['announceList'])
  },
  methods: {
    ...mapActions('adminForm', ['queryAnnounce', 'sendAnnounce']),
    formatType(row, column) {
      if (row.type === 'announce-update') {
        return '更新公告';
      } else if (row.type === 'announce-halt') {
        return '停机公告';
      } else {
        return ' ';
      }
    },
    descFilter(wsMsg) {
      if (!wsMsg) {
        return wsMsg;
      }
      let reg = new RegExp(/\n/g);
      let reg1 = new RegExp(/\s/);
      let reg2 = new RegExp(/</g);
      let reg3 = new RegExp(/>/g);
      let str1 = wsMsg
        .replace(reg2, '&lt;')
        .replace(reg3, '&gt;')
        .replace(reg, '</br>');
      let str2 = '';
      while (reg1.test(str1)) {
        str2 = str1.replace(reg1, '&nbsp;');
        str1 = str2;
      }
      return str2 ? str2 : str1;
    },
    handleSizeChange: function(size) {
      this.pagesize = size;
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    contentDetails(row) {
      this.dialogNoticeDetail = true;
      this.message = row.content;
      this.detailExpiryTime = row.expiry_time;
    },
    closeNoticeDialog() {
      this.dialogNoticeDetail = false;
    },
    addNotice() {
      this.dialogAddNotice = true;
    },

    confirmAddNotice(form) {
      this.$refs[form].validate(valid => {
        if (valid) {
          this.sendAnnounce({
            type: this.form.type,
            expiry_time: this.form.expiry_time,
            content: this.form.noticeContent
          });
          this.dialogAddNotice = false;
          this.queryAnnounce();
          this.$message({
            showClose: true,
            message: '公告发起成功',
            type: 'success'
          });
          this.$refs[form].resetFields();
        } else {
          return false;
        }
      });
    },
    handleClose() {
      this.dialogAddNotice = false;
      this.form = {};
    },
    tables() {
      const search = this.search.trim();
      if (search) {
        let data = this.announceList.filter(data => {
          let matchContent =
            data.content.includes(search) || data.create_time.includes(search);
          let announceType = false;
          // announce-halt  停机  announce-update  更新
          if ('更新公告'.includes(search)) {
            announceType = data.type === 'announce-update';
          }
          if ('停机公告'.includes(search)) {
            announceType = data.type === 'announce-halt';
          }
          return matchContent || announceType;
        });
        this.total = data.length;
        return data;
      }
      this.total = this.announceList.length;
      return this.announceList;
    }
  },
  mounted() {
    document
      .querySelector('body')
      .setAttribute('style', 'background-color:#E0E0E0');
    this.queryAnnounce();
  },

  beforeDestroy() {
    document.querySelector('body').removeAttribute('style');
  }
};
</script>

<style scoped>
.body {
  background-color: #e0e0e0 !important;
}
.bg-div {
  background-color: white;
  min-height: 100vh;
  width: 90%;
  margin: 0 auto;
  text-align: left;
  padding: 20px 0 0 0;
}
.bg-div >>> .bg-container {
  width: 90%;
  margin: 0 auto;
}
.searchIcon >>> .el-icon-search {
  color: #409eff;
  font-size: 15px;
  font-weight: 600;
}
.pagination {
  margin-top: 3%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
.abow_dialog >>> .el-dialog__header {
  background: #409eff;
  padding: 15px 20px 10px;
}
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 25px;
  font-weight: 500;
}
.abow_dialog >>> .el-icon-close:before {
  display: none;
}
.addNoticeBtn {
  text-align: center;
  margin-top: 2%;
  margin-bottom: 2%;
}
.confirmAddNotice {
  width: 100%;
}
.small_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.small_dialog >>> .el-dialog__header {
  background: #409eff;
}

.small_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.inputDialogContent >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
}
.expiryTime {
  margin-left: 10px;
}
.dialogKnowBtn {
  border: 0px;
  color: #409eff;
  margin-left: 80%;
  margin-top: 10px;
}
.span-position {
  position: absolute;
  right: 50px;
  margin: 10px 0 20px 0;
}
.middleContent {
  min-height: 100px;
  max-height: 300px;
  overflow: auto;
}
</style>
