<template>
  <f-block>
    <Loading :visible="UATtestResultLoading">
      <!-- 当前缺陷汇总展示 -->
      <div>
        <f-icon
          name="log_s_f"
          class="text-primary"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">当前任务缺陷情况</span>
      </div>
      <div>
        <div class="row item-start container-div">
          <div class="section-div">
            <div class="card-div">{{ UATnumtList.newIssueNum }}</div>
            <fdev-badge class="newCreated textBade">新建</fdev-badge>
          </div>
          <fdev-separator vertical />
          <div class="section-div">
            <div class="card-div">{{ UATnumtList.openIssueNum }}</div>
            <fdev-badge class="opened textBade">打开</fdev-badge>
          </div>
          <fdev-separator vertical />
          <div class="section-div">
            <div class="card-div">{{ UATnumtList.reopenIssueNum }}</div>
            <fdev-badge class="reOpened textBade">重新打开</fdev-badge>
          </div>
          <fdev-separator vertical />
          <div class="section-div">
            <div class="card-div">{{ UATnumtList.delayIssueNum }}</div>
            <fdev-badge class="delayedRep textBade">延迟修复</fdev-badge>
          </div>
          <fdev-separator vertical />
          <div class="section-div">
            <div class="card-div">{{ UATnumtList.leaOvIssueNum }}</div>
            <fdev-badge class="remained textBade">遗留</fdev-badge>
          </div>
          <fdev-separator vertical />
          <div
            class="section-div count-div"
            style="flex: initial;
    width: 200px;"
          >
            <div class="left-div">
              <div class="card-div">{{ UATnumtList.waitIssueNum }}</div>
              <fdev-badge class="waitConfirm textBade">待确认</fdev-badge>
            </div>
            <fdev-separator vertical class="slantSep" />
            <div class="right-div">
              <div class="right-item color-reject">
                拒绝
                <span class="card-span">{{ UATnumtList.refuseIssueNum }}</span>
              </div>
              <div class="right-item color-success">
                已修复
                <span class="card-span">{{ UATnumtList.repairIssueNum }}</span>
              </div>
            </div>
          </div>
          <fdev-separator vertical />
          <div
            class="section-div count-div"
            style="flex: initial;
    width: 200px;"
          >
            <div class="left-div">
              <div class="card-div">{{ UATnumtList.closedIssueNum }}</div>
              <fdev-badge class="closed textBade">已关闭</fdev-badge>
            </div>
            <fdev-separator vertical class="slantSep" />
            <div class="right-div">
              <div class="right-item color-close paddingCtrol">
                关闭
                <span class="card-span">{{ UATnumtList.closeIssueNum }}</span>
              </div>
              <div class="right-item color-close">
                确认拒绝
                <span class="card-span">{{ UATnumtList.affRefIssueNum }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 缺陷列表的展示 -->
      <div class="q-mt-llg">
        <UATDefect
          :jobName="taskName"
          :defectList="UATdefectList"
          :isLoginUserList="isLoginUserList"
          @update-status="updateUATStatus"
        />
      </div>
    </Loading>
  </f-block>
</template>
<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import Loading from '@/components/Loading';
import UATDefect from '@/components/Defect/UATDefect';
export default {
  name: 'uATtestResult',
  components: {
    UATDefect,
    Loading
  },
  data() {
    return {
      UATtestResultLoading: false
      // UATnumtList: {
      //   newIssueNum: 0,
      //   openIssueNum: 0,
      //   reopenIssueNum: 0,
      //   delayIssueNum: 0,
      //   leaOvIssueNum: 0,
      //   waitIssueNum: 0,
      //   refuseIssueNum: 0,
      //   repairIssueNum: 0,
      //   affRefIssueNum: 0,
      //   closedIssueNum: 0,
      //   closeIssueNum: 0
      // }
    };
  },
  props: {
    taskId: String,
    taskName: String
  },
  computed: {
    ...mapState('jobForm', ['UATnumtList', 'UATdefectList']),
    ...mapGetters('user', ['isLoginUserList'])
  },
  created() {
    this.init();
  },
  methods: {
    ...mapActions('jobForm', ['queryJiraIssues']),
    async init() {
      try {
        await this.queryJiraIssues({ taskId: this.taskId });
        this.UATtestResultLoading = false;
      } catch (e) {
        this.UATtestResultLoading = false;
      }
    },
    async updateUATStatus(defect) {
      await this.queryJiraIssues({ taskId: this.taskId });
      this.init();
    }
  }
};
</script>
<style lang="stylus" scoped>
.infoStyle {
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight: 600;
  margin-left: 8px;
}

.card-span {
  font-weight: 600;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  text-align: center;
  line-height: 22px;
  padding-left: 8px;
}

.container-div {
  margin-left: 0;
  margin-top: 32px;
}

.color-reject {
  font-weight: 600;
  font-size: 12px;
  color: #F46865;
  letter-spacing: 0;
  line-height: 12px;
  padding-top: 4px;
  padding-bottom: 4px;
}

.color-success {
  font-weight: 600;
  font-size: 12px;
  color: #4DBB59;
  letter-spacing: 0;
  line-height: 12px;
}

.color-close {
  font-weight: 600;
  font-size: 12px;
  color: #A8AFBE;
  letter-spacing: 0;
  line-height: 12px;
}

.paddingCtrol {
  padding-top: 4px;
  padding-bottom: 4px;
}

.count-div {
  display: flex;
}

.count-div > div {
  flex: 1;
}

.right-div {
  margin-right: 20px;
}

.slantSep {
  height: 52px;
}

.slantSep.q-separator--vertical {
  background: #DDDDDD;
  transform: rotate(25deg);
  width: 1px;
  margin-left: 8px;
}

.right-div > .right-item {
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0;
  line-height: 12px;
  width: 70px;
  text-align: right;
}

.section-div {
  flex: 1;
  text-align: center;
}

.left-div {
  margin-left: 20px;
  align-self: flex-start;
}

.card-div {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  letter-spacing: 0;
  text-align: center;
  line-height: 22px;
  margin-bottom: 8px;
}

.newCreated {
  background: rgba(77, 187, 89, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 40px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #4DBB59;
  letter-spacing: 0;
  line-height: 12px;
}

.opened {
  background: rgba(3, 120, 234, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 40px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #0378EA;
  letter-spacing: 0;
  line-height: 12px;
}

.reOpened {
  background: rgba(3, 120, 234, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 64px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #0378EA;
  letter-spacing: 0;
  line-height: 12px;
}

.delayedRep {
  background: rgba(253, 141, 0, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 64px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #FD8D00;
  letter-spacing: 0;
  line-height: 12px;
}

.remained {
  background: rgba(74, 102, 219, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 40px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #4A66DB;
  letter-spacing: 0;
  line-height: 12px;
}

.waitConfirm {
  background: rgba(244, 104, 101, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 52px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #F46865;
  letter-spacing: 0;
  line-height: 12px;
}

.closed {
  background: #A8AFBE;
  border-radius: 2px;
  border-radius: 2px;
  width: 52px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #FFFFFF;
  letter-spacing: 0;
  line-height: 12px;
}
.textBade {
  display: inline-grid;
}
.textBade.q-badge {
  padding: 0px 0px;
}
</style>
