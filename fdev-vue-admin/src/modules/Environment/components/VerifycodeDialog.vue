<template>
  <f-dialog v-model="dialogOpen" :title="description" @before-show="beforeShow">
    <fdev-card class="min-width">
      <fdev-card-section class="q-mt-md">
        <!-- 要引入验证码组件 -->
        <input
          type="text"
          class="verify-input"
          placeholder="请输入验证码"
          v-model="inputVerifyCode"
        />
        <VerifycationCode
          class="verifycode"
          @click-change="canvasClick"
          :verifyCode="verifyCode"
        ></VerifycationCode>
      </fdev-card-section>
    </fdev-card>
    <template v-slot:btnSlot>
      <fdev-btn label="关闭" outline dialog @click="closeDialog" />
      <fdev-btn dialog label="确认" @click="handleDelete" :loading="loading" />
    </template>
  </f-dialog>
</template>
<script>
import { errorNotify } from '@/utils/utils';
import VerifycationCode from '@/components/VerifycationCode';
import { mapState, mapActions } from 'vuex';
export default {
  components: {
    VerifycationCode
  },
  data() {
    return {
      inputVerifyCode: '' // 用户输入的验证码
    };
  },
  props: {
    description: {
      type: String,
      default: ''
    },
    value: {
      type: Boolean,
      default: () => false
    },
    loading: {
      type: Boolean,
      default: () => false
    }
  },
  // props: ['description'],
  computed: {
    ...mapState('environmentForm', {
      verifyCode: 'verifyCode' // 验证码
    }),
    // 弹窗打开或关闭
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getVerifyCode: 'getVerifyCode' // 获取验证码
    }),
    // 点击换一个 重新获取验证码
    async canvasClick() {
      await this.getVerifyCode();
      this.inputVerifyCode = '';
    },
    // 删除实体
    async handleDelete() {
      if (
        this.verifyCode !== '' &&
        this.verifyCode.toLowerCase() === this.inputVerifyCode.toLowerCase()
      ) {
        // 验证码正确 提交父组件删除事件
        this.$emit('handleDelete', this.inputVerifyCode);
      } else {
        errorNotify('验证码错误');
        await this.getVerifyCode();
      }
      this.inputVerifyCode = '';
    },
    // 关闭弹窗
    closeDialog() {
      this.dialogOpen = false;
    },
    beforeShow() {
      this.getVerifyCode();
    }
  }
  // created() {
  //   this.getVerifyCode();
  // }
};
</script>
<style lang="stylus" scoped>
.min-width
  min-with 300px
.verify-input
  display inline-block
  width 116px
  height 32px
  padding 2px;
  text-indent 2px;
  font-size 14px;
  line-height 1.5;
  color #495057;
  background-color #fff;
  background-clip padding-box;
  border 1px solid #ced4da;
  border-radius 2px;
  transition border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  &:focus
    color #495057;
    background-color #fff;
    border-color #80bdff;
    outline 0;
    box-shadow 0 0 0 2px rgba(0,123,255,.25);
.verifycode
  display inline-block
  vertical-align middle
</style>
