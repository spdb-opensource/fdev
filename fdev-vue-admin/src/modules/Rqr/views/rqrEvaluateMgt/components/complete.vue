<template>
  <div>
    <!-- 分析完成弹窗 -->
    <f-dialog title="请选择评估完成日期" v-model="open">
      <f-formitem
        label="评估完成日期"
        label-style="width: 112px"
        class="q-mt-lg"
        hint=""
      >
        <f-date
          v-model="end_assess_date"
          :rules="[val => !!val || '评估完成日期不能为空']"
          :options="dateOptions"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="open = false"/>
        <fdev-btn label="确定" dialog @click="complete"
      /></template>
    </f-dialog>
  </div>
</template>
<script>
import moment from 'moment';
import { confirmFinish } from '@/modules/Rqr/services/methods';
import { resolveResponseError, successNotify } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
export default {
  data() {
    return {
      open: false,
      end_assess_date: moment(new Date()).format('YYYY-MM-DD')
    };
  },
  props: {
    id: { type: String },
    startTime: { type: String }
  },
  validations: {
    end_assess_date: {
      required
    }
  },
  methods: {
    // 打开弹窗
    openDilaog() {
      this.end_assess_date = moment(new Date()).format('YYYY-MM-DD');
      this.open = true;
    },
    //完成评估
    async complete() {
      // 没选时间不发接口
      if (this.$v.end_assess_date.$invalid) {
        return;
      }
      try {
        await resolveResponseError(() =>
          confirmFinish({
            id: this.id,
            end_assess_date: this.end_assess_date
          })
        );
      } catch (e) {
        this.open = false;
      }
      this.open = false;
      successNotify('操作成功!');
      // 触发列表或详情刷新接口
      this.$emit('reFresh');
    },
    //评估完成日期只能选今天或今天以前(范围：起始评估~今天)
    dateOptions(date) {
      const today = moment(new Date()).format('YYYY/MM/DD');
      return (
        date <= today && date >= moment(this.startTime).format('YYYY/MM/DD')
      );
    }
  }
};
</script>
<style scoped></style>
