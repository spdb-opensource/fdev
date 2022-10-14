<template>
  <div>
    <f-dialog
      :value="value"
      @input="$emit('input', false)"
      right
      @before-show="initDialogData"
      :title="type === 'detail' ? '查看步骤' : '编辑步骤'"
    >
      <step-detail
        :step-info="dialogData"
        :plugin-self-info="pluginInfo"
        :type="type"
        :new-plugin="newPlugin"
        @receive-step-info="getStepInfo"
        @close="closeDialog"
      />
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import StepDetail from './StepDetail';

export default {
  name: 'StepView',
  components: { StepDetail },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: 'detail'
    },
    dialogData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      hasPlugin: false,
      newPlugin: false,
      pluginList: [],
      pluginTypes: [],
      filterKey: [],
      pluginInfo: {}
    };
  },
  computed: {
    ...mapState('configCIForm', ['plugins', 'pluginCategoryInfo']),
    params() {
      const key = this.filterKey;
      const pluginType = '0';
      return {
        pluginType,
        key
      };
    }
  },
  watch: {
    params: {
      deep: true,
      handler(val) {
        this.getPluginList(val);
      }
    }
  },
  methods: {
    ...mapActions('configCIForm', ['queryPlugin', 'queryCategory']),
    closeDialog() {
      this.$emit('input', false);
    },
    async initDialogData() {
      this.hasPlugin = false;
      this.newPlugin = false;
      this.pluginInfo = {};
      await this.queryCategory({ categoryLevel: 'plugin' });
      this.pluginTypes = this.pluginCategoryInfo;
      await this.getPluginList();
    },
    async getPluginList() {
      await this.queryPlugin(this.params);
      this.pluginList = this.plugins;
    },
    getStepInfo(stepInfo) {
      this.$emit('receive-step-info', stepInfo);
    }
  }
};
</script>
