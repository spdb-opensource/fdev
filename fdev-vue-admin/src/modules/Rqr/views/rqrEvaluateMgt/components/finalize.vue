<template>
  <div>
    <!-- 修改定稿日期弹窗 -->
    <f-dialog
      title="请填写定稿信息"
      v-model="open"
      @before-close="open = false"
    >
      <!-- 定稿日期 -->
      <f-formitem
        label="定稿日期"
        label-style="width: 112px"
        class="q-mt-lg"
        required
      >
        <f-date
          v-model="finalDate"
          :rules="[val => !!val || '所选日期不能为空']"
        />
      </f-formitem>
      <!-- 修改原因:再次修改时必填 -->
      <f-formitem
        label="修改原因"
        required
        label-style="width:112px"
        v-if="status === 1"
      >
        <fdev-input
          type="textarea"
          autofocus
          v-model="apply_reason"
          maxlength
          :rules="[
            val => !!val || '修改原因不能为空',
            () => $v.apply_reason.maxText || '输入不能超过50字'
          ]"
        />
      </f-formitem>
      <!-- 提示 -->
      <div>
        <f-icon
          name="alert_r_f"
          class="text-red-4"
          style="width:14px;height:14px"
        />
        <!-- 首次修改 -->
        <span v-if="status === 0" class="tips-desc">
          您可以进行修改定稿日期，下次修改定稿日期需要进行审批
        </span>
        <!-- 再次修改 -->
        <span v-else class="tips-desc">
          您已经进行过定稿日期修改，本次修改需填写修改原因进行提交审批
        </span>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="open = false"/>
        <fdev-btn label="确定" dialog @click="handleChange"
      /></template>
    </f-dialog>
  </div>
</template>
<script>
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { updateFinalDate } from '@/modules/Rqr/services/methods';
import { resolveResponseError, successNotify } from '@/utils/utils';
export default {
  data() {
    return {
      open: false,
      finalDate: moment(new Date()).format('YYYY-MM-DD'),
      apply_reason: null
    };
  },
  props: {
    id: { type: String },
    status: { type: Number } //0:未修改  1:已修改过大于1次  2:无法修改
  },
  validations: {
    finalDate: { required },
    apply_reason: {
      required,
      maxText(val) {
        if (val) {
          return val.length <= 50;
        } else return true;
      }
    }
  },
  methods: {
    // 打开弹窗
    openDilaog() {
      // 值重置
      this.finalDate = moment(new Date()).format('YYYY-MM-DD');
      this.apply_reason = null;
      this.open = true;
    },
    //校验
    handleVerify() {
      // 未选择时间
      if (this.$v.finalDate.$invalid) {
        return false;
      }
      // 再次修改时未填写修改原因
      if (this.status === 1 && this.$v.apply_reason.$invalid) {
        return false;
      }
      return true;
    },
    //修改定稿日期
    async handleChange() {
      // 不满足校验不发接口
      if (!this.handleVerify()) {
        return;
      }
      let params = {
        id: this.id, //需求评估id
        final_date: this.finalDate //修改的定稿日期
      };
      //如果再次修改需要加上申请原因
      if (this.status === 1) {
        params.apply_reason = this.apply_reason;
      }
      // 修改接口
      try {
        await resolveResponseError(() => updateFinalDate(params));
        this.open = false;
        successNotify('操作成功!');
        // 触发列表或详情刷新接口
        this.$emit('reFresh');
      } catch (e) {
        this.open = false;
      }
    }
  }
};
</script>
<style scoped lang="stylus">
.tips-desc
  font-size: 12px;
  font-weight:bold;
  color: #666666;
  line-height: 22px;
</style>
