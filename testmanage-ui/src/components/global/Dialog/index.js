import { Dialog } from 'element-ui';
import Vue from 'vue';
import './Dialog.css';

export default Vue.extend({
  name: 'Dialog',

  data() {
    return {
      dialogOpened: true
    };
  },

  props: {
    title: String,
    visible: Boolean,
    width: {
      type: String,
      default: '520px'
    },
    beforeClose: Function,
    center: Boolean,
    closeOnClickModal: Boolean
  },

  watch: {
    visible(val) {
      if (val) {
        this.$refs.dialog.$children.forEach(item => {
          if (item.resetFields) {
            item.clearValidate();
          }
        });
      }
    }
  },

  methods: {
    handleWrapperClick() {
      this.$confirm('关闭弹窗后数据将会丢失，确认要关闭?', '关闭弹窗', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.handleClose(false);
      });
    },
    handleClose(val) {
      this.$emit('update:visible', val);
    }
  },

  render(h) {
    const on = {
      'update:visible': val => {
        this.handleClose(val);
      }
    };
    return (
      <Dialog
        visible={this.visible}
        {...{ on }}
        title={this.title}
        width={this.width}
        custom-class={this.$slots.footer ? 'dialog have-footer' : 'dialog'}
        center={this.center}
        ref="dialog"
        before-close={this.beforeClose}
        close-on-click-modal={this.closeOnClickModal}
      >
        {this.$slots.default}
        <div slot="footer" class="dialog-footer">
          {this.$slots.footer}
        </div>
      </Dialog>
    );
  },
  mounted() {
    if (!this.closeOnClickModal) {
      this.$refs.dialog.handleWrapperClick = this.handleWrapperClick;
    }
  }
});
