<template>
  <f-block>
    <div class="bg-white row q-px-md q-pt-md">
      <div class="tip">
        Tip: 只转换带有"$schema": "http://json-schema.org/draft-03/schema"
        的节点，其他节点原样返回!
      </div>
      <fdev-space />

      <fdev-btn-group unelevated>
        <fdev-btn
          label="下载JsonSchema"
          color="blue-6"
          dialog
          @click="download"
          :disable="!jsonSchema4"
        />
        <fdev-btn dialog label="返回" @click="goBack" color="blue-7" />
      </fdev-btn-group>
    </div>
    <div class="bg-white wrapper row">
      <div class="textarea-wrapper col-5">
        <p class="text-h6">JsonSchema draft-3</p>
        <fdev-input
          v-model="jsonSchema3"
          type="textarea"
          input-class="textarea"
          outlined
        />
      </div>
      <div class="textarea-wrapper col">
        <fdev-tooltip
          v-show="!jsonSchema3"
          anchor="top middle"
          self="center middle"
        >
          请输入jsonSchema3
        </fdev-tooltip>
        <fdev-btn
          label="开始转换"
          dialog
          :loading="loading['interfaceForm/convertJsonSchema']"
          @click="handleConvertJsonSchema"
          :disable="!jsonSchema3"
        />
      </div>
      <div class="textarea-wrapper col-5">
        <p class="text-h6">JsonSchema draft-4</p>
        <fdev-input
          v-model="jsonSchema4"
          type="textarea"
          input-class="textarea"
          outlined
          readonly
        />
      </div>
    </div>
  </f-block>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { successNotify } from '@/utils/utils';
export default {
  name: 'SchemaConcersion',
  data() {
    return {
      jsonSchema3: '',
      jsonSchema4: ''
    };
  },
  computed: {
    ...mapState('global', ['loading']),
    ...mapState('interfaceForm', ['jsonSchema'])
  },

  methods: {
    ...mapActions('interfaceForm', ['convertJsonSchema']),
    async handleConvertJsonSchema() {
      try {
        await this.convertJsonSchema({
          changeBeforJson: this.jsonSchema3
        });
        this.jsonSchema4 = this.jsonSchema;
        successNotify('转换成功');
      } catch (error) {
        this.jsonSchema4 = '';
      }
    },
    download() {
      this.$q
        .dialog({
          title: '请输入文件名',
          cancel: true,
          persistent: true,
          prompt: {
            model: 'JsonSchema4',
            type: 'text' // optional
          }
        })
        .onOk(name => {
          const link = document.createElement('a');
          const file = new Blob([this.jsonSchema4], {
            type: 'data:text/plain;charset=utf-8'
          });
          const URL = window.URL.createObjectURL(file);

          link.href = URL;
          link.download = `${name}.json`;
          document.body.appendChild(link);

          link.click();

          window.URL.revokeObjectURL(link.href);
          document.body.removeChild(link);
          successNotify('下载成功！');
        });
    }
  }
};
</script>

<style lang="stylus" scoped>
.textarea-wrapper
  text-align center
.textarea-wrapper >>> .textarea
  min-height calc(100vh - 200px)!important
.wrapper
  align-items center
  padding 16px
>>> .q-btn
  text-transform none
.tip
  line-height 36px
</style>
