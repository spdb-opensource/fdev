<template>
  <div class="UserProfile">
    <h5 class="title">
      {{ userInfo.user_name_cn }}
    </h5>
    <p>{{ userInfo.email }}</p>
    <el-divider />
    <el-row>
      <el-col :span="8">
        公司：{{ userInfo.company ? userInfo.company.name : '' || '-' }}
      </el-col>
      <el-col :span="8">
        小组：{{ userInfo.group ? userInfo.group.name : '' }}
      </el-col>
      <el-col :span="8">
        角色:
        <el-tag class="tag-margin" v-for="role in userInfo.role" :key="role.id">
          {{ role.name }}
        </el-tag>
      </el-col>
    </el-row>
    <el-row class="margin-top">
      <el-col :span="8"> 级别：{{ userInfo.ftms_level || '' }} </el-col>
      <el-col :span="8"> 手机号：{{ userInfo.telephone || '-' }} </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      userInfo: {}
    };
  },
  computed: {},
  methods: {},
  created() {
    this.userInfo = this.$route.query.userInfo;
  }
};
</script>

<style scope>
.UserProfile {
  width: 80%;
  margin: 0 auto;
  text-align: left;
}
.title {
  font-size: 25px;
  text-align: left;
  margin: 0;
}
.margin-top {
  margin-top: 15px;
}
.tag-margin {
  margin-left: 8px;
}
</style>
