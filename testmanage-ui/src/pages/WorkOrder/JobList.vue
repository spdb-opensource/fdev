<template>
  <div class="page-container">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item>
        <el-input
          v-model="formInline.taskName"
          placeholder="主任务名称"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
      </el-form-item>
    </el-form>

    <div class="card-container">
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in workList" :key="index">
          <el-card class="box-card" :body-style="{ padding: '20px 0 0' }">
            <div class="card-body">
              <el-row class="text-grey-1">
                <el-col :span="24">主任务名称</el-col>
              </el-row>
              <el-row class="text-grey-2">
                <el-col :span="24">{{ item.taskName }}</el-col>
              </el-row>
              <el-row class="text-grey-2">
                <el-col :span="7">需求编号/实施单元编号</el-col>
                <el-col :span="17">{{ item.No }}</el-col>
              </el-row>
              <el-row class="text-grey-2">
                <el-col :span="7">测试人员</el-col>
                <el-col :span="17">{{ item.tester | testerFilter }}</el-col>
              </el-row>
            </div>
            <div class="divider"></div>
            <el-row class="card-footer">
              <el-col :span="12">
                <el-tag type="warning">SIT</el-tag>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>
<script>
export default {
  name: 'JobList',
  data() {
    return {
      value1: [],
      formInline: {
        taskName: ''
      },
      workList: []
    };
  },
  filters: {
    testerFilter(users) {
      if (!users) {
        return;
      }
      let userNames = [];
      users.forEach(user => {
        userNames.push(user.name);
      });
      return userNames.join(',');
    }
  },
  methods: {
    onSubmit() {}
  },
  created() {}
};
</script>
<style scoped>
.title {
  font-size: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
}
.box-card {
  margin-top: 20px;
}
.card-body {
  padding: 0 20px 20px 20px;
}
.card-footer {
  padding: 10px 20px;
}
.card-container {
  margin-bottom: 20px;
}
</style>
