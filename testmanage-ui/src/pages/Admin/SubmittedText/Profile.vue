<template>
  <div class="SubmittedTextProfile" v-loading="loading">
    <h5 class="title">
      {{ taskMsg.name }}
      <div v-if="taskMsg.stage" class="title-tag" :class="tagType">
        {{ taskMsg.stage }}
      </div>
    </h5>
    <p class="rqrNo" v-if="taskMsg.demand">
      需求编号/需求名称：{{ taskMsg.demand.oa_contact_no }}/{{
        taskMsg.demand.oa_contact_name
      }}
    </p>

    <h6 class="small-title">任务详情</h6>

    <el-row class="margin-top">
      <el-col :span="8">
        实施单元: {{ taskMsg.fdev_implement_unit_no || '-' }}
      </el-col>
      <el-col :span="8">
        所属小组:
        <span v-if="taskMsg.group"
          >{{ taskMsg.group.name || '-' }}
        </span></el-col
      >
      <el-col :span="8">
        任务创建人:<span v-if="taskMsg.creator">{{
          taskMsg.creator.user_name_cn || '-'
        }}</span>
      </el-col>
    </el-row>

    <el-row class="margin-top">
      <el-col :span="8">
        任务负责人:
        <span v-for="(user, index) in taskMsg.master" :key="index">
          {{ user.user_name_cn }}</span
        >
      </el-col>
      <el-col :span="8">
        行内项目负责人:<span
          v-for="(user, index) in taskMsg.spdb_master"
          :key="index"
        >
          {{ user.user_name_cn }}</span
        >
      </el-col>
      <el-col :span="8">
        开发人员:<span v-for="(user, index) in taskMsg.developer" :key="index">
          {{ user.user_name_cn }}</span
        >
      </el-col>
    </el-row>

    <el-row class="margin-top">
      <el-col :span="8">
        测试人员:
        <span v-for="(user, index) in taskMsg.tester" :key="index">
          {{ user.user_name_cn }}</span
        >
      </el-col>
      <el-col :span="8">
        UAT承接方: {{ taskMsg.uat_testObject || '-' }}
      </el-col>
      <el-col :span="8"> 所属应用: {{ taskMsg.project_name || '-' }} </el-col>
    </el-row>

    <el-row class="margin-top">
      <el-col :span="8"> 分支: {{ taskMsg.feature_branch || '-' }} </el-col>
      <el-col :span="8"> 投产意向窗口: {{ taskMsg.proWantWindow }} </el-col>
    </el-row>
    <el-divider></el-divider>
    <div class="order-div">
      <h5 class="title order-submit">提测信息</h5>
    </div>
    <el-row class="margin-top">
      <el-col :span="8"> 提测日期: {{ submitMsg.createTime }} </el-col>
      <el-col :span="8"> 测试原因: {{ submitMsg.testReason }} </el-col>
      <el-col :span="8">
        涉及关联系统同步改造: {{ submitMsg.otherSystemChange || '-' }}
      </el-col>
    </el-row>
    <el-row class="margin-top">
      <el-col :span="8">
        是否涉及数据库改动: {{ submitMsg.databaseChange }}
      </el-col>
      <el-col :span="8">
        客户端版本: <span v-html="logFilter(submitMsg.clientVersion)" />
      </el-col>
    </el-row>

    <h6 class="small-title margin-top">功能描述</h6>
    <p class="margin-top" v-html="logFilter(submitMsg.repairDesc)" />

    <h6 class="small-title margin-top">测试环境</h6>
    <p class="margin-top" v-html="logFilter(submitMsg.testEnv)" />

    <h6 class="small-title margin-top">回归测试范围</h6>
    <p class="margin-top" v-html="logFilter(submitMsg.regressionTestScope)" />

    <h6 class="small-title margin-top">是否涉及交易接口改动</h6>
    <p class="margin-top" v-html="logFilter(submitMsg.interfaceChange)" />

    <el-divider></el-divider>
    <div class="order-div">
      <div @click.stop="exePlan(orderNoDetail)">
        <h5 class="title order">工单详情</h5>
      </div>
    </div>
    <el-row class="margin-top">
      <el-col :span="8">
        工单名称: {{ orderNoDetail.mainTaskName || '-' }}
      </el-col>
      <el-col :span="8">
        工单编号: {{ orderNoDetail.workOrderNo || '-' }}
      </el-col>
      <el-col :span="8">
        测试阶段: {{ testStage[orderNoDetail.stage] }}
      </el-col>
    </el-row>
    <el-row class="margin-top">
      <el-col :span="8"> 需求名称: {{ orderNoDetail.demandName }} </el-col>
      <el-col :span="8"> 需求编号: {{ orderNoDetail.demandNo || '-' }} </el-col>
      <el-col :span="8"> 实施单元: {{ orderNoDetail.unit || '-' }} </el-col>
    </el-row>
    <el-row class="margin-top">
      <el-col :span="8">
        工单负责人: {{ orderNoDetail.workManager || '-' }}
      </el-col>
      <el-col :span="8">
        测试小组长: {{ orderNoDetail.groupLeader || '-' }}
      </el-col>
      <el-col :span="8"> 测试人员: {{ orderNoDetail.testers || '-' }} </el-col>
    </el-row>
    <el-row class="margin-top">
      <el-col :span="8">
        计划SIT开始时间: {{ orderNoDetail.planSitDate || '-' }}
      </el-col>
      <el-col :span="8">
        计划UAT开始时间: {{ orderNoDetail.planUatDate }}
      </el-col>
      <el-col :span="8">
        计划投产开始时间: {{ orderNoDetail.planProDate || '-' }}
      </el-col>
    </el-row>
    <el-row class="margin-top">
      <el-col :span="8"> 备注: {{ orderNoDetail.remark || '-' }} </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { groupName, testStage } from '../model';
export default {
  name: 'SubmittedTextProfile',
  data() {
    return {
      loading: false,
      submitMsg: [],
      taskMsg: [],
      testStage
    };
  },
  props: {
    id: String
  },
  watch: {
    id(val) {
      this.init();
    }
  },
  computed: {
    ...mapState('adminForm', ['sitMsgDetail', 'orderNoDetail']),
    tagType() {
      if (this.sitMsgDetail.testReason === '缺陷') return 'Danger';
      if (this.sitMsgDetail.testReason === '需求变更') return 'Warning';
      return 'Primary';
    }
  },
  methods: {
    ...mapActions('adminForm', ['querySitMsgDetail', 'queryOrderByNo']),
    logFilter(val) {
      val = val ? val : '-';
      return val
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    async init() {
      this.loading = true;
      await this.querySitMsgDetail({ id: this.id });
      this.submitMsg = this.sitMsgDetail.submitInfo;
      this.taskMsg = this.sitMsgDetail.taskInfo;
      await this.queryOrderByNo({ workNo: this.submitMsg.workNo });
      this.loading = false;
    },
    /**
     * 跳转执行计划
     * */
    exePlan(item) {
      this.$router.push({
        name: 'QueryOrder',
        query: {
          workOrderNo: item.workOrderNo,
          mainTaskName: item.mainTaskName
        }
      });
      const workOrderNoData = {
        stage: item.stage,
        workOrderNo: item.workOrderNo,
        testers: item.testers,
        mainTaskName: item.mainTaskName,
        unitNo: item.unit,
        planPower: '',
        workNo: item.workOrderNo
      };
      sessionStorage.setItem(
        'planWorkOrderNo',
        JSON.stringify(workOrderNoData)
      );
    }
  },
  created() {
    this.init();
  },
  filters: {
    groupName(val) {
      return val ? groupName[val] : '-';
    }
  }
};
</script>

<style scope>
.SubmittedTextProfile {
  width: 80%;
  margin: 0 auto;
  text-align: left;
  font-size: 17.5px;
}
.margin-top {
  padding-left: 16px;
  font-size: 14px;
  margin-top: 16px;
  word-break: break-all;
}
.margin-bottom {
  margin-bottom: 16px;
}
.title {
  font-size: 0.9em;
  font-weight: 500;
  line-height: 2em;
  letter-spacing: 0.0125em;
  margin: 0;
  margin-bottom: 8px;
  color: black;
}
.small-title {
  font-weight: 700;
  margin: 0;
  margin-top: 16px;
  font-size: 14px;
  color: black;
}
.title-tag {
  font-size: 12px;
  display: inline-block;
  font-weight: 500;
  vertical-align: middle;
  padding: 0 6px;
  color: white;
  line-height: 20px;
  height: 20px;
  border-radius: 5px;
}
.Danger {
  background: #f56c6c;
}
.Warning {
  background: #e6a23c;
}
.Primary {
  background: #409eff;
}
.rqrNo {
  font-size: 14px;
  margin: 0;
  margin-bottom: 16px;
}
.icon {
  color: yellow;
}
.order-div {
  display: flex;
  flex-direction: row;
}
.order {
  color: #66b1ff;
  cursor: pointer;
}
.order-submit {
  color: #66b1ff;
}
</style>
