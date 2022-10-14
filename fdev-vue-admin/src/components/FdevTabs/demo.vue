<template>
  <div>
    <div class="row">
      <div>
        <fdev-tabs v-model="tab">
          <fdev-tab name="mails" label="横向tab1" />
          <fdev-tab name="alarms" label="横向tab2" />
          <fdev-tab name="movies" label="横向tab3" />
        </fdev-tabs>
      </div>
      <div>
        <fdev-tabs vertical v-model="tab">
          <fdev-tab name="mails" label="纵向tab1" />
          <fdev-tab name="alarms" label="纵向tab2" />
          <fdev-tab name="movies" label="纵向tab3" />
        </fdev-tabs>
      </div>
    </div>
    <h5 class="text-red">*可以和fdev-splitter、fdev-separator结合使用</h5>
  </div>
</template>

<script>
export default {
  name: 'FdevTabsDemo',
  data() {
    return { tab: 'mails' };
  }
};
</script>

<style></style>
