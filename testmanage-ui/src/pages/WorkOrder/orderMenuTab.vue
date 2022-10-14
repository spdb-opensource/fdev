<template>
  <el-row>
    <el-col :span="24">
      <div class="grid-content bg-purple-dark">
        <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
          <el-tab-pane
            label="我的工单"
            name="TestOrder"
            v-if="userRole >= 20"
          ></el-tab-pane>
          <el-tab-pane
            label="历史工单"
            name="HistoryOrder"
            v-if="userRole >= 20"
          ></el-tab-pane>
          <el-tab-pane label="工单查询" name="QueryOrder"></el-tab-pane>
          <el-tab-pane
            label="审批工单"
            name="ApprovalOrder"
            v-if="showAssessorItem() || userRole == 50"
          ></el-tab-pane>
          <el-tab-pane
            label="废弃工单"
            name="WasteOrder"
            v-if="userRole >= 20"
          ></el-tab-pane>
        </el-tabs>
      </div>
    </el-col>
  </el-row>
</template>
<script>
export default {
  name: 'orderMenuTab',
  data() {
    return {
      userRole: sessionStorage.getItem('Trole'),
      isAssessor: sessionStorage.getItem('isAssessor'),
      activeName: this.$route.name
    };
  },
  methods: {
    handleClick(tab, event) {
      this.$router.push({ path: `/${tab.name}` });
    },
    showAssessorItem() {
      let userInfor = JSON.parse(sessionStorage.getItem('userInfo'));
      return userInfor.role.some(item => {
        return item.name === '玉衡-审批人员';
      });
    }
  }
};
</script>
