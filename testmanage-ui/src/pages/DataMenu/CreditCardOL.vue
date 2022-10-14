<template>
  <div class="page-container">
    <div class="headerCSS">
      <span>信用卡操作说明</span>
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
      <div>
        <span class="demonstration">1、直接输入账户，不需要密码</span>
      </div>
      <el-image class="imageCss" :src="step1URL"></el-image>
      <div>
        <span class="demonstration">2、选UAT环境</span>
      </div>
      <el-image class="imageCss" :src="step2URL"></el-image>
      <div>
        <span class="demonstration"
          >3、可用的各种操作可以自己选择，这次新增的为“状态重置”在左侧列表里输入要重置的证件号（如果其它操作填卡号），右侧会提示操作结果：</span
        >
      </div>
      <el-image class="imageCss" :src="step3URL"></el-image>
    </div>
  </div>
</template>

<script>
import step1 from '../../assets/one.png';
import step2 from '../../assets/two.png';
import step3 from '../../assets/three.png';
export default {
  name: 'CreditCardOL',

  data() {
    return {
      step1URL: step1,
      step2URL: step2,
      step3URL: step3
    };
  },
  methods: {
    goBack() {
      this.$router.push('/TestData');
    }
  }
};
</script>

<style scoped>
.imageCss {
  width: 800px;
  height: 400px;
  padding-top: 10px;
  padding-bottom: 40px;
}
.headerCSS {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 30px;
}
.block {
  text-align: center;
}
.goBackBtn {
  float: right;
}
</style>
