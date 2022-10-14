<template>
  <div class="page-container">
    <div class="headerCSS">
      <span>ESB操作说明</span>
      <el-tooltip content="返回" placement="bottom" effect="light">
        <el-button
          icon="el-icon-refresh-left"
          type="primary"
          circle
          @click="goBack"
          class="goBackBtn"
        ></el-button>
      </el-tooltip>
    </div>
    <div class="block">
      <div class="chapterOne">
        <span
          >页面中间的显示的IP为地址查询，流水查询界面所对应的ESB环境。日志查询界面有
          专用的ESB环境选择器</span
        >
      </div>
      <div class="chapterTwo">
        <span
          >日志查询界面：输入要查询的交易码，选择需要查询
          ESB环境，以及前端ESB日志（ESB接收前台的日志和返回前台的日志）和后端ESB日志（ESB传给后端系统
          的日志和ESB收到后端系统响应的日志）</span
        >
      </div>
      <div class="chapterThree">
        <span>
          流水查询界面：分为当天流水查询和历史流水查询
          <br />当天流水查询：最大显示交易量1000条，精确查询请填写流水号等相关信息
          <br />历史流水查询：同上，由于ESB历史流水记录较多，查询相对当天流水要慢，请谅解
        </span>
      </div>
      <div class="chapterTwo">
        <span>
          地址查询叶签分为：接口查询，服务级别地址查询以及系统级别地址查询
          <br />接口查询：可以查询到当前所有在ESB注册过的服务，服务的调用方，提供方，以及ESB
          需求接口人员信息等，当前页面
          查询到的服务ID，交易码可以用于服务级别地址查询中使用
          <br />服务级别地址查询：由于ESB是根据服务配置后段地址，所以查询ESB配置的后台需要用到
          服务ID，请从接口查询里获取 到交易码对应得服务ID，再进行查询
          <br />系统级别地址查询：此叶签可以获取当前ESB环境上所对应的后端系统地址
        </span>
      </div>
      <div class="chapterThree">
        <span
          >常用链接叶签：提供wsdl,mapfile链接以及文件传输，系统联系人等相关查询。</span
        >
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ESBOL',

  data() {
    return {};
  },
  methods: {
    goBack() {
      this.$router.push('/TestData');
    }
  }
};
</script>

<style scoped>
.headerCSS {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 30px;
}
.chapterOne {
  color: red;
  margin-top: 20px;
}
.chapterTwo {
  color: green;
  margin-top: 20px;
}
.chapterThree {
  color: blue;
  margin-top: 20px;
}
.goBackBtn {
  float: right;
}
</style>
