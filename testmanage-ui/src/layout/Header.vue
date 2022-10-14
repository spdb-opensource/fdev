<template>
  <div>
    <el-row class="header" type="flex" align="middle">
      <el-col :span="showAuto ? 3 : 4">
        <el-tag>玉衡测试服务平台</el-tag>
      </el-col>
      <el-col :span="showAuto ? 15 : 14">
        <Menu />
      </el-col>
      <el-col :span="6" class="noprint">
        <el-row>
          <!-- <el-tooltip class="item"  content="sit测试消息" placement="left"> -->
          <el-menu
            :class="[
              classFlag ? 'touchMessage' : 'noticemessage',
              'el-menu-demo',
              'btn-float'
            ]"
            mode="horizontal"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
            @select="handleSelect"
          >
            <el-submenu index="noticemessage">
              <template slot="title">
                <div>
                  <span class="el-dropdown-link">
                    <i class="el-icon-message bell-style messageSize"
                      ><el-badge
                        v-if="dialogNoticeTable.length > 0"
                        is-dot
                      ></el-badge
                    ></i>
                  </span>
                </div>
              </template>
              <el-menu-item index="notice" class="noticeColor"
                >通知<el-badge
                  class="noticeCssDot"
                  v-if="dialogNoticeTable.length > 0"
                  is-dot
                ></el-badge
              ></el-menu-item>
              <el-menu-item
                index="message"
                :class="[classFlag ? 'activeNoticeColor' : '', 'noticeColor']"
                >消息</el-menu-item
              >
            </el-submenu>
          </el-menu>

          <el-dropdown trigger="click">
            <el-button class="button bell-button">
              <span class="el-dropdown-link">
                <i class="el-icon-bell bell-style"
                  ><el-badge
                    v-if="unreadSitMsg.length > 0"
                    :value="unreadSitMsg.length"
                    class="bell-notice"
                    :max="1000"
                /></i>
              </span>
            </el-button>
            <el-dropdown-menu slot="dropdown" v-if="unreadSitMsg.length > 0">
              <el-dropdown-item
                v-for="item in unreadToShow"
                :key="item.id"
                @click.native="changeUnreadMsgState(item)"
              >
                <div class="content-style">
                  <i class="el-icon-chat-dot-round msg-style"></i
                  >{{ `${item.content}(${item.type})` }}
                </div>
                <div class="time-style text-right">{{ item.create_time }}</div>
              </el-dropdown-item>
              <el-dropdown-item
                class="text-center"
                @click.native="more"
                v-if="unreadSitMsg.length > 5"
                >{{ `更多（${unreadSitMsg.length - 5}）` }}</el-dropdown-item
              >
            </el-dropdown-menu>
            <el-dropdown-menu v-else>
              <el-dropdown-item>暂无SIT未读消息</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!-- </el-tooltip> -->
          <el-dropdown trigger="click">
            <el-button class="button">
              <span class="el-dropdown-link">
                <el-row type="flex" align="middle">
                  <el-col :span="8" class="text-right">
                    <el-avatar :size="36" :src="circleUrl">{{
                      user_name_en | nameFilter
                    }}</el-avatar>
                  </el-col>
                  <el-col :span="16" class="text-left username">{{
                    username
                  }}</el-col>
                </el-row>
              </span>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                class="logOut"
                icon="el-icon-edit"
                @click.native="dialogFormVisible = true"
                >同步mantis</el-dropdown-item
              >
              <!-- <el-dropdown-item class="logOut" icon="el-icon-message" @click.native="messageNoc">消息记录</el-dropdown-item> -->
              <el-dropdown-item
                class="logOut"
                icon="el-icon-circle-close"
                @click.native="handleLogout"
                >退出登录</el-dropdown-item
              >
            </el-dropdown-menu>
          </el-dropdown>
        </el-row>
      </el-col>
    </el-row>

    <!-- 通知详情弹框 -->
    <el-dialog :visible.sync="dialogCardVisible" width="80%">
      <div slot="title">
        通知详情
        <el-switch
          class="mgr-50"
          v-model="mesFlag"
          active-text="未读"
          inactive-text="已读"
          active-value="1"
          inactive-value="0"
        >
        </el-switch>
      </div>
      <el-table
        stripe
        :data="dialogNoticeTable"
        tooltip-effect="dark"
        style="width: 100%"
      >
        <!-- <el-table-column type="selection"></el-table-column> -->
        <el-table-column
          prop="workNo"
          label="工单编号"
          width="150px"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column
          prop="taskName"
          label="任务名称"
          width="150px"
          show-overflow-tooltip
        />

        <el-table-column prop="workStage" label="测试阶段"></el-table-column>
        <el-table-column
          prop="taskReason"
          label="测试原因"
          :formatter="formatterTaskName"
        ></el-table-column>
        <el-table-column prop="rqrNo" label="需求编号"></el-table-column>
        <el-table-column
          prop="taskDesc"
          label="描述"
          width="200px"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column
          prop="messageFlag"
          label="是否已读"
          :formatter="formatterMessageFlag"
        ></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope" v-if="scope.row.messageFlag == 1">
            <el-button
              v-if="!scope.row.isEgdit"
              type="text"
              size="small"
              @click="handleAddPlan(scope.row)"
              >新增计划</el-button
            >
            <el-button
              @click="handleIgnore(scope.$index, scope.row)"
              type="text"
              size="small"
              >忽略</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="mesFlag == 1" type="primary" @click="handleAllIgnore()"
          >忽略全部</el-button
        >
      </div>
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[5, 10, 50, 100, 200, 500]"
          :page-size="pageSize"
          layout="sizes, prev, pager, next, jumper"
          :total="noticetotal"
        ></el-pagination>
      </div>
    </el-dialog>
    <!-- 新增执行计划  -->
    <el-dialog
      title="新增测试计划"
      :visible.sync="dialogAdd"
      width="35%"
      class="small_dialog"
    >
      <el-form :model="form" :label-width="formLabelWidth">
        <el-form-item label="计划名称">
          <el-input
            v-model="inputPlanName"
            autocomplete="off"
            style="width:80%"
            maxlength="90"
          ></el-input>
        </el-form-item>
        <el-form-item label="开始时间">
          <div class="block">
            <el-date-picker
              v-model="inputStartTime"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择开始日期"
              style="width:80%"
              :picker-options="startDatePicker"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="结束时间">
          <div class="block">
            <el-date-picker
              v-model="inputEndTime"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择结束日期"
              style="width:80%"
              :picker-options="endDatePicker"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="设备信息">
          <el-input
            v-model="deviceInfo"
            autocomplete="off"
            style="width:80%"
            clearable
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogAdd = false">取 消</el-button>
        <el-button
          type="primary"
          @click="
            inputAddPlan(
              inputPlanName,
              inputStartTime,
              inputEndTime,
              deviceInfo
            )
          "
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <el-dialog
      title="人员信息同步至mantis"
      :visible.sync="dialogFormVisible"
      width="40%"
      :before-close="CloseMantisDiolag"
      class="abow_dialog"
    >
      <el-form :model="form" :rules="rules" ref="form">
        <el-form-item
          label="mantis Token"
          :label-width="tokenLabelWidth"
          prop="mantis_token"
        >
          <el-input
            v-model="form.mantis_token"
            autocomplete="off"
            placeholder="请输入"
            maxlength="50"
            show-word-limit
            clearable
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <span class="mantisTooltip">
          <el-tooltip class="item" effect="light" placement="bottom">
            <div slot="content">
              获取mantis Token步骤
              <br />1、点击"进入mantis"可跳转至mantis平台
              <br />2、登录mantis平台后，用户名为：测试平台用户名，初始密码为fdev用户密码
              <br />3、登录成功后，页面跳转至创建API令牌
              <br />4、输入令牌名称"testmanage"，点击"创建API令牌"按钮
              <br />5、将生成的密钥复制粘贴到测试平台的mantis
              Token栏内，点击提交即可
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-link
            type="primary"
            href="xxx/api_tokens_page.php"
            target="_blank"
            :underline="false"
          >
            <el-link icon="el-icon-link" style="margin-bottom:7px"
              >进入mantis</el-link
            >
          </el-link>
        </span>
        <el-button @click="cancelMantisDialog('form')">取 消</el-button>
        <el-button type="primary" @click="submiToken('form')">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 公告实时弹窗 -->
    <el-dialog
      title="公 告"
      :visible.sync="dialogNotice"
      width="40%"
      class="abow_dialog"
      center
    >
      <div class="noticeContent">
        <el-collapse v-model="activeNames">
          <el-collapse-item
            v-for="(item, index) in newMsgList"
            :key="index"
            :name="index"
            class="noticeTitle"
          >
            <template slot="title">
              {{ item.type | typeFilter }}
            </template>
            <div v-html="descFilter(item.content)" class="middleContent" />
            <div v-if="item.expiry_time" class="notify-time">
              过期时间：{{ item.expiry_time }}
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeNoticeDialog" class="dialogKnowBtn"
          >知道啦</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { mapActions, mapState } from 'vuex';
import Menu from './Menu';
import { saveMantisToken } from '@/services/useradmin.js';
import { queryMessageRecord } from '@/services/header';
import { queryMessageCountImpl } from '@/services/header';
import { ignoreMessageImpl } from '@/services/order';
import { ignoreAllMessageImpl } from '@/services/order';
import { addPlan } from '@/services/plan';
import { logout } from '@/services/login';
import websocket from '@/common/socket.js';
import { queryAnnounce } from '@/services/useradmin';
import { updateNotifyStatus } from '@/services/useradmin';

export default {
  name: 'Header',
  components: { Menu },
  data() {
    var checkMantisToken = (rule, value, callback) => {
      const tokenReg = /[\u4E00-\u9FA5]/g;
      if (!value) {
        return callback(new Error('mantis Token不能为空！'));
      }
      setTimeout(() => {
        if (tokenReg.test(value)) {
          callback(new Error('请勿输入汉字！'));
        } else {
          callback();
        }
      }, 100);
    };
    return {
      //解决 element UI Notification弹出多个通知框时重叠问题
      notifyPromise: Promise.resolve(),
      mesFlag: '1', // 已读未读开关 flag
      circleUrl: '',
      username: sessionStorage.getItem('TuserName'),
      user_name_en: sessionStorage.getItem('user_en_name'),
      dialogFormVisible: false,
      form: {
        mantis_token: ''
      },
      mantis_token: '',
      tokenLabelWidth: '100px',
      user_id: '',
      rules: {
        mantis_token: [{ validator: checkMantisToken, trigger: 'blur' }]
      },
      userEnName: '' + sessionStorage.getItem('user_en_name'),
      userRole: sessionStorage.getItem('Trole'),
      dialogCardVisible: false,
      dialogNoticeTable: [],
      pageSize: 5,
      currentPage: 1,
      noticetotal: 0,
      //弹框
      dialogAdd: false,
      editFormVisible: false,
      caseDetails: false,
      copyDetails: false,
      caseDetailsAdd: false,
      caseEdit: false,
      formLabelWidth: '24%',
      labelWidth: '100px',
      labelPosition: 'right',
      //新增计划
      inputStartTime: '',
      inputEndTime: '',
      inputPlanName: '',
      deviceInfo: '',
      planWorkNo: '',
      startDatePicker: this.beginDate(),
      endDatePicker: this.processDate(),
      dialogNotice: false,
      message: '',
      detailExpiryTime: '',
      msgList: [],
      activeNames: [0],
      newMsgList: [],
      ws: '',
      classFlag: '',
      showAuto: false
    };
  },
  filters: {
    typeFilter(type) {
      if (type === 'announce-halt') {
        return '停机公告';
      } else {
        return '更新公告';
      }
    },
    nameFilter(name_en) {
      let user_name_en = name_en.replace(/^(t-|T-|csii_|c-|C-|c_|C_)/g, '');
      return user_name_en.split('')[0].toUpperCase();
    }
  },
  computed: {
    ...mapState('menu', ['userSitMsgList']),
    // 未读消息
    unreadSitMsg() {
      return this.userSitMsgList.filter(item => item.state === '');
    },
    // 展示的五条消息
    unreadToShow() {
      return this.unreadSitMsg.slice(0, 5);
    }
  },
  watch: {
    $route: {
      handler: function(to, from) {
        this.classFlag =
          this.$router.currentRoute.path.substring(1) === 'message';
      }
    },
    // 切换已读未读开关 请求对应的展示数据和总消息条数
    mesFlag(val) {
      this.queryMessageList(
        this.userEnName,
        this.userRole,
        this.pageSize,
        this.currentPage
      );
      this.queryMessageCount();
    }
  },
  methods: {
    ...mapActions('menu', ['getMessageUser']),
    handleSelect(key) {
      if (key === 'message') {
        this.$router.push('/message');
      } else if (key === 'notice') {
        this.messageNoc();
      }
    },
    handleLogout() {
      logout().then(res => {
        let tuiCheckedUpdateMenuWithUser = localStorage.getItem(
          `tuiCheckedUpdateMenuWithUser_${this.userEnName}`
        );
        sessionStorage.clear();
        localStorage.setItem(
          `tuiCheckedUpdateMenuWithUser_${this.userEnName}`,
          tuiCheckedUpdateMenuWithUser
        );
        this.$router.push('/login');
      });
    },
    queryNoticeList() {
      queryAnnounce().then(res => {});
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

    dateFormat(time) {
      var date = new Date(time);
      var year = date.getFullYear();

      var month =
        date.getMonth() + 1 < 10
          ? '0' + (date.getMonth() + 1)
          : date.getMonth() + 1;
      var day = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
      var hours =
        date.getHours() < 10 ? '0' + date.getHours() : date.getHours();
      var minutes =
        date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
      var seconds =
        date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
      return (
        year +
        '-' +
        month +
        '-' +
        day +
        ' ' +
        hours +
        ':' +
        minutes +
        ':' +
        seconds
      );
    },
    // 点击未读消息 更新消息状态
    async changeUnreadMsgState(item) {
      await updateNotifyStatus({ id: item.id });
      this.querySitMessage(); // 重新查询未读消息
      if (item.linkUri && item.linkUri.includes('sitMsg')) {
        const url = item.linkUri.substring(item.linkUri.indexOf('#') + 1);
        this.$router.push(url);
      }
      if (item.linkUri && item.linkUri.includes('MantisIssue')) {
        const url = item.linkUri.substring(item.linkUri.indexOf('#') + 14);
        this.$router.push({
          name: 'MantisIssue',
          query: { id: url }
        });
      }
    },
    // 更多未读消息 跳转到未读页面
    more() {
      this.$router.push('/Message');
    },
    async getWsMsg(event) {
      let msg = JSON.parse(event.data);
      if (!Array.isArray(msg)) {
        this.msgList.push(msg);
      } else {
        this.msgList = msg;
      }
      let announceList = this.msgList.filter(
        item => item.type === 'announce-update' || item.type === 'announce-halt'
      );

      let ids = localStorage.getItem('notices/announceIds')
        ? localStorage.getItem('notices/announceIds')
        : [];
      // 要显示的公告列表
      let copyMsgList = [];
      let newIds = [];
      announceList.forEach(item => {
        newIds.push(item.id);
        if (!Array.isArray(ids)) {
          ids = ids.split(',');
        }
        if (ids.indexOf(item.id) < 0) {
          ids.push(item.id);
          localStorage.setItem('notices/announceIds', ids);
          copyMsgList.push(item);
        }
      });
      // 当前时间currentDate
      let currentDate = this.dateFormat(new Date());
      let noOverTimeList = copyMsgList.filter(item => {
        return item.expiry_time > currentDate || !!item.expiry_time === false;
      });
      if (noOverTimeList.length > 0) {
        this.newMsgList = noOverTimeList;
        this.dialogNotice = true;
      }
      //重新调一下接口，查询通知，实时更新通知列表
      await this.queryNoticeList();
      // 筛选出新公告以后，将新的id列表存在本地。
      // localStorage.setItem("notices/announceIds", newIds);

      // 实时弹出用户的sit消息
      // 过滤出非公告、刷新的消息
      let sitMsgList = this.msgList.filter(
        item =>
          item.type !== 'announce-update' &&
          item.type !== 'announce-halt' &&
          item.type !== 'version-refresh'
      );

      sitMsgList.forEach(item => {
        let sitIds = localStorage.getItem('notices/sitIds') || '';
        let arrsitIds = sitIds.split(',');
        if (!sitIds || sitIds.indexOf(item.id) < 0) {
          arrsitIds.push(item.id);
          localStorage.setItem('notices/sitIds', arrsitIds);
          this.notifyPromise = this.notifyPromise.then(() => {
            const h = this.$createElement;
            this.$notify.info({
              title: `${item.type}`,
              message: h('p', { style: 'cursor: pointer' }, `${item.content}`),
              onClick: () => {
                this.changeUnreadMsgState(item);
              }
            });
          });
        }
      });
      let updateList = this.msgList.filter(
        item => item.type == 'version-refresh'
      );
      let i = 0;
      // updateList为空时不会报错
      updateList.forEach(item => {
        let updateIds = localStorage.getItem('notice/version-refresh') || '';
        i++;
        // 玉衡变更刷新 （同时变更好几次，只显示最新一次变更）
        if (updateList[0] && i === updateList.length) {
          if (updateIds.indexOf(updateList[updateList.length - 1].id) < 0) {
            const h = this.$createElement;
            this.$notify({
              title: `${item.content}`,
              message: h('p', { style: 'cursor: pointer' }, `刷新`),
              type: 'warning',
              duration: 0,
              showClose: false,
              onClick: () => {
                window.location.reload(true); // 刷新
              }
            });
          }
        }
        updateIds = updateIds.split(',');
        updateIds.push(item.id);
        localStorage.setItem('notice/version-refresh', updateIds);
      });
      //重新调一下接口，查询sit消息，实时更新sit消息列表
      this.querySitMessage();
    },
    // 查询sit消息
    async querySitMessage() {
      await this.getMessageUser({
        target: localStorage.getItem('user_en_name')
      });
    },
    closeNoticeDialog() {
      this.dialogNotice = false;
      window.location.reload(true); // 强制刷新
    },
    handleSizeChange: function(size) {
      this.pageSize = size;
      this.queryMessageList(
        this.userEnName,
        this.userRole,
        this.pageSize,
        this.currentPage
      );
    },
    // 通知分页
    handleCurrentChange(val) {
      this.queryMessageList(this.userEnName, this.userRole, this.pageSize, val);
    },
    //查询通知详情表单
    queryMessageList(user_en_name, userRole, pageSize, currentPage) {
      queryMessageRecord({
        user_en_name: user_en_name,
        pageSize: pageSize,
        currentPage: currentPage,
        userRole: userRole,
        messageFlag: this.mesFlag
      }).then(res => {
        this.dialogNoticeTable = res;
      });
    },
    handleAddPlan(row) {
      this.dialogAdd = true;
      this.planWorkNo = row.workNo;
    },
    messageNoc() {
      this.dialogCardVisible = true;
      this.queryMessageList(
        this.userEnName,
        this.userRole,
        this.pageSize,
        this.currentPage
      );
      this.queryMessageCount();
    },
    // 通知列表弹框 测试原因转译
    formatterTaskName(row, column) {
      if (row.taskReason === '1') {
        return '正常';
      } else if (row.taskReason === '2') {
        return '缺陷';
      } else if (row.taskReason === '3') {
        return '需求变更';
      } else {
        return '';
      }
    },
    // 通知列表弹框 测试原因转译
    formatterMessageFlag(row, column) {
      if (row.messageFlag === '1') {
        return '未读';
      } else {
        return '已读';
      }
    },
    //消息总数
    queryMessageCount() {
      queryMessageCountImpl({
        user_en_name: this.userEnName,
        userRole: this.userRole,
        messageFlag: this.mesFlag
        // (!this.userEnName)?this.userEnName:''
      }).then(res => {
        this.noticetotal = res.total;
        if (this.noticetotal > 0) {
          this.showNotice = true;
        }
      });
    },
    //消息忽略
    handleIgnore(index, row) {
      this.$confirm('此操作将忽略该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        ignoreMessageImpl({
          messageId: row.messageId
        })
          .then(res => {
            this.dialogNoticeTable.splice(index, 1);
            this.$message({
              type: 'success',
              message: '忽略成功!'
            });
            this.queryMessageList(
              this.userEnName,
              this.userRole,
              this.pageSize,
              this.currentPage
            );
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消忽略'
            });
          });
      });
    },
    //全部忽略
    handleAllIgnore() {
      this.$confirm('此操作将忽略全部文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        ignoreAllMessageImpl({
          user_en_name: this.userEnName
          //(!this.userEnName)?this.userEnName:''
        })
          .then(res => {
            this.dialogNoticeTable = [];
            this.$message({
              type: 'success',
              message: '忽略成功!'
            });
            this.queryMessageList(
              this.userEnName,
              this.userRole,
              this.pageSize,
              this.currentPage
            );
            this.showNotice = false;
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消忽略'
            });
          });
      });
    },

    CloseMantisDiolag() {
      this.dialogFormVisible = false;
      this.form.mantis_token = '';
    },

    cancelMantisDialog(form) {
      this.$refs[form].resetFields();
      this.dialogFormVisible = false;
      this.form.mantis_token = '';
    },

    async submiToken(form) {
      this.$refs[form].validate(valid => {
        if (valid) {
          this.dialogFormVisible = true;
          saveMantisToken({
            user_id: parseInt(this.user_id),
            mantis_token: this.form.mantis_token
          }).then(res => {
            this.$message({
              showClose: true,
              type: 'success',
              message: '提交成功!'
            });
            sessionStorage.setItem('mantisToken', this.form.mantis_token);

            this.$refs[form].resetFields();
            this.dialogFormVisible = false;
            this.form.mantis_token = '';
          });
        } else {
          return false;
        }
      });
    },
    // 新增计划弹框开始结束时间控制
    beginDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.inputEndTime) {
            //如果结束时间不为空，则小于结束时间
            return new Date(self.inputEndTime).getTime() <= time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    processDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.inputStartTime) {
            //如果开始时间不为空，则结束时间大于开始时间
            return (
              new Date(self.inputStartTime).getTime() - 86400000 >
              time.getTime()
            );
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    inputAddPlan(planName, inputStartTime, inputEndTime, deviceInfo) {
      if (inputStartTime > inputEndTime) {
        this.$message({
          showClose: true,
          message: '開始時間小於結束時間',
          type: 'error'
        });
        return;
      }

      addPlan({
        planName: planName,
        workNo: this.planWorkNo,
        planStartDate: inputStartTime,
        planEndDate: inputEndTime,
        deviceInfo: deviceInfo
      }).then(res => {
        this.dialogAdd = false;
        this.inputStartTime = '';
        this.inputEndTime = '';
        this.inputPlanName = '';
        this.deviceInfo = '';
      });
    }
  },
  mounted() {
    this.showAuto =
      sessionStorage.getItem('groupName') === '互联网应用-公共组-效能'
        ? true
        : false;
    sessionStorage.getItem('userId');
    this.user_id = sessionStorage.getItem('userId');
    this.queryNoticeList();
  },
  created() {
    this.classFlag = this.$router.currentRoute.path.substring(1) === 'message';
    this.querySitMessage();
    this.queryMessageList(
      this.userEnName,
      this.userRole,
      this.pageSize,
      this.currentPage
    );
    this.ws = new websocket(`tuser-${this.userEnName}`, this.getWsMsg);
  },
  beforeDestroy() {
    this.ws.onclose();
  }
};
</script>
<style scoped>
.middleContent {
  min-height: 100px;
  max-height: 300px;
  overflow: auto;
}
.noticeContent {
  min-height: 200px;
  max-height: 400px;
  overflow: auto;
}

.notify-time {
  text-align: right;
  padding-right: 15px;
  margin-top: 10px;
}
.dialogKnowBtn {
  border: 0px;
  color: #409eff;
  margin-left: 80%;
  margin-top: 10px;
}
.header {
  text-align: center;
  background: #545c64;
  height: 65px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
}
.el-tag {
  height: 35px;
  line-height: 35px;
  font-size: 13px;
  font-weight: 600;
  font-family: Microsoft Yahei, simHei, Times;
}
.logOut {
  font-size: 14px;
  font-weight: 500;
  font-family: Microsoft Yahei;
}
.bell-style {
  font-size: 22px;
  color: white;
}
.bell-notice {
  margin-top: -15px;
  margin-left: -5px;
}
.bell-button {
  margin-top: 8px;
}
.time-style {
  font-size: 12px;
  height: 18px;
  line-height: 18px;
  padding-bottom: 3px;
}
.content-style {
  padding-top: 6px;
  height: 24px;
  line-height: 24px;
  position: relative;
  padding-left: 14px;
}
.msg-style {
  font-size: 18px;
  color: green;
  position: absolute;
  top: 9px;
  left: -10px;
}
.username {
  padding-left: 20px;
  color: #fff;
}
.el-dropdown {
  color: #fff;
  float: left;
}
.el-icon-edit {
  color: #fff;
  font-size: 20px;
  float: right;
}
.el-menu-demo .el-menu-item {
  font-size: 15px;
}
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.abow_dialog >>> .el-dialog__header {
  background: #409eff;
  padding: 15px 20px 10px;
}
.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 18px;
  font-weight: 600;
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
.pagination >>> .el-pagination {
  float: right !important;
}
.button {
  background: #545c64 !important;
  border: #545c64 !important;
}

.pagination {
  margin-top: 3%;
  margin-bottom: 8%;
}
.messageSize {
  font-size: 22px !important;
}
.el-icon-question {
  font-size: 20px;
  margin-right: 10px;
}

.mantisTooltip {
  float: left;
  margin-top: 8px;
  margin-left: 10px;
}
.noprint {
  display: block;
}
.noprint >>> .el-submenu__title {
  display: flex;
  align-items: center;
}
.noticeCssDot >>> .el-badge__content.is-dot {
  margin-top: 5px;
  margin-left: 2px;
  border: 1px solid #544c64 !important;
}
.noticemessage >>> .el-submenu__title {
  border-bottom-color: rgb(84, 92, 100) !important;
}
.touchMessage >>> .el-submenu__title {
  border-bottom-color: rgb(255, 208, 75) !important;
}
.notTouchMessage >>> .el-submenu__title {
  border-bottom-color: rgb(84, 92, 100) !important;
  color: white !important;
}
.noticeColor {
  color: white !important;
}
.activeNoticeColor {
  color: rgb(255, 208, 75) !important;
}
.btn-float {
  float: left;
}
@media print {
  .noprint {
    display: none;
  }
  .print {
    display: block;
  }
}

.noticeTitle >>> .el-collapse-item__header {
  font-size: 16px;
  color: #027be3;
}
.mgr-50 {
  position: absolute;
  margin-left: 50px;
}
</style>
