<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    dense
    title="问题导出"
  >
    <fdev-form>
      <f-formitem label="日期类型" required diaS>
        <fdev-select
          ref="exportModel.dateType"
          :options="dateTypeOptions"
          v-model="exportModel.dateType"
          hint=""
          disable
        />
      </f-formitem>

      <f-formitem label="开始日期" diaS>
        <f-date
          ref="exportModel.startDate"
          mask="YYYY-MM-DD"
          hint=""
          v-model="exportModel.startDate"
          :options="startDateOptions"
        />
      </f-formitem>
      <f-formitem label="结束日期" diaS>
        <f-date
          ref="exportModel.endDate"
          mask="YYYY-MM-DD"
          v-model="exportModel.endDate"
          :options="endDateOptions"
        />
      </f-formitem>
    </fdev-form>

    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog :loading="loading" label="确定" @click="submitForm" />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { exportProblemExcel } from '@/modules/Network/services/methods';
import { resolveResponseError, exportExcel } from '@/utils/utils';
export default {
  name: 'exportDlg',
  data() {
    return {
      exportModel: {
        dateType: '创建日期',
        endDate: '',
        startDate: ''
      },
      dateTypeOptions: [{ label: '创建日期', value: 'createTime' }],

      loading: false,
      time: ''
    };
  },
  props: {
    value: {
      type: Boolean
    },
    orderId: {
      type: String
    }
  },
  watch: {},
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupsData: 'groups'
    })
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('networkForm', ['addMeeting']),
    confirmToClose() {
      this.$emit('close', false);
    },
    endDateOptions(date) {
      if (this.exportModel.startDate)
        return date >= this.exportModel.startDate.replace(/-/g, '/');
      return true;
    },
    startDateOptions(date) {
      if (this.exportModel.endDate) {
        return date <= this.exportModel.endDate.replace(/-/g, '/');
      }
      return true;
    },
    async submitForm() {
      this.loading = true;
      let params = {
        id: this.orderId,
        endDate: this.exportModel.endDate,
        startDate: this.exportModel.startDate
      };
      let res = await resolveResponseError(() => exportProblemExcel(params));
      exportExcel(res);
      this.loading = false;
      this.$emit('close', false);
    }
  },
  created() {},
  mounted() {}
};
</script>
<style lang="stylus" scoped></style>
