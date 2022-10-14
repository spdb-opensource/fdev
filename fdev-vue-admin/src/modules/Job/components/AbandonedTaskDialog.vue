<template>
  <f-dialog
    :value="value"
    transition-show="slide-up"
    transition-hide="slide-down"
    class="q-dialog-per"
    @input="$emit('input', $event)"
    title="任务废弃"
  >
    <f-formitem
      label="废弃任务名"
      class="q-pb-md"
      :contracted="!$q.platform.is.desktop"
    >
      <div class="text-right">{{ taskName }}</div>
    </f-formitem>
    <f-formitem
      label="废弃原因"
      class="q-pb-md"
      :label-col="3"
      :contracted="!$q.platform.is.desktop"
    >
      <fdev-input
        v-model="$v.reason.$model"
        ref="reasonModel"
        type="textarea"
        :rules="[() => !$v.reason.$error || '请输入废弃原因']"
      />
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确认"
        @click="confirm"
        :loading="globalLoading['jobForm/abandonTask']"
      />
    </template>
  </f-dialog>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
export default {
  name: 'AbandonedTaskDialog',
  data() {
    return {
      reason: ''
    };
  },
  validations: {
    reason: {
      required
    }
  },
  props: {
    value: Boolean,
    taskName: String,
    id: String,
    release_node_name: String
  },
  watch: {
    value(val, oldVal) {
      this.reason = '';
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    })
  },
  methods: {
    ...mapActions('releaseForm', ['taskChangeNotise']),
    ...mapActions('jobForm', ['abandonTask']),
    async confirm() {
      this.$v.reason.$touch();
      validate([this.$refs.reasonModel]);
      if (this.$v.reason.$invalid) {
        return;
      }
      await this.abandonTask({
        id: this.id,
        discard_reason: this.reason
      });
      successNotify('任务已废弃');
      this.$emit('complete');
      this.$emit('input', false);
      //任务投产文件有变动或废弃通知
      if (this.release_node_name) {
        this.taskChangeNotise({
          release_node_name: this.release_node_name,
          task_id: this.id,
          type: '2'
        });
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog-height
  height 400px!important
</style>
