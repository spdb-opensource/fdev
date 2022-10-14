<template>
  <div>
    <el-header>
      <Header />
    </el-header>
    <div class="scrollbarSet">
      <el-container class="container">
        <router-view />
      </el-container>
    </div>
  </div>
</template>
<script>
import Header from './Header';
export default {
  name: 'basicLayout',
  components: { Header },
  data() {
    return {};
  }
};
</script>
<style scoped>
.container {
  text-align: center;
  width: 100%;
  margin: 20px auto;
}
.scrollbarSet {
  position: absolute;
  top: 65px;
  bottom: 25px;
  left: 0;
  width: 100%;
  overflow-y: auto;
}
</style>
