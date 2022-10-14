<template>
  <fdev-dialog
    :value="value"
    transition-show="slide-up"
    transition-hide="slide-down"
    class="dialog"
    @input="$emit('input', $event.value)"
    persistent
  >
    <fdev-layout view="Lhh lpR fff" container class="bg-white">
      <fdev-header bordered class="bg-primary">
        <fdev-toolbar>
          <fdev-toolbar-title>
            实施单元详情
          </fdev-toolbar-title>
          <fdev-btn
            flat
            v-close-popup
            round
            dense
            icon="close"
            @click="hideDialog"
          />
        </fdev-toolbar>
      </fdev-header>
      <fdev-page-container>
        <fdev-card-section>
          <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
            <Description :col="2" label="实施单元实施内容">{{
              implInfo.implement_unit_content
            }}</Description>
            <Description :col="2" label="实施牵头单位">
              {{ implInfo.implement_lead_dept }}
            </Description>
            <Description :col="2" label="涉及系统名称">{{
              implInfo.relate_system_name
            }}</Description>
            <Description :col="2" label="实施牵头团队">
              {{ implInfo.implement_lead_team }}
            </Description>
            <Description :col="2" label="实施牵头人">{{
              implInfo.implement_leader_all | nameFilter
            }}</Description>
            <Description :col="2" label="所属小组">{{
              implInfo.group_cn
            }}</Description>
            <Description :col="2" label="拟纳入项目名称">{{
              implInfo.project_name
            }}</Description>
            <Description :col="2" label="项目编号">{{
              implInfo.project_no
            }}</Description>
            <Description :col="2" label="计划启动开发日期">{{
              implInfo.plan_start_date
            }}</Description>
            <Description :col="2" label="实际启动开发日期">{{
              implInfo.real_start_date
            }}</Description>
            <Description :col="2" label="计划提交内测日期">{{
              implInfo.plan_inner_test_date
            }}</Description>
            <Description :col="2" label="实际提交内测日期">{{
              implInfo.real_inner_test_date
            }}</Description>
            <Description :col="2" label="计划提交业测日期">{{
              implInfo.plan_test_date
            }}</Description>
            <Description :col="2" label="实际提交业测日期">{{
              implInfo.real_test_date
            }}</Description>
            <Description :col="2" label="计划用户测试完成日期">{{
              implInfo.plan_test_finish_date
            }}</Description>
            <Description :col="2" label="实际用户测试完成日期">{{
              implInfo.real_test_finish_date
            }}</Description>
            <Description :col="2" label="计划投产日期">{{
              implInfo.plan_product_date
            }}</Description>
            <Description :col="2" label="实际投产日期">{{
              implInfo.real_product_date
            }}</Description>
            <Description :col="2" label="预期我部人员工作量（人月）">{{
              implInfo.dept_workload
            }}</Description>
            <Description :col="2" label="预期公司人员工作量（人月）">{{
              implInfo.company_workload
            }}</Description>
            <Description :col="2" label="是否进行UI审核">{{
              implInfo.ui_verify | uiFilter
            }}</Description>
            <Description :col="1" label="备注">{{
              implInfo.remark
            }}</Description>
          </div>
        </fdev-card-section>
      </fdev-page-container>
    </fdev-layout>
  </fdev-dialog>
</template>

<script>
import Description from '@/components/Description';
export default {
  name: 'ImplUnitProfile',
  components: { Description },
  data() {
    return {};
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    implInfo: {
      type: Object,
      default: () => {}
    }
  },
  filters: {
    nameFilter(val) {
      if (!val) {
        return;
      }
      if (Array.isArray(val)) {
        if (typeof val[0] === 'object') {
          return val.map(user => user.user_name_cn).join(',');
        } else {
          return val.join(',');
        }
      } else {
        return val.user_name_cn;
      }
    },
    uiFilter(val) {
      return val === false ? '否' : '是';
    }
  },
  methods: {
    hideDialog() {
      this.$emit('input', false);
    }
  }
};
</script>
<style lang="stylus" scoped>


.q-dialog__inner--minimized > div {
    max-width: 1000px;
}

.q-layout-padding {
  padding: 16px 26px 16px 20px;
}

.dialog {
  width: 93%;
}

.dialog-Height
  min-width 1300px
  height 700px

.title {
  font-size: 18px;
}

.card {
  padding: 10px;
}

.select-width {
  max-width: 248px;
}

.row > div
  padding: 10px 15px
  border: 1px
  margin-top :1rem
</style>
