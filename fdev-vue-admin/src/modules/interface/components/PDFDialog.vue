<template>
  <f-dialog v-model="dialogOpen" right f-sc title="使用规范">
    <div style="width:860px">
      <fdev-tabs align="left" v-model="tab" class="text-grey-7">
        <fdev-tab name="schema" label="REST" />
        <fdev-tab name="SOAP" label="SOAP" />
        <fdev-tab name="SOP" label="SOP" />
        <fdev-tab name="trade" label="交易" />
      </fdev-tabs>
      <fdev-page>
        <fdev-tab-panels v-model="tab">
          <fdev-tab-panel name="schema" class="q-pa-none">
            <Schema />
          </fdev-tab-panel>
          <fdev-tab-panel name="SOAP" class="q-pa-none">
            <SOAP />
          </fdev-tab-panel>
          <fdev-tab-panel name="SOP" class="q-pa-none">
            <SOP />
          </fdev-tab-panel>
          <fdev-tab-panel name="trade" class="q-pa-none">
            <Trade />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </fdev-page>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn dialog label="确认" @click="closeDialog" />
    </template>
  </f-dialog>
</template>

<script>
import Schema from '../components/Schema';
import SOAP from '../components/SOAP';
import SOP from '../components/SOP';
import Trade from '../components/Trade';

export default {
  name: 'PDFDialog',
  components: { Schema, SOAP, SOP, Trade },
  props: ['value'],
  data() {
    return {
      tab: 'schema'
    };
  },
  computed: {
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
    // 关闭弹窗
    closeDialog() {
      this.dialogOpen = false;
    }
  }
};
</script>
