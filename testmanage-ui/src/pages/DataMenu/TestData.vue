<template>
  <div class="page-container">
    <el-row>
      <el-col :span="7" :offset="1">
        <el-card class="box-card" shadow="always">
          <div slot="header" class="clearfix">
            <el-link href="xxx" target="_blank">
              <span class="headerCss">ESB地址</span>
            </el-link>
          </div>
          <el-link
            type="primary"
            icon="el-icon-link"
            style="margin-bottom:7px"
            @click="ESBOL"
            >使用说明</el-link
          >
        </el-card>
      </el-col>
      <el-col :span="7" :offset="1">
        <el-card class="box-card" shadow="always">
          <div slot="header" class="clearfix">
            <el-link
              href="xxx/serviceGovOut/WebIndex.html"
              target="_blank"
            >
              <span class="headerCss">ESB对外平台</span>
            </el-link>
          </div>
          <div class="text item"></div>
        </el-card>
      </el-col>
      <el-col :span="7" :offset="1">
        <el-card class="box-card" shadow="always">
          <div slot="header" class="clearfix">
            <el-link href="xxx" target="_blank">
              <span class="headerCss">信用卡中心</span>
            </el-link>
          </div>
          <el-link
            type="primary"
            icon="el-icon-link"
            style="margin-bottom:7px"
            @click="creditCardOL"
            >使用说明</el-link
          >
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'TestData',

  data() {
    return {};
  },
  methods: {
    creditCardOL() {
      this.$router.push('/CreditCardOL');
    },
    ESBOL() {
      this.$router.push('/ESBOL');
    }
  }
};
</script>

<style scoped>
.page-container {
  padding-top: 40px;
}
.headerCss {
  font-size: 16px;
  font-weight: 600;
}
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: '';
}
.clearfix:after {
  clear: both;
}

.box-card {
  width: 280px;
  border: gainsboro solid 1px;
}
.el-card__header {
  border-bottom: 1px solid gainsboro;
}
.box-card >>> .el-card__body {
  height: 40px !important;
}
</style>
