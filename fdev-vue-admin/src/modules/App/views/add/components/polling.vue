<template>
  <div class="polling">
    <fdev-circular-progress
      show-value
      class="q-ma-md"
      :value="progress"
      size="120px"
      color="light-blue"
      :max="1"
      track-color="grey-3"
      v-show="progress !== 1"
    >
      <span v-if="addAppStep.status === '1'">
        {{ parseInt(progress * 100) }}%
      </span>
      <fdev-icon
        name="error"
        color="red"
        size="50px"
        v-if="addAppStep.status === '2'"
      />
    </fdev-circular-progress>
    <div>
      <fdev-icon
        name="check_circle"
        v-if="progress === 1"
        color="positive"
        size="86px"
      />
      <div class="title text-title">
        {{ addAppStep.status | type }}
      </div>
      <div class="description">
        {{ addAppStep.cur_msg }}
      </div>
      <div
        class="actions"
        v-if="progress === 1 && appData.appCiType === 'fdev-ci'"
      >
        <div class="q-mb-md">
          请先配置流水线，否则不能进行持续集成，点击去配置按钮
        </div>
        <div class="row justify-center q-gutter-x-sm">
          <fdev-btn
            color="primary"
            @click="toAddPipeline(addAppStep.app_id)"
            label="去配置"
          />
        </div>
      </div>
      <div
        class="actions"
        v-if="progress === 1 && appData.appCiType === 'git-ci'"
      >
        <div class="q-mb-md" v-if="!examLabel">
          您的应用尚未绑定过部署信息，请移步到环境配置管理下的部署信息进行绑定！
        </div>
        <div class="row justify-center q-gutter-x-md">
          <fdev-btn
            @click="toDeployMessage(addAppStep.app_id)"
            label="去绑定"
            v-if="!examLabel"
          />
          <fdev-btn dialog to="/app/list" label="返回列表" v-if="examLabel" />
          <fdev-btn
            :to="`/app/list/${addAppStep.app_id}`"
            label="查看应用"
            v-if="examLabel"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import websocket from '@/utils/socket';

export default {
  name: 'Polling',
  props: {
    label: Array,
    appData: Object,
    appId: Object
  },
  data() {
    return {
      stepNow: 1,
      progress: 0,
      addAppStep: {},
      ws: {}
    };
  },
  watch: {
    'addAppStep.rate_progress': {
      handler(val) {
        this.progress = Number(val);
      }
    },
    'addAppStep.status'(val) {
      if (val === '2' || val === '0') {
        this.closeWebsocket();
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    iconName() {
      if (this.addAppStep.status === '0') {
        return 'check';
      } else if (this.addAppStep.status === '1') {
        return 'assignment';
      } else {
        return 'error';
      }
    },
    examLabel() {
      return this.label.indexOf('不涉及环境部署') > -1;
    }
  },
  filters: {
    type(val) {
      if (val === '0') {
        return '新增成功';
      } else if (val === '1') {
        return '新增应用中……';
      } else if (val === '2') {
        return '新增失败';
      } else {
        return '新增开始';
      }
    }
  },
  methods: {
    start() {
      this.progress = 0;
      this.ws = new websocket(
        `fapp-${this.currentUser.user_name_en}/${new Date().valueOf()}`,
        this.getWsApp
      );
    },
    getWsApp(event) {
      this.addAppStep = JSON.parse(JSON.parse(event.data).content);
    },
    closeWebsocket() {
      this.ws.onclose();
    },
    toAddPipeline(appId) {
      this.$router.push({
        path: `/app/list/${appId}`
      });
    },
    toDeployMessage(appId) {
      this.$router.push({
        path: '/envModel/handle',
        query: {
          appId
        }
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';
.polling
  text-align: center;
  margin-bottom: 24px;
  width: 72%;
  margin: 0 auto;
  @media screen and (max-width: $sizes.sm) {
    width: 100%;
  }
  .title
    font-size: 24px;
    font-weight: 500;
    line-height: 32px;
    margin-bottom: 16px;
  .description
    font-size: 14px;
    line-height: 22px;
    color: $grey-7;
    margin-bottom: 24px;
  .extra
    background: #fafafa;
    padding: 24px 40px;
    border-radius: $generic-border-radius;
    text-align: left;
    @media screen and (max-width: $sizes.sm)
      padding: 18px 20px;
  .actions
    margin-top: 32px;
</style>
