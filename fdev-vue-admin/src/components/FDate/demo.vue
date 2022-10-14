<template>
  <div>
    <f-formitem label="单个日期">
      <f-date v-model="date1" disable hint="" />
    </f-formitem>
    <f-formitem label="单个日期" required>
      <f-date v-model="date2" :rules="[val => !!val || '不能为空']" />
    </f-formitem>
    <f-formitem label="单个日期(YYYY/MM/DD)" required>
      <f-date
        v-model="date3"
        mask="YYYY/MM/DD"
        :rules="[val => !!val || '不能为空', val => false || '其他校验不通过']"
      />
    </f-formitem>
    <f-formitem label="日期区间">
      <f-date
        range
        v-model="dateRange1"
        hint=""
        :rules="[val => !!val || '不能为空', val => false || '其他校验不通过']"
      />
    </f-formitem>
    <f-formitem label="日期区间">
      <f-date range v-model="dateRange2" hint="" />
    </f-formitem>
    <f-formitem label="日期区间(YYYY/MM/DD)">
      <f-date range v-model="dateRange3" mask="YYYY/MM/DD" hint="" />
    </f-formitem>
  </div>
</template>

<script>
export default {
  name: 'FDateDemo',
  data() {
    return {
      date1: '2021-08-25',
      date2: '',
      date3: '',
      dateRange1: { from: '', to: '' },
      dateRange2: { from: '2021-08-01', to: '2021-08-15' },
      dateRange3: { from: '2021/08/06', to: '2021/08/26' }
    };
  }
};
</script>

<style></style>
