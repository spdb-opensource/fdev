<template>
  <div class="bg-div">
    <el-container class="bg-container">
      <el-header class="header-style">
        SIT测试消息
      </el-header>
      <el-main class="main-style">
        <el-tabs
          v-model="activeName"
          @tab-click="handleClick"
          type="border-card"
        >
          <el-tab-pane label="未读消息" name="first">
            <MsgTable
              :tables="unreadSitMsg"
              :type="type"
              :msgTypeList="msgTypeList"
              @redMsgs="redMsgs"
              @changeMsgState="changeMsgState"
            />
          </el-tab-pane>
          <el-tab-pane label="已读消息" name="second">
            <MsgTable :tables="readSitMsg" />
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </div>
</template>
<script>
import { mapActions, mapState } from 'vuex';
import MsgTable from '@/components/MsgTable';

export default {
  name: 'Message',
  components: { MsgTable },
  data() {
    return {
      activeName: 'first',
      type: 'unread'
    };
  },
  computed: {
    ...mapState('menu', ['userSitMsgList']),
    unreadSitMsg() {
      return this.userSitMsgList.filter(item => item.state === '');
    },
    readSitMsg() {
      return this.userSitMsgList.filter(item => item.state === '1');
    },
    msgTypeList() {
      let types = this.userSitMsgList
        .filter(item => {
          return item.state === '';
        })
        .map(item => item.type);
      types = Array.from(new Set(types));
      return types;
    }
  },
  methods: {
    ...mapActions('menu', ['getMessageUser']),
    ...mapActions('adminForm', [
      'updateNotifyStatus',
      'batchUpdateNotiftStatus'
    ]),
    handleClick(tab, event) {},
    async changeMsgState(id) {
      await this.updateNotifyStatus({ id });
      this.querySitMessage(); // 重新查询sit消息
    },
    async querySitMessage() {
      // 查询sit消息
      await this.getMessageUser({
        target: localStorage.getItem('user_en_name')
      });
    },
    async redMsgs(ids) {
      await this.batchUpdateNotiftStatus({ ids });
      this.$message({
        type: 'success',
        message: '一键已读成功'
      });
      this.querySitMessage();
    }
  },
  created() {
    this.querySitMessage(); // 查询sit消息
  }
};
</script>

<style scoped>
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
.header-style {
  margin-left: 20px;
  text-align: center;
}
.main-style {
  padding: 20px;
}
</style>
