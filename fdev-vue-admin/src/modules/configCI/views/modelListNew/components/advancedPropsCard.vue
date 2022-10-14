<!-- 高级属性查看&编辑抽屉弹窗 -->
<template>
  <f-dialog
    v-model="dialogOpen"
    right
    f-sc
    title="高级属性"
    @before-show="beforeShow"
    @before-close="beforeClose"
  >
    <div class="container scroll-thin">
      <div class="card q-mb-md">
        <div class="bg-blue-1 full-width title-div q-mb-md row">
          <div class="title-icon row justify-center items-center">
            <f-icon
              name="version_s_f"
              class="text-blue-8 icon-size-md"
              style="padding:6px"
            />
          </div>
          <div class="row justify-center items-center">
            {{ proNameCn }}
          </div>
        </div>
        <div class="content">
          <div
            class="advanced"
            v-if="Object.keys(advancedProperties).length > 0"
          >
            <div v-for="(i, index) in advancedProperties.counts" :key="index">
              <div class="advanced-card q-mx-md q-mb-md q-px-md q-pt-md">
                <div
                  v-for="property in advancedProperties.properties"
                  :key="property.nameEn"
                  class="row q-mb-md"
                >
                  <div class="advanced-label row items-center q-mr-md">
                    {{ property.nameCn }}
                  </div>
                  <div class="advanced-value row items-center ">
                    {{ property[`value${index + 1}`] }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template v-slot:btnSlot>
      <fdev-btn label="确定" dialog @click="closeDialog"
    /></template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: 'advancedPropsCard',
  props: {
    value: {
      type: Boolean,
      default: () => false
    },
    env: {
      type: Object,
      default: () => ({
        type: '',
        nameEn: '',
        exist: []
      })
    },
    entityInfo: {
      type: Object,
      default: () => ({})
    },
    arr: {
      type: Array,
      default: () => []
    },
    proNameEn: {
      type: String,
      default: ''
    },
    proNameCn: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      properties: [],
      advancedProperties: []
    };
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'envList',
      'templateDetail',
      'entityModelDetail'
    ]),
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
    ...mapActions('entityManageConfigForm', [
      'queryEnvList',
      'queryTemplateById',
      'addEntityClass',
      'updateEntityClass',
      'queryEntityModelDetail'
    ]),
    beforeShow() {
      this.properties = this.entityInfo.properties;
      const advancedPro = this.properties.find(
        pro => pro.nameEn === this.proNameEn
      );
      const counts = this.arr.length;
      Reflect.set(advancedPro, 'counts', counts);
      for (let i = 0; i < counts; i++) {
        advancedPro.properties.forEach(item => {
          this.$set(item, `value${i + 1}`, this.arr[i][item.nameEn]);
        });
      }
      this.advancedProperties = advancedPro;
    },
    beforeClose() {
      this.properties = [];
      this.type = '';
    },
    closeDialog() {
      this.beforeClose();
      this.dialogOpen = false;
    }
  }
};
</script>
<style lang="stylus" scoped>
.card
  .title-div
    height 54px
    .title-icon
      width 32px
      height 54px
      margin-left 8px
  .advanced-card
    background-color #F7F7F7
    border 1px dashed #BBBBBB
    border-radius 8px
    .advanced-label
      width 180px
      word-wrap break-word
    .advanced-value
      word-wrap break-word
</style>
